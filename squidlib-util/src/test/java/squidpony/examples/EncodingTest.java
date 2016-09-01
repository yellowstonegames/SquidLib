package squidpony.examples;

import squidpony.StringKit;

/**
 * Created by Tommy Ettinger on 6/1/2016.
 */
public class EncodingTest {
    //@Test
    public void test2D()
    {
        CharSequence cs = StringKit.encode(new byte[][]{
                new byte[]{1,2, 3},
                new byte[]{4, 5, 6},
                new byte[]{7, 8, 9}});
        System.out.println(cs);
        byte[][] bytes = StringKit.decodeBytes2D(cs);
        for (int x = 0; x < bytes.length; x++) {
            for (int y = 0; y < bytes[x].length; y++) {
                System.out.print(bytes[x][y] + " ");
            }
            System.out.println();
        }
    }
    //@Test
    public void test3D()
    {
        CharSequence cs = StringKit.encode(new byte[][][]{
                {
                        new byte[]{1, 2, 3},
                        new byte[]{4, 5, 6},
                        new byte[]{7, 8, 9}
                },
                {
                        new byte[]{11, 12, 13},
                        new byte[]{14, 15, 16},
                        new byte[]{17, 18, 19}
                },
        });
        System.out.println(cs);
        byte[][][] bytes = StringKit.decodeBytes3D(cs);
        for (int x = 0; x < bytes.length; x++) {
            for (int y = 0; y < bytes[x].length; y++) {
                for (int z = 0; z < bytes[x][y].length; z++) {
                    System.out.print(bytes[x][y][z] + " ");
                }
                System.out.println(',');
            }
            System.out.println();
        }
    }
}
