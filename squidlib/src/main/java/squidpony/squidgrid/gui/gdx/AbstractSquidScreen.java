package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import squidpony.IColorCenter;
import squidpony.ScreenSizeManager;
import squidpony.panel.IPanelBuilder;

/**
 * A SquidLib-aware partial implementation of {@link ScreenAdapter}. This is a
 * very general implementation (on which I rely in my game), please do not
 * change that too radically.
 * 
 * <p>
 * By implementing {@link #getNext()}, you specify how to switch between screens
 * (for example: splash screen -> (main menu screen <-> game screen)). To build
 * your {@link SquidPanel}, you should use the protected methods that this class
 * provides. In this way, you won't have to worry about the screen size and
 * resizes.
 * </p>
 * 
 * <p>
 * Moving from a screen to another is either triggered by libgdx (when it calls
 * {@link #resize(int, int)} and {@link #dispose()}) or by you (you can call
 * {@link #dispose()} directly). In both cases, it'll make
 * {@link SquidApplicationAdapter}'s {@link ApplicationAdapter#render()} method
 * call {@link #getNext()}, hereby triggering screen change.
 * </p>
 * 
 * @author smelC
 * 
 * @param <T>
 *            The type of color
 */
public abstract class AbstractSquidScreen<T extends Color> extends ScreenAdapter {

	/**
	 * The current size manager. It is always up-to-date w.r.t. to the actual
	 * screen's size, except when a call to {@link #resize(int, int)} has been
	 * done by libgdx, and {@link #getNext()} wasn't called yet.
	 */
	protected ScreenSizeManager sizeManager;

	protected final IColorCenter<T> colorCenter;
	protected final IPanelBuilder ipb;

	protected boolean disposed = false;
	protected boolean resized = false;

	/**
	 * @param sizeManager
	 *            A size manager that is correct w.r.t. to the current screen
	 *            size. It is usually built by inspecting the current screen
	 *            size (see {@link Gdx#graphics}) and a cell size you want.
	 * 
	 *            <p>
	 *            The screen's size is not computed automatically by this
	 *            constructor, because usually you can build a single instance
	 *            of {@link ScreenSizeManager} at startup, and pass it along all
	 *            {@link AbstractSquidScreen}. The instance will only change
	 *            when there's some resizing (i.e. when ligdx calls
	 *            {@link ScreenAdapter#resize(int, int)}).
	 *            </p>
	 * @param colorCenter
	 *            The color center to use.
	 * @param ipb
	 *            How to build fresh {@link SquidPanel}.
	 */
	public AbstractSquidScreen(ScreenSizeManager sizeManager, IColorCenter<T> colorCenter,
			IPanelBuilder ipb) {
		this.sizeManager = sizeManager;
		this.colorCenter = colorCenter;
		this.ipb = ipb;
	}

	@Override
	public void dispose() {
		if (!disposed) {
			/* This should make getNext()'s behavior change */
			disposed = true;
			/*
			 * Either we're moving to a new screen, in which case this avoids
			 * glitches if the new screen does not paint everything; or we're
			 * leaving in which case we don't care.
			 */
			UIUtil.drawRectangle(0, 0, sizeManager.screenWidth, sizeManager.screenHeight, ShapeType.Filled,
					getClearingColor());
		}
	}

	@Override
	public void resize(int width, int height) {
		/* This should make getNext()'s behavior change */
		this.resized |= true;
		this.sizeManager = sizeManager.changeScreenSize(width, height);
	}

	/**
	 * Implementations of this method should likely inspect {@link #disposed}
	 * and {@link #resized}. When {@link #resized} holds, this method typically
	 * returns an instance that is a variation of {@code this}, where the font
	 * size has been changed (to get the new size, use {@link #sizeManager}; it
	 * has been updated already). When {@link #disposed} holds, the usual
	 * behavior is for this method to return null to quit the whole application
	 * (that's the assumption that {@link squidpony.SquidApplicationAdapter}
	 * does) or to return another screen to move forward (for example when
	 * switching from the main/splash screen to the game's screen).
	 * 
	 * <p>
	 * This method is normally called when the user is moving to the next screen
	 * (so that's your game logic) or when {@link #isDisposed()} holds or when
	 * {@link #hasPendingResize()} holds.
	 * </p>
	 * 
	 * @return The screen to use after this one, or {@code null} if the
	 *         application is quitting.
	 */
	public abstract /* @Nullable */ AbstractSquidScreen<T> getNext();

	/**
	 * @return Whether libgdx called {@link #dispose()} on {@code this}.
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * @return Whether libgdx called {@link #resize(int, int)} on {@code this}.
	 */
	public boolean hasPendingResize() {
		return resized;
	}

	/**
	 * Ideally, you should always go through this method to create a
	 * {@link SquidPanel}.
	 * 
	 * @return How this class builds {@link SquidPanel}.
	 */
	public IPanelBuilder getPanelbuilder() {
		return ipb;
	}

	/**
	 * @param desiredCellSize
	 * @return A screen wide squid panel, margins-aware, and with its position
	 *         set.
	 */
	protected final SquidPanel buildScreenWideSquidPanel(int desiredCellSize) {
		return ipb.buildScreenWide(sizeManager.screenWidth, sizeManager.screenHeight, desiredCellSize, null);
	}

	/**
	 * @return A screen wide squid panel, margins-aware, and with its position
	 *         set.
	 */
	protected final SquidPanel buildScreenWideSquidPanel() {
		final SquidPanel result = buildSquidPanel(sizeManager.wCells, sizeManager.hCells);
		/* TODO smelC Draw margins ? */
		result.setPosition(sizeManager.leftMargin, sizeManager.botMargin);
		return result;
	}

	/**
	 * @param width
	 * @param height
	 * @return A panel of size {@code (width, height)} that uses the default
	 *         cell width/cell height. Its position isn't set.
	 */
	protected final SquidPanel buildSquidPanel(int width, int height) {
		return buildSquidPanel(width, height, sizeManager.cellWidth, sizeManager.cellHeight);
	}

	/**
	 * @param width
	 * @param height
	 * @param cellSize
	 * @return A panel of size {@code (width, height)} that has {@code cellSize}
	 *         . Its position isn't set.
	 */
	protected final SquidPanel buildSquidPanel(int width, int height, int cellWidth, int cellHeight) {
		return ipb.buildByCells(width, height, cellWidth, cellHeight, null);
	}

	/**
	 * @return The color to use to repaint the screen entirely when moving from
	 *         a {@link SquidScreen} to another.
	 */
	protected T getClearingColor() {
		return colorCenter.getBlack();
	}

}
