package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.NumberTools;

import static squidpony.squidgrid.gui.gdx.SColor.FLOAT_WHITE;
import static squidpony.squidgrid.gui.gdx.SColor.floatGet;
import static squidpony.squidmath.NumberTools.swayTight;

/**
 */
public class FastNoiseVisualizer extends ApplicationAdapter {

    private FastNoise noise = new FastNoise(1);
    private static final int MODE_LIMIT = 1;
    private int mode = 0;
    private int dim = 0; // this can be 0, 1, or 2; add 2 to get the actual dimensions
    private int octaves = 3;
    private float freq = 0.125f;
    private boolean inverse = false;
    private SpriteBatch batch;
    //private SparseLayers display;//, overlay;
    
    private TextCellFactory tcf;
    private static final int width = 512, height = 512;
    private static final float[][] back = new float[width][height];

    private SquidInput input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;

    private double total = 0.0;

    public static float prepare(double n)
    {
        //return (float)n * 0.5f + 0.5f;
        return swayTight((float)n * 1.5f + 0.5f);
    }

    public static float prepare(float n)
    {
        //return (n * 0.5f) + 0.5f;
        return swayTight(n * 1.5f + 0.5f);
    }

    public static float prepare(double n, float multiplier)
    {
        //return (float)n * 0.5f + 0.5f;
        return swayTight((float)n * multiplier + 0.5f);
    }

    public static float prepare(float n, float multiplier)
    {
        //return (n * 0.5f) + 0.5f;
        return swayTight(n * multiplier + 0.5f);
    }

    public static float basicPrepare(double n)
    {
        return (float)n * 0.5f + 0.5f;
    }

    public static float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        tcf = new TextCellFactory().includedFont().width(1).height(1).initBySize();
        view = new ScreenViewport();
        ArrayTools.fill(back, FLOAT_WHITE);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case '-':                         
                        mode = (mode + MODE_LIMIT - 1) % MODE_LIMIT;
                        break;
                    case 'u':
                    case 'U':
                    case SquidInput.ENTER:                         
                        if (key == SquidInput.ENTER) {
                            mode++;
                            mode %= MODE_LIMIT;
                            ctr = -256;
                        }
                        putMap();
                        break;
                    case 'C':
                    case 'c':
                        ctr++;
                        if(alt) keepGoing = !keepGoing;
                        putMap();
                        break;
                    case 'E':
                        noise.setSeed(noise.getSeed() - 1);
                        putMap();
                        break;
                    case 'e':
                        noise.setSeed(noise.getSeed() + 1);
                        putMap();
                        break;
                    case 'N':
                    case 'n':
                        noise.setNoiseType((noise.getNoiseType() + 1) % 10);
                        putMap();
                        break;
                    case 'D':
                    case 'd':
                        dim = (dim+1) % 3;
                        putMap();
                        break;
                    case 'F':
                    case 'f':
                        noise.setFrequency(NumberTools.swayTight(freq += 0.125f) * 0.25f + 0x1p-7f);
                        putMap();
                        break;
                    case 'R':
                    case 'r':
                        noise.setFractalType((noise.getFractalType() + 1) % 3);
                        putMap();
                        break;
                    case 'H':
                    case 'h':
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        putMap();
                        break;
                    case 'L':
                    case 'l':
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        putMap();
                        break;
                    case 'i':
                    case 'I':
                        if(inverse = !inverse) {
                            noise.setFractalLacunarity(0.5f);
                            noise.setFractalGain(2f);
                        }
                        else {
                            noise.setFractalLacunarity(2f);
                            noise.setFractalGain(0.5f);
                        }
                        putMap();
                        break;
                    case 'K': // sKip
                    case 'k':
                        ctr += 1000;
                        putMap();
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        input.setRepeatGap(Long.MAX_VALUE);
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        putMap();
    }

    public void putMap() {
        float bright;
        switch (dim) {
            case 0:
                for (int x = 0; x < 512; x++) {
                    for (int y = 0; y < 512; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x + ctr, y + ctr));
                        back[x][y] = floatGet(bright, bright, bright, 1f);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < 512; x++) {
                    for (int y = 0; y < 512; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, ctr));
                        back[x][y] = floatGet(bright, bright, bright, 1f);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < 512; x++) {
                    for (int y = 0; y < 512; y++) {
                        bright = basicPrepare(noise.getNoise(x - ctr, y - ctr, x + y, y - x + ctr));
                        back[x][y] = floatGet(bright, bright, bright, 1f);
                    }
                }
                
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        if (keepGoing) {
            ctr++;
            putMap();
        }
        // if the user clicked, we have a list of moves to perform.

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
        batch.begin();
        tcf.draw(batch, back, 0, 0);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
        //display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        //Gdx.graphics.requestRendering();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Hash Visualization";
        config.width = width;
        config.height = height;
        config.foregroundFPS = 0;
        config.vSyncEnabled = false;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new FastNoiseVisualizer(), config);
    }
}
