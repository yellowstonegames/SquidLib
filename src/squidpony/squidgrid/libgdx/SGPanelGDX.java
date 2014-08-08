package squidpony.squidgrid.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import java.awt.Color;
import java.awt.Font;
import java.util.TreeMap;
import squidpony.annotation.Beta;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.TextCellFactory;

/**
 * A libGDX implementation of SPanel that allows both text and graphics.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class SGPanelGDX extends Group {

    private TextCellFactory textFactory;
    private TreeMap<String, Pixmap> imageMap = new TreeMap<>();
    private Pixmap foregroundImage;
    private Image foreImage;
    private int cellWidth, cellHeight, gridWidth, gridHeight;
    private boolean[][] imageChanged;
    private Color defaultForeColor = SColor.WHITE;

    public int getCellHeight() {
        return cellHeight;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void initialize(int cellWidth, int cellHeight, int gridWidth, int gridHeight, Font font) {
        textFactory = new TextCellFactory(font, cellWidth, cellHeight);
        doInitialization(gridWidth, gridHeight);
    }

    public void initialize(int gridWidth, int gridHeight, Font font) {
        textFactory = new TextCellFactory(font);
        doInitialization(gridWidth, gridHeight);
    }

    /**
     * Clears backing arrays and sets fields to proper size for the new grid size.
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

        cellWidth = textFactory.width();
        cellHeight = textFactory.height();

        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        this.setWidth(w);
        this.setHeight(h);
        this.setOrigin(0, 0);
        this.setPosition(0, 0);
        foregroundImage = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        foreImage = new Image();
        refresh();
    }

    public void clearCell(int x, int y) {
        placeCharacter(x, y, ' ');
    }

    public void clearCell(int x, int y, Color color) {
        placeCharacter(x, y, ' ', color);
    }

    public void placeCharacter(int x, int y, char c) {
        placeCharacter(x, y, c, defaultForeColor);
    }

    public void placeCharacter(int x, int y, char c, Color fore) {
        if (c != ' ') {
            String key = textFactory.asKey(c, fore);
            Pixmap image = imageMap.get(key);
            if (image == null) {
                image = PixmapFactory.createPixmap(textFactory.get(c, fore));
                imageMap.put(key, image);
            }
            placeImage(x, y, image);
        }
    }

    public void placeImage(int x, int y, String key) {
        Pixmap image = imageMap.get(key);
        if (image != null) {
            placeImage(x, y, image);
            imageChanged[x][y] = true;
        }
    }

    public void placeImage(int x, int y, String key, Color background) {
        clearCell(x, y, background);

        Pixmap image = imageMap.get(key);
        if (image != null) {
            placeImage(x, y, image);
        }
        imageChanged[x][y] = true;
    }

    /**
     * Places an image at the given grid coordinate.
     *
     * @param x
     * @param y
     * @param image
     */
    public void placeImage(int x, int y, Pixmap image) {
        imageChanged[x][y] = true;
            foregroundImage.drawPixmap(image, getImageX(x), getImageY(y));
       
    }

    public void placeText(int xOffset, int yOffset, char[][] chars) {
        placeText(xOffset, yOffset, chars, defaultForeColor);
    }

    public void placeText(int xOffset, int yOffset, char[][] chars, Color foreground) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                        placeCharacter(x, y, chars[x - xOffset][y - yOffset], foreground);
                }
            }
        }
    }

    public void placeHorizontalString(int xOffset, int yOffset, String string) {
        placeHorizontalString(xOffset, yOffset, string, defaultForeColor);
    }

    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        placeText(xOffset, yOffset, temp, foreground);
    }

    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground) {
        placeText(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground);
    }

    public void placeVerticalString(int xOffset, int yOffset, String string) {
        placeVerticalString(xOffset, yOffset, string, defaultForeColor);
    }

    public void refresh() {
        removeActor(foreImage);
        foreImage = new Image(new Texture(foregroundImage));
        foreImage.setOrigin(0, 0);
        foreImage.setPosition(0, 0);
        addActor(foreImage);
    }

    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeColor = defaultForeground;
    }

    public void setText(char[][] chars) {
        placeText(0, 0, chars);
    }

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
