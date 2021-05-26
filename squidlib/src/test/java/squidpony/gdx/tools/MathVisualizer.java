package squidpony.gdx.tools;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 1/13/2018.
 */
public class MathVisualizer extends ApplicationAdapter {
    private int mode = 14;
    private int modes = 57;
    private FilterBatch batch;
    private SparseLayers layers;
    private InputAdapter input;
    private Stage stage;
    private int[] amounts = new int[512];
    private double[] dAmounts = new double[512];
    private DiverRNG diver;
    private MoonwalkRNG rng;
    private boolean hasGauss;
    private double followingGauss;
    private RandomBias bias;
    private RandomXS128 xs128;
    private XSP xsp;
    private EditRNG edit;
    private TweakRNG tweak;
    private long seed = 1L;
    private long startTime;
    private double[] circleCoord = new double[2];

    private double twist(double input) {
        return (input = input * 0.5 + 1.0) - (int)input;
    }
    private float twist(float input) {
        return (input = input * 0.5f + 1.0f) - (int)input;
    }
//    public static double planarDetermine(long base, final int index) {
//        return (((base = Long.reverse(base * index)) ^ base >>> 11) & 0x1fffffffffffffL) * 0x1p-53;
//    }

    public static double planarDetermine(long base, final int index) {
        final double mixed = NumberTools.setExponent((base * index >>> 12 ^ index) * 1.6180339887498948482, 0x400); // 2.0 to 4.0
        return NumberTools.setExponent(Math.pow((mixed + index), mixed * 2.6180339887498948482), 0x3ff) - 1.0;
//        return ((Long.rotateLeft((base *= index), 31) ^ Long.rotateLeft(base, 17) ^ Long.rotateLeft(base, 42)) >>> 11) * 0x1p-53;
    }

    public void insideBallRejection(final double[] vector)
    {
        double v1, v2, v3;
        do {
            v1 = 2 * diver.nextDouble() - 1; // between -1 and 1
            v2 = 2 * diver.nextDouble() - 1; // between -1 and 1
            v3 = 2 * diver.nextDouble() - 1; // between -1 and 1
        } while (v1 * v1 + v2 * v2 + v3 * v3 > 1);
        vector[0] = v1;
        vector[1] = v2;
    }
    public final float fastGaussian()
    {
        long a = diver.nextLong(), b = diver.nextLong();
        a = (a & 0x0003FF003FF003FFL) +     ((a & 0x0FFC00FFC00FFC00L) >>> 10);
        b = (b & 0x0003FF003FF003FFL) +     ((b & 0x0FFC00FFC00FFC00L) >>> 10);
        a = (a & 0x000000007FF007FFL) +     ((a & 0x0007FF0000000000L) >>> 40);
        b = (b & 0x000000007FF007FFL) +     ((b & 0x0007FF0000000000L) >>> 40);
        return (((a & 0x0000000000000FFFL) + ((a & 0x000000007FF00000L) >>> 20))
                - ((b & 0x0000000000000FFFL) + ((b & 0x000000007FF00000L) >>> 20))) * (0x1p-10f);
    }

    /*
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    /**
     * Returns the inverse error function result.
     * <p>
     * This implementation is described in the paper:
     * <a href="http://people.maths.ox.ac.uk/gilesm/files/gems_erfinv.pdf">Approximating
     * the erfinv function</a> by Mike Giles, Oxford-Man Institute of Quantitative Finance,
     * which was published in GPU Computing Gems, volume 2, 2010.
     * The source code is available <a href="http://gpucomputing.net/?q=node/1828">here</a>.
     * The code as it is here is from Apache Commons Math 3.
     * </p>
     * @param x the value
     * @return t such that x = erf(t)
     * @since 3.2
     */
    public static double erfInv(final double x) {

        // beware that the logarithm argument must be
        // commputed as (1.0 - x) * (1.0 + x),
        // it must NOT be simplified as 1.0 - x * x as this
        // would induce rounding errors near the boundaries +/-1
        double w = - Math.log((1.0 - x) * (1.0 + x));
        double p;

        if (w < 6.25) {
            w -= 3.125;
            p =  -3.6444120640178196996e-21;
            p =   -1.685059138182016589e-19 + p * w;
            p =   1.2858480715256400167e-18 + p * w;
            p =    1.115787767802518096e-17 + p * w;
            p =   -1.333171662854620906e-16 + p * w;
            p =   2.0972767875968561637e-17 + p * w;
            p =   6.6376381343583238325e-15 + p * w;
            p =  -4.0545662729752068639e-14 + p * w;
            p =  -8.1519341976054721522e-14 + p * w;
            p =   2.6335093153082322977e-12 + p * w;
            p =  -1.2975133253453532498e-11 + p * w;
            p =  -5.4154120542946279317e-11 + p * w;
            p =    1.051212273321532285e-09 + p * w;
            p =  -4.1126339803469836976e-09 + p * w;
            p =  -2.9070369957882005086e-08 + p * w;
            p =   4.2347877827932403518e-07 + p * w;
            p =  -1.3654692000834678645e-06 + p * w;
            p =  -1.3882523362786468719e-05 + p * w;
            p =    0.0001867342080340571352 + p * w;
            p =  -0.00074070253416626697512 + p * w;
            p =   -0.0060336708714301490533 + p * w;
            p =      0.24015818242558961693 + p * w;
            p =       1.6536545626831027356 + p * w;
        } else if (w < 16.0) {
            w = Math.sqrt(w) - 3.25;
            p =   2.2137376921775787049e-09;
            p =   9.0756561938885390979e-08 + p * w;
            p =  -2.7517406297064545428e-07 + p * w;
            p =   1.8239629214389227755e-08 + p * w;
            p =   1.5027403968909827627e-06 + p * w;
            p =   -4.013867526981545969e-06 + p * w;
            p =   2.9234449089955446044e-06 + p * w;
            p =   1.2475304481671778723e-05 + p * w;
            p =  -4.7318229009055733981e-05 + p * w;
            p =   6.8284851459573175448e-05 + p * w;
            p =   2.4031110387097893999e-05 + p * w;
            p =   -0.0003550375203628474796 + p * w;
            p =   0.00095328937973738049703 + p * w;
            p =   -0.0016882755560235047313 + p * w;
            p =    0.0024914420961078508066 + p * w;
            p =   -0.0037512085075692412107 + p * w;
            p =     0.005370914553590063617 + p * w;
            p =       1.0052589676941592334 + p * w;
            p =       3.0838856104922207635 + p * w;
        } else if (!Double.isInfinite(w)) {
            w = Math.sqrt(w) - 5.0;
            p =  -2.7109920616438573243e-11;
            p =  -2.5556418169965252055e-10 + p * w;
            p =   1.5076572693500548083e-09 + p * w;
            p =  -3.7894654401267369937e-09 + p * w;
            p =   7.6157012080783393804e-09 + p * w;
            p =  -1.4960026627149240478e-08 + p * w;
            p =   2.9147953450901080826e-08 + p * w;
            p =  -6.7711997758452339498e-08 + p * w;
            p =   2.2900482228026654717e-07 + p * w;
            p =  -9.9298272942317002539e-07 + p * w;
            p =   4.5260625972231537039e-06 + p * w;
            p =  -1.9681778105531670567e-05 + p * w;
            p =   7.5995277030017761139e-05 + p * w;
            p =  -0.00021503011930044477347 + p * w;
            p =  -0.00013871931833623122026 + p * w;
            p =       1.0103004648645343977 + p * w;
            p =       4.8499064014085844221 + p * w;
        } else {
            // this branch does not appears in the original code, it
            // was added because the previous branch does not handle
            // x = +/-1 correctly. In this case, w is positive infinity
            // and as the first coefficient (-2.71e-11) is negative.
            // Once the first multiplication is done, p becomes negative
            // infinity and remains so throughout the polynomial evaluation.
            // So the branch above incorrectly returns negative infinity
            // instead of the correct positive infinity.
            p = Double.POSITIVE_INFINITY;
        }

        return p * x;
    }
    
    
    public double erfGaussian(){
        return erfInv(rng.nextDouble() * 2.0 - 1.0);
    }
    
    public final float editedCurve()
    {
        long r = diver.nextLong(), s = diver.nextLong();
        return ((r >>> 56) - (r >>> 48 & 255) + (r >>> 40 & 255) - (r >>> 32 & 255) + (r >>> 24 & 255) - (r >>> 16 & 255) + (r >>> 8 & 255) - (r & 255)) * 0x1p-8f
                + ((s >>> 48) - (s >>> 32 & 65535) + (s >>> 16 & 65535) - (s & 65535)) * 0x1p-16f;
//        final long r = diver.nextLong(), s = diver.nextLong();
//        return (((r & 0xFFFFFFL) + (r >>> 40)) * 0x1p-25f + (1.0f - ((s & 0xFFFFFFL) * 0x1p-24f) * ((s >>> 40) * 0x1p-24f))) * 0.5f;

//        return 0.1f * (diver.nextFloat() + diver.nextFloat() + diver.nextFloat()
//                + diver.nextFloat() + diver.nextFloat() + diver.nextFloat())
//                + 0.2f * ((1f - diver.nextFloat() * diver.nextFloat()) + (1f - diver.nextFloat() * diver.nextFloat()));
        
//                - (s & 0xFFFFFFL) - (r >>> 20 & 0xFFFFFFL) - (s >>> 26 & 0xFFFFFFL) - (t >>> 40) - (t >>> 13 & 0xFFFFFFL)
//        return  ((r & 0xFFFFFFL) + (r >>> 20 & 0xFFFFFFL) + (s >>> 40)
//                - (s & 0xFFFFFFL) - (s >>> 20 & 0xFFFFFFL) - (r >>> 40)
//        ) * 0x1p-26f;
//        return ((r & 0xFFFFFFL) * 0x1p-23f - ((s & 0xFFFFFFL) * 0x1p-23f)) * ((r >> 40) * 0x1p-23f) * ((s >> 40) * 0x1p-23f);
    }
    public final double nextGaussianBasicBoxMuller() {
        if (hasGauss = !hasGauss) {
            final double u1 = Math.sqrt(-2.0 * Math.log(1.0 - rng.nextDouble())), u2 = rng.nextDouble();
            followingGauss = u1 * NumberTools.sin_(u2);
            return u1 * NumberTools.cos_(u2);
        }
        else return followingGauss;
    }
    public final double nextGaussian() {
        if (hasGauss = !hasGauss) {
            double v1, v2, s;
            do {
                v1 = 2 * rng.nextDouble() - 1; // between -1 and 1
                v2 = 2 * rng.nextDouble() - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s > 1 || s == 0);
            final double multiplier = Math.sqrt(-2 * Math.log(s) / s);
            followingGauss = v2 * multiplier;
            return v1 * multiplier;
        }
        else return followingGauss;
    }

    /**
     * <a href="https://marc-b-reynolds.github.io/distribution/2021/03/18/CheapGaussianApprox.html">Credit to Marc B.
     * Reynolds</a>. The fairly-weird method to only have to sample one random number is mine, but could certainly be
     * improved by anyone who has the time to test the range on different XOR constants or multipliers.
     * @return a normal-distributed double with standard deviation 1.0, actually ranging from -7.881621123730187 to 7.7975656902966115
     */
    private double nextGaussianCountX(){
		final long u1 = rng.nextLong();
        return 0x1.fb760cp-35 * ((Long.bitCount(u1 * 0xC6BC279692B5C323L ^ 0xC6AC29E5C6AC29E5L) - 32L << 32) + (u1 & 0xFFFFFFFFL) - (u1 >>> 32));
	}

    private double nextGaussianCountDual(){
		final long u1 = rng.nextLong();
		return 0x1.fb760cp-35 * ((Long.bitCount(rng.nextLong()) - 32L << 32) + (u1 & 0xFFFFFFFFL) - (u1 >>> 32));
	}


    public void insideBallBoxMuller(final double[] vector)
    {
        double mag = 0.0;
        double v1, v2, v3, s;
        do {
            v1 = 2 * diver.nextDouble() - 1; // between -1 and 1
            v2 = 2 * diver.nextDouble() - 1; // between -1 and 1
            v3 = 2 * diver.nextDouble() - 1; // between -1 and 1
            s = v1 * v1 + v2 * v2 + v3 * v3;
        } while (s > 1 || s == 0);
        double multiplier = Math.sqrt(-2 * Math.log(s) / s);
        mag += (vector[0] = (v1 *= multiplier)) * (v1);
        mag += (vector[1] = (v2 *= multiplier)) * (v2);
        mag += (v3 *= multiplier) * (v3);
        if(mag == 0.0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.cbrt(diver.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }
    public void insideBallBoxMullerFast(final double[] vector)
    {
        double v1 = fastGaussian(), v2 = fastGaussian(), v3 = fastGaussian();
        double mag = (vector[0] = v1) * v1 + (vector[1] = v2) * v2 + v3 * v3;
        if(mag == 0.0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.cbrt(diver.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }
    public void insideBallExponential(final double[] vector)
    {
        double v1 = nextGaussian(), v2 = nextGaussian(), v3 = nextGaussian();
        double mag = v1 * v1 + v2 * v2 + v3 * v3 - 2.0 * Math.log(diver.nextDouble());
        if(mag == 0.0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = 1.0 / Math.sqrt(mag);
        vector[0] = v1 * mag;
        vector[1] = v2 * mag;
    }
    public void insideBallExponentialFast(final double[] vector) {
        double v1 = fastGaussian(), v2 = fastGaussian(), v3 = fastGaussian();//, sq = diver.nextDouble() * diver.nextDouble();
//        double mag = v1 * v1 + v2 * v2 + v3 * v3 + 1.0 / (1.0 - diver.nextDouble() * diver.nextDouble()) - 0.25;
        double mag = v1 * v1 + v2 * v2 + v3 * v3 - 2.0 * Math.log(diver.nextDouble());
        if (mag == 0.0) {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        } else
            mag = 1.0 / Math.sqrt(mag);
        //if (Math.abs(v3 * mag) < 0.1) 
        {
            vector[0] = v1 * mag;
            vector[1] = v2 * mag;
        }
    }
    public void onSphereTrig(final double[] vector)
    {
        double theta = diver.nextDouble();
        double d = diver.nextDouble();
        double phi = NumberTools.acos_(d * 2.0 - 1.0);
        double sinPhi = NumberTools.sin_(phi);

        vector[0] = NumberTools.cos_(theta) * sinPhi;
        vector[1] = NumberTools.sin_(theta) * sinPhi;
        //vector[2] = NumberTools.cos_(phi);
    }
    public void insideCircleBoxMuller(final double[] vector)
    {
        double mag = 0.0;
        double v1, v2, s;
        do {
            v1 = 2 * diver.nextDouble() - 1; // between -1 and 1
            v2 = 2 * diver.nextDouble() - 1; // between -1 and 1
            s = v1 * v1 + v2 * v2;
        } while (s > 1 || s == 0);
        double multiplier = Math.sqrt(-2 * Math.log(s) / s);
        mag += (vector[0] = (v1 *= multiplier)) * (v1);
        mag += (vector[1] = (v2 *= multiplier)) * (v2);
        //mag += -2.0 * Math.log(diver.nextDouble());
        if(mag == 0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.sqrt(diver.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }
    public void insideCircleBoxMullerFast(final double[] vector)
    {
        double mag = 0.0;
        double v1 = fastGaussian(), v2 = fastGaussian();
        mag += (vector[0] = v1) * v1;
        mag += (vector[1] = v2) * v2;
        if(mag == 0)
        {
            vector[0] = 0.0;
            vector[1] = 0.0;
            return;
        }
        else
            mag = Math.sqrt(diver.nextDouble()) / Math.sqrt(mag);
        vector[0] *= mag;
        vector[1] *= mag;
    }

    private static class XSP {
        private long state0;
        public XSP()
        {
            state0 = (long) ((Math.random() - 0.5) * 0x10000000000000L)
                    ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L);
        }
        public long nextLong()
        {
//            long s1 = this.state0;
//            final long s0 = this.state1;
//            this.state0 = s0;
//            s1 ^= s1 << 23;
//            return (this.state1 = (s1 ^ s0 ^ (s1 >>> 17) ^ (s0 >>> 26))) + s0;

            long z = (state0 = state0 * 0x41C64E6DL + 1L);
            z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
            return (z ^ z >>> 25);

//            final long s0 = state0;
//            long s1 = state1;
//            final long result = s0 + s1;
//            s1 ^= s0;
//            state0 = (s0 << 55 | s0 >>> 9) ^ s1 ^ (s1 << 14);
//            state1 = (s1 << 36 | s1 >>> 28);
//            return (result << 23 | result >>> 41) + s0;

        }
        public long nextLong (final long n) {
            if (n <= 0) return 0L;
            for (;;) {
                final long bits = nextLong() >>> 1;
                final long value = bits % n;
                if (bits - value + n - 1L >= 0L) return value;
            }
        }
        public long nextLongMult(long bound)
        {
            long rand = nextLong();
            if (bound <= 0) return 0;
            final long randLow = rand & 0xFFFFFFFFL;
            final long boundLow = bound & 0xFFFFFFFFL;
            rand >>>= 32;
            bound >>>= 32;
            final long z = (randLow * boundLow >>> 32);
            long t = rand * boundLow + z;
            final long tLow = t & 0xFFFFFFFFL;
            t >>>= 32;
            return rand * bound + t + (tLow + randLow * bound >> 32);
        }
        public long nextLongBit(long bound)
        {
            final long mask = -1L >>> Long.numberOfLeadingZeros(--bound|1L);
            long x;
            do {
                x = nextLong() & mask;
            } while (x > bound);
            return x;

        }
    }
//    private final XoRoRNG r0 = new XoRoRNG(1234567890L),
//            r1 = new XoRoRNG(1234567890L),
//            r2 = new XoRoRNG(1234567890L);
//    public final long mult128(long bound)
//    {
//        long rand = r0.nextLong();
//        if (bound <= 0) return 0;
//        final long randLow = rand & 0xFFFFFFFFL;
//        final long boundLow = bound & 0xFFFFFFFFL;
//        rand >>>= 32;
//        bound >>>= 32;
//        final long z = (randLow * boundLow >>> 32);
//        long t = rand * boundLow + z;
//        final long tLow = t & 0xFFFFFFFFL;
//        t >>>= 32;
//        return rand * bound + t + (tLow + randLow * bound >> 32);
//    }
//    public final long bitmask(long bound)
//    {
//        final long mask = -1L >>> Long.numberOfLeadingZeros(--bound|1L);
//        long x;
//        do {
//            x = r2.nextLong() & mask;
//        } while (x > bound);
//        return x;
//    }
//    public final long traditional(final long bound)
//    {
//        if (bound <= 0) return 0L;
//        for (;;) {
//            final long bits = r1.nextLong() & 0x7FFFFFFFFFFFFFFFL;
//            final long value = bits % bound;
//            if (bits - value + bound - 1L >= 0L) return value;
//        }
//    }

    public static int determinePositive16(final int state)
    {
        return state >>> 1 ^ (-(state & 1) & 0xB400);
    }


    @Override
    public void create() {
        startTime = TimeUtils.millis();
        Coord.expandPoolTo(512, 512);
        diver = new DiverRNG(1234567890L);
        rng = new MoonwalkRNG(1234567890);
        seed = DiverRNG.determine(12345L);
        bias = new RandomBias();
        edit = new EditRNG();
        xs128 = new RandomXS128();
        xsp = new XSP();
        tweak = new TweakRNG();
        batch = new FilterBatch();
        stage = new Stage(new StretchViewport(512, 520), batch);
        layers = new SparseLayers(512, 520, 1, 1, new TextCellFactory().includedFont());
        layers.setDefaultForeground(SColor.WHITE);
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if(keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER)
                {
                    mode = (mode + 1) % modes;
                    System.out.println("Changed to mode " + mode);
                    update();
                    return true;
                } else if(keycode == Input.Keys.MINUS || keycode == Input.Keys.BACKSPACE)
                {
                    mode = (mode + modes - 1) % modes;
                    System.out.println("Changed to mode " + mode);
                    update();
                    return true;
                }

                return false;
            }
        };
        Gdx.input.setInputProcessor(input);
        stage.addActor(layers);
        update();
    }

    public void update() {
        Arrays.fill(amounts, 0);
        Arrays.fill(dAmounts, 0.0);
        ArrayTools.fill(layers.backgrounds, 0f);
        switch (mode) {
            case 0: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(NumberTools.formCurvedFloat(diver.nextLong()) * 256 + 256)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 1: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(twist(NumberTools.formCurvedFloat(diver.nextLong())) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 2: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(twist(NumberTools.formDouble(diver.nextLong()) *
                            NumberTools.formDouble(diver.nextLong()) - NumberTools.formDouble(diver.nextLong()) *
                            NumberTools.formDouble(diver.nextLong())) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 3: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
                long state;
                for (int i = 0; i < 0x1000000; i++) {
                    state = diver.nextLong();
                    amounts[Noise.fastFloor((NumberTools.formFloat((int) state) * 0.5 +
                            (NumberTools.formFloat((int) (state >>> 20)) + NumberTools.formFloat((int) (state >>> 41))) * 0.25) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 4: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                bias.distribution = RandomBias.EXP_TRI;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(bias.biasedDouble(0.6, 512.0))]++;
                    //amounts[Noise.fastFloor(Math.nextAfter(random.biasedDouble(0.5, 512.0), 0.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 5: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                bias.distribution = RandomBias.BATHTUB_TRUNCATED;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(bias.biasedDouble(0.6, 512.0))]++;
                    //amounts[Noise.fastFloor(Math.nextAfter(random.biasedDouble(0.5, 512.0), 0.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = Math.max(0, 519 - (amounts[i] >> 8)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 6: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                edit.setCentrality(200);
                edit.setExpected(0.75);
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(edit.nextDouble(512.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 7: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                edit.setCentrality(-200);
                edit.setExpected(0.75);
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(edit.nextDouble(512.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 8: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
//                random.setCentrality(50);
//                random.setExpected(0.6);
                long centrality = NumberTools.doubleToLongBits(1.625) & 0xfffffffffffffL;
                double offset = 0.15, range = 0.6;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(((diver.nextLong() & 0xfffffffffffffL) * 0x1p-54 + offset + range * ((centrality > (diver.nextLong() & 0xfffffffffffffL) ?
                            ((diver.nextLong() & 0xfffffffffffffL) - (diver.nextLong() & 0xfffffffffffffL)) * 0x1p-53 + 0.5 :
                            twist(((diver.nextLong() & 0xfffffffffffffL) - (diver.nextLong() & 0xfffffffffffffL)) * 0x1p-52)))) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 9: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
//                random.setCentrality(50);
//                random.setExpected(0.6);
                long centrality = NumberTools.doubleToLongBits(1.375) & 0xfffffffffffffL;
                double offset = 0.15, range = 0.6;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(((diver.nextLong() & 0xfffffffffffffL) * 0x1p-54 + offset + range * ((centrality > (diver.nextLong() & 0xfffffffffffffL) ?
                            ((diver.nextLong() & 0xfffffffffffffL) - (diver.nextLong() & 0xfffffffffffffL)) * 0x1p-53 + 0.5 :
                            twist(((diver.nextLong() & 0xfffffffffffffL) - (diver.nextLong() & 0xfffffffffffffL)) * 0x1p-52)))) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 10: {
                float s = (float) Math.exp(Math.sin((System.currentTimeMillis() & 0xFFFF) * 0x1p-9) * 2);
                float t = 0.5f;
                Gdx.graphics.setTitle("s=" + s + ", t=" + t);
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x100000; i++) {
                    amounts[(int) (MathExtras.barronSpline(diver.nextFloat(), s, t) * 511.999)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 5)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 11: {
                float s = 8;
                float t = (float) Math.sin((System.currentTimeMillis() & 0xFFFF) * 0x1p-9) * 0.5f + 0.5f;
                Gdx.graphics.setTitle("s=" + s + ", t=" + t);
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x100000; i++) {
                    amounts[(int) (MathExtras.barronSpline(diver.nextFloat(), s, t) * 511.999)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 5)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;

//            case 10: {
//                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
//                //DiverRNG diver = new DiverRNG();
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) (diver.nextFloat() * 512)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
//            }
//            break;
//            case 11: {
//                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) (MathUtils.random() * 512)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
//            }
//            break;
            case 12: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128.nextFloat() * 512");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) (xs128.nextFloat() * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 13: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " DiverRNG, random.nextInt(0x200)");
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[diver.nextInt(512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 14: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " nextGaussian()");
                for (int i = 0; i < 0x500000; i++) {
                    double d = nextGaussian() * 64.0 + 256.0;
                    if (d >= 0 && d < 512)
                        amounts[(int) d]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 7)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " XSP, mult128 & 0x1FFL");
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) ((xsp.nextLongMult(0x1800000000000000L)) & 0x1FFL)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " MathUtils.random(0x1FF)");
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[MathUtils.random(0x1FF)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
            }
            break;
            case 15: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " probit");
                for (int i = 0; i < 0x500000; i++) {
                    double d = MathExtras.probit(nextExclusiveDouble()) * 64.0 + 256.0;
                    if (d >= 0 && d < 512)
                        amounts[(int) d]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 7)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " GaussianDistribution, clamped [-4,4]");
//                for (int i = 0; i < 0x100000; i++) {
//                    amounts[Noise.fastFloor(MathUtils.clamp(GaussianDistribution.instance.nextDouble(rng), -0x3.FCp0, 0x3.FCp0) * 64 + 256)]++;
////                    amounts[MathUtils.round(MathUtils.clamp(fastGaussian() * 64f + 256f, 0f, 511f))]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = Math.max(0, 519 - (amounts[i] >> 5)); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }

            }
            break;
            case 16: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " nextGaussianCountX()");
                for (int i = 0; i < 0x500000; i++) {
                    double d = nextGaussianCountX() * 64.0 + 256.0;
                    if (d >= 0 && d < 512)
                        amounts[(int) d]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 7)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " probit cubed");
//                for (int i = 0; i < 0x40000; i++) {
//                    double d = Math.cbrt(MathExtras.probit(nextExclusiveDouble()));
//                    d = d * 64.0 + 256.0;
//                    if (d >= 0 && d < 512)
//                        amounts[(int) d]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = Math.max(0, 519 - (amounts[i] >> 3)); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " editedCurve");
//                for (int i = 0; i < 0x50000; i++) {
//                    float f = editedCurve() * 64f + 256f;
//                    if(f >= 0 && f < 512)
//                        amounts[MathUtils.floor(f)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = Math.max(0, 519 - (amounts[i] >> 3)); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " XSP, traditional & 0x1FFL");
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) ((xsp.nextLong(0x1800000000000000L)) & 0x1FFL)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " DiverRNG, random.nextLong(0x1000000000000000L) & 0x1FFL");
//                //DiverRNG diver = new DiverRNG();
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) ((random.nextLong(0x1000000000000000L)) & 0x1FFL)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
            }
            break;
            case 17: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " nextGaussianCountDual()");
                for (int i = 0; i < 0x500000; i++) {
                    double d = nextGaussianCountDual() * 64.0 + 256.0;
                    if (d >= 0 && d < 512)
                        amounts[(int) d]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 7)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }

//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " MathUtils.random(0x0FFFFFFFFFFFFFFFL) & 0x1FFL // UH OH");
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) (MathUtils.random(0x0FFFFFFFFFFFFFFFL) & 0x1FFL)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = Math.max(0, 519 - (amounts[i] >> 8)); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
            }
            break;
            case 18: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, random.nextLong(0x1800000000000000L) & 0x1FFL");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((xs128.nextLong(0x1800000000000000L)) & 0x1FFL)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 19: {
                long size = (System.nanoTime() >>> 28 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Halton[striding 1](2,3) sequence, first " + size + " points");
                int x, y, a = 421;
                for (int i = 0; i < size; i++) {
                    //a = determinePositive16(a);
                    a++;
                    x = (int) (VanDerCorputQRNG.determine2(a) * 510) + 1;
                    y = (int) (VanDerCorputQRNG.determine(3, a) * 510) + 1;
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
                x = (int) (VanDerCorputQRNG.determine2(a) * 510) + 1;
                y = (int) (VanDerCorputQRNG.determine(3, a) * 510) + 1;
                layers.backgrounds[x + 1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y + 1] = -0x1.794bfep125F;
                layers.backgrounds[x - 1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y - 1] = -0x1.794bfep125F;

            }
            break;
            case 20: {
                long size = (System.nanoTime() >>> 28 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Halton[striding 1](2,39) sequence, first " + size + " points");
                int x, y, a = 421;
                for (int i = 0; i < size; i++) {
//                    a = determinePositive16(a);
                    a++;
                    x = (int) (VanDerCorputQRNG.determine2(a) * 510) + 1;
                    y = (int) (VanDerCorputQRNG.determine(39, a) * 510) + 1;
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
                x = (int) (VanDerCorputQRNG.determine2(a) * 510) + 1;
                y = (int) (VanDerCorputQRNG.determine(39, a) * 510) + 1;
                layers.backgrounds[x + 1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y + 1] = -0x1.794bfep125F;
                layers.backgrounds[x - 1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y - 1] = -0x1.794bfep125F;

            }
            break;
            // from http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // this works much better than I had previously found, since the LFSR I was using wasn't being advanced
            // correctly, and this caused a pattern in the output because there was a pattern in the input.
            // Notably, with very large indices this still doesn't get collisions, but Halton does.
            case 21: {
                long size = 0x1000L;//(System.nanoTime() >>> 28 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Roberts sequence, first " + size + " points");
                int x, y, a = 1;
//                double p, q;

                for (int i = 0; i < size; i++) {
                    //1.32471795724474602596 0.7548776662466927 0.5698402909980532
//                    a = determinePositive16(a);
                    a++;
                    x = (a * 0xC13FA9A9) >>> 23;
                    y = (a * 0x91E10DA5) >>> 23;

//                    p = 0.5 + a * 0.7548776662466927;
//                    q = 0.5 + a * 0.5698402909980532;
//                    x = (int) ((p - (int)p) * 512);
//                    y = (int) ((q - (int)q) * 512);
//                    a = GreasedRegion.disperseBits((int) (VanDerCorputQRNG.altDetermine(7L, i) * 0x40000));
//                    x = a & 0x1ff;
//                    y = a >>> 16 & 0x1ff;

//                    a = GreasedRegion.disperseBits((int)(VanDerCorputQRNG.altDetermine(7L, i) * 0x4000));
//                    x = a & 0x7f;
//                    y = a >>> 16 & 0x7f;
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
//                x = (a * 0xC13FA9A9) >>> 23;
//                y = (a * 0x91E10DA5) >>> 23;
//                for (int i = 0; i < 512; i++) {
//                    layers.backgrounds[i][y] = -0x1.794bfep125F;
//                }
//                for (int i = 0; i < 512; i++) {
//                    layers.backgrounds[x][i] = -0x1.794bfep125F;
//                }

//                layers.backgrounds[x+1][y] = -0x1.794bfep125F;
//                layers.backgrounds[x][y+1] = -0x1.794bfep125F;
//                layers.backgrounds[x-1][y] = -0x1.794bfep125F;
//                layers.backgrounds[x][y-1] = -0x1.794bfep125F;
            }
            break;
            case 22: {
                long size = 0x1000L;//(System.nanoTime() >>> 28 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Jittered Roberts sequence, first " + size + " points");
                int x, y, a = 1, z;
                for (int i = 0; i < size; i++) {
                    //1.32471795724474602596 0.7548776662466927 0.5698402909980532
                    //0x5320B74F 0xC13FA9A9 0x91E10DA5
                    //0x5320B74ECA44ADACL 0xC13FA9A902A6328FL 0x91E10DA5C79E7B1DL
                    // 0x9E3779B97F4A7C15L
                    //a = determinePositive16(a);
//                    x = (int) (0x8000000000000000L + a * 0xC13FA9A902A6328FL >>> 55);
//                    y = (int) (0x8000000000000000L + a * 0x91E10DA5C79E7B1DL >>> 55);
                    a++;
                    x = (a * 0xC13FA9A9);
                    y = (a * 0x91E10DA5);
                    z = (a * 0x9E3779B9);
                    x = (x >>> 23) + (z >>> 29 & 6) - (z >>> 25 & 6) & 511;
                    y = (y >>> 23) + (z >>> 27 & 6) - (z >>> 23 & 6) & 511;

//                    a = GreasedRegion.disperseBits((int) (VanDerCorputQRNG.altDetermine(7L, i) * 0x40000));
//                    x = a & 0x1ff;
//                    y = a >>> 16 & 0x1ff;

//                    a = GreasedRegion.disperseBits((int)(VanDerCorputQRNG.altDetermine(7L, i) * 0x4000));
//                    x = a & 0x7f;
//                    y = a >>> 16 & 0x7f;
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + a);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
                //0xD1B54A32D192ED03L, 0xABC98388FB8FAC03L, 0x8CB92BA72F3D8DD7L

//                x = (a * 0xC13FA9A9);
//                y = (a * 0x91E10DA5);
//                z = (a * 0x9E3779B9);
//                x = (x >>> 23) + (z >>> 29 & 6) - (z >>> 25 & 6) & 511;
//                y = (y >>> 23) + (z >>> 27 & 6) - (z >>> 23 & 6) & 511;
//                for (int i = 0; i < 512; i++) {
//                    layers.backgrounds[i][y] = -0x1.794bfep125F;
//                }
//                for (int i = 0; i < 512; i++) {
//                    layers.backgrounds[x][i] = -0x1.794bfep125F;
//                }


//                if(x < 511) layers.backgrounds[x+1][y] = -0x1.794bfep125F;
//                if(y < 511) layers.backgrounds[x][y+1] = -0x1.794bfep125F;
//                if(x > 0) layers.backgrounds[x-1][y] = -0x1.794bfep125F;
//                if(y > 0) layers.backgrounds[x][y-1] = -0x1.794bfep125F;

            }
            break;
            case 23: {
                long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
//                final long size = 128;
                Gdx.graphics.setTitle("Halton[Striding 1](ROOT2,ROOT3) sequence, first " + size + " points");
                int x, y, a = 421;
                double ROOT2 = Math.sqrt(2), ROOT3 = Math.sqrt(3);
                for (int i = 0; i < size; i++) {
                    a++;
                    x = (int) (vdc(ROOT2, a) * 510) + 1;
                    y = (int) (vdc(ROOT3, a) * 510) + 1;
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
                x = (int) (vdc(ROOT2, a) * 510) + 1;
                y = (int) (vdc(ROOT3, a) * 510) + 1;
                layers.backgrounds[x + 1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y + 1] = -0x1.794bfep125F;
                layers.backgrounds[x - 1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y - 1] = -0x1.794bfep125F;
            }
            break;

//            case 23: {
//                //long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
//                final long size = 128;
//                Gdx.graphics.setTitle("Haltoid(777) sequence, first " + size + " points");
//                //int a, x, y, p2 = 777 * 0x2C9277B5 | 1;
//                //int lfsr = 7;
//                // (lfsr = (lfsr >>> 1 ^ (-(lfsr & 1) & 0x3802))) // 0x20400 is 18-bit // 0xD008 is 16-bit // 0x3802 is 14-bit
//                int x, y;
//                Coord c;
//                for (int i = 0; i < size; i++) {
////                    a = GreasedRegion.disperseBits(Integer.reverse(p2 * (i + 1))); //^ 0xAC564B05
////                    x = a >>> 7 & 0x1ff;
////                    y = a >>> 23 & 0x1ff;
//                    c = VanDerCorputQRNG.haltoid(777, 510, 510, 1, 1, i);
//                    x = c.x;
//                    y = c.y;
////                    x = a >>> 9 & 0x7f;
////                    y = a >>> 25 & 0x7f;
////                    if(layers.backgrounds[x][y] != 0f)
////                    {
////                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
////                        System.out.println("Overlap on index " + i);
////                    }
////                    else
//                    final float color = SColor.floatGetHSV(i * 0x1p-7f, 1f - i * 0x0.3p-7f, 0.7f, 1f);
//                    layers.backgrounds[x][y] = color;
//                    layers.backgrounds[x + 1][y] = color;
//                    layers.backgrounds[x - 1][y] = color;
//                    layers.backgrounds[x][y + 1] = color;
//                    layers.backgrounds[x][y - 1] = color;
//                }
//            }
//            break;
            case 24: {
                Arrays.fill(amounts, 0);
                long ctr = (System.nanoTime() >>> 24), xx, yy;
                Gdx.graphics.setTitle("ClassicNoise 2D hash at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                for (int x = 0; x < 512; x++) {
                    xx = x + ctr;
                    for (int y = 0; y < 512; y++) {
                        yy = y + ctr;
                        //amounts[(int)((((seed = (((1234567L * (0x632BE59BD9B4E019L + (xx << 23))) ^ 0x9E3779B97F4A7C15L) * (0xC6BC279692B5CC83L + (yy << 23)))) ^ seed >>> 27 ^ xx + yy) * 0xAEF17502108EF2D9L)
                        //        >>> 55)]++;
                        amounts[HastyPointHash.hash256(xx, yy, seed)]++;
                        //amounts[((int) (((seed = 1234567L ^ 0xB4C4D * xx ^ 0xEE2C3 * yy) ^ seed >>> 13) * seed) >>> 24)]++;
                    }
                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i>>2] >> 4); j < 520; j++) {
//                        layers.backgrounds[i][j] = color;
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
                double[] angle = new double[2];
                int x, y;
                float color;
                for (int t = 0; t < 256; t++) {
//                    angle = SeededNoise.phiGrad2[t];
                    angle[0] = MathUtils.cosDeg(t * 1.40625f);
                    angle[1] = MathUtils.sinDeg(t * 1.40625f);
                    color = (t & 4) == 4
                            ? -0x1.c98066p126F
                            : -0x1.d08864p126F;
                    for (int j = amounts[t] >> 3 & -4; j >= 128; j -= 4) {
                        x = Noise.fastFloor(angle[0] * j + 260);
                        y = Noise.fastFloor(angle[1] * j + 260);
                        layers.backgrounds[x][y] = color;
                        layers.backgrounds[x + 1][y] = color;
                        layers.backgrounds[x - 1][y] = color;
                        layers.backgrounds[x][y + 1] = color;
                        layers.backgrounds[x][y - 1] = color;
//                        layers.backgrounds[x+1][y+1] = color;
//                        layers.backgrounds[x-1][y-1] = color;
//                        layers.backgrounds[x-1][y+1] = color;
//                        layers.backgrounds[x+1][y-1] = color;
                    }
                    for (int j = Math.min(amounts[t] >> 3 & -4, 128); j >= 32; j -= 4) {
                        x = Noise.fastFloor(angle[0] * j + 260);
                        y = Noise.fastFloor(angle[1] * j + 260);
                        layers.backgrounds[x][y] = color;
//                        layers.backgrounds[x+1][y] = color;
//                        layers.backgrounds[x-1][y] = color;
//                        layers.backgrounds[x][y+1] = color;
//                        layers.backgrounds[x][y-1] = color;
                    }
                }
            }
            break;
            case 25: {
                Gdx.graphics.setTitle("swayAngleRandomized() at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                float theta = NumberTools.swayAngleRandomized(9999L, TimeUtils.timeSinceMillis(startTime) * 0x3p-11f);
                float c = MathUtils.cos(theta), s = MathUtils.sin(theta);
                int x, y;
                float color;
                color = SColor.FLOAT_BLACK;
                for (int j = 150; j >= 1; j -= 4) {
                    x = Noise.fastFloor(c * j + 260);
                    y = Noise.fastFloor(s * j + 260);
                    layers.backgrounds[x][y] = color;
                    layers.backgrounds[x + 1][y] = color;
                    layers.backgrounds[x - 1][y] = color;
                    layers.backgrounds[x][y + 1] = color;
                    layers.backgrounds[x][y - 1] = color;
                    layers.backgrounds[x + 1][y + 1] = color;
                    layers.backgrounds[x - 1][y - 1] = color;
                    layers.backgrounds[x - 1][y + 1] = color;
                    layers.backgrounds[x + 1][y - 1] = color;
                }
//                for (int j = 128; j >= 32; j -= 4) {
//                    x = Noise.fastFloor(c * j + 260);
//                    y = Noise.fastFloor(s * j + 260);
//                    layers.backgrounds[x][y] = color;
////                        layers.backgrounds[x+1][y] = color;
////                        layers.backgrounds[x-1][y] = color;
////                        layers.backgrounds[x][y+1] = color;
////                        layers.backgrounds[x][y-1] = color;
//                }
            }
            break;
            case 26: {
                Gdx.graphics.setTitle("insideBallRejection at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 100000; j++) {
                    insideBallRejection(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
                }
            }
            break;
            case 27: {
                Gdx.graphics.setTitle("insideBallBoxMuller at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 100000; j++) {
                    insideBallBoxMuller(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
                }
            }
            break;
            case 28: {
                Gdx.graphics.setTitle("insideBallBoxMullerFast at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 100000; j++) {
                    insideBallBoxMullerFast(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
                }
            }
            break;
            case 29: {
//                Gdx.graphics.setTitle("Weird color thing at " + Gdx.graphics.getFramesPerSecond() + " FPS");
//                int x, y;
//                float co, cg, color;
//                final float luma = NumberTools.swayTight(TimeUtils.timeSinceMillis(startTime) * 0x1.2p-11f);
//                for (int j = 0; j < 100000; j++) {
////                    final long t = diver.nextLong();
////                    final float mag = ((t & 0xFFFFFFL) * 0x1.0p-25f) + ((t >>> 40) * 0x1.0p-25f),
//                    //final float mag = (float) Math.sqrt(1.0f - diver.nextFloat()), angle = diver.nextFloat() * MathUtils.PI2;
//                    // + (0x1.0p0f - ((s & 0xFFFFFFL) * 0x1.0p-24f) * ((s >>> 40) * 0x1.0p-24f))) * 0.5f;
//                    //warm = NumberTools.cos(angle) * mag;
//                    //mild = NumberTools.sin(angle) * mag;
//                    cg = (diver.nextFloat() + diver.nextFloat() + diver.nextFloat() + diver.nextFloat() + diver.nextFloat() + diver.nextFloat()
//                            - diver.nextFloat() - diver.nextFloat() - diver.nextFloat() - diver.nextFloat() - diver.nextFloat() - diver.nextFloat()) * 0.17f % 1f; // -1 to 1, curved random
//                    co = (diver.nextFloat() + diver.nextFloat() + diver.nextFloat() + diver.nextFloat() + diver.nextFloat() + diver.nextFloat()
//                                    - diver.nextFloat() - diver.nextFloat()- diver.nextFloat() - diver.nextFloat() - diver.nextFloat() - diver.nextFloat()) * 0.17f % 1f; // -1 to 1, curved random
////                    mild = Math.signum(mild) * (float) Math.pow(Math.abs(mild), 1.05);
////                    warm = Math.signum(warm) * (float) Math.pow(Math.abs(warm), 0.8);
////                    if (mild > 0 && warm < 0) warm += mild * 1.666f;
////                    else if (mild < -0.6) warm *= 0.4f - mild;
//                    color = SColor.floatGetYCoCg(luma, co, cg, 1f);
//                    co = SColor.chrominanceOrange(color);
//                    cg = SColor.chrominanceGreen(color);
//                    x = Noise.fastFloor(co * 250 + 260);
//                    y = Noise.fastFloor(cg * 250 + 260);
//                    layers.backgrounds[x][y] = color;
//                }
                Gdx.graphics.setTitle("insideBallExponential at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 100000; j++) {
                    insideBallExponential(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
                }
            }
            break;
            case 30: {
                Gdx.graphics.setTitle("OnSphereTrig at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                //final float luma = NumberTools.swayTight(TimeUtils.timeSinceMillis(startTime) * 0x1.2p-11f);
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 100000; j++) {
                    onSphereTrig(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
//                    layers.backgrounds[x][y] = SColor.floatGetYCwCm(luma, (float)circleCoord[0], (float)circleCoord[1], 1f);
                }
            }
            break;
            case 31: {
                Gdx.graphics.setTitle("Sine frequencies");
                float p = MathUtils.PI;
                if ((TimeUtils.timeSinceMillis(startTime) & 512L) == 0L) {
                    for (int i = 1; i <= 0x400000; i++, p = (p + 1.6180339887498949f) % MathUtils.PI2) {
//                    amounts[MathUtils.round(MathUtils.sinDeg(p) * 255f + 256f)]++;           // libGDX's LUT way
                        amounts[MathUtils.round((NumberTools.sin(p)) * 255f + 256f)]++;   // SquidLib's no-LUT way
                    }
                } else {
                    for (int i = 1; i <= 0x400000; i++, p = (p + 1.6180339887498949f) % MathUtils.PI2) {
                        amounts[MathUtils.round(MathUtils.sin(p) * 255f + 256f)]++;           // libGDX's LUT way
//                    amounts[MathUtils.round(minimaxSin(p) * 255f + 256f)]++;           // libGDX's LUT way
//                        amounts[MathUtils.round((NumberTools.sinDegrees(p)) * 255f + 256f)]++;   // SquidLib's no-LUT way
                    }

                }
//                float q = 0.6180339887498949f;
//                for (int i = 1; i <= 0x1000000; i++, p = (p + 1.6180339887498949f) % 360f, q = (q + MathUtils.PI) % 360f) {
//                    amounts[MathUtils.round((NumberTools.sinDegrees(p) + NumberTools.sinDegrees(q)) * 255f * 0.5f + 256f)]++;   // SquidLib's no-LUT way

                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] >>> 8)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 32: {
                Gdx.graphics.setTitle("atan2_ random, uniform points at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    amounts[(int) (NumberTools.atan2_(diver.nextFloat() - 0.5f,
                            diver.nextFloat() - 0.5f) * 512f)]++;   // SquidLib's no-LUT way
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 33: {
                Gdx.graphics.setTitle("atan2_ random, first quadrant points at " + Gdx.graphics.getFramesPerSecond());
//                Gdx.graphics.setTitle("atan2_ random, triangular points at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    amounts[(int) (NumberTools.atan2_(diver.nextFloat() * 0.5f, diver.nextFloat() * 0.5f) * 2047.99f)]++;
//                    long r = DiverRNG.randomize(i);
//                    amounts[(int) (NumberTools.atan2_((r & 0xFFFF) - (r >>> 16 & 0xFFFF),
//                            (r >>> 32 & 0xFFFF) - (r >>> 48 & 0xFFFF)) * 512f)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 34: {
                Gdx.graphics.setTitle("atan2_ random, less-biased, inverted triangular at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    long r = diver.nextLong();
                    double a = (((r & 0xFFF) + (r >>> 12 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-13,
                            b = (((r >>> 24 & 0xFFF) + (r >>> 36 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-13;
                    amounts[(int) (NumberTools.atan2_(Math.cbrt(a),
                            Math.cbrt(b)) * 385.0 + (diver.nextLong() & 127))]++;
//                    amounts[(int)(NumberTools.atan2_((r & 0xFF) + (r >>> 8 & 0xFF) - (r >>> 16 & 0xFF) - (r >>> 24 & 0xFF),
//                            (r >>> 32 & 0xFF) + (r >>> 40 & 0xFF) - (r >>> 48 & 0xFF) - (r >>> 56)) * 512f)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 35: {
                Gdx.graphics.setTitle("atan2_ random, biased-toward-center, inverted triangular at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    long r = diver.nextLong();
                    double a = (((r & 0xFFF) + (r >>> 12 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-13,
                            b = (((r >>> 24 & 0xFFFF) + (r >>> 36 & 0xFFF) & 0xFFF) - 0xBFF.8p0) * 0x1p-13;
                    amounts[(int) (NumberTools.atan2_(Math.cbrt(a),
                            Math.cbrt(b)) * 385.0 + (diver.nextLong() & 127))]++;
//                    amounts[(int)(NumberTools.atan2_((r & 0xFF) + (r >>> 8 & 0xFF) - (r >>> 16 & 0xFF) - (r >>> 24 & 0xFF),
//                            (r >>> 32 & 0xFF) + (r >>> 40 & 0xFF) - (r >>> 48 & 0xFF) - (r >>> 56)) * 512f)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 36: {
                Gdx.graphics.setTitle("atan2_ random, biased-toward-extreme at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    long r = diver.nextLong();
                    double a = (((r & 0xFFF) + (r >>> 12 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-13,
                            b = (((r >>> 24 & 0xFFF) + (r >>> 36 & 0xFFF) & 0xFFF) - 0x3FF.8p0) * 0x1p-13;
                    amounts[(int) (NumberTools.atan2_(Math.cbrt(a),
                            Math.cbrt(b)) * 385.0 + (diver.nextLong() & 0x7F))]++;
//                    amounts[(int)(NumberTools.atan2_((r & 0xFF) + (r >>> 8 & 0xFF) - (r >>> 16 & 0xFF) - (r >>> 24 & 0xFF),
//                            (r >>> 32 & 0xFF) + (r >>> 40 & 0xFF) - (r >>> 48 & 0xFF) - (r >>> 56)) * 512f)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 37: {
                float tm = (TimeUtils.timeSinceMillis(startTime) * 7 & 0xFFFF) * 0x1p-17f;
                Gdx.graphics.setTitle("atan2_ random, bias value " + tm + ", at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    long r = diver.nextLong();
                    amounts[(int) ((NumberTools.sin_((r & 0xFFFL) * 0x1p-12f) * tm
                            + ((r >>> 16 & 0xFFFL) - (r >>> 28 & 0xFFFL) + (r >>> 40 & 0xFFFL) - (r >>> 52)) * 0x1p-13f * (0.5f - tm)
//                    + (NumberTools.asin_((r >>> 32 & 0xFFFF) * 0x1p-16f) - NumberTools.asin_((r >>> 48) * 0x1p-16f)) * (2f - tm)
                            + 0.5f) * 511f)]++;
                }
//                long tm = (TimeUtils.timeSinceMillis(startTime) * 7 & 0xFFFF) - 0x8000;
//                Gdx.graphics.setTitle("atan2_ random, bias value " + tm + ", at " + Gdx.graphics.getFramesPerSecond());
//                for (int i = 1; i <= 0x100000; i++) {
//                    long r = diver.nextLong();
//                    long a = ((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)),
//                            b = ((((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF))) + tm);
//                    amounts[(int) (NumberTools.atan2_(a, b) * 385.0 + (diver.nextLong() & 127))]++;
////                    amounts[(int) (NumberTools.atan2_(Math.cbrt(a),
////                            Math.cbrt(b)) * 384.0 + 
////                            ((DiverRNG.determine(r & 0xFFFFFFFFL) & 127) +
////                                    (DiverRNG.determine(r >>> 32) & 127)
////                                    & 127) + 0.5)]++;
////                    amounts[(int) (NumberTools.atan2_(a,
////                            b) * 448.0 + (DiverRNG.determine(r) & 63) + 0.5)]++;
////                    amounts[(int) (NumberTools.atan2_(Math.cbrt(a),
////                            Math.cbrt(b)) * 384.0 + (r >>> 48 & 0x7F) + 0.5)]++;
////                    amounts[(int)(NumberTools.atan2_((r & 0xFF) + (r >>> 8 & 0xFF) - (r >>> 16 & 0xFF) - (r >>> 24 & 0xFF),
////                            (r >>> 32 & 0xFF) + (r >>> 40 & 0xFF) - (r >>> 48 & 0xFF) - (r >>> 56)) * 512f)]++;
//                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 38: {
                long tm = (TimeUtils.timeSinceMillis(startTime) * 17 & 0x1FFFF) - 0x10000;
                tweak.setCentrality(tm);
                Gdx.graphics.setTitle("TweakRNG, centrality " + tm + ", at " + Gdx.graphics.getFramesPerSecond());
                for (int i = 1; i <= 0x100000; i++) {
                    amounts[tweak.next(9)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (amounts[i] / 50)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
//                Gdx.graphics.setTitle("atan2_ random, biased-toward-extreme, Gudermannian at " + Gdx.graphics.getFramesPerSecond());
//                for (int i = 1; i <= 0x200000; i++) {
//                    long r = DiverRNG.randomize(i);
////                    long a = ((0x1000 + (r & 0xFFF) - (r >>> 12 & 0xFFF) & 0xFFF) - 0x800),
////                            b = ((0x1000 + (r >>> 24 & 0xFFF) - (r >>> 36 & 0xFFF) & 0xFFF) - 0x400);
////                    double a = (((r & 0xFFF) + (r >>> 12 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-13,
////                            b = (((r >>> 24 & 0xFFF) + (r >>> 36 & 0xFFF) & 0xFFF) - 0x3FF.8p0) * 0x1p-13;
//                    double a = Math.expm1((((r & 0xFFF) + (r >>> 12 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-16),
//                            b = Math.expm1((((r >>> 24 & 0xFFF) + (r >>> 36 & 0xFFF) & 0xFFF) - 0x3FF.8p0) * 0x1p-16);
//                    amounts[(int)(NumberTools.atan2_(Math.asin(a / (a + 2.0)),
//                            NumberTools.asin(b / (b + 2.0))) * 385.0 + (r >>> 48 & 0x7F))]++;
////                    amounts[(int)(NumberTools.atan2_(icbrt(a),
////                            icbrt(b)) * 384.0 + (r >>> 48 & 0x7F) + 0.5)]++;
////                    amounts[(int)(NumberTools.atan2_(a*0.6042181313 + 0.4531635984,
////                            b*0.6042181313 + 0.4531635984) * 384.0 + (r >>> 48 & 0x7F) + 0.5)]++;
////                    amounts[(int)(NumberTools.atan2_(Math.cbrt(a),
////                            Math.cbrt(b)) * 384.0 + (r >>> 48 & 0x7F) + 0.5)]++;
////                    amounts[(int)(NumberTools.atan2_((r & 0xFF) + (r >>> 8 & 0xFF) - (r >>> 16 & 0xFF) - (r >>> 24 & 0xFF),
////                            (r >>> 32 & 0xFF) + (r >>> 40 & 0xFF) - (r >>> 48 & 0xFF) - (r >>> 56)) * 512f)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = Math.max(0, 520 - (amounts[i] / 100)); j < 520; j++) {
//                        layers.backgrounds[i+8][j] = color;
//                    }
//                }
//                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
//                    for (int i = 0; i < jj + 2; i++) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
//            }
//                Gdx.graphics.setTitle("atan2_ random, biased-toward-extreme, inverted triangular points, Gudermannian");
//                for (int i = 1; i <= 0x200000; i++) {
//                    long r = DiverRNG.randomize(i);
//                    double a = Math.expm1((((r & 0xFFF) + (r >>> 12 & 0xFFF) & 0xFFF) - 0x7FF.8p0) * 0x1p-13),
//                            b = Math.expm1((((r >>> 24 & 0xFFF) + (r >>> 36 & 0xFFF) & 0xFFF) - 0x3FF.8p0) * 0x1p-13);
//                    amounts[(int)(NumberTools.atan2_(NumberTools.asin(a / (a + 2.0)),
//                            NumberTools.asin(b / (b + 2.0))) * 385.0 + (r >>> 48 & 0x7F))]++;
////                    amounts[(int)(NumberTools.atan2_((r & 0xFF) + (r >>> 8 & 0xFF) - (r >>> 16 & 0xFF) - (r >>> 24 & 0xFF),
////                            (r >>> 32 & 0xFF) + (r >>> 40 & 0xFF) - (r >>> 48 & 0xFF) - (r >>> 56)) * 512f)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = Math.max(0, 520 - (amounts[i] / 100)); j < 520; j++) {
//                        layers.backgrounds[i+8][j] = color;
//                    }
//                }
//                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
//                    for (int i = 0; i < jj + 2; i++) {
//                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
//                    }
//                }
//            }
//        break;
            case 39: {
                Gdx.graphics.setTitle("Math vs. MathUtils, asin() absolute error");

                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += Math.abs(Math.asin((i - 25600) / 25600.0) - NumberTools.asin((i - 25600) / 25600f));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * 100.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 40: {
                Gdx.graphics.setTitle("Math vs. MathUtils, acos() absolute error");

                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += Math.abs(Math.acos((i - 25600) / 25600.0) - NumberTools.acos((i - 25600) / 25600f));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * 100.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 41: {
                Gdx.graphics.setTitle("Math vs. MathUtils, sin() absolute error");

                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += Math.abs(Math.sin(((i - 25600) / 12800.0) * Math.PI) - MathUtils.sin(((i - 25600) / 12800f) * MathUtils.PI));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * Math.PI * 200.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 42: {
                Gdx.graphics.setTitle("Math vs. MathUtils, cos() absolute error");
                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += Math.abs(Math.cos(((i - 25600) / 12800.0) * Math.PI) - MathUtils.cos(((i - 25600) / 12800f) * MathUtils.PI));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * Math.PI * 200.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 43: {
                Gdx.graphics.setTitle("Math vs. MathUtils, asin() relative error");

                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += (Math.asin((i - 25600) / 25600.0) - NumberTools.asin((i - 25600) / 25600f));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * 100.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 44: {
                Gdx.graphics.setTitle("Math vs. MathUtils, acos() relative error");

                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += (Math.acos((i - 25600) / 25600.0) - NumberTools.acos((i - 25600) / 25600f));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * 100.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 45: {
                Gdx.graphics.setTitle("Math vs. MathUtils, sin() relative error");

                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += (Math.sin(((i - 25600) / 12800.0) * Math.PI) - MathUtils.sin(((i - 25600) / 12800f) * MathUtils.PI));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * Math.PI * 200.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 46: {
                Gdx.graphics.setTitle("Math vs. MathUtils, cos() relative error");
                for (int i = 0; i < 51200; i++) {
                    dAmounts[i / 100] += (Math.cos(((i - 25600) / 12800.0) * Math.PI) - MathUtils.cos(((i - 25600) / 12800f) * MathUtils.PI));
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 520 - (int) Math.round(dAmounts[i] * Math.PI * 200.0)); j < 520; j++) {
                        layers.backgrounds[i + 8][j] = color;
                    }
                }
                for (int j = 510, jj = 0; j >= 0; j -= 10, jj = (jj + 1) % 5) {
                    for (int i = 0; i < jj + 2; i++) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 47: {
                // thanks to Mitchell Spector, https://math.stackexchange.com/a/1860731
                Gdx.graphics.setTitle("Spiral Numbering Thing, from position");
                for (int x = -8; x < 8; x++) {
                    for (int y = -8; y < 8; y++) {
                        int m, sign, g;
                        if ((x + (x >> 31) ^ x >> 31) >= (y + (y >> 31) ^ y >> 31)) {
                            m = -x;
                            sign = -1;
                        } else {
                            m = y;
                            sign = 0;
                        }
                        g = 4 * m * m + ((m >= 0) ? y + x : (m + m + y - x + sign ^ sign));
                        float color = SColor.floatGetI(g, g, g);
                        for (int a = 0; a < 32; a++) {
                            for (int b = 0; b < 32; b++) {
                                layers.backgrounds[256 + 8 + (x << 5) + a][256 + (y << 5) + b] = color;
                            }
                        }
                    }
                }
            }
            break;
            case 48: {
                // thanks to Jonathan M, https://stackoverflow.com/a/20591835
                Gdx.graphics.setTitle("Spiral Numbering Thing, from index");
                for (int g = 0; g < 256; g++) {
                    final int root = (int) (Math.sqrt(g));
                    final int sign = -(root & 1);
                    final int big = (root * (root + 1)) - g << 1;
                    final int y = ((root + 1 >> 1) + sign ^ sign) + ((sign ^ sign + Math.min(big, 0)) >> 1);
                    final int x = ((root + 1 >> 1) + sign ^ sign) - ((sign ^ sign + Math.max(big, 0)) >> 1);
                    float color = SColor.floatGetI(g, g, g);
                    for (int a = 0; a < 32; a++) {
                        for (int b = 0; b < 32; b++) {
                            layers.backgrounds[256 + 8 + (x << 5) + a][256 + (y << 5) + b] = color;
                        }
                    }
                }
            }
            break;
            case 49: {
                Gdx.graphics.setTitle("Jitter Test at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                long r = diver.nextLong(), s = diver.nextLong();
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 10000; j++) {
//                    x = Noise.fastFloor(Math.cbrt(((short)r) * ((short)(r >>> 16)) * 0x1p-32) * 250 + 260);
//                    y = Noise.fastFloor(Math.cbrt(((short)(r>>>32)) * ((short)(r >>> 48)) * 0x1p-32) * 250 + 260);
                    x = ((int) ((Long.bitCount(r += 0xC13FA9A902A633EBL) - Long.bitCount(r += 0xC13FA9A902A633EBL) + (int) (r += 0xC13FA9A902A633EBL) * 0x1.4p-30) * 4 + 256) & 511) + 4;
                    y = ((int) ((Long.bitCount(s += 0x9E3779B97F4C0401L) - Long.bitCount(s += 0x9E3779B97F4C0401L) + (int) (s += 0x9E3779B97F4C0401L) * 0x1.4p-30) * 4 + 256) & 511) + 4;
                    // 0xC13FA9A902A6328FL 0x9E3779B97F4A7C15L
                    // changed to (bit counts are better)
                    // 0xC13FA9A902A633EBL 0x9E3779B97F4C0401L
                    layers.backgrounds[x][y] = color;
                }
            }
            break;
            case 50: {
                Gdx.graphics.setTitle("Indexed Blue Noise Points at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                long size = (System.nanoTime() >>> 20 & 0xfff) + 1L;
                int x, y;
                for (int i = 0; i < size; i++) {
                    BlueNoise.indexedBlueNoisePoint(circleCoord, i, 1.0);
                    x = (int) (circleCoord[0] * 512);
                    y = (int) (circleCoord[1] * 512);
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
            }
            break;
            case 51: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, bits of random.nextDouble()");
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x10000; i++) {
                    long bits = Double.doubleToLongBits(xs128.nextDouble());
                    for (int j = 0, jj = 504; j < 64; j++, jj -= 8) {
                        if (1L == (bits >>> j & 1L))
                            amounts[jj] = amounts[jj + 1] = amounts[jj + 2] = amounts[jj + 3]
                                    = amounts[jj + 4] = amounts[jj + 5] = ++amounts[jj + 6];
                    }
                }
                for (int i = 0; i < 512; i++) {
                    if ((i & 7) == 3) {
                        for (int j = 510 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.c98066p126F;
                        }
                    } else {
                        for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.d08864p126F;
                        }
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 52: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, bits of nextExclusiveDouble()");
                for (int i = 0; i < 0x10000; i++) {
                    long bits = Double.doubleToLongBits(nextExclusiveDouble());
                    for (int j = 0, jj = 504; j < 64; j++, jj -= 8) {
                        if (1L == (bits >>> j & 1L))
                            amounts[jj] = amounts[jj + 1] = amounts[jj + 2] = amounts[jj + 3]
                                    = amounts[jj + 4] = amounts[jj + 5] = ++amounts[jj + 6];
                    }
                }
                for (int i = 0; i < 512; i++) {
                    if ((i & 7) == 3) {
                        for (int j = 510 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.c98066p126F;
                        }
                    } else {
                        for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.d08864p126F;
                        }
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 53: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, bits of nextFloat()");
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x10000; i++) {
                    int bits = Float.floatToIntBits(xs128.nextFloat());
                    for (int j = 0, jj = 504 - 128; j < 32; j++, jj -= 8) {
                        if (1 == (bits >>> j & 1))
                            amounts[jj] = amounts[jj + 1] = amounts[jj + 2] = amounts[jj + 3]
                                    = amounts[jj + 4] = amounts[jj + 5] = ++amounts[jj + 6];
                    }
                }
                for (int i = 0; i < 512; i++) {
                    if ((i & 7) == 3) {
                        for (int j = 510 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.c98066p126F;
                        }
                    } else {
                        for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.d08864p126F;
                        }
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 54: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, bits of nextExclusiveFloat()");
                for (int i = 0; i < 0x10000; i++) {
                    int bits = Float.floatToIntBits(nextExclusiveFloat());
                    for (int j = 0, jj = 504 - 128; j < 32; j++, jj -= 8) {
                        if (1 == (bits >>> j & 1))
                            amounts[jj] = amounts[jj + 1] = amounts[jj + 2] = amounts[jj + 3]
                                    = amounts[jj + 4] = amounts[jj + 5] = ++amounts[jj + 6];
                    }
                }
                for (int i = 0; i < 512; i++) {
                    if ((i & 7) == 3) {
                        for (int j = 510 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.c98066p126F;
                        }
                    } else {
                        for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                            layers.backgrounds[i][j] = -0x1.d08864p126F;
                        }
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 55: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " cbrt lines");
                for (int i = 0; i < 512; i++) {
                    float in = (i - 255);
                    double m = Math.cbrt(in);
                    if (m >= -15 && m <= 15)
                        layers.backgrounds[i][(int) (m * 16 + 255)] = -0x1.d08864p126F;
                    float a = MathExtras.cbrt(in);
                    if (a >= -15 && a <= 15)
                        layers.backgrounds[i][(int) (a * 16 + 255)] = -0x1.6a9704p125F;
                    float s = cbrtShape(in);
                    if (s >= -15 && s <= 15)
                        layers.backgrounds[i][(int) (s * 16 + 255)] = -0x1.30012cp125F;
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
            }
            break;
            case 56:
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " gaussian bits");
                for (int i = 0; i < 0x2000; i++) {
//                    long bits = diver.nextLong(), expo = 997L + Long.bitCount(bits) << 52;
//                    double d = NumberTools.longBitsToDouble((bits & ~0x7FF0_0000_0000_0000L) | expo) * 64.0 + 256.0;
//                    double d = (diver.nextLong() >>> 12) / NumberTools.longBitsToDouble(((diver.nextLong() & 0xB00F_FFFF_FFFF_FFFFL) | 0x4340_0000_0000_0000L)) * 64.0 + 256.0;
                    long d = (Double.doubleToLongBits(nextGaussian()) >>> 52 & 0x7FFL) - 0x300L;
                    if (d >= 0 && d < 512)
                        amounts[(int) d]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = Math.max(0, 519 - (amounts[i] >> 3)); j < 520; j++) {
                        layers.backgrounds[i][j] = color;
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.backgrounds[i][j] = -0x1.7677e8p125F;
                    }
                }
                break;
        }
    }

    /**
     * This is a simplified version of <a href="https://allendowney.com/research/rand/">this
     * algorithm by Allen Downey</a>. This version can return double values between 2.710505431213761E-20 and
     * 0.9999999999999999, or 0x1.0p-65 and 0x1.fffffffffffffp-1 in hex notation. It cannot return 0 or 1. It has much
     * more uniform bit distribution across its mantissa/significand bits than {@link Random#nextDouble()}. Where Random
     * is less likely to produce a "1" bit for its lowest 5 bits of mantissa (the least significant bits numerically,
     * but potentially important for some uses), with the least significant bit produced half as often as the most
     * significant bit in the mantissa, this has approximately the same likelihood of producing a "1" bit for any
     * positions in the mantissa.
     * @return a random uniform double between 0 and 1 (both exclusive, unlike most nextDouble() implementations)
     */
    public static double nextExclusiveDouble(){
        final long bits = MathUtils.random.nextLong();
        return NumberUtils.longBitsToDouble(1022L - Long.numberOfTrailingZeros(bits) << 52
                | bits >>> 12);
    }
    /**
     * This is a simplified version of <a href="https://allendowney.com/research/rand/">this
     * algorithm by Allen Downey</a>. This version can return double values between 2.7105054E-20 to 0.99999994, or
     * 0x1.0p-65 to 0x1.fffffep-1 in hex notation. It cannot return 0 or 1. It has much
     * more uniform bit distribution across its mantissa/significand bits than {@link Random#nextDouble()}. Where Random
     * is less likely to produce a "1" bit for its lowest 5 bits of mantissa (the least significant bits numerically,
     * but potentially important for some uses), with the least significant bit produced half as often as the most
     * significant bit in the mantissa, this has approximately the same likelihood of producing a "1" bit for any
     * positions in the mantissa.
     * @return a random uniform double between 0 and 1 (both exclusive, unlike most nextDouble() implementations)
     */
    public static float nextExclusiveFloat(){
        final long bits = MathUtils.random.nextLong();
        return NumberUtils.intBitsToFloat(126 - Long.numberOfTrailingZeros(bits) << 23
                | (int)(bits >>> 41));
    }

    private double acbrt(double r)
    {
        double a = 1.4774329094 - 0.8414323527/(r+0.7387320679),
        //a = 0.6042181313 * r + 0.4531635984,
                a3 = a * a * a, a3r = a3 + r;
        //a *= ((a3r + r) / (a3 + a3r));
        return a * ((a3r + r) / (a3 + a3r));
    }

    private float cbrtShape(float x){
        final int i = NumberTools.floatToRawIntBits(x);
        return NumberTools.intBitsToFloat(((i & 0x7FFFFFFF) - 0x3F800000) / 3 + 0x3F800000 | (i & 0x80000000));
    }
    
    private double isqrt(long x)
    {
        final long sign = x >> 63;
        x = x + sign ^ sign;
        return Math.sqrt(x) * (sign | 1L);
        
//        final int bits_x = 64 - Long.numberOfLeadingZeros(x);
//        if(bits_x == 0) return 0;
//        final int exp_r = (bits_x + 2) / 3;
//        
//        long r = 1L << exp_r;
////        for (int i = 0; i < 3; i++) {
//        r = (2L * r + x / (r * r)) / 3L;
//        r = (2L * r + x / (r * r)) / 3L;
// //       }
//
//        /* initial estimate: 2 ^ ceil(b / 3) */
////        long est_r = 1L << exp_r, r;
////
////        do /* quadratic convergence (?) */
////        {
////            r = est_r;
////            est_r = (2 * r + x / (r * r)) / 3;
////        }
////        while (est_r < r);
//        
//        return r + sign ^ sign;
    }
    private int hyperCurve(DiverRNG random, int bound) {
        long a = random.nextLong(), mask;
        int lz = Integer.numberOfLeadingZeros(bound), res = 0, ctr = 0;
        while (lz < 32)
        {
            mask = 0x7FFFFFFFL >>> lz;
            bound &= mask;
            if((ctr & 63) > ((ctr += 32 - lz) & 63))
            {
                res += (a & (mask >>> ctr)); // implicitly does ctr & 63
                a = DiverRNG.determine(a);
                mask = 0x7FFFFFFFL >>> 32 - (ctr & 63);
//                res += (a & mask);
            }
            res += (a & mask);
            a >>>= 31 - lz;
            lz = Integer.numberOfLeadingZeros(bound);
        }
        return res;
    }
    public static double vdc(double base, int index)
    {
        //0.7548776662466927, 0.5698402909980532
        double n = (index+1 & 0x7fffffff);
        base += 0.6180339887498949;
        base *= 0.6180339887498949;
        base -= (int)base;
        double res = 0.0;
        while (n >= 1) {
            res += (n *= base);
            base += 0.6180339887498949;
            base *= 0.6180339887498949;
            base -= (int) base;
        }
//        double denominator = base;
//        int n = (index+1 & 0x7fffffff);
//        while (n > 0.0)
//        {
//            res += (n % base) / denominator;
//            n /= base;
//            denominator *= base;
//        }
        return res - (int)res;
    }
    
    public static float minimaxSin(float x)
    {
//        x %= MathUtils.PI2;
        final float c3 = -0.16666f, c5 = 0.0083143f, c7 = -0.00018542f;
        float xp = x * x;
        final float d = xp * c3, h = xp * c7;
        xp *= xp;
        return ((h + c5) * xp + d + 1) * x;
        
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update();
        stage.draw();
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Visualizer for Math Testing/Checking");
        config.useVsync(false);
        config.setWindowedMode(512, 520);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new MathVisualizer(), config);
    }

}
