package squidpony.gdx.tools;

import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.utils.ShortArray;
import squidpony.Maker;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.CoordDouble;
import squidpony.squidmath.IndexedDelaunayTriangulator;
import squidpony.squidmath.IntVLA;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 9/8/2020.
 */
public class DelaunayCheck {

	public static void main(String[] args) {
		{
			float[] points = new float[]{
					2000.7620849609375f,
					3044.865234375f,
					3121.152099609375f,
					2331.909912109375f,
					1909.64794921875f,
					1924.1240234375f,
					3096.233642578125f,
					2911.776611328125f,
					776.0308227539062f,
					658.64501953125f,
					3619.53564453125f,
					163.5050811767578f,
			};
			DelaunayTriangulator delaunay = new DelaunayTriangulator();
			ShortArray trigs = delaunay.computeTriangles(points, false);

			for (int c = 0, n = trigs.size; c < n; c += 3) {
				System.out.println("(" + trigs.get(c) + ", " + trigs.get(c + 1) + ", " + trigs.get(c + 2) + ")");
			}
		}
		System.out.println();
		{
			Arrangement<CoordDouble> arrange = Maker.makeArrange(
					new CoordDouble(2000.7620849609375f, 3044.865234375f),
					new CoordDouble(3121.152099609375f, 2331.909912109375f),
					new CoordDouble(1909.64794921875f, 1924.1240234375f),
					new CoordDouble(3096.233642578125f, 2911.776611328125f),
					new CoordDouble(776.0308227539062f, 658.64501953125f),
					new CoordDouble(3619.53564453125f, 163.5050811767578f)
			);
			squidpony.squidmath.DelaunayTriangulator delaunay = new squidpony.squidmath.DelaunayTriangulator(arrange.keySet());
			ArrayList<squidpony.squidmath.DelaunayTriangulator.Triangle> tris = delaunay.triangulate();
			for(squidpony.squidmath.DelaunayTriangulator.Triangle t : tris){
				System.out.println("(" + arrange.getInt(t.a) + ", " + arrange.getInt(t.b) + ", " + arrange.getInt(t.c) + ")");
			}
		}
		System.out.println();
		{
			double[] points = new double[]{
					2000.7620849609375,
					3044.865234375,
					3121.152099609375,
					2331.909912109375,
					1909.64794921875,
					1924.1240234375,
					3096.233642578125,
					2911.776611328125,
					776.0308227539062,
					658.64501953125,
					3619.53564453125,
					163.5050811767578,
			};

			IndexedDelaunayTriangulator delaunay = new IndexedDelaunayTriangulator();
			IntVLA trigs = delaunay.computeTriangles(points, false);
			for (int c = 0, n = trigs.size; c < n; c += 3) {
				System.out.println("(" + trigs.get(c) + ", " + trigs.get(c + 1) + ", " + trigs.get(c + 2) + ")");
			}

		}
		System.out.println("\nExtra Point:");
		{
			Arrangement<CoordDouble> arrange = Maker.makeArrange(
					new CoordDouble(2000.7620849609375f, 3044.865234375f),
					new CoordDouble(3121.152099609375f, 2331.909912109375f),
					new CoordDouble(1909.64794921875f, 1924.1240234375f),
					new CoordDouble(3096.233642578125f, 2911.776611328125f),
					new CoordDouble(776.0308227539062f, 658.64501953125f),
					new CoordDouble(3619.53564453125f, 163.5050811767578f),
					new CoordDouble(1.0, 1.0)
			);
			squidpony.squidmath.DelaunayTriangulator delaunay = new squidpony.squidmath.DelaunayTriangulator(arrange.keySet());
			ArrayList<squidpony.squidmath.DelaunayTriangulator.Triangle> tris = delaunay.triangulate();
			for(squidpony.squidmath.DelaunayTriangulator.Triangle t : tris){
				System.out.println("(" + arrange.getInt(t.a) + ", " + arrange.getInt(t.b) + ", " + arrange.getInt(t.c) + ")");
			}
		}
		System.out.println("\nExtra Point:");
		{
			double[] points = new double[]{
					2000.7620849609375,
					3044.865234375,
					3121.152099609375,
					2331.909912109375,
					1909.64794921875,
					1924.1240234375,
					3096.233642578125,
					2911.776611328125,
					776.0308227539062,
					658.64501953125,
					3619.53564453125,
					163.5050811767578,
					1.0,
					1.0
			};

			IndexedDelaunayTriangulator delaunay = new IndexedDelaunayTriangulator();
			IntVLA trigs = delaunay.computeTriangles(points, false);
			for (int c = 0, n = trigs.size; c < n; c += 3) {
				System.out.println("(" + trigs.get(c) + ", " + trigs.get(c + 1) + ", " + trigs.get(c + 2) + ")");
			}

		}

		// Prints out:
		// (4, 2, 5)
		// (2, 3, 1) // wrong!
		// (2, 4, 0)
		// (3, 5, 1)
		// (2, 0, 3) // wrong!
		// (5, 2, 1)
	}
}
