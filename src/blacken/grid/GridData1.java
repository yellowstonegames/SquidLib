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

package com.googlecode.blacken.grid;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal data structure and serialization format for Grid<Z>.
 *
 * @author yam655
 */
final class GridData1<Z> implements Serializable {
    private static final long serialVersionUID = 1212L;

    /**
     * Perform an upgrade from the test GridData0 format.
     *
     * @param map
     * @return
     */
    static Map<String, Object> upgrade(Map<String, Object> map) {

        int start_x = (int) map.remove("x1");
        map.put("start_x", start_x);
        int start_y = (int) map.remove("y1");
        map.put("start_y", start_y);
        ArrayList<ArrayList> grid = (ArrayList<ArrayList>) map.get("grid");
        int size_y = grid.size();
        int size_x = 0;
        if (size_y > 0) {
            size_x = grid.get(0).size();
        }
        map.put("size_y", size_y);
        map.put("size_x", size_x);
        map.put("irregular", false);
        // map = GridData2.upgrade(map);
        return map;
    }

    // They're private, really, but I didn't like the complaints that they weren't used.

    ArrayList<ArrayList<Z>> grid = null;
    int start_x = 0;
    int start_y = 0;
    int size_x = 0;
    int size_y = 0;
    Z empty = null;
    boolean irregular = false;

    public GridData1() {
        // for serialization
    }

    public void set(String name, Object value) {
        try {
            Class myClass = this.getClass();
            Field field = myClass.getDeclaredField(name);
            field.set(this, value);
        } catch (IllegalAccessException | IllegalArgumentException | 
                 NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(String.format("failed to set field '%s'", name), ex);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> ret = new HashMap<>();
        Class myClass = this.getClass();
        Field[] fields = myClass.getDeclaredFields();
        for (Field f : fields) {
            try {
                ret.put(f.getName(), f.get(this));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                // should never happen.
                return null;
            }
        }
        ret.put("__target__", Grid.class.getName());
        ret.put("__version__", 1);
        return ret;
    }

    public Object readResolve() throws ObjectStreamException {
        Map<String, Object> map = toMap();
        // map = GridData2.upgrade(map);
        Grid g = new Grid(map);
        return g;
    }

}
