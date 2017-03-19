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

public final class NumberUtils {
    public static int floatToIntBits (float value) {
        return Float.floatToIntBits(value);
    }

    public static int floatToRawIntBits (float value) {
        return Float.floatToIntBits(value);
    }

    public static int floatToIntColor (float value) {
        return Float.floatToIntBits(value);
    }

    /**
     * Get an index from the exponent part of the int {@code bits} representing a float with
     * {@code ((bits >> 24 & 0x7e) | (bits >> 23 & 1))}
     */
    private static final float[] multipliers = {
            0x0.8p-126f, 0x1.0p-126f, 0x1.0p-123f, 0x1.0p-122f, 0x1.0p-119f, 0x1.0p-118f, 0x1.0p-115f, 0x1.0p-114f,
            0x1.0p-111f, 0x1.0p-110f, 0x1.0p-107f, 0x1.0p-106f, 0x1.0p-103f, 0x1.0p-102f, 0x1.0p-99f, 0x1.0p-98f,
            0x1.0p-95f, 0x1.0p-94f, 0x1.0p-91f, 0x1.0p-90f, 0x1.0p-87f, 0x1.0p-86f, 0x1.0p-83f, 0x1.0p-82f,
            0x1.0p-79f, 0x1.0p-78f, 0x1.0p-75f, 0x1.0p-74f, 0x1.0p-71f, 0x1.0p-70f, 0x1.0p-67f, 0x1.0p-66f,
            0x1.0p-63f, 0x1.0p-62f, 0x1.0p-59f, 0x1.0p-58f, 0x1.0p-55f, 0x1.0p-54f, 0x1.0p-51f, 0x1.0p-50f,
            0x1.0p-47f, 0x1.0p-46f, 0x1.0p-43f, 0x1.0p-42f, 0x1.0p-39f, 0x1.0p-38f, 0x1.0p-35f, 0x1.0p-34f,
            0x1.0p-31f, 0x1.0p-30f, 0x1.0p-27f, 0x1.0p-26f, 0x1.0p-23f, 0x1.0p-22f, 0x1.0p-19f, 0x1.0p-18f,
            0x1.0p-15f, 0x1.0p-14f, 0x1.0p-11f, 0x1.0p-10f, 0x1.0p-7f, 0x1.0p-6f, 0x1.0p-3f, 0x1.0p-2f,
            0x1.0p1f, 0x1.0p2f, 0x1.0p5f, 0x1.0p6f, 0x1.0p9f, 0x1.0p10f, 0x1.0p13f, 0x1.0p14f,
            0x1.0p17f, 0x1.0p18f, 0x1.0p21f, 0x1.0p22f, 0x1.0p25f, 0x1.0p26f, 0x1.0p29f, 0x1.0p30f,
            0x1.0p33f, 0x1.0p34f, 0x1.0p37f, 0x1.0p38f, 0x1.0p41f, 0x1.0p42f, 0x1.0p45f, 0x1.0p46f,
            0x1.0p49f, 0x1.0p50f, 0x1.0p53f, 0x1.0p54f, 0x1.0p57f, 0x1.0p58f, 0x1.0p61f, 0x1.0p62f,
            0x1.0p65f, 0x1.0p66f, 0x1.0p69f, 0x1.0p70f, 0x1.0p73f, 0x1.0p74f, 0x1.0p77f, 0x1.0p78f,
            0x1.0p81f, 0x1.0p82f, 0x1.0p85f, 0x1.0p86f, 0x1.0p89f, 0x1.0p90f, 0x1.0p93f, 0x1.0p94f,
            0x1.0p97f, 0x1.0p98f, 0x1.0p101f, 0x1.0p102f, 0x1.0p105f, 0x1.0p106f, 0x1.0p109f, 0x1.0p110f,
            0x1.0p113f, 0x1.0p114f, 0x1.0p117f, 0x1.0p118f, 0x1.0p121f, 0x1.0p122f, 0x1.0p125f, 0x1.0p126f};

    public static float intToFloatColor(final int bits)
    {
        // This avoids using bits in the NaN range (the bit at mask 0x01000000 is always 0).
        // See Float.intBitsToFloat javadocs.
        // This unfortunately means we don't get the full range of alpha.
        return (1 - (bits >>> 30 & 2)) * (1f + 0x0.000002p0f * (bits & 0x7fffff)) *
                multipliers[(bits >> 24 & 0x7e) | (bits >> 23 & 1)];
    }

    public static float intBitsToFloat (int value) {
        return Float.intBitsToFloat(value);
    }

    public static long doubleToLongBits (double value) {
        return Double.doubleToLongBits(value);
    }

    public static double longBitsToDouble (long value) {
        return Double.longBitsToDouble(value);
    }
}