package squidpony.squidgrid.mapping;

import squidpony.GwtCompatibility;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.util.*;

/**
 * Generator for maps of high-tech areas like space stations or starships, with repeated modules laid out in random ways.
 * Different from traditional fantasy dungeon generation in that it should seem generally less chaotic in how it's laid
 * out, and repeated elements with minor tweaks should be especially common.
 * Created by Tommy Ettinger on 4/2/2016.
 */
@Beta
public class ModularMapGenerator {
    public DungeonUtility utility;
    protected int height, width;
    public StatefulRNG rng;
    protected long rebuildSeed;
    protected boolean seedFixed = false;

    protected char[][] map = null;
    protected int[][] environment = null;
    private PacMazeGenerator mazeGenerator;
    //public RegionMap<MapModule> layout, modules, inverseModules;
    public RegionMap<MapModule> layout;
    public LinkedHashMap<Integer, ArrayList<MapModule>> modules;
    public LinkedHashMap<Coord, MapModule> displacement;
    private void putModule(short[] module)
    {
        MapModule mm = new MapModule(CoordPacker.unpackChar(module, '.', '#'));
        //short[] b = CoordPacker.rectangle(1 + mm.max.x, 1 + mm.max.y);
        //modules.put(b, mm);
        //inverseModules.put(CoordPacker.negatePacked(b), mm);
        ArrayList<MapModule> mms = modules.get(mm.category);
        if(mms == null) {
            mms = new ArrayList<>(16);
            mms.add(mm);
            modules.put(mm.category, mms);
        }
        else
            mms.add(mm);
    }
    private void putRectangle(int width, int height, float multiplier)
    {
        putModule(CoordPacker.rectangle(Math.round(width * multiplier), Math.round(height * multiplier)));
    }
    private void putCircle(int radius, float multiplier)
    {
        putModule(CoordPacker.circle(Coord.get(Math.round(radius * multiplier), Math.round(radius * multiplier)),
                Math.round(radius * multiplier),
                Math.round((radius+1)*2 * multiplier), Math.round((radius+1)*2 * multiplier)));
    }

    private void initModules()
    {
        layout = new RegionMap<>(64);
        //modules = new RegionMap<>(64);
        //inverseModules = new RegionMap<>(64);
        modules = new LinkedHashMap<>(64);
        displacement = new LinkedHashMap<>(64);
        float multiplier = (float) Math.sqrt(Math.max(1f, Math.min(width, height) / 24f));
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
    public ModularMapGenerator()
    {
        this(60, 30);
    }

    /**
     * Make a ModularMapGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a StatefulRNG (backed by LightRNG) using a random seed.
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public ModularMapGenerator(int width, int height)
    {
        this(width, height, new StatefulRNG());
    }

    /**
     * Make a ModularMapGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     *            but if it is not a StatefulRNG, a new StatefulRNG will be used, randomly seeded by this parameter
     */
    public ModularMapGenerator(int width, int height, RNG rng)
    {
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
        mazeGenerator = new PacMazeGenerator(width, height, this.rng);
        initModules();
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     * @param copying the DungeonGenerator to copy
     */
    public ModularMapGenerator(ModularMapGenerator copying)
    {
        rng = new StatefulRNG(copying.rng.getState());
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = copying.height;
        width = copying.width;
        map = GwtCompatibility.copy2D(copying.map);
        environment = GwtCompatibility.copy2D(copying.environment);
        mazeGenerator = new PacMazeGenerator(width, height, rng);
        layout = new RegionMap<>(copying.layout);
        modules = new LinkedHashMap<>(copying.modules);
    }
    /**
     * Get the most recently generated char[][] map out of this class. The
     * map may be null if generate() or setMap() have not been called.
     * @return a char[][] map, or null.
     */
    public char[][] getMap() {
        return map;
    }
    /**
     * Get the most recently generated char[][] map out of this class without any chars other than '#' or '.', for
     * walls and floors respectively. The map may be null if generate() or setMap() have not been called.
     * @return a char[][] map with only '#' for walls and '.' for floors, or null.
     */
    public char[][] getBareMap() {
        return DungeonUtility.simplifyDungeon(map);
    }

    /*
    public char[][] generate()
    {
        int minDim = Math.min(height, width), adjMin = Math.min(minDim, 20),
                maxDim = Math.max(height, width), adjMax = Math.min(maxDim, 25);
        MapModule mm;

        // you gave it a tiny map, what can it do?
        if(minDim < 16) {

            mm = rng.getRandomElement(modules.values().toList());
            map = GwtCompatibility.first(modules.allAt(rng.between(3, adjMin), rng.between(3, adjMin))).map;
            return DungeonUtility.wallWrap(map);
        }

        int frustration = 0, wrath = 0, count = 0;
        while ((mm = rng.getRandomElement(modules.allAt(rng.between(adjMin / 3, adjMin), rng.between(adjMin / 3, adjMin)))) == null
                        && frustration++ < 50)
        {} // intentionally empty, assigns in check for loop termination
        if(frustration >= 50 || mm == null)
        {
            mm = rng.getRandomElement(modules.values().toList());
            map = GwtCompatibility.first(modules.allAt(rng.between(3, adjMin), rng.between(3, adjMin))).map;
            return DungeonUtility.wallWrap(map);
        }
        // ok, mm is valid.
        frustration = 0;

        int placeX = rng.nextInt(minDim - mm.max.x - 4) + 4, placeY = rng.nextInt(minDim - mm.max.y - 4) + 4;
        for (int x = 0; x < mm.max.x; x++) {
            System.arraycopy(mm.map[x], 0, map[x + placeX], placeY, mm.max.y);
            count += mm.max.y;
        }
        int coreMinX = placeX, coreMinY = placeY,
                coreMaxX = placeX + mm.max.x, coreMaxY = placeY + mm.max.y,
                nodeWidth = Math.min(coreMinX, width - coreMaxX + 1),
                nodeHeight = Math.min(coreMinY, height - coreMaxY + 1);
        while (count < width * height * 0.3 && wrath < 50)
        {
            while ((mm = rng.getRandomElement(inverseModules.allAt(rng.nextInt(nodeWidth), rng.nextInt(nodeHeight)))) == null
                    && frustration++ < 50)
            {} // intentionally empty, assigns in check for loop termination
            if(frustration >= 50 || mm == null)
                break;
            frustration = 0;
            placeY = rng.nextInt(coreMinY);
            do {


                placeX = coreMinX; //rng.between(coreMinX, coreMaxX)
                for (int x = 0; x < mm.max.x; x++) {
                    System.arraycopy(mm.map[x], 0, map[x + placeX], placeY, mm.max.y);
                    count += mm.max.y;
                }
                placeX += mm.max.x + 1;
            }while (placeX < coreMaxX);

            wrath++;
        }

        return map;
    }
    */
    public char[][] generate()
    {
        MapModule mm, mm2;
        int xPos, yPos, categorySize = 32, alteredSize = (categorySize * 3) >>> 1, bits = 5;
        Coord[][] grid = new Coord[1][1];
        // find biggest category and drop down as many modules as we can fit
        for (; categorySize >= 4; categorySize >>= 1, alteredSize = (categorySize * 3) >>> 1, bits--) {
            if(width / alteredSize <= 0 || height / alteredSize <= 0)
                continue;
            grid = new Coord[width / alteredSize][height / alteredSize];
            for (int xLimit = alteredSize, x = 0; xLimit <= width; xLimit += alteredSize, x += alteredSize) {
                for (int yLimit = alteredSize, y = 0; yLimit <= height; yLimit += alteredSize, y += alteredSize) {
                    if (layout.allAt(x, y).isEmpty() && (bits <= 3 || rng.nextInt(6) < bits)) {
                        mm = rng.getRandomElement(modules.get(categorySize)).rotate(rng.nextInt(4));
                        if (mm == null) break;
                        xPos = rng.nextInt(3) << (bits - 2);
                        yPos = rng.nextInt(3) << (bits - 2);
                        for (int px = 0; px < mm.max.x; px++) {
                            System.arraycopy(mm.map[px], 0, map[px + x + xPos], y + yPos, mm.max.y);
                        }
                        layout.put(CoordPacker.rectangle(x + xPos, y + yPos, categorySize, categorySize), mm);
                        displacement.put(Coord.get(x + xPos, y + yPos), mm);
                        grid[x / alteredSize][y / alteredSize] = Coord.get(x + xPos, y + yPos);
                    }
                }
            }
            if(layout.size > 0)
                break;
        }
        Coord a, b;
        for (int w = 0; w < grid.length; w++) {
            for (int h = 0; h < grid[w].length; h++) {
                a = grid[w][h];
                if(a == null)
                    continue;
                int connectors = rng.nextInt(16);
                if((connectors & 1) == 1 && w > 0 && grid[w-1][h] != null)
                {
                    b = grid[w-1][h];
                    connectLeftRight(displacement.get(b), b.x, b.y, displacement.get(a), a.x, a.y);
                }
                if((connectors & 2) == 2 && w < width - 1 && grid[w+1][h] != null)
                {
                    b = grid[w+1][h];
                    connectLeftRight(displacement.get(a), a.x, a.y, displacement.get(b), b.x, b.y);
                }
                if((connectors & 4) == 4 && h > 0 && grid[w][h-1] != null)
                {
                    b = grid[w][h-1];
                    connectLeftRight(displacement.get(b), b.x, b.y, displacement.get(a), a.x, a.y);
                }
                if((connectors & 8) == 8 && h < height - 1 && grid[w][h+1] != null)
                {
                    b = grid[w][h+1];
                    connectLeftRight(displacement.get(a), a.x, a.y, displacement.get(b), b.x, b.y);
                }
            }
        }
        Coord begin;
        for(Map.Entry<Coord, MapModule> dmm : displacement.entrySet())
        {
            begin = dmm.getKey();
            mm = dmm.getValue();
            //int newCat = mm.category;
            //if(newCat >= 16) newCat >>>= 1;
            //if(newCat >= 8) newCat >>>= 1;
            //mm2 = rng.getRandomElement(modules.get(newCat));
            int shiftsX = (mm.category * 3 / 2) * ((begin.x * 2) / (3 * mm.category)) - begin.x,
                    shiftsY = (mm.category * 3 / 2) * ((begin.y * 2) / (3 * mm.category)) - begin.y,
                    leftSize = Integer.highestOneBit(shiftsX),
                    rightSize = Integer.highestOneBit((mm.category >>> 1) - shiftsX),
                    topSize = Integer.highestOneBit(shiftsY),
                    bottomSize = Integer.highestOneBit((mm.category >>> 1) - shiftsY);
            if(leftSize >= 4 && !mm.leftDoors.isEmpty())
            {
                mm2 = rng.getRandomElement(modules.get(leftSize));
                if (mm2 == null) continue;
                if(mm2.rightDoors.isEmpty())
                {
                    if(!mm2.topDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else if(!mm2.leftDoors.isEmpty())
                        mm2 = mm2.flip(true, false);
                    else if(!mm2.bottomDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    for (int px = 0; px < mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x - shiftsX], begin.y + i * mm.category / 4, mm2.max.y);
                    }
                    layout.put(CoordPacker.rectangle(begin.x - shiftsX, begin.y + i * mm.category / 4, leftSize, leftSize), mm2);
                    connectLeftRight(mm2, begin.x - shiftsX, begin.y + i * mm.category / 4, mm, begin.x, begin.y);
                }
            }
            if(rightSize >= 4 && !mm.rightDoors.isEmpty())
            {
                mm2 = rng.getRandomElement(modules.get(rightSize));
                if (mm2 == null) continue;
                if(mm2.leftDoors.isEmpty())
                {
                    if(!mm2.topDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else if(!mm2.rightDoors.isEmpty())
                        mm2 = mm2.flip(true, false);
                    else if(!mm2.bottomDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    for (int px = 0; px < mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x + mm.category], begin.y + i * mm.category / 4, mm2.max.y);
                    }
                    layout.put(CoordPacker.rectangle(begin.x + mm.category, begin.y + i * mm.category / 4, rightSize, rightSize), mm2);
                    connectLeftRight(mm, begin.x, begin.y, mm2, begin.x + mm.category, begin.y + i * mm.category / 4);
                }
            }
            if(topSize >= 4 && !mm.topDoors.isEmpty())
            {
                mm2 = rng.getRandomElement(modules.get(topSize));
                if (mm2 == null) continue;
                if(mm2.bottomDoors.isEmpty())
                {
                    if(!mm2.leftDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else if(!mm2.topDoors.isEmpty())
                        mm2 = mm2.flip(false, true);
                    else if(!mm2.rightDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    for (int px = 0; px < mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x - shiftsX + i * mm.category / 4], begin.y, mm2.max.y);
                    }
                    layout.put(CoordPacker.rectangle(begin.x - shiftsX + i * mm.category / 4, begin.y, topSize, topSize), mm2);
                    connectTopBottom(mm2, begin.x - shiftsX + i * mm.category / 4, begin.y, mm, begin.x, begin.y);
                }
            }
            if(topSize >= 4 && !mm.bottomDoors.isEmpty())
            {
                mm2 = rng.getRandomElement(modules.get(bottomSize));
                if (mm2 == null) continue;
                if(mm2.topDoors.isEmpty())
                {
                    if(!mm2.rightDoors.isEmpty())
                        mm2 = mm2.rotate(1);
                    else if(!mm2.topDoors.isEmpty())
                        mm2 = mm2.flip(false, true);
                    else if(!mm2.leftDoors.isEmpty())
                        mm2 = mm2.rotate(3);
                    else continue;
                }
                for (int i = 0; i < 4; i++) {
                    for (int px = 0; px < mm2.max.x; px++) {
                        System.arraycopy(mm2.map[px], 0, map[px + begin.x - shiftsX + i * mm.category / 4], begin.y, mm2.max.y);
                    }
                    layout.put(CoordPacker.rectangle(begin.x - shiftsX + i * mm.category / 4, begin.y, bottomSize, bottomSize), mm2);
                    connectTopBottom(mm, begin.x, begin.y, mm2, begin.x - shiftsX + i * mm.category / 4, begin.y);
                }
            }
        }
        return map;
    }

    /**
     * Change the underlying char[][]; only affects the toString method, and of course getMap
     * @param map a char[][], probably produced by an earlier call to this class and then modified.
     */
    public void setMap(char[][] map) {
        this.map = map;
        if(map == null)
        {
            width = 0;
            height = 0;
            return;
        }
        width = map.length;
        if(width > 0)
            height = map[0].length;
    }

    /**
     * Height of the map in cells.
     * @return Height of the map in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Width of the map in cells.
     * @return Width of the map in cells.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the environment int 2D array for use with classes like RoomFinder.
     * @return the environment int 2D array
     */
    public int[][] getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment int 2D array.
     * @param environment a 2D array of int, where each int corresponds to a constant in MixedGenerator.
     */
    public void setEnvironment(int[][] environment) {
        this.environment = environment;
    }

    private void connectLeftRight(MapModule left, int leftX, int leftY, MapModule right, int rightX, int rightY)
    {
        if(left.rightDoors == null || left.rightDoors.isEmpty()
                || right.leftDoors == null || right.leftDoors.isEmpty())
            return;
        List<Coord> line = new ArrayList<>(1), temp;
        int best = 1024;
        Coord tl;
        for(Coord l : left.rightDoors)
        {
            tl = l.translate(leftX, leftY);
            for(Coord r : right.leftDoors)
            {
                temp = OrthoLine.line(tl, r.translate(rightX, rightY));
                if(temp.size() < best)
                {
                    line = temp;
                    best = line.size();
                }
            }
        }
        temp = new ArrayList<>(line.size());
        for(Coord c : line)
        {
            if(map[c.x][c.y] == '#')
            {
                map[c.x][c.y] = '.';
                temp.add(c);
            }
        }
        layout.put(CoordPacker.packSeveral(temp), null);

    }
    private void connectTopBottom(MapModule top, int topX, int topY, MapModule bottom, int bottomX, int bottomY)
    {
        if(top.bottomDoors == null || top.bottomDoors.isEmpty()
                || bottom.topDoors == null || bottom.topDoors.isEmpty())
            return;
        List<Coord> line = new ArrayList<>(1), temp;
        int best = 1024;
        Coord tt;
        for(Coord t : top.bottomDoors)
        {
            tt = t.translate(topX, topY);
            for(Coord b : bottom.topDoors)
            {
                temp = OrthoLine.line(tt, b.translate(bottomX, bottomY));
                if(temp.size() < best)
                {
                    line = temp;
                    best = line.size();
                }
            }
        }
        temp = new ArrayList<>(line.size());
        for(Coord c : line)
        {
            if(map[c.x][c.y] == '#')
            {
                map[c.x][c.y] = '.';
                temp.add(c);
            }
        }
        layout.put(CoordPacker.packSeveral(temp), null);
    }
}
