package squidpony.examples.squidgrid.gui.swing.listener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import squidpony.squidgrid.gui.SGKeyListener;
import squidpony.squidgrid.gui.SGKeyListener.CaptureType;

/**
 * Demonstrates the use of SGKeyListener
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SGKeyListenerDemo {

    public static void main(String[] args) {
        JFrame frame = new JFrame("SGKeyListenerDemo -- Press 'x' to exit");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SGKeyListener key = new SGKeyListener(true, CaptureType.DOWN);
        frame.addKeyListener(key);

        JLabel label = new JLabel("Pressed Character");
        frame.add(label);

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);//center the frame on the screen

        char c;
        do {
            frame.repaint();
            c = key.next().getKeyChar();
            label.setText("\nKey pressed: " + c);
        } while (true);
    }
}
