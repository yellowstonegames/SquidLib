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

/**
 * Internally used; see {@link squidpony.squidgrid.mapping.styled.TilesetType} members for the actual documentation.
 * <br>
 * It is currently pretty much impossible to create tilesets beyond these, since these were ported from a public domain C
 * library that, unfortunately, does not document how its tilesets were created or how to make more.
 * <br>
 * http://nothings.org/gamedev/herringbone/
 * <br>
 * http://nothings.org/gamedev/herringbone/herringbone_src.html (the tileset PNGs are not used directly; they were
 * previously converted to JSON and then to this Java source)
 */
package squidpony.tileset;
