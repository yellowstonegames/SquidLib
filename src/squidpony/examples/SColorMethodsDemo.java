package squidpony.examples;

import squidpony.squidcolor.SColorFactory;

/**
 * Shows the SColorChooser.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SColorMethodsDemo {

    public static void main(String... args) {
        SColorFactory.showSColorChooser(null);
        //previous method blocks, so exit once closed
        System.exit(0);
    }
}
