package squidpony.squidmath;

import java.util.Objects;

/**
 * A simple alternative to EnumMap that has a zero-argument constructor (which makes serialization easier) but is a
 * little less efficient. However, it supports more features by inheriting from {@link OrderedMap}, such as retrieval of
 * random keys or values with {@link #randomKey(RNG)} and  {@link #randomValue(RNG)}, iteration in insertion order
 * instead of always using enum declaration order (order can be shuffled with {@link #shuffle(RNG)} or reordered with
 * {@link #reorder(int...)}), and a little more. The implementation is nearly trivial due to how OrderedMap allows
 * customization of hashing strategy with its IHasher option, and this class always uses a specific custom IHasher to
 * hash Enum values by their ordinal.
 * <br>
 * Created by Tommy Ettinger on 10/21/2017.
 */
public class EnumOrderedMap<K extends Enum<K>, V> extends OrderedMap<K, V> {
    public static class EnumHasher implements CrossHash.IHasher
    {
        @Override
        public int hash(Object data) {
            return (data instanceof Enum) ? ((Enum)data).ordinal() : -1;
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return Objects.equals(left, right);
        }
    }
    public static final EnumHasher eh = new EnumHasher();
    public EnumOrderedMap()
    {
        super(16, 0.9375f, eh);
    }
    public EnumOrderedMap(Class<K> enumClass) {
        super(enumClass.getEnumConstants().length, 0.9375f, eh);
    }
    public EnumOrderedMap(K enumObject) {
        super(enumObject.getClass().getEnumConstants().length, 0.9375f, eh);
    }
}
