/*******************************************************************************
 * Copyright 2017 See libGDX AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;
import squidpony.squidmath.NumberTools;

public final class NumberUtils {

    public static int floatToIntBits (float value) {
        return NumberTools.floatToIntBits(value);
    }

    public static int floatToRawIntBits (float value) {
        return NumberTools.floatToIntBits(value);
    }

    public static int floatToIntColor (float value) {
        return NumberTools.floatToIntBits(value);
    }

    public static float intToFloatColor (int value) {
        // This mask avoids using bits in the NaN range. See Float.intBitsToFloat javadocs.
        // This unfortunately means we don't get the full range of alpha.
        return NumberTools.intBitsToFloat(value & 0xfeffffff);
    }

    public static float intBitsToFloat (int value) {
        return NumberTools.intBitsToFloat(value);
    }

    /**
     * Changed from the original NumberUtils version, which was not yet implemented.
     * @param value any double
     * @return the bits that represent that double as a long
     */
    public static long doubleToLongBits (double value) {
        return NumberTools.doubleToLongBits(value);
    }

    /**
     * Changed from the original NumberUtils version, which was not yet implemented.
     * @param value any long, but certain input ranges (those that match NaN) may be condensed
     * @return the double that the given long bits represent
     */
    public static double longBitsToDouble (long value) {
        return NumberTools.longBitsToDouble(value);
    }
}