package squidpony.squidai;

import squidpony.annotation.GwtIncompatible;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;

import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An AOE type that has a center Coord only and only affects that single Coord. Useful if you need an AOE implementation
 * for something that does not actually affect an area.
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class doesn't use any other SquidLib class to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class PointAOE implements AOE {
    private Coord center, origin = null;
    private int mapWidth, mapHeight;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public PointAOE(Coord center)
    {
        this.center = center;
    }
    public PointAOE(Coord center, int minRange, int maxRange)
    {
        this.center = center;
        reach.minDistance = minRange;
        reach.maxDistance = maxRange;
    }

    public Coord getCenter() {
        return center;
    }


    public void setCenter(Coord center) {

        if (center.isWithin(mapWidth, mapHeight) &&
                AreaUtils.verifyReach(reach, origin, center))
        {
            this.center = center;
        }
    }

    @Override
    public void shift(Coord aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets)
        {
            if(center.x == p.x && center.y == p.y)
                return true;
        }
        return false;
    }

    @Override
    public OrderedMap<Coord, ArrayList<Coord>> idealLocations(Collection<Coord> targets, Collection<Coord> requiredExclusions) {
        if(targets == null)
            return new OrderedMap<>();

        int totalTargets = targets.size();
        OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets);

        if(totalTargets == 0)
            return bestPoints;


        double dist = 0.0;
        for(Coord p : targets) {
            if (AreaUtils.verifyReach(reach, origin, p)) {

                dist = reach.metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= reach.maxDistance && dist >= reach.minDistance) {
                    ArrayList<Coord> ap = new ArrayList<>();
                    ap.add(p);
                    bestPoints.put(p, ap);
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

        int totalTargets = priorityTargets.size() + lesserTargets.size();
        OrderedMap<Coord, ArrayList<Coord>> bestPoints = new OrderedMap<>(totalTargets * 4);

        if(totalTargets == 0)
            return bestPoints;

        double dist = 0.0;

        for(Coord p : priorityTargets) {
            if (AreaUtils.verifyReach(reach, origin, p)) {

                dist = reach.metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= reach.maxDistance && dist >= reach.minDistance) {
                    ArrayList<Coord> ap = new ArrayList<>();
                    ap.add(p);
                    ap.add(p);
                    ap.add(p);
                    ap.add(p);
                    bestPoints.put(p, ap);
                }
            }
        }
        if(bestPoints.isEmpty()) {
            for (Coord p : lesserTargets) {
                if (AreaUtils.verifyReach(reach, origin, p)) {

                    dist = reach.metric.radius(origin.x, origin.y, p.x, p.y);
                    if (dist <= reach.maxDistance && dist >= reach.minDistance) {
                        ArrayList<Coord> ap = new ArrayList<>();
                        ap.add(p);
                        bestPoints.put(p, ap);
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
        if (map != null && map.length > 0) {
            mapWidth = map.length;
            mapHeight = map[0].length;
        }
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
        OrderedMap<Coord, Double> ret = new OrderedMap<>(1);
        ret.put(Coord.get(center.x, center.y), 1.0);
        return ret;
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

    }
}
