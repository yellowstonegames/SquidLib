package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.FakeLanguageGen;
import squidpony.NaturalLanguageCipher;
import squidpony.StringKit;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * The main class of the game, constructed once in each of the platform-specific Launcher classes. Doesn't use any
 * platform-specific code.
 */
// If this is an example project in SquidSetup, then squidlib-util (the core of squidlib's logic code, including
// map generation and pathfinding), RegExodus (a dependency of squidlib-util, used for validating generated text and
// some other usages in a way that works cross-platform) and the squidlib (the text-based display module) are always
// dependencies. If you didn't change those dependencies, this class should run out of the box.
//
// This class is useful as a starting point, since it has dungeon generation and some of the trickier parts of input
// handling (using the mouse to get a path for the player) already filled in. You can remove any imports or usages of
// classes that you don't need.

// A main game class that uses LibGDX to display, which is the default for SquidLib, needs to extend ApplicationAdapter
// or something related, like Game. Game adds features that SquidLib doesn't currently use, so ApplicationAdapter is
// perfectly fine for these uses.
public class BasicDemo2 extends ApplicationAdapter {
    SpriteBatch batch;

    private RNG rng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private Color[][] colors, bgColors;

    //Here, gridHeight refers to the total number of rows to be displayed on the screen.
    //We're displaying 30 rows of dungeon, then 7 more rows of text generation to show some tricks with language.
    //gridHeight is 30 because that variable will be used for generating the dungeon and handling movement within
    //the upper 30 rows. The bonusHeight is the number of additional rows that aren't handled like the dungeon rows for
    //UI reasons; here we show text generation in them. Next is gridWidth, which is 90 because we want 90 grid spaces
    //across the whole screen. cellWidth and cellHeight are 9 and 19, which will match the starting dimensions of a
    //cell, but won't be stuck at that value because we use a "Stretchable" font, and the cells can change size. While
    //gridWidth and gridHeight are measured in spaces on the grid, cellWidth and cellHeight are the initial pixel
    //dimensions of one cell. If the screen is resized, one pixel on the original screen will take up more or less
    //space on the resized screen, but the same measurements will be used (usually called world coordinates by libGDX).

    /** In number of cells */
    public static final int gridWidth = 80;
    /** In number of cells */
    public static final int gridHeight = 30;
    /** In number of cells */
    public static final int bonusHeight = 7;
    /** The initial pixel width of a cell */
    public static final int cellWidth = 9;
    /** The initial pixel height of a cell */
    public static final int cellHeight = 17;

    private SquidInput input;
    private Color bgColor;
    private Stage stage;
    private DijkstraMap playerToCursor;
    private Coord cursor, playerPosition;
    private AnimatedEntity playerMobile;
    private List<Coord> toCursor;
    private List<Coord> awaitedMoves;
    private final String artOfWar =
            "Sun Tzu said: In the practical art of war, the best thing of all is to take the" +
            " enemy's country whole and intact; to shatter and destroy it is not so good. So" +
            ", too, it is better to recapture an army entire than to destroy it, to capture " +
            "a regiment, a detachment or a company entire than to destroy them. Hence to fig" +
            "ht and conquer in all your battles is not supreme excellence; supreme excellenc" +
            "e consists in breaking the enemy's resistance without fighting.";
    private ArrayList<String> lang;
    private NaturalLanguageCipher translator;
    private char[] line;
    private double[][] resistance;
    private double[][] visible;
    private GreasedRegion blockage;
    private GreasedRegion seen;

    @Override
    public void create() {
        // gotta have a random number generator. We can seed an RNG with any long we want, or even a String.
        rng = new RNG("SquidLib!");

        //Some classes in SquidLib need access to a batch to render certain things, so it's a good idea to have one.
        batch = new SpriteBatch();
        //Here we make sure our Stage, which holds any text-based grids we make, uses our Batch.
        stage = new Stage(new StretchViewport(gridWidth * cellWidth, (gridHeight + bonusHeight) * cellHeight), batch);
        // the font will try to load Iosevka Slab as an embedded bitmap font with a distance field effect.
        // the distance field effect allows the font to be stretched without getting blurry or grainy too easily.
        // this font is covered under the SIL Open Font License (fully free), so there's no reason it can't be used.
        display = new SquidLayers(gridWidth, gridHeight + bonusHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableSlabFont().setSmoothingMultiplier(1.625f));
        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        display.setTextSize(cellWidth * 1.1f, cellHeight * 1.1f);

        // this makes animations medium-slow, which makes multi-cell movement slower but nicer-looking.
        display.setAnimationDuration(0.11f);

        //These need to have their positions set before adding any entities if there is an offset involved.
        //There is no offset used here, but it's still a good practice here to set positions early on.
        display.setPosition(0, 0);

        //This uses the seeded RNG we made earlier to build a procedural dungeon using a method that takes rectangular
        //sections of pre-drawn dungeon and drops them into place in a tiling pattern. It makes good "ruined" dungeons.
        dungeonGen = new DungeonGenerator(gridWidth, gridHeight, rng);
        //uncomment this next line to randomly add water to the dungeon in pools.
        //dungeonGen.addWater(15);
        //decoDungeon is given the dungeon with any decorations we specified. (Here, we didn't, unless you chose to add
        //water to the dungeon. In that case, decoDungeon will have different contents than bareDungeon, next.)
        decoDungeon = dungeonGen.generate();
        //getBareDungeon provides the simplest representation of the generated dungeon -- '#' for walls, '.' for floors.
        bareDungeon = dungeonGen.getBareDungeon();
        //When we draw, we may want to use a nicer representation of walls. DungeonUtility has lots of useful methods
        //for modifying char[][] dungeon grids, and this one takes each '#' and replaces it with a box-drawing character.
        lineDungeon = DungeonUtility.hashesToLines(decoDungeon);

        resistance = DungeonUtility.generateResistances(bareDungeon);
        visible = new double[gridWidth][gridHeight];

        //Coord is the type we use as a general 2D point, usually in a dungeon.
        //Because we know dungeons won't be incredibly huge, Coord performs best for x and y values less than 256, but
        // by default it can also handle some negative x and y values (-3 is the lowest it can efficiently store). You
        // can call Coord.expandPool() or Coord.expandPoolTo() if you need larger maps to be just as fast.
        cursor = Coord.get(-1, -1);
        // here, we need to get a random floor cell to place the player upon, without the possibility of putting him
        // inside a wall. There are a few ways to do this in SquidLib. The most straightforward way is to randomly
        // choose x and y positions until a floor is found, but particularly on dungeons with few floor cells, this can
        // have serious problems -- if it takes too long to find a floor cell, either it needs to be able to figure out
        // that random choice isn't working and instead choose the first it finds in simple iteration, or potentially
        // keep trying forever on an all-wall map. There are better ways! These involve using a kind of specific storage
        // for points or regions, getting that to store only floors, and finding a random cell from that collection of
        // floors. The two kinds of such storage used commonly in SquidLib are the "packed data" as short[] produced by
        // CoordPacker (which use very little memory, but can be slow, and are treated as unchanging by CoordPacker so
        // any change makes a new array), and GreasedRegion objects (which use slightly more memory, tend to be faster
        // on almost all operations compared to the same operations with CoordPacker, and default to changing the
        // GreasedRegion object when you call a method on it instead of making a new one). Even though CoordPacker
        // sometimes has better documentation, GreasedRegion is generally a better choice; it was added to address
        // shortcomings in CoordPacker, particularly for speed, and the worst-case scenarios for data in CoordPacker are
        // no problem whatsoever for GreasedRegion. CoordPacker is called that because it compresses the information
        // for nearby Coords into a smaller amount of memory. GreasedRegion is called that because it encodes regions,
        // but is "greasy" both in the fatty-food sense of using more space, and in the "greased lightning" sense of
        // being especially fast. Both of them can be seen as storing regions of points in 2D space as "on" and "off."

        // Here we fill a GreasedRegion so it stores the cells that contain a floor, the '.' char, as "on."
        final GreasedRegion placement = new GreasedRegion(bareDungeon, '.');
        //player is, here, just a Coord that stores his position. In a real game, you would probably have a class for
        //creatures, and possibly a subclass for the player. The singleRandom() method on GreasedRegion finds one Coord
        // in that region that is "on," or -1,-1 if there are no such cells. It takes an RNG object as a parameter, and
        // if you gave a seed to the RNG constructor, then the cell this chooses will be reliable for testing. If you
        // don't seed the RNG, any valid cell should be possible.
        playerPosition = placement.singleRandom(rng);
        playerMobile = display.animateActor(playerPosition.x, playerPosition.y, '@', SColor.CW_FLUSH_BLUE);
        // Uses shadowcasting FOV and reuses the visible array without creating new arrays constantly.
        FOV.reuseFOV(resistance, visible, playerPosition.x, playerPosition.y, 9.0, Radius.CIRCLE);
        // 0.0 is the upper bound (inclusive), so any Coord in visible that is more well-lit than 0.0 will _not_ be in
        // the blockage Collection, but anything 0.0 or less will be in it. This lets us use blockage to prevent access
        // to cells we can't see from the start of the move.
        blockage = new GreasedRegion(visible, 0.0);
        // Here we mark the initially seen cells as anything that wasn't included in the unseen "blocked" region.
        // We invert the copy's contents to prepare for a later step, which makes blockage contain only the cells that
        // are above 0.0, then copy it to save this step as the seen cells. We will modify seen later independently of
        // the blocked cells, so a copy is correct here. Most methods on GreasedRegion objects will modify the
        // GreasedRegion they are called on, which can greatly help efficiency on long chains of operations.
        seen = blockage.not().copy();
        // Here is one of those methods on a GreasedRegion; fringe8way takes a GreasedRegion (here, the set of cells
        // that are visible to the player), and modifies it to contain only cells that were not in the last step, but
        // were adjacent to a cell that was present in the last step. This can be visualized as taking the area just
        // beyond the border of a region, using 8-way adjacency here because we specified fringe8way instead of fringe.
        // We do this because it means pathfinding will only have to work with a small number of cells (the area just
        // out of sight, and no further) instead of all invisible cells when figuring out if something is currently
        // impossible to enter.
        blockage.fringe8way();
        //This is used to allow clicks or taps to take the player to the desired area.
        toCursor = new ArrayList<>(200);
        //When a path is confirmed by clicking, we draw from this List to find which cell is next to move into.
        awaitedMoves = new ArrayList<>(200);
        // DijkstraMap is the pathfinding swiss-army knife we use here to find a path to the latest cursor position.
        // DijkstraMap.Measurement is an enum that determines the possibility or preference to enter diagonals. Here,
        // MANHATTAN is used, which means 4-way movement only, no diagonals possible. Alternatives are CHEBYSHEV, which
        // allows 8 directions of movement at the same cost for all directions, and EUCLIDEAN, which allows 8
        // directions, but will prefer orthogonal moves unless diagonal ones are clearly closer "as the crow flies."
        // EUCLIDEAN is ideal for NPC movement when diagonals are allowed, because it will cause them to prefer paths in
        // straight lines when they are optimal or tied for optimal, while CHEBYSHEV will make erratic zig-zagging paths
        // just as optimal as straight lines, and that tends to make enemies look crazy or stupid.
        playerToCursor = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.MANHATTAN);
        //These next two lines mark the player as something we want paths to go to or from, and get the distances to the
        // player from all walkable cells in the dungeon.
        playerToCursor.setGoal(playerPosition);
        playerToCursor.scan(blockage);

        //The next three lines set the background color for anything we don't draw on, but also create 2D arrays of the
        //same size as decoDungeon that store simple indexes into a common list of colors, using the colors that looks
        // up as the colors for the cell with the same x and y.
        bgColor = SColor.DARK_SLATE_GRAY;
        SColor.LIMITED_PALETTE[3] = SColor.DB_GRAPHITE;
        colors = MapUtility.generateDefaultColors(decoDungeon);
        bgColors = MapUtility.generateDefaultBGColors(decoDungeon);
        lang = new ArrayList<>(16);
        StringKit.wrap(lang, artOfWar, gridWidth - 2);
        translator = new NaturalLanguageCipher(rng.getRandomElement(FakeLanguageGen.registered));
        StringKit.wrap(lang, translator.cipher(artOfWar), gridWidth - 2);
        translator.initialize(rng.getRandomElement(FakeLanguageGen.registered), 0L);

        // this is a big one.
        // SquidInput can be constructed with a KeyHandler (which just processes specific keypresses), a SquidMouse
        // (which is given an InputProcessor implementation and can handle multiple kinds of mouse move), or both.
        // keyHandler is meant to be able to handle complex, modified key input, typically for games that distinguish
        // between, say, 'q' and 'Q' for 'quaff' and 'Quip' or whatever obtuse combination you choose. The
        // implementation here handles hjkl keys (also called vi-keys), numpad, arrow keys, and wasd for 4-way movement.
        // Shifted letter keys produce capitalized chars when passed to KeyHandler.handle(), but we don't care about
        // that so we just use two case statements with the same body, i.e. one for 'A' and one for 'a'.
        // You can also set up a series of future moves by clicking within FOV range, using mouseMoved to determine the
        // path to the mouse position with a DijkstraMap (called playerToCursor), and using touchUp to actually trigger
        // the event when someone clicks.
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.UP_ARROW:
                        toCursor.clear();
                        //-1 is up on the screen
                        awaitedMoves.add(playerPosition.translate(0, -1));
                        break;
                    case SquidInput.DOWN_ARROW:
                        toCursor.clear();
                        //+1 is down on the screen
                        awaitedMoves.add(playerPosition.translate(0, 1));
                        break;
                    case SquidInput.LEFT_ARROW:
                        toCursor.clear();
                        awaitedMoves.add(playerPosition.translate(-1, 0));
                        break;
                    case SquidInput.RIGHT_ARROW:
                        toCursor.clear();
                        awaitedMoves.add(playerPosition.translate(1, 0));
                        break;
                    case SquidInput.UP_LEFT_ARROW:
                        toCursor.clear();
                        //-1 is up on the screen
                        awaitedMoves.add(playerPosition.translate(-1, -1));
                        break;
                    case SquidInput.UP_RIGHT_ARROW:
                        toCursor.clear();
                        //+1 is down on the screen
                        awaitedMoves.add(playerPosition.translate(1, -1));
                        break;
                    case SquidInput.DOWN_LEFT_ARROW:
                        toCursor.clear();
                        awaitedMoves.add(playerPosition.translate(-1, 1));
                        break;
                    case SquidInput.DOWN_RIGHT_ARROW:
                        toCursor.clear();
                        awaitedMoves.add(playerPosition.translate(1, 1));
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                        Gdx.app.exit();
                        break;
                    // Here, if you enter a '*' character (usually Shift+8) while holding Ctrl, a little effect appears.
                    case '*':
                        if (ctrl)
                            display.getForegroundLayer().burst(playerPosition.x, playerPosition.y, true, '@', SColor.CW_LIGHT_YELLOW, 0.75f);
                        break;
                }
            }
        },
                //The second parameter passed to a SquidInput can be a SquidMouse, which takes mouse or touchscreen
                //input and converts it to grid coordinates (here, a cell is 12 wide and 24 tall, so clicking at the
                // pixel position 15,51 will pass screenX as 1 (since if you divide 15 by 12 and round down you get 1),
                // and screenY as 2 (since 51 divided by 24 rounded down is 2)).
                new SquidMouse(cellWidth, cellHeight, gridWidth, gridHeight, 0, 0, new InputAdapter() {

                    // if the user clicks and there are no awaitedMoves queued up, generate toCursor if it
                    // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
                    @Override
                    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                        if (awaitedMoves.isEmpty()) {
                            if (toCursor.isEmpty()) {
                                cursor = Coord.get(screenX, screenY);
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
                                if (!toCursor.isEmpty()) {
                                    line = OrthoLine.lineChars(toCursor);
                                    toCursor = toCursor.subList(1, toCursor.size());
                                }
                            }
                            awaitedMoves.addAll(toCursor);
                        }
                        return true;
                    }

                    @Override
                    public boolean touchDragged(int screenX, int screenY, int pointer) {
                        return mouseMoved(screenX, screenY);
                    }

                    // causes the path to the mouse position to become highlighted (toCursor contains a list of Coords that
                    // receive highlighting). Uses DijkstraMap.findPathPreScanned() to find the path, which is rather fast.
                    @Override
                    public boolean mouseMoved(int screenX, int screenY) {
                        if (!awaitedMoves.isEmpty())
                            return false;
                        if (cursor.x == screenX && cursor.y == screenY) {
                            return false;
                        }
                        cursor = Coord.get(screenX, screenY);
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
                        if (!toCursor.isEmpty()) {
                            line = OrthoLine.lineChars(toCursor);
                            toCursor = toCursor.subList(1, toCursor.size());
                        }
                        return false;
                    }
                }));
        // each of these adds a different kind of input handling support
        // you can comment out any of the next 3 lines to disable wasd or vi-key keybindings.
        input.keyMappingFromString("AD←@DD→@SD↓@WD↑@a@←@d@→@s@↓@w@↑@"); // wasd 4-way movement
        input.keyMappingFromString("HD←@JD↓@KD↑@LD→@h@←@j@↓@k@↑@l@→@"); // hjkl vi-keys 4-way movement
        input.keyMappingFromString("BD↙@ND↘@UD↗@YD↖@b@↙@n@↘@u@↗@y@↖@"); // yubn vi-keys diagonal movement
        //this block is used to manually assign the key mapping, instead of from a string
//        input.remap('y', false, false, false, SquidInput.UP_LEFT_ARROW, false, false, false);
//        input.remap('Y', false, false, true, SquidInput.UP_LEFT_ARROW, false, false, false);
//        input.remap('b', false, false, false, SquidInput.DOWN_LEFT_ARROW, false, false, false);
//        input.remap('B', false, false, true, SquidInput.DOWN_LEFT_ARROW, false, false, false);
//        input.remap('n', false, false, false, SquidInput.DOWN_RIGHT_ARROW, false, false, false);
//        input.remap('N', false, false, true, SquidInput.DOWN_RIGHT_ARROW, false, false, false);
//        input.remap('u', false, false, false, SquidInput.UP_RIGHT_ARROW, false, false, false);
//        input.remap('U', false, false, true, SquidInput.UP_RIGHT_ARROW, false, false, false);
        // this we use during development to make the Strings passed to keyMappingFromString()
//        System.out.println(input.keyMappingToString());
        //Setting the InputProcessor is ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        //You might be able to get by with the next line instead of the above line, but the former is preferred.
        //Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        stage.addActor(display);

    }

    /**
     * Move the player if he isn't bumping into a wall or trying to go off the map somehow.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     *
     * @param xmod
     * @param ymod
     */
    private void move(int xmod, int ymod) {
        int newX = playerPosition.x + xmod, newY = playerPosition.y + ymod;
        if (newX >= 0 && newY >= 0 && newX < gridWidth && newY < gridHeight
                && bareDungeon[newX][newY] != '#') {
            display.slide(playerMobile, newX, newY);
            playerPosition = playerPosition.translate(xmod, ymod);
            FOV.reuseFOV(resistance, visible, playerPosition.x, playerPosition.y, 9.0, Radius.CIRCLE);
            // This is just like the constructor used earlier, but affects an existing GreasedRegion without making
            // a new one just for this movement.
            blockage.refill(visible, 0.0);
            seen.or(blockage.not());
            blockage.fringe8way();
        } else {
            display.bump(playerMobile, 1, Direction.getRoughDirection(xmod, ymod), 0.25f);
            display.getForegroundLayer().summon(0.35f, playerPosition.x, playerPosition.y, playerPosition.x, playerPosition.y - 1, '?', SColor.CW_BRIGHT_INDIGO, SColor.ELECTRIC_PURPLE.cpy().sub(0, 0, 0, 1), false, 0f, 0f, 0.8f);
            display.getBackgroundLayer().tint(playerPosition.x, playerPosition.y, SColor.PURPLE, 0.3f);
            display.getForegroundLayer().tint(newX, newY, SColor.OVERDYED_RED_BROWN, 0.4f);
            //display.getForegroundLayer().burst(0.1f, playerPosition.x, playerPosition.y, 2, true, '?', SColor.CW_BRIGHT_INDIGO, SColor.ELECTRIC_PURPLE.cpy().sub(0,0,0,1),  false, -1f, 0.8f);
        }
        // removes the first line displayed of the Art of War text or its translation.
        lang.remove(0);
        // if the last line reduced the number of lines we can show to less than what we try to show, we fill in more
        // lines using a randomly selected fake language to translate the same Art of War text.
        while (lang.size() < bonusHeight - 1)
        {
            StringKit.wrap(lang, translator.cipher(artOfWar), gridWidth - 2);
            translator.initialize(rng.getRandomElement(FakeLanguageGen.registered), 0L);
        }
    }

    /**
     * Draws the map, applies any highlighting for the path to the cursor, and then draws the player.
     */
    public void putMap() {
        double tm = (System.currentTimeMillis() & 0xffffffL) * 0.001;
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                if (visible[i][j] > 0.1) {
                    int bright = (int) (-65 +
                            180 * (visible[i][j] * (1.0 + 0.2 * SeededNoise.noise(i * 0.2, j * 0.2, tm, 10000))));
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], bright);
                } else if (seen.contains(i, j))
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], -80);
                else
                    display.put(i, j, ' ', SColor.BLACK);
            }
        }
        Coord pt;
        for (int i = 0; i < toCursor.size(); i++) {
            pt = toCursor.get(i);
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.put(pt.x, pt.y, line[i + 1], SColor.CW_FLUSH_JADE, bgColors[pt.x][pt.y], 130);
        }
        //this helps compatibility with the HTML target, which doesn't support String.format()
        char[] spaceArray = new char[gridWidth];
        Arrays.fill(spaceArray, ' ');
        String spaces = String.valueOf(spaceArray);

        for (int i = 0; i < bonusHeight - 1; i++) {
            display.putString(0, gridHeight + i + 1, spaces, SColor.BLACK, SColor.COSMIC_LATTE);
            display.putString(1, gridHeight + i + 1, lang.get(i), SColor.BLACK, SColor.COSMIC_LATTE);
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if (!awaitedMoves.isEmpty()) {
            if (!display.hasActiveAnimations()) {
                // this doesn't check for input, but instead processes and removes Coords from awaitedMoves.
                Coord m = awaitedMoves.remove(0);
                // sets up the line to the cursor from the player
                line = OrthoLine.lineChars(toCursor);
                if (!toCursor.isEmpty()) // this check is necessary because we can't remove from an empty list
                    toCursor.remove(0); // and this just removed the player's current position from the list
                // move takes the relative distance to move for x and y, instead of an absolute position, so this is
                // relative to the player's current position.
                move(m.x - playerPosition.x, m.y - playerPosition.y);
                // this only happens if we just removed the last Coord from awaitedMoves, and it's only then that we need to
                // re-calculate the distances from all cells to the player. We don't need to calculate this information on
                // each part of a many-cell move (just the end), nor do we need to calculate it whenever the mouse moves.
                if (awaitedMoves.isEmpty()) {
                    // the next two lines remove any lingering data needed for earlier paths
                    playerToCursor.clearGoals();
                    playerToCursor.resetMap();
                    // the next line marks the player as a "goal" cell, which seems counter-intuitive, but it works because all
                    // cells will try to find the distance between themselves and the nearest goal, and once this is found, the
                    // distances don't change as long as the goals don't change. Since the mouse will move and new paths will be
                    // found, but the player doesn't move until a cell is clicked, the "goal" is the non-changing cell, so the
                    // player's position, and the "target" of a pathfinding method like DijkstraMap.findPathPreScanned() is the
                    // currently-moused-over cell, which we only need to set where the mouse is being handled.
                    playerToCursor.setGoal(playerPosition);
                    playerToCursor.scan(blockage);
                }
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if (input.hasNext()) {
            input.next();
        }
        // certain classes that use scene2d.ui widgets need to be told to act() to process input.
        stage.act();
        // this next part is a minor optimization; of the next 6 lines of code, all but one are found in
        // the body of stage.draw(). The difference is, we add the line that calls display.drawActor(),
        // which if it was called separately from stage.draw(), would need stage to begin and end its
        // Batch before the same Batch had begin and end called again for just this actor. Each flush of
        // the Batch is slow, and happens on each batch.end(), so saving a flush every frame helps.
        Camera camera = stage.getCamera();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        stage.getRoot().draw(batch, 1f);
        display.drawActor(batch, 1f, playerMobile); // this line is different from Stage.draw()
        batch.end();
        Gdx.graphics.setTitle("Basic SquidLib Demo running at FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        //very important to have the mouse behave correctly if the user fullscreens or resizes the game!
        input.getMouse().reinitialize((float) width / gridWidth, (float) height / (gridHeight + bonusHeight), gridWidth, gridHeight, 0, 0);
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib GDX Basic Demo";
        config.width = gridWidth * cellWidth;
        config.height = (gridHeight + bonusHeight) * cellHeight;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.backgroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
        new LwjglApplication(new BasicDemo2(), config);
    }

}
