package squidpony;

import regexodus.*;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static squidpony.Maker.makeList;
import static squidpony.Maker.makeOM;

/**
 * A text processing class that can swap out occurrences of words and replace them with their synonyms.
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class Thesaurus implements Serializable{
    private static final long serialVersionUID = 3387639905758074640L;
    protected static final Pattern wordMatch = Pattern.compile("([\\pL`]+)"),
            similarFinder = Pattern.compile(".*?\\b(\\w\\w\\w\\w).*?{\\@1}.*$", "ui");
    public OrderedMap<CharSequence, GapShuffler<String>> mappings;
    public ArrayList<FakeLanguageGen.Alteration> alterations = new ArrayList<>(4);
    protected StatefulRNG rng;
    public transient ArrayList<FakeLanguageGen> randomLanguages = new ArrayList<>(2);
    public transient String latestGenerated = "Nationia";
    /**
     * Constructs a new Thesaurus with an unseeded RNG used to shuffle word order.
     */
    public Thesaurus()
    {
        mappings = new OrderedMap<>(256, Hashers.caseInsensitiveStringHasher);
        rng = new StatefulRNG();
    }

    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with the next long from the given RNG.
     * @param rng an RNG that will only be used to get one long (for seeding this class' RNG)
     */
    public Thesaurus(IRNG rng)
    {
        mappings = new OrderedMap<>(256, Hashers.caseInsensitiveStringHasher);
        this.rng = new StatefulRNG(rng.nextLong());
    }

    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a long for seeding this class' RNG
     */
    public Thesaurus(long shuffleSeed)
    {
        mappings = new OrderedMap<>(256, Hashers.caseInsensitiveStringHasher);
        this.rng = new StatefulRNG(shuffleSeed);
    }


    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a String for seeding this class' RNG
     */
    public Thesaurus(String shuffleSeed)
    {
        mappings = new OrderedMap<>(256, Hashers.caseInsensitiveStringHasher);
        this.rng = new StatefulRNG(shuffleSeed);
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
        if(synonyms.isEmpty())
            return this;
        long prevState = rng.getState();
        rng.setState(CrossHash.hash64(synonyms));
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng);
        for(String syn : synonyms)
        {
            mappings.put(syn, shuffler);
        }
        rng.setState(prevState);
        return this;
    }

    public Thesaurus addReplacement(CharSequence before, String after)
    {
        mappings.put(before, new GapShuffler<String>(after));
        return this;
    }

    /**
     * Allows this Thesaurus to replace a specific keyword, typically containing multiple backtick characters ('`') so
     * it can't be confused with a "real word," with one of the words in synonyms (chosen in shuffled order). The
     * backtick is the only punctuation character that this class' word matcher considers part of a word, both for this
     * reason and because it is rarely used in English text.
     * @param keyword a word (typically containing backticks, '`') that will be replaced by a word from synonyms
     * @param synonyms a Collection of lower-case Strings with similar meaning and the same part of speech
     * @return this for chaining
     */
    public Thesaurus addCategory(String keyword, Collection<String> synonyms)
    {
        if(synonyms.isEmpty())
            return this;
        long prevState = rng.getState();
        rng.setState(CrossHash.hash64(synonyms));
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng);
        mappings.put(keyword, shuffler);
        rng.setState(prevState);
        return this;
    }

    /**
     * Adds several pre-made categories to this Thesaurus' known categories, but won't cause it to try to replace normal
     * words with synonyms (only categories, which contain backticks in the name). The keywords this currently knows,
     * and the words it will replace those keywords with, are:
     * <br>
     * (THIS IS OUT OF DATE, THERE ARE MORE KNOWN.)
     * <br>
     * <ul>
     *     <li>"calm`adj`": harmonious, peaceful, pleasant, serene, placid, tranquil, calm</li>
     *     <li>"calm`noun`": harmony, peace, kindness, serenity, tranquility, calm</li>
     *     <li>"org`noun`": fraternity, brotherhood, order, group, foundation, association, guild, fellowship, partnership</li>
     *     <li>"org`nouns`": fraternities, brotherhoods, orders, groups, foundations, associations, guilds, fellowships, partnerships</li>
     *     <li>"empire`adj`": imperial, prince's, king's, sultan's, regal, dynastic, royal, hegemonic, monarchic, ascendant, emir's, lordly</li>
     *     <li>"empire`noun`": empire, emirate, kingdom, sultanate, dominion, dynasty, imperium, hegemony, triumvirate, ascendancy, monarchy, commonwealth</li>
     *     <li>"empire`nouns`": empires, emirates, kingdoms, sultanates, dominions, dynasties, imperia, hegemonies, triumvirates, ascendancies, monarchies, commonwealths</li>
     *     <li>"union`adj`": united, allied, people's, confederated, federated, congressional, independent, associated, unified, democratic</li>
     *     <li>"union`noun`": union, alliance, coalition, confederation, federation, congress, confederacy, league, faction, republic</li>
     *     <li>"union`nouns`": unions, alliances, coalitions, confederations, federations, congresses, confederacies, leagues, factions, republics</li>
     *     <li>"militia`noun`": rebellion, resistance, militia, liberators, warriors, fighters, militants, front, irregulars</li>
     *     <li>"militia`nouns`": rebellions, resistances, militias, liberators, warriors, fighters, militants, fronts, irregulars</li>
     *     <li>"gang`noun`": gang, syndicate, mob, crew, posse, mafia, cartel</li>
     *     <li>"gang`nouns`": gangs, syndicates, mobs, crews, posses, mafias, cartels</li>
     *     <li>"duke`noun`": duke, earl, baron, fief, lord, shogun</li>
     *     <li>"duke`nouns`": dukes, earls, barons, fiefs, lords, shoguns</li>
     *     <li>"duchy`noun`": duchy, earldom, barony, fiefdom, lordship, shogunate</li>
     *     <li>"duchy`nouns`": duchies, earldoms, baronies, fiefdoms, lordships, shogunates</li>
     *     <li>"magical`adj`": arcane, enchanted, sorcerous, ensorcelled, magical, mystical</li>
     *     <li>"holy`adj`": auspicious, divine, holy, sacred, prophetic, blessed, godly</li>
     *     <li>"unholy`adj`": bewitched, occult, unholy, macabre, accursed, profane, vile</li>
     *     <li>"forest`adj`": natural, primal, verdant, lush, fertile, bountiful</li>
     *     <li>"forest`noun`": nature, forest, greenery, jungle, woodland, grove, copse</li>
     *     <li>"fancy`adj`": grand, glorious, magnificent, magnanimous, majestic, great, powerful</li>
     *     <li>"evil`adj`": heinous, scurrilous, terrible, horrible, debased, wicked, evil, malevolent, nefarious, vile</li>
     *     <li>"good`adj`": righteous, moral, good, pure, compassionate, flawless, perfect</li>
     *     <li>"sinister`adj`": shadowy, silent, lethal, deadly, fatal, venomous, cutthroat, murderous, bloodstained, stalking</li>
     *     <li>"sinister`noun`": shadow, silence, assassin, ninja, venom, poison, snake, murder, blood, razor, tiger</li>
     *     <li>"blade`noun`": blade, knife, sword, axe, stiletto, katana, scimitar, hatchet, spear, glaive, halberd,
     *               hammer, maul, flail, mace, sickle, scythe, whip, lance, nunchaku, saber, cutlass, trident</li>
     *     <li>"bow`noun`": bow, longbow, shortbow, crossbow, sling, atlatl, bolas, javelin, net, shuriken, dagger</li>
     *     <li>"weapon`noun`": blade, knife, sword, axe, stiletto, katana, scimitar, hatchet, spear, glaive, halberd,
     *               hammer, maul, flail, mace, sickle, scythe, whip, lance, nunchaku, saber, cutlass, trident,
     *               bow, longbow, shortbow, crossbow, sling, atlatl, bolas, javelin, net, shuriken, dagger</li>
     *     <li>"musket`noun`": arquebus, blunderbuss, musket, matchlock, flintlock, wheellock, cannon</li>
     *     <li>"grenade`noun`": rocket, grenade, missile, bomb, warhead, explosive, flamethrower</li>
     *     <li>"rifle`noun`": pistol, rifle, handgun, firearm, longarm, shotgun</li>
     *     <li>"blade`nouns`": blades, knives, swords, axes, stilettos, katana, scimitars, hatchets, spears, glaives, halberds,
     *               hammers, mauls, flails, maces, sickles, scythes, whips, lances, nunchaku, sabers, cutlasses, tridents</li>
     *     <li>"bow`nouns`": bows, longbows, shortbows, crossbows, slings, atlatls, bolases, javelins, nets, shuriken, daggers</li>
     *     <li>"weapon`nouns`": blades, knives, swords, axes, stilettos, katana, scimitars, hatchets, spears, glaives, halberds,
     *               hammers, mauls, flails, maces, sickles, scythes, whips, lances, nunchaku, sabers, cutlasses, tridents,
     *               bows, longbows, shortbows, crossbows, slings, atlatls, bolases, javelins, nets, shuriken, daggers</li>
     *     <li>"musket`nouns`": arquebusses, blunderbusses, muskets, matchlocks, flintlocks, wheellocks, cannons</li>
     *     <li>"grenade`nouns`": rockets, grenades, missiles, bombs, warheads, explosives, flamethrowers</li>
     *     <li>"rifle`nouns`": pistols, rifles, handguns, firearms, longarms, shotguns</li>
     *     <li>"tech`adj`": cyber, digital, electronic, techno, hacker, crypto, turbo, mechanical, servo</li>
     *     <li>"sole`adj`": sole, true, singular, total, ultimate, final, last</li>
     *     <li>"light`adj`": bright, glowing, solar, stellar, lunar, radiant, luminous, shimmering</li>
     *     <li>"light`noun`": light, glow, sun, star, moon, radiance, dawn, torch</li>
     *     <li>"light`nouns`": lights, glimmers, suns, stars, moons, torches</li>
     *     <li>"smart`adj`": brilliant, smart, genius, wise, clever, cunning, mindful, aware</li>
     *     <li>"smart`noun`": genius, wisdom, cunning, awareness, mindfulness, acumen, smarts, knowledge</li>
     *     <li>"bandit`noun`": thief, raider, bandit, rogue, brigand, highwayman, pirate</li>
     *     <li>"bandit`nouns`": thieves, raiders, bandits, rogues, brigands, highwaymen, pirates</li>
     *     <li>"guard`noun`": protector, guardian, warden, defender, guard, shield, sentinel, watchman, knight</li>
     *     <li>"guard`nouns`": protectors, guardians, wardens, defenders, guards, shields, sentinels, watchmen, knights</li>
     *     <li>"rage`noun`": rage, fury, anger, wrath, frenzy, vengeance</li>
     * </ul>
     * Capitalizing the first letter in the keyword where it appears in text you call process() on will capitalize the
     * first letter of the produced fake word. Capitalizing the second letter will capitalize the whole produced fake
     * word. This applies only per-instance of each keyword; it won't change the internally-stored list of words.
     * @return this for chaining
     */
    public Thesaurus addKnownCategories()
    {
        for(Map.Entry<String, ArrayList<String>> kv : categories.entrySet())
        {
            addCategory(kv.getKey(), kv.getValue());
        }
        return this;
    }

    /**
     * Adds a large list of words pre-generated by FakeLanguageGen and hand-picked for fitness, and makes them
     * accessible with a keyword based on the language and any tweaks made to it. The keywords this currently knows:
     * <br>
     * <ul>
     *     <li>"jp`gen`": Imitation Japanese</li>
     *     <li>"fr`gen`": Imitation French; contains some accented chars</li>
     *     <li>"gr`gen`": Imitation Greek (romanized)</li>
     *     <li>"ru`gen`": Imitation Russian (romanized)</li>
     *     <li>"sw`gen`": Imitation Swahili</li>
     *     <li>"so`gen`": Imitation Somali</li>
     *     <li>"en`gen`": Imitation English (not very good on its own)</li>
     *     <li>"ar`gen`": Imitation Arabic (better); doesn't have accents and should be more readable</li>
     *     <li>"nr`gen`": Imitation Old Norse (using simpler and easier-to-pronounce spelling)</li>
     *     <li>"nr`acc`gen`": Imitation Old Norse (using the uncommon letters used in modern Icelandic)</li>
     *     <li>"hi`gen`": Imitation Hindi (romanized and with accents removed)</li>
     *     <li>"kr`gen`": Imitation Korean (romanized)</li>
     *     <li>"na`gen`": Imitation Nahuatl (spelled like how Spanish loanwords from Nahuatl are spelled)</li>
     *     <li>"mn`gen`": Imitation Mongolian (the form of the language from medieval times, romanized)</li>
     *     <li>"in`gen`": Imitation Inuktitut (romanized)</li>
     *     <li>"si`gen`": "Simplish" (simplified imitation English, without special word endings like "-ight")</li>
     *     <li>"fn`gen`": Fantasy Names; styled after the possibly-Europe-like names common in fantasy books</li>
     *     <li>"fn`acc`gen`": Fancy Fantasy Names; the same as "fn`gen`", but with lots of accented chars</li>
     *     <li>"lc`gen`": Lovecraft; styled after the names of creatures from H.P. Lovecraft's Cthulhu Mythos</li>
     *     <li>"el`gen`": Elf, modeled after J.R.R. Tolkien's languages for elves</li>
     *     <li>"gb`gen`": Goblin, fantasy language of sneaky species</li>
     *     <li>"if`gen`": Infernal, fantasy language of clever and subtle fiends</li>
     *     <li>"dm`gen`": Demonic, fantasy language of brutish fiends</li>
     *     <li>"al`a`gen`": Alien A, fantasy language with very unusual consonants</li>
     *     <li>"al`e`gen`": Alien E, fantasy language with click sounds but no "lip sounds"</li>
     *     <li>"al`i`gen`": Alien I, fantasy language with many tones and "liquid sounds"</li>
     *     <li>"al`o`gen`": Alien O, fantasy language with long words and lengthy vowel clusters</li>
     *     <li>"al`u`gen`": Alien U, fantasy language with many accented consonants and different suffixes</li>
     *     <li>"ru`so`gen`": Mix of imitation Russian (75%) and Somali (25%)</li>
     *     <li>"gr`hi`gen`": Mix of imitation Greek (50%) and Hindi (accents removed, 50%)</li>
     *     <li>"sw`fr`gen`": Mix of imitation Swahili (70%) and French (30%)</li>
     *     <li>"ar`jp`gen`": Mix of imitation Arabic (accents removed, 60%) and Japanese (40%)</li>
     *     <li>"sw`gr`gen`": Mix of imitation Swahili (60%) and Greek (40%)</li>
     *     <li>"gr`so`gen`": Mix of imitation Greek (60%) and Somali (40%)</li>
     *     <li>"en`hi`gen`": Mix of imitation English (60%) and Hindi (accents removed, 40%)</li>
     *     <li>"en`jp`gen`": Mix of imitation English (60%) and Japanese (40%)</li>
     *     <li>"so`hi`gen`": Mix of imitation Somali (60%) and Hindi (accents removed, 40%)</li>
     *     <li>"ru`gr`gen`": Mix of imitation Russian (60%) and Greek (40%)</li>
     *     <li>"lc`gr`gen`": Mix of Lovecraft-styled names (60%) and imitation Russian (40%)</li>
     *     <li>"fr`mod`gen`": Imitation French; modified to replace doubled consonants like "gg" with "gsh" or similar</li>
     *     <li>"jp`mod`gen`": Imitation Japanese; modified to sometimes double vowels from "a" to "aa" or similar</li>
     *     <li>"so`mod`gen`": Imitation Somali (not really); modified beyond recognition and contains accents</li>
     * </ul>
     * Capitalizing the first letter in the keyword where it appears in text you call process() on will capitalize the
     * first letter of the produced fake word, which is often desirable for things like place names. Capitalizing the
     * second letter will capitalize the whole produced fake word. This applies only per-instance of each keyword; it
     * won't change the internally-stored list of words.
     * @return this for chaining
     */
    public Thesaurus addFakeWords()
    {
        long state = rng.getState();
        for(Map.Entry<CharSequence, FakeLanguageGen> kv : languages.entrySet())
        {
            ArrayList<String> words = new ArrayList<>(16);
            for (int i = 0; i < 16; i++) {
                words.add(kv.getValue().word(rng, false, rng.between(2, 4)));
            }
            addCategory(StringKit.replace(kv.getKey(), "gen", "pre"), words);
        }
        rng.setState(state);
        return this;
    }

    private StringBuilder modify(CharSequence text)
    {
        Matcher m;
        StringBuilder sb = new StringBuilder(text);
        Replacer.StringBuilderBuffer tb, working = Replacer.wrap(sb);
        StringBuilder tmp;
        boolean found;
        FakeLanguageGen.Alteration alt;
        for (int a = 0; a < alterations.size(); a++) {
            alt = alterations.get(a);
            tmp = working.sb;
            tb = Replacer.wrap(new StringBuilder(tmp.length()));
            m = alt.replacer.getPattern().matcher(tmp);

            found = false;
            while (true) {
                if (rng.nextDouble() < alt.chance) {
                    if (!Replacer.replaceStep(m, alt.replacer.getSubstitution(), tb))
                        break;
                    found = true;
                } else {
                    if (!m.find())
                        break;
                    found = true;
                    m.getGroup(MatchResult.PREFIX, tb);
                    m.getGroup(MatchResult.MATCH, tb);
                    m.setTarget(m, MatchResult.SUFFIX);
                }
            }
            if (found) {
                m.getGroup(MatchResult.TARGET, tb);
                working = tb;
            }
        }
        return working.sb;

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
        if(alterations.isEmpty())
            return rep.replace(text);
        else
            return modify(rep.replace(text)).toString();
    }

    public String lookup(String word)
    {
        if(word.isEmpty())
            return word;
        String word2 = word.toLowerCase();
        if(mappings.containsKey(word2))
        {
            String nx = mappings.get(word2).next();
            if(nx.isEmpty())
                return nx;
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
                return nx.toUpperCase();
            if(Category.Lu.contains(word.charAt(0)))
            {
                return Character.toUpperCase(nx.charAt(0)) + nx.substring(1, nx.length());
            }
            return nx;
        }
        else if(languages.containsKey(word2))
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
                return languages.get(word2).word(rng, false, rng.between(2, 4)).toUpperCase();
            if(Category.Lu.contains(word.charAt(0)))
            {
                return languages.get(word2).word(rng, true, rng.between(2, 4));
            }
            return languages.get(word2).word(rng, false, rng.between(2, 4));
        }
        return word;
    }

    private class SynonymSubstitution implements Substitution
    {
        private StringBuilder temp = new StringBuilder(64);
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            //dest.append(lookup(match.group(0)));
            temp.setLength(0);
            match.getGroup(0, temp);
            writeLookup(dest, temp);
        }
    }

    private void writeLookup(TextBuffer dest, StringBuilder word) {
        if(word.length() <= 0)
            return;
        if(mappings.containsKey(word))
        {
            String nx = mappings.get(word).next();
            if(nx.isEmpty())
                return;
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(nx.toUpperCase());
                return;
            }
            if(Category.Lu.contains(word.charAt(0)))
            {
                dest.append(Character.toUpperCase(nx.charAt(0)));
                dest.append(nx.substring(1, nx.length()));
                return;
            }
            dest.append(nx);
            return;
        }
        else if(languages.containsKey(word))
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(languages.get(word).word(rng, false, rng.between(2, 4)).toUpperCase());
            }
            else if(Category.Lu.contains(word.charAt(0)))
            {
                dest.append(languages.get(word).word(rng, true, rng.between(2, 4)));
            }
            else
            {
                dest.append(languages.get(word).word(rng, false, rng.between(2, 4)));
            }
            return;
        }
        if(dest instanceof Replacer.StringBuilderBuffer)
        {
            ((Replacer.StringBuilderBuffer)dest).sb.append(word);
        }
        else
            dest.append(word.toString());

    }

    private class RandomLanguageSubstitution implements Substitution
    {
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            FakeLanguageGen lang = FakeLanguageGen.randomLanguage(rng.nextLong());
            randomLanguages.add(lang);
            if(match.isCaptured(1))
            {
                lang = FakeLanguageGen.randomLanguage(rng.nextLong());
                randomLanguages.add(lang);
                do {
                    latestGenerated = randomLanguages.get(0).word(rng, true, Math.min(rng.between(2, 5), rng.between(1, 5)))
                            + "-" + randomLanguages.get(1).word(rng, true, Math.min(rng.between(2, 5), rng.between(1, 5)));
                }while (latestGenerated.length() <= 5 || latestGenerated.length() >= 17);
                dest.append(latestGenerated);
            }
            else
            {
                do{
                    latestGenerated = lang.word(rng, true, Math.min(rng.between(2, 5), rng.between(1, 5)));
                }while (latestGenerated.length() <= 2 || latestGenerated.length() >= 11);
                dest.append(latestGenerated);
            }
        }
    }

    private class KnownLanguageSubstitution implements Substitution
    {
        public FakeLanguageGen language;
        public KnownLanguageSubstitution(FakeLanguageGen lang)
        {
            language = lang;
        }
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            if (match.isCaptured(1)) {
                do
                {
                    latestGenerated = language.word(rng, true, Math.min(rng.between(2, 5), rng.between(1, 5))) +
                            "-" + language.word(rng, true, Math.min(rng.between(2, 5), rng.between(1, 5)));
                }while (latestGenerated.length() <= 5 || latestGenerated.length() >= 17);
                dest.append(latestGenerated);
            } else
            {
                do{
                    latestGenerated = language.word(rng, true, Math.min(rng.between(2, 5), rng.between(1, 5)));
                }while (latestGenerated.length() <= 2 || latestGenerated.length() >= 11);
                dest.append(latestGenerated);
            }
        }
    }

    /**
     * Generates a random possible name for a nation, such as "Iond-Gouccief Alliance" or "The Last Drayo Commonwealth".
     * Needs {@link #addKnownCategories()} to be called on this Thesaurus first. May use accented characters, as in
     * "Thùdshù-Hyóttiálb Hegemony" or "The Glorious Chô Empire"; if you want to strip these out and replace accented
     * chars with their un-accented counterparts, you can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making nation names, by
     * getting the FakeLanguageGen elements of this class' {@link #randomLanguages} field. Using one of these
     * FakeLanguageGen objects, you can produce many more words with a similar style to the nation name, like "Drayo" in
     * "The Last Drayo Commonwealth". If more than one language was used in the nation name, as in "Thùdshù-Hyóttiálb
     * Hegemony", you will have two languages in randomLanguages, so here "Thùdshù" would be generated by the first
     * language, and "Hyóttiálb" by the second language. Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can. This also assigns the
     * {@link #latestGenerated} field to contain the part of the nation name without any larger titles; in the case of
     * "The Glorious Chô Empire", the latestGenerated field would be assigned "Chô" at the same time the longer name
     * would be returned. This field will be reassigned if this method is called again.
     *
     * @return a random name for a nation or a loose equivalent to a nation, as a String
     */
    public String makeNationName()
    {
        String working = process(rng.getRandomElement(nationTerms));
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(rng.getRandomElement(nationTerms));
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working);
    }
    /**
     * Generates a random possible name for a nation, such as "Iond-Gouccief Alliance" or "The Last Drayo Commonwealth",
     * with the FakeLanguageGen already available instead of randomly created. Needs {@link #addKnownCategories()} to be
     * called on this Thesaurus first. May use accented characters, as in "Thùdshù Hegemony" or "The Glorious Chô
     * Empire", if the given language can produce them; if you want to strip these out and replace accented chars
     * with their un-accented counterparts, you can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link FakeLanguageGen#removeAccents()} on the FakeLanguageGen you would give this. This assigns the
     * {@link #latestGenerated} field to contain the part of the nation name without any larger titles; in the case of
     * "The Glorious Chô Empire", the latestGenerated field would be assigned "Chô" at the same time the longer name
     * would be returned. This field will be reassigned if this method is called again.
     * <br>
     * Some nation names use a hyphenated pairing of what would normally be names in two different languages; if one of
     * those names is produced by this it will produce two names in the same linguistic style. The randomLanguages field
     * is not populated by this method; it is assumed that since you are passing this a FakeLanguageGen, you already
     * have the one you want to use anyway.
     *
     * @param language a FakeLanguageGen that will be used to construct any non-English names
     * @return a random name for a nation or a loose equivalent to a nation, as a String
     */
    public String makeNationName(FakeLanguageGen language)
    {
        String working = process(rng.getRandomElement(nationTerms));
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(rng.getRandomElement(nationTerms));
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working);
    }

    private static final String[] nationTerms = new String[]{
            "Union`adj` Union`noun` of @", "Union`adj` @ Union`noun`", "@ Union`noun`", "@ Union`noun`", "@-@ Union`noun`", "Union`adj` Union`noun` of @",
            "Union`adj` Duchy`nouns` of @",  "The @ Duchy`noun`", "The Fancy`adj` @ Duchy`noun`", "The Sole`adj` @ Empire`noun`",
            "@ Empire`noun`", "@ Empire`noun`", "@ Empire`noun`", "@-@ Empire`noun`", "The Fancy`adj` @ Empire`noun`", "The Fancy`adj` @ Empire`noun`", "The Holy`adj` @ Empire`noun`",};

    public static final OrderedMap<String, ArrayList<String>> categories = makeOM(
            "calm`adj`",
            makeList("harmonious", "peaceful", "pleasant", "serene", "placid", "tranquil", "calm"),
            "calm`noun`",
            makeList("harmony", "peace", "kindness", "serenity", "tranquility", "calm"),
            "org`noun`",
            makeList("fraternity", "brotherhood", "order", "group", "foundation", "association", "guild", "fellowship", "partnership"),
            "org`nouns`",
            makeList("fraternities", "brotherhoods", "orders", "groups", "foundations", "associations", "guilds", "fellowships", "partnerships"),
            "empire`adj`",
            makeList("imperial", "prince's", "king's", "sultan's", "regal", "dynastic", "royal", "hegemonic", "monarchic", "ascendant", "emir's", "lordly"),
            "empire`noun`",
            makeList("empire", "emirate", "kingdom", "sultanate", "dominion", "dynasty", "imperium", "hegemony", "triumvirate", "ascendancy", "monarchy", "commonwealth"),
            "empire`nouns`",
            makeList("empires", "emirates", "kingdoms", "sultanates", "dominions", "dynasties", "imperia", "hegemonies", "triumvirates", "ascendancies", "monarchies", "commonwealths"),
            "emperor`noun`",
            makeList("emperor", "emir", "king", "sultan", "lord", "ruler", "pharaoh"),
            "emperor`nouns`",
            makeList("emperors", "emirs", "kings", "sultans", "lords", "rulers", "pharaohs"),
            "empress`noun`",
            makeList("empress", "emira", "queen", "sultana", "lady", "ruler", "pharaoh"),
            "empress`nouns`",
            makeList("empresses", "emiras", "queens", "sultanas", "ladies", "rulers", "pharaohs"),
            "union`adj`",
            makeList("united", "allied", "people's", "confederated", "federated", "congressional", "independent", "associated", "unified", "democratic"),
            "union`noun`",
            makeList("union", "alliance", "coalition", "confederation", "federation", "congress", "confederacy", "league", "faction", "republic"),
            "union`nouns`",
            makeList("unions", "alliances", "coalitions", "confederations", "federations", "congresses", "confederacies", "leagues", "factions", "republics"),
            "militia`noun`",
            makeList("rebellion", "resistance", "militia", "liberators", "warriors", "fighters", "militants", "front", "irregulars"),
            "militia`nouns`",
            makeList("rebellions", "resistances", "militias", "liberators", "warriors", "fighters", "militants", "fronts", "irregulars"),
            "gang`noun`",
            makeList("gang", "syndicate", "mob", "crew", "posse", "mafia", "cartel"),
            "gang`nouns`",
            makeList("gangs", "syndicates", "mobs", "crews", "posses", "mafias", "cartels"),
            "duke`noun`",
            makeList("duke", "earl", "baron", "fief", "lord", "shogun"),
            "duke`nouns`",
            makeList("dukes", "earls", "barons", "fiefs", "lords", "shoguns"),
            "duchy`noun`",
            makeList("duchy", "earldom", "barony", "fiefdom", "lordship", "shogunate"),
            "duchy`nouns`",
            makeList("duchies", "earldoms", "baronies", "fiefdoms", "lordships", "shogunates"),
            "magical`adj`",
            makeList("arcane", "enchanted", "sorcerous", "ensorcelled", "magical", "mystical"),
            "holy`adj`",
            makeList("auspicious", "divine", "holy", "sacred", "prophetic", "blessed", "godly", "virtuous"),
            "priest`noun`",
            makeList("priest", "bishop", "chaplain", "cleric", "cardinal", "preacher"),
            "priest`nouns`",
            makeList("priests", "bishops", "chaplains", "clergy", "cardinals", "preachers"),
            "unholy`adj`",
            makeList("bewitched", "occult", "unholy", "macabre", "accursed", "profane", "vile"),
            "witch`noun`",
            makeList("witch", "warlock", "necromancer", "cultist", "occultist", "defiler"),
            "witch`nouns`",
            makeList("witches", "warlocks", "necromancers", "cultists", "occultists", "defilers"),
            "forest`adj`",
            makeList("natural", "primal", "verdant", "lush", "fertile", "bountiful"),
            "forest`noun`",
            makeList("nature", "forest", "greenery", "jungle", "woodland", "grove", "copse"),
            "shaman`noun`",
            makeList("shaman", "druid", "warden", "animist"),
            "shaman`nouns`",
            makeList("shamans", "druids", "wardens", "animists"),
            "fancy`adj`",
            makeList("grand", "glorious", "magnificent", "magnanimous", "majestic", "great", "powerful"),
            "evil`adj`",
            makeList("heinous", "scurrilous", "terrible", "horrible", "debased", "wicked", "evil", "malevolent", "nefarious", "vile", "cruel", "abhorrent"),
            "villain`noun`",
            makeList("villain", "knave", "evildoer", "killer", "blasphemer", "monster", "murderer"),
            "villain`nouns`",
            makeList("villains", "knaves", "evildoers", "killers", "blasphemers", "monsters", "murderers"),
            "monster`noun`",
            makeList("fiend", "abomination", "demon", "devil", "ghoul", "monster", "beast", "creature"),
            "monsters`nouns`",
            makeList("fiends", "abominations", "demons", "devils", "ghouls", "monsters", "beasts", "creatures"),
            "good`adj`",
            makeList("righteous", "moral", "good", "pure", "compassionate", "flawless", "perfect", "kind"),
            "lethal`adj`",
            makeList("silent", "lethal", "deadly", "fatal", "venomous", "cutthroat", "murderous", "bloodstained", "stalking", "poisonous"),
            "lethal`noun`",
            makeList("silence", "killer", "assassin", "ninja", "venom", "poison", "snake", "murder", "blood", "razor", "tiger", "slayer"),
            "blade`noun`", // really any melee weapon
            makeList("blade", "knife", "sword", "axe", "stiletto", "katana", "scimitar", "hatchet", "spear", "glaive", "halberd",
                    "hammer", "maul", "flail", "mace", "sickle", "scythe", "whip", "lance", "nunchaku", "saber", "cutlass", "trident"),
            "bow`noun`", // really any medieval or earlier ranged weapon
            makeList("bow", "longbow", "shortbow", "crossbow", "sling", "atlatl", "bolas", "javelin", "net", "shuriken", "dagger"),
            "weapon`noun`", // any medieval or earlier weapon (not including firearms or newer)
            makeList("blade", "knife", "sword", "axe", "stiletto", "katana", "scimitar", "hatchet", "spear", "glaive", "halberd",
                    "hammer", "maul", "flail", "mace", "sickle", "scythe", "whip", "lance", "nunchaku", "saber", "cutlass", "trident",
                    "bow", "longbow", "shortbow", "crossbow", "sling", "atlatl", "bolas", "javelin", "net", "shuriken", "dagger"),
            "musket`noun`",
            makeList("arquebus", "blunderbuss", "musket", "matchlock", "flintlock", "wheellock", "cannon"),
            "grenade`noun`",
            makeList("rocket", "grenade", "missile", "bomb", "warhead", "explosive", "flamethrower"),
            "rifle`noun`",
            makeList("pistol", "rifle", "handgun", "firearm", "longarm", "shotgun"),
            "blade`nouns`",
            makeList("blades", "knives", "swords", "axes", "stilettos", "katana", "scimitars", "hatchets", "spears", "glaives", "halberds",
                    "hammers", "mauls", "flails", "maces", "sickles", "scythes", "whips", "lances", "nunchaku", "sabers", "cutlasses", "tridents"),
            "bow`nouns`",
            makeList("bows", "longbows", "shortbows", "crossbows", "slings", "atlatls", "bolases", "javelins", "nets", "shuriken", "daggers"),
            "weapon`nouns`",
            makeList("blades", "knives", "swords", "axes", "stilettos", "katana", "scimitars", "hatchets", "spears", "glaives", "halberds",
                    "hammers", "mauls", "flails", "maces", "sickles", "scythes", "whips", "lances", "nunchaku", "sabers", "cutlasses", "tridents",
                    "bows", "longbows", "shortbows", "crossbows", "slings", "atlatls", "bolases", "javelins", "nets", "shuriken", "daggers"),
            "musket`nouns`",
            makeList("arquebusses", "blunderbusses", "muskets", "matchlocks", "flintlocks", "wheellocks", "cannons"),
            "grenade`nouns`",
            makeList("rockets", "grenades", "missiles", "bombs", "warheads", "explosives", "flamethrowers"),
            "rifle`nouns`",
            makeList("pistols", "rifles", "handguns", "firearms", "longarms", "shotguns"),
            "scifi`adj`",
            makeList("plasma", "warp", "tachyonic", "phase", "gravitational", "photonic", "nanoscale", "laser", "quantum", "genetic"),
            "tech`adj`",
            makeList("cyber", "digital", "electronic", "techno", "hacker", "crypto", "turbo", "mechanical", "servo"),
            "sole`adj`",
            makeList("sole", "true", "singular", "total", "ultimate", "final", "last"),
            "light`adj`",
            makeList("bright", "glowing", "solar", "stellar", "lunar", "radiant", "luminous", "shimmering", "gleaming"),
            "light`noun`",
            makeList("light", "glow", "sun", "star", "moon", "radiance", "dawn", "torch", "shimmer", "gleam"),
            "light`nouns`",
            makeList("lights", "glimmers", "suns", "stars", "moons", "torches"),
            "shadow`noun`",
            makeList("shadow", "darkness", "gloom", "blackness", "murk", "twilight"),
            "shadow`nouns`",
            makeList("shadows", "darkness", "gloom", "blackness", "murk", "twilight"),
            "fire`noun`",
            makeList("fire", "flame", "inferno", "conflagration", "pyre", "blaze"),
            "fire`nouns`",
            makeList("fires", "flames", "infernos", "conflagrations", "pyres", "blazes"),
            "ice`noun`",
            makeList("ice", "frost", "snow", "chill", "blizzard", "cold"),
            "ice`nouns`",
            makeList("ice", "frosts", "snow", "chills", "blizzards", "cold"),
            "lightning`noun`",
            makeList("lightning", "thunder", "thunderbolt", "storm", "spark", "shock"),
            "lightning`nouns`",
            makeList("lightning", "thunder", "thunderbolts", "storms", "sparks", "shocks"),
            "smart`adj`",
            makeList("brilliant", "smart", "genius", "wise", "clever", "cunning", "mindful", "aware"),
            "smart`noun`",
            makeList("genius", "wisdom", "cunning", "awareness", "mindfulness", "acumen", "smarts", "knowledge"),
            "stupid`adj`",
            makeList("stupid", "dumb", "idiotic", "foolish", "reckless", "careless", "sloppy", "dull", "moronic", "complacent"),
            "stupid`noun`",
            makeList("stupidity", "idiocy", "foolishness", "recklessness", "carelessness", "sloppiness", "complacency"),
            "bandit`noun`",
            makeList("thief", "raider", "bandit", "rogue", "brigand", "highwayman", "pirate"),
            "bandit`nouns`",
            makeList("thieves", "raiders", "bandits", "rogues", "brigands", "highwaymen", "pirates"),
            "soldier`noun`",
            makeList("soldier", "warrior", "fighter", "mercenary", "trooper", "combatant"),
            "soldier`nouns`",
            makeList("soldiers", "warriors", "fighters", "mercenaries", "troops", "combatants"),
            "guard`noun`",
            makeList("protector", "guardian", "warden", "defender", "guard", "shield", "sentinel", "watchman", "knight", "paladin", "templar"),
            "guard`nouns`",
            makeList("protectors", "guardians", "wardens", "defenders", "guards", "shields", "sentinels", "watchmen", "knights", "paladins", "templars"),
            "hunter`noun`",
            makeList("hunter", "poacher", "trapper", "warden", "stalker", "tracker"),
            "explorer`noun`",
            makeList("explorer", "pathfinder", "seeker", "questant", "wanderer", "nomad"),
            "hunter`nouns`",
            makeList("hunters", "poachers", "trappers", "wardens", "stalkers", "trackers"),
            "explorer`nouns`",
            makeList("explorers", "pathfinders", "seekers", "questants", "wanderers", "nomads"),
            "rage`noun`",
            makeList("rage", "fury", "anger", "wrath", "frenzy", "vengeance"),
            "ominous`adj`",
            makeList("ominous", "foreboding", "fateful", "baleful", "portentous"),
            "many`adj`",
            makeList("many", "myriad", "thousandfold", "infinite", "countless", "unlimited"),
            "impossible`adj`",
            makeList("impossible", "forbidden", "incomprehensible", "ineffable", "unearthly", "abominable", "unspeakable", "indescribable"),
            "gaze`noun`",
            makeList("eye", "gaze", "stare", "observation", "purveyance", "watch"),
            "pain`noun`",
            makeList("pain", "agony", "misery", "excruciation", "torture"),
            "god`noun`",
            makeList("god", "deity", "ruler", "king", "father", "lord", "lordship"),
            "goddess`noun`",
            makeList("goddess", "deity", "ruler", "queen", "mother", "lady", "ladyship"),
            "hero`noun`",
            makeList("hero", "champion", "savior", "crusader", "knight"),
            "heroes`nouns`",
            makeList("heroes", "champions", "saviors", "crusaders", "knights"),
            "heroine`noun`",
            makeList("heroine", "champion", "savior", "crusader", "knight", "maiden"),
            "heroines`nouns`",
            makeList("heroines", "champions", "saviors", "crusaders", "knights", "maidens"),
            "popular`adj`",
            makeList("beloved", "adored", "revered", "worshipped"),
            "unpopular`adj`",
            makeList("reviled", "despised", "hated", "loathed")
            );
    public static final OrderedMap<String, ArrayList<String>>
            adjective = new OrderedMap<>(categories),
            noun = new OrderedMap<>(categories),
            nouns = new OrderedMap<>(categories);
    public static final OrderedMap<CharSequence, FakeLanguageGen> languages = new OrderedMap<CharSequence, FakeLanguageGen>(
            100, Hashers.caseInsensitiveStringHasher
    ).putPairs(
            "lc`gen`",
            FakeLanguageGen.LOVECRAFT,
            "jp`gen`",
            FakeLanguageGen.JAPANESE_ROMANIZED,
            "fr`gen`",
            FakeLanguageGen.FRENCH,
            "gr`gen`",
            FakeLanguageGen.GREEK_ROMANIZED,
            "ru`gen`",
            FakeLanguageGen.RUSSIAN_ROMANIZED,
            "sw`gen`",
            FakeLanguageGen.SWAHILI,
            "so`gen`",
            FakeLanguageGen.SOMALI,
            "en`gen`",
            FakeLanguageGen.ENGLISH,
            "fn`gen`",
            FakeLanguageGen.FANTASY_NAME,
            "fn`acc`gen`",
            FakeLanguageGen.FANCY_FANTASY_NAME,
            "ar`gen`",
            FakeLanguageGen.ARABIC_ROMANIZED,
            "hi`gen`",
            FakeLanguageGen.HINDI_ROMANIZED,
            "in`gen`",
            FakeLanguageGen.INUKTITUT,
            "nr`acc`gen`",
            FakeLanguageGen.NORSE,
            "nr`gen`",
            FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE),
            "na`gen`",
            FakeLanguageGen.NAHUATL,
            "mn`gen`",
            FakeLanguageGen.MONGOLIAN,
            "kr`gen`",
            FakeLanguageGen.KOREAN_ROMANIZED,
            "si`gen`",
            FakeLanguageGen.SIMPLISH,
            "el`gen`",
            FakeLanguageGen.ELF,
            "gb`gen`",
            FakeLanguageGen.GOBLIN,
            "if`gen`",
            FakeLanguageGen.INFERNAL,
            "dm`gen`",
            FakeLanguageGen.DEMONIC,
            "al`a`gen`",
            FakeLanguageGen.ALIEN_A,
            "al`e`gen`",
            FakeLanguageGen.ALIEN_E,
            "al`i`gen`",
            FakeLanguageGen.ALIEN_I,
            "al`o`gen`",
            FakeLanguageGen.ALIEN_O,
            "al`u`gen`",
            FakeLanguageGen.ALIEN_U,
            "ru`so`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.RUSSIAN_ROMANIZED, 3, FakeLanguageGen.SOMALI, 2),
            "gr`hi`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.GREEK_ROMANIZED, 3, FakeLanguageGen.HINDI_ROMANIZED, 2),
            "sw`fr`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.SWAHILI, 3, FakeLanguageGen.FRENCH, 2),
            "ar`jp`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.ARABIC_ROMANIZED, 3, FakeLanguageGen.JAPANESE_ROMANIZED, 2),
            "sw`gr`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.SWAHILI, 3, FakeLanguageGen.GREEK_ROMANIZED, 2),
            "gr`so`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.GREEK_ROMANIZED, 3, FakeLanguageGen.SOMALI, 2),
            "en`hi`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.ENGLISH, 3, FakeLanguageGen.HINDI_ROMANIZED, 2),
            "en`jp`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.ENGLISH, 3, FakeLanguageGen.JAPANESE_ROMANIZED, 2),
            "so`hi`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.SOMALI, 3, FakeLanguageGen.HINDI_ROMANIZED, 2),
            "fr`mod`gen`",
            FakeLanguageGen.FRENCH.addModifiers(FakeLanguageGen.modifier("([^aeiou])\\1", "$1ph", 0.3),
                    FakeLanguageGen.modifier("([^aeiou])\\1", "$1ch", 0.4),
                    FakeLanguageGen.modifier("([^aeiou])\\1", "$1sh", 0.5),
                    FakeLanguageGen.modifier("([^aeiou])\\1", "$1", 0.9)),
            "jp`mod`gen`",
            FakeLanguageGen.JAPANESE_ROMANIZED.addModifiers(FakeLanguageGen.Modifier.DOUBLE_VOWELS),
            "so`mod`gen`",
            FakeLanguageGen.SOMALI.addModifiers(FakeLanguageGen.modifier("([kd])h", "$1"),
                    FakeLanguageGen.modifier("([pfsgkcb])([aeiouy])", "$1l$2", 0.35),
                    FakeLanguageGen.modifier("ii", "ai"),
                    FakeLanguageGen.modifier("uu", "ia"),
                    FakeLanguageGen.modifier("([aeo])\\1", "$1"),
                    FakeLanguageGen.modifier("^x", "v"),
                    FakeLanguageGen.modifier("([^aeiou]|^)u([^aeiou]|$)", "$1a$2", 0.6),
                    FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1v$2", 0.06),
                    FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1l$2", 0.07),
                    FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1n$2", 0.07),
                    FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1z$2", 0.08),
                    FakeLanguageGen.modifier("([^aeiou])[aeiou]+$", "$1ia", 0.35),
                    FakeLanguageGen.modifier("([^aeiou])[bpdtkgj]", "$1"),
                    FakeLanguageGen.modifier("[jg]$", "th"),
                    FakeLanguageGen.modifier("g", "c", 0.92),
                    FakeLanguageGen.modifier("([aeiou])[wy]$", "$1l", 0.6),
                    FakeLanguageGen.modifier("([aeiou])[wy]$", "$1n"),
                    FakeLanguageGen.modifier("[qf]$", "l", 0.4),
                    FakeLanguageGen.modifier("[qf]$", "n", 0.65),
                    FakeLanguageGen.modifier("[qf]$", "s"),
                    FakeLanguageGen.modifier("cy", "sp"),
                    FakeLanguageGen.modifier("kl", "sk"),
                    FakeLanguageGen.modifier("qu+", "qui"),
                    FakeLanguageGen.modifier("q([^u])", "qu$1"),
                    FakeLanguageGen.modifier("cc", "ch"),
                    FakeLanguageGen.modifier("[^aeiou]([^aeiou][^aeiou])", "$1"),
                    FakeLanguageGen.Modifier.NO_DOUBLES),
            "ru`gr`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.RUSSIAN_ROMANIZED, 3, FakeLanguageGen.GREEK_ROMANIZED, 2),
            "lc`gr`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.LOVECRAFT, 3, FakeLanguageGen.GREEK_ROMANIZED, 2),
            "in`so`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.INUKTITUT, 3, FakeLanguageGen.SOMALI, 2),
            "ar`in`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.ARABIC_ROMANIZED, 3, FakeLanguageGen.INUKTITUT, 2),
            "na`lc`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.NAHUATL, 3, FakeLanguageGen.LOVECRAFT, 2),
            "na`sw`gen`",
            FakeLanguageGen.mixAll(FakeLanguageGen.NAHUATL, 3, FakeLanguageGen.SWAHILI, 2),
            "nr`ru`gen`",
            FakeLanguageGen.mixAll(
                    FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE), 3,
                    FakeLanguageGen.RUSSIAN_ROMANIZED, 2),
            "nr`in`gen`",
            FakeLanguageGen.mixAll(
                    FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE), 3,
                    FakeLanguageGen.INUKTITUT, 2),
            "mn`so`gen`",
            FakeLanguageGen.mixAll(
                    FakeLanguageGen.MONGOLIAN, 3,
                    FakeLanguageGen.SOMALI, 2),
            "dm`gr`gen`",
            FakeLanguageGen.mixAll(
                    FakeLanguageGen.DEMONIC, 3,
                    FakeLanguageGen.GREEK_ROMANIZED, 2),
            "if`na`gen`",
            FakeLanguageGen.mixAll(
                    FakeLanguageGen.INFERNAL, 3,
                    FakeLanguageGen.NAHUATL, 2
            ),
            "el`in`gen`",
            FakeLanguageGen.mixAll(
                    FakeLanguageGen.ELF, 3,
                    FakeLanguageGen.INUKTITUT, 2
            )
    );

    /**
     * Thesaurus preset that changes all text to sound like this speaker: "Desaurus preset dat changez all text to sound
     * like dis speakah." You may be familiar with a certain sci-fi game that has orks that sound like this.
     */
    public static Thesaurus ORK = new Thesaurus("WAAAAAGH!");
    static {
        ORK.alterations.add(new FakeLanguageGen.Alteration("\\bth", "d"));
        ORK.alterations.add(new FakeLanguageGen.Alteration("th", "dd"));
        ORK.alterations.add(new FakeLanguageGen.Alteration("er\\b", "ah"));
        ORK.alterations.add(new FakeLanguageGen.Alteration("es\\b", "ez"));
        ORK.addReplacement("the", "da")
                .addReplacement("their", "deyr")
                .addReplacement("yes", "ya")
                .addReplacement("your", "youse")
                .addReplacement("yours", "youses")
                .addReplacement("going", "gon'")
                .addReplacement("and", "an'")
                .addReplacement("to", "*snort*")
                .addReplacement("rhythm", "riddim")
                .addReplacement("get", "git")
                .addReplacement("good", "gud");
        Iterator<String> it = adjective.keySet().iterator();
        while (it.hasNext()){
            if(!it.next().contains("`adj`"))
                it.remove();
        }
        it = noun.keySet().iterator();
        while (it.hasNext()){
            if(!it.next().contains("`noun`"))
                it.remove();
        }
        it = nouns.keySet().iterator();
        while (it.hasNext()){
            if(!it.next().contains("`nouns`"))
                it.remove();
        }


    }
}
