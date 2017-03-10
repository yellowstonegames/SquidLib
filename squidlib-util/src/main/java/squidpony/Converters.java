package squidpony;

import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.ArrayList;

/**
 * Ways to produce concrete implementations of StringConvert for various data structures.
 * Keeping the StringConvert producers separate from the data structures allows us to convert
 * JDK types as well as to keep the parts that need ObText, and thus RegExodus, separate from
 * the more general-use data structures.
 * Created by Tommy Ettinger on 3/9/2017.
 */
public class Converters {
    public static <K> StringConvert<OrderedSet<K>> convertOrderedSet(final StringConvert<K> convert)
    {
        return new StringConvert<OrderedSet<K>>() {
            @Override
            public String stringify(OrderedSet<K> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                for (int i = 0; i < item.size();) {
                    k = item.getAt(i);
                    if(item == k)
                        return "";
                    ObText.appendQuoted(sb, convert.stringify(k));
                    if(++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public OrderedSet<K> restore(String text) {
                ObText.ContentMatcher m = ObText.makeMatcher(text);
                OrderedSet<K> d = new OrderedSet<>();
                while (m.find())
                {
                    if(m.hasMatch())
                    {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }
    public static <K, V> StringConvert<OrderedMap<K, V>> convertOrderedMap(final StringConvert<K> convertK, final StringConvert<V> convertV)
    {
        return new StringConvert<OrderedMap<K, V>>() {
            @Override
            public String stringify(OrderedMap<K, V> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                V v;
                for (int i = 0; i < item.size();) {
                    k = item.keyAt(i);
                    if(k == item)
                        return "";
                    ObText.appendQuoted(sb, convertK.stringify(k));
                    sb.append(' ');
                    v = item.getAt(i);
                    if(v == item)
                        return "";
                    ObText.appendQuoted(sb, convertV.stringify(v));
                    if(++i < item.size())
                        sb.append('\n');
                }
                return sb.toString();
            }

            @Override
            public OrderedMap<K, V> restore(String text) {
                ObText.ContentMatcher m = ObText.makeMatcher(text);
                OrderedMap<K, V> d = new OrderedMap<>();
                String t;
                while (m.find())
                {
                    if(m.hasMatch())
                    {
                        t = m.getMatch();
                        if(m.find() && m.hasMatch())
                        {
                            d.put(convertK.restore(t), convertV.restore(m.getMatch()));
                        }
                    }
                }
                return d;
            }
        };
    }
    public static <K> StringConvert<ArrayList<K>> convertArrayList(final StringConvert<K> convert)
    {
        return new StringConvert<ArrayList<K>>() {
            @Override
            public String stringify(ArrayList<K> item) {
                StringBuilder sb = new StringBuilder(100);
                K k;
                for (int i = 0; i < item.size();) {
                    k = item.get(i);
                    if(item == k)
                        return "";
                    ObText.appendQuoted(sb, convert.stringify(k));
                    if(++i < item.size())
                        sb.append(' ');
                }
                return sb.toString();
            }

            @Override
            public ArrayList<K> restore(String text) {
                ObText.ContentMatcher m = ObText.makeMatcher(text);
                ArrayList<K> d = new ArrayList<>();
                while (m.find())
                {
                    if(m.hasMatch())
                    {
                        d.add(convert.restore(m.getMatch()));
                    }
                }
                return d;
            }
        };
    }

}
