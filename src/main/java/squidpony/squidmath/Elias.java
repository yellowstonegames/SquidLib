package squidpony.squidmath;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import squidpony.annotation.Beta;

/**
 * Contains methods to draw antialiased lines based on floating point
 * coordinates.
 *
 * Because of the way this line is calculated, endpoints may be swapped and
 * therefore the list may not be in start-to-end order.
 *
 * Based on work by Hugo Elias at
 * http://freespace.virgin.net/hugo.elias/graphics/x_wuline.htm which is in turn
 * base on work by Wu.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Elias {

    static private List<Point> path;
    static private float[][] lightMap;
    static private int width, height;
    static private double threshold = 0.0;

    private Elias() {
    }

    public synchronized static float[][] lightMap(double startx, double starty, double endx, double endy) {
        line(startx, starty, endx, endy);
        return lightMap;
    }

    /**
     * Gets the line between the two points.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    public synchronized static List<Point> line(double startx, double starty, double endx, double endy) {
        path = new LinkedList<>();
        width = (int) (Math.max(startx, endx) + 1);
        height = (int) (Math.max(starty, endy) + 1);
        lightMap = new float[width][height];
        runLine(startx, starty, endx, endy);
        return path;
    }
    /**
     * Gets the line between the two points.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @param brightnessThreshold between 0.0 (default) and 1.0; only Points with higher brightness will be included
     * @return
     */
    public synchronized static List<Point> line(double startx, double starty, double endx, double endy,
                                                double brightnessThreshold) {
        threshold = brightnessThreshold;
        path = new LinkedList<>();
        width = (int) (Math.max(startx, endx) + 1);
        height = (int) (Math.max(starty, endy) + 1);
        lightMap = new float[width][height];
        runLine(startx, starty, endx, endy);
        return path;
    }
    public synchronized static List<Point> line(Point start, Point end) {
        return line(start.x, start.y, end.x, end.y);
    }
    public synchronized static List<Point> line(Point start, Point end, double brightnessThreshold) {
        return line(start.x, start.y, end.x, end.y, brightnessThreshold);
    }

    public synchronized static List<Point> getLastPath()
    {
        return path;
    }

    /**
     * Marks the location as having the visibility given.
     *
     * @param x
     * @param y
     * @param c
     */
    private static void mark(double x, double y, double c) {
        //check bounds overflow from antialiasing
        if (x >= 0 && x < width && y >= 0 && y < height && c > threshold) {
            path.add(new Point((int) x, (int) y));
            lightMap[(int) x][(int) y] = (float) c;
        }
    }

    private static double trunc(double x) {
        if (x < 0) {
            return Math.ceil(x);
        } else {
            return Math.floor(x);
        }
    }

    private static double frac(double x) {
        return x - trunc(x);
    }

    private static double invfrac(double x) {
        return 1 - frac(x);
    }

    private static void runLine(double startx, double starty, double endx, double endy) {
        double x1 = startx, y1 = starty, x2 = endx, y2 = endy;
        double grad, xd, yd, length, xm, ym, xgap, ygap, xend, yend, xf, yf, brightness1, brightness2;
        int x, y, ix1, ix2, iy1, iy2;
        boolean shallow = false;

        xd = x2 - x1;
        yd = y2 - y1;

        if (Math.abs(xd) > Math.abs(yd)) {
            shallow = true;
        }

        if (!shallow) {
            double temp = x1;
            x1 = y1;
            y1 = temp;
            temp = x2;
            x2 = y2;
            y2 = temp;
            xd = x2 - x1;
            yd = y2 - y1;
        }
        if (x1 > x2) {
            double temp = x1;
            x1 = x2;
            x2 = temp;
            temp = y1;
            y1 = y2;
            y2 = temp;
            xd = x2 - x1;
            yd = y2 - y1;
        }

        grad = yd / xd;

        //add the first end point
        xend = trunc(x1 + .5);
        yend = y1 + grad * (xend - x1);

        xgap = invfrac(x1 + .5);

        ix1 = (int) xend;
        iy1 = (int) yend;

        brightness1 = invfrac(yend) * xgap;
        brightness2 = frac(yend) * xgap;

        if (shallow) {
            mark(ix1, iy1, brightness1);
            mark(ix1, iy1 + 1, brightness2);
        } else {
            mark(iy1, ix1, brightness1);
            mark(iy1 + 1, ix1, brightness2);
        }

        yf = yend + grad;

        //add the second end point
        xend = trunc(x2 + .5);
        yend = y2 + grad * (xend - x2);

        xgap = invfrac(x2 - .5);

        ix2 = (int) xend;
        iy2 = (int) yend;

        brightness1 = invfrac(yend) * xgap;
        brightness2 = frac(yend) * xgap;

        if (shallow) {
            mark(ix2, iy2, brightness1);
            mark(ix2, iy2 + 1, brightness2);
        } else {
            mark(iy2, ix2, brightness1);
            mark(iy2 + 1, ix2, brightness2);
        }

        //add the in-between points
        for (x = ix1 + 1; x < ix2; x++) {
            brightness1 = invfrac(yf);
            brightness2 = frac(yf);

            if (shallow) {
                mark(x, (int) yf, brightness1);
                mark(x, (int) yf + 1, brightness2);
            } else {
                mark((int) yf, x, brightness1);
                mark((int) yf + 1, x, brightness2);
            }

            yf += grad;
        }
    }
}
