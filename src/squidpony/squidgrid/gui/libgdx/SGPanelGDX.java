package squidpony.squidgrid.gui.libgdx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.TreeMap;
import squidpony.squidgrid.gui.awt.ImageCellMap;
import squidpony.squidgrid.gui.awt.TextCellFactory;

/**
 * A libGDX implementation of SPanel that allows both text and graphics.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class SGPanelGDX extends Group {

    private TextCellFactory textFactory = new TextCellFactory();
    private ImageCellMap imageFactory = new ImageCellMap(new Dimension(20, 20));
    private TreeMap<BufferedImage, Pixmap> imageMap = new TreeMap<BufferedImage, Pixmap>();
    private Image background;
    private int cellWidth, cellHeight, gridWidth, gridHeight;
}
