package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.panel.IColoredString;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.SpatialMap;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

public class EverythingDemo extends ApplicationAdapter {
    private enum Phase {WAIT, PLAYER_ANIM, MONSTER_ANIM}

    private static class Monster {
        public AnimatedEntity entity;
        public int state;

        public Monster(Actor actor, int x, int y, int state) {
            entity = new AnimatedEntity(actor, x, y);
            this.state = state;
        }

        public Monster(AnimatedEntity ae, int state) {
            entity = ae;
            this.state = state;
        }

        public Monster change(int state) {
            this.state = state;
            return this;
        }

        public Monster change(AnimatedEntity ae) {
            entity = ae;
            return this;
        }

        public Monster move(int x, int y) {
            entity.gridX = x;
            entity.gridY = y;
            return this;
        }
    }

    SpriteBatch batch;

    private Phase phase = Phase.WAIT;
    private StatefulRNG rng;
    private SquidLayers display;
    //private SquidPanel subCell;

    // for more convenient access to some methods
    private SquidPanel fg;
    private SquidMessageBox messages;
    /**
     * Non-{@code null} iff '?' was pressed before
     */
    private /*Nullable*/ Actor help;
    private DungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private double[][] res;
    private Color[][] colors, bgColors;
    private double[][] fovmap;
    private AnimatedEntity player;
    private FOV fov;
    /**
     * In number of cells
     */
    private int width;
    /**
     * In number of cells
     */
    private int height;
    /**
     * In number of cells
     */
    private int totalWidth;
    /**
     * In number of cells
     */
    private int totalHeight;
    /**
     * The pixel width of a cell
     */
    private int cellWidth;
    /**
     * The pixel height of a cell
     */
    private int cellHeight;
    private SquidInput input;
    private int health = 7;
    private SquidColorCenter fgCenter, bgCenter;
    private Color bgColor;
    private SpatialMap<Integer, Monster> monsters;
    private GreasedRegion floors, blockage, seen, currentlySeen;
    private DijkstraMap getToPlayer, playerToCursor;
    private Stage stage, messageStage;
    private int framesWithoutAnimation = 0;
    private Coord cursor;
    private Coord playerPos;
    private List<Coord> toCursor;
    private List<Coord> awaitedMoves;
    private String lang;
    private SquidColorCenter[] colorCenters;
    private int currentCenter;
    private boolean changingColors = false;
    private TextCellFactory textFactory;
    public static final int INTERNAL_ZOOM = 1;
    private Viewport viewport, messageViewport;
    private Camera camera;
    private float currentZoomX = INTERNAL_ZOOM, currentZoomY = INTERNAL_ZOOM;
    private Vector2 screenPosition;

    @Override
    public void create() {
        // gotta have a random number generator. We give a seed to StatefulRNG, which will ensure the dungeon is the
        // same every time.
        rng = new StatefulRNG(0xBADBEEFB0BBL);

        // for demo purposes, we allow changing the SquidColorCenter and the filter effect associated with it.
        // next, we populate the colorCenters array with the SquidColorCenters that will modify any colors we request
        // of them using the filter we specify. Only one SquidColorCenter will be used at any time for foreground, and
        // sometimes another will be used for background.
        colorCenters = new SquidColorCenter[20];

        // HallucinateFilter makes all the colors very saturated and move even when you aren't doing anything.

        colorCenters[0] = new SquidColorCenter(new Filters.HallucinateFilter());
        colorCenters[1] = colorCenters[0];

        // WiggleFilter was used here to make a glitchy effect, but it's mostly headache-inducing.

        // MultiLerpFilter here is given two colors to tint everything toward one of; this is meant to reproduce the
        // "Hollywood action movie poster" style of using primarily light orange (explosions) and gray-blue (metal).

        colorCenters[2] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.GAMBOGE_DYE, SColor.COLUMBIA_BLUE},
                new float[]{0.25f, 0.2f}
        ));
        colorCenters[3] = colorCenters[2];
        
        // PaletteFilter here is used to limit colors to specific sets.
        Filters.PaletteFilter paletteFilter = new Filters.PaletteFilter(SColor.COLOR_WHEEL_PALETTE);
        colorCenters[4] = new SquidColorCenter(
                new Filters.ChainFilter(paletteFilter,
                        new Filters.SaturationValueFilter(1f, 1.25f)));
        colorCenters[5] = new SquidColorCenter(
                new Filters.ChainFilter(paletteFilter,
                        new Filters.SaturationValueFilter(0.85f, 0.875f)));

        colorCenters[6] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.CW_PALE_ROSE, SColor.CW_BRIGHT_APRICOT, SColor.CW_LIGHT_YELLOW, SColor.CW_BRIGHT_HONEYDEW, SColor.CW_FADED_BLUE, SColor.CW_BRIGHT_PURPLE},
                new float[]{0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f}
        ));
        colorCenters[7] = colorCenters[6];

        // MultiLerpFilter here is given three colors and will tint any requested color toward one of those three; this
        // is meant to look bolder.

        colorCenters[8] = new SquidColorCenter(new Filters.MultiLerpFilter(
                new Color[]{SColor.CW_RED, SColor.CW_BRIGHT_SAPPHIRE, SColor.CW_RICH_GREEN},
                new float[]{0.2f, 0.25f, 0.25f}
        ));
        colorCenters[9] = colorCenters[8];

        // Indices into colorCenters after this point don't receive small changes to colors from lighting/water ripples.

        // ColorizeFilter here is given a slightly-grayish dark brown to imitate a sepia tone.

        colorCenters[10] = new SquidColorCenter(new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.7f, -0.05f));
        colorCenters[11] = new SquidColorCenter(new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.65f, 0.07f));

        // SaturationFilter here is used to de-saturate the colors slightly. Background is less saturated.

        colorCenters[12] = new SquidColorCenter(new Filters.SaturationFilter(0.7f));
        colorCenters[13] = new SquidColorCenter(new Filters.SaturationFilter(0.5f));

        // SaturationFilter here is used to over-saturate the colors slightly. Background is less saturated.

        colorCenters[14] = new SquidColorCenter(new Filters.SaturationFilter(1.35f));
        colorCenters[15] = new SquidColorCenter(new Filters.SaturationFilter(1.15f));

        // No filter here.

        colorCenters[16] = DefaultResources.getSCC();
        colorCenters[17] = colorCenters[16];

        // DistinctRedGreenFilter may be able to help people with common forms of red-green colorblindness to
        // distinguish red and green colors by subtly adjusting hue and brightness in different directions for
        // primarily-red colors versus primarily-green ones.

        colorCenters[18] = new SquidColorCenter(new Filters.DistinctRedGreenFilter());
        colorCenters[19] = colorCenters[18];

        // reduces the amount of colors we need by using one color for any two extremely similar values.
        // you can use granularity = 2 without much difference (it may perform better), but granularity starts to
        // take a toll on visual quality at 3 or higher.
        for (int i = 0; i < colorCenters.length; i++) {
            colorCenters[i].granularity = 2;
        }
        //DefaultResources.getSCC().granularity = 3;

        batch = new SpriteBatch();
        width = 90;
        height = 26;
        totalWidth = width * 3;
        totalHeight = height * 3;
        //Only needed if totalWidth and/or totalHeight is 257 or larger
        Coord.expandPoolTo(totalWidth, totalHeight);
        dungeonGen = new DungeonGenerator(totalWidth, totalHeight, rng);
        dungeonGen.addWater(36, 6);
        dungeonGen.addGrass(15);
        dungeonGen.addBoulders(5);
        dungeonGen.addDoors(12, true);
        //SerpentMapGenerator mix = new SerpentMapGenerator(totalWidth, totalHeight, rng, 0.35);
        //mix.putCaveCarvers(2);
        //mix.putWalledBoxRoomCarvers(3);
        //mix.putWalledRoundRoomCarvers(2);
        //char[][] mg = mix.generate();
        decoDungeon = dungeonGen.generate(TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        //bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = dungeonGen.getBareDungeon();
        lineDungeon = DungeonUtility.hashesToLines(dungeonGen.getDungeon(), true);


        //NOTE: cellWidth and cellHeight are assigned values that are significantly larger than the corresponding sizes
        //in the EverythingDemoLauncher's main method. Because they are scaled up by an integer here, they can be scaled
        //down when rendered, allowing certain small details to appear sharper. This _only_ works with distance field,
        //a.k.a. stretchable, fonts! INTERNAL_ZOOM is a tradeoff between rendering more pixels to increase quality (when
        //values are high) or rendering fewer pixels for speed (when values are low). Using 1 seems to work well with
        //the current technique for distance field font rendering, and in contrast to earlier versions, using higher
        //internal zoom levels can actually reduce quality when the font is scaled down to be fairly small.
        cellWidth = 13 * INTERNAL_ZOOM;
        cellHeight = 23 * INTERNAL_ZOOM;
        // getSlabFamily loads a family of fonts, Iosevka Slab, that have a distance field effect as mentioned earlier.
        // We set its width and height to the cellWidth and cellHeight, then call initBySize() to finish its setup.
        // SquidLib has font families so that different formatting can be rendered without changing the font.
        // getSlabFamily is actually 4 fonts merged into one, with a regular, bold, italic, and bold italic version all
        // clumped into one TextCellFactory; some parts of SquidLib, like GDXMarkup, know how to turn special markup in
        // a String into a modified IColoredString with not just color markup coded into the IColoredString, but bold
        // and italic stored specially in (usually) unused parts of the font. Unlike one approach tried briefly, this
        // technique does not require any changes to the classes used, and works with an ordinary TextCellFactory and a
        // GDXMarkup to apply the bold and italic alterations.
        textFactory = DefaultResources.getSlabFamily().width(cellWidth).height(cellHeight).initBySize();
        // Creates a layered series of text grids in a SquidLayers object, using the previously set-up textFactory and
        // SquidColorCenters.
        display = new SquidLayers(width, height, cellWidth, cellHeight,
                textFactory.copy(), bgCenter, fgCenter, decoDungeon);
        //NOT USED CURRENTLY
        //subCell is a SquidPanel, the same class that SquidLayers has for each of its layers, but we want to render
        //certain effects on top of all other panels, which can't be done in the all-in-one-pass rendering of the grids
        //in SquidLayers, though it could be done with a slight hassle if the effects are made into AnimatedEntity
        //objects or Actors, then rendered separately like the monsters are (see render() below). It is called subCell
        //because its text will be made smaller than a full cell, and appears in the upper left corner for things like
        //the current health of the player and an '!' for alerted monsters.
        //subCell = new SquidPanel(width, height, textFactory.copy(), fgCenter);

        display.setAnimationDuration(0.11f);
        // we use a "torchlight" effect where the field of view wavers slightly, so a slightly-yellow color like
        // SColor.COSMIC_LATTE works well for the color of lighting. This tints everything very slightly yellow, but
        // this is mostly unnoticeable for things like deep water that already have a vivid color here.
        // if you check the JavaDocs on SColor.COSMIC_LATTE, you will (depending on IDE) probably see a nice preview
        // of the actual color, which should be practically white but just a little closer to yellow.
        display.setLightingColor(SColor.COSMIC_LATTE);
        messages = new SquidMessageBox(width, 4,
                textFactory);
        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        messages.setTextSize(cellWidth * 1.1f,cellHeight * 1.1f); //cellWidth * 1f, cellHeight  * 1.034f
        display.setTextSize(cellWidth * 1.1f,cellHeight * 1.1f); //cellWidth * 1f, cellHeight  * 1.034f
        //The subCell SquidPanel uses a smaller size here; the numbers 8 and 16 should change if cellWidth or cellHeight
        //change, and the INTERNAL_ZOOM multiplier keeps things sharp, the same as it does all over here.
        //subCell.setTextSize(8 * INTERNAL_ZOOM, 16 * INTERNAL_ZOOM);
        viewport = new StretchViewport(width * cellWidth, height * cellHeight);
        messageViewport = new StretchViewport(width * cellWidth, 4 * cellHeight);
        camera = viewport.getCamera();
        stage = new Stage(viewport, batch);
        messageStage = new Stage(messageViewport, batch);
        //These need to have their positions set before adding any entities if there is an offset involved.
        messages.setBounds(0, 0, cellWidth * width, cellHeight * 4);
        display.setPosition(0, 0);
        viewport.setScreenY((int)messages.getHeight());
        //subCell.setPosition(0, messages.getHeight());
//                "Use numpad or vi-keys ([CW Bright Red]h[CW Bright Apricot]j[CW Bright Yellow]k[CW Bright Lime]l" +
//                        "[CW Bright Jade]y[CW Bright Azure]u[CW Bright Sapphire]b[CW Flush Purple]n[]) to move. Use " +
        // Here we try to bring attention to the important keys by using a mixed-color String (IColoredString).
        IColoredString<Color> text = GDXMarkup.instance.colorString(
                "Use numpad or vi-keys ([@0.0 0.8 1]h[@0.1 0.8 1]j[@0.17 0.8 1]k[@0.21 0.8 1]l" +
                        "[@0.37 0.8 1]y[@0.53 0.8 1]u[@0.65 0.8 1]b[@0.81 0.8 1]n[]) to move. Use " +
                        "[CW Pale Indigo]?[] for help, [#BFA38A]f[] to filter colors, [CW Gray White]q[] to quit. " + //CW Faded Brown
                        "Click the [/]top[/] or [/]bottom[/] border of [*]this [/]box[] to scroll.");
        /*IColoredString.Impl.create("Use numpad or vi-keys (", Color.WHITE);
        text.append('h', SColor.CW_BRIGHT_RED);
        text.append('j', SColor.CW_BRIGHT_APRICOT);
        text.append('k', SColor.CW_BRIGHT_YELLOW);
        text.append('l', SColor.CW_BRIGHT_LIME);
        text.append('y', SColor.CW_BRIGHT_JADE);
        text.append('u', SColor.CW_BRIGHT_AZURE);
        text.append('b', SColor.CW_BRIGHT_SAPPHIRE);
        text.append('n', SColor.CW_FLUSH_PURPLE);
        text.append(") to move. Use ");
        text.append('?', SColor.CW_PALE_INDIGO);
        text.append(" for help, ");
        text.append('f', SColor.CW_FADED_BROWN);
        text.append(" to filter colors, ");
        text.append('q', SColor.CW_GRAY_WHITE);
        text.append(" to quit. Click the top or bottom border of this box to scroll.");
        */
        messages.appendWrappingMessage(text);

        // The display is almost all set up, so now we can tell it to use the filtered color centers we want.
        // 8 is unfiltered. You can change this to 0-9 to use different filters, or press 'f' in play.
        currentCenter = 8;

        fgCenter = colorCenters[currentCenter * 2];
        bgCenter = colorCenters[currentCenter * 2 + 1];
        display.setFGColorCenter(fgCenter);
        display.setBGColorCenter(bgCenter);

        // it's more efficient to get random floors from a set containing only tightly-stored floor positions.
        GreasedRegion placement = new GreasedRegion(bareDungeon, '.');
        playerPos = placement.singleRandom(rng);
        display.setGridOffsetX(playerPos.x - (width >> 1));
        display.setGridOffsetY(playerPos.y - (height >> 1));

        fg = display.getForegroundLayer();
        placement.remove(playerPos);
        int numMonsters = 200;
        monsters = new SpatialMap<>(numMonsters);
        for (int i = 0; i < numMonsters; i++) {
            Coord monPos = placement.singleRandom(rng);
            placement = placement.remove(monPos);
            monsters.put(monPos, i, new Monster(display.animateActor(monPos.x, monPos.y, 'Я',
                    fgCenter.filter(SColor.LIMITED_PALETTE[11])), 0));
        }
        // your choice of FOV matters here.
        fov = new FOV(FOV.RIPPLE_TIGHT);
        res = DungeonUtility.generateResistances(decoDungeon);
        floors = new GreasedRegion(res, 0.99);
        fovmap = new double[totalWidth][totalHeight];
        FOV.reuseFOV(res, fovmap, playerPos.x, playerPos.y, 8, Radius.CIRCLE);
        blockage = new GreasedRegion(fovmap, 0.0);
        seen = blockage.not().copy();
        currentlySeen = seen.copy();
        blockage.fringe8way();
        getToPlayer = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.CHEBYSHEV);
        getToPlayer.rng = rng;
        getToPlayer.setGoal(playerPos);
        getToPlayer.scan(null, null);

        player = display.animateActor(playerPos.x, playerPos.y, '@',
                fgCenter.loopingGradient(SColor.CAPE_JASMINE, SColor.HAN_PURPLE, 45), 1.5f, false);
//                fgCenter.filter(display.getPalette().get(30)));
        cursor = Coord.get(-1, -1);
        toCursor = new ArrayList<>(10);
        awaitedMoves = new ArrayList<>(10);
        //DijkstraMap is the pathfinding swiss-army knife we use here to find a path to the latest cursor position.
        //DijkstraMap.Measurement is an enum that determines the possibility or preference to enter diagonals. Here, the
        //EUCLIDEAN value is used, which allows 8 directions of movement but prefers orthogonal moves, unless a
        //diagonal move is clearly closer "as the crow flies." Alternatives are CHEBYSHEV,  which allows 8 directions of
        //movement at the same cost for all directions, and MANHATTAN, which means 4-way movement only, no diagonals.
        playerToCursor = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.EUCLIDEAN);
        //These next two lines mark the player as something we want paths to go to or from, and get the distances to the
        // player from all walkable cells in the dungeon.
        playerToCursor.setGoal(playerPos);
        playerToCursor.partialScan(13, blockage);
        bgColor = SColor.DARK_SLATE_GRAY;
        colors = MapUtility.generateDefaultColors(decoDungeon);
        bgColors = MapUtility.generateDefaultBGColors(decoDungeon);
        // the line after this automatically sets the brightness of backgrounds in display to match their contents, so
        // here we simply fill the contents of display with our dungeon (but we don't set the actual colors yet).
        ArrayTools.insert(decoDungeon, display.getForegroundLayer().contents, 0, 0);
        MapUtility.fillLightnessModifiers(display.lightnesses, decoDungeon);
        //display.autoLight((System.currentTimeMillis() & 0xFFFFFFL) * 0.012);

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
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W': {
                        toCursor.clear();
                        //-1 is up on the screen
                        awaitedMoves.add(playerPos.translate(0, -1));
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S': {
                        toCursor.clear();
                        //1 is down on the screen
                        awaitedMoves.add(playerPos.translate(0, 1));
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A': {
                        toCursor.clear();
                        awaitedMoves.add(playerPos.translate(-1, 0));
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D': {
                        toCursor.clear();
                        awaitedMoves.add(playerPos.translate(1, 0));
                        break;
                    }

                    case SquidInput.UP_LEFT_ARROW:
                    case 'y':
                    case 'Y': {
                        toCursor.clear();
                        awaitedMoves.add(playerPos.translate(-1, -1));
                        break;
                    }
                    case SquidInput.UP_RIGHT_ARROW:
                    case 'u':
                    case 'U': {
                        toCursor.clear();
                        awaitedMoves.add(playerPos.translate(1, -1));
                        break;
                    }
                    case SquidInput.DOWN_RIGHT_ARROW:
                    case 'n':
                    case 'N': {
                        toCursor.clear();
                        awaitedMoves.add(playerPos.translate(1, 1));
                        break;
                    }
                    case SquidInput.DOWN_LEFT_ARROW:
                    case 'b':
                    case 'B': {
                        toCursor.clear();
                        awaitedMoves.add(playerPos.translate(-1, 1));
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
                    case 'f':
                    case 'F':{
                        currentCenter = (currentCenter + 1) % 10;
                        //currentCenter = (currentCenter + 1 & 1) + 8; // for testing red-green color blindness filter

                        // idx is 3 when we use the HallucinateFilter, which needs special work
                        changingColors = currentCenter == 0;
                        fgCenter = colorCenters[currentCenter * 2];
                        bgCenter = colorCenters[currentCenter * 2 + 1];
                        display.setFGColorCenter(fgCenter);
                        display.setBGColorCenter(bgCenter);
                        break;
                    }
                    case 'r': // red green color blindness mode on
                    {
                        changingColors = false;
                        fgCenter = colorCenters[18];
                        bgCenter = colorCenters[19];
                        display.setFGColorCenter(fgCenter);
                        display.setBGColorCenter(bgCenter);
                        break;
                    }
                    case 'R': // red green color blindness mode off
                    {
                        changingColors = false;
                        fgCenter = colorCenters[16];
                        bgCenter = colorCenters[17];
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
                int sx = screenX + display.getGridOffsetX(), sy = screenY + display.getGridOffsetY();
                if (fovmap[sx][sy] > 0.0 && awaitedMoves.isEmpty()) {
                    if (toCursor.isEmpty()) {
                        cursor = Coord.get(sx, sy);
                        //This uses DijkstraMap.findPathPreScannned() to get a path as a List of Coord from the current
                        // player position to the position the user clicked on. The "PreScanned" part is an optimization
                        // that's special to DijkstraMap; because the whole map has already been fully analyzed by the
                        // DijkstraMap.scan() method at the start of the program, and re-calculated whenever the player
                        // moves, we only need to do a fraction of the work to find the best path with that info.
                        toCursor = playerToCursor.findPathPreScanned(cursor);
                        //findPathPreScanned includes the current cell (goal) by default, which is helpful when
                        // you're finding a path to a monster or loot, and want to bump into it, but here can be
                        // confusing because you would "move into yourself" as your first move without this.
                        // Getting a sublist avoids potential performance issues with removing from the start of an
                        // ArrayList, since it keeps the original list around and only gets a "view" of it.
                        if(!toCursor.isEmpty())
                            toCursor = toCursor.subList(1, toCursor.size());

                    }
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
                if(!awaitedMoves.isEmpty())
                    return false;
                int sx = screenX + display.getGridOffsetX(), sy = screenY + display.getGridOffsetY();
                if((sx < 0 || sx >= totalWidth || sy < 0 || sy >= totalHeight)
                        || (cursor.x == sx && cursor.y == sy))
                {
                    return false;
                }
                if (fovmap[sx][sy] > 0.0) {
                    cursor = Coord.get(sx, sy);
                    //This uses DijkstraMap.findPathPreScannned() to get a path as a List of Coord from the current
                    // player position to the position the user clicked on. The "PreScanned" part is an optimization
                    // that's special to DijkstraMap; because the whole map has already been fully analyzed by the
                    // DijkstraMap.scan() method at the start of the program, and re-calculated whenever the player
                    // moves, we only need to do a fraction of the work to find the best path with that info.
                    toCursor = playerToCursor.findPathPreScanned(cursor);
                    //findPathPreScanned includes the current cell (goal) by default, which is helpful when
                    // you're finding a path to a monster or loot, and want to bump into it, but here can be
                    // confusing because you would "move into yourself" as your first move without this.
                    // Getting a sublist avoids potential performance issues with removing from the start of an
                    // ArrayList, since it keeps the original list around and only gets a "view" of it.
                    if(!toCursor.isEmpty())
                        toCursor = toCursor.subList(1, toCursor.size());

                }
                return false;
            }
        }));
        //set this to true to test visual input on desktop
        //input.forceButtons = true;
        //actions to give names to in the visual input menu
        //input.init("filter", "??? help?", "quit");
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(input, messageStage));
        //subCell.setOffsetY(messages.getGridHeight() * cellHeight);
        // and then add display and messages, our two visual components, to the list of things that act in Stage.
        stage.addActor(display);
        // stage.addActor(subCell); // this is not added since it is manually drawn after other steps
        messageStage.addActor(messages);
        //viewport = input.resizeInnerStage(stage);
        screenPosition = new Vector2(cellWidth, cellHeight);
    }

    /**
     * Move the player or open closed doors, remove any monsters the player bumped, then update the DijkstraMap and
     * have the monsters that can see the player try to approach.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     *
     * @param xmod
     * @param ymod
     */
    private void move(final int xmod, final int ymod) {
        clearHelp();

        if (health <= 0) return;
        int newX = player.gridX + xmod, newY = player.gridY + ymod;
        float midX = player.gridX + xmod * 0.5f, midY = player.gridY + ymod * 0.5f;
        if (newX >= 0 && newY >= 0 && newX < totalWidth && newY < totalHeight
                && bareDungeon[newX][newY] != '#') {
            // '+' is a door.
            if (lineDungeon[newX][newY] == '+') {
                decoDungeon[newX][newY] = '/';
                lineDungeon[newX][newY] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(decoDungeon);
                floors.refill(res, 0.99);
                // recalculate FOV, store it in fovmap for the render to use.
                FOV.reuseFOV(res, fovmap, player.gridX, player.gridY, 8, Radius.CIRCLE);
                blockage.refill(fovmap, 0.0);
                seen.or(currentlySeen.remake(blockage.not()));
                blockage.fringe8way();

            } else {
                // recalculate FOV, store it in fovmap for the render to use.
                FOV.reuseFOV(res, fovmap, newX, newY, 8, Radius.CIRCLE);
                blockage.refill(fovmap, 0.0);
                seen.or(currentlySeen.remake(blockage.not()));
                blockage.fringe8way();
                //player.gridX = newX;
                //player.gridY = newY;

                if(monsters.remove(Coord.get(newX, newY)) != null)
                {
                    display.addAction(new PanelEffect.ExplosionEffect(display.getBackgroundLayer(), 1f,
                            currentlySeen, Coord.get(newX, newY), 5));
                }
                //display.setGridOffsetX(newX - (width >> 1));
                //display.setGridOffsetY(newY - (height >> 1));
                //display.slideWorld(xmod, ymod, -1);
                final Vector3 pos = camera.position.cpy(), original = camera.position.cpy(),
                        nextPos = camera.position.cpy().add(
                                midX > totalWidth - (width + 1) * 0.5f || midX < (width + 1) * 0.5f ? 0 : (xmod * cellWidth),
                                midY > totalHeight - (height + 1) * 0.5f || midY < (height + 1) * 0.5f ? 0 : (-ymod * cellHeight),
                                0);
                display.slide(player, newX, newY);
                playerPos = playerPos.translate(xmod, ymod);
                display.addAction(
                        new TemporalAction(display.getAnimationDuration()) {
                            @Override
                            protected void update(float percent) {
                                pos.lerp(nextPos, percent);
                                camera.position.set(pos);
                                pos.set(original);
                                camera.update();
                            }
                            @Override
                            protected void end() {
                                super.end();
                                display.setGridOffsetX(player.gridX - (width >> 1));
                                display.setGridOffsetY(player.gridY - (height >> 1));
                                camera.position.set(original);
                                camera.update();

                            }
                        });
            }
            phase = Phase.PLAYER_ANIM;
        }
    }

    // check if a monster's movement would overlap with another monster.
    private boolean checkOverlap(Monster mon, int x, int y, ArrayList<Coord> futureOccupied) {
        if (monsters.containsPosition(Coord.get(x, y)) && !mon.equals(monsters.get(Coord.get(x, y))))
            return true;
        for (Coord p : futureOccupied) {
            if (x == p.x && y == p.y)
                return true;
        }
        return false;
    }

    private void postMove() {

        phase = Phase.MONSTER_ANIM;
        Coord[] playerArray = {Coord.get(player.gridX, player.gridY)};
        OrderedSet<Coord> monplaces = monsters.positions();
        int monCount = monplaces.size();

        // recalculate FOV, store it in fovmap for the render to use.
        FOV.reuseFOV(res, fovmap, player.gridX, player.gridY, 8, Radius.CIRCLE);
        blockage.refill(fovmap, 0.0);
        seen.or(currentlySeen.remake(blockage.not()));
        blockage.fringe8way();
        // handle monster turns
        ArrayList<Coord> nextMovePositions;
        for(int ci = 0; ci < monCount; ci++)
        {
            Coord pos = monplaces.removeFirst();
            Monster mon = monsters.get(pos);
            //mon.entity.actor.setPosition(fg.adjustX(pos.x, false), fg.adjustY(pos.y));
            // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
            if (mon.state > 0 || fovmap[pos.x][pos.y] > 0.1) {
                if (mon.state == 0) {
                    messages.appendMessage("The AЯMED GUAЯD shouts at you, \"" +
                            FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 1, 3,
                                    new String[]{",", ",", ",", " -"}, new String[]{"!"}, 0.25) + "\"");
//                    display.addAction(PanelEffect.makeGrenadeEffect(new PanelEffect.ProjectileEffect(display.getForegroundLayer(), 0.6f, floors, pos, playerArray[0], '*', SColor.DB_GRAPHITE),
//                            new PanelEffect.ExplosionEffect(display.getForegroundLayer(), 0.8f, floors, playerArray[0], 6)));
                    display.addAction(new PanelEffect.GlowBallEffect(display.getBackgroundLayer(), 0.5f,
                            floors, pos, playerArray[0], 1, SColor.CW_BRIGHT_RED));
                }
                getToPlayer.clearGoals();
                nextMovePositions = getToPlayer.findPath(1, monplaces, null, pos, playerArray);
                if (nextMovePositions != null && !nextMovePositions.isEmpty()) {
                    Coord tmp = nextMovePositions.get(0);
                    // if we would move into the player, instead damage the player and give newMons the current
                    // position of this monster.
                    if (tmp.x == player.gridX && tmp.y == player.gridY) {
                        display.tint(player.gridX, player.gridY, SColor.PURE_CRIMSON, 0, 0.415f);
                        health--;
                        //player.setText("" + health);
                        monsters.positionalModify(pos, mon.change(1));
                        monplaces.add(pos);

                    }
                    // otherwise store the new position in newMons.
                    else {
                        /*if (fovmap[mon.getKey().x][mon.getKey().y] > 0.0) {
                            display.put(mon.getKey().x, mon.getKey().y, 'M', 11);
                        }*/
                        monsters.positionalModify(pos, mon.change(1));
                        monsters.move(pos, tmp);
                        display.slide(mon.entity, tmp.x, tmp.y);
                        //mon.entity.gridX = tmp.x;
                        //mon.entity.gridY = tmp.y;
                        //mon.entity.actor.setPosition(fg.adjustX(tmp.x, false), fg.adjustY(tmp.y));
                        monplaces.add(tmp);
                    }
                } else {
                    monsters.positionalModify(pos, mon.change(1));
                    monplaces.add(pos);
                }
                /*
                // this block is used to ensure that the monster picks the best path, or a random choice if there
                // is more than one equally good best option.
                Direction choice = null;
                double best = 9990.0;
                Direction[] ds = new Direction[8];
                rng.shuffle(Direction.OUTWARDS, ds);
                for (Direction d : ds) {
                    Coord tmp = pos.translate(d);
                    if (monPathMap[tmp.x][tmp.y] < best &&
                            !checkOverlap(mon, tmp.x, tmp.y, nextMovePositions)) {
                        // pathMap is a 2D array of doubles where 0 is the goal (the player).
                        // we use best to store which option is closest to the goal.
                        best = monPathMap[tmp.x][tmp.y];
                        choice = d;
                    }
                }
                if (choice != null) {
                    Coord tmp = pos.translate(choice);
                    // if we would move into the player, instead damage the player and give newMons the current
                    // position of this monster.
                    if (tmp.x == player.gridX && tmp.y == player.gridY) {
                        display.tint(player.gridX, player.gridY, SColor.PURE_CRIMSON, 0, 0.415f);
                        health--;
                        //player.setText("" + health);
                        monsters.positionalModify(pos, mon.change(1));
                    }
                    // otherwise store the new position in newMons.
                    else {
                        nextMovePositions.add(tmp);
                        monsters.positionalModify(pos, mon.change(1));
                        monsters.move(pos, tmp);
                        display.slide(mon.entity, tmp.x, tmp.y);

                    }
                } else {
                    monsters.positionalModify(pos, mon.change(1));
                }
                */
            }
            else {
                monplaces.add(pos);
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
        IColoredString<Color> helping4 = new IColoredString.Impl<>("Each Я is an AЯMED GUAЯD; bump into them to kill them.", Color.WHITE);
        IColoredString<Color> helping5 = new IColoredString.Impl<>("If an Я starts its turn next to where you just moved, you take damage.", Color.WHITE);
        
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
        text.add(helping4);
        text.add(helping5);

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
        int offsetX = display.getGridOffsetX(), offsetY = display.getGridOffsetY();

        // this will very occasionally go from very high to very low, but if a very large long for time is multiplied
        // by a float, then you generally will get Float.POSITIVE_INFINITY, and similarly for some doubles. Infinite
        // results are not good for the smooth noise we use the current time for! We want the time to go up slowly and
        // steadily, so the animation of the "torchlight" effect looks right.
        long tm = System.currentTimeMillis() & 0xFFFFFF;
        for (int i = -1, ci = Math.max(0, offsetX-1); i <= width && ci < totalWidth; i++, ci++) {
            for (int j = -1, cj = Math.max(0, offsetY-1); j <= height && cj < totalHeight; j++, cj++) {
                overlapping = monsters.containsPosition(Coord.get(ci, cj)) || (player.gridX == ci && player.gridY == cj);
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being brighter at +75 lightness and 0.0 being rather dark at -105.
                if (fovmap[ci][cj] > 0.0) {
                    if(currentCenter > 5)
                        display.put(ci, cj, (overlapping) ? ' ' : lineDungeon[ci][cj], fgCenter.filter(colors[ci][cj]), bgCenter.filter(bgColors[ci][cj]),
                            (int) (-95 +
                                    160 * (fovmap[ci][cj] * (1.0 + 0.2 * SeededNoise.noise(ci * 0.2, cj * 0.2, tm * 0.0004, 10000)))));
                    else
                        display.put(ci, cj,  (overlapping) ? ' ' : lineDungeon[ci][cj], fgCenter.filter(colors[ci][cj]), bgCenter.filter(bgColors[ci][cj]),
                                0);
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                } else if (seen.contains(ci, cj)) {
                    display.put(ci, cj, lineDungeon[ci][cj], fgCenter.filter(colors[ci][cj]), bgCenter.filter(bgColors[ci][cj]), -140);
                }
            }
        }
        Coord pt;
        for (int i = 0; i < toCursor.size(); i++) {
            pt = toCursor.get(i);
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.highlight(pt.x, pt.y, (int) (170 * fovmap[pt.x][pt.y]));
        }
        messages.putBordersCaptioned(SColor.FLOAT_WHITE,
                GDXMarkup.instance.colorString("Health: [Red Pigment][*]" + health
                        + "[], Mana: [CW Azure][/]0[], Groove: [Psychedelic Purple][*][/]"
                        + (int)(SeededNoise.noise(player.gridX * 0.01, player.gridY * 0.01, tm * 0.00015, 1999) * 4 + 5)));
        //if(pt != null)
        //    display.putString(0, 0, String.valueOf(monPathMap[pt.x][pt.y]));
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(currentCenter > 5) // this does the standard lighting for walls, floors, etc. but also uses the time to do the Simplex noise thing.
            display.autoLight((System.currentTimeMillis() & 0xFFFFFFL) * 0.012);
        else // this does standard lighting without Simplex noise.
            MapUtility.fillLightnessModifiers(display.lightnesses, decoDungeon);
        // you done bad. you done real bad.
        if (health <= 0) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(width / 2 - 18 + fg.getGridOffsetX(), height / 2 - 10 + fg.getGridOffsetY(), "   THE TSAR WILL HAVE YOUR HEAD!    ");
            display.putBoxedString(width / 2 - 18 + fg.getGridOffsetX(), height / 2 - 5 + fg.getGridOffsetY(), "      AS THE OLD SAYING GOES,       ");
            display.putBoxedString(width / 2 - lang.length() / 2 + fg.getGridOffsetX(), height / 2 + fg.getGridOffsetY(), lang);
            display.putBoxedString(width / 2 - 18 + fg.getGridOffsetX(), height / 2 + 5 + fg.getGridOffsetY(), "             q to quit.             ");

            // because we return early, we still need to draw.
            messageViewport.apply(false);
            messageStage.draw();
            viewport.apply(false);
            stage.draw();
            // q still needs to quit.
            if (input.hasNext())
                input.next();
            return;
        }
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if (!awaitedMoves.isEmpty()) {

            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            if (!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 2) {
                    framesWithoutAnimation = 0;
                    switch (phase) {
                        case WAIT:
                        case MONSTER_ANIM:
                            Coord m = awaitedMoves.remove(0);
                            if(!toCursor.isEmpty())
                                toCursor.remove(0);
                            move(m.x - player.gridX, m.y - player.gridY);
                            // this only happens if we just removed the last Coord from awaitedMoves, and it's only then that we need to
                            // re-calculate the distances from all cells to the player. We don't need to calculate this information on
                            // each part of a many-cell move (just the end), nor do we need to calculate it whenever the mouse moves.
                            if(awaitedMoves.isEmpty())
                            {
                                // the next two lines remove any lingering data needed for earlier paths
                                playerToCursor.clearGoals();
                                playerToCursor.resetMap();
                                // the next line marks the player as a "goal" cell, which seems counter-intuitive, but it works because all
                                // cells will try to find the distance between themselves and the nearest goal, and once this is found, the
                                // distances don't change as long as the goals don't change. Since the mouse will move and new paths will be
                                // found, but the player doesn't move until a cell is clicked, the "goal" is the non-changing cell, so the
                                // player's position, and the "target" of a pathfinding method like DijkstraMap.findPathPreScanned() is the
                                // currently-moused-over cell, which we only need to set where the mouse is being handled.
                                playerToCursor.setGoal(m);
                                playerToCursor.partialScan(13, blockage);
                            }

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
            if (framesWithoutAnimation >= 2) {
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


        // the order here matters. We apply two viewports at different times to clip different areas.
        messageViewport.apply(false);
        // you do need to tell each Stage to act().
        messageStage.act();
        // ... just like you need to tell each stage to draw().
        messageStage.draw();
        stage.act();
        //here we apply the other viewport, which clips a different area while leaving the message area intact.
        viewport.apply(false);
        // each stage has its own batch that it starts an ends, so certain batch-wide effects only change one stage.
        stage.draw();
        if (help == null) {
            screenPosition.set(cellWidth * 5, cellHeight);
            stage.screenToStageCoordinates(screenPosition);
            // display does not draw all AnimatedEntities by default, since FOV often changes how they need to be drawn.
            batch.begin();
            // the player needs to get drawn every frame, of course.
            display.drawActor(batch, 1.0f, player);

            for (Monster mon : monsters) {
                // monsters are only drawn if within FOV.
                if (fovmap[mon.entity.gridX][mon.entity.gridY] > 0.0) {
                    display.drawActor(batch, 1.0f, mon.entity);
                }
            }
            display.getTextFactory().draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", screenPosition.x, screenPosition.y);

            //subCell.draw(batch, 1.0F);
            // batch must end if it began.
            batch.end();
        }
        // if using a filter that changes each frame, clear the known relationship between requested and actual colors
        if (changingColors) {
            fgCenter.clearCache();
            bgCenter.clearCache();
        }
        Gdx.graphics.setTitle("SquidLib Everything Demo running at FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // message box won't respond to clicks on the far right if the stage hasn't been updated with a larger size
        currentZoomX = (float)width / this.width;
        // total new screen height in pixels divided by total number of rows on the screen
        currentZoomY = (float)height / (this.height + messages.getGridHeight());
        // message box should be given updated bounds since I don't think it will do this automatically
        messages.setBounds(0, 0, width, currentZoomY * messages.getGridHeight());
        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
        input.getMouse().reinitialize(currentZoomX, currentZoomY, this.width, this.height, 0, 0);
        currentZoomX = cellWidth / currentZoomX;
        currentZoomY = cellHeight / currentZoomY;
        messageViewport.update(width, height, false);
        messageViewport.setScreenBounds(0, 0, width, (int)messages.getHeight());
        //input.update(width, height, false);
        viewport.update(width, height, false);
        viewport.setScreenBounds(0, (int)messages.getHeight(), width, height - (int)messages.getHeight());
    }
}