package squidpony.squidmath;

import java.util.Arrays;

/**
 * Region encoding of 64x64 areas as a number of long arrays; uncompressed (fatty), but fast (greased lightning).
 * Created by Tommy Ettinger on 6/24/2016.
 */
public class GreasedRegion {
    public long[] data;
    public int height;
    public int width;
    private int ySections;
    private long yEndMask;
    public GreasedRegion(boolean[][] bits)
    {
        width = bits.length;
        height = bits[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(bits[x][y]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    public GreasedRegion(char[][] map, char yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        //yEndMask = ~(-1L << (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    public GreasedRegion(boolean[] bits, int height, int width)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = (-1L >>> (64 - (height & 63)));
        data = new long[width * ySections];
        for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
            if(bits[a]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
    }

    public GreasedRegion(GreasedRegion other)
    {
        width = other.width;
        height = other.height;
        ySections = other.ySections;
        yEndMask = other.yEndMask;
        data = new long[width * ySections];
        System.arraycopy(other.data, 0, data, 0, width * ySections);
    }
    public GreasedRegion remake(GreasedRegion other) {
        if (width == other.width && height == other.height) {
            System.arraycopy(other.data, 0, data, 0, width * ySections);
            return this;
        } else {
            width = other.width;
            height = other.height;
            ySections = other.ySections;
            yEndMask = other.yEndMask;
            data = new long[width * ySections];
            System.arraycopy(other.data, 0, data, 0, width * ySections);
            return this;
        }
    }
    public boolean[][] decode()
    {
        boolean[][] bools = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bools[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0;
            }
        }
        return bools;
    }

    public char[][] toChars(char on, char off)
    {
        char[][] chars = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return chars;
    }

    public char[][] toChars()
    {
        return toChars('.', '#');
    }

    public GreasedRegion or(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] |= other.data[x * ySections + y];
            }
            /*
            for (int y = 0; y < height && y < other.height; y++) {
                data[x * ySections + (y >> 6)] &= other.data[x * ySections + (y >> 6)];
            }

             */
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }

        return this;
    }

    public GreasedRegion and(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= other.data[x * ySections + y];
            }
        }
        return this;
    }

    public GreasedRegion andNot(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= ~other.data[x * ySections + y];
            }
        }
        return this;
    }

    public GreasedRegion xor(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] ^= other.data[x * ySections + y];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    public GreasedRegion not()
    {
        for (int a = 0; a < data.length; a++)
        {
            data[a] = ~ data[a];
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    public GreasedRegion translate(int x, int y)
    {
        if(width < 2 || ySections == 0 || (x == 0 && y == 0))
            return this;

        long[] data2 = new long[width * ySections];
        int start = Math.max(0, x), len = Math.min(width, width + x) - start;
        long prev, tmp;
        if(x < 0)
        {
            System.arraycopy(data, Math.max(0, -x) * ySections, data2, 0, len * ySections);
        }
        else if(x > 0)
        {
            System.arraycopy(data, 0, data2, start * ySections, len * ySections);
        }
        else
        {
            System.arraycopy(data, 0, data2, 0, len * ySections);
        }
        if(y < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -y)) << (64 + y);
                    data2[i * ySections + j] >>>= -y;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(y > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = ySections - 1; j >= 0; j--) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> y)) >>> (64 - y);
                    data2[i * ySections + j] <<= y;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data2[a] &= yEndMask;
            }
        }
        data = data2;
        return this;
    }
    public GreasedRegion expand()
    {
        if(width < 2 || ySections == 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= (data[a] << 1) | (data[a] >>> 1) | (data[a+ySections]);
            next[width-ySections+a] |= (data[width-ySections+a] << 1) | (data[width-ySections+a] >>> 1) | (data[width-ySections*2+a]);

            for (int i = ySections; i < (width - 1) * ySections; i+= ySections) {
                next[i+a] |= (data[i+a] << 1) | (data[i+a] >>> 1) | (data[i - ySections+a]) | (data[i + ySections+a]);
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63;
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i + 1] & 1L) << 63;
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion fringe()
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand();
        return andNot(cpy);
    }

    public GreasedRegion retract()
    {
        if(width <= 2 || ySections == 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections]);
                }
            }
            else if(a > 0) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1)
                            & (data[i - ySections])
                            & (data[i + ySections]);
                }
            }
            else if(a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections]);
                }
            }
            else // only the case when ySections == 1
            {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1) & (data[i] >>> 1) & (data[i - ySections]) & (data[i + ySections]);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion surface()
    {
        GreasedRegion cpy = new GreasedRegion(this).retract();
        return xor(cpy);
    }



    public GreasedRegion expand8way()
    {
        if(width < 2 || ySections == 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= (data[a] << 1) | (data[a] >>> 1)
                    | (data[a+ySections]) | (data[a+ySections] << 1) | (data[a+ySections] >>> 1);
            next[width-ySections+a] |= (data[width-ySections+a] << 1) | (data[width-ySections+a] >>> 1)
                    | (data[width-ySections*2+a]) | (data[width-ySections*2+a] << 1) | (data[width-ySections*2+a] >>> 1);

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= (data[i] << 1) | (data[i] >>> 1)
                        | (data[i - ySections]) | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                        | (data[i + ySections]) | (data[i + ySections] << 1) | (data[i + ySections] >>> 1);
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i + 1] & 1L) << 63) |
                            ((data[i - ySections + 1] & 1L) << 63) |
                            ((data[i + ySections+ 1] & 1L) << 63);
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion fringe8way()
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand8way();
        return andNot(cpy);
    }

    public GreasedRegion retract8way()
    {
        if(width <= 2 || ySections == 0)
            return this;

        long[] next = new long[width * ySections];
        System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                            & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                }
            }
            else if(a > 0) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1)
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                            & (data[i - ySections] >>> 1)
                            & (data[i + ySections] >>> 1);
                }
            }
            else if(a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & (data[i - ySections] << 1)
                            & (data[i + ySections] << 1)
                            & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                            & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                }
            }
            else // only the case when ySections == 1
            {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & (data[i] >>> 1)
                            & (data[i - ySections])
                            & (data[i + ySections])
                            & (data[i - ySections] << 1)
                            & (data[i + ySections] << 1)
                            & (data[i - ySections] >>> 1)
                            & (data[i + ySections] >>> 1);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }


    public GreasedRegion surface8way()
    {
        GreasedRegion cpy = new GreasedRegion(this).retract8way();
        return xor(cpy);
    }


    public int count()
    {
        int c = 0;
        for (int i = 0; i < width * ySections; i++) {
            c += Long.bitCount(data[i]);
        }
        return c;
    }
    public boolean test(int x, int y)
    {
        return x >= 0 && y >= 0 && x < width && y < height && ySections > 0 &&
                ((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreasedRegion that = (GreasedRegion) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (ySections != that.ySections) return false;
        if (yEndMask != that.yEndMask) return false;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = CrossHash.hash(data);
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + ySections;
        result = 31 * result + (int) (yEndMask ^ (yEndMask >>> 32));
        return result;
    }
}
