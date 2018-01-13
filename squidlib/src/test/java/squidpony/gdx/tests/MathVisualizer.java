package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.*;

import java.util.Arrays;

import static squidpony.squidmath.NumberTools.intBitsToFloat;

/**
 * Created by Tommy Ettinger on 1/13/2018.
 */
public class MathVisualizer extends ApplicationAdapter {
    private int mode = 0;
    private int modes = 8;
    private SpriteBatch batch;
    private SparseLayers layers;
    private InputAdapter input;
    private int[] amounts = new int[512];
    @Override
    public void create() {
        batch = new SpriteBatch();
        layers = new SparseLayers(512, 520, 1, 1, new TextCellFactory().includedFont());
        layers.setDefaultForeground(SColor.WHITE);
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                mode = (mode + 1) % modes;
                update();
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
        update();
    }
    public static float formCurvedFloat(long start) {
        return   (intBitsToFloat((int)((start = start * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) >>> 41) | 0x3F000000)
                + intBitsToFloat((int)((start = start * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) >>> 41) | 0x3F000000)
                + intBitsToFloat((int)((start = start * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) >>> 41) | 0x3F000000)
                + intBitsToFloat((int)((start * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) >>> 41) | 0x3F000000)
                - 3f);
    }

    public void update()
    {
        Arrays.fill(amounts, 0);
        ArrayTools.fill(layers.backgrounds, 0f);
        switch (mode)
        {
            case 0: {
                ThrustAltRNG random = new ThrustAltRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(NumberTools.formCurvedFloat(random.nextLong()) * 256) + 256]++;
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
            case 1: {
                ThrustAltRNG random = new ThrustAltRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor(NumberTools.formCurvedFloat(random.nextLong()) * 256) & 511]++;
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
                ThrustAltRNG random = new ThrustAltRNG();
                for (int i = 0; i < 0x1000000; i++) {
                    amounts[Noise.fastFloor((NumberTools.formDouble(random.nextLong()) *
                            NumberTools.formDouble(random.nextLong()) - NumberTools.formDouble(random.nextLong()) *
                            NumberTools.formDouble(random.nextLong())) * 256) + 256]++;
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
                ThrustAltRNG random = new ThrustAltRNG();
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
                    amounts[Noise.fastFloor(Math.nextAfter(random.biasedDouble(0.5, 512.0), 0.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 9); j < 520; j++) {
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
                    amounts[Noise.fastFloor(Math.nextAfter(random.biasedDouble(0.5, 512.0), 0.0))]++;
                }
                for (int i = 0; i < 512; i++) {
                    float color = (i & 63) == 0 ? -0x1.c98066p126F : -0x1.d08864p126F;
                    for (int j = 519 - (amounts[i] >> 9); j < 520; j++) {
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
                random.setCentrality(50);
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
                random.setCentrality(-50);
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
        config.width = 512;
        config.height = 520;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new MathVisualizer(), config);
    }

}
