/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the "math NOTICE.txt" file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package squidpony.squidmath;
import java.lang.ArithmeticException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Implementation of a Sobol sequence as a Quasi-Random Number Generator.
 * <p>
 * A Sobol sequence is a low-discrepancy sequence with the property that for all values of N,
 * its subsequence (x1, ... xN) has a low discrepancy. It can be used to generate quasi-random
 * points in a space S, which are equi-distributed. This is not a true random number generator,
 * and is not even a pseudo-random number generator; the sequence generated from identical
 * starting points with identical dimensions will be exactly the same. Calling this class'
 * nextVector, nextIntVector, and nextLongVector methods all increment the position in the
 * sequence, and do not use separate sequences for separate types.
 * <p>
 * The implementation already comes with support for up to 1000 dimensions with direction numbers
 * calculated from <a href="http://web.maths.unsw.edu.au/~fkuo/sobol/">Stephen Joe and Frances Kuo</a>.
 * <p>
 * The generator supports two modes:
 * <ul>
 *   <li>sequential generation of points: {@link #nextVector()}, {@link #nextIntVector()},
 *   {@link #nextLongVector()}, and the bounded variants on each of those</li>
 *   <li>random access to the i-th point in the sequence: {@link #skipTo(int)}</li>
 * </ul>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Sobol_sequence">Sobol sequence (Wikipedia)</a>
 * @see <a href="http://web.maths.unsw.edu.au/~fkuo/sobol/">Sobol sequence direction numbers</a>
 *
 * Created by Tommy Ettinger on 5/2/2015 based off Apache Commons Math 4.
 */
public class SobolQRNG implements RandomnessSource {

    /** The number of bits to use. */
    private static final int BITS = 52;

    /** The scaling factor. */
    private static final double SCALE = Math.pow(2, BITS);

    /** The maximum supported space dimension. */
    private static final int MAX_DIMENSION = 1000;

    /** The resource containing the direction numbers. */
    private static final String RESOURCE_NAME = "/qrng/new-joe-kuo-6.1000.txt";

    /** Character set for file input. */
    private static final String FILE_CHARSET = "US-ASCII";

    /** Space dimension. */
    private final int dimension;

    /** The current index in the sequence. Starts at 1, not 0, because 0 acts differently and shouldn't be typical.*/
    private int count = 1;

    /** The direction vector for each component. */
    private final long[][] direction;

    /** The current state. */
    private final long[] x;

    /**
     * Construct a new Sobol sequence generator for the given space dimension.
     * You should call {@link #skipTo(int)} with a fairly large number (over 1000) to ensure the results aren't
     * too obviously non-random. If you skipTo(1), all doubles in that result will be 0.5, and if you skipTo(0),
     * all will be 0 (this class starts at index 1 instead of 0 for that reason). This is true for all dimensions.
     *
     * @param dimension the space dimension
     * @throws ArithmeticException if the space dimension is outside the allowed range of [1, 1000]
     */
    public SobolQRNG(final int dimension) throws ArithmeticException {
        if (dimension < 1 || dimension > MAX_DIMENSION) {
            throw new ArithmeticException("Dimension " + dimension + "is outside the allowed range of [1, 1000]");
        }

        // initialize the other dimensions with direction numbers from a resource
        final InputStream is = getClass().getResourceAsStream(RESOURCE_NAME);
        if (is == null) {
            throw new ArithmeticException("Could not load embedded resource 'new-joe-kuo-6.1000'");
        }

        this.dimension = dimension;

        // init data structures
        direction = new long[dimension][BITS + 1];
        x = new long[dimension];

        try {
            initFromStream(is);
        } catch (IOException e) {
            // the internal resource file could not be read -> should not happen
            throw new InternalError("Resource is unreadable, abort!\n" + e.getMessage());
        } catch (ArithmeticException e) {
            // the internal resource file could not be parsed -> should not happen
            throw new InternalError("Resource is unreadable, abort!\n" + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) { // NOPMD
                // ignore
            }
        }
    }
/*
    / **
     * Construct a new Sobol sequence generator for the given space dimension with
     * direction vectors loaded from the given stream.
     * <p>
     * The expected format is identical to the files available from
     * <a href="http://web.maths.unsw.edu.au/~fkuo/sobol/">Stephen Joe and Frances Kuo</a>.
     * The first line will be ignored as it is assumed to contain only the column headers.
     * The columns are:
     * <ul>
     *  <li>d: the dimension</li>
     *  <li>s: the degree of the primitive polynomial</li>
     *  <li>a: the number representing the coefficients</li>
     *  <li>m: the list of initial direction numbers</li>
     * </ul>
     * Example:
     * <pre>
     * d       s       a       m_i
     * 2       1       0       1
     * 3       2       1       1 3
     * </pre>
     * <p>
     * The input stream <i>must</i> be an ASCII text containing one valid direction vector per line.
     *
     * @param dimension the space dimension
     * @param is the stream to read the direction vectors from
     * @throws ArithmeticException if the space dimension is outside the range [1, max], where
     *   max refers to the maximum dimension found in the input stream
     * @throws IOException if an error occurs while reading from the input stream
     * /
    public SobolQRNG(final int dimension, final InputStream is)
            throws ArithmeticException, IOException {

        if (dimension < 1) {
            throw new ArithmeticException("The given dimension, " + dimension + ", is too low (must be greater than 0)");
        }

        this.dimension = dimension;

        // init data structures
        direction = new long[dimension][BITS + 1];
        x = new long[dimension];

        // initialize the other dimensions with direction numbers from the stream
        int lastDimension = initFromStream(is);
        if (lastDimension < dimension) {
            throw new ArithmeticException("Dimension " + dimension + "is outside the allowed range of [1, 1000]");
        }
    }
*/
    /**
     * Load the direction vector for each dimension from the given stream.
     * <p>
     * The input stream <i>must</i> be an ASCII text containing one
     * valid direction vector per line.
     *
     * @param is the input stream to read the direction vector from
     * @return the last dimension that has been read from the input stream
     * @throws IOException if the stream could not be read
     * @throws ArithmeticException if the content could not be parsed successfully
     */
    private int initFromStream(final InputStream is) throws ArithmeticException, IOException {

        // special case: dimension 1 -> use unit initialization
        for (int i = 1; i <= BITS; i++) {
            direction[0][i] = 1l << (BITS - i);
        }

        final Charset charset = Charset.forName(FILE_CHARSET);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        int dim = -1;

        try {
            // ignore first line
            reader.readLine();

            int lineNumber = 2;
            int index = 1;
            String line = null;
            while ( (line = reader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " ");
                try {
                    dim = Integer.parseInt(st.nextToken());
                    if (dim >= 2 && dim <= dimension) { // we have found the right dimension
                        final int s = Integer.parseInt(st.nextToken());
                        final int a = Integer.parseInt(st.nextToken());
                        final int[] m = new int[s + 1];
                        for (int i = 1; i <= s; i++) {
                            m[i] = Integer.parseInt(st.nextToken());
                        }
                        initDirectionVector(index++, a, m);
                    }

                    if (dim > dimension) {
                        return dim;
                    }
                } catch (NoSuchElementException e) {
                    throw new ArithmeticException("Resource error at line " + lineNumber + ":\n  " + line);
                } catch (NumberFormatException e) {
                    throw new ArithmeticException("Resource error at line " + lineNumber + ":\n  " + line);
                }
                lineNumber++;
            }
        } finally {
            reader.close();
        }

        return dim;
    }

    /**
     * Calculate the direction numbers from the given polynomial.
     *
     * @param d the dimension, zero-based
     * @param a the coefficients of the primitive polynomial
     * @param m the initial direction numbers
     */
    private void initDirectionVector(final int d, final int a, final int[] m) {
        final int s = m.length - 1;
        for (int i = 1; i <= s; i++) {
            direction[d][i] = ((long) m[i]) << (BITS - i);
        }
        for (int i = s + 1; i <= BITS; i++) {
            direction[d][i] = direction[d][i - s] ^ (direction[d][i - s] >> s);
            for (int k = 1; k <= s - 1; k++) {
                direction[d][i] ^= ((a >> (s - 1 - k)) & 1) * direction[d][i - k];
            }
        }
    }

    /** Generate a random vector.
     * @return a random vector as an array of double in the range [0.0, 1.0).
     */
    public double[] nextVector() {
        final double[] v = new double[dimension];
        if (count == 0) {
            count++;
            return v;
        }

        // find the index c of the rightmost 0
        int c = 1;
        int value = count - 1;
        while ((value & 1) == 1) {
            value >>= 1;
            c++;
        }

        for (int i = 0; i < dimension; i++) {
            x[i] ^= direction[i][c];
            v[i] = (double) x[i] / SCALE;
        }
        count++;
        return v;
    }

    /** Generate a random vector.
     * @param max the maximum exclusive value for the elements of the desired vector; minimum is 0 inclusive.
     * @return a random vector as an array of double.
     */
    public double[] nextVector(final double max) {
        final double[] v = new double[dimension];
        if (count == 0) {
            count++;
            return v;
        }

        // find the index c of the rightmost 0
        int c = 1;
        int value = count - 1;
        while ((value & 1) == 1) {
            value >>= 1;
            c++;
        }

        for (int i = 0; i < dimension; i++) {
            x[i] ^= direction[i][c];
            v[i] = (double) x[i] / SCALE * max;
        }
        count++;
        return v;
    }

    /** Generate a random vector.
     * @return a random vector as an array of long (only 52 bits are actually used for the result, plus sign bit).
     */
    public long[] nextLongVector() {
        final long[] v = new long[dimension];
        if (count == 0) {
            count++;
            return v;
        }

        // find the index c of the rightmost 0
        int c = 1;
        int value = count - 1;
        while ((value & 1) == 1) {
            value >>= 1;
            c++;
        }

        for (int i = 0; i < dimension; i++) {
            x[i] ^= direction[i][c];
            v[i] = x[i];
        }
        count++;
        return v;
    }
    /** Generate a random vector.
     * @param max the maximum exclusive value for the elements of the desired vector; minimum is 0 inclusive.
     * @return a random vector as an array of long (only 52 bits are actually used for the result, plus sign bit).
     */
    public long[] nextLongVector(final long max) {
        final long[] v = new long[dimension];

        if (count == 0) {
            count++;
            return v;
        }

        // find the index c of the rightmost 0
        int c = 1;
        int value = count - 1;
        while ((value & 1) == 1) {
            value >>= 1;
            c++;
        }

        for (int i = 0; i < dimension; i++) {
            x[i] ^= direction[i][c];
            //suboptimal, but this isn't meant for quality of randomness, actually the opposite.
            v[i] = (long)(x[i] / SCALE * max) % max;
        }
        count++;
        return v;
    }

    /** Generate a random vector.
     * @return a random vector as an array of int.
     */
    public int[] nextIntVector() {
        final int[] v = new int[dimension];
        if (count == 0) {
            count++;
            return v;
        }

        // find the index c of the rightmost 0
        int c = 1;
        int value = count - 1;
        while ((value & 1) == 1) {
            value >>= 1;
            c++;
        }

        for (int i = 0; i < dimension; i++) {
            x[i] ^= direction[i][c];
            v[i] = (int) (x[i] & 0x7fffffff);
        }
        count++;
        return v;
    }

    /** Generate a random vector.
     * @param max the maximum exclusive value for the elements of the desired vector; minimum is 0 inclusive.
     * @return a random vector as an array of int.
     */
    public int[] nextIntVector(final int max) {
        final int[] v = new int[dimension];
        if (count == 0) {
            count++;
            return v;
        }

        // find the index c of the rightmost 0
        int c = 1;
        int value = count - 1;
        while ((value & 1) == 1) {
            value >>= 1;
            c++;
        }

        for (int i = 0; i < dimension; i++) {
            x[i] ^= direction[i][c];
            //suboptimal, but this isn't meant for quality of randomness, actually the opposite.
            v[i] = (int)(x[i] / SCALE * max) % max;
        }
        count++;
        return v;
    }

    /**
     * Skip to the i-th point in the Sobol sequence.
     * <p>
     * This operation can be performed in O(1).
     * If index is somehow negative, this uses its absolute value instead of throwing an exception.
     * If index is 0, the result will always be entirely 0.
     * You should skipTo a number greater than 1000 if you want random-seeming individual numbers in each vector.
     *
     * @param index the index in the sequence to skip to
     * @return the i-th point in the Sobol sequence
     */
    public double[] skipTo(final int index) {
        if (index == 0) {
            // reset x vector
            Arrays.fill(x, 0);
        } else {
            final int i = (index > 0) ? (index - 1) : (-index - 1);
            final long grayCode = i ^ (i >> 1); // compute the gray code of i = i XOR floor(i / 2)
            for (int j = 0; j < dimension; j++) {
                long result = 0;
                for (int k = 1; k <= BITS; k++) {
                    final long shift = grayCode >> (k - 1);
                    if (shift == 0) {
                        // stop, as all remaining bits will be zero
                        break;
                    }
                    // the k-th bit of i
                    final long ik = shift & 1;
                    result ^= ik * direction[j][k];
                }
                x[j] = result;
            }
        }
        count = (index >= 0) ? index : -index;
        return nextVector();
    }

    /**
     * Returns the index i of the next point in the Sobol sequence that will be returned
     * by calling {@link #nextVector()}.
     *
     * @return the index of the next point
     */
    public int getNextIndex() {
        return count;
    }

    /**
     *
     * @param bits the number of bits to be returned
     * @return
     */
    @Override
    public int next(int bits) {
        return (int) (nextIntVector()[0] & (1L << bits) - 1);
    }

}