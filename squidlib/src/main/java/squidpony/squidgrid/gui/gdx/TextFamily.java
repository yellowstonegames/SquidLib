package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import squidpony.squidmath.OrderedMap;

/**
 * A variant of {@link TextCellFactory} that allows switching between regular, bold, italic, and bold italic styles.
 * While almost interchangeable with TextCellFactory, it must be set up differently, using the
 * {@link #fontFamily(String, String...)} or {@link #fontFamilyDistance(String, String...)} methods, and (potentially
 * importantly) it does not correctly render chars above {@code '\\u3fff'} due to using some of the bits that normally
 * represent late-in-Unicode character codes to instead represent bold and italic modes. Two TextFamily values are
 * present in DefaultResources, {@link DefaultResources#getLeanFamily()} and {@link DefaultResources#getSlabFamily()};
 * using them is currently the recommended way because this class is somewhat picky about how it can be built.
 * You may want to use {@link GDXMarkup#colorString(CharSequence)} to produce an {@link squidpony.panel.IColoredString}
 * that contains the specially-altered chars that store bold and italic mode data.
 * Created by Tommy Ettinger on 10/26/2017.
 */
public class TextFamily extends TextCellFactory {
    
    public static final int REGULAR = 0, BOLD = 1, ITALIC = 2, BOLD_ITALIC = 3;
    public int currentStyle = 0;
    public BitmapFont[] family = new BitmapFont[4];
    protected float[] dfx = new float[4], dfy = new float[4];
    /**
     * Creates a default valued factory. One of the initialization methods must
     * be called before this factory can be used!
     */
    public TextFamily() {
        super(null);
    }

    /**
     * A default valued factory that uses the given {@link AssetManager} to load
     * the font file. Use this constructor if you are likely to load the same
     * font over and over (recall that, without an {@link AssetManager}, each
     * instance of {@link TextCellFactory} will load its font from disk). This
     * primarily matters if you are using fonts not bundled with SquidLib, since
     * accessing a BitmapFont with a method (not a String) from DefaultResources
     * caches the BitmapFont already.
     *
     * @param assetManager an ordinary libGDX AssetManager
     */
    public TextFamily(/* Nullable */ AssetManager assetManager) {
        super(assetManager);
    }
    @Override
    public TextFamily copy()
    {
        TextFamily next = new TextFamily(assetManager);
        //next.bmpFont = bmpFont;
        if(bmpFont == null)
            bmpFont = DefaultResources.getIncludedFont();
        if(family[0] != null)
            next.family[0] = DefaultResources.copyFont(family[0]);
        if(family[1] != null)
            next.family[1] = DefaultResources.copyFont(family[1]);
        if(family[2] != null)
            next.family[2] = DefaultResources.copyFont(family[2]);
        if(family[3] != null)
            next.family[3] = DefaultResources.copyFont(family[3]);
        System.arraycopy(dfx, 0, next.dfx, 0, 4);
        System.arraycopy(dfy, 0, next.dfy, 0, 4);
        next.currentStyle = currentStyle;
        next.bmpFont = next.family[next.currentStyle];
        next.block = block;
        next.swap = new OrderedMap<>(swap);
        next.distanceField = distanceField;
        next.msdf = msdf;
        next.distanceFieldScaleX = distanceFieldScaleX;
        next.distanceFieldScaleY = distanceFieldScaleY;
        next.shader = null;
        next.fitting = fitting;
        next.height = height;
        next.width = width;
        next.actualCellWidth = actualCellWidth;
        next.actualCellHeight = actualCellHeight;
        next.descent = descent;
        next.lineHeight = lineHeight;
        //next.modifiedHeight = modifiedHeight;
        next.smoothingMultiplier = smoothingMultiplier;
        next.scc = scc;
        next.directionGlyph = directionGlyph;
        if(initializedBySize)
            next.initBySize();
        else if(initializedByFont)
            next.initByFont();
        return next;
    }

    private BitmapFont loadFont(TextureAtlas atlas, String fontName) {
        String fnt = fontName + ".fnt";
        if (Gdx.files.internal(fnt).exists())
            return new BitmapFont(Gdx.files.internal(fnt), atlas.findRegion(fontName));
        else if (Gdx.files.classpath(fnt).exists())
            return new BitmapFont(Gdx.files.classpath(fnt), atlas.findRegion(fontName));
        else
            return DefaultResources.getIncludedFont();
    }
    private BitmapFont loadFont(BitmapFontLoader.BitmapFontParameter atlasFinder, String fontName) {
        assetManager.load(new AssetDescriptor<>(fontName, BitmapFont.class, atlasFinder));
        assetManager.finishLoading();
        return assetManager.get(fontName, BitmapFont.class);
    }
    public TextFamily defaultFamilyLeanDistance()
    {
        setSmoothingMultiplier(1.75f);
        return fontFamilyDistance("Iosevka_Family.atlas",
                "Iosevka-distance-family", "Iosevka-Bold-distance-family",
                "Iosevka-Italic-distance-family", "Iosevka-Bold-Italic-distance-family");
    }
    public TextFamily defaultFamilySlabDistance()
    {
        setSmoothingMultiplier(1.75f);
        return fontFamilyDistance("Iosevka_Slab_Family.atlas",
                "Iosevka-Slab-distance-family", "Iosevka-Slab-Bold-distance-family",
                "Iosevka-Slab-Italic-distance-family", "Iosevka-Slab-Bold-Italic-distance-family");
    }
    public TextFamily fontFamilyDistance(String atlasName, String... fontNames)
    {
        fontFamily(atlasName, fontNames);
        distanceField = true;
        for (int i = 0; i < 4; i++) {
            dfx[i] = family[i].getSpaceWidth() - 1f;
            dfy[i] = family[i].getLineHeight() - 1f;
        }
        distanceFieldScaleX = dfx[currentStyle];
        distanceFieldScaleY = dfy[currentStyle];
        return this;
    }
    public TextFamily fontFamily(String atlasName, String... fontNames)
    {
        int len;
        if(atlasName == null || fontNames == null || (len = fontNames.length) == 0)
            return this;
        if(assetManager != null) 
        {
            BitmapFontLoader.BitmapFontParameter atlasFinder = new BitmapFontLoader.BitmapFontParameter();
            atlasFinder.atlasName = atlasName;
            switch (len)
            {
                case 1:
                    family[3] = family[2] = family[1] = family[0] = loadFont(atlasFinder, fontNames[0]);
                    break;
                case 2:
                    family[2] = family[0] = loadFont(atlasFinder, fontNames[0]);
                    family[3] = family[1] = loadFont(atlasFinder, fontNames[1]);
                    break;
                case 3:
                    family[0] = loadFont(atlasFinder, fontNames[0]);
                    family[1] = loadFont(atlasFinder, fontNames[1]);
                    family[3] = family[2] = loadFont(atlasFinder, fontNames[2]);
                    break;                 
                default:
                    family[0] = loadFont(atlasFinder, fontNames[0]);
                    family[1] = loadFont(atlasFinder, fontNames[1]);
                    family[2] = loadFont(atlasFinder, fontNames[2]);
                    family[3] = loadFont(atlasFinder, fontNames[3]);
            }
        }
        else
        {
            TextureAtlas atlas;
            if(Gdx.files.internal(atlasName).exists())
                atlas = new TextureAtlas(atlasName);
            else if(Gdx.files.classpath(atlasName).exists())
                atlas = new TextureAtlas(Gdx.files.classpath(atlasName));
            else return this;
            switch (len)
            {
                case 1:
                    family[3] = family[2] = family[1] = family[0] = loadFont(atlas, fontNames[0]);
                    break;
                case 2:
                    family[2] = family[0] = loadFont(atlas, fontNames[0]);
                    family[3] = family[1] = loadFont(atlas, fontNames[1]);
                    break;
                case 3:
                    family[0] = loadFont(atlas, fontNames[0]);
                    family[1] = loadFont(atlas, fontNames[1]);
                    family[3] = family[2] = loadFont(atlas, fontNames[2]);
                    break;
                default:
                    family[0] = loadFont(atlas, fontNames[0]);
                    family[1] = loadFont(atlas, fontNames[1]);
                    family[2] = loadFont(atlas, fontNames[2]);
                    family[3] = loadFont(atlas, fontNames[3]);
            }
        }
        bmpFont = family[currentStyle];
        return this;
    }

    @Override
    public TextCellFactory initBySize() {
        if(bmpFont == null)
            bmpFont = DefaultResources.getIncludedFont();

        //bmpFont.setFixedWidthGlyphs(fitting);
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        if(msdf)
        {
            for (int i = 0; i < 4; i++) {
                family[i].getData().setScale(width / dfx[i], height / dfy[i]);
            }
            shader = new ShaderProgram(DefaultResources.vertexShader, DefaultResources.msdfFragmentShader);
            if (!shader.isCompiled()) {
                Gdx.app.error("shader", "Distance Field font shader compilation failed:\n" + shader.getLog());
            }
            //lineTweak = lineHeight / 20f;
            //distanceFieldScaleX *= (((float)width) / height) / (distanceFieldScaleX / distanceFieldScaleY);
        }
        else if(distanceField)
        {
            for (int i = 0; i < 4; i++) {
                family[i].getData().setScale(width / dfx[i], height / dfy[i]);
            }
            //bmpFont.getData().setScale(width / distanceFieldScaleX, height / distanceFieldScaleY);

            shader = new ShaderProgram(DefaultResources.vertexShader, DefaultResources.fragmentShader);
            if (!shader.isCompiled()) {
                Gdx.app.error("shader", "Distance Field font shader compilation failed:\n" + shader.getLog());
            }
            //lineTweak = lineHeight / 20f;
            //distanceFieldScaleX *= (((float)width) / height) / (distanceFieldScaleX / distanceFieldScaleY);
        }
        else {
            shader = SpriteBatch.createDefaultShader();
            //lineTweak = lineHeight * 0.0625f;
        }
        lineHeight = bmpFont.getLineHeight();
        descent = bmpFont.getDescent();
        //(msdf ? -8 * bmpFont.getDescent(): bmpFont.getDescent());
        style = new Label.LabelStyle(bmpFont, null);
        BitmapFont.Glyph g = bmpFont.getData().getGlyph(directionGlyph);
        dirMarker = new TextureRegion(bmpFont.getRegion(g.page), g.srcX, g.srcY, g.width, g.height);
        initialized = true;
        initializedBySize = true;
        return this;
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param c the char to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param encodedColor the LibGDX Color to use, converted to float as by {@link Color#toFloatBits()}
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, char c, float encodedColor, float x, float y) {
        setStyle(c >>> 14);
        c &= '\u3fff';
        super.draw(batch, c, encodedColor, x, y);
    }

    /**
     * Changes the currently-used font style, such as from regular to italic or bold; values for style can range from 0
     * to 3 (inclusive), and normally you would use one of the constants {@link #REGULAR}, {@link #BOLD},
     * {@link #ITALIC}, or {@link #BOLD_ITALIC} as that parameter.
     * @param style an int (can be 0, 1, 2, or 3) that determines which style to use; typically a constant in this class
     */
    @Override
    public void setStyle(int style)
    {
        currentStyle = style & 3;
        bmpFont = family[currentStyle];
        if(distanceField)
        {
            distanceFieldScaleX = dfx[currentStyle];
            distanceFieldScaleY = dfy[currentStyle];
            lineHeight = bmpFont.getLineHeight();
            descent = bmpFont.getDescent();

        }
    }

}
