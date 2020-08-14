package squidpony.examples;

import squidpony.squidmath.*;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 7/30/2020.
 */
public class MathWorkspace {
	private static double fraction(final double value){
		return value - (long)value;
	}
	public static void main(String[] args) {
		//// OLD, using GoatRNG:
		//SUMS:
		//x: +0.66677894631043, y: -0.67457046130430, z: -0.86203306900659, w: +0.63575181967243, u: +0.47384803638072
		//SUMS:
		//x: +0.78622392168906, y: +0.45377098605460, z: -0.46919628523987, w: -0.46668770371011, u: -0.70259586342991


		//GaussianDistribution g = GaussianDistribution.instance;
		//RNG rng = new RNG(new GoatRNG(0xB0BAFE7715DEADL, 0xBEEF1E57CAFEFEEDL));
		double x, y, z, w, u, inv;
		double[] data = new double[1280];
		int[] ants = new int[32]; // quadrants, octants, ???, ???
		long run = 1L;
		while (true) {
			Arrays.fill(ants, 0);
			int i = 0, a;
			while (i < 1280) {
				a = 0;
//				x = g.nextDouble(rng);
//				y = g.nextDouble(rng);
//				z = g.nextDouble(rng);
//				w = g.nextDouble(rng);
//				u = g.nextDouble(rng);
				x = MathExtras.probit(fraction(0.8812714616335696 * run));
				y = MathExtras.probit(fraction(0.7766393890897682 * run));
				z = MathExtras.probit(fraction(0.6844301295853426 * run));
				w = MathExtras.probit(fraction(0.6031687406857282 * run));
				u = MathExtras.probit(fraction(0.5315553977157913 * run));
				++run;
				// this uses 2.0 because... well in 3D, Perlin gradient vectors have the same length as the distance to the
				// center of an edge of a cube, or equivalently, the distance to any vertex on a unit square. In 5D, I'm
				// guessing that the distance to a vertex on a unit tesseract (4D hypercube) should work, which is 2.0.
				inv = 2.0 / Math.sqrt(x * x + y * y + z * z + w * w + u * u);
				if (x < 0.0) a |= 1;
				if (y < 0.0) a |= 2;
				if (z < 0.0) a |= 4;
				if (w < 0.0) a |= 8;
				if (u < 0.0) a |= 16;
				if (ants[a] < 8) {
					data[i++] = x * inv;
					data[i++] = y * inv;
					data[i++] = z * inv;
					data[i++] = w * inv;
					data[i++] = u * inv;
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
					Math.abs(x) < 0.625 &&
					Math.abs(y) < 0.625 &&
					Math.abs(z) < 0.625 &&
					Math.abs(w) < 0.625 &&
					Math.abs(u) < 0.625 && 
							Math.abs(x + y + z + w + u) < 0.2) {
				i = 0;
				for (int j = 0; j < 256; j++) {
					for (int k = 0; k < 5; k++) {
						System.out.printf("%+1.10ff, ", data[i++]);
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
