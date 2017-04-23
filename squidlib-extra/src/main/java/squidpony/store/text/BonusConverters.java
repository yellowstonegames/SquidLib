package squidpony.store.text;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import squidpony.Converters;
import squidpony.StringConvert;
import squidpony.StringKit;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.StatefulRandomness;

/**
 * Created by Tommy Ettinger on 4/22/2017.
 */
public class BonusConverters {
    public static final StringConvert<OrderedMap<Character, String>> convertMapCharString =
            Converters.convertOrderedMap(Converters.convertChar, Converters.convertString);
    public static final StringConvert<StatefulRNG> convertStatefulRNG = new StringConvert<StatefulRNG>("StatefulRNG") {
        @Override
        public String stringify(StatefulRNG item) {
            return StringKit.hex(item.getState()) + ':' + (item.getRandomness().getClass().getSimpleName());
        }

        @Override
        public StatefulRNG restore(String text) {
            long state = StringKit.longFromHex(text);
            try {
                StatefulRandomness sr = (StatefulRandomness) (ClassReflection.forName(text.substring(text.indexOf(':') + 1)).newInstance());
                sr.setState(state);
                return new StatefulRNG(sr);
            }catch (Exception re)
            {
                return new StatefulRNG(state);
            }
        }
    };
    public static final StringConvert<SpillWorldMap> convertSpillWorldMap = new StringConvert<SpillWorldMap>("SpillWorldMap") {
        @Override
        public String stringify(SpillWorldMap item) {
            return item.width
                    + "\t" + item.height
                    + "\t" + item.name
                    + '\t' + Converters.convertArrayInt2D.stringify(item.heightMap)
                    + '\t' + Converters.convertArrayCoord.stringify(item.mountains)
                    + '\t' + Converters.convertArrayChar2D.stringify(item.politicalMap)
                    + '\t' + convertMapCharString.stringify(item.atlas)
                    + '\t' + convertStatefulRNG.stringify(item.rng);
        }

        @Override
        public SpillWorldMap restore(String text) {
            int pos;
            SpillWorldMap swm = new SpillWorldMap(
                    Integer.decode(text.substring(0, (pos = text.indexOf('\t')))),
                    Integer.decode(text.substring(pos+1, (pos = text.indexOf('\t', pos+1)))),
                    text.substring(pos+1, (pos = text.indexOf('\t', pos+1)))
            );
            swm.heightMap = Converters.convertArrayInt2D.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1))));
            swm.mountains = Converters.convertArrayCoord.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1))));
            swm.politicalMap = Converters.convertArrayChar2D.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1))));
            swm.atlas.clear();
            swm.atlas.putAll(convertMapCharString.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1)))));
            swm.rng = convertStatefulRNG.restore(text.substring(pos+1));
            return swm;
        }
    };


}
