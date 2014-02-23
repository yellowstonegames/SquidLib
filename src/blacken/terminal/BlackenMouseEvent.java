/* blacken - a library for Roguelike games
 * Copyright © 2010, 2011 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.googlecode.blacken.terminal;

import java.util.EnumSet;
import java.util.Objects;

/**
 * The Blacken mouse event.
 * 
 * @author yam655
 */
public class BlackenMouseEvent {
    private BlackenEventType type;
    private int y = -1;
    private int x = -1;
    private int clickCount = 0;
    private EnumSet<BlackenModifier> modifiers = EnumSet.noneOf(BlackenModifier.class);
    private EnumSet<BlackenMouseButton> remainingButtons = EnumSet.noneOf(BlackenMouseButton.class);
    private BlackenMouseButton actingButton = BlackenMouseButton.NO_BUTTON;
    private double rotation = 0.0;
        
    @Override
    public String toString() {
        return String.format("Mouse: %s %d, %d, %s (%s) %s x%d", type.name(), y, x,
                      BlackenModifier.getModifierString(modifiers), remainingButtons,
                      actingButton, clickCount);
    }
    
    /**
     * Get the type of the event
     * @return the event type
     */
    public BlackenEventType getType() {
        return type;
    }

    /**
     * Set the event type
     * @param type the event type
     */
    public void setType(BlackenEventType type) {
        this.type = type;
    }

    /**
     * Get the Y coordinate
     * @return the coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Set the Y coordinate
     * @param y coordinate
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Get the X coordinate
     * @return the coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Set the X coordinate
     * @param x coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the click count
     * @return click count
     */
    public int getClickCount() {
        return clickCount;
    }

    /**
     * Set the click count
     * @param clickCount click count
     */
    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }
    
    /**
     * Create a new mouse event
     * @param type event type
     */
    public BlackenMouseEvent(BlackenEventType type) {
        this.type = type;
    }
    
    /**
     * Get the position
     * @return {y, x}
     */
    public int[] getPosition() {
        int[] ret = {y, x};
        return ret;
    }
    
    /**
     * Set the position
     * @param y coordinate
     * @param x coordinate
     */
    public void setPosition(int y, int x) {
        this.y = y;
        this.x = x;
    }

    /**
     * Set the modifiers
     * @param modifiers set of modifiers
     */
    public void setModifiers(EnumSet<BlackenModifier> modifiers) {
        if (modifiers == null) {
            this.modifiers = EnumSet.noneOf(BlackenModifier.class);
        } else {
            this.modifiers = modifiers;
        }
    }

    /**
     * Get the modifiers
     * @return the modifiers
     */
    public EnumSet<BlackenModifier> getModifiers() {
        return modifiers;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 41 * hash + this.y;
        hash = 41 * hash + this.x;
        hash = 41 * hash + this.clickCount;
        hash = 41 * hash + Objects.hashCode(this.modifiers);
        hash = 41 * hash + Objects.hashCode(this.remainingButtons);
        hash = 41 * hash + (this.actingButton != null ? this.actingButton.hashCode() : 0);
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.rotation) ^ (Double.doubleToLongBits(this.rotation) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlackenMouseEvent other = (BlackenMouseEvent) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.x != other.x) {
            return false;
        }
        if (this.clickCount != other.clickCount) {
            return false;
        }
        if (!Objects.equals(this.modifiers, other.modifiers)) {
            return false;
        }
        if (!Objects.equals(this.remainingButtons, other.remainingButtons)) {
            return false;
        }
        if (this.actingButton != other.actingButton) {
            return false;
        }
        if (Double.doubleToLongBits(this.rotation) != Double.doubleToLongBits(other.rotation)) {
            return false;
        }
        return true;
    }


    /**
     * Get the acting button.
     * @return
     * @deprecated Use {@link #getActingButton()} instead.
     */
    @Deprecated
    public int getButton() {
        int b = 0;
        switch(actingButton) {
            case BUTTON_1: 
                b = 1;
                break;
            case BUTTON_2: 
                b = 2;
                break;
            case BUTTON_3: 
                b = 3;
                break;
            case WHEEL_UP: 
                b = 4;
                break;
            case WHEEL_DOWN: 
                b = 5;
                break;
        }
        return b;
    }

    /**
     *
     * @param b
     * @deprecated Use {@link #setActingButton(BlackenMouseButton)} instead.
     */
    @Deprecated
    public void setButton(int b) {
        switch (b) {
            case 1:
                actingButton = BlackenMouseButton.BUTTON_1;
                break;
            case 2:
                actingButton = BlackenMouseButton.BUTTON_2;
                break;
            case 3:
                actingButton = BlackenMouseButton.BUTTON_3;
                break;
            case 4:
                actingButton = BlackenMouseButton.WHEEL_UP;
                break;
            case 5:
                actingButton = BlackenMouseButton.WHEEL_DOWN;
                break;
            default:
                actingButton = BlackenMouseButton.NO_BUTTON;
                break;
        }
    }

    /**
     * Set the acting button
     * @param button
     */
    public void setActingButton(BlackenMouseButton button) {
        this.actingButton = button;
    }
    
    public BlackenMouseButton getActingButton() {
        return actingButton;
    }

    /**
     * Set the state of the buttons after the event.
     * @param buttons buttons
     */
    public void setRemainingButtons(EnumSet<BlackenMouseButton> buttons) {
        if (buttons == null) {
            this.remainingButtons = EnumSet.noneOf(BlackenMouseButton.class);
        } else {
            this.remainingButtons = buttons;
        }
    }

    /**
     * Get the state of the buttons after the event.
     * @return button set
     */
    public EnumSet<BlackenMouseButton> getRemainingButtons() {
        return remainingButtons;
    }

    public void setRotation(double rot) {
        this.rotation = rot;
    }
    public double getRotation() {
        return rotation;
    }
}
