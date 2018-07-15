package squidpony.squidai;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * An AOE type that has an origin, a radius, an angle, and a span; it will blast from the origin to a length equal to
 * radius along the angle (in degrees), moving somewhat around corners/obstacles, and also spread a total of span
 * degrees around the angle (a span of 90 will affect a full quadrant, centered on angle). You can specify the
 * RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * RADIUS.CIRCLE (Euclidean measurement) will produce the most real-looking cones. This will produce doubles for its
 * {@link #findArea()} method which are greater than 0.0 and less than or equal to 1.0.
 *
 * This class uses {@link FOV} to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class ConeAOE implements AOE, Serializable {
    private static final long serialVersionUID = 2L;
    private FOV fov;
    private Coord origin;
    private double radius, angle, span;
    private double[][] map;
    private char[][] dungeon;
    private Radius radiusType;
    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public ConeAOE(Coord origin, Coord endCenter, double span, Radius radiusType)
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.origin = origin;
        radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
        angle = (Math.toDegrees(NumberTools.atan2(endCenter.y - origin.y, endCenter.x - origin.x)) % 360.0 + 360.0) % 360.0;
//        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
        this.span = span;
        this.radiusType = radiusType;
    }
    public ConeAOE(Coord origin, int radius, double angle, double span, Radius radiusType)
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.origin = origin;
        this.radius = radius;
//        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
        this.angle = angle;
        this.span = span;
        this.radiusType = radiusType;
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

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        if (reach.limit == null || reach.limit == AimLimit.FREE ||
                (reach.limit == AimLimit.EIGHT_WAY && (int) angle % 45 == 0) ||
                (reach.limit == AimLimit.DIAGONAL && (int) angle % 90 == 45) ||
                (reach.limit == AimLimit.ORTHOGONAL && (int) angle % 90 == 0)) {
            this.angle = angle;
//            this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//            this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
        }
    }

    public void setEndCenter(Coord endCenter) {
//        radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
        if (AreaUtils.verifyLimit(reach.limit, origin, endCenter)) {
            angle = (Math.toDegrees(NumberTools.atan2(endCenter.y - origin.y, endCenter.x - origin.x)) % 360.0 + 360.0) % 360.0;
//            startAngle = Math.abs((angle - span / 2.0) % 360.0);
//            endAngle = Math.abs((angle + span / 2.0) % 360.0);
        }
    }

    public double getSpan() {
        return span;
    }

    public void setSpan(double span) {
        this.span = span;
//        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
//        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
    }

    public Radius getRadiusType() {
        return radiusType;
    }

    public void setRadiusType(Radius radiusType) {
        this.radiusType = radiusType;
    }

    @Override
    public void shift(Coord aim) {
        setEndCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets) {
            if (radiusType.radius(origin.x, origin.y, p.x, p.y) <= radius) {
                double d = (angle - Math.toDegrees(NumberTools.atan2(p.y - origin.y, p.x - origin.x)) % 360.0 + 360.0) % 360.0;
                if(d > 180)
                    d = 360 - d;
                if(d < span / 2.0)
                    return true;
            }
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

        Coord[] ts = targets.toArray(new Coord[targets.size()]);
        Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
        Coord t;

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];
        double tAngle; //, tStartAngle, tEndAngle;


        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        double[][] tmpfov;
        Coord tempPt = Coord.get(0, 0);

        for (int i = 0; i < exs.length; i++) {
            t = exs[i];
//            tRadius = radiusType.radius(origin.x, origin.y, t.x, t.y);
            tAngle = (Math.toDegrees(NumberTools.atan2(t.y - origin.y, t.x - origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
            tmpfov = fov.calculateFOV(map, origin.x, origin.y, radius, radiusType, tAngle, span);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0 || !AreaUtils.verifyLimit(reach.limit, origin, tempPt)) ? '!' : dungeonCopy[x][y];
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
//            tRadius = radiusType.radius(origin.x, origin.y, t.x, t.y);
            tAngle = (Math.toDegrees(NumberTools.atan2(t.y - origin.y, t.x - origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);

            tmpfov = fov.calculateFOV(map, origin.x, origin.y, radius, radiusType, tAngle, span);


            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0)
                    {
                        compositeMap[i][x][y] = dm.physicalMap[x][y];
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][t.x][t.y] >= DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 99999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
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
                    ArrayList<Coord> ap = new ArrayList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1 << i)) != 0)
                            ap.add(ts[i]);
                    }
                    if(ap.size() > 0) {
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

        Coord[] pts = priorityTargets.toArray(new Coord[priorityTargets.size()]);
        Coord[] lts = lesserTargets.toArray(new Coord[lesserTargets.size()]);
        Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
        Coord t;

        double[][][] compositeMap = new double[totalTargets][dungeon.length][dungeon[0].length];
        double tAngle; //, tStartAngle, tEndAngle;

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

            tAngle = (Math.toDegrees(NumberTools.atan2(t.y - origin.y, t.x - origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);
            tmpfov = fov.calculateFOV(map, origin.x, origin.y, radius, radiusType, tAngle, span);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (tmpfov[x][y] > 0.0 || !AreaUtils.verifyLimit(reach.limit, origin, tempPt)) ? '!' : dungeonCopy[x][y];
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
            tAngle = (Math.toDegrees(NumberTools.atan2(t.y - origin.y, t.x - origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);

            tmpfov = fov.calculateFOV(map, origin.x, origin.y, radius, radiusType, tAngle, span);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0){
                        compositeMap[i][x][y] = dm.physicalMap[x][y];
                        dungeonPriorities[x][y] = dungeon[x][y];
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][pts[i].x][pts[i].y] >= DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 399999.0);
                }
                continue;
            }



            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
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
            tAngle = (Math.toDegrees(NumberTools.atan2(t.y - origin.y, t.x - origin.x)) % 360.0 + 360.0) % 360.0;
//            tStartAngle = Math.abs((tAngle - span / 2.0) % 360.0);
//            tEndAngle = Math.abs((tAngle + span / 2.0) % 360.0);

            tmpfov = fov.calculateFOV(map, origin.x, origin.y, radius, radiusType, tAngle, span);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (tmpfov[x][y] > 0.0){
                         compositeMap[i][x][y] = dm.physicalMap[x][y];
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][lts[i - pts.length].x][lts[i - pts.length].y] >= DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++)
                {
                    Arrays.fill(compositeMap[i][x], 99999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(t);
            dm.scan(null, null);
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
        int maxEffect = (int)(radiusType.volume2D(radius) * Math.max(5, span) / 360.0);
        double allowed = Math.toRadians(span / 2.0);
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

        double tmpAngle, ang;
        boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
        for (int x = 1; x < dungeon.length - 1; x += radius) {
            BY_POINT:
            for (int y = 1; y < dungeon[x].length - 1; y += radius) {
                ang = NumberTools.atan2(y - origin.y, x - origin.x); // between -pi and pi

                for(Coord ex : requiredExclusions) {
                    if (radiusType.radius(x, y, ex.x, ex.y) <= radius) {
                        tmpAngle = Math.abs(ang - NumberTools.atan2(ex.y - origin.y, ex.x - origin.x));
                        if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle;
                        if(tmpAngle < allowed)
                            continue BY_POINT;
                    }
                }
                ctr = 0;
                for(Coord tgt : targets) {
                    if (radiusType.radius(x, y, tgt.x, tgt.y) <= radius) {
                        tmpAngle = Math.abs(ang - NumberTools.atan2(tgt.y - origin.y, tgt.x - origin.x));
                        if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle;
                        if(tmpAngle < allowed)
                            ctr++;
                    }
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
                    for (int x = Math.max(1, it.x - (int)(radius) / 2); x < it.x + (radius + 1) / 2 && x < dungeon.length - 1; x++) {
                        BY_POINT:
                        for (int y = Math.max(1, it.y - (int)(radius) / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
                        {
                            if(tested[x][y])
                                continue;
                            tested[x][y] = true;
                            ang = NumberTools.atan2(y - origin.y, x - origin.x); // between -pi and pi
                            for(Coord ex : requiredExclusions) {
                                if (radiusType.radius(x, y, ex.x, ex.y) <= radius) {
                                    tmpAngle = Math.abs(ang - NumberTools.atan2(ex.y - origin.y, ex.x - origin.x));
                                    if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle;
                                    if(tmpAngle < allowed)
                                        continue BY_POINT;
                                }
                            }

                            ctr = 0;
                            for(Coord tgt : targets) {
                                if (radiusType.radius(x, y, tgt.x, tgt.y) <= radius) {
                                    tmpAngle = Math.abs(ang - NumberTools.atan2(tgt.y - origin.y, tgt.x - origin.x));
                                    if(tmpAngle > Math.PI) tmpAngle = PI2 - tmpAngle;
                                    if(tmpAngle < allowed)
                                        ctr++;
                                }
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
        OrderedMap<Coord, Double> r = AreaUtils.arrayToHashMap(fov.calculateFOV(map, origin.x, origin.y, radius,
                radiusType, angle, span));
        r.remove(origin);
        return r;
    }

    /**
     * Unused because FOVCache rarely provides a speed boost and usually does the opposite. The implementation for this
     * method should be a no-op.
     * @param cache an FOV that could be an FOVCache for the current level; can be null to stop using the cache
     * @deprecated AOE doesn't really benefit from using an FOVCache
     */
    @Override
    @Deprecated
    public void setCache(FOV cache) {
        fov = cache;
    }

}
