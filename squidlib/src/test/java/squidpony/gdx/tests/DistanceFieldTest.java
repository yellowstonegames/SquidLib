package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.ThrustAltRNG;

import java.util.List;

/**
 * This test allows you to compare the appearance of an SDF font, {@link DefaultResources#getStretchableSlabFont()},
 * with an MSDF font, {@link DefaultResources#getCrispSlabFont()}. They should look very close except at high and low
 * zoom levels, where SDF may start to get blurry and MSDF may show artifacts. Neither is amazing at extremely small
 * sizes, though. The MSDF font is shown first as font 1/2, and the SDF font after you press any key as font 2/2.
 * <br>
 * Created by Tommy Ettinger on 12/27/2016.
 */
public class DistanceFieldTest extends ApplicationAdapter {
    /**
     * In number of cells
     */
    private static int width = 70;
    /**
     * In number of cells
     */
    private static int height = 13;
    
    private static int totalWidth = 1000, totalHeight = 500;

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private Vector3 startPosition, targetPosition;
    private float worldWidth, worldHeight;
    private TextCellFactory[] factories;
    private SparseLayers display;
    private SparseLayers[] displays;
    private int index = 0;
    @Override
    public void create() {
        batch = new SpriteBatch();
        factories = new TextCellFactory[]{
                DefaultResources.getCrispSlabFont().width(32).height(64).initBySize(),
                DefaultResources.getStretchableSlabFont().width(32).height(64).initBySize(),
        };
        //factories[0].font().setUseIntegerPositions(true);
        //factories[1].font().setUseIntegerPositions(true);
        worldWidth = factories[0].width() * width;
        worldHeight = factories[0].height() * height;
        viewport = new StretchViewport(worldWidth, worldHeight);
        displays = new SparseLayers[]{
                new SparseLayers(width, height, 16, 32, factories[0]),
                new SparseLayers(width, height, 16, 32, factories[1]),
        };
        StringBuilder sb = new StringBuilder(width * height);
        long seed = System.nanoTime();
        FakeLanguageGen lang = FakeLanguageGen.randomLanguage(seed);
        while (sb.length() < width * (height - 2))
        {
            sb.append(lang.sentence(seed += 1337, 4, 9)).append(' ');
        }
        List<String> wrapped = StringKit.wrap(sb, width);
        for (int i = 0; i < factories.length; i++) {
            display = displays[i];
            int y = 0;
            for(String s : wrapped)
            {
                display.put(0, y++, s, SColor.WHITE);
                if (y >= height) {
                    break;
                }
            }
        }
        display = displays[index];
        stage = new Stage(viewport, batch);

        startPosition = new Vector3(display.getX(), display.getY(), 0f);
        targetPosition = new Vector3(
                (int) ((LightRNG.determineFloat(System.nanoTime()) * 0.5f - 0f) * worldWidth),
                (int) ((LightRNG.determineFloat(ThrustAltRNG.determine(System.nanoTime())) * -0.5f + 0f) * worldHeight),
                0f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                    index = ((index + 1) % factories.length);                     
                    display = displays[index];
                    stage.clear();
                    stage.addActor(display);
                Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index+1) + "/" + factories.length + " (press any key)");
                return true;
            }
        });
        Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index+1) + "/" + factories.length + " (press any key)");

        stage.addActor(display);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        final long time = (System.currentTimeMillis() & 0x3FFFL);
//        final float zoom = MathUtils.clamp((time & 0x1FFFL) * 0x1.7p-13f - 0x0.7p-14f, 0.0f, 1f);
//        if((time & 0x2000L) == 0L) {
        final long time = (System.nanoTime() >>> 21);
        final float zoom = MathUtils.clamp((time & 0x7FFL) * 0x3p-12f - 0.25f, 0.0f, 1f);
        if((time & 0x800L) == 0x800L) {
            viewport.setWorldSize(worldWidth * (0.25f + zoom * 2f), worldHeight * (0.25f + zoom * 2f));
            display.setPosition(MathUtils.lerp(targetPosition.x, startPosition.x, zoom),
                    MathUtils.lerp(targetPosition.y, startPosition.y, zoom));
        }
        else {
            viewport.setWorldSize(worldWidth * (2.25f - zoom * 2f), worldHeight * (2.25f - zoom * 2f));
            display.setPosition(MathUtils.lerp(startPosition.x, targetPosition.x, zoom),
                    MathUtils.lerp(startPosition.y, targetPosition.y, zoom));
            if (zoom <= 0.001f) {
                targetPosition.set(
                        (int) ((LightRNG.determineFloat(System.nanoTime()) - 0.5f) * worldWidth),
                        (int) ((-LightRNG.determineFloat(ThrustAltRNG.determine(System.nanoTime())) + 0.5) * worldHeight),
                        0f);
            }
        }
        viewport.update(totalWidth, totalHeight, false);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        totalWidth = width;
        totalHeight = height;
        viewport.update(width, height, false);
    }
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Fonts, preview 1/2 (press any key)";
//        config.width = totalWidth = LwjglApplicationConfiguration.getDesktopDisplayMode().width - 10;
//        config.height = totalHeight = LwjglApplicationConfiguration.getDesktopDisplayMode().height - 128;
        config.width = totalWidth = 1100;
        config.height = totalHeight = 550;
        config.x = 0;
        config.y = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DistanceFieldTest(), config);
    }

}