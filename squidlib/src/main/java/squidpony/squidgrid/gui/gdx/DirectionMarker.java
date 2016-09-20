package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import squidpony.squidgrid.Direction;

import java.util.Collection;

/**
 * Created by Tommy Ettinger on 9/19/2016.
 */
public class DirectionMarker extends ColorChangeLabel {
    public Direction direction;
    protected DirectionMarker()
    {
        direction = Direction.UP;
    }
    public DirectionMarker(Direction dir, LabelStyle style, Collection<Color> colors)
    {
        super("^", style, colors);
        setOrigin(1); //Align.center
        setDirection(dir);
    }
    public DirectionMarker(Direction dir, LabelStyle style, Color... colors)
    {
        super("^", style, colors);
        setOrigin(1); //Align.center
        setDirection(dir);
    }
    public DirectionMarker(Direction dir, LabelStyle style, float loopTime, Collection<Color> colors)
    {
        super("^", style, loopTime, colors);
        setOrigin(1); //Align.center
        setDirection(dir);
    }
    public DirectionMarker(Direction dir, LabelStyle style, float loopTime, Color... colors)
    {
        super("^", style, loopTime, colors);
        setOrigin(1); //Align.center
        setDirection(dir);
    }
    public DirectionMarker(Direction dir, LabelStyle style, float loopTime, boolean doubleWidth, Collection<Color> colors)
    {
        super("^", style, loopTime, colors);
        setOrigin(doubleWidth ? 16 : 1); //Align.right or Align.center
        setDirection(dir);
    }
    public DirectionMarker(Direction dir, LabelStyle style, float loopTime, boolean doubleWidth, Color... colors)
    {
        super("^", style, loopTime, colors);
        setOrigin(doubleWidth ? 16 : 1); //Align.right or Align.center
        setDirection(dir);
    }

    public void setDirection(Direction dir)
    {
        direction = dir;
        setRotation(directionToDegrees(dir));
    }

    /**
     * Updates the actor based on time. Typically this is called each frame by
     * {@link Stage#act(float)}.
     * <p>
     * The default implementation calls
     * {@link Action#act(float)} on each action and removes actions that are complete.
     *
     * @param delta Time in seconds since the last frame.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
    }

    public static float directionToDegrees(Direction d)
    {
        switch (d)
        {
            case UP_LEFT: return 45f;
            case LEFT: return 90f;
            case DOWN_LEFT: return 135f;
            case DOWN: return 180f;
            case DOWN_RIGHT: return 225f;
            case RIGHT: return 270f;
            case UP_RIGHT: return 315f;
            default: return 0f;
        }
    }
}
