package squidpony.store.text;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import squidpony.*;
import squidpony.annotation.Beta;
import squidpony.squidgrid.mapping.PoliticalMapper;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.*;

import java.util.List;

import static squidpony.Converters.*;

/**
 * Created by Tommy Ettinger on 4/22/2017.
 */
@Beta
@SuppressWarnings("unchecked")
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
                StatefulRandomness sr = (StatefulRandomness) ClassReflection.newInstance(ClassReflection.forName(text.substring(text.indexOf(':') + 1)));
                sr.setState(state);
                return new StatefulRNG(sr);
            }catch (Exception re)
            {
                return new StatefulRNG(state);
            }
        }
    };
    public static final StringConvert<StatefulRandomness> convertStatefulRandomness = new StringConvert<StatefulRandomness>("StatefulRandomness") {
        @Override
        public String stringify(StatefulRandomness item) {
            return StringKit.hex(item.getState()) + ':' + (item.getClass().getSimpleName());
        }

        @Override
        public StatefulRandomness restore(String text) {
            long state = StringKit.longFromHex(text);
            try {
                StatefulRandomness sr = (StatefulRandomness) ClassReflection.newInstance(ClassReflection.forName(text.substring(text.indexOf(':') + 1)));
                sr.setState(state);
                return sr;
            }catch (Exception re)
            {
                return new LinnormRNG(state);
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
                RandomnessSource rs = (RandomnessSource) ClassReflection.newInstance(ClassReflection.forName(text.substring(text.indexOf(':') + 1)));
                return new RNG(rs);
            }catch (Exception re)
            {
                return new RNG();
            }
        }
    };
    public static <K> StringConvert<ProbabilityTable<K>> convertProbabilityTable(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("ProbabilityTable", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination
        final StringConvert<Arrangement<K>> convertArrange = Converters.convertArrangement(convert);
        return new StringConvert<ProbabilityTable<K>>(types) {
            @Override
            public String stringify(ProbabilityTable<K> item) {
                StringBuilder sb = new StringBuilder(256);
                appendQuoted(sb, convertStatefulRandomness.stringify(item.getRandom()));
                sb.append(' ');
                appendQuoted(sb, convertIntVLA.stringify(item.weights));
                sb.append(' ');
                appendQuoted(sb, convertArrange.stringify(item.table));
                for (int i = 0; i < item.extraTable.size(); i++) {
                    sb.append(' ');
                    appendQuoted(sb, stringify(item.extraTable.get(i)));
                }
                return sb.toString();
            }

            @Override
            public ProbabilityTable<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                if(!m.find() || !m.hasMatch())
                    return null;
                ProbabilityTable<K> pt = new ProbabilityTable<>(convertStatefulRandomness.restore(m.getMatch()));
                if(!m.find() || !m.hasMatch())
                    return pt;
                pt.weights.addAll(convertIntVLA.restore(m.getMatch()));
                if(!m.find() || !m.hasMatch())
                {
                    pt.weights.clear();
                    return pt;
                }
                pt.table.putAll(convertArrange.restore(m.getMatch()));
                while (m.find()) {
                    if (m.hasMatch()) {
                        pt.extraTable.add(restore(m.getMatch()));
                    }
                }
                return pt;
            }
        };
    }

    public static <K> StringConvert<ProbabilityTable<K>> convertProbabilityTable(final CharSequence type) {
        return convertProbabilityTable((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<ProbabilityTable<K>> convertProbabilityTable(final Class<K> type) {
        return convertProbabilityTable((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

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
