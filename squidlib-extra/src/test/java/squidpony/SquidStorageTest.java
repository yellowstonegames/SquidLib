package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.ProbabilityTable;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThunderRNG;

import java.util.Arrays;

/**
 * Just a test.
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class SquidStorageTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        if(false) {
            SquidStorage store = new SquidStorage("StorageTest");
            store.compress = true;
            System.out.println(store.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!"), r2;

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, FakeLanguageGen.ARABIC_ROMANIZED, 5, FakeLanguageGen.JAPANESE_ROMANIZED, 3), lang2;
            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme"), w2;
            world.generate(15, true);
            GreasedRegion grease = new GreasedRegion(new ThunderRNG(75L), 75, 75), g2;
            store.put("rng", srng);
            store.put("language", randomLanguage);
            store.put("world", world);
            store.put("grease", grease);

            System.out.println(store.show());
            store.store("Test");

            System.out.println("Stored preference bytes: " + store.preferencesSize());
            r2 = store.get("Test", "rng", StatefulRNG.class);
            lang2 = store.get("Test", "language", FakeLanguageGen.class);
            w2 = store.get("Test", "world", SpillWorldMap.class);
            g2 = store.get("Test", "grease", GreasedRegion.class);
            long seed1 = srng.getState(), seed2 = r2.getState();
            System.out.println("StatefulRNG states equal: " + (seed1 == seed2));
            System.out.println("FakeLanguageGen values equal: " + randomLanguage.equals(lang2));
            System.out.println("FakeLanguageGen outputs equal: " + randomLanguage.sentence(srng, 5, 10).equals(lang2.sentence(r2, 5, 10)));
            System.out.println("SpillWorldMap.politicalMap values equal: " + Arrays.deepEquals(world.politicalMap, w2.politicalMap));
            System.out.println("SpillWorldMap.atlas values equal: " + world.atlas.equals(w2.atlas));
            System.out.println("GreasedRegion values equal: " + grease.equals(g2));

            store.preferences.clear();
            store.preferences.flush();
            Gdx.app.exit();
        }
        else {

            SquidStorage noCompression = new SquidStorage("StorageTest"), yesCompression = new SquidStorage("StorageCompressed");
            noCompression.compress = false;
            yesCompression.compress = true;
            System.out.println(noCompression.preferences.get().values());
            System.out.println(yesCompression.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!");

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, FakeLanguageGen.ARABIC_ROMANIZED, 5, FakeLanguageGen.JAPANESE_ROMANIZED, 3);

            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme");
            world.generate(15, true);
            GreasedRegion grease = new GreasedRegion(new ThunderRNG(75L), 75, 75);
            String text = randomLanguage.sentence(srng.copy(), 5, 8);
            ProbabilityTable<String> table = new ProbabilityTable<>("I heard you like JSON...");
            table.add("well", 1).add("this", 2).add("ain't", 3).add("real", 4).add("JSON!", 5);
            //String text = table.random();

            noCompression.put("rng", srng);
            noCompression.put("language", randomLanguage);
            noCompression.put("generated", text);
            noCompression.put("world", world);
            noCompression.put("grease", grease);
            noCompression.put("table", table);
            noCompression.put("drawn", text);

            yesCompression.put("rng", srng);
            yesCompression.put("language", randomLanguage);
            yesCompression.put("generated", text);
            yesCompression.put("world", world);
            yesCompression.put("grease", grease);
            yesCompression.put("table", table);
            yesCompression.put("drawn", text);

            System.out.println(text);

            String shown = noCompression.show();
            System.out.println(shown);
            System.out.println("Uncompressed preference bytes: " + shown.length() * 2);
            shown = yesCompression.show();
            System.out.println();
            System.out.println(shown);
            System.out.println("Compressed preference bytes: " + shown.length() * 2);
            noCompression.preferences.clear();
            noCompression.preferences.flush();
            yesCompression.store("Compressed");

            System.out.println(yesCompression.get("Compressed", "language", FakeLanguageGen.class).sentence(srng.copy(), 5, 8));
            System.out.println(yesCompression.get("Compressed", "drawn", String.class));
            yesCompression.preferences.clear();
            yesCompression.preferences.flush();

            Gdx.app.exit();
        }
    }

    public static void main(String[] args)
    {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: SquidStorage";
        config.width = 512;
        config.height = 128;
        new LwjglApplication(new SquidStorageTest(), config);
    }
}
