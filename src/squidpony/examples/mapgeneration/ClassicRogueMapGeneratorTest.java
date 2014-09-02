package squidpony.examples.mapgeneration;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.generation.ClassicRogueMapGenerator;
import squidpony.squidgrid.gui.SwingPane;

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
    private SwingPane back, front;
    private ClassicRogueMapGenerator gen;

    public static void main(String... args) {
        new ClassicRogueMapGeneratorTest().go();
    }

    private void go() {
        gen = new ClassicRogueMapGenerator(horizontalRooms, verticalRooms, width, height, minRoomWidth, maxRoomWidth, minRoomHeight, maxRoomHeight);

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
        ClassicRogueMapGenerator.Terrain[][] map;
        map = gen.create();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, SColor.BLACK);
                SColor color;
                switch (map[x][y]) {
                    case DOOR:
                        color = SColor.BROWNER;
                        break;
                    case FLOOR:
                        color = SColor.ALOEWOOD_BROWN;
                        break;
                    default:
                        color = SColor.LIGHT_GRAY;
                }
                front.put(x, y, map[x][y].symbol, color);
            }
        }

        back.refresh();
        front.refresh();
    }
}
