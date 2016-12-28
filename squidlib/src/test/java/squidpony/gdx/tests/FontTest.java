package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidgrid.gui.gdx.TextCellFactory;

/**
 * Created by Tommy Ettinger on 12/27/2016.
 */
public class FontTest extends ApplicationAdapter {
    /**
     * In number of cells
     */
    private static final int width = 120;
    /**
     * In number of cells
     */
    private static final int height = 24;
    /**
     * The pixel width of a cell
     */
    private static int cellWidth = 13;
    /**
     * The pixel height of a cell
     */
    private static int cellHeight = 30;

    private static int totalWidth = 1346, totalHeight = 700;

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private Viewport[] viewports;
    private TextCellFactory tcf;
    private TextCellFactory[] factories;
    private SquidPanel display;
    private SquidPanel[] displays;
    private int index = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        factories = new TextCellFactory[]{
                DefaultResources.getStretchableFont().width(13).height(30).initBySize(),
                DefaultResources.getStretchableTypewriterFont().width(14).height(28).initBySize(),
                DefaultResources.getStretchableCodeFont().width(15).height(27).initBySize(),
                DefaultResources.getStretchableDejaVuFont().width(14).height(25).initBySize(),
                DefaultResources.getStretchableSciFiFont().width(18).height(32).initBySize(),
        };
        viewports = new Viewport[]{
                new StretchViewport(factories[0].width() * width, factories[0].height() * height),
                new StretchViewport(factories[1].width() * width, factories[1].height() * height),
                new StretchViewport(factories[2].width() * width, factories[2].height() * height),
                new StretchViewport(factories[3].width() * width, factories[3].height() * height),
                new StretchViewport(factories[4].width() * width, factories[4].height() * height),
        };
        displays = new SquidPanel[]{
                new SquidPanel(width, height, factories[0]),
                new SquidPanel(width, height, factories[1]),
                new SquidPanel(width, height, factories[2]),
                new SquidPanel(width, height, factories[3]),
                new SquidPanel(width, height, factories[4]),
        };
        for (int i = 4; i >= 0; i--) {
            tcf = factories[i];
            display = displays[i];
            BitmapFont.BitmapFontData data = tcf.font().getData();
            int p = 0, x = 0, y = 0;
            BitmapFont.Glyph[] glyphs;
            BitmapFont.Glyph g;
            ALL_PAGES:
            while (p < data.glyphs.length) {
                glyphs = data.glyphs[p++];
                if(glyphs == null) continue;
                int gl = glyphs.length;
                for (int gi = 0; gi < gl; gi++) {
                    if ((g = glyphs[gi]) != null) {
                        display.put(x++, y, (char) g.id);
                        if (x >= width) {
                            x = 0;
                            if (++y >= height) {
                                break ALL_PAGES;
                            }
                        }
                    }
                }
            }
        }
        tcf = factories[index];
        display = displays[index];
        viewport = viewports[index];
        cellWidth = tcf.width();
        cellHeight = tcf.height();
        stage = new Stage(viewport, batch);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                viewport = viewports[index = (index + 1) % 5];
                tcf = factories[index];
                display = displays[index];
                stage.clear();
                stage.setViewport(viewport);
                stage.addActor(display);
                Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index+1) + "/5 (press any key)");
                return true;
            }
        });
        Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index+1) + "/5 (press any key)");

        stage.addActor(display);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.getViewport().update(totalWidth, totalHeight, true);
        stage.getViewport().apply(true);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        totalWidth = width;
        totalHeight = height;
        stage.getViewport().update(width, height, true);
    }
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Fonts, preview 1/5 (press any key)";
        config.width = totalWidth = LwjglApplicationConfiguration.getDesktopDisplayMode().width - 10;
        config.height = totalHeight = LwjglApplicationConfiguration.getDesktopDisplayMode().height - 128;
        config.x = 0;
        config.y = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new FontTest(), config);
    }

}