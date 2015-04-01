package squidpony;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Point3D;
import squidpony.squidmath.RNG;

/**
 * Provides utilities for working with colors as well as caching operations for
 * color creation.
 *
 * All returned SColor objects are cached so multiple requests for the same
 * SColor will not create duplicate long term objects.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SColorFactory {

    private static final TreeMap<String, SColor> nameLookup = new TreeMap<>();
    private static final TreeMap<Integer, SColor> valueLookup = new TreeMap<>();
    private static RNG rng = new RNG();
    private static Map<Integer, SColor> colorBag = new HashMap<>();
    private static Map<String, ArrayList<SColor>> pallets = new HashMap<>();
    private static int floor = 1;//what multiple to floor rgb values to in order to reduce total colors

    /**
     * Prevents any instances from being created.
     */
    private SColorFactory() {
    }

    /**
     * Returns the SColor Constant who's name is the one provided. If one cannot
     * be found then null is returned.
     *
     * This method constructs a list of the SColor constants the first time it
     * is called.
     *
     * @param s
     * @return
     */
    public static SColor colorForName(String s) {
        if (nameLookup.isEmpty()) {
            for (SColor sc : SColor.FULL_PALLET) {
                nameLookup.put(sc.getName(), sc);
            }
        }

        return nameLookup.get(s);
    }

    /**
     * Returns the SColor who's value matches the one passed in. If no SColor
     * Constant matches that value then a cached or new SColor is returned that
     * matches the provided value.
     *
     * This method constructs a list of the SColor constants the first time it
     * is called.
     *
     * @param rgb
     * @return
     */
    public static SColor colorForValue(int rgb) {
        if (valueLookup.isEmpty()) {
            for (SColor sc : SColor.FULL_PALLET) {
                valueLookup.put(sc.getRGB(), sc);
            }
        }

        return valueLookup.containsKey(rgb) ? valueLookup.get(rgb) : asSColor(rgb);
    }

    /**
     * Returns the number of SColor objects currently cached.
     *
     * @return
     */
    public static int quantityCached() {
        return colorBag.size();
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
    private static int blend(int a, int b, double coef) {
        coef = Math.min(1, coef);
        coef = Math.max(0, coef);
        return (int) (a + (b - a) * coef);
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
    public static SColor blend(SColor color1, SColor color2, double coef) {
        return asSColor(blend(color1.getAlpha(), color2.getAlpha(), coef),
                blend(color1.getRed(), color2.getRed(), coef),
                blend(color1.getGreen(), color2.getGreen(), coef),
                blend(color1.getBlue(), color2.getBlue(), coef));
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
    public static SColor randomBlend(SColor color1, SColor color2, double min, double max) {
        return blend(color1, color2, rng.between(min, max));
    }

    /**
     * Adds the two colors together.
     *
     * @param color1
     * @param color2
     * @return
     */
    public static SColor add(SColor color1, SColor color2) {
        return asSColor(color1.getAlpha() + color2.getAlpha(), color1.getRed() + color2.getRed(), color1.getGreen() + color2.getGreen(), color1.getBlue() + color2.getBlue());
    }

    /**
     * Uses the second color as a light source, meaning that each of the red,
     * green, and blue values of the first color are multiplied by the lighting
     * color's percentage of full value (255).
     *
     * @param color
     * @param light
     * @return
     */
    public static SColor lightWith(SColor color, SColor light) {
        return asSColor((int) (color.getAlpha() * light.getAlpha() / 255f), (int) (color.getRed() * light.getRed() / 255f), (int) (color.getGreen() * light.getGreen() / 255f), (int) (color.getBlue() * light.getBlue() / 255f));
    }

    /**
     * Clears the backing cache.
     *
     * Should only be used if an extreme number of colors are being created and
     * then not reused, such as when blending different colors in different
     * areas that will not be revisited.
     */
    public static void emptyCache() {
        colorBag = new HashMap<>();
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
     * @param value
     */
    public static void setFloor(int value) {
        floor = Math.max(1, value);
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
    public static SColor asSColor(int argb) {
        int working = argb;
        if (floor != 1) {//need to convert to floored values
            int a = (argb >> 24) & 0xff;
            a -= a % floor;
            int r = (argb >> 16) & 0xff;
            r -= r % floor;
            int g = (argb >> 8) & 0xff;
            g -= g % floor;
            int b = argb & 0xff;
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
     * Returns an SColor that is opaque.
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static SColor asSColor(int r, int g, int b) {
        return asSColor(255, r, g, b);
    }

    /**
     * Returns an SColor with the given values, with those values clamped
     * between 0 and 255.
     *
     * @param a
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static SColor asSColor(int a, int r, int g, int b) {
        a = Math.min(a, 255);
        a = Math.max(a, 0);
        r = Math.min(r, 255);
        r = Math.max(r, 0);
        g = Math.min(g, 255);
        g = Math.max(g, 0);
        b = Math.min(b, 255);
        b = Math.max(b, 0);
        return asSColor(a * 256 * 256 * 256 + r * 256 * 256 + g * 256 + b);
    }

    /**
     * Returns an SColor representation of the provided Color. If there is a
     * named SColor constant that matches the value, then that constant is
     * returned.
     *
     * @param color
     * @return
     */
    public static SColor asSColor(Color color) {
        return colorForValue(color.getRGB());
    }

    /**
     * Returns an SColor that is a slightly dimmer version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor dim(SColor color) {
        return blend(color, SColor.BLACK, 0.1);
    }

    /**
     * Returns an SColor that is a somewhat dimmer version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor dimmer(SColor color) {
        return blend(color, SColor.BLACK, 0.3);
    }

    /**
     * Returns an SColor that is a lot darker version of the provided color.
     *
     * @param color
     * @return
     */
    public static SColor dimmest(SColor color) {
        return blend(color, SColor.BLACK, 0.7);
    }

    /**
     * Returns an SColor that is a slightly lighter version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor light(SColor color) {
        return blend(color, SColor.WHITE, 0.1);
    }

    /**
     * Returns an SColor that is a somewhat lighter version of the provided
     * color.
     *
     * @param color
     * @return
     */
    public static SColor lighter(SColor color) {
        return blend(color, SColor.WHITE, 0.3);
    }

    /**
     * Returns an SColor that is a lot lighter version of the provided color.
     *
     * @param color
     * @return
     */
    public static SColor lightest(SColor color) {
        return blend(color, SColor.WHITE, 0.6);
    }

    /**
     * Returns an SColor that is the fully desaturated (greyscale) version of
     * the provided color.
     *
     * @param color
     * @return
     */
    public static SColor desaturated(SColor color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
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
    public static SColor desaturate(SColor color, double percent) {
        return blend(color, desaturated(color), percent);
    }

    /**
     * Returns a list of colors starting at the first color and moving to the
     * second color. The end point colors are included in the list.
     *
     * @param color1
     * @param color2
     * @return
     */
    public static ArrayList<SColor> asGradient(SColor color1, SColor color2) {
        String name = palletNamer(color1, color2);
        if (pallets.containsKey(name)) {
            return pallets.get(name);
        }

        //get the gradient
        Queue<Point3D> gradient = Bresenham.line3D(scolorToCoord3D(color1), scolorToCoord3D(color2));
        ArrayList<SColor> ret = new ArrayList<>();
        for (Point3D coord : gradient) {
            ret.add(coord3DToSColor(coord));
        }

        pallets.put(name, ret);
        return ret;
    }

    /**
     * Returns the pallet associate with the provided name, or null if there is
     * no such pallet.
     *
     * @param name
     * @return
     */
    public static ArrayList<SColor> pallet(String name) {
        return pallets.get(name);
    }

    /**
     * Returns the SColor that is the provided percent towards the end of the
     * pallet. Bounds are checked so as long as there is at least one color in
     * the palette, values below 0 will return the first element and values
     * above 1 will return the last element;
     *
     * If there is no pallette keyed to the provided name, null is returned.
     *
     * @param name
     * @param percent
     * @return
     */
    public static SColor fromPallet(String name, float percent) {
        ArrayList<SColor> list = pallets.get(name);
        if (list == null) {
            return null;
        }

        int index = Math.round(list.size() * percent);//find the index that's the given percent into the gradient
        index = Math.min(index, list.size() - 1);
        index = Math.max(index, 0);
        return list.get(index);
    }

    /**
     * Presents a pop-up JColorChooser dialog showing all of the SColor
     * constants along with examples of what the various dimming and lightening
     * methods change them to..
     *
     * @param parent The component which is the parent of this dialog
     * @return the SColor that is chosen in the dialog
     */
    public static SColor showSColorChooser(Component parent) {
        final JColorChooser chooser = new JColorChooser();
        chooser.setChooserPanels(new AbstractColorChooserPanel[]{new SColorChooserPanel()});

        JDialog dialog = JColorChooser.createDialog(parent, "Choose A Color", true, chooser, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pickedColor = asSColor(chooser.getColor().getRGB());
            }
        }, null);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return pickedColor;
    }
    private static SColor pickedColor;//needed for the color chooser

    /**
     * Places the pallet into the cache, along with each of the member colors.
     *
     * @param name
     * @param pallet
     */
    public static void addPallet(String name, ArrayList<SColor> pallet) {
        ArrayList<SColor> temp = new ArrayList<>();

        //make sure all the colors in the pallet are also in the general color cache
        for (SColor sc : pallet) {
            temp.add(asSColor(sc.getRGB()));
        }

        pallets.put(name, temp);
    }

    /**
     * Converts the provided color into a three dimensional coordinate point for
     * use in the Bresenham algorithms.
     *
     * @param color
     * @return
     */
    private static Point3D scolorToCoord3D(SColor color) {
        return new Point3D(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Converts the provided three dimensional coordinate into a color for use
     * in the Bresenham algorithms.
     *
     * @param coord
     * @return
     */
    private static SColor coord3DToSColor(Point3D coord) {
        return asSColor(coord.x, coord.y, coord.z);
    }

    private static String palletNamer(SColor color1, SColor color2) {
        return color1.getName() + " to " + color2.getName();
    }
}
