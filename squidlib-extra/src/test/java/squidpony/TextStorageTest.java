package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.ProbabilityTable;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.ThunderRNG;
import squidpony.store.text.BonusConverters;
import squidpony.store.text.TextStorage;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * Just a test.
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class TextStorageTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        if(true) {
            TextStorage store = new TextStorage("TextStorage", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            store.compress = true;
            System.out.println(store.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!"), r2;

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, FakeLanguageGen.ARABIC_ROMANIZED, 5, FakeLanguageGen.JAPANESE_ROMANIZED, 3), lang2;
            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme"), w2;
            world.generate(15, true);
            GreasedRegion grease = new GreasedRegion(new ThunderRNG(75L), 75, 75), g2;
            store.put("rng", srng, BonusConverters.convertStatefulRNG);
            store.put("language", randomLanguage, Converters.convertFakeLanguageGen);
            store.put("world", world, BonusConverters.convertSpillWorldMap);
            store.put("grease", grease, Converters.convertGreasedRegion);

            store.store("Test");
            System.out.println(store.show());

            System.out.println("Stored preference bytes: " + store.preferencesSize());
            r2 = store.get("Test", "rng", BonusConverters.convertStatefulRNG, StatefulRNG.class);
            lang2 = store.get("Test", "language", Converters.convertFakeLanguageGen, FakeLanguageGen.class);
            w2 = store.get("Test", "world", BonusConverters.convertSpillWorldMap, SpillWorldMap.class);
            g2 = store.get("Test", "grease", Converters.convertGreasedRegion, GreasedRegion.class);
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

            EnumMap<Direction, String> em = new EnumMap<>(Direction.class), empty = new EnumMap<>(Direction.class);

            em.put(Direction.DOWN_LEFT, "California");
            em.put(Direction.DOWN_RIGHT, "Florida");
            em.put(Direction.UP_RIGHT, "Maine");
            em.put(Direction.UP_LEFT, "Washington");
            em.put(Direction.DOWN, "Texas");

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
            noCompression.put("enum_map", em);
            noCompression.put("empty_enum_map", empty);

            yesCompression.put("rng", srng);
            yesCompression.put("language", randomLanguage);
            yesCompression.put("generated", text);
            yesCompression.put("world", world);
            yesCompression.put("grease", grease);
            yesCompression.put("table", table);
            yesCompression.put("drawn", text);
            yesCompression.put("enum_map", em);
            yesCompression.put("empty_enum_map", empty);

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
            System.out.println(em);
            System.out.println(yesCompression.get("Compressed", "enum_map", EnumMap.class));

            //note, these are different because EnumMap needs the enum's Class to be constructed, and an empty EnumMap
            //can't have any keys' Class queried (no keys are present). EnumMap has a field that stores the Class as a
            //final field, but it's private so we can't safely use it.
            System.out.println(empty);
            System.out.println(yesCompression.get("Compressed", "empty_enum_map", EnumMap.class));
            yesCompression.preferences.clear();
            yesCompression.preferences.flush();
            Gdx.app.exit();
        }
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new TextStorageTest(), config);
    }
}
