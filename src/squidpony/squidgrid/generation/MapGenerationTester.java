package squidpony.squidgrid.generation;

import squidpony.squidgrid.shape.TiledShape;
import java.awt.Shape;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import squidpony.squidgrid.shape.ShapeGenerator;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class MapGenerationTester {

    public static void main(String... args) {
        MapGenerationTester test = new MapGenerationTester();
//        test.testHerringbone();
//        test.testStackBond();
//        test.testBrick();
        test.testRunningBond();
    }

    private void testTiledShapeBuilder() {
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

    public void testHerringbone() {
        ArrayList<TiledShape> verts = new ArrayList<>();
        ArrayList<TiledShape> horzs = new ArrayList<>();
        verts.add(new TiledShape(loadShapeImage("tiles/herringbone small vertical test.png")));
        horzs.add(new TiledShape(loadShapeImage("tiles/herringbone small horizontal test.png")));
        System.out.println(ShapeGenerator.buildHerringboneShape(400, 100, verts, horzs));
    }

    public void testStackBond() {
        ArrayList<TiledShape> tiles = new ArrayList<>();
        tiles.add(new TiledShape(loadShapeImage("tiles/herringbone horizontal test.png")));
        System.out.println(ShapeGenerator.buildStackBond(400, 100, tiles));
    }

    public void testBrick() {
        ArrayList<TiledShape> tiles = new ArrayList<>();
        tiles.add(new TiledShape(loadShapeImage("tiles/brick test.png")));
        System.out.println(ShapeGenerator.buildBrick(400, 100, tiles, false, 3));
    }

    public void testRunningBond() {
        ArrayList<TiledShape> verts = new ArrayList<>();
        ArrayList<TiledShape> horzs = new ArrayList<>();
        
        verts.add(new TiledShape(loadShapeImage("tiles/herringbone vertical test.png")).invert());
        horzs.add(new TiledShape(loadShapeImage("tiles/herringbone horizontal test.png")).invert());
        System.out.println(ShapeGenerator.buildRunningBond(100, 100, verts, horzs));
        System.out.println("");
        
        verts = new ArrayList<>();
        horzs = new ArrayList<>();
        verts.add(new TiledShape(loadShapeImage("tiles/herringbone small vertical test.png")).invert());
        horzs.add(new TiledShape(loadShapeImage("tiles/herringbone small horizontal test.png")).invert());
        System.out.println(ShapeGenerator.buildRunningBond(100, 100, verts, horzs));
        System.out.println("");

        verts = new ArrayList<>();
        horzs = new ArrayList<>();
        verts.add(new TiledShape(loadShapeImage("tiles/brick test.png")).rotateClockwise());
        horzs.add(new TiledShape(loadShapeImage("tiles/brick test.png")));
        System.out.println(ShapeGenerator.buildRunningBond(100, 100, verts, horzs));
    }
}
