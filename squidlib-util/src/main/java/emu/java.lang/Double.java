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

import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;

public final class Double extends Number implements Comparable<Double> {
    private static final Int8Array wba = Int8ArrayNative.create(8);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
    private static final Float64Array wfa = Float64ArrayNative.create(wba.buffer(), 0, 1);

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

    public static double longBitsToDouble(long bits) {
        wia.set(0, (int)(bits >>> 32));
        wia.set(1, (int)(bits & 0xffffffffL));
        return wfa.get(0);
    }
}}