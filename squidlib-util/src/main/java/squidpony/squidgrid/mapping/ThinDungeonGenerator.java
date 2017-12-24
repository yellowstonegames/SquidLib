package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 8/7/2016.
 */
public class ThinDungeonGenerator extends SectionDungeonGenerator {
    public static final int
            ROOM_WALL_RETRACT = 1, ROOM_WALL_NORMAL = 2, ROOM_WALL_EXPAND = 4, ROOM_WALL_CHAOTIC = 8,
            CORRIDOR_WALL_RETRACT = 16, CORRIDOR_WALL_NORMAL = 32, CORRIDOR_WALL_EXPAND = 64, CORRIDOR_WALL_CHAOTIC = 128,
            CAVE_WALL_RETRACT = 256, CAVE_WALL_NORMAL = 512, CAVE_WALL_EXPAND = 1024, CAVE_WALL_CHAOTIC = 2048;
    public int wallShapes = ROOM_WALL_EXPAND | CORRIDOR_WALL_EXPAND | CAVE_WALL_CHAOTIC;
    /**
     * Make a DungeonGenerator with a LightRNG using a random seed, height 40, and width 40.
     */
    public ThinDungeonGenerator() {
        super();
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
     * Make a DungeonGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed. You can give the static fields of this class
     * as arguments to roomShape, corridorShape and caveShape. For clarity, the 3 fields should each be given
     * a constant corresponding to the same kind of area, e.g. ROOM_WALL_EXPAND or ROOM_WALL_RETRACT could be
     * given to roomShape but preferably would not be given to the others. If the combination of arguments is
     * invalid, such as if they are all 0, this uses the default shapes: expanded room and corridor walls, and
     * chaotic cave walls controlled by Perlin noise.
     * <br>
     * Though there's no constructor that takes a single int which merges roomShape, corridorShape, and
     * caveShape, the internal representation of those shapes is one int. If you have one of those ints, or you
     * made one yourself (such as with bitwise OR on three different constants), you can pass it to any of the
     * three shape arguments and 0 to the other two shapes; it will be processed in the same way.
     *
     * @param width         The width of the dungeon in cells
     * @param height        The height of the dungeon in cells
     * @param roomShape     expected to be an int constant: ROOM_WALL_EXPAND, ROOM_WALL_RETRACT, or ROOM_WALL_CHAOTIC
     * @param corridorShape expected to be an int constant: CORRIDOR_WALL_EXPAND, CORRIDOR_WALL_RETRACT, or CORRIDOR_WALL_CHAOTIC
     * @param caveShape     expected to be an int constant: CAVE_WALL_EXPAND, CAVE_WALL_RETRACT, or CAVE_WALL_CHAOTIC
     */
    public ThinDungeonGenerator(int width, int height, int roomShape, int corridorShape, int caveShape) {
        super(width, height);
        wallShapes = roomShape | corridorShape | caveShape;
        if ((wallShapes & 0xF) == 0 || (wallShapes & 0xF0) == 0 || (wallShapes & 0xF00) == 0
                || Integer.bitCount(wallShapes) != 3)
            wallShapes = ROOM_WALL_EXPAND | CORRIDOR_WALL_EXPAND | CAVE_WALL_CHAOTIC;
    }

    /**
     * Make a DungeonGenerator with the given height, width, and RNG for generating random features. You can
     * give the static fields of this class as arguments to roomShape, corridorShape and caveShape. For
     * clarity, the 3 fields should each be given a constant corresponding to the same kind of area, e.g.
     * ROOM_WALL_EXPAND or ROOM_WALL_RETRACT could be given to roomShape but preferably would not be given to
     * the others. If the combination of arguments is invalid, such as if they are all 0, this uses the default
     * shapes: expanded room and corridor walls, and chaotic cave walls controlled by Perlin noise.
     * <br>
     * Though there's no constructor that takes a single int which merges roomShape, corridorShape, and
     * caveShape, the internal representation of those shapes is one int. If you have one of those ints, or you
     * made one yourself (such as with bitwise OR on three different constants), you can pass it to any of the
     * three shape arguments and 0 to the other two shapes; it will be processed in the same way.
     *
     * @param width         The width of the dungeon in cells
     * @param height        The height of the dungeon in cells
     * @param roomShape     expected to be an int constant: ROOM_WALL_EXPAND, ROOM_WALL_RETRACT, or ROOM_WALL_CHAOTIC
     * @param corridorShape expected to be an int constant: CORRIDOR_WALL_EXPAND, CORRIDOR_WALL_RETRACT, or CORRIDOR_WALL_CHAOTIC
     * @param caveShape     expected to be an int constant: CAVE_WALL_EXPAND, CAVE_WALL_RETRACT, or CAVE_WALL_CHAOTIC
     * @param rng           The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     */
    public ThinDungeonGenerator(int width, int height, RNG rng, int roomShape, int corridorShape, int caveShape) {
        super(width, height, rng);
        wallShapes = roomShape | corridorShape | caveShape;
        if ((wallShapes & 0xF) == 0 || (wallShapes & 0xF0) == 0 || (wallShapes & 0xF00) == 0
                || Integer.bitCount(wallShapes) != 3)
            wallShapes = ROOM_WALL_EXPAND | CORRIDOR_WALL_EXPAND | CAVE_WALL_CHAOTIC;
    }


    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     *
     * @param copying the DungeonGenerator to copy
     */
    public ThinDungeonGenerator(ThinDungeonGenerator copying) {
        super(copying);
        wallShapes = copying.wallShapes;
    }

    /**
     * Doors are not supported by this class. There's a lot of confusing and ambiguous situations that can easily
     * result from trying to automatically place doors when any given map could need doors on walkable cells, prefer
     * doors between walkable cells aligned to walls, have mixes of "expanded", "retracted", and "normal" wall change
     * modes, or any number of other issues. There are also some very confusing cases where automatically-placed doors,
     * if this class tries to reposition them in retracted-wall mode (or in any case where rooms and corridors don't
     * have the same change mode), wind up hanging in the middle of a room or with partial connecting walls but not any
     * sensible layout. If you want doors here, you should assign them yourself how you see fit.
     * @param percentage ignored.
     * @param doubleDoors ignored.
     * @return this for chaining.
     */
    @Override
    public SectionDungeonGenerator addDoors(int percentage, boolean doubleDoors)
    {
        doorFX = 0;
        return this;
    }

    public char[][] makeThin() {

        int nw = (width << 1) - 1, nh = (height << 1) - 1;
        char[][] d2 = new char[nw][nh];
        int[][] e2 = new int[nw][nh];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                d2[x * 2][y * 2] = dungeon[x][y];
                e2[x * 2][y * 2] = finder.environment[x][y];
            }
        }
        int eLow, eHigh;
        char dLow, dHigh;
        int tempShapes;
        for (int y = 0; y < nh; y += 2) {
            CELL_WISE:
            for (int x = 1; x < nw - 1; x += 2) {
                eLow = e2[x - 1][y];
                eHigh = e2[x + 1][y];
                dLow = d2[x - 1][y];
                dHigh = d2[x + 1][y];
                tempShapes = wallShapes;
                switch (eLow) {
                    case MixedGenerator.CAVE_FLOOR:
                        tempShapes >>= 4;
                    case MixedGenerator.CORRIDOR_FLOOR:
                        tempShapes >>= 4;
                        break;
                    default:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_FLOOR:
                                tempShapes >>= 4;
                            case MixedGenerator.CORRIDOR_FLOOR:
                                tempShapes >>= 4;
                        }
                }
                switch (tempShapes & 0xf) {
//                    case ROOM_WALL_RETRACT:
                    case ROOM_WALL_NORMAL:
                        if (y > 1 && y < nh - 2) {
                            if ((dLow == '+') && (e2[x - 1][y - 2] & 1) + (e2[x - 1][y + 2] & 1) == 0) {
                                d2[x][y] = dHigh;
                                e2[x][y] = eHigh;
                                d2[x - 1][y - 1] = '#';
                                d2[x - 1][y + 1] = '#';
                                e2[x - 1][y - 1] = 777;
                                e2[x - 1][y + 1] = 777;
                                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                    d2[x][y] = 6;
                                continue CELL_WISE;
                            } /*else if ((dHigh == '+') && (e2[x+1][y - 2] & 1) + (e2[x+1][y - 2] & 1) == 0) {
                                d2[x][y] = dLow;
                                e2[x][y] = eLow;
                                d2[x+1][y-1] = '#';
                                d2[x+1][y+1] = '#';
                                e2[x+1][y-1] = MixedGenerator.UNTOUCHED;
                                e2[x+1][y+1] = MixedGenerator.UNTOUCHED;
                                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                    d2[x][y] = '\u0006';
                                continue CELL_WISE;
                            }*/
                        }
                        break;
//                    case ROOM_WALL_RETRACT:
//                        if (x > 2 && y > 1 && y < nh - 2) {
//                            if ((dLow == '+') && (e2[x-1][y + 2] & 1) + (e2[x-1][y - 2] & 1) == 0) {
//                                d2[x][y] = dLow;
//                                e2[x][y] = eLow;
//                                d2[x][y-1] = d2[x-1][y - 2];
//                                e2[x][y-1] = e2[x-1][y - 2];
//                                d2[x][y+1] = d2[x-1][y + 2];
//                                e2[x][y+1] = e2[x-1][y + 2];
//                                d2[x-1][y] = d2[x-3][y];
//                                e2[x-1][y] = e2[x-3][y];
//                                d2[x-1][y-1] = d2[x-3][y-1];
//                                d2[x-1][y+1] = d2[x-3][y+1];
//                                e2[x-1][y-1] = e2[x-3][y-1];
//                                e2[x-1][y+1] = e2[x-3][y+1];
//                                continue CELL_WISE;
//                            }
//                        }
//                        break;
                    case ROOM_WALL_RETRACT:
                    case ROOM_WALL_EXPAND:
                        if (y > 1 && y < nh - 2) {
                            if ((dLow == '+') && ((e2[x - 1][y - 2] & 1) + (e2[x + 1][y - 2] & 1) != 0 || (e2[x - 1][y + 2] & 1) + (e2[x + 1][y + 2] & 1) != 0)) {
                                d2[x][y] = dLow;
                                e2[x][y] = eLow;
                                d2[x - 1][y] = dHigh;
                                e2[x - 1][y] = eHigh;
                                continue CELL_WISE;
                            } else if ((dHigh == '+') && ((e2[x - 1][y - 2] & 1) + (e2[x + 1][y - 2] & 1) != 0 || (e2[x - 1][y + 2] & 1) + (e2[x + 1][y + 2] & 1) != 0)) {
                                d2[x][y] = dHigh;
                                e2[x][y] = eHigh;
                                d2[x + 1][y] = dLow;
                                e2[x + 1][y] = eLow;
                                continue CELL_WISE;
                            }
                        }
                        break;
                    case ROOM_WALL_CHAOTIC:
                        if (dLow == '+') {
                            d2[x - 1][y] = dHigh;
                            e2[x - 1][y] = eHigh;
                            if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                d2[x][y] = 6;
                            continue CELL_WISE;
                        } else if (dHigh == '+') {
                            d2[x + 1][y] = dLow;
                            e2[x + 1][y] = eLow;
                            if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                d2[x][y] = 6;
                            continue CELL_WISE;
                        }
                        break;
                }
                tempShapes = wallShapes;
                switch (eLow) {
                    case 777:
                        d2[x][y] = '#';
                        e2[x][y] = MixedGenerator.UNTOUCHED;
                        continue CELL_WISE;
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
                                tempShapes >>= 4;
                            case MixedGenerator.CORRIDOR_FLOOR:
                                tempShapes >>= 4;
                            default:
                                switch (tempShapes & 0xF) {
                                    case ROOM_WALL_CHAOTIC:
                                        if (PerlinNoise.noise(x * 6, y * 6) > -0.2) {
                                            e2[x][y] = eHigh;
                                            d2[x][y] = dHigh;
                                        } else {
                                            e2[x][y] = MixedGenerator.UNTOUCHED;
                                            d2[x][y] = dLow;
                                        }
                                        break;
                                    case ROOM_WALL_RETRACT:
                                    case ROOM_WALL_EXPAND:
                                        e2[x][y] = MixedGenerator.UNTOUCHED;
                                        d2[x][y] = dLow;
                                        break;

//                                    case ROOM_WALL_RETRACT:
//                                        e2[x][y] = eHigh;
//                                        d2[x][y] = dHigh;
//                                        if(x > 2 && (e2[x-3][y] & 1) != 0) { // wall is thin enough
//                                            e2[x - 1][y] = eHigh;
//                                            d2[x - 1][y] = dHigh;
//                                        }
//                                        break;
                                    default:
                                        e2[x][y] = eHigh;
                                        d2[x][y] = dHigh;
                                        break;
                                }
                                break;
                        }
                        break;
                    case MixedGenerator.CAVE_FLOOR:
                        tempShapes >>= 4;
                    case MixedGenerator.CORRIDOR_FLOOR:
                        tempShapes >>= 4;
                    default:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                switch (tempShapes & 0xF) {
                                    case ROOM_WALL_CHAOTIC:
                                        if (PerlinNoise.noise(x * 6, y * 6) > -0.2) {
                                            e2[x][y] = eLow;
                                            d2[x][y] = dLow;
                                        } else {
                                            e2[x][y] = MixedGenerator.UNTOUCHED;
                                            d2[x][y] = dHigh;
                                        }
                                        break;
                                    case ROOM_WALL_RETRACT:
                                    case ROOM_WALL_EXPAND:
                                        e2[x][y] = MixedGenerator.UNTOUCHED;
                                        d2[x][y] = dHigh;
                                        break;

//                                    case ROOM_WALL_RETRACT:
//                                        e2[x][y] = eLow;
//                                        d2[x][y] = dLow;
//                                        /*if(x < nh - 3 && (e2[x+3][y] & 1) != 0) { // wall is thin enough
//                                            e2[x + 1][y] = eLow;
//                                            d2[x + 1][y] = dLow;
//                                        }
//                                        */
//                                        break;
                                    default:
                                        e2[x][y] = eLow;
                                        d2[x][y] = dLow;
                                        break;
                                }
                                break;
                            case MixedGenerator.CAVE_FLOOR:
                                e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                d2[x][y] = dHigh;
                                break;
                            default:
                                e2[x][y] = eLow;
                                d2[x][y] = dLow;
                                break;
                        }
                        break;
                }
                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0) {
                    d2[x][y] = 6;
                }
            }
        }
        for (int x = 0; x < nw; x++) {
            CELL_WISE:
            for (int y = 1; y < nh - 1; y += 2) {
                eLow = e2[x][y - 1];
                eHigh = e2[x][y + 1];
                dLow = d2[x][y - 1];
                dHigh = d2[x][y + 1];
                tempShapes = wallShapes;
                switch (eLow) {
                    case MixedGenerator.CAVE_FLOOR:
                        tempShapes >>= 4;
                    case MixedGenerator.CORRIDOR_FLOOR:
                        tempShapes >>= 4;
                        break;
                    default:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_FLOOR:
                                tempShapes >>= 4;
                            case MixedGenerator.CORRIDOR_FLOOR:
                                tempShapes >>= 4;
                        }
                }
                switch (tempShapes & 0xf) {
                    case ROOM_WALL_NORMAL:
                        if (x > 0 && x < nw - 1) {
                            if (d2[x - 1][y - 1] == '+' || d2[x - 1][y + 1] == '+' || d2[x + 1][y - 1] == '+' || d2[x + 1][y + 1] == '+') {
                                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                    d2[x][y] = 6;
                                continue CELL_WISE;
                            } else if ((dLow == '/') && (e2[x - 2][y - 1] & 1) + (e2[x - 2][y + 1] & 1) == 0) {
                                d2[x][y] = dHigh;
                                e2[x][y] = eHigh;
                                d2[x - 1][y - 1] = '#';
                                d2[x + 1][y - 1] = '#';
                                e2[x - 1][y - 1] = MixedGenerator.UNTOUCHED;
                                e2[x + 1][y - 1] = MixedGenerator.UNTOUCHED;
                                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                    d2[x][y] = 6;
                                continue CELL_WISE;
                            } else if ((dHigh == '/') && (e2[x - 2][y + 1] & 1) + (e2[x + 2][y + 1] & 1) == 0) {
                                d2[x][y] = dLow;
                                e2[x][y] = eLow;
                                d2[x - 1][y + 1] = '#';
                                d2[x + 1][y + 1] = '#';
                                e2[x - 1][y + 1] = MixedGenerator.UNTOUCHED;
                                e2[x + 1][y + 1] = MixedGenerator.UNTOUCHED;
                                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                    d2[x][y] = 6;
                                continue CELL_WISE;
                            }
                        }
                        break;
//                    case ROOM_WALL_RETRACT:
//                        if (y > 2 && x > 1 && x < nw - 2) {
//                            if ((dLow == '/') && (e2[x-2][y-1] & 1) + (e2[x+2][y-1] & 1) == 0) {
//                                d2[x][y] = dLow;
//                                e2[x][y] = eLow;
//                                d2[x-1][y] = d2[x-2][y-1];
//                                e2[x-1][y] = e2[x-2][y-1];
//                                d2[x+1][y] = d2[x+2][y-1];
//                                e2[x+1][y] = e2[x+2][y-1];
//                                d2[x][y-1] = d2[x][y-3];
//                                e2[x][y-1] = e2[x][y-3];
//                                d2[x-1][y-1] = d2[x-1][y-3];
//                                d2[x+1][y-1] = d2[x+1][y-3];
//                                e2[x-1][y-1] = e2[x-1][y-3];
//                                e2[x+1][y-1] = e2[x+1][y-3];
//                                continue CELL_WISE;
//                            }
//                        }
//
//                        break;
                    case ROOM_WALL_RETRACT:
                    case ROOM_WALL_EXPAND:
                        if (x > 0 && x < nw - 1) {
                            if ((dLow == '/') && ((e2[x - 1][y - 1] & 1) + (e2[x - 1][y + 1] & 1) != 0 || (e2[x + 1][y - 1] & 1) + (e2[x + 1][y + 1] & 1) != 0)) {
                                d2[x][y] = dLow;
                                e2[x][y] = eLow;
                                d2[x][y - 1] = dHigh;
                                e2[x][y - 1] = eHigh;
                                continue CELL_WISE;
                            } else if ((dHigh == '/') && ((e2[x - 1][y - 1] & 1) + (e2[x - 1][y + 1] & 1) != 0 || (e2[x + 1][y - 1] & 1) + (e2[x + 1][y + 1] & 1) != 0)) {
                                d2[x][y] = dHigh;
                                e2[x][y] = eHigh;
                                d2[x][y + 1] = dLow;
                                e2[x][y + 1] = eLow;
                                continue CELL_WISE;
                            }
                        }
                        break;
                    case ROOM_WALL_CHAOTIC:
                        if (dLow == '/') {
                            d2[x][y - 1] = dHigh;
                            e2[x][y - 1] = eHigh;
                            if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                d2[x][y] = 6;
                            continue CELL_WISE;
                        } else if (dHigh == '/') {
                            d2[x][y + 1] = dLow;
                            e2[x][y + 1] = eLow;
                            if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0)
                                d2[x][y] = 6;
                            continue CELL_WISE;
                        }
                        break;
                }
                tempShapes = wallShapes;

                switch (eLow) {
                    case 777:
                        d2[x][y] = '#';
                        e2[x][y] = MixedGenerator.UNTOUCHED;
                        break;
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
                                tempShapes >>= 4;
                            case MixedGenerator.CORRIDOR_FLOOR:
                                tempShapes >>= 4;
                            default:
                                switch (tempShapes & 0xF) {
                                    case ROOM_WALL_CHAOTIC:
                                        if (PerlinNoise.noise(x * 6, y * 6) > -0.2) {
                                            e2[x][y] = eHigh;
                                            d2[x][y] = dHigh;
                                        } else {
                                            e2[x][y] = MixedGenerator.UNTOUCHED;
                                            d2[x][y] = dLow;
                                        }
                                        break;
                                    case ROOM_WALL_RETRACT:
                                    case ROOM_WALL_EXPAND:
                                        e2[x][y] = MixedGenerator.UNTOUCHED;
                                        d2[x][y] = dLow;
                                        break;

//                                    case ROOM_WALL_RETRACT:
//                                        e2[x][y] = eHigh;
//                                        d2[x][y] = dHigh;
//                                        if(y > 2 && (e2[x][y-3] & 1) != 0) { // wall is thin enough
//                                            e2[x][y - 1] = eHigh;
//                                            d2[x][y - 1] = dHigh;
//                                        }
//                                        break;
                                    default:
                                        e2[x][y] = eLow;
                                        d2[x][y] = dLow;
                                        break;
                                }
                                break;
                        }
                        break;
                    case MixedGenerator.CAVE_FLOOR:
                        tempShapes >>= 4;
                    case MixedGenerator.CORRIDOR_FLOOR:
                        tempShapes >>= 4;
                    default:
                        switch (eHigh) {
                            case MixedGenerator.CAVE_WALL:
                            case MixedGenerator.CORRIDOR_WALL:
                            case MixedGenerator.ROOM_WALL:
                            case MixedGenerator.UNTOUCHED:
                                switch (tempShapes & 0xF) {
                                    case ROOM_WALL_CHAOTIC:
                                        if (PerlinNoise.noise(x * 6, y * 6) > -0.2) {
                                            e2[x][y] = eLow;
                                            d2[x][y] = dLow;
                                        } else {
                                            e2[x][y] = MixedGenerator.UNTOUCHED;
                                            d2[x][y] = dHigh;
                                        }
                                        break;
                                    case ROOM_WALL_RETRACT:
                                    case ROOM_WALL_EXPAND:
                                        e2[x][y] = MixedGenerator.UNTOUCHED;
                                        d2[x][y] = dHigh;
                                        break;

//                                    case ROOM_WALL_RETRACT:
//                                        e2[x][y] = eLow;
//                                        d2[x][y] = dLow;
//                                         /*if(y > 2 && (e2[x][y-3] & 1) != 0) { // wall is thin enough
//                                            e2[x][y + 1] = eLow;
//                                            d2[x][y + 1] = dLow;
//                                        } */
//                                        break;
                                    default:
                                        e2[x][y] = eLow;
                                        d2[x][y] = dLow;
                                        break;
                                }
                                break;
                            case MixedGenerator.CAVE_FLOOR:
                                e2[x][y] = MixedGenerator.CAVE_FLOOR;
                                d2[x][y] = dHigh;
                                break;
                            default:
                                e2[x][y] = eHigh;
                                d2[x][y] = dHigh;
                                break;
                        }
                        break;
                }

                if (e2[x][y] != MixedGenerator.UNTOUCHED || d2[x][y] == 0) {
                    d2[x][y] = 6;
                }
            }
        }
        int currentEnv, offset;
        char currentDun;
        int[][] eArchive = ArrayTools.copy(e2);
        if ((wallShapes & 0x111) != 0) // any environments are retracting
        {
            for (int x = 2; x < nw - 2; x += 2) {
                for (int y = 0; y < nh - 2; y++) {
                    offset = (d2[x - 1][y] == '+') ? 1 : 2;
                    if ((eArchive[x][y] & 1) == 0 && ((currentEnv = eArchive[x - offset][y]) & 1) != 0) {
                        currentDun = d2[x - offset][y];
                        if (currentDun == '+' || currentDun == '/') {
                            switch (currentEnv) {
                                case MixedGenerator.ROOM_FLOOR:
                                    if ((wallShapes & ROOM_WALL_RETRACT) != 0) {
                                        e2[x + 2 - offset][y] = currentEnv;
                                        d2[x + 2 - offset][y] = currentDun;
                                        e2[x + 1 - offset][y] = eArchive[x - 1 - offset][y];
                                        d2[x + 1 - offset][y] = d2[x - 1 - offset][y];
                                        e2[x - offset][y] = eArchive[x - 1 - offset][y];
                                        d2[x - offset][y] = d2[x - 1 - offset][y];
                                    }
                                    break;
                                case MixedGenerator.CAVE_FLOOR:
                                    if ((wallShapes & CAVE_WALL_RETRACT) != 0) {

//                                    e2[x][y] = (currentDun == '+' || currentDun == '/') ? MixedGenerator.UNTOUCHED : currentEnv;
//                                    d2[x][y] = (currentDun == '+' || currentDun == '/') ? '\u0005' : currentDun;
//                                    e2[x-1][y] = (currentDun == '+' || currentDun == '/') ? MixedGenerator.UNTOUCHED : currentEnv;
//                                    d2[x-1][y] = (currentDun == '+' || currentDun == '/') ? '\u0005' : 6;
                                        e2[x + 2 - offset][y] = currentEnv;
                                        d2[x + 2 - offset][y] = currentDun;
                                        e2[x + 1 - offset][y] = eArchive[x - 1 - offset][y];
                                        d2[x + 1 - offset][y] = d2[x - 1 - offset][y];
                                        e2[x - offset][y] = eArchive[x - 1 - offset][y];
                                        d2[x - offset][y] = d2[x - 1 - offset][y];
                                    }
                                    break;
                                case MixedGenerator.CORRIDOR_FLOOR:
                                    if ((wallShapes & CORRIDOR_WALL_RETRACT) != 0) {
                                        e2[x + 2 - offset][y] = currentEnv;
                                        d2[x + 2 - offset][y] = currentDun;
                                        e2[x + 1 - offset][y] = eArchive[x - 1 - offset][y];
                                        d2[x + 1 - offset][y] = d2[x - 1 - offset][y];
                                        e2[x - offset][y] = eArchive[x - 1 - offset][y];
                                        d2[x - offset][y] = d2[x - 1 - offset][y];

                                    }
                                    break;
                            }
                        } else {
                            switch (currentEnv) {
                                case MixedGenerator.ROOM_FLOOR:
                                    if ((wallShapes & ROOM_WALL_RETRACT) != 0) {
                                        e2[x][y] = currentEnv;
                                        d2[x][y] = currentDun;
                                        e2[x - 1][y] = currentEnv;
                                        d2[x - 1][y] = 6;

                                    }
                                    break;
                                case MixedGenerator.CAVE_FLOOR:
                                    if ((wallShapes & CAVE_WALL_RETRACT) != 0) {

//                                    e2[x][y] = (currentDun == '+' || currentDun == '/') ? MixedGenerator.UNTOUCHED : currentEnv;
//                                    d2[x][y] = (currentDun == '+' || currentDun == '/') ? '\u0005' : currentDun;
//                                    e2[x-1][y] = (currentDun == '+' || currentDun == '/') ? MixedGenerator.UNTOUCHED : currentEnv;
//                                    d2[x-1][y] = (currentDun == '+' || currentDun == '/') ? '\u0005' : 6;
                                        e2[x][y] = currentEnv;
                                        d2[x][y] = currentDun;
                                        e2[x - 1][y] = currentEnv;
                                        d2[x - 1][y] = 6;


                                    }
                                    break;
                                case MixedGenerator.CORRIDOR_FLOOR:
                                    if ((wallShapes & CORRIDOR_WALL_RETRACT) != 0) {
                                        e2[x][y] = currentEnv;
                                        d2[x][y] = currentDun;
                                        e2[x - 1][y] = currentEnv;
                                        d2[x - 1][y] = 6;

                                    }
                                    break;
                            }
                        }
                    }
                }
            }

//            for (int x = nw - 2; x > 1; x -= 2) {
//                for (int y = 0; y < nh; y++) {
//                    if(((env2 = e2[x - 1][y]) & 1) == 0 && ((currentEnv = e2[x + 1][y]) & 1) != 0) {
//                        currentDun = d2[x - 1][y];
//                        switch (currentEnv) {
//                            case MixedGenerator.ROOM_FLOOR:
//                                if ((wallShapes & ROOM_WALL_RETRACT) != 0) {
//                                    e2[x][y] = env2;
//                                    d2[x][y] = currentDun;
//                                }
//                                break;
//                            case MixedGenerator.CAVE_FLOOR:
//                                if ((wallShapes & CAVE_WALL_RETRACT) != 0) {
//                                    e2[x][y] = env2;
//                                    d2[x][y] = currentDun;
//                                }
//                                break;
//                            case MixedGenerator.CORRIDOR_FLOOR:
//                                if ((wallShapes & CORRIDOR_WALL_RETRACT) != 0) {
//                                    e2[x][y] = env2;
//                                    d2[x][y] = currentDun;
//                                }
//                                break;
//                        }
//                    }
//                }
//            }

            eArchive = ArrayTools.copy(e2);
            for (int x = 0; x < nw - 2; x++) {
                for (int y = 2; y < nh - 2; y += 2) {
                    offset = (d2[x][y - 1] == '+') ? 1 : 2;
                    if ((eArchive[x][y] & 1) == 0 && ((currentEnv = eArchive[x][y - offset]) & 1) != 0) {
                        currentDun = d2[x][y - offset];
                        if (currentDun == '+' || currentDun == '/') {
                            switch (currentEnv) {
                                case MixedGenerator.ROOM_FLOOR:
                                    if ((wallShapes & ROOM_WALL_RETRACT) != 0) {
                                        e2[x][y + 2 - offset] = currentEnv;
                                        d2[x][y + 2 - offset] = currentDun;
                                        e2[x][y + 1 - offset] = eArchive[x][y - 1 - offset];
                                        d2[x][y + 1 - offset] = d2[x][y - 1 - offset];
                                        e2[x][y - offset] = eArchive[x][y - 1 - offset];
                                        d2[x][y - offset] = d2[x][y - 1 - offset];
                                    }
                                    break;
                                case MixedGenerator.CAVE_FLOOR:
                                    if ((wallShapes & CAVE_WALL_RETRACT) != 0) {
                                        e2[x][y + 2 - offset] = currentEnv;
                                        d2[x][y + 2 - offset] = currentDun;
                                        e2[x][y + 1 - offset] = eArchive[x][y - 1 - offset];
                                        d2[x][y + 1 - offset] = d2[x][y - 1 - offset];
                                        e2[x][y - offset] = eArchive[x][y - 1 - offset];
                                        d2[x][y - offset] = d2[x][y - 1 - offset];
                                    }
                                    break;
                                case MixedGenerator.CORRIDOR_FLOOR:
                                    if ((wallShapes & CORRIDOR_WALL_RETRACT) != 0) {
                                        e2[x][y + 2 - offset] = currentEnv;
                                        d2[x][y + 2 - offset] = currentDun;
                                        e2[x][y + 1 - offset] = eArchive[x][y - 1 - offset];
                                        d2[x][y + 1 - offset] = d2[x][y - 1 - offset];
                                        e2[x][y - offset] = eArchive[x][y - 1 - offset];
                                        d2[x][y - offset] = d2[x][y - 1 - offset];
                                    }
                                    break;
                            }

                        } else {
                            switch (currentEnv) {
                                case MixedGenerator.ROOM_FLOOR:
                                    if ((wallShapes & ROOM_WALL_RETRACT) != 0) {
                                        e2[x][y] = currentEnv;
                                        d2[x][y] = currentDun;
                                        e2[x][y - 1] = currentEnv;
                                        d2[x][y - 1] = 6;
                                    }
                                    break;
                                case MixedGenerator.CAVE_FLOOR:
                                    if ((wallShapes & CAVE_WALL_RETRACT) != 0) {
                                        e2[x][y] = currentEnv;
                                        d2[x][y] = currentDun;
                                        e2[x][y - 1] = currentEnv;
                                        d2[x][y - 1] = 6;
                                    }
                                    break;
                                case MixedGenerator.CORRIDOR_FLOOR:
                                    if ((wallShapes & CORRIDOR_WALL_RETRACT) != 0) {
                                        e2[x][y] = currentEnv;
                                        d2[x][y] = currentDun;
                                        e2[x][y - 1] = currentEnv;
                                        d2[x][y - 1] = 6;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

//            for (int x = 0; x < nw; x++) {
//                for (int y = nh - 2; y > 1; y -= 2) {
//                    if(((env2 = e2[x][y - 1]) & 1) == 0 && ((currentEnv = e2[x][y + 1]) & 1) != 0) {
//                        currentDun = d2[x][y + 1];
//                        switch (currentEnv) {
//                            case MixedGenerator.ROOM_FLOOR:
//                                if ((wallShapes & ROOM_WALL_RETRACT) != 0) {
//                                    e2[x][y] = env2;
//                                    d2[x][y] = currentDun;
//                                }
//                                break;
//                            case MixedGenerator.CAVE_FLOOR:
//                                if ((wallShapes & CAVE_WALL_RETRACT) != 0) {
//                                    e2[x][y] = env2;
//                                    d2[x][y] = currentDun;
//                                }
//                                break;
//                            case MixedGenerator.CORRIDOR_FLOOR:
//                                if ((wallShapes & CORRIDOR_WALL_RETRACT) != 0) {
//                                    e2[x][y] = env2;
//                                    d2[x][y] = currentDun;
//                                }
//                                break;
//                        }
//                    }
//                }
//            }
        }


        for (int x = 0; x < nw; x++) {
            for (int y = 0; y < nh; y++) {
                if (d2[x][y] == '\u0005') {
                    e2[x][y] = MixedGenerator.UNTOUCHED;
                    d2[x][y] = '#';
                }
            }
        }

        for (int x = 1; x < nw - 1; x += 2) {
            for (int y = 1; y < nh - 1; y += 2) {
                if (d2[x - 1][y] == '#' && d2[x + 1][y] == '#') {
                    e2[x][y] = MixedGenerator.UNTOUCHED;
                    d2[x][y] = '#';
                }
            }
        }
        for (int x = 1; x < nw - 1; x++) {
            for (int y = 1; y < nh - 1; y += 2) {
                if (e2[x][y - 1] == MixedGenerator.UNTOUCHED || e2[x][y + 1] == MixedGenerator.UNTOUCHED) {
                    if (e2[x - 1][y] == MixedGenerator.UNTOUCHED) {
                        e2[x][y] = MixedGenerator.UNTOUCHED;
                        d2[x][y] = '#';
                    } else if (e2[x + 1][y] == MixedGenerator.UNTOUCHED) {
                        e2[x][y] = MixedGenerator.UNTOUCHED;
                        d2[x][y] = '#';
                    }
                }
            }
        }
        dungeon = d2;
        width = nw;
        height = nh;
        if (stairsUp != null) {
            stairsUp = stairsUp.multiply(2);
        }
        if (stairsDown != null) {
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

    /**
     * Modifies this ThinDungeonGenerator to remove corners between thin walls (truncating the corners, effectively).
     * Regenerates the RoomFinder and Placement objects this uses.
     */
    public void removeHardCorners()
    {
        final int[][] environment = finder.environment;
        final int width = dungeon.length, height = dungeon[0].length;
        for (int x = 1; x < width - 1; x += 2) {
            for (int y = 1; y < height - 1; y += 2) {
                if((environment[x+1][y] & 1) != (environment[x-1][y] & 1) &&
                        (environment[x][y+1] & 1) != (environment[x][y-1] & 1)) {
                    dungeon[x][y] = 6;
                }
            }
        }
        finder = new RoomFinder(dungeon, environment);
        placement = new Placement(finder);

    }

    /**
     * Provides a string representation of the latest generated dungeon.
     * Because the spaces between occupy-able cells, when walkable, use the control character U+0006
     * to indicate that they're not meant to be displayed, this replaces any instances of that character
     * in the printable output with ordinary space characters, while keeping the internal representation
     * as the control character.
     *
     * @return a printable string version of the latest generated dungeon.
     */
    @Override
    public String toString() {
        return super.toString().replace('\u0006', ' ');
    }

}
