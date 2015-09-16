package squidpony.squidmath;

/**
 * A simple interface for RandomnessSources that have the additional property of a state that can be re-set.
 * Created by Tommy Ettinger on 9/15/2015.
 */
public interface StatefulRandomness extends RandomnessSource {
    /**
     * Get the current internal state of the Reseedable as a long.
     * @return the current internal state of this object.
     */
    public long getState();

    /**
     * Set the current internal state of this Reseedable with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0, even though some implementations can handle that.
     */
    public void setState(long state);
}
