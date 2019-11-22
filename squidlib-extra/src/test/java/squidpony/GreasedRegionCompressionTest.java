package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.GreasedRegion;

/**
 * Expected output:
 * <br>
 * <pre>
 * Earth Map
 * Base size   : 18839
 * LZS size    : 3794
 * Custom size : 2376
 * Both size   : 1142
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * Australia Map
 * Base size   : 8517
 * LZS size    : 1622
 * Custom size : 806
 * Both size   : 434
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * </pre>
 * <br>
 * I'm quite surprised that re-compressing the GreasedRegions (shown on the "Both" rows) works so well.
 * <br>
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class GreasedRegionCompressionTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        GreasedRegion earth = new WorldMapGenerator.MimicMap().earthOriginal.copy();
        String baseString = earth.serializeToString();
        String lz = LZSEncoding.compressToUTF16(baseString);
        String me = earth.toCompressedString();
        String both = LZSEncoding.compressToUTF16(me);
        System.out.println("Earth Map");
        System.out.println("Base size   : " + baseString.length());
        System.out.println("LZS size    : " + lz.length());
        System.out.println("Custom size : " + me.length());
        System.out.println("Both size   : " + both.length());
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(lz)).equals(earth)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(me).equals(earth)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(both)).equals(earth)));
        Gdx.files.local("Earth.txt").writeString(baseString, false, "UTF-16");
        Gdx.files.local("Earth_Comp.txt").writeString(me, false, "UTF-16");
        Gdx.files.local("Earth_LZS.txt").writeString(lz, false, "UTF-16");
        Gdx.files.local("Earth_Both.txt").writeString(both, false, "UTF-16");
        
        GreasedRegion australia = new WorldMapGenerator.LocalMimicMap().earthOriginal.copy();
        baseString = australia.serializeToString();
        lz = LZSEncoding.compressToUTF16(baseString);
        me = australia.toCompressedString();
        both = LZSEncoding.compressToUTF16(me);
        System.out.println("Australia Map");
        System.out.println("Base size   : " + baseString.length());
        System.out.println("LZS size    : " + lz.length());
        System.out.println("Custom size : " + me.length());
        System.out.println("Both size   : " + both.length());
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(lz)).equals(australia)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(me).equals(australia)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(both)).equals(australia)));
        Gdx.files.local("Australia.txt").writeString(baseString, false, "UTF-16");
        Gdx.files.local("Australia_Comp.txt").writeString(me, false, "UTF-16");
        Gdx.files.local("Australia_LZS.txt").writeString(lz, false, "UTF-16");
        Gdx.files.local("Australia_Both.txt").writeString(both, false, "UTF-16");
        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new GreasedRegionCompressionTest(), config);
    }
}
