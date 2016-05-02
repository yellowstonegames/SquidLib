package squidpony;

import regexodus.*;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.StatefulRNG;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Tommy Ettinger on 5/1/2016.
 */
public class LanguageCipher implements Serializable{
    public FakeLanguageGen language;
    private StatefulRNG rng;
    // not a LinkedHashMap because this should never be need a random element to be requested
    private HashMap<String, String> table;
    private static final Pattern wordMatch = Pattern.compile("(\\pL+)|(\\pL[\\pL\\p{Pd}]*\\pL)");
    public LanguageCipher()
    {
        this(FakeLanguageGen.ENGLISH);
    }

    public LanguageCipher(FakeLanguageGen language)
    {
        this.language = language.copy();
        rng = new StatefulRNG();
        table = new HashMap<>(512);
    }

    public LanguageCipher(LanguageCipher other)
    {
        this.language = other.language.copy();
        this.rng = new StatefulRNG();
        this.table = new HashMap<>(other.table);
    }

    public String lookup(String source)
    {
        if(source == null || source.isEmpty())
            return "";
        String s2 = source.toLowerCase(), ciphered;
        if(table.containsKey(s2))
            ciphered = table.get(s2);
        else {
            long h = CrossHash.hash64(s2);
            rng.setState(h);
            ciphered = language.word(rng, false, (int) Math.ceil(s2.length() / (2.2 + rng.nextDouble())));
            table.put(s2, ciphered);
        }
        char[] chars = ciphered.toCharArray();
        // Lu is the upper case letter category in Unicode; we're using regexodus for this because GWT probably
        // won't respect unicode case data on its own. We are using GWT to capitalize, though. Hope it works...
        if(Category.Lu.contains(source.charAt(0)))
            chars[0] = Character.toUpperCase(chars[0]);
        if(source.length() > 1 && Category.Lu.contains(source.charAt(1))) {
            for (int i = 1; i < chars.length; i++) {
                chars[i] = Character.toUpperCase(chars[i]);
            }
        }
        return new String(chars);
    }

    public String cipher(CharSequence text)
    {
        Replacer rep = wordMatch.replacer(new Substitution() {
            @Override
            public void appendSubstitution(MatchResult match, TextBuffer dest) {
                dest.append(lookup(match.group(0)));
            }
        });
        return rep.replace(text);
    }
}
