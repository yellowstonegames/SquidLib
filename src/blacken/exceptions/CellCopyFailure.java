/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
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
 *
 * @author yam655
 */
public class CellCopyFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public CellCopyFailure() {
        super();
    }

    public CellCopyFailure(String message) {
        super(message);
    }

    public CellCopyFailure(Throwable cause) {
        super(cause);
    }

    public CellCopyFailure(String message, Throwable cause) {
        super(message, cause);
    }
}
