package squidpony.examples;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.annotation.Beta;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.mapping.DividedMazeGenerator;

/**
 * Displays randomly built maps from the Divided Maze Generator.
 *
 * A new dungeon is generated every time the mouse is clicked.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class DividedMazeTest {

    private static final int width = 100, height = 80, scale = 10;

    private JFrame frame;
    private SquidPanel back, front;
    private DividedMazeGenerator gen;

    public static void main(String... args) {
        new DividedMazeTest().go();
    }

    private void go() {
        gen = new DividedMazeGenerator(width, height);

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
        map = gen.create();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, SColor.BLACK);
                front.put(x, y, map[x][y] ? SColor.ATOMIC_TANGERINE : SColor.DULL_BLUE);
            }
        }

        back.refresh();
        front.refresh();
    }
}
