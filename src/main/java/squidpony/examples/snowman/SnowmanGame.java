package squidpony.examples.snowman;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.squidgrid.FOV;
import squidpony.SColor;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidgrid.gui.SquidKey;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.RNG;

/**
 * This class starts up the game.
 *
 * @author Eben Howard
 */
public class SnowmanGame {

    private static final int cellWidth = 22, cellHeight = 22;
    private static final int width = 50, height = 30, statWidth = 12, fontSize = 22, outputLines = 1;
    private static final int minimumRoomSize = 3;
    private static final String CHARS_USED = "☃☺.,Xy";//even though '▒' is used, it makes sizing weird and it's okay if it doesn't all fit in the cell so it's not in this list
    private final FOV fov = new FOV();
    private final RNG rng = new RNG();
    private JFrame frame;
    private SquidPanel mapPanel, mapBackPanel, statsPanel, outputPanel;
    private SquidKey keyListener;
    private Monster player;
    private int playerStrength = 7;
    private ArrayList<Monster> monsters = new ArrayList<>();
    private ArrayList<Treasure> treasuresFound = new ArrayList<>();
    private Tile[][] map;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new SnowmanGame().go();
    }

    /**
     * Starts the game.
     */
    private void go() {
        initializeFrame();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        player = new Monster(Monster.PLAYER);

        createMap();
        updateMap();
        updateStats();
        printOut("Welcome to Final Lab");

        runTurn();
    }

    /**
     * This is the main game loop method that takes input and process the results. Right now it doesn't loop!
     */
    private void runTurn() {
        int key = keyListener.next().getExtendedKeyCode();
        boolean success = false;
        if (key == KeyEvent.VK_RIGHT) {
            success = tryToMove(Direction.RIGHT);
        }

        //update all end of turn items
        if (success) {
            updateMap();
            moveAllMonsters();
            updateMap();
            player.causeDamage(1);//health drains each turn!
            updateStats();
        }
    }

    /**
     * Attempts to move in the given direction. If a monster is in that direction then the player attacks the monster.
     *
     * Returns false if there was a wall in the direction and so no action was taken.
     *
     * @param dir
     * @return
     */
    private boolean tryToMove(Direction dir) {
        Tile tile = map[player.x + dir.deltaX][player.y + dir.deltaY];
        if (tile.isWall()) {
            return false;
        }

        Monster monster = tile.getMonster();
        if (monster == null) {//move the player
            map[player.x][player.y].setMonster(null);
            mapPanel.slide(new Point(player.x, player.y), dir);
            player.x += dir.deltaX;
            player.y += dir.deltaY;
            map[player.x][player.y].setMonster(player);
            mapPanel.waitForAnimations();
            return true;
        } else {//attack!
            mapPanel.bump(new Point(player.x, player.y), dir);
            mapPanel.waitForAnimations();
            boolean dead = monster.causeDamage(playerStrength);
            if (dead) {
                monsters.remove(monster);
                map[player.x + dir.deltaX][player.y + dir.deltaY].setMonster(null);//no more monster
                printOut("Killed the " + monster.getName());
            }
            return true;
        }
    }

    /**
     * Updates the map display to show the current view
     */
    private void updateMap() {
        doFOV();
        mapBackPanel.erase();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
//                map[x][y].setSeen(true);//uncomment this to see the fully generated map rather than the player's view
                mapPanel.put(x, y, map[x][y].getSymbol(), map[x][y].getColor());
                if (map[x][y].isSeen()) {
                    mapBackPanel.put(x, y, SColor.DARK_SLATE_GRAY);
                }
            }
        }

        mapBackPanel.refresh();
        mapPanel.refresh();
    }

    /**
     * Updates the stats display to show current values
     */
    private void updateStats() {
        int y = 0;
        String info = "STATS";
        statsPanel.put((statWidth - info.length()) / 2, y, info);

        y += 2;
        info = "Health " + player.getHealth();
        statsPanel.put((statWidth - info.length()) / 2, y, info);
        statsPanel.refresh();
    }

    /**
     * Sets the output panel to show the message.
     *
     * @param message
     */
    private void printOut(String message) {
        outputPanel.put(0, 0, message);
        outputPanel.refresh();
    }

    /**
     * Calculates the Field of View and marks the maps spots seen appropriately.
     */
    private void doFOV() {
        double[][] walls = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walls[x][y] = map[x][y].isWall() ? 1.0 : 0.0;
            }
        }
        fov.calculateFOV(walls, player.x, player.y, width + height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y].setSeen(fov.isLit(x, y));
            }
        }
    }

    /**
     * Creates the map to contain random bits of wall
     */
    private void createMap() {
        map = new Tile[width][height];

        //make all the edges into walls
        for (int x = 0; x < width; x++) {
            map[x][0] = new Tile(true);
            map[x][height - 1] = new Tile(true);
        }
        for (int y = 0; y < height; y++) {
            map[0][y] = new Tile(true);
            map[width - 1][y] = new Tile(true);
        }

        //fill the rest in with floor
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                map[x][y] = new Tile();
            }
        }

        //randomly place some chunks of wall
        for (int i = 0; i < 8; i++) {
            placeWallChunk();
        }

        //randomly place the player
        placeMonster(player);

        //randomly place some monsters
        for (int i = 0; i < 20; i++) {
            placeMonster(new Monster(Monster.SNOWMAN));
        }

        for (int i = 0; i < 10; i++) {
            placeTreasure(new Treasure("Chocolate Coin", 1));
        }

        for (int i = 0; i < 5; i++) {
            placeTreasure(new Treasure("Coal", 0));
        }
    }

    /**
     * Randomly places a group of walls in the map. This replaces whatever was in that location previously.
     */
    private void placeWallChunk() {
        int spread = 5;
        int centerX = rng.nextInt(width);
        int centerY = rng.nextInt(height);

        for (int placeX = centerX - spread; placeX < centerX + spread; placeX++) {
            for (int placeY = centerY - spread; placeY < centerY + spread; placeY++) {
                if (rng.nextDouble() < 0.2 && placeX > 0 && placeX < width - 1 && placeY > 0 && placeY < height - 1) {
                    map[placeX][placeY] = new Tile(true);
                }
            }
        }
    }

    /**
     * Places the provided monster into an open tile space.
     *
     * @param monster
     */
    private void placeMonster(Monster monster) {
        int x = rng.nextInt(width - 2) + 1;
        int y = rng.nextInt(height - 2) + 1;
        if (map[x][y].isWall() || map[x][y].getMonster() != null) {
            placeMonster(monster);//try again recursively
        } else {
            map[x][y].setMonster(monster);
            monster.x = x;
            monster.y = y;

            if (!monster.equals(Monster.PLAYER)) {
                monsters.add(monster);
            }
        }
    }

    /**
     * Places the provided monster into an open tile space.
     *
     * @param treasure
     */
    private void placeTreasure(Treasure treasure) {
        int x = rng.nextInt(width - 2) + 1;
        int y = rng.nextInt(height - 2) + 1;
        if (map[x][y].isWall() || map[x][y].getTreasure() != null) {
            placeTreasure(treasure);//try again recursively
        } else {
            map[x][y].setTreasure(treasure);
        }
    }

    /**
     * Moves the monster given if possible. Monsters will not move into walls, other monsters, or the player.
     *
     * @param monster
     */
    private void moveMonster(Monster monster) {
        Direction dir = Direction.CARDINALS[rng.nextInt(Direction.CARDINALS.length)];//get a random direction
        Tile tile = map[monster.x + dir.deltaX][monster.y + dir.deltaY];
        if (!tile.isWall() && tile.getMonster() == null) {
            map[monster.x][monster.y].setMonster(null);
            if (tile.isSeen()) {//only show animation if within sight
                mapPanel.slide(new Point(monster.x, monster.y), dir);
                mapPanel.waitForAnimations();
            }
            monster.x += dir.deltaX;
            monster.y += dir.deltaY;
            map[monster.x][monster.y].setMonster(monster);
        } else if (tile.isSeen()) {//only show animation if within sight
            mapPanel.bump(new Point(monster.x, monster.y), dir);
            mapPanel.waitForAnimations();
        }
    }

    /**
     * Moves all the monsters, one at a time.
     */
    private void moveAllMonsters() {
        for (int i = 0; i < monsters.size(); i++) {
            moveMonster(monsters.get(i));
        }
    }

    /**
     * Sets up the frame for display and keyboard input.
     */
    private void initializeFrame() {
        frame = new JFrame("Final Lab - The Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            frame.setIconImage(ImageIO.read(new File("./icon.png")));
        } catch (IOException ex) {
            //don't do anything if it failed, the default Java icon will be used
        }

        Font font = new Font("Lucidia", Font.PLAIN, fontSize);

        keyListener = new SquidKey(true, SquidKey.CaptureType.DOWN);
        frame.addKeyListener(keyListener);

        Container panel = frame.getContentPane();
        panel.setBackground(SColor.BLACK);
        panel.setLayout(new BorderLayout());

        JLayeredPane layers = new JLayeredPane();
        TextCellFactory textFactory = new TextCellFactory().font(font).width(cellWidth).height(cellHeight).fit(CHARS_USED);
        mapPanel = new SquidPanel(width, height, textFactory, null);
        mapPanel.put(width / 2 - 4, height / 2, "Loading");
        mapPanel.refresh();
        mapBackPanel = new SquidPanel(width, height, textFactory, null);
        mapBackPanel.refresh();
        layers.setLayer(mapPanel, JLayeredPane.PALETTE_LAYER);
        layers.setLayer(mapBackPanel, JLayeredPane.DEFAULT_LAYER);
        layers.add(mapPanel);
        layers.add(mapBackPanel);

        layers.setSize(mapPanel.getPreferredSize());
        layers.setPreferredSize(mapPanel.getPreferredSize());
        panel.add(layers, BorderLayout.WEST);

        statsPanel = new SquidPanel(statWidth, mapPanel.gridHeight(), textFactory, null);
        statsPanel.setDefaultForeground(SColor.RUST);
        statsPanel.refresh();
        panel.add(statsPanel, BorderLayout.EAST);

        outputPanel = new SquidPanel(mapPanel.gridWidth() + statsPanel.gridWidth(), outputLines, textFactory, null);
        outputPanel.setDefaultForeground(SColor.BURNT_BAMBOO);
        outputPanel.refresh();
        panel.add(outputPanel, BorderLayout.SOUTH);

        frame.pack();
    }

}
