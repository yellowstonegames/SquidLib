package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

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

    public StarterKit(int gridWidth, int gridHeight, int cellWidth, int cellHeight)
    {
        this(DefaultResources.getStretchableFont(), gridWidth, gridHeight, cellWidth, cellHeight);
    }

    public StarterKit(TextCellFactory textFactory, int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(textFactory, gridWidth, gridHeight, cellWidth, cellHeight, 0, 0);
    }
    public StarterKit(TextCellFactory textFactory, int gridWidth, int gridHeight, int cellWidth, int cellHeight,
                      int additionalWidth, int additionalHeight) {
        this.cellWidth = cellWidth * 2;
        this.cellHeight = cellHeight * 2;
        this.textFactory = DefaultResources.getStretchableFont().setSmoothingMultiplier(2f / 3f)
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
     * guess at what a particular game needs for its rendering code. You should probably draw
     *
     * Applies the current viewport to the stage, draws the stage, and makes any actions or events related to
     * the stage take effect. Should not be called inside a {@link SpriteBatch#begin()} block, since this calls
     * it itself.
     */
    public void draw()
    {
        stage.getViewport().apply(true);
        stage.draw();
        stage.act();
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
