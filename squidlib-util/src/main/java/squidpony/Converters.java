package squidpony;

import squidpony.squidmath.*;

import java.util.ArrayList;

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
                OrderedSet<K> d = new OrderedSet<>();
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
                        sb.append('\n');
                }
                return sb.toString();
            }

            @Override
            public OrderedMap<K, V> restore(String text) {
                ObText.ContentMatcher m = makeMatcher(text);
                OrderedMap<K, V> d = new OrderedMap<>();
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

    public static final StringConvert<Coord> convertCoord = new StringConvert<Coord>("Coord") {
        @Override
        public String stringify(Coord item) {
            return item.x + "," + item.y;
        }

        @Override
        public Coord restore(String text) {
            return Coord.get(StringKit.intFromDec(text), StringKit.intFromDec(text, text.indexOf(',') + 1, text.length()));
        }
    };

    public static final StringConvert<Coord[]> convertArrayCoord = new StringConvert<Coord[]>("Coord[]") {
        @Override
        public String stringify(Coord[] item) {
            int len = item.length;
            StringBuilder sb = new StringBuilder(len * 5);
            for (int i = 0; i < len; ) {
                sb.append(item[i].x).append(',').append(item[i].y);
                if (++i < len)
                    sb.append(';');
            }
            return sb.toString();
        }

        @Override
        public Coord[] restore(String text) {
            Coord[] coords = new Coord[StringKit.count(text, ';') + 1];
            int start = -1, end = text.indexOf(',');
            for (int i = 0; i < coords.length; i++) {
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
            return item.toString();
        }

        @Override
        public Boolean restore(String text) {
            return "true".equals(text);
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

    public static final StringConvert<boolean[]> convertArrayBoolean = new StringConvert<boolean[]>("boolean[]") {
        @Override
        public String stringify(boolean[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public boolean[] restore(String text) {
            int amount = StringKit.count(text, ",");
            if (amount <= 0) return new boolean[]{"true".equals(text)};
            boolean[] splat = new boolean[amount + 1];
            int dl = 1, idx = -dl, idx2;
            for (int i = 0; i < amount; i++) {
                splat[i] = "true".equals(StringKit.safeSubstring(text, idx + dl, idx = text.indexOf(',', idx + dl)));
            }
            if ((idx2 = text.indexOf(',', idx + dl)) < 0) {
                splat[amount] = "true".equals(StringKit.safeSubstring(text, idx + dl, text.length()));
            } else {
                splat[amount] = "true".equals(StringKit.safeSubstring(text, idx + dl, idx2));
            }
            return splat;
        }
    };

    public static final StringConvert<byte[]> convertArrayByte = new StringConvert<byte[]>("byte[]") {
        @Override
        public String stringify(byte[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public byte[] restore(String text) {
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


    public static final StringConvert<short[]> convertArrayShort = new StringConvert<short[]>("short[]") {
        @Override
        public String stringify(short[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public short[] restore(String text) {
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

    public static final StringConvert<int[]> convertArrayInt = new StringConvert<int[]>("int[]") {
        @Override
        public String stringify(int[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public int[] restore(String text) {
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


    public static final StringConvert<long[]> convertArrayLong = new StringConvert<long[]>("long[]") {
        @Override
        public String stringify(long[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public long[] restore(String text) {
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

    public static final StringConvert<float[]> convertArrayFloat = new StringConvert<float[]>("float[]") {
        @Override
        public String stringify(float[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public float[] restore(String text) {
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

    public static final StringConvert<double[]> convertArrayDouble = new StringConvert<double[]>("double[]") {
        @Override
        public String stringify(double[] item) {
            return StringKit.join(",", item);
        }

        @Override
        public double[] restore(String text) {
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


    public static final StringConvert<char[]> convertArrayChar = new StringConvert<char[]>("char[]") {
        @Override
        public String stringify(char[] item) {
            return String.valueOf(item);
        }

        @Override
        public char[] restore(String text) {
            return text.toCharArray();
        }
    };

    public static final StringConvert<char[][]> convertArrayChar2D = new StringConvert<char[][]>("char[][]") {
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
