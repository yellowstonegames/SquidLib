package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import squidpony.SquidTags;
import squidpony.panel.IColoredString;

import java.util.List;

/**
 * A less abstract version of {@link AbstractTextScreen} tuned for libgdx
 * {@link Color}. It uses a {@link GroupCombinedPanel} for display.
 * 
 * <p>
 * Here's an example of this class in action:
 * 
 * <img src="http://i.imgur.com/g34bHvo.png"></img>
 * </p>
 * 
 * @author smelC
 * 
 * @see AbstractTextScreen
 */
@Deprecated
public abstract class TextScreen extends AbstractTextScreen<Color> {

	protected /* @Nullable */ GroupCombinedPanel<Color> gcp;

	/**
	 * Doc: see
	 * {@link AbstractTextScreen#AbstractTextScreen(squidpony.squidgrid.gui.gdx.AbstractSquidScreen.SquidScreenInput, List, int[])}
	 * .
	 */
	public TextScreen(SquidScreenInput<Color> si, /* @Nullable */List<IColoredString<Color>> text,
			/* @Nullable */ int[] alignment) {
		super(si, text, alignment);
	}

	@Override
	public void render(float delta) {
		if (gcp != null)
			/* Job done already */
			return;

		gcp = new GroupCombinedPanel<>(buildScreenWideSquidPanel(), buildScreenWideSquidPanel());

		final int width = gcp.getGridWidth();
		final int height = gcp.getGridHeight();

		if (text == null) {
			Gdx.app.log(SquidTags.SCREEN,
					"Cannot display null list of text in " + getClass().getSimpleName());
			return;
		}

		int y = 0;
		for (IColoredString<Color> ics : text) {
			if (height <= y)
				/* Outside the screen */
				break;

			if (ics == null) {
				/* An empty line */
				y++;
				continue;
			}

			/* the alignment for 'ics' */
			final int a;
			if (alignment == null)
				/* default is left */
				a = -1;
			else {
				if (y < alignment.length) {
					final int b = alignment[y];
					if (-1 <= b && b <= 1)
						a = b;
					else {
						Gdx.app.log(SquidTags.SCREEN,
								"Unrecognized alignment in " + getClass().getSimpleName() + ":" + b
										+ ". Expected -1, 0, or 1. Defaulting to -1 (left)");
						/* default is left */
						a = -1;
					}
				} else {
					/* default is left */
					a = -1;
				}
			}
			final int x;
			if (a == -1)
				/* left */
				x = 0;
			else if (a == 0) {
				/* center */
				final int len = ics.length();
				if (width <= len)
					/* String too large, it'll get cut */
					x = 0;
				else
					x = width - (ics.length() / 2);
			} else {
				/* right */
				assert a == 1;
				final int len = ics.length();
				x = len < width ? width - len : 0;
			}

			gcp.putFG(x, y, ics);
			y++;
		}

		if (stage == null)
			/* Looks like we need to build it */
			stage = buildStage();

		stage.addActor(gcp);
		stage.draw();
	}

}
