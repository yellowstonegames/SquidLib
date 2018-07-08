package squidpony;

import squidpony.squidmath.*;

import java.util.*;

/**
 * Ways to produce concrete implementations of StringConvert for various data structures.
 * Keeping the StringConvert producers separate from the data structures allows us to convert
 * JDK types as well as to keep the parts that need ObText, and thus RegExodus, separate from
 * the more general-use data structures.
 * Created by Tommy Ettinger on 3/9/2017.
 */
@SuppressWarnings("unchecked")
public class Converters {

    public static void appendQuoted(StringBuilder sb, String text)
    {
        ObText.appendQuoted(sb, text);
    }

    public static ObText.ContentMatcher makeMatcher(CharSequence text)
    {
        return ObText.makeMatcherNoComments(text);
    }
    
    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("OrderedSet", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<OrderedSet<K>>(types) {
            @Override
            public String stringify(OrderedSet<K> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                for (int i = 0; i < item.size(); ) {
                    k = item.getAt(i);
                    if (item == k)
                        return "";
                    ObText.appendQuoted(sb, convert.stringify(k));
                    if (++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public OrderedSet<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                OrderedSet<K> d;
                if(convert.isArray)
                    d = new OrderedSet<>(CrossHash.generalHasher);
                else
                    d = new OrderedSet<>();
                while (m.find()) {
                    if (m.hasMatch()) {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final CharSequence type) {
        return convertOrderedSet((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final Class<K> type) {
        return convertOrderedSet((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }
    public static <K> StringConvert<UnorderedSet<K>> convertUnorderedSet(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("UnorderedSet", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<UnorderedSet<K>>(types) {
            @Override
            public String stringify(UnorderedSet<K> item) {
                StringBuilder sb = new StringBuilder(100);
                int i = 0;
                for (K k : item) {
                    if (item == k)
                        return "";
                    ObText.appendQuoted(sb, convert.stringify(k));
                    if (++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public UnorderedSet<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                UnorderedSet<K> d;
                if(convert.isArray)
                    d = new UnorderedSet<>(CrossHash.generalHasher);
                else
                    d = new UnorderedSet<>();
                while (m.find()) {
                    if (m.hasMatch()) {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

    public static <K> StringConvert<UnorderedSet<K>> convertUnorderedSet(final CharSequence type) {
        return convertUnorderedSet((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<UnorderedSet<K>> convertUnorderedSet(final Class<K> type) {
        return convertUnorderedSet((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final StringConvert<K> convertK, final StringConvert<V> convertV) {
        CharSequence[] types = StringConvert.asArray("OrderedMap", convertK.name, convertV.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<OrderedMap<K, V>>(types) {
            @Override
            public String stringify(OrderedMap<K, V> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                V v;
                for (int i = 0; i < item.size(); ) {
                    k = item.keyAt(i);
                    if (k == item)
                        return "";
                    appendQuoted(sb, convertK.stringify(k));
                    sb.append(' ');
                    v = item.getAt(i);
                    if (v == item)
                        return "";
                    appendQuoted(sb, convertV.stringify(v));
                    if (++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public OrderedMap<K, V> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                OrderedMap<K, V> d;
                if(convertK.isArray)
                    d = new OrderedMap<>(CrossHash.generalHasher);
                else
                    d = new OrderedMap<>();
                String t;
                while (m.find()) {
                    if (m.hasMatch()) {
                        t = m.getMatch();
                        if (m.find() && m.hasMatch()) {
                            d.put(convertK.restore(t), convertV.restore(m.getMatch()));
                        }
                    }
                }
                return d;
            }
        };
    }

    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final CharSequence typeK, final CharSequence typeV) {
        return convertOrderedMap((StringConvert<K>) StringConvert.get(typeK), (StringConvert<V>) StringConvert.get(typeV));
    }

    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final Class<K> typeK, final Class<V> typeV) {
        return convertOrderedMap((StringConvert<K>) StringConvert.get(typeK.getSimpleName()),
                (StringConvert<V>) StringConvert.get(typeV.getSimpleName()));
    }

    public static <K, V> StringConvert<UnorderedMap<K, V>> convertUnorderedMap(final StringConvert<K> convertK, final StringConvert<V> convertV) {
        CharSequence[] types = StringConvert.asArray("UnorderedMap", convertK.name, convertV.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<UnorderedMap<K, V>>(types) {
            @Override
            public String stringify(UnorderedMap<K, V> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                V v;
                Iterator<Map.Entry<K, V>> it = item.entrySet().iterator();
                Map.Entry<K, V> ent;
                while (it.hasNext())
                {
                    ent = it.next();
                    k = ent.getKey();
                    if (k == item)
                        return "";
                    appendQuoted(sb, convertK.stringify(k));
                    sb.append(' ');
                    v = ent.getValue();
                    if (v == item)
                        return "";
                    appendQuoted(sb, convertV.stringify(v));
                    if (it.hasNext())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public UnorderedMap<K, V> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                UnorderedMap<K, V> d;
                if(convertK.isArray)
                    d = new UnorderedMap<>(CrossHash.generalHasher);
                else
                    d = new UnorderedMap<>();
                String t;
                while (m.find()) {
                    if (m.hasMatch()) {
                        t = m.getMatch();
                        if (m.find() && m.hasMatch()) {
                            d.put(convertK.restore(t), convertV.restore(m.getMatch()));
                        }
                    }
                }
                return d;
            }
        };
    }

    public static <K, V> StringConvert<UnorderedMap<K, V>> convertUnorderedMap(final CharSequence typeK, final CharSequence typeV) {
        return convertUnorderedMap((StringConvert<K>) StringConvert.get(typeK), (StringConvert<V>) StringConvert.get(typeV));
    }

    public static <K, V> StringConvert<UnorderedMap<K, V>> convertUnorderedMap(final Class<K> typeK, final Class<V> typeV) {
        return convertUnorderedMap((StringConvert<K>) StringConvert.get(typeK.getSimpleName()),
                (StringConvert<V>) StringConvert.get(typeV.getSimpleName()));
    }

    public static <K> StringConvert<HashSet<K>> convertHashSet(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("HashSet", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<HashSet<K>>(types) {
            @Override
            public String stringify(HashSet<K> item) {
                StringBuilder sb = new StringBuilder(100);
                Iterator<K> it = item.iterator();
                K k;
                while (it.hasNext()){
                    k = it.next();
                    if (item == k)
                        return "";
                    ObText.appendQuoted(sb, convert.stringify(k));
                    if (it.hasNext())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public HashSet<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                HashSet<K> d = new HashSet<>();
                while (m.find()) {
                    if (m.hasMatch()) {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

    public static <K> StringConvert<HashSet<K>> convertHashSet(final CharSequence type) {
        return convertHashSet((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<HashSet<K>> convertHashSet(final Class<K> type) {
        return convertHashSet((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static <K, V> StringConvert<HashMap<K, V>> convertHashMap(final StringConvert<K> convertK, final StringConvert<V> convertV) {
        CharSequence[] types = StringConvert.asArray("HashMap", convertK.name, convertV.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<HashMap<K, V>>(types) {
            @Override
            public String stringify(HashMap<K, V> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                V v;
                Iterator<K> kit = item.keySet().iterator();
                Iterator<V> vit = item.values().iterator();
                while (kit.hasNext()) {
                    k = kit.next();
                    if (k == item)
                        return "";
                    appendQuoted(sb, convertK.stringify(k));
                    sb.append(' ');
                    v = vit.next();
                    if (v == item)
                        return "";
                    appendQuoted(sb, convertV.stringify(v));
                    if (kit.hasNext())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public HashMap<K, V> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                HashMap<K, V> d = new HashMap<>();
                String t;
                while (m.find()) {
                    if (m.hasMatch()) {
                        t = m.getMatch();
                        if (m.find() && m.hasMatch()) {
                            d.put(convertK.restore(t), convertV.restore(m.getMatch()));
                        }
                    }
                }
                return d;
            }
        };
    }

    public static <K, V> StringConvert<HashMap<K, V>> convertHashMap(final CharSequence typeK, final CharSequence typeV) {
        return convertHashMap((StringConvert<K>) StringConvert.get(typeK), (StringConvert<V>) StringConvert.get(typeV));
    }

    public static <K, V> StringConvert<HashMap<K, V>> convertHashMap(final Class<K> typeK, final Class<V> typeV) {
        return convertHashMap((StringConvert<K>) StringConvert.get(typeK.getSimpleName()),
                (StringConvert<V>) StringConvert.get(typeV.getSimpleName()));
    }

    public static <K> StringConvert<Arrangement<K>> convertArrangement(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("Arrangement", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination

        return new StringConvert<Arrangement<K>>(types) {
            @Override
            public String stringify(Arrangement<K> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                for (int i = 0; i < item.size(); ) {
                    k = item.keyAt(i);
                    if (item == k)
                        return "";
                    ObText.appendQuoted(sb, convert.stringify(k));
                    if (++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public Arrangement<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                Arrangement<K> d;
                if(convert.isArray)
                    d = new Arrangement<>(CrossHash.generalHasher);
                else
                    d = new Arrangement<>();
                while (m.find()) {
                    if (m.hasMatch()) {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

    public static <K> StringConvert<Arrangement<K>> convertArrangement(final CharSequence type) {
        return convertArrangement((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<Arrangement<K>> convertArrangement(final Class<K> type) {
        return convertArrangement((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }


    public static <K> StringConvert<ArrayList<K>> convertArrayList(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("ArrayList", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination
        return new StringConvert<ArrayList<K>>(types) {
            @Override
            public String stringify(ArrayList<K> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                for (int i = 0; i < item.size(); ) {
                    k = item.get(i);
                    if (item == k)
                        return "";
                    appendQuoted(sb, convert.stringify(k));
                    if (++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public ArrayList<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                ArrayList<K> d = new ArrayList<>();
                while (m.find()) {
                    if (m.hasMatch()) {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

    public static <K> StringConvert<ArrayList<K>> convertArrayList(final CharSequence type) {
        return convertArrayList((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<ArrayList<K>> convertArrayList(final Class<K> type) {
        return convertArrayList((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }
    public static <K> StringConvert<List<K>> convertList(final StringConvert<K> convert) {
        CharSequence[] types = StringConvert.asArray("List", convert.name);
        StringConvert found = StringConvert.lookup(types);
        if (found != null)
            return found; // in this case we've already created a StringConvert for this type combination
        return new StringConvert<List<K>>(types) {
            @Override
            public String stringify(List<K> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                for (int i = 0; i < item.size(); ) {
                    k = item.get(i);
                    if (item == k)
                        return "";
                    appendQuoted(sb, convert.stringify(k));
                    if (++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public ArrayList<K> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                ArrayList<K> d = new ArrayList<>();
                while (m.find()) {
                    if (m.hasMatch()) {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

    public static <K> StringConvert<List<K>> convertList(final CharSequence type) {
        return convertList((StringConvert<K>) StringConvert.get(type));
    }

    public static <K> StringConvert<List<K>> convertList(final Class<K> type) {
        return convertList((StringConvert<K>) StringConvert.get(type.getSimpleName()));
    }

    public static final StringConvert<Coord> convertCoord = new StringConvert<Coord>("Coord") {
        @Override
        public String stringify(Coord item) {
            if(item == null) return "n";
            return item.x + "," + item.y;
        }

        @Override
        public Coord restore(String text) {
            if(text == null || text.equals("n")) return null;
            return Coord.get(StringKit.intFromDec(text), StringKit.intFromDec(text, text.indexOf(',') + 1, text.length()));
        }
    };

    public static final StringConvert<Coord[]> convertArrayCoord = new StringConvert<Coord[]>(true,"Coord[]") {
        @Override
        public String stringify(Coord[] item) {
            if(item == null)
                return "N";
            int len = item.length;
            StringBuilder sb = new StringBuilder(len * 5);
            for (int i = 0; i < len; ) {
                if(item[i] == null)
                    sb.append('n');
                else
                    sb.append(item[i].x).append(',').append(item[i].y);
                if (++i < len)
                    sb.append(';');
            }
            return sb.toString();
        }

        @Override
        public Coord[] restore(String text) {
            if(text == null || text.equals("N"))
                return null;
            Coord[] coords = new Coord[StringKit.count(text, ';') + 1];
            int start = -1, end = text.indexOf(',');
            for (int i = 0; i < coords.length; i++) {
                if(text.charAt(start+1) == 'n') {
                    coords[i] = null;
                    start = text.indexOf(';', end + 1);
                }
                else
                    coords[i] = Coord.get(StringKit.intFromDec(text, start + 1, end),
                        StringKit.intFromDec(text, end + 1, (start = text.indexOf(';', end + 1))));
                end = text.indexOf(',', start + 1);
            }
            return coords;
        }
    };

    public static final StringConvert<GreasedRegion> convertGreasedRegion = new StringConvert<GreasedRegion>("GreasedRegion") {
        @Override
        public String stringify(GreasedRegion item) {
            return item.serializeToString();
        }

        @Override
        public GreasedRegion restore(String text) {
            return GreasedRegion.deserializeFromString(text);
        }
    };
    public static final StringConvert<IntVLA> convertIntVLA = new StringConvert<IntVLA>("IntVLA") {
        @Override
        public String stringify(IntVLA item) {
            return item.toString(",");
        }

        @Override
        public IntVLA restore(String text) {
            return IntVLA.deserializeFromString(text);
        }
    };

    public static final StringConvert<FakeLanguageGen> convertFakeLanguageGen = new StringConvert<FakeLanguageGen>("FakeLanguageGen")
    {
        @Override
        public String stringify(FakeLanguageGen item) {
            return item.serializeToString();
        }

        @Override
        public FakeLanguageGen restore(String text) {
            return FakeLanguageGen.deserializeFromString(text);
        }
    };

    public static final StringConvert<FakeLanguageGen.SentenceForm> convertSentenceForm = new StringConvert<FakeLanguageGen.SentenceForm>("FakeLanguageGen$SentenceForm")
    {
        @Override
        public String stringify(FakeLanguageGen.SentenceForm item) {
            return item.serializeToString();
        }

        @Override
        public FakeLanguageGen.SentenceForm restore(String text) {
            return FakeLanguageGen.SentenceForm.deserializeFromString(text);
        }
    };

    public static final StringConvert<MarkovText> convertMarkovText = new StringConvert<MarkovText>("MarkovText")
    {
        @Override
        public String stringify(MarkovText item) {
            return item.serializeToString();
        }

        @Override
        public MarkovText restore(String text) {
            return MarkovText.deserializeFromString(text);
        }
    };

    public static final StringConvert<ObText> convertObText = new StringConvert<ObText>("ObText") {
        @Override
        public String stringify(ObText item) {
            return item.serializeToString();
        }

        @Override
        public ObText restore(String text) {
            return ObText.deserializeFromString(text);
        }
    };

    public static final StringConvert<WeightedTable> convertWeightedTable = new StringConvert<WeightedTable>("WeightedTable") {
        @Override
        public String stringify(WeightedTable item) {
            return item.serializeToString();
        }

        @Override
        public WeightedTable restore(String text) {
            return WeightedTable.deserializeFromString(text);
        }
    };



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////// CORE JDK TYPES, PRIMITIVES, AND PRIMITIVE ARRAYS ARE THE ONLY TYPES AFTER THIS POINT
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Simple implementation to help when passing StringConverts around with data that is already a String.
     */
    public static final StringConvert<String> convertString = new StringConvert<String>("String") {
        @Override
        public String stringify(String item) {
            return item;
        }

        @Override
        public String restore(String text) {
            return text;
        }
    };

    public static final StringConvert<Boolean> convertBoolean = new StringConvert<Boolean>("Boolean") {
        @Override
        public String stringify(Boolean item) {
            return item == null ? "n" : item ? "1" : "0";
        }

        @Override
        public Boolean restore(String text) {
            char c;
            return (text == null || text.isEmpty() || (c = text.charAt(0)) == 'n') ? null : c == '1';
        }
    };

    public static final StringConvert<Byte> convertByte = new StringConvert<Byte>("Byte") {
        @Override
        public String stringify(Byte item) {
            return item.toString();
        }

        @Override
        public Byte restore(String text) {
            return Byte.decode(text);
        }
    };

    public static final StringConvert<Short> convertShort = new StringConvert<Short>("Short") {
        @Override
        public String stringify(Short item) {
            return item.toString();
        }

        @Override
        public Short restore(String text) {
            return Short.decode(text);
        }
    };

    public static final StringConvert<Integer> convertInt = new StringConvert<Integer>("Integer") {
        @Override
        public String stringify(Integer item) {
            return item.toString();
        }

        @Override
        public Integer restore(String text) {
            return Integer.decode(text);
        }
    };

    public static final StringConvert<Long> convertLong = new StringConvert<Long>("Long") {
        @Override
        public String stringify(Long item) {
            return item.toString();
        }

        @Override
        public Long restore(String text) {
            return Long.decode(text);
        }
    };

    public static final StringConvert<Float> convertFloat = new StringConvert<Float>("Float") {
        @Override
        public String stringify(Float item) {
            return item.toString();
        }

        @Override
        public Float restore(String text) {
            return Float.parseFloat(text);
        }
    };

    public static final StringConvert<Double> convertDouble = new StringConvert<Double>("Double") {
        @Override
        public String stringify(Double item) {
            return item.toString();
        }

        @Override
        public Double restore(String text) {
            return Double.parseDouble(text);
        }
    };

    public static final StringConvert<Character> convertChar = new StringConvert<Character>("Character") {
        @Override
        public String stringify(Character item) {
            return item.toString();
        }

        @Override
        public Character restore(String text) {
            return text.charAt(0);
        }
    };

    public static final StringConvert<boolean[]> convertArrayBoolean = new StringConvert<boolean[]>(true,"boolean[]") {
        @Override
        public String stringify(boolean[] item) {
            return StringKit.joinAlt(item);
        }

        @Override
        public boolean[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = text.length();
            if (amount <= 0) return new boolean[0];
            boolean[] splat = new boolean[amount];
            for (int i = 0; i < amount; i++) {
                splat[amount] = text.charAt(i) == '1';
            }
            return splat;
        }
    };

    public static final StringConvert<byte[]> convertArrayByte = new StringConvert<byte[]>(true,"byte[]") {
        @Override
        public String stringify(byte[] item) {
            if(item == null) return "N";
            return StringKit.join(",", item);
        }

        @Override
        public byte[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new byte[]{Byte.decode(text)};
            byte[] splat = new byte[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };


    public static final StringConvert<short[]> convertArrayShort = new StringConvert<short[]>(true,"short[]") {
        @Override
        public String stringify(short[] item) {
            if(item == null) return "N";
            return StringKit.join(",", item);
        }

        @Override
        public short[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new short[]{Short.decode(text)};
            short[] splat = new short[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };

    public static final StringConvert<int[]> convertArrayInt = new StringConvert<int[]>(true,"int[]") {
        @Override
        public String stringify(int[] item) {
            if(item == null) return "N";
            return StringKit.join(",", item);
        }

        @Override
        public int[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new int[]{Integer.decode(text)};
            int[] splat = new int[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };


    public static final StringConvert<long[]> convertArrayLong = new StringConvert<long[]>(true,"long[]") {
        @Override
        public String stringify(long[] item) {
            if(item == null) return "N";
            return StringKit.join(",", item);
        }

        @Override
        public long[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new long[]{Long.decode(text)};
            long[] splat = new long[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };

    public static final StringConvert<float[]> convertArrayFloat = new StringConvert<float[]>(true,"float[]") {
        @Override
        public String stringify(float[] item) {
            if(item == null) return "N";
            return StringKit.join(",", item);
        }

        @Override
        public float[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new float[]{Float.parseFloat(text)};
            float[] splat = new float[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };

    public static final StringConvert<double[]> convertArrayDouble = new StringConvert<double[]>(true,"double[]") {
        @Override
        public String stringify(double[] item) {
            if(item == null) return "N";
            return StringKit.join(",", item);
        }

        @Override
        public double[] restore(String text) {
            if(text == null || text.equals("N")) return null;
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new double[]{Double.parseDouble(text)};
            double[] splat = new double[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };


    public static final StringConvert<char[]> convertArrayChar = new StringConvert<char[]>(true,"char[]") {
        @Override
        public String stringify(char[] item) {
            if(item == null) return "";
            return String.valueOf(item);
        }

        @Override
        public char[] restore(String text) {
            return text.toCharArray();
        }
    };

    public static final StringConvert<boolean[][]> convertArrayBoolean2D = new StringConvert<boolean[][]>(true, "boolean[][]") {
        @Override
        public String stringify(boolean[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.joinAlt(item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.joinAlt(item[i]));
            }
            return sb.toString();
        }

        @Override
        public boolean[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new boolean[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            boolean[][] val = new boolean[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new boolean[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = end - start;
                    val[i] = new boolean[amount];
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = text.charAt(start + j) == '1';
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };

    public static final StringConvert<byte[][]> convertArrayByte2D = new StringConvert<byte[][]>(true, "byte[][]") {
        @Override
        public String stringify(byte[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.join(",", item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.join(",", item[i]));
            }
            return sb.toString();
        }

        @Override
        public byte[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new byte[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            byte[][] val = new byte[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new byte[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = StringKit.count(text, ",", start, end);
                    if (amount <= 0){
                        val[i] = new byte[]{Byte.decode(text)};
                        continue;
                    }
                    val[i] = new byte[amount + 1];
                    int dl = 1, idx = start - dl, idx2;
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
                    }
                    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                        val[i][amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
                    } else if(idx2 < end){
                        val[i][amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, idx2));
                    } else {
                        val[i][amount] = Byte.decode(StringKit.safeSubstring(text, idx + dl, end));
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };


    public static final StringConvert<short[][]> convertArrayShort2D = new StringConvert<short[][]>(true, "short[][]") {
        @Override
        public String stringify(short[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.join(",", item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.join(",", item[i]));
            }
            return sb.toString();
        }

        @Override
        public short[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new short[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            short[][] val = new short[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new short[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = StringKit.count(text, ",", start, end);
                    if (amount <= 0){
                        val[i] = new short[]{Short.decode(text)};
                        continue;
                    }
                    val[i] = new short[amount + 1];
                    int dl = 1, idx = start - dl, idx2;
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
                    }
                    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                        val[i][amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
                    } else if(idx2 < end){
                        val[i][amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, idx2));
                    } else {
                        val[i][amount] = Short.decode(StringKit.safeSubstring(text, idx + dl, end));
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };

    public static final StringConvert<int[][]> convertArrayInt2D = new StringConvert<int[][]>(true, "int[][]") {
        @Override
        public String stringify(int[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.join(",", item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.join(",", item[i]));
            }
            return sb.toString();
        }

        @Override
        public int[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new int[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            int[][] val = new int[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new int[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = StringKit.count(text, ",", start, end);
                    if (amount <= 0){
                        val[i] = new int[]{Integer.decode(text)};
                        continue;
                    }
                    val[i] = new int[amount + 1];
                    int dl = 1, idx = start - dl, idx2;
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
                    }
                    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                        val[i][amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
                    } else if(idx2 < end){
                        val[i][amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, idx2));
                    } else {
                        val[i][amount] = Integer.decode(StringKit.safeSubstring(text, idx + dl, end));
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };

    public static final StringConvert<long[][]> convertArrayLong2D = new StringConvert<long[][]>(true, "long[][]") {
        @Override
        public String stringify(long[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.join(",", item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.join(",", item[i]));
            }
            return sb.toString();
        }

        @Override
        public long[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new long[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            long[][] val = new long[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new long[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = StringKit.count(text, ",", start, end);
                    if (amount <= 0){
                        val[i] = new long[]{Long.decode(text)};
                        continue;
                    }
                    val[i] = new long[amount + 1];
                    int dl = 1, idx = start - dl, idx2;
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
                    }
                    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                        val[i][amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, text.length()));
                    } else if(idx2 < end){
                        val[i][amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, idx2));
                    } else {
                        val[i][amount] = Long.decode(StringKit.safeSubstring(text, idx + dl, end));
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };

    public static final StringConvert<float[][]> convertArrayFloat2D = new StringConvert<float[][]>(true, "float[][]") {
        @Override
        public String stringify(float[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.join(",", item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.join(",", item[i]));
            }
            return sb.toString();
        }

        @Override
        public float[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new float[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            float[][] val = new float[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new float[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = StringKit.count(text, ",", start, end);
                    if (amount <= 0){
                        val[i] = new float[]{Float.parseFloat(text)};
                        continue;
                    }
                    val[i] = new float[amount + 1];
                    int dl = 1, idx = start - dl, idx2;
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
                    }
                    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                        val[i][amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, text.length()));
                    } else if(idx2 < end){
                        val[i][amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, idx2));
                    } else {
                        val[i][amount] = Float.parseFloat(StringKit.safeSubstring(text, idx + dl, end));
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };

    public static final StringConvert<double[][]> convertArrayDouble2D = new StringConvert<double[][]>(true, "double[][]") {
        @Override
        public String stringify(double[][] item) {
            if(item == null)
                return "N";
            int len;
            if((len = item.length) <= 0)
                return "";
            StringBuilder sb = new StringBuilder(len * 128);
            if(item[0] == null)
                sb.append('n');
            else
                sb.append(StringKit.join(",", item[0]));
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                    sb.append(";n");
                else
                    sb.append(';').append(StringKit.join(",", item[i]));
            }
            return sb.toString();
        }

        @Override
        public double[][] restore(String text) {
            if(text == null) return null;
            int len;
            if((len = text.length()) <= 0) return new double[0][0];
            if(text.charAt(0) == 'N') return null;
            int width = StringKit.count(text, ';')+1;
            double[][] val = new double[width][];
            int start = 0, end = text.indexOf(';');
            for (int i = 0; i < width; i++) {
                if(start == end || start >= len) val[i] = new double[0];
                else if(text.charAt(start) == 'n') val[i] = null;
                else {
                    int amount = StringKit.count(text, ",", start, end);
                    if (amount <= 0){
                        val[i] = new double[]{Double.parseDouble(text)};
                        continue;
                    }
                    val[i] = new double[amount + 1];
                    int dl = 1, idx = start - dl, idx2;
                    for (int j = 0; j < amount; j++) {
                        val[i][j] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
                    }
                    if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                        val[i][amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, text.length()));
                    } else if(idx2 < end){
                        val[i][amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, idx2));
                    } else {
                        val[i][amount] = Double.parseDouble(StringKit.safeSubstring(text, idx + dl, end));
                    }
                }
                start = end+1;
                end = text.indexOf(';', start);
            }
            return val;
        }
    };


    public static final StringConvert<char[][]> convertArrayChar2D = new StringConvert<char[][]>(true, "char[][]") {
        @Override
        public String stringify(char[][] item) {
            int len, l2, sum;
            if (item == null) return "N"; // N for null
            if((len = item.length) <= 0) return "R0|0|";
            sum = l2 = item[0].length;
            char regular = 'R'; // R for rectangular
            for (int i = 1; i < len; i++) {
                if(item[i] == null)
                {
                    regular = 'J'; // J for jagged
                }
                else if(l2 != (l2 = item[i].length))
                {
                    regular = 'J';  // J for jagged
                    sum += l2;
                }
            }
            StringBuilder sb;
            if(regular == 'R')
            {
                sb = new StringBuilder(len * l2 + 15);
                sb.append('R').append(len).append('|').append(l2).append('|');
                for (int i = 0; i < len; i++) {
                    sb.append(item[i]);
                }
            }
            else
            {
                sb = new StringBuilder(len * 7 + sum + 8);
                sb.append('J').append(len).append('|');
                for (int i = 0; i < len; i++) {
                    if(item[i] == null)
                        sb.append("-|");
                    else
                        sb.append(item[i].length).append('|').append(item[i]);
                }
            }
            return sb.toString();
        }

        @Override
        public char[][] restore(String text) {
            if(text == null || text.length() <= 1) return null;
            if(text.charAt(0) == 'R')
            {
                int width, height, start = 1, end = text.indexOf('|');
                width = StringKit.intFromDec(text, start, end);
                start = end+1;
                end = text.indexOf('|', start);
                height = StringKit.intFromDec(text, start, end);
                start = end+1;
                char[][] val = new char[width][height];
                for (int i = 0; i < width; i++) {
                    text.getChars(start, start += height, val[i], 0);
                }
                return val;
            }
            else
            {
                int width, current, start = 1, end = text.indexOf('|');
                width = StringKit.intFromDec(text, start, end);
                start = end + 1;
                char[][] val = new char[width][];
                for (int i = 0; i < width; i++) {
                    end = text.indexOf('|', start);
                    if (text.charAt(start) == '-') {
                        val[i] = null;
                        start = end + 1;
                    } else {
                        current = StringKit.intFromDec(text, start, end);
                        start = end + 1;
                        val[i] = new char[current];
                        text.getChars(start, start += current, val[i], 0);
                    }
                }
                return val;
            }
        }
    };

}
