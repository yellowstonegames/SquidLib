package squidpony.examples;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.SColorFactory;
import squidpony.squidgrid.fov.FOVTranslator;
import squidpony.squidgrid.fov.ShadowFOV;
import squidpony.squidgrid.gui.SwingPane;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidmath.RNG;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    private static final RNG rng = new RNG();
    private static final int width = 13, height = 13;
    boolean map[][] = new boolean[width][height];
    private SwingPane back, front;
    private FOVTranslator fov = new FOVTranslator(new ShadowFOV());

    public static void main(String... args) {
        new Playground().go();
    }

    private void go() {

        SColorFactory.addPallet("floor", SColorFactory.asGradient(SColor.BLACK_DYE, SColorFactory.desaturate(SColor.ROSE_MADDER, 0.6)));
        SColorFactory.addPallet("path", SColorFactory.asGradient(SColor.AMUR_CORK_TREE, SColor.AZUL));

        JFrame frame = new JFrame("FOV Comparison Test");
        frame.getContentPane().setBackground(SColor.BLACK);

        JLayeredPane layers = new JLayeredPane();
        TextCellFactory factory = new TextCellFactory(new Font("Arial", Font.BOLD, 40), 20, 20, true);
        back = new SwingPane(width, height, factory, null);
        front = new SwingPane(width, height, factory, null);
        layers.setLayer(back, JLayeredPane.DEFAULT_LAYER);
        layers.setLayer(front, JLayeredPane.PALETTE_LAYER);
        layers.add(back);
        layers.add(front);
        layers.setPreferredSize(back.getPreferredSize());
        layers.setSize(back.getPreferredSize());

        frame.add(layers);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                calculate();
            }

        });

        calculate();
        frame.setVisible(true);
    }

    private void draw() {
        back.erase();
        front.erase();

        fov.calculateFOV(map, 0, 0, Integer.max(width, height));

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (fov.isLit(x, y)) {
                    back.put(x, y, SColor.SLATE_GRAY);
                    front.put(x, y, map[x][y] ? '.' : '#', SColor.ALICE_BLUE);
                } else {
                    back.put(x, y, SColor.BLACK);
                    front.put(x, y, map[x][y] ? '.' : '#', SColor.ALOEWOOD);
                }
            }
        }
        back.refresh();

        front.refresh();
    }

    private void calculate() {

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = rng.nextBoolean();
            }
        }

        map[0][0] = false;

//        print();
        draw();
    }

}
