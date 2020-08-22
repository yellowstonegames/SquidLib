package squidpony;

import squidpony.panel.IColoredString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * An helper class for code that deals with lists of {@link IColoredString}s. It
 * does nothing smart, its only purpose is to save you some typing for frequent
 * calls. 
 * <br>
 * This class is deprecated in favor of using standard JDK {@code ArrayList<IColoredString<T>>}, where T is usually
 * Color from libGDX. Using ArrayList lets you build these using {@link Maker#makeList(Object[])}, if you want. Some of
 * the methods in this class aren't very obvious for what they actually do; {@link #add(Object)} will add a new line,
 * while {@link #addText(String)} will append text to the last line.
 * 
 * @author smelC
 * @deprecated This class duplicates the functionality of an ArrayList of IColoredString, and isn't very clear.
 */
@Deprecated
public class ColoredStringList<T> extends ArrayList<IColoredString<T>> {

	private static final long serialVersionUID = -5111205714079762803L;

	public ColoredStringList() {
		super();
	}

	public ColoredStringList(int expectedSize) {
		super(expectedSize);
	}

	/**
	 * @return A fresh empty instance.
	 */
	public static <T> ColoredStringList<T> create() {
		return new ColoredStringList<T>();
	}

	/**
	 * @param expectedSize
	 * @return A fresh empty instance.
	 */
	public static <T> ColoredStringList<T> create(int expectedSize) {
		return new ColoredStringList<T>(expectedSize);
	}

	/**
	 * Appends {@code text} to {@code this}, without specifying its color.
	 * 
	 * @param text the text to append
	 */
	public void addText(String text) {
		addColoredText(text, null);
	}

	/**
	 * Appends {@code text} to {@code this}.
	 * 
	 * @param text the text to append
	 */
	public void addText(IColoredString<T> text) {
		final int sz = size();
		if (sz == 0)
			add(text);
		else {
			get(sz - 1).append(text);
		}
	}

	/**
	 * Appends colored text to {@code this}.
	 * 
	 * @param text the text to append
	 */
	public void addColoredText(String text, T c) {
		if (isEmpty())
			addColoredTextOnNewLine(text, c);
		else {
			final IColoredString<T> last = get(size() - 1);
			last.append(text, c);
		}
	}

	/**
	 * Appends text to {@code this}, on a new line; without specifying its
	 * color.
	 * 
	 * @param text the text to append
	 */
	public void addTextOnNewLine(String text) {
		addColoredTextOnNewLine(text, null);
	}

	public void addTextOnNewLine(IColoredString<T> text) {
		add(text);
	}

	/**
	 * Appends colored text to {@code this}.
	 * 
	 * @param text the text to append
	 */
	public void addColoredTextOnNewLine(String text, /* @Nullable */ T color) {
		this.add(IColoredString.Impl.create(text, color));
	}

	/**
	 * Adds {@code texts} to {@code this}, starting a new line for the first
	 * one.
	 * 
	 * @param texts the Collection of objects extending IColoredString to append
	 */
	public void addOnNewLine(Collection<? extends IColoredString<T>> texts) {
		final Iterator<? extends IColoredString<T>> it = texts.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (first) {
				addTextOnNewLine(it.next());
				first = false;
			} else
				addText(it.next());
		}
	}

	/**
	 * Contrary to {@link Collection#addAll(Collection)}, this method appends
	 * text to the current text, without inserting new lines.
	 *
	 * @param texts the Collection of objects extending IColoredString to append
	 */
	public void addAllText(Collection<? extends IColoredString<T>> texts) {
		for (IColoredString<T> text : texts)
			addText(text);
	}

	/**
	 * Jumps a line.
	 */
	public void addEmptyLine() {
		addTextOnNewLine("");
		addTextOnNewLine("");
	}

	/**
	 * Changes a color in members of {@code this}.
	 * 
	 * @param old The color to replace. Can be {@code null}.
	 */
	public void replaceColor(T old, T new_) {
		final int sz = size();
		for (int i = 0; i < sz; i++)
			get(i).replaceColor(old, new_);
	}

}
