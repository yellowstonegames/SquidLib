package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;

/**
 * An abstract class that handles changes between a given color component and an actual one, and may carry state as a
 * float array.
 * Created by Tommy Ettinger on 10/31/2015.
 */
public abstract class Filter {
    public abstract Color alter(float r, float g, float b, float a);
    public float[] state;
}
