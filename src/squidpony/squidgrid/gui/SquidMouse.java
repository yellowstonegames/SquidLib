package squidpony.squidgrid.gui;

import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

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
public class SquidMouse implements MouseInputListener {

    private final int cellWidth, cellHeight;
    private final MouseInputListener listener;

    /**
     * Sets the size of the cell so that all mouse input can be evaluated as
     * relative to the grid. All input is passed to the provided listener once
     * it has had its coordinates translated to grid coordinates.
     *
     * @param cellWidth
     * @param cellHeight
     * @param listener
     */
    public SquidMouse(int cellWidth, int cellHeight, MouseInputListener listener) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.listener = listener;
    }

    private MouseEvent translateToGrid(MouseEvent e) {
        int x = e.getX() / cellWidth;
        int y = e.getY() / cellHeight;
        return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        listener.mouseClicked(translateToGrid(e));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        listener.mousePressed(translateToGrid(e));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        listener.mouseReleased(translateToGrid(e));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        listener.mouseEntered(translateToGrid(e));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        listener.mouseExited(translateToGrid(e));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        listener.mouseDragged(translateToGrid(e));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        listener.mouseMoved(translateToGrid(e));
    }
}
