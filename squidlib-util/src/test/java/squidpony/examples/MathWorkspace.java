package squidpony.examples;

import squidpony.squidmath.GaussianDistribution;
import squidpony.squidmath.GoatRNG;
import squidpony.squidmath.MoonwalkRNG;
import squidpony.squidmath.RNG;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 7/30/2020.
 */
public class MathWorkspace {
	public static void main(String[] args) {
		GaussianDistribution g = GaussianDistribution.instance;
		//MoonwalkRNG rng = new MoonwalkRNG(0xB0BAFE7715DEADL);
		RNG rng = new RNG(new GoatRNG(0xB0BAFE7715DEADL, 0xBEEF1E57CAFEFEEDL));
		double x, y, z, w, u, inv;
		double[] data = new double[1280];
		int[] ants = new int[32]; // quadrants, octants, ???, ???
		while (true) {
			Arrays.fill(ants, 0);
			int i = 0, a;
			while (i < 1280) {
				a = 0;
				x = g.nextDouble(rng);
				y = g.nextDouble(rng);
				z = g.nextDouble(rng);
				w = g.nextDouble(rng);
				u = g.nextDouble(rng);
				// this uses 2.0 because... well in 3D, Perlin gradient vectors have the same length as the distance to the
				// center of an edge of a cube, or equivalently, the distance to any vertex on a unit square. In 5D, I'm
				// guessing that the distance to a vertex on a unit tesseract (4D hypercube) should work, which is 2.0.
				inv = 2.0 / Math.sqrt(x * x + y * y + z * z + w * w + u * u);
				if ((x *= inv) < 0.0) a |= 1;
				if ((y *= inv) < 0.0) a |= 2;
				if ((z *= inv) < 0.0) a |= 4;
				if ((w *= inv) < 0.0) a |= 8;
				if ((u *= inv) < 0.0) a |= 16;
				if (ants[a] < 8) {
					data[i++] = x;
					data[i++] = y;
					data[i++] = z;
					data[i++] = w;
					data[i++] = u;
					ants[a]++;
				}
			}
			x = 0;
			y = 0;
			z = 0;
			w = 0;
			u = 0;
			for (int j = 0; j < 1280; j += 5) {
				x += data[j];
				y += data[j + 1];
				z += data[j + 2];
				w += data[j + 3];
				u += data[j + 4];
			}
			if (
					Math.abs(x) < 1.0 &&
					Math.abs(y) < 1.0 &&
					Math.abs(z) < 1.0 &&
					Math.abs(w) < 1.0 &&
					Math.abs(u) < 1.0 && 
							Math.abs(x + y + z + w + u) < 1.0) {
				i = 0;
				for (int j = 0; j < 256; j++) {
					for (int k = 0; k < 5; k++) {
						System.out.printf("%+1.10f, ", data[i++]);
					}
					System.out.println();
				}

				System.out.println("\n");
				System.out.printf("SUMS:\nx: %+3.14f, y: %+3.14f, z: %+3.14f, w: %+3.14f, u: %+3.14f\n", x, y, z, w, u);
				return;
			}
		}
	}
}
