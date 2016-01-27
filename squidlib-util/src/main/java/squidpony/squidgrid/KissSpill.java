package squidpony.squidgrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.RNG;

/**
 * A alternative to {@link Spill}, whose purpose is to have a simpler API. You
 * can specify the characters that are impassable (in other words: that should
 * not be spilled on) using {@link #addImpassableChar(char)} and
 * {@link #rmImpassableChar(char)}. By default the set of impassable characters
 * is {@code '#'}.
 * 
 * @author smelC
 * 
 * @see Spill An alternative implementation of spilling.
 */
public class KissSpill {

	protected final Set<Character> impassable;

	/**
	 * A fresh instance, whose only impassable character is '#'.
	 */
	public KissSpill() {
		this.impassable = new HashSet<Character>();
		/* The default */
		addImpassableChar('#');
	}

	/**
	 * Adds {@code c} to the set of impassable characters.
	 * 
	 * @param c
	 *            The character to add.
	 */
	public void addImpassableChar(char c) {
		this.impassable.add(c);
	}

	/**
	 * Removes {@code c} from the set of impassable characters.
	 * 
	 * @param c
	 *            The character to remove.
	 * @return Whether it was in there.
	 */
	public boolean rmImpassableChar(char c) {
		return this.impassable.remove(c);
	}

	/**
	 * @param rng
	 * @param level
	 * @param start
	 *            Where the spill should start. It should be passable, otherwise
	 *            an empty list gets returned. Consider using
	 *            {@link DungeonUtility#getRandomCell(RNG, char[][], Set, int)}
	 *            to find it.
	 * @param volume
	 *            The number of cells to spill on.
	 * @return The spill. It is list of coordinates (containing {@code start})
	 *         valid in {@code level} that are all adjacents and whose symbol is
	 *         passable. If non-empty, this is guaranteed to be an
	 *         {@link ArrayList}.
	 */
	public List<Coord> spill(RNG rng, char[][] level, Coord start, int volume) {
		if (!DungeonUtility.inLevel(level, start) || !passable(level[start.x][start.y]))
			return Collections.emptyList();

		final List<Coord> result = new ArrayList<Coord>(volume);

		Direction[] dirs = new Direction[Direction.OUTWARDS.length];

		final LinkedList<Coord> toTry = new LinkedList<Coord>();
		toTry.add(start);
		final Set<Coord> trieds = new HashSet<Coord>();

		while (!toTry.isEmpty()) {
			assert result.size() < volume;
			final Coord current = toTry.removeFirst();
			assert DungeonUtility.inLevel(level, current);
			assert passable(level[current.x][current.y]);
			if (trieds.contains(current))
				continue;
			trieds.add(current);
			/*
			 * Here it holds that either 'current == start' or there's a Coord
			 * in 'result' that is adjacent to 'current'.
			 */
			result.add(current);
			if (result.size() == volume)
				/* We're done */
				break;
			/* Now prepare data for next iterations */
			/* Randomize directions */
			dirs = rng.shuffle(Direction.OUTWARDS, dirs);
			for (Direction d : dirs) {
				final Coord next = current.translate(d);
				if (DungeonUtility.inLevel(level, next) && !trieds.contains(next)
						&& passable(level[next.x][next.y]))
					/* A valid cell for trying to be spilled on */
					toTry.add(next);
			}
		}

		return result;
	}

	protected boolean passable(char c) {
		return !impassable.contains(c);
	}

}
