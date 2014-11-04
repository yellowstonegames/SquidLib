package squidpony.squidgrid.gui;

import java.awt.Font;

/**
 * This class is used to set the properties desired in a TextCellFactory object on creation.
 *
 * The builder defaults to using the JVM's default Serif font at size 12, no padding, and antialias on. If more
 * vertical padding is needed, it is recommended to try adding it mostly to the top. Often a padding of 1 on top
 * can give a good appearance.
 *
 * If the width and height are set then the factory will size the cells to those dimensions rather than to the font
 * size. In this case the font size is treated as an upper limit, and so a small font size in a large cell will render
 * at that font size, but a large font size in a small cell will be shrunk until it can fit inside the defined cell size.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TextCellFactoryBuilder {

    protected Font font;
    protected String fitting;
    protected boolean antialias;
    protected int leftPadding, rightPadding, topPadding, bottomPadding;
    protected int width, height;

    public TextCellFactoryBuilder() {
        font(Font.decode("Serif"));
        fit(TextCellFactory.DEFAULT_FITTING);
        antialias(true);
        padding(0);
        width(0);
        height(0);
    }

    public TextCellFactoryBuilder font(Font font) {
        this.font = font;
        return this;
    }

    /**
     * Sets the characters that will be guaranteed to fit to the provided ones.
     * Will override any previously set string.
     *
     * @param fit
     * @return
     */
    public TextCellFactoryBuilder fit(String fit) {
        this.fitting = fit;
        return this;
    }

    /**
     * Adds the characters in the string to the list of characters that will be guaranteed to fit.
     *
     * @param fit
     * @return
     */
    public TextCellFactoryBuilder addFit(String fit) {
        this.fitting += fit;
        return this;
    }

    /**
     * When set to true, all fonts will be rendered with antialiasing.
     *
     * @param antialias
     * @return
     */
    public TextCellFactoryBuilder antialias(boolean antialias) {
        this.antialias = antialias;
        return this;
    }

    /**
     * Sets the amount of padding on all sides to the provided value.
     *
     * @param padding
     * @return
     */
    public TextCellFactoryBuilder padding(int padding) {
        leftPadding = padding;
        rightPadding = padding;
        topPadding = padding;
        bottomPadding = padding;
        return this;
    }

    public TextCellFactoryBuilder leftPadding(int padding) {
        leftPadding = padding;
        return this;
    }

    public TextCellFactoryBuilder rightPadding(int padding) {
        rightPadding = padding;
        return this;
    }

    public TextCellFactoryBuilder topPadding(int padding) {
        topPadding = padding;
        return this;
    }

    public TextCellFactoryBuilder bottomPadding(int padding) {
        bottomPadding = padding;
        return this;
    }

    public TextCellFactoryBuilder width(int width) {
        this.width = width;
        return this;
    }

    public TextCellFactoryBuilder height(int height) {
        this.height = height;
        return this;
    }

}
