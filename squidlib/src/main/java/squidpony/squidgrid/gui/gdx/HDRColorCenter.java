package squidpony.squidgrid.gui.gdx;

import squidpony.IColorCenter;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's {@link HDRColor}.
 * 
 * @author smelC
 * @author Tommy Ettinger
 * @see SColor Another way to obtain colors by using pre-allocated (and named) instances.
 */
public class HDRColorCenter extends IColorCenter.Skeleton<HDRColor> {

	@Override
	protected HDRColor create(int red, int green, int blue, int opacity) {
		return new HDRColor(red / 255f, green / 255f, blue / 255f, opacity / 255f);
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

}
