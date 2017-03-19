/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.lang;

import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;

/**
 * Wraps a primitive <code>float</code> as an object.
 */
public final class Float extends Number implements Comparable<Float> {
    public static final float MAX_VALUE = 3.4028235e+38f;
    public static final float MIN_VALUE = 1.4e-45f;
    public static final int MAX_EXPONENT = 127;
    public static final int MIN_EXPONENT = -126;
    public static final float MIN_NORMAL = 1.1754943508222875E-38f;
    public static final float NaN = 0f / 0f;
    public static final float NEGATIVE_INFINITY = -1f / 0f;
    public static final float POSITIVE_INFINITY = 1f / 0f;
    public static final int SIZE = 32;
    public static final int BYTES = SIZE / Byte.SIZE;
    public static final Class<Float> TYPE = float.class;

    private static final Int8Array wba = Int8ArrayNative.create(4);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 1);
    private static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 1);

    public static int compare(float x, float y) {
        return Double.compare(x, y);
    }

    public static int floatToIntBits(float value) {
        wfa.set(0, value);
        return wia.get(0);
    }
    public static int floatToRawIntBits(float value) {
        wfa.set(0, value);
        return wia.get(0);
    }

    /**
     * Not as bad as it was before!
     * @param f a float to hash
     * @return hash value of float (not truncated; converted to int using a modified {@link #floatToRawIntBits(float)})
     */
    public static int hashCode(float f) {
        wfa.set(0, f);
        return wia.get(0) + wba.get(0);
    }

    public static float intBitsToFloat(int bits) {
        wia.set(0, bits);
        return wfa.get(0);
    }

    public static boolean isFinite(float x) {
        return Double.isFinite(x);
    }

    public static boolean isInfinite(float x) {
        return Double.isInfinite(x);
    }

    public static boolean isNaN(float x) {
        return Double.isNaN(x);
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static float parseFloat(String s) throws NumberFormatException {
        double doubleValue = __parseAndValidateDouble(s);
        if (doubleValue > Float.MAX_VALUE) {
            return Float.POSITIVE_INFINITY;
        } else if (doubleValue < -Float.MAX_VALUE) {
            return Float.NEGATIVE_INFINITY;
        }
        return (float) doubleValue;
    }

    public static float sum(float a, float b) {
        return a + b;
    }

    public static String toString(float b) {
        return String.valueOf(b);
    }

    public static Float valueOf(float f) {
        return new Float(f);
    }

    public static Float valueOf(String s) throws NumberFormatException {
        return new Float(s);
    }

    private final transient float value;

    public Float(double value) {
        this.value = (float) value;
    }

    public Float(float value) {
        this.value = value;
    }

    public Float(String s) {
        value = parseFloat(s);
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    @Override
    public int compareTo(Float b) {
        return compare(value, b.value);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Float) && (((Float) o).value == value);
    }

    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Not as bad as it was before!
     * @return hash value of this (not truncated; converted to int using a modified {@link #floatToRawIntBits(float)})
     */
    public int hashCode() {
        return hashCode(value);
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    public boolean isInfinite() {
        return isInfinite(value);
    }

    public boolean isNaN() {
        return isNaN(value);
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public String toString() {
        return toString(value);
    }

}
