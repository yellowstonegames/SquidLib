package squidpony.squidgrid.fov;

import squidpony.annotation.Beta;

/**
 * Interface for FOVSolvers which can work with thin walls.
 *
 * Thin walls are walls which are between grid cells, signified by indicating a
 * wall on the edge of a cell.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public interface ThinWallFOVSolver extends FOVSolver{
    
}
