package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.NaturalLanguageCipher;
import squidpony.panel.IColoredString;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.LineKit;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a small, not-overly-simple demo that presents some important features of SquidLib and shows a faster,
 * cleaner, and more recently-introduced way of displaying the map and other text. Features include dungeon map
 * generation, field of view, pathfinding (to the mouse position), simplex noise (used for a flickering torch effect),
 * language generation/ciphering, a colorful glow effect, and ever-present random number generation (with a seed).
 * You can increase the size of the map on most target platforms (but GWT struggles with large... anything) by
 * changing gridHeight and gridWidth to affect the visible area or bigWidth and bigHeight to adjust the size of the
 * dungeon you can move through, with the camera following your '@' symbol.
 */
public class SparseDemo extends ApplicationAdapter {
    SpriteBatch batch;

    private IRNG rng;
    private SparseLayers display, languageDisplay;
    private DungeonGenerator dungeonGen;
    // decoDungeon stores the dungeon map with features like grass and water, if present, as chars like '"' and '~'.
    // bareDungeon stores the dungeon map with just walls as '#' and anything not a wall as '.'.
    // Both of the above maps use '#' for walls, and the next two use box-drawing characters instead.
    // lineDungeon stores the whole map the same as decoDungeon except for walls, which are box-drawing characters here.
    // prunedDungeon takes lineDungeon and adjusts it so unseen segments of wall (represented by box-drawing characters)
    //   are removed from rendering; unlike the others, it is frequently changed.
    private char[][] decoDungeon, bareDungeon, lineDungeon, prunedDungeon;
    private float[][] colors, bgColors;

    //Here, gridHeight refers to the total number of rows to be displayed on the screen.
    //We're displaying 25 rows of dungeon, then 7 more rows of text generation to show some tricks with language.
    //gridHeight is 25 because that variable will be used for generating the dungeon (the actual size of the dungeon
    //will be triple gridWidth and triple gridHeight), and determines how much off the dungeon is visible at any time.
    //The bonusHeight is the number of additional rows that aren't handled like the dungeon rows and are shown in a
    //separate area; here we use them for translations. The gridWidth is 90, which means we show 90 grid spaces
    //across the whole screen, but the actual dungeon is larger. The cellWidth and cellHeight are 10 and 20, which will
    //match the starting dimensions of a cell in pixels, but won't be stuck at that value because we use a "Stretchable"
    //font, and so the cells can change size (they don't need to be scaled by equal amounts, either). While gridWidth
    //and gridHeight are measured in spaces on the grid, cellWidth and cellHeight are the initial pixel dimensions of
    //one cell; resizing the window can make the units cellWidth and cellHeight use smaller or larger than a pixel.

    /** In number of cells */
    private static final int gridWidth = 90;
    /** In number of cells */
    private static final int gridHeight = 25;

    /** In number of cells */
    private static final int bigWidth = gridWidth * 2;
    /** In number of cells */
    private static final int bigHeight = gridHeight * 2;

    /** In number of cells */
    private static final int bonusHeight = 7;
    /** The pixel width of a cell */
    private static final int cellWidth = 10;
    /** The pixel height of a cell */
    private static final int cellHeight = 20;
    private SquidInput input;
    private Color bgColor;
    private Stage stage, languageStage;
    private DijkstraMap playerToCursor;
    private Coord cursor, player;
    private ArrayList<Coord> toCursor;
    private List<Coord> awaitedMoves;

    private Vector2 screenPosition;


    // a passage from the ancient text The Art of War, which remains relevant in any era but is mostly used as a basis
    // for translation to imaginary languages using the NaturalLanguageCipher and FakeLanguageGen classes.
    private final String artOfWar =
            "[@ 0.8 0.06329113 0.30980393][/]Sun Tzu[/] said: In the practical art of war, the best thing of all is " +
                    "to take the enemy's country whole and intact; to shatter and destroy it is not so good. So, " +
                    "too, it is better to recapture an army entire than to destroy it, to capture " +
                    "a regiment, a detachment or a company entire than to destroy them. Hence to fight " +
                    "and conquer in all your battles is not supreme excellence; supreme excellence " +
                    "consists in breaking the enemy's resistance without fighting.[]";
    // A translation dictionary for going back and forth between English and an imaginary language that this generates
    // words for, using some of the rules that the English language tends to follow to determine if two words should
    // share a common base word (such as "read" and "reader" needing similar translations). This is given randomly
    // selected languages from the FakeLanguageGen class, which is able to produce text that matches a certain style,
    // usually that of a natural language but some imitations of fictional languages, such as languages spoken by elves,
    // goblins, or demons, are present as well. An unusual trait of FakeLanguageGen is that it can mix two or more
    // languages to make a new one, which most other kinds of generators have a somewhat-hard time doing.
    private NaturalLanguageCipher translator;
    // this is initialized with the word-wrapped contents of artOfWar, then has translations of that text to imaginary
    // languages appended after the plain-English version. The contents have the first item removed with each step, and
    // have new translations added whenever the line count is too low.
    private ArrayList<IColoredString<Color>> lang;
    private double[][] resistance;
    private double[][] visible;
    // GreasedRegion is a hard-to-explain class, but it's an incredibly useful one for map generation and many other
    // tasks; it stores a region of "on" cells where everything not in that region is considered "off," and can be used
    // as a Collection of Coord points. However, it's more than that! Because of how it is implemented, it can perform
    // bulk operations on as many as 64 points at a time, and can efficiently do things like expanding the "on" area to
    // cover adjacent cells that were "off", retracting the "on" area away from "off" cells to shrink it, getting the
    // surface ("on" cells that are adjacent to "off" cells) or fringe ("off" cells that are adjacent to "on" cells),
    // and generally useful things like picking a random point from all "on" cells.
    // Here, we use a GreasedRegion to store all floors that the player can walk on, a small rim of cells just beyond
    // the player's vision that blocks pathfinding to areas we can't see a path to, and we also store all cells that we
    // have seen in the past in a GreasedRegion (in most roguelikes, there would be one of these per dungeon floor).
    private GreasedRegion floors, blockage, seen, currentlySeen;
    // a Glyph is a kind of scene2d Actor that only holds one char in a specific color, but is drawn using the behavior
    // of TextCellFactory (which most text in SquidLib is drawn with) instead of the different and not-very-compatible
    // rules of Label, which older SquidLib code used when it needed text in an Actor. Glyphs are also lighter-weight in
    // memory usage and time taken to draw than Labels.
    private TextCellFactory.Glyph pg;
    // libGDX can use a kind of packed float (yes, the number type) to efficiently store colors, but it also uses a
    // heavier-weight Color object sometimes; SquidLib has a large list of SColor objects that are often used as easy
    // predefined colors since SColor extends Color. SparseLayers makes heavy use of packed float colors internally,
    // but also allows Colors instead for most methods that take a packed float. Some cases, like very briefly-used
    // colors that are some mix of two other colors, are much better to create as packed floats from other packed
    // floats, usually using SColor.lerpFloatColors(), which avoids creating any objects. It's ideal to avoid creating
    // new objects (such as Colors) frequently for only brief usage, because this can cause temporary garbage objects to
    // build up and slow down the program while they get cleaned up (garbage collection, which is slower on Android).
    // Recent versions of SquidLib include the packed float literal in the JavaDocs for any SColor, along with previews
    // of that SColor as a background and foreground when used with other colors, plus more info like the hue,
    // saturation, and value of the color. Here we just use the packed floats directly from the SColor docs, but we also
    // mention what color it is in a line comment, which is a good habit so you can see a preview if needed.
    private static final float FLOAT_LIGHTING = -0x1.cff1fep126F, // same result as SColor.COSMIC_LATTE.toFloatBits()
            GRAY_FLOAT = -0x1.7e7e7ep125F; // same result as SColor.CW_GRAY_BLACK.toFloatBits()
    private FloatFilters.YCbCrFilter filter;

    /**
     * Briefly named because we may use it a lot; filters a packed float color with a FloatFilter and does no caching.
     * @param color a packed float color
     * @return a packed float color, filtered
     */
    private float f(float color)
    {
        return filter.alter(color);
    }
    /**
     * Briefly named because we may use it a lot; filters a libGDX Color with a FloatFilter and does no caching.
     * @param color a libGDX Color or a subclass like SColor
     * @return a packed float color, filtered
     */
    private float f(Color color)
    {
        return filter.alter(color);
    }
    @Override
    public void create () {
        // gotta have a random number generator. We can seed an RNG with any long we want, or even a String.
        // if the seed is identical between two runs, any random factors will also be identical (until user input may
        // cause the usage of an RNG to change). You can randomize the dungeon and several other initial settings by
        // just removing the String seed, making the line "rng = new RNG();" . Keeping the seed as a default allows
        // changes to be more easily reproducible, and using a fixed seed is strongly recommended for tests. 
        rng = new RNG(artOfWar);
        // testing FloatFilter; YCbCrFilter multiplies the brightness (Y) and chroma (Cb, Cr) of a color 
        filter = new FloatFilters.YCbCrFilter(0.875f, 0.6f, 0.6f);

        //Some classes in SquidLib need access to a batch to render certain things, so it's a good idea to have one.
        batch = new SpriteBatch();
        StretchViewport mainViewport = new StretchViewport(gridWidth * cellWidth, gridHeight * cellHeight),
                languageViewport = new StretchViewport(gridWidth * cellWidth, bonusHeight * cellHeight);
        mainViewport.setScreenBounds(0, 0, gridWidth * cellWidth, gridHeight * cellHeight);
        languageViewport
                .setScreenBounds(0, 0, gridWidth * cellWidth, bonusHeight * cellHeight);
        //Here we make sure our Stage, which holds any text-based grids we make, uses our Batch.
        stage = new Stage(mainViewport, batch);
        languageStage = new Stage(languageViewport, batch);
        // the font will try to load Iosevka Slab as an embedded bitmap font with a MSDF effect (multi scale distance
        // field, a way to allow a bitmap font to stretch while still keeping sharp corners and round curves).
        // the MSDF effect is handled internally by a shader in SquidLib, and will switch to a different shader if a SDF
        // effect is used (SDF is called "Stretchable" in DefaultResources, where MSDF is called "Crisp").
        // this font is covered under the SIL Open Font License (fully free), so there's no reason it can't be used.
        // it also includes 4 text faces (regular, bold, oblique, and bold oblique) so methods in GDXMarkup can make
        // italic or bold text without switching fonts (they can color sections of text too).
        display = new SparseLayers(bigWidth, bigHeight + bonusHeight, cellWidth, cellHeight,
                DefaultResources.getCrispSlabFamily());

        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        display.font.tweakWidth(cellWidth * 1.075f).tweakHeight(cellHeight * 1.1f).initBySize();

        languageDisplay = new SparseLayers(gridWidth, bonusHeight - 1, cellWidth, cellHeight, display.font);
        // SparseDisplay doesn't currently use the default background fields, but this isn't really a problem; we can
        // set the background colors directly as floats with the SparseDisplay.backgrounds field, and it can be handy
        // to hold onto the current color we want to fill that with in the defaultPackedBackground field.
        // SparseLayers has fillBackground() and fillArea() methods for coloring all or part of the backgrounds.
        languageDisplay.defaultPackedBackground = FLOAT_LIGHTING; // happens to be the same color used for lighting

        //This uses the seeded RNG we made earlier to build a procedural dungeon using a method that takes rectangular
        //sections of pre-drawn dungeon and drops them into place in a tiling pattern. It makes good winding dungeons
        //with rooms by default, but in the later call to dungeonGen.generate(), you can use a TilesetType such as
        //TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS or TilesetType.CAVES_LIMIT_CONNECTIVITY to change the sections that
        //this will use, or just pass in a full 2D char array produced from some other generator, such as
        //SerpentMapGenerator, OrganicMapGenerator, or DenseRoomMapGenerator.
        dungeonGen = new DungeonGenerator(bigWidth, bigHeight, rng);
        //uncomment this next line to randomly add water to the dungeon in pools.
        //dungeonGen.addWater(15);
        //decoDungeon is given the dungeon with any decorations we specified. (Here, we didn't, unless you chose to add
        //water to the dungeon. In that case, decoDungeon will have different contents than bareDungeon, next.)
        decoDungeon = dungeonGen.generate();
        //getBareDungeon provides the simplest representation of the generated dungeon -- '#' for walls, '.' for floors.
        bareDungeon = dungeonGen.getBareDungeon();
        //When we draw, we may want to use a nicer representation of walls. DungeonUtility has lots of useful methods
        //for modifying char[][] dungeon grids, and this one takes each '#' and replaces it with a box-drawing char.
        //The end result looks something like this, for a smaller 60x30 map:
        //
        // ┌───┐┌──────┬──────┐┌──┬─────┐   ┌──┐    ┌──────────┬─────┐
        // │...││......│......└┘..│.....│   │..├───┐│..........│.....└┐
        // │...││......│..........├──┐..├───┤..│...└┴────......├┐.....│
        // │...││.................│┌─┘..│...│..│...............││.....│
        // │...││...........┌─────┘│....│...│..│...........┌───┴┴───..│
        // │...│└─┐....┌───┬┘      │........│..│......─────┤..........│
        // │...└─┐│....│...│       │.......................│..........│
        // │.....││........└─┐     │....│..................│.....┌────┘
        // │.....││..........│     │....├─┬───────┬─┐......│.....│
        // └┬──..└┼───┐......│   ┌─┴─..┌┘ │.......│ │.....┌┴──┐..│
        //  │.....│  ┌┴─..───┴───┘.....└┐ │.......│┌┘.....└─┐ │..│
        //  │.....└──┘..................└─┤.......││........│ │..│
        //  │.............................│.......├┘........│ │..│
        //  │.............┌──────┐........│.......│...─┐....│ │..│
        //  │...........┌─┘      └──┐.....│..─────┘....│....│ │..│
        // ┌┴─────......└─┐      ┌──┘..................│..──┴─┘..└─┐
        // │..............└──────┘.....................│...........│
        // │............................┌─┐.......│....│...........│
        // │..│..│..┌┐..................│ │.......├────┤..──┬───┐..│
        // │..│..│..│└┬──..─┬───┐......┌┘ └┐.....┌┘┌───┤....│   │..│
        // │..├──┤..│ │.....│   │......├───┘.....│ │...│....│┌──┘..└──┐
        // │..│┌─┘..└┐└┬─..─┤   │......│.........└─┘...│....││........│
        // │..││.....│ │....│   │......│...............│....││........│
        // │..││.....│ │....│   │......│..┌──┐.........├────┘│..│.....│
        // ├──┴┤...│.└─┴─..┌┘   └┐....┌┤..│  │.....│...└─────┘..│.....│
        // │...│...│.......└─────┴─..─┴┘..├──┘.....│............└─────┤
        // │...│...│......................│........│..................│
        // │.......├───┐..................│.......┌┤.......┌─┐........│
        // │.......│   └──┐..┌────┐..┌────┤..┌────┘│.......│ │..┌──┐..│
        // └───────┘      └──┘    └──┘    └──┘     └───────┘ └──┘  └──┘
        //this is also good to compare against if the map looks incorrect, and you need an example of a correct map when
        //no parameters are given to generate().
        lineDungeon = DungeonUtility.hashesToLines(decoDungeon);

        resistance = DungeonUtility.generateResistances(decoDungeon);
        visible = new double[bigWidth][bigHeight];

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
        floors = new GreasedRegion(bareDungeon, '.');
        //player is, here, just a Coord that stores his position. In a real game, you would probably have a class for
        //creatures, and possibly a subclass for the player. The singleRandom() method on GreasedRegion finds one Coord
        // in that region that is "on," or -1,-1 if there are no such cells. It takes an RNG object as a parameter, and
        // if you gave a seed to the RNG constructor, then the cell this chooses will be reliable for testing. If you
        // don't seed the RNG, any valid cell should be possible.
        player = floors.singleRandom(rng);

        //These need to have their positions set before adding any entities if there is an offset involved.
        //There is no offset used here, but it's still a good practice here to set positions early on.
//        display.setPosition(gridWidth * cellWidth * 0.5f - display.worldX(player.x),
//                gridHeight * cellHeight * 0.5f - display.worldY(player.y));
        display.setPosition(0f, 0f);
        // Uses shadowcasting FOV and reuses the visible array without creating new arrays constantly.
        FOV.reuseFOV(resistance, visible, player.x, player.y, 9.0, Radius.CIRCLE);//, (System.currentTimeMillis() & 0xFFFF) * 0x1p-4, 60.0);
        
        // 0.01 is the upper bound (inclusive), so any Coord in visible that is more well-lit than 0.01 will _not_ be in
        // the blockage Collection, but anything 0.01 or less will be in it. This lets us use blockage to prevent access
        // to cells we can't see from the start of the move.
        blockage = new GreasedRegion(visible, 0.0);
        seen = blockage.not().copy();
        currentlySeen = seen.copy();
        blockage.fringe8way();
        // prunedDungeon starts with the full lineDungeon, which includes features like water and grass but also stores
        // all walls as box-drawing characters. The issue with using lineDungeon as-is is that a character like '┬' may
        // be used because there are walls to the east, west, and south of it, even when the player is to the north of
        // that cell and so has never seen the southern connecting wall, and would have no reason to know it is there.
        // By calling LineKit.pruneLines(), we adjust prunedDungeon to hold a variant on lineDungeon that removes any
        // line segments that haven't ever been visible. This is called again whenever seen changes. 
        prunedDungeon = ArrayTools.copy(lineDungeon);
        // We call pruneLines with an optional parameter here, LineKit.lightAlt, which will allow prunedDungeon to use
        // the half-line chars "╴╵╶╷". These chars aren't supported by all fonts, but they are by the one we use here.
        // The default is to use LineKit.light , which will replace '╴' and '╶' with '─' and '╷' and '╵' with '│'.
        LineKit.pruneLines(lineDungeon, seen, LineKit.lightAlt, prunedDungeon);

        //This is used to allow clicks or taps to take the player to the desired area.
        toCursor = new ArrayList<>(200);
        //When a path is confirmed by clicking, we draw from this List to find which cell is next to move into.
        awaitedMoves = new ArrayList<>(200);
        //DijkstraMap is the pathfinding swiss-army knife we use here to find a path to the latest cursor position.
        //DijkstraMap.Measurement is an enum that determines the possibility or preference to enter diagonals. Here, the
        // MANHATTAN value is used, which means 4-way movement only, no diagonals possible. Alternatives are CHEBYSHEV,
        // which allows 8 directions of movement at the same cost for all directions, and EUCLIDEAN, which allows 8
        // directions, but will prefer orthogonal moves unless diagonal ones are clearly closer "as the crow flies."
        playerToCursor = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.MANHATTAN);
        //These next two lines mark the player as something we want paths to go to or from, and get the distances to the
        // player from all walkable cells in the dungeon.
        playerToCursor.setGoal(player);
        playerToCursor.setGoal(player);
        // DijkstraMap.partialScan only finds the distance to get to a cell if that distance is less than some limit,
        // which is 13 here. It also won't try to find distances through an impassable cell, which here is the blockage
        // GreasedRegion that contains the cells just past the edge of the player's FOV area.
        playerToCursor.partialScan(13, blockage);

        //The next three lines set the background color for anything we don't draw on, but also create 2D arrays of the
        //same size as decoDungeon that store the colors for the foregrounds and backgrounds of each cell as packed
        //floats (a format SparseLayers can use throughout its API), using the colors for the cell with the same x and
        //y. By changing an item in SColor.LIMITED_PALETTE, we also change the color assigned by MapUtility to floors.
        bgColor = SColor.DARK_SLATE_GRAY;
        SColor.LIMITED_PALETTE[3] = SColor.DB_GRAPHITE;
        colors = MapUtility.generateDefaultColorsFloat(decoDungeon);
        bgColors = MapUtility.generateDefaultBGColorsFloat(decoDungeon);
//        for (int x = 0; x < bigWidth; x++) {
//            for (int y = 0; y < bigHeight; y++) {
//                colors[x][y] = f(colors[x][y]);
//                bgColors[x][y] = f(bgColors[x][y]);
//            }
//        }
        //places the player as an '@' at his position in orange.
        pg = display.glyph('@', f(SColor.SAFETY_ORANGE), player.x, player.y);

        // here we build up a List of IColoredString values formed by formatting the artOfWar text (this colors the
        // whole thing dark gray and puts the name at the start in italic/oblique face) and wrapping it to fit within
        // the width we want, filling up lang with the results.
        lang = new ArrayList<>(16);
        GDXMarkup.instance.colorString(artOfWar).wrap(gridWidth - 2, lang);
        // here we choose a random language from all the hand-made FakeLanguageGen text generators, and make a
        // NaturalLanguageCipher out of it. This Cipher takes words it finds in artOfWar and translates them to the
        // fictional language it selected.
        translator = new NaturalLanguageCipher(rng.getRandomElement(FakeLanguageGen.registered));
        // this is just like the call above except we work on the translated artOfWar text instead of the original.
        GDXMarkup.instance.colorString(translator.cipher(artOfWar)).wrap(gridWidth - 2, lang);
        // now we change the language again and tell the NaturalLanguageCipher, translator, what we chose.
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
                switch (key)
                {
                    case SquidInput.UP_ARROW:
//                    case 'k':
//                    case 'K':
                    case 'w':
                    case 'W':
                    {
                        toCursor.clear();
                        //-1 is up on the screen
                        awaitedMoves.add(player.translate(0, -1));
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
//                    case 'j':
//                    case 'J':
                    case 's':
                    case 'S':
                    {
                        toCursor.clear();
                        //+1 is down on the screen
                        awaitedMoves.add(player.translate(0, 1));
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
//                    case 'h':
//                    case 'H':
                    case 'a':
                    case 'A':
                    {
                        toCursor.clear();
                        awaitedMoves.add(player.translate(-1, 0));
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
//                    case 'l':
//                    case 'L':
                    case 'd':
                    case 'D':
                    {
                        toCursor.clear();
                        awaitedMoves.add(player.translate(1, 0));
                        break;
                    }
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                        break;
                    }
                    case 'c':
                    case 'C':
                    {
                        seen.fill(true);
                        break;
                    }
                }
            }
        },
                //The second parameter passed to a SquidInput can be a SquidMouse, which takes mouse or touchscreen
                //input and converts it to grid coordinates (here, a cell is 10 wide and 20 tall, so clicking at the
                // pixel position 16,51 will pass screenX as 1 (since if you divide 16 by 10 and round down you get 1),
                // and screenY as 2 (since 51 divided by 20 rounded down is 2)).
                new SquidMouse(cellWidth, cellHeight, gridWidth, gridHeight, 0, 0, new InputAdapter() {

            // if the user clicks and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                // This is needed because we center the camera on the player as he moves through a dungeon that is three
                // screens wide and three screens tall, but the mouse still only can receive input on one screen's worth
                // of cells. (gridWidth >> 1) halves gridWidth, pretty much, and that we use to get the centered
                // position after adding to the player's position (along with the gridHeight).
                screenX += player.x - (gridWidth >> 1);
                screenY += player.y - (gridHeight >> 1);
                // we also need to check if screenX or screenY is out of bounds.
                if (screenX < 0 || screenY < 0 || screenX >= bigWidth || screenY >= bigHeight)
                    return false;
                if (awaitedMoves.isEmpty()) {
                    if (toCursor.isEmpty()) {
                        cursor = Coord.get(screenX, screenY);
                        //This uses DijkstraMap.findPathPreScannned() to get a path as a List of Coord from the current
                        // player position to the position the user clicked on. The "PreScanned" part is an optimization
                        // that's special to DijkstraMap; because the whole map has already been fully analyzed by the
                        // DijkstraMap.scan() method at the start of the program, and re-calculated whenever the player
                        // moves, we only need to do a fraction of the work to find the best path with that info.
                        toCursor.clear();
                        playerToCursor.findPathPreScanned(toCursor, cursor);
                        //findPathPreScanned includes the current cell (goal) by default, which is helpful when
                        // you're finding a path to a monster or loot, and want to bump into it, but here can be
                        // confusing because you would "move into yourself" as your first move without this.
                        if(!toCursor.isEmpty())
                            toCursor.remove(0);
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
                if(!awaitedMoves.isEmpty())
                    return false;
                // This is needed because we center the camera on the player as he moves through a dungeon that is three
                // screens wide and three screens tall, but the mouse still only can receive input on one screen's worth
                // of cells. (gridWidth >> 1) halves gridWidth, pretty much, and that we use to get the centered
                // position after adding to the player's position (along with the gridHeight).
                screenX += player.x - (gridWidth >> 1);
                screenY += player.y - (gridHeight >> 1);
                // we also need to check if screenX or screenY is out of bounds.
                if(screenX < 0 || screenY < 0 || screenX >= bigWidth || screenY >= bigHeight ||
                        (cursor.x == screenX && cursor.y == screenY))
                {
                    return false;
                }
                cursor = Coord.get(screenX, screenY);
                //This uses DijkstraMap.findPathPreScannned() to get a path as a List of Coord from the current
                // player position to the position the user clicked on. The "PreScanned" part is an optimization
                // that's special to DijkstraMap; because the whole map has already been fully analyzed by the
                // DijkstraMap.scan() method at the start of the program, and re-calculated whenever the player
                // moves, we only need to do a fraction of the work to find the best path with that info.

                toCursor.clear();
                playerToCursor.findPathPreScanned(toCursor, cursor);
                //findPathPreScanned includes the current cell (goal) by default, which is helpful when
                // you're finding a path to a monster or loot, and want to bump into it, but here can be
                // confusing because you would "move into yourself" as your first move without this.
                if(!toCursor.isEmpty()) 
                    toCursor.remove(0);
                return false;
            }
        }));
        //Setting the InputProcessor is ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        //You might be able to get by with the next line instead of the above line, but the former is preferred.
        //Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        stage.addActor(display);
        languageStage.addActor(languageDisplay);

        screenPosition = new Vector2(cellWidth, cellHeight);
    }
    /**
     * Move the player if he isn't bumping into a wall or trying to go off the map somehow.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod
     * @param ymod
     */
    private void move(final int xmod, final int ymod) {
        int newX = player.x + xmod, newY = player.y + ymod;
        if (newX >= 0 && newY >= 0 && newX < bigWidth && newY < bigHeight
                && bareDungeon[newX][newY] != '#')
        {
            display.slide(pg, player.x, player.y, newX, newY, 0.12f, null);
            player = player.translate(xmod, ymod);
            FOV.reuseFOV(resistance, visible, player.x, player.y, 9.0, Radius.CIRCLE);//, (System.currentTimeMillis() & 0xFFFF) * 0x1p-4, 60.0);
            // This is just like the constructor used earlier, but affects an existing GreasedRegion without making
            // a new one just for this movement.
            blockage.refill(visible, 0.0);
            seen.or(currentlySeen.remake(blockage.not()));
            blockage.fringe8way();
            // By calling LineKit.pruneLines(), we adjust prunedDungeon to hold a variant on lineDungeon that removes any
            // line segments that haven't ever been visible. This is called again whenever seen changes.
            LineKit.pruneLines(lineDungeon, seen, LineKit.lightAlt, prunedDungeon);
        }
        else
        {
            // A SparseLayers knows how to move a Glyph (like the one for the player, pg) out of its normal alignment
            // on the grid, and also how to move it back again. Using bump() will move pg quickly about a third of the
            // way into a wall, then back to its former position at normal speed.
            display.bump(pg, Direction.getRoughDirection(xmod, ymod), 0.25f);
            // PanelEffect is a type of Action (from libGDX) that can run on a SparseLayers or SquidPanel.
            // This particular kind of PanelEffect creates a purple glow around the player when he bumps into a wall.
            // Other kinds can make explosions or projectiles appear.
            display.addAction(new PanelEffect.PulseEffect(display, 1f, currentlySeen, player, 3
                    , new float[]{f(SColor.CW_FADED_PURPLE)}
                    ));
            // recolor() will change the color of a cell over time from what it is currently to a target color, which is
            // DB_BLOOD here from a DawnBringer palette. We give it a Runnable to run after the effect finishes, which
            // permanently sets the color of the cell you bumped into to the color of your bloody nose. Without such a 
            // Runnable, the cell would get drawn over with its normal wall color.
            display.recolor(0f, player.x + xmod, player.y + ymod, 0, f(SColor.DB_BLOOD), 0.4f, new Runnable() {
                int x = player.x + xmod;
                int y = player.y + ymod;
                @Override
                public void run() {
                    colors[x][y] = f(SColor.DB_BLOOD);
                }
            });
            //display.addAction(new PanelEffect.ExplosionEffect(display, 1f, floors, player, 6));
        }
        // removes the first line displayed of the Art of War text or its translation.
        lang.remove(0);
        // if the last line reduced the number of lines we can show to less than what we try to show, we fill in more
        // lines using a randomly selected fake language to translate the same Art of War text.
        while (lang.size() < bonusHeight - 1)
        {
            // refills lang with wrapped lines from the translated artOfWar text
            GDXMarkup.instance.colorString(translator.cipher(artOfWar)).wrap(gridWidth - 2, lang);
            translator.initialize(rng.getRandomElement(FakeLanguageGen.registered), 0L);
        }
    }

    /**
     * Draws the map, applies any highlighting for the path to the cursor, and then draws the player.
     */
    public void putMap()
    {
        //In many other situations, you would clear the drawn characters to prevent things that had been drawn in the
        //past from affecting the current frame. This isn't a problem here, but would probably be an issue if we had
        //monsters running in and out of our vision. If artifacts from previous frames show up, uncomment the next line.
        //display.clear();
        filter.crMul = NumberTools.swayRandomized(123456789L, (System.currentTimeMillis() & 0x1FFFFFL) * 0x1.2p-10f) * 1.75f;
        filter.cbMul = NumberTools.swayRandomized(987654321L, (System.currentTimeMillis() & 0x1FFFFFL) * 0x1.1p-10f) * 1.75f;
        pg.setPackedColor(f(SColor.SAFETY_ORANGE));
        // The loop here only will draw tiles if they are potentially in the visible part of the map.
        // It starts at an x,y position equal to the player's position minus half of the shown gridWidth and gridHeight,
        // minus one extra cell to allow the camera some freedom to move. This position won't go lower than 0. The
        // rendering in each direction ends when the edge of the map (bigWidth or bigHeight) is reached, or if
        // gridWidth/gridHeight + 2 cells have been rendered (the + 2 is also for the camera movement).
        for (int x = Math.max(0, player.x - (gridWidth >> 1) - 1), i = 0; x < bigWidth && i < gridWidth + 2; x++, i++) {
            for (int y = Math.max(0, player.y - (gridHeight >> 1) - 1), j = 0; y < bigHeight && j < gridHeight + 2; y++, j++) {
                if (visible[x][y] > 0.0) {
                    // Here we use a convenience method in SparseLayers that puts a char at a specified position (the
                    // first three parameters), with a foreground color for that char (fourth parameter), as well as
                    // placing a background tile made of a one base color (fifth parameter) that is adjusted to bring it
                    // closer to FLOAT_LIGHTING (sixth parameter) based on how visible the cell is (seventh parameter,
                    // comes from the FOV calculations) in a way that fairly-quickly changes over time.
                    // This effect appears to shrink and grow in a circular area around the player, with the lightest
                    // cells around the player and dimmer ones near the edge of vision. This lighting is "consistent"
                    // because all cells at the same distance will have the same amount of lighting applied.
                    // We use prunedDungeon here so segments of walls that the player isn't aware of won't be shown.
                    display.putWithConsistentLight(x, y, prunedDungeon[x][y], f(colors[x][y]), f(bgColors[x][y]), FLOAT_LIGHTING, visible[x][y]);
                } else if (seen.contains(x, y))
                    display.put(x, y, prunedDungeon[x][y], f(colors[x][y]), SColor.lerpFloatColors(f(bgColors[x][y]), GRAY_FLOAT, 0.45f));
            }
        }
        Coord pt;
        for (int i = 0; i < toCursor.size(); i++) {
            pt = toCursor.get(i);
            // use a brighter light to trace the path to the cursor, mixing the background color with mostly white.
            display.put(pt.x, pt.y, SColor.lerpFloatColors(f(bgColors[pt.x][pt.y]), SColor.FLOAT_WHITE, 0.85f));
        }
        languageDisplay.clear(0);
        languageDisplay.fillBackground(f(languageDisplay.defaultPackedBackground));
        for (int i = 0; i < 6; i++) {
            languageDisplay.put(1, i, lang.get(i));
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getCamera().position.x = pg.getX();
        stage.getCamera().position.y =  pg.getY();

        putMap();
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            // this doesn't check for input, but instead processes and removes Coords from awaitedMoves.
            if (!display.hasActiveAnimations()) {
                Coord m = awaitedMoves.remove(0);
                if(!toCursor.isEmpty())
                    toCursor.remove(0);
                move(m.x - player.x, m.y - player.y);
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
                    // found, but the player doesn't move until a cell is clicked, the "goal" is the non-changing cell (the
                    // player's position), and the "target" of a pathfinding method like DijkstraMap.findPathPreScanned() is the
                    // currently-moused-over cell, which we only need to set where the mouse is being handled.
                    playerToCursor.setGoal(player);
                    // DijkstraMap.partialScan only finds the distance to get to a cell if that distance is less than some limit,
                    // which is 13 here. It also won't try to find distances through an impassable cell, which here is the blockage
                    // GreasedRegion that contains the cells just past the edge of the player's FOV area.
                    playerToCursor.partialScan(13, blockage);
                }
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if(input.hasNext()) {
            input.next();
        }
        //else
        //    move(0,0);
        // we need to do some work with viewports here so the language display (or game info messages in a real game)
        // will display in the same place even though the map view will move around. We have the language stuff set up
        // its viewport so it is in place and won't be altered by the map. Then we just tell the Stage for the language
        // texts to draw.
        languageStage.getViewport().apply(false);
        languageStage.draw();
        // certain classes that use scene2d.ui widgets need to be told to act() to process input.
        stage.act();
        // we have the main stage set itself up after the language stage has already drawn.
        stage.getViewport().apply(false);
        // stage has its own batch and must be explicitly told to draw().
        batch.setProjectionMatrix(stage.getCamera().combined);
        screenPosition.set(cellWidth * 6, cellHeight);
        stage.screenToStageCoordinates(screenPosition);
        batch.begin();
        stage.getRoot().draw(batch, 1);
        display.font.draw(batch, Gdx.graphics.getFramesPerSecond() + " FPS", screenPosition.x, screenPosition.y);
        batch.end();
        Gdx.graphics.setTitle("SparseLayers Demo running at FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    @Override
	public void resize(int width, int height) {
		super.resize(width, height);

        // message box won't respond to clicks on the far right if the stage hasn't been updated with a larger size
        float currentZoomX = (float)width / gridWidth;
        // total new screen height in pixels divided by total number of rows on the screen
        float currentZoomY = (float)height / (gridHeight + bonusHeight);
        // message box should be given updated bounds since I don't think it will do this automatically
        languageDisplay.setBounds(0, 0, width, currentZoomY * bonusHeight);
        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
        // a quirk of how the camera works requires the mouse to be offset by half a cell if the width or height is odd
        // (gridWidth & 1) is 1 if gridWidth is odd or 0 if it is even; it's good to know and faster than using % , plus
        // in some other cases it has useful traits (x % 2 can be 0, 1, or -1 depending on whether x is negative, while
        // x & 1 will always be 0 or 1).
        input.getMouse().reinitialize(currentZoomX, currentZoomY, gridWidth, gridHeight,
                (gridWidth & 1) * (int)(currentZoomX * -0.5f), (gridHeight & 1) * (int) (currentZoomY * -0.5f));
        languageStage.getViewport().update(width, height, false);
        languageStage.getViewport().setScreenBounds(0, 0, width, (int)languageDisplay.getHeight());
        stage.getViewport().update(width, height, false);
        stage.getViewport().setScreenBounds(0, (int)languageDisplay.getHeight(), width, height - (int)languageDisplay.getHeight());
	}
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib GDX Sparse Demo";
        config.width = gridWidth * cellWidth;
        config.height = (gridHeight + bonusHeight) * cellHeight;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.backgroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
        new LwjglApplication(new SparseDemo(), config);
    }

}
