package squidpony.examples;

import java.awt.Font;
import squidpony.squidgrid.mapping.shape.TiledShape;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import squidpony.SColorFactory;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidgrid.mapping.shape.ShapeGenerator;

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
//        test.testRunningBond();
//        test.testBasketWeave();
        test.testWindmill();
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
        shape.deteriorate(sparsity, " ");
        System.out.println(shape);
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
        System.out.println(ShapeGenerator.buildBrick(400, 100, tiles, 3));
    }

    public void testRunningBond() {
        ArrayList<TiledShape> verts = new ArrayList<>();
        ArrayList<TiledShape> horzs = new ArrayList<>();

        verts.add(new TiledShape(loadShapeImage("tiles/herringbone vertical test.png")));
        horzs.add(new TiledShape(loadShapeImage("tiles/herringbone horizontal test.png")));
        System.out.println(ShapeGenerator.buildRunningBond(100, 100, verts, horzs));
        System.out.println("");

        verts = new ArrayList<>();
        horzs = new ArrayList<>();
        verts.add(new TiledShape(loadShapeImage("tiles/herringbone small vertical test.png")));
        horzs.add(new TiledShape(loadShapeImage("tiles/herringbone small horizontal test.png")));
        System.out.println(ShapeGenerator.buildRunningBond(100, 100, verts, horzs));
        System.out.println("");

        verts = new ArrayList<>();
        horzs = new ArrayList<>();
        TiledShape shape = new TiledShape(loadShapeImage("tiles/brick test.png"));
        shape.rotateClockwise();
        verts.add(shape);
        horzs.add(new TiledShape(loadShapeImage("tiles/brick test.png")));
        System.out.println(ShapeGenerator.buildRunningBond(100, 100, verts, horzs));
    }

    public void testBasketWeave() {
        ArrayList<TiledShape> verts = new ArrayList<>();
        ArrayList<TiledShape> horzs = new ArrayList<>();
        TiledShape shape = new TiledShape(loadShapeImage("tiles/brick test.png"));
        shape.rotateClockwise();
        verts.add(shape);
        horzs.add(new TiledShape(loadShapeImage("tiles/brick test.png")));
        System.out.println(ShapeGenerator.buildBasketWeave(100, 100, verts, horzs, true));
        System.out.println("");
        System.out.println(ShapeGenerator.buildBasketWeave(100, 100, verts, horzs, false));
    }

    public void testWindmill() {
        ArrayList<TiledShape> verts = new ArrayList<>();
        ArrayList<TiledShape> horzs = new ArrayList<>();
        TiledShape shape = new TiledShape(loadShapeImage("tiles/brick test.png"));
        shape.rotateClockwise();
        verts.add(shape);
        horzs.add(new TiledShape(loadShapeImage("tiles/brick test.png")));
        showColors(ShapeGenerator.buildWindmill(100, 100, verts, horzs, verts));
    }

    public void showColors(TiledShape shape) {
        JFrame frame = new JFrame();
        SquidPanel pane = new SquidPanel(shape.width(), shape.height(), new TextCellFactory().font(new Font("Ariel", Font.PLAIN, 10)).width(12).height(12), null);
        frame.add(pane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        for (int x = 0; x < shape.width(); x++) {
            for (int y = 0; y < shape.height(); y++) {
                pane.put(x, y, SColorFactory.asSColor(Integer.parseInt(shape.getStringAt(x, y))));
            }
        }
        pane.refresh();
    }
}
