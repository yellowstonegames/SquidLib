package squidpony.examples;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.SColorFactory;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidmath.RNG;
import squidpony.squidmath.AStarSearch;

/**
 * This program tests GridSearch for correctness. It's visual to make the weighting easy to see along with the path
 * found.
 * 
 * A new grid and random search start and end will be generated when the mouse is clicked.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class GridSearchTest {

    private int width = 100;
    private int height = 50;
    private double[][] map;
    private RNG rng = new RNG();
    private AStarSearch search;
    private Queue<Point> path;
    private Point start, target;
    private float weight = 10;
    private SquidPanel back, front;

    public static void main(String... args) {
        new GridSearchTest().go();
    }

    private void go() {
        map = new double[width][height];

        SColorFactory.addPallet("floor", SColorFactory.asGradient(SColor.BLACK_DYE, SColorFactory.desaturate(SColor.ROSE_MADDER, 0.6)));
        SColorFactory.addPallet("path", SColorFactory.asGradient(SColor.AMUR_CORK_TREE, SColor.AZUL));

        JFrame frame = new JFrame("A* Test");
        frame.getContentPane().setBackground(SColor.BLACK);

        JLayeredPane layers = new JLayeredPane();
        TextCellFactory factory = new TextCellFactory().font(new Font("Arial", Font.BOLD, 22)).width(12).height(16);
        back = new SquidPanel(width, height, factory, null);
        front = new SquidPanel(width, height, factory, null);
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
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                back.put(x, y, SColorFactory.fromPallet("floor", ((float) (map[x][y] + weight) / weight) / 2));
            }
        }
        back.refresh();

        if (path == null) {
            System.out.println("No path found.");
        } else {
            float grade = 0;
            float splitSize = 1f / (path.size() + 2);
            front.put(start.x, start.y, 'X', SColorFactory.fromPallet("path", grade));
            grade += splitSize;
            for (Point p : path) {
                front.put(p.x, p.y, 'X', SColorFactory.fromPallet("path", grade));
                grade += splitSize;
            }
            front.put(target.x, target.y, 'X', SColorFactory.fromPallet("path", grade));
        }
        front.refresh();
    }

    private void calculate() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = rng.nextDouble() > 0.1 ? weight : -weight; // rng.nextInt((int) weight);//randomly set some areas as harder to walk
            }
        }
        search = new AStarSearch(map, AStarSearch.SearchType.MANHATTAN);
        start = new Point(rng.nextInt(width), rng.nextInt(height));
        target = new Point(rng.nextInt(width), rng.nextInt(height));
        path = search.path(start.x, start.y, target.x, target.y);

//        print();
        draw();
    }

    private void print() {
        if (path == null) {
            System.out.println("No path found.");
        } else {
            for (Point p : path) {
                System.out.println(p + " " + map[p.x][p.y]);
            }
        }
    }
}
