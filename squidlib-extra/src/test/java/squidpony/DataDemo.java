package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.JsonWriter;
import squidpony.squidmath.OrderedMap;

import java.util.Scanner;

/**
 * A simple demonstration of how DataConverter and DataCompressor can be used independently of Preferences (which is
 * what SquidStorage uses for saving, mainly). This reads in a public domain fragment of a game design, 200 summaries of
 * medieval character classes with abilities and some minor stat numbers, from a) an uncompressed JSON file, b) a highly
 * compressed "JSON through a food processor" file, and a tab-separated value file that served as the original data (it
 * was copied from a spreadsheet). If the files exist (they won't necessarily exist on the first run), then the contents
 * of the two JSON-like files are read into OrderedMap data structures, and values are pulled from alternating Maps, in
 * order, and printed to show they produce the same output as the input file(the tab-separated one). After that, the
 * tab-separated file is read, parsed, and output is demonstrated by writing the adventurers OrderedMap to the same two
 * files that were (possibly) read earlier, enabling a future test to try the output of this run as input for its run.
 * <br>
 * Created by Tommy Ettinger on 1/10/2017.
 */
public class DataDemo extends ApplicationAdapter {
    private static String ROOT_DIR = "squidlib-extra/"; // "squidlib-extra/" if you're running this file from the parent project and "" if you're running from the squidlib-extra project directly

    @Override
    public void create() {
        super.create();
        DataConverter convert = new DataConverter(JsonWriter.OutputType.json);
        DataCompressor compress = new DataCompressor();
        OrderedMap<String, Adventurer> adventurers = new OrderedMap<>(200, 0.8f);
        if(Gdx.files.local(ROOT_DIR + "src/test/resources/generated/UncompressedAdventurers.js").exists()) {
            OrderedMap<String, Adventurer> fromNormal = convert.fromJson(adventurers.getClass(),
                    Gdx.files.local(ROOT_DIR + "src/test/resources/generated/UncompressedAdventurers.js")),
                    fromCompressed = compress.fromJson(adventurers.getClass(),
                            Gdx.files.local(ROOT_DIR + "src/test/resources/generated/CompressedAdventurers.js"));
            for (int i = 0; i < 200; i++) {
                if ((i & 1) == 0)
                    System.out.println(fromNormal.getAt(i).serializeToString());
                else
                    System.out.println(fromCompressed.getAt(i).serializeToString());
            }
        }
        // write out the JSON files read from a tab-separated value file
        String txt = Gdx.files.classpath("MedievalClasses.txt").readString();
        Scanner scanner = new Scanner(txt);
        while (scanner.hasNextLine())
        {
            Adventurer adventurer = Adventurer.deserializeFromString(scanner.nextLine());
            adventurers.put(adventurer.name, adventurer);
        }
        Gdx.files.local(ROOT_DIR + "src/test/resources/generated/UncompressedAdventurers.js")
                .writeString(convert.toJson(adventurers, OrderedMap.class), false, "UTF-8");
        Gdx.files.local(ROOT_DIR + "src/test/resources/generated/CompressedAdventurers.js")
                .writeString(compress.toJson(adventurers, OrderedMap.class), false, "UTF-8");

        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: DataDemo";
        config.width = 512;
        config.height = 128;
        new LwjglApplication(new DataDemo(), config);
    }

    public static class Adventurer
    {
        public String name;
        public String[] features;
        public int meleeRes, rangedRes, magicRes, ailmentRes;
        public Adventurer()
        {
            this("Sommelier", 0, 4, 4, 0,
                    "Scorn aura, dominating",
                    "Restaurant Base, upgrade Wine",
                    "Limitless Wine, identify potions",
                    "Limitless Cheese, cleanse items");
        }
        public Adventurer(String title, int melee, int ranged, int magic, int ailment, String... traits)
        {
            name = title;
            meleeRes = melee;
            rangedRes = ranged;
            magicRes = magic;
            ailmentRes = ailment;
            features = traits;
        }
        public String serializeToString()
        {
            return name + '\t' + StringKit.join("\t", features)
                    + '\t' + meleeRes + '\t' + rangedRes + '\t' + magicRes + '\t' + ailmentRes;
        }
        public static Adventurer deserializeFromString(String text)
        {
            String[] parts = StringKit.split(text, "\t");
            if(parts.length != 9)
                return new Adventurer();
            return new Adventurer(parts[0], Integer.parseInt(parts[5]), Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7]), Integer.parseInt(parts[8]), parts[1], parts[2], parts[3], parts[4]);
        }
    }
}
