package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 4/2/2015.
 */
public class StyledDungeonTest {

    private static final int width = 60, height = 60;
    public static void main( String[] args )
    {
        DungeonGenerator dg = new DungeonGenerator(width, height, new RNG(0x1337DEADBEEFL));
        String[] names = {
                "    /**\n" +
                        "     * A generally useful kind of dungeon for ruins or underground manufactured areas.",
                        "     */\n" +
                        "    DEFAULT_DUNGEON,",
                        "    /**\n" +
                        "     * A good general kind of cave, with long distances between merging paths.",
                        "     */\n" +
                        "    CAVES_LIMIT_CONNECTIVITY,",
                        "    /**\n" +
                        "     * Only usable if using Chebyshev distances; many connections are diagonal-only.",
                        "     */\n" +
                        "    CAVES_TINY_CORRIDORS,",
                        "    /**\n" +
                        "     * Most parts of the cave are large and open, but tiny corridors link them, providing hiding places.",
                        "     */\n" +
                        "    CORNER_CAVES,",
                        "    /**\n" +
                        "     * Very basic demo dungeon.",
                        "     */\n" +
                        "    HORIZONTAL_CORRIDORS_A,",
                        "    /**\n" +
                        "     * Slightly less basic demo dungeon.",
                        "     */\n" +
                        "    HORIZONTAL_CORRIDORS_B,",
                        "    /**\n" +
                        "     * A bit more complexity in this demo dungeon.",
                        "     */\n" +
                        "    HORIZONTAL_CORRIDORS_C,",
                        "    /**\n" +
                        "     * A reference implementation of where you can place walls; mostly floor.",
                        "     */\n" +
                        "    LIMIT_CONNECTIVITY_FAT,",
                        "    /**\n" +
                        "     * A reference implementation of where you can place walls; mostly wall.",
                        "     */\n" +
                        "    LIMITED_CONNECTIVITY,",
                        "    /**\n" +
                        "     * A generally good maze; MAZE_A and MAZE_B should both be interchangeable, but not on the same map.",
                        "     */\n" +
                        "    MAZE_A,",
                        "    /**\n" +
                        "     * A generally good maze; MAZE_A and MAZE_B should both be interchangeable, but not on the same map.",
                        "     */\n" +
                        "    MAZE_B,",
                        "    /**\n" +
                        "     * A map that's less dungeon-like than the others, with lots of open space.",
                        "     */\n" +
                        "    OPEN_AREAS,",
                        "    /**\n" +
                        "     * An excellent natural cave style that looks worn down haphazardly, as by burrowing creatures or deep rivers.",
                        "     */\n" +
                        "    REFERENCE_CAVES,",
                        "    /**\n" +
                        "     * Mostly open, kinda weird.",
                        "     */\n" +
                        "    ROOMS_AND_CORRIDORS_A,",
                        "    /**\n" +
                        "     * Mostly open, but with long corridors that should be a good fit for ranged combat.",
                        "     */\n" +
                        "    ROOMS_AND_CORRIDORS_B,",
                        "    /**\n" +
                        "     * A nice old-school roguelike map, with thin corridors and rectangular rooms.",
                        "     */\n" +
                        "    ROOMS_LIMIT_CONNECTIVITY,",
                        "    /**\n" +
                        "     * A thing of beauty. Good for maps that need to seem otherworldly or unusual, like heavenly planes.",
                        "     */\n" +
                        "    ROUND_ROOMS_DIAGONAL_CORRIDORS,",
                        "    /**\n" +
                        "     * A more open cave, but portions of this may seem artificial. Consider alternating with REFERENCE_CAVES .",
                        "     */\n" +
                        "    SIMPLE_CAVES,",
                        "    /**\n" +
                        "     * Kinda... not the best map. Very predictable.",
                        "     */\n" +
                        "    SQUARE_ROOMS_WITH_RANDOM_RECTS;\n"
        };
        TilesetType[] tts = TilesetType.values();
        char[][] trans = new char[height][width], dungeon;
        for(int i = 0; i < tts.length; i++)
        {
            TilesetType tt = tts[i];
            System.out.println(names[i * 2]);
            System.out.println("     * <br>\n     * Example:\n     * <br>\n     * <pre>");
            dungeon = dg.generate(tt);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    trans[y][x] = dungeon[x][y];
                }
            }
            for (int row = 0; row < height; row++) {
                System.out.print("     * ");
                System.out.println(trans[row]);
            }
            System.out.println("     * </pre>");
            System.out.println(names[i * 2 + 1]);
            //System.out.println(dg);
            //System.out.println();
        }
    }
}
