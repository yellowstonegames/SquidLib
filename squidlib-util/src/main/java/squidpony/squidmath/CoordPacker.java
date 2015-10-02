package squidpony.squidmath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Tommy Ettinger on 10/1/2015.
 */
public class CoordPacker {
    public static final int DEPTH = 8;
    private static final int BITS = DEPTH << 1;

    private static short[] hilbertX = new short[0x10000], hilbertY = new short[0x10000];
    static {
        ClassLoader cl = CoordPacker.class.getClassLoader();
        InputStream xStream = cl.getResourceAsStream("hilbert/x.bin"),
                    yStream = cl.getResourceAsStream("hilbert/y.bin");
        byte[] xBytes = new byte[0x20000], yBytes = new byte[0x20000];
        try {
            xStream.read(xBytes);
            ByteBuffer.wrap(xBytes).asShortBuffer().get(hilbertX);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            yStream.read(yBytes);
            ByteBuffer.wrap(yBytes).asShortBuffer().get(hilbertY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static short[] pack(double[][] map)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, current;
        int skip = 0;
        for(int i = 0; i < 0x10000; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) continue;
            current = map[hilbertX[i]][hilbertY[i]] > 0.0;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
            }
        }
        if(!on)
            packing.add((short)skip);
        return packing.shrink();
    }

    public static short[] pack(boolean[][] map)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, current;
        int skip = 0;
        for(int i = 0; i < 0x10000; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) continue;
            current = map[hilbertX[i]][hilbertY[i]];
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
            }
        }
        if(!on)
            packing.add((short)skip);
        return packing.shrink();
    }

    public static boolean[][] unpack(short[] packed, int width, int height)
    {
        if(packed == null || packed.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.unpack() must be given a non-empty array");
        boolean[][] unpacked = new boolean[width][height];
        boolean on = false;
        int idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int toSkip = idx +(packed[p] & 0xffff); idx < toSkip; idx++) {
                    unpacked[hilbertX[idx]][hilbertY[idx]] = true;
                }
            } else {
                idx += packed[p] & 0xffff;
            }
        }
        return unpacked;
    }

    /**
     * Encode a number n as a Gray code; Gray codes have a relation to the Hilbert curve and may be useful.
     * Source: http://xn--2-umb.com/15/hilbert , http://aggregate.org/MAGIC/#Gray%20Code%20Conversion
     * @param n
     * @return
     */
    public static int grayEncode(int n){
        return n ^ (n >> 1);
    }

    /**
     * Decode a number from a Gray code n; Gray codes have a relation to the Hilbert curve and may be useful.
     * Source: http://xn--2-umb.com/15/hilbert , http://aggregate.org/MAGIC/#Gray%20Code%20Conversion
     * @param n
     * @return
     */
    public static int grayDecode(int n) {
        int p = n;
        while ((n >>= 1) != 0)
            p ^= n;
        return p;
    }

    /**
     * Not currently used, may be used in the future.
     * Source: https://www.cs.dal.ca/research/techreports/cs-2006-07 ; algorithm provided in pseudocode
     * @param n
     * @param mask
     * @param i
     * @return
     */
    public static int grayCodeRank(int n, int mask, int i)
    {
        int r = 0;
        for (int k = n - 1; k >= 0; k--)
        {
            if(((mask >> k) & 1) == 1)
                r = (r << 1) | ((i >> k) & 1);
        }
        return  r;
    }

    /**
     *
     * Source: https://www.cs.dal.ca/research/techreports/cs-2006-07 ; algorithm provided in pseudocode
     * @param n
     * @param mask
     * @param altMask
     * @param rank
     * @return
     */
    public static int grayCodeRankInverse(int n, int mask, int altMask, int rank)
    {
        int i = 0, g = 0, j = Integer.bitCount(mask) - 1;
        for(int k = n - 1; k >= 0; k--)
        {
            if(((mask >> k) & 1) == 1)
            {
                i ^= (-((rank >> j) & 1) ^ i) & (1 << k);
                g ^= (-((((i >> k) & 1) + ((i >> k) & 1)) % 2) ^ g) & (1 << k);
                --j;
            }
            else
            {
                g ^= (-((altMask >> k) & 1) ^ g) & (1 << k);
                i ^= (-((((g >> k) & 1) + ((i >> (k+1)) & 1)) % 2) ^ i) & (1 << k);
            }
        }
        return  i;
    }

    /**
     * Takes an x, y position and returns the length to travel along the 256x256 Hilbert curve to reach that position.
     * This assumes x and y are between 0 and 255, inclusive.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x
     * @param y
     * @return
     */
    public static int posToHilbert( final int x, final int y )
    {
        int hilbert = 0;
        int remap = 0xb4;
        int block = DEPTH;
        while( block > 0 )
        {
            --block;
            int mcode = ( ( x >> block ) & 1 ) | ( ( ( y >> ( block ) ) & 1 ) << 1);
            int hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
            remap ^= ( 0x82000028 >> ( hcode << 3 ) );
            hilbert = ( ( hilbert << 2 ) + hcode );
        }
        return hilbert;
    }
    /**
     * Takes a position as a Morton code, with interleaved x and y bits and x in the least significant bit, and returns
     * the length to travel along the 256x256 Hilbert curve to reach that position.
     * This uses 16 bits of the Morton code and requires that the code is non-negative.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton
     * @return
     */
    public static int mortonToHilbert( final int morton )
    {
        int hilbert = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int mcode = ( ( morton >> block ) & 3 );
            int hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
            remap ^= ( 0x82000028 >> ( hcode << 3 ) );
            hilbert = ( ( hilbert << 2 ) + hcode );
        }
        return hilbert;
    }

    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Morton code representing the position
     * in 2D space that corresponds to that point on the Hilbert curve; the Morton code will have interleaved x and y
     * bits and x in the least significant bit. This variant uses a lookup table for the 256x256 Hilbert curve, which
     * should make it faster than calculating the position repeatedly.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */

    public static int hilbertToMorton( final int hilbert )
    {
        return mortonEncode(hilbertX[hilbert], hilbertY[hilbert]);
    }

    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This variant uses a lookup table for the
     * 256x256 Hilbert curve, which should make it faster than calculating the position repeatedly.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    public static Coord hilbertToCoord( final int hilbert )
    {
        return Coord.get(hilbertX[hilbert], hilbertY[hilbert]);
    }


    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Morton code representing the position
     * in 2D space that corresponds to that point on the Hilbert curve; the Morton code will have interleaved x and y
     * bits and x in the least significant bit. This variant does not use a lookup table, and is likely slower.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    public static int hilbertToMortonNoLUT( final int hilbert )
    {
        int morton = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int hcode = ( ( hilbert >> block ) & 3 );
            int mcode = ( ( remap >> ( hcode << 1 ) ) & 3 );
            remap ^= ( 0x330000cc >> ( hcode << 3 ) );
            morton = ( ( morton << 2 ) + mcode );
        }
        return morton;
    }
    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This variant does not use a lookup table,
     * and is likely slower.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    public static Coord hilbertToCoordNoLUT( final int hilbert )
    {
        int x = 0, y = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int hcode = ( ( hilbert >> block ) & 3 );
            int mcode = ( ( remap >> ( hcode << 1 ) ) & 3 );
            remap ^= ( 0x330000cc >> ( hcode << 3 ) );
            x = (x << 1) + (mcode & 1);
            y = (y << 1) + ((mcode & 2) >> 1);
        }
        return Coord.get(x, y);
    }

    /**
     * Takes a position as a Coord called pt and returns the length to travel along the 256x256 Hilbert curve to reach
     * that position.
     * This assumes pt.x and pt.y are between 0 and 255, inclusive.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param pt
     * @return
     */
    public static int coordToHilbert(final Coord pt)
    {
        return posToHilbert(pt.x, pt.y);
    }
    /**
     * Takes two 8-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 8 bits.
     * This returns a 32-bit Morton code but only uses 16 bits, and will not encode information in the sign bit.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param index1
     * @param index2
     * @return
     */
    public static int mortonEncode(int index1, int index2)
    { // pack 2 8-bit (unsigned) indices into a 32-bit (signed...) Morton code
        index1 &= 0x000000ff;
        index2 &= 0x000000ff;
        index1 |= ( index1 << 4 );
        index2 |= ( index2 << 4 );
        index1 &= 0x00000f0f;
        index2 &= 0x00000f0f;
        index1 |= ( index1 << 2 );
        index2 |= ( index2 << 2 );
        index1 &= 0x00003333;
        index2 &= 0x00003333;
        index1 |= ( index1 << 1 );
        index2 |= ( index2 << 1 );
        index1 &= 0x00005555;
        index2 &= 0x00005555;
        return index1 | ( index2 << 1 );
    }
    /**
     * Takes two 16-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 16 bits.
     * This returns a 32-bit Morton code and may encode information in the sign bit.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param index1
     * @param index2
     * @return
     */
    public static int mortonEncode16(int index1, int index2)
    { // pack 2 16-bit indices into a 32-bit Morton code
        index1 &= 0x0000ffff;
        index2 &= 0x0000ffff;
        index1 |= ( index1 << 8 );
        index2 |= ( index2 << 8 );
        index1 &= 0x00ff00ff;
        index2 &= 0x00ff00ff;
        index1 |= ( index1 << 4 );
        index2 |= ( index2 << 4 );
        index1 &= 0x0f0f0f0f;
        index2 &= 0x0f0f0f0f;
        index1 |= ( index1 << 2 );
        index2 |= ( index2 << 2 );
        index1 &= 0x33333333;
        index2 &= 0x33333333;
        index1 |= ( index1 << 1 );
        index2 |= ( index2 << 1 );
        index1 &= 0x55555555;
        index2 &= 0x55555555;
        return index1 | ( index2 << 1 );
    }

    /**
     * Takes a Morton code, with interleaved x and y bits and x in the least significant bit, and returns the Coord
     * representing the same x, y position.
     * This uses 16 bits of the Morton code and requires that the code is non-negative.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton
     * @return
     */
    public static Coord mortonDecode( final int morton )
    { // unpack 2 8-bit (unsigned) indices from a 32-bit (signed...) Morton code
        int value1 = morton;
        int value2 = ( value1 >> 1 );
        value1 &= 0x5555;
        value2 &= 0x5555;
        value1 |= ( value1 >> 1 );
        value2 |= ( value2 >> 1 );
        value1 &= 0x3333;
        value2 &= 0x3333;
        value1 |= ( value1 >> 2 );
        value2 |= ( value2 >> 2 );
        value1 &= 0x0f0f;
        value2 &= 0x0f0f;
        value1 |= ( value1 >> 4 );
        value2 |= ( value2 >> 4 );
        value1 &= 0x00ff;
        value2 &= 0x00ff;
        return Coord.get(value1, value2);
    }
    /**
     * Takes a Morton code, with interleaved x and y bits and x in the least significant bit, and returns the Coord
     * representing the same x, y position. With this method, x and y can have up to 16 bits, but Coords returned by
     * this method will not be cached if they have a x or y component greater than 255.
     * This uses 32 bits of the Morton code and will treat the sign bit as the most significant bit of y, unsigned.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton
     * @return
     */
    public static Coord mortonDecode16( final int morton )
    { // unpack 2 16-bit indices from a 32-bit Morton code
        int value1 = morton;
        int value2 = ( value1 >>> 1 );
        value1 &= 0x55555555;
        value2 &= 0x55555555;
        value1 |= ( value1 >>> 1 );
        value2 |= ( value2 >>> 1 );
        value1 &= 0x33333333;
        value2 &= 0x33333333;
        value1 |= ( value1 >>> 2 );
        value2 |= ( value2 >>> 2 );
        value1 &= 0x0f0f0f0f;
        value2 &= 0x0f0f0f0f;
        value1 |= ( value1 >>> 4 );
        value2 |= ( value2 >>> 4 );
        value1 &= 0x00ff00ff;
        value2 &= 0x00ff00ff;
        value1 |= ( value1 >>> 8 );
        value2 |= ( value2 >>> 8 );
        value1 &= 0x0000ffff;
        value2 &= 0x0000ffff;
        return Coord.get(value1, value2);
    }
}
