package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;

import squidpony.IFilter;

/**
 * An abstract class that handles changes between a given color component and an actual one, and may carry state as a
 * float array.
 * Created by Tommy Ettinger on 10/31/2015.
 */
public abstract class Filter<T extends Color> implements IFilter<T> {

    public float[] state;

}
