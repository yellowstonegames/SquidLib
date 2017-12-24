package squidpony.squidmath;

/**
 * A simple interface for RandomnessSources that have the additional capability to skip forward or backward in their
 * generated number stream.
 * Created by Tommy Ettinger on 9/15/2015.
 */
public interface SkippingRandomness extends RandomnessSource {
    /**
     * Advances or rolls back the SkippingRandomness' state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to {@link #nextLong()},
     * and returns the random number produced at that step. Negative numbers can be used to step backward, or 0 can be
     * given to get the most-recently-generated long from {@link #nextLong()}.
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    long skip(long advance);
}
