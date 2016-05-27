package squidpony;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import squidpony.panel.IColoredString;

/**
 * An helper class for code that deals with lists of {@link IColoredString}s. It
 * does nothing smart, its only purpose is to save you some typing for frequent
 * calls. It is particularly useful when feeding large pieces of text to classes
 * like {@link TextPanel}.
 * 
 * @author smelC
 */
public class ColoredStringList<T> extends ArrayList<IColoredString<T>> {

	private static final long serialVersionUID = -5111205714079762803L;

	public ColoredStringList() {
		super();
	}

	/**
	 * Appends {@code text} to {@code this}, without specifying its color.
	 * 
	 * @param text
	 */
	public void addText(String text) {
		addColoredText(text, null);
	}

	/**
	 * Appends {@code text} to {@code this}.
	 * 
	 * @param text
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
	 * @param text
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
	 * @param text
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
	 * @param text
	 */
	public void addColoredTextOnNewLine(String text, /* @Nullable */ T color) {
		this.add(IColoredString.Impl.<T> create(text, color));
	}

	/**
	 * Adds {@code texts} to {@code this}, starting a new line for the first
	 * one.
	 * 
	 * @param texts
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
	 * @param texts
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

}
