package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import squidpony.IColorCenter;
import squidpony.panel.IColoredString;

import java.util.ArrayList;

/**
 * A specialized SquidPanel that is meant for displaying messages in a scrolling pane. Height must be at least 3 cells,
 * because clicking/tapping the top or bottom borders (which are part of the grid's height) will scroll up or down.
 * Created by Tommy Ettinger on 12/10/2015.
 */
public class SquidMessageBox extends SquidPanel {
    protected ArrayList<IColoredString<Color>> messages = new ArrayList<IColoredString<Color>>(256);
    protected int messageIndex = 0;
    private char[][] basicBorders;
    /**
     * Creates a bare-bones panel with all default values for text rendering.
     *
     * @param gridWidth  the number of cells horizontally
     * @param gridHeight the number of cells vertically, must be at least 3
     */
    public SquidMessageBox(int gridWidth, int gridHeight) {
        super(gridWidth, gridHeight);
        if(gridHeight < 3)
            throw new ExceptionInInitializerError("gridHeight must be at least 3, was given: " + gridHeight);
        messages.add(new IColoredString.Impl<Color>());
        basicBorders = assembleBorders();
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default square font.
     *
     * @param gridWidth  the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth  the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public SquidMessageBox(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        super(gridWidth, gridHeight, cellWidth, cellHeight);
        if(gridHeight < 3)
            throw new ExceptionInInitializerError("gridHeight must be at least 3, was given: " + gridHeight);
        messages.add(new IColoredString.Impl<Color>());
        basicBorders = assembleBorders();
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     * <p/>
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth  the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory    the factory to use for cell rendering
     */
    public SquidMessageBox(int gridWidth, int gridHeight, TextCellFactory factory) {
        super(gridWidth, gridHeight, factory);
        if(gridHeight < 3)
            throw new ExceptionInInitializerError("gridHeight must be at least 3, was given: " + gridHeight);
        messages.add(new IColoredString.Impl<Color>());
        basicBorders = assembleBorders();
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     * <p/>
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth  the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory    the factory to use for cell rendering
     * @param center     The color center to use. Can be {@code null}, but then must be set later on with
     *                   {@link #setColorCenter(IColorCenter)}.
     */
    public SquidMessageBox(int gridWidth, int gridHeight, final TextCellFactory factory, IColorCenter<Color> center) {
        super(gridWidth, gridHeight, factory, center);
        if(gridHeight < 3)
            throw new ExceptionInInitializerError("gridHeight must be at least 3, was given: " + gridHeight);
        messages.add(new IColoredString.Impl<Color>());
        basicBorders = assembleBorders();
    }
    private void makeBordersClickable()
    {
        addListener(new InputListener(){
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(x >= 0 && x < getWidth())
                {
                    if(y < cellHeight)
                    {
                        nudgeDown();
                        return true;
                    }
                    else if(y >= getHeight() - cellHeight)
                    {
                        nudgeUp();
                        return true;
                    }
                }
                return false;
            }
        });
    }
    public void appendMessage(String message)
    {
        messages.add(new IColoredString.Impl<Color>(message, defaultForeground));
        messageIndex = messages.size() - 1;
    }
    public void appendMessage(IColoredString<Color> message)
    {
        messages.add(message);
        messageIndex = messages.size() - 1;
    }
    public void nudgeUp()
    {
        messageIndex = Math.max(0, messageIndex - 1);
    }
    public void nudgeDown()
    {
        messageIndex = Math.min(messages.size() - 1, messageIndex + 1);
    }
    private char[][] assembleBorders() {
        char[][] result = new char[gridWidth][gridHeight];
        result[0][0] = '┌';
        result[gridWidth - 1][0] = '┐';
        result[0][gridHeight - 1] = '└';
        result[gridWidth - 1][gridHeight - 1] = '┘';
        for (int i = 1; i < gridWidth - 1; i++) {
            result[i][0] = '─';
            result[i][gridHeight - 1] = '─';
        }
        for (int y = 1; y < gridHeight - 1; y++) {
            result[0][y] = '│';
            result[gridWidth - 1][y] = '│';
        }
        for (int y = 1; y < gridHeight - 1; y++) {
            for (int x = 1; x < gridWidth - 1; x++) {
                result[x][y] = ' ';
                result[x][y] = ' ';
            }
        }
        return result;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        put(basicBorders);
        for (int i = 1; i < gridHeight - 1 && i <= messageIndex; i++) {
            put(1, gridHeight - 1 - i, messages.get(messageIndex + 1 - i));
        }
        act(Gdx.graphics.getDeltaTime());
        super.draw(batch, parentAlpha);
    }

    /**
     * Sets the position of the actor's bottom left corner.
     *
     * @param x
     * @param y
     */
    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        setBounds(x, y, gridWidth * cellWidth, gridHeight * cellHeight);
        makeBordersClickable();
    }
}
