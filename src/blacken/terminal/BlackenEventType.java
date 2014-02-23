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

/**
 * The available event types.
 * 
 * @author Steven Black
 */
public enum BlackenEventType {
    /**
     * The mouse was clicked.
     */
    MOUSE_CLICKED, 
    /**
     * The mouse entered the window.
     */
    MOUSE_ENTERED, 
    /**
     * The mouse exited the window.
     */
    MOUSE_EXITED, 
    /**
     * The mouse was pressed.
     */
    MOUSE_PRESSED, 
    /**
     * The mouse was dragged
     */
    MOUSE_DRAGGED, 
    /**
     * The mouse was moved.
     */
    MOUSE_MOVED, 
    /**
     * The window was activated
     */
    WINDOW_ACTIVATED, 
    /**
     * The window was closed.
     */
    WINDOW_CLOSED, 
    /**
     * The window is closing.
     */
    WINDOW_CLOSING, 
    /**
     * The window is deactivated
     */
    WINDOW_DEACTIVATED, 
    /**
     * The window is deiconified.
     */
    WINDOW_DEICONIFIED, 
    /**
     * The window is iconified.
     */
    WINDOW_ICONIFIED, 
    /**
     * The window is opened.
     */
    WINDOW_OPENED, 
    /**
     * The window gained focus.
     */
    WINDOW_GAINED_FOCUS,
    /**
     * The window lost focus
     */
    WINDOW_LOST_FOCUS, 
    /**
     * Mouse wheel events
     */
    MOUSE_WHEEL, 
    /**
     * The mouse was released.
     */
    MOUSE_RELEASED,
}
