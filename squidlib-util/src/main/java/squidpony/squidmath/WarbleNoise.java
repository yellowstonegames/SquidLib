package squidpony.squidmath;

public class WarbleNoise implements Noise.Noise3D {

    public WarbleNoise(){
        this(0x1234567890ABCDEFL);
    }
    public WarbleNoise(long seed) {
        this.seed = seed;
        workSeed[0] = workSeed[3] = DiverRNG.determineDouble(seed) + 0.5;
        workSeed[1] = workSeed[4] = DiverRNG.determineDouble(seed + 1L) + 0.5;
        workSeed[2] = workSeed[5] = DiverRNG.determineDouble(seed + 2L) + 0.5;
    }
    protected long seed;
    protected final double[] results = new double[6];
    private final double[] working = new double[18];
    private final double[] workSeed = new double[12];

    @Override
    public double getNoise(double x, double y, double z) {
        working[0] = working[3] = working[6] = x;
        working[1] = working[4] = working[7] = y;
        working[2] = working[5] = working[8] = z;
        warble(3);
        warble(3);
        warble(3);
        for (int i = 0; i < 9; i++) {
            working[i] *= Math.PI;
        }
        for (int i = 0; i < 3; i++) {
            results[i] = sway(i, 0, 3.0);
        }
        return results[0];
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        workSeed[0] = workSeed[3] = DiverRNG.determineDouble(seed) + 0.5;
        workSeed[1] = workSeed[4] = DiverRNG.determineDouble(seed + 1L) + 0.5;
        workSeed[2] = workSeed[5] = DiverRNG.determineDouble(seed + 2L) + 0.5;
        return getNoise(x, y, z);
    }
    private double sway(int element, int workingOffset, double seedChange) {
        return NumberTools.sin(workSeed[element] + seedChange + working[element + 2 + workingOffset]
                - NumberTools.cos(workSeed[element + 2] + seedChange + working[element + 1 + workingOffset])
                + NumberTools.cos(workSeed[element + 1] + seedChange + working[element + workingOffset]));
    }
    private void warble(final int size){
        for (int i = 0; i < size; i++) {
            results[i] = working[i] + sway(i, 1, 0.0);
        }
        System.arraycopy(results, 0, working, 0, size);
        System.arraycopy(results, 0, working, size, size);
        System.arraycopy(results, 0, working, size + size, size);
        for (int i = 0; i < size; i++) {
            results[i] += sway(i, 2, 1.0);
        }
        System.arraycopy(results, 0, working, 0, size);
        System.arraycopy(results, 0, working, size, size);
        System.arraycopy(results, 0, working, size + size, size);
        for (int i = 0; i < size; i++) {
            results[i] = (results[i] + sway(i, 0, 2.0)) * 0.25;
        }
        System.arraycopy(results, 0, working, 0, size);
        System.arraycopy(results, 0, working, size, size);
        System.arraycopy(results, 0, working, size + size, size);
    }
    /*
const float SEED = 42.0;
const vec3 COEFFS = fract((SEED + 23.4567) * vec3(0.8191725133961645, 0.6710436067037893, 0.5497004779019703)) + 0.5;

vec3 swayRandomized(vec3 seed, vec3 value)
{
    return sin(seed.xyz + value.zxy - cos(seed.zxy + value.yzx) + cos(seed.yzx + value.xyz));
}

vec3 cosmic(vec3 c, vec3 con)
{
    con += swayRandomized(c, con.yzx);
    con += swayRandomized(c + 1.0, con.xyz);
    con += swayRandomized(c + 2.0, con.zxy);
    return con * 0.25;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy * 64.0 + swayRandomized(COEFFS.zxy, (iTime * 0.1875) * COEFFS.yzx).xy * 32.0;
    // aTime, s, and c could be uniforms in some engines.
    float aTime = iTime * 0.0625;
    vec3 adj = vec3(-1.11, 1.41, 1.61);
    vec3 s = (swayRandomized(vec3(34.0, 76.0, 59.0), aTime + adj)) * 0.25;
    vec3 c = (swayRandomized(vec3(27.0, 67.0, 45.0), aTime - adj)) * 0.25;
    vec3 con = vec3(0.0004375, 0.0005625, 0.0008125) * aTime + c * uv.x + s * uv.y;

    con = cosmic(COEFFS, con);
    con = cosmic(COEFFS, con);
    con = cosmic(COEFFS, con);

    fragColor = vec4(swayRandomized(COEFFS + 3.0, con * (3.14159265)) * 0.5 + 0.5,1.0);
}
     */
}
