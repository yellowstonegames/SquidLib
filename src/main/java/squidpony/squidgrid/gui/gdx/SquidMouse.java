package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.InputProcessor;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;

/**
 * This mouse listener allows for easy interface with a grid based system. This
 * class covers all aspects of mouse movement and clicking, but not mouse wheel
 * events
 *
 * This class is meant to be used as a wrapper to your own mouse listener, it
 * simply converts the coordinates from UI Component x,y to Grid based x,y
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SquidMouse implements InputProcessor {

    private final int cellWidth, cellHeight;
    private InputProcessor processor;

    /**
     * Sets the size of the cell so that all mouse input can be evaluated as
     * relative to the grid. All input is passed to the provided InputProcessor once
     * it has had its coordinates translated to grid coordinates.
     *
     * @param cellWidth
     * @param cellHeight
     * @param processor an InputProcessor that implements some of touchUp(), touchDown(), touchDragged(), mouseMoved(), or scrolled().
     */
    public SquidMouse(int cellWidth, int cellHeight, InputProcessor processor) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.processor = processor;
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

    private int translateX(int screenX) {
        return  screenX / cellWidth;
    }
    private int translateY(int screenY) {
        return  screenY / cellHeight;
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
