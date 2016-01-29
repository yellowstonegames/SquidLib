package squidpony.squidgrid;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.RNG;

import java.util.*;

/**
 * A alternative to {@link Spill}, whose purpose is to have a simpler API. You
 * can specify the characters that are impassable (in other words: that should
 * not be spilled on) using {@link #addImpassableChar(char)} and
 * {@link #removeImpassableChar(char)}. By default the set of impassable characters
 * is {@code '#'}.
 * 
 * @author smelC
 * 
 * @see Spill An alternative implementation of spilling.
 */
public class Splash {

	private static Splash splashCache = null;
	private static int splashHash = -1;
	protected final Set<Character> impassable;

	/**
	 * A fresh instance, whose only impassable character is '#'.
	 */
	public Splash() {
		this.impassable = new HashSet<Character>();
		/* The default */
		addImpassableChar('#');
	}
	/**
	 * A fresh instance, adding the chars in blocked to the set of impassable characters,
	 * then also adding '#' if it isn't present. You can remove '#' with
	 * {@link #removeImpassableChar(char)} if you use '#' to mean something non-blocking.
	 */
	public Splash(Set<Character> blocked) {
		this.impassable = new HashSet<Character>(blocked);
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
	public boolean removeImpassableChar(char c) {
		return this.impassable.remove(c);
	}

	/**
	 * @param rng used to randomize the floodfill
	 * @param level char 2D array with x, y indices for the dungeon/map level
	 * @param start
	 *            Where the spill should start. It should be passable, otherwise
	 *            an empty list gets returned. Consider using
	 *            {@link DungeonUtility#getRandomCell(RNG, char[][], Set, int)}
	 *            to find it.
	 * @param volume
	 *            The number of cells to spill on.
	 * @return The spill. It is a list of coordinates (containing {@code start})
	 *         valid in {@code level} that are all adjacent and whose symbol is
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

	/**
	 * @param rng used to randomize the floodfill
	 * @param level char 2D array with x, y indices for the dungeon/map level
	 * @param start
	 *            Where the spill should start. It should be passable, otherwise
	 *            an empty list gets returned. Consider using
	 *            {@link DungeonUtility#getRandomCell(RNG, char[][], Set, int)}
	 *            to find it.
	 * @param volume
	 *            The number of cells to spill on.
	 * @param impassable the set of chars on the level that block the spill, such
	 *                   as walls or maybe other spilled things (oil and water).
	 *                   May be null, which makes this treat '#' as impassable.
	 * @return The spill. It is a list of coordinates (containing {@code start})
	 *         valid in {@code level} that are all adjacent and whose symbol is
	 *         passable. If non-empty, this is guaranteed to be an
	 *         {@link ArrayList}.
	 */
	public static List<Coord> spill(RNG rng, char[][] level, Coord start, int volume, Set<Character> impassable)
	{
		Set<Character> blocked;
		if(impassable == null)
			blocked = new HashSet<Character>(2);
		else
			blocked = impassable;
		if(splashCache == null || blocked.hashCode() != splashHash)
		{
			splashHash = blocked.hashCode();
			splashCache = new Splash(blocked);
		}
		return splashCache.spill(rng, level, start, volume);
	}

}
