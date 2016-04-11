package squidpony;

import squidpony.squidmath.Coord;

import java.util.Iterator;

/**
 * Methods useful to be GWT compatible.
 * 
 * @author smelC
 */
public class GwtCompatibility {

	/**
	 * @param input
	 * @return A clone of {@code input}.
	 */
	public static Coord[] cloneCoords(Coord[] input) {
		final Coord[] result = new Coord[input.length];
		for (int i = 0; i < input.length; i++)
			result[i] = input[i];
		return result;
	}

	/**
	 * @param op
	 * @param d
	 * @return The remainder of {@code op / d}.
	 */
	/* smelC: because Math.IEEEremainder isn't GWT compatible */
	public static double IEEEremainder(double op, double d) {
		final double div = op / d;
		return op - (div * d);
	}

    /**
     * Gets the first item in an Iterable of T, or null if it is empty. Meant for collections like LinkedHashSet, which
     * can promise a stable first element but don't provide a way to access it. Not exactly a GWT compatibility method,
     * but more of a Java standard library stand-in.
     * @param collection an Iterable of T; if collection is null or empty this returns null
     * @param <T> any object type
     * @return the first element in collection, or null if it is empty or null itself
     */
	public static <T> T first(Iterable<T> collection)
    {
        if(collection == null)
            return null;
        Iterator<T> it = collection.iterator();
        if(it.hasNext())
            return it.next();
        return null;
    }

}
