package squidpony;

import squidpony.squidmath.RNG;

import java.util.HashMap;
import java.util.Map;

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
	 *            The red component. For screen colors, in-between 0 (inclusive)
     *            and 256 (exclusive).
	 * @param green
	 *            The green component. For screen colors, in-between 0 (inclusive)
     *            and 256 (exclusive).
	 * @param blue
	 *            The blue component. For screen colors, in-between 0 (inclusive)
     *            and 256 (exclusive).
	 * @param opacity
	 *            The alpha component. In-between 0 (inclusive) and 256
	 *            (exclusive). Larger values mean more opacity; 0 is clear.
	 * @return A possibly transparent color.
	 */
	T get(int red, int green, int blue, int opacity);

	/**
	 * @param red
	 *            The red component. For screen colors, in-between 0 (inclusive)
     *            and 256 (exclusive).
	 * @param green
	 *            The green component. For screen colors, in-between 0 (inclusive)
     *            and 256 (exclusive).
	 * @param blue
	 *            The blue component. For screen colors, in-between 0 (inclusive)
     *            and 256 (exclusive).
	 * @return An opaque color.
	 */
	T get(int red, int green, int blue);

    /**
     *
     * @param hue The hue of the desired color from 0.0 (red, inclusive) towards orange, then
     *            yellow, and eventually to purple before looping back to almost the same red
     *            (1.0, exclusive)
     * @param saturation the saturation of the color from 0.0 (a grayscale color; inclusive)
     *                   to 1.0 (a bright color, exclusive)
     * @param value the value (essentially lightness) of the color from 0.0 (black,
     *                   inclusive) to 1.0 (inclusive) for screen colors or arbitrarily high
     *                   for HDR colors.
     * @param opacity the alpha component as a float; 0.0f is clear, 1.0f is opaque.
     * @return a possibly transparent color
     */
    T getHSV(float hue, float saturation, float value, float opacity);

    /**
     *
     * @param hue The hue of the desired color from 0.0 (red, inclusive) towards orange, then
     *            yellow, and eventually to purple before looping back to almost the same red
     *            (1.0, exclusive)
     * @param saturation the saturation of the color from 0.0 (a grayscale color; inclusive)
     *                   to 1.0 (a bright color, exclusive)
     * @param value the value (essentially lightness) of the color from 0.0 (black,
     *                   inclusive) to 1.0 (inclusive) for screen colors or arbitrarily high
     *                   for HDR colors.
     * @return an opaque color
     */
    T getHSV(float hue, float saturation, float value);

	/**
	 * @return Opaque white.
	 */
	T getWhite();

	/**
	 * @return Opaque black.
	 */
	T getBlack();

	/**
	 * @return The fully transparent color.
	 */
	T getTransparent();

	/**
	 * @param rng an RNG from SquidLib.
	 * @param opacity
	 *            The alpha component. In-between 0 (inclusive) and 256
	 *            (exclusive). Larger values mean more opacity; 0 is clear.
	 * @return A random color, except for the alpha component.
	 */
	T getRandom(RNG rng, int opacity);

	/**
	 * @param c a concrete color
	 * @return The red component. For screen colors, in-between 0 (inclusive) and 256 (exclusive).
	 */
	int getRed(T c);

	/**
	 * @param c a concrete color
	 * @return The green component. For screen colors, in-between 0 (inclusive) and 256
	 *         (exclusive).
	 */
	int getGreen(T c);

	/**
	 * @param c a concrete color
	 * @return The blue component. For screen colors, in-between 0 (inclusive) and 256 (exclusive).
	 */
	int getBlue(T c);

	/**
	 * @param c a concrete color
	 * @return The alpha component. In-between 0 (inclusive) and 256
	 *         (exclusive).
	 */
	int getAlpha(T c);

    /**
     *
     * @param c a concrete color
     * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
     * eventually to purple before looping back to almost the same red (1.0, exclusive)
     */
    float getHue(T c);

    /**
     *
     * @param c a concrete color
     * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a
     * bright color, exclusive)
     */
    float getSaturation(T c);

    /**
     *
     * @param c a concrete color
     * @return the value (essentially lightness) of the color from 0.0 (black, inclusive) to
     * 1.0 (inclusive) for screen colors or arbitrarily high for HDR colors.
     */
    float getValue(T c);

	/**
	 * A skeletal implementation of {@link IColorCenter}.
	 * 
	 * @author smelC
	 * 
	 * @param <T> a concrete color type
	 */
	abstract class Skeleton<T> implements IColorCenter<T> {

		private final Map<Long, T> cache = new HashMap<Long, T>(256);

		protected Skeleton() {
			/* Nothing to do */
		}

		@Override
		public T get(int red, int green, int blue, int opacity) {
			final Long value = getUniqueIdentifier((short)red, (short)green, (short)blue, (short)opacity);
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
        public T getHSV(float hue, float saturation, float value, float opacity) {
            if ( saturation < 0.0001 )                       //HSV from 0 to 1
            {
                return get(Math.round(value * 255), Math.round(value * 255), Math.round(value * 255),
                        Math.round(opacity * 255));
            }
            else
            {
                float h = hue * 6f;
                if ( h >= 6 ) h = 0;      //H must be < 1
                int i = (int)h;             //Or ... var_i = floor( var_h )
                float a = value * ( 1 - saturation );
                float b = value * ( 1 - saturation * ( h - i ) );
                float c = value * ( 1 - saturation * ( 1 - ( h - i ) ) );

                switch (i)
                {
                    case 0: return get(Math.round(value * 255), Math.round(c * 255), Math.round(a * 255),
                            Math.round(opacity * 255));
                    case 1: return get(Math.round(b * 255), Math.round(value * 255), Math.round(a * 255),
                            Math.round(opacity * 255));
                    case 2: return get(Math.round(a * 255), Math.round(value * 255), Math.round(c * 255),
                            Math.round(opacity * 255));
                    case 3: return get(Math.round(a * 255), Math.round(b * 255), Math.round(value * 255),
                            Math.round(opacity * 255));
                    case 4: return get(Math.round(c * 255), Math.round(a * 255), Math.round(value * 255),
                            Math.round(opacity * 255));
                    default: return get(Math.round(value * 255), Math.round(a * 255), Math.round(b * 255),
                            Math.round(opacity * 255));
                }
            }
        }

        @Override
        public T getHSV(float hue, float saturation, float value) {
            return getHSV(hue, saturation, value, 1.0f);
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
         * @param c a concrete color
         * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a
         * bright color, exclusive)
         */
        @Override
        public float getSaturation(T c) {
            float r = getRed(c) / 255f;                     //RGB from 0 to 255
            float g = getGreen(c) / 255f;
            float b = getBlue(c) / 255f;

            float min = Math.min(Math.min(r, g ), b);    //Min. value of RGB
            float max = Math.max(Math.max(r, g), b);    //Min. value of RGB
            float delta = max - min;                     //Delta RGB value

            float saturation;

            if ( delta < 0.0001f )                     //This is a gray, no chroma...
            {
                saturation = 0;
            }
            else                                    //Chromatic data...
            {
                saturation = delta / max;
            }
            return saturation;
        }

        /**
         * @param c a concrete color
         * @return the value (essentially lightness) of the color from 0.0 (black, inclusive) to
         * 1.0 (inclusive) for screen colors or arbitrarily high for HDR colors.
         */
        @Override
        public float getValue(T c) {
            float r = getRed(c) / 255f;                     //RGB from 0 to 255
            float g = getGreen(c) / 255f;
            float b = getBlue(c) / 255f;

            float max = Math.max(Math.max(r, g), b);    //Min. value of RGB

            return max;
        }

        /**
         * @param c a concrete color
         * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
         * eventually to purple before looping back to almost the same red (1.0, exclusive)
         */
        @Override
        public float getHue(T c) {
            float r = getRed(c) / 255f;                     //RGB from 0 to 255
            float g = getGreen(c) / 255f;
            float b = getBlue(c) / 255f;

            float min = Math.min(Math.min(r, g ), b);    //Min. value of RGB
            float max = Math.max(Math.max(r, g), b);    //Min. value of RGB
            float delta = max - min;                     //Delta RGB value

            float hue;

            if ( delta < 0.0001f )                     //This is a gray, no chroma...
            {
                hue = 0;                                //HSV results from 0 to 1
            }
            else                                    //Chromatic data...
            {
                float rDelta = ( ( ( max - r ) / 6f ) + ( delta / 2f ) ) / delta;
                float gDelta = ( ( ( max - g ) / 6f ) + ( delta / 2f ) ) / delta;
                float bDelta = ( ( ( max - b ) / 6f ) + ( delta / 2f ) ) / delta;

                if       ( r == max ) hue = bDelta - gDelta;
                else if ( g == max ) hue = ( 1f / 3f ) + rDelta - bDelta;
                else                 hue = ( 2f / 3f ) + gDelta - rDelta;

                if ( hue < 0 ) hue += 1f;
                else if ( hue > 1 ) hue -= 1;
            }
            return hue;
        }

        /**
		 * Create a concrete instance of the color type given as a type parameter.
		 * @param red the red component of the desired color
		 * @param green the green component of the desired color
		 * @param blue the blue component of the desired color
		 * @param opacity the alpha component or opacity of the desired color
		 * @return a fresh instance of the concrete color type
		 */
		protected abstract T create(int red, int green, int blue, int opacity);

		private long getUniqueIdentifier(short r, short g, short b, short a) {
			return ((a & 0xffL) << 48) | ((r & 0xffffL) << 32) | ((g & 0xffffL) << 16) | (b & 0xffffL);
		}

	}
}
