package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidgrid.mapping.shape.TiledShape;

/**
 * Creates a map for use in creating adventure areas.
 * 
 * Currently does not have any functionality, in place for future expansion.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class DungeonGenerator {

    private TiledShape[] layers;
    private String[] layerNames;

    /**
     * 
     *
     * @param layers
     */
    public DungeonGenerator(TiledShape[] layers) {
        this(layers, null);
    }

    public DungeonGenerator(TiledShape[] layers, String[] layerNames) {
        this.layers = layers;
        this.layerNames = layerNames;
    }

}
