package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidgrid.Spill;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.*;
import java.util.*;

/**
 * An AOE type that has a center and a volume, and will randomly expand in all directions until it reaches volume or
 * cannot expand further. Specify the RadiusType as Radius.DIAMOND for Manhattan distance (and the best results),
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean. You can specify a seed for the RNG and a fresh RNG will
 * be used for all random expansion; the RNG will reset to the specified seed after each generation so the same
 * CloudAOE can be used in different places by just changing the center. You can cause the CloudAOE to not reset after
 * generating each time by using setExpanding(true) and cause it to reset after the next generation by setting it back
 * to the default of false. If expanding is true, then multiple calls to findArea with the same center and larger
 * volumes will produce more solid clumps of affected area with fewer gaps, and can be spaced out over multiple calls.
 *
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidgrid.Spill to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class CloudAOE implements AOE {
    private Spill spill;
    private Point center, origin = null;
    private int volume;
    private long seed;
    private boolean expanding;
    private Radius rt, limitType = null;
    private int minRange = 1, maxRange = 1;
    private Radius metric = Radius.SQUARE;
    private char[][] dungeon;

    public CloudAOE(Point center, int volume, Radius radiusType)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.spill.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.spill.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.spill.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }

    public CloudAOE(Point center, int volume, Radius radiusType, int minRange, int maxRange)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        rt = radiusType;
        this.minRange = minRange;
        this.maxRange = maxRange;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.spill.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.spill.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.spill.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }
    public CloudAOE(Point center, int volume, Radius radiusType, long rngSeed)
    {
        this.seed = rngSeed;
        this.spill = new Spill(new RNG(new LightRNG(rngSeed)));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.spill.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.spill.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.spill.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }
    public CloudAOE(Point center, int volume, Radius radiusType, long rngSeed, int minRange, int maxRange)
    {
        this.seed = rngSeed;
        this.spill = new Spill(new RNG(new LightRNG(rngSeed)));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE: this.spill.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case CUBE:
            case SQUARE: this.spill.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.spill.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
        this.minRange = minRange;
        this.maxRange = maxRange;
    }
    private CloudAOE()
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = new Point(1, 1);
        this.volume = 1;
        this.spill.measurement = Spill.Measurement.MANHATTAN;
        rt = Radius.DIAMOND;
        this.expanding = false;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        if (AreaUtils.verifyLimit(limitType, origin, center)) {
            this.center = center;
        }
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public Radius getRadiusType() {
        return rt;
    }

    public void setRadiusType(Radius radiusType) {
        rt = radiusType;
        switch (radiusType)
        {
            case SPHERE:
            case CIRCLE:
                break;
            case CUBE:
            case SQUARE:
                break;
            default:
                break;
        }
    }

    @Override
    public void shift(Point aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Point> targets) {
        for (Point p : targets)
        {
            if(rt.radius(center.x, center.y, p.x, p.y) <= Math.sqrt(volume) * 0.75)
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

        if(totalTargets == 0 || volume <= 0)
            return bestPoints;

        if(volume == 1)
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

        Spill sp;

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }

        Point tempPt = new Point(0, 0);
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);
            for (int x = 0; x < dungeon.length; x++) {
                tempPt.x = x;
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt.y = y;
                    dungeonCopy[x][y] = (sp.spillMap[x][y] || !AreaUtils.verifyLimit(limitType, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = ts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(spill.measurement == Spill.Measurement.CHEBYSHEV) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(spill.measurement == Spill.Measurement.EUCLIDEAN) dmm = DijkstraMap.Measurement.EUCLIDEAN;

        double radius = Math.sqrt(volume) * 0.75;

        for (int i = 0; i < ts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dmm);

            t = ts[i];
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);

            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (sp.spillMap[x][y]){
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
            dm.resetMap();
            dm.clearGoals();
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
                    }
                }
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

        if(totalTargets == 0 || volume <= 0)
            return bestPoints;

        if(volume == 1)
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
        Spill sp;

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length],
                dungeonPriorities = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
            Arrays.fill(dungeonPriorities[i], '#');
        }
        Point tempPt = new Point(0,0);
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);
            for (int x = 0; x < dungeon.length; x++) {
                tempPt.x = x;
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt.y = y;
                    dungeonCopy[x][y] = (sp.spillMap[x][y] || !AreaUtils.verifyLimit(limitType, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = pts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(spill.measurement == Spill.Measurement.CHEBYSHEV) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(spill.measurement == Spill.Measurement.EUCLIDEAN) dmm = DijkstraMap.Measurement.EUCLIDEAN;

        double radius = Math.sqrt(volume) * 0.75;

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dmm);

            t = pts[i];
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);



            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (sp.spillMap[x][y]){
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
            if(compositeMap[i][pts[i].x][pts[i].y] > DijkstraMap.FLOOR)
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
            sp = new Spill(dungeon, spill.measurement);
            sp.lrng.setState(this.seed);

            sp.start(t, volume, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (sp.spillMap[x][y]){
                        dist = metric.radius(origin.x, origin.y, x, y);
                        if(dist <= maxRange + radius && dist >= minRange - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][lts[i - pts.length].x][lts[i - pts.length].y] > DijkstraMap.FLOOR)
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
                        if((pbits & (1 << i)) != 0)
                            ap.add(pts[i]);
                    }
                    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
                        if ((pbits & (1 << i)) != 0) {
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                        }
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
        int radius = Math.max(1, (int) (Math.sqrt(volume) * 1.5));
        ArrayList<ArrayList<Point>> locs = new ArrayList<ArrayList<Point>>(totalTargets);

        for(int i = 0; i < totalTargets; i++)
        {
            locs.add(new ArrayList<Point>(volume));
        }
        if(totalTargets == 1)
            return locs;
        double ctr = 0;
        if(radius < 1)
        {
            locs.get(totalTargets - 2).addAll(targets);
            return locs;
        }
        double tempRad;
        boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
        for (int x = 1; x < dungeon.length - 1; x += radius) {
            BY_POINT:
            for (int y = 1; y < dungeon[x].length - 1; y += radius) {
                for(Point ex : requiredExclusions)
                {
                    if(rt.radius(x, y, ex.x, ex.y) <= radius * 0.75)
                        continue BY_POINT;
                }
                ctr = 0;
                for(Point tgt : targets)
                {
                    tempRad = rt.radius(x, y, tgt.x, tgt.y);
                    if(tempRad < radius)
                        ctr += 1.0 - (tempRad / radius) * 0.5;
                }
                if(ctr >= 1)
                    locs.get((int)(totalTargets - ctr)).add(new Point(x, y));
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
                                if(rt.radius(x, y, ex.x, ex.y) <= radius * 0.75)
                                    continue BY_POINT;
                            }

                            ctr = 0;
                            for(Point tgt : targets)
                            {
                                tempRad = rt.radius(x, y, tgt.x, tgt.y);
                                if(tempRad < radius)
                                    ctr += 1.0 - (tempRad / radius) * 0.5;
                            }
                            if(ctr >= 1)
                                locs.get((int)(totalTargets - ctr)).add(new Point(x, y));
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
        spill.initialize(map);
        this.dungeon = map;
    }

    @Override
    public LinkedHashMap<Point, Double> findArea() {
        spill.start(center, volume, null);
        LinkedHashMap<Point, Double> r = AreaUtils.arrayToHashMap(spill.spillMap);
        if(!expanding)
        {
            spill.reset();
            spill.lrng.setState(this.seed);
        }
        return r;
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

    public boolean isExpanding() {
        return expanding;
    }

    public void setExpanding(boolean expanding) {
        this.expanding = expanding;
    }
}
