package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidmath.StatefulRNG;

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
    public SquidLayers layers;
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
     */
    public SpriteBatch batch;
    /**
     * An important part of how this will be displayed; the viewport defaults to a displayed width of
     * {@code cellWidth * gridWidth} and a displayed height of {@code cellHeight * gridHeight}, after cellWidth
     * and cellHeight were doubled by default, and will be stretched or shrunk to fit the actual screen size.
     */
    public Viewport viewport;

    /**
     * Almost all of SquidLib comes into contact with randomness at some point, so this is a good place to show one
     * way of handling that randomness. StatefulRNG can be "seeded" at the start to set the initial state, like any
     * other RNG, but it can also have the current state acquired later with {@link StatefulRNG#getState()} or have the
     * current state set in-place with {@link StatefulRNG#setState(long)} (note, this doesn't create a new RNG, like you
     * would have to do to re-seed with java.util.Random). This can be useful to get a snapshot of the random sequence
     * where you might want to take an action, undo it back to the snapshot, and try again. It can also be useful for
     * saving the game and reloading it exactly, though the optional serialization in squidlib-extra also does this.
     * You can pass a StatefulRNG to anything that expects an RNG, and you'll encounter a lot of methods that employ
     * RNG (and some that specifically require or prefer StatefulRNG) throughout squidlib-util.
     * <br>
     * This field defaults to a StatefulRNG seeded with the number SQUIDLIB (written in base 36), or 2252637788195L in
     * base 10. Like most StatefulRNG instances, it uses a LinnormRNG internally, which is very fast and has a good-enough
     * period (how many numbers it generates before repeating the cycle) at (2 to the 64) random numbers.
     */
    public StatefulRNG rng = new StatefulRNG(2252637788195L);

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
     * any of the pre-constructed TextCellFactory objects in {@link DefaultResources}. such as
     * {@link DefaultResources#getStretchableTypewriterFont()}, {@link DefaultResources#getStretchableDejaVuFont()},
     * {@link DefaultResources#getStretchableSquareFont()}, or {@link DefaultResources#getStretchableCodeFont()}, as
     * long as you have the right assets available (their documentation says the exact files you need). While you can
     * construct your own TextCellFactory given a BitmapFont, that won't work well as a distance field font unless you
     * used some very unusual configuration making the font, so the font would only look good at one size or possibly a
     * multiple of that size. The defaults are recommended for now; a separate project is used to make the distance
     * field monospace fonts (tommyettinger/Glamer on GitHub) and more can be made as needed from permissively-licensed
     * fonts if a game has particular aesthetic requirements.
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
     * and height for each letter (cellWidth and cellHeight), using the given TextCellFactory for the font. You can use
     * any of the pre-constructed TextCellFactory objects in {@link DefaultResources}. such as
     * {@link DefaultResources#getStretchableTypewriterFont()}, {@link DefaultResources#getStretchableDejaVuFont()},
     * {@link DefaultResources#getStretchableSquareFont()}, or {@link DefaultResources#getStretchableCodeFont()}, as
     * long as you have the right assets available (their documentation says the exact files you need). While you can
     * construct your own TextCellFactory given a BitmapFont, that won't work well as a distance field font unless you
     * used some very unusual configuration making the font, so the font would only look good at one size or possibly a
     * multiple of that size. The defaults are recommended for now; a separate project is used to make the distance
     * field monospace fonts (tommyettinger/Glamer on GitHub) and more can be made as needed from permissively-licensed
     * fonts if a game has particular aesthetic requirements.
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
        this.textFactory = DefaultResources.getStretchableFont()
                .width(this.cellWidth).height(this.cellHeight).initBySize();
        layers = new SquidLayers(gridWidth, gridHeight, this.cellWidth, this.cellHeight, textFactory);
        layers.setTextSize(cellWidth, cellHeight + 2);
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        batch = new SpriteBatch();
        viewport = new StretchViewport(this.cellWidth * gridWidth + additionalWidth, this.cellHeight * gridHeight + additionalHeight);
        stage = new Stage(viewport, batch);
        stage.addActor(layers);
    }

    /**
     * Not a complete drawing solution; so much of the logic related to drawing is specific to each game, like
     * FOV being used to make certain things not render if they are out of sight, that this doesn't even try to
     * guess at what a particular game needs for its rendering code. You should probably draw any AnimatedEntity
     * objects, like what {@link SquidLayers#animateActor(int, int, char, Color)} returns, separately and after
     * calling this method. The recommended way to draw those objects is with {@link #drawEntity(AnimatedEntity)},
     * which must be called between SpriteBatch's begin() and end() methods, while this method cannot be called
     * between those SpriteBatch methods. The solution, usually, is to call this method, then call
     * {@link SpriteBatch#begin()}, do any logic to determine what AnimatedEntity objects need to be shown (are
     * they in FOV, are they alive, etc.), draw the ones that should be shown with drawEntity(), and finally
     * call {@link SpriteBatch#end()} when no more AnimatedEntity objects need to be drawn. Note that this
     * method renders all of {@link #stage}, which may include other GUI elements using scene2d.ui, but the
     * AnimatedEntity objects in a SquidLayers aren't part of any Stage to allow them to be rendered as special
     * cases for visibility.
     * <br>
     * Specifically, this applies the current viewport to the stage, draws the stage, and makes any actions or
     * events related to the stage take effect. Should not be called inside a {@link SpriteBatch#begin()} block,
     * since this calls it itself by drawing the stage, and also calls {@link SpriteBatch#end()} afterwards.
     */
    public void draw()
    {
        stage.getViewport().apply(true);
        stage.draw();
        stage.act();
    }

    /**
     * Draws an AnimatedEntity object; must be called between {@link SpriteBatch#begin()} and {@link SpriteBatch#end()}.
     * You can obtain the correct batch with the {@link #batch} field, and ideally all calls to this method will be
     * inside a single block of one begin() and one end(), that is, the batch shouldn't start and end for each entity
     * to draw.
     * @param entity an AnimatedEntity to draw, usually obtained through one of many methods in {@link SquidLayers}
     */
    public void drawEntity(AnimatedEntity entity)
    {
        if(batch.isDrawing())
            layers.drawActor(batch, 1.0f, entity);
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
