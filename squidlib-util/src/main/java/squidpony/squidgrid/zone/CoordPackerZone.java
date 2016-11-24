package squidpony.squidgrid.zone;

import squidpony.squidgrid.zone.Zone.Skeleton;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A zone constructed by {@link CoordPacker}.
 * 
 * @author smelC
 */
public class CoordPackerZone extends Skeleton implements ImmutableZone {

	protected final short[] shorts;

	protected transient List<Coord> unpacked;

	private static final long serialVersionUID = -3718415979846804238L;

	public CoordPackerZone(short[] shorts) {
		this.shorts = shorts;
	}

	@Override
	public boolean isEmpty() {
		return CoordPacker.isEmpty(shorts);
	}

	@Override
	public int size() {
		return CoordPacker.count(shorts);
	}

	@Override
	public boolean contains(int x, int y) {
		return CoordPacker.regionsContain(shorts, CoordPacker.packOne(x, y));
	}

	@Override
	public boolean contains(Coord c) {
		return CoordPacker.regionsContain(shorts, CoordPacker.packOne(c));
	}

	@Override
	public List<Coord> getAll() {
		if (unpacked == null) {
			final Coord[] allPacked = CoordPacker.allPacked(shorts);
			unpacked = new ArrayList<Coord>(allPacked.length);
			Collections.addAll(unpacked, allPacked);
		}
		return unpacked;
	}

    @Override
    public CoordPackerZone expand(int distance) {
        return new CoordPackerZone(CoordPacker.expand(shorts, distance, 256, 256));
    }
    @Override
    public CoordPackerZone expand8way(int distance) {
        return new CoordPackerZone(CoordPacker.expand(shorts, distance, 256, 256, true));
    }

	@Override
	public String toString() {
		return (unpacked == null ? shorts : unpacked).toString();
	}
}