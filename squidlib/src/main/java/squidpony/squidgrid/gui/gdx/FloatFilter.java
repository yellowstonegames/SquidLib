/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
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
 */

package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.IFilter;

/**
 * Like {@link IFilter}, but produces packed floats that encode colors instead of {@link Color} objects.
 * Accepts packed float colors (as produced by {@link Color#toFloatBits()}, or given in SColor documentation) or Color
 * objects (including {@link SColor} instances).
 * <br>
 * Created by Tommy Ettinger on 7/22/2018.
 */
public abstract class FloatFilter {
    /**
     * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
     * @param color a packed float color, as produced by {@link Color#toFloatBits()}
     * @return a packed float color, as produced by {@link Color#toFloatBits()}
     */
    public abstract float alter(float color);
    /**
     * Takes a {@link Color} or subclass of Color (such as {@link SColor}, which is a little more efficient here) and
     * produces a packed float color that this FloatFilter edited.
     * @param color a {@link Color} or instance of a subclass such as {@link SColor} 
     * @return a packed float color, as produced by {@link Color#toFloatBits()}
     */
    public float alter(Color color)
    {
        return alter(color.toFloatBits());
    }
}
