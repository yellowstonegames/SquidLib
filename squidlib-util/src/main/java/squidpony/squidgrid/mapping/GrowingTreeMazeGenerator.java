package squidpony.squidgrid.mapping;


import squidpony.ArrayTools;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.OrderedSet;

/**
 * A maze generator that can be configured using a {@link ChoosingMethod}, which can be customized for the app.
 * Based in part on code from <a href="http://weblog.jamisbuck.org/2011/1/27/maze-generation-growing-tree-algorithm">Jamis Buck's blog</a>.
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class GrowingTreeMazeGenerator implements IDungeonGenerator {

    private IRNG rng;
    private int width, height;
    public char[][] dungeon;
    
    public GrowingTreeMazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        rng = new GWTRNG();
    }
    public GrowingTreeMazeGenerator(int width, int height, IRNG rng) {
        this.width = width;
        this.height = height;
        this.rng = rng;
    }

    /**
     * Gets the most recently-produced dungeon as a 2D char array, usually produced by calling {@link #generate()} or
     * some similar method present in a specific implementation. This normally passes a direct reference and not a copy,
     * so you can normally modify the returned array to propagate changes back into this IDungeonGenerator.
     *
     * @return the most recently-produced dungeon/map as a 2D char array
     */
    @Override
    public char[][] getDungeon() {
        return dungeon;
    }

    /**
     * Builds and returns a 2D char array maze by using {@link #newest} with {@link #generate(ChoosingMethod)}.
     * 
     * @return {@link #dungeon}, after filling it with a maze
     */
    @Override
    public char[][] generate() {
        return generate(newest);
    }

    /**
     * Builds and returns a 2D char array maze using the provided chooser method object. The most maze-like dungeons
     * use {@link #newest}, the least maze-like use {@link #oldest}, and the most jumbled use {@link #random} or a
     * mix of others using {@link #mix(ChoosingMethod, double, ChoosingMethod, double)}.
     * 
     * @param choosing the callback object for making the split decision
     * @return {@link #dungeon}, after filling it with a maze
     */
    public char[][] generate(ChoosingMethod choosing) {
        if(dungeon == null || dungeon.length != width || dungeon[0].length != height)
            dungeon = ArrayTools.fill('#', width, height);
        else 
            ArrayTools.fill(dungeon, '#');
        
        int x = rng.nextInt(width - 1) | 1;
        int y = rng.nextInt(height - 1) | 1;

        OrderedSet<Coord> deck = new OrderedSet<>();
        deck.add(Coord.get(x, y));

        Direction[] dirs = new Direction[4];
        System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
        OUTER:
        while (!deck.isEmpty()) {
            int i = choosing.chooseIndex(deck.size());
            Coord p = deck.getAt(i);
            rng.shuffleInPlace(dirs);

            for (Direction dir : dirs) {
                x = p.x + dir.deltaX * 2;
                y = p.y + dir.deltaY * 2;
                if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
                    if (dungeon[x][y] == '#' && deck.add(Coord.get(x, y))) {
                        dungeon[x][y] = '.';
                        dungeon[p.x + dir.deltaX][p.y + dir.deltaY] = '.';
                        continue OUTER;
                    }
                }
            }
            
            deck.remove(p);
        }

        return dungeon;
    }

    /**
     * A way to configure how {@link #generate(ChoosingMethod)} places paths through the maze. Most often, you'll want
     * to at least start with {@link #newest}, since it generates the most maze-like dungeons, and try the other
     * predefined ChoosingMethods eventually. You can use {@link #mix(ChoosingMethod, double, ChoosingMethod, double)}
     * to mix two ChoosingMethods, which can be a good way to add a little variety to a maze.
     */
    public interface ChoosingMethod {

        /**
         * Given the size to choose from, will return a single value smaller than the passed in value and greater than
         * or equal to 0. The value chosen is dependent on the individual implementation.
         *
         * @param size the exclusive upper bound for results; always at least 1
         * @return an int between 0 (inclusive) and {@code size} (exclusive)
         */
        int chooseIndex(int size);
    }

    /**
     * Produces high-quality mazes that are very similar to those produced by a recursive back-tracking algorithm.
     */
    public final ChoosingMethod newest = new ChoosingMethod() {
        @Override
        public int chooseIndex(int size) {
            return size - 1;
        }
    };
    /**
     * Produces mostly straight corridors that dead-end at the map's edge; probably only useful with {@link #mix(ChoosingMethod, double, ChoosingMethod, double)}.
     */
    public final ChoosingMethod oldest = new ChoosingMethod() {
        @Override
        public int chooseIndex(int size) {
            return 0;
        }
    };
    /**
     * Produces chaotic, jumbled spans of corridors that are similar to those produced by Prim's algorithm.
     */
    public final ChoosingMethod random = new ChoosingMethod() {
        @Override
        public int chooseIndex(int size) {
            return rng.nextSignedInt(size);
        }
    };

    /**
     * Mixes two ChoosingMethod values, like {@link #newest} and {@link #random}, given a weight for each, and produces
     * a new ChoosingMethod that randomly (respecting weight) picks one of those ChoosingMethods each time it is used.
     * @param methodA the first ChoosingMethod to mix; must not be null
     * @param chanceA the weight to favor choosing methodA
     * @param methodB the second ChoosingMethod to mix; must not be null
     * @param chanceB the weight to favor choosing methodB
     * @return a ChoosingMethod that randomly picks between {@code methodA} and {@code methodB} each time it is used
     */
    public ChoosingMethod mix(final ChoosingMethod methodA, final double chanceA,
                              final ChoosingMethod methodB, final double chanceB) {
        final double a = Math.max(0.0, chanceA);
        final double sum = a + Math.max(0.0, chanceB);
        if(sum <= 0.0) return random;
        return new ChoosingMethod() {
            @Override
            public int chooseIndex(int size) {
                return rng.nextDouble(sum) < a ? methodA.chooseIndex(size) : methodB.chooseIndex(size);
            }
        };
    }
}
