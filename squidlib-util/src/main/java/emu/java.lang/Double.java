/*
 * Copyright 2007 Google Inc.
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

import static javaemul.internal.InternalPreconditions.checkNotNull;

import javaemul.internal.JsUtils;

import jsinterop.annotations.JsMethod;

import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;


/**
 * Wraps a primitive <code>double</code> as an object.
 */
public final class Double extends Number implements Comparable<Double> {
    public static final double MAX_VALUE = 1.7976931348623157e+308;
    public static final double MIN_VALUE = 4.9e-324;
    public static final double MIN_NORMAL = 2.2250738585072014e-308;
    public static final int MAX_EXPONENT = 1023;
    // ==Math.getExponent(Double.MAX_VALUE);
    public static final int MIN_EXPONENT = -1022;
    // ==Math.getExponent(Double.MIN_NORMAL);

    public static final double NaN = 0d / 0d;
    public static final double NEGATIVE_INFINITY = -1d / 0d;
    public static final double POSITIVE_INFINITY = 1d / 0d;
    public static final int SIZE = 64;
    public static final int BYTES = SIZE / Byte.SIZE;
    public static final Class<Double> TYPE = double.class;

    private static final Int8Array wba = Int8ArrayNative.create(8);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float64Array wfa = Float64ArrayNative.create(wba.buffer(), 0, 1);

    public static int compare(double x, double y) {
        if (x < y) {
            return -1;
        }
        if (x > y) {
            return 1;
        }
        if (x == y) {
            return 0;
        }

        if (isNaN(x)) {
            if (isNaN(y)) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }

    public static long doubleToLongBits(double value) {
        wfa.set(0, value);
        return ((long)wia.get(0)) << 32 | (wia.get(1) & 0xffffffffL);
    }
    public static long doubleToRawLongBits(double value) {
        wfa.set(0, value);
        return ((long)wia.get(0)) << 32 | (wia.get(1) & 0xffffffffL);
    }

    /**
     * Not as bad as it was before!
     * @param f a float to hash
     * @return hash code for this (not truncated; converted to long using {@link #doubleToRawLongBits(float)}, sorta)
     */
    public static int hashCode(double d) {
        wfa.set(0, d);
        return (wia.get(0) ^ wia.get(1)) + wba.get(0);
    }

    public static boolean isFinite(double x) {
        return JsUtils.isFinite(x);
    }

    public static boolean isInfinite(double x) {
        return !isNaN(x) && !isFinite(x);
    }

    public static boolean isNaN(double x) {
        return JsUtils.isNaN(x);
    }
    public static double longBitsToDouble(long bits) {
        wia.set(0, (int)(bits >>> 32));
        wia.set(1, (int)(bits & 0xffffffffL));
        return wfa.get(0);
    }

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static double parseDouble(String s) throws NumberFormatException {
        return __parseAndValidateDouble(s);
    }

    public static double sum(double a, double b) {
        return a + b;
    }

    public static String toString(double b) {
        return String.valueOf(b);
    }

    public static Double valueOf(double d) {
        return new Double(d);
    }

    public static Double valueOf(String s) throws NumberFormatException {
        return new Double(s);
    }

    public Double(double value) {
    /*
     * Call to $create(value) must be here so that the method is referenced and not
     * pruned before new Double(value) is replaced by $create(value) by
     * RewriteConstructorCallsForUnboxedTypes.
     */
        $create(value);
    }

    public Double(String s) {
    /*
     * Call to $create(value) must be here so that the method is referenced and not
     * pruned before new Double(value) is replaced by $create(value) by
     * RewriteConstructorCallsForUnboxedTypes.
     */
        $create(s);
    }

    @Override
    public byte byteValue() {
        return (byte) doubleValue();
    }

    @Override
    public int compareTo(Double b) {
        return compare(doubleValue(), b.doubleValue());
    }

    @Override
    public double doubleValue() {
        return JsUtils.unsafeCastToDouble(checkNotNull(this));
    }

    @Override
    public boolean equals(Object o) {
        return checkNotNull(this) == o;
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    /**
     * Performance caution: using Double objects as map keys is not recommended.
     * Using double values as keys is generally a bad idea due to difficulty
     * determining exact equality. In addition, there is no efficient JavaScript
     * equivalent of <code>doubleToIntBits</code>. As a result, this method
     * computes a hash code by truncating the whole number portion of the double,
     * which may lead to poor performance for certain value sets if Doubles are
     * used as keys in a {@link java.util.HashMap}.
     */
    @Override
    public int hashCode() {
        return hashCode(doubleValue());
    }

    @Override
    public int intValue() {
        return (int) doubleValue();
    }

    public boolean isInfinite() {
        return isInfinite(doubleValue());
    }

    public boolean isNaN() {
        return isNaN(doubleValue());
    }

    @Override
    public long longValue() {
        return (long) doubleValue();
    }

    @Override
    public short shortValue() {
        return (short) doubleValue();
    }

    @Override
    public String toString() {
        return toString(doubleValue());
    }

    protected static Double $create(double x) {
        return createNative(x);
    }

    protected static Double $create(String s) {
        return createNative(Double.parseDouble(s));
    }

    private static native Double createNative(double x) /*-{
    return x;
  }-*/;

    @JsMethod
    protected static boolean $isInstance(Object instance) {
        return "number".equals(JsUtils.typeOf(instance));
    }
}
