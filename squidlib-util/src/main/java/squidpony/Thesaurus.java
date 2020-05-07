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
    protected static final Pattern wordMatch = Pattern.compile("([\\pL`]+|@)"),
            similarFinder = Pattern.compile(".*?\\b(\\w\\w\\w\\w).*?{\\@1}.*$", "ui");
    public OrderedMap<CharSequence, GapShuffler<String>> mappings;
    public ArrayList<FakeLanguageGen.Alteration> alterations = new ArrayList<>(4);
    public SilkRNG rng;
    protected GapShuffler<String> plantTermShuffler, fruitTermShuffler, nutTermShuffler, flowerTermShuffler,
            potionTermShuffler;
    public FakeLanguageGen defaultLanguage = FakeLanguageGen.SIMPLISH;
    public transient ArrayList<FakeLanguageGen> randomLanguages = new ArrayList<>(2);
    public transient String latestGenerated = "Nationia";
    /**
     * Constructs a new Thesaurus with an unseeded RNG used to shuffle word order.
     */
    public Thesaurus()
    {
        mappings = new OrderedMap<>(256, Hashers.caseInsensitiveStringHasher);
        rng = new SilkRNG();
        addKnownCategories();
    }

    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with the next long from the given RNG.
     * @param rng an RNG that will only be used to get one long (for seeding this class' RNG)
     */
    public Thesaurus(IRNG rng)
    {
        this(rng.nextLong());
    }

    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a long for seeding this class' RNG
     */
    public Thesaurus(long shuffleSeed)
    {
        mappings = new OrderedMap<>(256, Hashers.caseInsensitiveStringHasher);
        this.rng = new SilkRNG(shuffleSeed);
        addKnownCategories();
    }


    /**
     * Constructs a new Thesaurus, seeding its RNG (used to shuffle word order) with shuffleSeed.
     * @param shuffleSeed a String for seeding this class' RNG
     */
    public Thesaurus(String shuffleSeed)
    {
        this(CrossHash.hash64(shuffleSeed));
    }

    /**
     * Changes the sequences for all groups of synonyms this can produce, effectively turning this Thesaurus into a
     * different version that knows all the same synonyms and categories but will produce different results.
     * @param state any long; will be given to {@link SilkRNG#setState(long)}
     */
    public void refresh(long state)
    {
        refresh((int)(state & 0xFFFFFFFFL), (int)(state >>> 32));
    }

    /**
     * Changes the sequences for all groups of synonyms this can produce, effectively turning this Thesaurus into a
     * different version that knows all the same synonyms and categories but will produce different results.
     * @param stateA any int; the first part of the state of a {@link SilkRNG}
     * @param stateB any int; the second part of the state of a {@link SilkRNG}
     */
    public void refresh(int stateA, int stateB)
    {
        this.rng.setState(stateA, stateB);
        final int sz = mappings.size();
        for (int i = 0; i < sz; i++) {
            mappings.getAt(i).setRNG(rng, true);
        }
    }

    public Thesaurus addReplacement(CharSequence before, String after)
    {
        mappings.put(before, new GapShuffler<>(after));
        return this;
    }

    /**
     * Allows this Thesaurus to replace a specific keyword, typically containing multiple backtick characters
     * ({@code `}) so it can't be confused with a "real word," with one of the words in synonyms (chosen in shuffled
     * order). The backtick is the only punctuation character that this class' word matcher considers part of a word,
     * both for this reason and because it is rarely used in English text.
     * @param keyword a word (typically containing backticks, {@code `}) that will be replaced by a word from synonyms
     * @param synonyms a Collection of lower-case Strings with similar meaning and the same part of speech
     * @return this for chaining
     */
    public Thesaurus addCategory(String keyword, Collection<String> synonyms)
    {
        if(synonyms.isEmpty())
            return this;
        GapShuffler<String> shuffler = new GapShuffler<>(synonyms, rng, true);
        mappings.put(keyword, shuffler);
        return this;
    }

    /**
     * Adds several pre-made categories to this Thesaurus' known categories, but won't cause it to try to replace normal
     * words with synonyms (only categories, which contain backticks in the name). The keywords this currently knows,
     * and the words it will replace those keywords with, are:
     * <br>
     * <ul>
     *     <li>calm`adj` : calm, harmonious, peaceful, placid, pleasant, serene, tranquil</li>
     *     <li>calm`noun` : calm, harmony, kindness, peace, serenity, tranquility</li>
     *     <li>org`noun` : association, brotherhood, fellowship, foundation, fraternity, group, guild, order, partnership</li>
     *     <li>org`nouns` : associations, brotherhoods, fellowships, foundations, fraternities, groups, guilds, orders, partnerships</li>
     *     <li>empire`adj` : ascendant, dynastic, emir's, hegemonic, imperial, king's, lordly, monarchic, prince's, regal, royal, sultan's</li>
     *     <li>empire`noun` : ascendancy, commonwealth, dominion, dynasty, emirate, empire, hegemony, imperium, kingdom, monarchy, sultanate, triumvirate</li>
     *     <li>empire`nouns` : ascendancies, commonwealths, dominions, dynasties, emirates, empires, hegemonies, imperia, kingdoms, monarchies, sultanates, triumvirates</li>
     *     <li>emperor`noun` : emir, emperor, king, lord, pharaoh, ruler, sultan</li>
     *     <li>emperor`nouns` : emirs, emperors, kings, lords, pharaohs, rulers, sultans</li>
     *     <li>empress`noun` : emira, empress, lady, pharaoh, queen, ruler, sultana</li>
     *     <li>empress`nouns` : emiras, empresses, ladies, pharaohs, queens, rulers, sultanas</li>
     *     <li>union`adj` : allied, associated, confederated, congressional, democratic, federated, independent, people's, unified, united</li>
     *     <li>union`noun` : alliance, coalition, confederacy, confederation, congress, faction, federation, league, republic, union</li>
     *     <li>union`nouns` : alliances, coalitions, confederacies, confederations, congresses, factions, federations, leagues, republics, unions</li>
     *     <li>militia`noun` : fighters, front, irregulars, liberators, militants, militia, rebellion, resistance, warriors</li>
     *     <li>militia`nouns` : fighters, fronts, irregulars, liberators, militants, militias, rebellions, resistances, warriors</li>
     *     <li>gang`noun` : cartel, crew, gang, mafia, mob, posse, syndicate</li>
     *     <li>gang`nouns` : cartels, crews, gangs, mafias, mobs, posses, syndicates</li>
     *     <li>duke`noun` : baron, duke, earl, fief, lord, shogun</li>
     *     <li>duke`nouns` : barons, dukes, earls, fiefs, lords, shoguns</li>
     *     <li>duchy`noun` : barony, duchy, earldom, fiefdom, lordship, shogunate</li>
     *     <li>duchy`nouns` : baronies, duchies, earldoms, fiefdoms, lordships, shogunates</li>
     *     <li>magical`adj` : arcane, enchanted, ensorcelled, magical, mystical, sorcerous</li>
     *     <li>holy`adj` : auspicious, blessed, divine, godly, holy, prophetic, sacred, virtuous</li>
     *     <li>priest`noun` : bishop, cardinal, chaplain, cleric, preacher, priest</li>
     *     <li>priest`nouns` : bishops, cardinals, chaplains, clergy, preachers, priests</li>
     *     <li>unholy`adj` : accursed, bewitched, macabre, occult, profane, unholy, vile</li>
     *     <li>witch`noun` : cultist, defiler, necromancer, occultist, warlock, witch</li>
     *     <li>witch`nouns` : cultists, defilers, necromancers, occultists, warlocks, witches</li>
     *     <li>forest`adj` : bountiful, fertile, lush, natural, primal, verdant</li>
     *     <li>forest`noun` : copse, forest, glen, greenery, grove, jungle, nature, woodland</li>
     *     <li>shaman`noun` : animist, druid, shaman, warden</li>
     *     <li>shaman`nouns` : animists, druids, shamans, wardens</li>
     *     <li>fancy`adj` : glorious, grand, great, magnanimous, magnificent, majestic, powerful</li>
     *     <li>evil`adj` : abhorrent, cruel, debased, evil, heinous, horrible, malevolent, nefarious, scurrilous, terrible, vile, wicked</li>
     *     <li>villain`noun` : blasphemer, evildoer, killer, knave, monster, murderer, villain</li>
     *     <li>villain`nouns` : blasphemers, evildoers, killers, knaves, monsters, murderers, villains</li>
     *     <li>monster`noun` : abomination, beast, creature, demon, devil, fiend, ghoul, monster</li>
     *     <li>monsters`nouns` : abominations, beasts, creatures, demons, devils, fiends, ghouls, monsters</li>
     *     <li>good`adj` : compassionate, flawless, good, kind, moral, perfect, pure, righteous</li>
     *     <li>lethal`adj` : bloodstained, cutthroat, deadly, fatal, lethal, murderous, poisonous, silent, stalking, venomous</li>
     *     <li>lethal`noun` : assassin, blood, killer, murder, ninja, poison, razor, silence, slayer, snake, tiger, venom</li>
     *     <li>blade`noun` : axe, blade, cutlass, flail, glaive, halberd, hammer, hatchet, katana, knife, lance, mace, maul, nunchaku, saber, scimitar, scythe, sickle, spear, stiletto, sword, trident, whip</li>
     *     <li>bow`noun` : atlatl, bolas, bow, crossbow, dagger, javelin, longbow, net, shortbow, shuriken, sling</li>
     *     <li>weapon`noun` : atlatl, axe, blade, bolas, bow, crossbow, cutlass, dagger, flail, glaive, halberd, hammer, hatchet, javelin, katana, knife, lance, longbow, mace, maul, net, nunchaku, saber, scimitar, scythe, shortbow, shuriken, sickle, sling, spear, stiletto, sword, trident, whip</li>
     *     <li>musket`noun` : arquebus, blunderbuss, cannon, flintlock, matchlock, musket, wheellock</li>
     *     <li>grenade`noun` : bomb, explosive, flamethrower, grenade, missile, rocket, warhead</li>
     *     <li>rifle`noun` : firearm, handgun, longarm, pistol, rifle, shotgun</li>
     *     <li>blade`nouns` : axes, blades, cutlasses, flails, glaives, halberds, hammers, hatchets, katana, knives, lances, maces, mauls, nunchaku, sabers, scimitars, scythes, sickles, spears, stilettos, swords, tridents, whips</li>
     *     <li>bow`nouns` : atlatls, bolases, bows, crossbows, daggers, javelins, longbows, nets, shortbows, shuriken, slings</li>
     *     <li>weapon`nouns` : atlatls, axes, blades, bolases, bows, crossbows, cutlasses, daggers, flails, glaives, halberds, hammers, hatchets, javelins, katana, knives, lances, longbows, maces, mauls, nets, nunchaku, sabers, scimitars, scythes, shortbows, shuriken, sickles, slings, spears, stilettos, swords, tridents, whips</li>
     *     <li>musket`nouns` : arquebusses, blunderbusses, cannons, flintlocks, matchlocks, muskets, wheellocks</li>
     *     <li>grenade`nouns` : bombs, explosives, flamethrowers, grenades, missiles, rockets, warheads</li>
     *     <li>rifle`nouns` : firearms, handguns, longarms, pistols, rifles, shotguns</li>
     *     <li>scifi`adj` : genetic, gravitational, laser, nanoscale, phase, photonic, plasma, quantum, tachyonic, warp</li>
     *     <li>tech`adj` : crypto, cyber, digital, electronic, hacker, mechanical, servo, techno, turbo</li>
     *     <li>sole`adj` : final, last, singular, sole, total, true, ultimate</li>
     *     <li>light`adj` : bright, gleaming, glowing, luminous, lunar, radiant, shimmering, solar, stellar</li>
     *     <li>light`noun` : dawn, gleam, glow, light, moon, radiance, shimmer, star, sun, torch</li>
     *     <li>light`nouns` : glimmers, lights, moons, stars, suns, torches</li>
     *     <li>shadow`noun` : blackness, darkness, gloom, murk, shadow, twilight</li>
     *     <li>shadow`nouns` : blackness, darkness, gloom, murk, shadows, twilight</li>
     *     <li>fire`noun` : blaze, conflagration, fire, flame, inferno, pyre</li>
     *     <li>fire`nouns` : blazes, conflagrations, fires, flames, infernos, pyres</li>
     *     <li>ice`noun` : blizzard, chill, cold, frost, ice, snow</li>
     *     <li>ice`nouns` : blizzards, chills, cold, frosts, ice, snow</li>
     *     <li>lightning`noun` : lightning, shock, spark, storm, thunder, thunderbolt</li>
     *     <li>lightning`nouns` : lightning, shocks, sparks, storms, thunder, thunderbolts</li>
     *     <li>ground`noun` : clay, dirt, earth, loam, mud, peat, sand, soil</li>
     *     <li>lake`noun` : bog, fen, glade, lake, pond, puddle, sea, swamp</li>
     *     <li>leaf`noun` : bark, blossom, branch, bud, cress, flower, leaf, root, sap, seed, shoot, stalk, stem, thorn, twig, vine, wood, wort</li>
     *     <li>fruit`noun` : apple, banana, berry, cherry, date, fig, fruit, grape, juniper, lime, mango, melon, papaya, peach, pear, quince</li>
     *     <li>nut`noun` : almond, bean, cashew, chestnut, hazelnut, nut, pea, peanut, pecan, walnut</li>
     *     <li>flower`noun` : amaryllis, camellia, chrysanthemum, daisy, dandelion, flower, gardenia, hibiscus, jasmine, lantana, lilac, lily, lotus, mallow, oleander, orchid, peony, petunia, phlox, rose, tulip</li>
     *     <li>tree`noun` : alder, beech, birch, cactus, cedar, elm, hazel, juniper, larch, magnolia, mangrove, maple, oak, palm, pine, tree, willow</li>
     *     <li>flavor`noun` : acid, grease, herb, salt, smoke, spice, sugar</li>
     *     <li>flavor`adj` : bitter, salty, savory, smoky, sour, spicy, sweet</li>
     *     <li>color`adj` : black, blue, brown, gray, green, orange, red, violet, white, yellow</li>
     *     <li>shape`adj` : delicate, drooping, fibrous, fragile, giant, hardy, hollow, long, miniature, spiny, stiff, stubby, sturdy, thorny, tufted, yielding</li>
     *     <li>sensory`adj` : aromatic, fragrant, fuzzy, glossy, pungent, rough, rustling, smooth, soft, weeping</li>
     *     <li>liquid`noun` : brew, broth, elixir, fluid, liquid, potion, serum, tonic</li>
     *     <li>liquid`adj` : bubbling, congealing, effervescent, milky, murky, slimy, sloshing, swirling, thick</li>
     *     <li>bottle`noun` : bottle, canister, flagon, flask, jug, phial, vial</li>
     *     <li>bottle`adj` : brown glass, clear glass, curvaceous glass, dull pewter, fluted crystal, green glass, rough-cut glass, sharp-edged tin, shining silver, smoky glass, tarnished silver</li>
     *     <li>calabash`adj` : calabash, hollow gourd, milk carton, waterskin, wineskin</li>
     *     <li>smart`adj` : aware, brilliant, clever, cunning, genius, mindful, smart, wise</li>
     *     <li>smart`noun` : acumen, awareness, cunning, genius, knowledge, mindfulness, smarts, wisdom</li>
     *     <li>stupid`adj` : careless, complacent, dull, dumb, foolish, idiotic, moronic, reckless, sloppy, stupid</li>
     *     <li>stupid`noun` : carelessness, complacency, foolishness, idiocy, recklessness, sloppiness, stupidity</li>
     *     <li>bandit`noun` : bandit, brigand, highwayman, pirate, raider, rogue, thief</li>
     *     <li>bandit`nouns` : bandits, brigands, highwaymen, pirates, raiders, rogues, thieves</li>
     *     <li>soldier`noun` : combatant, fighter, mercenary, soldier, trooper, warrior</li>
     *     <li>soldier`nouns` : combatants, fighters, mercenaries, soldiers, troops, warriors</li>
     *     <li>guard`noun` : defender, guard, guardian, knight, paladin, protector, sentinel, shield, templar, warden, watchman</li>
     *     <li>guard`nouns` : defenders, guardians, guards, knights, paladins, protectors, sentinels, shields, templars, wardens, watchmen</li>
     *     <li>hunter`noun` : hunter, poacher, stalker, tracker, trapper, warden</li>
     *     <li>explorer`noun` : explorer, nomad, pathfinder, questant, seeker, wanderer</li>
     *     <li>hunter`nouns` : hunters, poachers, stalkers, trackers, trappers, wardens</li>
     *     <li>explorer`nouns` : explorers, nomads, pathfinders, questants, seekers, wanderers</li>
     *     <li>rage`noun` : anger, frenzy, fury, rage, vengeance, wrath</li>
     *     <li>ominous`adj` : baleful, fateful, foreboding, ominous, portentous</li>
     *     <li>many`adj` : countless, infinite, manifold, many, myriad, thousandfold, unlimited</li>
     *     <li>impossible`adj` : abominable, forbidden, impossible, incomprehensible, indescribable, ineffable, unearthly, unspeakable</li>
     *     <li>gaze`noun` : eye, gaze, observation, purveyance, stare, watch</li>
     *     <li>pain`noun` : agony, excruciation, misery, pain, torture</li>
     *     <li>god`noun` : deity, father, god, king, lord, lordship, ruler</li>
     *     <li>goddess`noun` : deity, goddess, lady, ladyship, mother, queen, ruler</li>
     *     <li>hero`noun` : champion, crusader, hero, knight, savior</li>
     *     <li>heroes`nouns` : champions, crusaders, heroes, knights, saviors</li>
     *     <li>heroine`noun` : champion, crusader, heroine, knight, maiden, savior</li>
     *     <li>heroines`nouns` : champions, crusaders, heroines, knights, maidens, saviors</li>
     *     <li>popular`adj` : adored, beloved, revered, worshipped</li>
     *     <li>unpopular`adj` : despised, hated, loathed, reviled</li>
     *     <li>glyph`noun` : glyph, mark, seal, sigil, sign, symbol</li>
     *     <li>glyph`nouns` : glyphs, marks, seals, sigils, signs, symbols</li>
     *     <li>power`noun` : authority, dominance, force, potency, power, strength</li>
     *     <li>power`adj` : authoritative, dominant, forceful, potent, powerful, strong</li>
     * </ul>
     * There are also terms, which typically produce multiple words procedurally and may use {@link #defaultLanguage}.
     * See {@link #makePlantName()}, {@link #makeFruitName()}, {@link #makeNutName()}, {@link #makeFlowerName()}, and
     * {@link #makePotionDescription()} for more info and examples.
     * <li>
     *     <li>plant`term` : @'s color`adj`	leaf`noun`, @'s color`adj` flower`noun`, @'s color`adj` tree`noun`, @'s flower`noun`, @'s ground`noun`	leaf`noun`, @'s sensory`adj`-leaf`noun`, @'s shape`adj` flower`noun`, @'s tree`noun`, color`adj` flower`noun`, color`adj` flower`noun` of @, color`adj` fruit`noun` tree`noun`, color`adj` nut`noun` tree`noun`, color`adj`-leaf`noun` flower`noun`, flavor`adj` fruit`noun` tree`noun`, flavor`adj` nut`noun` tree`noun`, flavor`noun`	leaf`noun` tree`noun`, flower`noun` of @, ground`noun`	flower`noun`, ground`noun`	leaf`noun`, ground`noun`	leaf`noun` of @, leaf`noun` of @, sensory`adj` flower`noun` of @, sensory`adj` flower`noun`-flower`noun`, sensory`adj` tree`noun` of @, sensory`adj` tree`noun`-tree`noun`, sensory`adj`-leaf`noun` flower`noun`, sensory`adj`-leaf`noun` tree`noun`, shape`adj` flower`noun`, shape`adj`-fruit`noun` tree`noun`, shape`adj`-leaf`noun` flower`noun`, shape`adj`-leaf`noun` tree`noun`</li>
     *     <li>fruit`term` : @'s color`adj` fruit`noun`, @'s flavor`adj` fruit`noun`, @'s fruit`noun`, color`adj` fruit`noun`-fruit`noun`, flavor`adj` fruit`noun`-fruit`noun`, fruit`noun` of @</li>
     *     <li>nut`term` : @'s color`adj` nut`noun`, @'s flavor`adj` nut`noun`, @'s nut`noun`, color`adj` nut`noun`, color`adj` nut`noun` of @, flavor`adj` nut`noun`, nut`noun` of @, sensory`adj` nut`noun`</li>
     *     <li>flower`term` : @'s color`adj` flower`noun`, @'s flower`noun`, @'s shape`adj` flower`noun`, color`adj` flower`noun`, color`adj` flower`noun` of @, color`adj`-leaf`noun` flower`noun`, flower`noun` of @, ground`noun`	flower`noun`, sensory`adj` flower`noun` of @, sensory`adj` flower`noun`-flower`noun`, sensory`adj`-leaf`noun` flower`noun`, shape`adj` flower`noun`, shape`adj`-leaf`noun` flower`noun`</li>
     *     <li>potion`term` : a bottle`adj` bottle`noun` containing a few drops of a color`adj` liquid`noun`, a bottle`adj` bottle`noun` filled with a color`adj` liquid`noun`, a bottle`adj` bottle`noun` filled with a liquid`adj` color`adj` liquid`noun`, a bottle`adj` bottle`noun` half-filled with a liquid`adj` color`adj` liquid`noun`, a calabash`adj` filled with a color`adj` liquid`noun`</li>
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
        plantTermShuffler = new GapShuffler<>(plantTerms, rng, true);
        fruitTermShuffler = new GapShuffler<>(fruitTerms, rng, true);
        nutTermShuffler = new GapShuffler<>(nutTerms, rng, true);
        flowerTermShuffler = new GapShuffler<>(flowerTerms, rng, true);
        potionTermShuffler = new GapShuffler<>(potionTerms, rng, true);
        mappings.put("plant`term`", plantTermShuffler);
        mappings.put("fruit`term`", fruitTermShuffler);
        mappings.put("nut`term`", nutTermShuffler);
        mappings.put("flower`term`", flowerTermShuffler);
        mappings.put("potion`term`", potionTermShuffler);
        return this;
    }

    /**
     * Adds a large list of words pre-generated by FakeLanguageGen and hand-picked for fitness, and makes them
     * accessible with a keyword based on the language. The keywords this currently knows:
     * <br>
     * "lovecraft`pre`", "english`pre`", "greek_romanized`pre`",
     * "greek_authentic`pre`", "french`pre`", "russian_romanized`pre`",
     * "russian_authentic`pre`", "japanese_romanized`pre`", "swahili`pre`",
     * "somali`pre`", "hindi_romanized`pre`", "arabic_romanized`pre`",
     * "inuktitut`pre`", "norse`pre`", "nahuatl`pre`", "mongolian`pre`",
     * "fantasy`pre`", "fancy_fantasy`pre`", "goblin`pre`", "elf`pre`",
     * "demonic`pre`", "infernal`pre`", "simplish`pre`", "alien_a`pre`",
     * "korean_romanized`pre`", "alien_e`pre`", "alien_i`pre`", "alien_o`pre`",
     * "alien_u`pre`", "dragon`pre`", "kobold`pre`", "insect`pre`", "maori`pre`",
     * "spanish`pre`", "deep_speech`pre`", "norse_simplified`pre`",
     * "hletkip`pre`", "ancient_egyptian`pre`", "crow`pre`", "imp`pre`",
     * "malay`pre`", "celestial`pre`", "chinese_romanized`pre`",
     * "cherokee_romanized`pre`", "vietnamese`pre`"
     * <br>
     * These correspond to similarly-named fields in {@link FakeLanguageGen}, just without {@code `pre`}; for instance
     * {@code "cherokee_romanized`pre`"} corresponds to {@link FakeLanguageGen#CHEROKEE_ROMANIZED}. You can use these
     * same keywords with {@code `gen`} instead of {@code `pre`} to generate at runtime based on the current RNG state,
     * instead of using one of several pre-generated words, and doing that does not require addFakeWords() to be used.
     * <br>
     * Capitalizing the first letter in the keyword where it appears in text you call process() on will capitalize the
     * first letter of the produced fake word, which is often desirable for things like place names. Capitalizing the
     * second letter will capitalize the whole produced fake word. This applies only per-instance of each keyword; it
     * won't change the internally-stored list of words.
     * @return this for chaining
     */
    public Thesaurus addFakeWords()
    {
        //long state = rng.getState();
        for(Map.Entry<CharSequence, FakeLanguageGen> kv : languages.entrySet())
        {
            ArrayList<String> words = new ArrayList<>(16);
            for (int i = 0; i < 16; i++) {
                words.add(kv.getValue().word(rng, false, rng.between(2, 4)));
            }
            addCategory(StringKit.replace(kv.getKey(), "`gen", "`pre"), words);
        }
        //rng.setState(state);
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
//        Replacer rep = wordMatch.replacer(new SynonymSubstitution());
        //String t = rep.replace(text);
        Matcher m = wordMatch.matcher(text);
        Substitution substitution = new SynonymSubstitution();
        Replacer.StringBuilderBuffer dest = Replacer.wrap(new StringBuilder(text.length()));
        while (m.find()) {
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            m.setTarget(m, MatchResult.SUFFIX);
        }
        m.getGroup(MatchResult.TARGET, dest);

        m.setTarget(dest.sb);
        dest.sb.setLength(0);
        while (m.find()) {
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            m.setTarget(m, MatchResult.SUFFIX);
        }
        m.getGroup(MatchResult.TARGET, dest);

        if(alterations.isEmpty())
            return StringKit.replace(StringKit.correctABeforeVowel(dest.sb), "\t", "");
        else
            return StringKit.replace(modify(StringKit.correctABeforeVowel(dest.sb)), "\t", "");
    }

    public String lookup(String word)
    {
        if(word.isEmpty())
            return word;
        if("@".equals(word))
        {
            return defaultLanguage.word(rng, true, rng.between(2, 4));
        }
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
                return Character.toUpperCase(nx.charAt(0)) + nx.substring(1);
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
        private final StringBuilder temp = new StringBuilder(64);
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            //dest.append(lookup(match.group(0)));
            temp.setLength(0);
            match.getGroup(0, temp);
            writeLookup(dest, temp);
        }
    }

    private void writeLookup(TextBuffer dest, StringBuilder word) {
        if(word == null || word.length() <= 0)
            return;
        if(word.charAt(0) == '@' && word.length() == 1)
        {
            dest.append(defaultLanguage.word(rng, true, rng.between(2, 4)));
            return;
        }
        else if(mappings.containsKey(word))
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
                dest.append(nx.substring(1));
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
        else if(numbers.containsKey(word))
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(numberWordInRange(2, numbers.get(word)).toUpperCase());
            }
            else if(Category.Lu.contains(word.charAt(0)))
            {
                String w = numberWordInRange(2, numbers.get(word));
                dest.append(Character.toUpperCase(w.charAt(0)));
                dest.append(w.substring(1));
            }
            else
            {
                dest.append(numberWordInRange(2, numbers.get(word)));
            }
            return;
        }
        else if(numberAdjectives.containsKey(word))
        {
            if(word.length() > 1 && Category.Lu.contains(word.charAt(1)))
            {
                dest.append(numberAdjectiveInRange(2, numberAdjectives.get(word)).toUpperCase());
            }
            else if(Category.Lu.contains(word.charAt(0)))
            {
                String w = numberAdjectiveInRange(2, numberAdjectives.get(word));
                dest.append(Character.toUpperCase(w.charAt(0)));
                dest.append(w.substring(1));
            }
            else
            {
                dest.append(numberAdjectiveInRange(2, numberAdjectives.get(word)));
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
     * May use accented characters, as in
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
        if(!this.mappings.containsKey("empire`noun`"))
        {
            addKnownCategories();
        }
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
     * with the FakeLanguageGen already available instead of randomly created. May use accented characters, as in
     * "Thùdshù Hegemony" or "The Glorious Chô Empire",
     * if the given language can produce them; if you want to strip these out and replace accented chars
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
        if(!this.mappings.containsKey("empire`noun`"))
        {
            addKnownCategories();
        }
        String working = process(rng.getRandomElement(nationTerms));
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(rng.getRandomElement(nationTerms));
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working);
    }

    /**
     * Generates a random possible name for a plant or tree, such as "Ikesheha's maple" or "sugarleaf birch".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the FakeLanguageGen elements of this class' {@link #randomLanguages} field. Using one of these
     * FakeLanguageGen objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makePlantName()
    {
        if(!this.mappings.containsKey("tree`noun`"))
        {
            addKnownCategories();
        }
        if(plantTermShuffler == null)
        {
            plantTermShuffler = new GapShuffler<>(plantTerms, rng, true);
        }
        String working = process(plantTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(plantTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a plant or tree, such as "Ikesheha's maple" or "sugarleaf birch",
     * with the FakeLanguageGen already available instead of randomly created. May use accented characters, as in
     * "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link FakeLanguageGen#removeAccents()} on the FakeLanguageGen you would give this.
     *
     * @param language a FakeLanguageGen that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makePlantName(FakeLanguageGen language)
    {
        if(!this.mappings.containsKey("tree`noun`"))
        {
            addKnownCategories();
        }
        if(plantTermShuffler == null)
        {
            plantTermShuffler = new GapShuffler<>(plantTerms, rng, true);
        }
        String working = process(plantTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(plantTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible name for a plant or tree, such as "green lime-melon" or "Ung's date".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the FakeLanguageGen elements of this class' {@link #randomLanguages} field. Using one of these
     * FakeLanguageGen objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFruitName()
    {
        if(!this.mappings.containsKey("fruit`noun`"))
        {
            addKnownCategories();
        }
        if(fruitTermShuffler == null)
        {
            fruitTermShuffler = new GapShuffler<>(fruitTerms, rng, true);
        }
        String working = process(fruitTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(fruitTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a plant or tree, such as "green lime-melon" or "Ung's date",
     * with the FakeLanguageGen already available instead of randomly created. May use accented characters, as in
     * "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link FakeLanguageGen#removeAccents()} on the FakeLanguageGen you would give this.
     *
     * @param language a FakeLanguageGen that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFruitName(FakeLanguageGen language)
    {
        if(!this.mappings.containsKey("fruit`noun`"))
        {
            addKnownCategories();
        }
        if(fruitTermShuffler == null)
        {
            fruitTermShuffler = new GapShuffler<>(fruitTerms, rng, true);
        }
        String working = process(fruitTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(fruitTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible name for a plant or tree, such as "nut of Gikoim" or "Pelyt's cashew".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the FakeLanguageGen elements of this class' {@link #randomLanguages} field. Using one of these
     * FakeLanguageGen objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeNutName()
    {
        if(!this.mappings.containsKey("nut`noun`"))
        {
            addKnownCategories();
        }
        if(nutTermShuffler == null)
        {
            nutTermShuffler = new GapShuffler<>(nutTerms, rng, true);
        }
        String working = process(nutTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(nutTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a plant or tree, such as "nut of Gikoim" or "Pelyt's cashew",
     * with the FakeLanguageGen already available instead of randomly created.
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link FakeLanguageGen#removeAccents()} on the FakeLanguageGen you would give this.
     *
     * @param language a FakeLanguageGen that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeNutName(FakeLanguageGen language)
    {
        if(!this.mappings.containsKey("nut`noun`"))
        {
            addKnownCategories();
        }
        if(nutTermShuffler == null)
        {
            nutTermShuffler = new GapShuffler<>(nutTerms, rng, true);
        }
        String working = process(nutTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(nutTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible name for a plant or tree, such as "tulip of Jirui" or "Komert's thorny lilac".
     * May use accented characters, as in "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which returns a CharSequence that can be converted
     * to String if needed. Shortly after calling this method, but before
     * calling it again, you can retrieve the generated random languages, if any were used while making names, by
     * getting the FakeLanguageGen elements of this class' {@link #randomLanguages} field. Using one of these
     * FakeLanguageGen objects, you can produce many more words with a similar style to the person or place name, like
     * "Drayo" in "The Last Drayo Commonwealth". Calling this method replaces the current contents of
     * randomLanguages, so if you want to use those languages, get them while you can.
     *
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFlowerName()
    {
        if(!this.mappings.containsKey("flower`noun`"))
        {
            addKnownCategories();
        }
        if(flowerTermShuffler == null)
        {
            flowerTermShuffler = new GapShuffler<>(flowerTerms, rng, true);
        }
        String working = process(flowerTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(flowerTermShuffler.next());
        randomLanguages.clear();
        RandomLanguageSubstitution sub = new RandomLanguageSubstitution();
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }
    /**
     * Generates a random possible name for a plant or tree, such as "tulip of Jirui" or "Komert's thorny lilac",
     * with the FakeLanguageGen already available instead of randomly created. May use accented characters, as in
     * "Emôa's greenwood", if the given language can
     * produce them; if you want to strip these out and replace accented chars with their un-accented counterparts, you
     * can use {@link FakeLanguageGen#removeAccents(CharSequence)}, which
     * returns a CharSequence that can be converted to String if needed, or simply get an accent-less language by
     * calling {@link FakeLanguageGen#removeAccents()} on the FakeLanguageGen you would give this.
     *
     * @param language a FakeLanguageGen that will be used to construct any non-English names
     * @return a random name for a plant, shrub, or tree, as a String
     */
    public String makeFlowerName(FakeLanguageGen language)
    {
        if(!this.mappings.containsKey("flower`noun`"))
        {
            addKnownCategories();
        }
        if(flowerTermShuffler == null)
        {
            flowerTermShuffler = new GapShuffler<>(flowerTerms, rng, true);
        }
        String working = process(flowerTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(flowerTermShuffler.next());
        randomLanguages.clear();
        KnownLanguageSubstitution sub = new KnownLanguageSubstitution(language);
        Replacer replacer = Pattern.compile("@(-@)?").replacer(sub);
        return replacer.replace(working).replace("\t", "");
    }

    /**
     * Generates a random possible description for a potion in a container, such as "a smoky glass flask containing a
     * few drops of an orange tonic", "a milk carton filled with a red fluid", "a shining silver bottle filled with an
     * effervescent violet potion", or "a wineskin filled with a black serum".
     *
     * @return a random description for a potion or other liquid in a container
     */
    public String makePotionDescription()
    {
        if(!this.mappings.containsKey("liquid`noun`"))
        {
            addKnownCategories();
        }
        if(potionTermShuffler == null)
        {
            potionTermShuffler = new GapShuffler<>(potionTerms, rng, true);
        }
        String working = process(potionTermShuffler.next());
        int frustration = 0;
        while (frustration++ < 8 && similarFinder.matches(working))
            working = process(potionTermShuffler.next());
        return StringKit.correctABeforeVowel(working);
    }

    /**
     * Gets an English word for a given number, if this knows it. These words are known from 0 ("zero") to 20
     * ("twenty"), as well as some higher numbers. If a word isn't known for a number, this returns the number as a
     * String, such as "537" or "-1".
     * @param number the number to get a word for
     * @return the word associated with a number as a String, or if none is known, {@code String.valueOf(number)}.
     */
    public static String numberWord(final int number)
    {
        switch (number){
            case 0: return "zero";
            case 1: return "one";
            case 2: return "two";
            case 3: return "three";
            case 4: return "four";
            case 5: return "five";
            case 6: return "six";
            case 7: return "seven";
            case 8: return "eight";
            case 9: return "nine";
            case 10: return "ten";
            case 11: return "eleven";
            case 12: return "twelve";
            case 13: return "thirteen";
            case 14: return "fourteen";
            case 15: return "fifteen";
            case 16: return "sixteen";
            case 17: return "seventeen";
            case 18: return "eighteen";
            case 19: return "nineteen";
            case 20: return "twenty";
            case 30: return "thirty";
            case 40: return "fourty";
            case 50: return "fifty";
            case 60: return "sixty";
            case 70: return "seventy";
            case 80: return "eighty";
            case 90: return "ninety";
            case 100: return "hundred";
            case 1000: return "thousand";
            case 1000000: return "million";
            case 1000000000: return "billion";
            default: return String.valueOf(number);
        }
    }

    /**
     * Gets an English word that describes a numbered position in some sequence, if this knows it (such as "second" or
     * "eleventh"). These words are known from 1 ("first") to 20 ("twentieth"), as well as some higher numbers. If a
     * word isn't known for a number, this appends a suffix based on the last base-10 digit to the number, as with "0th"
     * or "42nd".
     * @param number the number to get a position adjective for
     * @return the word associated with a number as a String, or if none is known, {@code String.valueOf(number)} followed by a two-char suffix from the last digit.
     */
    public static String numberAdjective(final int number)
    {
        switch (number){
            case 1: return "first";
            case 2: return "second";
            case 3: return "third";
            case 4: return "fourth";
            case 5: return "fifth";
            case 6: return "sixth";
            case 7: return "seventh";
            case 8: return "eighth";
            case 9: return "ninth";
            case 10: return "tenth";
            case 11: return "eleventh";
            case 12: return "twelfth";
            case 13: return "thirteenth";
            case 14: return "fourteenth";
            case 15: return "fifteenth";
            case 16: return "sixteenth";
            case 17: return "seventeenth";
            case 18: return "eighteenth";
            case 19: return "nineteenth";
            case 20: return "twentieth";
            case 30: return "thirtieth";
            case 40: return "fourtieth";
            case 50: return "fiftieth";
            case 60: return "sixtieth";
            case 70: return "seventieth";
            case 80: return "eightieth";
            case 90: return "ninetieth";
            case 100: return "hundredth";
            case 1000: return "thousandth";
            case 1000000: return "millionth";
            case 1000000000: return "billionth";
            default: 
            {
                switch (number % 10){
                    case 1: return number + "st";
                    case 2: return number + "nd";
                    case 3: return number + "rd";
                    default:return number + "th";
                }
            }
        }
    }

    /**
     * Gets an English word for a number, if this knows it, where the number is chosen randomly between lowest and
     * highest, both inclusive. These words are known from 0 ("zero") to 20 ("twenty"), as well as some higher numbers.
     * If a word isn't known for a number, this returns the number as a String, such as "537" or "-1".
     * @param lowest the lower bound for numbers this can choose, inclusive
     * @param highest the upper bound for numbers this can choose, inclusive
     * @return a String word for a number in the given range, such as "six" or "eleven", if it is known
     */
    public String numberWordInRange(int lowest, int highest)
    {
        return numberWord(rng.nextSignedInt(highest + 1 - lowest) + lowest);
    }
    /**
     * Gets an English word that describes a numbered position in some sequence, if this knows it (such as "second" or
     * "eleventh"), where the number is chosen randomly between lowest and highest, both inclusive. These words are
     * known from 1 ("first") to 20 ("twentieth"), as well as some output for other numbers (such as "42nd" or "23rd").
     * @param lowest the lower bound for numbers this can choose, inclusive
     * @param highest the upper bound for numbers this can choose, inclusive
     * @return a String word for a number in the given range, such as "third" or "twelfth", if it is known
     */
    public String numberAdjectiveInRange(int lowest, int highest)
    {
        return numberAdjective(rng.nextSignedInt(highest + 1 - lowest) + lowest);
    }
    
    private static final String[] nationTerms = new String[]{
            "Union`adj` Union`noun` of @", "Union`adj` @ Union`noun`", "@ Union`noun`", "@ Union`noun`", "@-@ Union`noun`", "Union`adj` Union`noun` of @",
            "Union`adj` Duchy`nouns` of @",  "The @ Duchy`noun`", "The Fancy`adj` @ Duchy`noun`", "The Sole`adj` @ Empire`noun`",
            "@ Empire`noun`", "@ Empire`noun`", "@ Empire`noun`", "@-@ Empire`noun`", "The Fancy`adj` @ Empire`noun`", "The Fancy`adj` @ Empire`noun`", "The Holy`adj` @ Empire`noun`",};

    private static final String[] plantTerms = new String[]{
            "@'s color`adj`\tleaf`noun`",
            "@'s tree`noun`",
            "@'s color`adj` tree`noun`",
            "@'s flower`noun`",
            "@'s shape`adj` flower`noun`",
            "@'s color`adj` flower`noun`",
            "flower`noun` of @",
            "leaf`noun` of @",
            "@'s ground`noun`\tleaf`noun`",
            "ground`noun`\tleaf`noun` of @",
            "sensory`adj` tree`noun` of @",
            "sensory`adj` flower`noun` of @",
            "color`adj` flower`noun` of @",
            "@'s sensory`adj`-leaf`noun`",
            "ground`noun`\tleaf`noun`",

            "shape`adj` flower`noun`",
            "color`adj` flower`noun`",
            "flavor`noun`\tleaf`noun` tree`noun`",
            "flavor`adj` fruit`noun` tree`noun`",
            "flavor`adj` nut`noun` tree`noun`",
            "color`adj` fruit`noun` tree`noun`",
            "color`adj` nut`noun` tree`noun`",
            "shape`adj`-fruit`noun` tree`noun`",
            "shape`adj`-leaf`noun` tree`noun`",
            "sensory`adj` tree`noun`-tree`noun`",
            "sensory`adj`-leaf`noun` tree`noun`",
            "color`adj`-leaf`noun` flower`noun`",
            "shape`adj`-leaf`noun` flower`noun`",
            "sensory`adj` flower`noun`-flower`noun`",
            "sensory`adj`-leaf`noun` flower`noun`",
            "ground`noun`\tflower`noun`",
    };
    private static final String[] fruitTerms = new String[]{
            "fruit`noun` of @",
            "@'s fruit`noun`",
            "@'s flavor`adj` fruit`noun`",
            "@'s color`adj` fruit`noun`",
            "flavor`adj` fruit`noun`-fruit`noun`",
            "color`adj` fruit`noun`-fruit`noun`",
    };
    private static final String[] nutTerms = new String[]{
            "nut`noun` of @",
            "color`adj` nut`noun` of @",
            "@'s nut`noun`",
            "@'s flavor`adj` nut`noun`",
            "@'s color`adj` nut`noun`",
            "flavor`adj` nut`noun`",
            "color`adj` nut`noun`",
            "sensory`adj` nut`noun`",
    };
    private static final String[] flowerTerms = new String[]{
            "flower`noun` of @",
            "sensory`adj` flower`noun` of @",
            "color`adj` flower`noun` of @",
            "@'s flower`noun`",
            "@'s shape`adj` flower`noun`",
            "@'s color`adj` flower`noun`",
            "shape`adj` flower`noun`",
            "color`adj` flower`noun`",
            "color`adj`-leaf`noun` flower`noun`",
            "shape`adj`-leaf`noun` flower`noun`",
            "sensory`adj` flower`noun`-flower`noun`",
            "sensory`adj`-leaf`noun` flower`noun`",
            "ground`noun`\tflower`noun`",

    };
    private static final String[] potionTerms = new String[]{
            "a bottle`adj` bottle`noun` filled with a liquid`adj` color`adj` liquid`noun`",
            "a bottle`adj` bottle`noun` filled with a color`adj` liquid`noun`",
            "a calabash`adj` filled with a color`adj` liquid`noun`",
            "a bottle`adj` bottle`noun` half-filled with a liquid`adj` color`adj` liquid`noun`",
            "a bottle`adj` bottle`noun` containing a few drops of a color`adj` liquid`noun`",
    };
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
            makeList("nature", "forest", "greenery", "jungle", "woodland", "grove", "copse", "glen"),
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
            "ground`noun`",
            makeList("earth", "sand", "soil", "loam", "dirt", "clay", "mud", "peat"),
            "lake`noun`",
            makeList("puddle", "pond", "lake", "sea", "swamp", "bog", "fen", "glade"),
            "leaf`noun`",
            makeList("leaf", "bark", "root", "thorn", "seed", "branch", "twig", "wort", "cress", "flower", "wood", "vine", "sap", "bud", "blossom", "shoot", "stalk", "stem"),
            "fruit`noun`",
            makeList("fruit", "berry", "apple", "peach", "cherry", "melon", "lime", "fig", "date", "mango", "banana", "juniper", "grape", "papaya", "pear", "quince"),
            "nut`noun`",
            makeList("nut", "bean", "almond", "peanut", "pecan", "walnut", "cashew", "pea", "chestnut", "hazelnut"),
            "flower`noun`",
            makeList("flower", "rose", "lilac", "orchid", "peony", "oleander", "chrysanthemum", "amaryllis", "camellia", "mallow", "lily", "gardenia", "daisy", "hibiscus", "dandelion", "jasmine", "lotus", "lantana", "phlox", "petunia", "tulip"),
            "tree`noun`",
            makeList("tree", "oak", "pine", "juniper", "maple", "beech", "birch", "larch", "willow", "alder", "cedar", "palm", "magnolia", "hazel", "cactus", "mangrove", "elm"),
            "flavor`noun`",
            makeList("sugar", "spice", "acid", "herb", "salt", "grease", "smoke"),
            "flavor`adj`",
            makeList("sweet", "spicy", "sour", "bitter", "salty", "savory", "smoky"),
            "color`adj`",
            makeList("black", "white", "red", "orange", "yellow", "green", "blue", "violet", "gray", "brown"),
            "shape`adj`",
            makeList("hollow", "tufted", "drooping", "fibrous", "giant", "miniature", "delicate", "hardy", "spiny", "thorny", "fragile", "sturdy", "long", "stubby", "stiff", "yielding"),
            "sensory`adj`",
            makeList("fragrant", "pungent", "rustling", "fuzzy", "glossy", "weeping", "rough", "smooth", "soft", "aromatic"),
            "liquid`noun`",
            makeList("liquid", "elixir", "tonic", "fluid", "brew", "broth", "potion", "serum"),
            "liquid`adj`",
            makeList("bubbling", "effervescent", "swirling", "murky", "thick", "congealing", "sloshing", "slimy", "milky"),
            "bottle`noun`",
            makeList("bottle", "flask", "vial", "jug", "phial", "flagon", "canister"),
            "bottle`adj`",
            makeList("clear glass", "smoky glass", "green glass", "brown glass", "fluted crystal", "tarnished silver", "dull pewter", "shining silver", "curvaceous glass", "rough-cut glass", "sharp-edged tin"),
            "calabash`adj`",
            makeList("hollow gourd", "calabash", "milk carton", "waterskin", "wineskin"),
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
            makeList("many", "myriad", "thousandfold", "infinite", "countless", "unlimited", "manifold"),
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
            makeList("reviled", "despised", "hated", "loathed"),
            "glyph`noun`",
            makeList("glyph", "sign", "symbol", "sigil", "seal", "mark"),
            "glyph`nouns`",
            makeList("glyphs", "signs", "symbols", "sigils", "seals", "marks"),
            "power`noun`",
            makeList("power", "force", "potency", "strength", "authority", "dominance"),
            "power`adj`",
            makeList("powerful", "forceful", "potent", "strong", "authoritative", "dominant")
            );
    public static final OrderedMap<String, ArrayList<String>>
            adjective = new OrderedMap<>(categories),
            noun = new OrderedMap<>(categories),
            nouns = new OrderedMap<>(categories);
    public static final OrderedMap<CharSequence, FakeLanguageGen> languages = new OrderedMap<CharSequence, FakeLanguageGen>(
            FakeLanguageGen.registeredNames.length, Hashers.caseInsensitiveStringHasher);
    static {
        for (int i = 0; i < FakeLanguageGen.registeredNames.length; i++) {
            languages.put(FakeLanguageGen.registeredNames[i].replace(' ', '_').toLowerCase() + "`gen`", FakeLanguageGen.registered[i]);
        }
    }
    public static final OrderedMap<CharSequence, Integer> numbers = new OrderedMap<CharSequence, Integer>(
            21, Hashers.caseInsensitiveStringHasher
    ).putPairs(
            "zero`noun`", 0,
            "one`noun`", 1,
            "two`noun`", 2,
            "three`noun`", 3,
            "four`noun`", 4,
            "five`noun`", 5,
            "six`noun`", 6,
            "seven`noun`", 7,
            "eight`noun`", 8,
            "nine`noun`", 9,
            "ten`noun`", 10,
            "eleven`noun`", 11,
            "twelve`noun`", 12,
            "thirteen`noun`", 13,
            "fourteen`noun`", 14,
            "fifteen`noun`", 15,
            "sixteen`noun`", 16,
            "seventeen`noun`", 17,
            "eighteen`noun`", 18,
            "nineteen`noun`", 19,
            "twenty`noun`", 20
    ),
            numberAdjectives = new OrderedMap<CharSequence, Integer>(
                    21, Hashers.caseInsensitiveStringHasher
            ).putPairs(
                    "zero`adj`", 0,
                    "one`adj`", 1,
                    "two`adj`", 2,
                    "three`adj`", 3,
                    "four`adj`", 4,
                    "five`adj`", 5,
                    "six`adj`", 6,
                    "seven`adj`", 7,
                    "eight`adj`", 8,
                    "nine`adj`", 9,
                    "ten`adj`", 10,
                    "eleven`adj`", 11,
                    "twelve`adj`", 12,
                    "thirteen`adj`", 13,
                    "fourteen`adj`", 14,
                    "fifteen`adj`", 15,
                    "sixteen`adj`", 16,
                    "seventeen`adj`", 17,
                    "eighteen`adj`", 18,
                    "nineteen`adj`", 19,
                    "twenty`adj`", 20
            );

    /**
     * Thesaurus preset that changes all text to sound like this speaker: "Desaurus preset dat changez all text to sound
     * like dis speakah." You may be familiar with a certain sci-fi game that has orks who sound like this.
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
        
        // not related to ORK; this filters out synonyms that aren't in the appropriate list
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
