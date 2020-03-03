package squidpony.squidgrid.mapping;

import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;

public class BasicCaveGenerator implements IDungeonGenerator {
    public int width;
    public int height;
    public IRNG random;
    public char[][] dungeon;
    public BasicCaveGenerator(){
        this(80, 40);
    }
    public BasicCaveGenerator(int width, int height) {
        this(width, height, new GWTRNG());
    }
    public BasicCaveGenerator(int width, int height, IRNG random) {
        this.width = width;
        this.height = height;
        this.random = random;
    }

    /**
     * Generates a dungeon or other map as a 2D char array. Any implementation may allow its own configuration and
     * customization of how dungeons are generated, but each must provide this as a sane default. Most implementations
     * should use the convention of '#' representing a wall and '.' representing a bare floor, but beyond that, anything
     * could be present in the char array.
     *
     * @return a 2D char array representing some kind of map, probably using standard conventions for walls/floors
     */
    @Override
    public char[][] generate() {
        if (dungeon == null || dungeon.length != width || dungeon.length <= 0 || dungeon[0].length != height || dungeon[0].length <= 0)
            dungeon = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random.nextDouble() < 0.5)
                    dungeon[x][y] = '#';
                else
                    dungeon[x][y] = '.';
            }
        }
        char[][] working = new char[width][height];
        for (int iter = 0; iter < 5; iter++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int sum = 0;
                    for (int innerX = -1; innerX <= 1; innerX++) {
                        for (int innerY = -1; innerY <= 1; innerY++) {
                            if (x + innerX >= 0 && x + innerX < width
                                    && y + innerY >= 0 && y + innerY < height
                                    && dungeon[x + innerX][y + innerY] == '.')
                                sum++;
                        }
                    }
                    if (sum < 5)
                        working[x][y] = '#';
                    else
                        working[x][y] = '.';
                }
            }
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    dungeon[x][y] = working[x][y];
                }
            }
        }
        return dungeon;
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
}
