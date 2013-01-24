package squidpony.squidutility;

import java.util.Objects;

/**
 * A Collection that stores two values related in some way.
 *
 * In additions to having values accessible by key, they may also be accessed by
 * value.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Pair<F, S> {

    private F first;
    private S second;

    // Generic constructor
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<F, S> test = (Pair<F, S>) other;
        if (first.equals(test.first) && second.equals(test.second)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.first);
        hash = 73 * hash + Objects.hashCode(this.second);
        return hash;
    }
}
