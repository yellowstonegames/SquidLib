package squidpony.squidmath;

/**
 * An interface to indicate a {@link RandomnessSource} that is intentionally flawed to create output patterns where a
 * truly random sequence would have none. All FlawedRandomness implementations are static inner classes inside this
 * interface, hopefully to avoid confusion with ones that are meant for general use.
 * <br>
 * Created by Tommy Ettinger on 11/10/2019.
 */
public interface FlawedRandomness extends RandomnessSource {
    /**
     * A flawed randomness source that depends almost entirely on its starting state for any random-seeming results in
     * its output. Simply outputs a number that starts with the initial seed and increases by {@code 0x1111111111111111}
     * each time, or {@code 1229782938247303441}.
     */
    class BigCounter implements FlawedRandomness, StatefulRandomness
    {
        public long state;

        public BigCounter()
        {
            this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                    ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
        }
        public BigCounter(long state) {
            this.state = state;
        }

        @Override
        public long getState() {
            return state;
        }

        @Override
        public void setState(long state) {
            this.state = state;
        }

        @Override
        public int next(int bits) {
            return (int)((state += 0x1111111111111111L) >>> 64 - bits);
        }

        @Override
        public long nextLong() {
            return (state += 0x1111111111111111L);
        }

        @Override
        public BigCounter copy() {
            return new BigCounter(state);
        }
    }

    /**
     * A flawed randomness source that adds a rotation of its state, to its state, every generation. The rotation amount
     * is also determined by state. This one's probably pretty bad; I don't really know how bad it will be to a human
     * observer, but it also depends on what cycle it starts in. The state probably shouldn't ever be 0, since this
     * will only produce 0 after its state becomes 0. Of course, this is flawed, so it can become 0 in the course of
     * normal generation.
     */
    class AddRotate implements FlawedRandomness, StatefulRandomness
    {
        public long state;

        public AddRotate()
        {
            this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                    ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
        }
        public AddRotate(long state) {
            this.state = state == 0 ? 1 : state;
        }

        @Override
        public long getState() {
            return state;
        }

        @Override
        public void setState(long state) {
            this.state = state == 0 ? 1 : state;
        }

        @Override
        public int next(int bits) {
            return (int)((state += (state << state | state >>> -state)) >>> 64 - bits);
        }

        @Override
        public long nextLong() {
            return (state += (state << state | state >>> -state));
        }

        @Override
        public AddRotate copy() {
            return new AddRotate(state);
        }
    }
}
