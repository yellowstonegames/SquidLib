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
 * This indicates that an invalid string format was found.
 * 
 * @author yam655
 */
public class InvalidStringFormatException extends Exception {
    private static final long serialVersionUID = 1395889187899154754L;

    /**
     * An illegal string format was found.
     */
    public InvalidStringFormatException() {
        super();
    }

    /**
     * An illegal string format was found.
     * @param arg0 descriptive message
     */
    public InvalidStringFormatException(String arg0) {
        super(arg0);
    }

    /**
     * An illegal string format was found.
     * @param arg0 cause
     */
    public InvalidStringFormatException(Throwable arg0) {
        super(arg0);
    }

    /**
     * An illegal string format was found.
     * @param arg0 descriptive message
     * @param arg1 cause
     */
    public InvalidStringFormatException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
