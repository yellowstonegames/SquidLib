package squidpony.squidgrid.mapping.locks.generators;

import squidpony.squidgrid.mapping.locks.IRoomLayout;

/**
 * Interface for classes that provide methods to procedurally generate new
 * {@link IRoomLayout}s.
 */
public interface ILayoutGenerator {

    /**
     * Generates a new {@link IRoomLayout}.
     */
    void generate();
    
    /**
     * Gets the most recently generated {@link IRoomLayout}.
     * 
     * @return the most recently generated IRoomLayout
     */
    IRoomLayout getRoomLayout();
    
}
