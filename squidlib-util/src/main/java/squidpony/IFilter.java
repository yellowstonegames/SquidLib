package squidpony;

/**
 * A filter is a function on colors. It is usually used in {@link IColorCenter}
 * to tint all colors.
 * 
 * @author Tommy Ettinger
 * @author smelC
 * @param <T>
 *            The type of colors that this filter outputs.
 * @see IColorCenter
 */
public interface IFilter<T> {

	/**
	 * @param r
	 *            The red component.
	 * @param g
	 *            The green component.
	 * @param b
	 *            The blue component.
	 * @param a
	 *            The alpha component.
	 * @return An alteration of {@code (r,g,b,a)}.
	 */
	T alter(float r, float g, float b, float a);

}
