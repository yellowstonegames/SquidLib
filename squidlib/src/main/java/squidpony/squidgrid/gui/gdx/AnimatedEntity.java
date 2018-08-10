package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import squidpony.squidgrid.Direction;

import java.util.Collection;

/**
 * A simple class that wraps an Actor with its grid position, animating state, and if it is a double-width Actor.
 * Created by Tommy Ettinger on 7/22/2015.
 */
public class AnimatedEntity {
    public Actor actor;
    public int gridX, gridY;
    public boolean animating = false;
    public boolean doubleWidth = false;
    public AnimatedEntity(Actor actor, int x, int y)
    {
        this.actor = actor;
        gridX = x;
        gridY = y;
    }
    public AnimatedEntity(Actor actor, int x, int y, boolean doubleWidth)
    {
        this.actor = actor;
        gridX = x;
        gridY = y;
        this.doubleWidth = doubleWidth;
    }
    public void setText(String text)
    {
        if(actor instanceof Label)
        {
            ((Label)actor).setText(text);
        }
    }

    /**
     * Rotates this so that "up" points in the specified direction. Only some Actors can actually be rotated; Images
     * can, for example, but Labels cannot. This method is most likely to be used with
     * {@link TextCellFactory#makeDirectionMarker(Color)},
     * {@link TextCellFactory#makeDirectionMarker(Collection, float, boolean)}, or one of the directionMarker methods
     * in SquidPanel or SquidLayers, since those produce an Image (or {@link ColorChangeImage}) that can be sensibly
     * rotated to indicate a direction over a cell.
     * @param dir the direction that "up" for this should point toward
     */
    public void setDirection(Direction dir) {
        actor.setRotation(directionToDegrees(dir));
    }
    private static float directionToDegrees(Direction d)
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
