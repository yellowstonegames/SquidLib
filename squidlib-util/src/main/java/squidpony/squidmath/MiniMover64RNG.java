package squidpony.squidmath;

import squidpony.StringKit;

/**
 * The fastest generator in this library on desktop JVMs; one of Mark Overton's subcycle generators from
 * <a href="http://www.drdobbs.com/tools/229625477">this article</a>, specifically a CMR with a 64-bit state, that has
 * its result multiplied by a constant. Its period is unknown, but is at the very least 2 to the 42, since the generator
 * passes PractRand after generating that many 64-bit integers (it passes with two minor anomalies, and none at the end,
 * the 32TB mark). It probably won't pass many tests when the bits are reversed, so that is something to be aware of.
 * <br>
 * Notably, this generator's {@link #nextLong()} method is extremely small (as are all of the methods that use it as a
 * basis), which may help with inlining decisions for HotSpot. Generating the next step just needs a bitwise rotation of
 * the current state, multiplying the result by a 32-bit constant, and assigning that to state. Generating a long after
 * that only needs a multiplication by a 31-bit constant, which could be modified to allow this to pass tests when the
 * bits are reversed (presumably by appending one or more xorshift operations). The choice of constants for the
 * multipliers and for the rotation needs to be carefully verified; earlier choices came close to failing PractRand at
 * 8TB (and were worsening, so were likely to fail at 16TB), but this set of constants has higher quality in testing.
 * For transparency, the constants used are the state multiplier 0x9E3779B9L, which is 2 to the 32 divided by the golden
 * ratio, the post-processing multiplier 0x41C64E6DL, which was recommended in PractRand as a small LCG multiplier, and
 * a left rotation constant of 21, which was chosen because it is slightly smaller than 1/3 of 64, and that seems to
 * work well in a 64-bit CMR generator.
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. It skips at most 16712703 times into its core generator's cycle when seeding. It uses
 * constants to store 256 known midpoints for the generator, then skips an additional up-to-1023 times past that point.
 * Each midpoint is 65536 generations ahead of the previous midpoint. There are 2 to the 18 possible starting states for
 * this generator when using {@link #seed(int)}, but it is unknown if that method actually puts the generator in the
 * longest possible cycle, or just a sufficiently long one.
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed. This generator has less state than {@link Mover64RNG}, has a shorter period than
 * it, and is faster than it in all aspects except the time needed to {@link #seed(int)}.
 * <br>
 * Created by Tommy Ettinger on 11/26/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class MiniMover64RNG implements RandomnessSource {
    private long state;
    public MiniMover64RNG()
    {
        seed((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public MiniMover64RNG(final int state)
    {
        seed(state);
    }

    /**
     * Not advised for external use; prefer {@link #MiniMover64RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param state the state to use, exactly unless it is 0 (then this uses 1)
     */
    public MiniMover64RNG(final long state)
    {
        this.state = state == 0L ? 1L : state;
    }

    private static final long[] starting = {
            0x000000009E3779B9L, 0x3B8FC958F31EDA72L, 0xD65FCA34221AE79EL, 0xD1D0129A934029AEL, 0x76487440E53D6DF1L, 0x23A3513401A622B0L, 0x90190513B057E092L, 0xE71A9E0801045EDDL,
            0xCC8C18579C4F58F7L, 0x63F2C5AB11CE6A0DL, 0xD68A4E08B923DB48L, 0xAAB0DE27C8F2AF64L, 0x86CB87754B5642E8L, 0xDEFA30634C0B7577L, 0x43BE36F3C3B74F65L, 0x6B6FD89017A0A97DL,
            0x440B41A2A5E1C0D6L, 0xFFEB33C5B24EE830L, 0x26E53595E2280A26L, 0x12F2526D06CDF7FDL, 0xFEAFF92D0E5FF424L, 0xCD6D34166E5C15ABL, 0xFC3F2307ED6A5094L, 0xD19147F8E373B79CL,
            0x6B4385E08BD17503L, 0xE3307AF38B4B3F69L, 0x9E075AF9344F3172L, 0xE6ABC10084A3C64BL, 0x623E72CFA2124CE1L, 0x9619FFDC7741150EL, 0x35BBB499321FB17CL, 0x547D93E6EABAE288L,
            0xB1BB900999391989L, 0xF2B6A36E2B74ABFDL, 0xB1554E2018BBBDCEL, 0xC7D2949E15B0F435L, 0xB6CE7BB5CB4B85D3L, 0x89D8EF6444C21B72L, 0x807C2CDCCAA04650L, 0x508E97EC6F7E8200L,
            0x6E26B960CA372975L, 0xB18325F328CACED6L, 0x1BE26F268AAEE28DL, 0x7B9D09A322914A6AL, 0xB60D3754CA86E8AEL, 0x2BDDE7F688AAC807L, 0x4277DB3E755DF315L, 0x0D9D5C7376A318B0L,
            0x77B8AE1FCB50740AL, 0xE98BBFA83A0E1B98L, 0xFA7D325D5012FAF6L, 0xFBAFB6A750122E5CL, 0x2FC293145BF17259L, 0xAEE61EF2E2C1136EL, 0xD843DA31FA30166AL, 0xCB5439714BE182A0L,
            0x2BF7D169AA3FCB21L, 0x6A32949AFE26086AL, 0x0D2282637A5DE391L, 0xBCE4ABF63EAD1D03L, 0xEC5A1E315AC21C06L, 0xEE78DE4B0817F57DL, 0x60D8A7563EF1477AL, 0x911E18BF0D40144BL,
            0xA3DD993DA75E5585L, 0x76CACC2F1D980C23L, 0x13B08273152A6CF4L, 0x412B694F86A0FD74L, 0xE33E2FA2EC85334EL, 0xFBFDB2E43EAE4FDAL, 0x901685992003C139L, 0x25DB6A3A3F382B37L,
            0x7A3F9DCCAA9A31A1L, 0x42FECE662CC20318L, 0xE5830C50C07AAC70L, 0x1BFFD4DA173ABA99L, 0xC7360E3B7192B2C7L, 0x741470808ED902F3L, 0xC92962DDDFEA8FFAL, 0x055D06BDE448E8C2L,
            0xE0A7CC888C02BE8FL, 0x8E7E0790DD8E07EAL, 0x826B0553895B1AAFL, 0xE8C58BA7AAA58FFEL, 0xCF7151A441189F5FL, 0x3A7A1CBB4E879FA7L, 0xD72EEC8A7EFCBBA3L, 0x6B8C77A8954D0BF7L,
            0x73F34FE8FB0D8A9BL, 0x6AE297552616C873L, 0x2C4BFD618875BD73L, 0xE98E66DEFEB78A21L, 0xCB834E733E709C50L, 0x8549E7EE0A84CDC2L, 0x2B6E96B9372049C8L, 0x815706308CBE0965L,
            0x0744B5093B9CABBAL, 0x8DCF6EA1903504E7L, 0x132A796A48ADFCA5L, 0x9F8F27C1E909C058L, 0x2CEE06E6EE1F5464L, 0x4D62EB09818C8F28L, 0x227FE2C3F1D602BFL, 0x3E27033C9D492A2DL,
            0xBDA74A65A3510F69L, 0x9EDA095387235CE8L, 0xD4E4C6B09EB5236DL, 0x4BD5574FDFEF003DL, 0x44E862A20801F72FL, 0x27E6EFE688B4C83EL, 0xC9E8B6E36B64B351L, 0x94F8B89DD9D95A23L,
            0x52BCD222D63A833BL, 0x7D4E109CBCAC447BL, 0x205C5D9B41D4B536L, 0x2DEE86E015A01563L, 0xD50E141D4E626F0EL, 0x784CF0A2A7DF0F2CL, 0xB415A9891FB8D999L, 0xF55E85E3D31FFEAAL,
            0x2C8273E565F8596CL, 0x3EDCC59F02D0AD91L, 0x24FBE3D4820DF713L, 0x18D9A3DA2219387EL, 0xCF1F6C734E66C1F5L, 0x277967EBA92FADA7L, 0x004D1C3CE5DB911BL, 0xBB24BA3C8385C723L,
            0x8D5875128360F2A7L, 0xA9C6C4DF9FCCCA06L, 0x6FECA1183DDF2208L, 0xEC6A1B4526680673L, 0x1F30BD544B870B14L, 0xC8CEF1B3296A340FL, 0xD0D83FE58A46B927L, 0x918D0D521351E66EL,
            0x365B57385CD83843L, 0x3801138F4D2D36DFL, 0x3FBB9D893B0D2343L, 0x11F44781EA77AD00L, 0x43DCD114D96729FDL, 0x3BD4DCCB594B6200L, 0xA24A9B213A09B058L, 0x09A3E7F0BE142BB1L,
            0x90236C4D945599B7L, 0x2BA8A0608D0417ECL, 0xC24FEF571FC782DFL, 0x8DC625863607028BL, 0xBEDA2DBFB8A84987L, 0x915D2C8925A72E05L, 0xDBE8B345156E5C33L, 0xB699F01B3A7135B8L,
            0x73E82A7E2DBE8CF5L, 0xEC0348F876389FB5L, 0x4A0B54EA2D49A245L, 0x338791CF4309ED1CL, 0xC600944F8239852CL, 0x54E2A687D4995C26L, 0x7C9E29BADBED71CBL, 0x4094B0FF0339D075L,
            0xF0204249A6058FB0L, 0x6541EE890DA7F33BL, 0xF2FB714B53B6540CL, 0x3299C9024579D55BL, 0x3AE82580C2CB344DL, 0x62281AC6A4C19184L, 0x523711B215B65DA7L, 0xD2232BC66BAEBF8CL,
            0xF05868D81D54CADFL, 0xF8C670CBFE46DAB5L, 0x60F49E5FA3A30680L, 0x33BED3AE1CA5FA89L, 0x0B9577229DA48E79L, 0x5586CC9E612E7655L, 0x31272E47B47F8AF2L, 0x529A3EC1636A9FC0L,
            0x90501EF07A4EBFADL, 0xAEFF651E4A047F52L, 0xF160B66AEA6603BDL, 0x9BC156E2775665D1L, 0xD3C898E787E6ADBCL, 0x393F6D6B6B4AC94DL, 0x9795EE44CBA5E935L, 0x676232369E68CA73L,
            0x516FC161A97658B9L, 0x7243223BD7B8812AL, 0x4061A73DE3AC1EB6L, 0x87589DE0B4CBA6B6L, 0x17F2D34487B117AFL, 0x5E7786C984DEDF4BL, 0xD30DF191B5B0923DL, 0xA49791249D2684B3L,
            0x1A0CBCFB5BE5AB5CL, 0x7628D1C1EB51C2F2L, 0x50168CC527A39AF8L, 0x697CB60CF8F9322BL, 0x48AFF611B2E10946L, 0xA3858FD4EA18E2B6L, 0xEFD20E4CEF2A65F5L, 0xDB1CED0A445A293FL,
            0xFDF61684CC927D67L, 0xDC6376DBBB5D3BFCL, 0xD09BFB2F6F8D6CDFL, 0x1E84842524902A05L, 0xBD92B8C37F9EE46DL, 0x06F7A007ED5C2ED6L, 0x82EE75DF651DCCA7L, 0x9F2D4ED71D6180EEL,
            0xA0D3DCD3B8AE1150L, 0x7D17C26D2C397B06L, 0x8C0720297799B091L, 0x71070EF8E8D38300L, 0xBC0430BB90CF3145L, 0xEA523D477537C90CL, 0xD42D8753251C0BD3L, 0xF0967F3D663603D9L,
            0x6A724E528FA997CFL, 0xA476827F739B7100L, 0xFB4848834480191AL, 0xC9D2E5724A0AE765L, 0xD220F37EB7FD56A5L, 0xC0986CDDC6A4613AL, 0x4B70F47719C3D8D8L, 0x87AD1A0426B0BE6EL,
            0x0753E68807C62CB5L, 0x99BA97040B35F7BFL, 0xB278C34645360313L, 0x2A3BB3439F729DFDL, 0x1BA7FEDEFE42E878L, 0xE689C25E229FF893L, 0x5A886195FB13BB22L, 0x16B17F955E2C9A76L,
            0x40AF38E5B5413AC6L, 0xA2F26EB7E9A4949FL, 0xAFE07155EE2C5FCDL, 0x46F6C31C27832F3EL, 0x82E6051363F4CC9DL, 0x3D2B03828EAB31D3L, 0x126CA811A46BF03BL, 0x69E8E26BDF6409F1L,
            0xF9E2985426D876ADL, 0x008CA76E0541E8E0L, 0xC27A4485B397F943L, 0x85EB66C4C81E90DFL, 0x44FEB62F172BF3E0L, 0x238A7B993CE155BEL, 0x988BF9ECF8F36F59L, 0x3A58DA6AB00C876AL,
            0x74348C5D00115985L, 0xD0291A87BAA4649EL, 0x7A36C1D79F4FC33BL, 0x28548F67E91EA20FL, 0xB01469B524F79B6EL, 0x67AEEBA546D67C35L, 0x30E10DE9E3BABCCBL, 0x45F9DC2FC4B25D03L,
    };

    /**
     * Seeds the state using 18 bits of the given int {@code s}. Although 262144 seeds are possible, this will only
     * generate a new state at most 1023 times, and each generated state takes less time than {@link #nextLong()}.
     * Giving this method sequential values for s will be guaranteed to produce larger distances between the sequences
     * produced by those states; the worst-case is when s values are separated by exactly 256. For most values of s and
     * s + 1, the sequence starting with s + 1 is the same as the sequence starting with s after it generates 65536 long
     * values. However, the sequence starting with s + 256 is the same as the sequence starting with s after it
     * generates 1 long value.
     * @param s only 18 bits are used (values 0 to 262143 inclusive will all have different results).
     */
    public final void seed(final int s) {
        long v = starting[s & 0xFF];
        for (int i = s >>> 8 & 0x3FF; i > 0; i--) {
            v = (v << 21 | v >>> 43) * 0x9E3779B9L;
        }
        state = v;
    }

    public final int nextInt()
    {
        return (int)((state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 0x41C64E6DL);
    }
    @Override
    public final int next(final int bits)
    {
        return (int)((state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 0x41C64E6DL) >>> (32 - bits);
    }
    @Override
    public final long nextLong()
    {
        return (state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 0x41C64E6DL;
    }

    /**
     * Produces a copy of this MiniMover64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this MiniMover64RNG
     */
    @Override
    public MiniMover64RNG copy() {
        return new MiniMover64RNG(state);
    }

    /**
     * Gets the state; if this generator was set with {@link #MiniMover64RNG()},
     * {@link #MiniMover64RNG(int)}, or {@link #seed(int)}, then this will be on the optimal subcycle, otherwise it
     * may not be. 
     * @return the state, a long
     */
    public long getState()
    {
        return state;
    }

    /**
     * Sets the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #seed(int)} to guarantee a good subcycle.
     * @param state any int
     */
    public void setState(final long state)
    {
        this.state = state == 0L ? 1L : state;
    }
    
    @Override
    public String toString() {
        return "MiniMover64RNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiniMover64RNG miniMover64RNG = (MiniMover64RNG) o;

        return state == miniMover64RNG.state;
    }

    @Override
    public int hashCode() {
        return (int)(state ^ state >>> 32);
    }

//    public static void main(String[] args)
//    {
//        long stateA = 0x9E3779B9L;
//        System.out.println("long[] starting = {");
//        for (int ctr = 0; ctr < 256; ctr++) {
//            System.out.printf("0x%016XL, ", stateA);
//            if((ctr & 7) == 7)
//                System.out.println();
//            for (int i = 0; i < 0x10000; i++) {
//                stateA = (stateA << 21 | stateA >>> 43) * 0x9E3779B9L;
//            }
//        }
//        System.out.println("};");
//    }
}
