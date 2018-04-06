package squidpony;

import squidpony.panel.IColoredString;
import squidpony.squidmath.IRNG;

import java.util.ArrayList;
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
     *            (1.0, exclusive). Values outside this range should be treated as wrapping
     *            around, so 1.1f and -0.9f would be the same as 0.1f .
     * @param saturation the saturation of the color from 0.0 (a grayscale color; inclusive)
     *                   to 1.0 (a bright color, inclusive)
     * @param value the value (essentially lightness) of the color from 0.0 (black,
     *                   inclusive) to 1.0 (very bright, inclusive).
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
	 *                   inclusive) to 1.0 (very bright, inclusive).
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
	T getRandom(IRNG rng, int opacity);

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
     * 1.0 (very bright, inclusive).
     */
    float getValue(T c);

    /**
     * @param c
     * @return The color that {@code this} shows when {@code c} is requested. May be {@code c} itself.
     */
    T filter(T c);

	/**
	 * @param ics
	 * @return {@code ics} filtered according to {@link #filter(Object)}. May be
	 *         {@code ics} itself if unchanged.
	 */
	IColoredString<T> filter(IColoredString<T> ics);

	/**
	 * Gets a copy of t and modifies it to make a shade of gray with the same brightness.
	 * The doAlpha parameter causes the alpha to be considered in the calculation of brightness and also changes the
	 * returned alpha of the color.
	 * Not related to reified types or any usage of "reify."
	 * @param t a T to copy; only the copy will be modified
	 * @param doAlpha
	 *            Whether to include (and hereby change) the alpha component.
	 * @return A monochromatic variation of {@code t}.
	 */
	T greify(/*@Nullable*/ T t, boolean doAlpha);

    /**
     * Gets the linear interpolation from Color start to Color end, changing by the fraction given by change.
     * @param start the initial color T
     * @param end the "target" color T
     * @param change the degree to change closer to end; a change of 0.0f produces start, 1.0f produces end
     * @return a new T between start and end
     */
    T lerp(T start, T end, float change);
    /**
     * Gets a fully-desaturated version of the given color (keeping its brightness, but making it grayscale).
     * Keeps alpha the same; if you want alpha to be considered (and brightness to be calculated differently), then
     * you can use greify() in this class instead.
     * @param color the color T to desaturate (will not be modified)
     * @return the grayscale version of color
     */
    T desaturated(T color);

    /**
     * Brings a color closer to grayscale by the specified degree and returns the new color (desaturated somewhat).
     * Alpha is left unchanged.
     * @param color the color T to desaturate
     * @param degree a float between 0.0f and 1.0f; more makes it less colorful
     * @return the desaturated (and if a filter is used, also filtered) new color T
     */
    T desaturate(T color, float degree);

    /**
     * Fully saturates color (makes it a vivid color like red or green and less gray) and returns the modified copy.
     * Leaves alpha unchanged.
     * @param color the color T to saturate (will not be modified)
     * @return the saturated version of color
     */
    T saturated(T color);


    /**
     * Saturates color (makes it closer to a vivid color like red or green and less gray) by the specified degree and
     * returns the new color (saturated somewhat). If this is called on a color that is very close to gray, this does
     * not necessarily return a specific color, but most implementations will treat a hue of 0 as red.
     * @param color the color T to saturate
     * @param degree a float between 0.0f and 1.0f; more makes it more colorful
     * @return the saturated (and if a filter is used, also filtered) new color
     */
    public T saturate(T color, float degree);

	/**
	 * Finds a gradient with 16 steps going from fromColor to toColor,
	 * both included in the gradient.
	 * @param fromColor the color to start with, included in the gradient
	 * @param toColor the color to end on, included in the gradient
	 * @return an ArrayList composed of the blending steps from fromColor to toColor, with length equal to steps
	 */
	public ArrayList<T> gradient(T fromColor, T toColor);

	/**
	 * Finds a gradient with the specified number of steps going from fromColor to toColor,
	 * both included in the gradient.
	 * @param fromColor the color to start with, included in the gradient
	 * @param toColor the color to end on, included in the gradient
	 * @param steps the number of elements to use in the gradient
	 * @return an ArrayList composed of the blending steps from fromColor to toColor, with length equal to steps
	 */
	public ArrayList<T> gradient(T fromColor, T toColor, int steps);

	/**
	 * A skeletal implementation of {@link IColorCenter}.
	 * 
	 * @author smelC
	 * 
	 * @param <T> a concrete color type
	 */
	abstract class Skeleton<T> implements IColorCenter<T> {

		private final Map<Long, T> cache = new HashMap<>(256);

		protected /*Nullable*/ IFilter<T> filter;

		/**
		 * @param filter
		 * 			The filter to use, or {@code null} for no filter.
		 */
		protected Skeleton(/*Nullable*/ IFilter<T> filter) {
			this.filter = filter;
		}

        /**
         * It clears the cache. You may need to do this to limit the cache to the colors used in a specific section.
		 * This is also useful if a Filter changes what colors it should return on a frame-by-frame basis; in that case,
		 * you can call clearCache() at the start or end of a frame to ensure the next frame gets different colors.
         */
        public void clearCache()
        {
            cache.clear();
        }

		/**
		 * The actual cache is not public, but there are cases where you may want to know how many different colors are
		 * actually used in a frame or a section of the game. If the cache was emptied (which might be from calling
		 * {@link #clearCache()}), some colors were requested, then this is called, the returned int should be the
		 * count of distinct colors this IColorCenter had created and cached; duplicates won't be counted twice.
		 * @return
		 */
		public int cacheSize()
		{
			return cache.size();
		}

        /**
         * You may want to copy colors between IColorCenter instances that have different create() methods -- and as
         * such, will have different values for the same keys in the cache. This allows you to copy the cache from other
         * into this Skeleton, but using this Skeleton's create() method.
         * @param other another Skeleton of the same type that will have its cache copied into this Skeleton
         */
        public void copyCache(Skeleton<T> other)
        {
            for (Map.Entry<Long, T> k : other.cache.entrySet())
            {
                cache.put(k.getKey(), create(getRed(k.getValue()), getGreen(k.getValue()), getBlue(k.getValue()),
                        getAlpha(k.getValue())));
            }
        }

		/**
		 * If you're changing the filter, you should likely call
		 * {@link #clearCache()}.
		 * 
		 * @param filter
		 *            The filter to use, or {@code null} to turn filtering OFF.
		 * @return {@code this}
		 */
		public Skeleton<T> setFilter(IFilter<T> filter) {
			this.filter = filter;
			return this;
		}

		protected transient Long tempValue;

        @Override
        public T get(int red, int green, int blue, int opacity) {
            tempValue = getUniqueIdentifier(red, green, blue, opacity);
            T t = cache.get(tempValue);
            if (t == null) {
				/* Miss */
                t = create(red, green, blue, opacity);
				/* Put in cache */
                cache.put(tempValue, t);
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
                float h = ((hue + 6f) % 1f) * 6f; // allows negative hue to wrap
                int i = (int)h;
                float a = value * (1 - saturation);
                float b = value * (1 - saturation * (h - i));
                float c = value * (1 - saturation * (1 - (h - i)));

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
		public T getRandom(IRNG rng, int opacity) {
			return get(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), opacity);
		}

        /**
         * @param r the red component in 0.0 to 1.0 range, typically
         * @param g the green component in 0.0 to 1.0 range, typically
         * @param b the blue component in 0.0 to 1.0 range, typically
         * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a
         * bright color, exclusive)
         */
        public float getSaturation(float r, float g, float b) {
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
         * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a
         * bright color, exclusive)
         */
        @Override
        public float getSaturation(T c) {
            return getSaturation(getRed(c) / 255f, getGreen(c) / 255f, getBlue(c) / 255f);
        }

        /**
         * @param r the red component in 0.0 to 1.0 range, typically
         * @param g the green component in 0.0 to 1.0 range, typically
         * @param b the blue component in 0.0 to 1.0 range, typically
         * @return the value (essentially lightness) of the color from 0.0 (black, inclusive) to
         * 1.0 (very bright, inclusive).
         */
        public float getValue(float r, float g, float b)
        {
            return Math.max(Math.max(r, g), b);
        }
        /**
         * @param c a concrete color
         * @return the value (essentially lightness) of the color from 0.0 (black, inclusive) to
		 * 1.0 (very bright, inclusive).
		 */
        @Override
        public float getValue(T c) {
            float r = getRed(c) / 255f;                     //RGB from 0 to 255
            float g = getGreen(c) / 255f;
            float b = getBlue(c) / 255f;

            return Math.max(Math.max(r, g), b);
        }

        /**
         * @param r the red component in 0.0 to 1.0 range, typically
         * @param g the green component in 0.0 to 1.0 range, typically
         * @param b the blue component in 0.0 to 1.0 range, typically
         * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
         * eventually to purple before looping back to almost the same red (1.0, exclusive)
         */
        public float getHue(float r, float g, float b) {
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
                float rDelta = (((max - r) / 6f) + (delta / 2f)) / delta;
                float gDelta = (((max - g) / 6f) + (delta / 2f)) / delta;
                float bDelta = (((max - b) / 6f) + (delta / 2f)) / delta;

                if       (r == max) hue = bDelta - gDelta;
                else if (g == max) hue = (1f / 3f) + rDelta - bDelta;
                else                 hue = (2f / 3f) + gDelta - rDelta;

                if (hue < 0) hue += 1f;
                else if (hue > 1) hue -= 1;
            }
            return hue;
        }

        /**
         * @param c a concrete color
         * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
         * eventually to purple before looping back to almost the same red (1.0, exclusive)
         */
        @Override
        public float getHue(T c) {
            return getHue(getRed(c) / 255f, getGreen(c) / 255f, getBlue(c) / 255f);
        }

        @Override
        public T filter(T c)
        {
        	return c == null ? null : get(getRed(c), getGreen(c), getBlue(c), getAlpha(c));
        }


		@Override
		public IColoredString<T> filter(IColoredString<T> ics) {
			/*
			 * It is common not to have a filter or to have the identity one. To
			 * avoid always copying strings in this case, we first roll over the
			 * string to see if there'll be a change.
			 * 
			 * This is clearly a subjective design choice but my industry
			 * experience is that minimizing allocations is the thing to do for
			 * performances, hence I prefer iterating twice to do that.
			 */
			boolean change = false;
			for (IColoredString.Bucket<T> bucket : ics) {
				final T in = bucket.getColor();
				if (in == null)
					continue;
				final T out = filter(in);
				if (in != out) {
					change = true;
					break;
				}
			}

			if (change) {
				final IColoredString<T> result = IColoredString.Impl.create();
				for (IColoredString.Bucket<T> bucket : ics)
					result.append(bucket.getText(), filter(bucket.getColor()));
				return result;
			} else
				/* Only one allocation: the iterator, yay \o/ */
				return ics;
		}
		/**
		 * Gets a copy of t and modifies it to make a shade of gray with the same brightness.
		 * The doAlpha parameter causes the alpha to be considered in the calculation of brightness and also changes the
		 * returned alpha of the color.
		 * Not related to reified types or any usage of "reify."
		 * @param t a T to copy; only the copy will be modified
		 * @param doAlpha
		 *            Whether to include (and hereby change) the alpha component.
		 * @return A monochromatic variation of {@code t}.
		 */
		@Override
		public T greify(T t, boolean doAlpha) {
			if (t == null)
				/* Cannot do */
				return null;
			final int red = getRed(t);
			final int green = getGreen(t);
			final int blue = getBlue(t);
			final int alpha = getAlpha(t);
			final int rgb = red + green + blue;
			final int mean;
			final int newAlpha;
			if (doAlpha) {
				mean = (rgb + alpha) / 4;
				newAlpha = mean;
			} else {
				mean = rgb / 3;
				/* No change */
				newAlpha = alpha;
			}
			return get(mean, mean, mean, newAlpha);
		}

		/**
		 * Gets the linear interpolation from Color start to Color end, changing by the fraction given by change.
         * This implementation tries to work with colors in a way that is as general as possible, using getRed() instead
         * of some specific detail that depends on how a color is implemented. Other implementations that specialize in
         * a specific type of color may be able to be more efficient.
         * @param start the initial color T
         * @param end the "target" color T
         * @param change the degree to change closer to end; a change of 0.0f produces start, 1.0f produces end
         * @return a new T between start and end
		 */
		public T lerp(T start, T end, float change)
		{
			if(start == null || end == null)
				return null;
			final int sr = getRed(start), sg = getGreen(start), sb = getBlue(start), sa = getAlpha(start),
					er = getRed(end), eg = getGreen(end), eb = getBlue(end), ea = getAlpha(end);
			return get(
					(int)(sr + change * (er - sr)),
					(int)(sg + change
							* (eg - sg)),
					(int)(sb + change * (eb - sb)),
					(int)(sa + change * (ea - sa))
			);
		}
		/**
		 * Gets a fully-desaturated version of the given color (keeping its brightness, but making it grayscale).
		 * Keeps alpha the same; if you want alpha to be considered (and brightness to be calculated differently), then
		 * you can use greify() in this class instead.
		 * @param color the color T to desaturate (will not be modified)
		 * @return the grayscale version of color
		 */
		public T desaturated(T color)
		{
			int f = (int)Math.min(255, getRed(color) * 0.299f + getGreen(color) * 0.587f + getBlue(color) * 0.114f);
			return get(f, f, f, getAlpha(color));
		}

		/**
		 * Brings a color closer to grayscale by the specified degree and returns the new color (desaturated somewhat).
		 * Alpha is left unchanged.
		 * @param color the color T to desaturate
		 * @param degree a float between 0.0f and 1.0f; more makes it less colorful
		 * @return the desaturated (and if a filter is used, also filtered) new color T
		 */
		public T desaturate(T color, float degree)
		{
			return lerp(color, desaturated(color), degree);
		}

        /**
         * Fully saturates color (makes it a vivid color like red or green and less gray) and returns the modified copy.
         * Leaves alpha unchanged.
         * @param color the color T to saturate (will not be modified)
         * @return the saturated version of color
         */
        public T saturated(T color)
        {
            return getHSV(getHue(color), 1f, getValue(color), getAlpha(color));
        }
		/**
		 * Saturates color (makes it closer to a vivid color like red or green and less gray) by the specified degree and
		 * returns the new color (saturated somewhat). If this is called on a color that is very close to gray, this is
		 * likely to produce a red hue by default (if there's no hue to make vivid, it needs to choose something).
		 * @param color the color T to saturate
		 * @param degree a float between 0.0f and 1.0f; more makes it more colorful
		 * @return the saturated (and if a filter is used, also filtered) new color
		 */
		public T saturate(T color, float degree)
		{
			return lerp(color, saturated(color), degree);
		}

		@Override
		public ArrayList<T> gradient(T fromColor, T toColor) {
			return gradient(fromColor, toColor, 16);
		}

		@Override
		public ArrayList<T> gradient(T fromColor, T toColor, int steps) {
			ArrayList<T> colors = new ArrayList<>((steps > 1) ? steps : 1);
			colors.add(filter(fromColor));
			if(steps < 2)
				return colors;
			for (float i = 1; i < steps; i++) {
				colors.add(lerp(fromColor, toColor, i / (steps - 1f)));
			}
			return colors;

		}

        /**
         * Create a concrete instance of the color type given as a type parameter. That's the
         * place to use the {@link #filter}.
         *
         * @param red the red component of the desired color
         * @param green the green component of the desired color
         * @param blue the blue component of the desired color
         * @param opacity the alpha component or opacity of the desired color
         * @return a fresh instance of the concrete color type
         */
        protected abstract T create(int red, int green, int blue, int opacity);

		protected long getUniqueIdentifier(int r, int g, int b, int a) {
			return ((a & 0xffL) << 48) | ((r & 0xffffL) << 32) | ((g & 0xffffL) << 16) | (b & 0xffffL);
		}

	}
}
