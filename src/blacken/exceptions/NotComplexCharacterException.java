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
 * A simple character was found where a complex was required.
 * 
 * @author Steven Black
 */
public class NotComplexCharacterException extends Exception {

    private static final long serialVersionUID = 3051266593008304578L;

    /**
     * Failure to find a complex character sequence.
     */
    public NotComplexCharacterException() {
        super();
    }

    /**
     * Failure to find a complex character sequence.
     * @param message descriptive message
     */
    public NotComplexCharacterException(String message) {
        super(message);
    }

    /**
     * Failure to find a complex character sequence.
     * @param cause the cause
     */
    public NotComplexCharacterException(Throwable cause) {
        super(cause);
    }

    /**
     * Failure to find a complex character sequence.
     * @param message descriptive message
     * @param cause the cause
     */
    public NotComplexCharacterException(String message, Throwable cause) {
        super(message, cause);
    }

}
