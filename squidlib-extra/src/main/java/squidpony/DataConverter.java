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

package squidpony;

import com.badlogic.gdx.utils.JsonWriter;
import squidpony.store.json.JsonConverter;
/**
 * Augmented version of libGDX's Json class that knows how to handle various data types common in SquidLib.
 * This includes OrderedMap, which notably allows non-String keys (libGDX's default Map serializer requires keys to be
 * Strings), but does not currently allow the IHasher to be set (which only should affect OrderedMaps with array keys).
 * It also makes significantly shorter serialized output for 2D char arrays, GreasedRegion and FakeLanguageGen objects,
 * and various collections (IntDoubleOrderedMap, IntVLA, Arrangement, K2, and K2V1 at least).
 * Created by Tommy Ettinger on 1/9/2017.
 */
public class DataConverter extends JsonConverter {
    /**
     * Creates a new DataConverter using "minimal" output type, so it omits double quotes whenever possible but gives
     * up compatibility with most other JSON readers. Give the constructor
     * {@link JsonWriter.OutputType#json} if you need full compatibility.
     */
    public DataConverter() {
        super();
    }

    /**
     * Creates a new DataConverter with the given OutputType; typically minimal is fine, but compatibility may require
     * you to use json; the javascript type is a sort of middle ground.
     * @param outputType a JsonWriter.OutputType enum value that determines what syntax can be omitted from the output
     */
    public DataConverter(JsonWriter.OutputType outputType) {
        super(outputType);
    }
}
