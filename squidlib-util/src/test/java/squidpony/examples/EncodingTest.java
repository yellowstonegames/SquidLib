package squidpony.examples;

import squidpony.StringKit;

/**
 * Created by Tommy Ettinger on 6/1/2016.
 */
public class EncodingTest {
    public static void main(String[] args)
    {
        CharSequence cs = StringKit.encode(new byte[][]{
                new byte[]{1,2, 3},
                new byte[]{4, 5, 6},
                new byte[]{7, 8, 9}});
        System.out.println(cs);
        byte[][] bytes = StringKit.decodeBytes2D(cs);
        for (int x = 0; x < bytes.length; x++) {
            for (int y = 0; y < bytes[x].length; y++) {
                System.out.print(bytes[x][y]);
            }
            System.out.println();
        }
    }
}
