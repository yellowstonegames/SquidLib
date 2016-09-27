package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import squidpony.squidgrid.Direction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A Label that changes its color automatically, taking its current color from a list such as a gradient. Useful for
 * implementing a "blink" effect where a creature alternates being visible and invisible, for a magical object that has
 * all the colors of the rainbow, or for all sorts of similar uses. The color pattern loops, by default one loop per two
 * seconds (but this can be changed), so longer lists of colors will display each color for a shorter time.
 * Created by Tommy Ettinger on 3/23/2016.
 */
public class ColorChangeLabel extends Label {

    private List<Color> colors;
    protected float progress = 0f;
    protected float loopTime = 2f;
    protected ColorChangeLabel()
    {
        this(null, null);
    }
    /**
     * Constructs a ColorChangeLabel. Used internally by TextCellFactory, but library users are unlikely to need this.
     * @param text the text to display in this ColorChangeLabel
     * @param style the LabelStyle to use for this; typically TextCellFactory handles this
     * @param colors a Collection (usually a List) of Color, such as one returned by SquidColorCenter's gradient method
     */
    public ColorChangeLabel(CharSequence text, LabelStyle style, Collection<Color> colors) {
        this(text, style, 2f, false, colors);
    }

    /**
     * Constructs a ColorChangeLabel. Used internally by TextCellFactory, but library users are unlikely to need this.
     * @param text the text to display in this ColorChangeLabel
     * @param style the LabelStyle to use for this; typically TextCellFactory handles this
     * @param loopTime the amount of time, in seconds, it takes to loop through all the colors in the list
     * @param colors a Collection (usually a List) of Color, such as one returned by SquidColorCenter's gradient method
     */
    public ColorChangeLabel(CharSequence text, LabelStyle style, float loopTime, Collection<Color> colors){
        this(text, style, loopTime, false, colors);
    }

    /**
     * Constructs a ColorChangeLabel. Used internally by TextCellFactory, but library users are unlikely to need this.
     * @param text the text to display in this ColorChangeLabel
     * @param style the LabelStyle to use for this; typically TextCellFactory handles this
     * @param loopTime the amount of time, in seconds, it takes to loop through all the colors in the list
     * @param doubleWidth true if this takes up two grid cells; only matters if you use {@link AnimatedEntity#setDirection(Direction)}
     * @param colors a Collection (usually a List) of Color, such as one returned by SquidColorCenter's gradient method
     */
    public ColorChangeLabel(CharSequence text, LabelStyle style, float loopTime, boolean doubleWidth, Collection<Color> colors){
        super(text, style);
        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = new ArrayList<>(colors);
        this.loopTime = loopTime == 0 ? 1 : loopTime;
    }
    /**
     * Constructs a ColorChangeLabel. Used internally by TextCellFactory, but library users are unlikely to need this.
     * @param text the text to display in this ColorChangeLabel
     * @param style the LabelStyle to use for this; typically TextCellFactory handles this
     * @param colors an array or vararg of Color
     */
    public ColorChangeLabel(CharSequence text, LabelStyle style, Color... colors) {
        this(text, style, 2f, false, colors);
    }

    /**
     * Constructs a ColorChangeLabel. Used internally by TextCellFactory, but library users are unlikely to need this.
     * @param text the text to display in this ColorChangeLabel
     * @param style the LabelStyle to use for this; typically TextCellFactory handles this
     * @param loopTime the amount of time, in seconds, it takes to loop through all the colors in the list
     * @param colors an array or vararg of Color
     */
    public ColorChangeLabel(CharSequence text, LabelStyle style, float loopTime, Color... colors){
        this(text, style, loopTime, false, colors);
    }

    /**
     * Constructs a ColorChangeLabel. Used internally by TextCellFactory, but library users are unlikely to need this.
     * @param text the text to display in this ColorChangeLabel
     * @param style the LabelStyle to use for this; typically TextCellFactory handles this
     * @param loopTime the amount of time, in seconds, it takes to loop through all the colors in the list
     * @param doubleWidth true if this takes up two grid cells; only matters if you use {@link AnimatedEntity#setDirection(Direction)}
     * @param colors an array or vararg of Color
     */
    public ColorChangeLabel(CharSequence text, LabelStyle style, float loopTime, boolean doubleWidth, Color... colors){
        super(text, style);
        if (colors == null || colors.length == 0)
            this.colors = DefaultResources.getSCC().rainbow(12);
        else {
            this.colors = new ArrayList<>(colors.length);
            Collections.addAll(this.colors, colors);
        }
        this.loopTime = loopTime == 0 ? 1 : loopTime;
    }

    /**
     * Returns the color the actor will be tinted when drawn. Takes the Color from the List of Color this was constructed
     * with or assigned with setColors, not the normal Actor color assigned with setColor.
     * @return the Color this will be drawn with
     */
    @Override
    public Color getColor() {
        return colors.get((int)(progress * colors.size() / loopTime));
    }

    /**
     * Sets the list of colors this uses to choose what color it draws with.
     * @param colors a Collection (usually a List) of Color, such as one returned by SquidColorCenter's gradient method
     */
    public void setColors(Collection<Color> colors)
    {
        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = new ArrayList<>(colors);
    }

    /**
     * Sets the list of colors this uses to choose what color it draws with.
     * @param colors an array or vararg of Color
     */
    public void setColors(Color... colors)
    {
        if (colors == null || colors.length == 0)
            this.colors = DefaultResources.getSCC().rainbow(12);
        else {
            this.colors = new ArrayList<>(colors.length);
            Collections.addAll(this.colors, colors);
        }
    }
    /**
     * Updates the actor based on time. Typically this is called each frame by
     * {@link com.badlogic.gdx.scenes.scene2d.Stage#act(float)}.
     * <p>
     * The default implementation calls
     * {@link com.badlogic.gdx.scenes.scene2d.Action#act(float)} on each action and removes actions that are complete.
     *
     * @param delta Time in seconds since the last frame.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        progress = (progress + delta) % (loopTime - 0.001f);
    }

    /**
     * Changes the amount of time this takes to loop through all colors, and also resets the current loop to its start.
     * @param loopTime the amount of time, in seconds, it takes to loop through all the colors in the list
     */
    public void resetLoopTime(float loopTime)
    {
        this.loopTime = loopTime;
        progress = 0f;
    }
}
