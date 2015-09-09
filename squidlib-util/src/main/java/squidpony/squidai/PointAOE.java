package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

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
    private Radius limitType = null;
    private char[][] dungeon;
    private int minRange = 1, maxRange = 1;
    private Radius metric = Radius.SQUARE;
    public PointAOE(Coord center)
    {
        this.center = center;
    }
    public PointAOE(Coord center, int minRange, int maxRange)
    {
        this.center = center;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    private PointAOE()
    {
        center = new Coord(1, 1);
    }

    public Coord getCenter() {
        return center;
    }

    public void setCenter(Coord center) {
        if(AreaUtils.verifyLimit(limitType, origin, center))
        {
            this.center = center;
        }
    }
    @Override
    public void shift(Coord aim) {
        setCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Coord> targets) {
        for (Coord p : targets)
        {
            if(center.x == p.x && center.y == p.y)
                return true;
        }
        return false;
    }

    @Override
    public LinkedHashMap<Coord, ArrayList<Coord>> idealLocations(Set<Coord> targets, Set<Coord> requiredExclusions) {
        if(targets == null)
            return new LinkedHashMap<Coord, ArrayList<Coord>>();

        int totalTargets = targets.size();
        LinkedHashMap<Coord, ArrayList<Coord>> bestPoints = new LinkedHashMap<Coord, ArrayList<Coord>>(totalTargets);

        if(totalTargets == 0)
            return bestPoints;


        double dist = 0.0;
        for(Coord p : targets) {
            if (AreaUtils.verifyLimit(limitType, origin, p)) {

                dist = metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= maxRange && dist >= minRange) {
                    ArrayList<Coord> ap = new ArrayList<Coord>();
                    ap.add(p);
                    bestPoints.put(p, ap);
                }
            }
        }
        return bestPoints;
    }


    @Override
    public LinkedHashMap<Coord, ArrayList<Coord>> idealLocations(Set<Coord> priorityTargets, Set<Coord> lesserTargets, Set<Coord> requiredExclusions) {
        if(priorityTargets == null)
            return idealLocations(lesserTargets, requiredExclusions);
        if(requiredExclusions == null) requiredExclusions = new LinkedHashSet<Coord>();

        int totalTargets = priorityTargets.size() + lesserTargets.size();
        LinkedHashMap<Coord, ArrayList<Coord>> bestPoints = new LinkedHashMap<Coord, ArrayList<Coord>>(totalTargets * 4);

        if(totalTargets == 0)
            return bestPoints;

        double dist = 0.0;

        for(Coord p : priorityTargets) {
            if (AreaUtils.verifyLimit(limitType, origin, p)) {

                dist = metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= maxRange && dist >= minRange) {
                    ArrayList<Coord> ap = new ArrayList<Coord>();
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
                if (AreaUtils.verifyLimit(limitType, origin, p)) {

                    dist = metric.radius(origin.x, origin.y, p.x, p.y);
                    if (dist <= maxRange && dist >= minRange) {
                        ArrayList<Coord> ap = new ArrayList<Coord>();
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
                    locs.get(totalTargets - ctr).add(new Coord(x, y));
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
                                locs.get(totalTargets - ctr).add(new Coord(x, y));
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
        this.dungeon = map;
    }

    @Override
    public LinkedHashMap<Coord, Double> findArea() {
        LinkedHashMap<Coord, Double> ret = new LinkedHashMap<Coord, Double>(1);
        ret.put(new Coord(center.x, center.y), 1.0);
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
