package squidpony.examples;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.squidgrid.mapping.ClassicRogueMapGenerator;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.mapping.Terrain;
import squidpony.squidgrid.Direction;

/**
 * Displays randomly built maps from the ClassicRogueMapGenerator.
 *
 * A new dungeon is generated every time the mouse is clicked.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class ClassicRogueMapGeneratorTest {

    private static final int width = 100, height = 80, scale = 10,
            horizontalRooms = 5, verticalRooms = 4,
            minRoomWidth = 3, maxRoomWidth = 15,
            minRoomHeight = 4, maxRoomHeight = 15;

    private JFrame frame;
    private SquidPanel back, front;
    private ClassicRogueMapGenerator gen;

    public static void main(String... args) {
        new ClassicRogueMapGeneratorTest().go();
    }

    private void go() {
        gen = new ClassicRogueMapGenerator(horizontalRooms, verticalRooms, width, height, minRoomWidth, maxRoomWidth, minRoomHeight, maxRoomHeight);

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
        Terrain[][] map;
        map = gen.create();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, SColor.BLACK);
                SColor color;
                boolean hasNeighbor = false;
                for (Direction dir : Direction.OUTWARDS) {
                    int x2 = x + dir.deltaX;
                    int y2 = y + dir.deltaY;
                    if (x2 >= 0 && y2 >= 0 && x2 < width && y2 < height && map[x2][y2] != Terrain.WALL) {
                        hasNeighbor = true;
                        break;
                    }
                }
                if (hasNeighbor) {
                    front.put(x, y, map[x][y].symbol(), map[x][y].color());
                }
            }
        }

        back.refresh();
        front.refresh();
    }
}
