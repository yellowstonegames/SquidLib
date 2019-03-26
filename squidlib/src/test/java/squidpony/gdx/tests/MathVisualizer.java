package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.*;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 1/13/2018.
 */
public class MathVisualizer extends ApplicationAdapter {
    private int mode = 24;
    private int modes = 29;
    private SpriteBatch batch;
    private SparseLayers layers;
    private InputAdapter input;
    private Stage stage;
    private int[] amounts = new int[512];
    private DiverRNG diver;
    private RandomBias bias;
    private RandomXS128 xs128;
    private XSP xsp;
    private EditRNG edit;
    private long seed = 1L;
    private long startTime = 0L;
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

    public void insideCircleRejection(final double[] vector)
    {
        double v1, v2;
        do {
            v1 = 2 * diver.nextDouble() - 1; // between -1 and 1
            v2 = 2 * diver.nextDouble() - 1; // between -1 and 1
        } while (v1 * v1 + v2 * v2 > 1);
        vector[0] = v1;
        vector[1] = v2;
    }
    public final double fastGaussian()
    {
        long a = diver.nextLong();
        a = (a & 0x00FF00FF00FF00FFL) + ((a & 0xFF00FF00FF00FF00L) >>> 8);
        a = (a & 0x000001FF000001FFL) + ((a & 0x01FF000001FF0000L) >>> 16);
        return ((a & 0x00000000000003FFL) + ((a & 0x000003FF00000000L) >>> 32) - 1020L) * 0x1.010101010101p-8;
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
        diver = new DiverRNG();
        seed = DiverRNG.determine(12345L);
        bias = new RandomBias();
        edit = new EditRNG();
        xs128 = new RandomXS128();
        xsp = new XSP();
        batch = new SpriteBatch();
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
                }
                return false;
            }
        };
        Gdx.input.setInputProcessor(input);
        stage.addActor(layers);
        update();
    }

    public void update()
    {
        Arrays.fill(amounts, 0);
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
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                //DiverRNG diver = new DiverRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) (diver.nextFloat() * 512)]++;
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
            case 11: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) (MathUtils.random() * 512)]++;
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
            case 12: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
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
                        " XSP, mult128 & 0x1FFL");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((xsp.nextLongMult(0x1800000000000000L)) & 0x1FFL)]++;
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
                        " XSP, bitBased & 0x1FFL");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((xsp.nextLongBit(0x1800000000000000L)) & 0x1FFL)]++;
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
//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " RandomXS128, random.nextInt(0x200)");
//                RandomXS128 random = new RandomXS128();
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[random.nextInt(512)]++;
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
            case 16: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " XSP, traditional & 0x1FFL");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((xsp.nextLong(0x1800000000000000L)) & 0x1FFL)]++;
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
                        " MathUtils.random(0x0FFFFFFFFFFFFFFFL) & 0x1FFL // UH OH");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) (MathUtils.random(0x0FFFFFFFFFFFFFFFL) & 0x1FFL)]++;
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
                    x = (int) (VanDerCorputQRNG.determine2(a) * 512);
                    y = (int) (VanDerCorputQRNG.determine(3, a) * 512);
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
                x = (int) (VanDerCorputQRNG.determine2(a) * 512);
                y = (int) (VanDerCorputQRNG.determine(3, a) * 512);
                layers.backgrounds[x+1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y+1] = -0x1.794bfep125F;
                layers.backgrounds[x-1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y-1] = -0x1.794bfep125F;

            }
            break;
            case 20: {
                long size = (System.nanoTime() >>> 28 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Halton[striding 1](2,39) sequence, first " + size + " points");
                int x, y, a = 421;
                for (int i = 0; i < size; i++) {
//                    a = determinePositive16(a);
                    a++;
                    x = (int) (VanDerCorputQRNG.determine2(a) * 512);
                    y = (int) (VanDerCorputQRNG.determine(39, a) * 512);
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.backgrounds[x][y] = SColor.FLOAT_BLACK;
                }
                x = (int) (VanDerCorputQRNG.determine2(a) * 512);
                y = (int) (VanDerCorputQRNG.determine(39, a) * 512);
                layers.backgrounds[x+1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y+1] = -0x1.794bfep125F;
                layers.backgrounds[x-1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y-1] = -0x1.794bfep125F;

            }
            break;
            // from http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // this works much better than I had previously found, since the LFSR I was using wasn't being advanced
            // correctly, and this caused a pattern in the output because there was a pattern in the input.
            // Notably, with very large indices this still doesn't get collisions, but Halton does.
            case 21: {
                long size = (System.nanoTime() >>> 28 & 0xfff) + 1L;
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
                x = (a * 0xC13FA9A9) >>> 23;
                y = (a * 0x91E10DA5) >>> 23;
                layers.backgrounds[x+1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y+1] = -0x1.794bfep125F;
                layers.backgrounds[x-1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y-1] = -0x1.794bfep125F;
            }
            break;
            case 22: {
                long size = (System.nanoTime() >>> 28 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Roberts sequence (later start), first " + size + " points");
                int x, y, a = 100000;
                for (int i = 0; i < size; i++) {
                    //1.32471795724474602596 0.7548776662466927 0.5698402909980532
                    //0x5320B74F 0xC13FA9A9 0x91E10DA5
                    //0x5320B74ECA44ADACL 0xC13FA9A902A6328FL 0x91E10DA5C79E7B1DL
                    // 0x9E3779B97F4A7C15L
                    //a = determinePositive16(a);
//                    x = (int) (0x8000000000000000L + a * 0xC13FA9A902A6328FL >>> 55);
//                    y = (int) (0x8000000000000000L + a * 0x91E10DA5C79E7B1DL >>> 55);
                    a++;
                    x = (a * 0xC13FA9A9) >>> 23;
                    y = (a * 0x91E10DA5) >>> 23;

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
                x = (a * 0xC13FA9A9) >>> 23;
                y = (a * 0x91E10DA5) >>> 23;
                layers.backgrounds[x+1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y+1] = -0x1.794bfep125F;
                layers.backgrounds[x-1][y] = -0x1.794bfep125F;
                layers.backgrounds[x][y-1] = -0x1.794bfep125F;

            }
            break;

            case 23: {
                //long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
                final long size = 128;
                Gdx.graphics.setTitle("Haltoid(777) sequence, first " + size + " points");
                //int a, x, y, p2 = 777 * 0x2C9277B5 | 1;
                //int lfsr = 7;
                // (lfsr = (lfsr >>> 1 ^ (-(lfsr & 1) & 0x3802))) // 0x20400 is 18-bit // 0xD008 is 16-bit // 0x3802 is 14-bit
                int x, y;
                Coord c;
                for (int i = 0; i < size; i++) {
//                    a = GreasedRegion.disperseBits(Integer.reverse(p2 * (i + 1))); //^ 0xAC564B05
//                    x = a >>> 7 & 0x1ff;
//                    y = a >>> 23 & 0x1ff;
                    c = VanDerCorputQRNG.haltoid(777, 510, 510, 1, 1, i);
                    x = c.x;
                    y = c.y;
//                    x = a >>> 9 & 0x7f;
//                    y = a >>> 25 & 0x7f;
//                    if(layers.backgrounds[x][y] != 0f)
//                    {
//                        layers.backgrounds[x][y] = -0x1.7677e8p125F;
//                        System.out.println("Overlap on index " + i);
//                    }
//                    else
                    final float color = SColor.floatGetHSV(i * 0x1p-7f, 1f - i * 0x0.3p-7f, 0.7f, 1f);
                    layers.backgrounds[x][y] = color;
                    layers.backgrounds[x + 1][y] = color;
                    layers.backgrounds[x - 1][y] = color;
                    layers.backgrounds[x][y + 1] = color;
                    layers.backgrounds[x][y - 1] = color;
                }
            }
            break;
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
                        amounts[Noise.HastyPointHash.hash256(xx, yy, seed)]++;
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
                    layers.backgrounds[x + 1][y+1] = color;
                    layers.backgrounds[x - 1][y-1] = color;
                    layers.backgrounds[x-1][y + 1] = color;
                    layers.backgrounds[x+1][y - 1] = color;
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
            case 26:{
                Gdx.graphics.setTitle("insideCircleRejection at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 10000; j++) {
                    insideCircleRejection(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
//                    layers.backgrounds[x + 1][y] = color;
//                    layers.backgrounds[x - 1][y] = color;
//                    layers.backgrounds[x][y + 1] = color;
//                    layers.backgrounds[x][y - 1] = color;
//                    layers.backgrounds[x + 1][y+1] = color;
//                    layers.backgrounds[x - 1][y-1] = color;
//                    layers.backgrounds[x-1][y + 1] = color;
//                    layers.backgrounds[x+1][y - 1] = color;
                }
            }
            break;
            case 27:{
                Gdx.graphics.setTitle("insideCircleBoxMuller at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 10000; j++) {
                    insideCircleBoxMuller(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
//                    layers.backgrounds[x + 1][y] = color;
//                    layers.backgrounds[x - 1][y] = color;
//                    layers.backgrounds[x][y + 1] = color;
//                    layers.backgrounds[x][y - 1] = color;
//                    layers.backgrounds[x + 1][y+1] = color;
//                    layers.backgrounds[x - 1][y-1] = color;
//                    layers.backgrounds[x-1][y + 1] = color;
//                    layers.backgrounds[x+1][y - 1] = color;
                }
            }
            break;
            case 28:{
                Gdx.graphics.setTitle("insideCircleBoxMullerFast at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                int x, y;
                float color = SColor.FLOAT_BLACK;
                for (int j = 0; j < 10000; j++) {
                    insideCircleBoxMullerFast(circleCoord);
                    x = Noise.fastFloor(circleCoord[0] * 250 + 260);
                    y = Noise.fastFloor(circleCoord[1] * 250 + 260);
                    layers.backgrounds[x][y] = color;
//                    layers.backgrounds[x + 1][y] = color;
//                    layers.backgrounds[x - 1][y] = color;
//                    layers.backgrounds[x][y + 1] = color;
//                    layers.backgrounds[x][y - 1] = color;
//                    layers.backgrounds[x + 1][y+1] = color;
//                    layers.backgrounds[x - 1][y-1] = color;
//                    layers.backgrounds[x-1][y + 1] = color;
//                    layers.backgrounds[x+1][y - 1] = color;
                }
            }
//            case 24: {
//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " DiverRNG, hyperCurve(0x1FF)");
//                //DiverRNG diver = new DiverRNG();
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[hyperCurve(diver, 0x1FF)]++;
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

        }
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

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        update();
        stage.draw();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Visualizer for Math Testing/Checking";
        config.width = 512;
        config.height = 520;
        config.foregroundFPS = 0;
        config.vSyncEnabled = false;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new MathVisualizer(), config);
    }

}
