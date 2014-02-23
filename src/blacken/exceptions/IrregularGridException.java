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
 * An operation was performed that is disallowed due to the regularity of 
 * the grid.
 * 
 * <p>This can either be an operation on an irregular grid which is disallowed
 * because it is an irregular grid, or an operation on a regular grid which is
 * only allowed on irregular grids.</p>
 * 
 * @author Steven Black
 */
public class IrregularGridException extends IllegalArgumentException {
    private static final long serialVersionUID = 2473129069282045073L;

    /**
     * Grid regularity exception.
     */
    public IrregularGridException() {
        // do nothing
    }

    /**
     * Grid regularity exception.
     * @param arg0 descriptive message
     */
    public IrregularGridException(String arg0) {
        super(arg0);
    }

    /**
     * Grid regularity exception.
     * @param arg0 cause
     */
    public IrregularGridException(Throwable arg0) {
        super(arg0);
    }

    /**
     * Grid regularity exception.
     * @param arg0 descriptive message
     * @param arg1 cause
     */
    public IrregularGridException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
