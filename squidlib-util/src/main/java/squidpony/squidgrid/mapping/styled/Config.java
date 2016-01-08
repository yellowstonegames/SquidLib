package squidpony.squidgrid.mapping.styled;

/**
 * Part of the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Config {
    public boolean is_corner;
    public int num_color_0, num_color_1, num_color_2, num_color_3, num_color_4 = 0, num_color_5 = 0;
    public int num_x_variants, num_y_variants, short_side_length;

    /**
     * Probably not something you will construct manually. See DungeonGen .
     */
    public Config() {
        is_corner = true;
        num_color_0 = 1;
        num_color_1 = 1;
        num_color_2 = 1;
        num_color_3 = 1;
        num_x_variants = 1;
        num_y_variants = 1;
    }

    public Config(boolean is_corner, int num_color_0, int num_color_1, int num_color_2, int num_color_3,
                  int num_color_4, int num_color_5, int num_x_variants, int num_y_variants, int short_side_length) {
        this.is_corner = is_corner;
        this.num_color_0 = num_color_0;
        this.num_color_1 = num_color_1;
        this.num_color_2 = num_color_2;
        this.num_color_3 = num_color_3;
        this.num_color_4 = num_color_4;
        this.num_color_5 = num_color_5;
        this.num_x_variants = num_x_variants;
        this.num_y_variants = num_y_variants;
        this.short_side_length = short_side_length;
    }

    public String toString()
    {
        return "new Config(" + is_corner + ", " +num_color_0 + ", " + num_color_1  + ", " + num_color_2 +
                ", " + num_color_3 + ", " + num_color_4  + ", " + num_color_5  + ", " + num_x_variants  +
                ", " + num_y_variants  + ", " + short_side_length + ")";
    }
}
