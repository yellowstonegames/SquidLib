/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

/** Sets the actor's color (or a specified color), from the current to the new color. Note this action transitions from the color
 * at the time the action starts to the specified color.
 * @author Nathan Sweet */
public class PackedColorAction extends TemporalAction {
    private float start;
    private Color color;
    private float end;

    protected void begin () {
        if (color == null) color = target.getColor();
        start = color.toFloatBits();
    }

    protected void update (float percent) {
        Color.abgr8888ToColor(color, SColor.lerpFloatColors(start, end, percent));
    }

    public void reset () {
        super.reset();
        color = null;
    }

    public Color getColor () {
        return color;
    }

    /** Sets the color to modify. If null (the default), the {@link #getActor() actor's} {@link Actor#getColor() color} will be
     * used. */
    public void setColor (Color color) {
        this.color = color;
    }

    public float getEndColor () {
        return end;
    }

    /** Sets the color to transition to. Required. */
    public void setEndColor (float color) {
        end = color;
    }
    /** Sets the actor's color instantly. */
    static public PackedColorAction color (float color) {
        return color(color, 0, null);
    }

    /** Transitions from the color at the time this action starts to the specified color. */
    static public PackedColorAction color (float color, float duration) {
        return color(color, duration, null);
    }

    /** Transitions from the color at the time this action starts to the specified color. */
    static public PackedColorAction color (float color, float duration, Interpolation interpolation) {
        PackedColorAction action = Actions.action(PackedColorAction.class);
        action.setEndColor(color);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }

}
