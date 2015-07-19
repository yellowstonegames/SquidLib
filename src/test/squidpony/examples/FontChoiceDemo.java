package squidpony.examples;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import squidpony.SColor;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidgrid.gui.TextCellFactory;

/**
 * Demonstrates some of the capabilities of the squidpony.squidgrid package.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class FontChoiceDemo {

    private SquidPanel display, back;
    private JMenu menu;
    private JFrame frame;
    private FontChoiceControlPanel control;
    private int width = 16, height = 16;
    private Random rng = new Random();
    private SColor foreground, background;
    private TextCellFactory textFactory;

    public FontChoiceDemo() {
        frame = new JFrame("SquidGrid and SquidColor Font Choice Demonstration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //build menu
        JMenuBar bar = new JMenuBar();
        menu = new JMenu("Tools");
        bar.add(menu);
        JMenuItem tempItem = new JMenuItem("Save Image");
        tempItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });
        menu.add(tempItem);
        frame.setJMenuBar(bar);

        control = new FontChoiceControlPanel(width, height);
        frame.getContentPane().add(control, BorderLayout.NORTH);

        textFactory = new TextCellFactory().font(control.getFontFace()).width(control.getCellWidth()).height(control.getCellHeight());
        display = new SquidPanel(width, height, textFactory, null);
        back = new SquidPanel(width, height, textFactory, null);

        final JLayeredPane layers = new JLayeredPane();
        layers.setLayer(display, JLayeredPane.PALETTE_LAYER);
        layers.setLayer(back, JLayeredPane.DEFAULT_LAYER);
        layers.add(display);
        layers.add(back);
        layers.setSize(display.getPreferredSize());
        layers.setPreferredSize(display.getPreferredSize());
        layers.setMinimumSize(display.getPreferredSize());
        frame.add(layers, BorderLayout.SOUTH);
        frame.getContentPane().setBackground(SColor.BLACK);

        String text = "";
        for (char c = 33; c <= 125; c++) {
            text += c;
        }
        control.inputTextArea.setText(text);

        control.updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                control.validateInput();
                layers.remove(display);
                layers.remove(back);

                textFactory = new TextCellFactory();
                textFactory.font(control.getFontFace()).width(control.getCellWidth()).height(control.getCellHeight());
                textFactory.antialias(control.antialiasBox.isSelected());
                textFactory.topPadding(control.getTopPad());
                textFactory.bottomPadding(control.getBottomPad());
                textFactory.leftPadding(control.getLeftPad());
                textFactory.rightPadding(control.getRightPad());
                textFactory.fit(control.inputTextArea.getText());
                display = new SquidPanel(width, height, textFactory, null);
                back = new SquidPanel(width, height, textFactory, null);

                layers.setLayer(display, JLayeredPane.PALETTE_LAYER);
                layers.setLayer(back, JLayeredPane.DEFAULT_LAYER);
                layers.add(display);
                layers.add(back);
                layers.setSize(display.getPreferredSize());
                layers.setPreferredSize(display.getPreferredSize());
                layers.setMinimumSize(display.getPreferredSize());
                changeDisplay();
            }
        });

        control.validateInput();
        control.updateButton.doClick();

        frame.setVisible(true);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.repaint();
    }

    /**
     * Saves the current display panel as an image.
     */
    private void saveImage() {
        BufferedImage image = (BufferedImage) display.createImage(display.getWidth(), display.getHeight());
        Graphics2D g = image.createGraphics();
        display.paint(g);
        g.dispose();
        try {
            ImageIO.write(image, "png", new File("text.png"));
        } catch (IOException ex) {
            Logger.getLogger(FontChoiceDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Uses the information in the control panel to update the display.
     */
    private void changeDisplay() {
        control.fontSizeField.setText("" + textFactory.font().getSize());
        control.cellWidthField.setText("" + textFactory.width());
        control.cellHeightField.setText("" + textFactory.height());
        foreground = new SColor(control.foreColorPanel.getBackground());
        background = new SColor(control.backColorPanel.getBackground());
        String text = control.inputTextArea.getText();
        if (text.length() > 0) {
            int position = 0;
            for (int y = 0; y < display.gridHeight(); y++) {
                for (int x = 0; x < display.gridWidth(); x++) {
                    if (control.colorizeToggleButton.isSelected()) {
                        foreground = SColor.FULL_PALLET[rng.nextInt(SColor.FULL_PALLET.length)];
                        background = SColor.FULL_PALLET[rng.nextInt(SColor.FULL_PALLET.length)];
                    }
                    if (position < text.codePointCount(0, text.length())) {
                        int code = text.codePointAt(position);
                        display.put(x, y, code, foreground);
                        back.put(x, y, background);
                        position += Character.isBmpCodePoint(code) ? 1 : 2;//have to move two positions if it's a surrogate pair
                    } else {
                        display.clear(x, y);
                        back.put(x, y, background);
                    }
                }
            }
        } else {
            for (int x = 0; x < display.gridWidth(); x++) {
                for (int y = 0; y < display.gridHeight(); y++) {
                    if (control.colorizeToggleButton.isSelected()) {
                        foreground = SColor.FULL_PALLET[rng.nextInt(SColor.FULL_PALLET.length)];
                        background = SColor.FULL_PALLET[rng.nextInt(SColor.FULL_PALLET.length)];
                    }
                    if (rng.nextBoolean()) {
                        display.put(x, y, (char) ('A' + (x + y) % 26), foreground);
                        back.put(x, y, background);
                    } else {
                        display.put(x, y, (char) ('a' + (x + y) % 26), foreground);
                        back.put(x, y, background);
                    }
                }
            }
        }
        display.refresh();
        back.refresh();
        frame.pack();
        frame.repaint();
    }

    public static void main(String... args) {
        new FontChoiceDemo();
    }
}
