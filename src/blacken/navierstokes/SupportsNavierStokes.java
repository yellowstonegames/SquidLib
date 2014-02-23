/* blacken - a library for Roguelike games
 * Copyright © 2011 Steven Black <yam655@gmail.com>
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
package com.googlecode.blacken.navierstokes;

/**
 * @author yam655
 *
 */
public interface SupportsNavierStokes {
    /**
     * Get a Navier-Stokes value
     * @param idx index for the particular value
     * @param layer which layer will this be for?
     * @return value
     */
    public float getNavierStokes(int idx, int layer);
    /**
     * Set a Navier-Stokes value
     * @param idx index for the particular value
     * @param layer which layer will this be for?
     * @param value value to use
     */
    public void setNavierStokes(int idx, int layer, float value);
}
