package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidgrid.Spill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.*;
import java.util.HashMap;

/**
 * An AOE type that has a center and a volume, and will randomly expand in all directions until it reaches volume or
 * cannot expand further. Specify the RadiusType as Radius.DIAMOND for Manhattan distance (and the best results),
 * RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean. You can specify a seed for the RNG and a fresh RNG will
 * be used for all random expansion; the RNG will reset to the specified seed after each generation so the same
 * CloudAOE can be used in different places by just changing the center. You can cause the CloudAOE to not reset after
 * generating each time by using setExpanding(true) and cause it to reset after the next generation by setting it back
 * to the default of false. If expanding is true, then multiple calls to getArea with the same center and larger volumes
 * will produce more coherent clumps of affected area with fewer gaps, and can be spaced out over multiple calls.
 *
 * This class uses squidpony.squidgrid.Spill to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class CloudAOE implements AOE {
    private Spill spill;
    private Point center;
    private int volume;
    private Spill.Measurement measurement;
    private long seed;
    private boolean expanding;
    public CloudAOE(Point center, int volume, Radius radiusType)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = center;
        this.volume = volume;
        this.expanding = false;
        switch (radiusType)
        {
            case CIRCLE: this.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case SQUARE: this.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.measurement = Spill.Measurement.MANHATTAN;
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
        switch (radiusType)
        {
            case CIRCLE: this.measurement = Spill.Measurement.EUCLIDEAN;
                break;
            case SQUARE: this.measurement = Spill.Measurement.CHEBYSHEV;
                break;
            default: this.measurement = Spill.Measurement.MANHATTAN;
                break;
        }
    }
    private CloudAOE()
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.spill = new Spill(new RNG(l));
        this.center = new Point(1, 1);
        this.volume = 1;
        this.measurement = Spill.Measurement.MANHATTAN;
        this.expanding = false;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public void setMap(char[][] map) {
        spill.initialize(map);
    }

    @Override
    public HashMap<Point, Double> findArea() {
        spill.start(center, volume, null);
        HashMap<Point, Double> r = AreaUtils.arrayToHashMap(spill.spillMap);
        if(!expanding)
         {
            spill.reset();
            spill.rng.setRandomness(new LightRNG(this.seed));
        }
        return r;
    }

    public boolean isExpanding() {
        return expanding;
    }

    public void setExpanding(boolean expanding) {
        this.expanding = expanding;
    }
}
