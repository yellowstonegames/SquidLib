package squidpony;

import squidpony.annotation.Beta;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.K2;

import java.util.Objects;

/**
 * Used to standardize conversion for a given type, {@code T}, to and from a serialized String format.
 * This abstract class should usually be made concrete by a single-purpose class (not the type T itself).
 * Also includes a static registry of types as CharSequence arrays (including the classes of generic type parameters in
 * array elements after the first) to the  corresponding StringConvert objects that have been constructed for those
 * types, although the registry must store the StringConvert objects without any further types (you can cast the
 * StringConvert to a StringConvert with the desired generic type, or call {@link #restore(String)} on the
 * un-parametrized type and get back an Object that can be cast to the correct type, but we aren't able to store the
 * actual type). The static method {@link #lookup(CharSequence[])} can be used to find the StringConvert registered for
 * a type name combination. The static method {@link #get(CharSequence)} can be used to find a StringConvert by its
 * name. The static utility method {@link #asArray(CharSequence[])} can be used to reduce the amount of arrays produced
 * by varargs, especially when you have a bunch of Class items and need Strings, but the array it returns must not be
 * edited once used to construct a StringConvert.
 */
@Beta
public abstract class StringConvert<T> {
    public final CharSequence name;
    public final CharSequence[] typeNames;

    /**
     * Constructs a StringConvert using a vararg or array of CharSequence objects, such as Strings. If an array is
     * passed, it must not be altered after usage. If no arguments are passed, if types is null, or if the first item of
     * types is null, then this uses a special type representation where the name is "void" and typeNames has "void" as
     * its only element. If types has length 1, then the name will be the "simple name" of the first element in types,
     * as produced by {@link Class#getSimpleName()} (note that this produces an empty string for anonymous classes), and
     * typeNames will again have that simple name as its only value. Otherwise, this considers items after the first to
     * be the names of generic type arguments of the first, using normal Java syntax of {@code "Outer<A,B>"} if given
     * the Strings for types {@code "Outer", "A", "B"}. No spaces will be present in the name, but thanks to some
     * customization of the registry, you can give a String with spaces in it to {@link #get(CharSequence)} and still
     * find the correct one). You can give type names with generic components as the names of generic type arguments,
     * such as {@code new StringConvert("OrderedMap", "String", "OrderedSet<String>")} for a mapping of String keys to
     * values that are themselves sets of Strings. After constructing a StringConvert, it is automatically registered
     * so it can be looked up by name with {@link #get(CharSequence)} or by component generic types with
     * {@link #lookup(CharSequence...)}; both of these will not return a StringConvert with type info for what it
     * takes and returns beyond "Object", but the result can be cast to a StringConvert with the correct type.
     * @param types a vararg of Class objects representing the type this can convert, including generic type parameters
     *              of the first element, if there are any, at positions after the first
     */
    public StringConvert(final CharSequence... types) {
        if(types == null || types.length <= 0 || types[0] == null)
        {
            name = "void";
            typeNames = new String[]{"void"};
        }
        else if(types.length == 1) {
            name = types[0];
            typeNames = types;
        }
        else
        {
            name = new StringBuilder(64);
            ((StringBuilder)name).append(types[0]).append('<').append(types[1]);
            for (int i = 2; i < types.length; i++) {
                ((StringBuilder)name).append(',').append(types[i]);
            }
            ((StringBuilder)name).append('>');
            typeNames = types;
        }

    }
    public CharSequence getName() {return name;}
    public abstract String stringify(T item);
    public abstract T restore(String text);

    /**
     * Gets the registered StringConvert for the given type name, if there is one, or returns null otherwise.
     * The name can have the normal parts of a generic type, such as "OrderedMap&lt;String, ArrayList&lt;String&gt;&gt;",
     * as long as such a type was fully registered. For that example, you could use
     * {@code Converters.convertOrderedMap(Converters.convertString, Converters.convertArrayList(Converters.convertString))}
     * to produce and register a StringConvert for the aforementioned generic type.
     * @param name the name of the type to find a registered StringConvert, such as "ArrayList&lt;String&gt;" or "char[]"
     * @return the registered StringConvert, if it was found, or null if none was found
     */
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
    public static StringConvert<?> lookup(final CharSequence... types)
    {
        return registry.getAFromB(types);
    }

    /**
     * Simply takes a vararg of Class and returns the simple names of the Classes as a String array.
     * Can be handy to avoid re-creating arrays implicitly from varargs of Class items.
     * @param types a vararg of Class
     * @return the String simple names of types as an array
     */
    public static CharSequence[] asArray(final CharSequence... types)
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
            char c;
            for (int i = 0; i < len; i++) {
                if((c = s.charAt(i)) != ' ')
                    result += (a ^= 0x85157AF5 * c);
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

    public static final CrossHash.IHasher spaceIgnoringArrayHasher = new CrossHash.IHasher() {
        @Override
        public int hash(final Object data) {
            if (data == null)
                return 0;
            if(!(data instanceof CharSequence[]))
                return data.hashCode();
            CharSequence[] data2 = (CharSequence[])data;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data2.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * spaceIgnoringHasher.hash(data2[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof CharSequence[] && right instanceof CharSequence[]) ? CrossHash.equalityHelper((CharSequence[]) left, (CharSequence[]) right, spaceIgnoringHasher) : Objects.equals(left, right));
        }
    };

    public static final K2<StringConvert, CharSequence[]> registry =
            new K2<>(128, 0.75f, spaceIgnoringHasher, spaceIgnoringArrayHasher);


}
