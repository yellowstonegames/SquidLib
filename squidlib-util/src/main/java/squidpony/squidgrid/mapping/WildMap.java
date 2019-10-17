package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;
import squidpony.squidmath.BlueNoise;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.SilkRNG;

import java.util.ArrayList;

/**
 * A finite 2D area map for some kind of wilderness, with specifics handled by subclasses.
 * Regional maps for wilderness areas have very different requirements from mostly-artificial dungeons. This is intended
 * to work alongside {@link WorldMapGenerator} and {@link WorldMapGenerator.DetailedBiomeMapper} to produce blends
 * between biomes where a strict line can't be drawn between, say, woodland and grassland.
 * <br>
 * Using this code mostly involves constructing a subclass, adding kinds of game objects that you want to appear
 * in this type of map to {@link #contentTypes}, and only then calling {@link #generate()}, which is implemented by
 * subclasses only. You can use just about any type for {@code T}, including {@link Object}; all this code uses is the
 * number of items in {@link #contentTypes} to put correct indices into {@link #content}. Because contentTypes is an
 * ArrayList, you can have and are encouraged to have duplicates when an object should appear more often. An index of -1
 * in content indicates nothing of note is present there. There is also a String array of {@link #floorTypes} that is
 * not typically user-set unless you subclass WildMap yourself; it is used to look up the indices in {@link #floors}.
 * The floors are set to reasonable values for the particular subclass, so a forest might have "dirt" and "leaves" among
 * others, while a desert might only have "sand". Again, only the indices matter, so you could change the values in
 * {@link #floorTypes} to match names of textures in a graphical game and make lookup easier, or to a char followed by
 * the name of a color (as in SColor in the display module) for a text-based game.
 * <br>
 * Created by Tommy Ettinger on 10/16/2019.
 */
@Beta
public abstract class WildMap<T> {
    private static final long serialVersionUID = 1L;
    public final int width, height;
    public int seedA, seedB;
    public SilkRNG rng;
    public ArrayList<T> contentTypes;
    public String[] floorTypes;
    public final int[][] content, floors;
    public WildMap(){
        this(128,128);
    }
    public WildMap(int width, int height)
    {
        this(width, height, new SilkRNG());
    }
    public WildMap(int width, int height, int seedA, int seedB)
    {
        this(width, height, new SilkRNG(seedA, seedB));
    }
    public WildMap(int width, int height, SilkRNG rng)
    {
        this.width = width;
        this.height = height;
        this.rng = rng;
        this.seedA = this.rng.stateA;
        this.seedB = this.rng.stateB;
        content = ArrayTools.fill(-1, width, height);
        floors = new int[width][height];
        contentTypes = new ArrayList<>(64);
    }
    
    public abstract void generate();
    
    public static class IceMap<T> extends WildMap<T>{
        public IceMap(){
            this(128,128);
        }
        public IceMap(int width, int height)
        {
            this(width, height, new SilkRNG());
        }
        public IceMap(int width, int height, int seedA, int seedB)
        {
            this(width, height, new SilkRNG(seedA, seedB));
        }
        public IceMap(int width, int height, SilkRNG rng) {
            super(width, height, rng);
            floorTypes = new String[]{"snowy ground", "snowy ground", "icy ground", };
        }
        @Override
        public void generate() {
            ArrayTools.fill(content, -1);
            final int seed = rng.nextInt(), otherSeed = rng.nextInt();
            final int limit = contentTypes.size(), floorLimit = floorTypes.length;
            int b;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if((b = BlueNoise.getSeeded(x, y, seed) + 128) < limit)
                        content[x][y] = b;
                    floors[x][y] = (int)((FastNoise.instance.layered2D(x,  y, otherSeed, 2, 0x1p-5f) * 0.4999f + 0.5f) * floorLimit);
                }
            }
        }
    }
}
