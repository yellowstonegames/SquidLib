package squidpony.squidmath;

/**
 * An empty marker interface to indicate that an implementor has known or intentional issues with a key property of its
 * functionality. This is almost always combined with another interface, as in {@link FlawedRandomness}, which uses this
 * to indicate that implementations are not as "fair" as other {@link RandomnessSource} implementations, and usually
 * have severe statistical defects. Typically, you would use a flawed implementation to compare with a non-flawed one,
 * or because the flaws have aesthetic merit from their statistical biases.
 * <br>
 * Created by Tommy Ettinger on 4/14/2020.
 */
public interface IFlawed {
}
