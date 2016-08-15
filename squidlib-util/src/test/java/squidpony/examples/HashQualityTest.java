package squidpony.examples;

import squidpony.squidmath.CrossHash;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 8/15/2016.
 */
public class HashQualityTest {

    public static void main(String[] args)
    {
        byte[][] bytes = {
                null,
                {},
                {0},
                {0, 0},
                {0, 0, 0},
                {1},
                {1, 1},
                {1, 1, 1},
                {-1},
                {-1, -1},
                {-1, -1, -1},
                {31},
                {31, 31},
                {31, 31, 31},
                {-31},
                {-31, -31},
                {-31, -31, -31},
        };
        short[][] shorts = {
                null,
                {},
                {0},
                {0, 0},
                {0, 0, 0},
                {1},
                {1, 1},
                {1, 1, 1},
                {-1},
                {-1, -1},
                {-1, -1, -1},
                {31},
                {31, 31},
                {31, 31, 31},
                {-31},
                {-31, -31},
                {-31, -31, -31},
        };
        int[][] ints = {
                null,
                {},
                {0},
                {0, 0},
                {0, 0, 0},
                {1},
                {1, 1},
                {1, 1, 1},
                {-1},
                {-1, -1},
                {-1, -1, -1},
                {31},
                {31, 31},
                {31, 31, 31},
                {-31},
                {-31, -31},
                {-31, -31, -31},
        };
        long[][] longs = {
                null,
                {},
                {0},
                {0, 0},
                {0, 0, 0},
                {1},
                {1, 1},
                {1, 1, 1},
                {-1},
                {-1, -1},
                {-1, -1, -1},
                {31},
                {31, 31},
                {31, 31, 31},
                {-31},
                {-31, -31},
                {-31, -31, -31},
        };
        int len = bytes.length;
        CrossHash.Sip sip = new CrossHash.Sip(0x9E3779B97F4A7C15L, 0xBF58476D1CE4E5B9L);
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("JDK bytes: " + Arrays.hashCode(bytes[i]));
            System.out.println("JDK shorts: " + Arrays.hashCode(shorts[i]));
            System.out.println("JDK ints: " + Arrays.hashCode(ints[i]));
            System.out.println("JDK longs: " + Arrays.hashCode(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("CrossHash bytes: " + CrossHash.hash(bytes[i]));
            System.out.println("CrossHash shorts: " + CrossHash.hash(shorts[i]));
            System.out.println("CrossHash ints: " + CrossHash.hash(ints[i]));
            System.out.println("CrossHash longs: " + CrossHash.hash(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Sip bytes: " + sip.hash(bytes[i]));
            System.out.println("Sip shorts: " + sip.hash(shorts[i]));
            System.out.println("Sip ints: " + sip.hash(ints[i]));
            System.out.println("Sip longs: " + sip.hash(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Lightning bytes: " + CrossHash.Lightning.hash(bytes[i]));
            System.out.println("Lightning shorts: " + CrossHash.Lightning.hash(shorts[i]));
            System.out.println("Lightning ints: " + CrossHash.Lightning.hash(ints[i]));
            System.out.println("Lightning longs: " + CrossHash.Lightning.hash(longs[i]));
        }


    }
}
