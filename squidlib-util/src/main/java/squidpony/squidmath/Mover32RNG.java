package squidpony.squidmath;

import squidpony.StringKit;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a cmr^cmr with two 32-bit states; this is the fastest 32-bit generator that still passes statistical
 * tests, plus it's optimized for GWT (sometimes). It has a period of just under 2 to the 64, 0xFFF1F6F18B2A1330, which
 * is roughly 2 to the 63.999691, and allows 2 to the 32 initial seeds.
 * <br>
 * This seems to do well in PractRand testing (32 TB passed), but this is not a generator Overton tested. "Chaotic"
 * generators like this one tend to score well in PractRand, but it isn't clear if they will fail other tests (in
 * particular, they can't generate all possible long values, and also can't generate 0 or possibly some other ints). As
 * for desktop/server speed, this is faster than {@link Lathe32RNG} (which is also high-quality) and is also faster than
 * {@link Starfish32RNG} (which is very fast but has quality issues) and {@link ThrustAlt32RNG} (which has a very small
 * period and probably isn't very useful). However, this is slower than Lathe32RNG when using GWT and viewing in
 * Firefox; for some reason {@link Starfish32RNG} optimizes well on Firefox and less well on Chrome, but Mover does very
 * well in older Chrome (faster than Lathe) and rather poorly on Firefox. It doesn't do amazingly well in current Chrome
 * versions, however, and Lathe beats it most of the time there. On 64-bit desktop or server Java, you may want to
 * prefer {@link Mover64RNG}, which is the same algorithm using larger words and constants. While each of the two parts
 * of a Mover32RNG can have their full period evaluated, making the total period possible to calculate, the same cannot
 * be said for Mover64RNG (its period is high enough for most usage, but the actual total is still unknown).
 * <br>
 * Its period is 0xFFF1F6F18B2A1330 for the largest cycle, which it always initializes into if {@link #setState(int)} is
 * used. setState() only allows 2 to the 32 starting states, but less than 2 to the 64 states are in the largest cycle,
 * so using a long or two ints to set the state seems ill-advised. The generator has two similar parts, each updated
 * without needing to read from the other part. Each is a 32-bit CMR generator, which multiplies a state by a constant,
 * rotates by another constant, and stores that as the next state. The particular constants used here were found by
 * randomly picking 16-bit odd numbers as multipliers, checking the period for every non-zero rotation, and reporting
 * the multiplier and rotation amount when a period was found that was greater than 0xFF000000. Better multipliers are
 * almost guaranteed to exist, but finding them would be a challenge.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It uses two generators with different cycle lengths, and skips at most 65536 times
 * into each generator's cycle independently when seeding. It uses constants to store 128 known midpoints for each
 * generator, which ensures it calculates an advance for each generator at most 511 times. 
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed.
 * <br>
 * Created by Tommy Ettinger on 8/6/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class Mover32RNG implements RandomnessSource {
    private int stateA, stateB;
    public Mover32RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Mover32RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #Mover32RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA
     * @param stateB
     */
    public Mover32RNG(final int stateA, final int stateB)
    {
        this.stateA = stateA == 0 ? 1 : stateA;
        this.stateB = stateB == 0 ? 1 : stateB;
    }

    private static final int[] startingA = {
            0x00000001, 0xCB2DA1A7, 0x215A5ADF, 0x2688266B, 0xEA31ECEE, 0x3F02F6A8, 0xB0833422, 0xC791ACA6,
            0x976236C8, 0xF57961C0, 0x16EBE830, 0xCCCC2F10, 0x165D9801, 0x15E02FEA, 0xA302CC65, 0xAF68AE37,
            0x4997CCA3, 0xF331F604, 0xF1DE5DA7, 0x07F21BA7, 0xD1752EC7, 0x308B16F2, 0x1B92D899, 0xF1A38AC8,
            0x58F317B2, 0x1CC8EC79, 0x62588F4B, 0x975BF8FE, 0xE589C2D0, 0xB087C03D, 0x600F5DD0, 0xA32BD629,
            0x3B52D26D, 0x0C7C18FD, 0xEB037A63, 0xE6F8BC93, 0x2CD250CF, 0x84327882, 0xA708FC6E, 0x5873EF12,
            0x72FD78CF, 0xFFE73771, 0x18817285, 0x8EB3BC50, 0xE68597E0, 0xDF719E77, 0x35FE32C8, 0xF60532A1,
            0xE93A1484, 0x697DF36B, 0xDFD41306, 0x37E0FADD, 0x6883EB39, 0xAF9CF955, 0xE11EB329, 0xDA951CC2,
            0x325ECD67, 0x1DD8AC79, 0x7632669F, 0x0949BCB0, 0x965B0557, 0xB72DC0BC, 0x84448A7C, 0x6AC9B9CF,
            0x92B7742A, 0xCFB27744, 0xFF154B26, 0xFD11E5F7, 0x5B6DE8D4, 0x59727211, 0x0A36FF7F, 0x56657899,
            0xF9848758, 0x59415D9E, 0xE70E6901, 0x90858D00, 0x10B73995, 0x324FC7AD, 0xC62F801D, 0x4BBDA0E8,
            0x70C8FDD5, 0xCC4376F1, 0x489AD7B7, 0xF4FB2500, 0x2279E051, 0x7840BF9E, 0x876AABF9, 0xF374F7BD,
            0x6074B429, 0xC2EE6430, 0x238172DA, 0xFE3D050E, 0x5EF2F6B4, 0xF6359946, 0x127AAD89, 0xECA6FA56,
            0x678B27CE, 0xDCD03A3C, 0xA45371BB, 0x5F2F422C, 0xE26B613C, 0x70DD9AF4, 0x1B0787BB, 0x0B8D2553,
            0x3A430C3F, 0xAFF29AE2, 0x9DFAEB51, 0x1DE0F40E, 0x0467D74A, 0x85949411, 0xF8BC0358, 0x558BA744,
            0x41A5B43A, 0x6B7E1C89, 0x9BF095BD, 0x5E2473CC, 0x4DFBF45B, 0xFB3510DC, 0xB7EC5786, 0xA99D6129,
            0x120988F6, 0x796A7DE7, 0x9DEFD945, 0x0D2B25CE, 0xB7C1107E, 0x72E29D75, 0x85E01D79, 0x69AB992F,
    }, startingB = {
            0x00000001, 0xAB7EE445, 0x6FB35C9C, 0x459AB7A2, 0xEA61D065, 0x306F5E5F, 0xCE50A64A, 0x6D76B642,
            0x11F3C6D4, 0xB3FF1D66, 0x657E6790, 0x4C62472D, 0xBEABAB16, 0xFD455176, 0xDCB98EDB, 0x1FC27360,
            0x80C1241C, 0xC0C5BCC0, 0x6A67518B, 0xF2D69A39, 0xFA7D6C16, 0xE906A517, 0x899FFA7B, 0x2E42A99D,
            0xBAF5B6E8, 0x3BBDC45A, 0x2497A707, 0xEA2DB138, 0x7D4ABF97, 0x552F5D4E, 0x15FE4BBD, 0xBC51DF5A,
            0x465BDC95, 0x736A018F, 0x8A72CB63, 0x103119BE, 0x40403117, 0xA295957B, 0xCDDA9C19, 0xF0551CC6,
            0x77CBAB76, 0xA054FD6A, 0x8974C93F, 0x8E314DC1, 0x42BC030E, 0x7F090540, 0x177998EA, 0x20457F09,
            0xC13609D7, 0xA2683753, 0xE9F84638, 0x1BE07B83, 0x5DB36480, 0x39AE5B3A, 0xE044E164, 0x6E6B6191,
            0x6036E5C8, 0x00703FE1, 0x53935ED8, 0x6B4443F5, 0x8FB91605, 0x146478C9, 0x2D0429BB, 0x86E8F88A,
            0xD8DFFDB7, 0x77223F7D, 0x2B065674, 0xD80D2DD6, 0x0DFE5CEB, 0x44A495A5, 0x758EF0A9, 0x5FB55BA5,
            0x8935A9B1, 0x84189069, 0xAA2194BC, 0x5FB95103, 0x6B60B887, 0xC63A769E, 0xA74BE357, 0x9F71B1F8,
            0x3320B09E, 0xD369B3FC, 0xBCDB4B4E, 0xDC4DBCC9, 0x01F67CD2, 0xB3F6AA2B, 0x082CA2B3, 0xA54F168F,
            0x0A9F82C9, 0x77DC3F93, 0x18D32D96, 0x1FC3FCE5, 0x97542B7A, 0x88CA9F81, 0x75370CE4, 0x8C2749C3,
            0x94B63AE4, 0xC55E3BB7, 0x176BA775, 0x8C2BFEFC, 0x8C457557, 0xD8BFFD54, 0xB3D322DC, 0x072D766C,
            0x40912BF4, 0x99CA7F36, 0xBC78BE45, 0x22F95B6B, 0xB37B05C9, 0x23493DB3, 0xCFBDC9C3, 0xB0379084,
            0x2A2BFA20, 0x9A9DA93D, 0xCDE62486, 0x079CF8E6, 0x5B45CF64, 0xA19945A8, 0x196C1AA8, 0x9B19C771,
            0x702CC28B, 0xFF4C5B02, 0x2FDD78D2, 0x71FFBD4E, 0xDF4C60A4, 0x143FAB0B, 0xAD9C8EB0, 0x6F35837D,
    };

    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA *= 0x89A7;
            stateA = (stateA << 13 | stateA >>> 19);
        }
        stateB = startingB[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateB *= 0xBCFD;
            stateB = (stateB << 17 | stateB >>> 15);
        }
    }

    public final int nextInt()
    {
        int y = stateA * 0x89A7;
        stateA = (y = (y << 13 | y >>> 19));
        final int x = stateB * 0xBCFD;
        return (y ^ (stateB = (x << 17 | x >>> 15)));
    }
    @Override
    public final int next(final int bits)
    {
        int y = stateA * 0x89A7;
        stateA = (y = (y << 13 | y >>> 19));
        final int x = stateB * 0xBCFD;
        return (y ^ (stateB = (x << 17 | x >>> 15))) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        int y = stateA * 0x89A7;
        y = (y << 13 | y >>> 19);
        int x = stateB * 0xBCFD;
        final long t = y ^ (x = (x << 17 | x >>> 15));
        y *= 0x89A7;
        stateA = (y = (y << 13 | y >>> 19));
        x *= 0xBCFD;
        return t << 32 ^ (y ^ (stateB = (x << 17 | x >>> 15)));
    }

    /**
     * Produces a copy of this Mover32RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Mover32RNG
     */
    @Override
    public Mover32RNG copy() {
        return new Mover32RNG(stateA, stateB);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Mover32RNG()}, {@link #Mover32RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "A" part of the state, an int
     */
    public int getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "B" part of the state; if this generator was set with {@link #Mover32RNG()}, {@link #Mover32RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "B" part of the state, an int
     */
    public int getStateB()
    {
        return stateB;
    }
    /**
     * Sets the "A" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateA any int
     */
    public void setStateA(final int stateA)
    {
        this.stateA = stateA;
    }

    /**
     * Sets the "B" part of the state to any int, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateB any int
     */
    public void setStateB(final int stateB)
    {
        this.stateB = stateB;
    }
    
    @Override
    public String toString() {
        return "Mover32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mover32RNG mover32RNG = (Mover32RNG) o;

        return stateA == mover32RNG.stateA && stateB == mover32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB | 0;
    }

//    public static void main(String[] args)
//    {
//        // A 10 0xC010AEB4
//        // B 22 0x195B9108
//        // all  0x04C194F3485D5A68
//
//        // A 17 0xF7F87D28
//        // B 14 0xF023E25B 
//        // all  0xE89BB7902049CD38
//
//
//        // A11 B14 0xBBDA9763B6CA318D
//        // A8  B14 0xC109F954C76CB09C
//        // A17 B14 0xE89BB7902049CD38
////        BigInteger result = BigInteger.valueOf(0xF7F87D28L);
////        BigInteger tmp = BigInteger.valueOf(0xF023E25BL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("0x%016X\n", result.longValue());
//        int stateA = 1, i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(stateA * 0x9E37, 17)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }
//        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        i = 0;
//        for (; ; i++) {
//            if((stateA = Integer.rotateLeft(stateA * 0x4E6D, 14)) == 1)
//            {
//                System.out.printf("0x%08X\n", i);
//                break;
//            }
//        }         
//        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%016X\n", result.longValue());
//
//    }

//    public static void main(String[] args)
//    {
//        Mover32RNG m = new Mover32RNG();
//        System.out.println("int[] startingA = {");
//        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x00000200) {
//            m.setState(i);
//            System.out.printf("0x%08X, ", m.stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//        }
//        System.out.println("}, startingB = {");
//        for (int i = 0, ctr = 0; ctr < 128; ctr++, i+= 0x02000000) {
//            m.setState(i);
//            System.out.printf("0x%08X, ", m.stateB);
//            if((ctr & 7) == 7)
//                System.out.println();
//        }
//        System.out.println("};");
//    }
    
///////// BEGIN subcycle finder code and period evaluator
//    public static void main(String[] args)
//    {
//        // multiplying
//        // A refers to 0x9E377
//        // A 10 0xC010AEB4
//        // B refers to 0x64E6D
//        // B 22 0x195B9108
//        // all  0x04C194F3485D5A68
//
//        // A=Integer.rotateLeft(A*0x9E377, 17) 0xF7F87D28
//        // B=Integer.rotateLeft(A*0x64E6D, 14) 0xF023E25B 
//        // all  0xE89BB7902049CD38
//
//
//        // A11 B14 0xBBDA9763B6CA318D
//        // A8  B14 0xC109F954C76CB09C
//        // A17 B14 0xE89BB7902049CD38
////        BigInteger result = BigInteger.valueOf(0xF7F87D28L);
////        BigInteger tmp = BigInteger.valueOf(0xF023E25BL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("0x%016X\n", result.longValue());
//        // 0x9E37
//        // rotation 27: 0xEE06F34D
//        // 0x9E35
//        // rotation 6 : 0xE1183C3A
//        // rotation 19: 0xC4FCFC55
//        // 0x9E3B
//        // rotation 25: 0xE69313ED
//        // 0xDE4D
//        // rotation 3 : 0xF6C16607
//        // rotation 23: 0xD23AD58D
//        // rotation 29: 0xC56DC41F
//        // 0x1337
//        // rotation 7: 0xF41BD009
//        // rotation 20: 0xF5846878
//        // rotation 25: 0xF38658F9
//        // 0xACED
//        // rotation 28: 0xFC98CC08
//        // rotation 31: 0xFA18CD57
//        // 0xBA55
//        // rotation 19: 0xFB059E43
//        // 0xC6D5
//        // rotation 05: 0xFFD78FD4
//        // 0x5995
//        // rotation 28: 0xFF4AB87D
//        // rotation 02: 0xFF2AA5D5
//        // 0xA3A9
//        // rotation 09: 0xFF6B3AF7
//        // 0xB9EF
//        // rotation 23: 0xFFAEB037
//        // 0x3D29
//        // rotation 04: 0xFF6B92C5
//        // 0x5FAB
//        // rotation 09: 0xFF7E3277 // seems to be very composite
//        // 0xCB7F
//        // rotation 01: 0xFF7F28FE
//        // 0x89A7
//        // rotation 13: 0xFFFDBF50 // wow! note that this is a multiple of 16
//        // 0xBCFD
//        // rotation 17: 0xFFF43787 // second-highest yet, also an odd number
//        // 0xA01B
//        // rotation 28: 0xFFEDA0B5
//        // 0xC2B9
//        // rotation 16: 0xFFEA9001
//        
//        
//        // adding
//        // 0x9E3779B9
//        // rotation 2 : 0xFFCC8933
//        // rotation 7 : 0xF715CEDF
//        // rotation 25: 0xF715CEDF
//        // rotation 30: 0xFFCC8933
//        // 0x6C8E9CF5
//        // rotation 6 : 0xF721971A
//        // 0x41C64E6D
//        // rotation 13: 0xFA312DBF
//        // rotation 19: 0xFA312DBF
//        // rotation 1 : 0xF945B8A7
//        // rotation 31: 0xF945B8A7
//        // 0xC3564E95
//        // rotation 1 : 0xFA69E895 also 31
//        // rotation 5 : 0xF2BF5E23 also 27
//        // 0x76BAF5E3
//        // rotation 14: 0xF4DDFC5A also 18
//        // 0xA67943A3 
//        // rotation 11: 0xF1044048 also 21
//        // 0x6C96FEE7
//        // rotation 2 : 0xF4098F0D
//        // 0xA3014337
//        // rotation 15: 0xF3700ABF also 17
//        // 0x9E3759B9
//        // rotation 1 : 0xFB6547A2 also 31
//        // 0x6C8E9CF7
//        // rotation 7 : 0xFF151D74 also 25
//        // rotation 13: 0xFD468E2B also 19
//        // rotation 6 : 0xF145A7EB also 26
//        // 0xB531A935
//        // rotation 13: 0xFF9E2F67 also 19
//        // 0xC0EF50EB
//        // rotation 07: 0xFFF8A98D also 25
//        // 0x518DC14F
//        // rotation 09: 0xFFABD755 also 23 // probably not prime
//        // 0xA5F152BF
//        // rotation 07: 0xFFB234B2 also 27
//        // 0x8092D909
//        // rotation 10: 0xFFA82F7C also 22
//        // 0x73E2CCAB
//        // rotation 09: 0xFF9DE8B1 also 23
//        // stateB = rotate32(stateB + 0xB531A935, 13)
//        // stateC = rotate32(stateC + 0xC0EF50EB, 7)
//
//        // subtracting, rotating, and bitwise NOT:
//        // 0xC68E9CF3
//        // rotation 13: 0xFEF97E17, also 19 
//        // 0xC68E9CB7
//        // rotation 12: 0xFE3D7A2E
//
//        // left xorshift
//        // 5
//        // rotation 15: 0xFFF7E000
//        // 13
//        // rotation 17: 0xFFFD8000
//
//        // minus left shift, then xor
//        // state - (state << 12) ^ 0xC68E9CB7, rotation 21: 0xFFD299CB
//        // add xor
//        // state + 0xC68E9CB7 ^ 0xDFF4ECB9, rotation 30: 0xFFDAEDF7
//        // state + 0xC68E9CB7 ^ 0xB5402ED7, rotation 01: 0xFFE73631
//        // state + 0xC68E9CB7 ^ 0xB2B386E5, rotation 24: 0xFFE29F5D
//        // sub xor
//        // state - 0x9E3779B9 ^ 0xE541440F, rotation 22: 0xFFFC9E3E
//
//
//        // best power of two:
//        // can get 63.999691 with: (period is 0xFFF1F6F18B2A1330)
//        // multiplying A by 0x89A7 and rotating left by 13
//        // multiplying B by 0xBCFD and rotating left by 17
//        // can get 63.998159 with: (period is 0xFFAC703E2B6B1A30)
//        // multiplying A by 0x89A7 and rotating left by 13
//        // multiplying B by 0xB9EF and rotating left by 23
//        // can get 63.998 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // xorshifting B left by 5 (B ^ B << 5) and rotating left by 15
//        // can get 63.99 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // adding 0x6C8E9CF7 for B and rotating left by 7
//        // can get 63.98 with:
//        // adding 0x9E3779B9 for A and rotating left by 2
//        // multiplying by 0xACED, NOTing, and rotating left by 28 for B
//        // 0xFF6B3AF7L 0xFFAEB037L 0xFFD78FD4L
//        
//        // 0xFF42E24AF92DCD8C, 63.995831
//        //BigInteger result = BigInteger.valueOf(0xFF6B3AF7L), tmp = BigInteger.valueOf(0xFFD78FD4L);
//
//        BigInteger result = BigInteger.valueOf(0xFFFDBF50L), tmp = BigInteger.valueOf(0xFFF43787L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        tmp = BigInteger.valueOf(0xFFEDA0B5L);
//        result = tmp.divide(result.gcd(tmp)).multiply(result);
//        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
////        tmp = BigInteger.valueOf(0xFFABD755L);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("\n0x%s, %2.6f\n", result.toString(16).toUpperCase(), Math.log(result.doubleValue()) / Math.log(2));
//        int stateA = 1, i;
//        LinnormRNG lin = new LinnormRNG();
//        System.out.println(lin.getState());
//        Random rand = new RNG(lin).asRandom();
//        for (int c = 1; c <= 200; c++) {
//            //final int r = (ThrustAlt32RNG.determine(20007 + c) & 0xFFFF)|1;
//            final int r = BigInteger.probablePrime(20, rand).intValue();
//            //System.out.printf("(x ^ x << %d) + 0xC68E9CB7\n", c);
//            System.out.printf("%03d/200, testing r = 0x%08X\n", c, r);
//            for (int j = 1; j < 32; j++) {
//                i = 0;
//                for (; ; i++) {
//                    if ((stateA = Integer.rotateLeft(stateA * r, j)) == 1) {
//                        if (i >>> 24 == 0xFF)
//                            System.out.printf("(state * 0x%08X, rotation %02d: 0x%08X\n", r, j, i);
//                        break;
//                    }
//                }
//            }
//        }
//
////        int stateA = 1, i = 0;
////        for (; ; i++) {
////            if((stateA = Integer.rotateLeft(~(stateA * 0x9E37), 7)) == 1)
////            {
////                System.out.printf("0x%08X\n", i);
////                break;
////            }
////        }
////        BigInteger result = BigInteger.valueOf(i & 0xFFFFFFFFL);
////        i = 0;
////        for (; ; i++) {
////            if((stateA = Integer.rotateLeft(~(stateA * 0x4E6D), 17)) == 1)
////            {
////                System.out.printf("0x%08X\n", i);
////                break;
////            }
////        }         
////        BigInteger tmp = BigInteger.valueOf(i & 0xFFFFFFFFL);
////        result = tmp.divide(result.gcd(tmp)).multiply(result);
////        System.out.printf("\n0x%016X\n", result.longValue());
//
//    }
///////// END subcycle finder code and period evaluator
    
    
//    public static void main(String[] args)
//    {
//        int stateA = 1, stateB = 1;
//        System.out.println("int[] startingA = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%08X, ", stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateA *= 0x89A7;
//                stateA = (stateA << 13 | stateA >>> 19);
//            }
//        }
//        System.out.println("}, startingB = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%08X, ", stateB);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateB *= 0xBCFD;
//                stateB = (stateB << 17 | stateB >>> 15);
//            }
//        }
//        System.out.println("};");
//    }
}
