package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
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
    private int mode = 13;
    private int modes = 24;
    private SpriteBatch batch;
    private SparseLayers layers;
    private InputAdapter input;
    private Stage stage;
    private int[] amounts = new int[512];
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
    private static class XSP {
        private long state0;//, state1;
        public XSP()
        {
            state0 = (long) ((Math.random() - 0.5) * 0x10000000000000L)
                    ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L);
//            state1 = ~LightRNG.determine(state0);
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
    private final XoRoRNG r0 = new XoRoRNG(1234567890L),
            r1 = new XoRoRNG(1234567890L),
            r2 = new XoRoRNG(1234567890L);
    public final long mult128(long bound)
    {
        long rand = r0.nextLong();
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
    public final long bitmask(long bound)
    {
        final long mask = -1L >>> Long.numberOfLeadingZeros(--bound|1L);
        long x;
        do {
            x = r2.nextLong() & mask;
        } while (x > bound);
        return x;
    }
    public final long traditional(final long bound)
    {
        if (bound <= 0) return 0L;
        for (;;) {
            final long bits = r1.nextLong() & 0x7FFFFFFFFFFFFFFFL;
            final long value = bits % bound;
            if (bits - value + bound - 1L >= 0L) return value;
        }
    }

    @Override
    public void create() {
        Coord.expandPoolTo(512, 512);
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);
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
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(NumberTools.formCurvedFloat(random.nextLong()) * 256 + 256)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 1: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(twist(NumberTools.formCurvedFloat(random.nextLong())) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 2: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(twist(NumberTools.formDouble(random.nextLong()) *
                            NumberTools.formDouble(random.nextLong()) - NumberTools.formDouble(random.nextLong()) *
                            NumberTools.formDouble(random.nextLong())) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 3: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                LinnormRNG random = new LinnormRNG();
                long state;
                for (int i = 0; i < 0x1000000; i++) {
                    state = random.nextLong();
                    amounts[Noise.fastFloor((NumberTools.formFloat((int) state) * 0.5 +
                            (NumberTools.formFloat((int) (state >>> 20)) + NumberTools.formFloat((int) (state >>> 41))) * 0.25) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 4: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                RandomBias random = new RandomBias();
                random.distribution = RandomBias.EXP_TRI;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(random.biasedDouble(0.6, 512.0))]++;
                    //amounts[Noise.fastFloor(Math.nextAfter(random.biasedDouble(0.5, 512.0), 0.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 5: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                RandomBias random = new RandomBias();
                random.distribution = RandomBias.BATHTUB_TRUNCATED;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(random.biasedDouble(0.6, 512.0))]++;
                    //amounts[Noise.fastFloor(Math.nextAfter(random.biasedDouble(0.5, 512.0), 0.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 6: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                EditRNG random = new EditRNG();
                random.setCentrality(200);
                random.setExpected(0.75);
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(random.nextDouble(512.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 7: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                EditRNG random = new EditRNG();
                random.setCentrality(-200);
                random.setExpected(0.75);
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(random.nextDouble(512.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 8: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                LinnormRNG random = new LinnormRNG();
//                random.setCentrality(50);
//                random.setExpected(0.6);
                long centrality = NumberTools.doubleToLongBits(1.625) & 0xfffffffffffffL;
                double offset = 0.15, range = 0.6;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(((random.nextLong() & 0xfffffffffffffL) * 0x1p-54 + offset + range * ((centrality > (random.nextLong() & 0xfffffffffffffL) ?
                            ((random.nextLong() & 0xfffffffffffffL) - (random.nextLong() & 0xfffffffffffffL)) * 0x1p-53 + 0.5 :
                            twist(((random.nextLong() & 0xfffffffffffffL) - (random.nextLong() & 0xfffffffffffffL)) * 0x1p-52)))) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 9: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                LinnormRNG random = new LinnormRNG();
//                random.setCentrality(50);
//                random.setExpected(0.6);
                long centrality = NumberTools.doubleToLongBits(1.375) & 0xfffffffffffffL;
                double offset = 0.15, range = 0.6;
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(((random.nextLong() & 0xfffffffffffffL) * 0x1p-54 + offset + range * ((centrality > (random.nextLong() & 0xfffffffffffffL) ?
                            ((random.nextLong() & 0xfffffffffffffL) - (random.nextLong() & 0xfffffffffffffL)) * 0x1p-53 + 0.5 :
                            twist(((random.nextLong() & 0xfffffffffffffL) - (random.nextLong() & 0xfffffffffffffL)) * 0x1p-52)))) * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 10: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) (random.nextFloat() * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
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
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 12: {
                Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
                RandomXS128 random = new RandomXS128();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) (random.nextFloat() * 512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 13: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " LinnormRNG, random.nextInt(0x200)");
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[random.nextInt(512)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 14: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " XSP, mult128 & 0x1FFL");
                XSP random = new XSP();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((random.nextLongMult(0x1800000000000000L)) & 0x1FFL)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
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
//                        layers.put(i, j, color);
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.put(i, j, -0x1.7677e8p125F);
//                    }
//                }
            }
            break;
            case 15: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " XSP, bitBased & 0x1FFL");
                XSP random = new XSP();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((random.nextLongBit(0x1800000000000000L)) & 0x1FFL)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
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
//                        layers.put(i, j, color);
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.put(i, j, -0x1.7677e8p125F);
//                    }
//                }
            }
            break;
            case 16: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " XSP, traditional & 0x1FFL");
                XSP random = new XSP();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((random.nextLong(0x1800000000000000L)) & 0x1FFL)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
//                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
//                        " LinnormRNG, random.nextLong(0x1000000000000000L) & 0x1FFL");
//                LinnormRNG random = new LinnormRNG();
//                for (int i = 0; i < 0x1000000; i++) {
//                    amounts[(int) ((random.nextLong(0x1000000000000000L)) & 0x1FFL)]++;
//                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
//                        layers.put(i, j, color);
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.put(i, j, -0x1.7677e8p125F);
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
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 18: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, random.nextLong(0x1800000000000000L) & 0x1FFL");
                RandomXS128 random = new RandomXS128();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((random.nextLong(0x1800000000000000L)) & 0x1FFL)]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0
                            ? -0x1.c98066p126F // CW Azure
                            : -0x1.d08864p126F; // CW Sapphire
                    for (int j = 519 - (amounts[i] >> 8); j < 520; j++) {
                        layers.put(i, j, color);
                    }
                }
                for (int i = 0; i < 10; i++) {
                    for (int j = 8; j < 520; j += 32) {
                        layers.put(i, j, -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 19: {
                long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Halton[striding 1](2,3) sequence, first " + size + " points");
                int x, y;
                for (int i = 0; i < size; i++) {
                    x = (int) (VanDerCorputQRNG.determine2(i) * 512);
                    y = (int) (VanDerCorputQRNG.determine(3, i) * 512);
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.put(x, y, -0x1.7677e8p125F);
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.put(x, y, SColor.FLOAT_BLACK);
                }
            }
            break;
            case 20: {
                long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Halton[striding 1](2,39) sequence, first " + size + " points");
                int x, y;
                for (int i = 0; i < size; i++) {
                    x = (int) (VanDerCorputQRNG.determine2(i) * 512);
                    y = (int) (VanDerCorputQRNG.determine(39, i) * 512);
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.put(x, y, -0x1.7677e8p125F);
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.put(x, y, SColor.FLOAT_BLACK);
                }
            }
            break;
            case 21: {
                long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
                Gdx.graphics.setTitle("AltVDC(7) sequence, first " + size + " points");
                int a, x, y;
                for (int i = 0; i < size; i++) {
                    a = GreasedRegion.disperseBits((int) (VanDerCorputQRNG.altDetermine(7L, i) * 0x40000));
                    x = a & 0x1ff;
                    y = a >>> 16 & 0x1ff;
//                    a = GreasedRegion.disperseBits((int)(VanDerCorputQRNG.altDetermine(7L, i) * 0x4000));
//                    x = a & 0x7f;
//                    y = a >>> 16 & 0x7f;
                    if (layers.backgrounds[x][y] != 0f) {
                        layers.put(x, y, -0x1.7677e8p125F);
                        System.out.println("Overlap on index " + i);
                    } else
                        layers.put(x, y, SColor.FLOAT_BLACK);
                }
            }
            break;
            case 22: {
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
//                        layers.put(x, y, -0x1.7677e8p125F);
//                        System.out.println("Overlap on index " + i);
//                    }
//                    else
                    final float color = SColor.floatGetHSV(i * 0x1p-7f, 1f - i * 0x0.3p-7f, 0.7f, 1f);
                    layers.put(x, y, color);
                    layers.put(x + 1, y, color);
                    layers.put(x - 1, y, color);
                    layers.put(x, y + 1, color);
                    layers.put(x, y - 1, color);
                }
            }
            break;
            case 23:
                Arrays.fill(amounts, 0);
                long ctr = (System.nanoTime() >>> 24), xx, yy, seed;
                Gdx.graphics.setTitle("ClassicNoise 2D hash at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                for (int x = 0; x < 512; x++) {
                    xx = x + ctr;
                    for (int y = 0; y < 512; y++) {
                        yy = y + ctr;
                        //amounts[(int)((((seed = (((1234567L * (0x632BE59BD9B4E019L + (xx << 23))) ^ 0x9E3779B97F4A7C15L) * (0xC6BC279692B5CC83L + (yy << 23)))) ^ seed >>> 27 ^ xx + yy) * 0xAEF17502108EF2D9L)
                        //        >>> 55)]++;
                        amounts[((int)(((seed = 1234567L ^ 0xB4C4D * xx ^ 0xEE2C3 * yy) ^ seed >>> 13) * seed) >>> 24)]++;
                    }
                }
//                for (int i = 0; i < 512; i++) {
//                    float color = (i & 63) == 0
//                            ? -0x1.c98066p126F // CW Azure
//                            : -0x1.d08864p126F; // CW Sapphire
//                    for (int j = 519 - (amounts[i>>2] >> 4); j < 520; j++) {
//                        layers.put(i, j, color);
//                    }
//                }
//                for (int i = 0; i < 10; i++) {
//                    for (int j = 8; j < 520; j += 32) {
//                        layers.put(i, j, -0x1.7677e8p125F);
//                    }
//                }
                double[] angle;
                int x, y;
                float color;
                for (int t = 0; t < 256; t++) {
                    angle = PerlinNoise.phiGrad2[t];
                    color = (t & 4) == 4
                            ? -0x1.c98066p126F
                            : -0x1.d08864p126F;
                    for (int j = amounts[t] >> 3 & -4; j >= 128; j-=4) {
                        x = Noise.fastFloor(angle[0] * j + 260);
                        y = Noise.fastFloor(angle[1] * j + 260);
                        layers.put(x, y, color);
                        layers.put(x+1, y, color);
                        layers.put(x-1, y, color);
                        layers.put(x, y+1, color);
                        layers.put(x, y-1, color);
//                        layers.put(x+1, y+1, color);
//                        layers.put(x-1, y-1, color);
//                        layers.put(x-1, y+1, color);
//                        layers.put(x+1, y-1, color);
                    }
                    for (int j = Math.min(amounts[t] >> 3 & -4, 128); j >= 32; j-=4) {
                        x = Noise.fastFloor(angle[0] * j + 260);
                        y = Noise.fastFloor(angle[1] * j + 260);
                        layers.put(x, y, color);
//                        layers.put(x+1, y, color);
//                        layers.put(x-1, y, color);
//                        layers.put(x, y+1, color);
//                        layers.put(x, y-1, color);
                    }
                }

                break;
        }
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(1f, 1f, 1f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //layers.put(10, 10, '@');
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
