package squidpony.squidmath;

/**
 * An interface for "point hashes", that is, functions that produce usually-unique integer results given multiple
 * integer inputs. The hash functions this guarantees an implementor will provide all take int inputs and return an int.
 * There are lots of cases where an implementor would have other functions that take long inputs or return longs, and in
 * those cases the interface methods would probably be implemented by delegating to the long inputs and/or casting the
 * result to a long. Implementors must have a {@link #setState(int)} function, but it doesn't have to do anything if the
 * algorithm doesn't support a state; similarly, {@link #hashWithState(int, int, int)} can return the same thing as
 * {@link #hash(int, int)} if states aren't supported. If states are supported, then calling {@link #hash(int, int)}
 * while the state is, for example, 42 should be the same as calling {@link #hashWithState(int, int, int)} with a state
 * parameter of 42 (regardless of what the state actually is in the implementor).
 * <br>
 * Created by Tommy Ettinger on 4/13/2020.
 */
public interface IPointHash {
    void setState(int state);
    int hash(int x, int y);
    int hash(int x, int y, int z);
    int hash(int x, int y, int z, int w);
    int hash(int x, int y, int z, int w, int u);
    int hash(int x, int y, int z, int w, int u, int v);
    int hashWithState(int x, int y, int state);
    int hashWithState(int x, int y, int z, int state);
    int hashWithState(int x, int y, int z, int w, int state);
    int hashWithState(int x, int y, int z, int w, int u, int state);
    int hashWithState(int x, int y, int z, int w, int u, int v, int state);

    /**
     * A convenience abstract class to implement IPointHash when you have an int for state. Subclasses will need to
     * implement {@link #hashWithState(int, int, int)},  {@link #hashWithState(int, int, int, int)},
     * {@link #hashWithState(int, int, int, int, int)}, and  {@link #hashWithState(int, int, int, int, int, int, int)}.
     * They can optionally override {@link #setState(int)}, and can at their discretion provide an accessor for the
     * protected int {@link #state}.
     */
    abstract class IntImpl implements IPointHash {
        protected int state = 42;
        public IntImpl(){
            setState(42);
        }
        public IntImpl(int state){
            setState(state);
        }

        @Override
        public void setState(int state) {
            this.state = state;
        }

        @Override
        public int hash(int x, int y) {
            return hashWithState(x, y, state);
        }

        @Override
        public int hash(int x, int y, int z) {
            return hashWithState(x, y, z, state);
        }

        @Override
        public int hash(int x, int y, int z, int w) {
            return hashWithState(x, y, z, w, state);
        }

        @Override
        public int hash(int x, int y, int z, int w, int u) {
            return hashWithState(x, y, z, w, u, state);
        }

        @Override
        public int hash(int x, int y, int z, int w, int u, int v) {
            return hashWithState(x, y, z, w, u, v, state);
        }
    }
    /**
     * A convenience abstract class to implement IPointHash when you have a long for state. Subclasses will need to
     * implement {@link #hashWithState(int, int, int)},  {@link #hashWithState(int, int, int, int)},
     * {@link #hashWithState(int, int, int, int, int)}, {@link #hashWithState(int, int, int, int, int, int)}, an
     * {@link #hashWithState(int, int, int, int, int, int, int)}.
     * They can optionally override {@link #setState(int)} or {@link #setState(long)}, and can at their discretion
     * provide an accessor for the protected long {@link #state}.
     */
    abstract class LongImpl implements IPointHash {
        protected long state = 42;
        public LongImpl(){
            setState(42);
        }
        public LongImpl(int state){
            setState(state);
        }
        public LongImpl(long state){
            setState(state);
        }

        @Override
        public void setState(int state) {
            this.state = state;
        }
        
        public void setState(long state){
            this.state = state;
        }

        @Override
        public int hash(int x, int y) {
            return hashWithState(x, y, (int) (state ^ state >>> 32));
        }

        @Override
        public int hash(int x, int y, int z) {
            return hashWithState(x, y, z, (int) (state ^ state >>> 32));
        }

        @Override
        public int hash(int x, int y, int z, int w) {
            return hashWithState(x, y, z, w, (int) (state ^ state >>> 32));
        }

        @Override
        public int hash(int x, int y, int z, int w, int u) {
            return hashWithState(x, y, z, w, u, (int) (state ^ state >>> 32));
        }
        @Override
        public int hash(int x, int y, int z, int w, int u, int v) {
            return hashWithState(x, y, z, w, u, v, (int) (state ^ state >>> 32));
        }
    }
}
