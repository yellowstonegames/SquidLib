package squidpony.examples;

import squidpony.Colors;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.gui.SquidKey;
import squidpony.squidgrid.gui.SquidLayers;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import javax.swing.JFrame;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;

/**
 * A demo to show as many aspects of SquidLib at once as possible.
 * Created by Tommy Ettinger on 7/8/2015.
 */
public class EverythingDemo {
    private static JFrame frame;

    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    int[][] colors, bgColors, lights;
    double[][] fovmap;
    private Point player;
    private FOV fov;
    private int width, height;
    private SquidKey keyListener;
    private int counter;
    private boolean[][] seen;
    private boolean drawing;
    public EverythingDemo() {
        width = 80;
        height = 30;

        Font fnt = new Font("Dialog", Font.PLAIN, 24);
        try {
            fnt = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Rogue-Zodiac.ttf")).deriveFont(32.0f);
        } catch (Exception e) {
        }
        display = new SquidLayers(width, height, 12, 24, fnt);

        lrng = new LightRNG();
        rng = new RNG(lrng);
        DungeonUtility.rng = rng;
        dungeonGen = new DungeonGenerator(width, height, rng);
        dungeonGen.addWater(10);
        dungeonGen.addDoors(15, true);
        bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.hashesToLines(bareDungeon);
        player = DungeonUtility.randomFloor(bareDungeon);
        fov = new FOV(FOV.RIPPLE_TIGHT);
        res = DungeonUtility.generateResistances(bareDungeon);
        colors = DungeonUtility.generatePaletteIndices(bareDungeon);
        bgColors = DungeonUtility.generateBGPaletteIndices(bareDungeon);
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, 0);
        seen = new boolean[width][height];

        keyListener = new SquidKey(true, SquidKey.CaptureType.DOWN);
        drawing = false;
    }

    private void move(int xmod, int ymod) {
        if (player.x + xmod >= 0 && player.y + ymod >= 0 && player.x + xmod < width && player.y + ymod < height
                && bareDungeon[player.x + xmod][player.y + ymod] != '#') {
            if (lineDungeon[player.x + xmod][player.y + ymod] == '+') {
                bareDungeon[player.x + xmod][player.y + ymod] = '/';
                lineDungeon[player.x + xmod][player.y + ymod] = '/';
                res = DungeonUtility.generateResistances(bareDungeon);

            } else
                player.move(player.x + xmod, player.y + ymod);
        }
    }

    private boolean handle(KeyEvent k) {
        switch (k.getExtendedKeyCode()) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_NUMPAD4:
            case KeyEvent.VK_H:
                move(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_NUMPAD6:
            case KeyEvent.VK_L:
                move(1, 0);
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_NUMPAD8:
            case KeyEvent.VK_K:
                move(0, -1);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_NUMPAD2:
            case KeyEvent.VK_J:
                move(0, 1);
                break;
            case KeyEvent.VK_Q:
                System.exit(0);

        }
        return true;
    }

    private void redraw() {
        if (!drawing) {
            drawing = true;
            //display.erase();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (fovmap[i][j] > 0.0) {
                        seen[i][j] = true;
                        display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j],
                                lights[i][j] + (int) (-100 + 250 * fovmap[i][j]));
                    } else if (seen[i][j]) {
                        display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], -140);
                    }
                }
            }
            display.put(player.x, player.y, '@', 30);
            display.refresh();
            drawing = false;
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("SquidLib Everything Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final EverythingDemo demo = new EverythingDemo();

        frame.addKeyListener(demo.keyListener);

        Container panel = frame.getContentPane();
        panel.setBackground(Colors.DARK_SLATE_GRAY);
        panel.add(demo.display);

        frame.getContentPane().setBackground(Colors.DARK_SLATE_GRAY);
        frame.setVisible(true);

        frame.pack();
        frame.setLocationRelativeTo(null);

        int[][] tempLights = new int[demo.width][demo.height];
        demo.fovmap = demo.fov.calculateFOV(demo.res, demo.player.x, demo.player.y, 8);

//        demo.redraw();
        (new Thread(new Runnable() {
            private int upd = (int) ((System.currentTimeMillis() / 80.0) % 64000);
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    upd = (int) ((System.currentTimeMillis() / 80.0) % 64000);
                    if (!demo.drawing && demo.keyListener.hasNext()) {
                        if (demo.counter != upd) {
                            demo.handle(demo.keyListener.next());
                            demo.fovmap = demo.fov.calculateFOV(demo.res, demo.player.x, demo.player.y, 8);
                            demo.redraw();
                        }
                    }
                    else if (!demo.drawing && demo.counter != upd) {
                        demo.counter = upd;
                        demo.lights = DungeonUtility.generateLightnessModifiers(demo.bareDungeon, demo.counter);
                        demo.redraw();

                    }
                }
            }
        })).start();
    }
}
