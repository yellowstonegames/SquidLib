package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import squidpony.SquidTags;
import squidpony.panel.IPanelBuilder;

/**
 * A partial application adapter that uses the Zodiac-Square fonts. It should be
 * completed as follows: the {@link #create()} method should assign
 * {@link #screen}. Then, you should implement
 * {@link AbstractSquidScreen#getNext()}; and you'll be done.
 * 
 * @author smelC
 */
public class SquidApplicationAdapter extends ApplicationAdapter {

	protected final IPanelBuilder ipb;

	/** Should be assigned in {@link #create()} */
	protected /* @Nullable */ AbstractSquidScreen<Color> screen;

	/**
	 * @param icc
	 * @param ipb
	 *            An {@link IPanelBuilder} that specifies which font sizes are
	 *            available. Use {@link IPanelBuilder.Skeleton} to help build
	 *            this instance.
	 */
	public SquidApplicationAdapter() {
		this.ipb = new SquidPanelBuilder(12, 24, 0, DefaultResources.getSCC(), null) {
			@Override
			protected String fontfile(int sz) {
				if (sz == 12)
					return "Zodiac-Square-12x12.fnt";
				else if (sz == 24)
					return "Zodiac-Square-24x24.fnt";
				else
					throw new IllegalStateException(
							"Sorry this panel builder only supports a square font of size 12 or 24");
			}
		};
	}

	@Override
	public void render() {
		if (screen == null) {
			/* Weird */
			Gdx.app.log(SquidTags.SCREEN,
					"Unexpected state in " + getClass().getSimpleName() + ". Did create get called ?");
			return;
		}

		if (screen.isDisposed()) {
			screen = screen.getNext();
			if (screen == null) {
				/* Quit */
				Gdx.app.exit();
				/* This point is unreachable */
			}
		} else if (screen.hasPendingResize())
			/* Rebuild a new screen */
			screen = screen.getNext();
		else
			/* Normal behavior, forward */
			screen.render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize(int width, int height) {
		if (screen == null) {
			/* Weird */
			Gdx.app.log(SquidTags.SCREEN,
					"Unexpected state in " + getClass().getSimpleName() + ". Did create get called ?");
		} else
			/* forward */
			screen.resize(width, height);
	}

	@Override
	public void pause() {
		if (screen != null)
			/* forward */
			screen.pause();
	}

	@Override
	public void resume() {
		if (screen != null)
			/* forward */
			screen.pause();
	}

	@Override
	public void dispose() {
		if (screen != null)
			/* forward, to clean up */
			screen.dispose();
	}

}
