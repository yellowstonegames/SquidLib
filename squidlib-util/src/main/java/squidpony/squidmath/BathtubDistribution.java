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
 * An IDistribution that produces results between 0.0 inclusive and 1.0 exclusive, but is much more likely to produce
 * results near 0.0 or 1.0, further from 0.5.
 * <br>
 * Created by Tommy Ettinger on 11/23/2019.
 */
public class BathtubDistribution extends IDistribution.SimpleDistribution implements IDistribution {
    public static final BathtubDistribution instance = new BathtubDistribution();
    /**
     * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     *
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    @Override
    public double nextDouble(IRNG rng) {
        double d = (rng.nextDouble() - 0.5) * 2.0;
        d = d * d * d + 1.0;
        return d - (int)d;
    }
}
