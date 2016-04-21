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
		final double div = Math.round(op / d);
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

    /**
     * Gets a copy of the 2D char array, source, that has the same data but shares no references with source.
     * @param source a 2D char array
     * @return a copy of source, or null if source is null
     */
	public static char[][] copy2D(char[][] source)
	{
		if(source == null)
			return null;
		if(source.length < 1)
			return new char[0][0];
		char[][] target = new char[source.length][source[0].length];
		for (int i = 0; i < source.length && i < target.length; i++) {
			System.arraycopy(source[i], 0, target[i], 0, source[i].length);
		}
		return target;
	}

    /**
     * Gets a copy of the 2D double array, source, that has the same data but shares no references with source.
     * @param source a 2D double array
     * @return a copy of source, or null if source is null
     */
	public static double[][] copy2D(double[][] source)
	{
		if(source == null)
			return null;
		if(source.length < 1)
			return new double[0][0];
		double[][] target = new double[source.length][source[0].length];
		for (int i = 0; i < source.length && i < target.length; i++) {
			System.arraycopy(source[i], 0, target[i], 0, source[i].length);
		}
		return target;
	}

    /**
     * Gets a copy of the 2D int array, source, that has the same data but shares no references with source.
     * @param source a 2D int array
     * @return a copy of source, or null if source is null
     */
    public static int[][] copy2D(int[][] source)
    {
        if(source == null)
            return null;
        if(source.length < 1)
            return new int[0][0];
        int[][] target = new int[source.length][source[0].length];
        for (int i = 0; i < source.length && i < target.length; i++) {
            System.arraycopy(source[i], 0, target[i], 0, source[i].length);
        }
        return target;
    }

    /**
     * Gets a copy of the 2D boolean array, source, that has the same data but shares no references with source.
     * @param source a 2D boolean array
     * @return a copy of source, or null if source is null
     */
    public static boolean[][] copy2D(boolean[][] source)
    {
        if(source == null)
            return null;
        if(source.length < 1)
            return new boolean[0][0];
        boolean[][] target = new boolean[source.length][source[0].length];
        for (int i = 0; i < source.length && i < target.length; i++) {
            System.arraycopy(source[i], 0, target[i], 0, source[i].length);
        }
        return target;
    }

}
