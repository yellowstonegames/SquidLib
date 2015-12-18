package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import squidpony.squidmath.StatefulRNG;

/**
 * Default BitmapFonts, a sample image, and a central RNG for use with LibGDX.
 * The fonts provided are all monospaced, with most looking rather similar (straight orthogonal lines and elbow curves),
 * but the one that looks... better than the rest (Inconsolata-LGC, accessible by getLargeSmoothFont()) also supports
 * Greek and Cyrillic, and is the only one to do so. The most Latin script support is in the font Mandrill, accessible
 * by getDefaultUnicodeFont() and getLargeUnicodeFont() in two different sizes, and the latter should be suitable for
 * everything from Polish to Vietnamese.
 * <br>
 * The sample image is a tentacle taken from a public domain icon collection graciously released by Henrique Lazarini;
 * it's fitting for SquidLib to have a tentacle as a logo or something, I guess?
 * <br>
 * You can get a default RNG with getGuiRandom(); this should probably not be reused for non-GUI-related randomness,
 * but is meant instead to be used wherever randomized purely-aesthetic effects are needed, such as a jiggling effect.
 * Created by Tommy Ettinger on 7/11/2015.
 */
public class DefaultResources implements LifecycleListener {
    private BitmapFont narrow1 = null, narrow2 = null, narrow3 = null,
            smooth1 = null,
            smooth2 = null,
            square1 = null, square2 = null,
            unicode1 = null, unicode2 = null;
    private BitmapFont[] zooms = null;
    public static final String squareName = "Zodiac-Square-12x12.fnt",
            narrowName = "Rogue-Zodiac-6x12.fnt",
            unicodeName = "Mandrill-6x16.fnt",
            squareNameLarge = "Zodiac-Square-24x24.fnt",
            narrowNameLarge = "Rogue-Zodiac-12x24.fnt",
            unicodeNameLarge = "Mandrill-12x32.fnt",
            narrowNameExtraLarge = "Rogue-Zodiac-18x36.fnt",
            smoothName = "Inconsolata-LGC-8x18.fnt",
            smoothNameLarge = "Inconsolata-LGC-12x24.fnt";
    private SquidColorCenter scc = null;
    private Texture tentacle = null;
    private TextureRegion tentacleRegion = null;
    private StatefulRNG guiRandom;

    private static DefaultResources instance = null;

    private DefaultResources()
    {
        Gdx.app.addLifecycleListener(this);
    }

    private static void initialize()
    {
        if(instance == null)
            instance = new DefaultResources();
    }

    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultFont()
    {
        initialize();
        if(instance.square1 == null)
        {
            try {
                instance.square1 = new BitmapFont(Gdx.files.classpath("Zodiac-Square-12x12.fnt"), Gdx.files.classpath("Zodiac-Square-12x12.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.square1;
    }
    /**
     * Returns a 24x24px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 32 pt.
     */
    public static BitmapFont getLargeFont()
    {
        initialize();
        if(instance.square2 == null)
        {
            try {
                instance.square2 = new BitmapFont(Gdx.files.classpath("Zodiac-Square-24x24.fnt"), Gdx.files.classpath("Zodiac-Square-24x24.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.square2;
    }
    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultNarrowFont()
    {
        initialize();
        if(instance.narrow1 == null)
        {
            try {
                instance.narrow1 = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-6x12.fnt"), Gdx.files.classpath("Rogue-Zodiac-6x12.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.narrow1;
    }

    /**
     * Returns a 12x24px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 32 pt.
     */
    public static BitmapFont getLargeNarrowFont()
    {
        initialize();
        if(instance.narrow2 == null)
        {
            try {
                instance.narrow2 = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-12x24.fnt"), Gdx.files.classpath("Rogue-Zodiac-12x24.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.narrow2;
    }
    /**
     * Returns a 12x24px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 32 pt.
     */
    public static BitmapFont getExtraLargeNarrowFont()
    {
        initialize();
        if(instance.narrow3 == null)
        {
            try {
                instance.narrow3 = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-18x36.fnt"), Gdx.files.classpath("Rogue-Zodiac-18x36.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.narrow3;
    }

    /**
     * Returns a 8x18px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * Caches the font for later calls.
     * @return the BitmapFont object representing Inconsolata-LGC.ttf at size... pretty sure it's 8x18 pixels
     */
    public static BitmapFont getSmoothFont()
    {
        initialize();
        if(instance.smooth1 == null)
        {
            try {
                instance.smooth1 = new BitmapFont(Gdx.files.classpath("Inconsolata-LGC-8x18.fnt"), Gdx.files.classpath("Inconsolata-LGC-8x18.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.smooth1;
    }
    /**
     * Returns a 12x24px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * Caches the font for later calls.
     * @return the BitmapFont object representing Inconsolata-LGC.ttf at size... not actually sure, 12x24 pixels.
     */
    public static BitmapFont getLargeSmoothFont()
    {
        initialize();
        if(instance.smooth2 == null)
        {
            try {
                instance.smooth2 = new BitmapFont(Gdx.files.classpath("Inconsolata-LGC-12x24.fnt"), Gdx.files.classpath("Inconsolata-LGC-12x24.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.smooth2;
    }
    /**
     * Returns a 6x16px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Mandrill.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultUnicodeFont()
    {
        initialize();
        if(instance.unicode1 == null)
        {
            try {
                instance.unicode1 = new BitmapFont(Gdx.files.classpath("Mandrill-6x16.fnt"), Gdx.files.classpath("Mandrill-6x16.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.unicode1;
    }

    /**
     * Returns a 12x32px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Mandrill.ttf at size 32 pt.
     */
    public static BitmapFont getLargeUnicodeFont()
    {
        initialize();
        if(instance.unicode2 == null)
        {
            try {
                instance.unicode2 = new BitmapFont(Gdx.files.classpath("Mandrill-12x32.fnt"), Gdx.files.classpath("Mandrill-12x32.png"), false);
            } catch (Exception e) {
            }
        }
        return instance.unicode2;
    }
    /**
     * Returns a 20x20px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * This variant is (almost) perfectly square, and box drawing characters should line up at size 20x20 px, but other
     * glyphs will have much more horizontal spacing than in other fonts. Caches the font for later calls.
     * @return the BitmapFont object representing Inconsolata-LGC-Square at size 16 (pt?).
     */
    public static BitmapFont getSquareSmoothFont()
    {
        initialize();
        if(instance.zooms == null)
        {
            instance.zooms = new BitmapFont[12];
        }
        if(instance.zooms[1] == null)
        {
            try {
                instance.zooms[1] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-16.fnt"));
            } catch (Exception e) {
            }
        }
        return instance.zooms[1];
    }
    /**
     * Returns a size you specify of a very smooth and generally good-looking font (based on Inconsolata) as an embedded
     * resource. The zoomLevel should be between 0 and 11 inclusive, and will be clamped to this range if outside it.
     * The actual pixel size varies a lot between zoomLevels; zoomLevel 0 should work at 17x17 px, zoomLevel 1 at 20x20
     * px, up to zoomLevel 11 at 51x51 px. This font fully supports Latin, Greek, Cyrillic, and of particular interest
     * to SquidLib, Box Drawing characters. This variant is (almost) perfectly square, and box drawing characters should
     * line up at square sizes, but other glyphs will have much more horizontal spacing than in other fonts. Caches the
     * font for later calls.
     * @param zoomLevel between 0 and 11 inclusive; higher numbers give bigger fonts
     * @return the BitmapFont object representing Inconsolata-LGC-Square at the given zoomLevel.
     */
    public static BitmapFont getZoomedFont(int zoomLevel)
    {
        initialize();
        zoomLevel = Math.max(0, Math.min(zoomLevel, 11));
        if(instance.zooms == null)
        {
            instance.zooms = new BitmapFont[12];
        }
        if(instance.zooms[zoomLevel] == null)
        {
            try {
                switch (zoomLevel)
                {
                    case 0: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-14.fnt"));
                        break;
                    case 1: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-16.fnt"));
                        break;
                    case 2: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-18.fnt"));
                        break;
                    case 3: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-20.fnt"));
                        break;
                    case 4: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-22.fnt"));
                        break;
                    case 5: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-24.fnt"));
                        break;
                    case 6: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-26.fnt"));
                        break;
                    case 7: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-28.fnt"));
                        break;
                    case 8: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-31.fnt"));
                        break;
                    case 9: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-35.fnt"));
                        break;
                    case 10: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-39.fnt"));
                        break;
                    case 11: instance.zooms[zoomLevel] = new BitmapFont(Gdx.files.classpath("zoom/Inconsolata-LGC-Square-42.fnt"));
                        break;

                }
            } catch (Exception e) {
            }
        }
        return instance.zooms[zoomLevel];
    }

    /**
     * Gets an image of a (squid-like, for SquidLib) tentacle, 32x32px.
     * Source is public domain: http://opengameart.org/content/496-pixel-art-icons-for-medievalfantasy-rpg
     * Created by Henrique Lazarini (7Soul1, http://7soul1.deviantart.com/ )
     * @return a TextureRegion containing an image of a tentacle.
     */
    public static TextureRegion getTentacle()
    {
        initialize();
        if(instance.tentacle == null || instance.tentacleRegion == null)
        {
            try {
                instance.tentacle = new Texture(Gdx.files.classpath("Tentacle.png"));
                instance.tentacleRegion = new TextureRegion(instance.tentacle);
            } catch (Exception ignored) {
            }
        }
        return instance.tentacleRegion;
    }

    /**
     * This is a static global LightRNG that's meant for usage in cases where the seed does not matter and any changes
     * to this LightRNG's state will not change behavior elsewhere in the program; this means the GUI mainly.
     */
    public static StatefulRNG getGuiRandom()
    {
        initialize();
        if(instance.guiRandom == null)
        {
            instance.guiRandom =  new StatefulRNG();
        }
        return instance.guiRandom;
    }
    /**
     * This is a static global LightRNG that's meant for usage in cases where the seed does not matter and any changes
     * to this LightRNG's state will not change behavior elsewhere in the program; this means the GUI mainly.
     */
    public static SquidColorCenter getSCC()
    {
        initialize();
        if(instance.scc == null)
        {
            instance.scc =  new SquidColorCenter();
        }
        return instance.scc;
    }

    /**
     * Special symbols that can be used as icons if you use the narrow default font.
     */
    public static final String narrowFontSymbols = "ሀሁሂሃሄህሆሇለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማሜ",
                                  narrowFontAll = " !\"#$%&'()*+,-./0123\n" +
                                          "456789:;<=>?@ABCDEFG\n" +
                                          "HIJKLMNOPQRSTUVWXYZ[\n" +
                                          "\\]^_`abcdefghijklmno\n" +
                                          "pqrstuvwxyz{|}~¡¢£¤¥\n" +
                                          "¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹\n" +
                                          "º»¼½¾¿×ß÷øɍɎሀሁሂሃሄህሆሇ\n" +
                                          "ለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማ\n" +
                                          "ሜẞ‐‒–—―‖‗‘’‚‛“”„‟†‡•\n" +
                                          "…‧‹›€™"+
                                          "←↑→↓↷↺↻"+ // left, up, right, down, "tap", "counterclockwise", "clockwise"
                                          "∀∁∂∃∄∅∆\n" +
                                          "∇∈∉∋∌∎∏∐∑−∓∔∕∖∘∙√∛∜∝\n" +
                                          "∞∟∠∡∢∣∤∥∦∧∨∩∪∫∬∮∯∱∲∳\n" +
                                          "∴∵∶∷≈≋≠≡≢≣≤≥≦≧≨≩≪≫─━\n" +
                                          "│┃┄┅┆┇┈┉┊┋┌┍┎┏┐┑┒┓└┕\n" +
                                          "┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩\n" +
                                          "┪┫┬┭┮┯┰┱┲┳┴┵┶┷┸┹┺┻┼┽\n" +
                                          "┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║\n" +
                                          "╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥\n" +
                                          "╦╧╨╩╪╫╬╭╮╯╰╱╲╳╴╵╶╷╸╹\n" +
                                          "╺╻╼╽╾╿▁▄▅▆▇█▌▐░▒▓▔▖▗\n" +
                                          "▘▙▚▛▜▝▞▟";

    /**
     * Called when the {@link Application} is about to pause
     */
    @Override
    public void pause() {
        if(narrow1 != null) {
            narrow1.dispose();
            narrow1 = null;
        }
        if(narrow2 != null) {
            narrow2.dispose();
            narrow2 = null;
        }
        if(narrow3 != null) {
            narrow3.dispose();
            narrow3 = null;
        }
        if(smooth1 != null) {
            smooth1.dispose();
            smooth1 = null;
        }
        if(smooth2 != null) {
            smooth2.dispose();
            smooth2 = null;
        }
        if(square1 != null) {
            square1.dispose();
            square1 = null;
        }
        if(square2 != null) {
            square2.dispose();
            square1 = null;
        }
        if (unicode1 != null) {
            unicode1.dispose();
            unicode1 = null;
        }
        if (unicode2 != null) {
            unicode2.dispose();
            unicode2 = null;
        }
        if(tentacle != null) {
            tentacle.dispose();
            tentacle = null;
        }


        if(zooms != null) {
            for (int i = 0; i < 12; i++) {
                if(zooms[i] != null)
                {
                    zooms[i].dispose();
                    zooms[i] = null;
                }
            }
            zooms = null;
        }
    }

    /**
     * Called when the Application is about to be resumed
     */
    @Override
    public void resume() {
        initialize();
    }

    /**
     * Called when the {@link Application} is about to be disposed
     */
    @Override
    public void dispose() {
        pause();
        Gdx.app.removeLifecycleListener(this);
        instance = null;
    }
}
