package squidpony.squidgrid.gui.gdx;

import squidpony.IColorCenter;

import com.badlogic.gdx.graphics.Color;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's {@link Color}.
 * 
 * @author smelC
 */
public class SquidColorCenter extends IColorCenter.Skeleton<Color> {

	@Override
	protected Color create(int red, int green, int blue, int opacity) {
		return new Color(red / 255f, green / 255f, blue / 255f, opacity / 255f);
	}

	@Override
	public int getRed(Color c) {
		return Math.round(c.r * 255f);
	}

	@Override
	public int getGreen(Color c) {
		return Math.round(c.g * 255f);
	}

	@Override
	public int getBlue(Color c) {
		return Math.round(c.b * 255f);
	}

	@Override
	public int getAlpha(Color c) {
		return Math.round(c.a * 255f);
	}

}
