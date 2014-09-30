package squidpony.examples;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.FOVSolver;
import squidpony.FOVSolver.FOVType;
import squidpony.SColor;
import squidpony.squidgrid.gui.SwingPane;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidmath.RNG;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOVComparisonTest {

    private static final RNG rng = new RNG();
    private static final int width = 25, height = 25;
    private static final int translucence = 0xFF;
    private double map[][] = new double[width][height];
    private SwingPane front, back;
    private FOVSolver shadowFov = new FOVSolver(FOVType.SHADOW);
    private FOVSolver rippleFov = new FOVSolver(FOVType.RIPPLE);

    public static void main(String... args) {
        new FOVComparisonTest().go();
    }

    private void go() {
        JFrame frame = new JFrame("FOV Comparison Test");
        frame.getContentPane().setBackground(SColor.BLACK);

        JLayeredPane layers = new JLayeredPane();
        TextCellFactory factory = new TextCellFactory(new Font("Arial", Font.BOLD, 40), 30, 30, true);
        front = new SwingPane(width, height, factory, null);
        back = new SwingPane(width, height, factory, null);
        layers.setLayer(front, JLayeredPane.POPUP_LAYER);
        layers.setLayer(back, JLayeredPane.PALETTE_LAYER + 2);
        layers.add(front);
        layers.add(back);
        layers.setPreferredSize(front.getPreferredSize());
        layers.setSize(front.getPreferredSize());

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
        front.erase();
        back.erase();

        shadowFov.calculateFOV(map, width / 2, height / 2);
        rippleFov.calculateFOV(map, width / 2, height / 2);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                front.put(x, y, map[x][y] < 1 ? '.' : '#', SColor.DARK_SLATE_GRAY);

                int r = 0, g = 0, b = 0;
                if (shadowFov.isLit(x, y)) {
//                    shadowcasting.put(x, y, new SColor(255, 0, 0, translucence));
                    r = 255;
                }

                if (rippleFov.isLit(x, y)) {
//                    ripple.put(x, y, new SColor(0, 255, 0, translucence));
                    g = 255;
                }
                back.put(x, y, new SColor(r, g, b, translucence));
            }
        }

        front.put(width / 2, height / 2, '@', SColor.GREEN_YELLOW);
        front.put(1, height - 1, "Shadowcasting FOV", new SColor(255, 0, 0, translucence));
        front.put(1, height - 2, "Ripple FOV", new SColor(0, 255, 0, translucence));

        front.refresh();
        back.refresh();
    }

    private void calculate() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = rng.nextInt(2);
            }
        }

        map[width / 2][height / 2] = 0;
        draw();
    }

}
