package squidpony.squidai;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;

import java.awt.*;
import java.util.*;

/**
 * An AOE type that has a center and a radius, and uses shadowcasting to create a burst of rays from the center, out to
 * the distance specified by radius. You can specify the RadiusType to Radius.DIAMOND for Manhattan distance,
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidgrid.FOV to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class BurstAOE implements AOE {
    private FOV fov;
    private Point center, origin;
    private int radius;
    private double[][] map;
    private char[][] dungeon;
    private Radius radiusType, limitType;
    private int minRange = 1, maxRange = 1;
    private Radius metric = Radius.SQUARE;
    public BurstAOE(Point center, int radius, Radius radiusType)
    {
        fov = new FOV(FOV.SHADOW);
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
    }
    public BurstAOE(Point center, int radius, Radius radiusType, int minRange, int maxRange)
    {
        fov = new FOV(FOV.SHADOW);
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
        this.minRange = minRange;
        this.maxRange = maxRange;
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

        if (AreaUtils.verifyLimit(limitType, origin, center))
        {
            this.center = center;
        }
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
    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Set<Point> targets, Set<Point> requiredExclusions) {
        if(targets == null)
            return new LinkedHashMap<Point, ArrayList<Point>>();
        if(requiredExclusions == null) requiredExclusions = new LinkedHashSet<Point>();

        //requiredExclusions.remove(origin);
        int totalTargets = targets.size();
        LinkedHashMap<Point, ArrayList<Point>> bestPoints = new LinkedHashMap<Point, ArrayList<Point>>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        if(radius == 0)
        {
            for(Point p : targets)
            {
                ArrayList<Point> ap = new ArrayList<Point>();
                ap.add(p);
                bestPoints.put(p, ap);
            }
            return bestPoints;
        }
        Point[] ts = targets.toArray(new Point[targets.size()]);
        Point[] exs = requiredExclusions.toArray(new Point[requiredExclusions.size()]);
        Point t = exs[0];

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        double[][] tmpfov;
        Point tempPt = new Point(0,0);
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);
            for (int x = 0; x < dungeon.length; x++) {
                tempPt.x = x;
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt.y = y;
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0 || !AreaUtils.verifyLimit(limitType, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = ts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE || radiusType == Radius.CUBE) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE || radiusType == Radius.SPHERE) dmm = DijkstraMap.Measurement.EUCLIDEAN;

        for (int i = 0; i < ts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dmm);

            t = ts[i];
            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0){
                        dist = metric.radius(origin.x, origin.y, x, y);
                        if(dist <= maxRange + radius && dist >= minRange - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][ts[i].x][ts[i].y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 99999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] != '!') ? dm.gradientMap[x][y] : 99999.0;
                }
            }
        }
        double bestQuality = 99999 * ts.length;
        double[][] qualityMap = new double[dungeon.length][dungeon[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0.0;
                long bits = 0;
                for (int i = 0; i < ts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999.0 && i < 63)
                        bits |= 1 << i;
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    ArrayList<Point> ap = new ArrayList<Point>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1 << i)) != 0)
                            ap.add(ts[i]);
                    }

                    if(ap.size() > 0) {
                        bestQuality = qualityMap[x][y];
                        bestPoints.clear();
                        bestPoints.put(new Point(x, y), ap);
                    }                }
                else if(qualityMap[x][y] == bestQuality)
                {
                    ArrayList<Point> ap = new ArrayList<Point>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1 << i)) != 0)
                            ap.add(ts[i]);
                    }

                    if (ap.size() > 0) {
                        bestPoints.put(new Point(x, y), ap);
                    }
                }
            }
        }

        return bestPoints;
    }


    @Override
    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Set<Point> priorityTargets, Set<Point> lesserTargets, Set<Point> requiredExclusions) {
        if(priorityTargets == null)
            return idealLocations(lesserTargets, requiredExclusions);
        if(requiredExclusions == null) requiredExclusions = new LinkedHashSet<Point>();

        //requiredExclusions.remove(origin);
        int totalTargets = priorityTargets.size() + lesserTargets.size();
        LinkedHashMap<Point, ArrayList<Point>> bestPoints = new LinkedHashMap<Point, ArrayList<Point>>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        if(radius == 0)
        {
            for(Point p : priorityTargets)
            {
                ArrayList<Point> ap = new ArrayList<Point>();
                ap.add(p);
                bestPoints.put(p, ap);
            }
            return bestPoints;
        }
        Point[] pts = priorityTargets.toArray(new Point[priorityTargets.size()]);
        Point[] lts = lesserTargets.toArray(new Point[lesserTargets.size()]);
        Point[] exs = requiredExclusions.toArray(new Point[requiredExclusions.size()]);
        Point t = exs[0];

        double[][][] compositeMap = new double[totalTargets][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length],
                dungeonPriorities = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
            Arrays.fill(dungeonPriorities[i], '#');
        }
        double[][] tmpfov;
        Point tempPt = new Point(0,0);
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);
            for (int x = 0; x < dungeon.length; x++) {
                tempPt.x = x;
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt.y = y;
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0 || !AreaUtils.verifyLimit(limitType, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = pts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE || radiusType == Radius.CUBE) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE || radiusType == Radius.SPHERE) dmm = DijkstraMap.Measurement.EUCLIDEAN;

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dmm);
            t = pts[i];
            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0){
                        dist = metric.radius(origin.x, origin.y, x, y);
                        if(dist <= maxRange + radius && dist >= minRange - radius) {
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                            dungeonPriorities[x][y] = dungeon[x][y];
                        }
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][t.x][t.y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 399999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] != '!') ? dm.gradientMap[x][y] : 399999.0;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }

        t = lts[0];

        for (int i = pts.length; i < totalTargets; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dmm);
            t = lts[i - pts.length];
            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);

            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0){
                        dist = metric.radius(origin.x, origin.y, x, y);
                        if(dist <= maxRange + radius && dist >= minRange - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][t.x][t.y] > DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++)
                {
                    Arrays.fill(compositeMap[i][x], 99999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] != '!' && dungeonPriorities[x][y] != '#') ? dm.gradientMap[x][y] : 99999.0;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }
        double bestQuality = 99999 * lts.length + 399999 * pts.length;
        double[][] qualityMap = new double[dungeon.length][dungeon[0].length];
        for (int x = 0; x < qualityMap.length; x++) {
            for (int y = 0; y < qualityMap[x].length; y++) {
                qualityMap[x][y] = 0.0;
                long pbits = 0, lbits = 0;
                for (int i = 0; i < pts.length; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 399999.0 && i < 63)
                        pbits |= 1 << i;
                }
                for (int i = pts.length; i < totalTargets; ++i) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999.0 && i < 63)
                        lbits |= 1 << i;
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    ArrayList<Point> ap = new ArrayList<Point>();

                    for (int i = 0; i < pts.length && i < 63; ++i) {
                        if((pbits & (1 << i)) != 0)
                            ap.add(pts[i]);
                    }
                    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
                        if((lbits & (1 << i)) != 0)
                            ap.add(lts[i - pts.length]);
                    }

                    if(ap.size() > 0) {
                        bestQuality = qualityMap[x][y];
                        bestPoints.clear();
                        bestPoints.put(new Point(x, y), ap);
                    }
                }
                else if(qualityMap[x][y] == bestQuality)
                {
                    ArrayList<Point> ap = new ArrayList<Point>();

                    for (int i = 0; i < pts.length && i < 63; ++i) {
                        if ((pbits & (1 << i)) != 0) {
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                        }
                    }
                    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
                        if((lbits & (1 << i)) != 0)
                            ap.add(lts[i - pts.length]);
                    }

                    if (ap.size() > 0) {
                        bestPoints.put(new Point(x, y), ap);
                    }
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
    public LinkedHashMap<Point, Double> findArea() {
        return AreaUtils.arrayToHashMap(fov.calculateFOV(map, center.x, center.y, radius, radiusType));
    }


    @Override
    public Point getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Point origin) {
        this.origin = origin;

    }

    @Override
    public Radius getLimitType() {
        return limitType;
    }

    @Override
    public int getMinRange() {
        return minRange;
    }

    @Override
    public int getMaxRange() {
        return maxRange;
    }

    @Override
    public Radius getMetric() {
        return metric;
    }

    @Override
    public void setLimitType(Radius limitType) {
        this.limitType = limitType;

    }

    @Override
    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    @Override
    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;

    }

    @Override
    public void setMetric(Radius metric) {
        this.metric = metric;
    }

}
