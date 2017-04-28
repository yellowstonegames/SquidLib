package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.panel.IColoredString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A screen designed to write some text in full screen. This class supports text
 * alignment (left, center, right) and text wrapping (see {@link #wrap(int)}).
 * <p>
 * You may prefer using {@link TextPanel} or sometimes {@link LinesPanel} where
 * you want to display more than a few words of legible wrapped text.
 * </p>
 *
 * @author smelC
 */
public abstract class AbstractTextScreen<T extends Color> extends AbstractSquidScreen<T> {

    /**
     * Can contain null members (denoting empty lines)
     */
    protected List<IColoredString<T>> text;
    protected /* @Nullable */ int[] alignment;

    /**
     * @param ssi       See super class
     * @param text      The text to display. From top to bottom. Use {@code null}
     *                  members to jump lines.
     *                  <p>
     *                  Give {@code null} if you want to set it later (using
     *                  {@link #init(List, int[])}).
     *                  </p>
     * @param alignment How to alignment members of {@code text}. -1 for left, 0 for
     *                  center, 1 for right. The default is to align left
     *                  <p>
     *                  Give {@code null} if you want to set it later (using
     *                  {@link #init(List, int[])}).
     *                  </p>
     */
    public AbstractTextScreen(SquidScreenInput<T> ssi, /* @Nullable */ List<IColoredString<T>> text,
            /* @Nullable */ int[] alignment) {
        super(ssi);
        this.text = text;
        this.alignment = alignment;
    }

    /**
     * You should call this method at most once. You should call this method
     * only before rendering this screen.
     *
     * @param text      The text to display. From top to bottom. Use {@code null}
     *                  members to jump lines.
     * @param alignment How to alignment members of {@code text}. -1 for left, 0 for
     *                  center, 1 for right. The default is to align left
     */
    public void init(List<IColoredString<T>> text, /* @Nullable */ int[] alignment) {
        this.text = text;
        this.alignment = alignment;
    }

    /**
     * Wraps the text inside {@code this} according to {@code width}. This
     * screen's text must have been set already. This, of course, preserves the
     * text alignment (if any).
     *
     * @param width
     * @throws IllegalStateException If {@code this}'s text hasn't been initialized yet.
     */
    public void wrap(int width) {
        if (text == null)
            throw new IllegalStateException("Cannot wrap an unitialized " + getClass().getSimpleName());

        final List<IColoredString<T>> tsave = text;
        text = new ArrayList<>(tsave.size() * 2);
        final int[] asave = alignment;
        final /* @Nullable */ List<Integer> newAlignments = asave == null ? null
                : new ArrayList<Integer>(asave.length * 2);
        int i = 0;
        for (IColoredString<T> t : tsave) {
			/* Wrap line */
            if (t == null) {
				/* An empty line */
                text.add(null);
                if (newAlignments != null)
                    newAlignments.add(/* doesn't matter */ 0);
            } else {
                final List<IColoredString<T>> wrapped = t.wrap(width);
                final /* @Nullable */ Integer alignment = asave == null || asave.length <= i ? null : asave[i];
                for (IColoredString<T> line : wrapped) {
					/* Add wrapped */
                    text.add(line);
                    if (newAlignments != null && alignment != null)
						/* Keep alignment */
                        newAlignments.add(alignment);
                }
            }
            i++;
        }
        alignment = newAlignments == null ? null : toIntArray(newAlignments);
    }

    protected int[] toIntArray(Collection<Integer> l) {
        final int[] result = new int[l.size()];
        int j = 0;
        for (int i : l)
            result[j++] = i;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        if (text != null) {
			/* Show text */
            final Iterator<? extends IColoredString<?>> it = text.iterator();
            final String eol = System.getProperty("line.separator");
            buf.append(eol);
            while (it.hasNext()) {
                final IColoredString<?> ics = it.next();
                buf.append(ics == null ? "" : ics.present());
                if (it.hasNext())
                    buf.append(eol);
            }
        }
        return buf.toString();
    }

}
