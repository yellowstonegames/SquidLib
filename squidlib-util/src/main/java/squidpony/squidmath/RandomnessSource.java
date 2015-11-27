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

}
