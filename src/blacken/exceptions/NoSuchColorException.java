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
package com.googlecode.blacken.exceptions;

/**
 * The requested color key does not exist.
 * 
 * @author Steven Black
 */
public class NoSuchColorException extends Exception {

    /**
     * The serialVersionUID is required by Serializable objects
     */
    private static final long serialVersionUID = -4372977339401691848L;

    /**
     * Thrown when a color is looked up which does not exist
     */
    public NoSuchColorException() {
        super();
    }

    /**
     * The serialVersionUID is required by Serializable objects
     * 
     * @param message
     *            Description of the problem
     * @param cause
     *            Related exception
     */
    public NoSuchColorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The serialVersionUID is required by Serializable objects
     * 
     * @param message
     *            Description of the problem
     */
    public NoSuchColorException(String message) {
        super(message);
    }

    /**
     * The serialVersionUID is required by Serializable objects
     * 
     * @param cause
     *            Related exception
     */
    public NoSuchColorException(Throwable cause) {
        super(cause);
    }
}
