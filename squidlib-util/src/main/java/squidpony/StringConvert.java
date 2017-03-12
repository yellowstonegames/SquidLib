package squidpony;

/**
 * Used to standardize conversion for a given type, {@code T}, to and from a serialized String format.
 * This abstract class should usually be made concrete by a single-purpose class (not the type T itself).
 */
public abstract class StringConvert<T> {
    public StringConvert() {};
    public abstract String stringify(T item);
    public abstract T restore(String text);
}
