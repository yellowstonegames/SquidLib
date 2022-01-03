/* File generated automatically by TilesetsGenerator.java. Do not edit. This file is committed for convenience. */
package squidpony.tileset;

import squidpony.squidgrid.mapping.styled.*;

/** @author TilesetsGenerator.java */
public class LimitedConnectivity {

  public static final Tileset INSTANCE = new Tileset();

  static {
    /* Initialize #INSTANCE */ 
    INSTANCE.config.is_corner=true;
    INSTANCE.config.num_x_variants=1;
    INSTANCE.config.num_y_variants=1;
    INSTANCE.config.short_side_length=9;
    INSTANCE.config.num_colors[0]=2;
    INSTANCE.config.num_colors[1]=2;
    INSTANCE.config.num_colors[2]=2;
    INSTANCE.config.num_colors[3]=2;
    INSTANCE.max_tiles.h=64;
    INSTANCE.max_tiles.v=64;
    INSTANCE.h_tiles = new Tile[64];
    /* Build h_tiles #0 */
    INSTANCE.h_tiles[0] = new Tile(0,0,0,0,0,0
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #1 */
    INSTANCE.h_tiles[1] = new Tile(1,0,0,0,0,0
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #2 */
    INSTANCE.h_tiles[2] = new Tile(0,1,0,0,0,0
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #3 */
    INSTANCE.h_tiles[3] = new Tile(1,1,0,0,0,0
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #4 */
    INSTANCE.h_tiles[4] = new Tile(0,0,1,0,0,0
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #5 */
    INSTANCE.h_tiles[5] = new Tile(1,0,1,0,0,0
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #6 */
    INSTANCE.h_tiles[6] = new Tile(0,1,1,0,0,0
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #7 */
    INSTANCE.h_tiles[7] = new Tile(1,1,1,0,0,0
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #8 */
    INSTANCE.h_tiles[8] = new Tile(0,0,0,1,0,0
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #9 */
    INSTANCE.h_tiles[9] = new Tile(1,0,0,1,0,0
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #10 */
    INSTANCE.h_tiles[10] = new Tile(0,1,0,1,0,0
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #11 */
    INSTANCE.h_tiles[11] = new Tile(1,1,0,1,0,0
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #12 */
    INSTANCE.h_tiles[12] = new Tile(0,0,1,1,0,0
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #13 */
    INSTANCE.h_tiles[13] = new Tile(1,0,1,1,0,0
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #14 */
    INSTANCE.h_tiles[14] = new Tile(0,1,1,1,0,0
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #15 */
    INSTANCE.h_tiles[15] = new Tile(1,1,1,1,0,0
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #16 */
    INSTANCE.h_tiles[16] = new Tile(0,0,0,0,1,0
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #17 */
    INSTANCE.h_tiles[17] = new Tile(1,0,0,0,1,0
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #18 */
    INSTANCE.h_tiles[18] = new Tile(0,1,0,0,1,0
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #19 */
    INSTANCE.h_tiles[19] = new Tile(1,1,0,0,1,0
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #20 */
    INSTANCE.h_tiles[20] = new Tile(0,0,1,0,1,0
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #21 */
    INSTANCE.h_tiles[21] = new Tile(1,0,1,0,1,0
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #22 */
    INSTANCE.h_tiles[22] = new Tile(0,1,1,0,1,0
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #23 */
    INSTANCE.h_tiles[23] = new Tile(1,1,1,0,1,0
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #24 */
    INSTANCE.h_tiles[24] = new Tile(0,0,0,1,1,0
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #25 */
    INSTANCE.h_tiles[25] = new Tile(1,0,0,1,1,0
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #26 */
    INSTANCE.h_tiles[26] = new Tile(0,1,0,1,1,0
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #27 */
    INSTANCE.h_tiles[27] = new Tile(1,1,0,1,1,0
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #28 */
    INSTANCE.h_tiles[28] = new Tile(0,0,1,1,1,0
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #29 */
    INSTANCE.h_tiles[29] = new Tile(1,0,1,1,1,0
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #30 */
    INSTANCE.h_tiles[30] = new Tile(0,1,1,1,1,0
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #31 */
    INSTANCE.h_tiles[31] = new Tile(1,1,1,1,1,0
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #32 */
    INSTANCE.h_tiles[32] = new Tile(0,0,0,0,0,1
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #33 */
    INSTANCE.h_tiles[33] = new Tile(1,0,0,0,0,1
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #34 */
    INSTANCE.h_tiles[34] = new Tile(0,1,0,0,0,1
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #35 */
    INSTANCE.h_tiles[35] = new Tile(1,1,0,0,0,1
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #36 */
    INSTANCE.h_tiles[36] = new Tile(0,0,1,0,0,1
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #37 */
    INSTANCE.h_tiles[37] = new Tile(1,0,1,0,0,1
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #38 */
    INSTANCE.h_tiles[38] = new Tile(0,1,1,0,0,1
    ,18,9,16,16L,16L,16L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #39 */
    INSTANCE.h_tiles[39] = new Tile(1,1,1,0,0,1
    ,18,9,16,16L,16L,16L,496L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #40 */
    INSTANCE.h_tiles[40] = new Tile(0,0,0,1,0,1
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #41 */
    INSTANCE.h_tiles[41] = new Tile(1,0,0,1,0,1
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,496L,16L,16L,16L,16L);
    /* Build h_tiles #42 */
    INSTANCE.h_tiles[42] = new Tile(0,1,0,1,0,1
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #43 */
    INSTANCE.h_tiles[43] = new Tile(1,1,0,1,0,1
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,511L,16L,16L,16L,16L);
    /* Build h_tiles #44 */
    INSTANCE.h_tiles[44] = new Tile(0,0,1,1,0,1
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #45 */
    INSTANCE.h_tiles[45] = new Tile(1,0,1,1,0,1
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #46 */
    INSTANCE.h_tiles[46] = new Tile(0,1,1,1,0,1
    ,18,9,0,0L,0L,0L,511L,0L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #47 */
    INSTANCE.h_tiles[47] = new Tile(1,1,1,1,0,1
    ,18,9,0,0L,0L,56L,504L,56L,0L,0L,0L,0L,0L,0L,0L,511L,0L,0L,0L,0L);
    /* Build h_tiles #48 */
    INSTANCE.h_tiles[48] = new Tile(0,0,0,0,1,1
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #49 */
    INSTANCE.h_tiles[49] = new Tile(1,0,0,0,1,1
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #50 */
    INSTANCE.h_tiles[50] = new Tile(0,1,0,0,1,1
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #51 */
    INSTANCE.h_tiles[51] = new Tile(1,1,0,0,1,1
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #52 */
    INSTANCE.h_tiles[52] = new Tile(0,0,1,0,1,1
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #53 */
    INSTANCE.h_tiles[53] = new Tile(1,0,1,0,1,1
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #54 */
    INSTANCE.h_tiles[54] = new Tile(0,1,1,0,1,1
    ,18,9,16,16L,16L,16L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #55 */
    INSTANCE.h_tiles[55] = new Tile(1,1,1,0,1,1
    ,18,9,16,16L,16L,16L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #56 */
    INSTANCE.h_tiles[56] = new Tile(0,0,0,1,1,1
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #57 */
    INSTANCE.h_tiles[57] = new Tile(1,0,0,1,1,1
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L,16L);
    /* Build h_tiles #58 */
    INSTANCE.h_tiles[58] = new Tile(0,1,0,1,1,1
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #59 */
    INSTANCE.h_tiles[59] = new Tile(1,1,0,1,1,1
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,16L,16L,16L,16L);
    /* Build h_tiles #60 */
    INSTANCE.h_tiles[60] = new Tile(0,0,1,1,1,1
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #61 */
    INSTANCE.h_tiles[61] = new Tile(1,0,1,1,1,1
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #62 */
    INSTANCE.h_tiles[62] = new Tile(0,1,1,1,1,1
    ,18,9,0,0L,0L,0L,511L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    /* Build h_tiles #63 */
    INSTANCE.h_tiles[63] = new Tile(1,1,1,1,1,1
    ,18,9,0,0L,0L,0L,496L,16L,16L,16L,16L,16L,16L,16L,16L,31L,0L,0L,0L,0L);
    INSTANCE.v_tiles = new Tile[64];
    /* Build v_tiles #0 */
    INSTANCE.v_tiles[0] = new Tile(0,0,0,0,0,0
    ,9,18,8208,8208L,8208L,8208L,8223L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #1 */
    INSTANCE.v_tiles[1] = new Tile(1,0,0,0,0,0
    ,9,18,8208,8208L,8208L,8208L,8223L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #2 */
    INSTANCE.v_tiles[2] = new Tile(0,1,0,0,0,0
    ,9,18,16,16L,16L,28688L,28703L,28688L,8208L,8208L,8208L);
    /* Build v_tiles #3 */
    INSTANCE.v_tiles[3] = new Tile(1,1,0,0,0,0
    ,9,18,16,16L,16L,28688L,28703L,28688L,8208L,8208L,8208L);
    /* Build v_tiles #4 */
    INSTANCE.v_tiles[4] = new Tile(0,0,1,0,0,0
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #5 */
    INSTANCE.v_tiles[5] = new Tile(1,0,1,0,0,0
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #6 */
    INSTANCE.v_tiles[6] = new Tile(0,1,1,0,0,0
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #7 */
    INSTANCE.v_tiles[7] = new Tile(1,1,1,0,0,0
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #8 */
    INSTANCE.v_tiles[8] = new Tile(0,0,0,1,0,0
    ,9,18,8208,8208L,8208L,8208L,8223L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #9 */
    INSTANCE.v_tiles[9] = new Tile(1,0,0,1,0,0
    ,9,18,8208,8208L,8208L,8208L,8223L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #10 */
    INSTANCE.v_tiles[10] = new Tile(0,1,0,1,0,0
    ,9,18,16,16L,16L,28688L,28703L,28688L,8208L,8208L,8208L);
    /* Build v_tiles #11 */
    INSTANCE.v_tiles[11] = new Tile(1,1,0,1,0,0
    ,9,18,16,16L,16L,28688L,28703L,28688L,8208L,8208L,8208L);
    /* Build v_tiles #12 */
    INSTANCE.v_tiles[12] = new Tile(0,0,1,1,0,0
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #13 */
    INSTANCE.v_tiles[13] = new Tile(1,0,1,1,0,0
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #14 */
    INSTANCE.v_tiles[14] = new Tile(0,1,1,1,0,0
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #15 */
    INSTANCE.v_tiles[15] = new Tile(1,1,1,1,0,0
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #16 */
    INSTANCE.v_tiles[16] = new Tile(0,0,0,0,1,0
    ,9,18,8208,8208L,8208L,8208L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #17 */
    INSTANCE.v_tiles[17] = new Tile(1,0,0,0,1,0
    ,9,18,8208,8208L,8208L,8208L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #18 */
    INSTANCE.v_tiles[18] = new Tile(0,1,0,0,1,0
    ,9,18,16,16L,16L,16L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #19 */
    INSTANCE.v_tiles[19] = new Tile(1,1,0,0,1,0
    ,9,18,16,16L,16L,16L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #20 */
    INSTANCE.v_tiles[20] = new Tile(0,0,1,0,1,0
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #21 */
    INSTANCE.v_tiles[21] = new Tile(1,0,1,0,1,0
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #22 */
    INSTANCE.v_tiles[22] = new Tile(0,1,1,0,1,0
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #23 */
    INSTANCE.v_tiles[23] = new Tile(1,1,1,0,1,0
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #24 */
    INSTANCE.v_tiles[24] = new Tile(0,0,0,1,1,0
    ,9,18,8208,8208L,8208L,8208L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #25 */
    INSTANCE.v_tiles[25] = new Tile(1,0,0,1,1,0
    ,9,18,8208,8208L,8208L,8208L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #26 */
    INSTANCE.v_tiles[26] = new Tile(0,1,0,1,1,0
    ,9,18,16,16L,16L,16L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #27 */
    INSTANCE.v_tiles[27] = new Tile(1,1,0,1,1,0
    ,9,18,16,16L,16L,16L,16383L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #28 */
    INSTANCE.v_tiles[28] = new Tile(0,0,1,1,1,0
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #29 */
    INSTANCE.v_tiles[29] = new Tile(1,0,1,1,1,0
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #30 */
    INSTANCE.v_tiles[30] = new Tile(0,1,1,1,1,0
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #31 */
    INSTANCE.v_tiles[31] = new Tile(1,1,1,1,1,0
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #32 */
    INSTANCE.v_tiles[32] = new Tile(0,0,0,0,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #33 */
    INSTANCE.v_tiles[33] = new Tile(1,0,0,0,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #34 */
    INSTANCE.v_tiles[34] = new Tile(0,1,0,0,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #35 */
    INSTANCE.v_tiles[35] = new Tile(1,1,0,0,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #36 */
    INSTANCE.v_tiles[36] = new Tile(0,0,1,0,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #37 */
    INSTANCE.v_tiles[37] = new Tile(1,0,1,0,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #38 */
    INSTANCE.v_tiles[38] = new Tile(0,1,1,0,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #39 */
    INSTANCE.v_tiles[39] = new Tile(1,1,1,0,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #40 */
    INSTANCE.v_tiles[40] = new Tile(0,0,0,1,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #41 */
    INSTANCE.v_tiles[41] = new Tile(1,0,0,1,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #42 */
    INSTANCE.v_tiles[42] = new Tile(0,1,0,1,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #43 */
    INSTANCE.v_tiles[43] = new Tile(1,1,0,1,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #44 */
    INSTANCE.v_tiles[44] = new Tile(0,0,1,1,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #45 */
    INSTANCE.v_tiles[45] = new Tile(1,0,1,1,0,1
    ,9,18,8208,8208L,8208L,8208L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #46 */
    INSTANCE.v_tiles[46] = new Tile(0,1,1,1,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #47 */
    INSTANCE.v_tiles[47] = new Tile(1,1,1,1,0,1
    ,9,18,16,16L,16L,16L,253983L,8208L,8208L,8208L,8208L);
    /* Build v_tiles #48 */
    INSTANCE.v_tiles[48] = new Tile(0,0,0,0,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #49 */
    INSTANCE.v_tiles[49] = new Tile(1,0,0,0,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #50 */
    INSTANCE.v_tiles[50] = new Tile(0,1,0,0,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #51 */
    INSTANCE.v_tiles[51] = new Tile(1,1,0,0,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #52 */
    INSTANCE.v_tiles[52] = new Tile(0,0,1,0,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #53 */
    INSTANCE.v_tiles[53] = new Tile(1,0,1,0,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #54 */
    INSTANCE.v_tiles[54] = new Tile(0,1,1,0,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #55 */
    INSTANCE.v_tiles[55] = new Tile(1,1,1,0,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #56 */
    INSTANCE.v_tiles[56] = new Tile(0,0,0,1,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #57 */
    INSTANCE.v_tiles[57] = new Tile(1,0,0,1,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #58 */
    INSTANCE.v_tiles[58] = new Tile(0,1,0,1,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #59 */
    INSTANCE.v_tiles[59] = new Tile(1,1,0,1,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #60 */
    INSTANCE.v_tiles[60] = new Tile(0,0,1,1,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #61 */
    INSTANCE.v_tiles[61] = new Tile(1,0,1,1,1,1
    ,9,18,8208,8208L,8208L,8208L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #62 */
    INSTANCE.v_tiles[62] = new Tile(0,1,1,1,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
    /* Build v_tiles #63 */
    INSTANCE.v_tiles[63] = new Tile(1,1,1,1,1,1
    ,9,18,16,16L,16L,16L,262143L,8192L,8192L,8192L,8192L);
  }

}