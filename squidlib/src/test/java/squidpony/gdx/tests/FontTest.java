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
    private static int[] widths;
    /**
     * In number of cells
     */
    private static int[] heights;

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
        widths = new int[]{100, 95, 90, 110, 95, 50};
        heights = new int[]{20, 21, 20, 28, 18, 20};
        factories = new TextCellFactory[]{
                DefaultResources.getStretchableFont().width(13).height(30).initBySize(),
                DefaultResources.getStretchableTypewriterFont().width(14).height(28).initBySize(),
                DefaultResources.getStretchableCodeFont().width(15).height(27).initBySize(),
                DefaultResources.getStretchableDejaVuFont().width(14).height(25).initBySize(),
                DefaultResources.getStretchableSciFiFont().width(28).height(64).initBySize(),
                DefaultResources.getStretchableSquareFont().width(17).height(17).initBySize(),
        };
        viewports = new Viewport[]{
                new StretchViewport(factories[0].width() * widths[0], factories[0].height() * heights[0]),
                new StretchViewport(factories[1].width() * widths[2], factories[1].height() * heights[1]),
                new StretchViewport(factories[2].width() * widths[2], factories[2].height() * heights[2]),
                new StretchViewport(factories[3].width() * widths[3], factories[3].height() * heights[3]),
                new StretchViewport(factories[4].width() * widths[4], factories[4].height() * heights[4]),
                new StretchViewport(factories[5].width() * widths[5], factories[5].height() * heights[5]),
        };
        displays = new SquidPanel[]{
                new SquidPanel(widths[0], heights[0], factories[0]),
                new SquidPanel(widths[2], heights[1], factories[1]),
                new SquidPanel(widths[2], heights[2], factories[2]),
                new SquidPanel(widths[3], heights[3], factories[3]),
                new SquidPanel(widths[4], heights[4], factories[4]),
                new SquidPanel(widths[5], heights[5], factories[5]),
        };
        for (int i = 0; i < factories.length; i++) {
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
                        if (x >= widths[i]) {
                            x = 0;
                            if (++y >= heights[i]) {
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
                tcf = factories[index = (index + 1) % factories.length];
                viewport = viewports[index];
                display = displays[index];
                stage.clear();
                stage.setViewport(viewport);
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
        config.title = "SquidLib Demo: Fonts, preview 1/6 (press any key)";
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