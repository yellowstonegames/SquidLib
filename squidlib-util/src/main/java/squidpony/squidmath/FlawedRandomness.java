package squidpony.squidmath;

/**
 * An interface to indicate a {@link RandomnessSource} that is intentionally flawed to create output patterns where a
 * truly random sequence would have none. All FlawedRandomness implementations are static inner classes inside this
 * interface, hopefully to avoid confusion with ones that are meant for general use.
 * <br>
 * Created by Tommy Ettinger on 11/10/2019.
 */
public interface FlawedRandomness extends RandomnessSource {
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
        public RandomnessSource copy() {
            return null;
        }
    }
}
