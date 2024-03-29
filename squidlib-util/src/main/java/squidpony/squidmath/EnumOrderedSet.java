/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidmath;

/**
 * A simple Set of Enum items (which are already unique if used in a normal Set) that keeps insertion order. By
 * inheriting from {@link OrderedSet}, it gets features not present in normal JDK Sets, such as retrieval of random
 * items with {@link #randomItem(IRNG)}, iteration in insertion order instead of always using enum declaration order
 * (order can be shuffled with {@link #shuffle(IRNG)} or reordered with {@link #reorder(int...)}), and a little more. The
 * implementation is nearly trivial due to how OrderedMap allows customization of hashing strategy with its IHasher
 * option, and this class always uses a specific custom IHasher to hash Enum values by their ordinal. This IHasher is
 * shared with {@link EnumOrderedMap}.
 * <br>
 * Created by Tommy Ettinger on 10/21/2017.
 */
public class EnumOrderedSet<K extends Enum<?>> extends OrderedSet<K> {
    public EnumOrderedSet()
    {
        super(16, 0.9375f, HashCommon.enumHasher);
    }
    public EnumOrderedSet(Class<K> enumClass) {
        super(enumClass.getEnumConstants().length, 0.9375f, HashCommon.enumHasher);
    }
    public EnumOrderedSet(K enumObject) {
        super(enumObject.getClass().getEnumConstants().length, 0.9375f, HashCommon.enumHasher);
    }


    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        int n = size(), i = 0;
        boolean first = true;
        s.append("EnumOrderedSet{");
        while (i < n) {
            if (first) first = false;
            else s.append(", ");
            s.append(getAt(i++));
        }
        s.append("}");
        return s.toString();
    }
}
