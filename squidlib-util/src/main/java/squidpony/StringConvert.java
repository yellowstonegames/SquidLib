package squidpony;

import squidpony.annotation.Beta;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.K2;

/**
 * Used to standardize conversion for a given type, {@code T}, to and from a serialized String format.
 * This abstract class should usually be made concrete by a single-purpose class (not the type T itself).
 * Also includes a static registry of types as Class arrays (including the classes of generic type parameters in array
 * elements after the first) to the  corresponding StringConvert objects that have been constructed for those types,
 * although the registry must store the StringConvert objects without any further types (you can cast the StringConvert
 * to a StringConvert with the desired generic type, or call {@link #restore(String)} on the un-parametrized type and
 * get back an Object that can be cast to the correct type, but we aren't able to store the actual type, other than the
 * Class array used to look up a StringConvert). The static method {@link #lookup(Class[])} can be used to find the
 * StringConvert registered for a type combination. The static method {@link #get(CharSequence)} can be used to find a
 * StringConvert by its name. The static utility method {@link #asArray(Class[])} can be used to help reduce the amount
 * of arrays produced by varargs, but the array it returns must not be edited once used to construct a StringConvert.
 */
@Beta
public abstract class StringConvert<T> {
    protected final String name;
    protected final Class[] classes;

    /**
     * Constructs a StringConvert using a vararg or array of Class objects. If an array is passed, it must not be
     * altered after usage. If no arguments are passed, if types is null, or if the first item of types is null, then
     * this uses a special class array consisting of {@link Void#TYPE}, and the name will be "void". If types has length
     * 1, then the name will be the "simple name" of the first element in types, as produced by
     * {@link Class#getSimpleName()} (note that this produces an empty string for anonymous classes). Otherwise, this
     * considers items after the first to be generic type arguments of the first, and currently does not consider nested
     * generic type parameters (this will be addressed later, if possible). In this last case, the name uses Java
     * conventions for generics, turning the call {@code new StringConvert(OrderedMap.class, String.class, Long.class)}
     * into a StringConvert with the name {@code "OrderedMap<String,Long>"} (no spaces will be present in the name, but
     * thanks to some customization of the registry, you can give a String with spaces in it to
     * {@link #get(CharSequence)} and still find the correct one).
     * @param types a vararg of Class objects representing the type this can convert, including generic type parameters
     *              of the first element, if there are any, at positions after the first
     */
    public StringConvert(final Class... types) {
        if(types == null || types.length <= 0 || types[0] == null)
        {
            name = "void";
            classes = new Class[]{Void.TYPE};
        }
        else if(types.length == 1) {
            name = types[0].getSimpleName();
            classes = types;
        }
        else
        {
            StringBuilder sb = new StringBuilder(64);
            sb.append(types[0].getSimpleName()).append('<').append(types[1].getSimpleName());
            for (int i = 2; i < types.length; i++) {
                sb.append(',').append(types[i].getSimpleName());
            }
            name = sb.append('>').toString();
            classes = types;
        }

    }
    public String getName() {return name;}
    public abstract String stringify(T item);
    public abstract T restore(String text);

    public static StringConvert<?> get(final CharSequence name)
    {
        int i = registry.indexOfA(name);
        if(i < 0) return null;
        return registry.getAAt(i);
    }
    /**
     * Looks up the StringConvert for a given vararg of Class instances (if an array of Classes is used other than a
     * vararg, it must not be altered in the future, nor reused in a way that modifies its elements). Returns null if no
     * StringConvert is found. You should usually cast the returned StringConvert, if non-null, to the specific
     * StringConvert generic type you want.
     * @param types the vararg of types to look up
     * @return the StringConvert registered for the given types, or null if none has been made
     */
    public static StringConvert<?> lookup(final Class... types)
    {
        return registry.getAFromB(types);
    }

    /**
     * Simply takes a vararg of Class and returns it as an array.
     * Can be handy to avoid re-creating arrays implicitly from varargs of Class items.
     * @param types a vararg of Class
     * @return types as an array
     */
    public static Class[] asArray(final Class... types)
    {
        return types;
    }

    public static final CrossHash.IHasher spaceIgnoringHasher = new CrossHash.IHasher() {
        @Override
        public int hash(Object data) {
            if (data == null || !(data instanceof StringConvert || data instanceof CharSequence))
                return 0;
            final CharSequence s;
            if(data instanceof StringConvert) s = ((StringConvert) data).getName();
            else s = (CharSequence) data;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = s.length();
            for (int i = 0; i < len; i++) {
                if(s.charAt(i) != ' ')
                    result += (a ^= 0x85157AF5 * s.charAt(i));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            if (left == right) return true;
            if(!((left instanceof StringConvert || left instanceof CharSequence)
                    && (right instanceof StringConvert || right instanceof CharSequence)))
                return false;
            final CharSequence ls, rs;
            if(left instanceof StringConvert) ls = ((StringConvert) left).getName();
            else ls = (CharSequence)left;
            if(right instanceof StringConvert) rs = ((StringConvert) right).getName();
            else rs = (CharSequence)right;
            final int llen = ls.length(), rlen = rs.length();
            char lc = ' ', rc = ' ';
            for (int l = 0, r = 0; l < llen && r < rlen; l++, r++) {
                while (l < llen && (lc = ls.charAt(l)) == ' ') ++l;
                while (r < rlen && (rc = rs.charAt(r)) == ' ') ++r;
                if(lc != rc)
                    return false;
            }
            return true;
        }
    };
    public static final K2<StringConvert, Class[]> registry =
            new K2<>(128, 0.75f, spaceIgnoringHasher, CrossHash.objectArrayHasher);


}
