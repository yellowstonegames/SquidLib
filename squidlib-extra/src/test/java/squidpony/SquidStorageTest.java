package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.StatefulRNG;

import java.util.Arrays;

/**
 * Just a test. //30934 4630
 * Created by Tommy Ettinger on 9/17/2016.
 */
public class SquidStorageTest extends ApplicationAdapter{
    @Override
    public void create() {
        super.create();
        SquidStorage store = new SquidStorage("StorageTest");
        if(true) {
            store.compress = true;
            System.out.println(store.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!"), r2;

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(srng), lang2;

            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme"), w2;
            world.generate(15, true);
            store.put("rng", srng);
            store.put("language", randomLanguage);
            store.put("world", world);
            store.store("Test");
            System.out.println("Stored preference bytes: " + store.preferencesSize());
            r2 = store.get("Test", "rng", StatefulRNG.class);

            lang2 = store.get("Test", "language", FakeLanguageGen.class);
            w2 = store.get("Test", "world", SpillWorldMap.class);
            long seed1 = srng.getState(), seed2 = r2.getState();
            System.out.println("StatefulRNG states equal: " + (seed1 == seed2));
            System.out.println("FakeLanguageGen values equal: " + randomLanguage.equals(lang2));
            System.out.println("FakeLanguageGen outputs equal: " + randomLanguage.sentence(srng, 5, 10).equals(lang2.sentence(r2, 5, 10)));
            System.out.println("SpillWorldMap.politicalMap values equal: " + Arrays.deepEquals(world.politicalMap, w2.politicalMap));
            System.out.println("SpillWorldMap.atlas values equal: " + world.atlas.equals(w2.atlas));

            store.preferences.clear();
            store.preferences.flush();
            Gdx.app.exit();
        }
        else {
            store.compress = false;
            System.out.println(store.preferences.get().values());
            StatefulRNG srng = new StatefulRNG("Hello, Storage!"), r2;

            FakeLanguageGen randomLanguage = FakeLanguageGen.randomLanguage(srng), lang2;

            SpillWorldMap world = new SpillWorldMap(120, 80, "FutureLandXtreme"), w2;
            world.generate(15, true);
            store.put("rng", srng);
            store.put("language", randomLanguage);
            store.put("world", world);
            String shown = store.show();
            System.out.println(shown);
            System.out.println("Uncompressed preference bytes: " + shown.length() * 2);

            store.preferences.clear();
            store.preferences.flush();
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
