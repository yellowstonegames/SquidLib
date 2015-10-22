package squidpony;

import java.util.HashMap;
import java.util.Map;

import squidpony.squidmath.RNG;

/**
 * How to manage colors, making sure that a color is allocated at most once.
 * 
 * <p>
 * If you aren't using squidlib's gdx part, you should use this interface (and
 * the {@link Skeleton} implementation), because it caches instances.
 * </p>
 * 
 * <p>
 * If you are using squidlib's gdx part, you should use this interface (and the
 * {@code SquidColorCenter} implementation) if:
 * 
 * <ul>
 * <li>You don't want to use preallocated instances (if you do, check out
 * {@code squidpony.squidgrid.gui.Colors})</li>
 * <li>You don't want to use named colors (if you do, check out
 * {@code com.badlogic.gdx.graphics.Colors})</li>
 * <li>You don't like libgdx's Color representation (components as floats
 * in-between 0 and 1) but prefer components within 0 (inclusive) and 256
 * (exclusive); and don't mind the overhead of switching the representations. My
 * personal opinion is that the overhead doesn't matter w.r.t other intensive
 * operations that we have in roguelikes (path finding).</li>
 * </ul>
 * 
 * @author smelC
 * 
 * @param <T>
 *            The concrete type of colors
 */
public interface IColorCenter<T> {

	/**
	 * @param red
	 *            The red component. In-between 0 (inclusive) and 256
	 *            (exclusive).
	 * @param green
	 *            The green component. In-between 0 (inclusive) and 256
	 *            (exclusive).
	 * @param blue
	 *            The blue component. In-between 0 (inclusive) and 256
	 *            (exclusive).
	 * @param opacity
	 *            The alpha component. In-between 0 (inclusive) and 256
	 *            (exclusive). Larger values mean more opacity.
	 * @return A possibly transparent color.
	 */
	public T get(int red, int green, int blue, int opacity);

	/**
	 * @param red
	 *            The red component. In-between 0 (inclusive) and 256
	 *            (exclusive).
	 * @param green
	 *            The green component. In-between 0 (inclusive) and 256
	 *            (exclusive).
	 * @param blue
	 *            The blue component. In-between 0 (inclusive) and 256
	 *            (exclusive).
	 * @return An opaque color.
	 */
	public T get(int red, int green, int blue);

	/**
	 * @return Opaque white.
	 */
	public T getWhite();

	/**
	 * @return Opaque black.
	 */
	public T getBlack();

	/**
	 * @return The fully transparent color.
	 */
	public T getTransparent();

	/**
	 * @param rng
	 * @param opacity
	 *            The alpha component. In-between 0 (inclusive) and 256
	 *            (exclusive). Larger values mean more opacity.
	 * @return A random color, except for the alpha component.
	 */
	public T getRandom(RNG rng, int opacity);

	/**
	 * @param c
	 * @return The red component. In-between 0 (inclusive) and 256 (exclusive).
	 */
	public int getRed(T c);

	/**
	 * @param c
	 * @return The green component. In-between 0 (inclusive) and 256
	 *         (exclusive).
	 */
	public int getGreen(T c);

	/**
	 * @param c
	 * @return The blue component. In-between 0 (inclusive) and 256 (exclusive).
	 */
	public int getBlue(T c);

	/**
	 * @param c
	 * @return The alpha component. In-between 0 (inclusive) and 256
	 *         (exclusive).
	 */
	public int getAlpha(T c);

	/**
	 * A skeletal implementation of {@link IColorCenter}.
	 * 
	 * @author smelC
	 * 
	 * @param <T>
	 */
	public static abstract class Skeleton<T> implements IColorCenter<T> {

		private final Map<Integer, T> cache = new HashMap<Integer, T>(256);

		protected Skeleton() {
			/* Nothing to do */
		}

		@Override
		public T get(int red, int green, int blue, int opacity) {
			final Integer value = getUniqueIdentifier(red, green, blue, opacity);
			T t = cache.get(value);
			if (t == null) {
				/* Miss */
				t = create(red, green, blue, opacity);
				/* Put in cache */
				cache.put(value, t);
			}
			return t;
		}

		@Override
		public T get(int red, int green, int blue) {
			return get(red, green, blue, 255);
		}

		@Override
		public T getWhite() {
			return get(255, 255, 255, 255);
		}

		@Override
		public T getBlack() {
			return get(0, 0, 0, 255);
		}

		@Override
		public T getTransparent() {
			return get(0, 0, 0, 0);
		}

		@Override
		public T getRandom(RNG rng, int opacity) {
			return get(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), opacity);
		}

		/**
		 * @return A fresh instance.
		 */
		protected abstract T create(int red, int green, int blue, int opacity);

		private int getUniqueIdentifier(int r, int g, int b, int a) {
			return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}

	}
}
