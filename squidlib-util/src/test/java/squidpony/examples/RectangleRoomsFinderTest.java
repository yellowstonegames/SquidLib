package squidpony.examples;

import java.util.Collection;
import java.util.Iterator;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.Rectangle;
import squidpony.squidgrid.mapping.RectangleRoomsFinder;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

/**
 * Test of {@link SimpleRoomFinder}. Different rooms are printed with digits
 * from 1 to 9. Digits are reused. So there can be multiple rooms with the same
 * digit.
 * 
 * @author smelC
 * 
 * @see RectangleRoomsFinder
 */
public class RectangleRoomsFinderTest {

	public static void main(String[] args) {
		DungeonBoneGen bg = new DungeonBoneGen(new RNG(new LightRNG(0x1337deadbeef48aaL)));
		for (TilesetType tt : TilesetType.values()) {
			System.out.println(tt);
			final char[][] dungeon = bg.generate(tt, 80, 40);
			bg.wallWrap();

			// DungeonUtility.debugPrint(dungeon);

			final RectangleRoomsFinder srf = new RectangleRoomsFinder(dungeon);
			final Collection<? extends Rectangle> rooms = srf.findRectangles();

			int nextRoomIndex = 1;
			for (Rectangle r : rooms) {
				final int roomIndex = nextRoomIndex++;
				if (nextRoomIndex == 10)
					nextRoomIndex = 1;
				if (size(Rectangle.Utils.cells(r)) != r.getWidth() * r.getHeight()) {
					throw new IllegalStateException("There's a bug, a room is badly formed");
				}
				final Iterator<Coord> cells = Rectangle.Utils.cells(r);
				final char c = String.valueOf(roomIndex).charAt(0);
				while (cells.hasNext()) {
					final Coord inr = cells.next();
					dungeon[inr.x][inr.y] = c;
				}
			}

			DungeonUtility.debugPrint(dungeon);
		}
	}

	private static int size(Iterator<?> it) {
		int result = 0;
		while (it.hasNext()) {
			result++;
			it.next();
		}
		return result;
	}

}
