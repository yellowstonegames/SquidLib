package squidpony.squidgrid;

import java.awt.Font;

/**
 * An extention to SGBlock which supports text data.
 *
 * @author SquidPony
 */
public interface SGTextBlock extends SGBlock{
    
    /**
     * Sets the string as a series of characters that will be displayed one
     * at a time in this block.
     * 
     * @param text 
     */
    public void setText(String text);
    
    /**
     * Sets the font to be used to the specified font. Does not force a redraw
     * of the character.
     * 
     * @param font 
     */
    public void setFont(Font font);
}
