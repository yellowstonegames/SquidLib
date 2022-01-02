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

package squidpony.squidgrid.mapping.styled;

/**
 * The outermost class in the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Tileset {
    public Config config;
    public Maximums max_tiles;
    public Tile[] h_tiles, v_tiles;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public Tileset() {
        config = new Config();
        max_tiles = new Maximums();
    }
}