package squidpony.squidmath;

/**
 * K.jpg's OpenSimplex 2, smooth variant ("SuperSimplex")
 * <br>
 * Using OpenSimplex 2 in its July 5, 2020 revision. This is the latest
 * revision as of February 26, 2021.
 * <ul>
 *   <li>2D is standard simplex, modified to support larger kernels.
 *   Implemented using a lookup table.</li>
 *   <li>3D is "Re-oriented 8-point BCC noise" which constructs a
 *   congruent BCC lattice in a much different way than usual.</li>
 *   <li>4D uses a naïve pregenerated lookup table, and averages out
 *   to the expected performance.</li>
 * </ul>
 * <br>
 * This implements SquidLib's Noise2, Noise3, and Noise4 interfaces, allowing
 * it to be used with the inner classes from {@link Noise} for things like layering
 * octaves of noise with {@link Noise.Layered2D} or making ridged noise with
 * {@link Noise.Ridged3D}. The {@link #getNoiseWithSeed(double, double, long)}
 * methods just use the seed to offset the coordinates, splitting up the seed into
 * 2, 3, or 4 smaller parts for 2D, 3D, and 4D respectively. This means the
 * getNoiseWithSeed() methods need very large and different seeds for a visual
 * difference to be apparent; just adding 1 to the seed won't help. The initial seed
 * passed to {@link #OpenSimplex2S(long)} matters a lot; it can't be changed later.
 * This is the smoother variant of OpenSimplex 2, and is expected to be slower than
 * {@link OpenSimplex2F} or the Simplex noise in {@link FastNoise}.
 */
public class OpenSimplex2S implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D {
	
	private static final int PSIZE = 2048;
	private static final int PMASK = 2047;

	private short[] perm;
	private Grad2[] permGrad2;
	private Grad3[] permGrad3;
	private Grad4[] permGrad4;

	public OpenSimplex2S(){
		this(1234567890987654321L);
	}

	public OpenSimplex2S(long seed) {
		perm = new short[PSIZE];
		permGrad2 = new Grad2[PSIZE];
		permGrad3 = new Grad3[PSIZE];
		permGrad4 = new Grad4[PSIZE];
		short[] source = new short[PSIZE]; 
		for (short i = 0; i < PSIZE; i++)
			source[i] = i;
		for (int i = PSIZE - 1; i >= 0; i--) {
			seed = seed * 6364136223846793005L + 1442695040888963407L;
			int r = (int)((seed >>> 32) * (i + 1) >>> 32);
			perm[i] = source[r];
			permGrad2[i] = GRADIENTS_2D[perm[i]];
			permGrad3[i] = GRADIENTS_3D[perm[i]];
			permGrad4[i] = GRADIENTS_4D[perm[i]];
			source[r] = source[i];
		}
	}
	
	/*
	 * Noise Evaluators
	 */
	
	/**
	 * 2D SuperSimplex noise, standard lattice orientation.
	 */
	public double noise2(double x, double y) {
		
		// Get points for A2* lattice
		double s = 0.366025403784439 * (x + y);
		double xs = x + s, ys = y + s;
		
		return noise2_Base(xs, ys);
	}
	
	/**
	 * 2D SuperSimplex noise, with Y pointing down the main diagonal.
	 * Might be better for a 2D sandbox style game, where Y is vertical.
	 * Probably slightly less optimal for heightmaps or continent maps.
	 */
	public double noise2_XBeforeY(double x, double y) {
		
		// Skew transform and rotation baked into one.
		double xx = x * 0.7071067811865476;
		double yy = y * 1.224744871380249;
		
		return noise2_Base(yy + xx, yy - xx);
	}
	
	/**
	 * 2D SuperSimplex noise base.
	 * Lookup table implementation inspired by DigitalShadow.
	 */
	private double noise2_Base(double xs, double ys) {
		double value = 0;
		
		// Get base points and offsets
		long xsb = Noise.longFloor(xs), ysb = Noise.longFloor(ys);
		double xsi = xs - xsb, ysi = ys - ysb;
		
		// Index to point list
		int a = (int)(xsi + ysi);
		int index =
			(a << 2) |
			(int)(xsi - ysi / 2 + 1 - a / 2.0) << 3 |
			(int)(ysi - xsi / 2 + 1 - a / 2.0) << 4;
		
		double ssi = (xsi + ysi) * -0.211324865405187;
		double xi = xsi + ssi, yi = ysi + ssi;

		// Point contributions
		for (int i = 0; i < 4; i++) {
			LatticePoint2D c = LOOKUP_2D[index + i];

			double dx = xi + c.dx, dy = yi + c.dy;
			double attn = 2.0 / 3.0 - dx * dx - dy * dy;
			if (attn <= 0) continue;

			int pxm = ((int) xsb + c.xsv) & PMASK, pym = ((int) ysb + c.ysv) & PMASK;
			Grad2 grad = permGrad2[perm[pxm] ^ pym];
			double extrapolation = grad.dx * dx + grad.dy * dy;
			
			attn *= attn;
			value += attn * attn * extrapolation;
		}
		
		return value;
	}
	
	/**
	 * 3D Re-oriented 8-point BCC noise, classic orientation
	 * Proper substitute for what 3D SuperSimplex would be,
	 * in light of Forbidden Formulae.
	 * Use noise3_XYBeforeZ or noise3_XZBeforeY instead, wherever appropriate.
	 */
	public double noise3_Classic(double x, double y, double z) {
		
		// Re-orient the cubic lattices via rotation, to produce the expected look on cardinal planar slices.
		// If texturing objects that don't tend to have cardinal plane faces, you could even remove this.
		// Orthonormal rotation. Not a skew transform.
		double r = (2.0 / 3.0) * (x + y + z);
		double xr = r - x, yr = r - y, zr = r - z;
		
		// Evaluate both lattices to form a BCC lattice.
		return noise3_BCC(xr, yr, zr);
	}
	
	/**
	 * 3D Re-oriented 8-point BCC noise, with better visual isotropy in (X, Y).
	 * Recommended for 3D terrain and time-varied animations.
	 * The Z coordinate should always be the "different" coordinate in your use case.
	 * If Y is vertical in world coordinates, call noise3_XYBeforeZ(x, z, Y) or use noise3_XZBeforeY.
	 * If Z is vertical in world coordinates, call noise3_XYBeforeZ(x, y, Z).
	 * For a time varied animation, call noise3_XYBeforeZ(x, y, T).
	 */
	public double noise3_XYBeforeZ(double x, double y, double z) {
		
		// Re-orient the cubic lattices without skewing, to make X and Y triangular like 2D.
		// Orthonormal rotation. Not a skew transform.
		double xy = x + y;
		double s2 = xy * -0.211324865405187;
		double zz = z * 0.577350269189626;
		double xr = x + s2 - zz, yr = y + s2 - zz;
		double zr = xy * 0.577350269189626 + zz;
		
		// Evaluate both lattices to form a BCC lattice.
		return noise3_BCC(xr, yr, zr);
	}
	
	/**
	 * 3D Re-oriented 8-point BCC noise, with better visual isotropy in (X, Z).
	 * Recommended for 3D terrain and time-varied animations.
	 * The Y coordinate should always be the "different" coordinate in your use case.
	 * If Y is vertical in world coordinates, call noise3_XZBeforeY(x, Y, z).
	 * If Z is vertical in world coordinates, call noise3_XZBeforeY(x, Z, y) or use noise3_XYBeforeZ.
	 * For a time varied animation, call noise3_XZBeforeY(x, T, y) or use noise3_XYBeforeZ.
	 */
	public double noise3_XZBeforeY(double x, double y, double z) {
		
		// Re-orient the cubic lattices without skewing, to make X and Z triangular like 2D.
		// Orthonormal rotation. Not a skew transform.
		double xz = x + z;
		double s2 = xz * -0.211324865405187;
		double yy = y * 0.577350269189626;
		double xr = x + s2 - yy; double zr = z + s2 - yy;
		double yr = xz * 0.577350269189626 + yy;
		
		// Evaluate both lattices to form a BCC lattice.
		return noise3_BCC(xr, yr, zr);
	}
	
	/**
	 * Generate overlapping cubic lattices for 3D Re-oriented BCC noise.
	 * Lookup table implementation inspired by DigitalShadow.
	 * It was actually faster to narrow down the points in the loop itself,
	 * than to build up the index with enough info to isolate 8 points.
	 */
	private double noise3_BCC(double xr, double yr, double zr) {
		
		// Get base and offsets inside cube of first lattice.
		long xrb = Noise.longFloor(xr), yrb = Noise.longFloor(yr), zrb = Noise.longFloor(zr);
		double xri = xr - xrb, yri = yr - yrb, zri = zr - zrb;
		
		// Identify which octant of the cube we're in. This determines which cell
		// in the other cubic lattice we're in, and also narrows down one point on each.
		int xht = (int)(xri + 0.5), yht = (int)(yri + 0.5), zht = (int)(zri + 0.5);
		int index = (xht << 0) | (yht << 1) | (zht << 2);
		
		// Point contributions
		double value = 0;
		LatticePoint3D c = LOOKUP_3D[index];
		while (c != null) {
			double dxr = xri + c.dxr, dyr = yri + c.dyr, dzr = zri + c.dzr;
			double attn = 0.75 - dxr * dxr - dyr * dyr - dzr * dzr;
			if (attn < 0) {
				c = c.nextOnFailure;
			} else {
				int pxm = ((int) xrb + c.xrv) & PMASK, pym = ((int) yrb + c.yrv) & PMASK, pzm = ((int) zrb + c.zrv) & PMASK;
				Grad3 grad = permGrad3[perm[perm[pxm] ^ pym] ^ pzm];
				double extrapolation = grad.dx * dxr + grad.dy * dyr + grad.dz * dzr;
				
				attn *= attn;
				value += attn * attn * extrapolation;
				c = c.nextOnSuccess;
			}
		}
		return value;
	}
	
	/**
	 * 4D SuperSimplex noise, classic lattice orientation.
	 */
	public double noise4_Classic(double x, double y, double z, double w) {
		
		// Get points for A4 lattice
		double s = 0.309016994374947 * (x + y + z + w);
		double xs = x + s, ys = y + s, zs = z + s, ws = w + s;
		
		return noise4_Base(xs, ys, zs, ws);
	}
	
	/**
	 * 4D SuperSimplex noise, with XY and ZW forming orthogonal triangular-based planes.
	 * Recommended for 3D terrain, where X and Y (or Z and W) are horizontal.
	 * Recommended for noise(x, y, sin(time), cos(time)) trick.
	 */
	public double noise4_XYBeforeZW(double x, double y, double z, double w) {
		
		double s2 = (x + y) * -0.28522513987434876941 + (z + w) * 0.83897065470611435718;
		double t2 = (z + w) * 0.21939749883706435719 + (x + y) * -0.48214856493302476942;
		double xs = x + s2, ys = y + s2, zs = z + t2, ws = w + t2;
		
		return noise4_Base(xs, ys, zs, ws);
	}
	
	/**
	 * 4D SuperSimplex noise, with XZ and YW forming orthogonal triangular-based planes.
	 * Recommended for 3D terrain, where X and Z (or Y and W) are horizontal.
	 */
	public double noise4_XZBeforeYW(double x, double y, double z, double w) {
		
		double s2 = (x + z) * -0.28522513987434876941 + (y + w) * 0.83897065470611435718;
		double t2 = (y + w) * 0.21939749883706435719 + (x + z) * -0.48214856493302476942;
		double xs = x + s2, ys = y + t2, zs = z + s2, ws = w + t2;
		
		return noise4_Base(xs, ys, zs, ws);
	}
	
	/**
	 * 4D SuperSimplex noise, with XYZ oriented like noise3_Classic,
	 * and W for an extra degree of freedom.
	 * Recommended for time-varied animations which texture a 3D object (W=time)
	 */
	public double noise4_XYZBeforeW(double x, double y, double z, double w) {
		
		double xyz = x + y + z;
		double ww = w * 1.118033988749894;
		double s2 = xyz * -0.16666666666666666 + ww;
		double xs = x + s2, ys = y + s2, zs = z + s2, ws = -0.5 * xyz + ww;
		
		return noise4_Base(xs, ys, zs, ws);
	}
	
	/**
	 * 4D SuperSimplex noise base.
	 * Using ultra-simple 4x4x4x4 lookup partitioning.
	 * This isn't as elegant or SIMD/GPU/etc. portable as other approaches,
	 * but it does compete performance-wise with optimized OpenSimplex1.
	 */
	private double noise4_Base(double xs, double ys, double zs, double ws) {
		double value = 0;
		
		// Get base points and offsets
		long xsb = Noise.longFloor(xs), ysb = Noise.longFloor(ys), zsb = Noise.longFloor(zs), wsb = Noise.longFloor(ws);
		double xsi = xs - xsb, ysi = ys - ysb, zsi = zs - zsb, wsi = ws - wsb;
		
		// Unskewed offsets
		double ssi = (xsi + ysi + zsi + wsi) * -0.138196601125011;
		double xi = xsi + ssi, yi = ysi + ssi, zi = zsi + ssi, wi = wsi + ssi;
			
		int index = (int) ((Noise.longFloor(xs * 4) & 3L)
			| ((Noise.longFloor(ys * 4) & 3L) << 2)
			| ((Noise.longFloor(zs * 4) & 3L) << 4)
			| ((Noise.longFloor(ws * 4) & 3L) << 6));
		
		// Point contributions
		for (LatticePoint4D c : LOOKUP_4D[index]) {
			double dx = xi + c.dx, dy = yi + c.dy, dz = zi + c.dz, dw = wi + c.dw;
			double attn = 0.8 - dx * dx - dy * dy - dz * dz - dw * dw;
			if (attn > 0) {
				attn *= attn;

				int pxm = ((int) xsb + c.xsv) & PMASK, pym = ((int) ysb + c.ysv) & PMASK;
				int pzm = ((int) zsb + c.zsv) & PMASK, pwm = ((int) wsb + c.wsv) & PMASK;
				Grad4 grad = permGrad4[perm[perm[perm[pxm] ^ pym] ^ pzm] ^ pwm];
				double extrapolation = grad.dx * dx + grad.dy * dy + grad.dz * dz + grad.dw * dw;
				
				value += attn * attn * extrapolation;
			}
		}
		return value;
	}
	
	/*
	 * Utility
	 */
	
	private static int fastFloor(double x) {
		int xi = (int)x;
		return x < xi ? xi - 1 : xi;
	}
	
	/*
	 * Definitions
	 */

	private static final LatticePoint2D[] LOOKUP_2D;
	private static final LatticePoint3D[] LOOKUP_3D;
	private static final LatticePoint4D[][] LOOKUP_4D;
	static {
		LOOKUP_2D = new LatticePoint2D[8 * 4];
		LOOKUP_3D = new LatticePoint3D[8];
		LOOKUP_4D = new LatticePoint4D[256][];
		
		for (int i = 0; i < 8; i++) {
			int i1, j1, i2, j2;
			if ((i & 1) == 0) {
				if ((i & 2) == 0) { i1 = -1; j1 = 0; } else { i1 = 1; j1 = 0; }
				if ((i & 4) == 0) { i2 = 0; j2 = -1; } else { i2 = 0; j2 = 1; }
			} else {
				if ((i & 2) != 0) { i1 = 2; j1 = 1; } else { i1 = 0; j1 = 1; }
				if ((i & 4) != 0) { i2 = 1; j2 = 2; } else { i2 = 1; j2 = 0; }
			}
			LOOKUP_2D[i * 4 + 0] = new LatticePoint2D(0, 0);
			LOOKUP_2D[i * 4 + 1] = new LatticePoint2D(1, 1);
			LOOKUP_2D[i * 4 + 2] = new LatticePoint2D(i1, j1);
			LOOKUP_2D[i * 4 + 3] = new LatticePoint2D(i2, j2);
		}
		
		for (int i = 0; i < 8; i++) {
			int i1, j1, k1, i2, j2, k2;
			i1 = (i >> 0) & 1; j1 = (i >> 1) & 1; k1 = (i >> 2) & 1;
			i2 = i1 ^ 1; j2 = j1 ^ 1; k2 = k1 ^ 1;
			
			// The two points within this octant, one from each of the two cubic half-lattices.
			LatticePoint3D c0 = new LatticePoint3D(i1, j1, k1, 0);
			LatticePoint3D c1 = new LatticePoint3D(i1 + i2, j1 + j2, k1 + k2, 1);
			
			// (1, 0, 0) vs (0, 1, 1) away from octant.
			LatticePoint3D c2 = new LatticePoint3D(i1 ^ 1, j1, k1, 0);
			LatticePoint3D c3 = new LatticePoint3D(i1, j1 ^ 1, k1 ^ 1, 0);
			
			// (1, 0, 0) vs (0, 1, 1) away from octant, on second half-lattice.
			LatticePoint3D c4 = new LatticePoint3D(i1 + (i2 ^ 1), j1 + j2, k1 + k2, 1);
			LatticePoint3D c5 = new LatticePoint3D(i1 + i2, j1 + (j2 ^ 1), k1 + (k2 ^ 1), 1);
			
			// (0, 1, 0) vs (1, 0, 1) away from octant.
			LatticePoint3D c6 = new LatticePoint3D(i1, j1 ^ 1, k1, 0);
			LatticePoint3D c7 = new LatticePoint3D(i1 ^ 1, j1, k1 ^ 1, 0);
			
			// (0, 1, 0) vs (1, 0, 1) away from octant, on second half-lattice.
			LatticePoint3D c8 = new LatticePoint3D(i1 + i2, j1 + (j2 ^ 1), k1 + k2, 1);
			LatticePoint3D c9 = new LatticePoint3D(i1 + (i2 ^ 1), j1 + j2, k1 + (k2 ^ 1), 1);
			
			// (0, 0, 1) vs (1, 1, 0) away from octant.
			LatticePoint3D cA = new LatticePoint3D(i1, j1, k1 ^ 1, 0);
			LatticePoint3D cB = new LatticePoint3D(i1 ^ 1, j1 ^ 1, k1, 0);
			
			// (0, 0, 1) vs (1, 1, 0) away from octant, on second half-lattice.
			LatticePoint3D cC = new LatticePoint3D(i1 + i2, j1 + j2, k1 + (k2 ^ 1), 1);
			LatticePoint3D cD = new LatticePoint3D(i1 + (i2 ^ 1), j1 + (j2 ^ 1), k1 + k2, 1);
			
			// First two points are guaranteed.
			c0.nextOnFailure = c0.nextOnSuccess = c1;
			c1.nextOnFailure = c1.nextOnSuccess = c2;
			
			// If c2 is in range, then we know c3 and c4 are not.
			c2.nextOnFailure = c3; c2.nextOnSuccess = c5;
			c3.nextOnFailure = c4; c3.nextOnSuccess = c4;
			
			// If c4 is in range, then we know c5 is not.
			c4.nextOnFailure = c5; c4.nextOnSuccess = c6;
			c5.nextOnFailure = c5.nextOnSuccess = c6;
			
			// If c6 is in range, then we know c7 and c8 are not.
			c6.nextOnFailure = c7; c6.nextOnSuccess = c9;
			c7.nextOnFailure = c8; c7.nextOnSuccess = c8;
			
			// If c8 is in range, then we know c9 is not.
			c8.nextOnFailure = c9; c8.nextOnSuccess = cA;
			c9.nextOnFailure = c9.nextOnSuccess = cA;
			
			// If cA is in range, then we know cB and cC are not.
			cA.nextOnFailure = cB; cA.nextOnSuccess = cD;
			cB.nextOnFailure = cC; cB.nextOnSuccess = cC;
			
			// If cC is in range, then we know cD is not.
			cC.nextOnFailure = cD; cC.nextOnSuccess = null;
			cD.nextOnFailure = cD.nextOnSuccess = null;
			
			LOOKUP_3D[i] = c0;
		}
		
		int[][] lookup4DPregen = {
			{ 0x15, 0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x15, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA },
			{ 0x01, 0x05, 0x11, 0x15, 0x41, 0x45, 0x51, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA },
			{ 0x01, 0x15, 0x16, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x15, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA },
			{ 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xAA },
			{ 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xAA },
			{ 0x05, 0x15, 0x16, 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xAA, 0xAB },
			{ 0x04, 0x05, 0x14, 0x15, 0x44, 0x45, 0x54, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA },
			{ 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xAA },
			{ 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0xAA },
			{ 0x05, 0x15, 0x16, 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x5B, 0x6A, 0x9A, 0xAA, 0xAB },
			{ 0x04, 0x15, 0x19, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x05, 0x15, 0x19, 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xAA, 0xAE },
			{ 0x05, 0x15, 0x19, 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x5E, 0x6A, 0x9A, 0xAA, 0xAE },
			{ 0x05, 0x15, 0x1A, 0x45, 0x4A, 0x55, 0x56, 0x59, 0x5A, 0x5B, 0x5E, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x15, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x11, 0x15, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xAA },
			{ 0x11, 0x15, 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xAA },
			{ 0x11, 0x15, 0x16, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xAA, 0xAB },
			{ 0x14, 0x15, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x9A, 0xA6, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x15, 0x16, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x6B, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x14, 0x15, 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xAA },
			{ 0x15, 0x16, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x6B, 0x9A, 0xAA, 0xAB },
			{ 0x14, 0x15, 0x19, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x19, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x6E, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x19, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x6E, 0x9A, 0xAA, 0xAE },
			{ 0x15, 0x1A, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x6B, 0x6E, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x10, 0x11, 0x14, 0x15, 0x50, 0x51, 0x54, 0x55, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x11, 0x15, 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xAA },
			{ 0x11, 0x15, 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0xA6, 0xAA },
			{ 0x11, 0x15, 0x16, 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x67, 0x6A, 0xA6, 0xAA, 0xAB },
			{ 0x14, 0x15, 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA6, 0xAA },
			{ 0x15, 0x16, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x6B, 0xA6, 0xAA, 0xAB },
			{ 0x14, 0x15, 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xAA },
			{ 0x15, 0x16, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0xAA, 0xAB },
			{ 0x14, 0x15, 0x19, 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x6A, 0x6D, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x19, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x6E, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x19, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6E, 0xAA, 0xAE },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x6B, 0x6E, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x10, 0x15, 0x25, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x11, 0x15, 0x25, 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xAA, 0xBA },
			{ 0x11, 0x15, 0x25, 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x6A, 0x76, 0xA6, 0xAA, 0xBA },
			{ 0x11, 0x15, 0x26, 0x51, 0x55, 0x56, 0x62, 0x65, 0x66, 0x67, 0x6A, 0x76, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x14, 0x15, 0x25, 0x54, 0x55, 0x59, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x25, 0x55, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x25, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA6, 0xAA, 0xBA },
			{ 0x15, 0x26, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x6B, 0x7A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x14, 0x15, 0x25, 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x6A, 0x79, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x25, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x25, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xAA, 0xBA },
			{ 0x15, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0x7A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x14, 0x15, 0x29, 0x54, 0x55, 0x59, 0x65, 0x68, 0x69, 0x6A, 0x6D, 0x79, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x15, 0x29, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x6E, 0x7A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x15, 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6E, 0x7A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0x6E, 0x7A, 0xAA, 0xAB, 0xAE, 0xBA, 0xBF },
			{ 0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x5A, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xAA },
			{ 0x41, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x44, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x45, 0x46, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB },
			{ 0x44, 0x45, 0x54, 0x55, 0x59, 0x5A, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xAA },
			{ 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x9A, 0x9B, 0xAA, 0xAB },
			{ 0x44, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x49, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x99, 0x9A, 0x9E, 0xAA, 0xAE },
			{ 0x45, 0x4A, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x50, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x59, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB },
			{ 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x56, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x15, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE },
			{ 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x50, 0x51, 0x54, 0x55, 0x65, 0x66, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xAA },
			{ 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xA7, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA },
			{ 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA9, 0xAA },
			{ 0x15, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xA6, 0xA9, 0xAA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA, 0xAD, 0xAE },
			{ 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x50, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x66, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x51, 0x55, 0x61, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA },
			{ 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x6A, 0xA5, 0xA6, 0xAA, 0xB6, 0xBA },
			{ 0x51, 0x55, 0x56, 0x62, 0x65, 0x66, 0x6A, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xBB },
			{ 0x54, 0x55, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA },
			{ 0x55, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x55, 0x56, 0x65, 0x66, 0x6A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x6A, 0xA5, 0xA9, 0xAA, 0xB9, 0xBA },
			{ 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x15, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x54, 0x55, 0x59, 0x65, 0x68, 0x69, 0x6A, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xBE },
			{ 0x55, 0x59, 0x65, 0x69, 0x6A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x15, 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xAA, 0xAB, 0xAE, 0xBA, 0xBF },
			{ 0x40, 0x41, 0x44, 0x45, 0x50, 0x51, 0x54, 0x55, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x95, 0x96, 0x9A, 0xA6, 0xAA },
			{ 0x41, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x95, 0x96, 0x97, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x44, 0x45, 0x54, 0x55, 0x59, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA },
			{ 0x45, 0x46, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB },
			{ 0x44, 0x45, 0x54, 0x55, 0x59, 0x95, 0x99, 0x9A, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xAA },
			{ 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0xAA, 0xAB },
			{ 0x44, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x95, 0x99, 0x9A, 0x9D, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x49, 0x55, 0x59, 0x5A, 0x95, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9E, 0xAA, 0xAE },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x99, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA },
			{ 0x51, 0x52, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xEA },
			{ 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA },
			{ 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xEA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x58, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE },
			{ 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x99, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA },
			{ 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB },
			{ 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE },
			{ 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xAF },
			{ 0x50, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB5, 0xBA },
			{ 0x51, 0x55, 0x61, 0x65, 0x66, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA },
			{ 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xB6, 0xBA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xBB },
			{ 0x54, 0x55, 0x64, 0x65, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA },
			{ 0x55, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA5, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xB9, 0xBA },
			{ 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
			{ 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA, 0xBB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xBE },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA5, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xBA },
			{ 0x40, 0x45, 0x51, 0x54, 0x55, 0x85, 0x91, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x85, 0x91, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA, 0xEA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x85, 0x91, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xD6, 0xEA },
			{ 0x41, 0x45, 0x51, 0x55, 0x56, 0x86, 0x92, 0x95, 0x96, 0x97, 0x9A, 0xA6, 0xAA, 0xAB, 0xD6, 0xEA, 0xEB },
			{ 0x44, 0x45, 0x54, 0x55, 0x59, 0x85, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xEA },
			{ 0x45, 0x55, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xDA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xDA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x86, 0x95, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB, 0xDA, 0xEA, 0xEB },
			{ 0x44, 0x45, 0x54, 0x55, 0x59, 0x85, 0x94, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xD9, 0xEA },
			{ 0x45, 0x55, 0x59, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xDA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xAA, 0xDA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB, 0xDA, 0xEA, 0xEB },
			{ 0x44, 0x45, 0x54, 0x55, 0x59, 0x89, 0x95, 0x98, 0x99, 0x9A, 0x9D, 0xA9, 0xAA, 0xAE, 0xD9, 0xEA, 0xEE },
			{ 0x45, 0x55, 0x59, 0x89, 0x95, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE, 0xDA, 0xEA, 0xEE },
			{ 0x45, 0x55, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE, 0xDA, 0xEA, 0xEE },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xDA, 0xEA, 0xEF },
			{ 0x50, 0x51, 0x54, 0x55, 0x65, 0x91, 0x94, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x51, 0x55, 0x91, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xE6, 0xEA },
			{ 0x51, 0x55, 0x56, 0x91, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xE6, 0xEA },
			{ 0x51, 0x55, 0x56, 0x92, 0x95, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB, 0xE6, 0xEA, 0xEB },
			{ 0x54, 0x55, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xE9, 0xEA },
			{ 0x55, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x55, 0x56, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x55, 0x56, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB },
			{ 0x54, 0x55, 0x59, 0x94, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xE9, 0xEA },
			{ 0x55, 0x59, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x45, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB },
			{ 0x54, 0x55, 0x59, 0x95, 0x98, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE, 0xE9, 0xEA, 0xEE },
			{ 0x55, 0x59, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
			{ 0x45, 0x55, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xAA, 0xAB, 0xAE, 0xEA, 0xEF },
			{ 0x50, 0x51, 0x54, 0x55, 0x65, 0x91, 0x94, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xE5, 0xEA },
			{ 0x51, 0x55, 0x65, 0x91, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xE6, 0xEA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x91, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xE6, 0xEA },
			{ 0x51, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xE6, 0xEA, 0xEB },
			{ 0x54, 0x55, 0x65, 0x94, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xE9, 0xEA },
			{ 0x55, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x51, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x94, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xE9, 0xEA },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x55, 0x56, 0x59, 0x65, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
			{ 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xEA, 0xEB },
			{ 0x54, 0x55, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xE9, 0xEA, 0xEE },
			{ 0x54, 0x55, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
			{ 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
			{ 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xEA },
			{ 0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0xA1, 0xA4, 0xA5, 0xA6, 0xA9, 0xAA, 0xB5, 0xBA, 0xE5, 0xEA, 0xFA },
			{ 0x51, 0x55, 0x65, 0x95, 0xA1, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA, 0xE6, 0xEA, 0xFA },
			{ 0x51, 0x55, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA, 0xE6, 0xEA, 0xFA },
			{ 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xE6, 0xEA, 0xFB },
			{ 0x54, 0x55, 0x65, 0x95, 0xA4, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA, 0xE9, 0xEA, 0xFA },
			{ 0x55, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
			{ 0x51, 0x55, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
			{ 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xAB, 0xBA, 0xEA, 0xFB },
			{ 0x54, 0x55, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA, 0xE9, 0xEA, 0xFA },
			{ 0x54, 0x55, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
			{ 0x55, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
			{ 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA, 0xEA },
			{ 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xE9, 0xEA, 0xFE },
			{ 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAE, 0xBA, 0xEA, 0xFE },
			{ 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA, 0xEA },
			{ 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xBA, 0xEA },
		};
		LatticePoint4D[] latticePoints = new LatticePoint4D[256];
		for (int i = 0; i < 256; i++) {
			int cx = ((i >> 0) & 3) - 1;
			int cy = ((i >> 2) & 3) - 1;
			int cz = ((i >> 4) & 3) - 1;
			int cw = ((i >> 6) & 3) - 1;
			latticePoints[i] = new LatticePoint4D(cx, cy, cz, cw);
		}
		for (int i = 0; i < 256; i++) {
			LOOKUP_4D[i] = new LatticePoint4D[lookup4DPregen[i].length];
			for (int j = 0; j < lookup4DPregen[i].length; j++) {
				LOOKUP_4D[i][j] = latticePoints[lookup4DPregen[i][j]];
			}
		}
	}

	@Override
	public double getNoise(double x, double y) {
		return noise2(x, y);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, long seed) {
		return noise2(x + (seed & 0xFFFFFFFFL), y + (seed >>> 32));
	}

	@Override
	public double getNoise(double x, double y, double z) {
		return noise3_Classic(x, y, z);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, long seed) {
		return noise3_Classic(x + (seed & 0x1FFFFFL), y + (seed >>> 21 & 0x1FFFFFL), z + (seed >>> 42));
	}

	@Override
	public double getNoise(double x, double y, double z, double w) {
		return noise4_Classic(x, y, z, w);
	}

	@Override
	public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
		return noise4_Classic(x + (seed & 0xFFFFL), y + (seed >>> 16 & 0xFFFFL), z + (seed >>> 32 & 0xFFFFL), w + (seed >>> 48));
	}

	private static class LatticePoint2D {
		int xsv, ysv;
		double dx, dy;
		public LatticePoint2D(int xsv, int ysv) {
			this.xsv = xsv; this.ysv = ysv;
			double ssv = (xsv + ysv) * -0.211324865405187;
			this.dx = -xsv - ssv;
			this.dy = -ysv - ssv;
		}
	}
	
	private static class LatticePoint3D {
		public double dxr, dyr, dzr;
		public int xrv, yrv, zrv;
		LatticePoint3D nextOnFailure, nextOnSuccess;
		public LatticePoint3D(int xrv, int yrv, int zrv, int lattice) {
			this.dxr = -xrv + lattice * 0.5; this.dyr = -yrv + lattice * 0.5; this.dzr = -zrv + lattice * 0.5;
			this.xrv = xrv + lattice * 1024; this.yrv = yrv + lattice * 1024; this.zrv = zrv + lattice * 1024;
		}
	}
	
	private static class LatticePoint4D {
		int xsv, ysv, zsv, wsv;
		double dx, dy, dz, dw;
		public LatticePoint4D(int xsv, int ysv, int zsv, int wsv) {
			this.xsv = xsv; this.ysv = ysv; this.zsv = zsv; this.wsv = wsv;
			double ssv = (xsv + ysv + zsv + wsv) * -0.138196601125011;
			this.dx = -xsv - ssv;
			this.dy = -ysv - ssv;
			this.dz = -zsv - ssv;
			this.dw = -wsv - ssv;
		}
	}
	
	/*
	 * Gradients
	 */
	
	private static class Grad2 {
		double dx, dy;
		public Grad2(double dx, double dy) {
			this.dx = dx; this.dy = dy;
		}
	}
	
	private static class Grad3 {
		double dx, dy, dz;
		public Grad3(double dx, double dy, double dz) {
			this.dx = dx; this.dy = dy; this.dz = dz;
		}
	}
	
	private static class Grad4 {
		double dx, dy, dz, dw;
		public Grad4(double dx, double dy, double dz,  double dw) {
			this.dx = dx; this.dy = dy; this.dz = dz; this.dw = dw;
		}
	}
	
//	private static final double N2 = 0.0551779947884094; // used for the FastNoise gradients
	private static final double N2 = 0.05481866495625118; // used for the OpenSimplex2S original gradients
	private static final double N3 = 0.2781926117527186;
	private static final double N4 = 0.11127401889945551;
	private static final Grad2[] GRADIENTS_2D;
	private static final Grad3[] GRADIENTS_3D;
	private static final Grad4[] GRADIENTS_4D;
	static {
		
		GRADIENTS_2D = new Grad2[PSIZE];
		Grad2[] grad2 = {
				// from FastNoise and SeededNoise
				new Grad2(0.6499429579167653, 0.759982994187637),
				new Grad2(-0.1551483029088119, 0.9878911904175052),
				new Grad2(-0.8516180517334043, 0.5241628506120981),
				new Grad2(-0.9518580082090311, -0.30653928330368374),
				new Grad2(-0.38568876701087174, -0.9226289476282616),
				new Grad2(0.4505066120763985, -0.8927730912586049),
				new Grad2(0.9712959670388622, -0.23787421973396244),
				new Grad2(0.8120673355833279, 0.5835637432865366),
				new Grad2(0.08429892519436613, 0.9964405106232257),
				new Grad2(-0.702488350003267, 0.7116952424385647),
				new Grad2(-0.9974536374007479, -0.07131788861160528),
				new Grad2(-0.5940875849508908, -0.804400361391775),
				new Grad2(0.2252075529515288, -0.9743108118529653),
				new Grad2(0.8868317111719171, -0.4620925405802277),
				new Grad2(0.9275724981153959, 0.373643226540993),
				new Grad2(0.3189067150428103, 0.9477861083074618),
				new Grad2(-0.5130301507665112, 0.8583705868705491),
				new Grad2(-0.9857873824221494, 0.1679977281313266),
				new Grad2(-0.7683809836504446, -0.6399927061806058),
				new Grad2(-0.013020236219374872, -0.9999152331316848),
				new Grad2(0.7514561619680513, -0.6597830223946701),
				new Grad2(0.9898275175279653, 0.14227257481477412),
				new Grad2(0.5352066871710182, 0.8447211386057674),
				new Grad2(-0.29411988281443646, 0.9557685360657266),
				new Grad2(-0.9175289804081126, 0.39766892022290273),
				new Grad2(-0.8985631161871687, -0.43884430750324743),
				new Grad2(-0.2505005588110731, -0.968116454790094),
				new Grad2(0.5729409678802212, -0.8195966369650838),
				new Grad2(0.9952584535626074, -0.09726567026534665),
				new Grad2(0.7207814785200723, 0.6931623620930514),
				new Grad2(-0.05832476124070039, 0.998297662136006),
				new Grad2(-0.7965970142012075, 0.6045107087270838),
				new Grad2(-0.977160478114496, -0.21250270589112422),
				new Grad2(-0.4736001288089817, -0.8807399831914728),
				new Grad2(0.36153434093875386, -0.9323587937709286),
				new Grad2(0.9435535266854258, -0.3312200813348966),
				new Grad2(0.8649775992346886, 0.5018104750024599),
				new Grad2(0.1808186720712497, 0.9835164502083277),
				new Grad2(-0.6299339540895539, 0.7766487066139361),
				new Grad2(-0.9996609468975833, 0.02603826506945166),
				new Grad2(-0.6695112313914258, -0.7428019325774111),
				new Grad2(0.12937272671950842, -0.9915960354807594),
				new Grad2(0.8376810167470904, -0.5461597881403947),
				new Grad2(0.959517028911149, 0.28165061908243916),
				new Grad2(0.4095816551369482, 0.9122734610714476),
				new Grad2(-0.42710760401484793, 0.9042008043530463),
				new Grad2(-0.9647728141412515, 0.2630844295924223),
				new Grad2(-0.8269869890664444, -0.562221059650754),
				new Grad2(-0.11021592552380209, -0.9939076666174438),
				new Grad2(0.6837188597775012, -0.72974551782423),
				new Grad2(0.998972441738333, 0.04532174585508431),
				new Grad2(0.6148313475439905, 0.7886586169422362),
				new Grad2(-0.1997618324529528, 0.9798444827088829),
				new Grad2(-0.8744989400706802, 0.48502742583822706),
				new Grad2(-0.9369870231562731, -0.3493641630687752),
				new Grad2(-0.3434772946489506, -0.9391609809082988),
				new Grad2(0.4905057254335028, -0.8714379687143274),
				new Grad2(0.9810787787756657, -0.1936089611460388),
				new Grad2(0.7847847614201463, 0.6197684069414349),
				new Grad2(0.03905187955516296, 0.9992371844077906),
				new Grad2(-0.7340217731995672, 0.6791259356474049),
				new Grad2(-0.9931964444524306, -0.1164509455824639),
				new Grad2(-0.5570202966000876, -0.830498879695542),
				new Grad2(0.2691336060685578, -0.9631028512493016),
				new Grad2(0.9068632806061, -0.4214249521425399),
				new Grad2(0.9096851999779008, 0.4152984913783901),
				new Grad2(0.27562369868737335, 0.9612656119522284),
				new Grad2(-0.5514058359842319, 0.8342371389734039),
				new Grad2(-0.9923883787916933, 0.12314749546456379),
				new Grad2(-0.7385858406439617, -0.6741594440488484),
				new Grad2(0.032311046904542805, -0.9994778618098213),
				new Grad2(0.7805865154410089, -0.6250477517051506),
				new Grad2(0.9823623706068018, 0.18698709264487903),
				new Grad2(0.49637249435561115, 0.8681096398768929),
				new Grad2(-0.3371347561867868, 0.9414564016304079),
				new Grad2(-0.9346092156607797, 0.35567627697379833),
				new Grad2(-0.877750600058892, -0.47911781859606817),
				new Grad2(-0.20636642697019966, -0.9784747813917093),
				new Grad2(0.6094977881394418, -0.7927877687333024),
				new Grad2(0.998644017504346, -0.052058873429796634),
				new Grad2(0.6886255051458764, 0.7251171723677399),
				new Grad2(-0.10350942208147358, 0.9946284731196666),
				new Grad2(-0.8231759450656516, 0.567786371327519),
				new Grad2(-0.9665253951623188, -0.2565709658288005),
				new Grad2(-0.43319680340129196, -0.9012993562201753),
				new Grad2(0.4034189716368784, -0.9150153732716426),
				new Grad2(0.9575954428121146, -0.28811624026678895),
				new Grad2(0.8413458575409575, 0.5404971304259356),
				new Grad2(0.13605818775026976, 0.9907008476558967),
				new Grad2(-0.664485735550556, 0.7473009482463117),
				new Grad2(-0.999813836664718, -0.01929487014147803),
				new Grad2(-0.6351581891853917, -0.7723820781910558),
				new Grad2(0.17418065221630152, -0.984713714941304),
				new Grad2(0.8615731658120597, -0.5076334109892543),
				new Grad2(0.945766171482902, 0.32484819358982736),
				new Grad2(0.3678149601703667, 0.9298990026206456),
				new Grad2(-0.4676486851245607, 0.883914423064399),
				new Grad2(-0.9757048995218635, 0.2190889067228882),
				new Grad2(-0.8006563717736747, -0.5991238388999518),
				new Grad2(-0.06505704156910719, -0.9978815467490495),
				new Grad2(0.716089639712196, -0.6980083293893113),
				new Grad2(0.9958918787052943, 0.09055035024139549),
				new Grad2(0.5784561871098056, 0.8157134543418942),
				new Grad2(-0.24396482815448167, 0.9697840804135497),
				new Grad2(-0.8955826311865743, 0.4448952131872543),
				new Grad2(-0.9201904205900768, -0.39147105876968413),
				new Grad2(-0.3005599364234082, -0.9537629289384008),
				new Grad2(0.5294967923694863, -0.84831193960148),
				new Grad2(0.9888453593035162, -0.1489458135829932),
				new Grad2(0.7558893631265085, 0.6546993743025888),
				new Grad2(-0.006275422246980369, 0.9999803093439501),
				new Grad2(-0.764046696121276, 0.6451609459244744),
				new Grad2(-0.9868981170802014, -0.16134468229090512),
				new Grad2(-0.5188082666339063, -0.8548906260290385),
				new Grad2(0.31250655826478446, -0.9499156020623616),
				new Grad2(0.9250311403279032, -0.3798912863223621),
				new Grad2(0.889928392754896, 0.45610026942404636),
				new Grad2(0.2317742435145519, 0.9727696027545563),
				new Grad2(-0.5886483179573486, 0.8083892365475831),
				new Grad2(-0.996949901406418, 0.0780441803450664),
				new Grad2(-0.707272817672466, -0.7069407057042696),
				new Grad2(0.07757592706207364, -0.9969864470194466),
				new Grad2(0.8081126726681943, -0.5890279350532263),
				new Grad2(0.9728783545459001, 0.23131733021125322),
				new Grad2(0.4565181982253288, 0.8897140746830408),
				new Grad2(-0.3794567783511009, 0.9252094645881026),
				new Grad2(-0.9497687200714887, 0.31295267753091066),
				new Grad2(-0.8551342041690687, -0.5184066867432686),
				new Grad2(-0.16180818807538452, -0.9868222283024238),
				new Grad2(0.6448020194233159, -0.7643496292585048),
				new Grad2(0.9999772516247822, -0.006745089543285545),
				new Grad2(0.6550543261176665, 0.7555817823601425),
				new Grad2(-0.14848135899860646, 0.9889152066936411),
				new Grad2(-0.848063153443784, 0.5298951667745091),
				new Grad2(-0.9539039899003245, -0.300111942535184),
				new Grad2(-0.3919032080850608, -0.9200064540494471),
				new Grad2(0.44447452934057863, -0.8957914895596358),
				new Grad2(0.9696693887216105, -0.24442028675267172),
				new Grad2(0.8159850520735595, 0.5780730012658526),
				new Grad2(0.0910180879994953, 0.9958492394217692),
				new Grad2(-0.6976719213969089, 0.7164173993520435),
				new Grad2(-0.9979119924958648, -0.06458835214597858),
				new Grad2(-0.5994998228898376, -0.8003748886334786),
				new Grad2(0.2186306161766729, -0.9758076929755208),
				new Grad2(0.8836946816279001, -0.46806378802740584),
				new Grad2(0.9300716543684309, 0.36737816720699407),
				new Grad2(0.32529236260160294, 0.9456134933645286),
				new Grad2(-0.5072286936943775, 0.8618114946396893),
				new Grad2(-0.9846317976415725, 0.17464313062106204),
				new Grad2(-0.7726803123417516, -0.6347953488483143),
				new Grad2(-0.019764457813331488, -0.9998046640256011),
				new Grad2(0.7469887719961158, -0.6648366525032559),
				new Grad2(0.9907646418168752, 0.13559286310672486),
				new Grad2(0.5408922318074902, 0.8410919055432124),
				new Grad2(-0.2876664477065717, 0.9577306588304888),
				new Grad2(-0.9148257956391065, 0.40384868903250853),
				new Grad2(-0.9015027194859215, -0.4327734358292892),
				new Grad2(-0.2570248925062563, -0.9664047830139022),
				new Grad2(0.5673996816983953, -0.8234425306046317),
				new Grad2(0.9945797473944409, -0.10397656501736473),
				new Grad2(0.7254405241129018, 0.6882848581617921),
				new Grad2(-0.05158982732517303, 0.9986683582233687),
				new Grad2(-0.7925014140531963, 0.609870075281354),
				new Grad2(-0.9785715990807187, -0.20590683687679034),
				new Grad2(-0.47953002522651733, -0.8775254725113429),
				new Grad2(0.35523727306945746, -0.9347761656258549),
				new Grad2(0.9412979532686209, -0.33757689964259285),
				new Grad2(0.868342678987353, 0.4959647082697184),
				new Grad2(0.18744846526420056, 0.9822744386728669),
				new Grad2(-0.6246810590458048, 0.7808800000444446),
				new Grad2(-0.9994625758058275, 0.03278047534097766),
				new Grad2(-0.674506266646887, -0.738269121834361),
				new Grad2(0.12268137965007223, -0.9924461089082646),
				new Grad2(0.8339780641890598, -0.5517975973592748),
				new Grad2(0.9613949601033843, 0.2751721837101493),
				new Grad2(0.41572570400265835, 0.9094900433932711),
				new Grad2(-0.42099897262033487, 0.907061114287578),
				new Grad2(-0.9629763390922247, 0.2695859238694348),
				new Grad2(-0.8307604078465821, -0.5566301687427484),
				new Grad2(-0.11691741449967302, -0.9931416405461567),
				new Grad2(0.6787811074228051, -0.7343406622310046),
				new Grad2(0.999255415972447, 0.03858255628819732),
				new Grad2(0.6201369341201711, 0.7844935837468874),
				new Grad2(-0.19314814942146824, 0.9811696042861612),
				new Grad2(-0.8712074932224428, 0.4909149659086258),
				new Grad2(-0.9393222007870077, -0.34303615422962713),
				new Grad2(-0.3498042060103595, -0.9368228314134226),
				new Grad2(0.4846166400948296, -0.8747266499559725),
				new Grad2(0.9797505510481769, -0.20022202106859724),
				new Grad2(0.7889473022428521, 0.6144608647291752),
				new Grad2(0.045790935472179155, 0.9989510449609544),
				new Grad2(-0.7294243101497431, 0.684061529222753),
				new Grad2(-0.9939593229024027, -0.10974909756074072),
				new Grad2(-0.562609414602539, -0.8267228354174018),
				new Grad2(0.26263126874523307, -0.9648962724963078),
				new Grad2(0.9040001019019392, -0.4275322394408211),
				new Grad2(0.9124657316291773, 0.4091531358824348),
				new Grad2(0.28210125132356934, 0.9593846381935018),
				new Grad2(-0.5457662881946498, 0.8379374431723614),
				new Grad2(-0.9915351626845509, 0.12983844253579577),
				new Grad2(-0.7431163048326799, -0.6691622803863227),
				new Grad2(0.02556874420628532, -0.9996730662170076),
				new Grad2(0.7763527553119807, -0.6302986588273021),
				new Grad2(0.9836012681423212, 0.1803567168386515),
				new Grad2(0.5022166799422209, 0.8647418148718223),
				new Grad2(-0.330776879188771, 0.9437089891455613),
				new Grad2(-0.9321888864830543, 0.3619722087639923),
				new Grad2(-0.8809623252471085, -0.47318641305008735),
				new Grad2(-0.21296163248563432, -0.9770605626515961),
				new Grad2(0.604136498566135, -0.7968808512571063),
				new Grad2(0.9982701582127194, -0.05879363249495786),
				new Grad2(0.6935008202914851, 0.7204558364362367),
				new Grad2(-0.09679820929680796, 0.9953040272584711),
				new Grad2(-0.8193274492343137, 0.5733258505694586),
				new Grad2(-0.9682340024187017, -0.25004582891994304),
				new Grad2(-0.4392662937408502, -0.8983569018954422),
				new Grad2(0.39723793388455464, -0.9177156552457467),
				new Grad2(0.9556302892322005, -0.2945687530984589),
				new Grad2(0.8449724198323217, 0.5348098818484104),
				new Grad2(0.14273745857559722, 0.9897605861618151),
				new Grad2(-0.6594300077680133, 0.7517659641504648),
				new Grad2(-0.9999212381512442, -0.01255059735959867),
				new Grad2(-0.6403535266476091, -0.768080308893523),
				new Grad2(0.16753470770767478, -0.9858661784001437),
				new Grad2(0.8581295336101056, -0.5134332513054668),
				new Grad2(0.9479357869928937, 0.31846152630759517),
				new Grad2(0.37407884501651706, 0.9273969040875156),
				new Grad2(-0.461675964944643, 0.8870486477034012),
				new Grad2(-0.9742049295269273, 0.22566513972130173),
				new Grad2(-0.8046793020829978, -0.5937097108850584),
				new Grad2(-0.07178636201352963, -0.9974200309943962),
				new Grad2(0.7113652211526822, -0.7028225395748172),
				new Grad2(0.9964799940037152, 0.08383091047075403),
				new Grad2(0.5839450884626246, 0.8117931594072332),
				new Grad2(-0.23741799789097484, 0.9714075840127259),
				new Grad2(-0.8925614000865144, 0.45092587758477687),
				new Grad2(-0.9228099950981292, -0.38525538665538556),
				new Grad2(-0.30698631553196837, -0.95171392869712),
				new Grad2(0.5237628071845146, -0.8518641451605984),
				new Grad2(0.9878182118285335, -0.15561227580071732),
				new Grad2(0.7602881737752754, 0.6495859395164404),
				new Grad2(4.6967723669845613E-4, 0.9999998897016406),
				new Grad2(-0.7596776469502666, 0.6502998329417794),
				new Grad2(-0.9879639510809196, -0.15468429579171308),
				new Grad2(-0.5245627784110601, -0.8513717704420726),
				new Grad2(0.3060921834538644, -0.9520018777441807),
				new Grad2(0.9224476966294768, -0.3861220622846781),
				new Grad2(0.8929845854878761, 0.45008724718774934),
				new Grad2(0.23833038910266038, 0.9711841358002995),
				new Grad2(-0.5831822693781987, 0.8123413326200348),
				new Grad2(-0.9964008074312266, 0.0847669213219385),
				new Grad2(-0.712025106726807, -0.7021540054650968),
				new Grad2(0.07084939947717452, -0.9974870237721009),
				new Grad2(0.8041212432524677, -0.5944653279629567),
				new Grad2(0.9744164792492415, 0.22474991650168097),
				new Grad2(0.462509014279733, 0.8866145790082576),

/*				// Original from OpenSimplex2S
				new Grad2( 0.130526192220052,  0.99144486137381),
				new Grad2( 0.38268343236509,   0.923879532511287),
				new Grad2( 0.608761429008721,  0.793353340291235),
				new Grad2( 0.793353340291235,  0.608761429008721),
				new Grad2( 0.923879532511287,  0.38268343236509),
				new Grad2( 0.99144486137381,   0.130526192220051),
				new Grad2( 0.99144486137381,  -0.130526192220051),
				new Grad2( 0.923879532511287, -0.38268343236509),
				new Grad2( 0.793353340291235, -0.60876142900872),
				new Grad2( 0.608761429008721, -0.793353340291235),
				new Grad2( 0.38268343236509,  -0.923879532511287),
				new Grad2( 0.130526192220052, -0.99144486137381),
				new Grad2(-0.130526192220052, -0.99144486137381),
				new Grad2(-0.38268343236509,  -0.923879532511287),
				new Grad2(-0.608761429008721, -0.793353340291235),
				new Grad2(-0.793353340291235, -0.608761429008721),
				new Grad2(-0.923879532511287, -0.38268343236509),
				new Grad2(-0.99144486137381,  -0.130526192220052),
				new Grad2(-0.99144486137381,   0.130526192220051),
				new Grad2(-0.923879532511287,  0.38268343236509),
				new Grad2(-0.793353340291235,  0.608761429008721),
				new Grad2(-0.608761429008721,  0.793353340291235),
				new Grad2(-0.38268343236509,   0.923879532511287),
				new Grad2(-0.130526192220052,  0.99144486137381)
*/
		};

		for (int i = 0; i < grad2.length; i++) {
			grad2[i].dx /= N2; grad2[i].dy /= N2;
		}
		for (int i = 0; i < PSIZE; i++) {
			GRADIENTS_2D[i] = grad2[i % grad2.length];
		}
		
		GRADIENTS_3D = new Grad3[PSIZE];
		Grad3[] grad3 = {
			new Grad3(-2.22474487139,      -2.22474487139,      -1.0),
			new Grad3(-2.22474487139,      -2.22474487139,       1.0),
			new Grad3(-3.0862664687972017, -1.1721513422464978,  0.0),
			new Grad3(-1.1721513422464978, -3.0862664687972017,  0.0),
			new Grad3(-2.22474487139,      -1.0,                -2.22474487139),
			new Grad3(-2.22474487139,       1.0,                -2.22474487139),
			new Grad3(-1.1721513422464978,  0.0,                -3.0862664687972017),
			new Grad3(-3.0862664687972017,  0.0,                -1.1721513422464978),
			new Grad3(-2.22474487139,      -1.0,                 2.22474487139),
			new Grad3(-2.22474487139,       1.0,                 2.22474487139),
			new Grad3(-3.0862664687972017,  0.0,                 1.1721513422464978),
			new Grad3(-1.1721513422464978,  0.0,                 3.0862664687972017),
			new Grad3(-2.22474487139,       2.22474487139,      -1.0),
			new Grad3(-2.22474487139,       2.22474487139,       1.0),
			new Grad3(-1.1721513422464978,  3.0862664687972017,  0.0),
			new Grad3(-3.0862664687972017,  1.1721513422464978,  0.0),
			new Grad3(-1.0,                -2.22474487139,      -2.22474487139),
			new Grad3( 1.0,                -2.22474487139,      -2.22474487139),
			new Grad3( 0.0,                -3.0862664687972017, -1.1721513422464978),
			new Grad3( 0.0,                -1.1721513422464978, -3.0862664687972017),
			new Grad3(-1.0,                -2.22474487139,       2.22474487139),
			new Grad3( 1.0,                -2.22474487139,       2.22474487139),
			new Grad3( 0.0,                -1.1721513422464978,  3.0862664687972017),
			new Grad3( 0.0,                -3.0862664687972017,  1.1721513422464978),
			new Grad3(-1.0,                 2.22474487139,      -2.22474487139),
			new Grad3( 1.0,                 2.22474487139,      -2.22474487139),
			new Grad3( 0.0,                 1.1721513422464978, -3.0862664687972017),
			new Grad3( 0.0,                 3.0862664687972017, -1.1721513422464978),
			new Grad3(-1.0,                 2.22474487139,       2.22474487139),
			new Grad3( 1.0,                 2.22474487139,       2.22474487139),
			new Grad3( 0.0,                 3.0862664687972017,  1.1721513422464978),
			new Grad3( 0.0,                 1.1721513422464978,  3.0862664687972017),
			new Grad3( 2.22474487139,      -2.22474487139,      -1.0),
			new Grad3( 2.22474487139,      -2.22474487139,       1.0),
			new Grad3( 1.1721513422464978, -3.0862664687972017,  0.0),
			new Grad3( 3.0862664687972017, -1.1721513422464978,  0.0),
			new Grad3( 2.22474487139,      -1.0,                -2.22474487139),
			new Grad3( 2.22474487139,       1.0,                -2.22474487139),
			new Grad3( 3.0862664687972017,  0.0,                -1.1721513422464978),
			new Grad3( 1.1721513422464978,  0.0,                -3.0862664687972017),
			new Grad3( 2.22474487139,      -1.0,                 2.22474487139),
			new Grad3( 2.22474487139,       1.0,                 2.22474487139),
			new Grad3( 1.1721513422464978,  0.0,                 3.0862664687972017),
			new Grad3( 3.0862664687972017,  0.0,                 1.1721513422464978),
			new Grad3( 2.22474487139,       2.22474487139,      -1.0),
			new Grad3( 2.22474487139,       2.22474487139,       1.0),
			new Grad3( 3.0862664687972017,  1.1721513422464978,  0.0),
			new Grad3( 1.1721513422464978,  3.0862664687972017,  0.0)
		};
		for (int i = 0; i < grad3.length; i++) {
			grad3[i].dx /= N3; grad3[i].dy /= N3; grad3[i].dz /= N3;
		}
		for (int i = 0; i < PSIZE; i++) {
			GRADIENTS_3D[i] = grad3[i % grad3.length];
		}
		
		GRADIENTS_4D = new Grad4[PSIZE];
		Grad4[] grad4 = {
			new Grad4(-0.753341017856078,    -0.37968289875261624,  -0.37968289875261624,  -0.37968289875261624),
			new Grad4(-0.7821684431180708,   -0.4321472685365301,   -0.4321472685365301,    0.12128480194602098),
			new Grad4(-0.7821684431180708,   -0.4321472685365301,    0.12128480194602098,  -0.4321472685365301),
			new Grad4(-0.7821684431180708,    0.12128480194602098,  -0.4321472685365301,   -0.4321472685365301),
			new Grad4(-0.8586508742123365,   -0.508629699630796,     0.044802370851755174,  0.044802370851755174),
			new Grad4(-0.8586508742123365,    0.044802370851755174, -0.508629699630796,     0.044802370851755174),
			new Grad4(-0.8586508742123365,    0.044802370851755174,  0.044802370851755174, -0.508629699630796),
			new Grad4(-0.9982828964265062,   -0.03381941603233842,  -0.03381941603233842,  -0.03381941603233842),
			new Grad4(-0.37968289875261624,  -0.753341017856078,    -0.37968289875261624,  -0.37968289875261624),
			new Grad4(-0.4321472685365301,   -0.7821684431180708,   -0.4321472685365301,    0.12128480194602098),
			new Grad4(-0.4321472685365301,   -0.7821684431180708,    0.12128480194602098,  -0.4321472685365301),
			new Grad4( 0.12128480194602098,  -0.7821684431180708,   -0.4321472685365301,   -0.4321472685365301),
			new Grad4(-0.508629699630796,    -0.8586508742123365,    0.044802370851755174,  0.044802370851755174),
			new Grad4( 0.044802370851755174, -0.8586508742123365,   -0.508629699630796,     0.044802370851755174),
			new Grad4( 0.044802370851755174, -0.8586508742123365,    0.044802370851755174, -0.508629699630796),
			new Grad4(-0.03381941603233842,  -0.9982828964265062,   -0.03381941603233842,  -0.03381941603233842),
			new Grad4(-0.37968289875261624,  -0.37968289875261624,  -0.753341017856078,    -0.37968289875261624),
			new Grad4(-0.4321472685365301,   -0.4321472685365301,   -0.7821684431180708,    0.12128480194602098),
			new Grad4(-0.4321472685365301,    0.12128480194602098,  -0.7821684431180708,   -0.4321472685365301),
			new Grad4( 0.12128480194602098,  -0.4321472685365301,   -0.7821684431180708,   -0.4321472685365301),
			new Grad4(-0.508629699630796,     0.044802370851755174, -0.8586508742123365,    0.044802370851755174),
			new Grad4( 0.044802370851755174, -0.508629699630796,    -0.8586508742123365,    0.044802370851755174),
			new Grad4( 0.044802370851755174,  0.044802370851755174, -0.8586508742123365,   -0.508629699630796),
			new Grad4(-0.03381941603233842,  -0.03381941603233842,  -0.9982828964265062,   -0.03381941603233842),
			new Grad4(-0.37968289875261624,  -0.37968289875261624,  -0.37968289875261624,  -0.753341017856078),
			new Grad4(-0.4321472685365301,   -0.4321472685365301,    0.12128480194602098,  -0.7821684431180708),
			new Grad4(-0.4321472685365301,    0.12128480194602098,  -0.4321472685365301,   -0.7821684431180708),
			new Grad4( 0.12128480194602098,  -0.4321472685365301,   -0.4321472685365301,   -0.7821684431180708),
			new Grad4(-0.508629699630796,     0.044802370851755174,  0.044802370851755174, -0.8586508742123365),
			new Grad4( 0.044802370851755174, -0.508629699630796,     0.044802370851755174, -0.8586508742123365),
			new Grad4( 0.044802370851755174,  0.044802370851755174, -0.508629699630796,    -0.8586508742123365),
			new Grad4(-0.03381941603233842,  -0.03381941603233842,  -0.03381941603233842,  -0.9982828964265062),
			new Grad4(-0.6740059517812944,   -0.3239847771997537,   -0.3239847771997537,    0.5794684678643381),
			new Grad4(-0.7504883828755602,   -0.4004672082940195,    0.15296486218853164,   0.5029860367700724),
			new Grad4(-0.7504883828755602,    0.15296486218853164,  -0.4004672082940195,    0.5029860367700724),
			new Grad4(-0.8828161875373585,    0.08164729285680945,   0.08164729285680945,   0.4553054119602712),
			new Grad4(-0.4553054119602712,   -0.08164729285680945,  -0.08164729285680945,   0.8828161875373585),
			new Grad4(-0.5029860367700724,   -0.15296486218853164,   0.4004672082940195,    0.7504883828755602),
			new Grad4(-0.5029860367700724,    0.4004672082940195,   -0.15296486218853164,   0.7504883828755602),
			new Grad4(-0.5794684678643381,    0.3239847771997537,    0.3239847771997537,    0.6740059517812944),
			new Grad4(-0.3239847771997537,   -0.6740059517812944,   -0.3239847771997537,    0.5794684678643381),
			new Grad4(-0.4004672082940195,   -0.7504883828755602,    0.15296486218853164,   0.5029860367700724),
			new Grad4( 0.15296486218853164,  -0.7504883828755602,   -0.4004672082940195,    0.5029860367700724),
			new Grad4( 0.08164729285680945,  -0.8828161875373585,    0.08164729285680945,   0.4553054119602712),
			new Grad4(-0.08164729285680945,  -0.4553054119602712,   -0.08164729285680945,   0.8828161875373585),
			new Grad4(-0.15296486218853164,  -0.5029860367700724,    0.4004672082940195,    0.7504883828755602),
			new Grad4( 0.4004672082940195,   -0.5029860367700724,   -0.15296486218853164,   0.7504883828755602),
			new Grad4( 0.3239847771997537,   -0.5794684678643381,    0.3239847771997537,    0.6740059517812944),
			new Grad4(-0.3239847771997537,   -0.3239847771997537,   -0.6740059517812944,    0.5794684678643381),
			new Grad4(-0.4004672082940195,    0.15296486218853164,  -0.7504883828755602,    0.5029860367700724),
			new Grad4( 0.15296486218853164,  -0.4004672082940195,   -0.7504883828755602,    0.5029860367700724),
			new Grad4( 0.08164729285680945,   0.08164729285680945,  -0.8828161875373585,    0.4553054119602712),
			new Grad4(-0.08164729285680945,  -0.08164729285680945,  -0.4553054119602712,    0.8828161875373585),
			new Grad4(-0.15296486218853164,   0.4004672082940195,   -0.5029860367700724,    0.7504883828755602),
			new Grad4( 0.4004672082940195,   -0.15296486218853164,  -0.5029860367700724,    0.7504883828755602),
			new Grad4( 0.3239847771997537,    0.3239847771997537,   -0.5794684678643381,    0.6740059517812944),
			new Grad4(-0.6740059517812944,   -0.3239847771997537,    0.5794684678643381,   -0.3239847771997537),
			new Grad4(-0.7504883828755602,   -0.4004672082940195,    0.5029860367700724,    0.15296486218853164),
			new Grad4(-0.7504883828755602,    0.15296486218853164,   0.5029860367700724,   -0.4004672082940195),
			new Grad4(-0.8828161875373585,    0.08164729285680945,   0.4553054119602712,    0.08164729285680945),
			new Grad4(-0.4553054119602712,   -0.08164729285680945,   0.8828161875373585,   -0.08164729285680945),
			new Grad4(-0.5029860367700724,   -0.15296486218853164,   0.7504883828755602,    0.4004672082940195),
			new Grad4(-0.5029860367700724,    0.4004672082940195,    0.7504883828755602,   -0.15296486218853164),
			new Grad4(-0.5794684678643381,    0.3239847771997537,    0.6740059517812944,    0.3239847771997537),
			new Grad4(-0.3239847771997537,   -0.6740059517812944,    0.5794684678643381,   -0.3239847771997537),
			new Grad4(-0.4004672082940195,   -0.7504883828755602,    0.5029860367700724,    0.15296486218853164),
			new Grad4( 0.15296486218853164,  -0.7504883828755602,    0.5029860367700724,   -0.4004672082940195),
			new Grad4( 0.08164729285680945,  -0.8828161875373585,    0.4553054119602712,    0.08164729285680945),
			new Grad4(-0.08164729285680945,  -0.4553054119602712,    0.8828161875373585,   -0.08164729285680945),
			new Grad4(-0.15296486218853164,  -0.5029860367700724,    0.7504883828755602,    0.4004672082940195),
			new Grad4( 0.4004672082940195,   -0.5029860367700724,    0.7504883828755602,   -0.15296486218853164),
			new Grad4( 0.3239847771997537,   -0.5794684678643381,    0.6740059517812944,    0.3239847771997537),
			new Grad4(-0.3239847771997537,   -0.3239847771997537,    0.5794684678643381,   -0.6740059517812944),
			new Grad4(-0.4004672082940195,    0.15296486218853164,   0.5029860367700724,   -0.7504883828755602),
			new Grad4( 0.15296486218853164,  -0.4004672082940195,    0.5029860367700724,   -0.7504883828755602),
			new Grad4( 0.08164729285680945,   0.08164729285680945,   0.4553054119602712,   -0.8828161875373585),
			new Grad4(-0.08164729285680945,  -0.08164729285680945,   0.8828161875373585,   -0.4553054119602712),
			new Grad4(-0.15296486218853164,   0.4004672082940195,    0.7504883828755602,   -0.5029860367700724),
			new Grad4( 0.4004672082940195,   -0.15296486218853164,   0.7504883828755602,   -0.5029860367700724),
			new Grad4( 0.3239847771997537,    0.3239847771997537,    0.6740059517812944,   -0.5794684678643381),
			new Grad4(-0.6740059517812944,    0.5794684678643381,   -0.3239847771997537,   -0.3239847771997537),
			new Grad4(-0.7504883828755602,    0.5029860367700724,   -0.4004672082940195,    0.15296486218853164),
			new Grad4(-0.7504883828755602,    0.5029860367700724,    0.15296486218853164,  -0.4004672082940195),
			new Grad4(-0.8828161875373585,    0.4553054119602712,    0.08164729285680945,   0.08164729285680945),
			new Grad4(-0.4553054119602712,    0.8828161875373585,   -0.08164729285680945,  -0.08164729285680945),
			new Grad4(-0.5029860367700724,    0.7504883828755602,   -0.15296486218853164,   0.4004672082940195),
			new Grad4(-0.5029860367700724,    0.7504883828755602,    0.4004672082940195,   -0.15296486218853164),
			new Grad4(-0.5794684678643381,    0.6740059517812944,    0.3239847771997537,    0.3239847771997537),
			new Grad4(-0.3239847771997537,    0.5794684678643381,   -0.6740059517812944,   -0.3239847771997537),
			new Grad4(-0.4004672082940195,    0.5029860367700724,   -0.7504883828755602,    0.15296486218853164),
			new Grad4( 0.15296486218853164,   0.5029860367700724,   -0.7504883828755602,   -0.4004672082940195),
			new Grad4( 0.08164729285680945,   0.4553054119602712,   -0.8828161875373585,    0.08164729285680945),
			new Grad4(-0.08164729285680945,   0.8828161875373585,   -0.4553054119602712,   -0.08164729285680945),
			new Grad4(-0.15296486218853164,   0.7504883828755602,   -0.5029860367700724,    0.4004672082940195),
			new Grad4( 0.4004672082940195,    0.7504883828755602,   -0.5029860367700724,   -0.15296486218853164),
			new Grad4( 0.3239847771997537,    0.6740059517812944,   -0.5794684678643381,    0.3239847771997537),
			new Grad4(-0.3239847771997537,    0.5794684678643381,   -0.3239847771997537,   -0.6740059517812944),
			new Grad4(-0.4004672082940195,    0.5029860367700724,    0.15296486218853164,  -0.7504883828755602),
			new Grad4( 0.15296486218853164,   0.5029860367700724,   -0.4004672082940195,   -0.7504883828755602),
			new Grad4( 0.08164729285680945,   0.4553054119602712,    0.08164729285680945,  -0.8828161875373585),
			new Grad4(-0.08164729285680945,   0.8828161875373585,   -0.08164729285680945,  -0.4553054119602712),
			new Grad4(-0.15296486218853164,   0.7504883828755602,    0.4004672082940195,   -0.5029860367700724),
			new Grad4( 0.4004672082940195,    0.7504883828755602,   -0.15296486218853164,  -0.5029860367700724),
			new Grad4( 0.3239847771997537,    0.6740059517812944,    0.3239847771997537,   -0.5794684678643381),
			new Grad4( 0.5794684678643381,   -0.6740059517812944,   -0.3239847771997537,   -0.3239847771997537),
			new Grad4( 0.5029860367700724,   -0.7504883828755602,   -0.4004672082940195,    0.15296486218853164),
			new Grad4( 0.5029860367700724,   -0.7504883828755602,    0.15296486218853164,  -0.4004672082940195),
			new Grad4( 0.4553054119602712,   -0.8828161875373585,    0.08164729285680945,   0.08164729285680945),
			new Grad4( 0.8828161875373585,   -0.4553054119602712,   -0.08164729285680945,  -0.08164729285680945),
			new Grad4( 0.7504883828755602,   -0.5029860367700724,   -0.15296486218853164,   0.4004672082940195),
			new Grad4( 0.7504883828755602,   -0.5029860367700724,    0.4004672082940195,   -0.15296486218853164),
			new Grad4( 0.6740059517812944,   -0.5794684678643381,    0.3239847771997537,    0.3239847771997537),
			new Grad4( 0.5794684678643381,   -0.3239847771997537,   -0.6740059517812944,   -0.3239847771997537),
			new Grad4( 0.5029860367700724,   -0.4004672082940195,   -0.7504883828755602,    0.15296486218853164),
			new Grad4( 0.5029860367700724,    0.15296486218853164,  -0.7504883828755602,   -0.4004672082940195),
			new Grad4( 0.4553054119602712,    0.08164729285680945,  -0.8828161875373585,    0.08164729285680945),
			new Grad4( 0.8828161875373585,   -0.08164729285680945,  -0.4553054119602712,   -0.08164729285680945),
			new Grad4( 0.7504883828755602,   -0.15296486218853164,  -0.5029860367700724,    0.4004672082940195),
			new Grad4( 0.7504883828755602,    0.4004672082940195,   -0.5029860367700724,   -0.15296486218853164),
			new Grad4( 0.6740059517812944,    0.3239847771997537,   -0.5794684678643381,    0.3239847771997537),
			new Grad4( 0.5794684678643381,   -0.3239847771997537,   -0.3239847771997537,   -0.6740059517812944),
			new Grad4( 0.5029860367700724,   -0.4004672082940195,    0.15296486218853164,  -0.7504883828755602),
			new Grad4( 0.5029860367700724,    0.15296486218853164,  -0.4004672082940195,   -0.7504883828755602),
			new Grad4( 0.4553054119602712,    0.08164729285680945,   0.08164729285680945,  -0.8828161875373585),
			new Grad4( 0.8828161875373585,   -0.08164729285680945,  -0.08164729285680945,  -0.4553054119602712),
			new Grad4( 0.7504883828755602,   -0.15296486218853164,   0.4004672082940195,   -0.5029860367700724),
			new Grad4( 0.7504883828755602,    0.4004672082940195,   -0.15296486218853164,  -0.5029860367700724),
			new Grad4( 0.6740059517812944,    0.3239847771997537,    0.3239847771997537,   -0.5794684678643381),
			new Grad4( 0.03381941603233842,   0.03381941603233842,   0.03381941603233842,   0.9982828964265062),
			new Grad4(-0.044802370851755174, -0.044802370851755174,  0.508629699630796,     0.8586508742123365),
			new Grad4(-0.044802370851755174,  0.508629699630796,    -0.044802370851755174,  0.8586508742123365),
			new Grad4(-0.12128480194602098,   0.4321472685365301,    0.4321472685365301,    0.7821684431180708),
			new Grad4( 0.508629699630796,    -0.044802370851755174, -0.044802370851755174,  0.8586508742123365),
			new Grad4( 0.4321472685365301,   -0.12128480194602098,   0.4321472685365301,    0.7821684431180708),
			new Grad4( 0.4321472685365301,    0.4321472685365301,   -0.12128480194602098,   0.7821684431180708),
			new Grad4( 0.37968289875261624,   0.37968289875261624,   0.37968289875261624,   0.753341017856078),
			new Grad4( 0.03381941603233842,   0.03381941603233842,   0.9982828964265062,    0.03381941603233842),
			new Grad4(-0.044802370851755174,  0.044802370851755174,  0.8586508742123365,    0.508629699630796),
			new Grad4(-0.044802370851755174,  0.508629699630796,     0.8586508742123365,   -0.044802370851755174),
			new Grad4(-0.12128480194602098,   0.4321472685365301,    0.7821684431180708,    0.4321472685365301),
			new Grad4( 0.508629699630796,    -0.044802370851755174,  0.8586508742123365,   -0.044802370851755174),
			new Grad4( 0.4321472685365301,   -0.12128480194602098,   0.7821684431180708,    0.4321472685365301),
			new Grad4( 0.4321472685365301,    0.4321472685365301,    0.7821684431180708,   -0.12128480194602098),
			new Grad4( 0.37968289875261624,   0.37968289875261624,   0.753341017856078,     0.37968289875261624),
			new Grad4( 0.03381941603233842,   0.9982828964265062,    0.03381941603233842,   0.03381941603233842),
			new Grad4(-0.044802370851755174,  0.8586508742123365,   -0.044802370851755174,  0.508629699630796),
			new Grad4(-0.044802370851755174,  0.8586508742123365,    0.508629699630796,    -0.044802370851755174),
			new Grad4(-0.12128480194602098,   0.7821684431180708,    0.4321472685365301,    0.4321472685365301),
			new Grad4( 0.508629699630796,     0.8586508742123365,   -0.044802370851755174, -0.044802370851755174),
			new Grad4( 0.4321472685365301,    0.7821684431180708,   -0.12128480194602098,   0.4321472685365301),
			new Grad4( 0.4321472685365301,    0.7821684431180708,    0.4321472685365301,   -0.12128480194602098),
			new Grad4( 0.37968289875261624,   0.753341017856078,     0.37968289875261624,   0.37968289875261624),
			new Grad4( 0.9982828964265062,    0.03381941603233842,   0.03381941603233842,   0.03381941603233842),
			new Grad4( 0.8586508742123365,   -0.044802370851755174, -0.044802370851755174,  0.508629699630796),
			new Grad4( 0.8586508742123365,   -0.044802370851755174,  0.508629699630796,    -0.044802370851755174),
			new Grad4( 0.7821684431180708,   -0.12128480194602098,   0.4321472685365301,    0.4321472685365301),
			new Grad4( 0.8586508742123365,    0.508629699630796,    -0.044802370851755174, -0.044802370851755174),
			new Grad4( 0.7821684431180708,    0.4321472685365301,   -0.12128480194602098,   0.4321472685365301),
			new Grad4( 0.7821684431180708,    0.4321472685365301,    0.4321472685365301,   -0.12128480194602098),
			new Grad4( 0.753341017856078,     0.37968289875261624,   0.37968289875261624,   0.37968289875261624)
		};
		for (int i = 0; i < grad4.length; i++) {
			grad4[i].dx /= N4; grad4[i].dy /= N4; grad4[i].dz /= N4; grad4[i].dw /= N4;
		}
		for (int i = 0; i < PSIZE; i++) {
			GRADIENTS_4D[i] = grad4[i % grad4.length];
		}
	}
}