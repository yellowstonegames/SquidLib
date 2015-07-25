package squidpony.squidai;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An AOE type that has a center and a radius, and uses shadowcasting to create a burst of rays from the center, out to
 * the distance specified by radius. You can specify the RadiusType to Radius.DIAMOND for Manhattan distance,
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * This will produce doubles for its getArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidgrid.FOV to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class BurstAOE implements AOE {
    private FOV fov;
    private Point center;
    private int radius;
    private double[][] map;
    private char[][] dungeon;
    private Radius radiusType;
    public BurstAOE(Point center, int radius, Radius radiusType)
    {
        fov = new FOV(FOV.SHADOW);
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
    }
    private BurstAOE()
    {
        fov = new FOV(FOV.SHADOW);
        center = new Point(1, 1);
        radius = 1;
        radiusType = Radius.DIAMOND;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Radius getRadiusType() {
        return radiusType;
    }

    public void setRadiusType(Radius radiusType) {
        this.radiusType = radiusType;
    }

    @Override
    public void shift(Point aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Point> targets) {
        for (Point p : targets)
        {
            if(radiusType.radius(center.x, center.y, p.x, p.y) <= radius)
                return true;
        }
        return false;
    }

    @Override
    public ArrayList<Point> idealLocations(Set<Point> targets, Set<Point> requiredExclusions) {
        if(targets == null)
            return new ArrayList<Point>();
        if(requiredExclusions == null) requiredExclusions = new LinkedHashSet<Point>();

        int totalTargets = targets.size();
        ArrayList<Point> bestPoints = new ArrayList<Point>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        if(radius == 0)
        {
            bestPoints.addAll(targets);
            return bestPoints;
        }
        Point[] ts = targets.toArray(new Point[targets.size()]);
        Point[] exs = targets.toArray(new Point[requiredExclusions.size()]);
        Point t = exs[0];

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        double[][] tmpfov;
        for (int i = 0; i < exs.length; ++i, t = exs[i]) {

            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = ts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE || radiusType == Radius.CUBE) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE || radiusType == Radius.SPHERE) dmm = DijkstraMap.Measurement.EUCLIDEAN;
        DijkstraMap dm = new DijkstraMap(dungeon, dmm);

        for (int i = 0; i < ts.length; ++i, t = ts[i]) {
            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (tmpfov[x][y] > 0.0) ? dm.physicalMap[x][y] : DijkstraMap.WALL;
                }
            }
            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] != '!') ? dm.gradientMap[x][y] : 99999.0;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }
        double bestQuality = 99999 * ts.length;
        double[][] qualityMap = new double[dungeon.length][dungeon[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0.0;
                for (int i = 0; i < ts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    bestQuality = qualityMap[x][y];
                    bestPoints.clear();
                    bestPoints.add(new Point(x, y));
                }
                else if(qualityMap[x][y] == bestQuality)
                {
                    bestPoints.add(new Point(x, y));
                }
            }
        }

        return bestPoints;
    }
    /*
    @Override
    public ArrayList<ArrayList<Point>> idealLocations(Set<Point> targets, Set<Point> requiredExclusions) {
        int totalTargets = targets.size() + 1;
        int maxEffect = (int)radiusType.volume2D(radius);
        ArrayList<ArrayList<Point>> locs = new ArrayList<ArrayList<Point>>(totalTargets);

        for(int i = 0; i < totalTargets; i++)
        {
            locs.add(new ArrayList<Point>(maxEffect));
        }
        if(totalTargets == 1)
            return locs;

        int ctr = 0;
        if(radius < 1)
        {
            locs.get(totalTargets - 2).addAll(targets);
            return locs;
        }

        boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
        for (int x = 1; x < dungeon.length - 1; x += radius) {
            BY_POINT:
            for (int y = 1; y < dungeon[x].length - 1; y += radius) {
                for(Point ex : requiredExclusions)
                {
                    if(radiusType.radius(x, y, ex.x, ex.y) <= radius)
                        continue BY_POINT;
                }
                ctr = 0;
                for(Point tgt : targets)
                {
                    if(radiusType.radius(x, y, tgt.x, tgt.y) <= radius)
                        ctr++;
                }
                if(ctr > 0)
                    locs.get(totalTargets - ctr).add(new Point(x, y));
            }
        }
        Point it;
        for(int t = 0; t < totalTargets - 1; t++)
        {
            if(locs.get(t).size() > 0) {
                int numPoints = locs.get(t).size();
                for (int i = 0; i < numPoints; i++) {
                    it = locs.get(t).get(i);
                    for (int x = Math.max(1, it.x - radius / 2); x < it.x + (radius + 1) / 2 && x < dungeon.length - 1; x++) {
                        BY_POINT:
                        for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
                        {
                            if(tested[x][y])
                                continue;
                            tested[x][y] = true;

                            for(Point ex : requiredExclusions)
                            {
                                if(radiusType.radius(x, y, ex.x, ex.y) <= radius)
                                    continue BY_POINT;
                            }

                            ctr = 0;
                            for(Point tgt : targets)
                            {
                                if(radiusType.radius(x, y, tgt.x, tgt.y) <= radius)
                                    ctr++;
                            }
                            if(ctr > 0)
                                locs.get(totalTargets - ctr).add(new Point(x, y));
                        }
                    }
                }
            }
        }
        return locs;
    }
*/
    @Override
    public void setMap(char[][] map) {
        this.map = DungeonUtility.generateResistances(map);
        this.dungeon = map;
    }

    @Override
    public HashMap<Point, Double> findArea() {
        return AreaUtils.arrayToHashMap(fov.calculateFOV(map, center.x, center.y, radius, radiusType));
    }
}
