package squidpony.squidgrid.gui.gdx;

/**
 * A group of nested classes under the empty interface Filters, these all are meant to perform different changes
 * to colors before they are created (they should be passed to SquidColorCenter's constructor, which can use them).
 * Created by Tommy Ettinger on 10/31/2015.
 */
public interface Filters {

    /**
     * A Filter that does nothing to the colors it is given but pass them along unchanged.
     */
    public class IdentityFilter extends Filter
    {
        public IdentityFilter()
        {
            state = new float[0];
        }

        @Override
        public HDRColor alter(float r, float g, float b, float a) {
            return new HDRColor(r, g, b, a);
        }
    }

    /**
     * A Filter that tracks the highest brightness for any component it was assigned and stores it in state as the first
     * and only element.
     */
    public class MaxValueFilter extends Filter
    {
        public MaxValueFilter()
        {
            state = new float[]{0};
        }

        @Override
        public HDRColor alter(float r, float g, float b, float a) {
            state[0] = Math.max(state[0], Math.max(r, Math.max(g, b)));
            return new HDRColor(r, g, b, a);
        }

    }

    /**
     * A Filter that performs a brightness adjustment to make dark areas lighter and light areas not much less bright.
     */
    public class GammaCorrectFilter extends Filter {
        /**
         * Sets up a GammaCorrectFilter with the desired gamma adjustment.
         *
         * @param gamma    should be 1.0 or less, and must be greater than 0. Typical values are between 0.4 to 0.8.
         * @param maxValue the maximum brightness in the colors this will be passed; use MaxValueFilter for this
         */
        public GammaCorrectFilter(float gamma, float maxValue) {
            state = new float[]{gamma, 1f / (float) Math.pow(maxValue, gamma)};
        }

        @Override
        public HDRColor alter(float r, float g, float b, float a) {
            return new HDRColor(state[1] * (float) Math.pow(r, state[0]),
                    state[1] * (float) Math.pow(g, state[0]),
                    state[1] * (float) Math.pow(b, state[0]),
                    a);
        }
    }
    /**
     * A Filter that performs a brightness adjustment to make dark areas lighter and light areas not much less bright.
     */
    public class LerpFilter extends Filter {
        /**
         * Sets up a LerpFilter with the desired color to linearly interpolate towards.
         *
         * @param r the red component to lerp towards
         * @param g the green component to lerp towards
         * @param b the blue component to lerp towards
         * @param a the opacity component to lerp towards
         * @param amount the amount to lerp by, should be between 0.0 and 1.0
         */
        public LerpFilter(float r, float g, float b, float a, float amount) {
            state = new float[]{r, g, b, a, amount};
        }

        @Override
        public HDRColor alter(float r, float g, float b, float a) {
            return new HDRColor(r, g, b, a).lerp(state[0], state[1], state[2], state[3], state[4]);
        }
    }
}
