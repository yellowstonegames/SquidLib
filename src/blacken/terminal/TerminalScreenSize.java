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

package com.googlecode.blacken.terminal;

/**
 *
 * @author Steven Black
 */
public enum TerminalScreenSize {
    SIZE_TINY(0.3F),
    SIZE_SMALL(0.5F),
    SIZE_MEDIUM(0.7F),
    SIZE_LARGE(0.9F),
    SIZE_MAX(1.0F),
    SIZE_FULLSCREEN(-1.0F);
    private float size = 0;
    /**
     * Specify a screen size. The size parameter is multiplied by both the
     * dimensions of the screen.
     * @param size
     */
    TerminalScreenSize(float size) {
        this.size = size;
    }
    public float getSize() {
        return this.size;
    }
}
