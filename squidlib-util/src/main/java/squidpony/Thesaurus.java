package squidpony;

import regexodus.*;
import squidpony.squidmath.GapShuffler;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A text processing class that can swap out occurrences of words and replace them with their synonyms.
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class Thesaurus implements Serializable{
    private static final long serialVersionUID = 3387639905758074640L;
    protected static final Pattern wordMatch = Pattern.compile("(\\pL+)");
    public LinkedHashMap<String, GapShuffler<String>> mappings;
    protected StatefulRNG rng;
    public Thesaurus()
    {
        mappings = new LinkedHashMap<>(256);
        rng = new StatefulRNG();
    }
    public Thesaurus(RNG rng)
    {
        mappings = new LinkedHashMap<>(256);
        this.rng = new StatefulRNG(rng.nextLong());
    }

    /**
     * Allows this Thesaurus to find the exact words in synonyms and, when requested, replace each occurrence with a
     * different word from the same Collection. Each word in synonyms should have the same part of speech, so "demon"
     * and "devils" should not be in the same list of synonyms (singular noun and plural noun), but "demon" and "devil"
     * could be (each is a singular noun). The Strings in synonyms should all be lower-case, since case is picked up
     * from the text as it is being replaced and not from the words themselves. Proper nouns should normally not be used
     * as synonyms, since this could get very confusing if it changed occurrences of "Germany" to "France" at random and
     * a character's name, like "Dorothy", to "Anne", "Emily", "Cynthia", etc. in the middle of a section about Dorothy.
     * The word matching pattern this uses only matches all-letter words, not words that contain hyphens, apostrophes,
     * or other punctuation.
     * @param synonyms a Collection of lower-case Strings with similar meaning and the same part of speech
     * @return this for chaining
     */
    public Thesaurus addSynonyms(Collection<String> synonyms)
    {
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng);
        for(String syn : synonyms)
        {
            mappings.put(syn, shuffler);
        }
        return this;
    }
    /**
     * Given a String, StringBuilder, or other CharSequence that should contain words this knows synonyms for, this
     * replaces each occurrence of such a known word with one of its synonyms, leaving unknown words untouched. Words
     * that were learned together as synonyms with addSynonyms() will be replaced in such a way that an individual
     * replacement word should not occur too close to a previous occurrence of the same word; that is, replacing the
     * text "You fiend! You demon! You despoiler of creation; devil made flesh!", where "fiend", "demon", and "devil"
     * are all synonyms, would never produce a string that contained "fiend" as the replacement for all three of those.
     * @param text a CharSequence, such as a String, that contains words in the source language
     * @return a String of the translated text.
     */
    public String process(CharSequence text)
    {
        Replacer rep = wordMatch.replacer(new SynonymSubstitution());
        return rep.replace(text);
    }

    public String lookup(String word)
    {
        if(word.isEmpty())
            return word;
        if(mappings.containsKey(word))
        {
            String nx = mappings.get(word).getNext();
            if(nx.isEmpty())
                return nx;
            if(word.length() > 1 && Character.isUpperCase(word.charAt(1)))
                return nx.toUpperCase();
            if(Character.isUpperCase(word.charAt(0)))
            {
                return Character.toUpperCase(nx.charAt(0)) + nx.substring(1, nx.length());
            }
            return nx;
        }
        return word;
    }

    private class SynonymSubstitution implements Substitution
    {
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            dest.append(lookup(match.group(0)));
        }
    }
}
