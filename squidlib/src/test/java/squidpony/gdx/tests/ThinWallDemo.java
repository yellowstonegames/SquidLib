package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.panel.IColoredString;
import squidpony.squidai.CustomDijkstraMap;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidgrid.mapping.ThinDungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

import static squidpony.squidgrid.mapping.ThinDungeonGenerator.*;

/**
 * This should look like http://i.imgur.com/FrbsCul.png at runtime.
 * Other comments are not updated yet; this started in RotationDemo, and is similar to it, but that demo wasn't fully
 * updated in its comments from EverythingDemo.
 */
public class ThinWallDemo extends ApplicationAdapter {
    private enum Phase {WAIT, PLAYER_ANIM, MONSTER_ANIM}

    private static class Creature {
        public AnimatedEntity entity;
        public int state, pos;

        public Creature(AnimatedEntity ae, int pos, int state) {
            entity = ae;
            this.state = state;
            move(pos);
        }

        public Creature change(int state) {
            this.state = state;
            return this;
        }

        public Creature change(AnimatedEntity ae) {
            entity = ae;
            return this;
        }

        public Creature move(int pos) {
            this.pos = pos;
            return this;
        }
    }

    SpriteBatch batch;

    private Phase phase = Phase.WAIT;
    private StatefulRNG rng;
    private SquidLayers display;
    private SquidMessageBox messages;
    /**
     * Non-{@code null} iff '?' was pressed before
     */
    private /*Nullable*/ Actor help;
    private ThinDungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] lights;
    private Color[][] colors, bgColors;
    private double[][] fovmap, monfovmap;
    private Creature player;
    private FOV fov, monfov;
    public static final int INTERNAL_ZOOM = 1;

    /**
     * In number of cells
     */
    private static final int width = 80;
    /**
     * In number of cells
     */
    private static final int height = 30;

    private static final int overlapWidth = width * 2 - 1;

    private static final int overlapHeight = height * 2 - 1;

    private static final int messageHeight = 4;
    /**
     * The pixel width of a cell
     */
    private static final int cellWidth = 12 * INTERNAL_ZOOM;
    /**
     * The pixel height of a cell
     */
    private static final int cellHeight = 24 * INTERNAL_ZOOM;
    private VisualInput input;
    private boolean[][] seen;
    private int health = 9;
    private SquidColorCenter fgCenter, bgCenter;
    private Color bgColor;
    private ArrayList<Color> playerMarkColors, monsterMarkColors;
    private OrderedMap<Integer, Creature> monsters;
    private GreasedRegion floors;
    private CustomDijkstraMap getToPlayer, playerToCursor;
    private Stage stage;
    private int framesWithoutAnimation = 0;
    private IntVLA toCursor, awaitedMoves;
    private String lang;
    private TextCellFactory textFactory;
    private Viewport viewport;
    private float currentZoomX = INTERNAL_ZOOM, currentZoomY = INTERNAL_ZOOM;

    public static final Adjacency adjacency = new Adjacency.ThinWallAdjacency(overlapWidth, overlapHeight, DijkstraMap.Measurement.EUCLIDEAN);
    @Override
    public void create() {
        // gotta have a random number generator. We can pass a seed as a long or String to an RNG.
        rng = new StatefulRNG(0xBADBEEFB0BBL);

        fgCenter = new SquidColorCenter(new Filters.SaturationValueFilter(1.3f, 0.95f));
        bgCenter = new SquidColorCenter(new Filters.SaturationValueFilter(0.8f, 0.8f));
        playerMarkColors = fgCenter.rainbow(64);
        monsterMarkColors = fgCenter.rainbow(0.75f, 0.65f, 64);
        //playerMarkColors = fgCenter.loopingGradient(SColor.HAN_PURPLE, SColor.PSYCHEDELIC_PURPLE, 64);
        //monsterMarkColors = fgCenter.loopingGradient(SColor.CRIMSON, SColor.ORANGE_RED, 64);
        batch = new SpriteBatch();

        // getStretchableFont loads a font from the assets, Inconsolata-LGC-Custom (a distance field font as mentioned
        // earlier). We set the smoothing multiplier on it only if we are using internal zoom to increase sharpness
        // on small details, but if the smoothing is incorrect some sizes look blurry or over-sharpened. This can be set
        // manually if you use a constant internal zoom; here we use 1f for internal zoom 1, about 2/3f for zoom 2, and
        // about 1/2f for zoom 3. If you have more zooms as options for some reason, this formula should hold for many
        // cases but probably not all. We also add a swap here to replace '#', which is used for boulders and pillars,
        // with a small Unicode box shape that looks better when it lies on the corner between walkable cells.
        textFactory = DefaultResources.getStretchableSlabFont()
                .width(cellWidth).height(cellHeight).initBySize().addSwap('#', '■'); //.setDirectionGlyph('ˆ')
        // Creates a layered series of text grids in a SquidLayers object, using the previously set-up textFactory and
        // SquidColorCenters. We use the bitwise right shift operator instead of division by 2 because I want more
        // people to know how to use bitwise operators; you can replace ">>1" with "/2" with no change in meaning.
        display = new SquidLayers(overlapWidth, overlapHeight, (cellWidth>>1), (cellHeight>>1),
                textFactory.copy(), bgCenter, fgCenter);
        display.getTextFactory().width(cellWidth).height(cellHeight).initBySize();
        display.getBackgroundLayer().setOnlyRenderEven(true);

        display.setAnimationDuration(0.1f);
        messages = new SquidMessageBox(width, messageHeight,
                textFactory.copy());

        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        messages.setTextSize(cellWidth * 1.1f, cellHeight * 1.1f);
        display.setTextSize(cellWidth * 1.05f, cellHeight * 1.05f);
        //The subCell SquidPanel uses a smaller size here; the numbers 8 and 16 should change if cellWidth or cellHeight
        //change, and the INTERNAL_ZOOM multiplier keeps things sharp, the same as it does all over here.
        viewport = new StretchViewport(width * cellWidth, (height + messageHeight + 1) * cellHeight);
        stage = new Stage(viewport, batch);

        //These need to have their positions set before adding any entities if there is an offset involved.
        messages.setBounds(0, 0, cellWidth * width, cellHeight * (messageHeight+1));
        display.setPosition(0, messages.getHeight());
        messages.appendWrappingMessage("You are the purple '@', and enemies are red. Click/tap a cell to move. " +
                "Use ? for help, q to quit. " +
                "Click the top or bottom border of this box to scroll.");

        dungeonGen = new ThinDungeonGenerator(width, height, rng, ROOM_WALL_RETRACT, CORRIDOR_WALL_RETRACT, CAVE_WALL_RETRACT);
        //dungeonGen = new ThinDungeonGenerator(width, height, rng, ROOM_WALL_EXPAND, CORRIDOR_WALL_EXPAND, CAVE_WALL_EXPAND);
        dungeonGen.addWater(0, 25, 6);
        dungeonGen.addGrass(0, 20);
        dungeonGen.addBoulders(0, 7);
        dungeonGen.addDoors(38, false);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng);
        //serpent.putCaveCarvers(3);
        serpent.putWalledBoxRoomCarvers(3);
        serpent.putWalledRoundRoomCarvers(2);
        //char[][] mg = serpent.generate();
        //decoDungeon = dungeonGen.generate(mg, serpent.getEnvironment());
        dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        lineDungeon = DungeonUtility.hashesToLines(dungeonGen.getDungeon(), true);
        bareDungeon = dungeonGen.getBareDungeon();
        dungeonGen.removeHardCorners();
        decoDungeon = ArrayTools.copy(dungeonGen.getDungeon());
        //lineDungeon = dungeonGen.getDungeon();
        /*
        decoDungeon = new char[][]{
                {'#','#','#','#',},
                {'#','.','.','#',},
                {'#','.','.','#',},
                {'#','#','#','#',},
        };
        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = new char[][]{
                {'#','#','#','#',},
                {'#','.','.','#',},
                {'#','.','.','#',},
                {'#','#','#','#',},
        };
        lineDungeon  = new char[][]{
                {'#','#','#','#',},
                {'#','.','.','#',},
                {'#','.','.','#',},
                {'#','#','#','#',},
        };
        lineDungeon = DungeonUtility.hashesToLines(lineDungeon);
        */

        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        final GreasedRegion placement = new GreasedRegion(decoDungeon, '.');
        floors = placement.copy();
        final Coord pl = placement.singleRandom(rng).makeEven();
        placement.remove(pl);
        int numMonsters = 25;
        monsters = new OrderedMap<>(numMonsters);
        int p;

        for (int i = 0; i < numMonsters; i++) {
            Coord monPos = placement.singleRandom(rng).makeEven();
            placement.remove(monPos);
            p = adjacency.composite(monPos.x, monPos.y, 0, 0);
            monsters.put(p, new Creature(display.animateActor(monPos.x, monPos.y, 'Я',
                    SColor.SCARLET),
                    p,
                    0));
        }
        // your choice of FOV matters here.
        fov = new FOV(FOV.SHADOW);
        monfov = new FOV(FOV.SHADOW);
        res = DungeonUtility.generateResistances(lineDungeon);
        fovmap = fov.calculateFOV(res, pl.x, pl.y, 12, Radius.SQUARE);
        getToPlayer = new CustomDijkstraMap(decoDungeon, adjacency, rng);
        getToPlayer.setGoal(adjacency.composite(pl.x, pl.y, 0, 0));

        player = new Creature(display.animateActor(pl.x, pl.y, '@', SColor.PSYCHEDELIC_PURPLE, false),
                adjacency.composite(pl.x, pl.y, 0, 0), health);
        /*
        player = new Creature(display.animateActor(1, 1, ' ', SColor.HAN_PURPLE, false),
                display.directionMarker(pl.x, pl.y, SColor.HAN_PURPLE, 3, false),
                adjacency.composite(1, 1, 4, 0), health);
        */
//                fgCenter.filter(display.getPalette().get(30)));
        toCursor = new IntVLA(10);
        awaitedMoves = new IntVLA(10);
        playerToCursor = new CustomDijkstraMap(decoDungeon, adjacency, rng);
        bgColor = SColor.DARK_SLATE_GRAY;

        colors = MapUtility.generateDefaultColors(lineDungeon);
        bgColors = MapUtility.generateDefaultBGColors(lineDungeon);
        lights = MapUtility.generateLightnessModifiers(decoDungeon, (System.currentTimeMillis() & 0xFFFFFFFFL) * 0.013);

        seen = new boolean[overlapWidth][overlapHeight];
        lang = FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 4, 6, new String[]{",", ",", ",", " -"},
                new String[]{"..."}, 0.25);
        // this is a big one.
        // SquidInput can be constructed with a KeyHandler (which just processes specific keypresses), a SquidMouse
        // (which is given an InputProcessor implementation and can handle multiple kinds of mouse move), or both.
        // keyHandler is meant to be able to handle complex, modified key input, typically for games that distinguish
        // between, say, 'q' and 'Q' for 'quaff' and 'Quip' or whatever obtuse combination you choose. The
        // implementation here handles hjklyubn keys for 8-way movement, numpad for 8-way movement, arrow keys for
        // 4-way movement, and wasd for 4-way movement. Shifted letter keys produce capitalized chars when passed to
        // KeyHandler.handle(), but we don't care about that so we just use two case statements with the same body,
        // one for the lower case letter and one for the upper case letter.
        // You can also set up a series of future moves by clicking within FOV range, using mouseMoved to determine the
        // path to the mouse position with a DijkstraMap (called playerToCursor), and using touchUp to actually trigger
        // the event when someone clicks.
        input = new VisualInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W': {
                        move(adjacency.move(player.pos, 0, -2));
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S': {
                        move(adjacency.move(player.pos, 0, 2));
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A': {
                        move(adjacency.move(player.pos, -2, 0));
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D': {
                        move(adjacency.move(player.pos, 2, 0));
                        break;
                    }

                    case SquidInput.UP_LEFT_ARROW:
                    case 'y':
                    case 'Y': {
                        move(adjacency.move(player.pos, -2, -2));
                        break;
                    }
                    case SquidInput.UP_RIGHT_ARROW:
                    case 'u':
                    case 'U': {
                        move(adjacency.move(player.pos, 2, -2));
                        break;
                    }
                    case SquidInput.DOWN_RIGHT_ARROW:
                    case 'n':
                    case 'N': {
                        move(adjacency.move(player.pos, 2, 2));
                        break;
                    }
                    case SquidInput.DOWN_LEFT_ARROW:
                    case 'b':
                    case 'B': {
                        move(adjacency.move(player.pos, -2, 2));
                        break;
                    }
                    case '?': {
                        toggleHelp();
                        break;
                    }
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                        break;
                    }
                }
            }
        }, new SquidMouse(cellWidth, cellHeight, width, height, cellWidth >> 1, cellHeight >> 1, new InputAdapter() {

            // if the user clicks within FOV range and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                screenX *= 2;
                screenY *= 2;
                if (fovmap[screenX][screenY] > 0.0 && awaitedMoves.size == 0) {
                    if (toCursor.size == 0) {
                        //Uses DijkstraMap to get a path from the player's position to the cursor
                        toCursor = playerToCursor.findPath(30, null, null, player.pos, adjacency.composite(screenX&-2, screenY&-2, 0, 0));
                    }
                    awaitedMoves.clear();
                    awaitedMoves.addAll(toCursor);
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return mouseMoved(screenX, screenY);
            }

            // causes the path to the mouse position to become highlighted (toCursor contains a list of points that
            // receive highlighting). Uses DijkstraMap.findPath() to find the path, which is surprisingly fast.
            @Override
            public boolean mouseMoved(int screenX, int screenY) {

                if (awaitedMoves.size != 0)
                    return false;
                screenX *= 2;
                screenY *= 2;
                if (fovmap[screenX][screenY] > 0.0) {
                    //Uses DijkstraMap to get a path. from the player's position to the cursor
                    toCursor = playerToCursor.findPath(30, null, null, player.pos, adjacency.composite(screenX&-2, screenY&-2, 0, 0));
                }

                return false;
            }
        }));
        //set this to true to test visual input on desktop
        input.forceButtons = false;
        //actions to give names to in the visual input menu
        input.init("filter", "??? help?", "quit");
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        // and then add display and messages, our two visual components, to the list of things that act in Stage.
        stage.addActor(display);
        // stage.addActor(subCell); // this is not added since it is manually drawn after other steps
        stage.addActor(messages);
        viewport = input.resizeInnerStage(stage);
    }

    /**
     * Move the player or open closed doors, remove any monsters the player bumped, then update the DijkstraMap and
     * have the monsters that can see the player try to approach.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     *
     * @param pos
     */
    private void move(int pos) {
        clearHelp();

        if (health <= 0) return;
        int currentX = player.entity.gridX, currentY = player.entity.gridY,
                newX = adjacency.extractX(pos), newY = adjacency.extractY(pos);
        if (newX >= 0 && newY >= 0 && newX < overlapWidth && newY < overlapHeight
                && bareDungeon[newX][newY] != '#') {
            // '+' is a closed door. '/' is an open door.
            if (lineDungeon[newX][newY] == '+') {
                decoDungeon[newX][newY] = '/';
                lineDungeon[newX][newY] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(lineDungeon);
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, currentX, currentY, 12, Radius.SQUARE);
            } else {
                if(lineDungeon[newX+1][newY] == '+' || lineDungeon[newX-1][newY] == '+'
                        || lineDungeon[newX][newY+1] == '+' || lineDungeon[newX][newY-1] == '+') {
                    if (lineDungeon[newX + 1][newY] == '+') {
                        decoDungeon[newX + 1][newY] = '/';
                        lineDungeon[newX + 1][newY] = '/';
                    }
                    if (lineDungeon[newX - 1][newY] == '+') {
                        decoDungeon[newX - 1][newY] = '/';
                        lineDungeon[newX - 1][newY] = '/';
                    }
                    if (lineDungeon[newX][newY + 1] == '+') {
                        decoDungeon[newX][newY + 1] = '/';
                        lineDungeon[newX][newY + 1] = '/';
                    }
                    if (lineDungeon[newX][newY - 1] == '+') {
                        decoDungeon[newX][newY - 1] = '/';
                        lineDungeon[newX][newY - 1] = '/';
                    }
                    res = DungeonUtility.generateResistances(lineDungeon);
                }
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, newX, newY, 12, Radius.SQUARE);
                //player.marker.setDirection(Direction.CLOCKWISE[adjacency.extractR(pos)]);
                display.slide(player.entity, newX, newY);
                player.move(pos);
                if(monsters.remove(pos) != null)
                {
                    //display.addAction(new PanelEffect.ExplosionEffect(display.getForegroundLayer(), 1f, floors.refill(res, 0.99), Coord.get(newX, newY), 9));
                    display.getForegroundLayer().burst(newX, newY, 1, true, '\'',
                            SColor.BLOOD, SColor.BLOOD.cpy().sub(0,0,0,1), false, 1f, 1f);
                    // the last statement is effectively equivalent to:
                    /*
                    Direction d;
                    for (int i = 0; i < 8; i++) {
                        d = Direction.CLOCKWISE[i];
                        display.getForegroundLayer().summon(newX, newY, newX-d.deltaX, newY+d.deltaY,
                                '\'', SColor.BLOOD, SColor.BLOOD.cpy().sub(0,0,0,1f),
                                45f * i, 45f * (i-1),
                                1f);
                    }*/
                    // but it's a lot nicer to just call burst() when you want the radiating-out effect!
                }
            }
            phase = Phase.PLAYER_ANIM;
        }
    }

    // check if a monster's movement would overlap with another monster.
    private boolean checkOverlap(Creature mon, int x, int y, ArrayList<Coord> futureOccupied) {
        int comp = adjacency.composite(x, y, 0, 0);
        if (monsters.containsKey(comp) && !mon.equals(monsters.get(comp)))
            return true;
        for (Coord p : futureOccupied) {
            if (x == p.x && y == p.y)
                return true;
        }
        return false;
    }

    private void postMove() {

        phase = Phase.MONSTER_ANIM;
        // The next two lines are important to avoid monsters treating cells the player WAS in as goals.
        getToPlayer.clearGoals();
        getToPlayer.resetMap();
        // now that goals are cleared, we can mark the current player position as a goal.
        // this is an important piece of DijkstraMap usage; the argument is a Set of Points for squares that
        // temporarily cannot be moved through (not walls, which are automatically known because the map char[][]
        // was passed to the DijkstraMap constructor, but things like moving creatures and objects).
        int[] monplaces = new int[monsters.size()];
        for (int i = 0; i < monplaces.length; i++) {
            monplaces[i] = monsters.getAt(i).pos;
        }

        //pathMap = getToPlayer.scan(monplaces);

        // recalculate FOV, store it in monster fovmap for the monster to use.
        monfovmap = monfov.calculateFOV(res, player.entity.gridX & -2, player.entity.gridY & -2, 12, Radius.SQUARE);
        // handle monster turns
        Creature mon;
        int ms = monsters.size(), tmp;
        IntVLA impassable = new IntVLA(ms), path;
        for (int i = 0; i < ms; i++) {
            impassable.add(monsters.getAt(i).pos);
        }
        int[] playerGoal = new int[]{player.pos};
        for (Integer pos : monsters.keySet()) {
            mon = monsters.get(pos);
            if(mon == null)
                continue;
            // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
            if (mon.state > 0 || monfovmap[adjacency.extractX(pos)][adjacency.extractY(pos)] > 0.1) {
                if (mon.state == 0) {
                    messages.appendMessage("The AЯMED GUAЯD shouts at you, \"" +
                            FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 1, 3,
                                    new String[]{",", ",", ",", " -"}, new String[]{"!"}, 0.25) + "\"");
                }

                path = getToPlayer.findPath(30, impassable, null, mon.pos, playerGoal);
                if(path.size == 0) {
                    mon.change(1);
                    continue;
                }
                tmp = path.first();
                if (tmp == player.pos) {
                    display.tint(player.entity.gridX, player.entity.gridY, SColor.PURE_CRIMSON, 0, 0.415f);
                    health--;
                    //player.setText("" + health);
                    mon.change(1);
                }
                // otherwise store the new position in newMons.
                else {
                    mon.change(1);
                    monsters.alter(pos, tmp);
                    display.slide(mon.entity, adjacency.extractX(tmp), adjacency.extractY(tmp));
                    mon.move(tmp);
                    impassable.add(tmp);
                }

                // this block is used to ensure that the monster picks the best path, or a random choice if there
                // is more than one equally good best option.
                /*
                Direction choice = null;
                double best = 9999.0;
                Direction[] ds = new Direction[8];
                rng.shuffle(Direction.OUTWARDS, ds);
                for (Direction d : ds) {
                    Coord tmp = pos.translate(d);
                    if (pathMap[tmp.x][tmp.y] < best &&
                            !checkOverlap(mon, tmp.x, tmp.y, nextMovePositions)) {
                        // pathMap is a 2D array of doubles where 0 is the goal (the player).
                        // we use best to store which option is closest to the goal.
                        best = pathMap[tmp.x][tmp.y];
                        choice = d;
                    }
                }
                */
            }
        }

    }

    private void toggleHelp() {
        if (help != null) {
            clearHelp();
            return;
        }
        final int nbMonsters = monsters.size();

		/* Prepare the String to display */
        final IColoredString<Color> cs = new IColoredString.Impl<>();
        cs.append("Still ", null);
        final Color nbColor;
        if (nbMonsters <= 1)
            /* Green */
            nbColor = Color.GREEN;
        else if (nbMonsters <= 5)
            /* Orange */
            nbColor = Color.ORANGE;
        else
            /* Red */
            nbColor = Color.RED;
        cs.appendInt(nbMonsters, nbColor);
        cs.append(" monster" + (nbMonsters == 1 ? "" : "s") + " to kill", null);

        IColoredString<Color> helping1 = new IColoredString.Impl<>("Use numpad or vi-keys (hjklyubn) to move.", Color.WHITE);
        IColoredString<Color> helping2 = new IColoredString.Impl<>("Use ? for help, f to change colors, q to quit.", Color.WHITE);
        IColoredString<Color> helping3 = new IColoredString.Impl<>("Click the top or bottom border of the lower message box to scroll.", Color.WHITE);

        /* Some grey color */
        final Color bgColor = new Color(0.3f, 0.3f, 0.3f, 0.9f);

        final Actor a;
            /*
			 * Use TextPanel. There's less work to do than with
			 * GroupCombinedPanel, and we can use a more legible variable-width font.
			 * It doesn't seem like it when reading this code, but this actually does
			 * much more than GroupCombinedPanel,  because we do line wrapping and
			 * justifying, without having to worry about sizes since TextPanel lays
			 * itself out.
			 */
        final TextPanel<Color> tp = new TextPanel<Color>(GDXMarkup.instance, DefaultResources.getStretchablePrintFont());
        tp.backgroundColor = SColor.DARK_SLATE_GRAY;

        final List<IColoredString<Color>> text = new ArrayList<>();
        text.add(cs);
			/* No need to call IColoredString::wrap, TextPanel does it on its own */
        text.add(helping1);
        text.add(helping2);
        text.add(helping3);

        final float w = width * cellWidth, aw = helping3.length() * cellWidth * 0.8f * INTERNAL_ZOOM;
        final float h = height * cellHeight, ah = cellHeight * 9f * INTERNAL_ZOOM;
        tp.init(aw, ah, text);
        a = tp.getScrollPane();
        final float x = (w - aw) / 2f;
        final float y = (h - ah) / 2f;
        a.setPosition(x, y);
        stage.setScrollFocus(a);

        help = a;

        stage.addActor(a);
    }

    private void clearHelp() {
        if (help == null)
			/* Nothing to do */
            return;
        help.clear();
        stage.getActors().removeValue(help, true);
        help = null;
    }

    public void putMap() {
        boolean overlapping;
        for (int i = 0; i < overlapWidth; i++) {
            for (int j = 0; j < overlapHeight; j++) {
                overlapping = monsters.containsKey(adjacency.composite(i, j, 0, 0)) || (player.entity.gridX == i && player.entity.gridY == j);
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +215 lightness and 0.0 being rather dark at -105.
                if (fovmap[i][j] > 0.0) {
                    seen[i][j] = true;
                    display.put(i, j, (overlapping) ? ' ' : lineDungeon[i][j], colors[i][j], bgColors[i][j],
                            lights[i][j] + (int) (-35 + 150 * fovmap[i][j]));
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                } else {// if (seen[i][j]) {
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], -150);
                }
            }
        }
        int pt, x, y;
        for (int p = 0; p < toCursor.size; p++) {
            pt = toCursor.get(p);
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.highlight(x = adjacency.extractX(pt), y = adjacency.extractY(pt), lights[x][y] + (int) (170 * fovmap[x][y]));
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        //Gdx.gl.glEnable(GL20.GL_BLEND);

        // this does the standard lighting for walls, floors, etc. but also uses the time to do the Simplex noise thing.
        MapUtility.fillLightnessModifiers(lights, decoDungeon, (System.currentTimeMillis() & 0xFFFFFFFFL) * 0.013);

        // you done bad. you done real bad.
        if (health <= 0) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(width / 2 - 18, height / 2 - 10, "   THE TSAR WILL HAVE YOUR HEAD!    ");
            display.putBoxedString(width / 2 - 18, height / 2 - 5, "      AS THE OLD SAYING GOES,       ");
            display.putBoxedString(width / 2 - lang.length() / 2, height / 2, lang);
            display.putBoxedString(width / 2 - 18, height / 2 + 5, "             q to quit.             ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if (input.hasNext())
                input.next();
            return;
        }
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if (awaitedMoves.size != 0) {

            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            if (!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 2) {
                    framesWithoutAnimation = 0;
                    switch (phase) {
                        case WAIT:
                        case MONSTER_ANIM:
                            int m = awaitedMoves.removeIndex(0);
                            toCursor.removeIndex(0);
                            move(m);
                            break;
                        case PLAYER_ANIM:
                            postMove();
                            break;
                    }
                }
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if (input.hasNext() && !display.hasActiveAnimations() && phase == Phase.WAIT) {
            input.next();
        }
        // if the previous blocks didn't happen, and there are no active animations, then either change the phase
        // (because with no animations running the last phase must have ended), or start a new animation soon.
        else if (!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 3) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case WAIT:
                        break;
                    case MONSTER_ANIM: {
                        phase = Phase.WAIT;
                    }
                    break;
                    case PLAYER_ANIM: {
                        postMove();

                    }
                }
            }
        }
        // if we do have an animation running, then how many frames have passed with no animation needs resetting
        else {
            framesWithoutAnimation = 0;
        }

        input.show();
        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.getViewport().apply(true);
        stage.draw();
        stage.act();

        if (help == null) {
            // display does not draw all AnimatedEntities by default, since FOV often changes how they need to be drawn.
            batch.begin();
            // the player needs to get drawn every frame, of course.
            display.drawActor(batch, 1.0f, player.entity);
            int mSize = monsters.size();
            Creature mon;
            for (int m = 0; m < mSize; m++) {
                mon = monsters.getAt(m);
                // monsters are only drawn if within FOV.
                if (fovmap[mon.entity.gridX][mon.entity.gridY] > 0.0) {
                    display.drawActor(batch, 1.0f, mon.entity);
                }
            }
            messages.put(width >> 1, 0, Character.forDigit(health, 10), SColor.DARK_PINK);
            // batch must end if it began.
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // message box won't respond to clicks on the far right if the stage hasn't been updated with a larger size
        currentZoomX = width * 1f / ThinWallDemo.width;
        // total new screen height in pixels divided by total number of rows on the screen
        currentZoomY = height * 1f / (ThinWallDemo.height + messages.getGridHeight());
        // message box should be given updated bounds since I don't think it will do this automatically
        messages.setBounds(0, 0, width, currentZoomY * messages.getGridHeight());
        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
        input.reinitialize(currentZoomX, currentZoomY, ThinWallDemo.width, ThinWallDemo.height, Math.round(cellWidth * 0.5f), Math.round(cellWidth * 0.5f), width, height);
        currentZoomX = cellWidth / currentZoomX;
        currentZoomY = cellHeight / currentZoomY;
        input.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Thin Wall Dungeon and Pathfinding";
        config.width = width * cellWidth / INTERNAL_ZOOM;
        config.height = (height+messageHeight+1) * cellHeight / INTERNAL_ZOOM;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new ThinWallDemo(), config);
    }

}
