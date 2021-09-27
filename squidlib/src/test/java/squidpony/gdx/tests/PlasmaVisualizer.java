package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidmath.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;
import static squidpony.squidgrid.gui.gdx.SColor.floatGet;

/**
 * Meant for demoing noise-like plasmas, such as {@link CosmicNumbering}.
 */
public class PlasmaVisualizer extends ApplicationAdapter {

    private ClassicNoise classic = new ClassicNoise(1234567890);
    private int noiseType = 0; // 0 for cosmic, 1 for warble
    private int dim = 0;
    private float freq = (float) Math.pow(1.618, -4.0);
    private boolean color = false;
    private long seed = 1234567890;
    private double[] connections = new double[3];

    private CosmicNumbering cosmos = new CosmicNumbering(seed, connections);
    private WarbleNoise warble = new WarbleNoise(seed);
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
                    case LEFT:
                        ctr--;
                        break;
                    case RIGHT:
                        ctr++;
                        break;
                    case E: //earlier seed
                        seed -= 0x9E3779B97F4A7C15L;
                        break;
                    case S: //seed
                        seed += 0x9E3779B97F4A7C15L;
                        break;
                    case MINUS:
                        noiseType = (noiseType + 1) & 1;
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        noiseType = (noiseType + 1) & 1;
                        break;
                    case F: // frequency
                        freq *= UIUtils.shift() ? 1.1f : 0.9f;
                        break;
                    case C: // color
                        color = !color;
                        break;
                    case K: // sKip
                        ctr += 1000;
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr;
        switch (noiseType) {
            case 0:
                if (color) {
                    Gdx.graphics.setTitle("Cosmic 3D Color Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                    bright = c * 0x5p-8f;
                    final double m = 0.25 * freq;
                    double s0 = NumberTools.swayRandomized(0x9E3779B97F4A7C15L, bright - 1.11f) * m;
                    double c0 = NumberTools.swayRandomized(0xC13FA9A902A6328FL, bright - 1.11f) * m;
                    double s1 = NumberTools.swayRandomized(0xD1B54A32D192ED03L, bright + 1.41f) * m;
                    double c1 = NumberTools.swayRandomized(0xDB4F0B9175AE2165L, bright + 1.41f) * m;
                    double s2 = NumberTools.swayRandomized(0xE19B01AA9D42C633L, bright + 2.61f) * m;
                    double c2 = NumberTools.swayRandomized(0xE60E2B722B53AEEBL, bright + 2.61f) * m;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            connections[0] = c * 0.007 + x * c0 - y * s0;
                            connections[1] = c * 0.009 - x * c1 + y * s1;
                            connections[2] = c * 0.013 + x * c2 + y * s2;

                            connections[0] = cosmos.getDoubleBase() + 0.5;
                            connections[1] = cosmos.getDoubleBase() + 0.5;
                            connections[2] = cosmos.getDoubleBase() + 0.5;
                            renderer.color(NumberTools.swayTight((float) connections[0]),
                                    NumberTools.swayTight((float) connections[1]),
                                    NumberTools.swayTight((float) connections[2]), 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }

                } else {
                    Gdx.graphics.setTitle("Cosmic 3D Noise at " + Gdx.graphics.getFramesPerSecond() + " FPS");
                    bright = c * 0x5p-8f;
                    final double m = 0.25 * freq;
                    double s0 = NumberTools.swayRandomized(0x9E3779B97F4A7C15L, bright - 1.11f) * m;
                    double c0 = NumberTools.swayRandomized(0xC13FA9A902A6328FL, bright - 1.11f) * m;
                    double s1 = NumberTools.swayRandomized(0xD1B54A32D192ED03L, bright + 1.41f) * m;
                    double c1 = NumberTools.swayRandomized(0xDB4F0B9175AE2165L, bright + 1.41f) * m;
                    double s2 = NumberTools.swayRandomized(0xE19B01AA9D42C633L, bright + 2.61f) * m;
                    double c2 = NumberTools.swayRandomized(0xE60E2B722B53AEEBL, bright + 2.61f) * m;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            connections[0] = c * 0.007 + x * c0 - y * s0;
                            connections[1] = c * 0.009 - x * c1 + y * s1;
                            connections[2] = c * 0.013 + x * c2 + y * s2;
                            connections[0] = cosmos.getDoubleBase() + 0.5;
                            connections[1] = cosmos.getDoubleBase() + 0.5;
                            connections[2] = cosmos.getDoubleBase() + 0.5;
                            bright = NumberTools.swayTight((float) connections[1]);//(float)connections[1] * 4f;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                }
                break;
            case 1:
                if (color) {
                    Gdx.graphics.setTitle("Warble3D Noise, color, one octave at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            alter3D(x * freq + c, y * freq + c, c + c);
                            warble.getNoise(connections[0], connections[1], connections[2]);
                            renderer.color(basicPrepare((float) warble.results[0]),
                                    basicPrepare((float) warble.results[1]),
                                    basicPrepare((float) warble.results[2]), 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                }
                else {
                    Gdx.graphics.setTitle("Warble3D Noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            alter3D(x * freq + c, y * freq + c, c + c);
                            bright =
                                    basicPrepare(warble.getNoise(connections[0], connections[1], connections[2])
                                    );
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                }
                break;
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
    private void alter3D(double x, double y, double ctr) {
        connections[0]  = x * 0.03125;
        connections[1]  = y * 0.03125;
        connections[2]  = ctr * 0.1375;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Test: Plasmas");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new PlasmaVisualizer(), config);
    }
}
