package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.OrderedSet;

import java.util.regex.Pattern;

/**
 * Created by Tommy Ettinger on 4/25/2019.
 */
public class WordsInBook extends ApplicationAdapter {
    public void create() {
        System.out.println("Starting up...");
        String book = Gdx.files.internal("bible_no_numbers.txt").readString("UTF8");
        System.out.println("Read in file; splitting up words...");
        String[] items = Pattern.compile("[^A-Za-z]+").split(book);
        CrossHash.IHasher hasher = new CrossHash.IHasher() {
            @Override
            public int hash(final Object data0) {
                if(data0 == null)
                    return 0;
                if(!(data0 instanceof CharSequence))
                    return data0.hashCode();
                CharSequence data = (CharSequence)data0;
                int result = 0x1A976FDF, z = 0x60642E25;
                final int len = data.length();
                for (int i = 0; i < len; i++) {
                    result ^= (z += (data.charAt(i) & 31) * 0x9E3779B9);
                    z ^= (result = (result << 20 | result >>> 12));
                }
                result += (z ^ z >>> 15 ^ 0xAE932BD5) * 0x632B9;
                result = (result ^ result >>> 15) * 0xFF51D;
                result = (result ^ result >>> 15) * 0xC4CEB;
                return result ^ result >>> 15;
            }

            @Override
            public boolean areEqual(Object left, Object right) {
                if(left == right)
                    return true;
                if(!(left instanceof CharSequence && right instanceof CharSequence))
                    return false;
                CharSequence l = (CharSequence)left, r = (CharSequence)right;
                int llen = l.length(), rlen = r.length();
                if(llen != rlen)
                    return false;
                for (int i = 0; i < llen; i++) {
                    if((l.charAt(i) & 31) != (r.charAt(i) & 31))
                        return false;
                }
                return true;
            }
        };
        System.out.println("Building OrderedSet...");
        OrderedSet<String> os = new OrderedSet<>(items, hasher);
        final int len = os.size();
        System.out.println("Length is " + len);
//        os.shuffle(new MoonwalkRNG());
        StringBuilder sb = new StringBuilder(len * 8);
        StringBuilder sbad = new StringBuilder(len * 8);
        for (String str : os)
        {
            StringKit.appendHex(sb, hasher.hash(str));
            StringKit.appendHex(sbad, str.toLowerCase().hashCode());
        }
        System.out.println("Writing to file...");
        Gdx.files.local("WordHashes.txt").writeString(sb.toString(), false, "UTF8");
        Gdx.files.local("BadHashes.txt").writeString(sbad.toString(), false, "UTF8");
        System.out.println("Done!");
        Gdx.app.exit();
    }
    public static void main(String[] args)
    {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new WordsInBook(), config);
    }
}
