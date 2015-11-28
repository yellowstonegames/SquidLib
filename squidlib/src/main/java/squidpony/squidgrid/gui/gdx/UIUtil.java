package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * @author smelC
 */
public class UIUtil {

	/**
	 * Draws margins around a rectangle
	 * 
	 * @param botLeftX
	 *            The rectangle's bottom left.
	 * @param botLeftY
	 *            The rectangle's bottom left.
	 * @param width
	 *            The rectangle's width.
	 * @param height
	 *            The rectangle's height.
	 * @param xmargin
	 *            The size of the left margin and the size of the right margin.
	 * @param ymargin
	 *            The size of the bottom margin and the size of the top margin.
	 * @param c
	 *            The margins' colors.
	 */
	public static void drawMarginsAround(float botLeftX, float botLeftY, int width, int height, int xmargin,
			int ymargin, Color c) {
		if (xmargin == 0 && ymargin == 0)
			return;

		final ShapeRenderer renderer = new ShapeRenderer();
		renderer.begin(ShapeType.Filled);
		renderer.setColor(c);

		if (0 < xmargin) {
			/* The left rectangle */
			renderer.rect(botLeftX - xmargin, botLeftY - ymargin, xmargin, height + (ymargin * 2));
			/* The right rectangle */
			renderer.rect(botLeftX + width, botLeftY - ymargin, xmargin, height + (ymargin * 2));
		}
		if (0 < ymargin) {
			/* The bottom rectangle */
			renderer.rect(botLeftX, botLeftY - ymargin, width, ymargin);
			/* The top rectangle */
			renderer.rect(botLeftX, botLeftY + height, width, ymargin);
		}

		renderer.end();
		renderer.dispose();
	}

	/**
	 * @param botLeftX
	 *            The bottom left x cell of the rectangle to draw around.
	 * @param botLeftY
	 *            The bottom left y cell of the rectangle to draw around.
	 * @param width
	 *            The width of the button considered.
	 * @param height
	 *            The width of the button considered.
	 */
	public static void drawMarginsAround(float botLeftX, float botLeftY, int width, int height, int margin,
			Color color, CornerStyle cornerStyle) {
		if (margin == 0 || color == null)
			/* Nothing to do */
			return;

		final ShapeRenderer renderer = new ShapeRenderer();
		renderer.begin(ShapeType.Filled);
		renderer.setColor(color);

		if (cornerStyle == CornerStyle.ROUNDED || cornerStyle == CornerStyle.MISSING) {
			/* Left margin */
			renderer.rect(botLeftX - margin, botLeftY, margin, height);
			/* Right margin */
			renderer.rect(botLeftX + width, botLeftY, margin, height);
		} else {
			/* Left margin */
			renderer.rect(botLeftX - margin, botLeftY - margin, margin, height + (margin * 2));
			/* Right margin */
			renderer.rect(botLeftX + width, botLeftY - margin, margin, height + (margin * 2));
		}
		/* Bottom margin */
		renderer.rect(botLeftX, botLeftY - margin, width, margin);
		/* Top margin */
		renderer.rect(botLeftX, botLeftY + height, width, margin);

		if (cornerStyle == CornerStyle.ROUNDED) {
			/* Bottom left */
			renderer.arc(botLeftX, botLeftY, margin, 180, 90);
			/* Top left */
			renderer.arc(botLeftX, botLeftY + height, margin, 90, 90);
			/* Top right */
			renderer.arc(botLeftX + width, botLeftY + height, margin, 0, 90);
			/* Bottom Right */
			renderer.arc(botLeftX + width, botLeftY, margin, 270, 90);
		}

		renderer.end();
		renderer.dispose();
	}

	/**
	 * @author smelC
	 */
	public static enum CornerStyle {
		SQUARE,
		/**
		 * Here's an example of this style:
		 * 
		 * <br>
		 * 
		 * <img src="http://i.imgur.com/AQgWeic.png"/>.
		 */
		ROUNDED,
		/**
		 * A NES-like style (to my taste..). Try it, I can't explain it with
		 * sentences. Here's an example:
		 * 
		 * <br>
		 * 
		 * <img src="http://i.imgur.com/PQSvT0t.png"/>
		 */
		MISSING,
	}

}
