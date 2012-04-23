package squidpony.squidgrid;

import java.awt.Component;
import java.awt.Dimension;

//TODO -- extend this to enable use of arbitrary images for the grid spaces

/**
 * This interface guarantees some basic functionality for swing components displaying
 * arbitrary images in a regular grid. Regular is used in the mathematical sense
 * to mean that all grid spaces have the exact same rectangular dimensions.
 *
 * The coordinate system is (x,y) with the upper left block being (0,0).
 *
 * @author Eben
 */
public interface SGDisplay {
    
    /**
     * Lets the component know that it should update it's display. This should take
     * advantage of the component's default buffering scheme.
     */
    public void refresh();

    /**
     * @return a Dimension representing the size of a single cell in the grid.
     */
    public Dimension getCellDimension();

    /**
     * Manually sets what the dimension of all cells should be. This does not force
     * a redraw or resize. Such methods must be called separately.
     * 
     * @param cellDimension 
     */
    public void setCellDimension(Dimension cellDimension);

    /**
     * @return the number of columns in the current grid.
     */
    public int getColumns();

    /**
     * Sets how many columns to make in the grid. This does not force a redraw
     * or resize. Such methods must be called separately.
     * 
     * @param columns 
     */
    public void setColumns(int columns);

    /**
     * @return the number of rows in the current grid.
     */
    public int getRows();

    /**
     * Sets how many rows to make in the grid. This does not force a redraw or resize.
     * Such methods must be called separately.
     * 
     * @param rows 
     */
    public void setRows(int rows);

    /**
     * @return The Swing Component used to display.
     */
    public Component getComponent();
}
