package squidpony.examples;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.squidgrid.FOV;
import static squidpony.squidgrid.FOV.RIPPLE;
import static squidpony.squidgrid.FOV.RIPPLE_LOOSE;
import static squidpony.squidgrid.FOV.RIPPLE_TIGHT;
import static squidpony.squidgrid.FOV.RIPPLE_VERY_LOOSE;
import static squidpony.squidgrid.FOV.SHADOW;
import squidpony.SColor;
import squidpony.SColorFactory;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidmath.RNG;

/**
 * This class shows off various FOV algorithms in an animated manner. It's meant
 * primarily for internal testing so is a bit rough.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOVAnimated {

    private static final RNG rng = new RNG();
    private static final int cellSize = 15;
    private static final int viewDistance = 20;
    private int width, height;
    private SquidPanel back, front;

    private double lightMap[][], map[][];
    private boolean indirect[][];//marks indirect lighting for Ripple FOV
    private int type = FOV.RIPPLE_TIGHT;
    private double radius, decay;
    private int startx, starty;
    private int rippleNeighbors;
    private Radius radiusStrategy;
    private TestMap testMap = new TestMap();

    public static void main(String... args) {
        new FOVAnimated().go();
    }

    private void go() {
        width = testMap.width();
        height = testMap.height();

        JFrame frame = new JFrame("FOV In Action");
        frame.getContentPane().setBackground(SColor.BLACK);
        TextCellFactory factory = new TextCellFactory().font(new Font("Arial", Font.BOLD, 26)).width(cellSize).height(cellSize);
        back = new SquidPanel(width, height, factory, null);
        front = new SquidPanel(width, height, factory, null);

        JLayeredPane layers = new JLayeredPane();
        layers.setLayer(back, JLayeredPane.DEFAULT_LAYER);
        layers.setLayer(front, JLayeredPane.PALETTE_LAYER);
        layers.add(back);
        layers.add(front);
        layers.setPreferredSize(back.getPreferredSize());
        layers.setSize(back.getPreferredSize());

        frame.add(layers);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        SColorFactory.addPallet("colors", SColorFactory.asGradient(SColor.ALICE_BLUE, SColor.BRIGHT_PINK));

        back.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    back.erase();
                    back.refresh();
                } else {
                    calculate(e.getX() / cellSize, e.getY() / cellSize);
                }
            }

        });

        front.erase();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                front.put(x, y, testMap.symbol(x, y), testMap.color(x, y));
            }
        }
        front.refresh();

        map = testMap.resistances();
//        calculate();
    }

    private void calculate(final int x, final int y) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                back.erase();
                back.refresh();
//                startx = rng.between(width / 5, (width * 4) / 5);
//                starty = rng.between(height / 5, (height * 4) / 5);
                startx = x;
                starty = y;

                mark(startx, starty, SColor.CRIMSON);
                calculateFOV(map, startx, starty, viewDistance, Radius.CIRCLE);
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void mark(int x, int y, double strength) {
        mark(x, y, SColorFactory.desaturate(SColor.LIME, 1 - strength));
    }

    private void mark(int x, int y, SColor color) {
        mark(x, y, color, false);
    }

    private void mark(int x, int y, SColor color, boolean temporary) {
        BufferedImage old = back.getImage(x, y);
        back.put(x, y, color);
        back.refresh();
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException ex) {
//        }
        if (temporary) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException ex) {
//            }
            back.put(x, y, old);
            back.refresh();
        }
    }

    /**
     * Checks to see if the location is considered even partially lit.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isLit(int x, int y) {
        return lightLevel(x, y) > 0;
    }

    /**
     * Returns the light value at the given location.
     *
     * @param x
     * @param y
     * @return
     */
    public double lightLevel(int x, int y) {
        if (lightMap == null) {
            return 0;
        } else {
            return lightMap[x][y];
        }
    }

    /**
     * Returns the initial resistance value at the given point. If no calculation has yet been run, it returns 0.
     *
     * @param x
     * @param y
     * @return
     */
    public double resistance(int x, int y) {
        if (map == null) {
            return 0;
        } else {
            return map[x][y];
        }
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y coordinates. Returns a light map where the
     * values represent a percentage of fully lit.
     *
     * The starting point for the calculation is considered to be at the center of the origin cell. Radius
     * determinations based on Euclidian calculations. The light will be treated as having infinite possible radius.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty) {
        return calculateFOV(resistanceMap, startx, starty, Integer.MAX_VALUE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y coordinates. Returns a light map where the
     * values represent a percentage of fully lit.
     *
     * The starting point for the calculation is considered to be at the center of the origin cell. Radius
     * determinations based on Euclidian calculations.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty, int radius) {
        return calculateFOV(resistanceMap, startx, starty, radius, Radius.CIRCLE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y coordinates. Returns a light map where the
     * values represent a percentage of fully lit.
     *
     * The starting point for the calculation is considered to be at the center of the origin cell. Radius
     * determinations are determined by the provided RadiusStrategy.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusStrategy provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty, double radius, Radius radiusStrategy) {
        this.map = resistanceMap;
        this.startx = startx;
        this.starty = starty;
        this.radius = radius;
        this.radiusStrategy = radiusStrategy;
        decay = 1.0 / radius;

        lightMap = new double[width][height];
        lightMap[startx][starty] = 1;//make the starting space full power

        switch (type) {
            case RIPPLE:
                indirect = new boolean[width][height];
                rippleNeighbors = 2;
                doRippleFOV(startx, starty);
                break;
            case RIPPLE_LOOSE:
                indirect = new boolean[width][height];
                rippleNeighbors = 3;
                doRippleFOV(startx, starty);
                break;
            case RIPPLE_TIGHT:
                indirect = new boolean[width][height];
                rippleNeighbors = 1;
                doRippleFOV(startx, starty);
                break;
            case RIPPLE_VERY_LOOSE:
                indirect = new boolean[width][height];
                rippleNeighbors = 6;
                doRippleFOV(startx, starty);
                break;
            case SHADOW:
                for (Direction d : Direction.DIAGONALS) {
                    shadowCast(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0);
                    shadowCast(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY);
                }
                break;
        }

        return lightMap;
    }

    private void doRippleFOV(int x, int y) {
        Deque<Point> dq = new LinkedList<>();
        dq.offer(new Point(x, y));
        while (!dq.isEmpty()) {
            Point p = dq.pop();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (Direction dir : Direction.OUTWARDS) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }

                double surroundingLight = nearRippleLight(x2, y2);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(new Point(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    private double nearRippleLight(int x, int y) {
        if (x == startx && y == starty) {
            return 1;
        }

//        mark(x, y, SColor.LILAC);

        List<Point> neighbors = new LinkedList<>();
        for (Direction di : Direction.OUTWARDS) {
            int x2 = x + di.deltaX;
            int y2 = y + di.deltaY;
            if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
                neighbors.add(new Point(x2, y2));
            }
        }

        if (neighbors.isEmpty()) {
            return 0;
        }

        while (neighbors.size() > rippleNeighbors) {
            Point p = neighbors.remove(0);
            double dist = radiusStrategy.radius(startx, starty, p.x, p.y);
            double dist2 = 0;
            for (Point p2 : neighbors) {
                dist2 = Math.max(dist2, radiusStrategy.radius(startx, starty, p2.x, p2.y));
            }
            if (dist < dist2) {//not the largest, put it back
                neighbors.add(p);
            }
        }

        double light = 0;
        int lit = 0, indirects = 0;
        for (Point p : neighbors) {
            if (lightMap[p.x][p.y] > 0) {
                lit++;
                if (indirect[p.x][p.y]) {
                    indirects++;
                }
                double dist = radiusStrategy.radius(x, y, p.x, p.y);
                light = Math.max(light, lightMap[p.x][p.y] - dist * decay - map[p.x][p.y]);
                mark(p.x, p.y, SColor.YELLOW, true);
            }
        }

        if (map[x][y] >= 1 || indirects >= lit) {
            indirect[x][y] = true;
//            mark(x, y, SColor.SAFETY_ORANGE);
        } else {
            mark(x, y, light);
        }
        return light;
    }

    private void shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy) {
        double newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < this.width && currentY < this.height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                //check if it's within the lightable area and light if needed
                if (radiusStrategy.radius(deltaX, deltaY) <= radius) {
                    double bright = 1 - decay * radiusStrategy.radius(deltaX, deltaY);
                    lightMap[currentX][currentY] = bright;
                    mark(currentX, currentY, bright);
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }

    private void doSpreadFOV(int x, int y) {
        if (lightMap[x][y] <= 0) {
            return;//no light to spread
        }

        for (int dx = x - 1; dx <= x + 1; dx++) {
            for (int dy = y - 1; dy <= y + 1; dy++) {
                //ensure in bounds
                if (dx < 0 || dx >= width || dy < 0 || dy >= height) {
                    continue;
                }

                double r2 = radiusStrategy.radius(startx, starty, dx, dy);
                if (r2 <= radius) {
                    double surroundingLight = nearSpreadLight(dx, dy);
                    if (lightMap[dx][dy] < surroundingLight) {
                        lightMap[dx][dy] = surroundingLight;
                        mark(dx, dy, surroundingLight);
                        doSpreadFOV(dx, dy);//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    private double nearSpreadLight(int x, int y) {
        Direction dir = Direction.getDirection(startx - x, starty - y);
        int x2 = x + dir.deltaX;
        int y2 = y + dir.deltaY;
        mark(x2, y2, SColor.YELLOW, true);

        if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height) {
            return 0;//no light from off the map
        }

        //find largest emmitted light in direction of source
        double light = Math.max(Math.max(lightMap[x2][y] - map[x2][y],
                lightMap[x][y2] - map[x][y2]),
                lightMap[x2][y2] - map[x2][y2]);

        double distance = radiusStrategy.radius(x, y, x2, y2);
        light -= decay * distance;
        return light;
    }

}
