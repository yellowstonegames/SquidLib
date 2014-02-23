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

package com.googlecode.blacken.dungeon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Steven Black
 */
public class SimpleDungeonConfig {

    /**
     * <p>The fall-back method precludes a lot of selective nulls. Either one of
     * the floors needs to be defined as non-null, or the diggable cell needs
     * to be defined. If there are both no floors and no diggable surface, a
     * NullPointerException is thrown as there is no way to dig anything.</p>
     *
     * <p>Note that the order does matter. We don't go recursive.</p>
     *
     * <dl>
     * <dt>floor</dt>
     * <dd>Default floor. Fallback: "room:floor", "hall:floor", <code>null</code></dd>
     * <dt>room:floor</dt>
     * <dd>Room internal floor. Fallback: "floor"</dd>
     * <dt>hall:floor</dt>
     * <dd>Hall/external floor. Fallback: "floor"</dd>
     * <dt>room:door</dt>
     * <dd>Dividers between room and hall floors. Fallback: "room:floor"</dd>
     * <dt>room:door</dt>
     * <dd>Dividers between hall (and hall) floors. Fallback: "hall:floor"</dd>
     * <dt>diggable<dt>
     * <dd>Diggable identifier. Fallback: <code>null</code></dd>
     * <dt>wall</dt>
     * <dd>Default wall. Fallback: "room:wall", "hall:wall", "diggable"</dd>
     * <dt>room:wall</dt>
     * <dd>Default room wall. Fallback: "wall"</dd>
     * <dt>hall:wall</dt>
     * <dd>Default hall wall. Fallback: "wall"</dd>
     * <dt>room:wall:horizontal</dt>
     * <dd>Default horizontal room wall. Fallback: "top", "bottom", "wall"</dd>
     * <dt>room:wall:top</dt>
     * <dd>Top wall. Fallback: "horizontal"</dd>
     * <dt>room:wall:bottom</dt>
     * <dd>Bottom wall. Fallback: "horizontal"</dd>
     * <dt>room:wall:vertical</dt>
     * <dd>Default vertical room wall. Fallback: "right", "left", "wall"</dd>
     * <dt>room:wall:right</dt>
     * <dd>Top wall. Fallback: "vertical"</dd>
     * <dt>room:wall:left</dt>
     * <dd>Bottom wall. Fallback: "vertical"</dd>
     * <dt>room:wall:top-right<dt>
     * <dd>Top-right corner. Fallback: "room:wall:corner", "room:wall:top"</dd>
     * <dt>room:wall:top-left<dt>
     * <dd>Top-left corner. Fallback: "room:wall:corner", "room:wall:top"</dd>
     * <dt>room:wall:bottom-right<dt>
     * <dd>Top-right corner. Fallback: "room:wall:corner", "room:wall:bottom"</dd>
     * <dt>room:wall:bottom-left<dt>
     * <dd>Top-left corner. Fallback: "room:wall:corner", "room:wall:bottom"</dd>
     * <dt>room:wall:corner</dt>
     * <dd>Generic corner. Fallback: "room:wall"</dd>
     * @param <T> type of config entry
     * @param config Mapping object
     */
    public static <T> void cleanConfig(Map<String, T> config) {
        Map<String, T> cfg = (Map<String, T>) config;
        T a = cfg.get("floor");
        T b = cfg.get("room:floor");
        T c = cfg.get("hall:floor");
        if (a == null) {
            if (b != null) {
                cfg.put("floor", b);
                a = b;
            } else if (c != null) {
                cfg.put("floor", c);
                a = c;
            }
        }
        if (b == null) {
            cfg.put("room:floor", a);
        }
        if (c == null) {
            cfg.put("hall:floor", a);
        }

        a = cfg.get("room:door");
        if (a == null) {
            cfg.put("room:door", cfg.get("room:floor"));
        }
        a = cfg.get("hall:door");
        if (a == null) {
            cfg.put("hall:door", cfg.get("hall:floor"));
        }

        if (cfg.get("floor") == null && cfg.get("diggable") == null) {
            throw new NullPointerException("Cannot perform with all nulls.");
        }

        a = cfg.get("wall");
        b = cfg.get("room:wall");
        c = cfg.get("hall:wall");
        if (a == null) {
            if (b != null) {
                cfg.put("wall", b);
                a = b;
            } else if (c != null) {
                cfg.put("wall", c);
                a = c;
            } else {
                a = cfg.get("diggable");
                cfg.put("wall", a);
                cfg.put("room:wall", a);
                cfg.put("hall:wall", a);
                b = c = a;
            }
        }
        if (b == null) {
            cfg.put("room:wall", a);
        }
        if (c == null) {
            cfg.put("hall:wall", a);
        }

        a = cfg.get("room:wall:horizontal");
        b = cfg.get("room:wall:top");
        c = cfg.get("room:wall:bottom");
        if (a == null) {
            if (b != null) {
                cfg.put("room:wall:horizontal", b);
                a = b;
            } else if (c != null) {
                cfg.put("room:wall:horizontal", c);
                a = c;
            } else {
                a = cfg.get("room:wall");
                cfg.put("room:wall:horizontal", a);
                cfg.put("room:wall:top", a);
                cfg.put("room:wall:bottom", a);
                b = c = a;
            }
        }
        if (b == null) {
            cfg.put("room:wall:top", a);
        }
        if (c == null) {
            cfg.put("room:wall:bottom", a);
        }

        a = cfg.get("room:wall:vertical");
        b = cfg.get("room:wall:right");
        c = cfg.get("room:wall:left");
        if (a == null) {
            if (b != null) {
                cfg.put("room:wall:vertical", b);
                a = b;
            } else if (c != null) {
                cfg.put("room:wall:vertical", c);
                a = c;
            } else {
                a = cfg.get("room:wall");
                cfg.put("room:wall:vertical", a);
                cfg.put("room:wall:right", a);
                cfg.put("room:wall:left", a);
                b = c = a;
            }
        }
        if (b == null) {
            cfg.put("room:wall:right", a);
        }
        if (c == null) {
            cfg.put("room:wall:left", a);
        }

        a = cfg.get("room:wall:corner");
        if (a == null) {
            a = cfg.get("room:wall:top");
        }
        b = cfg.get("room:wall:top-right");
        c = cfg.get("room:wall:top-left");
        if (b == null) {
            cfg.put("room:wall:top-right", a);
        }
        if (c == null) {
            cfg.put("room:wall:top-left", a);
        }
        a = cfg.get("room:wall:corner");
        if (a == null) {
            a = cfg.get("room:wall:bottom");
        }
        b = cfg.get("room:wall:bottom-right");
        c = cfg.get("room:wall:bottom-left");
        if (b == null) {
            cfg.put("room:wall:bottom-right", a);
        }
        if (c == null) {
            cfg.put("room:wall:bottom-left", a);
        }
        a = cfg.get("room:wall:corner");
        if (a == null) {
            cfg.put("room:wall:corner", cfg.get("room:wall"));
        }
    }

    public static <T> Set<T> findRoomWalls(Map<String, T> config) {
        Set<T> ret = new HashSet<>();
        T roomWall;
        roomWall = config.get("room:wall:top");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:bottom");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:left");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:right");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:top-right");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:top-left");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:bottom-right");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall:bottom-left");
        if (roomWall != null) {
            ret.add(roomWall);
        }
        roomWall = config.get("room:wall");
        if (roomWall != null) {
            ret.add(roomWall);
        }

        return ret;
    }

    /**
     * Define values in a map by way of a common set of keys in both a key
     * map and a value map.
     *
     * <p>The use-case for this function is creating maps suitable for the
     * {@link SimpleFactoryMap} class while using the names and fuzzy
     * reassignment of the standard config map.
     *
     * <p>In this common use-case, both the <code>keymap</code> and the
     * <code>valmap</code> must have been processed by
     * {@link #cleanConfig(java.util.Map)}.
     *
     * @param <K> key type
     * @param <X> value/key type
     * @param <Y> value/value type
     * @param keymap
     * @param valmap
     * @return Map<X, Y> for Map<K, X> and Map<K, Y>
     */
    public static <K, X, Y> Map<X, Y> commonKeyMap(Map<K, X> keymap, Map<K, Y> valmap) {
        Map<X, Y> ret = new HashMap<>();
        Set<K> realKeys = new HashSet<>(keymap.keySet());
        realKeys.retainAll(valmap.keySet());
        for (K key : realKeys) {
            ret.put(keymap.get(key), valmap.get(key));
        }
        return ret;
    }
}
