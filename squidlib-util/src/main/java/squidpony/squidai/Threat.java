package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;

import java.io.Serializable;

/**
 * A small class to store the area that a creature is perceived by other creatures to threaten.
 * Created by Tommy Ettinger on 11/8/2015.
 */
public class Threat implements Serializable {
    private static final long serialVersionUID = 1L;
    public Coord position;
    public Reach reach;

    public Threat(Coord position, int maxThreatDistance) {
        this.position = position;
        reach = new Reach(maxThreatDistance);
    }

    public Threat(Coord position, int minThreatDistance, int maxThreatDistance) {
        this.position = position;
        reach = new Reach(minThreatDistance, maxThreatDistance);
    }
    public Threat(Coord position, int minThreatDistance, int maxThreatDistance, Radius measurement) {
        this.position = position;
        reach = new Reach(minThreatDistance, maxThreatDistance, measurement);
    }
    public Threat(Coord position, int minThreatDistance, int maxThreatDistance, Radius measurement, AimLimit limits) {
        this.position = position;
        reach = new Reach(minThreatDistance, maxThreatDistance, measurement, limits);
    }
}
