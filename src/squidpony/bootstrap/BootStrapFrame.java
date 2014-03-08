package squidpony.bootstrap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import squidpony.annotation.Beta;
import squidpony.squidcolor.SColor;
import squidpony.squidcolor.SColorFactory;
import squidpony.squidgrid.gui.SGPane;
import squidpony.squidgrid.gui.awt.TextCellFactory;
import squidpony.squidgrid.gui.awt.event.SGMouseListener;
import squidpony.squidgrid.gui.swing.SwingPane;
import squidpony.squidgrid.util.Direction;

/**
 * Builds, manages, and displays a JFrame with a SwingPane in it allowing for a minimal-effort
 * construction of a GUI for use with SquidLib. Includes a 2-line text output area and side bar for
 * stats.
 *
 * The stats bar accepts input as a LinkedHashMap<String, Integer> and displays each entry on its
 * own line with the following restrictions. The String value is left justified. The Integer value
 * is right justified. If there is not enough space to display both, the Integer will take
 * precedence. In any case there will be at least one empty space between each String and Integer.
 * If there is enough vertical room, there will also be one empty horizontal space between each
 * pair. If there are more pairs than vertical space, they will be displayed in a first come, first
 * server order. The stats area has enough width to support "STR: 19" output.
 *
 * The output display area has the following restrictions. It displays three lines of text at a
 * time. Because it runs under both the map and stats areas, it can display slightly more characters
 * than the width of the main map. It includes a clickable scroll function (on the right-hand side)
 * to display older messages. It does not include the --MORE-- functionality seen in many
 * roguelikes. When a new message is output, the view is automatically scrolled to the bottom. For
 * best use, ensure that either only short messages are used or that messages no longer (in
 * characters) than two times the width of the map are used. This will allow all output to be
 * visible when it is output.
 *
 * This is purposefully a bare-bones GUI. For more control over appearance and things such as
 * multiple panels, a custom JFrame and direct use of SwingPane objects should be used.
 *
 * Amongst the limitations in this implementation are that number keys are always treated as
 * direction keys (appropriate for the numpad) and can't therefor be used in any other way. Output
 * strings will have any whitespace collapsed into a single space, although line returns are
 * respected.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class BootStrapFrame implements SGPane {

    private JFrame frame;
    private SwingPane mapPanel, statsPanel, outputPanel;
    private final GameLogic logic;
    private KeyListener keyListener;
    private final int outputLines = 3;
    private final int statsWidth = 8;
    private final int width, height;
    private final ArrayList<String> outputMessages = new ArrayList<>();
    private int displayedMessage = 0;
    private LinkedHashMap<String, Integer> stats = new LinkedHashMap<>();
    private Font font;
    private char upChar, downChar;

    /**
     * Builds and displays a top level GUI window with the provided grid width and height. The GUI
     * is guaranteed to fit within the screen resolution it is opened on.
     *
     * The GameLogic class passed in is where a program should respond to input. Any resulting
     * visual changes from that input can then be made on this SFrame.
     *
     * @param width
     * @param height
     * @param logic
     */
    public BootStrapFrame(int width, int height, GameLogic logic) {
        this.width = width;
        this.height = height;
        this.logic = logic;
        font = new Font("Lucidia", Font.PLAIN, 72);
        upChar = font.canDisplay('▲') ? '▲' : 'U';
        downChar = font.canDisplay('▼') ? '▼' : 'D';

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                initFrame();
            }
        });
        
        logic.beginGame();
    }

    /**
     * Sets up a JFrame and internal panels for a basic roguelike experience.
     */
    public void initFrame() {
        frame = new JFrame("SquidLib Bootstrap");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setUndecorated(true);
        try {
            frame.setIconImage(ImageIO.read(new File("./icon.png")));
        } catch (IOException ex) {
            //don't do anything if it failed, the default Java icon will be used
        }

        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        frame.getContentPane().setBackground(SColorFactory.dimmest(SColor.DARK_INDIGO));

        TextCellFactory textFactory = new TextCellFactory();
        textFactory.setAntialias(true);
        textFactory.addFit(new char[]{upChar, downChar});

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        int pixelWidth = frame.getContentPane().getWidth();
        int pixelHeight = frame.getContentPane().getHeight();
        pixelWidth = (int) Math.ceil(pixelWidth / (width + statsWidth));//convert to pixels per grid cell
        pixelHeight = (int) Math.ceil(pixelHeight / (height + outputLines));//convert to pixels per grid cell
        textFactory.initializeBySize(pixelWidth, pixelHeight, font);

        mapPanel = new SwingPane(width, height, textFactory);
        mapPanel.placeHorizontalString(width / 2 - 4, height / 2, "Loading...");
        mapPanel.refresh();
        frame.add(mapPanel, BorderLayout.WEST);

        statsPanel = new SwingPane(statsWidth, mapPanel.getGridHeight(), textFactory);
        statsPanel.setDefaultBackground(SColor.DARK_GRAY);
        statsPanel.setDefaultForeground(SColor.RUST);
        statsPanel.refresh();
        frame.add(statsPanel, BorderLayout.EAST);

        outputPanel = new SwingPane(mapPanel.getGridWidth() + statsPanel.getGridWidth(), outputLines, textFactory);
        outputPanel.setDefaultBackground(SColor.BLACK);
        outputPanel.setDefaultForeground(SColor.DODGER_BLUE);
        outputPanel.placeCharacter(outputPanel.getGridWidth() - 1, 0, upChar, SColor.DARK_BLUE_DYE, SColor.SILVER);
        outputPanel.placeCharacter(outputPanel.getGridWidth() - 1, outputPanel.getGridHeight() - 1, downChar, SColor.DARK_BLUE_DYE, SColor.SILVER);
        for (int y = 1; y < outputPanel.getGridHeight() - 1; y++) {//fill in vertical line between scroll arrowheads
            outputPanel.clearCell(outputPanel.getGridWidth() - 1, y, SColor.SILVER);
        }
        updateOutput();
        outputPanel.addMouseListener(new SGMouseListener(outputPanel.getCellWidth(), outputPanel.getCellHeight(), new OutputMouseListener()));

        frame.add(outputPanel, BorderLayout.SOUTH);

        keyListener = initKeyListener();
        frame.addKeyListener(keyListener);

        frame.getContentPane().setBackground(SColor.BLACK);
        frame.validate();

        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Builds a KeyListener with some directional input defaults.
     *
     * @return
     */
    private KeyListener initKeyListener() {

        return new KeyListener() {
            private final HashMap<Integer, Direction> dirKeys;

            {
                dirKeys = new HashMap<>();
                dirKeys.put(KeyEvent.VK_LEFT, Direction.LEFT);
                dirKeys.put(KeyEvent.VK_RIGHT, Direction.RIGHT);
                dirKeys.put(KeyEvent.VK_DOWN, Direction.DOWN);
                dirKeys.put(KeyEvent.VK_UP, Direction.UP);
                dirKeys.put(KeyEvent.VK_NUMPAD4, Direction.LEFT);
                dirKeys.put(KeyEvent.VK_NUMPAD6, Direction.RIGHT);
                dirKeys.put(KeyEvent.VK_NUMPAD2, Direction.DOWN);
                dirKeys.put(KeyEvent.VK_NUMPAD8, Direction.UP);
                dirKeys.put(KeyEvent.VK_NUMPAD1, Direction.DOWN_LEFT);
                dirKeys.put(KeyEvent.VK_NUMPAD3, Direction.DOWN_RIGHT);
                dirKeys.put(KeyEvent.VK_NUMPAD7, Direction.UP_LEFT);
                dirKeys.put(KeyEvent.VK_NUMPAD9, Direction.UP_RIGHT);
                dirKeys.put(KeyEvent.VK_NUMPAD5, Direction.NONE);
                dirKeys.put(KeyEvent.VK_4, Direction.LEFT);
                dirKeys.put(KeyEvent.VK_6, Direction.RIGHT);
                dirKeys.put(KeyEvent.VK_2, Direction.DOWN);
                dirKeys.put(KeyEvent.VK_8, Direction.UP);
                dirKeys.put(KeyEvent.VK_1, Direction.DOWN_LEFT);
                dirKeys.put(KeyEvent.VK_3, Direction.DOWN_RIGHT);
                dirKeys.put(KeyEvent.VK_7, Direction.UP_LEFT);
                dirKeys.put(KeyEvent.VK_9, Direction.UP_RIGHT);
                dirKeys.put(KeyEvent.VK_5, Direction.NONE);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    logic.acceptKeyboardInput(e.getKeyChar());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (dirKeys.containsKey(e.getExtendedKeyCode())) {
                    logic.acceptDirectionInput(dirKeys.get(e.getExtendedKeyCode()));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        };
    }

    /**
     * Displays the provided string in the output area. If the string is too long to fit, it will be
     * broken up at spaces if possible. If the string is too long to fit entirely within the output
     * buffer, part of it will be lost.
     *
     * In roguelike tradition, the most recent output line will be at the bottom of the output list.
     *
     * When this method is called, the output area is automatically scrolled to the bottom of the
     * output queue.
     *
     * @param message
     */
    public void output(String message) {
        Scanner scan = new Scanner(message);

        outputMessages.add(0, "");//add empty string as spacer between each message

        while (scan.hasNext()) {
            String working = "";

            //scan each line so that line breaks in message are respected
            Scanner line = new Scanner(scan.nextLine());
            while (line.hasNext()) {
                String word = line.next();
                if (word.length() >= width + statsWidth - 2) {//word can't fit on its own line, just break it
                    while (word.length() >= width + statsWidth - 2) {//keep breaking it up as needed
                        int breaker = width + statsWidth - 3 - working.length();
                        working += word.substring(0, breaker);
                        outputMessages.add(0, working);
                        word = word.substring(breaker);
                        working = "";
                    }
                    working = word;//the remainder gets put in
                } else if (word.length() + working.length() < width + statsWidth - 2) {//word will fit so just add it
                    working += word + " ";
                } else {//word needs a new line
                    outputMessages.add(0, working);
                    working = word;
                }
            }
            outputMessages.add(0, working);
        }

        displayedMessage = 0;
        updateOutput();
    }

    private void updateOutput() {
        for (int x = 0; x < outputPanel.getGridWidth() - 1; x++) {//clear everything but the scroll arrows
            for (int y = 0; y < outputPanel.getGridHeight(); y++) {
                outputPanel.clearCell(x, y);
            }
        }

        boolean recent = displayedMessage == 0;//only the recent message is message 0
        for (int y = 0; y < outputLines; y++) {
            int i = displayedMessage + y;
            if (outputMessages.size() > i) {
                String output = outputMessages.get(i);
                if (recent && (output == null || output == "")) {//after the first empty string all messages are old
                    recent = false;
                }
                outputPanel.placeHorizontalString(0, y, output, recent ? SColor.DODGER_BLUE : SColorFactory.dimmest(SColor.PALE_CORNFLOWER_BLUE), SColor.BLACK);
            }
        }

        outputPanel.refresh();
    }

    public void setStats(LinkedHashMap<String, Integer> statsMap) {
        stats = statsMap;
        updateStats();
    }

    public void updateStat(String stat, Integer value) {
        stats.put(stat, value);
        updateStats();
    }

    private void updateStats() {
        int i = 0;
        for (String s : stats.keySet()) {
            statsPanel.placeHorizontalString(0, i, s);
            String value = stats.get(s).toString();
            statsPanel.placeHorizontalString(statsWidth - 1 - value.length(), i, value);
            i += 2;
        }
    }

    @Override
    public void highlight(int x, int y) {
        mapPanel.highlight(x, y);
    }

    @Override
    public void highlight(int startx, int starty, int endx, int endy) {
        mapPanel.highlight(startx, starty, endx, endy);
    }

    @Override
    public void removeHighlight() {
        mapPanel.removeHighlight();
    }

    @Override
    public int getCellHeight() {
        return mapPanel.getCellHeight();
    }

    @Override
    public int getCellWidth() {
        return mapPanel.getCellWidth();
    }

    @Override
    public int getGridHeight() {
        return mapPanel.getGridHeight();
    }

    @Override
    public int getGridWidth() {
        return mapPanel.getGridHeight();
    }

    @Override
    public void setMaxDisplaySize(int width, int height) {
        mapPanel.setMaxDisplaySize(width, height);
    }

    @Override
    public void initialize(int cellWidth, int cellHeight, int gridWidth, int gridHeight, Font font) {
        mapPanel.initialize(cellWidth, cellHeight, gridWidth, gridHeight, font);
    }

    @Override
    public void initialize(int gridWidth, int gridHeight, Font font) {
        mapPanel.initialize(gridWidth, gridHeight, font);
    }

    @Override
    public void clearCell(int x, int y) {
        mapPanel.clearCell(x, y);
    }

    @Override
    public void clearCell(int x, int y, Color color) {
        mapPanel.clearCell(x, y, color);
    }

    @Override
    public void setCellBackground(int x, int y, Color color) {
        mapPanel.setCellBackground(x, y, color);
    }

    @Override
    public void placeCharacter(int x, int y, char c) {
        mapPanel.placeCharacter(x, y, c);
    }

    @Override
    public void placeCharacter(int x, int y, char c, Color fore, Color back) {
        mapPanel.placeCharacter(x, y, c, fore, back);
    }

    @Override
    public void placeCharacter(int x, int y, char c, Color fore) {
        mapPanel.placeCharacter(x, y, c, fore);
    }

    @Override
    public void placeHorizontalString(int xOffset, int yOffset, String string) {
        mapPanel.placeHorizontalString(xOffset, yOffset, string);
    }

    @Override
    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        mapPanel.placeHorizontalString(xOffset, yOffset, string, foreground, background);
    }

    @Override
    public void placeImage(int x, int y, String key) {
        mapPanel.placeImage(x, y, key);
    }

    @Override
    public void placeImage(int x, int y, String key, Color background) {
        mapPanel.placeImage(x, y, key, background);
    }

    @Override
    public void placeText(int xOffset, int yOffset, char[][] chars) {
        mapPanel.placeText(xOffset, yOffset, chars);
    }

    @Override
    public void placeText(int xOffset, int yOffset, char[][] chars, Color foreground, Color background) {
        mapPanel.placeText(xOffset, yOffset, chars, foreground, background);
    }

    @Override
    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        mapPanel.placeVerticalString(xOffset, yOffset, string, foreground, background);
    }

    @Override
    public void placeVerticalString(int xOffset, int yOffset, String string) {
        mapPanel.placeVerticalString(xOffset, yOffset, string);
    }

    @Override
    public void refresh() {
        mapPanel.refresh();
    }

    @Override
    public void setDefaultBackground(Color defaultBackground) {
        mapPanel.setDefaultBackground(defaultBackground);
    }

    @Override
    public void setDefaultForeground(Color defaultForeground) {
        mapPanel.setDefaultForeground(defaultForeground);
    }

    @Override
    public void setText(char[][] chars) {
        mapPanel.setText(chars);
    }

    @Override
    public boolean willFit(char character) {
        return mapPanel.willFit(character);
    }

    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param location
     * @param direction
     */
    public void bump(Point location, Direction direction) {
        mapPanel.bump(location, new Point(direction.deltaX, direction.deltaY));
    }

    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param location
     * @param direction
     */
    public void bump(Point location, Point direction) {
        mapPanel.bump(location, Direction.UP);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     *
     * @param start
     * @param end
     */
    public void slide(Point start, Point end) {
        mapPanel.slide(start, end);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed
     * for one grid square in the direction provided.
     *
     * @param start
     * @param direction
     */
    public void slide(Point start, Direction direction) {
        mapPanel.slide(start, direction);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided
     * speed. The speed is how many milliseconds should pass between movement steps.
     *
     * @param start
     * @param end
     * @param speed
     */
    public void slide(Point start, Point end, int speed) {
        mapPanel.slide(start, end, speed);
    }

    /**
     * Starts an wiggling animation for the object at the given location.
     *
     * @param location
     */
    public void wiggle(Point location) {
        mapPanel.wiggle(location);
    }

    /**
     * Causes the component to stop responding to input until all current animations are finished.
     *
     * Note that if an animation is set to not stop then this method will never return.
     */
    public void waitForAnimations() {
        mapPanel.waitForAnimations();
    }

    /**
     * Returns true if there are animations running when this method is called.
     *
     * Note that due to the nature of animations ending at various times, this result is not
     * guaranteed to be accurate.
     *
     * To fully ensure no animations are running, waitForAnimations() must be used.
     *
     * @return
     */
    public boolean hasActiveAnimations() {
        return mapPanel.hasActiveAnimations();
    }

    private class OutputMouseListener implements MouseInputListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getX() == width + statsWidth - 1) {
                if (e.getY() == 0) {
                    displayedMessage = Math.max(0, displayedMessage - 1);
                    updateOutput();
                } else if (e.getY() == outputLines - 1) {
                    displayedMessage = Math.min(outputMessages.size() - 1, displayedMessage + 1);
                    updateOutput();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

}
