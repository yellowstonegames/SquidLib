package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tommy Ettinger on 3/23/2016.
 */
public class ColorChangeImage extends Image {

    private List<Color> colors;
    private float progress = 0f;
    private float loopTime = 2f;

    /**
     * Creates an image stretched and aligned center, that will use the specified list of colors.
     *
     * @param texture the texture to use
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public ColorChangeImage(Texture texture, List<Color> colors) {
        super(texture);

        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = colors;
    }

    /**
     * Creates an image stretched and aligned center, that will use the specified list of colors.
     *
     * @param texture the texture to use
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public ColorChangeImage(Texture texture, float loopTime, List<Color> colors) {
        super(texture);

        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = colors;
        this.loopTime = loopTime;
    }
    /**
     * Creates an image stretched and aligned center, that will use the specified list of colors.
     *
     * @param texture the texture to use
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public ColorChangeImage(Texture texture, float loopTime, Color... colors) {
        super(texture);

        if(colors == null || colors.length == 0)
            this.colors = DefaultResources.getSCC().rainbow(12);
        else {
            this.colors = new ArrayList<>(colors.length);
            Collections.addAll(this.colors, colors);
        }
        this.loopTime = loopTime;
    }
    /**
     * Creates an image stretched and aligned center, that will use the specified list of colors.
     *
     * @param texture the texture to use
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public ColorChangeImage(TextureRegion texture, List<Color> colors) {
        super(texture);

        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = colors;
    }

    /**
     * Creates an image stretched and aligned center, that will use the specified list of colors.
     *
     * @param texture the texture to use
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public ColorChangeImage(TextureRegion texture, float loopTime, List<Color> colors) {
        super(texture);

        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = colors;
        this.loopTime = loopTime;
    }
    /**
     * Creates an image stretched and aligned center, that will use the specified list of colors.
     *
     * @param texture the texture to use
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public ColorChangeImage(TextureRegion texture, float loopTime, Color... colors) {
        super(texture);

        if(colors == null || colors.length == 0)
            this.colors = DefaultResources.getSCC().rainbow(12);
        else {
            this.colors = new ArrayList<>(colors.length);
            Collections.addAll(this.colors, colors);
        }
        this.loopTime = loopTime;
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
     * @param colors a List of Color, such as one returned by SquidColorCenter's gradient or rainbow methods
     */
    public void setColors(List<Color> colors)
    {
        if(colors == null || colors.isEmpty())
            this.colors = DefaultResources.getSCC().rainbow(12);
        else
            this.colors = colors;
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
