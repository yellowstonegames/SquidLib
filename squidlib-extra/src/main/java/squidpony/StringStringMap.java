/*
 * Copyright (c) 2022  Eben Howard, Tommy Ettinger, and contributors
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
 *
 *
 */

package squidpony;

import squidpony.squidmath.OrderedMap;

import java.util.Collection;

/**
 * Created by Tommy Ettinger on 1/2/2017.
 */
public class StringStringMap extends OrderedMap<String, String>
{
    public StringStringMap()
    {
        super();
    }
    public StringStringMap(int size, float f)
    {
        super(size, f);
    }
    public StringStringMap(Collection<String> k, Collection<String> v, float f)
    {
        super(k, v, f);
    }
}