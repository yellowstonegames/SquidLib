package squidpony;

import regexodus.Matcher;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

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
                Matcher m = ObText.pattern.matcher(text);
                OrderedSet<K> d = new OrderedSet<>();
                while (m.find())
                {
                    if(m.isCaptured("s"))
                    {
                        d.add(convert.restore(m.group("s")));
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
                Matcher m = ObText.pattern.matcher(text);
                OrderedMap<K, V> d = new OrderedMap<>();
                String t;
                while (m.find())
                {
                    if(m.isCaptured("s"))
                    {
                        t = m.group("s");
                        if(m.find() && m.isCaptured("s"))
                        {
                            d.put(convertK.restore(t), convertV.restore(m.group("s")));
                        }
                    }
                }
                return d;
            }
        };
    }
}
