package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.InputProcessor;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;

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
public class SquidMouse implements InputProcessor {

    protected final int cellWidth, cellHeight, offsetX, offsetY;
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
    public SquidMouse(int cellWidth, int cellHeight, InputProcessor processor) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.processor = processor;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    /**
     * Sets the size of the cell so that all mouse input can be evaluated as
     * relative to the grid. Offsets can be specified for x and y if the grid
     * is displayed at a position other than the full screen. All input is
     * passed to the provided InputProcessor once it has had its coordinates
     * translated to grid coordinates.
     *
     * If either offset is non-zero, then the InputProcessor must be able to
     * handle grid coordinates that are negative or higher than the dimensions
     * of the grid itself, since clicks outside of where the grid is displayed
     * are by definition not within bounds.
     *
     * @param cellWidth
     * @param cellHeight
     * @param offsetX
     * @param offsetY
     * @param processor an InputProcessor that implements some of touchUp(), touchDown(), touchDragged(), mouseMoved(), or scrolled().
     */
    public SquidMouse(int cellWidth, int cellHeight, int offsetX, int offsetY, InputProcessor processor) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.processor = processor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
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
        return  (screenX - offsetX) / cellWidth;
    }
    protected int translateY(int screenY) {
        return  (screenY - offsetY) / cellHeight;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return processor.touchDown(translateX(screenX), translateY(screenY), pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return processor.touchUp(translateX(screenX), translateY(screenY), pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return processor.touchDragged(translateX(screenX), translateY(screenY), pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return processor.mouseMoved(translateX(screenX), translateY(screenY));
    }

    @Override
    public boolean scrolled(int amount) {
        return processor.scrolled(amount);
    }
}
