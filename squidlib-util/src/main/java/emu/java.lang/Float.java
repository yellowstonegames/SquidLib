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

public final class Float extends Number implements Comparable<Float> {
    private static final Int8Array wba = Int8ArrayNative.create(4);
    private static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 1);
    private static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 1);

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
}