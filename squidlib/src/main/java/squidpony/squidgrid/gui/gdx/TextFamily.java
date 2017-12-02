package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.assets.AssetManager;
import squidpony.squidmath.OrderedMap;

/**
 * A variant of {@link TextCellFactory} that allows switching between regular, bold, italic, and bold italic styles.
 * It adds only one field to TextCellFactory, and is interchangeable except that (potentially importantly) it does not
 * correctly render chars above {@code '\\u3fff'} due to using some of the bits that normally represent late-in-Unicode
 * character codes to instead represent bold and italic modes. Two TextFamily values are present in DefaultResources,
 * {@link DefaultResources#getLeanFamily()} and {@link DefaultResources#getSlabFamily()}; using them is currently the
 * recommended way to use this class.
 * <br>
 * You may want to use {@link GDXMarkup#colorString(CharSequence)} to produce an {@link squidpony.panel.IColoredString}
 * that contains the specially-altered chars that store bold and italic mode data.
 * <br>
 * Created by Tommy Ettinger on 10/26/2017.
 */
public class TextFamily extends TextCellFactory {

    // constants used by some fonts for format information; this stores their order for reference.
    public static final int REGULAR = 0, BOLD = 1, ITALIC = 2, BOLD_ITALIC = 3;
    /**
     * How many styles are supported by this TextCellFactory; always 4 in TextFamily.
     */
    public final int STYLES = 4;
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
        if(bmpFont == null)
            bmpFont = DefaultResources.getIncludedFont();
        next.bmpFont = DefaultResources.copyFont(bmpFont);
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
        next.smoothingMultiplier = smoothingMultiplier;
        next.scc = scc;
        next.directionGlyph = directionGlyph;
        if(initializedBySize)
            next.initBySize();
        else if(initializedByFont)
            next.initByFont();
        return next;
    }
    public TextFamily defaultFamilyLeanDistance()
    {
        fontDistanceField("Iosevka-Family-distance.fnt", "Iosevka-Family-distance.png");
        setSmoothingMultiplier(2.1f);
        return this;
    }
    public TextFamily defaultFamilySlabDistance()
    {
        fontDistanceField("Iosevka-Slab-Family-distance.fnt", "Iosevka-Slab-Family-distance.png");
        setSmoothingMultiplier(2.1f);
        return this;
    }

}
