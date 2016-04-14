package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * @author smelC
 */
public class UIUtil {

	/**
	 * Draws margins around an actor.
	 * 
	 * @param renderer_
	 *            The renderer to use. If {@code null} a new one will be
	 *            allocated.
	 * @param a
	 * @param margin
	 *            The size of the margin to draw.
	 * @param c
	 *            The margins' colors.
	 */
	public static void drawMarginsAround(ShapeRenderer renderer_, Actor a, int margin, Color color,
			CornerStyle cornerStyle) {
		drawMarginsAround(renderer_, a.getX(), a.getY(), a.getWidth(), a.getHeight(), margin, color,
				cornerStyle, 1f, 1f);
	}

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
	 * @param renderer_
	 *            The renderer to use. If {@code null} a new one will be
	 *            allocated.
	 * @param botLeftX
	 *            The bottom left x cell of the rectangle to draw around.
	 * @param botLeftY
	 *            The bottom left y cell of the rectangle to draw around.
	 * @param width
	 *            The width of the button considered.
	 * @param height
	 *            The width of the button considered.
	 * @param margin
	 *            The size of the margin to draw.
	 * @param color
	 *            The color to draw
	 * @param cornerStyle
	 *            The style with which to draw the margins
	 */
	public static void drawMarginsAround(ShapeRenderer renderer_, float botLeftX, float botLeftY, float width,
			float height, float margin, Color color, CornerStyle cornerStyle) {
		drawMarginsAround(renderer_, botLeftX, botLeftY, width, height, margin, color, cornerStyle, 1f, 1f);
	}

	/**
	 * @param renderer_
	 *            The renderer to use. If {@code null} a new one will be
	 *            allocated.
	 * @param botLeftX
	 *            The bottom left x cell of the rectangle to draw around.
	 * @param botLeftY
	 *            The bottom left y cell of the rectangle to draw around.
	 * @param width
	 *            The width of the button considered.
	 * @param height
	 *            The width of the button considered.
	 * @param margin
	 *            The size of the margin to draw.
	 * @param color
	 *            The color to draw
	 * @param cornerStyle
	 *            The style with which to draw the margins
	 * @param zoomX
	 *            A multiplier for the world x-size of non-ShapeRenderer
	 *            objects, that needs to be reversed for this
	 * @param zoomY
	 *            A multiplier for the world y-size of non-ShapeRenderer
	 *            objects, that needs to be reversed for this
	 */
	public static void drawMarginsAround(ShapeRenderer renderer_, float botLeftX, float botLeftY, float width,
			float height, float margin, Color color, CornerStyle cornerStyle, float zoomX, float zoomY) {
		if (margin == 0 || color == null)
			/* Nothing to do */
			return;
		botLeftY += 1;

		final ShapeRenderer renderer = renderer_ == null ? new ShapeRenderer() : renderer_;
		renderer.begin(ShapeType.Filled);
		renderer.scale(1f / zoomX, 1f / zoomY, 1f);
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

		if (renderer_ == null)
			/* I allocated it, I must dispose it */
			renderer.dispose();
	}

	/**
	 * Draws a rectangle using a {@link ShapeRenderer}.
	 * 
	 * @parem sRender_ The renderer to use. If {@code null} a new one will be
	 *        allocated.
	 * @param botLeftX
	 *            The bottom left x of the rectangle.
	 * @param botLeftY
	 *            The bottom left y of the rectangle.
	 * @param width
	 *            The rectangle's width
	 * @param height
	 *            The rectangle's height
	 * @param st
	 *            The style to use
	 * @param color
	 *            The rectangle's color
	 */
	public static void drawRectangle(/* @Nullable */ShapeRenderer sRender_, float botLeftX, float botLeftY,
			float width, float height, ShapeType st, Color color) {
		final ShapeRenderer sRender = sRender_ == null ? new ShapeRenderer() : sRender_;
		final boolean reset;
		/* No matter the state of the given ShapeRenderer, we'll be fine, thanks to this: */
		if (!sRender.isDrawing()) {
			reset = true;
			sRender.begin(st);
		} else
			reset = false;
		sRender.setColor(color);
		sRender.rect(botLeftX, botLeftY, width, height);
		if (reset)
			sRender.end();
		if (sRender != sRender_)
			/* I allocated it */
			sRender.dispose();
	}

	/**
	 * Draws a rectangle using a {@link ShapeRenderer}, allocating a new one for
	 * the occasion.
	 * 
	 * @param botLeftX
	 *            The bottom left x of the rectangle.
	 * @param botLeftY
	 *            The bottom left y of the rectangle.
	 * @param width
	 *            The rectangle's width
	 * @param height
	 *            The rectangle's height
	 * @param st
	 *            The style to use
	 * @param color
	 *            The rectangle's color
	 */
	public static void drawRectangle(float botLeftX, float botLeftY, float width, float height, ShapeType st,
			Color color) {
		drawRectangle(null, botLeftX, botLeftY, width, height, st, color);
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
