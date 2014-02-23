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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Steven Black
 */
public class Util {
    private Util() {
        // this should never be directly instantiated.
    }

    /**
     * Copy a value somehow.
     *
     * <p>If possible, we clone the cell. If that isn't available we copy by
     * value.</p>
     *
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <Z> Z cloneOrCopy(Z value) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isPrimitive()) {
            return value;
        }
        Z ret = null;
        Method cloneMethod;
        try {
            cloneMethod = value.getClass().getMethod("clone");
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            return value;
        }
        if (cloneMethod != null) {
            try {
                ret = (Z) cloneMethod.invoke(value);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof CloneNotSupportedException) {
                    ret = value;
                } else {
                    throw new RuntimeException(e);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // IllegalArgumentException should never happen: Shouldn't take arguments
                // IllegalAccessException should never happen: Should have already occured
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Copy a value somehow.
     *
     * <p>Clone any object supporting the {@link Object#clone()} interface.
     *
     * <p>Unfortunately, this can't avoid introspection. {@link Cloneable} is
     * a marker interface, and it only has meaning if the object has a public
     * implementation of {@link Object#clone()}. Even then -- because of
     * inheritance -- a class may still not support the call.
     *
     * @param <Z> some type supporting the {@link Object#clone()} call
     * @param value
     * @throws IllegalArgumentException clone is not supported on this type
     * @throws NullPointerException <code>value</code> was null
     * @throws RuntimeException Other clone-related exception occurred.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <Z extends Cloneable> Z cloneIt(Z value) {
        if (value == null) {
            throw new NullPointerException("cannot be null");
        }
        if (value.getClass().isPrimitive()) {
            throw new IllegalArgumentException("cannot clone primitive types");
        }
        Z ret = null;
        Method cloneMethod;
        try {
            cloneMethod = value.getClass().getMethod("clone");
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("has no clone method", e);
        }
        if (cloneMethod != null) {
            try {
                ret = (Z) cloneMethod.invoke(value);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof CloneNotSupportedException) {
                    throw new IllegalArgumentException("clone not supported", e);
                } else {
                    throw new RuntimeException(e);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // IllegalArgumentException should never happen: Shouldn't take arguments
                // IllegalAccessException should never happen: Should have already occured
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

}
