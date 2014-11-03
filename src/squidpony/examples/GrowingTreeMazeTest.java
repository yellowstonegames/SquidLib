package squidpony.examples;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.annotation.Beta;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.mapping.GrowingTreeMazeGenerator;
import squidpony.squidgrid.mapping.GrowingTreeMazeGenerator.ChoosingMethod;
import squidpony.squidmath.RNG;

/**
 * Displays randomly built maps from the Growing Tree Maze Generator.
 *
 * A new dungeon is generated every time the mouse is clicked.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class GrowingTreeMazeTest {

    private static final int width = 100, height = 80, scale = 10;
    private static final RNG rng = new RNG();

    private JFrame frame;
    private SquidPanel back, front;
    private GrowingTreeMazeGenerator gen;

    public static void main(String... args) {
        new GrowingTreeMazeTest().go();
    }

    private void go() {
        gen = new GrowingTreeMazeGenerator(width, height);

        back = new SquidPanel(width, height, scale, scale);
        front = new SquidPanel(width, height, scale, scale);

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
        boolean[][] map;
        ChoosingMethod choosing;
//        int choice = rng.nextInt(5);
        int choice = 3;
        String name;
        switch (choice) {
            case 0:
                name = "Last";
                choosing = new ChoosingMethod() {
                    public int chooseIndex(int size) {
                        return size - 1;
                    }
                };
                break;
            case 1:
                name = "20 Half  80 Last";
                choosing = new ChoosingMethod() {
                    public int chooseIndex(int size) {
                        if (rng.nextDouble() < 0.2) {
                            return size / 2;
                        } else {
                            return size - 1;
                        }
                    }
                };
                break;
            case 2:
                name = "20 First  80 Last";
                choosing = new ChoosingMethod() {
                    public int chooseIndex(int size) {
                        if (rng.nextDouble() < 0.2) {
                            return 0;
                        } else {
                            return size - 1;
                        }
                    }
                };
                break;
            case 3:
                name = "Cray Cray";
                choosing = new ChoosingMethod() {
                    double d = rng.nextDouble();
                    double target = rng.nextDouble();
                    boolean up = target > d;
                    double change = 0.01;

                    public int chooseIndex(int size) {
                        int ret;
                         if (d < 0.5) {
                            ret = size - 1;
                        } else {
                            ret = rng.nextInt(size);
                        }
                        d += up ? change : -change;
                        if ((up && d >= target) || (!up && d <= target)) {
                            target = rng.nextDouble();
                            up = target > d;
                        }
                        return ret;
                    }
                };
                break;
            case 4:
            default:
                name = "Random";
                choosing = new ChoosingMethod() {
                    public int chooseIndex(int size) {
                        return rng.nextInt(size);
                    }
                };

        }

        map = gen.create(choosing);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, SColor.BLACK);
                front.put(x, y, map[x][y] ? SColor.ATOMIC_TANGERINE : SColor.DULL_BLUE);
            }
        }

        front.put(0, 0, '0' + choice);
        front.put(0, 1, name);

        back.refresh();
        front.refresh();
    }
}
