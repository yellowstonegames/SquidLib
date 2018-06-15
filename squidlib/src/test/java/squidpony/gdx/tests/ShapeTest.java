package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidmath.GapShuffler;
import squidpony.squidmath.MaskedShapeGenerator;
import squidpony.squidmath.ThrustAltRNG;

/**
 * Created by Tommy Ettinger on 10/12/2017.
 */
public class ShapeTest extends ApplicationAdapter {
    private SpriteBatch batch;
    private SparseLayers display;//, overlay;
    private static final int width = 96, height = 96;
    private static final int cellWidth = 6, cellHeight = 6;
    private SquidInput input;
    private Stage stage;
    private Viewport view;
    private MaskedShapeGenerator gen;
    private int[][] data;
    private float[][] colorChoices;
    private long counter = 0L;
    private GapShuffler<Integer> shuffler;
    @Override
    public void create() {
        gen = new MaskedShapeGenerator();
        batch = new SpriteBatch();
        counter = System.currentTimeMillis();
        shuffler = SColor.randomHueSequence();
        data = new int[width][height];
        colorChoices = ArrayTools.fill(SColor.FLOAT_BLACK, 6, 24);
        display = new SparseLayers(width, height, cellWidth, cellHeight, DefaultResources.getStretchableHeavySquareFont());
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        stage = new Stage(view, batch);
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        //overlay.setPosition(0, 0);
        //Stack stk = new Stack(display, overlay);
        //stage.addActor(stk);
        //stk.layout();
        stage.addActor(display);
        putMap();
    }
    public void putMap()
    {
        //int t;
        ArrayTools.fill(data, 0);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {
                gen.generateIntoShaded8way(data, x * 16 + 2, y * 16 + 2);
                int sat = ThrustAltRNG.determineBounded(++counter + (x << 3) + y, 3),
                hue = shuffler.next();
                colorChoices[x][y<<2|1] = SColor.COLOR_WHEEL_PALETTES[sat][hue].toFloatBits();
                colorChoices[x][y<<2|2] = SColor.COLOR_WHEEL_PALETTES[sat + 3][hue].toFloatBits();
                colorChoices[x][y<<2|3] = SColor.COLOR_WHEEL_PALETTES[sat + 6][hue].toFloatBits();
                //colorChoices[x][y] = ((t = counter + x + 6 * y) & 15) | (ThrustAltRNG.determineBounded(t, 3) << 4);
//                colorChoices[x][y] = ((counter + x + 36 * y) * 13 % 21) * 9
//                        + ThrustAltRNG.determineBounded(counter + x + 36 * y, 3);
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                switch (data[x][y])
                {
                    case 0:
                        display.backgrounds[x][y] = 0f;
                        break;
                    default: display.backgrounds[x][y] = colorChoices[x >> 4][(y >> 4) << 2 | (data[x][y] - 1)];
/*                    case 1:
                        display.backgrounds[x][y] = SColor.FLOAT_BLACK;
                        break;
                    case 2:
                        //display.backgrounds[x][y] = SColor.COLOR_WHEEL_PALETTE[colorChoices[x >> 4][y >> 4] + 6].toFloatBits();
                        display.backgrounds[x][y] = SColor.indexedColorWheel((t = colorChoices[x >> 4][y >> 4]) & 15, 2, t >> 4).toFloatBits();
                        break;
                    case 3:
                        display.backgrounds[x][y] = SColor.indexedColorWheel((t = colorChoices[x >> 4][y >> 4]) & 15, 1, t >> 4).toFloatBits();
                                //SColor.COLOR_WHEEL_PALETTE[colorChoices[x >> 4][y >> 4]].toFloatBits();
                        break;
                    case 4:
                        display.backgrounds[x][y] = SColor.indexedColorWheel((t = colorChoices[x >> 4][y >> 4]) & 15, 0, t >> 4).toFloatBits();
                        //SColor.COLOR_WHEEL_PALETTE[colorChoices[x >> 4][y >> 4] + 3].toFloatBits();
                        break;
                        */
                }
            }
        }
        //Gdx.graphics.setTitle("SquidLib Test: Procedural Spaceships at " + Gdx.graphics.getFramesPerSecond() + " FPS");
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0.75f, 0.75f, 0.75f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        putMap();

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        stage.act();
        // stage has its own batch and must be explicitly told to draw().
        stage.draw();
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
        config.title = "SquidLib Test: Procedural Spaceships";
        config.width = width * cellHeight;
        config.height = height * cellHeight;
        config.foregroundFPS = 2;
        config.vSyncEnabled = true;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new ShapeTest(), config);
    }
}

