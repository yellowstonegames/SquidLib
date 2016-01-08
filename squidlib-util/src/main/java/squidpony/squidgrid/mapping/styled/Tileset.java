package squidpony.squidgrid.mapping.styled;

import java.util.Arrays;

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

    public Tileset(Config config, Maximums max_tiles, Tile[] h_tiles, Tile[] v_tiles) {
        this.config = config;
        this.max_tiles = max_tiles;
        this.h_tiles = h_tiles;
        this.v_tiles = v_tiles;
    }

    public String toString()
    {
        String hStr = Arrays.deepToString(h_tiles), vStr = Arrays.deepToString(v_tiles);
        hStr = hStr.substring(1, hStr.length() - 1);
        vStr = vStr.substring(1, vStr.length() - 1);

        return "new Tileset(" + config + ", " + max_tiles + ", new Tile[]{" + hStr + "}, new Tile[]{" +
                vStr + "})";
    }
}