/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;

/**
 * Meant to produce the sort of narrow, looping, not-quite-maze-like passages found in a certain famous early arcade game.
 * Created by Tommy Ettinger on 3/30/2016.
 */
public class PacMazeGenerator {
    public IRNG rng;
    public int width, height;
    public int wallBreadth = 2, passageBreadth = 1;
    private int combinedBreadth = wallBreadth + passageBreadth;
    private GreasedRegion map;
    private int[][] env;
    private char[][] maze;

    public PacMazeGenerator() {
        this(250, 250);
    }

    public PacMazeGenerator(int width, int height) {
        this.height = height;
        this.width = width;
        rng = new GWTRNG();
    }

    public PacMazeGenerator(int width, int height, IRNG rng) {
        this.height = height;
        this.width = width;
        this.rng = rng;
    }

    /**
     *
     * @param width the x-size of the maze map to produce
     * @param height the y-size of the maze map to produce
     * @param wallBreadth how thick wall sections should be; defaults to 2
     * @param passageBreadth how broad passages should be; defaults to 1
     * @param rng an IRNG, such as a {@link squidpony.squidmath.RNG}, which can be seeded
     */
    public PacMazeGenerator(int width, int height, int wallBreadth, int passageBreadth, IRNG rng) {
        this.height = height;
        this.width = width;
        this.wallBreadth = wallBreadth;
        this.passageBreadth = passageBreadth;
        this.combinedBreadth = wallBreadth + passageBreadth;
        this.rng = rng;
    }

    private static final byte[] //unbiased_connections = new byte[]{3, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15},
            connections = new byte[]{
            3, 5, 6, 9, 10, 12,/*
                    3, 5, 6, 9, 10, 12,
                    3, 5, 6, 9, 10, 12,
                    3, 5, 6, 9, 10, 12,
                    7, 11, 13, 14,
                    7, 11, 13, 14,
                    15*/
    };
    private static final int connections_length = connections.length;

    private void write(GreasedRegion m, int x, int y, int xOffset, int yOffset) {
        int nx = x * combinedBreadth + xOffset + 1, ny = y * combinedBreadth + yOffset + 1;
        m.insert(nx, ny);
    }

    private void writeRect(GreasedRegion m, int x, int y, int endX, int endY, int xOffset, int yOffset) {
        int startX = Math.min(x, endX);
        int startY = Math.min(y, endY);
        int nx = startX * combinedBreadth + xOffset + 1, ny = startY * combinedBreadth + yOffset + 1;
        m.insertRectangle(nx, ny, Math.max(x, endX) - startX, Math.max(y, endY) - startY);
    }

    public GreasedRegion create() {
        map = new GreasedRegion(width, height);
        byte[][] conns = new byte[(width + combinedBreadth - 1) / combinedBreadth][(height + combinedBreadth - 1) / combinedBreadth];
        int xOff = (width % combinedBreadth == 1) ? -1 : 0, yOff = (height % combinedBreadth == 1) ? -1 : 0;
        for (int x = 0; x < (width + combinedBreadth - 1) / combinedBreadth; x++) {
            for (int y = 0; y < (height + combinedBreadth - 1) / combinedBreadth; y++) {
                conns[x][y] = connections[rng.nextInt(connections_length)];
            }
        }
        for (int x = 0; x < (width + combinedBreadth - 1) / combinedBreadth; x++) {
            for (int y = 0; y < (height + combinedBreadth - 1) / combinedBreadth; y++) {
                if (x > 0 && ((conns[x - 1][y] & 1) != 0 || (conns[x][y] & 2) != 0)) {
                    conns[x - 1][y] |= 1;
                    conns[x][y] |= 2;
                }
                if (x < conns.length - 1 && ((conns[x + 1][y] & 2) != 0 || (conns[x][y] & 1) != 0)) {
                    conns[x + 1][y] |= 2;
                    conns[x][y] |= 1;
                }
                if (y > 0 && ((conns[x][y - 1] & 4) != 0 || (conns[x][y] & 8) != 0)) {
                    conns[x][y - 1] |= 4;
                    conns[x][y] |= 8;
                }
                if (y < conns[0].length - 1 && ((conns[x][y + 1] & 8) != 0 || (conns[x][y] & 4) != 0)) {
                    conns[x][y + 1] |= 8;
                    conns[x][y] |= 4;
                }
            }
        }

        for (int x = 1; x < (width - 1) / combinedBreadth; x++) {
            for (int y = 1; y < (height - 1) / combinedBreadth; y++) {
                if (Integer.bitCount(conns[x][y]) >= 4) {
                    //byte temp = connections[rng.nextInt(connections_length)];
                    int temp = 1 << rng.next(2);
                    conns[x][y] ^= temp;
                    if ((temp & 2) != 0) conns[x - 1][y] ^= 1;
                    else if ((temp & 1) != 0) conns[x + 1][y] ^= 2;
                    else if ((temp & 8) != 0) conns[x][y - 1] ^= 4;
                    else if ((temp & 4) != 0) conns[x][y + 1] ^= 8;
                }
            }
        }
        for (int x = 0; x < (width + combinedBreadth - 1) / combinedBreadth; x++) {
            for (int y = 0; y < (height + combinedBreadth - 1) / combinedBreadth; y++) {
                for (int i = 0; i < passageBreadth; i++) {
                    for (int j = 0; j < passageBreadth; j++) {
                        write(map, x, y, xOff+i, yOff+j);
                    }
                }
                if (x > 0 && (conns[x][y] & 2) != 0)
                {
                    for (int w = 1; w <= wallBreadth; w++) {
                        for (int p = 0; p < passageBreadth; p++) {
                            write(map, x, y, xOff - w, yOff + p);
                        }
                    }
                }
                if (x < conns.length - 1 && (conns[x][y] & 1) != 0)
                {
                    for (int w = 1; w <= wallBreadth; w++) {
                        for (int p = 0; p < passageBreadth; p++) {
                            write(map, x, y, xOff + w, yOff + p);
                        }
                    }
                }
                if (y > 0 && (conns[x][y] & 8) != 0)
                {
                    for (int w = 1; w <= wallBreadth; w++) {
                        for (int p = 0; p < passageBreadth; p++) {
                            write(map, x, y, xOff + p, yOff - w);
                        }
                    }
                }
                if (y < conns[0].length - 1 && (conns[x][y] & 4) != 0)
                {
                    for (int w = 1; w <= wallBreadth; w++) {
                        for (int p = 0; p < passageBreadth; p++) {
                            write(map, x, y, xOff + p, yOff + w);
                        }
                    }
                }
            }
        }
        map.removeEdges();
        return map;
    }

    public char[][] generate() {
        create();
        maze = new char[width][height];
        env = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                maze[x][y] = map.contains(x, y) ? '.' : '#';
                env[x][y] = map.contains(x, y) ? DungeonUtility.CORRIDOR_FLOOR : DungeonUtility.CORRIDOR_WALL;
            }
        }

        return maze;
    }

    public int[][] getEnvironment() {
        if (env == null)
            return ArrayTools.fill(DungeonUtility.CORRIDOR_WALL, width, height);
        return env;
    }

    /**
     * Gets the maze as a 2D array of true for passable or false for blocked.
     *
     * @return a 2D boolean array; true is passable and false is not.
     */
    public GreasedRegion getMap() {
        if (map == null)
            return new GreasedRegion(width, height);
        return map;
    }

    /**
     * Gets the maze as a 2D array of ',' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    public char[][] getMaze() {
        if (maze == null)
            return ArrayTools.fill('#', width, height);
        return maze;
    }

    /**
     * Gets the maze as a 2D array of ',' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    public char[][] getDungeon() {
        return getMaze();
    }
}
