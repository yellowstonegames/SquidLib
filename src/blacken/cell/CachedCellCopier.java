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
import java.util.HashMap;
import java.util.Map;

import com.googlecode.blacken.exceptions.CellCopyFailure;

/**
 * This is just like FlexibleCellCopier, except it keeps a persistent cache of
 * the clone methods used for each object (and whether clone was supported).
 *
 * @author Steven Black
 */
public class CachedCellCopier implements GridCellCopier<Object>{
    static private CachedCellCopier instance = null;
    private Map<String, Method> registry = new HashMap<>();
    private CachedCellCopier() {
    }
    static public CachedCellCopier getInstance() {
        if (instance == null) {
            instance = new CachedCellCopier();
        }
        return instance;
    }

    @Override
    public Object copyCell(Object value) {
        if (value == null) {
            return value;
        }
        if (registry.containsKey(value.getClass().getName())) {
            return (Object) copyCell(value);
        }
        if (value.getClass().isPrimitive()) {
            registry.put(value.getClass().getName(), null);
            return value;
        }
        Object ret = null;
        Method cloneMethod;
        try {
            cloneMethod = value.getClass().getMethod("clone");
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            registry.put(value.getClass().getName(), null);
            return value;
        }
        if (cloneMethod != null) {
            try {
                ret = (Object) cloneMethod.invoke(value);
                registry.put(value.getClass().getName(), cloneMethod);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof CloneNotSupportedException) {
                    ret = value;
                    registry.put(value.getClass().getName(), null);
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
