package squidpony;

import regexodus.MatchIterator;
import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.squidmath.CrossHash;

/**
 * Various utility functions for making toString implementations easier.
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class StringKit {
    public static final String mask16 = "0000000000000000", mask8 = "00000000";

    public static String hex(long number) {
        String h = Long.toHexString(number);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String hex(int number) {
        String h = Integer.toHexString(number);
        return mask8.substring(0, 8 - h.length()) + h;
    }

    public static String hex(short number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask8.substring(4, 8 - h.length()) + h;
    }

    public static String hex(byte number) {
        String h = Integer.toHexString(number & 0xff);
        return mask8.substring(6, 8 - h.length()) + h;
    }

    public static String hexHash(boolean... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(byte... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(short... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(char... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(int... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(long... array) {
        return hex(CrossHash.hash64(array));
    }

    public static CharSequence encode(byte[] data)
    {
        if(data == null)
            return "";
        int len = data.length;
        char[] chars = new char[len];
        for (int i = 0, c = 0; c < len; i++, c++) {
            chars[c] = (char) (data[i] | 0x2800);
        }
        return new String(chars);
    }
    public static CharSequence encode(short[] data)
    {
        if(data == null)
            return "";
        int len = data.length * 2;
        char[] chars = new char[len];
        short item;
        for (int i = 0, c = 0; c < len; i++, c += 2) {
            item = data[i];
            chars[c] = (char) ((item & 0xff) | 0x2800);
            chars[c+1] = (char)((item >>> 8) | 0x2800);
        }
        return new String(chars);
    }
    public static CharSequence encode(int[] data)
    {
        if(data == null)
            return "";
        int len = data.length * 4, item;
        char[] chars = new char[len];
        for (int i = 0, c = 0; c < len; i++, c += 4) {
            item = data[i];
            chars[c] = (char) ((item & 0xff) | 0x2800);
            chars[c+1] = (char)(((item >>> 8)  & 0xff) | 0x2800);
            chars[c+2] = (char)(((item >>> 16) & 0xff) | 0x2800);
            chars[c+3] = (char)(((item >>> 24) & 0xff) | 0x2800);
        }
        return new String(chars);
    }
    public static CharSequence encode(float[] data)
    {
        if(data == null)
            return "";
        int len = data.length * 4, item;
        char[] chars = new char[len];
        for (int i = 0, c = 0; c < len; i++, c += 4) {
            item = Float.floatToIntBits(data[i]);
            chars[c] = (char) ((item & 0xff) | 0x2800);
            chars[c+1] = (char)(((item >>> 8)  & 0xff) | 0x2800);
            chars[c+2] = (char)(((item >>> 16) & 0xff) | 0x2800);
            chars[c+3] = (char)(((item >>> 24) & 0xff) | 0x2800);
        }
        return new String(chars);
    }
    public static CharSequence encode(long[] data)
    {
        if(data == null)
            return "";
        int len = data.length * 8;
        char[] chars = new char[len];
        long item;
        for (int i = 0, c = 0; c < len; i++, c += 8) {
            item = data[i];
            chars[c] = (char) ((item & 0xff) | 0x2800);
            chars[c+1] = (char)(((item >>> 8)  & 0xff) | 0x2800);
            chars[c+2] = (char)(((item >>> 16) & 0xff) | 0x2800);
            chars[c+3] = (char)(((item >>> 24) & 0xff) | 0x2800);
            chars[c+4] = (char)(((item >>> 32) & 0xff) | 0x2800);
            chars[c+5] = (char)(((item >>> 40) & 0xff) | 0x2800);
            chars[c+6] = (char)(((item >>> 48) & 0xff) | 0x2800);
            chars[c+7] = (char)(((item >>> 56) & 0xff) | 0x2800);
        }
        return new String(chars);
    }

    public static byte[] decodeBytes(CharSequence data)
    {
        if(data == null)
            return new byte[0];
        int len = data.length();
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte)(data.charAt(i) & 0xff);
        }
        return bytes;
    }

    public static short[] decodeShorts(CharSequence data)
    {
        if(data == null)
            return new short[0];
        int len = data.length() / 2;
        short[] shorts = new short[len];
        for (int i = 0; i < len; i++) {
            shorts[i] = (short) ((data.charAt(i * 2) & 0xff)
                    | ((data.charAt(i * 2 + 1) & 0xff) << 8));
        }
        return shorts;
    }
    public static int[] decodeInts(CharSequence data)
    {
        if(data == null)
            return new int[0];
        int len = data.length() / 4;
        int[] ints = new int[len];
        for (int i = 0; i < len; i++) {
            ints[i] = ((data.charAt(i * 4) & 0xff)
                    | ((data.charAt(i * 4 + 1) & 0xff) << 8)
                    | ((data.charAt(i * 4 + 2) & 0xff) << 16)
                    | ((data.charAt(i * 4 + 3) & 0xff) << 24));
        }
        return ints;
    }

    public static float[] decodeFloats(CharSequence data)
    {
        if(data == null)
            return new float[0];
        int len = data.length() / 4, tmp;
        float[] floats = new float[len];
        for (int i = 0; i < len; i++) {
            tmp = ((data.charAt(i * 4) & 0xff)
                    | ((data.charAt(i * 4 + 1) & 0xff) << 8)
                    | ((data.charAt(i * 4 + 2) & 0xff) << 16)
                    | ((data.charAt(i * 4 + 3) & 0xff) << 24));
            floats[i] = Float.intBitsToFloat(tmp);
        }
        return floats;
    }
    public static long[] decodeLongs(CharSequence data)
    {
        if(data == null)
            return new long[0];
        int len = data.length() / 8;
        long[] longs = new long[len];
        for (int i = 0; i < len; i++) {
            longs[i] = ((data.charAt(i * 4) & 0xffL)
                    | ((data.charAt(i * 4 + 1) & 0xffL) << 8)
                    | ((data.charAt(i * 4 + 2) & 0xffL) << 16)
                    | ((data.charAt(i * 4 + 3) & 0xffL) << 24)
                    | ((data.charAt(i * 4 + 2) & 0xffL) << 32)
                    | ((data.charAt(i * 4 + 3) & 0xffL) << 40)
                    | ((data.charAt(i * 4 + 2) & 0xffL) << 48)
                    | ((data.charAt(i * 4 + 3) & 0xffL) << 56));
        }
        return longs;
    }

    public static CharSequence encode(byte[][] data)
    {
        if(data == null || data.length == 0)
            return ";";
        StringBuilder sb = new StringBuilder(128);
        sb.append(encode(data[0]));
        for (int i = 1; i < data.length; i++) {
            sb.append('\t');
            sb.append(encode(data[i]));
        }
        sb.append(';');
        return sb;
    }

    public static CharSequence encode(short[][] data)
    {
        if(data == null || data.length == 0)
            return ";";
        StringBuilder sb = new StringBuilder(256);
        sb.append(encode(data[0]));
        for (int i = 1; i < data.length; i++) {
            sb.append('\t');
            sb.append(encode(data[i]));
        }
        sb.append(';');
        return sb;
    }

    public static CharSequence encode(int[][] data)
    {
        if(data == null || data.length == 0)
            return ";";
        StringBuilder sb = new StringBuilder(256);
        sb.append(encode(data[0]));
        for (int i = 1; i < data.length; i++) {
            sb.append('\t');
            sb.append(encode(data[i]));
        }
        sb.append(';');
        return sb;
    }

    public static CharSequence encode(float[][] data)
    {
        if(data == null || data.length == 0)
            return ";";
        StringBuilder sb = new StringBuilder(256);
        sb.append(encode(data[0]));
        for (int i = 1; i < data.length; i++) {
            sb.append('\t');
            sb.append(encode(data[i]));
        }
        sb.append(';');
        return sb;
    }

    public static CharSequence encode(long[][] data)
    {
        if(data == null || data.length == 0)
            return ";";
        StringBuilder sb = new StringBuilder(256);
        sb.append(encode(data[0]));
        for (int i = 1; i < data.length; i++) {
            sb.append('\t');
            sb.append(encode(data[i]));
        }
        sb.append(';');
        return sb;
    }

    public static CharSequence encode(byte[][][] data)
    {
        if(data == null || data.length == 0)
            return "#";
        StringBuilder sb = new StringBuilder(512);
        for (int i = 0; i < data.length; i++) {
            sb.append(encode(data[i]));
        }
        sb.append('#');
        return sb;
    }

    public static CharSequence encode(short[][][] data)
    {
        if(data == null || data.length == 0)
            return "#";
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < data.length; i++) {
            sb.append(encode(data[i]));
        }
        sb.append('#');
        return sb;
    }

    public static CharSequence encode(int[][][] data)
    {
        if(data == null || data.length == 0)
            return "#";
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < data.length; i++) {
            sb.append(encode(data[i]));
        }
        sb.append('#');
        return sb;
    }

    public static CharSequence encode(float[][][] data)
    {
        if(data == null || data.length == 0)
            return "#";
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < data.length; i++) {
            sb.append(encode(data[i]));
        }
        sb.append('#');
        return sb;
    }

    public static CharSequence encode(long[][][] data)
    {
        if(data == null || data.length == 0)
            return "#";
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < data.length; i++) {
            sb.append(encode(data[i]));
        }
        sb.append('#');
        return sb;
    }
    private static final Pattern decoder2D = Pattern.compile("(?:([^\t;]*)[\t;])+?", 0),
            decoder3D = Pattern.compile("([^;]*;)#?", 0);
    public static byte[][] decodeBytes2D(CharSequence data)
    {
        if(data == null)
            return new byte[0][0];
        Matcher matcher = decoder2D.matcher(data);
        MatchIterator mi = matcher.findAll();
        byte[][] values = new byte[mi.count()][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeBytes(mi.nextMatch().group(1));
        }
        return values;
    }
    public static short[][] decodeShorts2D(CharSequence data)
    {
        if(data == null)
            return new short[0][0];
        Matcher matcher = decoder2D.matcher(data);
        MatchIterator mi = matcher.findAll();
        short[][] values = new short[mi.count()][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeShorts(mi.nextMatch().group(1));
        }
        return values;
    }
    public static int[][] decodeInts2D(CharSequence data)
    {
        if(data == null)
            return new int[0][0];
        Matcher matcher = decoder2D.matcher(data);
        MatchIterator mi = matcher.findAll();
        int[][] values = new int[mi.count()][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeInts(mi.nextMatch().group(1));
        }
        return values;
    }

    public static float[][] decodeFloats2D(CharSequence data)
    {
        if(data == null)
            return new float[0][0];
        Matcher matcher = decoder2D.matcher(data);
        MatchIterator mi = matcher.findAll();
        float[][] values = new float[mi.count()][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeFloats(mi.nextMatch().group(1));
        }
        return values;
    }
    public static long[][] decodeLongs2D(CharSequence data)
    {
        if(data == null)
            return new long[0][0];
        Matcher matcher = decoder2D.matcher(data);
        MatchIterator mi = matcher.findAll();
        long[][] values = new long[mi.count()][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeLongs(mi.nextMatch().group(1));
        }
        return values;
    }

    public static byte[][][] decodeBytes3D(CharSequence data)
    {
        if(data == null)
            return new byte[0][0][0];
        Matcher matcher = decoder3D.matcher(data);
        MatchIterator mi = matcher.findAll();
        byte[][][] values = new byte[mi.count()][][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeBytes2D(mi.nextMatch().group(1));
        }
        return values;
    }
    public static short[][][] decodeShorts3D(CharSequence data)
    {
        if(data == null)
            return new short[0][0][0];
        Matcher matcher = decoder3D.matcher(data);
        MatchIterator mi = matcher.findAll();
        short[][][] values = new short[mi.count()][][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeShorts2D(mi.nextMatch().group(1));
        }
        return values;
    }
    public static int[][][] decodeInts3D(CharSequence data)
    {
        if(data == null)
            return new int[0][0][0];
        Matcher matcher = decoder3D.matcher(data);
        MatchIterator mi = matcher.findAll();
        int[][][] values = new int[mi.count()][][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeInts2D(mi.nextMatch().group(1));
        }
        return values;
    }
    public static float[][][] decodeFloats3D(CharSequence data)
    {
        if(data == null)
            return new float[0][0][0];
        Matcher matcher = decoder3D.matcher(data);
        MatchIterator mi = matcher.findAll();
        float[][][] values = new float[mi.count()][][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeFloats2D(mi.nextMatch().group(1));
        }
        return values;
    }
    public static long[][][] decodeLongs3D(CharSequence data)
    {
        if(data == null)
            return new long[0][0][0];
        Matcher matcher = decoder3D.matcher(data);
        MatchIterator mi = matcher.findAll();
        long[][][] values = new long[mi.count()][][];
        int i = 0;
        matcher.setTarget(data);
        while (mi.hasMore())
        {
            values[i++] = decodeLongs2D(mi.nextMatch().group(1));
        }
        return values;
    }

}
