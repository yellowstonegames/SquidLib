package squidpony.gdx.tests;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.Maker;
import squidpony.panel.IColoredString;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.LineKit;
import squidpony.squidmath.DiverRNG;
import squidpony.squidmath.GreasedRegion;

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

    private static int totalWidth = 1346, totalHeight = 700;

    private Stage stage;
    private FilterBatch batch;
    private Viewport viewport;
    private Viewport[] viewports;
    private TextCellFactory tcf;
    private TextCellFactory[] factories;
    private SquidPanel display;
    private SquidPanel[] displays;
    private TextPanel text;
    private ArrayList<TextPanel> texts;
    private int index = 0;
    private static final int ZOOM = 1;
    @Override
    public void create() {

//        // hacky code used to get the contents of a BitmapFont
//        BitmapFont.BitmapFontData sevenTwelve = DefaultResources.getSevenTwelveFont().getData();
//        System.out.println(sevenTwelve.missingGlyph);
//        sevenTwelve.missingGlyph = null;
//        StringBuilder sb = new StringBuilder(10000);
//        int count = 0;
//        for (int i = 0; i <= 0xFFFF; i++) {
//            if(sevenTwelve.hasGlyph((char)i))
//            {
//                sb.append((char)i);
//                if((++count) % 80 == 0)
//                    sb.append('\n');
//            }
//        }
//        Gdx.files.local("SevenTwelveContents.txt").writeString(sb.toString(), false, "UTF8");


        batch = new FilterBatch();
        //widths = new int[]{100, 95, 90, 110, 95, 50, 125, 170, 200, 90};
        //heights = new int[]{20, 21, 20, 28, 18, 20, 22, 25, 25, 25};
        widths =  new int[]{120, 160, 120, 160, 160,  50,  50,  100, 95,  90,  110, 120, 120, 120, 120, 130, 70,  90,  120, 105, 100, 100, 100, 250, 250};
        heights = new int[]{22,  29,  22,  29,  24,   25,  25,  20,  21,  20,  28,  22,  22,  22,  22,  45,  25,  25,  22,  27,  27,  25,  25,  20,  20};
        index = widths.length - 1;
        factories = new TextCellFactory[]{
                DefaultResources.getCrispSlabFont().width(11).height(21).initBySize(),
                DefaultResources.getCrispSlabFamily().width(11).height(21).initBySize(),
                DefaultResources.getCrispLeanFont().width(11).height(21).initBySize(),
                DefaultResources.getCrispLeanFamily().width(11).height(21).initBySize(),
                //DefaultResources.getCrispLeanItalicFont().width(14).height(28).initBySize(),
                DefaultResources.getCrispDejaVuFont().width(10).height(18).initBySize(),
                DefaultResources.getCrispIconFont().width(32).height(32).initBySize(),
                DefaultResources.getStretchableHeavySquareFont().width(32).height(32).initBySize(),

                DefaultResources.getStretchableFont().width(11).height(21).initBySize(),
                DefaultResources.getStretchableTypewriterFont().width(12).height(24).initBySize(),
                DefaultResources.getStretchableCodeFont().width(12).height(21).initBySize(),
                DefaultResources.getStretchableDejaVuFont().width(11).height(23).initBySize(),
                DefaultResources.getStretchableSlabFont().width(11).height(21).initBySize(),
                DefaultResources.getStretchableSlabLightFont().width(11).height(21).initBySize(),
                //DefaultResources.getStretchableSquareFont().width(20).height(20).initBySize(),
                DefaultResources.getStretchableLeanFont().width(11).height(21).initBySize(),
                DefaultResources.getStretchableLeanLightFont().width(11).height(21).initBySize(),

                DefaultResources.getStretchableCodeJPFont().width(17).height(18).initBySize(),
                DefaultResources.getCrispCurvySquareFont().width(16).height(16).initBySize(),
                DefaultResources.getCrispCascadiaFont().width(14).height(21).initBySize(),
                DefaultResources.getCrispSmoothFont().width(11).height(22).initBySize(),
                DefaultResources.getGoFamily().width(7).height(18).initBySize(),
                DefaultResources.getCrispPrintFamily().initBySize(),
                DefaultResources.getStretchablePrintFont().initBySize(),
                DefaultResources.getCrispCarvedFont().initBySize(),
                DefaultResources.getCrispRobotoSansFont().initBySize(),
                new TextCellFactory().fontMultiDistanceField("Roboto-Bold-msdf.fnt", "Roboto-Bold-msdf.png").width(3f).height(14).initBySize().setSmoothingMultiplier(2f)
//                new TextCellFactory().fontMultiDistanceField("VeryWow-msdf.fnt", "VeryWow-msdf.png").width(6f).height(24f).initBySize().setSmoothingMultiplier(1.5f)
                //DefaultResources.getStretchableCleanFont().initBySize(),
        };
        for(TextCellFactory tc : factories)
            tc.bmpFont.setUseIntegerPositions(false);
        int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        viewport = new StretchViewport(totalWidth, totalHeight);
        viewports = new Viewport[]{
                new StretchViewport(factories[0].width() * widths[0], factories[0].height() * heights[0]),
                new StretchViewport(factories[1].width() * widths[1], factories[1].height() * heights[1]),
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

                new StretchViewport(sw, sh),
                new StretchViewport(sw, sh),
                new StretchViewport(sw, sh),
                new StretchViewport(sw, sh),
                new StretchViewport(sw, sh),
//                new ScreenViewport(),
//                new ScreenViewport(),
//                new ScreenViewport(),
//                new ScreenViewport(),
//                new ScreenViewport(),

//                new StretchViewport(factories[20].width() * widths[20], factories[20].height() * heights[20]),
//                new StretchViewport(factories[21].width() * widths[21], factories[21].height() * heights[21]),
//                new StretchViewport(factories[22].width() * widths[22], factories[22].height() * heights[22]),
        };
        displays = new SquidPanel[]{
                new SquidPanel(widths[0 ], heights[0 ], factories[0 ]).setTextSize(factories[0 ].width() + 0f * ZOOM, factories[0].height() + 0f * ZOOM),
                new SquidPanel(widths[1 ], heights[1 ], factories[1 ]).setTextSize(factories[1 ].width() + 0f * ZOOM, factories[1].height() + 0f * ZOOM),
                new SquidPanel(widths[2 ], heights[2 ], factories[2 ]).setTextSize(factories[2 ].width() + 0f * ZOOM, factories[2].height() + 0f * ZOOM),
                new SquidPanel(widths[3 ], heights[3 ], factories[3 ]).setTextSize(factories[3 ].width() + 0f * ZOOM, factories[3].height() + 0f * ZOOM),
                new SquidPanel(widths[4 ], heights[4 ], factories[4 ]).setTextSize(factories[4 ].width() + 0f * ZOOM, factories[4].height() + 0f * ZOOM),
                new SquidPanel(widths[5 ], heights[5 ], factories[5 ]).setTextSize(factories[5 ].width() + 0f * ZOOM, factories[5].height() + 0f * ZOOM),
                new SquidPanel(widths[6 ], heights[6 ], factories[6 ]).setTextSize(factories[6 ].width() + 0.5f * ZOOM, factories[6].height() + 0.5f * ZOOM),
                new SquidPanel(widths[7 ], heights[7 ], factories[7 ]).setTextSize(factories[7 ].width() + 0.75f * ZOOM, factories[7].height() + 5.25f * ZOOM),
                new SquidPanel(widths[8 ], heights[8 ], factories[8 ]).setTextSize(factories[8 ].width() + 1f * ZOOM, factories[8].height() + 0.5f * ZOOM),
                new SquidPanel(widths[9 ], heights[9 ], factories[9 ]).setTextSize(factories[9 ].width() + 2.5f * ZOOM, factories[9].height() + 4f * ZOOM),
                new SquidPanel(widths[10], heights[10], factories[10]).setTextSize(factories[10].width() + 1f * ZOOM, factories[10].height() + 2.5f * ZOOM),
                new SquidPanel(widths[11], heights[11], factories[11]).setTextSize(factories[11].width() + 2f * ZOOM, factories[11].height() + 2.25f * ZOOM),
                new SquidPanel(widths[12], heights[12], factories[12]).setTextSize(factories[12].width() + 2f * ZOOM, factories[12].height() + 2.25f * ZOOM),
                new SquidPanel(widths[13], heights[13], factories[13]).setTextSize(factories[13].width() + 2f * ZOOM, factories[13].height() + 2.75f * ZOOM),
                new SquidPanel(widths[14], heights[14], factories[14]).setTextSize(factories[14].width() + 2f * ZOOM, factories[14].height() + 2.75f * ZOOM),
                new SquidPanel(widths[15], heights[15], factories[15]).setTextSize(factories[15].width() + 2f * ZOOM, factories[15].height() + 2.75f * ZOOM),
                new SquidPanel(widths[16], heights[16], factories[16]).setTextSize(factories[16].width() + -1f * ZOOM, factories[16].height() + -1f * ZOOM),
                new SquidPanel(widths[17], heights[17], factories[17]).setTextSize(factories[17].width() + 1f * ZOOM, factories[17].height() + 1f * ZOOM),
                new SquidPanel(widths[18], heights[18], factories[18]).setTextSize(factories[18].width() + 0f * ZOOM, factories[18].height() + 0f * ZOOM),
                new SquidPanel(widths[19], heights[19], factories[19]).setTextSize(factories[19].width() + 0f * ZOOM, factories[19].height() + 0f * ZOOM),
                new SquidPanel(widths[20], heights[20], factories[20]).setTextSize(factories[20].width() + 1f * ZOOM, factories[20].height() + 2f * ZOOM),
                new SquidPanel(widths[21], heights[21], factories[21]).setTextSize(factories[21].width() + 1f * ZOOM, factories[21].height() + 2f * ZOOM),
                new SquidPanel(widths[22], heights[22], factories[22]).setTextSize(factories[22].width() + 1f * ZOOM, factories[22].height() + 2f * ZOOM),
                new SquidPanel(widths[23], heights[23], factories[23]).setTextSize(factories[23].width() + 0f * ZOOM, factories[23].height() + 0f * ZOOM),
                new SquidPanel(widths[24], heights[24], factories[24]).setTextSize(factories[24].width() + 0f * ZOOM, factories[24].height() + 0f * ZOOM),
        };
        final GDXMarkup markup = GDXMarkup.instance;
        final ArrayList<IColoredString<Color>> samples = Maker.makeList(
                markup.colorString("The quick brown fox jumps over the lazy dog.")
                , markup.colorString("HAMBURGEVONS")
                , markup.colorString("Black Sphinx Of Quartz: Judge Ye My Vow!")
                , markup.colorString("Non-rainbow; [CW Red]r[CW Apricot]a[CW Bright Yellow]i[CW Green]n[CW Cyan]b[CW Blue]o[CW Purple]w[White]!")
                , markup.colorString("Sun Tzu said: In the practical art of war, the best thing of all is to take the enemy's country whole and intact; to shatter and destroy it is not so good.")
                , markup.colorString("So, too, it is better to recapture an army entire than to destroy it, to capture a regiment, a detachment or a company entire than to destroy them.")
                , markup.colorString("Hence to fight and conquer in all your battles is not supreme excellence; supreme excellence consists in breaking the enemy's resistance without fighting.")
        );
        texts = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            text = new TextPanel(factories[factories.length - 5 + i]);
            text.init(totalWidth, totalHeight, samples);
            texts.add(text);
        }
        tcf = factories[index];
        display = displays[index];
//        viewport = viewports[index];
        stage = new Stage(viewport, batch);

        Gdx.input.setInputProcessor(new InputAdapter() {
            final DiverRNG rng = new DiverRNG(System.nanoTime());
            @Override
            public boolean keyUp(int keycode) {
                if(keycode == Input.Keys.B)
                {
                    display.erase();
                    long r, h;
                    //r = determine(r);
                    r = GreasedRegion.approximateBits(rng, 15);
                    h = LineKit.flipHorizontal4x4(r);
                    display.put(4, 2, LineKit.decode4x4(r));
                    display.put(8, 2, LineKit.decode4x4(h));
                    display.put(4, 6, LineKit.decode4x4(LineKit.flipVertical4x4(r)));
                    display.put(8, 6, LineKit.decode4x4(LineKit.flipVertical4x4(h)));

//                    r = determine(h) & (determine(h + 1) | determine(h + 2));
//                    h = determine(r + 1) & (determine(r + 2) | determine(r + 3));
                    r = GreasedRegion.approximateBits(rng, 13);
                    h = GreasedRegion.approximateBits(rng, 13);
                    display.put(14, 2, LineKit.decode4x4(r));
                    display.put(18, 2, LineKit.decode4x4(LineKit.flipHorizontal4x4(r)));
                    display.put(14, 6, LineKit.decode4x4(h));
                    display.put(18, 6, LineKit.decode4x4(LineKit.flipHorizontal4x4(h)));

//                    r = (determine(r + 1) & determine(r + 2)) & LineKit.interiorCircleLarge;
//                    r ^= LineKit.transpose4x4(r);
//                    r |= LineKit.exteriorCircleLarge;
                    r = GreasedRegion.approximateBits(rng, 10) & LineKit.interiorCircleLarge;
                    r ^= LineKit.transpose4x4(r);
                    r |= LineKit.exteriorCircleLarge;
                    h = LineKit.flipHorizontal4x4(r);
                    display.put(24, 2, LineKit.decode4x4(r));
                    display.put(28, 2, LineKit.decode4x4(h));
                    display.put(24, 6, LineKit.decode4x4(LineKit.flipVertical4x4(r)));
                    display.put(28, 6, LineKit.decode4x4(LineKit.flipVertical4x4(h)));


//                    r = determine(r + 1);
                    r = GreasedRegion.approximateBits(rng, 15);
                    h = (r & LineKit.shallowInteriorSquareLarge) | LineKit.exteriorSquareLarge;
                    display.put(34, 2, LineKit.decode4x4(h));
                    h = LineKit.flipHorizontal4x4(h);
                    display.put(38, 2, LineKit.decode4x4(h));
//                    r = determine(r + 1);
                    r = GreasedRegion.approximateBits(rng, 14);
                    h = LineKit.flipVertical4x4(
                            (r & LineKit.shallowerInteriorSquareLarge) | LineKit.exteriorDiamondLarge);
                    display.put(34, 6, LineKit.decode4x4(h));
                    h = LineKit.flipHorizontal4x4(h);
                    display.put(38, 6, LineKit.decode4x4(h));

//                    r = determine(r+1) & determine(r + 2);
                    r = GreasedRegion.approximateBits(rng, 36);
                    r &= LineKit.flipHorizontal4x4(r);
//                    h = determine(h+1) & determine(h + 2);
                    h = GreasedRegion.approximateBits(rng, 36);
                    h &= LineKit.flipHorizontal4x4(h);
                    display.put(4,  12, LineKit.decode4x4(r));
                    display.put(4,  16, LineKit.decode4x4(h));

//                    r = determine(r+1) & determine(r + 2);
                    r = GreasedRegion.approximateBits(rng, 12);
                    r ^= LineKit.flipHorizontal4x4(r);
//                    h = determine(h+1) & determine(h + 2);
                    h = GreasedRegion.approximateBits(rng, 12);
                    h ^= LineKit.flipHorizontal4x4(h);
                    display.put(11,  12, LineKit.decode4x4(r));
                    display.put(11,  16, LineKit.decode4x4(h));

//                    r = determine(r+1) & determine(r + 2);
                    r = GreasedRegion.approximateBits(rng, 10);
                    r |= LineKit.flipHorizontal4x4(r);
//                    h = determine(h+1) & determine(h + 2);
                    h = GreasedRegion.approximateBits(rng, 10);
                    h |= LineKit.flipHorizontal4x4(h);
                    display.put(18,  12, LineKit.decode4x4(r));
                    display.put(18,  16, LineKit.decode4x4(h));
                }
                else if(keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                    index = ((index + 1) % factories.length);
//                    viewport = viewports[index];
                    if (index < factories.length - texts.size()) {
                        tcf = factories[index];
                        display = displays[index];
                        stage.clear();
                        stage.setViewport(viewport);
                        stage.addActor(display);
                    } else {
                        text = texts.get(index - factories.length + texts.size());
                        stage.clear();
                        stage.setViewport(viewport);
                        stage.addActor(text.getScrollPane());
                    }
                }
                Gdx.graphics.setTitle("SquidLib Demo: Fonts, previewing font #" + (index) + ": " + factories[index].bmpFont + " (press any key)");
                return true;
            }
        });
        Gdx.graphics.setTitle("SquidLib Demo: Fonts, previewing font #" + (index) + ": " + factories[index].bmpFont + " (press any key)");

        if(index < factories.length - texts.size()) 
            stage.clear();//stage.addActor(display);
        else 
        {
            text = texts.get(index - factories.length + texts.size());
            stage.clear();
//            stage.setViewport(viewport = viewports[index]);
            stage.addActor(text.getScrollPane());
        }
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        stage.getViewport().update(totalWidth, totalHeight, true);
//        stage.getViewport().apply(true);
        if (index < factories.length - texts.size()) {
            batch.begin();
            tcf.configureShader(batch);
            BitmapFont.BitmapFontData data = tcf.font().getData();
            int dgl = data.glyphs.length, p = 0, x = 0, y = totalHeight;
            BitmapFont.Glyph[] glyphs;
            BitmapFont.Glyph g;
            StringBuilder sb = new StringBuilder(1);
            sb.append(' ');
            ALL_PAGES:
            while (p < dgl) {
                glyphs = data.glyphs[p++];
                if (glyphs == null) continue;
                int gl = glyphs.length;
                for (int gi = 0; gi < gl; gi++) {
                    if ((g = glyphs[gi]) != null) {
//                        display.put(x, y, (char) g.id);
                        sb.setCharAt(0, (char) g.id);
                        tcf.font().draw(batch, sb, x, y);
                        x += tcf.actualCellWidth;
                        if (x >= totalWidth - tcf.actualCellWidth) {
                            x = 0;
                            if ((y -= tcf.actualCellHeight) <= 0) {
                                break ALL_PAGES;
                            }
                        }
                    }
                }
            }
            batch.end();
        }
        else
            stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
//        totalWidth = width;
//        totalHeight = height;
        stage.getViewport().update(width, height, true);
    }
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Fonts, preview 1/25 (press any key)");
//        config.width = totalWidth = Lwjgl3ApplicationConfiguration.getDesktopDisplayMode().width - 10;
//        config.height = totalHeight = Lwjgl3ApplicationConfiguration.getDesktopDisplayMode().height - 128;
        config.useVsync(true);
        config.setWindowedMode(totalWidth = 1000, totalHeight = 500);
        config.setWindowPosition(20, 20);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new FontTest(), config);
    }

}
