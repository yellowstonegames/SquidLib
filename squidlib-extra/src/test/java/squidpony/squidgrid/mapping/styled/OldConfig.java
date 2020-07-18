package squidpony.squidgrid.mapping.styled;

/**
 * Part of the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class OldConfig {

	/* If this list of fields is modified, TilesetsGenerator should be modified too */

    public boolean is_corner;
    public int num_color_0, num_color_1, num_color_2, num_color_3, num_color_4, num_color_5;
    public int num_x_variants, num_y_variants, short_side_length;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public OldConfig() {
        is_corner = true;

        num_color_0 = 1;
        num_color_1 = 1;
        num_color_2 = 1;
        num_color_3 = 1;
        num_x_variants = 1;
        num_y_variants = 1;
    }
}
