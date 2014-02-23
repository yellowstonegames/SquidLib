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
 * GridCellCopier implementation that can handle complex and simple types.
 *
 * <p>This uses {@link Util#cloneOrCopy(java.lang.Object)}, so it uses
 * introspection. It's quite flexible in terms of the types it can handle.
 * It handles objects which can both by copied by value (like primitive
 * types) as well as complex objects supporting {@link Object#clone()}.
 *
 * <p>This is currently the default cell copier.
 * @param <Z> the cell type
 */
public class FlexibleCellCopier<Z> implements GridCellCopier<Z> {
    @Override
    public Z copyCell(Z source) {
        return Util.cloneOrCopy(source);
    }
}
