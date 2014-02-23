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
 * A complex character showed up where only simple characters were expected.
 * 
 * @author yam655
 */
public class ComplexCharacterException extends Exception {

    private static final long serialVersionUID = 4389421563337664791L;

    /**
     * Illegal use of complex character.
     */
    public ComplexCharacterException() {
        super();
    }

    /**
     * Illegal use of complex character.
     * 
     * @param arg0 descriptive message
     * @param arg1 cause
     */
    public ComplexCharacterException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Illegal use of complex character.
     * 
     * @param arg0 descriptive message
     */
    public ComplexCharacterException(String arg0) {
        super(arg0);
    }

    /**
     * Illegal use of complex character.
     * 
     * @param arg0 cause
     */
    public ComplexCharacterException(Throwable arg0) {
        super(arg0);
    }

}
