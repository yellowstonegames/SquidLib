package squidpony.store.text;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import squidpony.Converters;
import squidpony.FakeLanguageGen;
import squidpony.StringConvert;
import squidpony.StringKit;
import squidpony.squidgrid.mapping.PoliticalMapper;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.*;

import java.util.List;

/**
 * Created by Tommy Ettinger on 4/22/2017.
 */
public class BonusConverters {
    public static final StringConvert<OrderedMap<Character, String>> convertMapCharString =
            Converters.convertOrderedMap(Converters.convertChar, Converters.convertString);
    public static final StringConvert<List<FakeLanguageGen>> convertListLanguage = Converters.convertList(Converters.convertFakeLanguageGen);
    public static final StringConvert<OrderedMap<Character, List<FakeLanguageGen>>> convertMapCharListLanguage
            = Converters.convertOrderedMap(Converters.convertChar, convertListLanguage);
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

    public static final StringConvert<RNG> convertRNG = new StringConvert<RNG>("RNG") {
        @Override
        public String stringify(RNG item) {
            return "RNG:" + (item.getRandomness().getClass().getSimpleName());
        }

        @Override
        public RNG restore(String text) {
            try {
                RandomnessSource rs = (RandomnessSource) (ClassReflection.forName(text.substring(text.indexOf(':') + 1)).newInstance());
                return new RNG(rs);
            }catch (Exception re)
            {
                return new RNG();
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

    public static final StringConvert<PoliticalMapper> convertPoliticalMapper = new StringConvert<PoliticalMapper>("PoliticalMapper") {
        @Override
        public String stringify(PoliticalMapper item) {
            return item.width
                    + "\t" + item.height
                    + "\t" + item.name
                    + '\t' + Converters.convertArrayChar2D.stringify(item.politicalMap)
                    + '\t' + convertMapCharString.stringify(item.atlas)
                    + '\t' + convertMapCharListLanguage.stringify(item.spokenLanguages)
                    + '\t' + convertStatefulRNG.stringify(item.rng);
        }

        @Override
        public PoliticalMapper restore(String text) {
            int pos;
            PoliticalMapper pm = new PoliticalMapper();
            pm.width = Integer.decode(text.substring(0, (pos = text.indexOf('\t'))));
            pm.height = Integer.decode(text.substring(pos+1, (pos = text.indexOf('\t', pos+1))));
            pm.name = text.substring(pos+1, (pos = text.indexOf('\t', pos+1)));
            pm.politicalMap = Converters.convertArrayChar2D.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1))));
            pm.atlas.clear();
            pm.atlas.putAll(convertMapCharString.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1)))));
            pm.spokenLanguages.clear();
            pm.spokenLanguages.putAll(convertMapCharListLanguage.restore(text.substring(pos+1, (pos = text.indexOf('\t', pos+1)))));
            pm.rng = convertStatefulRNG.restore(text.substring(pos+1));
            return pm;
        }
    };


}
