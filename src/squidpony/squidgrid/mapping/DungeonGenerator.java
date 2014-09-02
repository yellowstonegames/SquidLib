package squidpony.squidgrid.mapping;

import squidpony.squidgrid.shape.TiledShape;

/**
 * Creates a map for use in creating adventure areas.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class DungeonGenerator {
    private TiledShape[] layers;
    private String[] layerNames;
    
    /**
     * 
     * 
     * @param layers 
     */
    public DungeonGenerator(TiledShape[] layers){
        this(layers, null);
    }

    public DungeonGenerator(TiledShape[] layers, String[] layerNames) {
        this.layers = layers;
        this.layerNames = layerNames;
    }
        
    
}
