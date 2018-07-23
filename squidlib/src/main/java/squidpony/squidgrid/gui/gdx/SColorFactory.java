package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Coord3D;
import squidpony.squidmath.RNG;

import java.util.*;

/**
 * Provides utilities for working with colors as well as caching operations for
 * color creation.
 *
 * All returned SColor objects are cached so multiple requests for the same
 * SColor will not create duplicate long term objects.
 *
 * @see SquidColorCenter another technique for caching colors that allows filters.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - responsible for mutilating this class to work on libGDX
 */
public class SColorFactory {

    private final ObjectMap<String, SColor> nameLookup;
    private final IntMap<SColor> valueLookup;
    private RNG rng;
    private IntMap<SColor> colorBag;
    private ObjectMap<String, ArrayList<SColor>> palettes;
    private int floor = 1;//what multiple to floor rgb values to in order to reduce total colors

    /**
     * Constructs a new SColorFactory with an empty cache.
     * <br>
     * Implementation note: SColorFactory initially used static methods and was not intended to
     * be constructed by users, but for compatibility with Android (where static instances may
     * linger into another run of an application), it was reworked to require an object to be
     * constructed and for that object to contain the cache, rather than a static instance. Code
     * written for earlier versions of SquidLib may use static methods on SColorFactory; most
     * code written in SquidLib 3.0.0 beta 2 and newer prefers the IColorCenter implementations
     * such as SquidColorCenter when SColor features such as color names aren't required. Since
     * beta 4, SquidColorCenter implements most of SColorFactory's API, and also supports an
     * IFilter for automatic changes to requested colors.
     */
    public SColorFactory() {

        nameLookup = new ObjectMap<>();
        valueLookup = new IntMap<>();
        rng = DefaultResources.getGuiRandom();
        colorBag = new IntMap<>();
        palettes = new ObjectMap<>();
        floor = 1;

    }

    /**
     * DEPRECATED: You should use the Colors class from libGDX instead; it has all named SColors registered, though
     * calling its {@link com.badlogic.gdx.graphics.Colors#get(String)} method returns a Color, not an SColor.
     * Since Colors has a map registering all named SColor objects (and some Color objects) already, it is rather
     * wasteful to store another map of all SColors for this method (and only this method) to use.
     * <br>
     * Returns the SColor Constant whose name is the one provided. If one cannot be found then null is returned.
     * <br>
     * This method constructs a list of the SColor constants the first time it is called.
     *
     * @param s the name
     * @return  the SColor by name s
     * @deprecated You should use the Colors class from libGDX instead; it has all named SColors registered
     */
    @Deprecated
    public SColor colorForName(String s) {
        if (nameLookup.size == 0) {
            for (SColor sc : SColor.FULL_PALETTE) {
                nameLookup.put(sc.getName(), sc);
            }
        }

        return nameLookup.get(s);
    }

    /**
     * Returns the SColor whose value matches the one passed in. If no SColor
     * constant matches that value, then a cached or new SColor is returned that
     * matches the provided value.
     *
     * This method constructs a list of the SColor constants the first time it
     * is called.
     *
     * @param argb an int encoding 256 * 256 * 256 * alpha + 256 * 256 * red + 256 * green + blue
     * @return the SColor with value argb
     */
    public SColor colorForValue(int argb) {
        if (valueLookup.size == 0) {
            int len = SColor.FULL_PALETTE.length;
            SColor sc;
            for (int i = 0; i < len; i++) {
                sc = SColor.FULL_PALETTE[i];
                valueLookup.put(Color.argb8888(sc), sc);
            }
        }

        return valueLookup.containsKey(argb) ? valueLookup.get(argb) : asSColor(argb);
    }

    /**
     * Returns the number of SColor objects currently cached.
     *
     * @return
     */
    public int quantityCached() {
        return colorBag.size;
    }

    /**
     * Utility method to blend the two colors by the amount passed in as the
     * coefficient.
     *
     * @param a
     * @param b
     * @param coef
     * @return
     */
    @SuppressWarnings("unused")
	private int blend(int a, int b, double coef) {
        coef = MathUtils.clamp(coef, 0, 1);
        return (int) (a + (b - a) * coef);
    }
    /**
     * Utility method to blend the two colors by the amount passed in as the
     * coefficient.
     *
     * @param a
     * @param b
     * @param coef
     * @return
     */
    private float blend(float a, float b, double coef) {
        float cf = MathUtils.clamp((float)coef, 0, 1);
        return (a + (b - a) * cf);
    }

    /**
     * Returns an SColor that is the given distance from the first color to the
     * second color.
     *
     * @param color1 The first color
     * @param color2 The second color
     * @param coef The percent towards the second color, as 0.0 to 1.0
     * @return
     */
    public SColor blend(SColor color1, SColor color2, double coef) {
        return asSColor(blend(color1.a, color2.a, coef),
                blend(color1.r, color2.r, coef),
                blend(color1.g, color2.g, coef),
                blend(color1.b, color2.b, coef));
    }

    /**
     * Returns an SColor that is randomly chosen from the color line between the
     * two provided colors from the two provided points.
     *
     * @param color1
     * @param color2
     * @param min The minimum percent towards the second color, as 0.0 to 1.0
     * @param max The maximum percent towards the second color, as 0.0 to 1.0
     * @return
     */
    public SColor randomBlend(SColor color1, SColor color2, double min, double max) {
        return blend(color1, color2, rng.between(min, max));
    }

    /**
     * Adds the two colors together.
     *
     * @param color1
     * @param color2
     * @return
     */
    public SColor add(SColor color1, SColor color2) {
        return asSColor(color1.a + color2.a, color1.r + color2.r, color1.g + color2.g, color1.b + color2.b);
    }

    /**
     * Uses the second color as a light source, meaning that each of the red,
     * green, and blue values of the first color are multiplied by the lighting
     * color's percentage of full value (1.0).
     *
     * @param color
     * @param light
     * @return
     */
    public SColor lightWith(SColor color, SColor light) {
        return asSColor((color.a * light.a), (color.r * light.r), (color.g * light.g), (color.b * light.b));
    }

    /**
     * Clears the backing cache.
     *
     * Should only be used if an extreme number of colors are being created and
     * then not reused, such as when blending different colors in different
     * areas that will not be revisited.
     */
    public void emptyCache() {
        colorBag.clear();
    }

    /**
     * Sets the value at which each of the red, green, and blue values will be
     * set to the nearest lower multiple of.
     *
     * For example, a floor value of 5 would mean that each of those values
     * would be considered the nearest lower multiple of 5 when building the
     * colors.
     *
     * If the value passed in is less than 1, then the flooring value is set at
     * 1.
     *
     * @param value used to determine the precision of rounding
     */
    public void setFloor(int value) {
        floor = Math.max(1, value);
    }

    /**
     * Gets the value at which each of the red, green, and blue values will be
     * set to the nearest lower multiple of.
     * <br>
     * For example, a floor value of 5 would mean that each of those values
     * would be considered the nearest lower multiple of 5 when building the
     * colors.
     *
     * @return the current floor value as a long, which defaults to 1.
     */
    public long getFloor() {
        return floor;
    }

    /**
     * Returns the cached color that matches the desired rgb value.
     *
     * If the color is not already in the cache, it is created and added to the
     * cache.
     *
     * This method does not check to see if the value is already available as a
     * SColor constant. If such functionality is desired then please use
     * colorForValue(int rgb) instead.
     *
     * @param argb
     * @return
     */
    public SColor asSColor(int argb) {
        int working = argb;
        if (floor != 1) {//need to convert to floored values
            int a = (argb >>> 24) & 0xff;
            a -= a % floor;
            int r = (argb >> 16) & 0xff;
            r -= r % floor;
            int g = (argb >> 8) & 0xff;
            g -= g % floor;
            int b = (argb) & 0xff;
            b -= b % floor;

            //put back together
            working = ((a & 0xFF) << 24)
                    | ((r & 0xFF) << 16)
                    | ((g & 0xFF) << 8)
                    | (b & 0xFF);
        }

        if (colorBag.containsKey(working)) {
            return colorBag.get(working);
        } else {
            SColor color = new SColor(working);
            colorBag.put(working, color);
            return color;
        }
    }
    /**
     * Returns the cached color that matches the desired rgb value.
     *
     * If the color is not already in the cache, it is created and added to the
     * cache.
     *
     * This method does not check to see if the value is already available as a
     * SColor constant. If such functionality is desired then please use
     * colorForValue(int rgb) instead.
     *
     * @param a alpha, between 0.0 and 1.0 inclusive
     * @param r red, between 0.0 and 1.0 inclusive
     * @param g green, between 0.0 and 1.0 inclusive
     * @param b blue, between 0.0 and 1.0 inclusive
     * @return an SColor with an unspecified name and the given a, r, g, and b values
     */
    public SColor asSColor(float a, float r, float g, float b) {
        int working = 0;
        int aa = MathUtils.round(255 * a);
        aa -= aa % floor;
        int rr = MathUtils.round(255 * r);
        rr -= rr % floor;
        int gg = MathUtils.round(255 * g);
        gg -= gg % floor;
        int bb = MathUtils.round(255 * b);
        bb -= bb % floor;

        //put back together
        working = ((aa & 0xFF) << 24)
                | ((rr & 0xFF) << 16)
                | ((gg & 0xFF) << 8)
                | (bb & 0xFF);


        if (colorBag.containsKey(working)) {
            return colorBag.get(working);
        } else {
            SColor color = new SColor(working);
            colorBag.put(working, color);
            return color;
        }
    }

    /**
     * Returns an SColor that is opaque.
     *
     * @param r red, between 0 and 255 inclusive
     * @param g green, between 0 and 255 inclusive
     * @param b blue, between 0 and 255 inclusive
     * @return an SColor with an unspecified name, full (opaque) alpha, and the given r, g, and b values
     */
    public SColor asSColor(int r, int g, int b) {
        return asSColor(255, r, g, b);
    }

    /**
     * Returns an SColor with the given values, with those values clamped
     * between 0 and 255.
     *
     * @param a alpha, between 0 and 255 inclusive
     * @param r red, between 0 and 255 inclusive
     * @param g green, between 0 and 255 inclusive
     * @param b blue, between 0 and 255 inclusive
     * @return an SColor with an unspecified name and the given a, r, g, and b values
     */
    public SColor asSColor(int a, int r, int g, int b) {
        a = Math.max(Math.min(a, 255), 0);
        r = Math.max(Math.min(r, 255), 0);
        g = Math.max(Math.min(g, 255), 0);
        b = Math.max(Math.min(b, 255), 0);
        return asSColor((a << 24) | (r << 16) | (g << 8) | b);
    }

    /**
     * Returns an SColor representation of the provided Color. If there is a
     * named SColor constant that matches the value, then that constant is
     * returned.
     *
     * @param color
     * @return
     */
    public SColor asSColor(Color color) {
        return colorForValue(Color.rgba8888(color.a, color.r, color.g, color.b));
    }

    /**
     * Returns an SColor that is a slightly dimmer version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public SColor dim(SColor color) {
        return blend(color, SColor.BLACK, 0.1);
    }

    /**
     * Returns an SColor that is a somewhat dimmer version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public SColor dimmer(SColor color) {
        return blend(color, SColor.BLACK, 0.3);
    }

    /**
     * Returns an SColor that is a lot darker version of the provided color.
     *
     * @param color
     * @return
     */
    public SColor dimmest(SColor color) {
        return blend(color, SColor.BLACK, 0.7);
    }

    /**
     * Returns an SColor that is a slightly lighter version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public SColor light(SColor color) {
        return blend(color, SColor.WHITE, 0.1);
    }

    /**
     * Returns an SColor that is a somewhat lighter version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public SColor lighter(SColor color) {
        return blend(color, SColor.WHITE, 0.3);
    }

    /**
     * Returns an SColor that is a lot lighter version of the provided color.
     *
     * @param color
     * @return
     */
    public SColor lightest(SColor color) {
        return blend(color, SColor.WHITE, 0.6);
    }

    /**
     * Returns an SColor that is the fully desaturated (greyscale) version of
     * the provided color.
     *
     * @param color
     * @return
     */
    public SColor desaturated(SColor color) {
        int r = MathUtils.round(color.r * 255);
        int g = MathUtils.round(color.g * 255);
        int b = MathUtils.round(color.b * 255);

        int average = (int) (r * 0.299 + g * 0.587 + b * 0.114);

        return asSColor(average, average, average);
    }

    /**
     * Returns an SColor that is the version of the provided color desaturated
     * the given amount.
     *
     * @param color
     * @param percent The percent to desaturate, from 0.0 for none to 1.0 for
     * fully desaturated
     * @return
     */
    public SColor desaturate(SColor color, double percent) {
        return blend(color, desaturated(color), percent);
    }

    /**
     * Returns a list of colors starting at the first color and moving to the
     * second color. The end point colors are included in the list.
     *
     * @param color1 starting color
     * @param color2 ending color
     * @return an ArrayList of SColor going from color1 to color2; the length should be no greater than 256.
     */
    public ArrayList<SColor> asGradient(SColor color1, SColor color2) {
        String name = paletteNamer(color1, color2);
        if (palettes.containsKey(name)) {
            return palettes.get(name);
        }

        //get the gradient
        Queue<Coord3D> gradient = Bresenham.line3D(scolorToCoord3D(color1), scolorToCoord3D(color2));
        ArrayList<SColor> ret = new ArrayList<>();
        for (Coord3D coord : gradient) {
            ret.add(coord3DToSColor(coord));
        }

        palettes.put(name, ret);
        return ret;
    }

    /**
     * Returns the palette associate with the provided name, or null if there is
     * no such palette.
     *
     * @param name
     * @return
     */
    public ArrayList<SColor> palette(String name) {
        return palettes.get(name);
    }
    /**
     * Returns the palette associate with the provided name, or null if there is
     * no such palette.
     *
     * @param name
     * @return
     * @deprecated Prefer palette over this misspelled version.
     */
    public ArrayList<SColor> pallet(String name) {
        return palettes.get(name);
    }

    /**
     * Returns the SColor that is the provided percent towards the end of the
     * palette. Bounds are checked so as long as there is at least one color in
     * the palette, values below 0 will return the first element and values
     * above 1 will return the last element;
     *
     * If there is no palette keyed to the provided name, null is returned.
     *
     * @param name
     * @param percent
     * @return
     */
    public SColor fromPalette(String name, float percent) {
        ArrayList<SColor> list = palettes.get(name);
        if (list == null) {
            return null;
        }

        int index = Math.round(list.size() * percent);//find the index that's the given percent into the gradient
        index = Math.min(index, list.size() - 1);
        index = Math.max(index, 0);
        return list.get(index);
    }
    /**
     * Returns the SColor that is the provided percent towards the end of the
     * palette. Bounds are checked so as long as there is at least one color in
     * the palette, values below 0 will return the first element and values
     * above 1 will return the last element;
     *
     * If there is no palette keyed to the provided name, null is returned.
     *
     * @param name
     * @param percent
     * @return
     *
     * @deprecated Prefer fromPalette over this misspelled version; they are equivalent.
     */
    public SColor fromPallet(String name, float percent) {
        ArrayList<SColor> list = palettes.get(name);
        if (list == null) {
            return null;
        }

        int index = Math.round(list.size() * percent);//find the index that's the given percent into the gradient
        index = Math.min(index, list.size() - 1);
        index = Math.max(index, 0);
        return list.get(index);
    }

    /**
     * Places the palette into the cache, along with each of the member colors.
     *
     * @param name
     * @param palette
     * 
     * @deprecated Prefer addPalette over this misspelled version; they are equivalent.
     */
    public void addPallet(String name, ArrayList<SColor> palette) {
        addPalette(name, palette);
    }

    /**
     * Places the palette into the cache, along with each of the member colors.
     *
     * @param name
     * @param palette
     */
    public void addPalette(String name, ArrayList<SColor> palette) {
        ArrayList<SColor> temp = new ArrayList<>();

        //make sure all the colors in the palette are also in the general color cache
        for (SColor sc : palette) {
            temp.add(asSColor(sc.a, sc.r, sc.g, sc.b));
        }

        palettes.put(name, temp);
    }

    /**
     * Converts the provided color into a three dimensional coordinate point for
     * use in the Bresenham algorithms.
     *
     * @param color
     * @return
     */
    private Coord3D scolorToCoord3D(SColor color) {
        return new Coord3D(MathUtils.floor(color.r * 255), MathUtils.floor(color.g * 255), MathUtils.floor(color.b * 255));
    }

    /**
     * Converts the provided three dimensional coordinate into a color for use
     * in the Bresenham algorithms.
     *
     * @param coord
     * @return
     */
    private SColor coord3DToSColor(Coord3D coord) {
        return asSColor(coord.x, coord.y, coord.z);
    }

    private String paletteNamer(SColor color1, SColor color2) {
        return color1.getName() + " to " + color2.getName();
    }

}
