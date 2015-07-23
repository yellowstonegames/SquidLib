package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created by Tommy Ettinger on 7/22/2015.
 */
public class AnimatedEntity {
    public Actor actor;
    public int gridX, gridY;
    public boolean animating = false;
    public AnimatedEntity(Actor actor, int x, int y)
    {
        this.actor = actor;
        this.gridX = x;
        this.gridY = y;
    }
    public void setText(String text)
    {
        if(actor.getClass() == Label.class)
        {
            ((Label)actor).setText(text);
        }
    }
}