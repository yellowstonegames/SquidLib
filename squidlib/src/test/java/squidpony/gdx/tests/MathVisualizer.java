package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
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
    private int mode = 19;
    private int modes = 23;
    private SpriteBatch batch;
    private SparseLayers layers;
    private InputAdapter input;
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

    @Override
    public void create() {
        batch = new SpriteBatch();
        layers = new SparseLayers(512, 520, 2, 2, new TextCellFactory().includedFont());
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
        update();
    }

    public void update()
    {
        Arrays.fill(amounts, 0);
        ArrayTools.fill(layers.backgrounds, 0f);
        Gdx.graphics.setTitle("Math Visualizer: Mode " + mode);
        switch (mode)
        {
            case 0: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 1: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 2:  {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 3:  {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 4: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 5: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 6: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 7: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 8: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 9: {
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 10: {
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int)(random.nextFloat() * 512)]++;
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 11: {
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int)(MathUtils.random() * 512)]++;
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 12: {
                RandomXS128 random = new RandomXS128();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int)(random.nextFloat() * 512)]++;
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
                        layers.put(i, j,  -0x1.7677e8p125F);
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 14: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " MathUtils.random(0x1FF)");
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[MathUtils.random(0x1FF)]++;
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 15: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, random.nextInt(0x200)");
                RandomXS128 random = new RandomXS128();
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 16: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " LinnormRNG, random.nextLong(0x1000000000000000L) & 0x1FFL");
                LinnormRNG random = new LinnormRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((random.nextLong(0x1000000000000000L)) & 0x1FFL)]++;
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 18: {
                Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() +
                        " RandomXS128, random.nextLong(0x1000000000000000L) & 0x1FFL");
                RandomXS128 random = new RandomXS128();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[(int) ((random.nextLong(0x1000000000000000L)) & 0x1FFL)]++;
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
                        layers.put(i, j,  -0x1.7677e8p125F);
                    }
                }
            }
            break;
            case 19: {
                long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
                Gdx.graphics.setTitle("Halton[striding 1](2,3) sequence, first " + size + " points");
                int x, y;
                for (int i = 0; i < size; i++) {
                    x = (int)(VanDerCorputQRNG.determine2(i) * 512);
                    y = (int)(VanDerCorputQRNG.determine(3, i) * 512);
                    if(layers.backgrounds[x][y] != 0f)
                    {
                        layers.put(x, y, -0x1.7677e8p125F);
                        System.out.println("Overlap on index " + i);
                    }
                    else
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
                    a = GreasedRegion.disperseBits((int)(VanDerCorputQRNG.altDetermine(7L, i) * 0x40000));
                    x = a & 0x1ff;
                    y = a >>> 16 & 0x1ff;
//                    a = GreasedRegion.disperseBits((int)(VanDerCorputQRNG.altDetermine(7L, i) * 0x4000));
//                    x = a & 0x7f;
//                    y = a >>> 16 & 0x7f;
                    if(layers.backgrounds[x][y] != 0f)
                    {
                        layers.put(x, y, -0x1.7677e8p125F);
                        System.out.println("Overlap on index " + i);
                    }
                    else
                        layers.put(x, y, SColor.FLOAT_BLACK);
                }
            }
            break;
            case 22: {
                long size = (System.nanoTime() >>> 22 & 0xfff) + 1L;
                Gdx.graphics.setTitle("AltHalton(777) sequence, first " + size + " points");
                int a, x, y, p2 = 777 * 0x2C9277B5 | 1;
                //int lfsr = 7;
                // (lfsr = (lfsr >>> 1 ^ (-(lfsr & 1) & 0x3802))) // 0x20400 is 18-bit // 0xD008 is 16-bit // 0x3802 is 14-bit
                for (int i = 0; i < size; i++) {
                    a = GreasedRegion.disperseBits(Integer.reverse(p2 * (i + 1))); //^ 0xAC564B05
                    x = a >>> 7 & 0x1ff;
                    y = a >>> 23 & 0x1ff;
//                    x = a >>> 9 & 0x7f;
//                    y = a >>> 25 & 0x7f;
                    if(layers.backgrounds[x][y] != 0f)
                    {
                        layers.put(x, y, -0x1.7677e8p125F);
                        System.out.println("Overlap on index " + i);
                    }
                    else
                        layers.put(x, y, SColor.FLOAT_BLACK);
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
        batch.begin();
        layers.draw(batch, 1f);
        batch.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Visualizer for Math Testing/Checking";
        config.width = 512 * 2;
        config.height = 520 * 2;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new MathVisualizer(), config);
    }

}
