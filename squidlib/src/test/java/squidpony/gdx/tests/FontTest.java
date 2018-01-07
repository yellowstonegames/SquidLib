package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.gui.gdx.TextPanel;
import squidpony.squidgrid.mapping.LineKit;
import squidpony.squidmath.ThrustAltRNG;

import java.util.ArrayList;

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
    private static float cellWidth = 13;
    /**
     * The pixel height of a cell
     */
    private static float cellHeight = 30;

    private static int totalWidth = 1346, totalHeight = 700;

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private Viewport[] viewports;
    private TextCellFactory tcf;
    private TextCellFactory[] factories;
    private SquidPanel display;
    private SquidPanel[] displays;
    private TextPanel<Color> text;
    private ArrayList<TextPanel<Color>> texts;
    private int index = 5;
    private static final int ZOOM = 1;
    @Override
    public void create() {
        batch = new SpriteBatch();
        //widths = new int[]{100, 95, 90, 110, 95, 50, 125, 170, 200, 90};
        //heights = new int[]{20, 21, 20, 28, 18, 20, 22, 25, 25, 25};
        widths =  new int[]{90, 90, 90, 90, 90, 70, 100, 95,  90,  110, 120, 120, 120, 120, 105, 105, 105, 105, 130, 170, 200, 220};
        heights = new int[]{17, 17, 17, 17, 17, 29, 20,  21,  20,  28,  22,  22,  22,  22,  27,  27,  27,  27,  45, 25,  25,  25};
        factories = new TextCellFactory[]{
                DefaultResources.getCrispSlabFont().width(ZOOM * 14).height(28).initBySize(),
                DefaultResources.getCrispSlabItalicFont().width(ZOOM * 14).height(28).initBySize(),
                DefaultResources.getCrispLeanFont().width(ZOOM * 14).height(28).initBySize(),
                DefaultResources.getCrispLeanItalicFont().width(ZOOM * 14).height(28).initBySize(),
                DefaultResources.getCrispDejaVuFont().width(ZOOM * 14).height(28).initBySize(),
                DefaultResources.getStretchableHeavySquareFont().width(ZOOM * 24).height(24).initBySize(),

                DefaultResources.getStretchableFont().width(ZOOM * 13).height(30).initBySize(),
                DefaultResources.getStretchableTypewriterFont().width(ZOOM * 14).height(28).initBySize(),
                DefaultResources.getStretchableCodeFont().width(ZOOM * 15).height(27).initBySize(),
                DefaultResources.getStretchableDejaVuFont().width(ZOOM * 14).height(25).initBySize(),
                DefaultResources.getStretchableSlabFont().width(ZOOM * 13).height(21).initBySize(),
                DefaultResources.getStretchableSlabLightFont().width(ZOOM * 13).height(21).initBySize(),
                //DefaultResources.getStretchableSquareFont().width(ZOOM * 20).height(20).initBySize(),
                DefaultResources.getStretchableLeanFont().width(ZOOM * 13).height(21).initBySize(),
                DefaultResources.getStretchableLeanLightFont().width(ZOOM * 13).height(21).initBySize(),

                DefaultResources.getStretchableWideSlabFont().width(ZOOM * 15).height(21).initBySize(),
                DefaultResources.getStretchableWideSlabLightFont().width(ZOOM * 15).height(21).initBySize(),
                DefaultResources.getStretchableWideFont().width(ZOOM * 15).height(21).initBySize(),
                DefaultResources.getStretchableWideLightFont().width(ZOOM * 15).height(21).initBySize(),
                DefaultResources.getStretchableCodeJPFont().width(ZOOM * 21).height(ZOOM * 22).initBySize(),
                DefaultResources.getStretchableOrbitFont().initBySize(),
                DefaultResources.getStretchablePrintFont().initBySize(),
                DefaultResources.getStretchableCleanFont().initBySize(),
        };
        viewports = new Viewport[]{
                new StretchViewport(factories[0].width() * widths[0], factories[0].height() * heights[0]),
                new StretchViewport(factories[1].width() * widths[2], factories[1].height() * heights[1]),
                new StretchViewport(factories[2].width() * widths[2], factories[2].height() * heights[2]),
                new StretchViewport(factories[3].width() * widths[3], factories[3].height() * heights[3]),
                new StretchViewport(factories[4].width() * widths[4], factories[4].height() * heights[4]),
                new StretchViewport(factories[5].width() * widths[5], factories[5].height() * heights[5]),
                new StretchViewport(factories[6].width() * widths[6], factories[6].height() * heights[6]),
                new StretchViewport(factories[7].width() * widths[7], factories[7].height() * heights[7]),
                new StretchViewport(factories[8].width() * widths[8], factories[8].height() * heights[8]),
                new StretchViewport(factories[9].width() * widths[9], factories[9].height() * heights[9]),
                new StretchViewport(factories[10].width() * widths[10], factories[10].height() * heights[10]),
                new StretchViewport(factories[11].width() * widths[11], factories[11].height() * heights[11]),
                new StretchViewport(factories[12].width() * widths[12], factories[12].height() * heights[12]),
                new StretchViewport(factories[13].width() * widths[13], factories[13].height() * heights[13]),
                new StretchViewport(factories[14].width() * widths[14], factories[14].height() * heights[14]),
                new StretchViewport(factories[15].width() * widths[15], factories[15].height() * heights[15]),
                new StretchViewport(factories[16].width() * widths[16], factories[16].height() * heights[16]),
                new StretchViewport(factories[17].width() * widths[17], factories[17].height() * heights[17]),
                new StretchViewport(factories[18].width() * widths[18], factories[18].height() * heights[18]),
                new StretchViewport(factories[19].width() * widths[19], factories[19].height() * heights[19]),
                new StretchViewport(factories[20].width() * widths[20], factories[20].height() * heights[20]),
                new StretchViewport(factories[21].width() * widths[21], factories[21].height() * heights[21]),
        };
        displays = new SquidPanel[]{
                new SquidPanel(widths[0], heights[0], factories[0]).setTextSize(factories[0].width() + 0.5f * ZOOM, factories[0].height() + 1f * ZOOM),
                new SquidPanel(widths[2], heights[1], factories[1]).setTextSize(factories[1].width() + 1f * ZOOM, factories[1].height() + 2f * ZOOM),
                new SquidPanel(widths[2], heights[2], factories[2]).setTextSize(factories[2].width() + 2f * ZOOM, factories[2].height() + 4f * ZOOM),
                new SquidPanel(widths[3], heights[3], factories[3]).setTextSize(factories[3].width() + 1f * ZOOM, factories[3].height() + 3f * ZOOM),
                new SquidPanel(widths[4], heights[4], factories[4]).setTextSize(factories[4].width() + 0.5f * ZOOM, factories[4].height() + 1f * ZOOM),
                new SquidPanel(widths[5], heights[5], factories[5]).setTextSize(factories[5].width() + 0.5f * ZOOM, factories[5].height() + 0.5f * ZOOM),
                new SquidPanel(widths[6], heights[6], factories[6]).setTextSize(factories[6].width() + 0.75f * ZOOM, factories[6].height() + 5.25f * ZOOM),
                new SquidPanel(widths[7], heights[7], factories[7]).setTextSize(factories[7].width() + 1f * ZOOM, factories[7].height() + 0.5f * ZOOM),
                new SquidPanel(widths[8], heights[8], factories[8]).setTextSize(factories[8].width() + 2.5f * ZOOM, factories[8].height() + 4f * ZOOM),
                new SquidPanel(widths[9], heights[9], factories[9]).setTextSize(factories[9].width() + 1f * ZOOM, factories[9].height() + 2.5f * ZOOM),
                new SquidPanel(widths[10], heights[10], factories[10]).setTextSize(factories[10].width() + 2f * ZOOM, factories[10].height() + 2.25f * ZOOM),
                new SquidPanel(widths[11], heights[11], factories[11]).setTextSize(factories[11].width() + 2f * ZOOM, factories[11].height() + 2.25f * ZOOM),
                new SquidPanel(widths[12], heights[12], factories[12]).setTextSize(factories[12].width() + 2f * ZOOM, factories[12].height() + 2.75f * ZOOM),
                new SquidPanel(widths[13], heights[13], factories[13]).setTextSize(factories[13].width() + 2f * ZOOM, factories[13].height() + 2.75f * ZOOM),
                new SquidPanel(widths[14], heights[14], factories[14]).setTextSize(factories[14].width() + 2f * ZOOM, factories[12].height() + 2.75f * ZOOM),
                new SquidPanel(widths[15], heights[15], factories[15]).setTextSize(factories[15].width() + 2f * ZOOM, factories[15].height() + 2.75f * ZOOM),
                new SquidPanel(widths[16], heights[16], factories[16]).setTextSize(factories[16].width() + 2f * ZOOM, factories[16].height() + 2.75f * ZOOM),
                new SquidPanel(widths[17], heights[17], factories[17]).setTextSize(factories[17].width() + 2f * ZOOM, factories[17].height() + 2.75f * ZOOM),
                new SquidPanel(widths[18], heights[18], factories[18]).setTextSize(factories[18].width() + 0f * ZOOM, factories[18].height() + 0f * ZOOM),
                new SquidPanel(widths[19], heights[19], factories[19]).setTextSize(factories[19].width() + 1f * ZOOM, factories[19].height() + 2f * ZOOM),
                new SquidPanel(widths[20], heights[20], factories[20]).setTextSize(factories[20].width() + 1f * ZOOM, factories[20].height() + 2f * ZOOM),
                new SquidPanel(widths[21], heights[21], factories[21]).setTextSize(factories[21].width() + 1f * ZOOM, factories[21].height() + 2f * ZOOM),
        };
        final String[] samples = {"The quick brown fox jumps over the lazy dog.",
                "HAMBURGEVONS",
                "Black Sphinx Of Quartz: Judge Ye My Vow!"};
        texts = new ArrayList<>(4);
        text = new TextPanel<Color>(null, factories[factories.length - 3]);
        text.init(totalWidth, totalHeight, Color.WHITE, samples);
        texts.add(text);
        text = new TextPanel<Color>(null, factories[factories.length - 2]);
        text.init(totalWidth, totalHeight, Color.WHITE, samples);
        texts.add(text);
        text = new TextPanel<Color>(null, factories[factories.length - 1]);
        text.init(totalWidth, totalHeight, Color.WHITE, samples);
        texts.add(text);
        for (int i = 0; i < factories.length; i++) {
            tcf = factories[i];
            display = displays[i];
            BitmapFont.BitmapFontData data = tcf.font().getData();
            int dgl = data.glyphs.length, p = 0, x = 0, y = 0;
            BitmapFont.Glyph[] glyphs;
            BitmapFont.Glyph g;
            ALL_PAGES:
            while (p < dgl) {
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
            /*
            REST:
            while (y < heights[i])
            {
                display.put(x++, y, TextCellFactory.LINE_FITTING.charAt(DefaultResources.getGuiRandom().nextIntHasty(11)));
                if (x >= widths[i]) {
                    x = 0;
                    if (++y >= heights[i]) {
                        break REST;
                    }
                }
            }
            */
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
                if(keycode == Input.Keys.B)
                {
                    display.erase();
                    long r = System.nanoTime(), h;
                    r = ThrustAltRNG.determine(r);
                    h = LineKit.flipHorizontal4x4(r);
                    display.put(4, 2, LineKit.decode4x4(r));
                    display.put(8, 2, LineKit.decode4x4(h));
                    display.put(4, 6, LineKit.decode4x4(LineKit.flipVertical4x4(r)));
                    display.put(8, 6, LineKit.decode4x4(LineKit.flipVertical4x4(h)));

                    r = ThrustAltRNG.determine(h) & (ThrustAltRNG.determine(h + 1) | ThrustAltRNG.determine(h + 2));
                    h = ThrustAltRNG.determine(r + 1) & (ThrustAltRNG.determine(r + 2) | ThrustAltRNG.determine(r + 3));
                    display.put(14,2, LineKit.decode4x4(r));
                    display.put(18, 2, LineKit.decode4x4(LineKit.flipHorizontal4x4(r)));
                    display.put(14, 6, LineKit.decode4x4(h));
                    display.put(18, 6, LineKit.decode4x4(LineKit.flipHorizontal4x4(h)));

                    r = (ThrustAltRNG.determine(r + 1) & ThrustAltRNG.determine(r + 2)) & LineKit.interiorCircleLarge;
                    r ^= LineKit.transpose4x4(r);
                    r |= LineKit.exteriorCircleLarge;
                    h = LineKit.flipHorizontal4x4(r);
                    display.put(24, 2, LineKit.decode4x4(r));
                    display.put(28, 2, LineKit.decode4x4(h));
                    display.put(24, 6, LineKit.decode4x4(LineKit.flipVertical4x4(r)));
                    display.put(28, 6, LineKit.decode4x4(LineKit.flipVertical4x4(h)));

                    r = ThrustAltRNG.determine(r+1) & ThrustAltRNG.determine(r + 2);
                    r &= LineKit.flipHorizontal4x4(r);
                    h = ThrustAltRNG.determine(h+1) & ThrustAltRNG.determine(h + 2);
                    h &= LineKit.flipHorizontal4x4(h);
                    display.put(4,  12, LineKit.decode4x4(r));
                    display.put(4,  16, LineKit.decode4x4(h));

                    r = ThrustAltRNG.determine(r+1) & ThrustAltRNG.determine(r + 2);
                    r ^= LineKit.flipHorizontal4x4(r);
                    h = ThrustAltRNG.determine(h+1) & ThrustAltRNG.determine(h + 2);
                    h ^= LineKit.flipHorizontal4x4(h);
                    display.put(11,  12, LineKit.decode4x4(r));
                    display.put(11,  16, LineKit.decode4x4(h));

                    r = ThrustAltRNG.determine(r+1) & ThrustAltRNG.determine(r + 2);
                    r |= LineKit.flipHorizontal4x4(r);
                    h = ThrustAltRNG.determine(h+1) & ThrustAltRNG.determine(h + 2);
                    h |= LineKit.flipHorizontal4x4(h);
                    display.put(18,  12, LineKit.decode4x4(r));
                    display.put(18,  16, LineKit.decode4x4(h));
                }
                else {
                    index = ((index + 1) % factories.length);
                    viewport = viewports[index];
                    if (index < factories.length - 3) {
                        tcf = factories[index];
                        display = displays[index];
                        stage.clear();
                        stage.setViewport(viewport);
                        stage.addActor(display);
                    } else {
                        text = texts.get(index - factories.length + 3);
                        stage.clear();
                        stage.setViewport(viewport);
                        stage.addActor(text.getScrollPane());
                    }
                }
                Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index+1) + "/" + viewports.length + " (press any key)");
                return true;
            }
        });
        Gdx.graphics.setTitle("SquidLib Demo: Fonts, preview " + (index+1) + "/" + viewports.length + " (press any key)");

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
        config.title = "SquidLib Demo: Fonts, preview 1/22 (press any key)";
//        config.width = totalWidth = LwjglApplicationConfiguration.getDesktopDisplayMode().width - 10;
//        config.height = totalHeight = LwjglApplicationConfiguration.getDesktopDisplayMode().height - 128;
        config.width = totalWidth = 1000;
        config.height = totalHeight = 500;
        config.x = 0;
        config.y = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new FontTest(), config);
    }

}