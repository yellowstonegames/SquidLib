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

import java.io.Serializable;

/**
 * A slight variant on RNG that always uses a stateful RandomessSource and so can have its state
 * set or retrieved using setState() or getState().
 * Created by Tommy Ettinger on 9/15/2015.
 * @author Tommy Ettinger
 */
public class StatefulRNG extends RNG implements Serializable, IStatefulRNG {

	private static final long serialVersionUID = -2456306898212937163L;

	public StatefulRNG() {
        super();
    }

    public StatefulRNG(RandomnessSource random) {
        super((random instanceof StatefulRandomness) ? random : new DiverRNG(random.nextLong()));
    }

    /**
     * Seeded constructor uses DiverRNG, which is of high quality, but low period (which rarely matters for games),
     * and has good speed and tiny state size.
     */
    public StatefulRNG(long seed) {
        this(new DiverRNG(seed));
    }
    /**
     * String-seeded constructor uses the hash of the String as a seed for DiverRNG, which is of high quality, but
     * low period (which rarely matters for games), and has good speed and tiny state size.
     *
     * Note: This constructor changed behavior on April 22, 2017, again on December 23, 2017, and again on June 14,
     * 2018. The first was when it was noticed that it was not seeding very effectively (only assigning to 32 bits of
     * seed instead of all 64). The older behavior isn't fully preserved, since it used a rather low-quality String
     * hashing algorithm and so probably had problems producing good starting seeds, but you can get close by replacing
     * {@code new StatefulRNG(text)} with {@code new StatefulRNG(new LightRNG(CrossHash.hash(text)))}. The new technique
     * assigns to all 64 bits and has less correlation between similar inputs causing similar starting states. It's also
     * faster, but that shouldn't matter in a constructor. It uses a better hashing algorithm because CrossHash no
     * longer has the older, worse one. The latest change in June switched to DiverRNG instead of LightRNG.
     */
    public StatefulRNG(CharSequence seedString) {
        this(new DiverRNG(CrossHash.hash64(seedString)));
    }

    @Override
    public void setRandomness(RandomnessSource random) {
        super.setRandomness(random == null ? new DiverRNG() :
                (random instanceof StatefulRandomness) ? random : new DiverRNG(random.nextLong()));
    }

    /**
     * Creates a copy of this StatefulRNG; it will generate the same random numbers, given the same calls in order, as
     * this StatefulRNG at the point copy() is called. The copy will not share references with this StatefulRNG.
     *
     * @return a copy of this StatefulRNG
     */
    public StatefulRNG copy() {
        return new StatefulRNG(random.copy());
    }

    /**
     * Get a long that can be used to reproduce the sequence of random numbers this object will generate starting now.
     * @return a long that can be used as state.
     */
    @Override
    public long getState()
    {
        return ((StatefulRandomness)random).getState();
    }

    /**
     * Sets the state of the random number generator to a given long, which will alter future random numbers this
     * produces based on the state.
     * @param state a long, which typically should not be 0 (some implementations may tolerate a state of 0, however).
     */
    @Override
    public void setState(long state)
    {
        ((StatefulRandomness)random).setState(state);
    }

    @Override
    public String toString() {
        return "StatefulRNG{" + Long.toHexString(((StatefulRandomness)random).getState()) + "}";
    }
    /**
     * Returns this StatefulRNG in a way that can be deserialized even if only {@link IRNG}'s methods can be called.
     * @return a {@link Serializable} view of this StatefulRNG; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
    }

}
