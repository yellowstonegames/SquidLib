/**
 ------------------------------------------------------------------------------
 Rand.java: By Bob Jenkins.  My random number generator, ISAAC.
 rand.init() -- initialize
 rand.val()  -- get a random value
 MODIFIED:
 960327: Creation (addition of randinit, really)
 970719: use context, not global variables, for internal state
 980224: Translate to Java
 ------------------------------------------------------------------------------
 */

package squidpony.squidmath;

/**
 * This is a port of the public domain Isaac64 (cryptographic) random number generator to Java.
 * It is a RandomnessSource here, so it should generally be used to make an RNG, which has more features.
 * IsaacRNG is slower than the non-cryptographic RNGs in SquidLib, but much faster than cryptographic RNGs
 * that need SecureRandom, and it's compatible with GWT and Android to boot!
 * Created by Tommy Ettinger on 8/1/2016.
 */

public class IsaacRNG implements RandomnessSource {
    static final int SIZEL = 8;              /* log of size of results[] and mem[] */
    static final int SIZE = 256;               /* size of results[] and mem[] */
    static final int MASK = 255<<2;            /* for pseudorandom lookup */
    private int count;                           /* count through the results in results[] */
    long results[];                                /* the results given to the user */
    private long mem[];                                   /* the internal state */
    private long a;                                              /* accumulator */
    private long b;                                          /* the last result */
    private long c;              /* counter, guarantees cycle is at least 2^^72 */


    /**
     * Constructs an IsaacRNG with no seed; this will produce one sequence of numbers as if the seed were 0
     * (which it essentially is, though passing 0 to the constructor that takes a long will produce a different
     * sequence) instead of what the other RandomnessSources do (initialize with a low-quality random number
     * from Math.random()).
     */
    public IsaacRNG() {
        mem = new long[SIZE];
        results = new long[SIZE];
        init(false);
    }


    /**
     * Constructs an IsaacRNG with the given seed, which should be a rather large array of long values.
     * You should try to make seed a long[256], but smaller arrays will be tolerated without error.
     * Arrays larger than 256 items will only have the first 256 used.
     * @param seed an array of longs to use as a seed; ideally it should be 256 individual longs
     */
    public IsaacRNG(long seed[]) {
        mem = new long[SIZE];
        results = new long[SIZE];
        if(seed == null)
            init(false);
        else {
            System.arraycopy(seed, 0, results, 0, Math.min(256, seed.length));
            init(true);
        }
    }

    /**
     * Constructs an IsaacRNG with its state filled by the value of seed, run through the LightRNG algorithm.
     * @param seed any long; will have equal influence on all bits of state
     */
    public IsaacRNG(long seed) {
        mem = new long[SIZE];
        results = new long[SIZE];
        long z;
        for (int i = 0; i < 256; i++) {
            z = seed += 0x9E3779B97F4A7C15L;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            results[i] = z ^ (z >>> 31);
        }
        init(true);
    }

    /**
     * Constructs an IsaacRNG with its state filled by repeated hashing of seed.
     * @param seed a String that should be exceptionally long to get the best results.
     */
    public IsaacRNG(String seed) {
        mem = new long[SIZE];
        results = new long[SIZE];
        if(seed == null)
            init(false);
        else {
            char[] chars = seed.toCharArray();
            int slen = seed.length(), i = 0;
            for (; i < 256 && i < slen; i++) {
                results[i] = CrossHash.hash64(chars, i, slen);
            }
            for (; i < 256; i++) {
                results[i] = CrossHash.hash64(results);
            }
            init(true);
        }
    }

    private IsaacRNG(IsaacRNG other)
    {
        this(other.results);
    }

    /**
     *  Generates 256 results to be used by later calls to next() or nextLong().
     *  This is a fast (not small) implementation.
     *  */
    public final void regen() {
        int i, j;
        long x, y;

        b += ++c;
        for (i=0, j=128; i<128;) {
            x = mem[i];
            a ^= a<<21;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;

            x = mem[i];
            a ^= a>>>5;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;

            x = mem[i];
            a ^= a<<12;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;

            x = mem[i];
            a ^= a>>>33;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;
        }

        for (j=0; j<128;) {
            x = mem[i];
            a ^= a<<21;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;

            x = mem[i];
            a ^= a>>>5;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;

            x = mem[i];
            a ^= a<<12;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;

            x = mem[i];
            a ^= a>>>33;
            a += mem[j++];
            mem[i] = y = mem[(int)(x&MASK)>>3] + a + b;
            results[i++] = b = mem[(int)((y>>8)&MASK)>>3] + x;
        }
    }


    /**
     * Initializes this IsaacRNG; typically used from the constructor but can be called externally.
     * @param flag if true, use data from seed; if false, initializes this to unseeded random state
     */
    public final void init(boolean flag) {
        int i;
        long a,b,c,d,e,f,g,h;
        a=b=c=d=e=f=g=h=0x9e3779b97f4a7c13L;                        /* the golden ratio */

        for (i=0; i<4; ++i) {
            a-=e; f^=h>>>9;  h+=a;
            b-=f; g^=a<<9;  a+=b;
            c-=g; h^=b>>>23; b+=c;
            d-=h; a^=c<<15; c+=d;
            e-=a; b^=d>>>14; d+=e;
            f-=b; c^=e<<20; e+=f;
            g-=c; d^=f>>>17; f+=g;
            h-=d; e^=g<<14; g+=h;
            /*
            a^=b<<11;  d+=a; b+=c;
            b^=c>>>3;  e+=b; c+=d;
            c^=d<<8;   f+=c; d+=e;
            d^=e>>>16; g+=d; e+=f;
            e^=f<<10;  h+=e; f+=g;
            f^=g>>>4;  a+=f; g+=h;
            g^=h<<8;   b+=g; h+=a;
            h^=a>>>9;  c+=h; a+=b;
            */
        }

        for (i=0; i<256; i+=8) {              /* fill in mem[] with messy stuff */
            if (flag) {
                a+= results[i  ]; b+= results[i+1]; c+= results[i+2]; d+= results[i+3];
                e+= results[i+4]; f+= results[i+5]; g+= results[i+6]; h+= results[i+7];
            }
            a-=e; f^=h>>>9;  h+=a;
            b-=f; g^=a<<9;  a+=b;
            c-=g; h^=b>>>23; b+=c;
            d-=h; a^=c<<15; c+=d;
            e-=a; b^=d>>>14; d+=e;
            f-=b; c^=e<<20; e+=f;
            g-=c; d^=f>>>17; f+=g;
            h-=d; e^=g<<14; g+=h;
            mem[i  ]=a; mem[i+1]=b; mem[i+2]=c; mem[i+3]=d;
            mem[i+4]=e; mem[i+5]=f; mem[i+6]=g; mem[i+7]=h;
        }

        if (flag) {           /* second pass makes all of seed affect all of mem */
            for (i=0; i<256; i+=8) {
                a+=mem[i  ]; b+=mem[i+1]; c+=mem[i+2]; d+=mem[i+3];
                e+=mem[i+4]; f+=mem[i+5]; g+=mem[i+6]; h+=mem[i+7];
                a-=e; f^=h>>>9;  h+=a;
                b-=f; g^=a<<9;  a+=b;
                c-=g; h^=b>>>23; b+=c;
                d-=h; a^=c<<15; c+=d;
                e-=a; b^=d>>>14; d+=e;
                f-=b; c^=e<<20; e+=f;
                g-=c; d^=f>>>17; f+=g;
                h-=d; e^=g<<14; g+=h;
                mem[i  ]=a; mem[i+1]=b; mem[i+2]=c; mem[i+3]=d;
                mem[i+4]=e; mem[i+5]=f; mem[i+6]=g; mem[i+7]=h;
            }
        }

        regen();
        count = 256;
    }


    @Override
    public final long nextLong() {
        if (0 == count--) {
            regen();
            count = 255;
        }
        return results[count];
    }

    @Override
    public int next( int bits ) {
        //return (int)( nextLong() >>> (64 - bits) );
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Produces another RandomnessSource, but the new one will not produce the same data as this one.
     * This is meant to be a "more-secure" generator, so this helps reduce the ability to guess future
     * results from a given sequence of output.
     * @return another RandomnessSource with the same implementation but no guarantees as to generation
     */
    @Override
    public RandomnessSource copy() {
        return new IsaacRNG(results);
    }
}
