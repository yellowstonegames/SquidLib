package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidmath.GWTRNG;

/**
 * A convenience class that groups several commonly-used GUI classes into one object and provides ways to
 * initialize these kits for specific purposes, some of which would be a challenge to write without this code.
 * Created by Tommy Ettinger on 8/11/2016.
 */
public class StarterKit {
    /**
     * One of the more critical parts of rendering text is what font to use, and textFactory should usually
     * not be reassigned during a game because so many things depend on this value or a copy of it (so the
     * change might not affect what it was expected to, and might break other things).
     */
    public TextCellFactory textFactory;
    /**
     * The main way to interact with a text-based grid as for roguelikes. A SquidLayers object stores a
     * background and foreground SquidPanel, and this configures them as requested.
     */
    public SparseLayers layers;
    /**
     * The number of grid spaces on the x axis.
     */
    public int gridWidth;
    /**
     * The number of grid spaces on the y axis.
     */
    public int gridHeight;
    /**
     * The width of a cell that holds one char, in "relative pixels," where the screen is expected to stretch so
     * one relative pixel does not generally refer to one actual screen pixel (since high-DPI phones and
     * laptops may make a single pixel virtually impossible to see with the naked eye).
     * <br>
     * By default, this value is doubled to make stretching look more smooth.
     */
    public int cellWidth;
    /**
     * The height of a cell that holds one char, in "relative pixels," where the screen is expected to stretch
     * so one relative pixel does not generally refer to one actual screen pixel (since high-DPI phones and
     * laptops may make a single pixel virtually impossible to see with the naked eye).
     * <br>
     * By default, this value is doubled to make stretching look more smooth.
     */
    public int cellHeight;

    /**
     * All visible parts of this class are in the Stage, and if you add additional widget or scene2d.ui Actor
     * values to your game, they should probably be added to this Stage.
     */
    public Stage stage;
    /**
     * Used to draw lots of things, but mostly handled internally by the Stage.
     * You may need to call {@code batch.begin()} and {@code batch.end()} in some cases where you want to
     * render something that isn't a child of stage but is an Actor or similar render-able object.
     * This can apply a filter from {@link FloatFilters} (or a custom {@link FloatFilter}) to all drawn colors. 
     */
    public FilterBatch batch;
    /**
     * An important part of how this will be displayed; the viewport defaults to a displayed width of
     * {@code cellWidth * gridWidth} and a displayed height of {@code cellHeight * gridHeight}, after cellWidth
     * and cellHeight were doubled by default, and will be stretched or shrunk to fit the actual screen size.
     */
    public Viewport viewport;

    /**
     * Almost all of SquidLib comes into contact with randomness at some point, so this is a good place to show one
     * way of handling that randomness. GWTRNG acts as a normal implementation of {@link squidpony.squidmath.IRNG},
     * can be "seeded" at the start to set the initial state, like any other RNG, but it can also have the current state
     * acquired later with {@link GWTRNG#getState()} or have the current state set in-place with
     * {@link GWTRNG#setState(long)} (note, this doesn't create a new RNG, like you would have to do to re-seed with
     * java.util.Random). This can be useful to get a snapshot of the random sequence where you might want to take an
     * action, undo it back to the snapshot, and try again. It can also be useful for saving the game and reloading it
     * exactly, though the optional serialization in squidlib-extra also does this. You can pass a GWTRNG to anything
     * that expects an IRNG, and you'll encounter a lot of methods that employ IRNG (and some that specifically require
     * or prefer {@link squidpony.squidmath.IStatefulRNG}, which includes GWTRNG) throughout squidlib-util.
     * <br>
     * This field defaults to a GWTRNG seeded with the number SQUIDLIB (written in base 36), or 2252637788195L in
     * base 10. Its algorithm can produce 2 to the 64 minus 1 numbers before repeating, and as the name might suggest,
     * it should perform especially well on Google Web Toolkit for HTML deployment.
     */
    public GWTRNG rng = new GWTRNG(2252637788195L);

    /**
     * Constructs a StarterKit with the given width and height in cells (gridWidth and gridHeight) and the given width
     * and height for each letter (cellWidth and cellHeight), using a default font that is about half as wide as it is
     * tall but can stretch to other aspect ratios.
     * @param gridWidth the width of the display area in cells
     * @param gridHeight the height of the display area in cells
     * @param cellWidth the width of a single cell in pixels, before any stretching is applied
     * @param cellHeight the height of a single cell in pixels, before any stretching is applied
     */
    public StarterKit(int gridWidth, int gridHeight, int cellWidth, int cellHeight)
    {
        this(DefaultResources.getStretchableFont(), gridWidth, gridHeight, cellWidth, cellHeight);
    }
    /**
     * Constructs a StarterKit with the given width and height in cells (gridWidth and gridHeight) and the given width
     * and height for each letter (cellWidth and cellHeight), using the given TextCellFactory for the font. You can use
     * any of the pre-constructed TextCellFactory objects in {@link DefaultResources}, such as
     * {@link DefaultResources#getCrispLeanFamily()}, {@link DefaultResources#getCrispDejaVuFont()},
     * {@link DefaultResources#getCrispSlabFamily()}, or {@link DefaultResources#getStretchableTypewriterFont()}, as
     * long as you have the right assets available (their documentation says the exact files you need). While you can
     * construct your own TextCellFactory given a BitmapFont, that won't work well as a distance field font unless you
     * used some very unusual configuration making the font, so the font would only look good at one size or possibly a
     * multiple of that size. The defaults are recommended for now; a separate project is used to make the distance
     * field monospace fonts (<a href="https://github.com/tommyettinger/Glamer">tommyettinger/Glamer</a> on GitHub)
     * That project also serves as storage for fonts that were made with Glamer, and appropriately-licensed fonts are
     * added to the "premade" folder once they are converted.
     * <br>
     * If you don't know what font to pick, {@link DefaultResources#getCrispLeanFamily()} and
     * {@link DefaultResources#getCrispSlabFamily()} have the same (very large) character coverage, and have bold and
     * italic modes that can be accessed with {@link GDXMarkup} if you decide to use that later.
     * @param textFactory the TextCellFactory to use for the font
     * @param gridWidth the width of the display area in cells
     * @param gridHeight the height of the display area in cells
     * @param cellWidth the width of a single cell in pixels, before any stretching is applied
     * @param cellHeight the height of a single cell in pixels, before any stretching is applied
     */
    public StarterKit(TextCellFactory textFactory, int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(textFactory, gridWidth, gridHeight, cellWidth, cellHeight, 0, 0);
    }
    /**
     * Constructs a StarterKit with the given width and height in cells (gridWidth and gridHeight) and the given width
     * and height for each letter (cellWidth and cellHeight), using the given TextCellFactory for the font; this
     * overload also allows specifying additional space in pixels to be added to the right or bottom sides of the area
     * with the grid of chars. You can use any of the pre-constructed TextCellFactory objects in
     * {@link DefaultResources}, such as {@link DefaultResources#getCrispLeanFamily()}
     * {@link DefaultResources#getCrispDejaVuFont()}, {@link DefaultResources#getCrispSlabFamily()}, or
     * {@link DefaultResources#getStretchableTypewriterFont()}, as long as you have the right assets available (their
     * documentation says the exact files you need). While you can construct your own TextCellFactory given a
     * BitmapFont, that won't work well as a distance field font unless you used some very unusual configuration making
     * the font, so the font would only look good at one size or possibly a multiple of that size. The defaults are
     * recommended for now; a separate project is used to make the distance field monospace fonts
     * (<a href="https://github.com/tommyettinger/Glamer">tommyettinger/Glamer</a> on GitHub). That project also serves
     * as storage for fonts that were made with Glamer, and appropriately-licensed fonts are added to the "premade"
     * folder once they are converted.
     * <br>
     * If you don't know what font to pick, {@link DefaultResources#getCrispLeanFamily()} and
     * {@link DefaultResources#getCrispSlabFamily()} have the same (very large) character coverage, and have bold and
     * italic modes that can be accessed with {@link GDXMarkup} if you decide to use that later.
     * @param textFactory the TextCellFactory to use for the font
     * @param gridWidth the width of the display area in cells
     * @param gridHeight the height of the display area in cells
     * @param cellWidth the width of a single cell in pixels, before any stretching is applied
     * @param cellHeight the height of a single cell in pixels, before any stretching is applied
     * @param additionalWidth the width in pixels to add to the stretched area, before any stretching is applied
     * @param additionalHeight the height in pixels to add to the stretched area, before any stretching is applied
     */
    public StarterKit(TextCellFactory textFactory, int gridWidth, int gridHeight, int cellWidth, int cellHeight,
                      int additionalWidth, int additionalHeight) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.textFactory = textFactory == null ? DefaultResources.getCrispLeanFamily() : textFactory;
        layers = new SparseLayers(gridWidth, gridHeight, this.cellWidth, this.cellHeight, this.textFactory);
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        batch = new FilterBatch();
        viewport = new StretchViewport(this.cellWidth * gridWidth + additionalWidth, this.cellHeight * gridHeight + additionalHeight);
        stage = new Stage(viewport, batch);
        stage.addActor(layers);
    }

    /**
     * Not a complete drawing solution; so much of the logic related to drawing is specific to each game, like
     * FOV being used to make certain things not render if they are out of sight, that this doesn't even try to
     * guess at what a particular game needs for its rendering code. Any {@link TextCellFactory.Glyph} objects in
     * {@link #layers} will be rendered by that SparseLayers, but any that aren't stored in layers must be drawn
     * separately (Glyph has a {@link TextCellFactory.Glyph#draw(Batch, float)} method that must be called between
     * {@link Batch#begin()} and {@link Batch#end()}, typically with begin() called before all Glyphs are drawn in
     * a loop and then with end() called after). 
     * <br>
     * Specifically, this applies the current viewport to the stage, draws the stage, and makes any actions or
     * events related to the stage take effect. Should not be called inside a {@link FilterBatch#begin()} block,
     * since this calls it itself by drawing the stage, and also calls {@link FilterBatch#end()} afterwards.
     */
    public void draw()
    {
        stage.getViewport().apply(true);
        stage.act();
        stage.draw();
    }
    
    /**
     * Not a complete resize method; this is meant to handle the resizing of this StarterKit only and should be
     * called inside your main Game, ApplicationListener, etc. class' resize method.
     * @param width the new width of the screen; should be a parameter from the other resize() method
     * @param height the new height of the screen; should be a parameter from the other resize() method
     */
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

}
