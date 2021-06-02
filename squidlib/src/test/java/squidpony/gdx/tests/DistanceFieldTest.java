package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.squidgrid.gui.gdx.*;

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
    private static int height = 14;
    
    private static int totalWidth = 1100, totalHeight = 550;

    private Stage stage;
    private FilterBatch batch;
    private Viewport viewport;
    private Vector3 startPosition, targetPosition;
    private float worldWidth, worldHeight;
    private TextCellFactory[] factories;
    private TextCellFactory factory;
    private SparseLayers display;
    private SparseLayers[] displays;
    private int index;
    @Override
    public void create() {
        batch = new FilterBatch();
        factories = new TextCellFactory[]{
                DefaultResources.getCrispLeanFont().width(32).height(64).initBySize(),//.setSmoothingMultiplier(1f),
                DefaultResources.getStretchableLeanFont().width(32).height(64).initBySize(),
        };
        factory = factories[0];
        factories[0].font().setUseIntegerPositions(false);
        factories[1].font().setUseIntegerPositions(false);
        worldWidth = factory.width() * width;
        worldHeight = factory.height() * height;
        viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        displays = new SparseLayers[]{
                new SparseLayers(width, height, 32, 64, factories[0]),
                new SparseLayers(width, height, 32, 64, factories[1]),
        };
        StringBuilder sb = new StringBuilder(width * height);
        long seed = 123456789012345L;//System.nanoTime();
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

        display.setPosition(0f, 0f, Align.center);
        startPosition = new Vector3(display.getX(Align.center), display.getY(Align.center), 0f);
        targetPosition = new Vector3(
                (0.625f * worldWidth),
                (0.625f * worldHeight),
                0f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.SHIFT_LEFT ||
                        keycode == Input.Keys.CONTROL_LEFT ||
                        keycode == Input.Keys.ALT_LEFT ||
                        keycode == Input.Keys.PRINT_SCREEN)
                    return false;
                index = ((index + 1) % factories.length);
                display = displays[index];
                stage.clear();
                stage.addActor(display);
                Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index + 1) + "/" + factories.length + " (press any key)");
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
        final long time = (System.nanoTime() >>> 22);
        final float zoom = MathUtils.clamp((time & 0x7FFL) * 0x3p-12f - 0.25f, 0.0f, 1f);
        if((time & 0x800L) == 0x800L) {
            display.font.resetSize(32f * (0.25f + zoom * 2f), 64f * (0.25f + zoom * 2f));
            //viewport.setWorldSize(worldWidth * (0.25f + zoom * 2f), worldHeight * (0.25f + zoom * 2f));
            display.setPosition(MathUtils.lerp(targetPosition.x, startPosition.x, zoom),
                    MathUtils.lerp(targetPosition.y, startPosition.y, zoom), Align.center);
        }
        else {
            display.font.resetSize(32f * (2.25f - zoom * 2f), 64f * (2.25f - zoom * 2f));
//            viewport.setWorldSize(worldWidth * (2.25f - zoom * 2f), worldHeight * (2.25f - zoom * 2f));
            display.setPosition(MathUtils.lerp(startPosition.x, targetPosition.x, zoom),
                    MathUtils.lerp(startPosition.y, targetPosition.y, zoom), Align.center);
//            if (zoom <= 0.001f) {
//                targetPosition.set(
//                        (int) ((LightRNG.determineFloat(System.nanoTime()) + 1f) * worldWidth),
//                        (int) ((-LightRNG.determineFloat(ThrustAltRNG.determine(System.nanoTime())) - 1f) * worldHeight),
//                        0f);
//            }
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
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Fonts, preview 1/2 (press any key)");
//        config.width = totalWidth = Lwjgl3ApplicationConfiguration.getDesktopDisplayMode().width - 10;
//        config.height = totalHeight = Lwjgl3ApplicationConfiguration.getDesktopDisplayMode().height - 128;
        config.useVsync(true);
        config.setWindowedMode(totalWidth = 1100, totalHeight = 550);
        config.setWindowPosition(40, 40);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new DistanceFieldTest(), config);
    }

}
