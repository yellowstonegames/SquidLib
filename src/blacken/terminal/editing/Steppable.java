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

package com.googlecode.blacken.terminal.editing;

/**
 * Perform a step to complete an action.
 *
 * @author Steven Black
 */
public interface Steppable  {
    /**
     * Get the number of steps, if known, or -1 if unknown.
     *
     * @return number of steps, or -1 if unknown
     */
    public int getStepCount();
    /**
     * Get the current step.
     * 
     * @return current step
     */
    public int getCurrentStep();
    /**
     * Perform a step in an operation or perform one loop in an operation that
     * will require more than one.
     */
    public void step();

    /**
     * Are we complete?
     *
     * @return false if more steps; true if complete
     */
    public boolean isComplete();

}
