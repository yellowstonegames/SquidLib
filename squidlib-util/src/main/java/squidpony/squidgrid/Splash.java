package squidpony.squidgrid;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.zone.Zone;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IRNG;

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
		this.impassable = new HashSet<>();
		/* The default */
		addImpassableChar('#');
	}
	/**
	 * A fresh instance, adding the chars in blocked to the set of impassable characters,
	 * then also adding '#' if it isn't present. You can remove '#' with
	 * {@link #removeImpassableChar(char)} if you use '#' to mean something non-blocking.
	 */
	public Splash(Set<Character> blocked) {
		this.impassable = new HashSet<>(blocked);
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
	 *            {@link DungeonUtility#getRandomCell(IRNG, char[][], Set, int)}
	 *            to find it.
	 * @param volume
	 *            The number of cells to spill on.
	 * @param drunks
	 *            The ratio of drunks to use to make the splash more realistic.
	 *            Like for dungeon generation, if greater than 0, drunk walkers
	 *            will remove the splash's margins, to make it more realistic.
	 *            You don't need that if you're doing a splash that is bounded
	 *            by walls, because the fill will be realistic. If you're doing
	 *            a splash that isn't bounded, use that for its borders not to
	 *            be too square.
	 * 
	 *            <p>
	 *            Useful values are 0, 1, and 2. Giving more will likely yield
	 *            an empty result, on any decent map sizes.
	 *            </p>
	 * @return The spill. It is a list of coordinates (containing {@code start})
	 *         valid in {@code level} that are all adjacent and whose symbol is
	 *         passable. If non-empty, this is guaranteed to be an
	 *         {@link ArrayList}.
	 */
	public List<Coord> spill(IRNG rng, char[][] level, Coord start, int volume, int drunks) {
		if (!DungeonUtility.inLevel(level, start) || !passable(level[start.x][start.y]))
			return Collections.emptyList();

		final List<Coord> result = new ArrayList<>(volume);

		Direction[] dirs = new Direction[Direction.OUTWARDS.length];

		final LinkedList<Coord> toTry = new LinkedList<>();
		toTry.add(start);
		final Set<Coord> trieds = new HashSet<>();

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

		if (0 < drunks)
			inebriate(rng, result, Zone.Helper.border(result, null), drunks);

		return result;
	}

	/**
	 * Uses drunken walkers to alter the edge of the splash.
	 * Formerly called drunkinize (sic); code that extends Splash may need to change the name of an overridden method.
	 * @param rng random number generator
	 * @param zone
	 *            The zone to shrink
	 * @param border
	 *            {@code zone}'s border
	 * @param drunks
	 *            The number of drunken walkers to consider
	 */
	protected void inebriate(IRNG rng, List<Coord> zone, List<Coord> border, int drunks) {
		if (drunks == 0)
			return;

		final int sz = zone.size();
		final int nb = (sz / 10) * drunks;
		if (nb == 0)
			return;

		assert !border.isEmpty();
		for (int j = 0; j < nb && !zone.isEmpty(); j++) {
			inebriate0(rng, zone, border, drunks);
			if (border.isEmpty() || zone.isEmpty())
				return;
		}
	}

	protected boolean passable(char c) {
		return !impassable.contains(c);
	}

	/**
	 * Removes a circle from {@code zone}, by taking the circle's center in
	 * {@code zone} 's border: {@code border}.
	 * 
	 * @param border
	 *            {@code result}'s border.
	 */
	private void inebriate0(IRNG rng, List<Coord> zone, List<Coord> border, int nb) {
		assert !border.isEmpty();
		assert !zone.isEmpty();

		final int width = rng.nextInt(nb) + 1;
		final int height = rng.nextInt(nb) + 1;
		final int radius = Math.max(1, Math.round(nb * Math.min(width, height)));
		final Coord center = rng.getRandomElement(border);
		zone.remove(center);
		for (int dx = -radius; dx <= radius; ++dx) {
			final int high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
			for (int dy = -high; dy <= high; ++dy) {
				final Coord c = center.translate(dx, dy);
				zone.remove(c);
				if (zone.isEmpty())
					return;
			}
		}
	}

	/**
	 * @param rng
	 *            used to randomize the floodfill
	 * @param level
	 *            char 2D array with x, y indices for the dungeon/map level
	 * @param start
	 *            Where the spill should start. It should be passable, otherwise
	 *            an empty list gets returned. Consider using
	 *            {@link DungeonUtility#getRandomCell(IRNG, char[][], Set, int)}
	 *            to find it.
	 * @param volume
	 *            The number of cells to spill on.
	 * @param impassable the set of chars on the level that block the spill, such
	 *                   as walls or maybe other spilled things (oil and water).
	 *                   May be null, which makes this treat '#' as impassable.
	 * @param drunks
	 *            The ratio of drunks to use to make the splash more realistic.
	 *            Like for dungeon generation, if greater than 0, drunk walkers
	 *            will remove the splash's margins, to make it more realistic.
	 *            You don't need that if you're doing a splash that is bounded
	 *            by walls, because the fill will be realistic. If you're doing
	 *            a splash that isn't bounded, use that for its borders not to
	 *            be too square.
	 * 
	 *            <p>
	 *            Useful values are 0, 1, and 2. Giving more will likely yield
	 *            an empty result, on any decent map sizes.
	 *            </p>
	 * @return The spill. It is a list of coordinates (containing {@code start})
	 *         valid in {@code level} that are all adjacent and whose symbol is
	 *         passable. If non-empty, this is guaranteed to be an
	 *         {@link ArrayList}.
	 */
	public static List<Coord> spill(IRNG rng, char[][] level, Coord start, int volume, Set<Character> impassable, int drunks)
	{
		Set<Character> blocked;
		if(impassable == null)
			blocked = new HashSet<>(2);
		else
			blocked = impassable;
		if(splashCache == null || blocked.hashCode() != splashHash)
		{
			splashHash = blocked.hashCode();
			splashCache = new Splash(blocked);
		}
		return splashCache.spill(rng, level, start, volume, drunks);
	}

}
