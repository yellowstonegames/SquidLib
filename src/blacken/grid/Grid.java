/* blacken - a library for Roguelike games
 * Copyright © 2010-2012 Steven Black <yam655@gmail.com>
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
package com.googlecode.blacken.grid;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import com.googlecode.blacken.cell.FlexibleCellCopier;
import com.googlecode.blacken.cell.GridCellCopier;
import com.googlecode.blacken.cell.Util;
import com.googlecode.blacken.exceptions.IrregularGridException;
import com.googlecode.blacken.grid.Bresenham.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A two-dimensional grid. 
 * 
 * <p>Using a little work you can make a two dimensional grid with an 
 * arbitrarily-sized third dimension.</p>
 *
 * <p>Grids come in two varieties: regular and irregular. 
 * While regular grids are always rectangular, irregular grids can be both
 * arbitrary shapes, as well as contain voids.</p>
 * 
 * <p>A regular grid is exactly what you think of when you think of a grid, a 
 * two-dimensional array which can be arbitrarily sized along either dimension.
 * That is, our grid forbids <code>null</code> values in favor of having 
 * a known-good <code>empty</code> cell value. This allows the grid to be
 * arbitrarily resized with cells being dropped and added and the cells always
 * have known-good values.
 * 
 * <p>The irregular grids expressly allow <code>null</code> values. (Note
 * that ({@link #unset(int, int)} uses the stored <code>empty</code> value, not 
 * <code>null</code>.) Because irregular grids may have shaped edges which we
 * can not recreate, they explicitly forbid resizing.
 * 
 * <p>While in the general use you could use a regular grid with a special
 * "off-limits" cell value, the voids in the irregular grids allow a shaped
 * segment of a grid to be copied in to another grid. The <code>null</code> 
 * cells are never copied when copying a grid in to another grid, so if you 
 * copy one irregular grid in to another the number of <code>null</code> 
 * cells are always reduced.
 *
 * <p>Note that this (by default) uses introspection to determine
 * if the cells are cloneable. (It ends up copying cells by way of the
 * {@link Util#cloneOrCopy(Object)} function.) If you want to
 * avoid this, provide a {@link GridCellCopier} through
 * {@link #setCellCopier(GridCellCopier)}.
 *
 * <p>Serialization of the Grid does nothing to store the GridCellCopier in 
 * use. The GridCellCopier may change between versions, and the best 
 * GridCellCopier for your cell type should be used in all cases.
 *
 * @author Steven Black
 * @param <Z> an object implementing Cloneable or an object copied by reference
 */
public class Grid <Z>
implements Serializable, Regionlike {
    private static final long serialVersionUID = 709537762108751L;
    private static Logger LOGGER = LoggerFactory.getLogger(Grid.class);
    private ArrayList<ArrayList<Z>> grid = null;
    private int start_x = 0;
    private int start_y = 0;
    private int size_x = 0;
    private int size_y = 0;
    protected Z empty = null;
    private boolean irregular = false;
    private transient GridCellCopier<Z> cellCopier = new FlexibleCellCopier<>();

    /**
     * Create a new, zero height, zero width grid
     * 
     * <p>Because the <code>empty</code> value is set to <code>null</code>
     * it creates an irregular grid. You can safely resize the grid 
     * by calling {@link #setSize(int, int)} -- without
     * calling {@link #reset(int, int, Object)}.</p>
     */
    public Grid() {
        grid = new ArrayList<>();
        this.empty = null;
        this.size_x = 0;
        this.size_y = 0;
        this.start_x = 0;
        this.start_y = 0;
        this.irregular = true;
    }

    /**
     * This is undocumented because you should <em>not</em> use it.
     * @param map map of your doom (if you use this function)
     */
    Grid(Map<String, Object> map) {
        if (!map.get("__target__").equals(Grid.class.getName())) {
            throw new IllegalArgumentException("Not my map.");
        }
        if (map.get("__version__") != 1) {
            throw new IllegalArgumentException("Unsupported version");
        }
        grid = (ArrayList<ArrayList<Z>>) map.get("grid");
        empty = (Z) map.get("empty");
        size_x = (int) map.get("size_x");
        size_y = (int) map.get("size_y");
        start_x = (int) map.get("start_x");
        start_y = (int) map.get("start_y");
        irregular = (boolean) map.get("irregular");
    }

    /**
     * Create a new grid.
     * 
     * <p>The starting coordinate for the grid is 0,0.</p>
     * 
     * @param empty the prototype empty cell
     * @param numRows number of rows
     * @param numCols number of columns
     */
    public Grid(Z empty, int numRows, int numCols) {
        grid = new ArrayList<>();
        this.empty = cellCopier.copyCell(empty);
        this.size_y = 0;
        this.size_x = 0;
        if (numRows > 0) {
            internal_resize(numRows, numCols, true);
        }
    }

    /**
     * Create a new grid with a position.
     * 
     * @param empty the prototype empty cell
     * @param numRows number of rows
     * @param numCols number of columns
     * @param y starting Y coordinate
     * @param x starting X coordinate
     */
    public Grid(Z empty, int numRows, int numCols, int y, int x) {
        grid = new ArrayList<>();
        this.empty = cellCopier.copyCell(empty);
        this.size_x = 0;
        this.size_y = 0;
        this.start_y = y;
        this.start_x = x;
        if (numRows > 0) {
            internal_resize(numRows, numCols, true);
        }
    }
    
    /**
     * Create an irregularly shaped grid.
     * 
     * <p>An irregular grid does not have cells within the square that is the
     * normal bounds. This is handled by inserting nulls in the missing cells.
     * When we create an irregularly shaped grid, the entire grid is full of
     * nulls. To create the pattern, you need to explicitly set some of them
     * to non-null.</p>
     * 
     * <p>Irregularly shaped grids can not be resized after they're created. They
     * will throw an IrregularGridException if you try.</p>
     * 
     * @param empty the normal empty cell (though not the initial cell) 
     * @param numRows number of rows
     * @param numCols number of cells
     * @param y starting Y coordinate
     * @param x starting X coordinate
     * @param irregular true if irregular, false if regular
     * 
     */
    public Grid(Z empty, int numRows, int numCols, int y, int x, 
                boolean irregular) {
        this.irregular = irregular;
        grid = new ArrayList<>();
        if (irregular) {
            this.empty = null;
        } else {
            this.empty = cellCopier.copyCell(empty);
        }
        this.size_x = 0;
        this.size_y = 0;
        this.start_y = y;
        this.start_x = x;
        if (numRows > 0) {
            internal_resize(numRows, numCols, true);
        }
        if (irregular) {
            this.empty = cellCopier.copyCell(empty);
        }
    }

    /**
     * Create a box with custom corners.
     * 
     * The order of the arguments is based upon Curses.
     * 
     * Any of the Z arguments can be <code>null</code> which will cause that
     * section to not be written within the grid. This allows layering of boxes
     * to produce reasonable results, including the option for use of 'T'
     * line cells where they will intersect.
     * 
     * This doesn't support irregularly shaped grids. No exceptions raised,
     * instead of will cause the grid to conform to the box shape.
     * 
     * @param height height of the box
     * @param width width of the box
     * @param x1 starting X position
     * @param y1 starting Y position
     * @param left left-most cell (4)
     * @param right right-most cell (6)
     * @param top top-most cell (8)
     * @param bottom bottom-most cell (2)
     * @param topleft top-left corner cell (7)
     * @param topright top-right corner cell (9)
     * @param bottomleft bottom-left corner cell (1)
     * @param bottomright bottom-right corner cell (3)
     * @param inside interior cell (5)
     */
    public void box(int height, int width, int y1, int x1, 
                    Z left, Z right, Z top, Z bottom, 
                    Z topleft, Z topright, Z bottomleft, Z bottomright, 
                    Z inside) {
        int y2 = y1 + height -1;
        int x2 = x1 + width -1;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++){
                if (y == y1) {
                    if (x == x1){
                        if (topleft != null) {
                            setCopy(y, x, topleft);
                        }
                    } else if (x == x2) {
                        if (topright != null) {
                            setCopy(y, x, topright);
                        }
                    } else if (top != null) {
                        setCopy(y, x, top);
                    }
                } else if (y == y2) {
                    if (x == x1) {
                        if (bottomleft != null) {
                            setCopy(y, x, bottomleft);
                        }
                    } else if (x == x2) {
                        if (bottomright != null) {
                            setCopy(y, x, bottomright);
                        }
                    } else if (bottom != null) {
                        setCopy(y, x, bottom);
                    }
                } else if (x == x2) {
                    if (right != null) {
                        setCopy(y, x, right);
                    }
                } else if (x == x1) {
                    if (left != null) {
                        setCopy(y, x, left);
                    }
                } else if (inside != null) {
                    setCopy(y, x, inside);
                }
            }
        }
    }
    
    /**
     * Draw a box around the entirety of a region, ignoring region edge.
     * 
     * <p>Note that this function is called 'box' and not 'outline' or 'trace'.
     * A Regionlike item is capable of being much more than a box, but this
     * ignores all of that.</p>
     * 
     * <p>This is designed so that if the Z arguments were ('4', '6', '8', '2', 
     * '7', '9', '1', '3', '5'), it would yield:</p>
     * 
     * <pre>
     * 789
     * 456
     * 123
     * </pre>
     * 
     * This doesn't support irregularly shaped grids. No exceptions raised,
     * instead of will cause the grid to conform to the box shape.
     * 
     * @param region region which we plan to box
     * @param left left-most cell (4)
     * @param right right-most cell (6)
     * @param top top-most cell (8)
     * @param bottom bottom-most cell (2)
     * @param topleft top-left corner cell (7)
     * @param topright top-right corner cell (9)
     * @param bottomleft bottom-left corner cell (1)
     * @param bottomright bottom-right corner cell (3)
     * @param inside interior cell (5)
     */
    public void box(Regionlike region, 
                    Z left, Z right, Z top, Z bottom, 
                    Z topleft, Z topright, Z bottomleft, Z bottomright, 
                    Z inside) {
        box(region.getHeight(), region.getWidth(), 
                      region.getY(), region.getX(), 
                      left, right, top, bottom, topleft, topright, 
                      bottomleft, bottomright, inside);
    }

    /**
     * Call clear with the stored <code>empty</code> value.
     */
    public void clear() {
        clear(null);
    }

    /**
     * Clear the entire map, optionally using a new/different empty value.
     * 
     * If <code>empty</code> is null this will use the empty value from 
     * map creation (or last clearing).
     * 
     * @param empty The exact contents of each cell on the map. 
     */
    public void clear(Z empty) {
        if (empty != null) {
            this.empty = cellCopier.copyCell(empty);
        }
        if (size_y > 0 && size_x > 0) {
            resize(this.size_y, this.size_x, true);
        }
    }
    /**
     * Is position (y, x) contained within the grid?
     * 
     * <p>This supports irregular grids. If a cell is <code>null</code> it is
     * not in the in the grid.</p>
     */
    @Override
    public boolean contains(int y, int x) {
        if (x >= getX() && x < getX() + getWidth()) {
            if (y >= getY() && y < getY() + getHeight()) {
                if (this.irregular) {
                    return get(y, x) != null;
                }
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.Regionlike#contains(int, int, int, int)
     */
    @Override
    public boolean contains(int height, int width, int y1, int x1) {
        return BoxRegion.contains(this, height, width, y1, x1);
    }
    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.Regionlike#contains(com.googlecode.blacken.grid.Positionable)
     */
    @Override
    public boolean contains(Positionable p) {
        return contains(p.getY(), p.getX());
    }
    
    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.Regionlike#contains(com.googlecode.blacken.grid.Regionlike)
     */
    @Override
    public boolean contains(Regionlike r) {
        return BoxRegion.contains(this, r);
    }

    /**
     * get takes relative y, x offsets from start of grid
     * 
     * @param y row offset
     * @param x column offset
     * @throws IndexOutOfBoundsException x or y out of bounds
     * @return the Z at the given grid location
     */
    public Z get(int y, int x) {
        return grid.get(y - start_y).get(x - start_x);
    }
    /**
     * 
     * @param <T> a positionable type
     * @param pos a positionable item in the grid
     * @return the Z at the given grid location
     */
    public <T extends Positionable> Z get (T pos) {
        return grid.get(pos.getY() - start_y).get(pos.getX() - start_x); 
    }
    
    @Override
    public Regionlike getBounds() {
        return new BoxRegion(this);
    }
    
    @Override
    public RegionIterator getEdgeIterator() {
        RegionIterator ret = new BoxRegionIterator(this, true, false);
        return ret;
    }

    /**
     * Get the current empty cell
     * 
     * @return a copy of the current empty cell
     */
    public Z getEmpty() {
        return cellCopier.copyCell(empty);
    }

    /**
     * Copy a cell somehow.
     *
     * <p>If possible, we clone the cell. If that isn't available we copy by
     * reference.</p>
     *
     * @param cell
     * @return
     */
    public Z copyCell(Z cell) {
        return cellCopier.copyCell(cell);
    }
    
    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.Regionlike#getHeight()
     */
    @Override
    public int getHeight() {
        return this.size_y;
    }
    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.Regionlike#getInsideIterator()
     */
    @Override
    public RegionIterator getInsideIterator() {
        RegionIterator ret = new BoxRegionIterator(this, false, false);
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.blacken.grid.Regionlike#getNotOutsideIterator()
     */
    @Override
    public RegionIterator getNotOutsideIterator() {
        RegionIterator ret = new BoxRegionIterator(this, false, true);
        return ret;
    }

    @Override
    public Sizable getSize() {
        return new SimpleSize(size_y, size_x);
    }

    @Override
    public int getWidth() {
        return this.size_x;
    }

    @Override
    public int getX() {
        return start_x;
    }

    @Override
    public int getY() {
        return start_y;
    }

    @Override
    public boolean intersects(int height, int width, int y1, int x1) {
        return BoxRegion.intersects(this, height, width, y1, x1);
    }

    @Override
    public boolean intersects(Regionlike room) {
        return BoxRegion.intersects(this, room);
    }

    public Grid<Z> like() {
        // not quite a copy, instead we want a similar map
        return new Grid<>(empty, size_y, size_x, start_y, start_x, irregular);
    }
    
    /**
     * Create a line on a grid
     * 
     * @param y0 starting row
     * @param x0 starting column
     * @param y1 ending row
     * @param x1 ending column
     * @param cell cell contents
     */
    public void line(int y0, int x0, int y1, int x1, Z cell) {
        for (Positionable pos : new LineIterator(y0, x0, y1, x1)) {
            set(pos, cell);
        }
    }

    /**
     * Create a line on a grid (using Positionables)
     * 
     * @param start starting position
     * @param end ending position
     * @param cell cell contents
     */
    public void line(Positionable start, Positionable end, Z cell) {
        for (Positionable pos : new LineIterator(start, end)) {
            set(pos, cell);
        }
    }
    
    /**
     * <p>Turn this to an irregular grid.</p>
     * 
     * <p>This will cause the grid to raise an IrregularGridException if you
     * try to resize it. However, you will be able to set cells to 
     * <code>null</code> without raising a NullPointerException.</p>
     */
    public void makeIrregular() {
        if (this.irregular) {
            return;
        }
        this.irregular = true;
    }

    /**
     * Turn this to a regular grid.
     * 
     * This turns an irregular grid in to a regular grid by replacing the
     * <code>null</code> cells with empty cells.
     * 
     * @param filler used to fill <code>null</code> grid gaps
     */
    public void makeRegular(Z filler) {
        if (!this.irregular) {
            return;
        }
        if (filler == null) {
            if (this.empty == null) {
                throw new NullPointerException();
            }
            filler = this.empty;
        }
        for (int y = start_y; y < start_y + size_y; y++) {
            for (int x = start_x; x < start_x + size_x; x++) {
                Z c = get(y, x);
                if (c == null) {
                    set(y, x, cellCopier.copyCell(filler));
                }
            }
        }
        this.irregular = false;
    }

    /**
     * Reset the grid.
     * 
     * <p>While this does reset the grid using the new empty value (if not
     * null -- if null it leaves the empty value untouched). It does not
     * alter the regularity of the grid. An irregular grid remains irregular,
     * even though this function may be setting every cell to a non-null 
     * value.</p>
     * 
     * @param ysize new row count
     * @param xsize new column count
     * @param empty new cell template
     */
    public void reset(int ysize, int xsize, Z empty) {
        if (empty != null) {
            this.empty = cellCopier.copyCell(empty);
        }
        resize(ysize, xsize, true);
    }

    /**
     * Resize the grid, optionally wiping it. (Non-overridable.)
     *
     * <p>We use a private, internal resize command to make sure that when we
     * call it from the constructor to set things up, it doesn't trigger
     * derived-class weirdness.
     *
     * @param ysize -1 for current value
     * @param xsize -1 for current value
     * @param wipe
     */
    private void internal_resize(int ysize, int xsize, boolean wipe) {
        if (ysize == -1) {
            ysize = this.size_y;
        }
        if (xsize == -1) {
            xsize = this.size_x;
        }
        if (this.irregular) {
            if (!wipe) {
                if (size_x == 0 && size_y == 0) {
                    // legal -- we're doing the initial sizing
                } else if (ysize != size_y || xsize != size_x) {
                    throw new IrregularGridException("Can not change the size of an irregular map.");
                }
            }
        } else if ((size_x != xsize || size_y != ysize) && this.empty == null) {
            throw new NullPointerException();
        }
        int x2 = Math.min(size_x, xsize);
        int y2 = Math.min(size_y, ysize);
        ArrayList<ArrayList<Z>> old_grid = grid;
        if (wipe) {
            old_grid = null;
        }
        ArrayList<ArrayList<Z>> new_grid = new ArrayList<>();
        if(ysize != 0) {
            new_grid.ensureCapacity(ysize);
        }
        for (int y = 0; y < ysize; y++) {
            ArrayList<Z> grid_line = new ArrayList<>();
            if (xsize != 0) {
                grid_line.ensureCapacity(xsize);
            }
            if (y < y2 && x2 > 0) {
                if (this.irregular) {
                    for (int x = 0; x < x2; x++) {
                        if (old_grid == null || old_grid.get(y).get(x) == null) {
                            grid_line.add(null);
                        } else {
                            grid_line.add(cellCopier.copyCell(empty));
                        }
                    }
                } else if (old_grid != null) {
                    grid_line.addAll(old_grid.get(y).subList(0, x2));
                }
            }
            int x1 = grid_line.size();
            for (int x = x1; x < xsize; x++) {
                grid_line.add(cellCopier.copyCell(empty));
            }
            new_grid.add(grid_line);
        }
        grid = new_grid;
        size_x = xsize;
        size_y = ysize;
    }

    /**
     * Resize the grid, optionally wiping it.
     *
     * @param ysize -1 for current value
     * @param xsize -1 for current value
     * @param wipe true to wipe; false to leave existing cells
     */
    protected void resize(int ysize, int xsize, boolean wipe) {
        internal_resize(ysize,xsize, wipe);
    }

    /**
     * Set a cell to a value.
     * 
     * <p?This supports irregular grid shapes. Irregularly shaped grids can 
     * use this function to set a cell to <code>null</code>. Normal grids 
     * will have a <code>NullPointerException</code> thrown if they try.
     * 
     * @param y coordinate
     * @param x coordinate
     * @param value new value for cell
     * @return previous cell value
     */
    public Z set(int y, int x, Z value) {
        if (value == null) {
            if (!this.irregular) {
                throw new NullPointerException("Use 'unset' to clear a cell.");
            }
        }
        return grid.get(y - start_y).set(x - start_x, value); 
    }

    /**
     * Set a cell to a value.
     * 
     * This supports irregular grid shapes. Irregularly shaped grids can use
     * this function to set a cell to <code>null</code>. Normal grids will
     * have a NullPointerException thrown if they try.
     * 
     * @param <T> A Positionable type
     * @param pos the position of a cell
     * @param value a new value for cell
     * @return previous cell value
     */
    public <T extends Positionable> Z set (T pos, Z value) {
        if (value == null) {
            if (!this.irregular) {
                throw new NullPointerException("Use 'unset' to clear a cell.");
            }
        }
        return grid.get(pos.getY() - start_y).set(pos.getX() - start_x, value); 
    }

    /**
     * Set the cell to a copy of the <code>value</code>.
     * 
     * @param y coordinate
     * @param x coordinate
     * @param value new cell value
     * @return old cell value.
     */
    public Z setCopy(int y, int x, Z value) {
        int y1 = y - start_y;
        int x1 = x - start_x;
        if (value == null) {
            if (!this.irregular) {
                throw new NullPointerException("Use 'unset' to clear a cell.");
            }
            return grid.get(y1).set(x1, null);
        }
        return grid.get(y1).set(x1, cellCopier.copyCell(value));
    }

    /**
     * Set a cell to a copy of a <code>value</code>.
     * 
     * @param <T> positionable item
     * @param pos position
     * @param value new cell value
     * @return old cell value
     */
    public <T extends Positionable> Z setCopy (T pos, Z value) {
        int y1 = pos.getY() - start_y;
        int x1 = pos.getX() - start_x;
        if (value == null) {
            if (!this.irregular) {
                throw new NullPointerException("Use 'unset' to clear a cell.");
            }
            return grid.get(y1).set(x1, null); 
        }
        return grid.get(y1).set(x1, cellCopier.copyCell(value));
    }

    @Override
    public void setHeight(int height) {
        resize(height, this.size_x, false);
    }

    @Override
    public void setPosition(int y, int x) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void setPosition(Positionable point) {
        this.setX(point.getX());
        this.setY(point.getY());
    }
    
    /**
     * Set the size.
     * 
     * @param ysize new Y size
     * @param xsize new X size
     */
    @Override
    public void setSize(int ysize, int xsize) {
        resize(ysize, xsize, false);
    }

    /**
     * Set the size.
     * 
     * @param size new size
     */
    @Override
    public void setSize(Sizable size) {
        setSize(size.getHeight(), size.getWidth());
    }

    /**
     * Set the bounds size.
     * @param bounds the bounds 
     */
    @Override
    public void setBounds(Regionlike bounds) {
        setPosition(bounds.getY(), bounds.getX());
        setSize(bounds.getHeight(), bounds.getWidth());
    }

    @Override
    public void setWidth(int width) {
        resize(this.size_y, width, false);
    }

    @Override
    public void setX(int x) {
        start_x = x;
    }
    
    @Override
    public void setY(int y) {
        start_y = y;
    }

    /**
     * <p>Pull a copy of a grid in to a subgrid (by ints).</p>
     * 
     * <p>While the returned grid doesn't contain any <code>null</code>'s it is
     * still returned as an IrregularGrid. This allows the subgrid to have
     * segments which have been removed (useful if the subgrid will be 
     * reapplied to the grid).</p>
     * 
     * <p>The returned grid has the starting upper-left coordinate equal to
     * the base coordinate (y1, x1). That is, if a cell is at 123,345 in
     * the original grid, it will still be in that cell in the subgrid.</p>
     * 
     * @param numRows number of rows
     * @param numCols number of columns
     * @param y1 start Y coordinate
     * @param x1 start X coordinate
     * @return returns the new subgrid
     */
    public Grid<Z> subGrid(int numRows, int numCols, int y1, int x1) {
        // LOGGER.debug("Creating new subgrid");
        Grid<Z> ret = new Grid<>(this.empty, numRows, numCols, y1, x1, true);
        for (int y = y1; y < y1 + numRows; y++) {
            for (int x = x1; x < x1 + numCols; x++) {
                Z cell = get(y, x);
                if (cell != null) {
                    ret.setCopy(y, x, cell);
                }
            }
        }
        return ret;
    }

    /**
     * Pull a copy of a grid in to a subgrid (by region).
     * 
     * <p>The returned grid is an irregular grid. That is, the cells outside of
     * the region are <code>null</code>.</p>
     * 
     * <p>The returned grid has the starting upper-left coordinate equal to
     * the base coordinate of the region. That is, if a cell is at 123,345 in
     * the original grid, it will still be in that cell in the subgrid.</p>
     * 
     * @param r region used for location of subgrid
     * @return new grid
     */
    public Grid<Z> subGrid(Regionlike r) {
        Grid<Z> ret = new Grid<>(this.empty, r.getHeight(), r.getWidth(), 
                                  r.getY(), r.getX());

        RegionIterator inside = r.getNotOutsideIterator();
        int[] p = new int[4];
        boolean[] pattern;
        int segtype;
        
        while (!inside.isDone()) {
            segtype = inside.currentSegment(p);
            if (segtype == RegionIterator.SEG_BORDER_SOLID || 
                    segtype == RegionIterator.SEG_INSIDE_SOLID) {
                for (int y = p[0]; y <= p[2]; y++) {
                    for (int x = p[1]; x <= p[3]; x++) {
                        if (!contains(y, x)) {
                            continue;
                        }
                        ret.setCopy(y, x, get(y, x));
                    }
                }
            } else if (segtype == RegionIterator.SEG_BORDER_PATTERNED || 
                    segtype == RegionIterator.SEG_INSIDE_PATTERNED) {
                int pidx = -1;
                pattern = inside.currentPattern();
                for (int y = p[0]; y <= p[2]; y++) {
                    for (int x = p[1]; x <= p[3]; x++) {
                        pidx++;
                        if (pidx >= pattern.length) {
                            pidx = 0;
                        }
                        if (!pattern[pidx]) {
                            continue;
                        }
                        if (!contains(y, x)) {
                            continue;
                        }
                        ret.setCopy(y, x, get(y, x));
                    }
                }
            } else if (segtype == RegionIterator.SEG_COMPLETE) {
                // should never happen, but just in case...
                break;
            }
            inside.next();
        }
        
        return ret;
    }

    /**
     * <p>Set the (y, x) coordinate to the stored empty cell.</p>
     * 
     * @param y coordinate
     * @param x coordinate
     * @return previous value for cell
     */
    public Z unset(int y, int x) {
        return setCopy(y, x, empty);
    }

    /**
     * Set the (Positionable) coordinate to the stored empty cell.
     * 
     * @param pos position of the cell
     * @return previous value for cell
     */
    public Z unset(Positionable pos) {
        return setCopy(pos.getY(), pos.getX(), empty);
    }

    /**
     * Clear a specific region using the default empty value.
     * 
     * @param height height to wipe
     * @param width width to wipe
     * @param y1 coordinate to start
     * @param x1 coordinate to start
     */
    public void wipe(int height, int width, int y1, int x1) {
        wipe(height, width, y1, x1, empty);
    }

    /**
     * This is like clear(), but for a specific region.
     * 
     * This works with irregular regions. For null cells it doesn't overwrite
     * anything.
     * 
     * @param height height of the region
     * @param width width of the region
     * @param y1 anchor point (upper-left corner)
     * @param x1 anchor point (upper-left corner)
     * @param empty new empty value
     */
    public void wipe(int height, int width, int y1, int x1, Z empty) {
        // LOGGER.debug(String.format("wipe(numRows:%s, numCols:%s, origY:%s, origX:%s)", height, width, y1, x1));
        for (int y = y1; y < y1 + height; y++) {
            for (int x = x1; x < x1 + width; x++) {
                if (this.get(y, x) != null) {
                    // LOGGER.debug(String.format("SetCopy: (%s) at %s,%s", empty, y, x));
                    this.setCopy(y, x, empty);
                }
            }
        }
    }

    /**
     * Add one grid to another.
     *
     * <p>At first glance this function doesn't look useful. For the Grid
     * class it <em>isn't</em> useful. This function exists so we can have the
     * same addGrid logic used by both the Grid and {@link GridView} classes.</p>
     *
     * <p>This function doesn't go public until Blacken 2.0 -- when we get
     * GridView and Gridlike.</p>
     *
     * @param <T>
     * @param to
     * @param from
     * @since 2.0
     */
    private static <T> void addGrid(Grid<T> to, Grid<T> from) {
        // LOGGER.debug("Adding grid on top of this grid");
        if (from == null) {
            LOGGER.error("Null grid passed to Grid.addGrid()");
            return;
        }
        if (to == null) {
            throw new NullPointerException("Can not modify a null Gridlike");
        }
        for (int y = from.getY(); y < from.getY() + from.getHeight() ; y++) {
            if (y < to.getY() || y >= to.getY() + to.getHeight()) {
                // LOGGER.debug(String.format("Skipping ROW: %s,-", y));
                continue;
            }
            for (int x = from.getX(); x < from.getX() + from.getWidth(); x++) {
                if (x < to.getX() || x >= to.getX() + to.getWidth()) {
                    // LOGGER.debug(String.format("Skipping COL: %s,%s", y + to.getY(), x + to.getX()));
                    continue;
                }
                T cell = from.get(y, x);
                if (cell != null) {
                    to.setCopy(y, x, cell);
                    // LOGGER.debug(String.format("Copying: (%s) at %s,%s", cell.toString(), y, x));
                }
            }
        }
    }

    /**
     * Add a grid on top of the existing grid.
     * 
     * <p>This works best when the grid to copy is an irregular grid which
     * contains transparent values.</p>
     * 
     * @param source grid to copy
     */
    public void addGrid(Grid<Z> source) {
        addGrid(this, source);
    }

    /**
     * Move a block of cells within a single grid.
     * 
     * @param numRows number of rows to move
     * @param numCols number of columns to move
     * @param origY original Y coordinate
     * @param origX original X coordinate
     * @param newY new Y coordinate
     * @param newX new X coordinate
     * @param resetCell class/function to refresh a cell
     */
    public void moveBlock(int numRows, int numCols, int origY, int origX,
                          int newY, int newX, DirtyGridCell<Z> resetCell) {
        this.copyFrom(this, numRows, numCols, origY, origX, newY, newX, 
                      resetCell);
    }

    /**
     * Move a block of cells between grids.
     * 
     * <p>There is different behavior if you're copying from the same Grid or
     * a different Grid. If it is the same Grid, we clear the source location
     * so that you can use this to move blocks around the screen. This is
     * usually what is desired and removes the need to have the caller perform
     * "smear cleanup" before the Grid is presentable to the end-user.
     * 
     * <p>If you're copying from a different Grid, however, there's no issue
     * of smear. Copying from a Grid never causes visual smear, so there's no
     * need to perform smear cleanup. The source remains pristine and the 
     * destination only gets modified due to the copy from the source.
     * 
     * <p>Note that when dealing with the same Grid it sounds like identical
     * logic to what is performed by 
     * {@link #moveBlock(int, int, int, int, int, int, DirtyGridCell)}. There's
     * a reason for that. moveBlock() is a convenience function added for
     * semantic clarity in the code.
     * 
     * @param source source grid
     * @param numRows number of rows to move
     * @param numCols number of columns to move
     * @param startY start Y coordinate
     * @param startX start X coordinate
     * @param destY destination Y coordinate
     * @param destX destination X coordinate
     * @param resetCell class/function to refresh a cell
     */
    public void copyFrom(Grid<Z> source, int numRows, int numCols,
                         int startY, int startX, int destY, int destX, 
                         DirtyGridCell<Z> resetCell) {
        Grid<Z> tgrid;
        if (source == this) {
            tgrid = source.cutSubGrid(numRows, numCols, startY, startX);
        } else {
            tgrid = source.copySubGrid(numRows, numCols, startY, startX);
        }
        Point startingPoint = new Point(tgrid);
        tgrid.setPosition(destY, destX);
        this.addGrid(tgrid);
        if (resetCell != null) {
            // LOGGER.debug("Setting dirty state on copied cells");
            for (int y = 0; y < numRows; y++) {
                for (int x = 0; x < numCols; x++) {
                    Z cell = tgrid.get(y + tgrid.getY(), x + tgrid.getX());
                    if (cell == null) {
                        continue;
                    }
                    if (resetCell != null) {
                        resetCell.setDirty(grid.get(y+start_y).get(x+start_x), true);
                    }
                }
            }
        }
        tgrid.setPosition(startingPoint);
    }

    @Override
    public Positionable getPosition() {
        return new Point(this);
    }

    /**
     * Create a subgrid that is a copy of a section of this grid.
     * 
     * @param numRows
     * @param numCols
     * @param startY
     * @param startX
     * @return 
     */
    public Grid<Z> copySubGrid(int numRows, int numCols, int startY, int startX) {
        return this.subGrid(numRows, numCols, startY, startX);
    }

    /**
     * Create a subgrid that cuts (takes ownership of) the cells from this grid.
     *
     * @param numRows
     * @param numCols
     * @param y1
     * @param x1
     * @return
     */
    public Grid<Z> cutSubGrid(int numRows, int numCols, int y1, int x1) {
        Grid<Z> ret = new Grid<>(null, numRows, numCols, y1, x1, true);
        for (int y = y1; y < y1 + numRows; y++) {
            for (int x = x1; x < x1 + numCols; x++) {
                Z cell = get(y, x);
                if (cell != null) {
                    ret.set(y, x, cell);
                    setCopy(y, x, empty);
                }
            }
        }
        return ret;
    }

    @Override
    public void setBounds(int height, int width, int y, int x) {
        setPosition(y, x);
        setSize(height, width);
    }

    /**
     * Get the current cell copier.
     *
     * <p>Normally this shouldn't be needed except to directly access a
     * particular cell copier implementation. To simply
     * create a copy of a cell with the current cell copier, use
     * {@link #copyCell(java.lang.Object)}
     *
     * @return
     */
    public GridCellCopier<Z> getCellCopier() {
        return cellCopier;
    }

    /**
     * Specify an alternative cell copier implementation.
     *
     * <p>This can be used to avoid introspection.
     *
     * @param cellCopier null to restore the default implementation
     */
    public void setCellCopier(GridCellCopier<Z> cellCopier) {
        if (cellCopier == null) {
            cellCopier = new FlexibleCellCopier<>();
        }
        this.cellCopier = cellCopier;
    }

    Object writeReplace() throws ObjectStreamException {
        GridData1<Z> data = new GridData1<>();
        Class myClass = this.getClass();
        Field[] fields = myClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                int modifiers = field.getModifiers();
                if (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers)) {
                    continue;
                }
                data.set(field.getName(), field.get(this));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new RuntimeException("failed to get member", ex);
            }
        }
        return data;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.grid);
        hash = 11 * hash + this.start_x;
        hash = 11 * hash + this.start_y;
        hash = 11 * hash + Objects.hashCode(this.empty);
        hash = 11 * hash + (this.irregular ? 1 : 0);
        hash = 11 * hash + Objects.hashCode(this.cellCopier.getClass().toString());
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
        final Grid<Z> other = (Grid<Z>) obj;
        if (!Objects.equals(this.grid, other.grid)) {
            return false;
        }
        if (this.start_x != other.start_x) {
            return false;
        }
        if (this.start_y != other.start_y) {
            return false;
        }
        if (!Objects.equals(this.empty, other.empty)) {
            return false;
        }
        if (this.irregular != other.irregular) {
            return false;
        }
        if (!Objects.equals(this.cellCopier.getClass().getName(), other.cellCopier.getClass().getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Grid{" + "start_x=" + start_x + ", start_y=" + start_y +
                ", size_x=" + size_x + ", size_y=" + size_y +
                ", empty=" + empty + ", irregular=" + irregular + ", grid=@" +
                grid.hashCode() + ", cellCopier=" +
                cellCopier.getClass().getName() + "}";
    }

}
