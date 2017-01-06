package squidpony.examples;

import squidpony.StringKit;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    private void go() {
        System.out.println(StringKit.longFromHex("1234567890ABCDEF") == 0x1234567890ABCDEFL);
        System.out.println(StringKit.longFromBin("1") == 1L);
    }

}
