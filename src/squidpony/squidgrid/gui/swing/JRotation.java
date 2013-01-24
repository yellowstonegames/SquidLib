package squidpony.squidgrid.gui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A rotation selection widget for use in Swing applications.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class JRotation extends JPanel implements MouseMotionListener {

    private double rotation = 1;

    /**
     * For testing the widget.
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        final JRotation rot = new JRotation();
        f.getContentPane().add(rot);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        rot.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println(rot.getRotation());
            }
        });
    }

    /**
     * Returns the rotation from 0 to 2 pi.
     *
     * @return
     */
    public double getRotation() {
        return rotation;
    }

    public JRotation() {
        setPreferredSize(new Dimension(100, 100));
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setPaint(Color.WHITE);
        int diameter = Math.min(getWidth(), getHeight());
        g2.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter);

        g2.setPaint(Color.BLACK);
        AffineTransform t = g2.getTransform();
        g2.translate(getWidth() / 2, getHeight() / 2);
        g2.rotate(-rotation);
        g2.drawLine(0, 0, Math.max(getWidth(), getHeight()), 0);
        g2.setTransform(t);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int midX = getWidth() / 2;
        int midY = getHeight() / 2;
        if (x > midX) {
            if (y > midY) { // fourth quadrant
                rotation = 2 * Math.PI - Math.atan((y - midY) / (double) (x - midX));
            } else { // first quadrant
                rotation = Math.atan((midY - y) / (double) (x - midX));
            }
        } else {
            if (y > midY) { // third quarter
                rotation = Math.PI + Math.atan((y - midY) / (double) (midX - x));
            } else { // second quarter
                rotation = Math.PI - Math.atan((midY - y) / (double) (midX - x));
            }
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }
}
