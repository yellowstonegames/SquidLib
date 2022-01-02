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

import java.io.Serializable;

/**
 * This interface defines the interactions required of a random number
 * generator. It is a replacement for Java's built-in Random because for
 * improved performance.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public interface RandomnessSource extends Serializable {

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    int next(int bits);

    /**
     *
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     *
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    long nextLong();

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     * @return a copy of this RandomnessSource
     */
    RandomnessSource copy();
}
