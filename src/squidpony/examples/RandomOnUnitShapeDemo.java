package squidpony.examples;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.SwingPane;
import squidpony.squidgrid.util.BasicRadiusStrategy;
import squidpony.squidmath.Point3D;
import squidpony.squidmath.RNG;

/**
 * Shows some examples of getting random points in a shape.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class RandomOnUnitShapeDemo {

    private static final RNG rng = new RNG();
    private static final int width = 900, height = 600, scale = 1;

    private JFrame frame;
    private SwingPane back, front;

    public static void main(String... args) {
        new RandomOnUnitShapeDemo().go();
    }

    private void go() {
        back = new SwingPane(width, height, scale, scale);
        front = new SwingPane(width, height, scale, scale);

        frame = new JFrame();
        JLayeredPane layer = new JLayeredPane();
        layer.setLayer(back, JLayeredPane.DEFAULT_LAYER);
        layer.setLayer(front, JLayeredPane.PALETTE_LAYER);
        layer.add(back);
        layer.add(front);
        layer.setPreferredSize(back.getPreferredSize());
        layer.setSize(back.getPreferredSize());
        frame.add(layer);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        paint();
        frame.setVisible(true);

        frame.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                paint();
            }

        });
    }

    private void paint() {
        back.erase();
        front.erase();

        boolean[][] map = new boolean[width][height];
        int offset = width / 3 - 3;
        for (int i = 0; i < width * height / 10; i++) {
            Point3D p = BasicRadiusStrategy.SPHERE.onUnitShape3D(offset / 2);
            map[p.x + 1 + offset / 2][p.y + (height) / 2] = true;

            p = BasicRadiusStrategy.OCTAHEDRON.onUnitShape3D(offset / 2);
            map[p.x + 3 + 3 * offset / 2][p.y + (height) / 2] = true;

            p = BasicRadiusStrategy.CUBE.onUnitShape3D(offset / 2);
            map[p.x + 5 + 5 * offset / 2][p.y + (height) / 2] = true;
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, SColor.BLACK);
                if (map[x][y]) {
                    front.put(x, y, rng.getRandomElement(SColor.RED_SERIES));
                }
            }
        }

        back.refresh();
        front.refresh();
    }
}
