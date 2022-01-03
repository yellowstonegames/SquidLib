package squidpony.squidmath;

/**
 * A simple alternative to EnumMap that has a zero-argument constructor (which makes serialization easier) but is a
 * little less efficient. However, it supports more features by inheriting from {@link OrderedMap}, such as retrieval of
 * random keys or values with {@link #randomKey(IRNG)} and  {@link #randomValue(IRNG)}, iteration in insertion order
 * instead of always using enum declaration order (order can be shuffled with {@link #shuffle(IRNG)} or reordered with
 * {@link #reorder(int...)}), and a little more. The implementation is nearly trivial due to how OrderedMap allows
 * customization of hashing strategy with its IHasher option, and this class always uses a specific custom IHasher to
 * hash Enum values by their ordinal. This IHasher is shared with {@link EnumOrderedSet}.
 * <br>
 * Created by Tommy Ettinger on 10/21/2017.
 */
public class EnumOrderedMap<K extends Enum<?>, V> extends OrderedMap<K, V> {
    public EnumOrderedMap()
    {
        super(16, 0.9375f, HashCommon.enumHasher);
    }
    public EnumOrderedMap(Class<K> enumClass) {
        super(enumClass.getEnumConstants().length, 0.9375f, HashCommon.enumHasher);
    }
    public EnumOrderedMap(K enumObject) {
        super(enumObject.getClass().getEnumConstants().length, 0.9375f, HashCommon.enumHasher);
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        int n = size(), i = 0;
        boolean first = true;
        s.append("EnumOrderedMap{");
        while (i < n) {
            if (first) first = false;
            else s.append(", ");
            s.append(entryAt(i++));
        }
        s.append("}");
        return s.toString();
    }
}
