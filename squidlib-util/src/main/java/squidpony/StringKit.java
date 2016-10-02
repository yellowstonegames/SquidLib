package squidpony;

import squidpony.squidmath.CrossHash;

/**
 * Various utility functions for dealing with Strings, CharSequences, and char[]s; mostly converting numbers.
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class StringKit {

    public static String join(CharSequence delimiter, CharSequence... elements)
    {
        if(elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String joinArrays(CharSequence delimiter, char[]... elements)
    {
        if(elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }


    public static final String mask64 = "0000000000000000000000000000000000000000000000000000000000000000",
            mask32 = "00000000000000000000000000000000",
            mask16 = "0000000000000000",
            mask8 = "00000000";

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
    public static String bin(long number) {
        String h = Long.toBinaryString(number);
        return mask64.substring(0, 64 - h.length()) + h;
    }

    public static String bin(int number) {
        String h = Integer.toBinaryString(number);
        return mask32.substring(0, 32 - h.length()) + h;
    }

    public static String bin(short number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(byte number) {
        String h = Integer.toHexString(number & 0xff);
        return mask8.substring(0, 8 - h.length()) + h;
    }
    public static char[] apEncode(long number, int offset, char[] buf) {
        if(buf != null && buf.length >= 16 - offset) {
            buf[offset] = (char) ((number >>> 60) + 65);
            buf[offset+1] = (char) ((0xf & (number >>> 56)) + 65);
            buf[offset+2] = (char) ((0xf & (number >>> 52)) + 65);
            buf[offset+3] = (char) ((0xf & (number >>> 48)) + 65);
            buf[offset+4] = (char) ((0xf & (number >>> 44)) + 65);
            buf[offset+5] = (char) ((0xf & (number >>> 40)) + 65);
            buf[offset+6] = (char) ((0xf & (number >>> 36)) + 65);
            buf[offset+7] = (char) ((0xf & (number >>> 32)) + 65);
            buf[offset+8] = (char) ((0xf & (number >>> 28)) + 65);
            buf[offset+9] = (char) ((0xf & (number >>> 24)) + 65);
            buf[offset+10] = (char) ((0xf & (number >>> 20)) + 65);
            buf[offset+11] = (char) ((0xf & (number >>> 16)) + 65);
            buf[offset+12] = (char) ((0xf & (number >>> 12)) + 65);
            buf[offset+13] = (char) ((0xf & (number >>> 8)) + 65);
            buf[offset+14] = (char) ((0xf & (number >>> 4)) + 65);
            buf[offset+15] = (char) ((0xf & number) + 65);
        }
        return buf;
    }

    public static char[] apEncode(double number, int offset, char[] buf) {
        return apEncode(Double.doubleToLongBits(number), offset, buf);
    }
        public static char[] apEncode(int number, int offset, char[] buf) {
        if(buf != null && buf.length >= 8 - offset) {
            buf[offset] = (char)((number >>> 28) + 65);
            buf[offset+1] = (char)((0xf & (number >>> 24)) + 65);
            buf[offset+2] = (char)((0xf & (number >>> 20)) + 65);
            buf[offset+3] = (char)((0xf & (number >>> 16)) + 65);
            buf[offset+4] = (char)((0xf & (number >>> 12)) + 65);
            buf[offset+5] = (char)((0xf & (number >>> 8)) + 65);
            buf[offset+6] = (char)((0xf & (number >>> 4)) + 65);
            buf[offset+7] = (char)((0xf & number) + 65);
        }
        return buf;
    }
    public static char[] apEncode(float number, int offset, char[] buf) {
        return apEncode(Float.floatToIntBits(number), offset, buf);
    }
    public static char[] apEncode(short number, int offset, char[] buf) {
        if(buf != null && buf.length >= 4 - offset) {
            buf[offset] = (char)((number >>> 12) + 65);
            buf[offset+1] = (char)((0xf & (number >>> 8)) + 65);
            buf[offset+2] = (char)((0xf & (number >>> 4)) + 65);
            buf[offset+3] = (char)((0xf & number) + 65);
        }
        return buf;
    }

    public static char[] apEncode(char number, int offset, char[] buf) {
        if(buf != null && buf.length >= 4 - offset) {
            buf[offset] = (char)((number >>> 12) + 65);
            buf[offset+1] = (char)((0xf & (number >>> 8)) + 65);
            buf[offset+2] = (char)((0xf & (number >>> 4)) + 65);
            buf[offset+3] = (char)((0xf & number) + 65);
        }
        return buf;
    }

    public static char[] apEncode(byte number, int offset, char[] buf) {
        if(buf != null && buf.length >= 2 - offset) {
            buf[offset] = (char)((number >>> 4) + 65);
            buf[offset+1] = (char)((0xf & number) + 65);
        }
        return buf;

    }

    public static long apDecodeLong(char[] data, int offset)
    {
        return (data == null || data.length < 16 + offset) ? 0 :
                ((0xf & data[offset] - 65L) << 60)
                        | ((0xf & data[offset + 1] - 65L) << 56)
                        | ((0xf & data[offset + 2] - 65L) << 52)
                        | ((0xf & data[offset + 3] - 65L) << 48)
                        | ((0xf & data[offset + 4] - 65L) << 44)
                        | ((0xf & data[offset + 5] - 65L) << 40)
                        | ((0xf & data[offset + 6] - 65L) << 36)
                        | ((0xf & data[offset + 7] - 65L) << 32)
                        | ((0xf & data[offset + 8] - 65L) << 28)
                        | ((0xf & data[offset + 9] - 65L) << 24)
                        | ((0xf & data[offset + 10] - 65L) << 20)
                        | ((0xf & data[offset + 11] - 65L) << 16)
                        | ((0xf & data[offset + 12] - 65L) << 12)
                        | ((0xf & data[offset + 13] - 65L) << 8)
                        | ((0xf & data[offset + 14] - 65L) << 4)
                        | ((0xf & data[offset + 15] - 65L));
    }
    public static double apDecodeDouble(char[] data, int offset)
    {
        return (data == null || data.length < 16 + offset) ? 0.0 :
                Double.longBitsToDouble(((0xf & data[offset] - 65L) << 60)
                        | ((0xf & data[offset + 1] - 65L) << 56)
                        | ((0xf & data[offset + 2] - 65L) << 52)
                        | ((0xf & data[offset + 3] - 65L) << 48)
                        | ((0xf & data[offset + 4] - 65L) << 44)
                        | ((0xf & data[offset + 5] - 65L) << 40)
                        | ((0xf & data[offset + 6] - 65L) << 36)
                        | ((0xf & data[offset + 7] - 65L) << 32)
                        | ((0xf & data[offset + 8] - 65L) << 28)
                        | ((0xf & data[offset + 9] - 65L) << 24)
                        | ((0xf & data[offset + 10] - 65L) << 20)
                        | ((0xf & data[offset + 11] - 65L) << 16)
                        | ((0xf & data[offset + 12] - 65L) << 12)
                        | ((0xf & data[offset + 13] - 65L) << 8)
                        | ((0xf & data[offset + 14] - 65L) << 4)
                        | ((0xf & data[offset + 15] - 65L)));
    }
    public static int apDecodeInt(char[] data, int offset)
    {
        return (data == null || data.length < 8 + offset) ? 0 :
                ((0xf & data[offset] - 65) << 28)
                        | ((0xf & data[offset + 1] - 65) << 24)
                        | ((0xf & data[offset + 2] - 65) << 20)
                        | ((0xf & data[offset + 3] - 65) << 16)
                        | ((0xf & data[offset + 4] - 65) << 12)
                        | ((0xf & data[offset + 5] - 65) << 8)
                        | ((0xf & data[offset + 6] - 65) << 4)
                        | ((0xf & data[offset + 7] - 65));
    }
    public static float apDecodeFloat(char[] data, int offset)
    {
        return (data == null || data.length < 8 + offset) ? 0f :
                Float.intBitsToFloat(((0xf & data[offset] - 65) << 28)
                        | ((0xf & data[offset + 1] - 65) << 24)
                        | ((0xf & data[offset + 2] - 65) << 20)
                        | ((0xf & data[offset + 3] - 65) << 16)
                        | ((0xf & data[offset + 4] - 65) << 12)
                        | ((0xf & data[offset + 5] - 65) << 8)
                        | ((0xf & data[offset + 6] - 65) << 4)
                        | ((0xf & data[offset + 7] - 65)));
    }
    public static short apDecodeShort(char[] data, int offset)
    {
        return (short) ((data == null || data.length < 4 + offset) ? 0 :
                ((0xf & data[offset] - 65) << 12)
                        | ((0xf & data[offset + 1] - 65) << 8)
                        | ((0xf & data[offset + 2] - 65) << 4)
                        | ((0xf & data[offset + 3] - 65)));
    }
    public static byte apDecodeByte(char[] data, int offset)
    {
        return (byte) ((data == null || data.length < 2 + offset) ? 0 :
                ((0xf & data[offset] - 65) << 4)
                        | ((0xf & data[offset + 1] - 65)));
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
}
