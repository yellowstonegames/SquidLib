package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Measurement;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.RNG;

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
public class DijkstraDemo extends ApplicationAdapter {
    // FilterBatch is almost the same as SpriteBatch
    private FilterBatch batch;

    private IRNG rng;
    private SparseLayers display;
    private DungeonGenerator dungeonGen;
    // decoDungeon stores the dungeon map with features like grass and water, if present, as chars like '"' and '~'.
    // bareDungeon stores the dungeon map with just walls as '#' and anything not a wall as '.'.
    // Both of the above maps use '#' for walls, and the next two use box-drawing characters instead.
    // lineDungeon stores the whole map the same as decoDungeon except for walls, which are box-drawing characters here.
    // prunedDungeon takes lineDungeon and adjusts it so unseen segments of wall (represented by box-drawing characters)
    //   are removed from rendering; unlike the others, it is frequently changed.
    private char[][] decoDungeon, bareDungeon, lineDungeon;
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
    private static final int bigWidth = gridWidth;
    /** In number of cells */
    private static final int bigHeight = gridHeight;

    /** In number of cells */
    private static final int bonusHeight = 0;
    /** The pixel width of a cell */
    private static final int cellWidth = 11;
    /** The pixel height of a cell */
    private static final int cellHeight = 21;
    private SquidInput input;
    private Color bgColor;
    private Stage stage;
    private DijkstraMap playerToCursor;
    private Coord cursor, player;
    private ArrayList<Coord> toCursor;
    private List<Coord> awaitedMoves;

    private Vector2 screenPosition;


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
    private GreasedRegion floors;
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
    // This filters colors in a way we adjust over time, producing a sort of hue shift effect.
    // It can also be used to over- or under-saturate colors, change their brightness, or any combination of these. 
//    private FloatFilters.PaletteFilter pal;
//    private FloatFilters.MultiLerpFilter mlerp;
    private FloatFilters.YCbCrFilter ycbcr;
//    private FloatFilter sepia;
    @Override
    public void create () {
        // gotta have a random number generator. We can seed an RNG with any long we want, or even a String.
        // if the seed is identical between two runs, any random factors will also be identical (until user input may
        // cause the usage of an RNG to change). You can randomize the dungeon and several other initial settings by
        // just removing the String seed, making the line "rng = new RNG();" . Keeping the seed as a default allows
        // changes to be more easily reproducible, and using a fixed seed is strongly recommended for tests. 
        rng = new RNG(123);
//        pal = new FloatFilters.PaletteFilter(SColor.DAWNBRINGER_32);
//        mlerp = new FloatFilters.MultiLerpFilter(1f,
//                SColor.translucentColor(SColor.CW_RICH_GREEN, 0.6f),
//                SColor.translucentColor(SColor.CW_LIGHT_AZURE, 0.4f),
//                SColor.translucentColor(SColor.CW_ROSE, 0.8f),
//                SColor.translucentColor(SColor.CW_LIGHT_BROWN, 0.5f)
//        );
        // testing FloatFilter; YCbCrFilter multiplies the brightness (Y) and chroma (Cb, Cr) of a color 
        ycbcr = new FloatFilters.YCbCrFilter(0.875f, 0.6f, 0.6f);
//        sepia = new FloatFilters.ColorizeFilter(SColor.CLOVE_BROWN, 0.6f, 0.0f);

        //Some classes in SquidLib need access to a batch to render certain things, so it's a good idea to have one.
        // FilterBatch is exactly like the normal libGDX SpriteBatch except that it filters all colors used for text or
        // for tinting images.
        batch = new FilterBatch(ycbcr);
        StretchViewport mainViewport = new StretchViewport(gridWidth * cellWidth, gridHeight * cellHeight);
        mainViewport.setScreenBounds(0, 0, gridWidth * cellWidth, gridHeight * cellHeight);
        //Here we make sure our Stage, which holds any text-based grids we make, uses our Batch.
        stage = new Stage(mainViewport, batch);
        // the font will try to load Iosevka Slab as an embedded bitmap font with a MSDF effect (multi scale distance
        // field, a way to allow a bitmap font to stretch while still keeping sharp corners and round curves).
        // the MSDF effect is handled internally by a shader in SquidLib, and will switch to a different shader if a SDF
        // effect is used (SDF is called "Stretchable" in DefaultResources, where MSDF is called "Crisp").
        // this font is covered under the SIL Open Font License (fully free), so there's no reason it can't be used.
        // it also includes 4 text faces (regular, bold, oblique, and bold oblique) so methods in GDXMarkup can make
        // italic or bold text without switching fonts (they can color sections of text too).
        display = new SparseLayers(bigWidth, bigHeight + bonusHeight, cellWidth, cellHeight,
                DefaultResources.getCrispSlabFamily());
        display.addLayer();
        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        //display.font.tweakWidth(cellWidth * 1.075f).tweakHeight(cellHeight * 1.1f).initBySize();

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
        
        //This is used to allow clicks or taps to take the player to the desired area.
        toCursor = new ArrayList<>(200);
        //When a path is confirmed by clicking, we draw from this List to find which cell is next to move into.
        awaitedMoves = new ArrayList<>(200);
        //DijkstraMap is the pathfinding swiss-army knife we use here to find a path to the latest cursor position.
        //DijkstraMap.Measurement is an enum that determines the possibility or preference to enter diagonals. Here, the
        // MANHATTAN value is used, which means 4-way movement only, no diagonals possible. Alternatives are CHEBYSHEV,
        // which allows 8 directions of movement at the same cost for all directions, and EUCLIDEAN, which allows 8
        // directions, but will prefer orthogonal moves unless diagonal ones are clearly closer "as the crow flies."
        playerToCursor = new DijkstraMap(decoDungeon, Measurement.MANHATTAN);
        //These next two lines mark the player as something we want paths to go to or from, and get the distances to the
        // player from all walkable cells in the dungeon.
        playerToCursor.setGoal(player);
        // DijkstraMap.partialScan only finds the distance to get to a cell if that distance is less than some limit,
        // which is 13 here. It also won't try to find distances through an impassable cell, which here is the blockage
        // GreasedRegion that contains the cells just past the edge of the player's FOV area.
        playerToCursor.scan();

        //The next three lines set the background color for anything we don't draw on, but also create 2D arrays of the
        //same size as decoDungeon that store the colors for the foregrounds and backgrounds of each cell as packed
        //floats (a format SparseLayers can use throughout its API), using the colors for the cell with the same x and
        //y. By changing an item in SColor.LIMITED_PALETTE, we also change the color assigned by MapUtility to floors.
        bgColor = SColor.DARK_SLATE_GRAY;
        SColor.LIMITED_PALETTE[3] = SColor.DB_GRAPHITE;
        colors = ArrayTools.fill(SColor.FLOAT_BLACK, bigWidth, bigHeight);
        bgColors = new float[bigWidth][bigHeight];
        for (int x = 0; x < bigWidth; x++) {
            for (int y = 0; y < bigHeight; y++) {
                bgColors[x][y] = playerToCursor.gradientMap[x][y] >= DijkstraMap.FLOOR ? SColor.CW_GRAY_BLACK.toFloatBits()
                        : SColor.floatGetHSV((float) (playerToCursor.gradientMap[x][y]) * 0.0816f, 0.4f, 1f, 1f);
            }
        }
        //places the player as an '@' at his position in orange.
        pg = display.glyph('@', SColor.BLACK, player.x, player.y);

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
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                        break;
                    }
                    default:
                    {
                        player = floors.singleRandom(rng);
                        display.removeGlyph(pg);
                        pg = display.glyph('@', SColor.BLACK, player.x, player.y);
                        
                        toCursor.clear();
                        playerToCursor.reset();
                        playerToCursor.setGoal(player);
                        playerToCursor.scan();
                        for (int x = 0; x < bigWidth; x++) {
                            for (int y = 0; y < bigHeight; y++) {
                                bgColors[x][y] = playerToCursor.gradientMap[x][y] >= DijkstraMap.FLOOR ? SColor.CW_GRAY_BLACK.toFloatBits()
                                        : SColor.floatGetHSV((float) (playerToCursor.gradientMap[x][y]) * 0.0816f, 0.4f, 1f, 1f);
                            }
                        }
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
                return mouseMoved(screenX, screenY);
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
//                screenX += player.x - (gridWidth >> 1);
//                screenY += player.y - (gridHeight >> 1);
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
                playerToCursor.findAttackPath(toCursor, 1, 3, 7, null, null, null, player, cursor);

                for (int x = 0; x < bigWidth; x++) {
                    for (int y = 0; y < bigHeight; y++) {
                        bgColors[x][y] = playerToCursor.gradientMap[x][y] >= DijkstraMap.FLOOR ? SColor.CW_GRAY_BLACK.toFloatBits()
                                : SColor.floatGetHSV((float) (playerToCursor.gradientMap[x][y]) * 0.0816f, 0.4f, 1f, 1f);
                    }
                }


                //findPathPreScanned includes the current cell (goal) by default, which is helpful when
                // you're finding a path to a monster or loot, and want to bump into it, but here can be
                // confusing because you would "move into yourself" as your first move without this.
//                if(!toCursor.isEmpty()) 
//                    toCursor.remove(0);
                return false;
            }
        }));
        //Setting the InputProcessor is ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        //You might be able to get by with the next line instead of the above line, but the former is preferred.
        //Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        stage.addActor(display);

        screenPosition = new Vector2(cellWidth, cellHeight);
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
//        ycbcr.crMul = NumberTools.swayRandomized(123456789L, (System.currentTimeMillis() & 0x1FFFFFL) * 0x1.2p-10f) * 1.75f;
//        ycbcr.cbMul = NumberTools.swayRandomized(987654321L, (System.currentTimeMillis() & 0x1FFFFFL) * 0x1.1p-10f) * 1.75f;
        // The loop here only will draw tiles if they are potentially in the visible part of the map.
        // It starts at an x,y position equal to the player's position minus half of the shown gridWidth and gridHeight,
        // minus one extra cell to allow the camera some freedom to move. This position won't go lower than 0. The
        // rendering in each direction ends when the edge of the map (bigWidth or bigHeight) is reached, or if
        // gridWidth/gridHeight + 2 cells have been rendered (the + 2 is also for the camera movement).
        for (int x = Math.max(0, player.x - (gridWidth >> 1) - 1), i = 0; x < bigWidth && i < gridWidth + 2; x++, i++) {
            for (int y = Math.max(0, player.y - (gridHeight >> 1) - 1), j = 0; y < bigHeight && j < gridHeight + 2; y++, j++) {
                    // Here we use a convenience method in SparseLayers that puts a char at a specified position (the
                    // first three parameters), with a foreground color for that char (fourth parameter), as well as
                    // placing a background tile made of a one base color (fifth parameter) that is adjusted to bring it
                    // closer to FLOAT_LIGHTING (sixth parameter) based on how visible the cell is (seventh parameter,
                    // comes from the FOV calculations) in a way that fairly-quickly changes over time.
                    // This effect appears to shrink and grow in a circular area around the player, with the lightest
                    // cells around the player and dimmer ones near the edge of vision. This lighting is "consistent"
                    // because all cells at the same distance will have the same amount of lighting applied.
                    // We use prunedDungeon here so segments of walls that the player isn't aware of won't be shown.
                    display.put(x, y, lineDungeon[x][y], colors[x][y], bgColors[x][y]);
            }
        }
        Coord pt;
        display.clear(1);
        for (int i = 0; i < toCursor.size(); i++) {
            pt = toCursor.get(i);
            // use a brighter light to trace the path to the cursor, mixing the background color with mostly white.
            display.put(pt.x, pt.y, '*', SColor.darkenFloat(bgColors[pt.x][pt.y], 0.5f), 0f, 1);
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//        stage.getCamera().position.x = pg.getX();
//        stage.getCamera().position.y =  pg.getY();

        putMap();
        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }
        // certain classes that use scene2d.ui widgets need to be told to act() to process input.
        stage.act();
        // we have the main stage set itself up after the language stage has already drawn.
        stage.getViewport().apply(false);
        // stage has its own batch and must be explicitly told to draw().
        batch.setProjectionMatrix(stage.getCamera().combined);
        screenPosition.set(cellWidth * 12, cellHeight);
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
        // SquidMouse turns screen positions to cell positions, and needs to be told that cell sizes have changed
        // a quirk of how the camera works requires the mouse to be offset by half a cell if the width or height is odd
        // (gridWidth & 1) is 1 if gridWidth is odd or 0 if it is even; it's good to know and faster than using % , plus
        // in some other cases it has useful traits (x % 2 can be 0, 1, or -1 depending on whether x is negative, while
        // x & 1 will always be 0 or 1).
        input.getMouse().reinitialize(currentZoomX, currentZoomY, gridWidth, gridHeight, 0, 0);
                //(gridWidth & 1) * (int)(currentZoomX * -0.5f), (gridHeight & 1) * (int) (currentZoomY * -0.5f));
        stage.getViewport().update(width, height, false);
        stage.getViewport().setScreenBounds(0, 0, width, height);
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
        new LwjglApplication(new DijkstraDemo(), config);
    }

}
