package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.GreasedRegion;

import java.util.Arrays;

/**
 * Expected output:
 * <br>
 * <pre>
 * Earth Map
 * Base size   : 19379
 * LZS size    : 4064
 * Custom size : 1405
 * Both size   : 2292
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * Reading in written Earth files...
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 *
 * Heat Map
 * Base size   : 515572 / 131080 uncompressed bytes: 3.9332621299969484
 * LZS size    : 57816 / 131080 uncompressed bytes: 0.44107415318889226
 * Custom size : 167815 / 131080 uncompressed bytes: 1.2802487030820873
 * Both size   : 37508 / 131080 uncompressed bytes: 0.2861458651205371
 * BSE size    : 45896 / 131080 uncompressed bytes: 0.3501373207201709
 * BSELZS size : 70418 / 131080 uncompressed bytes: 0.5372139151663107
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * BSE correct? true
 * BSELZS correct? true
 * Reading in written Heat files...
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * BSE correct? true
 * BSELZS correct? true
 *
 * Australia Map
 * Base size   : 8517
 * LZS size    : 1622
 * Custom size : 434
 * Both size   : 683
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * Reading in written Australia files...
 * LZ correct? true
 * Custom correct? true
 * Both correct? true
 * </pre>
 * <br>
 * Custom mode calls {@link GreasedRegion#toCompressedString()}, which uses Hilbert Curve RLE compression followed by
 * LZ-String compression. This does worse when LZ-String is applied again in Both mode, because compressing data twice
 * in the same way generally doesn't work. For the heat map, this uses {@link GridCompression#compress(byte[][])}, which
 * does not apply LZ-String encoding, so using Both mode works well there (it only applies LZ-String once there).
 * <br>
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class GreasedRegionCompressionTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        WorldMapGenerator.MimicMap mimicMap = new WorldMapGenerator.MimicMap();
        GreasedRegion earth = mimicMap.earthOriginal.copy();
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
        System.out.println("Reading in written Earth files...");
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(Gdx.files.local("Earth_LZS.txt").readString("UTF-16"))).equals(earth)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(Gdx.files.local("Earth_Comp.txt").readString("UTF-16")).equals(earth)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Earth_Both.txt").readString("UTF-16"))).equals(earth)));

        System.out.println();

        mimicMap.generate(123L);
        String gcomp = GridCompression.compress(mimicMap.heatData);
        byte[][] heatData = GridCompression.decompress(gcomp);
        byte[] bytes = GridCompression.byteGridToByteArray(heatData);
        me = GridCompression.compress(heatData);
        baseString = Converters.convertArrayByte2D.stringify(heatData);
        lz = LZSEncoding.compressToUTF16(baseString);
        both = LZSEncoding.compressToUTF16(me);
        String bse = ByteStringEncoding.compress(bytes);
        String bselzs = LZSEncoding.compressToUTF16(bse);
        System.out.println("Heat Map");
        System.out.println("Base size   : " + baseString.length() + " / " + bytes.length + " uncompressed bytes: " + ((double)baseString.length()/bytes.length));
        System.out.println("LZS size    : " + lz.length() + " / " + bytes.length + " uncompressed bytes: " + ((double)lz.length()/bytes.length));
        System.out.println("Custom size : " + me.length() + " / " + bytes.length + " uncompressed bytes: " + ((double)me.length()/bytes.length));
        System.out.println("Both size   : " + both.length() + " / " + bytes.length + " uncompressed bytes: " + ((double)both.length()/bytes.length));
        System.out.println("BSE size    : " + bse.length() + " / " + bytes.length + " uncompressed bytes: " + ((double)bse.length()/bytes.length));
        System.out.println("BSELZS size : " + bselzs.length() + " / " + bytes.length + " uncompressed bytes: " + ((double)bselzs.length()/bytes.length));
        System.out.println("LZ correct? " + (Arrays.deepEquals(Converters.convertArrayByte2D.restore(LZSEncoding.decompressFromUTF16(lz)), heatData)));
        System.out.println("Custom correct? " + (Arrays.deepEquals((GridCompression.decompress(me)), heatData)));
        System.out.println("Both correct? " + (Arrays.deepEquals((GridCompression.decompress(LZSEncoding.decompressFromUTF16(both))), heatData)));
        System.out.println("BSE correct? " + (Arrays.deepEquals(GridCompression.byteArrayToByteGrid(ByteStringEncoding.decompress(bse)), heatData)));
        System.out.println("BSELZS correct? " + (Arrays.deepEquals(GridCompression.byteArrayToByteGrid(ByteStringEncoding.decompress(LZSEncoding.decompressFromUTF16(bselzs))), heatData)));
        Gdx.files.local("Heat.txt").writeString(baseString, false, "UTF-16");
        Gdx.files.local("Heat_Comp.txt").writeString(me, false, "UTF-16");
        Gdx.files.local("Heat_LZS.txt").writeString(lz, false, "UTF-16");
        Gdx.files.local("Heat_Both.txt").writeString(both, false, "UTF-16");
        Gdx.files.local("Heat_BSE.txt").writeString(bse, false, "UTF-16");
        Gdx.files.local("Heat_BSELZS.txt").writeString(bselzs, false, "UTF-16");
        System.out.println("Reading in written Heat files...");
        System.out.println("LZ correct? " + (Arrays.deepEquals(Converters.convertArrayByte2D.restore(LZSEncoding.decompressFromUTF16((Gdx.files.local("Heat_LZS.txt").readString("UTF-16")))), heatData)));
        System.out.println("Custom correct? " + (Arrays.deepEquals((GridCompression.decompress((Gdx.files.local("Heat_Comp.txt").readString("UTF-16")))), heatData)));
        System.out.println("Both correct? " + (Arrays.deepEquals((GridCompression.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Heat_Both.txt").readString("UTF-16")))), heatData)));
        System.out.println("BSE correct? " + (Arrays.deepEquals(GridCompression.byteArrayToByteGrid(ByteStringEncoding.decompress((Gdx.files.local("Heat_BSE.txt").readString("UTF-16")))), heatData)));
        System.out.println("BSELZS correct? " + (Arrays.deepEquals(GridCompression.byteArrayToByteGrid(ByteStringEncoding.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Heat_BSELZS.txt").readString("UTF-16")))), heatData)));
        
        System.out.println();
        WorldMapGenerator.LocalMimicMap localMimicMap = new WorldMapGenerator.LocalMimicMap();
        GreasedRegion australia = localMimicMap.earthOriginal.copy();
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
        System.out.println("Reading in written Australia files...");
        System.out.println("LZ correct? " + (GreasedRegion.deserializeFromString(LZSEncoding.decompressFromUTF16(Gdx.files.local("Australia_LZS.txt").readString("UTF-16"))).equals(australia)));
        System.out.println("Custom correct? " + (GreasedRegion.decompress(Gdx.files.local("Australia_Comp.txt").readString("UTF-16")).equals(australia)));
        System.out.println("Both correct? " + (GreasedRegion.decompress(LZSEncoding.decompressFromUTF16(Gdx.files.local("Australia_Both.txt").readString("UTF-16"))).equals(australia)));

        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new GreasedRegionCompressionTest(), config);
    }
}
