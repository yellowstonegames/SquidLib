package squidpony.squidgrid;

import java.awt.*;
import java.awt.image.*;
import javax.swing.JPanel;

/**
 * This class is a JPanel that will display a text string as a monospaced font
 * regardless of the font's actual spacing. This is accomplished by displaying
 * the text as a graphic.
 *
 * @author Eben Howard
 */
public class ConcreteSGTextPanel extends JPanel implements SGTextDisplay {
    //the array of the contents of the screen
    private BufferedImage[][] contents;
    private int rows, columns;
    private Dimension cellDimension, panelDimension;
    private BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
    TextBlockFactory factory = TextBlockFactory.getInstance();

    /**
     * Builds a new panel with the desired traits.
     * 
     * @param cellWidth Desired width in pixels. May be adjusted to allow cells to all be same size
     * @param cellHeight Desired height in pixels. May be adjusted to allow cells to all be same size
     * @param rows Number of cells horizontally.
     * @param columns Number of cells vertically.
     * @param font Base
     */
    public ConcreteSGTextPanel(int cellWidth, int cellHeight, int rows, int columns, Font font) {
        initialize(cellWidth, cellHeight, rows, columns, font);
    }

    /**
     * Builds a panel with the given Font determining the size of the cell dimensions.
     * 
     * @param rows
     * @param columns
     * @param font 
     */
    public ConcreteSGTextPanel(int rows, int columns, Font font) {
        initialize(rows, columns, font);
    }

    /**
     * Empty constructor to allow use for drag and drop in NetBeans.
     */
    public ConcreteSGTextPanel() {
    }

    private void redrawImage() {
        Graphics2D g2 = image.createGraphics();
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                g2.drawImage(contents[x][y], x * cellDimension.width, y * cellDimension.height, null);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

    @Override
    public Dimension getCellDimension() {
        return cellDimension;
    }

    @Override
    public void setCellDimension(Dimension cellDimension) {
        this.cellDimension = cellDimension;
        factory.setCellDimension(cellDimension);
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public void setColumns(int columns) {
        this.columns = columns;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public void setText(char[][] chars) {
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                setBlock(x, y, chars[x][y]);
            }
        }
    }

    @Override
    public void setBlock(int x, int y, char c) {
        setBlock(x, y, c, Color.BLACK, Color.WHITE);
    }

    @Override
    public void setBlock(int x, int y, char c, Color fore, Color back) {
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(factory.getImageFor(c, fore, back), x * cellDimension.width, y * cellDimension.height, null);
    }

    @Override
    public void refresh() {
        redrawImage();
    }

    @Override
    public void initialize(int width, int height, int rows, int columns, Font font) {
        factory.initializeBySize(width, height, font, true);//ask for whitespace
        doInitialization(rows, columns);
    }

    @Override
    public void initialize(int rows, int columns, Font font) {
        factory.initializeByFont(font, true);//initialize with whitespace
        doInitialization(rows, columns);
    }

    private void doInitialization(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        contents = new BufferedImage[columns][rows];


        cellDimension = factory.getCellDimension();

        int w = columns * cellDimension.width;
        int h = rows * cellDimension.height;
        panelDimension = new Dimension(w, h);

        setSize(panelDimension);
        setMinimumSize(panelDimension);
        setPreferredSize(panelDimension);

        image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.ORANGE);
        g2.fillRect(0, 0, getWidth(), getHeight());//cover old image with blank orange block
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void ensureFits(char[] characters, boolean whiteSpace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean willFit(char character, boolean whiteSpace) {
        return factory.willFit(character, whiteSpace);
    }
}
