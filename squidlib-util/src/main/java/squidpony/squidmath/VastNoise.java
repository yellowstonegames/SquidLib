package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * An experimental way of speeding up {@link FastNoise} by using a large buffer of random bytes, and choosing a byte
 * from the buffer purely by the current seed and position (a simpler calculation than a hash). This currently gives a
 * modest speed boost to {@link #SIMPLEX}, {@link #PERLIN}, and {@link #HONEY}, but does not improve the performance of
 * {@link #FOAM} or {@link #VALUE}. It should be interchangeable with FastNoise, but be aware that the first time this
 * class is imported, it will generate about a million random bytes using {@link TangleRNG}. Generating a million bytes
 * really doesn't take more time than the blink of an eye, but it does use 1MB of RAM for the class (not allocated per
 * object, at least).
 * <br>
 * Created by Tommy Ettinger on 9/12/2020.
 */
@Beta
public class VastNoise extends FastNoise {
	/**
	 * A constructor that takes no parameters, and uses all default settings with a seed of 1337. An example call to
	 * this would be {@code new VastNoise()}, which makes noise with the seed 1337, a default frequency of 1.0f/32.0f, 1
	 * octave of Simplex noise (since this doesn't specify octave count, it always uses 1 even for the
	 * SIMPLEX_FRACTAL noiseType this uses, but you can call {@link #setFractalOctaves(int)} later to benefit from the
	 * fractal noiseType), and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
	 */
	public VastNoise() {
		super();
	}

	/**
	 * A constructor that takes only a parameter for the VastNoise's seed, which should produce different results for
	 * any different seeds. An example call to this would be {@code new VastNoise(1337)}, which makes noise with the
	 * seed 1337, a default frequency of 1.0f/32.0f, 1 octave of Simplex noise (since this doesn't specify octave count,
	 * it always uses 1 even for the SIMPLEX_FRACTAL noiseType this uses, but you can call
	 * {@link #setFractalOctaves(int)} later to benefit from the fractal noiseType), and normal lacunarity and gain
	 * (when unspecified, they are 2f and 0.5f).
	 *
	 * @param seed the int seed for the noise, which should significantly affect the produced noise
	 */
	public VastNoise(int seed) {
		super(seed);
	}

	/**
	 * A constructor that takes two parameters to specify the VastNoise from the start. An example call to this
	 * would be {@code new VastNoise(1337, 0.02f)}, which makes noise with the seed 1337, a lower
	 * frequency, 1 octave of Simplex noise (since this doesn't specify octave count, it always uses 1 even for the
	 * SIMPLEX_FRACTAL noiseType this uses, but you can call {@link #setFractalOctaves(int)} later to benefit from the
	 * fractal noiseType), and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
	 *
	 * @param seed      the int seed for the noise, which should significantly affect the produced noise
	 * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
	 */
	public VastNoise(int seed, float frequency) {
		super(seed, frequency);
	}

	/**
	 * A constructor that takes a few parameters to specify the VastNoise from the start. An example call to this
	 * would be {@code new VastNoise(1337, 0.02f, VastNoise.SIMPLEX)}, which makes noise with the seed 1337, a lower
	 * frequency, 1 octave of Simplex noise (since this doesn't specify octave count, it always uses 1 even for
	 * noiseTypes like SIMPLEX_FRACTAL, but using a fractal noiseType can make sense if you call
	 * {@link #setFractalOctaves(int)} later), and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
	 *
	 * @param seed      the int seed for the noise, which should significantly affect the produced noise
	 * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
	 * @param noiseType the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
	 */
	public VastNoise(int seed, float frequency, int noiseType) {
		super(seed, frequency, noiseType);
	}

	/**
	 * A constructor that takes several parameters to specify the VastNoise from the start. An example call to this
	 * would be {@code new VastNoise(1337, 0.02f, VastNoise.SIMPLEX_FRACTAL, 4)}, which makes noise with the seed 1337, a lower
	 * frequency, 4 octaves of Simplex noise, and normal lacunarity and gain (when unspecified, they are 2f and 0.5f).
	 *
	 * @param seed      the int seed for the noise, which should significantly affect the produced noise
	 * @param frequency the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
	 * @param noiseType the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
	 * @param octaves   how many octaves of noise to use when the noiseType is one of the _FRACTAL types
	 */
	public VastNoise(int seed, float frequency, int noiseType, int octaves) {
		super(seed, frequency, noiseType, octaves);
	}

	/**
	 * A constructor that takes a lot of parameters to specify the VastNoise from the start. An example call to this
	 * would be {@code new VastNoise(1337, 0.02f, VastNoise.SIMPLEX_FRACTAL, 4, 0.5f, 2f)}, which makes noise with a
	 * lower frequency, 4 octaves of Simplex noise, and the "inverse" effect on how those octaves work (which makes
	 * the extra added octaves be more significant to the final result and also have a lower frequency, while normally
	 * added octaves have a higher frequency and tend to have a minor effect on the large-scale shape of the noise).
	 *
	 * @param seed       the int seed for the noise, which should significantly affect the produced noise
	 * @param frequency  the multiplier for all dimensions, which is usually fairly small (1.0f/32.0f is the default)
	 * @param noiseType  the noiseType, which should be a constant from this class (see {@link #setNoiseType(int)})
	 * @param octaves    how many octaves of noise to use when the noiseType is one of the _FRACTAL types
	 * @param lacunarity typically 2.0, or 0.5 to change how extra octaves work (inverse mode)
	 * @param gain       typically 0.5, or 2.0 to change how extra octaves work (inverse mode)
	 */
	public VastNoise(int seed, float frequency, int noiseType, int octaves, float lacunarity, float gain) {
		super(seed, frequency, noiseType, octaves, lacunarity, gain);
	}

	/**
	 * Copy constructor; copies all non-temporary fields from  {@code other} into this. This uses the same reference to
	 * an {@link IPointHash} set with {@link #setPointHash(IPointHash)} and to another VastNoise set with
	 * {@link #setCellularNoiseLookup(FastNoise)}, but otherwise everything it copies is a primitive.
	 *
	 * @param other another FastNoise or VastNoise, which must not be null
	 */
	public VastNoise(FastNoise other) {
		super(other);
	}
	
	private static final byte[] BUFFER = new byte[0x100000];
	static {
		new RNG(new TangleRNG(0x01234567L, 0x89ABCDEFL)).nextBytes(BUFFER);
	}

	@Override
	protected int hash32(int x, int y, int z, int s) {
		return BUFFER[s + (x * 17) + (y * 257) + (z * 4097) & 0xFFFFF] & 31;
	}

	@Override
	protected int hash256(int x, int y, int s) {
		return BUFFER[s + (x * 65) + (y * 4097) & 0xFFFFF] & 255;
	}

	@Override
	protected int hash256(int x, int y, int z, int s) {
		return BUFFER[s + (x * 17) + (y * 257) + (z * 4097) & 0xFFFFF] & 255;
	}

	@Override
	protected int hash256(int x, int y, int z, int w, int s) {
		return BUFFER[s + (x * 17) + (y * 257) + (z * 4097) + (w * 65537) & 0xFFFFF] & 255;
	}

	@Override
	protected int hash256(int x, int y, int z, int w, int u, int s) {
		return BUFFER[s + (x * 9) + (y * 65) + (z * 513) + (w * 4097) + (u * 32769) & 0xFFFFF] & 255;
	}

	@Override
	protected int hash256(int x, int y, int z, int w, int u, int v, int s) {
		return BUFFER[s + (x * 5) + (y * 17) + (z * 65) + (w * 257) + (u * 1025) + (u * 4097) & 0xFFFFF] & 255;
	}
}
