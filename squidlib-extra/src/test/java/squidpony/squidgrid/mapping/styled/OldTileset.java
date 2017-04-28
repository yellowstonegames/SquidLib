package squidpony.squidgrid.mapping.styled;

/**
 * The outermost class in the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class OldTileset {
    public OldConfig config;
    public Maximums max_tiles;
    public OldTile[] h_tiles, v_tiles;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public OldTileset() {
        config = new OldConfig();
        max_tiles = new Maximums();
        h_tiles = new OldTile[]{};
        v_tiles = new OldTile[]{};
    }
}