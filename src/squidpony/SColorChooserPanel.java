package squidpony;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.MouseInputListener;

/**
 * This class provides a way to interact with the pre-defined SColor constants
 * in a Swing GUI.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SColorChooserPanel extends AbstractColorChooserPanel {

    private SColor[] colors = SColor.FULL_PALLET;
    private TreeMap<String, SColor[]> colorMap = new TreeMap<>();
    private int colorHeight = 20;
    private int colorWidth = 40;
    private ColorPanel displayPanel = new ColorPanel();
    private JScrollPane scrollPane = new JScrollPane();
    private JComboBox colorComboBox = new JComboBox();
    private JTextField colorName = new JTextField();
    private boolean initialized = false;

    @Override
    public void updateChooser() {
    }

    @Override
    protected void buildChooser() {

        scrollPane.setViewportView(displayPanel);
        displayPanel.addMouseListener(displayPanel);
        displayPanel.addMouseMotionListener(displayPanel);
        colorComboBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorComboBoxActionPerformed(evt);
            }
        });

        colorMap.put("Achromatic Series", SColor.ACHROMATIC_SERIES);
        colorMap.put("Blue Green Series", SColor.BLUE_GREEN_SERIES);
        colorMap.put("Blue Violet Series", SColor.BLUE_VIOLET_SERIES);
        colorMap.put("Full Pallet", SColor.FULL_PALLET);
        colorMap.put("Rainbow", SColor.RAINBOW);
        colorMap.put("Red Series", SColor.RED_SERIES);
        colorMap.put("Red Violet Series", SColor.RED_VIOLET_SERIES);
        colorMap.put("Violet Series", SColor.VIOLET_SERIES);
        colorMap.put("Yellow Green Series", SColor.YELLOW_GREEN_SERIES);
        colorMap.put("Yellow Red Series", SColor.YELLOW_RED_SERIES);
        colorMap.put("Yellow Series", SColor.YELLOW_SERIES);

        colorComboBox.setModel(new DefaultComboBoxModel(colorMap.keySet().toArray()));
        setLayout(new BorderLayout());
        add(colorComboBox, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        colorName.setEditable(false);
        colorName.setHorizontalAlignment(JTextField.CENTER);
        add(colorName, BorderLayout.SOUTH);

        initialized = true;
    }

    private void refreshPanel() {
        scrollPane.getViewport().setViewSize(displayPanel.getPreferredScrollableViewportSize());
        scrollPane.getViewport().revalidate();
        repaint();
    }

    private void colorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        colors = colorMap.get((String) colorComboBox.getSelectedItem());
        refreshPanel();
    }

    private void displayPanelMouseMoved(java.awt.event.MouseEvent evt) {
        int index = evt.getY() / 20;
        if (index < colors.length) {
            String text = "";
            switch (evt.getX() / colorWidth) {
                case 0:
                    text += "Fully Desaturated ";
                    break;
                case 1:
                    text += "80% Desaturated ";
                    break;
                case 2:
                    text += "Lightest ";
                    break;
                case 3:
                    text += "Lighter ";
                    break;
                case 4:
                    text += "Light ";
                    break;
                case 6:
                    text += "Dim ";
                    break;
                case 7:
                    text += "Dimmer ";
                    break;
                case 8:
                    text += "Dimmest ";
                    break;
            }
            text += colors[index].getName();
//            displayPanel.setToolTipText(text);//don't need the tooltip with the display area
            colorName.setText(text);
        }
    }

    @Override
    public String getDisplayName() {
        return "SColor";
    }

    @Override
    public Icon getSmallDisplayIcon() {
        return null;
    }

    @Override
    public Icon getLargeDisplayIcon() {
        return null;
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (initialized) {
            displayPanel.setBackground(bg);
            scrollPane.setBackground(bg);
            colorComboBox.setBackground(bg);
            colorName.setBackground(bg);
        }
    }

    private class ColorPanel extends JPanel implements Scrollable, MouseInputListener {

        ColorPanel() {
            super(new FlowLayout());
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(colorWidth * 9, colorHeight * colors.length);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int x = 0;
            int y = 0;

            g.setColor(SColor.WHITE);
            g.fillRect(0, 0, getWidth(), colorHeight);

            for (int i = 0; i < colors.length; i++) {
                SColor color = colors[i];

                g.setColor(SColorFactory.desaturated(color));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.desaturate(color, 0.8));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.lightest(color));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.lighter(color));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.light(color));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(color);
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.dim(color));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.dimmer(color));
                g.fillRect(x, y, colorWidth, colorHeight);
                x += colorWidth;

                g.setColor(SColorFactory.dimmest(color));
                g.fillRect(x, y, getWidth() - x, colorHeight);//last column fills out the rest of the space

                x = 0;
                y += colorHeight;
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(colorWidth * 9, Math.min(400, colorHeight * colors.length));
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle rctngl, int i, int i1) {
            if (i == SwingConstants.VERTICAL) {
                return colorHeight;
            } else {
                return colorWidth;
            }
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle rctngl, int i, int i1) {
            if (i == SwingConstants.VERTICAL) {
                return colorHeight * 5;
            } else {
                return colorWidth * 3;
            }
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Robot robot = new Robot();
                getColorSelectionModel().setSelectedColor(robot.getPixelColor(e.getXOnScreen(), e.getYOnScreen()));
            } catch (AWTException ex) {
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
            displayPanelMouseMoved(e);
        }
    }
}
