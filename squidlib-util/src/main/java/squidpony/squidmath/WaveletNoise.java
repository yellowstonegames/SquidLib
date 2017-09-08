/**
 * Copyright 2014-2017 Steven T Sell (ssell@vertexfragment.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package squidpony.squidmath;

/**
 * Wavelet Noise, a kind of multi-dimensional noise that is technically unrelated to classic Perlin or Simplex Noise,
 * developed by Pixar to solve some difficult issues of 2D textures being displayed in 3D scenes. A good source for the
 * benefits this has is <a href="http://graphics.pixar.com/library/WaveletNoise/paper.pdf">The original Pixar paper</a>.
 * This is in SquidLib to experiment with using it for higher-dimensional noise. It is unusual in that it generates a
 * large number of pseudo-random floats when a WaveletNoise object is constructed, and uses the same block of random
 * numbers (the default is 48 * 48 * 48 numbers in a cube) in different ways as different areas are sampled. It has
 * somewhat-noticeable axis-aligned bias, and isn't as fast as something like SeededNoise or WhirlingNoise. These flaws
 * may be corrected at some point. It currently doesn't implement Noise2D or Noise3D, but it should be able to soon.
 * <br>
 * This code is originally from the Apache-licensed OcularEngine project, fixing small issues (float distribution from
 * the RNG): https://github.com/ssell/OcularEngine/blob/master/OcularCore/src/Math/Noise/WaveletNoise.cpp .
 * <br>
 * Created by Tommy Ettinger on 9/8/2017.
 */
public class WaveletNoise {
    public int seed = 0x1337BEEF;
    public static final WaveletNoise instance = new WaveletNoise(48);

    private static int mod(final int x, final int n) {
        final int m = x % n;
        return (m < 0) ? m + n : m;
    }

    private final int m_Dimensions;
    private final int m_NoiseSize;
    private final float[] m_Noise;
    public float m_Scale;

    public WaveletNoise()
    {
        this(48);
    }

    public WaveletNoise(final int dimensions) // may fix this at 64; it's the length/height/width of all axes
    {
        m_Dimensions = dimensions + (dimensions & 1); // make m_Dimensions an even number, increasing if needed

        m_NoiseSize = (m_Dimensions * m_Dimensions * m_Dimensions);
        m_Noise = new float[m_NoiseSize];
        m_Scale = 1.0f;

        generate();
    }

    //------------------------------------------------------------------------------
    // PUBLIC METHODS
    //------------------------------------------------------------------------------

    public float getValue(final float x) {
        return getValue(x, 0.0f, 0.0f);
    }

    public float getValue(final float x, final float y) {
        return getValue(x, y, 0.0f);
    }

    public float getValue(final float x, final float y, final float z) {
        return getRawNoise(x * m_Scale, y * m_Scale, z * m_Scale);
    }

    public void setScale(final float scale) {
        m_Scale = scale;
    }

    //------------------------------------------------------------------------------
    // PROTECTED METHODS
    //------------------------------------------------------------------------------

    public float getRawNoise(final float p0, final float p1, final float p2) {
        int n = m_Dimensions;
        int[] f = {0, 0, 0};
        int[] c = {0, 0, 0};
        int[] mid = {0, 0, 0};

        float[][] w = new float[3][3];
        float t;
        float result = 0.0f;

        //---------------------------------------------------
        // Evaluate quadratic B-spline basis functions
        mid[0] = Math.round(p0);
        t = mid[0] - (p0 - 0.5f);
        w[0][0] = t * t * 0.5f;
        w[0][2] = (1.0f - t) * (1.0f - t) * 0.5f;
        w[0][1] = 1.0f - w[0][0] - w[0][2];
        mid[1] = Math.round(p1);
        t = mid[1] - (p1 - 0.5f);
        w[1][0] = t * t * 0.5f;
        w[1][2] = (1.0f - t) * (1.0f - t) * 0.5f;
        w[1][1] = 1.0f - w[1][0] - w[1][2];
        mid[2] = Math.round(p2);
        t = mid[2] - (p2 - 0.5f);
        w[2][0] = t * t * 0.5f;
        w[2][2] = (1.0f - t) * (1.0f - t) * 0.5f;
        w[2][1] = 1.0f - w[2][0] - w[2][2];

        float weight;

        for (f[2] = -1; f[2] <= 1; f[2]++) {
            for (f[1] = -1; f[1] <= 1; f[1]++) {
                for (f[0] = -1; f[0] <= 1; f[0]++) {
                    weight = 1.0f;

                    for (int i = 0; i < 3; i++) {
                        c[i] = mod((mid[i] + f[i]), n);
                        weight *= w[i][f[i] + 1];
                    }

                    result += weight * m_Noise[(c[2] * n * n) + (c[1] * n) + c[0]];
                }
            }
        }

        return result;
    }

    public void generate() {
        int x = 0;
        int y = 0;
        int z = 0;
        int i = 0;

        float[] temp1 = new float[m_NoiseSize];
        float[] temp2 = new float[m_NoiseSize];
        float[] noise = new float[m_NoiseSize];

        //---------------------------------------------------
        // Step 1: Fill the tile with random numbers on range [0.0, 1.0]


        for (i = 0; i < m_NoiseSize; i++) {
            noise[i] = NumberTools.formCurvedFloat(ThrustRNG.determine(seed + i));
        }

        //---------------------------------------------------
        // Step 2 & 3: Downsample and then Upsample

        for (y = 0; y < m_Dimensions; y++) {
            for (z = 0; z < m_Dimensions; z++) {
                i = (y * m_Dimensions) + (z * m_Dimensions * m_Dimensions);

                downsample(noise, temp1, i, m_Dimensions, 1);
                upsample(temp1, temp2, i, m_Dimensions, 1);
            }
        }

        for (x = 0; x < m_Dimensions; x++) {
            for (z = 0; z < m_Dimensions; z++) {
                i = x + (z * m_Dimensions * m_Dimensions);

                downsample(temp2, temp1, i, m_Dimensions, m_Dimensions);
                upsample(temp1, temp2, i, m_Dimensions, m_Dimensions);
            }
        }

        for (x = 0; x < m_Dimensions; x++) {
            for (y = 0; y < m_Dimensions; y++) {
                i = x + (y * m_Dimensions);

                downsample(temp2, temp1, i, m_Dimensions, (m_Dimensions * m_Dimensions));
                upsample(temp1, temp2, i, m_Dimensions, (m_Dimensions * m_Dimensions));
            }
        }

        //---------------------------------------------------
        // Step 4: Substract out the coarse-scale constribution (original - (downsample upsample))

        for (i = 0; i < m_NoiseSize; i++) {
            noise[i] -= temp2[i];
        }

        //---------------------------------------------------
        // Step 5: Avoid even/odd variance difference by adding odd-offset version of noise to itself

        int offset = m_Dimensions >> 1 | 1;

        for (i = 0, x = 0; x < m_Dimensions; x++) {
            for (y = 0; y < m_Dimensions; y++) {
                for (z = 0; z < m_Dimensions; z++) {
                    temp1[i++] = noise[mod((x + offset), m_Dimensions) + (mod((y + offset), m_Dimensions) * m_Dimensions) + (mod((z + offset), m_Dimensions) * m_Dimensions * m_Dimensions)];
                }
            }
        }

        for (i = 0; i < m_NoiseSize; i++) {
            m_Noise[i] = (noise[i] + temp1[i]);// + 1.3125f);
        }
    }

    public void downsample(float[] from, float[] to, int idx, int n, int stride) {
        float coefficients[] =
                {
                        0.000334f, -0.001528f, 0.000410f, 0.003545f, -0.000938f, -0.008233f, 0.002172f, 0.019120f,
                        -0.005040f, -0.044412f, 0.011655f, 0.103311f, -0.025936f, -0.243780f, 0.033979f, 0.655340f,
                        0.655340f, 0.033979f, -0.243780f, -0.025936f, 0.103311f, 0.011655f, -0.044412f, -0.005040f,
                        0.019120f, 0.002172f, -0.008233f, -0.000938f, 0.003546f, 0.000410f, -0.001528f, 0.000334f
                };

        int tindex;
        int findex;
        int cindex;

        for (int i = 0; i < (n >> 1); i++) {
            to[i * stride+idx] = 0.0f;

            for (int j = ((2 * i) - 16); j < ((2 * i) + 16); j++) {
                cindex = 16 + (j - 2 * i);
                tindex = i * stride + idx;
                findex = mod(j, n) * stride + idx;

                to[tindex] += coefficients[cindex] * from[findex];
            }
        }
    }


    public void upsample(float[] from, float[] to, int idx, int n, int stride) {
        float coefficients[] =
                {
                        0.25f, 0.75f, 0.75f, 0.25f
                };

        //int cindex;
        //int tindex;
        //int findex;

        for (int i = 0; i < n; i++) {
            to[i * stride + idx] = coefficients[2 + (i & 1)] * from[mod((i >> 1), (n >> 1)) * stride + idx]
            + coefficients[2 + (i - 2 * ((i >> 1) + 1))] * from[mod((i >> 1) + 1, (n >> 1)) * stride + idx];
            /*
            for (int j = (i >> 1); j <= ((i >> 1) + 1); j++) {
                cindex = 2 + (i - (2 * j));
                findex = mod(j, (n >> 1)) * stride + idx;

                to[tindex] += coefficients[cindex] * from[findex];
            }
            */
        }
    }

    /*
    void Downsample (float[] from, float[] to, int idx, int n, int stride ) {

        float[] aCoeffs =
                {
                        0.000334f, -0.001528f, 0.000410f, 0.003545f, -0.000938f, -0.008233f, 0.002172f, 0.019120f,
                        -0.005040f, -0.044412f, 0.011655f, 0.103311f, -0.025936f, -0.243780f, 0.033979f, 0.655340f,
                        0.655340f, 0.033979f, -0.243780f, -0.025936f, 0.103311f, 0.011655f, -0.044412f, -0.005040f,
                        0.019120f, 0.002172f, -0.008233f, -0.000938f, 0.003546f, 0.000410f, -0.001528f, 0.000334f
                };
        for (int i=0; i<n>>1; i++) {
            to[i*stride+idx] = 0;
            for (int k=2*i-16; k<=2*i+16; k++)
                to[i*stride+idx] += aCoeffs[k-2*i+16] * from[mod(k,n)*stride+idx];
        }
    }

    void Upsample( float *from, float *to, int n, int stride) {
        float *p, pCoeffs[4] = { 0.25, 0.75, 0.75, 0.25 };
        p = &pCoeffs[2];
        for (int i=0; i<n; i++) {
            to[i*stride] = 0;
            for (int k=i/2; k<=i/2+1; k++)
                to[i*stride] += p[i-2*k] * from[Mod(k,n/2)*stride];
        }
    }
    void GenerateNoiseTile( int n, int olap) {
        n += n & 1;
        int ix, iy, iz, i, sz=n*n*n*sizeof(float);
        float *temp1=(float *)malloc(sz),*temp2=(float *)malloc(sz),*noise=(float *)malloc(sz);
        // Step 1. Fill the tile with random numbers in the range -1 to 1.
        for (i=0; i<n*n*n; i++) noise[i] = gaussianNoise();
        // Steps 2 and 3. Downsample and upsample the tile
        for (iy=0; iy<n; iy++) for (iz=0; iz<n; iz++) { // each x row
            i = iy*n + iz*n*n; Downsample( &noise[i], &temp1[i], n, 1 );
            Upsample( &temp1[i], &temp2[i], n, 1 );
        }
        for (ix=0; ix<n; ix++) for (iz=0; iz<n; iz++) { // each y row
            i = ix + iz*n*n; Downsample( &temp2[i], &temp1[i], n, n );
            Upsample( &temp1[i], &temp2[i], n, n );
        }
        for (ix=0; ix<n; ix++) for (iy=0; iy<n; iy++) { // each z row
            i = ix + iy*n; Downsample( &temp2[i], &temp1[i], n, n*n );
            Upsample( &temp1[i], &temp2[i], n, n*n );
        }
// Step 4. Subtract out the coarse-scale contribution
        for (i=0; i<n*n*n; i++) {noise[i]-=temp2[i];}
// Avoid even/odd variance difference by adding odd-offset version of noise to itself
        int offset=n/2; if (offset%2==0) offset++;
        for (i=0,ix=0; ix<n; ix++) for (iy=0; iy<n; iy++) for (iz=0; iz<n; iz++)
            temp1[i++] = noise[ Mod(ix+offset,n) + Mod(iy+offset,n)*n + Mod(iz+offset,n)*n*n ];
        for (i=0; i<n*n*n; i++) {noise[i]+=temp1[i];}
        noiseTileData=noise; noiseTileSize=n; free(temp1); free(temp2);
    }
    */
}
