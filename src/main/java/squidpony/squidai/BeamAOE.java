package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Elias;

import java.awt.Point;
import java.util.*;

/**
 * Beam Area of Effect that affects an slightly expanded (Elias) line from a given origin Point out to a given length,
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
public class BeamAOE implements AOE {
    private Point origin, end;
    private int radius;
    private int length;
    private char[][] dungeon;
    private DijkstraMap dijkstra;
    private Radius rt, limitType = null;

    private int minRange = 1, maxRange = 1;
    private Radius metric = Radius.SQUARE;

    public BeamAOE(Point origin, Point end)
    {
        this.dijkstra = new DijkstraMap();
        this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        this.end = end;
        this.length =(int)Math.round(rt.radius(origin.x, origin.y, end.x, end.y));
        this.maxRange = this.length;
        this.radius = 0;
    }
    public BeamAOE(Point origin, Point end, int radius)
    {
        this.dijkstra = new DijkstraMap();
        this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        this.length =(int)Math.round(rt.radius(origin.x, origin.y, end.x, end.y));
        this.maxRange = this.length;
    }
    public BeamAOE(Point origin, Point end, int radius, Radius radiusType)
    {
        this.dijkstra = new DijkstraMap();
        this.rt = radiusType;
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
        this.origin = origin;
        this.end = end;
        this.radius = radius;
        this.length =(int)Math.round(rt.radius(origin.x, origin.y, end.x, end.y));
        this.maxRange = this.length;
    }

    public BeamAOE(Point origin, double angle, int length)
    {
        this.dijkstra = new DijkstraMap();
        this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        double theta = Math.toRadians(angle);
        this.end = new Point((int)Math.round(Math.cos(theta) * length) + origin.x,
                (int)Math.round(Math.sin(theta) * length) + origin.y);
        this.length = length;
        this.maxRange = this.length;
        this.radius = 0;
    }
    public BeamAOE(Point origin, double angle, int length, int radius)
    {
        this.dijkstra = new DijkstraMap();
        this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
        rt = Radius.SQUARE;
        this.origin = origin;
        double theta = Math.toRadians(angle);
        this.end = new Point((int)Math.round(Math.cos(theta) * length) + origin.x,
                (int)Math.round(Math.sin(theta) * length) + origin.y);
        this.radius = radius;
        this.length = length;
        this.maxRange = this.length;
    }
    public BeamAOE(Point origin, double angle, int length, int radius, Radius radiusType)
    {
        this.dijkstra = new DijkstraMap();
        this.rt = radiusType;
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
        this.origin = origin;
        double theta = Math.toRadians(angle);
        this.end = new Point((int)Math.round(Math.cos(theta) * length) + origin.x,
                (int)Math.round(Math.sin(theta) * length) + origin.y);
        this.radius = radius;
        this.length = length;
        this.maxRange = this.length;
    }
    private double[][] initDijkstra()
    {
        List<Point> lit = Elias.line(origin, end, 0.4);

        dijkstra.initialize(dungeon);
        for(Point p : lit)
        {
            dijkstra.setGoal(p);
        }
        if(radius == 0)
            return dijkstra.gradientMap;
        return dijkstra.partialScan(radius, null);
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
        dijkstra.resetMap();
        dijkstra.clearGoals();
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
        this.length = maxRange;

    }

    @Override
    public void setMetric(Radius metric) {
        this.metric = metric;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        if (AreaUtils.verifyLimit(limitType, origin, end))
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
        this.rt = radiusType;
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
    public void shift(Point aim) {
        setEnd(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Point> targets) {
        for (Point p : targets)
        {
            if(rt.radius(origin.x, origin.y, p.x, p.y) + rt.radius(end.x, end.y, p.x, p.y) -
                    rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
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

        Point[] ts = targets.toArray(new Point[targets.size()]);
        Point[] exs = requiredExclusions.toArray(new Point[requiredExclusions.size()]);
        Point t;

        double[][][] compositeMap = new double[ts.length][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
        }
        DijkstraMap dt = new DijkstraMap(dungeon, dijkstra.measurement);
        Point tempPt = new Point(0, 0);
        for (int i = 0; i < exs.length; ++i) {
            t = rt.extend(origin, exs[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            List<Point> lit = Elias.line(origin, t, 0.4);

            for(Point p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            for (int x = 0; x < dungeon.length; x++) {
                tempPt.x = x;
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt.y = y;
                    dungeonCopy[x][y] = (dt.gradientMap[x][y] < DijkstraMap.FLOOR || !AreaUtils.verifyLimit(limitType, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        //t = rt.extend(origin, ts[0], length, false, dungeon.length, dungeon[0].length);

        for (int i = 0; i < ts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement);

            t = rt.extend(origin, ts[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            List<Point> lit = Elias.line(origin, t, 0.4);

            for(Point p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
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
            dm.setGoal(ts[i]);
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

        if(totalTargets == 0)
            return bestPoints;

        Point[] pts = priorityTargets.toArray(new Point[priorityTargets.size()]);
        Point[] lts = lesserTargets.toArray(new Point[lesserTargets.size()]);
        Point[] exs = requiredExclusions.toArray(new Point[requiredExclusions.size()]);
        Point t;// = rt.extend(origin, exs[0], length, false, dungeon.length, dungeon[0].length);

        double[][][] compositeMap = new double[totalTargets][dungeon.length][dungeon[0].length];

        char[][] dungeonCopy = new char[dungeon.length][dungeon[0].length],
                dungeonPriorities = new char[dungeon.length][dungeon[0].length];
        for (int i = 0; i < dungeon.length; i++) {
            System.arraycopy(dungeon[i], 0, dungeonCopy[i], 0, dungeon[i].length);
            Arrays.fill(dungeonPriorities[i], '#');
        }
        DijkstraMap dt = new DijkstraMap(dungeon, dijkstra.measurement);
        Point tempPt = new Point(0,0);
        for (int i = 0; i < exs.length; ++i) {
            t = rt.extend(origin, exs[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            List<Point> lit = Elias.line(origin, t, 0.4);

            for(Point p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);

            for (int x = 0; x < dungeon.length; x++) {
                tempPt.x = x;
                for (int y = 0; y < dungeon[x].length; y++) {
                    tempPt.y = y;
                    dungeonCopy[x][y] = (dt.gradientMap[x][y] < DijkstraMap.FLOOR  || !AreaUtils.verifyLimit(limitType, origin, tempPt)) ? '!' : dungeonCopy[x][y];
                }
            }
        }

        t = rt.extend(origin, pts[0], length, false, dungeon.length, dungeon[0].length);

        for (int i = 0; i < pts.length; ++i) {
            DijkstraMap dm = new DijkstraMap(dungeon, dijkstra.measurement);

            t = rt.extend(origin, pts[i], length, false, dungeon.length, dungeon[0].length);
            dt.resetMap();
            dt.clearGoals();
            List<Point> lit = Elias.line(origin, t, 0.4);

            for(Point p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
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
            dm.setGoal(pts[i]);
            dm.scan(null);
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
            List<Point> lit = Elias.line(origin, t, 0.4);

            for(Point p : lit)
            {
                dt.setGoal(p);
            }
            if(radius > 0)
                dt.partialScan(radius, null);


            double dist = 0.0;
            for (int x = 0; x < dungeon.length; x++) {
                for (int y = 0; y < dungeon[x].length; y++) {
                    if (dt.gradientMap[x][y] < DijkstraMap.FLOOR){
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
            dm.setGoal(lts[i - pts.length]);
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
            int volume = (int)(rt.radius(1, 1, dungeon.length - 2, dungeon[0].length - 2) * radius * 2.1);
            ArrayList<ArrayList<Point>> locs = new ArrayList<ArrayList<Point>>(totalTargets);
            for(int i = 0; i < totalTargets; i++)
            {
                locs.add(new ArrayList<Point>(volume));
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
                    for(Point tgt : targets)
                    {
                        if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) -
                            rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
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
                            for (int y = Math.max(1, it.y - radius / 2); y <= it.y + (radius - 1) / 2 && y < dungeon[0].length - 1; y++)
                            {
                                if(tested[x][y])
                                    continue;
                                tested[x][y] = true;

                                if(mayContainTarget(requiredExclusions, x, y))
                                    continue;

                                ctr = 0;
                                for(Point tgt : targets)
                                {
                                    if(rt.radius(origin.x, origin.y, tgt.x, tgt.y) + rt.radius(end.x, end.y, tgt.x, tgt.y) -
                                            rt.radius(origin.x, origin.y, end.x, end.y) <= 3.0 + radius)
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
        end = rt.extend(origin, end, length, false, map.length, map[0].length);
        dijkstra.resetMap();
        dijkstra.clearGoals();
    }

    @Override
    public LinkedHashMap<Point, Double> findArea() {
        double[][] dmap = initDijkstra();
        dmap[origin.x][origin.y] = DijkstraMap.DARK;
        dijkstra.resetMap();
        dijkstra.clearGoals();
        return AreaUtils.dijkstraToHashMap(dmap);
    }
}
