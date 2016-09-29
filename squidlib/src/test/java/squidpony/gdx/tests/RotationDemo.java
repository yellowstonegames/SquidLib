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
import squidpony.FakeLanguageGen;
import squidpony.panel.IColoredString;
import squidpony.squidai.CustomDijkstraMap;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.MixedGenerator;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

public class RotationDemo extends ApplicationAdapter {
    private enum Phase {WAIT, PLAYER_ANIM, MONSTER_ANIM}

    private static class Creature {
        public AnimatedEntity entity, marker;
        public int state, pos;

        public Creature(AnimatedEntity ae, AnimatedEntity marker, int pos, int state) {
            entity = ae;
            this.marker = marker;
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
            entity.gridX = adjacency.extractX(pos);
            entity.gridY = adjacency.extractY(pos);
            marker.gridX = entity.gridX;
            marker.gridY = entity.gridY;
            this.pos = pos;
            marker.setDirection(adjacency.directions[adjacency.extractR(pos)]);
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
    private SectionDungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] lights;
    private Color[][] colors, bgColors;
    private double[][] fovmap;
    private double[] pathMap;
    private Creature player;
    private FOV fov;
    public static final int INTERNAL_ZOOM = 1;

    /**
     * In number of cells
     */
    private static final int width = 90;
    /**
     * In number of cells
     */
    private static final int height = 30;
    /**
     * The pixel width of a cell
     */
    private static final int cellWidth = 13 * INTERNAL_ZOOM;
    /**
     * The pixel height of a cell
     */
    private static final int cellHeight = 26 * INTERNAL_ZOOM;
    private VisualInput input;
    private double counter;
    private boolean[][] seen;
    private int health = 7;
    private SquidColorCenter fgCenter, bgCenter;
    private Color bgColor;
    private ArrayList<Color> playerMarkColors, monsterMarkColors;
    private OrderedMap<Integer, Creature> monsters;
    private CustomDijkstraMap getToPlayer, playerToCursor;
    private Stage stage;
    private int framesWithoutAnimation = 0;
    private IntVLA toCursor, awaitedMoves;
    private String lang;
    private SquidColorCenter[] colorCenters;
    private int currentCenter;
    private boolean changingColors = false;
    private TextCellFactory textFactory;
    private Viewport viewport;
    private float currentZoomX = INTERNAL_ZOOM, currentZoomY = INTERNAL_ZOOM;

    public static final Adjacency adjacency = new Adjacency.RotationAdjacency(width, height, DijkstraMap.Measurement.EUCLIDEAN);
    @Override
    public void create() {
        // gotta have a random number generator. We seed a LightRNG with any long we want, then pass that to an RNG.
        rng = new StatefulRNG(0xBADBEEFB0BBL);

        // for demo purposes, we allow changing the SquidColorCenter and the filter effect associated with it.
        // next, we populate the colorCenters array with the SquidColorCenters that will modify any colors we request
        // of them using the filter we specify. Only one SquidColorCenter will be used at any time for foreground, and
        // sometimes another will be used for background.
        colorCenters = new SquidColorCenter[18];
        // MultiLerpFilter here is given two colors to tint everything toward one of; this is meant to reproduce the
        // "Hollywood action movie poster" style of using primarily light orange (explosions) and gray-blue (metal).

        colorCenters[0] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.GAMBOGE_DYE, SColor.COLUMBIA_BLUE},
                new float[]{0.25f, 0.2f}
        ));
        colorCenters[1] = colorCenters[0];

        // MultiLerpFilter here is given three colors to tint everything toward one of; this is meant to look bolder.

        colorCenters[2] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.RED_PIGMENT, SColor.MEDIUM_BLUE, SColor.LIME_GREEN},
                new float[]{0.2f, 0.25f, 0.25f}
        ));
        colorCenters[3] = colorCenters[2];

        // ColorizeFilter here is given a slightly-grayish dark brown to imitate a sepia tone.

        colorCenters[4] = new SquidColorCenter(new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.7f, -0.05f));
        colorCenters[5] = new SquidColorCenter(new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.65f, 0.07f));

        // HallucinateFilter makes all the colors very saturated and move even when you aren't doing anything.

        colorCenters[6] = new SquidColorCenter(new Filters.HallucinateFilter());
        colorCenters[7] = colorCenters[6];

        // SaturationFilter here is used to over-saturate the colors slightly. Background is less saturated.

        colorCenters[8] = new SquidColorCenter(new Filters.SaturationFilter(1.35f));
        colorCenters[9] = new SquidColorCenter(new Filters.SaturationFilter(1.15f));

        // SaturationFilter here is used to de-saturate the colors slightly. Background is less saturated.

        colorCenters[10] = new SquidColorCenter(new Filters.SaturationFilter(0.7f));
        colorCenters[11] = new SquidColorCenter(new Filters.SaturationFilter(0.5f));

        // WiggleFilter here is used to randomize the colors slightly.

        colorCenters[12] = new SquidColorCenter(new Filters.WiggleFilter());
        colorCenters[13] = colorCenters[12];

        // SaturationFilter here is used to de-saturate the colors slightly. Background is less saturated.

        colorCenters[14] = new SquidColorCenter(new Filters.PaletteFilter(SColor.BLUE_GREEN_SERIES));
        colorCenters[15] = new SquidColorCenter(new Filters.PaletteFilter(SColor.ACHROMATIC_SERIES));

        colorCenters[16] = DefaultResources.getSCC();
        colorCenters[17] = colorCenters[16];

        fgCenter = colorCenters[16];
        bgCenter = colorCenters[17];
        currentCenter = 8;
        playerMarkColors = fgCenter.loopingGradient(SColor.BRIGHT_GREEN, SColor.LIGHT_YELLOW_DYE, 64);
        monsterMarkColors = fgCenter.loopingGradient(SColor.CRIMSON, SColor.ATOMIC_TANGERINE, 64);
        batch = new SpriteBatch();

        // getStretchableFont loads an embedded font, Inconsolata-LGC-Custom, that is a distance field font as mentioned
        // earlier. We set the smoothing multiplier on it only because we are using internal zoom to increase sharpness
        // on small details, but if the smoothing is incorrect some sizes look blurry or over-sharpened. This can be set
        // manually if you use a constant internal zoom; here we use 1f for internal zoom 1, about 2/3f for zoom 2, and
        // about 1/2f for zoom 3. If you have more zooms as options for some reason, this formula should hold for many
        // cases but probably not all.
        textFactory = DefaultResources.getStretchableFont().setSmoothingMultiplier(2f / (INTERNAL_ZOOM + 1f))
                .width(cellWidth).height(cellHeight).initBySize();
        // Creates a layered series of text grids in a SquidLayers object, using the previously set-up textFactory and
        // SquidColorCenters.
        display = new SquidLayers(width, height, cellWidth, cellHeight,
                textFactory.copy(), bgCenter, fgCenter).addExtraLayer();
        //display.getBackgroundLayer().setOnlyRenderEven(true);

        display.setAnimationDuration(0.1f);
        messages = new SquidMessageBox(width, 4,
                textFactory.copy());
        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        messages.setTextSize(cellWidth, cellHeight + INTERNAL_ZOOM * 2);
        display.setTextSize(cellWidth, cellHeight + INTERNAL_ZOOM * 2);
        //The subCell SquidPanel uses a smaller size here; the numbers 8 and 16 should change if cellWidth or cellHeight
        //change, and the INTERNAL_ZOOM multiplier keeps things sharp, the same as it does all over here.
        viewport = new StretchViewport(width * cellWidth, (height + 4) * cellHeight);
        stage = new Stage(viewport, batch);

        //These need to have their positions set before adding any entities if there is an offset involved.
        messages.setBounds(0, 0, cellWidth * width, cellHeight * 4);
        display.setPosition(0, messages.getHeight());
        messages.appendWrappingMessage("Use numpad or vi-keys (hjklyubn) to move. Use ? for help, f to change colors, q to quit." +
                " Click the top or bottom border of this box to scroll.");
        counter = 0;

        dungeonGen = new SectionDungeonGenerator(width, height, rng);
        dungeonGen.addWater(8, 6);
        dungeonGen.addGrass(MixedGenerator.CAVE_FLOOR, 20);
        dungeonGen.addBoulders(0, 7);
        dungeonGen.addDoors(18, false);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng);
        serpent.putCaveCarvers(1);
        serpent.putWalledBoxRoomCarvers(2);
        serpent.putWalledRoundRoomCarvers(2);
        char[][] mg = serpent.generate();
        decoDungeon = dungeonGen.generate(mg, serpent.getEnvironment());

        // change the TilesetType to lots of different choices to see what dungeon works best.
        //bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = dungeonGen.getBareDungeon();
        lineDungeon = DungeonUtility.hashesToLines(dungeonGen.getDungeon(), true);

        adjacency.addCostRule('"', 1.0);
        adjacency.addCostRule('"', 0.001, true);
        adjacency.addCostRule('~', 1.0);
        adjacency.addCostRule('/', 1.0);
        adjacency.addCostRule('~', 0.001, true);
        adjacency.addCostRule(',', 0.001, true);
        adjacency.addCostRule('.', 0.001, true);
        adjacency.addCostRule('/', 0.001, true);

        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        final GreasedRegion placement = new GreasedRegion(bareDungeon, '.');
        final Coord pl = placement.singleRandom(rng);
        placement.remove(pl);
        int numMonsters = 25;
        monsters = new OrderedMap<>(numMonsters);
        int p;
        for (int i = 0; i < numMonsters; i++) {
            Coord monPos = placement.singleRandom(rng);
            placement.remove(monPos);
            p = adjacency.composite(monPos.x, monPos.y, rng.nextIntHasty(8), 0);
            monsters.put(p >> 3, new Creature(display.animateActor(monPos.x, monPos.y, 'Я',
                    fgCenter.filter(display.getPalette().get(11))),
                    display.directionMarker(monPos.x, monPos.y, monsterMarkColors, 3f, 3, false),
                    p,
                    0));
        }
        // your choice of FOV matters here.
        fov = new FOV(FOV.RIPPLE_TIGHT);
        res = DungeonUtility.generateResistances(decoDungeon);
        fovmap = fov.calculateFOV(res, pl.x, pl.y, 8, Radius.SQUARE);
        getToPlayer = new CustomDijkstraMap(decoDungeon, adjacency, rng);
        getToPlayer.setGoal(adjacency.composite(pl.x, pl.y, rng.nextIntHasty(8), 0));
        pathMap = getToPlayer.scan(null);

        player = new Creature(display.animateActor(pl.x, pl.y, '@', SColor.HAN_PURPLE, false),
                display.directionMarker(pl.x, pl.y, playerMarkColors, 4f, 3, false),
                adjacency.composite(pl.x, pl.y, rng.nextIntHasty(8), 0), health);
//                fgCenter.filter(display.getPalette().get(30)));
        toCursor = new IntVLA(10);
        awaitedMoves = new IntVLA(10);
        playerToCursor = new CustomDijkstraMap(decoDungeon, adjacency);
        final int[][] initialColors = DungeonUtility.generatePaletteIndices(decoDungeon),
                initialBGColors = DungeonUtility.generateBGPaletteIndices(decoDungeon);
        colors = new Color[width][height];
        bgColors = new Color[width][height];
        ArrayList<Color> palette = display.getPalette();
        bgColor = SColor.DARK_SLATE_GRAY;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                colors[i][j] = palette.get(initialColors[i][j]);
                bgColors[i][j] = palette.get(initialBGColors[i][j]);
            }
        }
        lights = DungeonUtility.generateLightnessModifiers(decoDungeon, counter);
        seen = new boolean[width][height];
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
                    /*
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W': {
                        move(0, -1);
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S': {
                        move(0, 1);
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A': {
                        move(-1, 0);
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D': {
                        move(1, 0);
                        break;
                    }

                    case SquidInput.UP_LEFT_ARROW:
                    case 'y':
                    case 'Y': {
                        move(-1, -1);
                        break;
                    }
                    case SquidInput.UP_RIGHT_ARROW:
                    case 'u':
                    case 'U': {
                        move(1, -1);
                        break;
                    }
                    case SquidInput.DOWN_RIGHT_ARROW:
                    case 'n':
                    case 'N': {
                        move(1, 1);
                        break;
                    }
                    case SquidInput.DOWN_LEFT_ARROW:
                    case 'b':
                    case 'B': {
                        move(-1, 1);
                        break;
                    }
                    */
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
                    case 'f':
                    case 'F': {
                        currentCenter = (currentCenter + 1) % 9;
                        // idx is 3 when we use the HallucinateFilter, which needs special work
                        changingColors = currentCenter == 3;
                        fgCenter = colorCenters[currentCenter * 2];
                        bgCenter = colorCenters[currentCenter * 2 + 1];
                        display.setFGColorCenter(fgCenter);
                        display.setBGColorCenter(bgCenter);
                        break;
                    }
                }
            }
        }, new SquidMouse(cellWidth, cellHeight, width, height, 0, 0, new InputAdapter() {

            // if the user clicks within FOV range and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (fovmap[screenX][screenY] > 0.0 && awaitedMoves.size == 0) {
                    /*
                    System.out.println("player.pos=" + player.pos +
                            " with x="+adjacency.extractX(player.pos) + ", y=" + adjacency.extractY(player.pos) + "r=" + adjacency.extractR(player.pos));
                    int clicked = adjacency.composite(screenX, screenY, 0, 0);
                    System.out.println("clicked pos=" + clicked +
                            " with x="+adjacency.extractX(clicked) + ", y=" + adjacency.extractY(clicked) + ", r=" + adjacency.extractR(clicked));
                    System.out.println("screenX=" + screenX + ", screenY=" + screenY);
                    */
                    if (toCursor.size == 0) {
                        //Uses DijkstraMap to get a path from the player's position to the cursor
                        toCursor = playerToCursor.findPath(30, null, null, player.pos, adjacency.composite(screenX, screenY, 0, 0));
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
                /*
                if (awaitedMoves.size != 0)
                    return false;
                if (fovmap[screenX][screenY] > 0.0) {
                    //Uses DijkstraMap to get a path. from the player's position to the cursor
                    toCursor = playerToCursor.findPath(30, null, null, player.pos, adjacency.composite(screenX, screenY, 0, 0));
                }
                */
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
        int oldX = player.entity.gridX, oldY = player.entity.gridY,
                newX = adjacency.extractX(pos), newY = adjacency.extractY(pos);
        if (newX >= 0 && newY >= 0 && newX < width && newY < height
                && bareDungeon[newX][newY] != '#') {
            // '+' is a door.
            if (lineDungeon[newX][newY] == '+') {
                decoDungeon[newX][newY] = '/';
                lineDungeon[newX][newY] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(decoDungeon);
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, player.entity.gridX, player.entity.gridY, 8, Radius.SQUARE);
            } else {
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, newX, newY, 8, Radius.SQUARE);
                //player.marker.setDirection(Direction.CLOCKWISE[adjacency.extractR(pos)]);
                display.slide(player.entity, newX, newY);
                display.slide(player.marker, newX, newY);
                player.move(pos);
                monsters.remove(pos >>> 3);
            }
            phase = Phase.PLAYER_ANIM;
        }
    }

    // check if a monster's movement would overlap with another monster.
    private boolean checkOverlap(Creature mon, int x, int y, ArrayList<Coord> futureOccupied) {
        int comp = adjacency.composite(x, y, 0, 0) >> 3;
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

        // recalculate FOV, store it in fovmap for the render to use.
        fovmap = fov.calculateFOV(res, player.entity.gridX, player.entity.gridY, 8, Radius.SQUARE);
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
            if (mon.state > 0 || fovmap[adjacency.extractX(pos << 3)][adjacency.extractY(pos << 3)] > 0.1) {
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
                if (tmp >>> 3 == player.pos >>> 3) {
                    display.tint(player.entity.gridX, player.entity.gridY, SColor.PURE_CRIMSON, 0, 0.415f);
                    health--;
                    //player.setText("" + health);
                    mon.change(1);
                }
                // otherwise store the new position in newMons.
                else {
                    mon.change(1);
                    monsters.alter(pos, tmp >>> 3);
                    display.slide(mon.entity, adjacency.extractX(tmp), adjacency.extractY(tmp));
                    mon.move(tmp);
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
        final TextPanel<Color> tp = new TextPanel<Color>(new GDXMarkup(), DefaultResources.getStretchablePrintFont());
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
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                overlapping = monsters.containsKey(adjacency.composite(i, j, 0, 0) >>> 3) || (player.entity.gridX == i && player.entity.gridY == j);
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +215 lightness and 0.0 being rather dark at -105.
                if (fovmap[i][j] > 0.0) {
                    seen[i][j] = true;
                    display.put(i, j, (overlapping) ? ' ' : lineDungeon[i][j], fgCenter.filter(colors[i][j]), bgCenter.filter(bgColors[i][j]),
                            lights[i][j] + (int) (-105 + 320 * fovmap[i][j]));
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                } else {// if (seen[i][j]) {
                    display.put(i, j, lineDungeon[i][j], fgCenter.filter(colors[i][j]), bgCenter.filter(bgColors[i][j]), -140);
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

        // used as the z-axis when generating Simplex noise to make water seem to "move"
        counter += Gdx.graphics.getDeltaTime() * 15;
        // this does the standard lighting for walls, floors, etc. but also uses counter to do the Simplex noise thing.
        lights = DungeonUtility.generateLightnessModifiers(decoDungeon, counter);
        //textFactory.configureShader(batch);

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
                if (framesWithoutAnimation >= 3) {
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
            display.drawActor(batch, 1.0f, player.marker);
            int mSize = monsters.size();
            Creature mon;
            for (int m = 0; m < mSize; m++) {
                mon = monsters.getAt(m);
                // monsters are only drawn if within FOV.
                if (fovmap[mon.entity.gridX][mon.entity.gridY] > 0.0) {
                    display.drawActor(batch, 1.0f, mon.entity);
                    display.drawActor(batch, 1.0f, mon.marker);
                }
            }
            messages.put(width >> 1, 0, Character.forDigit(health, 10), SColor.DARK_PINK);
            // batch must end if it began.
            batch.end();
        }
        // if using a filter that changes each frame, clear the known relationship between requested and actual colors
        if (changingColors) {
            fgCenter.clearCache();
            bgCenter.clearCache();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // message box won't respond to clicks on the far right if the stage hasn't been updated with a larger size
        currentZoomX = width * 1f / RotationDemo.width;
        // total new screen height in pixels divided by total number of rows on the screen
        currentZoomY = height * 1f / (RotationDemo.height + messages.getGridHeight());
        // message box should be given updated bounds since I don't think it will do this automatically
        messages.setBounds(0, 0, width, currentZoomY * messages.getGridHeight());
        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
        input.reinitialize(currentZoomX, currentZoomY, RotationDemo.width, RotationDemo.height, 0, 0, width, height);
        currentZoomX = cellWidth / currentZoomX;
        currentZoomY = cellHeight / currentZoomY;
        input.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Rotation in Pathfinding";
        config.width = width * cellWidth;
        config.height = height * cellHeight;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new RotationDemo(), config);
    }

}
