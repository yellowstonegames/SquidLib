package squidpony;

import squidpony.squidmath.CrossHash;

/**
 * Various utility functions for making toString implementations easier.
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class StringKit {
    public static final String mask16 = "0000000000000000", mask8 = "00000000";
    public static String hex(long number)
    {
        String h = Long.toHexString(number);
        return mask16.substring(0, 16 - h.length()) + h;
    }
    public static String hex(int number)
    {
        String h = Integer.toHexString(number);
        return mask8.substring(0, 8 - h.length()) + h;
    }
    public static String hex(short number)
    {
        String h = Integer.toHexString(number);
        return mask8.substring(4, 8 - h.length()) + h;
    }
    public static String hex(byte number)
    {
        String h = Integer.toHexString(number);
        return mask8.substring(6, 8 - h.length()) + h;
    }
    public static String hexHash(boolean... array)
    {
        return hex(CrossHash.hash(array));
    }
    public static String hexHash(byte... array)
    {
        return hex(CrossHash.hash(array));
    }
    public static String hexHash(short... array)
    {
        return hex(CrossHash.hash(array));
    }
    public static String hexHash(char... array)
    {
        return hex(CrossHash.hash(array));
    }
    public static String hexHash(int... array)
    {
        return hex(CrossHash.hash(array));
    }
    public static String hexHash(long... array)
    {
        return hex(CrossHash.hash(array));
    }

}
