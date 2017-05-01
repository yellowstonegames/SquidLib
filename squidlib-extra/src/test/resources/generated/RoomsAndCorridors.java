/* File generated automatically by TilesetsGenerator.java. Do not edit. This file is committed for convenience. */
package squidpony.tileset;

import squidpony.squidgrid.mapping.styled.*;

/** @author TilesetsGenerator.java */
public class RoomsAndCorridors {

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
    INSTANCE.config.num_colors[3]=1;
    INSTANCE.max_tiles.h=32;
    INSTANCE.max_tiles.v=16;
    INSTANCE.h_tiles = new Tile[32];
    /* Build h_tiles #0 */
    INSTANCE.h_tiles[0] = new Tile(0,0,0,0,0,0
    ,18,9,504,504L,504L,511L,511L,511L,0L,0L,254L,254L,254L,254L,511L,511L,511L,254L,254L,12L);
    /* Build h_tiles #1 */
    INSTANCE.h_tiles[1] = new Tile(1,0,0,0,0,0
    ,18,9,448,511L,511L,460L,460L,460L,12L,12L,12L,12L,12L,12L,511L,511L,511L,12L,12L,12L);
    /* Build h_tiles #2 */
    INSTANCE.h_tiles[2] = new Tile(0,1,0,0,0,0
    ,18,9,504,504L,504L,504L,511L,511L,15L,12L,12L,126L,127L,127L,511L,510L,510L,126L,12L,12L);
    /* Build h_tiles #3 */
    INSTANCE.h_tiles[3] = new Tile(1,1,0,0,0,0
    ,18,9,448,508L,508L,460L,460L,463L,15L,0L,0L,124L,127L,127L,511L,508L,508L,124L,124L,12L);
    /* Build h_tiles #4 */
    INSTANCE.h_tiles[4] = new Tile(0,0,0,1,0,0
    ,18,9,28,28L,510L,511L,127L,127L,126L,126L,126L,126L,0L,0L,511L,511L,511L,12L,12L,12L);
    /* Build h_tiles #5 */
    INSTANCE.h_tiles[5] = new Tile(1,0,0,1,0,0
    ,18,9,96,103L,511L,510L,0L,0L,508L,508L,508L,508L,508L,12L,511L,511L,511L,12L,12L,12L);
    /* Build h_tiles #6 */
    INSTANCE.h_tiles[6] = new Tile(0,1,0,1,0,0
    ,18,9,28,28L,508L,508L,31L,31L,31L,252L,252L,252L,255L,255L,511L,508L,508L,12L,12L,12L);
    /* Build h_tiles #7 */
    INSTANCE.h_tiles[7] = new Tile(1,1,0,1,0,0
    ,18,9,96,496L,496L,499L,499L,511L,511L,0L,0L,0L,63L,127L,511L,480L,496L,56L,28L,12L);
    /* Build h_tiles #8 */
    INSTANCE.h_tiles[8] = new Tile(0,0,0,0,1,0
    ,18,9,504,504L,504L,511L,511L,511L,0L,30L,30L,12L,508L,508L,15L,15L,15L,12L,12L,12L);
    /* Build h_tiles #9 */
    INSTANCE.h_tiles[9] = new Tile(1,0,0,0,1,0
    ,18,9,448,511L,511L,448L,0L,254L,254L,254L,254L,254L,510L,510L,255L,255L,255L,254L,12L,12L);
    /* Build h_tiles #10 */
    INSTANCE.h_tiles[10] = new Tile(0,1,0,0,1,0
    ,18,9,504,504L,504L,504L,63L,63L,63L,0L,0L,0L,511L,511L,15L,12L,12L,12L,12L,12L);
    /* Build h_tiles #11 */
    INSTANCE.h_tiles[11] = new Tile(1,1,0,0,1,0
    ,18,9,448,448L,510L,510L,62L,63L,63L,62L,62L,0L,511L,511L,15L,12L,12L,12L,12L,12L);
    /* Build h_tiles #12 */
    INSTANCE.h_tiles[12] = new Tile(0,0,0,1,1,0
    ,18,9,28,252L,252L,255L,511L,511L,508L,0L,126L,126L,510L,510L,127L,127L,15L,12L,12L,12L);
    /* Build h_tiles #13 */
    INSTANCE.h_tiles[13] = new Tile(1,0,0,1,1,0
    ,18,9,96,511L,511L,508L,508L,508L,508L,0L,63L,63L,511L,511L,63L,63L,63L,12L,12L,12L);
    /* Build h_tiles #14 */
    INSTANCE.h_tiles[14] = new Tile(0,1,0,1,1,0
    ,18,9,28,28L,28L,28L,511L,511L,511L,255L,255L,255L,511L,511L,15L,12L,60L,60L,12L,12L);
    /* Build h_tiles #15 */
    INSTANCE.h_tiles[15] = new Tile(1,1,0,1,1,0
    ,18,9,96,126L,126L,126L,510L,511L,511L,0L,0L,252L,511L,511L,255L,252L,252L,252L,252L,12L);
    /* Build h_tiles #16 */
    INSTANCE.h_tiles[16] = new Tile(0,0,0,0,0,1
    ,18,9,504,504L,504L,511L,511L,511L,0L,0L,252L,252L,252L,252L,255L,511L,511L,508L,56L,56L);
    /* Build h_tiles #17 */
    INSTANCE.h_tiles[17] = new Tile(1,0,0,0,0,1
    ,18,9,448,511L,511L,448L,448L,479L,31L,31L,31L,31L,31L,24L,31L,511L,511L,504L,56L,56L);
    /* Build h_tiles #18 */
    INSTANCE.h_tiles[18] = new Tile(0,1,0,0,0,1
    ,18,9,504,504L,504L,504L,511L,511L,63L,0L,0L,0L,63L,63L,63L,504L,504L,504L,56L,56L);
    /* Build h_tiles #19 */
    INSTANCE.h_tiles[19] = new Tile(1,1,0,0,0,1
    ,18,9,510,510L,454L,454L,454L,511L,255L,0L,0L,254L,255L,255L,255L,510L,510L,510L,254L,56L);
    /* Build h_tiles #20 */
    INSTANCE.h_tiles[20] = new Tile(0,0,0,1,0,1
    ,18,9,28,28L,508L,511L,255L,255L,254L,254L,254L,254L,254L,254L,255L,511L,511L,504L,56L,56L);
    /* Build h_tiles #21 */
    INSTANCE.h_tiles[21] = new Tile(1,0,0,1,0,1
    ,18,9,96,127L,511L,480L,0L,0L,504L,504L,504L,504L,504L,56L,63L,511L,511L,504L,56L,56L);
    /* Build h_tiles #22 */
    INSTANCE.h_tiles[22] = new Tile(0,1,0,1,0,1
    ,18,9,28,28L,508L,508L,31L,255L,255L,192L,448L,448L,15L,31L,63L,504L,504L,504L,56L,56L);
    /* Build h_tiles #23 */
    INSTANCE.h_tiles[23] = new Tile(1,1,0,1,0,1
    ,18,9,96,254L,510L,510L,254L,255L,255L,254L,56L,56L,63L,63L,63L,504L,504L,504L,56L,56L);
    /* Build h_tiles #24 */
    INSTANCE.h_tiles[24] = new Tile(0,0,0,0,1,1
    ,18,9,504,504L,504L,511L,511L,511L,0L,0L,0L,126L,126L,126L,127L,127L,511L,510L,126L,126L);
    /* Build h_tiles #25 */
    INSTANCE.h_tiles[25] = new Tile(1,0,0,0,1,1
    ,18,9,448,511L,511L,448L,254L,254L,254L,254L,254L,254L,254L,6L,63L,63L,511L,504L,56L,56L);
    /* Build h_tiles #26 */
    INSTANCE.h_tiles[26] = new Tile(0,1,0,0,1,1
    ,18,9,504,504L,504L,504L,63L,63L,63L,0L,248L,248L,255L,255L,255L,248L,504L,504L,56L,56L);
    /* Build h_tiles #27 */
    INSTANCE.h_tiles[27] = new Tile(1,1,0,0,1,1
    ,18,9,448,448L,448L,480L,112L,63L,31L,0L,0L,127L,127L,127L,127L,127L,511L,511L,127L,56L);
    /* Build h_tiles #28 */
    INSTANCE.h_tiles[28] = new Tile(0,0,0,1,1,1
    ,18,9,28,31L,31L,31L,511L,511L,511L,31L,31L,0L,0L,0L,63L,63L,511L,504L,56L,56L);
    /* Build h_tiles #29 */
    INSTANCE.h_tiles[29] = new Tile(1,0,0,1,1,1
    ,18,9,96,255L,255L,192L,478L,478L,478L,30L,254L,254L,254L,254L,255L,255L,511L,510L,56L,56L);
    /* Build h_tiles #30 */
    INSTANCE.h_tiles[30] = new Tile(0,1,0,1,1,1
    ,18,9,28,28L,28L,28L,511L,511L,511L,0L,252L,252L,255L,255L,255L,252L,508L,508L,56L,56L);
    /* Build h_tiles #31 */
    INSTANCE.h_tiles[31] = new Tile(1,1,0,1,1,1
    ,18,9,96,254L,254L,254L,510L,511L,511L,254L,0L,0L,63L,63L,63L,56L,504L,504L,56L,56L);
    INSTANCE.v_tiles = new Tile[16];
    /* Build v_tiles #0 */
    INSTANCE.v_tiles[0] = new Tile(0,0,0,0,0,0
    ,9,18,6175,6175L,6559L,260511L,260511L,260511L,16376L,16376L,16376L);
    /* Build v_tiles #1 */
    INSTANCE.v_tiles[1] = new Tile(1,0,0,0,0,0
    ,9,18,6156,6156L,8191L,262143L,262136L,262136L,16376L,16376L,16376L);
    /* Build v_tiles #2 */
    INSTANCE.v_tiles[2] = new Tile(0,0,1,0,0,0
    ,9,18,28703,261151L,261151L,261151L,12319L,12319L,16376L,16376L,16376L);
    /* Build v_tiles #3 */
    INSTANCE.v_tiles[3] = new Tile(1,0,1,0,0,0
    ,9,18,28684,262140L,262143L,262143L,16376L,16376L,16382L,16382L,16376L);
    /* Build v_tiles #4 */
    INSTANCE.v_tiles[4] = new Tile(0,0,0,1,0,0
    ,9,18,6175,6175L,6175L,260127L,260152L,260208L,16352L,16320L,16320L);
    /* Build v_tiles #5 */
    INSTANCE.v_tiles[5] = new Tile(1,0,0,1,0,0
    ,9,18,6156,6156L,16332L,262092L,262143L,262143L,16383L,16320L,16320L);
    /* Build v_tiles #6 */
    INSTANCE.v_tiles[6] = new Tile(0,0,1,1,0,0
    ,9,18,28703,262143L,262143L,262143L,16332L,16332L,65472L,65472L,16320L);
    /* Build v_tiles #7 */
    INSTANCE.v_tiles[7] = new Tile(1,0,1,1,0,0
    ,9,18,28684,258060L,262140L,262140L,16383L,16383L,16383L,16320L,16320L);
    /* Build v_tiles #8 */
    INSTANCE.v_tiles[8] = new Tile(0,0,0,0,1,0
    ,9,18,6175,6175L,6175L,260607L,260607L,260127L,6172L,6172L,6172L);
    /* Build v_tiles #9 */
    INSTANCE.v_tiles[9] = new Tile(1,0,0,0,1,0
    ,9,18,6156,6652L,6655L,260607L,260604L,260604L,6652L,6172L,6172L);
    /* Build v_tiles #10 */
    INSTANCE.v_tiles[10] = new Tile(0,0,1,0,1,0
    ,9,18,28703,262111L,262111L,262143L,7167L,7135L,7132L,7132L,6172L);
    /* Build v_tiles #11 */
    INSTANCE.v_tiles[11] = new Tile(1,0,1,0,1,0
    ,9,18,28684,262028L,262047L,262047L,130972L,130972L,130972L,6172L,6172L);
    /* Build v_tiles #12 */
    INSTANCE.v_tiles[12] = new Tile(0,0,0,1,1,0
    ,9,18,6175,65311L,65311L,261919L,261912L,261912L,65304L,6200L,6256L);
    /* Build v_tiles #13 */
    INSTANCE.v_tiles[13] = new Tile(1,0,0,1,1,0
    ,9,18,6156,6652L,6652L,260604L,260607L,260607L,6655L,6652L,6240L);
    /* Build v_tiles #14 */
    INSTANCE.v_tiles[14] = new Tile(0,0,1,1,1,0
    ,9,18,28703,260127L,260223L,260223L,6240L,6240L,6240L,6240L,6240L);
    /* Build v_tiles #15 */
    INSTANCE.v_tiles[15] = new Tile(1,0,1,1,1,0
    ,9,18,28684,261644L,261900L,262028L,130047L,129535L,129279L,129120L,6240L);
  }

}