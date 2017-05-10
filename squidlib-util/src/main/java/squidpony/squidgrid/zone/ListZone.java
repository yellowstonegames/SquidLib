package squidpony.squidgrid.zone;

import squidpony.squidgrid.zone.Zone.Skeleton;
import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A zone defined by a {@link List}.
 * 
 * @author smelC
 */
public class ListZone extends Skeleton {

	protected final List<Coord> coords;

	private static final long serialVersionUID = 1166468942544595692L;

	public ListZone(List<Coord> coords) {
		this.coords = coords;
	}

	public ListZone(Coord[] coords) {
		this.coords = new ArrayList<Coord>(coords.length);
		Collections.addAll(this.coords, coords);
	}

	public ListZone(Collection<Coord> coordCollection)
	{
		this.coords = new ArrayList<>(coordCollection);
	}
	@Override
	public boolean isEmpty() {
		return coords.isEmpty();
	}

	@Override
	public int size() {
		return coords.size();
	}

	@Override
	public boolean contains(Coord c) {
		return coords.contains(c);
	}

	@Override
	public boolean contains(int x, int y) {
		return coords.contains(Coord.get(x, y));
	}

	@Override
	public List<Coord> getAll() {
		return coords;
	}

	/**
	 * @return The list that backs up {@code this}. Use at your own risks.
	 */
	public List<Coord> getState() {
		return coords;
	}

	@Override
	public String toString() {
		return coords.toString();
	}
}