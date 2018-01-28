package squidpony.squidai;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;

import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.*;

/**
 * An AOE type that has a center and a radius, and will blast outward and somewhat around corners/obstacles, out to
 * the distance specified by radius. You can specify the radiusType to Radius.DIAMOND for Manhattan distance,
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * This will produce doubles for its findArea() method which are greater than 0.0 and less than or equal to 1.0.
 *
 * This class uses squidpony.squidgrid.FOV to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class BlastAOE implements AOE {
    private FOV fov;
    private Coord center, origin = null;
    private int radius;
    private double[][] map;
    private char[][] dungeon;
    private Radius radiusType;
    
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public BlastAOE(Coord center, int radius, Radius radiusType)
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
    }
    public BlastAOE(Coord center, int radius, Radius radiusType, int minRange, int maxRange)
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.center = center;
        this.radius = radius;
        this.radiusType = radiusType;
        reach.minDistance = minRange;
        reach.maxDistance = maxRange;
    }

    public Coord getCenter() {
        return center;
    }

    public void setCenter(Coord center) {

        if (map != null && center.isWithin(map.length, map[0].length) &&
                AreaUtils.verifyReach(reach, origin, center))
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
    public void shift(Coord aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets)
        {
            if(radiusType.radius(center.x, center.y, p.x, p.y) <= radius)
                return true;
        }
        return false;
    }

    @Override
    public OrderedMap<Coord, ArrayList<Coord>> idealLocations(Collection<Coord> targets, Collection<Coord> requiredExclusions) {
        if(targets == null)
            return new OrderedMap<>();
        if(requiredExclusions == null) requiredExclusions = new OrderedSet<>();

        //requiredExclusions.remove(origin);
        int totalTargets = targets.size();
        OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        if(radius == 0)
        {
            for(Coord p : targets)
            {
                ArrayList<Coord> ap = new ArrayList<>();
                ap.add(p);
                bestPoints.put(p, ap);
            }
            return bestPoints;
        }
        Coord[] ts = targets.toArray(new Coord[targets.size()]);
        Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
        Coord t;

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        double[][] tmpfov;
        Coord tempPt;
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];

            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0 || !AreaUtils.verifyReach(reach, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = ts[0];

        DijkstraMap.Measurement dmm = DijkstraMap.Measurement.MANHATTAN;
        if(radiusType == Radius.SQUARE || radiusType == Radius.CUBE) dmm = DijkstraMap.Measurement.CHEBYSHEV;
        else if(radiusType == Radius.CIRCLE || radiusType == Radius.SPHERE) dmm = DijkstraMap.Measurement.EUCLIDEAN;

        for (int i = 0; i < ts.length; i++) {
            DijkstraMap dm = new DijkstraMap(dungeon, dmm);

            t = ts[i];

            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (tmpfov[x][y] > 0.0) ? dm.physicalMap[x][y] : DijkstraMap.WALL;
                }
            }


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
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
                for (int i = 0; i < ts.length; i++) {
                    qualityMap[x][y] += compositeMap[i][x][y];
                    if(compositeMap[i][x][y] < 99999.0 && i < 63)
                        bits |= 1 << i;
                }
                if(qualityMap[x][y] < bestQuality)
                {
                    ArrayList<Coord> ap = new ArrayList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1 << i)) != 0)
                            ap.add(ts[i]);
                    }
                    if(ap.size() > 0)
                    {
                        bestQuality = qualityMap[x][y];
                        bestPoints.clear();
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
                else if(qualityMap[x][y] == bestQuality) {
                    ArrayList<Coord> ap = new ArrayList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if ((bits & (1 << i)) != 0)
                            ap.add(ts[i]);
                    }
                    if (ap.size() > 0) {
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
            }
        }

        return bestPoints;
    }

    @Override
    public OrderedMap<Coord, ArrayList<Coord>> idealLocations(Collection<Coord> priorityTargets, Collection<Coord> lesserTargets, Collection<Coord> requiredExclusions) {
        if(priorityTargets == null)
            return idealLocations(lesserTargets, requiredExclusions);
        if(requiredExclusions == null) requiredExclusions = new OrderedSet<>();

        //requiredExclusions.remove(origin);
        int totalTargets = priorityTargets.size() + lesserTargets.size();
        OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets * 8);

        if(totalTargets == 0)
            return bestPoints;

        if(radius == 0)
        {
            for(Coord p : priorityTargets)
            {
                ArrayList<Coord> ap = new ArrayList<>();
                ap.add(p);
                bestPoints.put(p, ap);
            }
            return bestPoints;
        }
        Coord[] pts = priorityTargets.toArray(new Coord[priorityTargets.size()]);
        Coord[] lts = lesserTargets.toArray(new Coord[lesserTargets.size()]);
        Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
        Coord t;

        double[][][] compositeMap = new double[totalTargets][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length],
                dungeonPriorities = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
            Arrays.fill(dungeonPriorities[i], '#');
        }
        double[][] tmpfov;
        Coord tempPt = Coord.get(0, 0);
        for (int i = 0; i < exs.length; ++i) {
            t = exs[i];

            tmpfov = fov.calculateFOV(map, t.x, t.y, radius, radiusType);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0 || !AreaUtils.verifyReach(reach, origin, tempPt)) ? '!' : dungeonCopy[x][y];
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
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius) {
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
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
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
                    ArrayList<Coord> ap = new ArrayList<>();

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
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
                else if(qualityMap[x][y] == bestQuality) {
                    ArrayList<Coord> ap = new ArrayList<>();

                    for (int i = 0; i < pts.length && i < 63; ++i) {
                        if ((pbits & (1 << i)) != 0) {
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                            ap.add(pts[i]);
                        }
                    }
                    for (int i = pts.length; i < totalTargets && i < 63; ++i) {
                        if ((lbits & (1 << i)) != 0)
                            ap.add(lts[i - pts.length]);
                    }
                    if (ap.size() > 0) {
                        bestPoints.put(Coord.get(x, y), ap);
                    }
                }
            }
        }

        return bestPoints;
    }

    /*
    @Override
    public ArrayList<ArrayList<Coord>> idealLocations(Set<Coord> targets, Set<Coord> requiredExclusions) {
        int totalTargets = targets.size() + 1;
        int maxEffect = (int)radiusType.volume2D(radius);
        ArrayList<ArrayList<Coord>> locs = new ArrayList<ArrayList<Coord>>(totalTargets);

        for(int i = 0; i < totalTargets; i++)
        {
            locs.add(new ArrayList<Coord>(maxEffect));
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
                for(Coord ex : requiredExclusions)
                {
                    if(radiusType.radius(x, y, ex.x, ex.y) <= radius)
                        continue BY_POINT;
                }
                ctr = 0;
                for(Coord tgt : targets)
                {
                    if(radiusType.radius(x, y, tgt.x, tgt.y) <= radius)
                        ctr++;
                }
                if(ctr > 0)
                    locs.get(totalTargets - ctr).add(Coord.get(x, y));
            }
        }
        Coord it;
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

                            for(Coord ex : requiredExclusions)
                            {
                                if(radiusType.radius(x, y, ex.x, ex.y) <= radius)
                                    continue BY_POINT;
                            }

                            ctr = 0;
                            for(Coord tgt : targets)
                            {
                                if(radiusType.radius(x, y, tgt.x, tgt.y) <= radius)
                                    ctr++;
                            }
                            if(ctr > 0)
                                locs.get(totalTargets - ctr).add(Coord.get(x, y));
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
        dungeon = map;
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
        return AreaUtils.arrayToHashMap(fov.calculateFOV(map, center.x, center.y, radius, radiusType));
    }

    @Override
    public Coord getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Coord origin) {
        this.origin = origin;

    }

    @Override
    public AimLimit getLimitType() {
        return reach.limit;
    }

    @Override
    public int getMinRange() {
        return reach.minDistance;
    }

    @Override
    public int getMaxRange() {
        return reach.maxDistance;
    }

    @Override
    public Radius getMetric() {
        return reach.metric;
    }

    /**
     * Gets the same values returned by getLimitType(), getMinRange(), getMaxRange(), and getMetric() bundled into one
     * Reach object.
     *
     * @return a non-null Reach object.
     */
    @Override
    public Reach getReach() {
        return reach;
    }

    @Override
    public void setLimitType(AimLimit limitType) {
        reach.limit = limitType;

    }

    @Override
    public void setMinRange(int minRange) {
        reach.minDistance = minRange;
    }

    @Override
    public void setMaxRange(int maxRange) {
        reach.maxDistance = maxRange;

    }

    @Override
    public void setMetric(Radius metric) {
        reach.metric = metric;
    }

    /**
     * Sets the same values as setLimitType(), setMinRange(), setMaxRange(), and setMetric() using one Reach object.
     *
     * @param reach a non-null Reach object.
     */
    @Override
    public void setReach(Reach reach) {
        if(reach != null)
            this.reach = reach;
    }

    /**
     * If you use FOVCache to pre-compute FOV maps for a level, you can share the speedup from using the cache with
     * some AOE implementations that rely on FOV. Not all implementations need to actually make use of the cache, but
     * those that use FOV for calculations should benefit. The cache parameter this receives should have completed its
     * calculations, which can be confirmed by calling awaitCache(). Ideally, the FOVCache will have done its initial
     * calculations in another thread while the previous level or menu was being displayed, and awaitCache() will only
     * be a formality.
     *
     * @param cache The FOVCache for the current level; can be null to stop using the cache
     */
    @GwtIncompatible
    @Override
    public void setCache(FOVCache cache) {
        fov = cache;
    }

}
