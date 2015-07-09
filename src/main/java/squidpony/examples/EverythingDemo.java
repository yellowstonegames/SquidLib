package squidpony.examples;

import squidpony.Colors;
import squidpony.SColor;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.gui.SquidKey;
import squidpony.squidgrid.gui.SquidLayers;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A demo to show as many aspects of SquidLib at once as possible.
 * Created by Tommy Ettinger on 7/8/2015.
 */
public class EverythingDemo {
    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    int[][] colors, bgColors, lights;
    private Point player;
    private FOV fov;
    private int width, height;
    private SquidKey keyListener;
    public EverythingDemo()
    {
        width = 80;
        height = 30;

        Font fnt = new Font("Dialog", Font.PLAIN, 24);
        try {
            fnt = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Rogue-Zodiac.ttf")).deriveFont(32.0f);
        }catch (Exception e) { }
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
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon);

        keyListener = new SquidKey(true, SquidKey.CaptureType.DOWN);

    }
    private void move(int xmod, int ymod) {
        if (player.x + xmod >= 0 && player.y + ymod >= 0 && player.x + xmod < width && player.y + ymod < height
                && bareDungeon[player.x + xmod][player.y + ymod] != '#') {
            if(lineDungeon[player.x + xmod][player.y + ymod] == '+')
            {
                bareDungeon[player.x + xmod][player.y + ymod] = '/';
                lineDungeon[player.x + xmod][player.y + ymod] = '/';
                res = DungeonUtility.generateResistances(bareDungeon);

            }
            else
                player.move(player.x + xmod, player.y + ymod);
        }
    }
    private boolean handle(KeyEvent k)
    {
        switch (k.getExtendedKeyCode())
        {
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
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("SquidLib Everything Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EverythingDemo demo = new EverythingDemo();

        frame.addKeyListener(demo.keyListener);

        Container panel = frame.getContentPane();
        panel.setBackground(Colors.DARK_SLATE_GRAY);
        panel.add(demo.display);

        frame.getContentPane().setBackground(Colors.DARK_SLATE_GRAY);
        frame.setVisible(true);

        frame.pack();
        frame.setLocationRelativeTo(null);

        int[][] tempLights = new int[demo.width][demo.height];
        double[][] tempFov = demo.fov.calculateFOV(demo.res, demo.player.x, demo.player.y, 8);
        for(int i = 0; i < demo.width; i++)
        {
            for(int j = 0; j < demo.height; j++) {
                if (tempFov[i][j] > 0.0) {
                    tempLights[i][j] = demo.lights[i][j] + (int) (-200 + 360 * tempFov[i][j]);
                    demo.display.put(i, j, demo.lineDungeon[i][j], demo.colors[i][j], demo.bgColors[i][j], tempLights[i][j]);
                }
                else {
                    tempLights[i][j] = -255;

                }
            }
        }
        demo.display.put(demo.player.x, demo.player.y, '@', 30);
        demo.display.refresh();
        frame.repaint();
        while (demo.handle(demo.keyListener.next()))
        {
            tempFov = demo.fov.calculateFOV(demo.res, demo.player.x, demo.player.y, 8);
            demo.display.erase();
            for(int i = 0; i < demo.width; i++)
            {
                for(int j = 0; j < demo.height; j++) {
                    if (tempFov[i][j] > 0.0) {
                        tempLights[i][j] = demo.lights[i][j] + (int) (-200 + 360 * tempFov[i][j]);
                        demo.display.put(i, j, demo.lineDungeon[i][j], demo.colors[i][j], demo.bgColors[i][j], tempLights[i][j]);
                    }
                    else {
                        tempLights[i][j] = -255;

                    }
                }
            }
            demo.display.put(demo.player.x, demo.player.y, '@', 30);
            demo.display.refresh();
            frame.repaint();
        }
    }
}
