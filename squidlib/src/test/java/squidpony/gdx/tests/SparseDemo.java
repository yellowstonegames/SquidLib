package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.RNG;
import squidpony.squidmath.SeededNoise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparseDemo extends ApplicationAdapter {
    SpriteBatch batch;

    private RNG rng;
    private SparseLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon;
    private float[][] colors, bgColors;
    private int[][] baseLightness;

    //Here, gridHeight refers to the total number of rows to be displayed on the screen.
    //We're displaying 24 rows of dungeon, then 8 more rows of text generation to show some tricks with language.
    //gridHeight is 24 because that variable will be used for generating the dungeon and handling movement within
    //the upper 24 rows. The bonusHeight is the number of additional rows that aren't handled like the dungeon rows for
    //UI reasons; here we use them for language samples. Next is gridWidth, which is 80 because we want 80 grid spaces
    //across the whole screen. cellWidth and cellHeight are 10 and 22, which will match the starting dimensions of a
    //cell, but won't be stuck at that value because we use a "Stretchable" font, and the cells can change size. While
    //gridWidth and gridHeight are measured in spaces on the grid, cellWidth and cellHeight are the initial pixel
    // dimensions of one cell. The font will look more crisp if the cell dimensions match the config multipliers
    //exactly, and the stretchable fonts (technically, distance field fonts) can resize to non-square sizes and
    //still retain most of that crispness.

    /** In number of cells */
    private static final int gridWidth = 80;
    /** In number of cells */
    private static final int gridHeight = 24;
    /** In number of cells */
    private static final int bonusHeight = 8;
    /** The pixel width of a cell */
    private static final int cellWidth = 10;
    /** The pixel height of a cell */
    private static final int cellHeight = 22;
    private SquidInput input;
    private Color bgColor;
    private Stage stage;
    private DijkstraMap playerToCursor;
    private Coord cursor, player;
    private List<Coord> toCursor;
    private List<Coord> awaitedMoves;
    private float secondsWithoutMoves;
    private String[] lang;
    private FakeLanguageGen.SentenceForm[] forms;
    private int langIndex = 0;
    private double[][] resistance;
    private double[][] visible;
    private GreasedRegion blockage;
    private GreasedRegion seen;
    private TextCellFactory.Glyph pg;
    private static final float WHITE_FLOAT = NumberUtils.intToFloatColor(-1),
            GRAY_FLOAT = NumberUtils.intToFloatColor(0xFF444444), CRIMSON_FLOAT = SColor.CRIMSON.toFloatBits(),
            YELLOW_FLOAT = SColor.CW_BRIGHT_GOLD.toFloatBits(),
            YELLOW_FADING_FLOAT = SColor.translucentColor(SColor.CW_YELLOW.toFloatBits(), 0f);
    @Override
    public void create () {
        // gotta have a random number generator. We can seed an RNG with any long we want, or even a String.
        rng = new RNG("SquidLib!");

        //Some classes in SquidLib need access to a batch to render certain things, so it's a good idea to have one.
        batch = new SpriteBatch();
        //Here we make sure our Stage, which holds any text-based grids we make, uses our Batch.
        stage = new Stage(new StretchViewport(gridWidth * cellWidth, (gridHeight + bonusHeight) * cellHeight), batch);
        // the font will try to load Iosevka Slab as an embedded bitmap font with a distance field effect.
        // the distance field effect allows the font to be stretched without getting blurry or grainy too easily.
        // this font is covered under the SIL Open Font License (fully free), so there's no reason it can't be used.
        display = new SparseLayers(gridWidth, gridHeight + bonusHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableSlabFont());
        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        display.font.tweakWidth(cellWidth * 1.1f).tweakHeight(cellHeight * 1.1f).initBySize();

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
        GreasedRegion placement = new GreasedRegion(bareDungeon, '.');
        //player is, here, just a Coord that stores his position. In a real game, you would probably have a class for
        //creatures, and possibly a subclass for the player. The singleRandom() method on GreasedRegion finds one Coord
        // in that region that is "on," or -1,-1 if there are no such cells. It takes an RNG object as a parameter, and
        // if you gave a seed to the RNG constructor, then the cell this chooses will be reliable for testing. If you
        // don't seed the RNG, any valid cell should be possible.
        player = placement.singleRandom(rng);
        // Uses shadowcasting FOV and reuses the visible array without creating new arrays constantly.
        FOV.reuseFOV(resistance, visible, player.x, player.y, 9.0, Radius.CIRCLE);
        // 0.01 is the upper bound (inclusive), so any Coord in visible that is more well-lit than 0.01 will _not_ be in
        // the blockage Collection, but anything 0.01 or less will be in it. This lets us use blockage to prevent access
        // to cells we can't see from the start of the move.
        blockage = new GreasedRegion(visible, 0.0);
        seen = blockage.copy().not();
        blockage.surface8way();
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
        playerToCursor.scan(blockage);

        //The next three lines set the background color for anything we don't draw on, but also create 2D arrays of the
        //same size as decoDungeon that store simple indexes into a common list of colors, using the colors that looks
        // up as the colors for the cell with the same x and y.
        bgColor = SColor.DARK_SLATE_GRAY;
        SColor.LIMITED_PALETTE[3] = SColor.DB_GRAPHITE;
        Color[][] temp = MapUtility.generateDefaultColors(decoDungeon);
        colors = new float[gridWidth][gridHeight];
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                colors[i][j] = temp[i][j].toFloatBits();
            }
        }
        temp = MapUtility.generateDefaultBGColors(decoDungeon);
        bgColors = new float[gridWidth][gridHeight];
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                bgColors[i][j] = temp[i][j].toFloatBits();
            }
        }

        baseLightness = ArrayTools.fill(40, gridWidth, gridHeight);

        //places the player as an '@' at his position in orange.
        pg = display.glyph('@', SColor.SAFETY_ORANGE.toFloatBits(), player.x, player.y);


        // this creates an array of sentence builders, where each imitates one or more languages or linguistic styles.
        // this serves to demonstrate the large amount of glyphs SquidLib supports.
        // there's no need to put much effort into understanding this section yet, and many games won't use the language
        // generation code at all. If you want to know what this does, the parameters are: the language to use,
        // minimum words in a sentence, maximum words in a sentence, "mid" punctuation that can be after a word (like a
        // comma), "end" punctuation that can be at the end of a sentence, frequency of "mid" punctuation (the chance in
        // 1.0 that a word will have something like a comma appended after it), and the limit on how many chars to use.
        forms = new FakeLanguageGen.SentenceForm[]
                {
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.ENGLISH, 5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.GREEK_AUTHENTIC, 5, 11, new String[]{",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.GREEK_ROMANIZED, 5, 11, new String[]{",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.LOVECRAFT, 3, 9, new String[]{",", ",", ";"},
                                new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.FRENCH, 4, 12, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.RUSSIAN_AUTHENTIC, 6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.25, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.RUSSIAN_ROMANIZED, 6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.25, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.JAPANESE_ROMANIZED, 5, 13, new String[]{",", ",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.SWAHILI, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.SOMALI, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.HINDI_ROMANIZED, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.NORSE, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.INUKTITUT, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.NAHUATL, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.FANTASY_NAME, 4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.22, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.FANCY_FANTASY_NAME, 4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.22, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.ELF, 5, 10, new String[]{",", ",", ",", ";", ";", " -"},
                                new String[]{".", ".", ".", "...", "?"}, 0.22, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.GOBLIN, 4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "...", "?"}, 0.1, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.DEMONIC, 4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", "!", "!", "!", "?!"}, 0.07, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.INFERNAL, 6, 13, new String[]{",", ",", ",", ";", ";", " -", "*", " ©"},
                                new String[]{".", ".", ".", "...", "?", "...", "?"}, 0.25, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.FRENCH.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.65), 5, 9, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "?", "..."}, 0.14, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.ENGLISH.addAccents(0.5, 0.15), 5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        new FakeLanguageGen.SentenceForm(FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.5).mix(FakeLanguageGen.FRENCH, 0.35)
                                .mix(FakeLanguageGen.RUSSIAN_ROMANIZED, 0.25).mix(FakeLanguageGen.GREEK_ROMANIZED, 0.2).mix(FakeLanguageGen.ENGLISH, 0.15)
                                .mix(FakeLanguageGen.FANCY_FANTASY_NAME, 0.12).mix(FakeLanguageGen.LOVECRAFT, 0.1)
                                , 5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                };
        /*
         * Now we generate the initial sentences for each of those many languages. We cycle through the shown sentences
         * by changing langIndex, and change the contents of each sentence once it is cycled out of being visible.
         */
        lang = new String[forms.length];
        for (int i = 0; i < forms.length; i++) {
            lang[i] = forms[i].sentence();
        }

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
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W':
                    {
                        toCursor.clear();
                        //-1 is up on the screen
                        awaitedMoves.add(player.translate(0, -1));
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S':
                    {
                        toCursor.clear();
                        //+1 is down on the screen
                        awaitedMoves.add(player.translate(0, 1));
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A':
                    {
                        toCursor.clear();
                        awaitedMoves.add(player.translate(-1, 0));
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
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
                if(awaitedMoves.isEmpty()) {
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
                        if(!toCursor.isEmpty())
                        {
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
                if(!awaitedMoves.isEmpty())
                    return false;
                if(cursor.x == screenX && cursor.y == screenY)
                {
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
                if(!toCursor.isEmpty())
                {
                    toCursor = toCursor.subList(1, toCursor.size());
                }
                return false;
            }
        }));
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
     * @param xmod
     * @param ymod
     */
    private void move(int xmod, int ymod) {
        int newX = player.x + xmod, newY = player.y + ymod;
        if (newX >= 0 && newY >= 0 && newX < gridWidth && newY < gridHeight
                && bareDungeon[newX][newY] != '#')
        {
            display.slide(pg, player.x, player.y, newX, newY, 0.12f, null);
            player = player.translate(xmod, ymod);
            FOV.reuseFOV(resistance, visible, player.x, player.y, 9.0, Radius.CIRCLE);
            // This is just like the constructor used earlier, but affects an existing GreasedRegion without making
            // a new one just for this movement.
            blockage.refill(visible, 0.01);
            seen.or(blockage.not());
            blockage.fringe8way();
        }
        else
        {
            display.bump(pg, Direction.getRoughDirection(xmod, ymod), 0.25f);
            display.burst(player.x, player.y, 1, Radius.CIRCLE, StringKit.PUNCTUATION, YELLOW_FLOAT, YELLOW_FADING_FLOAT, 0.45f);
            //display.tint(0f, player.x, player.y, CRIMSON_FLOAT, 0.13f, null);
        }
        // changes the top displayed sentence to a new one with the same language. the top will be cycled off next.
        lang[langIndex] = forms[langIndex].sentence();
        // cycles through the text snippets displayed whenever the player moves
        langIndex = (langIndex + 1) % lang.length;
    }

    /**
     * Draws the map, applies any highlighting for the path to the cursor, and then draws the player.
     */
    public void putMap()
    {
        long tm = (System.currentTimeMillis() & 0xffffffL);
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                if(visible[i][j] > 0.01) {
                    float bg = SColor.lerpFloatColors(bgColors[i][j], WHITE_FLOAT,(baseLightness[i][j] + 256f + (-105f +
                            180f * ((float)visible[i][j] * (1.0f + 0.2f * SeededNoise.noise(i * 0.2f, j * 0.2f, tm * 0.001f, 10000)))))
                            * 0x1p-9f); // "* 0x1p-9f" is equivalent to "/ 512.0", just possibly faster
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bg);
                }else if(seen.contains(i, j))
                    display.put(i, j, lineDungeon[i][j], colors[i][j], SColor.lerpFloatColors(bgColors[i][j], GRAY_FLOAT, 0.3f));
            }
        }
        Coord pt;
        for (int i = 0; i < toCursor.size(); i++) {
            pt = toCursor.get(i);
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.changeBackground(pt.x, pt.y, SColor.lerpFloatColors(bgColors[pt.x][pt.y], WHITE_FLOAT, 0.75f));
        }
        //this helps compatibility with the HTML target, which doesn't support String.format()
        char[] spaceArray = new char[gridWidth];
        Arrays.fill(spaceArray, ' ');
        String spaces = String.valueOf(spaceArray);

        for (int i = 0; i < 6; i++) {
            display.print(0, gridHeight + i + 1, spaces, SColor.BLACK, SColor.CREAM);
            display.print(2, gridHeight + i + 1, lang[(langIndex + i) % lang.length], SColor.BLACK, SColor.CREAM);
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            // this doesn't check for input, but instead processes and removes Coords from awaitedMoves.
            if (!display.hasActiveAnimations()) {
                //secondsWithoutMoves += Gdx.graphics.getDeltaTime();
                //if (secondsWithoutMoves >= 0.01) {
                //    secondsWithoutMoves = 0;
                    Coord m = awaitedMoves.remove(0);
                    if(!toCursor.isEmpty())
                        toCursor.remove(0);
                    move(m.x - player.x, m.y - player.y);
                //}
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
                    playerToCursor.setGoal(player);
                    playerToCursor.scan(blockage);
                }
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if(input.hasNext()) {
            input.next();
        }
        // certain classes that use scene2d.ui widgets need to be told to act() to process input.
        stage.act();
        // stage has its own batch and must be explicitly told to draw().
        stage.draw();
    }

    @Override
	public void resize(int width, int height) {
		super.resize(width, height);
        //very important to have the mouse behave correctly if the user fullscreens or resizes the game!
		input.getMouse().reinitialize((float) width / gridWidth, (float)height / (gridHeight + bonusHeight), gridWidth, gridHeight, 0, 0);
	}
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib GDX Basic Demo";
        config.width = gridWidth * cellWidth;
        config.height = (gridHeight + bonusHeight) * cellHeight;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
        new LwjglApplication(new SparseDemo(), config);
    }

}
