package squidpony.squidai;

import squidpony.squidgrid.Radius;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tommy Ettinger on 7/27/2015.
 */
public class Technique {
    public String name;
    public int minRange;
    public int maxRange;
    public AOE aoe;
    public Radius radiusType;
    private char[][] dungeon;
    private final static Point DEFAULT_POINT = new Point(0, 0);

    public Technique(String name, int range) {
        this.name = name;
        this.minRange = range;
        this.maxRange = range;
        this.aoe = new PointAOE(DEFAULT_POINT);
        this.radiusType = Radius.SQUARE;
    }

    public Technique(String name, int minRange, int maxRange) {
        this.name = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.aoe = new PointAOE(DEFAULT_POINT);
        this.radiusType = Radius.SQUARE;
    }

    public Technique(String name, int minRange, int maxRange, AOE aoe) {
        this.name = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.aoe = aoe;
        this.radiusType = Radius.SQUARE;
    }

    public Technique(String name, int minRange, int maxRange, AOE aoe, Radius radiusType) {
        this.name = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.aoe = aoe;
        this.radiusType = radiusType;
    }

    public void setMap(char[][] map)
    {
        this.dungeon = map;
        this.aoe.setMap(map);
    }

    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Point user, Set<Point> targets, Set<Point> requiredExclusions) {
        LinkedHashMap<Point, ArrayList<Point>> area = aoe.idealLocations(targets, requiredExclusions),
                r = new LinkedHashMap<Point, ArrayList<Point>>();
        for (Point shifter : area.keySet())
        {
            double dist = radiusType.radius(user.x, user.y, shifter.x, shifter.y);
            if(dist >= minRange && dist <= maxRange)
                r.put(shifter, area.get(shifter));
        }
        return  r;
    }

    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Point user, Set<Point> priorityTargets, Set<Point> lesserTargets, Set<Point> requiredExclusions) {
        LinkedHashMap<Point, ArrayList<Point>> area = aoe.idealLocations(priorityTargets, lesserTargets, requiredExclusions),
                r = new LinkedHashMap<Point, ArrayList<Point>>();
        for (Point shifter : area.keySet())
        {
            double dist = radiusType.radius(user.x, user.y, shifter.x, shifter.y);
            if(dist >= minRange && dist <= maxRange)
                r.put(shifter, area.get(shifter));
        }
        return  r;
    }
    public LinkedHashMap<Point, Double> apply(Point user, Point aimAt)
    {
        double dist = radiusType.radius(user.x, user.y, aimAt.x, aimAt.y);
        LinkedHashMap<Point, Double> r = new LinkedHashMap<Point, Double>();
        if(dist >= minRange && dist <= maxRange)
        {
            aoe.shift(aimAt);
            r = aoe.findArea();
        }
        return r;
    }
}