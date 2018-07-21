package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import java.util.List;

/**
 * Reads in a compressed MarkovTextLimited object from a file and generates/prints text with it.
 * <br>
 * Created by Tommy Ettinger on 7/17/2018.
 */
public class MarkovReadTest extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        long time = System.currentTimeMillis();
        String text = LZSPlus.decompress(Gdx.files.classpath("bible_markov_order_2_compressed.dat").readString("UTF16"));
        MarkovText mt = MarkovText.deserializeFromString(text);
        System.out.print((System.currentTimeMillis() - time));
        System.out.println(" ms");

        StringBuilder sb = new StringBuilder(10000);
        long seed = 1234567890L;
        while (sb.length() < 10000)
            sb.append(mt.chain(++seed, 1000)).append(' ');
        List<String> ls = StringKit.wrap(sb, 80);
        for(String s : ls)
        {
            System.out.println(s);
        }
        Gdx.app.exit();
    }

    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new MarkovReadTest(), config);
    }
}
