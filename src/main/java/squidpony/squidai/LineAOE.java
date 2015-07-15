package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Elias;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;

/**
 * Line Area of Effect that affects an slightly expanded (Elias) line plus an optional radius of cells around it, while
 * respecting obstacles in its path and possibly stopping if obstructed. You can specify a seed for the RNG and a
 * fresh RNG will be used for all random expansion; the RNG will reset to the specified seed after each generation so
 * the same LineAOE can be used in different places by just changing the start and end Points and possibly the radius.
 * That said, there is very little random about this, and it's possible the seed doesn't matter. You can specify the
 * RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * This class uses squidpony.squidmath.Elias and squidpony.squidai.DijkstraMap to create its area of effect.
 * Created by Tommy Ettinger on 7/14/2015.
 */
public class LineAOE implements AOE {
    private Point start, end;
    private int radius;
    private char[][] map, obstructions;
    private DijkstraMap dijkstra;

    private long seed;
    public LineAOE(Point start, Point end, int radius)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    public LineAOE(Point start, Point end, int radius, long seed)
    {
        this.seed = seed;
        LightRNG l = new LightRNG(seed);
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    public LineAOE(Point start, Point end, int radius, Radius radiusType)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.dijkstra = new DijkstraMap(new RNG(l));
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND: this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE: this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default: this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    public LineAOE(Point start, Point end, int radius, Radius radiusType, long seed)
    {
        this.seed = seed;
        LightRNG l = new LightRNG(seed);
        this.dijkstra = new DijkstraMap(new RNG(l));
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND: this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE: this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default: this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    private void initDijkstra()
    {
        for (int i = 0; i < obstructions.length; i++) {
            for (int j = 0; j < obstructions[i].length; j++) {
                obstructions[i][j] = map[i][j];
            }
        }
        float[][] unobstructed = Elias.lightMap(start.x, start.y, end.x, end.y);
        List<Point> lit = Elias.getLastPath();
        for (int i = 0; i < obstructions.length; i++) {
            for (int j = 0; j < obstructions[i].length; j++) {
                if(i >= unobstructed.length || j >= unobstructed[i].length
                        || unobstructed[i][j] == 0.0)
                    obstructions[i][j] = '#';
            }
        }

        if(dijkstra.measurement == DijkstraMap.Measurement.MANHATTAN) {
            for (Point p : lit) {
                for (int i = -radius; i <= radius; i++) {
                    for (int j = -radius; j <= radius; j++) {
                        if(Math.abs(i) + Math.abs(j) <= radius &&
                                p.x + i >= 0 && p.x + i < map.length && p.y + j >= 0 && p.y + j < map[0].length)
                        {
                            obstructions[p.x + i][p.y + j] = map[p.x + i][p.y + j];
                        }
                    }
                }
            }
        }
        else
        {
            for (Point p : lit) {
                for (int i = -radius; i <= radius; i++) {
                    for (int j = -radius; j <= radius; j++) {
                        if(p.x + i >= 0 && p.x + i < map.length && p.y + j >= 0 && p.y + j < map[0].length)
                        {
                            obstructions[p.x + i][p.y + j] = map[p.x + i][p.y + j];
                        }
                    }

                }
            }
        }
        dijkstra.initialize(obstructions);
        dijkstra.scan(null);
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        dijkstra.rng.setRandomness(new LightRNG(seed));
    }
    public Radius getRadiusType()
    {
        switch (dijkstra.measurement)
        {
            case EUCLIDEAN: return Radius.CIRCLE;
            case CHEBYSHEV: return Radius.SQUARE;
            default: return Radius.DIAMOND;
        }
    }
    public void setRadiusType(Radius radiusType)
    {
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND: this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE: this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default: this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
    }

    @Override
    public void setMap(char[][] map) {
        this.map = map;
        this.obstructions = new char[map.length][map[0].length];
    }

    @Override
    public HashMap<Point, Double> findArea() {
        initDijkstra();
        dijkstra.rng.setRandomness(new LightRNG(seed));
        return AreaUtils.dijkstraToHashMap(dijkstra);
    }
}
