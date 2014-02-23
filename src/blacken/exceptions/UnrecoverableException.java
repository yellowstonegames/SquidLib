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
 * An unrecoverable exception occurred.
 * 
 * @author yam655
 */
public class UnrecoverableException extends RuntimeException {
    private static final long serialVersionUID = 4239679840891572306L;

    /**
     * An unrecoverable exception occurred.
     */
    public UnrecoverableException() {
        super();
    }

    /**
     * An unrecoverable exception occurred.
     * @param message descriptive message
     */
    public UnrecoverableException(String message) {
        super(message);
    }

    /**
     * An unrecoverable exception occurred.
     * @param cause exception cause
     */
    public UnrecoverableException(Throwable cause) {
        super(cause);
    }

    /**
     * An unrecoverable exception occurred.
     * @param message descriptive message
     * @param cause exception cause
     */
    public UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

}
