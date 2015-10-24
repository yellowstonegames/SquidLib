package squidpony.panel;

import squidpony.annotation.Beta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A {@link String} divided in chunks of different colors. Use the
 * {@link Iterable} interface to get the pieces.
 * 
 * @author smelC
 * 
 * @param <T>
 *            The type of colors;
 */
@Beta
public interface IColoredString<T> extends Iterable<IColoredString.Bucket<T>> {

	/**
	 * Mutates {@code this} by appending {@code c} to it.
	 * 
	 * @param c
	 *            The text to append.
	 * @param color
	 *            {@code text}'s color. Or {@code null} to let the panel decide.
	 */
	void append(char c, /* @Nullable */T color);

	/**
	 * Mutates {@code this} by appending {@code text} to it. Does nothing if
	 * {@code text} is {@code null}.
	 * 
	 * @param text
	 *            The text to append.
	 * @param color
	 *            {@code text}'s color. Or {@code null} to let the panel decide.
	 */
	void append(/* @Nullable */String text, /* @Nullable */T color);

	/**
	 * Mutates {@code this} by appending {@code i} to it.
	 * 
	 * @param i
	 *            The int to append.
	 * @param color
	 *            {@code text}'s color. Or {@code null} to let the panel decide.
	 */
	void appendInt(int i, /* @Nullable */T color);

	/**
	 * Mutates {@code this} by appending {@code other} to it.
	 * 
	 * @param other
	 */
	void append(IColoredString<T> other);

	/**
	 * Deletes all content after index {@code len} (if any).
	 * 
	 * @param len
	 */
	void setLength(int len);

	/**
	 * @return The length of text.
	 */
	int length();

	/**
	 * @return The text that {@code this} represents.
	 */
	String present();

	/**
	 * A basic implementation of {@link IColoredString}.
	 * 
	 * @author smelC
	 * 
	 * @param <T>
	 *            The type of colors
	 */
	class Impl<T> implements IColoredString<T> {

		protected final LinkedList<Bucket<T>> fragments;

		/**
		 * An empty instance.
		 */
		public Impl() {
			fragments = new LinkedList<Bucket<T>>();
		}

		/**
		 * An instance initially containing {@code text} (with {@code color}).
		 * 
		 * @param text
		 *            The text that {@code this} should contain.
		 * @param color
		 *            The color of {@code text}.
		 */
		public Impl(String text, /* @Nullable */T color) {
			this();

			append(text, color);
		}

		@Override
		public void append(char c, T color) {
			append(String.valueOf(c), color);
		}

		@Override
		public void append(String text, T color) {
			if (text == null || text.isEmpty())
				return;

			if (fragments.isEmpty())
				fragments.add(new Bucket<T>(text, color));
			else {
				final Bucket<T> last = fragments.getLast();
				if (equals(last.color, color)) {
					/* Append to the last bucket, to avoid extending the list */
					final Bucket<T> novel = last.append(text);
					fragments.removeLast();
					fragments.addLast(novel);
				} else
					fragments.add(new Bucket<T>(text, color));
			}
		}

		@Override
		public void appendInt(int i, T color) {
			append(String.valueOf(i), color);
		}

		@Override
		/* KISS implementation */
		public void append(IColoredString<T> other) {
			for (IColoredString.Bucket<T> ofragment : other)
				append(ofragment.getText(), ofragment.getColor());
		}

		@Override
		public void setLength(int len) {
			int l = 0;
			final ListIterator<IColoredString.Bucket<T>> it = fragments.listIterator();
			while (it.hasNext()) {
				final IColoredString.Bucket<T> next = it.next();
				final String ftext = next.text;
				final int flen = ftext.length();
				final int nextl = l + flen;
				if (nextl < len)
					/* Nothing to do */
					continue;
				else if (nextl == len) {
					/* Delete all next fragments */
					while (it.hasNext())
						it.remove();
					/* We'll exit the outer loop right away */
				} else {
					assert len < nextl;
					/* Trim this fragment */
					final IColoredString.Bucket<T> trimmed = next.setLength(nextl - l);
					/* Replace this fragment */
					it.remove();
					it.add(trimmed);
					/* Delete all next fragments */
					while (it.hasNext())
						it.remove();
					/* We'll exit the outer loop right away */
				}
			}

		}

		@Override
		public int length() {
			int result = 0;
			for (Bucket<T> fragment : fragments)
				result += fragment.getText().length();
			return result;
		}

		@Override
		public String present() {
			final StringBuilder result = new StringBuilder();
			for (Bucket<T> fragment : fragments)
				result.append(fragment.text);
			return result.toString();
		}

		@Override
		public Iterator<Bucket<T>> iterator() {
			return fragments.iterator();
		}

		@Override
		public String toString() {
			return present();
		}

		protected static boolean equals(Object o1, Object o2) {
			if (o1 == null)
				return o2 == null;
			else
				return o1.equals(o2);
		}
	}

	/**
	 * A piece of a {@link IColoredString}: a text and its color.
	 * 
	 * @author smelC
	 * 
	 * @param <T>
	 *            The type of colors;
	 */
	class Bucket<T> {

		protected final String text;
		protected final/* @Nullable */T color;

		public Bucket(String text, /* @Nullable */T color) {
			this.text = text == null ? "" : text;
			this.color = color;
		}

		/**
		 * @param text
		 * @return An instance whose text is {@code this.text + text}. Color is
		 *         unchanged.
		 */
		public Bucket<T> append(String text) {
			if (text == null || text.isEmpty())
				/* Let's save an allocation */
				return this;
			else
				return new Bucket<T>(this.text + text, color);
		}

		public Bucket<T> setLength(int l) {
			final int here = text.length();
			if (here < l)
				return this;
			else
				return new Bucket<T>(text.substring(0, l), color);
		}

		/**
		 * @return The text that this bucket contains.
		 */
		public String getText() {
			return text;
		}

		/**
		 * @return The color of {@link #getText()}. Or {@code null} if none.
		 */
		public/* @Nullable */T getColor() {
			return color;
		}

		@Override
		public String toString() {
			if (color == null)
				return text;
			else
				return String.format("%s (%s)", text, color);
		}

	}
}
