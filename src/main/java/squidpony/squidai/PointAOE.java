package squidpony.squidai;

import squidpony.squidgrid.Radius;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An AOE type that has a center Point only and only affects that single Point. Useful if you need an AOE implementation
 * for something that does not actually affect an area.
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class doesn't use any other SquidLib class to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class PointAOE implements AOE {
    private Point center, origin = null;
    private Radius limitType = null;
    private char[][] dungeon;
    private int minRange = 1, maxRange = 1;
    private Radius metric = Radius.SQUARE;
    public PointAOE(Point center)
    {
        this.center = center;
    }
    public PointAOE(Point center, int minRange, int maxRange)
    {
        this.center = center;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    private PointAOE()
    {
        center = new Point(1, 1);
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        if(AreaUtils.verifyLimit(limitType, origin, center))
        {
            this.center = center;
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
            if(center.x == p.x && center.y == p.y)
                return true;
        }
        return false;
    }

    @Override
    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Set<Point> targets, Set<Point> requiredExclusions) {
        if(targets == null)
            return new LinkedHashMap<Point, ArrayList<Point>>();

        int totalTargets = targets.size();
        LinkedHashMap<Point, ArrayList<Point>> bestPoints = new LinkedHashMap<Point, ArrayList<Point>>(totalTargets);

        if(totalTargets == 0)
            return bestPoints;


        double dist = 0.0;
        for(Point p : targets) {
            if (AreaUtils.verifyLimit(limitType, origin, p)) {

                dist = metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= maxRange && dist >= minRange) {
                    ArrayList<Point> ap = new ArrayList<Point>();
                    ap.add(p);
                    bestPoints.put(p, ap);
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

        int totalTargets = priorityTargets.size() + lesserTargets.size();
        LinkedHashMap<Point, ArrayList<Point>> bestPoints = new LinkedHashMap<Point, ArrayList<Point>>(totalTargets * 4);

        if(totalTargets == 0)
            return bestPoints;

        double dist = 0.0;

        for(Point p : priorityTargets) {
            if (AreaUtils.verifyLimit(limitType, origin, p)) {

                dist = metric.radius(origin.x, origin.y, p.x, p.y);
                if (dist <= maxRange && dist >= minRange) {
                    ArrayList<Point> ap = new ArrayList<Point>();
                    ap.add(p);
                    ap.add(p);
                    ap.add(p);
                    ap.add(p);
                    bestPoints.put(p, ap);
                }
            }
        }
        if(bestPoints.isEmpty()) {
            for (Point p : lesserTargets) {
                if (AreaUtils.verifyLimit(limitType, origin, p)) {

                    dist = metric.radius(origin.x, origin.y, p.x, p.y);
                    if (dist <= maxRange && dist >= minRange) {
                        ArrayList<Point> ap = new ArrayList<Point>();
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
        this.dungeon = map;
    }

    @Override
    public LinkedHashMap<Point, Double> findArea() {
        LinkedHashMap<Point, Double> ret = new LinkedHashMap<Point, Double>(1);
        ret.put(new Point(center.x, center.y), 1.0);
        return ret;
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
