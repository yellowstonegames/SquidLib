package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;

/**
 * This mouse processor allows for easy conversion to a grid based system. This
 * class covers all aspects of mouse movement and clicking, passing those off
 * to a given InputProcessor after converting to cell-based grid coordinates
 * instead of pixel-based screen coordinates. It also passes off scroll events
 * to the InputProcessor without additional changes.
 *
 * This class is meant to be used as a wrapper to your own mouse InputProcessor,
 * it simply converts the coordinates from UI Component x,y to Grid based x,y
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 */
public class SquidMouse extends InputAdapter {

    protected float cellWidth, cellHeight, gridWidth, gridHeight;
    protected int offsetX, offsetY;
    protected InputProcessor processor;
    
    private int gridX, gridY;

    /**
     * Sets the size of the cell so that all mouse input can be evaluated as
     * relative to the grid. All input is passed to the provided InputProcessor once
     * it has had its coordinates translated to grid coordinates.
     *
     * Offsets are initialized to 0 here, and the grid is assumed to take up the
     * full game window.
     *
     * @param cellWidth the width of one cell in screen coordinates, usually pixels
     * @param cellHeight the height of one cell in screen coordinates, usually pixels
     * @param processor an InputProcessor that implements some of touchUp(), touchDown(), touchDragged(), mouseMoved(), or scrolled().
     */
    public SquidMouse(float cellWidth, float cellHeight, InputProcessor processor) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.processor = processor;
        offsetX = 0;
        offsetY = 0;
        gridWidth = Gdx.graphics.getWidth() / cellWidth;
        gridHeight = Gdx.graphics.getHeight() / cellHeight;
    }

    /**
     * Sets the size of the cell so that all mouse input can be evaluated as
     * relative to the grid. Offsets can be specified for x and y if the grid
     * is displayed at a position other than the full screen. Specify the
     * width and height in grid cells of the area to receive input, as well as
     * the offsets from the bottom and left edges also measured in screen
     * coordinates, which are often pixels but may be stretched or shrunk.
     * All input is passed to the provided InputProcessor once it's had its
     * coordinates translated to grid coordinates.
     *
     * If the input is not within the bounds of the grid as determined by
     * gridWidth, gridHeight, offsetX, and offsetY, the input will be clamped.
     *
     * @param cellWidth the width of one cell in screen coordinates, usually pixels
     * @param cellHeight the height of one cell in screen coordinates, usually pixels
     * @param gridWidth in number of cells horizontally on the grid
     * @param gridHeight in number of cells vertically on the grid
     * @param offsetX the horizontal offset in screen coordinates, usually pixels
     * @param offsetY the vertical offset in screen coordinates, usually pixels
     * @param processor an InputProcessor that implements some of touchUp(), touchDown(), touchDragged(), mouseMoved(), or scrolled().
     */
    public SquidMouse(float cellWidth, float cellHeight, float gridWidth, float gridHeight, int offsetX, int offsetY, InputProcessor processor) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.processor = processor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    public float getCellWidth() {
        return cellWidth;
    }

    public float getCellHeight() {
        return cellHeight;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public float getGridWidth() {
        return gridWidth;
    }

    public float getGridHeight() {
        return gridHeight;
    }

    public void setCellWidth(float cellWidth) {
        this.cellWidth = cellWidth;
    }

    public void setCellHeight(float cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setGridWidth(float gridWidth) {
        this.gridWidth = gridWidth;
    }

    public void setGridHeight(float gridHeight) {
        this.gridHeight = gridHeight;
    }

    public void reinitialize(float cellWidth, float cellHeight) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        offsetX = 0;
        offsetY = 0;
        gridWidth = Gdx.graphics.getWidth() / cellWidth;
        gridHeight = Gdx.graphics.getHeight() / cellHeight;
    }

    public void reinitialize(float cellWidth, float cellHeight, float gridWidth, float gridHeight, int offsetX, int offsetY) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    /**
     * Gets the InputProcessor this object uses to handle mouse input.
     *
     * @return the wrapped InputProcessor.
     */
    public InputProcessor getProcessor() {
        return processor;
    }

    /**
     * Sets the InputProcessor this object uses to handle mouse input.
     *
     * @param processor an InputProcessor that implements some of touchUp(), touchDown(), touchDragged(), mouseMoved(), or scrolled().
     */
    public void setProcessor(InputProcessor processor) {
        this.processor = processor;
    }

    /**
     * Translates the given screen pixel coordinate to the underlying grid coordinate, taking into account the related offset.
     *
     * @param screenX
     * @return
     */
    public int translateX(int screenX) {
        return MathUtils.floor((screenX + offsetX) / cellWidth);
    }

    /**
     * Translates the given screen pixel coordinate to the underlying grid coordinate, taking into account the related offset.
     *
     * @param screenY
     * @return
     */
    public int translateY(int screenY) {
        return MathUtils.floor((screenY + offsetY) / cellHeight);
    }

    /**
     * Returns true if the provided screen coordinate is on the grid, ignoring the current offset.
     *
     * @param gridX
     * @param gridY
     * @return
     */
    public boolean onGrid(int gridX, int gridY) {
        return gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        gridX = translateX(screenX);
        gridY = translateY(screenY);
        if (onGrid(gridX, gridY)) {
            return processor.touchDown(gridX, gridY, pointer, button);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        gridX = translateX(screenX);
        gridY = translateY(screenY);
        if (onGrid(gridX, gridY)) {
            return processor.touchUp(screenX, screenY, pointer, button);
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        gridX = translateX(screenX);
        gridY = translateY(screenY);
        if (onGrid(gridX, gridY)) {
            return processor.touchDragged(screenX, screenY, pointer);
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        gridX = translateX(screenX);
        gridY = translateY(screenY);
        if (onGrid(gridX, gridY)) {
            return processor.mouseMoved(screenX, screenY);
        }
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return processor.scrolled(amountX, amountY);
    }
}
