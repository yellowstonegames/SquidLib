package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.awt.Color;

/**
 * Class for creating text blocks.
 *
 * This class defaults to using one of the bundled sans fonts at size 12 px height, no
 * padding, and antialias on. If more vertical padding is needed, it is
 * recommended to try adding it mostly to the top. Antialias can't be turned off, so
 *
 * After all settings are set, one of the initialization methods must be called
 * before the factory can be used.
 *
 * In order to easily support Unicode, strings are treated as a series of code
 * points.
 *
 * All images have transparent backgrounds.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TextCellFactory {

    /**
     * The commonly used symbols in roguelike games.
     */
    public static final String DEFAULT_FITTING = "@!#$%^&*()_+1234567890-=~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz;:,'\"{}?/\\ ",
    LINE_FITTING = "┼├┤┴┬┌┐└┘│─", SQUID_FITTING = DEFAULT_FITTING + LINE_FITTING;

    private BitmapFont bmpFont = null;
    private Texture block = null;
    private String fitting = SQUID_FITTING;
    private int leftPadding = 0, rightPadding = 0, topPadding = 0, bottomPadding = 0;
    private int width = 1, height = 1;
    private boolean initialized = false;

    /**
     * Creates a default valued factory. One of the initialization methods must
     * be called before this factory can be used!
     */
    public TextCellFactory() {
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     *
     * Will match the width and height to 12 and 12, scaling the font to fit.
     *
     * Calling this after the factory has already been initialized will
     * re-initialize it.
     *
     * @return this for method chaining
     */
    public TextCellFactory initByFont() {
        bmpFont.setFixedWidthGlyphs(fitting);
        this.width = (int)bmpFont.getSpaceWidth();
        this.height = (int)(bmpFont.getLineHeight());
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        initialized = true;
        return this;
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     *
     * Will strictly use the provided width and height values to size the cells.
     *
     * Calling this after the factory has already been initialized will
     * re-initialize it.
     *
     * @return this for method chaining
     */
    public TextCellFactory initBySize() {
        bmpFont.setFixedWidthGlyphs(fitting);
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        initialized = true;
        return this;
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     *
     * (This is identical to initBySize() when using libGDX.)
     *
     * @return this for method chaining
     */
    public TextCellFactory initVerbatim() {
        return initBySize();
    }

    /**
     * Returns the font used by this factory.
     *
     * @return the font
     */
    public BitmapFont font() {
        return bmpFont;
    }

    /**
     * Sets this factory to use the provided font.
     *
     * @param fontpath the path to the font to use
     * @return this factory for method chaining
     */
    public TextCellFactory font(String fontpath) {
        this.bmpFont = new BitmapFont(Gdx.files.internal(fontpath));
        return this;
    }

    public TextCellFactory defaultNarrowFont()
    {
        bmpFont = DefaultResources.getLargeNarrowFont();
        return this;
    }

    public TextCellFactory defaultSquareFont()
    {
        bmpFont = DefaultResources.getDefaultFont();
        return this;
    }

    /**
     * Returns the width of a single cell.
     *
     * @return the width
     */
    public int width() {
        return width;
    }

    /**
     * Sets the factory's cell width to the provided value. Clamps at 1 on the
     * lower bound to ensure valid calculations.
     *
     * @param width the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory width(int width) {
        this.width = Math.max(1, width);
        return this;
    }

    /**
     * Returns the height of a single cell.
     *
     * @return
     */
    public int height() {
        return height;
    }

    /**
     * Sets the factory's cell height to the provided value. Clamps at 1 on the
     * lower bound to ensure valid calculations.
     *
     * @param height the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory height(int height) {
        this.height = Math.max(1, height);
        return this;
    }

    /**
     * Returns the current String of code points that are used for sizing the
     * cells.
     *
     * Note that this is actually a set of codepoints and treating them as an
     * array of chars might give undesired results.
     *
     * @return the String used for sizing calculations
     */
    public String fit() {
        return fitting;
    }

    /**
     * Sets the characters that will be guaranteed to fit to the provided ones.
     * This will override any previously set string.
     *
     * @param fit the String of code points to size to
     * @return this factory for method chaining
     */
    public TextCellFactory fit(String fit) {
        fitting = fit;
        bmpFont.setFixedWidthGlyphs(fitting);
        this.width = (int)bmpFont.getSpaceWidth();
        return this;
    }

    /**
     * Adds the code points in the string to the list of characters that will be
     * guaranteed to fit.
     *
     * @param fit the String of code points to size to
     * @return this factory for method chaining
     */
    public TextCellFactory addFit(String fit) {
        fitting += fit;
        bmpFont.setFixedWidthGlyphs(fitting);
        this.width = (int)bmpFont.getSpaceWidth();
        return this;
    }

    /**
     * Returns whether this factory is currently set to do antialiasing on the
     * characters rendered, which is always true.
     *
     * @return true if antialiasing is set
     */
    public boolean antialias() {
        return true;
    }

    /**
     * All fonts will be rendered with antialiasing, this doesn't do anything.
     *
     * @param antialias ignored, will always use antialiasing
     * @return this factory for method chaining
     * @deprecated AA is the wave of the future!
     */
    public TextCellFactory antialias(boolean antialias) {
        return this;
    }

    /**
     * Sets the amount of padding on all sides to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory padding(int padding) {
        leftPadding = padding;
        rightPadding = padding;
        topPadding = padding;
        bottomPadding = padding;
        return this;
    }
    
    /**
     * Returns the padding on the left side.
     * 
     * @return amount of padding in pixels
     */
    public int leftPadding() {
        return leftPadding;
    }

    /**
     * Sets the amount of padding on the left side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory leftPadding(int padding) {
        leftPadding = padding;
        return this;
    }

    /**
     * Returns the padding on the right side.
     * 
     * @return amount of padding in pixels
     */
    public int rightPadding() {
        return rightPadding;
    }
    
    /**
     * Sets the amount of padding on the right side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory rightPadding(int padding) {
        rightPadding = padding;
        return this;
    }

    /**
     * Returns the padding on the top side.
     * 
     * @return amount of padding in pixels
     */
    public int topPadding() {
        return topPadding;
    }
    
    /**
     * Sets the amount of padding on the top side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory topPadding(int padding) {
        topPadding = padding;
        return this;
    }

    /**
     * Returns the padding on the bottom side.
     * 
     * @return amount of padding in pixels
     */
    public int bottomPadding() {
        return bottomPadding;
    }

    /**
     * Sets the amount of padding on the bottom side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory bottomPadding(int padding) {
        bottomPadding = padding;
        return this;
    }
    /**
     * Returns true if this factory is fully initialized and ready to build text cells.
     * 
     * @return true if initialized
     */
    public boolean initialized() {
        return initialized;
    }


    /**
     * Returns true if the given character will fit inside the current cell
     * dimensions with the current font.
     *
     * ISO Control characters, non-printing characters and invalid unicode
     * characters are all considered by definition to fit.
     *
     * @param codepoint
     * @return
     */
    public boolean willFit(int codepoint) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        
        if (!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint)) {
            return true;
        }

        return fitting.contains(String.valueOf(Character.toChars(codepoint)));
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) with the default color (white), with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            batch.draw(block, x, y - height, width, height);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            batch.draw(block, x, y - height, width * s.length(), height);
        } else {
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }
    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified AWT Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param color the java.awt.Color to draw the char(s) with, all the same color
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, Color color, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            com.badlogic.gdx.graphics.Color orig = batch.getColor();
            batch.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            com.badlogic.gdx.graphics.Color orig = batch.getColor();
            batch.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            batch.draw(block, x, y - height, width * s.length(), height);
            batch.setColor(orig);
        } else {
            bmpFont.setColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }
    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified rgba color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param r 0.0 to 0.1 red value
     * @param g 0.0 to 0.1 green value
     * @param b 0.0 to 0.1 blue value
     * @param a 0.0 to 0.1 alpha value
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, float r, float g, float b, float a, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            com.badlogic.gdx.graphics.Color orig = batch.getColor();
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            com.badlogic.gdx.graphics.Color orig = batch.getColor();
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width * s.length(), height);
            batch.setColor(orig);
        } else {
            bmpFont.setColor(r, g, b, a);
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param color the LibGDX Color to draw the char(s) with, all the same color
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, com.badlogic.gdx.graphics.Color color, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        bmpFont.setColor(color);

        if (s == null) {
            com.badlogic.gdx.graphics.Color orig = batch.getColor();
            batch.setColor(color);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            com.badlogic.gdx.graphics.Color orig = batch.getColor();
            batch.setColor(color);
            batch.draw(block, x, y - height, width * s.length(), height);
            batch.setColor(orig);
        } else {
            bmpFont.setColor(color);
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }

    public Actor makeActor(String s, com.badlogic.gdx.graphics.Color color) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            Image im = new Image(block);
            im.setColor(color);
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            Image im = new Image(block);
            im.setColor(color);
            im.setSize(width * s.length(), height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Label lb = new Label(s, new Label.LabelStyle(bmpFont, null));
            lb.setColor(color);
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }

    /**
     * Returns a solid block of white, 1x1 pixel in size; can be drawn at other sizes by Batch.
     *
     * @return a white 1x1 pixel Texture.
     */
    public Texture getSolid() {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        return block;
    }

}
