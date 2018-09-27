package squidpony.squidmath;

import squidpony.StringKit;

/**
 * One of Mark Overton's subcycle generators from <a href="http://www.drdobbs.com/tools/229625477">this article</a>,
 * specifically a cmr^cmr with two 64-bit states. It is extremely fast, faster than {@link LinnormRNG} and
 * {@link TangleRNG}, but its period is unknown. The period is at the very least 2 to the 38, since each of its
 * sub-generators has been checked up to that period length without running out of period, and the total period of a
 * Mover64RNG should be greater than 2 to the 64 with a very high likelihood. The total period is also very unlikely to
 * be a power of two or even a number close to a power of two; it could be odd or even. The guarantees of Linnorm and
 * Tangle that they are certain to have a period of 2 to the 64 may be worthwhile reasons to use them; they also will be
 * faster when setting the state often. LightRNG is a SkippingRandomness and a StatefulRandomness, but this is neither,
 * so you may want to prefer LightRNG for that reason. This generator is not equidistributed, so it can return some long
 * values more often than others and some not at all; you may want to use a generator with guarantees on distribution if
 * you don't want to see some returned values (or pairs of values) more often than others. {@link Starfish32RNG} is
 * 2-dimensionally equidistributed for ints, so it returns all pairs of ints with equal likelihood, and it may be a good
 * RandomnessSource to use if distribution matters (though you should prefer {@link GWTRNG}, which is implemented using
 * Starfish32RNG, because it is specialized to use a 32-bit generator).
 * <br>
 * This seems to do well in PractRand testing, passing a full 32TB with two ("unusual") anomalies, but this is not one
 * of the exact generators Overton tested. "Chaotic" generators like this one tend to score well in PractRand, but it
 * isn't clear if they will fail other tests (in particular, they probably can't generate all possible long values, and
 * maybe can't generate some ints). This generator is not equidistributed.
 * <br>
 * The generator has two similar parts, each updated without needing to read from the other part. Each is a 64-bit CMR
 * generator, which multiplies a state by a constant, rotates by another constant, and stores that as the next state.
 * The multiplier constants used here were chosen arbitrarily, since almost all odd-number multipliers produce a period
 * for a 64-bit CMR generator that is too long to iterate through and compare. One multiplier is close to the LCG
 * multiplier used in PractRand (0x41C64E6B); the other is very close to the golden ratio times 2 to the 32
 * (0x9E3779B9). Better multipliers are almost guaranteed to exist, but finding them would be a challenge. The rotation
 * constants, 28 and 37, were chosen so they were sufficiently different and so the sum of their (left or right)
 * rotation amounts is close to 64, which seems to help quality. Oddly, substituting an addition for one of the two
 * multiplications slows this generator down reliably, and does nothing to help quality. Even though addition should be
 * faster, the two near-identical operations performed on two states may be able to be compiled into vector operations
 * if the compiler is clever enough, but an addition on one state and a multiplication on the other would not make sense
 * to see SIMD optimization.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It uses two generators with different cycle lengths, and skips at most 65536 times
 * into each generator's cycle independently when seeding. It uses constants to store 128 known midpoints for each
 * generator, which ensures it calculates an advance for each generator at most 511 times. There are 2 to the 32
 * possible starting states for this generator when using {@link #setState(int)}, but it is unknown if that method
 * actually puts the generator in the longest possible cycle, or just a sufficiently long one.
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed.
 * <br>
 * Created by Tommy Ettinger on 9/13/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class Mover64RNG implements RandomnessSource {
    private long stateA, stateB;
    public Mover64RNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public Mover64RNG(final int state)
    {
        setState(state);
    }

    /**
     * Not advised for external use; prefer {@link #Mover64RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param stateA
     * @param stateB
     */
    public Mover64RNG(final long stateA, final long stateB)
    {
        this.stateA = stateA == 0L ? 1L : stateA;
        this.stateB = stateB == 0L ? 1L : stateB;
    }

    private static final long[] startingA = {
            0x0000000000000001L, 0x770391C6587202CDL, 0x0148D0D6B055DE19L, 0x2CFFDEE4C4C8654FL, 0x3669291DE218F372L, 0x5B744ACB07F3D380L, 0x103F93C86BDF21D0L, 0x9A1D980831BCF2ABL,
            0x92D56961736A4B50L, 0x71A9832527530EADL, 0x4C524342889BCFE1L, 0xF39CFA3D37AB4038L, 0xA3E9A70AD8EEF84DL, 0x65C7AFEFFC4DA898L, 0x4D455E304CDC7741L, 0xA6EDACBD6740B1A7L,
            0xAA7F8E77C41AF5EBL, 0x96B50AD6E4AA2B18L, 0x77432395B55EDFD9L, 0x2748C2DD4565F1F0L, 0x3CAC2CDB2F8318D0L, 0x7D983C0295175158L, 0xDCFC33F629C3D00FL, 0x1EF0C5B47164F981L,
            0x3AB9A3877956251EL, 0xBA230F415A833533L, 0xA489CC2EF532A6BEL, 0xB212F25D09BFC366L, 0xB9014F210A77310DL, 0x8590EF3967A8C6E0L, 0x1011FD4E97B1F81AL, 0x1C57F18A80F4C131L,
            0x4AA90F013DB975E3L, 0xB3FAAC7A9374BD99L, 0xA15B9AA709431B2DL, 0xD3201A4C3953FFA2L, 0x0B34634F0B74BAB5L, 0x501389102E6E08EEL, 0xFCAC8A7CFCEB534DL, 0xA6A1A2C7CB5CEE8FL,
            0x5F461436431B3D6DL, 0x1F3DE41F1E991A39L, 0x96A5BD1D16EDC265L, 0xAEC3F8C461FA0749L, 0x4445933104846C0BL, 0xAD088B25A4AA0E59L, 0x862FCA81FF8B1BE5L, 0x12E5A795C17DA902L,
            0x5CA3CDC494DF2B93L, 0xCF612FCBDD25B41EL, 0xAD0CC4406EC6FCC3L, 0x352336C92FA965EAL, 0x675AB684694EE4A8L, 0x811A5D47EE8B3568L, 0x4937A92A07C372A4L, 0xE1483C680A24BEA4L,
            0x1B3E829B910E343CL, 0x0F5F8EF159F931C0L, 0x7F5DDFDA98EFE7EAL, 0xA2FA4A6C79F5C6EFL, 0xEA416C98A2A0945CL, 0x29CC34E89FCC5D02L, 0x157FC5094CCC1795L, 0x27252C1165C6E255L,
            0xAB963445C144A9BCL, 0x601530DECC304F69L, 0xC92D8F3257316572L, 0xC348074025724519L, 0x0F8305789523701EL, 0xD288EFE7BDDABF47L, 0xC428DA0AD18149BAL, 0xBA1D19D35E61A11EL,
            0x6D81979DC0110FA2L, 0x3C144A6DC2C2982BL, 0x7593425EA77652A8L, 0xBA416F84332EFD0AL, 0x691EAA02B1351B41L, 0xF1B15F5AD69A16BAL, 0x026D58B160B39D4CL, 0x813B48A15DA161E6L,
            0xCC92B59765EF4C5FL, 0x46B6C1ED44BF6877L, 0xA679D47C27EA4A03L, 0x393BEF21C904261CL, 0xE40A734EFE039992L, 0xD114E560A35EC443L, 0x85A46B901B80F546L, 0xCC8C80C6AB27F53CL,
            0xC9B5FCE7C3EE4A83L, 0x64D4B2A2A91ADC11L, 0x7157576E65940314L, 0x75BF0B0737304143L, 0x4A11300B7F32C8C5L, 0x4B4FB70D7701DD60L, 0xE877F97BEC9E8FACL, 0x151E431374EF9D79L,
            0x636214B0856DF427L, 0x088F1774DE7730CFL, 0x9E3B5CD7FF590F81L, 0x4DA157EA25850BC1L, 0xE9C7C31744E062F4L, 0x4767FCAF076B9508L, 0xC5C767D939AC8425L, 0x1ABAF0D4EC698A8FL,
            0x5035DC94FA971B81L, 0x718EE38E931713E2L, 0x497DB43133CEF0F2L, 0xF01BE721B0145805L, 0x9D6239853FF80744L, 0x256B893D4DD0689AL, 0x256647CAA07563D6L, 0xCE4087F877A6D24FL,
            0x68A0537869364FE2L, 0x32BA732DEC14AE42L, 0x3AAF6CDE0CE8DB48L, 0x552C1D9594CE212AL, 0x8BC1A33AE250B2E9L, 0xC02FCB678B465D00L, 0x496F580658AFD50AL, 0x6D0D982E45AD15A9L,
            0xC8E87307F336E8D0L, 0x257E726598418548L, 0xFADF2ED10B13D148L, 0x46FA6CC74F293535L, 0xF03227995C268856L, 0x46087E39622EA4CDL, 0x17EE09D3D2181207L, 0x9C7518A1E5AD4544L,
    }, startingB = {
            0x0000000000000001L, 0x07293E6E09EC6368L, 0x96D969CADB4CD368L, 0x3FF86768F89EAEB3L, 0x2F9FAC39CC8E5CB7L, 0xF0ACF2D0542EE141L, 0x7BF403A079DCD087L, 0xDA68703F5EAB9409L,
            0xF887EDE8E8AD388BL, 0xB93108A12DD8DC5CL, 0x98676A8BE90BB48EL, 0x3C66E22B602A7007L, 0x69A56A92BAD39B5BL, 0x58857B966DDF07BCL, 0x3B6890E3EDB96D6EL, 0xF0363B595221C86DL,
            0x62EE3C3A7A528614L, 0xB0175247E00B4935L, 0xE70D810777ED42ACL, 0x275CE4F27473631AL, 0xB5DF57C4502967E9L, 0xB8EB0B9EC111C7FEL, 0xC28F3B422CA03689L, 0xD09DD3A8FEAB2DD1L,
            0x4E2C713B5A7A0FFCL, 0x9AFC4BE99ED5F1AAL, 0xA89BFD2F6C2E97AEL, 0xF8735B9A6DF5F258L, 0xB2F89E533D9B9897L, 0xD89711EA7777E671L, 0x9658217AF4F448CAL, 0xEE3F474204385F6BL,
            0x2B20D085EAB7ECC0L, 0xDF4FBDB5877EC70AL, 0xA27D970C88F1246BL, 0x88D0B336E63ADE23L, 0xC06AF42B0855C181L, 0x00E8B464987358DBL, 0xDA1DF8BA1E45586EL, 0x4C12347AB35D2F03L,
            0x752C4942F1095640L, 0x608BD5FC9E04FA0AL, 0xB253E48775CDD5E1L, 0x643E8401460AAA59L, 0xE248C00B3A622F06L, 0xB01AD54DFE588BF2L, 0x1D486285F47A99D0L, 0x4ACE70E9A24E7B42L,
            0xE498314246C2E894L, 0x67BB0785AEA67873L, 0xAB50922AD5171ABDL, 0x4BFA6DEE10549DC3L, 0x889BB7C03B745D65L, 0x705D68BC7379AEECL, 0x08BC6282C82C8B72L, 0xB967A84918604EDCL,
            0x17F2AE6E78487967L, 0x038874F2D394FB80L, 0x7F7A2F1A581C66D3L, 0x99977A67381F6F7FL, 0x6B62915A4927F8D3L, 0x4DE18BB59A3C182DL, 0x94E508A682455109L, 0x986BE18241462557L,
            0x0578DFAE00F8A0D6L, 0x29B22988B2264886L, 0xD552345E6E2A3125L, 0x5DB9E3195164C051L, 0x0E43BA334827D573L, 0x3AFAF8799E87209CL, 0xBC0E249E28B42DE9L, 0x022A07577137E25FL,
            0x7DEB553C69DAA1FFL, 0x4A69C3A72EF45E41L, 0xBEBAC3CF3B608398L, 0xEB5771FF214E2487L, 0x9FB5E8C5B36B4CC9L, 0xC09F95341A44B518L, 0x668BEA20B4AE0875L, 0x633E56557743D5CBL,
            0x60F91113C85EAAAAL, 0xB7FD377C14A36222L, 0xFCF5360544E39E14L, 0xC8201F79E019A016L, 0x9298BE81EFD5200FL, 0xBEB6A71A91068F67L, 0xB48125BFEFE20180L, 0xC470152566C3E1A0L,
            0x46646F5388059BA1L, 0x6B2EFA0363CEB524L, 0xC60186015E2573E1L, 0x514BF9772FA2ACCEL, 0x1C44DACDE62A44EDL, 0x0CC4356D150B5469L, 0xDF21F9DAE98D5C86L, 0xA22573A5D741ACECL,
            0x722CB87504029D8AL, 0x5727EA9D310F90F7L, 0x06D1E7DC6CF5C689L, 0x735BAEB75FDD9F85L, 0xEF96C3AF03785BEAL, 0xBE453FC733BEFA00L, 0xE27E2672BFCC1C44L, 0x541C5523E0FBB038L,
            0xA04B840944E17E54L, 0x313CA18B6537B063L, 0xC7B93061D18C2FFEL, 0xE1D991D2E4A8CD20L, 0x5BB21B4ED59FAE91L, 0x7DB82C96F57D18C5L, 0x9EEBA39CBD611F6EL, 0x093E9402ABCC23F7L,
            0x9A7637252A4475F7L, 0x0C8A522F0B70DB19L, 0x3532D24B07A4D08BL, 0x633C908FA64BB58AL, 0x16A3123AD6B3DD79L, 0x1169BB0D6BD6DEC5L, 0xDABFB787CED62E83L, 0x8F17A15C52A3B9BDL,
            0xA2F3FA0F0F5F6FDFL, 0x95DA83EA34697FEFL, 0xFE1541E512CBAC77L, 0xE68287CDEB9302A5L, 0xB928A0223B695207L, 0x3F9D05B291DE5A8AL, 0x5E28B275895A2C79L, 0x8E9BD22FBFD57A6CL,
    };

    public final void setState(final int s) {
        stateA = startingA[s >>> 9 & 0x7F];
        for (int i = s & 0x1FF; i > 0; i--) {
            stateA *= 0x41C64E6BL;
            stateA = (stateA << 28 | stateA >>> 36);
        }
        stateB = startingB[s >>> 25];
        for (int i = s >>> 16 & 0x1FF; i > 0; i--) {
            stateB *= 0x9E3779B9L;
            stateB = (stateB << 37 | stateB >>> 27);
        }
    }

    public final int nextInt()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (int)((stateA = (a << 28 | a >>> 36)) ^ (stateB = (b << 37 | b >>> 27)));
    }
    @Override
    public final int next(final int bits)
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (int)((stateA = (a << 28 | a >>> 36)) ^ (stateB = (b << 37 | b >>> 27))) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        final long a = stateA * 0x41C64E6BL;
        final long b = stateB * 0x9E3779B9L;
        return (stateA = (a << 28 | a >>> 36)) ^ (stateB = (b << 37 | b >>> 27));
    }

    /**
     * Produces a copy of this Mover64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this Mover64RNG
     */
    @Override
    public Mover64RNG copy() {
        return new Mover64RNG(stateA, stateB);
    }

    /**
     * Gets the "A" part of the state; if this generator was set with {@link #Mover64RNG()}, {@link #Mover64RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "A" part of the state, an int
     */
    public long getStateA()
    {
        return stateA;
    }

    /**
     * Gets the "B" part of the state; if this generator was set with {@link #Mover64RNG()}, {@link #Mover64RNG(int)},
     * or {@link #setState(int)}, then this will be on the optimal subcycle, otherwise it may not be. 
     * @return the "B" part of the state, an int
     */
    public long getStateB()
    {
        return stateB;
    }
    /**
     * Sets the "A" part of the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateA any int
     */
    public void setStateA(final long stateA)
    {
        this.stateA = stateA;
    }

    /**
     * Sets the "B" part of the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #setState(int)} to guarantee a good subcycle.
     * @param stateB any int
     */
    public void setStateB(final long stateB)
    {
        this.stateB = stateB;
    }
    
    @Override
    public String toString() {
        return "Mover64RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mover64RNG mover64RNG = (Mover64RNG) o;

        return stateA == mover64RNG.stateA && stateB == mover64RNG.stateB;
    }

    @Override
    public int hashCode() { 
        long h = 31L * stateA + stateB;
        return (int)(h ^ h >>> 32);
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
//            //final int r = (Light32RNG.determine(20007 + c) & 0xFFFF)|1;
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
//        long stateA = 1, stateB = 1;
//        System.out.println("long[] startingA = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%016XL, ", stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateA *= 0x41C64E6BL;
//                stateA = (stateA << 28 | stateA >>> 36);
//            }
//        }
//        System.out.println("}, startingB = {");
//        for (int ctr = 0; ctr < 128; ctr++) {
//            System.out.printf("0x%016XL, ", stateB);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 512; i++) {
//                stateB *= 0x9E3779B9L;
//                stateB = (stateB << 37 | stateB >>> 27);
//            }
//        }
//        System.out.println("};");
//    }
}
