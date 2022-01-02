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

package squidpony.squidgrid.zone;

/**
 * Created by Tommy Ettinger on 11/24/2016.
 */
public interface MutableZone extends Zone {

    /**
     * Expands this Zone in the four cardinal directions, performing the expansion consecutively {@code distance} times.
     * Modified this Zone in-place and returns it for chaining.
     * @param distance the amount to expand outward using Manhattan distance (diamond shape)
     * @return this for chaining, after being modified in-place
     */
    Zone expand(int distance);

    /**
     * Expands this Zone in the four cardinal and four diagonal directions, performing the expansion consecutively
     * {@code distance} times. Modified this Zone in-place and returns it for chaining.
     * @param distance the amount to expand outward using Chebyshev distance (square shape)
     * @return this for chaining, after being modified in-place
     */
    Zone expand8way(int distance);
}
