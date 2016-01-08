package squidpony.panel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import squidpony.annotation.Beta;

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
	 * @return {@code true} if {@link #present()} is {@code ""}.
	 */
	public boolean isEmpty();

	/**
	 * @param width
	 *            A positive integer
	 * @return {@code this} split in pieces that would fit in a display with
	 *         {@code width} columns (if all words in {@code this} are smaller
	 *         or equal in length to {@code width}, otherwise wrapping will fail
	 *         for these words).
	 */
	List<IColoredString<T>> wrap(int width);

	/**
	 * Empties {@code this}.
	 */
	void clear();

	/**
	 * @param index
	 * @return The color at {@code index}, if any.
	 * @throws NoSuchElementException
	 *             If {@code index} equals or is greater to {@link #length()}.
	 */
	public /* @Nullable */ T colorAt(int index);

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

		/**
		 * A static constructor, to avoid having to write {@code <T>} in the
		 * caller.
		 * 
		 * @return {@code new Impl(s, t)}.
		 */
		public static <T> IColoredString.Impl<T> create() {
			return new IColoredString.Impl<T>("", null);
		}

		/**
		 * A static constructor, to avoid having to write {@code <T>} in the
		 * caller.
		 * 
		 * @return {@code new Impl(s, t)}.
		 */
		public static <T> IColoredString.Impl<T> create(String s, /* @Nullable */ T t) {
			return new IColoredString.Impl<T>(s, t);
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
				if (nextl < len) {
					/* Nothing to do */
					l += flen;
					continue;
				} else if (nextl == len) {
					/* Delete all next fragments */
					while (it.hasNext()) {
						it.next();
						it.remove();
					}
					/* We'll exit the outer loop right away */
				} else {
					assert len < nextl;
					/* Trim this fragment */
					final IColoredString.Bucket<T> trimmed = next.setLength(len - l);
					/* Replace this fragment */
					it.remove();
					it.add(trimmed);
					/* Delete all next fragments */
					while (it.hasNext()) {
						it.next();
						it.remove();
					}
					/* We'll exit the outer loop right away */
				}
			}
		}

		@Override
		public List<IColoredString<T>> wrap(int width) {
			if (width == 0) {
				/* Really, you should not rely on this behavior */
				System.err.println("Cannot wrap string in empty display");
				final List<IColoredString<T>> result = new LinkedList<IColoredString<T>>();
				result.add(this);
				return result;
			}

			final List<IColoredString<T>> result = new ArrayList<IColoredString<T>>();

			IColoredString<T> current = create();
			int curlen = 0;
			final Iterator<Bucket<T>> it = iterator();
			while (it.hasNext()) {
				final Bucket<T> next = it.next();
				final String bucket = next.getText();
				final String[] split = bucket.split(" ");
				final T color = next.color;
				for (int i = 0; i < split.length; i++) {
					final String chunk = split[i];
					final int chunklen = chunk.length();
					if (curlen + chunklen + (0 < curlen ? 1 : 0) <= width) {
						if (0 < curlen) {
							/*
							 * Do not forget space on which chunk got split. If
							 * the space is offscreen, it's harmless, hence not
							 * checking it.
							 */
							current.append(' ', null);
							curlen++;
						}

						/* Can add it */
						current.append(chunk, color);
						/* Extend size */
						curlen += chunklen;
					} else {
						/* Need to wrap */
						/* Flush content so far */
						if (!current.isEmpty())
							result.add(current);
						/*
						 * else: line was prepared, but did not contain anything
						 */
						if (chunklen <= width) {
							curlen = chunklen;
							current = create();
							current.append(chunk, color);
							/* Reinit size */
							curlen = chunklen;
						} else {
							/*
							 * This word is too long. Adding it and preparing a
							 * new line immediately.
							 */
							/* Add */
							result.add(new Impl<T>(chunk, color));
							/* Prepare for next rolls */
							current = create();
							/* Reinit size */
							curlen = 0;
						}
					}
				}
			}

			if (!current.isEmpty()) {
				/* Flush rest */
				result.add(current);
			}

			return result;
		}

		@Override
		public void clear() {
			fragments.clear();
		}

		@Override
		public int length() {
			int result = 0;
			for (Bucket<T> fragment : fragments)
				result += fragment.getText().length();
			return result;
		}

		@Override
		/* This implementation is resilient to empty buckets */
		public boolean isEmpty() {
			for (Bucket<?> bucket : fragments) {
				if (bucket.text == null || bucket.text.isEmpty())
					continue;
				else
					return false;
			}
			return true;
		}

		@Override
		public T colorAt(int index) {
			final ListIterator<IColoredString.Bucket<T>> it = fragments.listIterator();
			int now = 0;
			while (it.hasNext()) {
				final IColoredString.Bucket<T> next = it.next();
				final String ftext = next.text;
				final int flen = ftext.length();
				final int nextl = now + flen;
				if (index <= nextl)
					return next.color;
				now += flen;
			}
			throw new NoSuchElementException("Character at index " + index + " in " + this);
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
			if (here <= l)
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
				return text + "(" + color + ")";
		}

	}
}
