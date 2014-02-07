package squidpony.squidgrid.generation;

import java.awt.Shape;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class MapGenerationTester {

    public static void main(String... args) {
        new MapGenerationTester().go();
    }

    private void go() {
//        printMap(TiledShape.buildRectangle(15, 9, 3), 0.9);
//        printMap(TiledShape.buildRadialShape(BasicRadiusStrategy.CIRCLE, 10, 25, 25, 3, 0.5));
        Shape shape;
//        shape = new Rectangle2D.Double(0, 0, 100, 50);
//        shape = new QuadCurve2D.Double(0, 30, 25, -40, 200, 50);
//        printMap(new TiledShape(shape, 10.0, false, true), 0.2);
        printMap(loadShapeImage("sample.png"), 0.7);
    }

    private void printMap(TiledShape shape, double sparsity) {
        System.out.println(shape);
        System.out.println(shape.buildSparseShape(sparsity));
    }

    private static TiledShape loadShapeImage(String name) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File("./assets/" + name));
        } catch (IOException ex) {
            Logger.getLogger(MapGenerationTester.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new TiledShape(image);
    }
}
