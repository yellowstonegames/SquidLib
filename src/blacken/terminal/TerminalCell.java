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
package com.googlecode.blacken.terminal;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.googlecode.blacken.grid.DirtyGridCell;

/**
 * A concrete terminal cell.
 * 
 * <p>Note that the {@link Set}s returned by this are all read-only.
 *
 * @author Steven Black
 *
 */
public class TerminalCell implements Cloneable, TerminalCellLike {

    /**
     * Helper class to reset the cell to known-good values.
     * 
     * @author Steven Black
     *
     */
    public static class ResetCell implements DirtyGridCell<TerminalCellLike> {
        /*
         * (non-Javadoc)
         * @see com.googlecode.blacken.grid.ResetGridCell#reset(com.googlecode.blacken.grid.Copyable)
         */
        @Override
        public void setDirty(TerminalCellLike cell, boolean isDirty) {
            cell.setDirty(isDirty);
        }
    }
    private String sequence = "\u0000";
    private int fg_color = 0xFFAAAAAA;
    private int bg_color = 0xFF000000;
    private Set<TerminalStyle> style = EnumSet.noneOf(TerminalStyle.class);
    private Set<CellWalls> walls = EnumSet.noneOf(CellWalls.class);
    private boolean dirty = true;

    /**
     * Create a new terminal cell with default settings.
     */
    public TerminalCell() {
        super();
    }
    public TerminalCell(String sequence) {
        if (sequence != null) {
            this.sequence = sequence;
        }
    }
    public TerminalCell(String sequence, Integer foreground, 
            Integer background) {
        if (sequence != null) {
            this.sequence = sequence;
        }
        if (foreground != null) {
            this.fg_color = foreground;
        }
        if (background != null) {
            this.bg_color = background;
        }
    }
    /**
     * Create a new TerminalCell, setting important things.
     * 
     * @param sequence visible character sequence
     * @param foreground foreground color
     * @param background background color
     * @param style terminal cell style
     * @param dirty dirty status
     */
    public TerminalCell(String sequence, Integer foreground, Integer background, 
                        Set<TerminalStyle> style, Boolean dirty) {
        if (sequence != null) {
            this.sequence = sequence;
        }
        if (style != null && !style.isEmpty()) {
            this.style = EnumSet.copyOf(style);
        }
        if (foreground != null) {
            this.fg_color = foreground;
        }
        if (background != null) {
            this.bg_color = background;
        }
        if (dirty != null) {
            this.dirty = dirty;
        }
    }

    /**
     * Create a new cell.
     * 
     * @param cell cell to base this one off of
     * @deprecated Use clone() or set() instead.
     */
    @Deprecated
    public TerminalCell(TerminalCellLike cell) {
        set(cell);
    }

    @Override
    public void addCellWalls(CellWalls walls) {
        if (walls == null) return;
        dirty = true;
        this.walls.add(walls);
    }

    @Override
    public void addSequence(int codepoint) {
        if (this.sequence.equals("\u0000")) {
            this.sequence = "\u25cc";
        }
        this.sequence += String.copyValueOf(Character.toChars(codepoint));
        dirty = true;
    }

    @Override
    public void addSequence(String sequence) {
        if (this.sequence.equals("\u0000")) {
            this.sequence = "\u25cc";
        }
        this.sequence += sequence;
        dirty = true;
    }

    @Override
    public void clearCellWalls() {
        this.walls = EnumSet.noneOf(CellWalls.class);
        dirty = true;
    }

    @Override
    public void clearStyle() {
        this.style = EnumSet.noneOf(TerminalStyle.class);
        dirty = true;
    }

    @Override
    public TerminalCell clone() {
        TerminalCell ret;
        ret = new TerminalCell();
        ret.set(this);
        ret.setDirty(this.dirty);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TerminalCell)) {
            return false;
        }
        TerminalCell that = (TerminalCell)obj;
        if (!sequence.equals(that.getSequence())) {
            return false;
        }
        if (fg_color != that.getForeground()) {
            return false;
        }
        if (bg_color != that.getBackground()) {
            return false;
        }
        if (!style.equals(that.getStyle())) {
            return false;
        }
        if (!walls.equals(that.getCellWalls())) {
            return false;
        }
        if (dirty != that.isDirty()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.sequence);
        hash = 89 * hash + this.fg_color;
        hash = 89 * hash + this.bg_color;
        hash = 89 * hash + Objects.hashCode(this.style);
        hash = 89 * hash + Objects.hashCode(this.walls);
        hash = 89 * hash + (this.dirty ? 1 : 0);
        return hash;
    }
    
    @Override
    public int getBackground() {
        return bg_color;
    }

    @Override
    public Set<CellWalls> getCellWalls() {
        if (this.walls == null) {
            return null;
        }
        return Collections.unmodifiableSet(this.walls);
    }

    @Override
    public int getForeground() {
        return fg_color;
    }

    @Override
    public String getSequence() {
        return sequence;
    }

    @Override
    public Set<TerminalStyle> getStyle() {
        if (this.style == null) {
            return null;
        }
        return Collections.unmodifiableSet(this.style);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void set(TerminalCellLike same) {
        this.sequence = same.getSequence();
        this.fg_color = same.getForeground();
        this.bg_color = same.getBackground();
        this.style = same.getStyle();
        this.walls = same.getCellWalls();
        this.dirty = true;
    }
    
    @Override
    public void setBackground(int background) {
        this.bg_color = background;
        dirty = true;
    }

    @Override
    public void setCellWalls(CellWalls walls) {
        if (walls == null) {
            clearCellWalls();
            return;
        }
        dirty = true;
        this.walls = EnumSet.of(walls);
    }
    
    @Override
    public void setCellWalls(Set<CellWalls> walls) {
        if (walls == null || walls.isEmpty()) {
            clearCellWalls();
            return;
        }
        dirty = true;
        this.walls = EnumSet.copyOf(walls);
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void setForeground(int foreground) {
        this.fg_color = foreground;
        dirty = true;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = String.copyValueOf(Character.toChars(sequence));
        dirty = true;
    }

    @Override
    public void setSequence(String sequence) {
        if (sequence == null) {
            this.sequence = "\u0000";
        } else {
            this.sequence = sequence;
        }
        dirty = true;
    }
    
    @Override
    public void setStyle(Set<TerminalStyle> style) {
        if (style == null || style.isEmpty()) {
            clearStyle();
            return;
        }
        dirty = true;
        this.style = EnumSet.copyOf(style);
    }

    @Override
    public void setStyle(TerminalStyle style) {
        if (style == null) {
            clearStyle();
            return;
        }
        dirty = true;
        this.style = EnumSet.of(style);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (sequence == null) {
            buf.append("null");
        } else {
            buf.append('"');
            for (char c : sequence.toCharArray()) {
                if (c < ' ' || c > 127) {
                    buf.append(String.format("\\u04x", c));
                } else {
                    buf.append(c);
                }
            }
            buf.append('"');
        }
        buf.append(String.format(", 0x%08x", fg_color));
        buf.append(String.format(", 0x%08x", bg_color));
        if (dirty) {
            buf.append(", DIRTY");
        } else {
            buf.append(", clean");
        }
        if (style == null || style.isEmpty()) {
            buf.append(", {}");
        } else {
            buf.append(", {");
            for (TerminalStyle sty : style) {
                buf.append(sty.name());
                buf.append(", ");
            }
            buf.append("}");
        }
        if (walls == null || walls.isEmpty()) {
            buf.append(", {}");
        } else {
            buf.append(", {");
            for (CellWalls wall : walls) {
                buf.append(wall.name());
                buf.append(", ");
            }
            buf.append("}");
            
        }
        return buf.toString();
    }

}
