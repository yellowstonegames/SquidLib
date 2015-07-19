package squidpony.examples;

import squidpony.Colors;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.gui.DefaultResources;
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
import java.util.HashMap;

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
    private int[][] colors, bgColors, lights;
    private double[][] fovmap, pathMap;
    private Point player;
    private FOV fov;
    private int width, height;
    private SquidKey keyListener;
    private int counter;
    private boolean[][] seen, unchanged;
    private boolean drawing;
    private int health = 7;

    private HashMap<Point, Integer> monsters;
    private DijkstraMap getToPlayer;

    /**
     * Initialize the Everything.
     */
    public EverythingDemo() {
        width = 80;
        height = 30;
        // the font will try to load Rogue-Zodiac.ttf from resources. I (Tommy Ettinger) made it, and it's under the
        // same license as SquidLib.
        Font fnt = DefaultResources.getDefaultNarrowFont(2.0f);
        display = new SquidLayers(width, height, 12, 24, fnt);

        lrng = new LightRNG(0x1337BEEF);
        rng = new RNG(lrng);

        // this is important if you use a seeded RNG.
//        DungeonUtility.rng = rng;
        dungeonGen = new DungeonGenerator(width, height, rng);
        dungeonGen.addWater(10);
        dungeonGen.addDoors(15, true);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.hashesToLines(bareDungeon);
        char[][] placement = DungeonUtility.closeDoors(bareDungeon);
        player = DungeonUtility.randomFloor(placement);
        placement[player.x][player.y] = '@';
        int numMonsters = 25;
        monsters = new HashMap<Point, Integer>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Point monPos = DungeonUtility.randomFloor(placement);
            monsters.put(monPos, 0);
            placement[monPos.x][monPos.y] = 'M';
        }
        fov = new FOV(FOV.RIPPLE_TIGHT);
        getToPlayer = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN);
        getToPlayer.rng = rng;
        getToPlayer.setGoal(player);
        pathMap = getToPlayer.scan(null);
        res = DungeonUtility.generateResistances(bareDungeon);
        colors = DungeonUtility.generatePaletteIndices(bareDungeon);
        bgColors = DungeonUtility.generateBGPaletteIndices(bareDungeon);
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, 0);
        seen = new boolean[width][height];
        unchanged = new boolean[width][height];

        keyListener = new SquidKey(false, SquidKey.CaptureType.DOWN);
        drawing = false;
    }

    /**
     * Move the player or open closed doors, remove any monsters the player bumped, then update the DijkstraMap and
     * have the monsters that can see the player try to approach.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod
     * @param ymod
     */
    private void move(int xmod, int ymod) {
        if (player.x + xmod >= 0 && player.y + ymod >= 0 && player.x + xmod < width && player.y + ymod < height
                && bareDungeon[player.x + xmod][player.y + ymod] != '#') {
            // '+' is a door.
            if (lineDungeon[player.x + xmod][player.y + ymod] == '+') {
                bareDungeon[player.x + xmod][player.y + ymod] = '/';
                lineDungeon[player.x + xmod][player.y + ymod] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(bareDungeon);
            } else {
                player.move(player.x + xmod, player.y + ymod);
            }
            if(monsters.containsKey(player))
            {
                // this doesn't remove the player, it removes the monster that just got run over by the player.
                monsters.remove(player);
            }
            // The next two lines are important to avoid monsters treating cells the player WAS in as goals.
            getToPlayer.clearGoals();
            getToPlayer.resetMap();
            // now that goals are cleared, we can mark the current player position as a goal.
            getToPlayer.setGoal(player);
            // this is an important piece of DijkstraMap usage; the argument is a Set of Points for squares that
            // temporarily cannot be moved through (not walls, which are automatically known because the map char[][]
            // was passed to the DijkstraMap constructor, but things like moving creatures and objects).
            pathMap = getToPlayer.scan(monsters.keySet());

            // recalculate FOV, store it in fovmap for the redraw to use.
            fovmap = fov.calculateFOV(res, player.x, player.y, 8);
            HashMap<Point, Integer> newMons = new HashMap<Point, Integer>(monsters.size());
            // handle monster turns
            for(HashMap.Entry<Point, Integer> mon : monsters.entrySet())
            {
                // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
                if(mon.getValue() > 0 || fovmap[mon.getKey().x][mon.getKey().y] > 0.1)
                {
                    // this block is used to ensure that the monster picks the best path, or a random choice if there
                    // is more than one equally good best option.
                    Direction choice = null;
                    double best = 9999.0;
                    for(Direction d : getToPlayer.shuffle(Direction.CARDINALS))
                    {
                        Point tmp = new Point(mon.getKey().x + d.deltaX, mon.getKey().y + d.deltaY);
                        if(pathMap[tmp.x][tmp.y] < best &&
                                !monsters.containsKey(tmp) && !newMons.containsKey(tmp))
                        {
                            // pathMap is a 2D array of doubles where 0 is the goal (the player).
                            // we use best to store which option is closest to the goal.
                            best = pathMap[tmp.x][tmp.y];
                            choice = d;
                        }
                    }
                    if(choice != null)
                    {
                        Point tmp = new Point(mon.getKey().x + choice.deltaX, mon.getKey().y + choice.deltaY);
                        // if we would move into the player, instead damage the player and give newMons the current
                        // position of this monster.
                        if(player.equals(tmp))
                        {
                            health--;
                            newMons.put(mon.getKey(), 1);
                        }
                        // otherwise store the new position in newMons.
                        else
                            newMons.put(tmp, 1);
                    }
                    else
                    {
                        newMons.put(mon.getKey(), 1);
                    }
                }
                else
                {
                    newMons.put(mon.getKey(), mon.getValue());
                }
            }
            monsters = newMons;
        }
    }

    /**
     * Takes a KeyEvent that SquidKey passes it, and checks if it is an arrow key, HJKL, a numpad direction, or Q.
     * Any of those but Q will move the player 1 cell, and Q will just close the game.
     * @param k
     * @return true if the input was handled successfully, false if you should stop handling due to a quit.
     */
    private boolean handle(KeyEvent k) {
        if(health <= 0) {
            if(k.getExtendedKeyCode() == KeyEvent.VK_Q)
                System.exit(0);
            return true;
        }
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
                return false;
        }
        return true;
    }

    /**
     * Redraw the SquidLayers with the latest known information about FOV, seen tiles, etc.
     * Sets seen as well.
     */
    private void redraw() {
        // we need to make sure we aren't currently drawing when we try to redraw.
        if (!drawing) {
            // we set drawing to false at the end of the method.
            drawing = true;
            if(health <= 0)
            {
                display.putBoxedString(width / 2 - 11, height / 2 - 1, "YOU HAVE BEEN EATEN!");
                display.putBoxedString(width / 2 - 11, height / 2 + 5, "     q to quit.     ");
                display.refresh();
                drawing = false;
                return;
            }
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                    // and 1.0), with 1.0 being almost pure white at +150 lightness and 0.0 being rather dark at -100.
                    if (fovmap[i][j] > 0.0) {
                        seen[i][j] = true;
                        unchanged[i][j] = false;
                        display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j],
                                lights[i][j] + (int) (-100 + 250 * fovmap[i][j]));
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                    } else if (seen[i][j] && !unchanged[i][j]) {
                        display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], -140);
                        unchanged[i][j] = true;
                    }
                }
            }
            // the player doesn't care what was already rendered at its cell on the map.  30 is dark purple.
            display.put(player.x, player.y, Character.forDigit(health, 10), 30);

            for(Point mon : monsters.keySet()) {
                if (fovmap[mon.x][mon.y] > 0.0) {
                    display.put(mon.x, mon.y, 'M', 11);
                    unchanged[mon.x][mon.y] = false;
                }
            }
            // needed to see changes.
            display.refresh();
            // remember this was set to true? well, now we aren't drawing any more.
            drawing = false;
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("SquidLib Everything Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // final is only needed so the inner class that handles water animations can see demo
        final EverythingDemo demo = new EverythingDemo();

        // make the frame listen to our SquidKey
        frame.addKeyListener(demo.keyListener);

        // the stuff that surrounds the SquidLayers component should be the same color
        Container panel = frame.getContentPane();
        panel.setBackground(Colors.DARK_SLATE_GRAY);
        panel.add(demo.display);

        // NEEDED TO SEE ANYTHING IN THE APP/GAME
        frame.setVisible(true);

        // useful to make the size and location of the frame look correct
        frame.pack();
        frame.setLocationRelativeTo(null);

        // we only need to calculate FOV once everything is in place
        demo.fovmap = demo.fov.calculateFOV(demo.res, demo.player.x, demo.player.y, 8);

        // threads have a reputation for being tricky to deal with.
        // if you don't need animations, this section can be simplified to the commented block after it.
        (new Thread(new Runnable() {
            // upd is used to determine if enough time has passed between attempts to redraw. This tries to update at
            // about 15 frames per second, which is more than enough for a text-based game.
            private int upd = (int) ((System.currentTimeMillis() / 65.0) % 64000);
            @Override
            public void run() {
                // this runs in a non-UI thread, and doesn't directly change the UI -- only the lightness array.
                // this thread can be interrupted by the application closing, so we should terminate the loop then.
                while (!Thread.interrupted()) {
                    // upd gets assigned a value every iteration of the loop, but if very little time has passed, then
                    // the value won't be any different. this is important because upd needs to be different from
                    // demo.counter for enough time to be considered to have passed (you get graphical glitches from
                    // trying to render too frequently, or while something is being changed, with Swing).
                    upd = (int) ((System.currentTimeMillis() / 65.0) % 64000);
                    // if we are currently drawing, we do not want to suddenly jump in and render again.
                    // if there is input queued in the keyListener, then we want to resolve that and not go into the
                    // else if block below.
                    if (!demo.drawing && demo.keyListener.hasNext()) {
                        // again, we want to make sure the times are different enough. demo.counter is updated in the
                        // else if block below, and nowhere else, so if input is queued, the counter won't change, but
                        // upd will. it won't be longer than 65 milliseconds before this runs.
                        if (demo.counter != upd) {
                            // this gets the next queued input, and uses it to process movement or quitting.
                            demo.handle(demo.keyListener.next());
                            // redraw with the changed player position (if the player moved) and FOV.
                            demo.redraw();
                        }
                    }
                    // this needs the times to be different enough as well.
                    else if (!demo.drawing && demo.counter != upd) {
                        // counter is set to the current value of upd so we won't redraw again too soon.
                        demo.counter = upd;
                        // this makes the water change in lightness on its own. it could be used for many other tasks.
                        demo.lights = DungeonUtility.generateLightnessModifiers(demo.bareDungeon, demo.counter);
                        // redraw with the new lightness.
                        demo.redraw();

                    }
                }
            }
        // threads need to be started.
        })).start();

        // if you don't need animations that take place even while the player hasn't pressed a key,
        // the above section of code can be replaced with the following:
        /*
        while (demo.handle(demo.keyListener.next())) {
            demo.fovmap = demo.fov.calculateFOV(demo.res, demo.player.x, demo.player.y, 8);
            demo.redraw();
        }
        */
    }
}
