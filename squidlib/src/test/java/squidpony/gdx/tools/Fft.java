/* 
 * Free FFT and convolution (Java)
 * 
 * Copyright (c) 2017 Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/free-small-fft-in-multiple-languages
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */
package squidpony.gdx.tools;

public final class Fft {
	
	/* 
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function.
	 */
	public static void transform(double[] real, double[] imag) {
		int n = real.length;
		if (n != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		if (n != 0) {
			if ((n & (n - 1)) == 0)  // Is power of 2
				transformRadix2(real, imag);
			else  // More complicated algorithm for arbitrary sizes
				transformBluestein(real, imag);
		}
	}
	
	
	/* 
	 * Computes the inverse discrete Fourier transform (IDFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function. This transform does not perform scaling, so the inverse is not a true inverse.
	 */
	public static void inverseTransform(double[] real, double[] imag) {
		transform(imag, real);
	}

	private static double[] cosTable;
	private static double[] sinTable;
	
	public static void loadTables(final int n) {
		if (cosTable == null || sinTable == null || cosTable.length != n || sinTable.length != n) {
			cosTable = new double[n];
			sinTable = new double[n];
			for (int i = 0; i < n; i++) {
				cosTable[i] = Math.cos(i * Math.PI * 2.0 / n);
				sinTable[i] = Math.sin(i * Math.PI * 2.0 / n);
			}
		}
	}
	
	public static void loadTablesBluestein(final int n){
		if (cosTable == null || sinTable == null || cosTable.length != n || sinTable.length != n) {
			cosTable = new double[n];
			sinTable = new double[n];
			for (int i = 0, mask = n + n; i < n; i++) {
				int j = (i * i) % mask;
				cosTable[i] = Math.cos(Math.PI * j / n);
				sinTable[i] = Math.sin(Math.PI * j / n);
			}
		}
	}
	/* 
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
	 */
	public static void transformRadix2(double[] real, double[] imag) {
		// Length variables
		int n = real.length;
		if (n != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
		if (1 << levels != n)
			throw new IllegalArgumentException("Length is not a power of 2");
		loadTables(n);
		
		// Bit-reversed addressing permutation
		for (int i = 0; i < n; i++) {
			int j = Integer.reverse(i) >>> (32 - levels);
			if (j > i) {
				double temp = real[i];
				real[i] = real[j];
				real[j] = temp;
				temp = imag[i];
				imag[i] = imag[j];
				imag[j] = temp;
			}
		}
		
		// Cooley-Tukey decimation-in-time radix-2 FFT
		for (int size = 2; size <= n; size *= 2) {
			int halfsize = size / 2;
			int tablestep = n / size;
			for (int i = 0; i < n; i += size) {
				for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
					int l = j + halfsize;
					double tpre =  real[l] * cosTable[k] + imag[l] * sinTable[k];
					double tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k];
					real[l] = real[j] - tpre;
					imag[l] = imag[j] - tpim;
					real[j] += tpre;
					imag[j] += tpim;
				}
			}
			if (size == n)  // Prevent overflow in 'size *= 2'
				break;
		}
	}
	
	public static void transform2D(double[][] real, double[][] imag){
		final int n = real.length;
		loadTables(n);
		// window function
		for (int i = 0; i < n; i++) {
			double im = 0.5 * (1.0 - cosTable[i]);
			for (int j = 0; j < n; j++) {
				double jm = 0.5 * (1.0 - cosTable[j]);
				real[i][j] *= im * jm;
				imag[i][j] *= im * jm;
			}
		}

		for (int x = 0; x < n; x++) {
			transformRadix2(real[x], imag[x]);
		}
		double swap;
		for (int x = 0; x < n; x++) {
			for (int y = x + 1; y < n; y++) {
				swap = real[x][y];
				real[x][y] = real[y][x];
				real[y][x] = swap;
				swap = imag[x][y];
				imag[x][y] = imag[y][x];
				imag[y][x] = swap;
			}
		}
		for (int x = 0; x < n; x++) {
			transformRadix2(real[x], imag[x]);
		}
	}
	
	public static void getColors(double[][] real, double[][] imag, float[][] background){
		final int n = real.length, mask = n - 1, half = n >>> 1;
		double max = 0.0, mag, r, i;
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
//				r = real[x][y];
//				i = imag[x][y];
				r = real[x + half & mask][y + half & mask];
				i = imag[x + half & mask][y + half & mask];
				mag = Math.sqrt(r * r + i * i);
				max = Math.max(mag, max);
				background[x][y] = (float) mag;
			}
		}
		if(max <= 0.0)
			max = 1.0;
		double c = 255.0 / Math.log1p(max);
		int g;
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
				g = (int)(c * Math.log1p(background[x][y]));
				background[x][y] = Float.intBitsToFloat(g * 0x010101 | 0xFE000000);
			}
		}
	}
	
	/* 
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This requires the convolution function, which in turn requires the radix-2 FFT function.
	 * Uses Bluestein's chirp z-transform algorithm.
	 */
	public static void transformBluestein(double[] real, double[] imag) {
		// Find a power-of-2 convolution length m such that m >= n * 2 + 1
		int n = real.length;
		if (n != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		if (n >= 0x20000000)
			throw new IllegalArgumentException("Array too large");
		int m = Integer.highestOneBit(n) * 4;
		
		loadTablesBluestein(n);
		// Temporary vectors and preprocessing
		double[] areal = new double[m];
		double[] aimag = new double[m];
		for (int i = 0; i < n; i++) {
			areal[i] =  real[i] * cosTable[i] + imag[i] * sinTable[i];
			aimag[i] = -real[i] * sinTable[i] + imag[i] * cosTable[i];
		}
		double[] breal = new double[m];
		double[] bimag = new double[m];
		breal[0] = cosTable[0];
		bimag[0] = sinTable[0];
		for (int i = 1; i < n; i++) {
			breal[i] = breal[m - i] = cosTable[i];
			bimag[i] = bimag[m - i] = sinTable[i];
		}
		
		// Convolution
		double[] creal = new double[m];
		double[] cimag = new double[m];
		convolve(areal, aimag, breal, bimag, creal, cimag);
		
		// Postprocessing
		for (int i = 0; i < n; i++) {
			real[i] =  creal[i] * cosTable[i] + cimag[i] * sinTable[i];
			imag[i] = -creal[i] * sinTable[i] + cimag[i] * cosTable[i];
		}
	}
	
	
	/* 
	 * Computes the circular convolution of the given real vectors. Each vector's length must be the same.
	 */
	public static void convolve(double[] x, double[] y, double[] out) {
		int n = x.length;
		if (n != y.length || n != out.length)
			throw new IllegalArgumentException("Mismatched lengths");
		convolve(x, new double[n], y, new double[n], out, new double[n]);
	}
	
	
	/* 
	 * Computes the circular convolution of the given complex vectors. Each vector's length must be the same.
	 */
	public static void convolve(double[] xreal, double[] ximag,
			double[] yreal, double[] yimag, double[] outreal, double[] outimag) {
		
		int n = xreal.length;
		if (n != ximag.length || n != yreal.length || n != yimag.length
				|| n != outreal.length || n != outimag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		
		xreal = xreal.clone();
		ximag = ximag.clone();
		yreal = yreal.clone();
		yimag = yimag.clone();
		transform(xreal, ximag);
		transform(yreal, yimag);
		
		for (int i = 0; i < n; i++) {
			double temp = xreal[i] * yreal[i] - ximag[i] * yimag[i];
			ximag[i] = ximag[i] * yreal[i] + xreal[i] * yimag[i];
			xreal[i] = temp;
		}
		inverseTransform(xreal, ximag);
		
		for (int i = 0; i < n; i++) {  // Scaling (because this FFT implementation omits it)
			outreal[i] = xreal[i] / n;
			outimag[i] = ximag[i] / n;
		}
	}
	
}
