package squidpony.squidai;

import squidpony.squidmath.Coord;

/**
 * A small class to store the area that a creature is perceived by other creatures to threaten.
 * Created by Tommy Ettinger on 11/8/2015.
 */
public class Threat {

    public Coord position;
    public int minThreatDistance;
    public int maxThreatDistance;

    public Threat(Coord position, int maxThreatDistance) {
        this.position = position;
        minThreatDistance = 0;
        this.maxThreatDistance = (maxThreatDistance < 0) ? 0 : maxThreatDistance;
    }

    public Threat(Coord position, int minThreatDistance, int maxThreatDistance) {
        this.position = position;
        this.minThreatDistance = (minThreatDistance < 0) ? 0 : minThreatDistance;
        this.maxThreatDistance = (this.minThreatDistance > maxThreatDistance)
                ? this.minThreatDistance : maxThreatDistance;
    }
}
