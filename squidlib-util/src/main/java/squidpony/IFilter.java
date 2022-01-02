/*
 * Copyright (c) 2022  Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

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
