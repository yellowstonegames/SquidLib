package squidpony;

import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Static methods useful to be GWT-compatible.
 * You can think of the purpose of this class as "GWT, and Compatibility". There's a replacement for a Math method that
 * isn't available on GWT, a quick way to get the first element in an Iterable, and also a method to easily clone a
 * Coord array.
 * 
 * @author smelC
 * @author Tommy Ettinger
 */
public class GwtCompatibility {

	/**
     * Gets an exact copy of an array of Coord. References are shared, which should be the case for all usage of Coord
     * since they are immutable and thus don't need multiple variants on a Coord from the pool.
	 * @param input an array of Coord to copy
	 * @return A clone of {@code input}.
	 */
	public static Coord[] cloneCoords(Coord[] input) {
		final Coord[] result = new Coord[input.length];
        //System.arraycopy, despite being cumbersome, is the fastest way to copy an array on the JVM.
        System.arraycopy(input, 0, result, 0, input.length);
		return result;
	}

	/**
     * A replacement for {@link Math#IEEEremainder(double, double)}, because Math.IEEEremainder isn't GWT-compatible.
     * Gets the remainder of op / d, which can be negative if any parameter is negative.
	 * @param op the operand/dividend
	 * @param d the divisor
	 * @return The remainder of {@code op / d}, as a double; can be negative
	 */
	/* smelC: because Math.IEEEremainder isn't GWT compatible */
	public static double IEEEremainder(double op, double d) {
		final double div = Math.round(op / d);
		return op - (div * d);
	}

    /**
     * Stupidly simple convenience method that produces a range from 0 to end, not including end, as an int array.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param end the exclusive upper bound on the range
     * @return the range of ints as an int array
	 * @deprecated Use {@link ArrayTools#range(int)} instead.
     */
    @Deprecated
    public static int[] range(int end)
    {
		return ArrayTools.range(end);
    }

    /**
     * Stupidly simple convenience method that produces a range from start to end, not including end, as an int array.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param start the inclusive lower bound on the range
     * @param end the exclusive upper bound on the range
     * @return the range of ints as an int array
	 * @deprecated Use {@link ArrayTools#range(int, int)} instead.
     */
    @Deprecated
    public static int[] range(int start, int end)
    {
		return ArrayTools.range(start, end);
    }

    /**
     * Stupidly simple convenience method that produces a char range from start to end, including end, as a char array.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param start the inclusive lower bound on the range, such as 'a'
     * @param end the inclusive upper bound on the range, such as 'z'
     * @return the range of chars as a char array
	 * @deprecated Use {@link ArrayTools#charSpan(char, char)} instead.
     */
    @Deprecated
    public static char[] charSpan(char start, char end)
    {
		return ArrayTools.charSpan(start, end);
    }
    /**
     * Stupidly simple convenience method that produces a char array containing only letters that can be reasonably
     * displayed (with SquidLib's default text display assets, at least). The letters are copied from a single source
     * of 256 chars; if you need more chars or you don't need pure letters, you can use {@link #charSpan(char, char)}.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param charCount the number of letters to return in an array; the maximum this will produce is 256
     * @return the range of letters as a char array
	 * @deprecated Use {@link ArrayTools#letterSpan(int)} instead.
     */
    @Deprecated
    public static char[] letterSpan(int charCount)
    {
		return ArrayTools.letterSpan(charCount);
    }

    /**
     * Gets the first item in an Iterable of T, or null if it is empty. Meant for collections like LinkedHashSet, which
     * can promise a stable first element but don't provide a way to access it. Not exactly a GWT compatibility method,
     * but more of a Java standard library stand-in. Even though LinkedHashSet does not support this out-of-the-box,
     * OrderedSet already provides a first() method.
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
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D char array
	 * @return a copy of source, or null if source is null
	 * @deprecated Use {@link ArrayTools#copy(char[][])} instead.
	 */
	@Deprecated
	public static char[][] copy2D(char[][] source)
	{
		return ArrayTools.copy(source);
	}


	/**
	 * Gets a copy of the 2D int array, source, that has the same data but shares no references with source.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D int array
	 * @return a copy of source, or null if source is null
	 * @deprecated Use {@link ArrayTools#copy(int[][])} instead.
	 */
	@Deprecated
	public static int[][] copy2D(int[][] source)
	{
		return ArrayTools.copy(source);
	}

	/**
	 * Gets a copy of the 2D double array, source, that has the same data but shares no references with source.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D double array
	 * @return a copy of source, or null if source is null
	 * @deprecated Use {@link ArrayTools#copy(double[][])} instead.
	 */
	@Deprecated
	public static double[][] copy2D(double[][] source)
	{
		return ArrayTools.copy(source);
	}

	/**
	 * Gets a copy of the 2D boolean array, source, that has the same data but shares no references with source.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D boolean array
	 * @return a copy of source, or null if source is null
	 * @deprecated Use {@link ArrayTools#copy(boolean[][])} instead.
	 */
	@Deprecated
	public static boolean[][] copy2D(boolean[][] source)
	{
		return ArrayTools.copy(source);
	}

    /**
     * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
     * Modifies target in-place and also returns target for chaining.
     * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D char array that will be copied and inserted into target
     * @param target a 2D char array that will be modified by receiving as much of source as it can hold
     * @param x the x position in target to receive the items from the first cell in source
     * @param y the y position in target to receive the items from the first cell in source
     * @return a modified copy of target with source inserted into it at the given position
	 * @deprecated Use {@link ArrayTools#insert(char[][], char[][], int, int)} instead.
     */
    @Deprecated
    public static char[][] insert2D(char[][] source, char[][] target, int x, int y)
    {
		return ArrayTools.insert(source, target, x, y);
    }
	/**
	 * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
	 * Modifies target in-place and also returns target for chaining.
	 * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D int array that will be copied and inserted into target
	 * @param target a 2D int array that will be modified by receiving as much of source as it can hold
	 * @param x the x position in target to receive the items from the first cell in source
	 * @param y the y position in target to receive the items from the first cell in source
	 * @return a modified copy of target with source inserted into it at the given position
	 * @deprecated Use {@link ArrayTools#insert(int[][], int[][], int, int)} instead.
	 */
	@Deprecated
	public static int[][] insert2D(int[][] source, int[][] target, int x, int y)
	{
		return ArrayTools.insert(source, target, x, y);
	}
	/**
	 * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
	 * Modifies target in-place and also returns target for chaining.
	 * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D double array that will be copied and inserted into target
	 * @param target a 2D double array that will be modified by receiving as much of source as it can hold
	 * @param x the x position in target to receive the items from the first cell in source
	 * @param y the y position in target to receive the items from the first cell in source
	 * @return a modified copy of target with source inserted into it at the given position
	 * @deprecated Use {@link ArrayTools#insert(double[][], double[][], int, int)} instead.
	 */
	@Deprecated
	public static double[][] insert2D(double[][] source, double[][] target, int x, int y)
	{
		return ArrayTools.insert(source, target, x, y);
	}
	/**
	 * Inserts as much of source into target at the given x,y position as target can hold or source can supply.
	 * Modifies target in-place and also returns target for chaining.
	 * Used primarily to place a smaller array into a different position in a larger array, often freshly allocated.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param source a 2D boolean array that will be copied and inserted into target
	 * @param target a 2D boolean array that will be modified by receiving as much of source as it can hold
	 * @param x the x position in target to receive the items from the first cell in source
	 * @param y the y position in target to receive the items from the first cell in source
	 * @return a modified copy of target with source inserted into it at the given position
	 * @deprecated Use {@link ArrayTools#insert(boolean[][], boolean[][], int, int)} instead.
	 */
	@Deprecated
	public static boolean[][] insert2D(boolean[][] source, boolean[][] target, int x, int y)
	{
		return ArrayTools.insert(source, target, x, y);
	}

    /**
     * Creates a 2D array of the given width and height, filled with entirely with the value contents.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param contents the value to fill the array with
     * @param width the desired width
     * @param height the desired height
     * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
	 * @deprecated use {@link ArrayTools#fill(char, int, int)} instead.
     */
    @Deprecated
    public static char[][] fill2D(char contents, int width, int height)
    {
		return ArrayTools.fill(contents, width, height);
    }
	/**
	 * Creates a 2D array of the given width and height, filled with entirely with the value contents.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param contents the value to fill the array with
	 * @param width the desired width
	 * @param height the desired height
	 * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
	 * @deprecated use {@link ArrayTools#fill(int, int, int)} instead.
	 */
	@Deprecated
	public static int[][] fill2D(int contents, int width, int height)
	{
		return ArrayTools.fill(contents, width, height);
	}
	/**
	 * Creates a 2D array of the given width and height, filled with entirely with the value contents.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param contents the value to fill the array with
	 * @param width the desired width
	 * @param height the desired height
	 * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
	 * @deprecated use {@link ArrayTools#fill(double, int, int)} instead.
	 */
	@Deprecated
	public static double[][] fill2D(double contents, int width, int height)
	{
		return ArrayTools.fill(contents, width, height);
	}
	/**
	 * Creates a 2D array of the given width and height, filled with entirely with the value contents.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
	 * @param contents the value to fill the array with
	 * @param width the desired width
	 * @param height the desired height
	 * @return a freshly allocated 2D array of the requested dimensions, filled entirely with contents
	 * @deprecated use {@link ArrayTools#fill(boolean, int, int)} instead.
	 */
	@Deprecated
	public static boolean[][] fill2D(boolean contents, int width, int height)
	{
		return ArrayTools.fill(contents, width, height);
	}

    /**
     * Rearranges an ArrayList to use the given ordering, returning a copy; random orderings can be produced with
     * {@link squidpony.squidmath.RNG#randomOrdering(int)} or
     * {@link squidpony.squidmath.RNG#randomOrdering(int, int[])}. These orderings will never repeat an earlier element,
     * and the returned ArrayList may be shorter than the original if {@code ordering} isn't as long as {@code list}.
     * Using a random ordering is like shuffling, but allows you to repeat the shuffle exactly on other collections of
     * the same size. A reordering can also be inverted with {@link #invertOrdering(int[])} or
     * {@link #invertOrdering(int[], int[])}, getting the change that will undo another ordering.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param list an ArrayList that you want a reordered version of; will not be modified.
     * @param ordering an ordering, typically produced by one of RNG's randomOrdering methods.
     * @param <T> any generic type
     * @return a modified copy of {@code list} with its ordering changed to match {@code ordering}.
	 * @deprecated Use {@link ArrayTools#reorder(ArrayList, int...)} instead.
     */
    @Deprecated
    public static <T> ArrayList<T> reorder (ArrayList<T> list, int... ordering) {
		return ArrayTools.reorder(list, ordering);
    }

    /**
     * Given an ordering such as one produced by {@link squidpony.squidmath.RNG#randomOrdering(int, int[])}, this finds
     * its inverse, able to reverse the reordering and vice versa.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param ordering the ordering to find the inverse for
     * @return the inverse of ordering
	 * @deprecated Use {@link ArrayTools#invertOrdering(int[])} instead.
     */
    @Deprecated
    public static int[] invertOrdering(int[] ordering)
    {
		return ArrayTools.invertOrdering(ordering);
    }

    /**
     * Given an ordering such as one produced by {@link squidpony.squidmath.RNG#randomOrdering(int, int[])}, this finds
     * its inverse, able to reverse the reordering and vice versa. This overload doesn't allocate a new int
     * array, and instead relies on having an int array of the same size as ordering passed to it as an
     * additional argument.
	 * Delegates to ArrayTools, and using ArrayTools directly is preferred.
     * @param ordering the ordering to find the inverse for
     * @param dest the int array to put the inverse reordering into; should have the same length as ordering
     * @return the inverse of ordering; will have the same value as dest
	 * @deprecated Use {@link ArrayTools#invertOrdering(int[], int[])} instead.
     */
    @Deprecated
    public static int[] invertOrdering(int[] ordering, int[] dest)
    {
		return ArrayTools.invertOrdering(ordering, dest);
    }

}
