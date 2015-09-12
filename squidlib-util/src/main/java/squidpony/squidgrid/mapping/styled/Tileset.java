package squidpony.squidgrid.mapping.styled;

/**
 * The outermost class in the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Tileset {
    public Config config;
    public Maximums max_tiles;
    public Tile[] h_tiles, v_tiles;

    /**
     * Probably not something you will construct manually. See DungeonGen .
     */
    public Tileset() {
        config = new Config();
        max_tiles = new Maximums();
        h_tiles = new Tile[]{};
        v_tiles = new Tile[]{};
    }
}