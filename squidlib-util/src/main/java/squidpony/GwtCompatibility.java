package squidpony;

import squidpony.squidmath.Coord;

import java.util.Arrays;
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

    /**
     * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
     * Modifies target in-place and also returns target for chaining.
     * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
     * @param source a 2D char array that will be copied and inserted into target
     * @param target a 2D char array that will be modified by receiving as much of source as it can hold
     * @param x the x position in target to receive the items from the first cell in source
     * @param y the y position in target to receive the items from the first cell in source
     * @return a modified copy of target with source inserted into it at the given position
     */
    public static char[][] insert2D(char[][] source, char[][] target, int x, int y)
    {
        if(source == null || target == null)
            return target;
        if(source.length < 1 || source[0].length < 1)
            return copy2D(target);
        for (int i = 0; i < source.length && x + i < target.length; i++) {
            System.arraycopy(source[i], 0, target[x + i], y, Math.min(source[i].length, target[x+i].length - y));
        }
        return target;
    }
    /**
     * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
     * Modifies target in-place and also returns target for chaining.
     * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
     * @param source a 2D double array that will be copied and inserted into target
     * @param target a 2D double array that will be modified by receiving as much of source as it can hold
     * @param x the x position in target to receive the items from the first cell in source
     * @param y the y position in target to receive the items from the first cell in source
     * @return a modified copy of target with source inserted into it at the given position
     */
    public static double[][] insert2D(double[][] source, double[][] target, int x, int y)
    {
        if(source == null || target == null)
            return target;
        if(source.length < 1 || source[0].length < 1)
            return copy2D(target);
        for (int i = 0; i < source.length && x + i < target.length; i++) {
            System.arraycopy(source[i], 0, target[x + i], y, Math.min(source[i].length, target[x+i].length - y));
        }
        return target;
    }
    /**
     * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
     * Modifies target in-place and also returns target for chaining.
     * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
     * @param source a 2D int array that will be copied and inserted into target
     * @param target a 2D int array that will be modified by receiving as much of source as it can hold
     * @param x the x position in target to receive the items from the first cell in source
     * @param y the y position in target to receive the items from the first cell in source
     * @return a modified copy of target with source inserted into it at the given position
     */
    public static int[][] insert2D(int[][] source, int[][] target, int x, int y)
    {
        if(source == null || target == null)
            return target;
        if(source.length < 1 || source[0].length < 1)
            return copy2D(target);
        for (int i = 0; i < source.length && x + i < target.length; i++) {
            System.arraycopy(source[i], 0, target[x + i], y, Math.min(source[i].length, target[x+i].length - y));
        }
        return target;
    }
    /**
     * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
     * Modifies target in-place and also returns target for chaining.
     * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
     * @param source a 2D boolean array that will be copied and inserted into target
     * @param target a 2D boolean array that will be modified by receiving as much of source as it can hold
     * @param x the x position in target to receive the items from the first cell in source
     * @param y the y position in target to receive the items from the first cell in source
     * @return a modified copy of target with source inserted into it at the given position
     */
    public static boolean[][] insert2D(boolean[][] source, boolean[][] target, int x, int y)
    {
        if(source == null || target == null)
            return target;
        if(source.length < 1 || source[0].length < 1)
            return copy2D(target);
        for (int i = 0; i < source.length && x + i < target.length; i++) {
            System.arraycopy(source[i], 0, target[x + i], y, Math.min(source[i].length, target[x+i].length - y));
        }
        return target;
    }

    /**
     * Creates a 2D array of the given width and height, filled with entirely with the value contents.
     * @param contents the value to fill the array with
     * @param width the desired width
     * @param height the desired height
     * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
     */
    public static char[][] fill2D(char contents, int width, int height)
    {
        char[][] next = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(next[x], contents);
        }
        return next;
    }
    /**
     * Creates a 2D array of the given width and height, filled with entirely with the value contents.
     * @param contents the value to fill the array with
     * @param width the desired width
     * @param height the desired height
     * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
     */
    public static double[][] fill2D(double contents, int width, int height)
    {
        double[][] next = new double[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(next[x], contents);
        }
        return next;
    }
    /**
     * Creates a 2D array of the given width and height, filled with entirely with the value contents.
     * @param contents the value to fill the array with
     * @param width the desired width
     * @param height the desired height
     * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
     */
    public static int[][] fill2D(int contents, int width, int height)
    {
        int[][] next = new int[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(next[x], contents);
        }
        return next;
    }
    /**
     * Creates a 2D array of the given width and height, filled with entirely with the value contents.
     * @param contents the value to fill the array with
     * @param width the desired width
     * @param height the desired height
     * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
     */
    public static boolean[][] fill2D(boolean contents, int width, int height)
    {
        boolean[][] next = new boolean[width][height];
        if(contents) {
            for (int x = 0; x < width; x++) {
                Arrays.fill(next[x], true);
            }
        }
        return next;
    }
}
