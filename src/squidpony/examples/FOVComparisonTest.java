package squidpony.examples;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.squidgrid.fov.FOVTranslator;
import squidpony.squidgrid.fov.RippleFOV;
import squidpony.squidgrid.fov.ShadowFOV;
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
    boolean map[][] = new boolean[width][height];
    private SwingPane back, shadowcasting, ripple, rotatedShadow;
    private FOVTranslator shadowFov = new FOVTranslator(new ShadowFOV());
    private FOVTranslator rippleFov = new FOVTranslator(new RippleFOV());
//    private FOVTranslator rotatedShadowFOV = new FOVTranslator(new RotatedShadowFOV());

    public static void main(String... args) {
        new FOVComparisonTest().go();
    }

    private void go() {
        JFrame frame = new JFrame("FOV Comparison Test");
        frame.getContentPane().setBackground(SColor.BLACK);

        JLayeredPane layers = new JLayeredPane();
        TextCellFactory factory = new TextCellFactory(new Font("Arial", Font.BOLD, 40), 30, 30, true);
        back = new SwingPane(width, height, factory, null);
        shadowcasting = new SwingPane(width, height, factory, null);
        ripple = new SwingPane(width, height, factory, null);
        rotatedShadow = new SwingPane(width, height, factory, null);
        layers.setLayer(back, JLayeredPane.POPUP_LAYER);
        layers.setLayer(shadowcasting, JLayeredPane.PALETTE_LAYER);
        layers.setLayer(ripple, JLayeredPane.PALETTE_LAYER + 1);
        layers.setLayer(rotatedShadow, JLayeredPane.PALETTE_LAYER + 2);
        layers.add(back);
        layers.add(shadowcasting);
        layers.add(ripple);
        layers.add(rotatedShadow);
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
        shadowcasting.erase();
        ripple.erase();
        rotatedShadow.erase();

        shadowFov.calculateFOV(map, width / 2, height / 2, Integer.max(width, height));
        rippleFov.calculateFOV(map, width / 2, height / 2, Integer.max(width, height));
//        rotatedShadowFOV.calculateFOV(map, width / 2, height / 2, Integer.max(width, height));

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, !map[x][y] ? '.' : '#', SColor.DARK_SLATE_GRAY);

                int r = 0, g = 0, b = 0;
                if (shadowFov.isLit(x, y)) {
//                    shadowcasting.put(x, y, new SColor(255, 0, 0, translucence));
                    r = 255;
                }

                if (rippleFov.isLit(x, y)) {
//                    ripple.put(x, y, new SColor(0, 255, 0, translucence));
//                    g = 255;
                }

//                if (rotatedShadowFOV.isLit(x, y)) {
//                    spread.put(x, y, new SColor(0, 0, 255, translucence));
//                    b = 255;
//                }
                rotatedShadow.put(x, y, new SColor(r, g, b, translucence));
            }
        }

        back.put(width / 2, height / 2, '@', SColor.GREEN_YELLOW);
        back.put(1, height - 1, "Shadowcasting FOV", new SColor(255, 0, 0, translucence));
//        back.put(1, height - 2, "Ripple FOV", new SColor(0, 255, 0, translucence));
        back.put(1, height - 3, "Rotated Shadowcasting FOV", new SColor(0, 0, 255, translucence));

        back.refresh();
        shadowcasting.refresh();
        ripple.refresh();
        rotatedShadow.refresh();
    }

    private void calculate() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = rng.nextBoolean();
            }
        }

        map[width / 2][height / 2] = false;

//        print();
        draw();
    }

}
