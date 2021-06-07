package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidmath.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class NoiseVarietyVisualizer extends ApplicationAdapter {

    private ClassicNoise classic = new ClassicNoise(1234567890);
    private FastNoise foam = new FastNoise(1234567890, 2f, FastNoise.FOAM, 1);
    private FastNoise fast = new FastNoise(1234567890, 1f, FastNoise.SIMPLEX, 1);
    private FastNoise value = new FastNoise(1234567890, 1f, FastNoise.VALUE, 1);
    private int noiseType = 0; // 0 for classic, 1 for wave, 2 for fast, 3 for experimental
    private int dim = 0; // this can be 0, 1, or 2; add 2 to get the actual dimensions
    private int octaves = 0;
    private float freq = (float) Math.exp(-4.0);
    private int alternate = 0;
    private long seed = 1234567890;

    private Noise.Noise2D current2 = new Noise.Layered2D(classic, octaves + 1, freq);
    private Noise.Noise3D current3 = new Noise.Layered3D(classic, octaves + 1, freq);
    private Noise.Noise4D current4 = new Noise.Layered4D(classic, octaves + 1, freq);

    private ImmediateModeRenderer20 renderer;

    private static final int width = 512, height = 512;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;

    public static float basicPrepare(double n)
    {
        return (float) n * 0.5f + 0.5f;
    }

    private void refresh() {
        switch (alternate) {
            case 0: {
                switch (noiseType) {
                    case 0:
                        current2 = new Noise.Layered2D(classic, octaves + 1, freq);
                        current3 = new Noise.Layered3D(classic, octaves + 1, freq);
                        current4 = new Noise.Layered4D(classic, octaves + 1, freq);
                        break;
                    case 1:
                        current2 = new Noise.Layered2D(foam, octaves + 1, freq);
                        current3 = new Noise.Layered3D(foam, octaves + 1, freq);
                        current4 = new Noise.Layered4D(foam, octaves + 1, freq);
                        break;
                    case 2:
                        current2 = new Noise.Layered2D(fast, octaves + 1, freq);
                        current3 = new Noise.Layered3D(fast, octaves + 1, freq);
                        current4 = new Noise.Layered4D(fast, octaves + 1, freq);
                        break;
                    case 3:
                        current2 = new Noise.Layered2D(value, octaves + 1, freq);
                        current3 = new Noise.Layered3D(value, octaves + 1, freq);
                        current4 = new Noise.Layered4D(value, octaves + 1, freq);
                        break;
                }
            }
            break;
            case 1: {
                switch (noiseType) {
                    case 0:
                        current2 = new Noise.LayeredSpiral2D(classic, octaves + 1, freq);
                        current3 = new Noise.LayeredSpiral3D(classic, octaves + 1, freq);
                        current4 = new Noise.LayeredSpiral4D(classic, octaves + 1, freq);
                        break;
                    case 1:
                        current2 = new Noise.LayeredSpiral2D(foam, octaves + 1, freq);
                        current3 = new Noise.LayeredSpiral3D(foam, octaves + 1, freq);
                        current4 = new Noise.LayeredSpiral4D(foam, octaves + 1, freq);
                        break;
                    case 2:
                        current2 = new Noise.LayeredSpiral2D(fast, octaves + 1, freq);
                        current3 = new Noise.LayeredSpiral3D(fast, octaves + 1, freq);
                        current4 = new Noise.LayeredSpiral4D(fast, octaves + 1, freq);
                        break;
                    case 3:
                        current2 = new Noise.LayeredSpiral2D(value, octaves + 1, freq);
                        current3 = new Noise.LayeredSpiral3D(value, octaves + 1, freq);
                        current4 = new Noise.LayeredSpiral4D(value, octaves + 1, freq);
                        break;
                }
            }
            break;
            case 2: {
                switch (noiseType) {
                    case 0:
                        current2 = new Noise.Ridged2D(classic, octaves + 1, freq);
                        current3 = new Noise.Ridged3D(classic, octaves + 1, freq);
                        current4 = new Noise.Ridged4D(classic, octaves + 1, freq);
                        break;
                    case 1:
                        current2 = new Noise.Ridged2D(foam, octaves + 1, freq);
                        current3 = new Noise.Ridged3D(foam, octaves + 1, freq);
                        current4 = new Noise.Ridged4D(foam, octaves + 1, freq);
                        break;
                    case 2:
                        current2 = new Noise.Ridged2D(fast, octaves + 1, freq);
                        current3 = new Noise.Ridged3D(fast, octaves + 1, freq);
                        current4 = new Noise.Ridged4D(fast, octaves + 1, freq);
                        break;
                    case 3:
                        current2 = new Noise.Ridged2D(value, octaves + 1, freq);
                        current3 = new Noise.Ridged3D(value, octaves + 1, freq);
                        current4 = new Noise.Ridged4D(value, octaves + 1, freq);
                        break;
                }
            }
            break;

        }
    }
    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();

        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case P: //pause
                        keepGoing = !keepGoing;
                    case C:
                        ctr++;
                        break;
                    case E: //earlier seed
                        seed -= 0x9E3779B97F4A7C15L;
                        break;
                    case S: //seed
                        seed += 0x9E3779B97F4A7C15L;
                        break;
                    case MINUS:
                        noiseType = (noiseType + 3) & 3;
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        noiseType = (noiseType + 1) & 3;
                        break;
                    case D: //dimension
                        dim = (dim + 1) % 3;
                        break;
                    case F: // frequency
                        freq = ((float) Math.exp((System.currentTimeMillis() >>> 9 & 7) - 5));
                        break;
                    case A:
                    case R: // ridged
                        alternate = (alternate + 1) % 3;
                        break;
                    case H: // higher octaves
                        octaves = octaves + 1 & 7;
                        break;
                    case L: // lower octaves
                        octaves = octaves + 7 & 7;
                        break;
                    case K: // sKip
                        ctr += 1000;
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                refresh();
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 0.5f;
        if(true) {
//        if(noiseType != 3) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(current2.getNoiseWithSeed(x + ctr, y + ctr, seed));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(current3.getNoiseWithSeed(x, y, c, seed));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(current4.getNoiseWithSeed(x, y, ctr, 0x1p-4f * (x + y - ctr), seed));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            final double bump = current2.getNoiseWithSeed((x + ctr) * 0.25, (y+ctr) * 0.25, ~seed);
                            bright = (float) (Math.pow(2.0 - 2.0 * Math.abs(current2.getNoiseWithSeed(x + ctr, y + ctr, seed)),
                                    1.25 + 0.75 * bump) + bump * 0.5 + 0.5) * 0.2f;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            final double bump = current3.getNoiseWithSeed(x * 0.25, y * 0.25, c * 0.25, ~seed);
                            bright = (float) (Math.pow(2.0 - 2.0 * Math.abs(current3.getNoiseWithSeed(x, y, c, seed)),
                                    1.25 + 0.75 * bump) + bump * 0.5 + 0.5) * 0.2f;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            final double bump = current4.getNoiseWithSeed(x * 0.25, y * 0.25, ctr * 0.25, 0x1p-6 * (x + y - ctr), ~seed);
                            bright = (float) (Math.pow(2.0 - 2.0 * Math.abs(current4.getNoiseWithSeed(x, y, ctr, 0x1p-4f * (x + y - ctr), seed)),
                                    1.25 + 0.75 * bump) + bump * 0.5 + 0.5) * 0.2f;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        renderer.end();

    }

    @Override
    public void render() {
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            // standard clear the background routine for libGDX
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            ctr++;
            putMap();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Test: OpenSimplex Noise");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new NoiseVarietyVisualizer(), config);
    }
}
