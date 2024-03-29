package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.panel.IColoredString;
import squidpony.squidai.CustomDijkstraMap;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Measurement;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.*;

import java.util.ArrayList;

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
            marker.gridX = entity.gridX = adjacency.extractX(pos);
            marker.gridY = entity.gridY = adjacency.extractY(pos);
            this.pos = pos;
            marker.setDirection(adjacency.directions[adjacency.extractR(pos)]);
            return this;
        }
    }

    private FilterBatch batch;

    private Phase phase = Phase.WAIT;
    private StatefulRNG rng;
    private SquidLayers display;
    private TextPanel messagePanel;
    private ArrayList<CharSequence> messages;
    /**
     * Non-{@code null} iff '?' was pressed before
     */
    private /*Nullable*/ Actor help;
    private SectionDungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private double[][] res;
    private Color[][] colors, bgColors;
    private double[][] fovmap;
    private Creature player;
    private FOV fov;

    /**
     * In number of cells
     */
    private static final int gridWidth = 80;
    /**
     * In number of cells
     */
    private static final int gridHeight = 25;

    /**
     * Number of cells high for the TextPanel.
     */
    private static final int bonusHeight = 4;
    /**
     * The pixel width of a cell
     */
    private static final int cellWidth = 18;
    /**
     * The pixel height of a cell
     */
    private static final int cellHeight = 28;
    private SquidInput input;
    private boolean[][] seen;
    private int health = 9;
    private SquidColorCenter fgCenter, bgCenter;
    private Color bgColor;
    private ArrayList<Color> playerMarkColors, monsterMarkColors;
    private OrderedMap<Integer, Creature> monsters;
    private CustomDijkstraMap getToPlayer, playerToCursor;
    private Stage stage;
    private int framesWithoutAnimation;
    private IntVLA toCursor, awaitedMoves;
    private String lang;
    private TextCellFactory textFactory;
    private Viewport viewport;
    private float currentZoomX = 1f, currentZoomY = 1f;

    public static final Adjacency adjacency = new Adjacency.RotationAdjacency(gridWidth, gridHeight, Measurement.EUCLIDEAN);
    @Override
    public void create() {
        // gotta have a random number generator. We seed a LightRNG with any long we want, then pass that to an RNG.
        rng = new StatefulRNG(0xBADBEEFCAFEL);

        fgCenter = DefaultResources.getSCC();
        bgCenter = DefaultResources.getSCC();
        playerMarkColors = fgCenter.rainbow(64);
        monsterMarkColors = fgCenter.rainbow(0.75f, 0.65f, 64);
        //playerMarkColors = fgCenter.loopingGradient(SColor.HAN_PURPLE, SColor.PSYCHEDELIC_PURPLE, 64);
        //monsterMarkColors = fgCenter.loopingGradient(SColor.CRIMSON, SColor.ORANGE_RED, 64);
        batch = new FilterBatch();

        // getStretchableFont loads an embedded font, Inconsolata-LGC-Custom, that is a distance field font as mentioned
        // earlier. We set the smoothing multiplier on it only because we are using internal zoom to increase sharpness
        // on small details, but if the smoothing is incorrect some sizes look blurry or over-sharpened. This can be set
        // manually if you use a constant internal zoom; here we use 1f for internal zoom 1, about 2/3f for zoom 2, and
        // about 1/2f for zoom 3. If you have more zooms as options for some reason, this formula should hold for many
        // cases but probably not all.
        //textFactory = DefaultResources.getStretchableDejaVuFont().setSmoothingMultiplier(2f / (INTERNAL_ZOOM + 1f))
        //        .width(cellWidth).height(cellHeight).initBySize();
        textFactory = DefaultResources.getCrispSlabFont()
                .width(cellWidth).height(cellHeight).initBySize(); //.setDirectionGlyph('ˆ')

        // Creates a layered series of text grids in a SquidLayers object, using the previously set-up textFactory and
        // SquidColorCenters.
        display = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                textFactory.copy(), bgCenter, fgCenter);//.addExtraLayer();
        //display.getBackgroundLayer().setOnlyRenderEven(true);

        display.setAnimationDuration(0.1f);
        TextCellFactory font = DefaultResources.getCrispPrintFamily().initBySize();//.height(cellHeight).width(23)
        messagePanel = new TextPanel(font);
        viewport = new StretchViewport(gridWidth * cellWidth, (gridHeight + 4) * font.actualCellHeight);
        stage = new Stage(viewport, batch);
        messages = new ArrayList<>(32);
        messagePanel.initShared(cellWidth * gridWidth, font.actualCellHeight * 4, messages);
        //messagePanel.getScrollPane().setHeight(font.actualCellHeight * 4);
        messagePanel.getScrollPane().setStyle(new ScrollPane.ScrollPaneStyle());
        messagePanel.getScrollPane().setBounds(0, 0, cellWidth * gridWidth, font.actualCellHeight * 4);
        //These need to have their positions set before adding any entities if there is an offset involved.
//        messagePanel.setBounds(0, 0, cellWidth * width, cellHeight * 4);
        display.setPosition(0, cellHeight * 4);
        messages.add(GDXMarkup.instance.colorStringMarkup("You are the orange '[Cape Jasmine]@[]', and enemies are red '[Scarlet]Я[]'. Click a cell to turn and move. " +
                "The colorful [Aurora Heliotrope]^[] shows your facing direction; you rotate automatically to reach a goal. " +
                "Rotation takes as much time as moving forward one square, but you can only move in the direction you face. " +
                "Use ? for help, or q to quit."));
        messagePanel.scrollToEdge(false);

        dungeonGen = new SectionDungeonGenerator(gridWidth, gridHeight, rng);
        dungeonGen.addWater(0, 25, 6);
        dungeonGen.addGrass(DungeonUtility.CAVE_FLOOR, 20);
        dungeonGen.addBoulders(0, 7);
        dungeonGen.addDoors(18, false);
        dungeonGen.addLake(20, '£', '¢');
        SerpentMapGenerator serpent = new SerpentMapGenerator(gridWidth, gridHeight, rng);
        serpent.putCaveCarvers(1);
        serpent.putWalledBoxRoomCarvers(2);
        serpent.putWalledRoundRoomCarvers(2);
        char[][] mg = serpent.generate();
        decoDungeon = dungeonGen.generate(mg, serpent.getEnvironment());
        //decoDungeon = dungeonGen.generate();
        bareDungeon = dungeonGen.getBareDungeon();
        lineDungeon = DungeonUtility.hashesToLines(dungeonGen.getDungeon(), true);
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
            p = adjacency.composite(monPos.x, monPos.y, rng.nextIntHasty(4), 0);
            monsters.put(p >> 3, new Creature(display.animateActor(monPos.x, monPos.y, 'Я',//((i & 1) == 0) ? 'g' : 'K', //
                    SColor.SCARLET),
                    display.directionMarker(monPos.x, monPos.y, monsterMarkColors, 1.5f, 2, false),
                    p,
                    0));
        }
        // your choice of FOV matters here.
        fov = new FOV(FOV.RIPPLE_TIGHT);
        res = DungeonUtility.generateResistances(decoDungeon);
        fovmap = fov.calculateFOV(res, pl.x, pl.y, 8, Radius.SQUARE);
        getToPlayer = new CustomDijkstraMap(decoDungeon, adjacency, rng);
        getToPlayer.setGoal(adjacency.composite(pl.x, pl.y, rng.nextIntHasty(4), 0));

        player = new Creature(display.animateActor(pl.x, pl.y, '@', SColor.CAPE_JASMINE, false),
                display.directionMarker(pl.x, pl.y, playerMarkColors, 2f, 2, false),
                adjacency.composite(pl.x, pl.y, rng.nextIntHasty(4), 0), health);
        /*
        player = new Creature(display.animateActor(1, 1, ' ', SColor.HAN_PURPLE, false),
                display.directionMarker(pl.x, pl.y, SColor.HAN_PURPLE, 3, false),
                adjacency.composite(1, 1, 4, 0), health);
        */
//                fgCenter.filter(display.getPalette().get(30)));
        toCursor = new IntVLA(10);
        awaitedMoves = new IntVLA(10);
        playerToCursor = new CustomDijkstraMap(decoDungeon, adjacency, rng);
        bgColor = SColor.DB_INK;
//        colors = MapUtility.generateDefaultColors(decoDungeon);
//        bgColors = MapUtility.generateDefaultBGColors(decoDungeon);
        colors = MapUtility.generateDefaultColors(decoDungeon, '£', SColor.CW_LIGHT_YELLOW, '¢', SColor.CW_BRIGHT_ORANGE);
        bgColors = MapUtility.generateDefaultBGColors(decoDungeon, '£', SColor.CW_ORANGE, '¢',  SColor.CW_DARK_ORANGE);
        // the line after this automatically sets the brightness of backgrounds in display to match their contents, so
        // here we simply fill the contents of display with our dungeon (but we don't set the actual colors yet).
        ArrayTools.insert(decoDungeon, display.getForegroundLayer().contents, 0, 0);
        display.autoLight((System.currentTimeMillis() & 0xFFFFFFL) * 0.013, '£', '¢');
        seen = new boolean[gridWidth][gridHeight];
        /*
        lang = FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 4, 6, new String[]{",", ",", ",", " -"},
                new String[]{"..."}, 0.25);
        */
        lang = FakeLanguageGen.RUSSIAN_ROMANIZED.sentence(rng, 4, 6, new String[]{",", ",", ",", " -"},
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
        input = new SquidInput(new SquidInput.KeyHandler() {
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
                    case SquidInput.PAGE_UP:
                        messagePanel.scroll(-1f);
                        break;
                    case SquidInput.PAGE_DOWN:
                        messagePanel.scroll(1f);
                        break;
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
        }, new SquidMouse(cellWidth, cellHeight, gridWidth, gridHeight, 0, 0, new InputAdapter() {

            // if the user clicks within FOV range and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (fovmap[screenX][screenY] > 0.0 && awaitedMoves.size == 0) {
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

                if (awaitedMoves.size != 0)
                    return false;
                if (fovmap[screenX][screenY] > 0.0) {
                    //Uses DijkstraMap to get a path. from the player's position to the cursor
                    toCursor = playerToCursor.findPath(30, null, null, player.pos, adjacency.composite(screenX, screenY, 0, 0));
                }

                return false;
            }
        }));
        //set this to true to test visual input on desktop
//        input.forceButtons = false;
        input.setRepeatGap(Long.MAX_VALUE);
        //actions to give names to in the visual input menu
//        input.init("filter", "??? help?", "quit");
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        // and then add display and messagePanel, our two visual components, to the list of things that act in Stage.
        stage.addActor(display);
        stage.addActor(messagePanel.getScrollPane());
        //viewport = input.resizeInnerStage(stage);
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
        if (newX >= 0 && newY >= 0 && newX < gridWidth && newY < gridHeight
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
        int ms = monsters.size(), tmp;
        IntVLA impassable = new IntVLA(ms), path;
        for (int i = 0; i < ms; i++) {
            impassable.add(monsters.getAt(i).pos);
        }
        int[] playerGoal = new int[]{player.pos};
        for (int i = 0; i < ms; i++) {
            Integer pos = monsters.keyAt(i);
            if(pos == null)
                continue;
            Creature mon = monsters.getAt(i);
            // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
            if (mon.state > 0 || fovmap[adjacency.extractX(pos << 3)][adjacency.extractY(pos << 3)] > 0.1) {
                if (mon.state == 0) {
                    /*
                    messagePanel.appendMessage("The AЯMED GUAЯD shouts at you, \"" +
                            FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 1, 3,
                                    new String[]{",", ",", ",", " -"}, new String[]{"!"}, 0.25) + "\"");
                    */
                    messages.add(GDXMarkup.instance.colorStringMarkup("The [Scarlet]AЯMED GUAЯD[] shouts at you, \"" +
                            FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 1, 3,
                                    new String[]{",", ",", ",", " -"}, new String[]{"!"}, 0.25) + "\""));
                    messagePanel.scrollToEdge(false);
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
                    display.slide(mon.marker, adjacency.extractX(tmp), adjacency.extractY(tmp));
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
        final Color bgColor = new Color(0.3f, 0.3f, 0.3f, 1f);

        final Actor a;
            /*
			 * Use TextPanel. There's less work to do than with
			 * GroupCombinedPanel, and we can use a more legible variable-width font.
			 * It doesn't seem like it when reading this code, but this actually does
			 * much more than GroupCombinedPanel,  because we do line wrapping and
			 * justifying, without having to worry about sizes since TextPanel lays
			 * itself out.
			 */
        final TextPanel tp = new TextPanel(DefaultResources.getCrispPrintFamily());
        tp.backgroundColor = SColor.DARK_SLATE_GRAY;

        final ArrayList<IColoredString<Color>> text = new ArrayList<>();
        text.add(cs);
			/* No need to call IColoredString::wrap, TextPanel does it on its own */
        text.add(helping1);
        text.add(helping2);
        text.add(helping3);

        final float w = gridWidth * cellWidth, aw = helping3.length() * cellWidth * 0.8f;
        final float h = gridHeight * cellHeight, ah = cellHeight * 9f;
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
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                overlapping = monsters.containsKey(adjacency.composite(i, j, 0, 0) >>> 3) || (player.entity.gridX == i && player.entity.gridY == j);
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +215 lightness and 0.0 being rather dark at -105.
                if (fovmap[i][j] > 0.0) {
                    seen[i][j] = true;
                    display.put(i, j, (overlapping) ? ' ' : lineDungeon[i][j], fgCenter.filter(colors[i][j]), bgCenter.filter(bgColors[i][j]),
                            (int) (-105 + 250 * fovmap[i][j]));
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
            display.highlight(x = adjacency.extractX(pt), y = adjacency.extractY(pt), (int) (170 * fovmap[x][y]));
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // this does the standard lighting for walls, floors, etc. but also uses the time to do the Simplex noise thing.
        display.autoLight((System.currentTimeMillis() & 0xFFFFFFL) * 0.013, '£', '¢');
        //lights = DungeonUtility.generateLightnessModifiers(decoDungeon, (System.currentTimeMillis() & 0xFFFFFFL) * 0.013, '£', '¢');

        // you done bad. you done real bad.
        if (health <= 0) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(gridWidth / 2 - 18, gridHeight / 2 - 10, "   THE TSAR WILL HAVE YOUR HEAD!    ");
            display.putBoxedString(gridWidth / 2 - 18, gridHeight / 2 - 5, "      AS THE OLD SAYING GOES,       ");
            display.putBoxedString(gridWidth / 2 - lang.length() / 2, gridHeight / 2, lang);
            display.putBoxedString(gridWidth / 2 - 18, gridHeight / 2 + 5, "             q to quit.             ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if (input.hasNext())
                input.next();
            return;
        }
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        display.put(gridWidth >> 1, 0, Character.forDigit(health, 10), SColor.DARK_PINK);

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
        //input.show();
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
            // batch must end if it began.
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
//        super.resize(width, height);
//
//        // message box won't respond to clicks on the far right if the stage hasn't been updated with a larger size
//        currentZoomX = (float)width / gridWidth;
//        // total new screen height in pixels divided by total number of rows on the screen
//        currentZoomY = (float) height / (gridHeight + bonusHeight);
//        // message box should be given updated bounds since I don't think it will do this automatically
//        messagePanel.getScrollPane().setBounds(0, 0, width, currentZoomY * bonusHeight);
//        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
////        input.reinitialize(currentZoomX, currentZoomY, RotationDemo.gridWidth, RotationDemo.gridHeight, 0, 0, width, height);
//
//        // the viewports are updated separately so each doesn't interfere with the other's drawn area.
//        currentZoomX = cellWidth / currentZoomX;
//        currentZoomY = cellHeight / currentZoomY;
////        input.update(width, height, true);
//        stage.getViewport().update(width, height, true);
        super.resize(width, height);

        // message box won't respond to clicks on the far right if the stage hasn't been updated with a larger size
        float currentZoomX = (float)width / gridWidth;
        // total new screen height in pixels divided by total number of rows on the screen
        float currentZoomY = (float)height / (gridHeight + bonusHeight);
        // message box should be given updated bounds since I don't think it will do this automatically
        messagePanel.getScrollPane().setBounds(0, 0, width, currentZoomY * bonusHeight);
        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
        // a quirk of how the camera works requires the mouse to be offset by half a cell if the width or height is odd
        // (gridWidth & 1) is 1 if gridWidth is odd or 0 if it is even; it's good to know and faster than using % , plus
        // in some other cases it has useful traits (x % 2 can be 0, 1, or -1 depending on whether x is negative, while
        // x & 1 will always be 0 or 1).
        input.getMouse().reinitialize(currentZoomX, currentZoomY, gridWidth, gridHeight,
                0, (gridHeight & 1) * (int) (currentZoomY));        // the viewports are updated separately so each doesn't interfere with the other's drawn area.
        stage.getViewport().update(width, height, false);
        // we also set the bounds of that drawn area here for each viewport.
        stage.getViewport().setScreenBounds(0, 0, width, (int) messagePanel.getScrollPane().getHeight());
        // we did this for the language viewport, now again for the main viewport
        stage.getViewport().update(width, height, false);
        stage.getViewport().setScreenBounds(0, (int) messagePanel.getScrollPane().getHeight(),
                width, height - (int) messagePanel.getScrollPane().getHeight());
    }
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Rotation in Pathfinding");
        config.useVsync(true);
        config.setWindowedMode(gridWidth * cellWidth, gridHeight * cellHeight);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new RotationDemo(), config);
    }

}
