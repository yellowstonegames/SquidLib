/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.googlecode.blacken.cell;

/**
 * GridCellCopier implementation that is suitable for primitive types.
 *
 * <p>This is a simple implementation that copies by value. That is,
 * the input is also the output. If this is usable, it is guaranteed to
 * be faster than the {@link FlexibleCellCopier}
 * @param <Z> the cell type
 */
public class PrimitiveCellCopier<Z> implements GridCellCopier<Z> {
    @Override
    public Z copyCell(Z source) {
        return source;
    }
}
