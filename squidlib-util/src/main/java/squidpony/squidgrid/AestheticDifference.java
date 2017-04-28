package squidpony.squidgrid;

/**
 * Used by DetailedMimic to allow different kinds of detail, including differentiating color or map features.
 *
 * Created by Tommy Ettinger on 6/9/2016.
 */
public interface AestheticDifference {
    /**
     * Finds the difference between two int values, which implementations of this interface may treat as colors, as
     * kinds of map feature, as item placement factors, or various other ways. The difference should be, on average,
     * about 1.0 for inputs that have a decent range of values (shades of color as well as changes in hue, etc.).
     * <br>
     * The original implementation looked something like this, assuming the SquidColorCenter (from the display module,
     * not present in squidlib-util) was instantiated earlier for better efficiency:
     * <pre>
     *     SquidColorCenter scc = new SquidColorCenter();
     *     Color c1 = scc.get(a), c2 = scc.get(b);
     *     return ((c1.r - c2.r) * (c1.r - c2.r) + (c1.g - c2.g) * (c1.g - c2.g) + (c1.b - c2.b) * (c1.b - c2.b)) / 65536.0;
     * </pre>
     * @param a an int that may be interpreted in different ways by different implementations
     * @param b another int that may be interpreted in different ways by different implementations
     * @return the difference between a and b, ideally averaging about 1.0 for most inputs
     */
    double difference(int a, int b);

    AestheticDifference rgba8888 = new AestheticDifference() {
        @Override
        public double difference(int a, int b) {
            int aa = a >>> 24,
                    bb = b >>> 24,
                    t = aa - bb,
                    sum = t * t;
            aa = (a >>> 16) & 0xff;
            bb = (b >>> 16) & 0xff;
            t = aa - bb;
            sum += t * t;
            aa = (a >>> 8) & 0xff;
            bb = (b >>> 8) & 0xff;
            t = aa - bb;
            return (sum + t * t) / 65536.0;
        }
    };
}
