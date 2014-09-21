package squidpony.squidgrid.fov.edgeaware;

import java.util.HashMap;
import squidpony.annotation.Beta;
import squidpony.squidgrid.DirectionIntercardinal;

/**
 * A minimal implementation of EdgeAwareCell.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class BasicEdgeAwareCell implements EdgeAwareCell {

    HashMap<String, HashMap<DirectionIntercardinal, Float>> resistances = new HashMap<>();

    @Override
    public float resistance(String key, DirectionIntercardinal edge) {
        return resistances.get(key).get(edge);
    }

    @Override
    public float resistance(DirectionIntercardinal edge) {
        return resistances.get(null).get(edge);
    }

    @Override
    public void setResistance(String key, DirectionIntercardinal edge, float resistance) {
        HashMap<DirectionIntercardinal, Float> map = resistances.get(key);
        if (map == null){
            map = new HashMap<>();
        }
        map.put(edge, resistance);
        resistances.put(key, map);
    }

    @Override
    public void setResistance(DirectionIntercardinal edge, float resistance) {
        setResistance(null, edge, resistance);
    }
}
