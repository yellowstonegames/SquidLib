package squidpony.squidgrid.mapping;

import squidpony.GwtCompatibility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 8/7/2016.
 */
public class ThinDungeonGenerator extends SectionDungeonGenerator {
    /**
     * Make a DungeonGenerator with a LightRNG using a random seed, height 40, and width 40.
     */
    public ThinDungeonGenerator() {
    }

    /**
     * Make a DungeonGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed.
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public ThinDungeonGenerator(int width, int height) {
        super(width, height);
    }

    /**
     * Make a DungeonGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng    The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     */
    public ThinDungeonGenerator(int width, int height, RNG rng) {
        super(width, height, rng);
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     *
     * @param copying the DungeonGenerator to copy
     */
    public ThinDungeonGenerator(ThinDungeonGenerator copying) {
        super(copying);
    }

    public char[][] makeThin()
    {
        int nw = (width << 1) - 1, nh = (height << 1) - 1;
        char[][] d2 = new char[nw][nh];
        int[][] e2 = new int[nw][nh];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                d2[x*2][y*2] = dungeon[x][y];
                e2[x*2][y*2] = finder.environment[x][y];
            }
        }
        int eLow, eHigh;
        char dLow, dHigh;
        for (int y = 0; y < nh; y += 2) {
            for (int x = 1; x < nw; x+=2) {
                eLow = e2[x - 1][y];
                eHigh = e2[x + 1][y];
                dLow = d2[x - 1][y];
                dHigh = d2[x + 1][y];
                if(dLow == '+' || dLow == '/')
                {
                    d2[x][y] = dHigh;
                    e2[x][y] = eHigh;
                    continue;
                }
                else if(dHigh == '+' || dHigh == '/')
                {
                    d2[x][y] = dLow;
                    e2[x][y] = eLow;
                    continue;
                }

                switch (eLow) {
                    case MixedGenerator.CAVE_WALL:
                    case MixedGenerator.CORRIDOR_WALL:
                    case MixedGenerator.ROOM_WALL:
                    case MixedGenerator.UNTOUCHED:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                e2[x][y] = MixedGenerator.UNTOUCHED;
                                d2[x][y] = dLow;
                                break;
                            case MixedGenerator.CAVE_FLOOR:
                                if (PerlinNoise.noise(x * 0.3, y * 0.3) > -0.2) {
                                    e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                    d2[x][y] = dHigh;
                                } else {
                                    e2[x][y] = MixedGenerator.UNTOUCHED;
                                    d2[x][y] = dLow;
                                }
                                break;
                            default:
                                e2[x][y] = MixedGenerator.UNTOUCHED;
                                d2[x][y] = dLow;
                                break;
                        }
                        break;
                    case MixedGenerator.CAVE_FLOOR:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                if (PerlinNoise.noise(x * 0.3, y * 0.3) > -0.2) {
                                    e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                    d2[x][y] = dLow;
                                } else {
                                    e2[x][y] = MixedGenerator.UNTOUCHED;
                                    d2[x][y] = dHigh;
                                }
                                break;
                            default:
                                e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                d2[x][y] = dLow;
                        }
                        break;
                    default:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                e2[x][y] = MixedGenerator.UNTOUCHED;
                                d2[x][y] = dHigh;
                                break;
                            case MixedGenerator.CAVE_FLOOR:
                                e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                d2[x][y] = dHigh;
                                break;
                            default:
                                e2[x][y] = eLow;
                                d2[x][y] = dLow;
                        }
                }
            }
        }
        for (int x = 0; x < nw; x++) {
            for (int y = 1; y < nh; y += 2) {
                eLow = e2[x][y - 1];
                eHigh = e2[x][y + 1];
                dLow = d2[x][y-1];
                dHigh = d2[x][y+1];
                if(dLow == '+' || dLow == '/')
                {
                    d2[x][y] = dHigh;
                    e2[x][y] = eHigh;
                    continue;
                }
                else if(dHigh == '+' || dHigh == '/')
                {
                    d2[x][y] = dLow;
                    e2[x][y] = eLow;
                    continue;
                }
                switch (eLow) {
                    case MixedGenerator.CAVE_WALL:
                    case MixedGenerator.CORRIDOR_WALL:
                    case MixedGenerator.ROOM_WALL:
                    case MixedGenerator.UNTOUCHED:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                e2[x][y] = MixedGenerator.UNTOUCHED;
                                d2[x][y] = dLow;
                                break;
                            case MixedGenerator.CAVE_FLOOR:
                                if (PerlinNoise.noise(x * 0.3, y * 0.3) > -0.2) {
                                    e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                    d2[x][y] = dHigh;
                                } else {
                                    e2[x][y] = MixedGenerator.UNTOUCHED;
                                    d2[x][y] = dLow;
                                }
                                break;
                            default:
                                e2[x][y] = MixedGenerator.UNTOUCHED;
                                d2[x][y] = dLow;
                                break;
                        }
                        break;
                    case MixedGenerator.CAVE_FLOOR:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                if (PerlinNoise.noise(x * 0.3, y * 0.3) > -0.2) {
                                    e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                    d2[x][y] = dLow;
                                } else {
                                    e2[x][y] = MixedGenerator.UNTOUCHED;
                                    d2[x][y] = dHigh;
                                }
                                break;
                            default:
                                e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                d2[x][y] = dLow;
                        }
                        break;
                    default:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                e2[x][y] = MixedGenerator.UNTOUCHED;
                                d2[x][y] = dHigh;
                                break;
                            case MixedGenerator.CAVE_FLOOR:
                                e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                d2[x][y] = dHigh;
                                break;
                            default:
                                e2[x][y] = eLow;
                                d2[x][y] = dLow;
                        }
                }
            }
        }
        dungeon = d2;
        width = nw;
        height = nh;
        if(stairsUp != null)
        {
            stairsUp = stairsUp.multiply(2);
        }
        if(stairsDown != null)
        {
            stairsDown = stairsDown.multiply(2);
        }
        finder = new RoomFinder(dungeon, e2);
        placement = new Placement(finder);
        return dungeon;
    }

    @Override
    protected char[][] innerGenerate() {
        super.innerGenerate();
        makeThin();
        return dungeon;
    }

}
