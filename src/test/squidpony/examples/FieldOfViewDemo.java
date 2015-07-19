package squidpony.examples;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.MouseInputListener;
import squidpony.SColor;
import squidpony.SColorFactory;
import squidpony.squidgrid.gui.SquidMouse;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.gui.TextCellFactory;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Direction;
import static squidpony.squidgrid.Direction.*;

/**
 * Demonstrates the use of the Field of View and Line of Sight algorithms.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FieldOfViewDemo {

    private SquidPanel display, back;
    private JFrame frame;
    private static final String[] DEFAULT_MAP = new String[]{
        "øøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøø########################################øøøøøøøøøøøøøøøøøøøøøøøøø",
        "øøøøøøøøøøøøøøøøøø#########øøøøøøøø#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#..m.mmmmmmmmmmmmmm..m...ø",
        "øøøøøøøøøøø########.......##øøøøøøø#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#.mmTmmmmmmmmmmmmmm......ø",
        "øøøøø#######.......₤.......###øøøøø#¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸,TTT¸,,¸¸¸¸¸¸m.TmmmmmmmmmTmmmmmmm..m..ø",
        "øøø###₤₤₤₤₤..................#øøøøø#¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸,TTT,,¸¸¸¸¸¸mmmmm≈≈≈mm..mmmmmmmmm....ø",
        "øøø#₤₤₤₤₤.₤₤....₤............##øøøø#¸¸¸¸¸¸¸¸¸¸¸TTT¸¸¸¸¸¸¸¸¸¸¸¸¸,¸¸,,¸¸¸¸¸¸mmm≈≈≈≈≈mm.m.mmmmmmm.....ø",
        "øø##.₤₤₤₤₤₤₤₤.................####ø#¸¸¸¸¸¸¸TTTTTT¸¸¸¸¸¸¸¸¸¸¸¸¸¸c,¸,,¸¸¸¸¸¸mm≈≈m≈mmmmmmmmm≈≈≈m..m...ø",
        "øø#..₤₤₤₤₤₤₤.....................###¸¸¸TTTTTT¸¸¸¸¸¸¸¸¸¸¸¸¸¸ct¸ctc,,,¸¸¸¸¸¸m≈≈mmmmmmmTmmm≈≈≈≈≈≈≈≈...ø",
        "øø#...₤₤₤₤₤............₤............¸¸¸¸¸TTTTTT¸¸¸¸¸¸¸¸¸¸¸¸ctt¸c¸¸,,¸¸¸¸¸¸mm≈≈mmmmmTmmm≈≈≈m≈≈mmmm..ø",
        "øø#.₤₤₤₤₤₤₤...........₤₤............¸¸TTTTT¸¸TTT¸¸¸¸¸¸¸¸¸¸¸¸cc¸¸¸¸,,¸¸¸¸¸¸mmm≈mmmmmmmmm≈≈mTm≈≈mmm..ø",
        "ø##₤₤₤₤₤₤₤₤₤.......................###############################,,¸¸¸¸S¸#mmmm≈m≈≈≈mm≈≈≈≈mmmmm≈mmmø",
        "ø#₤₤₤₤₤₤₤₤₤₤..₤....................#.....#.....#.....#.....#.....#+/#¸¸¸S¸#.T.mm≈≈≈≈≈≈≈≈≈mmmTmmmmm.ø",
        "ø#₤₤₤₤..₤₤₤₤₤......................#.....#.....#.....#.....#.....#..#¸¸¸¸¸#.TT.mmm≈≈≈≈≈≈≈≈mmm.mmT.mø",
        "ø#.₤₤₤.₤₤₤₤₤₤......₤...............#.....#.....#.....#.....#.....#..#######.TTTTmmm≈≈≈≈≈≈mmT..mmm.mø",
        "ø#₤₤₤₤₤₤₤₤₤........₤...............#.....#.....#.....#.....#.....#/+#.....#..TTmmmm≈≈≈≈≈≈TTmTmmTmmmø",
        "ø#₤₤₤₤₤₤₤₤₤₤.......................#.....#.....#.....#.....#.....#..#.....#..T.mmmm≈≈≈≈≈≈≈mmTmTm≈≈≈ø",
        "ø#₤₤₤₤₤₤₤₤₤₤.......................#.....###+#####+#####/#####+###..+.....#...Tmmmmm≈≈≈≈≈≈≈≈mmmmT≈≈ø",
        "##₤₤₤₤₤₤₤₤₤₤.......................#######..........................#.....#mT..mmmmmm≈≈≈≈≈≈≈mmm≈≈≈mø",
        "#₤₤..₤₤₤₤₤₤₤₤......................#.....#..........................#.....#m..mmmmmmm≈≈≈≈≈≈≈Tm≈≈≈mmø",
        "#₤..₤₤₤₤₤₤₤₤₤......................#.....#..........................#######..mmmmmmmmm≈≈≈≈≈≈m≈≈mmmmø",
        "#..................................#.....#...####################...#...#E#..mmm.mmmmm≈≈≈≈≈≈≈≈mmmmmø",
        "#..................................#.....#...+..E#..............#.../.../.#.......mmmm≈≈≈≈≈mmmmmmmmø",
        "#..................................#.....#...#####..............#...#...#E#........mm≈≈≈≈≈mmmmmmmmmø",
        "#..................................#.....#...#..................#...#######...m.....m≈≈≈≈mmmmmmmmmmø",
        "#..................................#.....#...#..................#.........+......mmmm≈≈mmm....mm≈≈mø",
        "#..................................#.....#...#..................#........./...uu...um≈≈mu.....m≈≈≈mø",
        "#..................................#.....#...#..................#...#+###+#..uuuuuuuu≈≈uu.u.ummmmuuø",
        "#..................................#.....#...#.................##...#..#c.#uuuuuuuuuu≈≈uuuAuuuuuuuuø",
        "#..................................#.....#...#................#.#...#E.#t.#uuuuuAuuA≈≈≈≈≈uuuuuuuuuuø",
        "#..................................#.....#...#...............#..#...#E<#c.#uuAuAuuu≈≈≈≈≈≈≈AuAAuuAuuø",
        "#..................................#.....#...#.............##.../...#######uAuAAA≈≈≈≈≈≈≈≈≈≈AAAAAAAuø",
        "#..................................#.....#...#............#.....#...#.....#AAAuA≈≈≈≈≈≈≈≈≈≈≈AAAAAAAAø",
        "#..................................#.....#...#............#.....#...#.....#AAAA≈≈≈≈≈≈≈≈≈≈≈≈≈AAAAAAAø",
        "#..................................#.....#...####################...#.....#AAAAu≈≈≈≈≈≈≈≈≈≈≈≈≈≈AAAAAø",
        "#............................#.....#.....#.......EEEEEEEEEEE........#.....#AAAAuu.≈≈≈≈mmm≈≈≈≈≈AAuAAø",
        "#..................................#.....#........####.####.........#.....#AAAuuuu≈≈≈≈≈mm≈≈≈≈AAuuAAø",
        "#..................................#.....#..........................#.....#AAAAuuuu≈≈≈≈≈≈≈≈≈AAuuuAAø",
        "#..................................#.....####+#.....##..........###/#.....#AAAAAAAuu..≈≈≈≈≈AAAAuAAAø",
        "#..................................#.....#E.+.#.....##..........#.........#AAAAAAAAA.AAA≈≈AAAAAAAAAø",
        "#............................##....#.....####.#.....##..........#tttt+#...#AAAAAAAA..AAAuuu.uAAAAAAø",
        "#...................#..............#.....#E.+.#.....#...........#..c..#...#AAAAAAA....AAAAu..uAAAAAø",
        "#...................#..............#.....####.#.....#...........###..E#...#AAAAAA...AAAAAuuu.uAAAAAø",
        "#..................................#.....#E.+.#.....#...........#E+.EE#...#AAAAAAAAAAAAAAAAAuuAAAAAø",
        "###########################################################################AAAAAAAAAAAAAAAAAAAAAAAAø"
    };
    private DemoCell[][] map;
    private double[][] incomingLight;
    private SColor[][] lighting, playerLight;
    private double[][] resistances;
    private boolean[][] clean, lightSource, visible;
    private double[][] visbilityMap;
    private int width = DEFAULT_MAP[0].length(), height = DEFAULT_MAP.length;
    private int cellWidth, cellHeight, locx, locy;
    private LOS los;
    private SColor litNear, litFar;
    private int lightForce; //controls how far the light will spread
    private FOVDemoPanel panel;

    public static void main(String... args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }
        new FieldOfViewDemo();
    }

    private FieldOfViewDemo() {
        map = new DemoCell[width][height];
        resistances = new double[width][height];
        lighting = new SColor[width][height];
        playerLight = new SColor[width][height];
        clean = new boolean[width][height];
        lightSource = new boolean[width][height];
        visible = new boolean[width][height];
        visbilityMap = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char c = DEFAULT_MAP[y].charAt(x);
                map[x][y] = buildCell(c);
                resistances[x][y] = map[x][y].resistance;
                lighting[x][y] = SColor.BLACK;//set to not lit
                playerLight[x][y] = SColor.BLACK;
            }
        }

        //put start location at middle of map
        locx = width / 2;
        locy = height / 2;

        frame = new JFrame("SquidGrid Field of View Demonstration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        panel = new FOVDemoPanel();
        frame.add(panel, BorderLayout.NORTH);

        panel.clearBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });

        cellWidth = 18;
        cellHeight = 18;
        TextCellFactory text = new TextCellFactory().font(new Font("Arial", Font.BOLD, 18)).width(cellWidth).height(cellHeight);
        display = new SquidPanel(width, height, text, null);
        back = new SquidPanel(width, height, text, null);
        clear();

        JLayeredPane layers = new JLayeredPane();
        layers.setLayer(display, JLayeredPane.PALETTE_LAYER);
        layers.setLayer(back, JLayeredPane.DEFAULT_LAYER);
        layers.add(display);
        layers.add(back);
        layers.setSize(display.getPreferredSize());
        layers.setPreferredSize(display.getPreferredSize());
        layers.setMinimumSize(display.getPreferredSize());
        frame.add(layers, BorderLayout.SOUTH);

        frame.getContentPane().setBackground(SColor.BLACK);
        frame.setVisible(true);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.repaint();

        DemoInputListener dil = new DemoInputListener();
        MouseInputListener mil = new SquidMouse(cellWidth, cellHeight, dil);
        display.addMouseListener(mil);//listens for clicks and releases
        display.addMouseMotionListener(mil);//listens for movement based events
        frame.addKeyListener(dil);
    }

    private void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                display.put(x, y, map[x][y].representation, map[x][y].color);
                lighting[x][y] = SColor.BLACK;
                clean[x][y] = false;
                lightSource[x][y] = false;
                visible[x][y] = false;
            }
        }
        back.erase();
        display.refresh();
    }

    private void draw() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!clean[x][y]) {
                    if (!panel.placeLightSourceBox.isSelected() && !visible[x][y]) {
                        display.put(x, y, SColor.BLACK);
                    } else {
                        if (lightSource[x][y]) {
                            display.put(x, y, '⊚', lighting[x][y]);
                        } else {
                            SColor color;
                            if (panel.playerCastsLightBox.isSelected()) {
                                color = SColorFactory.lightWith(map[x][y].color, SColorFactory.add(lighting[x][y], playerLight[x][y]));
                            } else {
                                color = SColorFactory.lightWith(map[x][y].color, lighting[x][y]);
                            }
                            display.put(x, y, map[x][y].representation, color);
                        }
                    }
                    clean[x][y] = true;
                }
            }
        }

        //put the player at the origin of the FOV
        if (!panel.placeLightSourceBox.isSelected()) {
            display.put(locx, locy, '@', SColor.ALICE_BLUE);
            clean[locx][locy] = false;
        }
        display.refresh();
    }

    /**
     * Builds a cell based on the character in the map.
     *
     * @param c
     * @return
     */
    private DemoCell buildCell(char c) {
        float resistance = 0f;//default is transparent
        SColor color;
        switch (c) {
            case '.'://stone ground
                color = SColor.SLATE_GRAY;
                break;
            case '¸'://grass
                color = SColor.GREEN;
                break;
            case ','://pathway
                color = SColor.STOREROOM_BROWN;
                c = '.';
                break;
            case 'c':
                color = SColor.SEPIA;
                break;
            case '/':
                color = SColor.BROWNER;
                break;
            case '≈':
                color = SColor.AZUL;
                break;
            case '<':
            case '>':
                color = SColor.SLATE_GRAY;
                break;
            case 't':
                color = SColor.BROWNER;
                resistance = 0.3f;
                break;
            case 'm':
                color = SColor.BAIKO_BROWN;
                resistance = 0.1f;
                break;
            case 'u':
                color = SColor.TAN;
                resistance = 0.2f;
                break;
            case 'T':
            case '₤':
                color = SColor.FOREST_GREEN;
                resistance = 0.7f;
                break;
            case 'E':
                color = SColor.SILVER;
                resistance = 0.8f;
                break;
            case 'S':
                color = SColor.BREWED_MUSTARD_BROWN;
                resistance = 0.9f;
                break;
            case '#':
                color = SColor.SLATE_GRAY;
                resistance = 1f;
                break;
            case '+':
                color = SColor.BROWNER;
                resistance = 1f;
                break;
            case 'A':
                color = SColor.ALICE_BLUE;
                resistance = 1f;
                break;
            case 'ø':
                c = ' ';
                color = SColor.BLACK;
                resistance = 1f;
                break;
            default://opaque items
                resistance = 1f;//unknown is opaque
                color = SColor.DEEP_PINK;
        }
        return new DemoCell(resistance, c, color);
    }

    private void move(Direction dir) {
        int x = locx + dir.deltaX;
        int y = locy + dir.deltaY;

        //check for legality of move based solely on map boundary
        if (x >= 0 && x < width && y >= 0 && y < height) {
            locx = x;
            locy = y;
            doFOV(x, y);
        }
    }

    /**
     * Performs the Field of View process
     *
     * @param startx
     * @param starty
     */
    private void doFOV(int startx, int starty) {
        lightForce = panel.radiusSlider.getValue();
        litNear = SColorFactory.asSColor(panel.castColorPanel.getBackground().getRGB());
        litFar = SColorFactory.asSColor(panel.fadeColorPanel.getBackground().getRGB());
        incomingLight = panel.getFOVSolver().calculateFOV(resistances, startx, starty, lightForce, panel.getStrategy());
        SColorFactory.addPallet("light", SColorFactory.asGradient(litNear, litFar));

        if (panel.placeLightSourceBox.isSelected()) {
            lightSource[startx][starty] = true;
        } else {
            visbilityMap = panel.getFOVSolver().calculateFOV(resistances, startx, starty, Math.max(width / 2, height / 2));
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    boolean wasvisible = visible[x][y];
                    visible[x][y] = visbilityMap[x][y] > 0f;
                    clean[x][y] = !(wasvisible || visible[x][y]);
                }
            }
        }

        //repaint the level with new light map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (panel.placeLightSourceBox.isSelected()) {
                    if (incomingLight[x][y] > 0) {
                        clean[x][y] = false;
                        if (!lightSource[x][y]) {//don't add extra light to light sources
                            double bright = 1 - incomingLight[x][y];
                            lighting[x][y] = SColorFactory.add(lighting[x][y], SColorFactory.fromPallet("light", (float) bright));
                        }
                        if (x == startx && y == starty) {//light source is given it's full light
                            lighting[x][y] = litNear;
                        }
                    }
                } else {
                    if (panel.playerCastsLightBox.isSelected()) {
                        if (incomingLight[x][y] > 0) {
                            double bright = 1 - incomingLight[x][y];
                            playerLight[x][y] = SColorFactory.fromPallet("light", (float) bright);
                            clean[x][y] = false;
                        } else if (!playerLight[x][y].equals(SColor.BLACK)) {
                            playerLight[x][y] = SColor.BLACK;
                            clean[x][y] = false;
                        }
                    }
                }
            }
        }

        draw();
    }

    /**
     * Performs the Line of Sight calculation and paints target square with a green background if it can be reached or a
     * red if not.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     */
    private void doLOS(int startx, int starty, int endx, int endy) {//TODO -- figure out why this does strange things if dragged and released in same cell
        los = panel.getLOSSolver();

        //run the LOS calculation
        boolean seen = los.isReachable(resistances, startx, starty, endx, endy);
        Queue<Point> path = los.getLastPath();

        //draw out background for path followed
        for (Point p : path) {
            back.put(p.x, p.y, SColorFactory.blend(SColor.BLUE_GREEN_DYE, SColor.DARK_INDIGO, panel.getStrategy().radius(startx, starty, p.x, p.y) / panel.getStrategy().radius(startx, starty, endx, endy)));
        }

        //mark the start location
        if (startx >= 0 && startx < width && starty >= 0 && starty < height) {
            back.put(startx, starty, SColor.AMBER_DYE);
        }

        //mark end point
        if (endx >= 0 && endx < width && endy >= 0 && endy < height) {
            if (seen) {
                back.put(endx, endy, SColor.BRIGHT_GREEN);
            } else {
                back.put(endx, endy, SColor.RED_PIGMENT);
            }
            back.refresh();
        }
    }

    /**
     * A simple input listener.
     */
    private class DemoInputListener implements MouseInputListener, KeyListener {

        int startx, starty;
        boolean dragged = false;

        @Override
        public void mouseClicked(MouseEvent e) {
            dragged = false;
            locx = e.getX();
            locy = e.getY();
            doFOV(e.getX(), e.getY());
            if (!panel.placeLightSourceBox.isSelected()) {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        visible[x][y] = false;
                        clean[x][y] = false;
                    }
                }
                draw();
                doFOV(locx, locy);
            }
            frame.requestFocusInWindow();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dragged = false;
            startx = e.getX();
            starty = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragged && (startx != e.getX() || starty != e.getY())) {
                doLOS(startx, starty, e.getX(), e.getY());
            }
            dragged = false;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //nothing special happens
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //nothing special happens
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            dragged = true;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
//            int x = e.getX();
//            int y = e.getY();
//            String val = String.format("%.2f", visbilityMap[x][y]);
//            panel.tileValueField.setText(val);
//            System.out.println("" + x + ", " + y + " : " + val);
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getExtendedKeyCode();
            move(getDirection(code));
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        private Direction getDirection(int code) {
            switch (code) {
                case VK_LEFT:
                case VK_NUMPAD4:
                    return LEFT;
                case VK_RIGHT:
                case VK_NUMPAD6:
                    return RIGHT;
                case VK_UP:
                case VK_NUMPAD8:
                    return UP;
                case VK_DOWN:
                case VK_NUMPAD2:
                    return DOWN;
                case VK_NUMPAD1:
                    return DOWN_LEFT;
                case VK_NUMPAD3:
                    return DOWN_RIGHT;
                case VK_NUMPAD7:
                    return UP_LEFT;
                case VK_NUMPAD9:
                    return UP_RIGHT;
                default:
                    return NONE;
            }
        }
    }

    private class DemoCell {

        float resistance;
        char representation;
        SColor color;

        /**
         * Creates a new cell which has minimal properties needed to represent it.
         *
         * @param resistance
         * @param color
         * @param representation
         */
        public DemoCell(float resistance, char representation, SColor color) {
            this.resistance = resistance;
            this.representation = representation;
            this.color = color;
        }
    }
}
