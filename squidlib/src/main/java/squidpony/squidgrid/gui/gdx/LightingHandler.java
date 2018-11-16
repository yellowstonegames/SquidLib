package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedMap;

import java.io.Serializable;

import static squidpony.squidgrid.gui.gdx.SColor.FLOAT_WHITE;
import static squidpony.squidgrid.gui.gdx.SColor.lerpFloatColorsBlended;

/**
 * A convenience class that makes dealing with multiple colored light sources easier.
 * All fields are public and documented to encourage their use alongside the API methods. The typical usage case for
 * this class is when a game has complex lighting needs that should be consolidated into one LightingHandler per level,
 * where a level corresponds to a {@code char[][]}. After constructing a LightingHandler with the resistances for a
 * level, you should add all light sources with their positions, either using {@link #addLight(int, int, Radiance)} or
 * by directly putting keys and values into {@link #lights}. Then you can calculate the visible cells once lighting is
 * considered (which may include distant lit cells with unseen but unobstructing cells between the viewer and the light)
 * using {@link #calculateFOV(Coord)}, which should be called every time the viewer moves. You can update the flicker
 * and strobe effects on all Radiance objects, which is typically done every frame, using {@link #update()} or
 * {@link #updateAll()} (updateAll() is for when there is no viewer), and once that update() call has been made you can
 * call {@link #draw(SparseLayers)} to change the background colors of a SparseLayers, {@link #draw(SquidPanel)} to
 * change the colors of a SquidPanel (typically the background layer of a SquidLayers, as from
 * {@link SquidLayers#getBackgroundLayer()}), or {@link #draw(float[][])} to change a 2D float array that holds packed
 * float colors (which may be used in some custom setup). To place user-interface lighting effects that don't affect the
 * actual FOV of creatures in the game, you can use {@link #updateUI(Coord, Radiance)}, which is called after
 * {@link #update()} but before {@link #draw(float[][])}.
 * <br>
 * Created by Tommy Ettinger on 11/2/2018.
 */
public class LightingHandler implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * How light should spread; usually {@link Radius#CIRCLE} unless gameplay reasons need it to be SQUARE or DIAMOND.
     */
    public Radius radiusStrategy;
    /**
     * The 2D array of light-resistance values from 0.0 to 1.0 for each cell on the map, as produced by
     * {@link squidpony.squidgrid.mapping.DungeonUtility#generateResistances(char[][])}.
     */
    public double[][] resistances;
    /**
     * What the "viewer" (as passed to {@link #calculateFOV(Coord)}) can see either nearby without light or because an
     * area in line-of-sight has light in it. Edited by {@link #calculateFOV(Coord)} and {@link  #update()}, but
     * not {@link #updateUI(Coord, Radiance)} (which is meant for effects that are purely user-interface).
     */
    public double[][] fovResult;
    /**
     * A 2D array of doubles that are either 0.0 if a cell has an obstruction between it and the viewer, or greater than
     * 0.0 otherwise.
     */
    public double[][] losResult;
    /**
     * Temporary storage array used for calculations involving {@link #fovResult}; it sometimes may make sense for other
     * code to use this as temporary storage as well.
     */
    public double[][] tempFOV;
    /**
     * A pair of 2D float arrays with different usages; {@code colorLighting[0]} is a 2D array that stores the strength
     * of light in each cell, and {@code colorLighting[1]} is a 2D array that stores the color of light in each cell, as
     * a packed float color. Both 2D arrays are the size of the map, as defined by {@link #resistances} initially and
     * later available in {@link #width} and {@link #height}.
     */
    public float[][][] colorLighting;
    /**
     * Temporary storage array used for calculations involving {@link #colorLighting}; it sometimes may make sense for
     * other code to use this as temporary storage as well.
     */
    public float[][][] tempColorLighting;
    /**
     * Width of the 2D arrays used in this, as obtained from {@link #resistances}.
     */
    public int width;
    /**
     * Height of the 2D arrays used in this, as obtained from {@link #resistances}.
     */
    public int height;
    /**
     * The packed float color to mix background cells with when a cell has lighting and is within line-of-sight, but has
     * no background color to start with (its color is 0f as a packed float, or {@link SColor#TRANSPARENT}).
     */
    public float backgroundColor;
    /**
     * How far the viewer can see without light; defaults to 4.0 cells, and you are encouraged to change this member
     * field if the vision range changes after construction.
     */
    public double viewerRange;
    /**
     * A mapping from positions as {@link Coord} objects to {@link Radiance} objects that describe the color, lighting
     * radius, and changes over time of any in-game lights that should be shown on the map and change FOV. You can edit
     * this manually or by using {@link #moveLight(int, int, int, int)}, {@link #addLight(int, int, Radiance)}, and
     * {@link #removeLight(int, int)}.
     */
    public OrderedMap<Coord, Radiance> lights;

    /**
     * A GreasedRegion that stores any cells that are in line-of-sight or are close enough to a cell in line-of-sight to
     * potentially cast light into such a cell. Depends on the highest {@link Radiance#range} in {@link #lights}.
     */
    public GreasedRegion noticeable;
    
    /**
     * Unlikely to be used except during serialization; makes a LightingHandler for a 20x20 fully visible level.
     * The viewer vision range will be 4.0, and lights will use a circular shape.
     */
    public LightingHandler()
    {
        this(new double[20][20], SColor.FLOAT_BLACK, Radius.CIRCLE, 4.0);
    }

    /**
     * Given a resistance array as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateResistances(char[][])}
     * or {@link squidpony.squidgrid.mapping.DungeonUtility#generateSimpleResistances(char[][])}, makes a
     * LightingHandler that can have {@link Radiance} objects added to it in various locations. This will use a solid
     * black background when it casts light on cells without existing lighting. The viewer vision range will be 4.0, and
     * lights will use a circular shape.
     * @param resistance a resistance array as produced by DungeonUtility
     */
    public LightingHandler(double[][] resistance)
    {
        this(resistance, SColor.FLOAT_BLACK, Radius.CIRCLE, 4.0);
    }
    /**
     * Given a resistance array as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateResistances(char[][])}
     * or {@link squidpony.squidgrid.mapping.DungeonUtility#generateSimpleResistances(char[][])}, makes a
     * LightingHandler that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as a libGDX color
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingHandler(double[][] resistance, Color backgroundColor, Radius radiusStrategy, double viewerVisionRange)
    {
        this(resistance, backgroundColor.toFloatBits(), radiusStrategy, viewerVisionRange);
    }
    /**
     * Given a resistance array as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateResistances(char[][])}
     * or {@link squidpony.squidgrid.mapping.DungeonUtility#generateSimpleResistances(char[][])}, makes a
     * LightingHandler that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as a packed float (produced by {@link Color#toFloatBits()})
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingHandler(double[][] resistance, float backgroundColor, Radius radiusStrategy, double viewerVisionRange)
    {
        this.radiusStrategy = radiusStrategy;
        viewerRange = viewerVisionRange;
        this.backgroundColor = backgroundColor;
        resistances = resistance;
        width = resistances.length;
        height = resistances[0].length;
        fovResult = new double[width][height];
        tempFOV = new double[width][height];
        losResult = new double[width][height];
        colorLighting = SColor.blankColoredLighting(width, height);
        tempColorLighting = new float[2][width][height];
        Coord.expandPoolTo(width, height);
        lights = new OrderedMap<>(32);
        noticeable = new GreasedRegion(width, height);
    }

    /**
     * Adds a Radiance as a light source at the given position. Overwrites any existing Radiance at the same position.
     * @param x the x-position to add the Radiance at
     * @param y the y-position to add the Radiance at
     * @param light a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @return this for chaining
     */
    public LightingHandler addLight(int x, int y, Radiance light)
    {
        return addLight(Coord.get(x, y), light);
    }
    /**
     * Adds a Radiance as a light source at the given position. Overwrites any existing Radiance at the same position.
     * @param position the position to add the Radiance at
     * @param light a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @return this for chaining
     */
    public LightingHandler addLight(Coord position, Radiance light)
    {
        lights.put(position, light);
        return this;
    }

    /**
     * Removes a Radiance as a light source from the given position, if any is present.
     * @param x the x-position to remove the Radiance from
     * @param y the y-position to remove the Radiance from
     * @return this for chaining
     */
    public LightingHandler removeLight(int x, int y)
    {
        return removeLight(Coord.get(x, y));
    }
    /**
     * Removes a Radiance as a light source from the given position, if any is present.
     * @param position the position to remove the Radiance from
     * @return this for chaining
     */
    public LightingHandler removeLight(Coord position)
    {
        lights.remove(position);
        return this;
    }
    /**
     * If a Radiance is present at oldX,oldY, this will move it to newX,newY and overwrite any existing Radiance at
     * newX,newY. If no Radiance is present at oldX,oldY, this does nothing.
     * @param oldX the x-position to move a Radiance from
     * @param oldY the y-position to move a Radiance from
     * @param newX the x-position to move a Radiance to
     * @param newY the y-position to move a Radiance to
     * @return this for chaining
     */
    public LightingHandler moveLight(int oldX, int oldY, int newX, int newY)
    {
        return moveLight(Coord.get(oldX, oldY), Coord.get(newX, newY));
    }
    /**
     * If a Radiance is present at oldPosition, this will move it to newPosition and overwrite any existing Radiance at
     * newPosition. If no Radiance is present at oldPosition, this does nothing.
     * @param oldPosition the Coord to move a Radiance from
     * @param newPosition the Coord to move a Radiance to
     * @return this for chaining
     */
    public LightingHandler moveLight(Coord oldPosition, Coord newPosition)
    {
        Radiance old = lights.get(oldPosition);
        if(old == null) return this;
        lights.alter(oldPosition, newPosition);
        return this;
    }

    /**
     * Gets the Radiance at the given position, if present, or null if there is no light source there.
     * @param x the x-position to look up
     * @param y the y-position to look up
     * @return the Radiance at the given position, or null if none is present there
     */
    public Radiance get(int x, int y)
    {
        return lights.get(Coord.get(x, y));
    }
    /**
     * Gets the Radiance at the given position, if present, or null if there is no light source there.
     * @param position the position to look up
     * @return the Radiance at the given position, or null if none is present there
     */
    public Radiance get(Coord position)
    {
        return lights.get(position);
    }

    /**
     * Edits {@link #colorLighting} by adding in and mixing the colors in {@link #tempColorLighting}, with the strength
     * of light in tempColorLighting boosted by flare (which can be any finite float greater than -1f, but is usually
     * from 0f to 1f when increasing strength).
     * Primarily used internally, but exposed so outside code can do the same things this class can.
     * @param flare boosts the effective strength of lighting in {@link #tempColorLighting}; usually from 0 to 1
     */
    public void mixColoredLighting(float flare)
    {
        float[][][] basis = colorLighting, other = tempColorLighting;
        flare += 1f;
        float b0, b1, o0, o1;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0) {
                    if (resistances[x][y] >= 1) {
                        o0 = 0f;
                        o1 = 0f;
                        if (y > 0) {
                            if (losResult[x][y - 1] > 0 && resistances[x][y - 1] < 1) {
                                if (o0 != (o0 = Math.max(o0, other[0][x][y - 1]))) o1 = other[1][x][y - 1];
                            }
                            if (x > 0 && losResult[x - 1][y - 1] > 0 && resistances[x - 1][y - 1] < 1) {
                                if (o0 != (o0 = Math.max(o0, other[0][x - 1][y - 1]))) o1 = other[1][x - 1][y - 1];
                            }
                            if (x < width - 1 && losResult[x + 1][y - 1] > 0 && resistances[x + 1][y - 1] < 1) {
                                if (o0 != (o0 = Math.max(o0, other[0][x + 1][y - 1]))) o1 = other[1][x + 1][y - 1];
                            }
                        }
                        if (y < height - 1) {
                            if (losResult[x][y + 1] > 0 && resistances[x][y + 1] < 1) {
                                if (o0 != (o0 = Math.max(o0, other[0][x][y + 1]))) o1 = other[1][x][y + 1];
                            }
                            if (x > 0 && losResult[x - 1][y + 1] > 0 && resistances[x - 1][y + 1] < 1) {
                                if (o0 != (o0 = Math.max(o0, other[0][x - 1][y + 1]))) o1 = other[1][x - 1][y + 1];
                            }
                            if (x < width - 1 && losResult[x + 1][y + 1] > 0 && resistances[x + 1][y + 1] < 1) {
                                if (o0 != (o0 = Math.max(o0, other[0][x + 1][y + 1]))) o1 = other[1][x + 1][y + 1];
                            }
                        }
                        if (x > 0 && losResult[x - 1][y] > 0 && resistances[x - 1][y] < 1) {
                            if (o0 != (o0 = Math.max(o0, other[0][x - 1][y]))) o1 = other[1][x - 1][y];
                        }
                        if (x < width - 1 && losResult[x + 1][y] > 0 && resistances[x + 1][y] < 1) {
                            if (o0 != (o0 = Math.max(o0, other[0][x + 1][y]))) o1 = other[1][x + 1][y];
                        }
                    } else {
                        o0 = other[0][x][y];
                        o1 = other[1][x][y];
                    }
                    if (o0 == 0f || o1 == 0f)
                        continue;
                    b0 = basis[0][x][y];
                    b1 = basis[1][x][y];
                    if (b1 == FLOAT_WHITE) {
                        basis[1][x][y] = o1;
                        basis[0][x][y] = Math.min(1.0f, b0 + o0 * flare);
                    } else {
                        if (o1 != FLOAT_WHITE) {
                            float change = (o0 - b0) * 0.5f + 0.5f;
                            final int s = NumberTools.floatToIntBits(b1), e = NumberTools.floatToIntBits(o1),
                                    rs = (s & 0xFF), gs = (s >>> 8) & 0xFF, bs = (s >>> 16) & 0xFF, as = s & 0xFE000000,
                                    re = (e & 0xFF), ge = (e >>> 8) & 0xFF, be = (e >>> 16) & 0xFF, ae = (e >>> 25);
                            change *= ae * 0.007874016f;
                            basis[1][x][y] = NumberTools.intBitsToFloat(((int) (rs + change * (re - rs)) & 0xFF)
                                    | ((int) (gs + change * (ge - gs)) & 0xFF) << 8
                                    | (((int) (bs + change * (be - bs)) & 0xFF) << 16)
                                    | as);
                            basis[0][x][y] = Math.min(1.0f, b0 + o0 * change * flare);
                        } else {
                            basis[0][x][y] = Math.min(1.0f, b0 + o0 * flare);
                        }
                    }
                }
            }
        }
    }

    /**
     * Edits {@link #colorLighting} by adding in and mixing the given color where the light strength in {@link #tempFOV}
     * is greater than 0, with that strengt boosted by flare (which can be any finite float greater than -1f, but is
     * usually from 0f to 1f when increasing strength).
     * Primarily used internally, but exposed so outside code can do the same things this class can.
     * @param flare boosts the effective strength of lighting in {@link #tempColorLighting}; usually from 0 to 1
     */
    public void mixColoredLighting(float flare, float color)
    {
        final float[][][] basis = colorLighting;
        final double[][] otherStrength = tempFOV;
        flare += 1f;
        float b0, b1, o0, o1;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0) {
                    if (resistances[x][y] >= 1) {
                        o0 = 0f;
                        if (y > 0) {
                            if (losResult[x][y - 1] > 0 && resistances[x][y - 1] < 1) {
                                o0 = Math.max(o0, (float) otherStrength[x][y - 1]);
                            }
                            if (x > 0 && losResult[x - 1][y - 1] > 0 && resistances[x - 1][y - 1] < 1) {
                                o0 = Math.max(o0, (float) otherStrength[x - 1][y - 1]);
                            }
                            if (x < width - 1 && losResult[x + 1][y - 1] > 0 && resistances[x + 1][y - 1] < 1) {
                                o0 = Math.max(o0, (float) otherStrength[x + 1][y - 1]);
                            }
                        }
                        if (y < height - 1) {
                            if (losResult[x][y + 1] > 0 && resistances[x][y + 1] < 1) {
                                o0 = Math.max(o0, (float) otherStrength[x][y + 1]);
                            }
                            if (x > 0 && losResult[x - 1][y + 1] > 0 && resistances[x - 1][y + 1] < 1) {
                                o0 = Math.max(o0, (float) otherStrength[x - 1][y + 1]);
                            }
                            if (x < width - 1 && losResult[x + 1][y + 1] > 0 && resistances[x + 1][y + 1] < 1) {
                                o0 = Math.max(o0, (float) otherStrength[x + 1][y + 1]);
                            }
                        }
                        if (x > 0 && losResult[x - 1][y] > 0 && resistances[x - 1][y] < 1) {
                            o0 = Math.max(o0, (float) otherStrength[x - 1][y]);
                        }
                        if (x < width - 1 && losResult[x + 1][y] > 0 && resistances[x + 1][y] < 1) {
                            o0 = Math.max(o0, (float) otherStrength[x + 1][y]);
                        }
                        if(o0 != 0) o1 = color;
                        else continue;
                    } else {
                        if((o0 = (float) otherStrength[x][y]) != 0) o1 = color;
                        else continue;
                    }
                    b0 = basis[0][x][y];
                    b1 = basis[1][x][y];
                    if (b1 == FLOAT_WHITE) {
                        basis[1][x][y] = o1;
                        basis[0][x][y] = Math.min(1.0f, b0 + o0 * flare);
                    } else {
                        if (o1 != FLOAT_WHITE) {
                            float change = (o0 - b0) * 0.5f + 0.5f;
                            final int s = NumberTools.floatToIntBits(b1), e = NumberTools.floatToIntBits(o1),
                                    rs = (s & 0xFF), gs = (s >>> 8) & 0xFF, bs = (s >>> 16) & 0xFF, as = s & 0xFE000000,
                                    re = (e & 0xFF), ge = (e >>> 8) & 0xFF, be = (e >>> 16) & 0xFF, ae = (e >>> 25);
                            change *= ae * 0.007874016f;
                            basis[1][x][y] = NumberTools.intBitsToFloat(((int) (rs + change * (re - rs)) & 0xFF)
                                    | ((int) (gs + change * (ge - gs)) & 0xFF) << 8
                                    | (((int) (bs + change * (be - bs)) & 0xFF) << 16)
                                    | as);
                            basis[0][x][y] = Math.min(1.0f, b0 + o0 * change * flare);
                        } else {
                            basis[0][x][y] = Math.min(1.0f, b0 + o0 * flare);
                        }
                    }
                }
            }
        }
    }

    /**
     * Typically called every frame, this updates the flicker and strobe effects of Radiance objects and applies those
     * changes in lighting color and strength to the various fields of this LightingHandler. This will only have an
     * effect if {@link #calculateFOV(Coord)} or {@link #calculateFOV(int, int)} was called during the last time the
     * viewer position changed; typically calculateFOV() only needs to be called once per move, while update() needs to
     * be called once per frame. This method is usually called before each call to {@link #draw(float[][])}, but other
     * code may be between the calls and may affect the lighting in customized ways.
     */
    public void update()
    {
        Radiance radiance;
        SColor.eraseColoredLighting(colorLighting);
        final int sz = lights.size();
        Coord pos;
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            if(!noticeable.contains(pos))
                continue;
            radiance = lights.getAt(i);
            FOV.reuseFOV(resistances, tempFOV, pos.x, pos.y, radiance.currentRange());
            //SColor.colorLightingInto(tempColorLighting, tempFOV, radiance.color);
            mixColoredLighting(radiance.flare, radiance.color);
        }
    }
    /**
     * Typically called every frame when there isn't a single viewer, this updates the flicker and strobe effects of
     * Radiance objects and applies those changes in lighting color and strength to the various fields of this
     * LightingHandler. This method is usually called before each call to {@link #draw(float[][])}, but other code may
     * be between the calls and may affect the lighting in customized ways. This overload has no viewer, so all cells
     * are considered visible unless they are fully obstructed (solid cells behind walls, for example). Unlike update(),
     * this method does not need {@link #calculateFOV(Coord)} to be called for it to work properly.
     */
    public void updateAll()
    {
        Radiance radiance;
        for (int x = 0; x < width; x++) {
            PER_CELL:
            for (int y = 0; y < height; y++) {
                for (int xx = Math.max(0, x - 1), xi = 0; xi < 3 && xx < width; xi++, xx++) {
                    for (int yy = Math.max(0, y - 1), yi = 0; yi < 3 && yy < height; yi++, yy++) {
                        if(resistances[xx][yy] < 1.0){
                            losResult[x][y] = 1.0;
                            continue PER_CELL;
                        }
                    }
                }
            }
        }
        SColor.eraseColoredLighting(colorLighting);
        final int sz = lights.size();
        Coord pos;
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            radiance = lights.getAt(i);
            FOV.reuseFOV(resistances, tempFOV, pos.x, pos.y, radiance.currentRange());
            //SColor.colorLightingInto(tempColorLighting, tempFOV, radiance.color);
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0) {
                    fovResult[x][y] = MathUtils.clamp(losResult[x][y] + colorLighting[0][x][y], 0, 1);
                }
            }
        }
    }
    /**
     * Updates the flicker and strobe effects of a Radiance object and applies the lighting from just that Radiance to
     * just the {@link #colorLighting} field, without changing FOV. This method is meant to be used for GUI effects that
     * aren't representative of something a character in the game could interact with. It is usually called after
     * {@link #update()} and before each call to {@link #draw(float[][])}, but other code may be between the calls
     * and may affect the lighting in customized ways.
     * @param pos the position of the light effect
     * @param radiance the Radiance to update standalone, which does not need to be already added to this 
     */
    public void updateUI(Coord pos, Radiance radiance)
    {
        updateUI(pos.x, pos.y, radiance);
    }

    /**
     * Updates the flicker and strobe effects of a Radiance object and applies the lighting from just that Radiance to
     * just the {@link #colorLighting} field, without changing FOV. This method is meant to be used for GUI effects that
     * aren't representative of something a character in the game could interact with. It is usually called after
     * {@link #update()} and before each call to {@link #draw(float[][])}, but other code may be between the calls
     * and may affect the lighting in customized ways.
     * @param lightX the x-position of the light effect
     * @param lightY the y-position of the light effect
     * @param radiance the Radiance to update standalone, which does not need to be already added to this 
     */
    public void updateUI(int lightX, int lightY, Radiance radiance)
    {
        FOV.reuseFOV(resistances, tempFOV, lightX, lightY, radiance.currentRange());
        //SColor.colorLightingInto(tempColorLighting, tempFOV, radiance.color);
        mixColoredLighting(radiance.flare, radiance.color);
    }

    /**
     * Given a SparseLayers, fills the SparseLayers with different colors based on what lights are present in line of
     * sight of the viewer and the various flicker or strobe effects that Radiance light sources can do. You should
     * usually call {@link #update()} before each call to draw(), but you may want to make custom changes to the
     * lighting in between those two calls (that is the only place those changes will be noticed).
     * @param layers a SparseLayers that may have existing background colors (these will be mixed in)
     */
    public void draw(SparseLayers layers)
    {
        draw(layers.backgrounds);
    }
    /**
     * Given a SquidPanel that should be only solid blocks (such as the background of a SquidLayers) and a position for
     * the viewer (typically the player), fills the SquidPanel with different colors based on what lights are present in
     * line of sight of the viewer and the various flickering or pulsing effects that Radiance light sources can do. 
     * Given a SquidPanel that should be only solid blocks (such as the background of a SquidLayers), fills the
     * SquidPanel with different colors based on what lights are present in line of sight of the viewer and the various
     * flicker or strobe effects that Radiance light sources can do. You should usually call {@link #update()}
     * before each call to draw(), but you may want to make custom changes to the lighting in between those two calls
     * (that is the only place those changes will be noticed).
     * @param background a SquidPanel used as a background, such as the back Panel of a SquidLayers
     */
    public void draw(SquidPanel background)
    {
        draw(background.colors);
    }

    /**
     * Given a 2D array of packed float colors, fills the 2D array with different colors based on what lights are
     * present in line of sight of the viewer and the various flicker or strobe effects that Radiance light sources can
     * do. You should usually call {@link #update()} before each call to draw(), but you may want to make custom
     * changes to the lighting in between those two calls (that is the only place those changes will be noticed).
     * @param backgrounds a 2D float array, typically obtained from {@link squidpony.squidgrid.gui.gdx.SquidPanel#colors} or {@link squidpony.squidgrid.gui.gdx.SparseLayers#backgrounds}
     */
    public void draw(float[][] backgrounds)
    {
        float current;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0 && fovResult[x][y] > 0.0) {
                        current = backgrounds[x][y];
                        if(current == 0f)
                            current = backgroundColor;
                        backgrounds[x][y] = lerpFloatColorsBlended(current,
                                colorLighting[1][x][y], colorLighting[0][x][y] * 0.4f);
                }
            }
        }
    }
    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. Sets
     * {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the given viewer position and any lights
     * in {@link #lights}.
     * @param viewer the position of the player or other viewer
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public double[][] calculateFOV(Coord viewer)
    {
        return calculateFOV(viewer.x, viewer.y);
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. Sets
     * {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the given viewer position and any lights
     * in {@link #lights}.
     * @param viewerX the x-position of the player or other viewer
     * @param viewerY the y-position of the player or other viewer
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public double[][] calculateFOV(int viewerX, int viewerY)
    {
        Radiance radiance;
        FOV.reuseFOV(resistances, fovResult, viewerX, viewerY, viewerRange, radiusStrategy);
        FOV.reuseLOS(resistances, losResult, viewerX, viewerY);
        SColor.eraseColoredLighting(colorLighting);
        final int sz = lights.size();
        float maxRange = 0;
        for (int i = 0; i < sz; i++) {
            maxRange = Math.max(maxRange, lights.getAt(i).range);
        }
        noticeable.refill(losResult, 0.0001, Double.POSITIVE_INFINITY).expand8way((int) Math.ceil(maxRange));
        Coord pos;
        for (int i = 0; i < sz; i++) {
            pos = lights.keyAt(i);
            if(!noticeable.contains(pos))
                continue;
            radiance = lights.getAt(i);
            FOV.reuseFOV(resistances, tempFOV, pos.x, pos.y, radiance.range);
            //SColor.colorLightingInto(tempColorLighting, tempFOV, radiance.color);
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0) {
                    fovResult[x][y] = MathUtils.clamp(fovResult[x][y] + colorLighting[0][x][y], 0, 1);
                }
            }
        }
        return fovResult;
    }

}
