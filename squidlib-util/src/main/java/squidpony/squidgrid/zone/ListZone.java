package squidpony.squidgrid.zone;

import java.util.List;

import squidpony.squidgrid.zone.Zone.Skeleton;
import squidpony.squidmath.Coord;

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