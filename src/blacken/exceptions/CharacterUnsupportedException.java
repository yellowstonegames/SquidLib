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
 * An unsupported codepoint has been requested.
 * 
 * @author yam655
 *
 */
public class CharacterUnsupportedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3138235441574739078L;

	/**
	 * The codepoint is unsupported.
	 */
	public CharacterUnsupportedException() {
		super();
	}

	/**
	 * The codepoint is unsupported.
	 * @param message descriptive message
	 */
	public CharacterUnsupportedException(String message) {
		super(message);
	}

	/**
	 * The codepoint is unsupported.
	 * @param cause the cause
	 */
	public CharacterUnsupportedException(Throwable cause) {
		super(cause);
	}

	/**
         * The codepoint is unsupported.
         * @param message descriptive message
	 * @param cause the cause
	 */
	public CharacterUnsupportedException(String message, Throwable cause) {
		super(message, cause);
	}

}
