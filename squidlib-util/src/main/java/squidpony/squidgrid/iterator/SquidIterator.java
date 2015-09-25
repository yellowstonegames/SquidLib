package squidpony.squidgrid.iterator;

import java.util.Iterator;

import squidpony.squidmath.Coord;

/**
 * Iterators that return instances of {@link Coord} and that are useful in
 * roguelikes.
 * 
 * <p>
 * For the moment this is only a marker interface, but it may be extended in the
 * future.
 * </p>
 * 
 * @author smelC
 * 
 * @see SquidIterators
 */
public interface SquidIterator extends Iterator<Coord> {

	/* This is a marker interface */

}