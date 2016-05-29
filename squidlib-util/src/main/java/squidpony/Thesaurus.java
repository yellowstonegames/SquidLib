package squidpony;

import regexodus.*;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.GapShuffler;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static squidpony.Maker.*;

/**
 * A text processing class that can swap out occurrences of words and replace them with their synonyms.
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class Thesaurus implements Serializable{
    private static final long serialVersionUID = 3387639905758074640L;
    protected static final Pattern wordMatch = Pattern.compile("([\\pL`]+)");
    public LinkedHashMap<String, GapShuffler<String>> mappings;
    protected StatefulRNG rng;

    /**
     * Constructs a new Thesaurus with an unseeded RNG used to shuffle word order.
     */
    public Thesaurus()
    {
        mappings = new LinkedHashMap<>(256);
        rng = new StatefulRNG();
    }

    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with the next long from the given RNG.
     * @param rng an RNG that will only be used to get one long (for seeding this class' RNG)
     */
    public Thesaurus(RNG rng)
    {
        mappings = new LinkedHashMap<>(256);
        this.rng = new StatefulRNG(rng.nextLong());
    }

    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a long for seeding this class' RNG
     */
    public Thesaurus(long shuffleSeed)
    {
        mappings = new LinkedHashMap<>(256);
        this.rng = new StatefulRNG(shuffleSeed);
    }


    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a String for seeding this class' RNG
     */
    public Thesaurus(String shuffleSeed)
    {
        mappings = new LinkedHashMap<>(256);
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
        rng.setState(CrossHash.hash64(synonyms));
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng);
        for(String syn : synonyms)
        {
            mappings.put(syn, shuffler);
        }
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
        rng.setState(CrossHash.hash64(synonyms));
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng);
        mappings.put(keyword, shuffler);
        return this;
    }

    /**
     * Adds several pre-made categories to this Thesaurus' known categories, but won't cause it to try to replace normal
     * words with synonyms (only categories, which contain backticks in the name). The keywords this currently knows,
     * and the words it will replace those keywords with, are:
     * <br>
     * <ul>
     *     <li>"calm`adj`": harmonious, peaceful, pleasant, serene, placid, tranquil, calm</li>
     *     <li>"calm`noun`": harmony, peace, kindness, serenity, tranquility, calmn</li>
     *     <li>"org`noun`": fraternity, brotherhood, order, group, foundation</li>
     *     <li>"org`nouns`": fraternities, brotherhoods, orders, groups, foundations</li>
     *     <li>"empire`adj`": imperial, princely, kingly, regal, dominant, dynastic, royal, hegemonic, monarchic, ascendant</li>
     *     <li>"empire`noun`": empire, emirate, kingdom, sultanate, dominion, dynasty, imperium, hegemony, triumvirate, ascendancy</li>
     *     <li>"empire`nouns`": empires, emirates, kingdoms, sultanates, dominions, dynasties, imperia, hegemonies, triumvirates, ascendancies</li>
     *     <li>"duke`noun`": duke, earl, baron, fief, lord, shogun</li>
     *     <li>"duke`nouns`": dukes, earls, barons, fiefs, lords, shoguns</li>
     *     <li>"duchy`noun`": duchy, earldom, barony, fiefdom, lordship, shogunate</li>
     *     <li>"duchy`nouns`": duchies, earldoms, baronies, fiefdoms, lordships, shogunates</li>
     *     <li>"magical`adj`": arcane, enchanted, sorcerous, ensorcelled, magical, mystical</li>
     *     <li>"holy`adj`": auspicious, divine, holy, sacred, prophetic, blessed, godly</li>
     *     <li>"unholy`adj`": bewitched, occult, unholy, macabre, accursed, foul, vile</li>
     *     <li>"forest`adj`": natural, primal, verdant, lush, fertile, bountiful</li>
     *     <li>"forest`noun`": nature, forest, greenery, jungle, woodland, grove, copse</li>
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
     *     <li>"ar`acc`gen`": Imitation Arabic (worse); has special accents and uses two Greek letters as well</li>
     *     <li>"hi`gen`": Imitation Hindi (romanized and with accents removed)</li>
     *     <li>"fn`gen`": Fantasy Names; styled after the possibly-Europe-like names common in fantasy books</li>
     *     <li>"fn`acc`gen`": Fancy Fantasy Names; the same as "fn`gen`", but with lots of accented chars</li>
     *     <li>"lc`gen`": Lovecraft; styled after the names of creatures from H.P. Lovecraft's Cthulhu Mythos</li>
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
        for(Map.Entry<String, ArrayList<String>> kv : languages.entrySet())
        {
            addCategory(kv.getKey(), kv.getValue());
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
        String word2 = word.toLowerCase();
        if(mappings.containsKey(word2))
        {
            String nx = mappings.get(word2).getNext();
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

    public static LinkedHashMap<String, ArrayList<String>> categories = makeLHM(
            "calm`adj`",
            makeList("harmonious", "peaceful", "pleasant", "serene", "placid", "tranquil", "calm"),
            "calm`noun`",
            makeList("harmony", "peace", "kindness", "serenity", "tranquility", "calm"),
            "org`noun`",
            makeList("fraternity", "brotherhood", "order", "group", "foundation", "association", "guild", "fellowship", "partnership"),
            "org`nouns`",
            makeList("fraternities", "brotherhoods", "orders", "groups", "foundations", "associations", "guilds", "fellowships", "partnerships"),
            "empire`adj`",
            makeList("imperial", "princely", "kingly", "regal", "dominant", "dynastic", "royal", "hegemonic", "monarchic", "ascendant"),
            "empire`noun`",
            makeList("empire", "emirate", "kingdom", "sultanate", "dominion", "dynasty", "imperium", "hegemony", "triumvirate", "ascendancy"),
            "empire`nouns`",
            makeList("empires", "emirates", "kingdoms", "sultanates", "dominions", "dynasties", "imperia", "hegemonies", "triumvirates", "ascendancies"),
            "union`noun`",
            makeList("union", "alliance", "coalition", "confederation", "federation", "congress", "confederacy", "league", "faction"),
            "union`nouns`",
            makeList("unions", "alliances", "coalitions", "confederations", "federations", "congresses", "confederacies", "leagues", "factions"),
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
            makeList("auspicious", "divine", "holy", "sacred", "prophetic", "blessed", "godly"),
            "unholy`adj`",
            makeList("bewitched", "occult", "unholy", "macabre", "accursed", "profane", "vile"),
            "forest`adj`",
            makeList("natural", "primal", "verdant", "lush", "fertile", "bountiful"),
            "forest`noun`",
            makeList("nature", "forest", "greenery", "jungle", "woodland", "grove", "copse"),
            "fancy`adj`",
            makeList("grand", "glorious", "magnificent", "magnanimous", "majestic", "great", "powerful"),
            "evil`adj`",
            makeList("heinous", "scurrilous", "terrible", "horrible", "debased", "wicked", "evil", "malevolent", "nefarious", "vile"),
            "good`adj`",
            makeList("righteous", "moral", "good", "pure", "compassionate", "flawless", "perfect"),
            "sinister`adj`",
            makeList("shadowy", "silent", "lethal", "deadly", "fatal", "venomous", "cutthroat", "murderous", "bloodstained"),
            "sinister`noun`",
            makeList("shadow", "silence", "assassin", "ninja", "venom", "poison", "snake", "murder", "blood", "razor"),
            "blade`noun`",
            makeList("blade", "knife", "sword", "axe", "stiletto", "katana", "scimitar", "hatchet", "spear", "glaive", "halberd",
                    "hammer", "maul", "flail", "mace", "sickle", "scythe", "whip", "lance", "nunchaku", "saber", "cutlass", "trident"),
            "bow`noun`",
            makeList("bow", "longbow", "shortbow", "crossbow", "sling", "atlatl", "bolas", "javelin", "net", "shuriken", "dagger"),
            "weapon`noun`",
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
            "tech`adj`",
            makeList("cyber", "digital", "electronic", "techno", "hacker", "crypto", "turbo", "mechanical", "servo"),
            "sole`adj`",
            makeList("sole", "true", "singular", "total", "ultimate", "final"),
            "light`adj`",
            makeList("bright", "glowing", "solar", "stellar", "lunar", "radiant", "luminous", "shimmering"),
            "light`noun`",
            makeList("light", "glow", "sun", "star", "moon", "radiance", "dawn", "torch"),
            "light`nouns`",
            makeList("lights", "glimmers", "suns", "stars", "moons", "torches"),
            "smart`adj`",
            makeList("brilliant", "smart", "genius", "wise", "clever", "cunning", "mindful", "aware"),
            "smart`noun`",
            makeList("genius", "wisdom", "cunning", "awareness", "mindfulness", "acumen", "smarts", "knowledge"),
            "bandit`noun`",
            makeList("thief", "raider", "bandit", "rogue", "brigand", "highwayman", "pirate"),
            "bandit`nouns`",
            makeList("thieves", "raiders", "bandits", "rogues", "brigands", "highwaymen", "pirates"),
            "guard`noun`",
            makeList("protector", "guardian", "warden", "defender", "guard", "shield", "sentinel", "watchman", "knight"),
            "guard`nouns`",
            makeList("protectors", "guardians", "wardens", "defenders", "guards", "shields", "sentinels", "watchmen", "knights"),
            "rage`noun`",
            makeList("rage", "fury", "anger", "wrath", "frenzy", "vengeance")
            ),
            languages = makeLHM(
            "lc`gen`",
            makeList("lahatos", "iatsiltak", "hmimrekaarl", "yixaltaishk", "cthupaxa", "zvroggamraa", "ixaakran"),
            "jp`gen`",
            makeList("naimoken", "kishigu", "houdaibo", "souchaya", "aijake", "hyazuran", "pajokke", "sokkimou"),
            "fr`gen`",
            makeList("devive", "esiggoi", "afaddouille", "roiquide", "obaploui", "baîmefi", "vêggrôste", "blaçeglè", "bamissecois"),
            "gr`gen`",
            makeList("lemabus", "ithonxeum", "etoneoch", "eirkuirstes", "taunonkos", "krailozes", "amarstei", "psorsteomium"),
            "ru`gen`",
            makeList("belyvia", "tiuzhiskit", "dazyved", "dabrisazetsky", "shaskianyr", "goskabad", "deblieskib", "neskagre"),
            "sw`gen`",
            makeList("mzabandu", "nzaloi", "tamzamda", "anzibo", "jamsala", "latazi", "faazaza", "uzoge", "mbomuta", "nbasonga"),
            "so`gen`",
            makeList("daggidda", "xabuumaq", "naadhana", "goquusad", "baxiltuu", "qooddaddut", "mosumyuuc", "uggular", "jaabacyut"),
            "en`gen`",
            makeList("thabbackion", "joongipper", "urbigsus", "otsaffet", "pittesely", "ramesist", "elgimmac", "genosont", "bessented"),
            "fn`gen`",
            makeList("kemosso", "venzendo", "tybangue", "evendi", "ringamye", "drayusta", "acleutos", "nenizo", "ifelle", "rytoudo"),
            "fn`acc`gen`",
            makeList("tánzeku", "nìāfőshi", "ñoffêfès", "áfŏmu", "drĕstishű", "pyeryĕquı", "bėdĕbǽ", "nęìjônne", "mainűthî"),
            "ar`acc`gen`",
            makeList("azawiq", "al-ahaluq", "isabzīz", "zūrżuhikari", "īrālať", "ījīqab", "qizifih", "ibn-āħūkū", "šulilfas"),
            "ar`gen`",
            makeList("iibaatuu", "wiilnalza", "ulanzha", "jaliifa", "iqaddiz", "waatufaa", "lizhuqa", "qinzaamju", "zuzuri"),
            "hi`gen`",
            makeList("maghubadhit", "bhunasu", "ipruja", "dhuevasam", "nubudho", "ghasaibi", "virjorghu", "khlindairai", "irsinam"),
            "ru`so`gen`",
            makeList("tserokyb", "zhieziufoj", "bisaskug", "nuriesyv", "gybared", "bableqa", "pybadis", "wiuskoglif", "zakalieb"),
            "gr`hi`gen`",
            makeList("takhada", "bepsegos", "ovukhrim", "sinupiam", "nabogon", "umianum", "dhainukotron", "muisaithi", "aerpraidha"),
            "sw`fr`gen`",
            makeList("nchaleûja", "soëhusi", "nsavarço", "fambofai", "namyàmse", "mfonsapa", "zalasha", "hiplaîpu", "hœumyemza"),
            "ar`jp`gen`",
            makeList("jukkaizhi", "hibiikkiiz", "shomela", "qhabohuz", "isiikya", "akkirzuh", "jalukhmih", "uujajon", "ryaataibna"),
            "sw`gr`gen`",
            makeList("ozuxii", "muguino", "nauteicha", "mjixazi", "yataya", "pomboirki", "achuiga", "maleibe", "psizeso", "njameichim"),
            "gr`so`gen`",
            makeList("xaaxoteum", "basaalii", "azaibe", "oupeddom", "pseiqigioh", "garkame", "uddoulis", "jobegos", "eqisol"),
            "en`hi`gen`",
            makeList("promolchi", "dhontriso", "gobhamblom", "hombangot", "sutsidalm", "dhindhinaur", "megsesa", "skaghinma", "thacebha"),
            "en`jp`gen`",
            makeList("nyintazu", "haxinsen", "kedezorp", "angapec", "donesalk", "ranepurgy", "laldimyi", "ipprijain", "bizinni"),
            "so`hi`gen`",
            makeList("yiteevadh", "omithid", "qugadhit", "nujagi", "nidogish", "danurbha", "sarojik", "cigafo", "tavodduu", "huqoyint"),
            "fr`mod`gen`",
            makeList("egleidô", "glaiemegchragne", "veçebun", "aubudaî", "peirquembrut", "eglecque", "marçoimeaux", "jêmbrégshre"),
            "jp`mod`gen`",
            makeList("dotobyu", "nikinaan", "gimoummee", "aanzaro", "ryasheeso", "aizaizo", "nyaikkashaa", "kitaani", "maabyopai"),
            "so`mod`gen`",
            makeList("sanata", "ájisha", "soreeggár", "quágeleu", "abaxé", "tedora", "bloxajac", "tiblarxo", "oodagí", "jélebi"),
            "ru`gr`gen`",
            makeList("zydievov", "pyplerta", "gaupythian", "kaustybre", "larkygagda", "metuskiev", "vuvidzhian", "ykadzhodna", "paziutso"),
            "lc`gr`gen`",
            makeList("fesiagroigor", "gledzhiggiakh", "saghiathask", "sheglerliv", "hmepobor", "riagarosk", "kramrufot", "glonuskiub"));
}
