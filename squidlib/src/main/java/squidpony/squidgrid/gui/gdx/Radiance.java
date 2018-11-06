package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.StringKit;
import squidpony.squidmath.NumberTools;

import java.io.Serializable;

/**
 * Grouping of qualities related to glow and light emission. When a Radiance variable in some object is null, it
 * means that object doesn't emit light; if a Radiance variable is non-null, it will probably emit light unless the
 * color of light it produces is fully transparent. Light may take up one cell or extend into nearby cells, and the
 * radius may change over time in up to two patterns (flicker, which randomly increases and decreases lighting radius,
 * and/or strobe, which increases and decreases lighting radius in an orderly retract-expand-retract-expand pattern).
 * You can set the {@link #flare} variable to some value between 0.0f and 1.0f to temporarily expand the minimum radius
 * for strobe and/or flare, useful for gameplay-dependent brightening of a Radiance.
 * <br>
 * This object has 6 fields, each a float:
 * <ul>
 * <li>range, how far the light extends; 0f is "just this cell"</li>
 * <li>color, the color of the light as a float; typically opaque and lighter than the glowing object's color</li>
 * <li>flicker, the rate of random continuous change to radiance range</li>
 * <li>strobe, the rate of non-random continuous change to radiance range</li>
 * <li>flare, used to suddenly increase the minimum radius of lighting; expected to be changed after creation</li>
 * <li>delay, which delays the pattern of effects like strobe so a sequence can be formed with multiple Radiance</li>
 * </ul>
 * These all have defaults; if no parameters are specified the light will be white, affect only the current cell, and
 * won't flicker or strobe.
 * <br>
 * Created by Tommy Ettinger on 6/16/2018.
 */
public class Radiance implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * How far the radiated light extends; 0f is "just this cell", anything higher can go into neighboring cells.
     * This is permitted to be a non-integer value, which will make this extend into further cells partially.
     */
    public float range;
    /**
     * The color of light as a float; typically opaque and lighter than the glowing object's symbol.
     */
    public float color;
    /**
     * The rate of random continuous change to radiance range, like the light from a campfire. The random component of
     * the change is determined by the {@link System#identityHashCode(Object)} of this Radiance, which will probably
     * make all flicker effects different when flicker is non-0.
     */
    public float flicker;
    /**
     * The rate of non-random continuous change to radiance range, like a mechanical strobe effect. This looks like a
     * strobe light when the value is high enough, but at lower values it will smoothly pulse, which can be less
     * distracting to players.
     */
    public float strobe;

    /**
     * A time delay that applies to when the strobe and flicker effects change; useful with strobe to make a strobe
     * expand its lit radius at one point, then expand at a slightly later time at another Radiance with a delay. The
     * range for delay should be considered 0f to 1f, with 0f the default (no delay) and values between 0 and 1f that
     * fraction of a full strobe delayed from that default.
     */
    public float delay;
    /**
     * A temporary increase to the minimum radiance range, meant to brighten a glow during an effect.
     * This should be a float between 0f and 1f, with 0f meaning no change and 1f meaning always max radius.
     */
    public float flare;

    /**
     * All-default constructor; makes a single-cell unchanging white light.
     */
    public Radiance()
    {
        this(0f, SColor.FLOAT_WHITE, 0f, 0f, 0f, 0f);
    }

    /**
     * Makes an unchanging white light with the specified range in cells.
     * @param range possibly-non-integer radius to light, in cells
     */
    public Radiance(float range)
    {
        this(range, SColor.FLOAT_WHITE, 0f, 0f, 0f, 0f);
    }

    /**
     * Makes an unchanging light with the given color (as a packed float) and the specified range in cells.
     * @param range possibly-non-integer radius to light, in cells
     * @param color packed float color, as produced by {@link Color#toFloatBits()}
     */
    public Radiance(float range, float color)
    {
        this(range, color, 0f, 0f, 0f, 0f);
    }

    /**
     * Makes a flickering light with the given color (as a packed float) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal.
     * @param range possibly-non-integer radius to light, in cells
     * @param color packed float color, as produced by {@link Color#toFloatBits()}
     * @param flicker the rate at which to flicker, as a non-negative float
     */
    public Radiance(float range, float color, float flicker)
    {
        this(range, color, flicker, 0f, 0f, 0f);
    }
    /**
     * Makes a flickering light with the given color (as a packed float) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal.
     * @param range possibly-non-integer radius to light, in cells
     * @param color packed float color, as produced by {@link Color#toFloatBits()}
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     */
    public Radiance(float range, float color, float flicker, float strobe)
    {
        this(range, color, flicker, strobe, 0f, 0f);
    }
    /**
     * Makes a flickering light with the given color (as a libGDX Color) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal.
     * @param range possibly-non-integer radius to light, in cells
     * @param color a libGDX Color object; will not be modified
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     */
    public Radiance(float range, Color color, float flicker, float strobe)
    {
        this(range, color.toFloatBits(), flicker, strobe, 0f, 0f);
    }
    
    /**
     * Makes a flickering light with the given color (as a packed float) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal. The delay parameter is usually
     * from 0f to 1f, and is almost always 0f unless this is part of a group of related Radiance objects; it affects
     * when strobe and flicker hit "high points" and "low points", and should usually be used with strobe.
     * @param range possibly-non-integer radius to light, in cells
     * @param color packed float color, as produced by {@link Color#toFloatBits()}
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     * @param delay a delay applied to the "high points" and "low points" of strobe and flicker, from 0f to 1f
     */
    public Radiance(float range, float color, float flicker, float strobe, float delay)
    {
        this(range, color, flicker, strobe, delay, 0f);
    }
    /**
     * Makes a flickering light with the given color (as a packed float) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal. The delay parameter is usually
     * from 0f to 1f, and is almost always 0f unless this is part of a group of related Radiance objects; it affects
     * when strobe and flicker hit "high points" and "low points", and should usually be used with strobe. This allows
     * setting flare, where flare is used to create a sudden increase in the minimum radius for the Radiance, but flare
     * makes the most sense to set when an event should brighten a Radiance, not in the constructor. Valid values for
     * flare are usually between 0f and 1f.
     * @param range possibly-non-integer radius to light, in cells
     * @param color packed float color, as produced by {@link Color#toFloatBits()}
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     * @param delay a delay applied to the "high points" and "low points" of strobe and flicker, from 0f to 1f
     * @param flare affects the minimum radius for the Radiance, from 0f to 1f with a default of 0f
     */
    public Radiance(float range, float color, float flicker, float strobe, float delay, float flare)
    {
        this.range = range;
        this.color = color;
        this.flicker = flicker;
        this.strobe = strobe;
        this.delay = delay;
        this.flare = flare;
    }

    /**
     * Copies another Radiance exactly, except for the pattern its flicker may have, if any.
     * @param other another Radiance to copy
     */
    public Radiance(Radiance other)
    {
        this(other.range, other.color, other.flicker, other.strobe, other.delay, other.flare);
    }

    /**
     * Provides the calculated current range adjusted for flicker and strobe at the current time in milliseconds, with
     * flicker seeded with the identity hash code of this Radiance. Higher values of flicker and strobe will increase
     * the frequency at which the range changes but will not allow it to exceed its starting range, only to diminish
     * temporarily. If both flicker and strobe are non-0, the range will usually be smaller than if only one was non-0,
     * and if both are 0, this simply returns range.
     * @return the current range, adjusting for flicker and strobe using the current time
     */
    public float currentRange()
    {
        final float time = (System.currentTimeMillis() & 0x3ffffL) * 0x1.9p-9f;
        float current = range;
        if(flicker != 0f) 
            current *= NumberTools.swayRandomized(System.identityHashCode(this), time * flicker + delay) * 0.375f + 0.625f;
        if(strobe != 0f)
            current *= NumberTools.swayTight(time * strobe + delay) * 0.5f + 0.5f;
        return Math.max(current, range * flare);
    }

    /**
     * Makes a chain of Radiance objects that will pulse in a sequence, expanding from one to the next.
     * This chain is an array of Radiance where the order matters.
     * @param length how many Radiance objects should be in the returned array
     * @param range in cells, how far each Radiance should expand from its start at its greatest radius
     * @param color as a packed float color
     * @param strobe the rate at which the chain will pulse; should be greater than 0
     * @return an array of Radiance objects that will pulse in sequence.
     */
    public static Radiance[] makeChain(int length, float range, float color, float strobe)
    {
        if(length <= 1)
            return new Radiance[]{new Radiance(range, color, 0f, strobe)};
        Radiance[] chain = new Radiance[length];
        float d = -2f / (length);
        for (int i = 0; i < length; i++) {
            chain[i] = new Radiance(range, color, 0f, strobe, d * i);
        }
        return chain;
    }

    @Override
    public String toString() {
        return "Radiance{" +
                "range=" + range +
                ", color=" + color +
                ", flicker=" + flicker +
                ", strobe=" + strobe +
                ", delay=" + delay +
                ", flare=" + flare +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Radiance radiance = (Radiance) o;

        if (Float.compare(radiance.range, range) != 0) return false;
        if (Float.compare(radiance.color, color) != 0) return false;
        if (Float.compare(radiance.flicker, flicker) != 0) return false;
        if (Float.compare(radiance.strobe, strobe) != 0) return false;
        if (Float.compare(radiance.delay, delay) != 0) return false;
        return Float.compare(radiance.flare, flare) == 0;
    }

    @Override
    public int hashCode() {
        int result = (range != +0.0f ? NumberTools.floatToIntBits(range) : 0);
        result = 31 * result + (color != +0.0f ? NumberTools.floatToIntBits(color) : 0) | 0;
        result = 31 * result + (flicker != +0.0f ? NumberTools.floatToIntBits(flicker) : 0) | 0;
        result = 31 * result + (strobe != +0.0f ? NumberTools.floatToIntBits(strobe) : 0) | 0;
        result = 31 * result + (delay != +0.0f ? NumberTools.floatToIntBits(delay) : 0) | 0;
        result = 31 * result + (flare != +0.0f ? NumberTools.floatToIntBits(flare) : 0) | 0;
        return result;
    }

    public String serializeToString()
    {
        return  "{" + StringKit.hex(NumberTools.floatToIntBits(range)) +
                "," + StringKit.hex(NumberTools.floatToIntBits(color)) +
                "," + StringKit.hex(NumberTools.floatToIntBits(flicker)) +
                "," + StringKit.hex(NumberTools.floatToIntBits(strobe)) + 
                "," + StringKit.hex(NumberTools.floatToIntBits(delay)) +
                "," + StringKit.hex(NumberTools.floatToIntBits(flare)) +
                "}";
    }
    
    public static Radiance deserializeFromString(String data)
    {
        return data != null && data.length() >= 54
                ? new Radiance(
                NumberTools.intBitsToFloat(StringKit.intFromHex(data, 1, 9)),
                NumberTools.intBitsToFloat(StringKit.intFromHex(data, 10, 18)),
                NumberTools.intBitsToFloat(StringKit.intFromHex(data, 19, 27)),
                NumberTools.intBitsToFloat(StringKit.intFromHex(data, 28, 36)),
                NumberTools.intBitsToFloat(StringKit.intFromHex(data, 37, 45)),
                NumberTools.intBitsToFloat(StringKit.intFromHex(data, 46, 54)))
                : null;
    }
}
