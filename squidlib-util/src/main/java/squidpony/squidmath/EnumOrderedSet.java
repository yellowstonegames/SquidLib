package squidpony.squidmath;

/**
 * A simple Set of Enum items (which are already unique if used in a normal Set) that keeps insertion order. By
 * inheriting from {@link OrderedSet}, it gets features not present in normal JDK Sets, such as retrieval of random
 * items with {@link #randomItem(RNG)}, iteration in insertion order instead of always using enum declaration order
 * (order can be shuffled with {@link #shuffle(RNG)} or reordered with {@link #reorder(int...)}), and a little more. The
 * implementation is nearly trivial due to how OrderedMap allows customization of hashing strategy with its IHasher
 * option, and this class always uses a specific custom IHasher to hash Enum values by their ordinal. This IHasher is
 * shared with {@link EnumOrderedMap}.
 * <br>
 * Created by Tommy Ettinger on 10/21/2017.
 */
public class EnumOrderedSet<K extends Enum<?>> extends OrderedSet<K> {
    public EnumOrderedSet()
    {
        super(16, 0.9375f, HashCommon.enumHasher);
    }
    public EnumOrderedSet(Class<K> enumClass) {
        super(enumClass.getEnumConstants().length, 0.9375f, HashCommon.enumHasher);
    }
    public EnumOrderedSet(K enumObject) {
        super(enumObject.getClass().getEnumConstants().length, 0.9375f, HashCommon.enumHasher);
    }


    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        int n = size(), i = 0;
        boolean first = true;
        s.append("EnumOrderedSet{");
        while (i < n) {
            if (first) first = false;
            else s.append(", ");
            s.append(getAt(i++));
        }
        s.append("}");
        return s.toString();
    }
}
