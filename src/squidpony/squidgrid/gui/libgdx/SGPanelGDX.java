package squidpony.squidgrid.gui.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import java.awt.Color;
import java.awt.Font;
import java.util.TreeMap;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.SGPane;
import squidpony.squidgrid.gui.awt.TextCellFactory;

/**
 * A libGDX implementation of SPanel that allows both text and graphics.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class SGPanelGDX extends Group implements SGPane {

    private TextCellFactory textFactory = new TextCellFactory();
    private TreeMap<String, Pixmap> imageMap = new TreeMap<>();
    private Pixmap backgroundImage, foregroundImage;
    private Image backImage, foreImage;
    private int cellWidth, cellHeight, gridWidth, gridHeight;
    private boolean[][] imageChanged;
    private Font font;
    private Color defaultBackColor = SColor.CHARTREUSE;
    private Color defaultForeColor = SColor.ALIZARIN;

    @Override
    public int getCellHeight() {
        return cellHeight;
    }

    @Override
    public int getCellWidth() {
        return cellWidth;
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    @Override
    public void initialize(int cellWidth, int cellHeight, int gridWidth, int gridHeight, Font font) {
        textFactory.initializeBySize(cellWidth, cellHeight, font);
        this.font = textFactory.getFont();
        doInitialization(gridWidth, gridHeight);
    }

    @Override
    public void initialize(int gridWidth, int gridHeight, Font font) {
        textFactory.initializeByFont(font);
        this.font = textFactory.getFont();
        doInitialization(gridWidth, gridHeight);
    }

    /**
     * Clears backing arrays and sets fields to proper size for the new grid
     * size.
     */
    private void doInitialization(int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        imageChanged = new boolean[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                imageChanged[x][y] = true;
            }
        }

        cellWidth = textFactory.getCellDimension().width;
        cellHeight = textFactory.getCellDimension().height;

        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        this.setWidth(w);
        this.setHeight(h);
        this.setOrigin(0, 0);
        this.setPosition(0, 0);
        backgroundImage = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        foregroundImage = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        backImage = new Image();
        foreImage = new Image();
        refresh();
    }

    @Override
    public void clearCell(int x, int y) {
        placeCharacter(x, y, ' ', defaultBackColor, defaultBackColor);
    }

    @Override
    public void clearCell(int x, int y, Color color) {
        placeCharacter(x, y, ' ', color, color);
    }

    @Override
    public void placeCharacter(int x, int y, char c) {
        placeCharacter(x, y, c, defaultForeColor, defaultBackColor);
    }

    @Override
    public void placeCharacter(int x, int y, char c, Color fore, Color back) {
        String key = textFactory.getStringRepresentationOf(' ', back, back);
        Pixmap background = imageMap.get(key);
        if (background == null) {
            background = PixmapFactory.createPixmap(textFactory.getImageFor(' ', fore, back));
            imageMap.put(key, background);
        }
        placeImage(x, y, background, false);

        if (c != ' ') {
            key = textFactory.getStringRepresentationOf(c, fore, back);
            Pixmap image = imageMap.get(key);
            if (image == null) {
                image = PixmapFactory.createPixmap(textFactory.getImageFor(c, fore, back));
                imageMap.put(key, image);
            }
            placeImage(x, y, image, true);
        }
    }

    @Override
    public void placeCharacter(int x, int y, char c, Color fore) {
        placeCharacter(x, y, c, fore, defaultBackColor);
    }

    @Override
    public void placeImage(int x, int y, String key) {
        Pixmap image = imageMap.get(key);
        if (image != null) {
            placeImage(x, y, image, true);
            imageChanged[x][y] = true;
        }
    }

    @Override
    public void placeImage(int x, int y, String key, Color background) {
        clearCell(x, y, background);

        Pixmap image = imageMap.get(key);
        if (image != null) {
            placeImage(x, y, image, true);
        }
        imageChanged[x][y] = true;
    }

    /**
     * Places an image at the given grid coordinate.
     *
     * @param x
     * @param y
     * @param image
     * @param foreground if true then the image will be placed in the foreground
     */
    public void placeImage(int x, int y, Pixmap image, boolean foreground) {
        imageChanged[x][y] = true;
        if (foreground) {
            foregroundImage.drawPixmap(image, getImageX(x), getImageY(y));
        } else {
            backgroundImage.drawPixmap(image, getImageX(x), getImageY(y));
        }
    }

    @Override
    public void placeText(int xOffset, int yOffset, char[][] chars) {
        placeText(xOffset, yOffset, chars, defaultForeColor, defaultBackColor);
    }

    @Override
    public void placeText(int xOffset, int yOffset, char[][] chars, Color foreground, Color background) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    placeCharacter(x, y, chars[x - xOffset][y - yOffset], foreground, background);
                }
            }
        }
    }

    @Override
    public void placeHorizontalString(int xOffset, int yOffset, String string) {
        placeHorizontalString(xOffset, yOffset, string, defaultForeColor, defaultBackColor);
    }

    @Override
    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        placeText(xOffset, yOffset, temp, foreground, background);
    }

    @Override
    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        placeText(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground, background);
    }

    @Override
    public void placeVerticalString(int xOffset, int yOffset, String string) {
        placeVerticalString(xOffset, yOffset, string, defaultForeColor, defaultBackColor);
    }

    @Override
    public void refresh() {
        removeActor(backImage);
        removeActor(foreImage);
        backImage = new Image(new Texture(backgroundImage));
        foreImage = new Image(new Texture(foregroundImage));
        backImage.setOrigin(0, 0);
        foreImage.setOrigin(0, 0);
        backImage.setPosition(0, 0);
        foreImage.setPosition(0, 0);
        addActor(backImage);
        addActor(foreImage);
    }

    @Override
    public void setDefaultBackground(Color defaultBackground) {
        this.defaultBackColor = defaultBackground;
    }

    @Override
    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeColor = defaultForeground;
    }

    @Override
    public void setText(char[][] chars) {
        placeText(0, 0, chars);
    }

    @Override
    public boolean willFit(char character) {
        return textFactory.willFit(character);
    }

    /**
     * Returns the x value of the cell containing the image point passed in.
     *
     * @param x
     * @return
     */
    private int getGridX(int x) {
        return cellWidth / x;
    }

    /**
     * Returns the y value of the cell containing the image point passed in.
     *
     * @param y
     * @return
     */
    private int getGridY(int y) {
        return cellHeight / y;
    }

    private int getImageX(int x) {
        return x * cellWidth;
    }

    private int getImageY(int y) {
        return y * cellHeight;
    }
}
