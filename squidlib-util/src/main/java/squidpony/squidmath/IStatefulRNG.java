package squidpony.squidmath;

/**
 * Simply groups the two interfaces {@link IRNG} and {@link StatefulRandomness} so some implementations of IRNG can have
 * their states read from and written to.
 * <br>
 * Created by Tommy Ettinger on 11/25/2018.
 */
public interface IStatefulRNG extends IRNG, StatefulRandomness {
}
