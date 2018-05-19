package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.util.*;

/**
 * Generator for maps of high-tech areas like space stations or starships, with repeated modules laid out in random ways.
 * Different from traditional fantasy dungeon generation in that it should seem generally less chaotic in how it's laid
 * out, and repeated elements with minor tweaks should be especially common. May also be useful in fantasy games for
 * regimented areas built by well-organized military forces.
 * <br>
 * Preview: https://gist.github.com/tommyettinger/c711f8fc83fa9919245d88092444bf7f
 * Created by Tommy Ettinger on 4/2/2016.
 */
@Beta
public class ModularMapGenerator implements IDungeonGenerator {
    public DungeonUtility utility;
    protected int height, width;
    public StatefulRNG rng;
    protected long rebuildSeed;
    protected boolean seedFixed = false;

    protected char[][] map = null;
    protected int[][] environment = null;
    //public RegionMap<MapModule> layout, modules, inverseModules;
    public RegionMap<MapModule> layout;
    public OrderedMap<Integer, ArrayList<MapModule>> modules;
    public OrderedMap<Coord, MapModule> displacement;

    private void putModule(short[] module) {
        char[][] unp = CoordPacker.unpackChar(module, '.', '#');
        MapModule mm = new MapModule(ArrayTools.insert(unp,
                ArrayTools.fill('#', unp.length + 2, unp[0].length + 2), 1, 1));
        //short[] b = CoordPacker.rectangle(1 + mm.max.x, 1 + mm.max.y);
        //modules.put(b, mm);
        //inverseModules.put(CoordPacker.negatePacked(b), mm);
        ArrayList<MapModule> mms = modules.get(mm.category);
        if (mms == null) {
            mms = new ArrayList<>(16);
            mms.add(mm);
            modules.put(mm.category, mms);
        } else
            mms.add(mm);
    }

    private void putRectangle(int width, int height, float multiplier) {
        putModule(CoordPacker.rectangle(Math.round(width * multiplier), Math.round(height * multiplier)));
    }

    private void putCircle(int radius, float multiplier) {
        putModule(CoordPacker.circle(Coord.get(Math.round(radius * multiplier + 1), Math.round(radius * multiplier + 1)),
                Math.round(radius * multiplier),
                Math.round((radius + 1) * 2 * multiplier + 1), Math.round((radius + 1) * 2 * multiplier + 1)));
    }

    private void initModules() {
        layout = new RegionMap<>(64);
        //modules = new RegionMap<>(64);
        //inverseModules = new RegionMap<>(64);
        modules = new OrderedMap<>(64);
        for (int i = 1; i <= 64; i <<= 1) {
            ArrayList<MapModule> mms = new ArrayList<>(16);
            modules.put(i, mms);
        }
        displacement = new OrderedMap<>(64);
        float multiplier = 1;//(float) Math.sqrt(Math.max(1f, Math.min(width, height) / 24f));
        putRectangle(2, 2, multiplier);
        putRectangle(3, 3, multiplier);
        putRectangle(4, 4, multiplier);
        putRectangle(4, 2, multiplier);
        putRectangle(2, 4, multiplier);
        putRectangle(6, 6, multiplier);
        putRectangle(6, 3, multiplier);
        putRectangle(3, 6, multiplier);
        putCircle(2, multiplier);

        putRectangle(8, 8, multiplier);
        putRectangle(6, 12, multiplier);
        putRectangle(12, 6, multiplier);
        putCircle(4, multiplier);

        putRectangle(14, 14, multiplier);
        putRectangle(9, 18, multiplier);
        putRectangle(18, 9, multiplier);
        putRectangle(14, 18, multiplier);
        putRectangle(18, 14, multiplier);
        putCircle(7, multiplier);
    }

    /**
     * Make a ModularMapGenerator with a StatefulRNG (backed by LightRNG) using a random seed, height 30, and width 60.
     */
    public ModularMapGenerator() {
        this(60, 30);
    }

    /**
     * Make a ModularMapGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a StatefulRNG (backed by LightRNG) using a random seed.
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public ModularMapGenerator(int width, int height) {
        this(width, height, new StatefulRNG());
    }

    /**
     * Make a ModularMapGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng    The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     *               but if it is not a StatefulRNG, a new StatefulRNG will be used, randomly seeded by this parameter
     */
    public ModularMapGenerator(int width, int height, RNG rng) {
        CoordPacker.init();
        this.rng = (rng instanceof StatefulRNG) ? (StatefulRNG) rng : new StatefulRNG(rng.nextLong());
        utility = new DungeonUtility(this.rng);
        rebuildSeed = this.rng.getState();
        this.height = height;
        this.width = width;
        map = new char[width][height];
        environment = new int[width][height];
        for (int x = 0; x < this.width; x++) {
            Arrays.fill(map[x], '#');
        }
        initModules();
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     *
     * @param copying the DungeonGenerator to copy
     */
    public ModularMapGenerator(ModularMapGenerator copying) {
        CoordPacker.init();
        rng = new StatefulRNG(copying.rng.getState());
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = copying.height;
        width = copying.width;
        map = ArrayTools.copy(copying.map);
        environment = ArrayTools.copy(copying.environment);
        layout = new RegionMap<>(copying.layout);
        modules = new OrderedMap<>(copying.modules);
    }

    /**
     * Get the most recently generated char[][] map out of this class. The
     * map may be null if generate() or setMap() have not been called.
     *
     * @return a char[][] map, or null.
     */
    public char[][] getMap() {
        return map;
    }
    /**
     * Get the most recently generated char[][] map out of this class. The
     * map may be null if generate() or setMap() have not been called.
     *
     * @return a char[][] map, or null.
     */
    public char[][] getDungeon() {
        return map;
    }

    /**
     * Get the most recently generated char[][] map out of this class without any chars other than '#' or '.', for
     * walls and floors respectively. The map may be null if generate() or setMap() have not been called.
     *
     * @return a char[][] map with only '#' for walls and '.' for floors, or null.
     */
    public char[][] getBareMap() {
        return DungeonUtility.simplifyDungeon(map);
    }

    public char[][] generate() {
        MapModule mm, mm2;
        int xPos, yPos, categorySize = 32, alteredSize = (categorySize * 3) >>> 1, bits = 5, ctr;
        Coord[][] grid = new Coord[1][1];
        // find biggest category and drop down as many modules as we can fit
        for (; categorySize >= 4; categorySize >>= 1, alteredSize = (categorySize * 3) >>> 1, bits--) {
            if (width / alteredSize <= 0 || height / alteredSize <= 0)
                continue;
            grid = new Coord[width / alteredSize][height / alteredSize];
            ctr = 0;
            for (int xLimit = alteredSize - 1, x = 0; xLimit < width; xLimit += alteredSize, x += alteredSize) {
                for (int yLimit = alteredSize - 1, y = 0; yLimit < height; yLimit += alteredSize, y += alteredSize) {
                    if (layout.allAt(x + alteredSize / 2, y + alteredSize / 2).isEmpty()) // && (bits <= 3 || rng.nextInt(5) < bits)
                    {
                        if (rng.between(2, grid.length * grid[0].length + 3) == ctr++)
                            continue;
                        mm = rng.getRandomElement(modules.get(categorySize));
                        if (mm == null) break;
                        mm = mm.rotate(rng.nextInt(4));
                        xPos = rng.nextInt(3) << (bits - 2);
                        yPos = rng.nextInt(3) << (bits - 2);
                        for (int px = 0; px <= mm.max.x; px++) {
                            System.arraycopy(mm.map[px], 0, map[px + x + xPos], y + yPos, mm.max.y + 1);
                            System.arraycopy(mm.environment[px], 0, environment[px + x + xPos], y + yPos, mm.max.y + 1);
                        }
                        layout.put(CoordPacker.rectangle(x + xPos, y + yPos, categorySize, categorySize), mm);
                        displacement.put(Coord.get(x + xPos, y + yPos), mm);
                        grid[x / alteredSize][y / alteredSize] = Coord.get(x + xPos, y + yPos);
                    }
                }
            }
            if (!layout.isEmpty())
                break;
        }
        Coord a, b;
        int gw = grid.length;
        if (gw > 0) {
            int gh = grid[0].length;
            for (int w = 0; w < gw; w++) {
                for (int h = 0; h < gh; h++) {
                    a = grid[w][h];
                    if (a == null)
                        continue;
                    int connectors = rng.nextInt(16) | rng.nextInt(16);
                    if ((connectors & 1) == 1 && w > 0 && grid[w - 1][h] != null) {
                        b = grid[w - 1][h];
                        connectLeftRight(displacement.get(b), b.x, b.y, displacement.get(a), a.x, a.y);
                    }
                    if ((connectors & 2) == 2 && w < gw - 1 && grid[w + 1][h] != null) {
                        b = grid[w + 1][h];
                        connectLeftRight(displacement.get(a), a.x, a.y, displacement.get(b), b.x, b.y);
                    }
                    if ((connectors & 4) == 4 && h > 0 && grid[w][h - 1] != null) {
                        b = grid[w][h - 1];
                        connectTopBottom(displacement.get(b), b.x, b.y, displacement.get(a), a.x, a.y);
                    }
                    if ((connectors & 8) == 8 && h < gh - 1 && grid[w][h + 1] != null) {
                        b = grid[w][h + 1];
                        connectTopBottom(displacement.get(a), a.x, a.y, displacement.get(b), b.x, b.y);
                    }
                }
            }
        }
        Coord begin;
        short[] packed;
        for (Map.Entry<Coord, MapModule> dmm : displacement.entrySet()) {
            begin = dmm.getKey();
            mm = dmm.getValue();
            //int newCat = mm.category;
            //if(newCat >= 16) newCat >>>= 1;
            //if(newCat >= 8) newCat >>>= 1;
            //mm2 = rng.getRandomElement(modules.get(newCat));
            int shiftsX = begin.x - (mm.category * 3 / 2) * ((begin.x * 2) / (3 * mm.category)),
                    shiftsY = begin.y - (mm.category * 3 / 2) * ((begin.y * 2) / (3 * mm.category)),
                    leftSize = Integer.highestOneBit(shiftsX),
                    rightSize = Integer.highestOneBit((mm.category >>> 1) - shiftsX),
                    topSize = Integer.highestOneBit(shiftsY),
                    bottomSize = Integer.highestOneBit((mm.category >>> 1) - shiftsY);
            if (leftSize >= 4 && !mm.leftDoors.isEmpty()) {
                mm2 = rng.getRandomElement(modules.get(leftSize));
                if (mm2 == null) continue;
                if (mm2.rightDoors.isEmpty()) {
                    if (!mm2.topDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else if (!mm2.leftDoors.isEmpty())
                        mm2 = mm2.flip(true, false);
                    else if (!mm2.bottomDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    packed = CoordPacker.rectangle(begin.x - shiftsX, begin.y + i * mm.category / 4, leftSize, leftSize);
                    if (layout.containsRegion(packed))
                        continue;
                    for (int px = 0; px <= mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x - shiftsX], begin.y + i * mm.category / 4, mm2.max.y + 1);
                        System.arraycopy(mm2.environment[px], 0, environment[px + begin.x - shiftsX], begin.y + i * mm.category / 4, mm2.max.y + 1);
                    }
                    layout.put(packed, mm2);
                    connectLeftRight(mm2, begin.x - shiftsX, begin.y + i * mm.category / 4, mm, begin.x, begin.y);
                }
            }
            if (rightSize >= 4 && !mm.rightDoors.isEmpty()) {
                mm2 = rng.getRandomElement(modules.get(rightSize));
                if (mm2 == null) continue;
                if (mm2.leftDoors.isEmpty()) {
                    if (!mm2.bottomDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else if (!mm2.rightDoors.isEmpty())
                        mm2 = mm2.flip(true, false);
                    else if (!mm2.topDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    packed = CoordPacker.rectangle(begin.x + mm.category, begin.y + i * mm.category / 4, rightSize, rightSize);
                    if (layout.containsRegion(packed))
                        continue;
                    for (int px = 0; px <= mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x + mm.category], begin.y + i * mm.category / 4, mm2.max.y + 1);
                        System.arraycopy(mm2.environment[px], 0, environment[px + begin.x + mm.category], begin.y + i * mm.category / 4, mm2.max.y + 1);
                    }
                    layout.put(packed, mm2);
                    connectLeftRight(mm, begin.x, begin.y, mm2, begin.x + mm.category, begin.y + i * mm.category / 4);
                }
            }
            if (topSize >= 4 && !mm.topDoors.isEmpty()) {
                mm2 = rng.getRandomElement(modules.get(topSize));
                if (mm2 == null) continue;
                if (mm2.bottomDoors.isEmpty()) {
                    if (!mm2.leftDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else if (!mm2.topDoors.isEmpty())
                        mm2 = mm2.flip(false, true);
                    else if (!mm2.rightDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    packed = CoordPacker.rectangle(begin.x + i * mm.category / 4, begin.y - shiftsY, topSize, topSize);
                    if (layout.containsRegion(packed))
                        continue;
                    for (int px = 0; px <= mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x + i * mm.category / 4], begin.y - shiftsY, mm2.max.y + 1);
                        System.arraycopy(mm2.environment[px], 0, environment[px + begin.x + i * mm.category / 4], begin.y - shiftsY, mm2.max.y + 1);
                    }
                    layout.put(packed, mm2);
                    connectTopBottom(mm2, begin.x + i * mm.category / 4, begin.y - shiftsY, mm, begin.x, begin.y);
                }
            }
            if (bottomSize >= 4 && !mm.bottomDoors.isEmpty()) {
                mm2 = rng.getRandomElement(modules.get(bottomSize));
                if (mm2 == null) continue;
                if (mm2.topDoors.isEmpty()) {
                    if (!mm2.rightDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else if (!mm2.topDoors.isEmpty())
                        mm2 = mm2.flip(false, true);
                    else if (!mm2.leftDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    packed = CoordPacker.rectangle(begin.x + i * mm.category / 4, begin.y + mm.category, bottomSize, bottomSize);
                    if (layout.containsRegion(packed))
                        continue;
                    for (int px = 0; px <= mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x + i * mm.category / 4], begin.y + mm.category, mm2.max.y + 1);
                        System.arraycopy(mm2.environment[px], 0, environment[px + begin.x + i * mm.category / 4], begin.y + mm.category, mm2.max.y + 1);
                    }
                    layout.put(packed, mm2);
                    connectTopBottom(mm, begin.x, begin.y, mm2, begin.x + i * mm.category / 4, begin.y + mm.category);
                }
            }
        }
        return map;
    }

    /**
     * Change the underlying char[][]; only affects the toString method, and of course getMap
     *
     * @param map a char[][], probably produced by an earlier call to this class and then modified.
     */
    public void setMap(char[][] map) {
        this.map = map;
        if (map == null) {
            width = 0;
            height = 0;
            return;
        }
        width = map.length;
        if (width > 0)
            height = map[0].length;
    }

    /**
     * Height of the map in cells.
     *
     * @return Height of the map in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Width of the map in cells.
     *
     * @return Width of the map in cells.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the environment int 2D array for use with classes like RoomFinder.
     *
     * @return the environment int 2D array
     */
    public int[][] getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment int 2D array.
     *
     * @param environment a 2D array of int, where each int corresponds to a constant in MixedGenerator.
     */
    public void setEnvironment(int[][] environment) {
        this.environment = environment;
    }

    private void connectLeftRight(MapModule left, int leftX, int leftY, MapModule right, int rightX, int rightY) {
        if (left.rightDoors == null || left.rightDoors.isEmpty()
                || right.leftDoors == null || right.leftDoors.isEmpty())
            return;
        List<Coord> line = new ArrayList<>(1), temp;
        int best = 1024;
        Coord tl, tr, twl, twr, wl = null, wr = null;
        for (Coord l : left.rightDoors) {
            tl = twl = l.translate(leftX, leftY);
            if (tl.x > 0 && tl.x < width - 1 && map[tl.x - 1][tl.y] != '#')
                tl = Coord.get(tl.x + 1, tl.y);
            else if (tl.x > 0 && tl.x < width - 1 && map[tl.x + 1][tl.y] != '#')
                tl = Coord.get(tl.x - 1, tl.y);
            else if (tl.y > 0 && tl.y < height - 1 && map[tl.x][tl.y - 1] != '#')
                tl = Coord.get(tl.x, tl.y + 1);
            else if (tl.y > 0 && tl.y < height - 1 && map[tl.x][tl.y + 1] != '#')
                tl = Coord.get(tl.x, tl.y - 1);
            else
                continue;

            for (Coord r : right.leftDoors) {
                tr = twr = r.translate(rightX, rightY);
                if (tr.x > 0 && tr.x < width - 1 && map[tr.x - 1][tr.y] != '#')
                    tr = Coord.get(tr.x + 1, tr.y);
                else if (tr.x > 0 && tr.x < width - 1 && map[tr.x + 1][tr.y] != '#')
                    tr = Coord.get(tr.x - 1, tr.y);
                else if (tr.y > 0 && tr.y < height - 1 && map[tr.x][tr.y - 1] != '#')
                    tr = Coord.get(tr.x, tr.y + 1);
                else if (tr.y > 0 && tr.y < height - 1 && map[tr.x][tr.y + 1] != '#')
                    tr = Coord.get(tr.x, tr.y - 1);
                else
                    continue;
                temp = OrthoLine.line(tl, tr);
                if (temp.size() < best) {
                    line = temp;
                    best = line.size();
                    wl = twl;
                    wr = twr;
                }
            }
        }
        temp = new ArrayList<>(line.size());
        for (Coord c : line) {
            if (map[c.x][c.y] == '#') {
                map[c.x][c.y] = '.';
                environment[c.x][c.y] = DungeonUtility.CORRIDOR_FLOOR;
                temp.add(c);
            }
        }
        if (wl != null && map[wl.x][wl.y] == '#') {
            //if(line.isEmpty())
            map[wl.x][wl.y] = '.';
            environment[wl.x][wl.y] = DungeonUtility.ROOM_FLOOR;
            //else
            //    map[wl.x][wl.y] = '+';
        }
        if (wr != null && map[wr.x][wr.y] == '#') {
            //if(line.isEmpty())
            map[wr.x][wr.y] = '.';
            environment[wr.x][wr.y] = DungeonUtility.ROOM_FLOOR;
            //else
            //    map[wr.x][wr.y] = '+';
        }
        layout.put(CoordPacker.packSeveral(temp), null);

    }

    private void connectTopBottom(MapModule top, int topX, int topY, MapModule bottom, int bottomX, int bottomY) {
        if (top.bottomDoors == null || top.bottomDoors.isEmpty()
                || bottom.topDoors == null || bottom.topDoors.isEmpty())
            return;
        List<Coord> line = new ArrayList<>(1), temp;
        int best = 1024;
        Coord tt, tb, twt, twb, wt = null, wb = null;
        for (Coord l : top.bottomDoors) {
            tt = twt = l.translate(topX, topY);
            if (tt.y > 0 && tt.y < height - 1 && map[tt.x][tt.y - 1] != '#')
                tt = Coord.get(tt.x, tt.y + 1);
            else if (tt.y > 0 && tt.y < height - 1 && map[tt.x][tt.y + 1] != '#')
                tt = Coord.get(tt.x, tt.y - 1);
            else if (tt.x > 0 && tt.x < width - 1 && map[tt.x - 1][tt.y] != '#')
                tt = Coord.get(tt.x + 1, tt.y);
            else if (tt.x > 0 && tt.x < width - 1 && map[tt.x + 1][tt.y] != '#')
                tt = Coord.get(tt.x - 1, tt.y);
            else
                continue;

            for (Coord r : bottom.topDoors) {
                tb = twb = r.translate(bottomX, bottomY);
                if (tb.y > 0 && tb.y < height - 1 && map[tb.x][tb.y - 1] != '#')
                    tb = Coord.get(tb.x, tb.y + 1);
                else if (tb.y > 0 && tb.y < height - 1 && map[tb.x][tb.y + 1] != '#')
                    tb = Coord.get(tb.x, tb.y - 1);
                else if (tb.x > 0 && tb.x < width - 1 && map[tb.x - 1][tb.y] != '#')
                    tb = Coord.get(tb.x + 1, tb.y);
                else if (tb.x > 0 && tb.x < width - 1 && map[tb.x + 1][tb.y] != '#')
                    tb = Coord.get(tb.x - 1, tb.y);
                else
                    continue;
                temp = OrthoLine.line(tt, tb);
                if (temp.size() < best) {
                    line = temp;
                    best = line.size();
                    wt = twt;
                    wb = twb;
                }
            }
        }
        temp = new ArrayList<>(line.size());
        for (Coord c : line) {
            if (map[c.x][c.y] == '#') {
                map[c.x][c.y] = '.';
                environment[c.x][c.y] = DungeonUtility.CORRIDOR_FLOOR;
                temp.add(c);
            }
        }
        if (wt != null && map[wt.x][wt.y] == '#') {
            //if(line.isEmpty())
            map[wt.x][wt.y] = '.';
            environment[wt.x][wt.y] = DungeonUtility.ROOM_FLOOR;
            //else
            //    map[wl.x][wl.y] = '+';

        }
        if (wb != null && map[wb.x][wb.y] == '#') {
            //if(line.isEmpty())
            map[wb.x][wb.y] = '.';
            environment[wb.x][wb.y] = DungeonUtility.ROOM_FLOOR;
            //else
            //    map[wb.x][wb.y] = '+';
        }
        layout.put(CoordPacker.packSeveral(temp), null);
    }
}
