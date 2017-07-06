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
    protected int  offsetX, offsetY;
    protected InputProcessor processor;

    /**
     * Sets the size of the cell so that all mouse input can be evaluated as
     * relative to the grid. All input is passed to the provided InputProcessor once
     * it has had its coordinates translated to grid coordinates.
     *
     * Offsets are initialized to 0 here, and the grid is assumed to take up the
     * full game window.
     *
     * @param cellWidth
     * @param cellHeight
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
     * the offsets from the bottom and left edges also measured in grid cells.
     * All input is passed to the provided InputProcessor once it's had its
     * coordinates translated to grid coordinates.
     *
     * If the input is not within the bounds of the grid as determined by
     * gridWidth, gridHeight, offsetX, and offsetY, the input will be clamped.
     *
     * @param cellWidth
     * @param cellHeight
     * @param gridWidth
     * @param gridHeight
     * @param offsetX
     * @param offsetY
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


    public void reinitialize(float cellWidth, float cellHeight)
    {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        offsetX = 0;
        offsetY = 0;
        gridWidth = Gdx.graphics.getWidth() / cellWidth;
        gridHeight = Gdx.graphics.getHeight() / cellHeight;
    }
    public void reinitialize(float cellWidth, float cellHeight, float gridWidth, float gridHeight, int offsetX, int offsetY)
    {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    /**
     * Gets the InputProcessor this object uses to handle mouse input.
     * @return the wrapped InputProcessor.
     */
    public InputProcessor getProcessor() {
        return processor;
    }

    /**
     * Sets the InputProcessor this object uses to handle mouse input.
     * @param processor an InputProcessor that implements some of touchUp(), touchDown(), touchDragged(), mouseMoved(), or scrolled().
     */
    public void setProcessor(InputProcessor processor) {
        this.processor = processor;
    }

	protected int translateX(int screenX) {
		return MathUtils.floor((screenX + offsetX) / cellWidth);
	}

	protected int translateY(int screenY) {
		return MathUtils.floor((screenY + offsetY) / cellHeight);
	}

    public boolean onGrid(int screenX, int screenY)
    {
        int tmp = MathUtils.floor((screenX + offsetX) / cellWidth);
        if(tmp < 0 || tmp >= gridWidth)
            return false;
        tmp = MathUtils.floor((screenY + offsetY) / cellHeight);
        return tmp >= 0 && tmp < gridHeight;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(onGrid(screenX, screenY))
            return processor.touchDown(translateX(screenX), translateY(screenY), pointer, button);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(onGrid(screenX, screenY))
            return processor.touchUp(translateX(screenX), translateY(screenY), pointer, button);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(onGrid(screenX, screenY))
            return processor.touchDragged(translateX(screenX), translateY(screenY), pointer);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if(onGrid(screenX, screenY))
            return processor.mouseMoved(translateX(screenX), translateY(screenY));
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return processor.scrolled(amount);
    }
}
