package squidpony.squidgrid.fov;

import squidpony.squidgrid.util.RadiusStrategy;
import squidpony.annotation.Beta;

/**
 * This class merges the results from two or more FOVSolvers.
 * 
 * Currently a work in progress.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class MergedFOV implements FOVSolver {
    
    private float totalWeight;

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Used when constructing a MergedFOV to indicate what kind of merge should be performed.
     */
    public enum MergeType {

        /**
         * The weight will be used to average out all light maps.
         */
        WEIGHTED_AVERAGE,
        /**
         * The weight will be used to average out all light maps, but if any
         * light map shows a cell to have no light then it will be marked as
         * unlit.
         */
        AND,
        /**
         * The maximum light from the light maps will be used. The weight is
         * ignored.
         */
        MAXIMUM,
        /**
         * The minimum light from the light maps will be used. The weight is
         * ignored.
         */
        MINIMUM;
        
        float[][] getLight(float[][][] maps){
            int width = maps[0].length;
            int height = maps[0][0].length;
            float[][] result = new float[width][height];
            
            for(int z = 0;z<maps.length;z++){
                for(int x = 0;x<width;x++){
                    for(int y = 0;y<height;y++){
                        switch (this){
                            case WEIGHTED_AVERAGE:
                               
                                break;
                            case AND:
                                
                                break;
                            case MAXIMUM:
                                
                                break;
                            case MINIMUM: 
                               
                                break;
                        }
                    }
                }
            }
            
            return result;
        }
    }
}
