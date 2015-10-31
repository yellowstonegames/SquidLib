package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.IColorCenter;

/**
 * A concrete implementation of {@link IColorCenter} for {@link HDRColor}, an extension of libgdx's
 * {@link com.badlogic.gdx.graphics.Color}.
 * Arguments that expect HDRColor can also be passed SColor constants, since that class extends HDRColor now.
 *
 * @author smelC
 * @author Tommy Ettinger
 * @see SColor Another way to obtain colors by using pre-allocated (and named) instances.
 */
public class SquidColorCenter extends IColorCenter.Skeleton<HDRColor> {

    public Filter filter;
    public SquidColorCenter()
    {
        filter = new Filters.IdentityFilter();
    }
    public SquidColorCenter(Filter filterEffect)
    {
        filter = filterEffect;
    }
	@Override
	protected HDRColor create(int red, int green, int blue, int opacity) {
		return filter.alter(red / 255f, green / 255f, blue / 255f, opacity / 255f);
	}
    public HDRColor get(HDRColor c)
    {
        return get(Math.round(c.hr * 255f), Math.round(c.hg * 255f), Math.round(c.hb * 255f), Math.round(c.a * 255f));
    }
    public HDRColor get(Color c)
    {
        return get(Math.round(c.r * 255f), Math.round(c.g * 255f), Math.round(c.b * 255f), Math.round(c.a * 255f));
    }
    public HDRColor get(long c)
    {
        return get((int)((c >> 40) & 0xffff), (int)((c >> 24) & 0xffff), (int)((c >> 8) & 0xffff), (int)(c & 0xff));
    }
	@Override
	public int getRed(HDRColor c) {
		return Math.round(c.hr * 255f);
	}

	@Override
	public int getGreen(HDRColor c) {
		return Math.round(c.hg * 255f);
	}

	@Override
	public int getBlue(HDRColor c) {
		return Math.round(c.hb * 255f);
	}

	@Override
	public int getAlpha(HDRColor c) {
		return Math.round(c.a * 255f);
	}

    public static long encode (HDRColor color) {
        return (Math.round(color.hr * 255.0) << 40) | (Math.round(color.hg * 255.0) << 24) | (Math.round(color.hb * 255.0) << 8) | Math.round(color.a * 255.0);
    }
    public static long encode (Color color) {
        return (Math.round(color.r * 255.0) << 40) | (Math.round(color.g * 255.0) << 24) | (Math.round(color.b * 255.0) << 8) | Math.round(color.a * 255.0);
    }
}
