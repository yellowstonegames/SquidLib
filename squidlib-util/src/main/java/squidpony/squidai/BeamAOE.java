package squidpony.squidai;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;

import static java.lang.Math.round;
import static squidpony.squidmath.NumberTools.cos;
import static squidpony.squidmath.NumberTools.sin;

/**
 * Beam Area of Effect that affects an slightly expanded (Elias) line from a given origin Coord out to a given length,
 * plus an optional radius of cells around the path of the line, while respecting obstacles in its path and possibly
 * stopping if obstructed. There are several ways to specify the BeamAOE's direction and length, including specifying
 * an endpoint or specifying an angle in degrees and a distance to the end of the line (without the radius included).
 * You can specify the RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for Chebyshev, or
 * RADIUS.CIRCLE for Euclidean.
 *
 * You may want the LineAOE class instead of this. LineAOE travels point-to-point and does not restrict length, while
 * BeamAOE travels a specific length (and may have a radius, like LineAOE) but then stops only after the travel down the
 * length and radius has reached its end. This difference is relevant if a game has effects that have a definite
 * area measured in a rectangle or elongated pillbox shape, such as a "20-foot-wide bolt of lightning, 100 feet long."
 * BeamAOE is more suitable for that effect, while LineAOE may be more suitable for things like focused lasers that
 * pass through small (likely fleshy) obstacles but stop after hitting the aimed-at target.
 *
 * BeamAOE will strike a small area behind the user and in the opposite direction of the target if the radius is
 * greater than 0. This behavior may be altered in a future version.
 *
 * This will produce doubles for its findArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidmath.Elias and squidpony.squidai.DijkstraMap to create its area of effect.
 * Created by Tommy Ettinger on 7/14/2015.
 */
public class BeamAOE implements AOE, Serializable {
    private static final long serialVersionUID = 2L;
    private Coord origin, end;
    private int radius;
    private int length;
    private char[][] dungeon;
    private DijkstraMap dijkstra;
    private Radius rt;
    private LOS los;

    private Reach reach = new Reach(1, 1, Radius.SQUARE, AimLimit.FREE);

    public BeamAOE(Coord origin, Coord end)
    {
        dijkstra = new DijkstraMap();
        dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        this.end = end;
        length =(int) round(rt.radius(origin.x, origin.y, end.x, end.y));
        reach.maxDistance = length;
        radius = 0;
        los = new LOS(LOS.THICK);
    }
    public BeamAOE(Coord origin, Coord end, int radius)
    {
        dijkstra = new DijkstraMap();
        dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        length =(int) round(rt.radius(origin.x, origin.y, end.x, end.y));
        reach.maxDistance = length;
        los = new LOS(LOS.THICK);
    }
    public BeamAOE(Coord origin, Coord end, int radius, Radius radiusType)
    {
        dijkstra = new DijkstraMap();
        rt = radiusType;
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND:
                dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE:
                dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default:
                dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        length =(int) round(rt.radius(origin.x, origin.y, end.x, end.y));
        reach.maxDistance = length;
        los = new LOS(LOS.THICK);
    }

    public BeamAOE(Coord origin, double angle, int length)
    {
        dijkstra = new DijkstraMap();
        dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        double theta = Math.toRadians(angle);
        end = Coord.get((int) round(cos(theta) * length) + origin.x,
                (int) round(sin(theta) * length) + origin.y);
        this.length = length;
        reach.maxDistance = this.length;
        radius = 0;
        los = new LOS(LOS.THICK);
    }
    public BeamAOE(Coord origin, double angle, int length, int radius)
    {
        dijkstra = new DijkstraMap();
        dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        double theta = Math.toRadians(angle);
        end = Coord.get((int) round(cos(theta) * length) + origin.x,
                (int) round(sin(theta) * length) + origin.y);
        this.radius = radius;
        this.length = length;
        reach.maxDistance = this.length;
        los = new LOS(LOS.THICK);
    }
    public BeamAOE(Coord origin, double angle, int length, int radius, Radius radiusType)
    {
        dijkstra = new DijkstraMap();
        rt = radiusType;
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND:
                dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE:
                dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default:
                dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
        this.origin = origin;
        double theta = Math.toRadians(angle);
        end = Coord.get((int) round(cos(theta) * length) + origin.x,
                (int) round(sin(theta) * length) + origin.y);
        this.radius = radius;
        this.length = length;
        reach.maxDistance = this.length;
        los = new LOS(LOS.THICK);
    }
    private double[][] initDijkstra()
    {
        los.isReachable(dungeon, origin.x, origin.y, end.x, end.y, rt);
        Queue<Coord> lit = los.getLastPath();

        dijkstra.initialize(dungeon);
        for(Coord p : lit)
        {
            dijkstra.setGoal(p);
        }
        if(radius == 0)
            return dijkstra.gradientMap;
        return dijkstra.partialScan(radius, null);
    }

    @Override
	public Coord getOrigin() {
        return origin;
    }

    @Override
	public void setOrigin(Coord origin) {
        this.origin = origin;
        dijkstra.resetMap();
        dijkstra.clearGoals();
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
        length = maxRange;

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

    public Coord getEnd() {
        return end;
    }

    public void setEnd(Coord end) {
        if (AreaUtils.verifyReach(reach, origin, end))
        {
            this.end = rt.extend(origin, end, length, false, dungeon.length, dungeon[0].length);
        }

    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Radius getRadiusType()
    {
        return rt;
    }
    public void setRadiusType(Radius radiusType)
    {
        rt = radiusType;
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND:
                dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE:
                dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default:
                dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
    }

    @Override
    public void shift(Coord aim) {
        setEnd(aim);
    }

    @Override
    public boolean mayContainTarget(Collection<Coord> targets) {
        for (Coord p : targets)
        {
            if(rt.radius(origin.x, origin.y, p.x, p.y) + rt.radius(end.x, end.y, p.x, p.y) -
                    rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
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

        Coord[] ts = targets.toArray(new Coord[targets.size()]);
        Coord[] exs = requiredExclusions.toArray(new Coord[requiredExclusions.size()]);
        Coord t;

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        DijkstraMap dt = new DijkstraMap(dungeon, dijkstra.measurement);
        double[][] resMap = DungeonUtility.generateResistances(dungeon);
        Coord tempPt = Coord.get(0, 0);
        for (int i = 0; i < exs.length; ++i) {
            t = rt.extend(origin, exs[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(resMap, origin.x, origin.y, t.x, t.y, rt);
            Queue<Coord> lit = los.getLastPath();


            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (dt.gradientMap[x][y] < DijkstraMap.FLOOR || !AreaUtils.verifyReach(reach, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        //t = rt.extend(origin, ts[0], length, false, dungeon.length, dungeon[0].length);

        for (int i = 0; i < ts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement);

            t = rt.extend(origin, ts[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(resMap, origin.x, origin.y, t.x, t.y, rt);
            Queue<Coord> lit = los.getLastPath();

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
                    }
                    else compositeMap[i][x][y] = DijkstraMap.WALL;
                }
            }
            if(compositeMap[i][ts[i].x][ts[i].y] >= DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 99999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(ts[i]);
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
                else if(qualityMap[x][y] == bestQuality)
                {
                    ArrayList<Coord> ap = new ArrayList<>();

                    for (int i = 0; i < ts.length && i < 63; ++i) {
                        if((bits & (1 << i)) != 0)
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
        Coord t;// = rt.extend(origin, exs[0], length, false, dungeon.length, dungeon[0].length);

        double[][][] compositeMap = new double[totalTargets][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length],
                dungeonPriorities = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
            Arrays.fill(dungeonPriorities[i], '#');
        }
        DijkstraMap dt = new DijkstraMap(dungeon, dijkstra.measurement);
        double[][] resMap = DungeonUtility.generateResistances(dungeon);
        Coord tempPt = Coord.get(0, 0);
        for (int i = 0; i < exs.length; ++i) {
            t = rt.extend(origin, exs[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(resMap, origin.x, origin.y, t.x, t.y, rt);
            Queue<Coord> lit = los.getLastPath();

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt = Coord.get(x, y);
                    dungeonCopy[x][y] = (dt.gradientMap[x][y] < DijkstraMap.FLOOR  || !AreaUtils.verifyReach(reach, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = rt.extend(origin, pts[0], length, false, dungeon.length, dungeon[0].length);

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement);

            t = rt.extend(origin, pts[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(resMap, origin.x, origin.y, t.x, t.y, rt);
            Queue<Coord> lit = los.getLastPath();

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
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
            if(compositeMap[i][pts[i].x][pts[i].y] >= DijkstraMap.FLOOR)
            {
                for (int x = 0; x < dungeon.length; x++) {
                    Arrays.fill(compositeMap[i][x], 399999.0);
                }
                continue;
            }


            dm.initialize(compositeMap[i]);
            dm.setGoal(pts[i]);
            dm.scan(null, null);
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    compositeMap[i][x][y] = (dm.gradientMap[x][y] < DijkstraMap.FLOOR  && dungeonCopy[x][y] != '!') ? dm.gradientMap[x][y] : 399999.0;
                }
            }
            dm.resetMap();
            dm.clearGoals();
        }

        t = rt.extend(origin, lts[0], length, false, dungeon.length, dungeon[0].length);

        for (int i = pts.length; i < totalTargets; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement);

            t = rt.extend(origin, lts[i - pts.length], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            los.isReachable(resMap, origin.x, origin.y, t.x, t.y, rt);
            Queue<Coord> lit = los.getLastPath();

            for(Coord p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
                        dist = reach.metric.radius(origin.x, origin.y, x, y);
                        if(dist <= reach.maxDistance + radius && dist >= reach.minDistance - radius)
                            compositeMap[i][x][y] = dm.physicalMap[x][y];
                        else
                            compositeMap[i][x][y] = DijkstraMap.WALL;
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
            dm.setGoal(lts[i - pts.length]);
            dm.scan(null, null);
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
                else if(qualityMap[x][y] == bestQuality)
                {
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
                        if((lbits & (1 << i)) != 0)
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
            int volume = (int)(rt.radius(1, 1, dungeon.length - 2, dungeon[0].length - 2) * radius * 2.1);
            ArrayList<ArrayList<Coord>> locs = new ArrayList<ArrayList<Coord>>(totalTargets);
            for(int i = 0; i < totalTargets; i++)
            {
                locs.add(new ArrayList<Coord>(volume));
            }
            if(totalTargets == 1)
                return locs;

            int ctr = 0;

            boolean[][] tested = new boolean[dungeon.length][dungeon[0].length];
            for (int x = 1; x < dungeon.length - 1; x += radius) {
                for (int y = 1; y < dungeon[x].length - 1; y += radius) {

                    if(mayContainTarget(requiredExclusions, x, y))
                        continue;
                    ctr = 0;
                    for(Coord tgt : targets)
                    {
                        if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) -
                            rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
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
                            for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
                            {
                                if(tested[x][y])
                                    continue;
                                tested[x][y] = true;

                                if(mayContainTarget(requiredExclusions, x, y))
                                    continue;

                                ctr = 0;
                                for(Coord tgt : targets)
                                {
                                    if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) -
                                            rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
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
        dungeon = map;
        end = rt.extend(origin, end, length, false, map.length, map[0].length);
        dijkstra.resetMap();
        dijkstra.clearGoals();
    }

    @Override
    public OrderedMap<Coord, Double> findArea() {
        double[][] dmap = initDijkstra();
        dmap[origin.x][origin.y] = DijkstraMap.DARK;
        dijkstra.resetMap();
        dijkstra.clearGoals();
        return AreaUtils.dijkstraToHashMap(dmap);
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
    }

}
