/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.blacken.cell;

/**
 * Copy a grid's cell.
 *
 * <p>This exists to help avoid introspection when copying cells.
 *
 * @author Steven Black
 * @param <Z> cell type
 */
public interface GridCellCopier<Z> {
    /**
     * Copy a cell in the most efficient way.
     *
     * @param source
     * @return copy of <code>source</code>
     */
    public Z copyCell(Z source);
}
