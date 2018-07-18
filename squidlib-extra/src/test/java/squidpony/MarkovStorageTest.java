package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

/**
 * Reads in an edited copy of Project Gutenberg's King James Bible and produces a compressed MarkovText file. This file
 * can be read back in MarkovReadTest in the same package.
 * <br>
 * Created by Tommy Ettinger on 7/17/2018.
 */
public class MarkovStorageTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        long time = System.currentTimeMillis();
        final String text = Gdx.files.classpath("bible_no_numbers.txt").readString("UTF8");
        MarkovText mt = new MarkovText();
        mt.analyze(text.replace("\n\n", "\n."));
        String data = mt.serializeToString();
        //Gdx.files.local("bible_markov.dat").writeString(data, false, "UTF8");
        Gdx.files.local("bible_markov_compressed.dat").writeString(LZSPlus.compress(data), false, "UTF16");
        System.out.print((System.currentTimeMillis() - time));
        System.out.println(" ms");
        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new MarkovStorageTest(), config);
    }
}
