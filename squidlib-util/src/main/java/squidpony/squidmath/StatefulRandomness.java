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

package squidpony.squidmath;

/**
 * A simple interface for RandomnessSources that have the additional property of a state that can be re-set.
 * Created by Tommy Ettinger on 9/15/2015.
 */
public interface StatefulRandomness extends RandomnessSource {
    /**
     * Get the current internal state of the StatefulRandomness as a long.
     * @return the current internal state of this object.
     */
    long getState();

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0, even though some implementations can handle that.
     */
    void setState(long state);

    /**
     * Produces a copy of this StatefulRandomness that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this StatefulRandomness
     */
    @Override
    RandomnessSource copy();
}
