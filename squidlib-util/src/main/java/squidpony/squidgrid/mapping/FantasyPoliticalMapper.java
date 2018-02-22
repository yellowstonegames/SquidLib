package squidpony.squidgrid.mapping;

import squidpony.*;
import squidpony.annotation.Beta;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * When you have a world map as produced by {@link WorldMapGenerator}, you may want to fill it with claims by various
 * factions, where each faction may be hand-made and may consist of humans or some fantasy species, such as goblins,
 * elves, or demons. This can assign contiguous areas of land to various factions, while acknowledging any preferences
 * some species may have for specific types of land (elves may strongly prefer forest terrain, or flying demons may be
 * the ideal residents for difficult mountainous terrain). This needs both a {@link WorldMapGenerator}, preferably
 * {@link squidpony.squidgrid.mapping.WorldMapGenerator.TilingMap} or
 * {@link squidpony.squidgrid.mapping.WorldMapGenerator.EllipticalMap} because they distribute area equally across the
 * map, and a {@link squidpony.squidgrid.mapping.WorldMapGenerator.BiomeMapper} to allocate biomes and height/moisture
 * info.
 */
@Beta
public class FantasyPoliticalMapper implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * Represents a group that claims territory on a world-map, such as a nation. Each Faction has a name, a short name
     * that may be the same as the regular name, a FakeLanguageGen that would be used to generate place names in that
     * Faction's territory, and a number of (possibly null) arrays and Sets that represent what terrains this Faction
     * wants to claim and which it will avoid (e.g. elves may prefer claiming forests, while frost giants won't claim
     * anywhere that's even remotely warm).
     */
    public static class Faction implements Serializable
    {
        private static final long serialVersionUID = 0L;

        public String name, shortName;
        public FakeLanguageGen language;
        /**
         * An UnorderedSet of String keys, where each key is the name of a biome this Faction wants to occupy.
         * May be null if no biomes are specifically preferred.
         */
        public UnorderedSet<String> preferredBiomes;
        /**
         * An UnorderedSet of String keys, where each key is the name of a biome this Faction will never occupy.
         * May be null if all biomes are available, but unless this is specified in a constructor, the default will be
         * to consider "Ocean" blocked.
         */
        public UnorderedSet<String> blockedBiomes;

        /**
         * An int array of height codes that this Faction prefers; 0, 1, 2, and 3 are all oceans, while 4 is shoreline
         * or low-lying land and higher numbers (up to 8, inclusive) are used for increasing elevations.
         */
        public int[] preferredHeight;
        /**
         * An int array of heat codes that this Faction prefers; typically a 6-code scale is used where 0, 1, and 2 are
         * cold and getting progressively warmer, while 3, 4, and 5 are warm to warmest.
         */
        public int[] preferredHeat;
        /**
         * An int array of moisture codes that this Faction prefers; typically a 6-code scale is used where 0, 1, and 2
         * are dry and getting progressively more precipitation, while 3, 4, and 5 are wet to wettest.
         */
        public int[] preferredMoisture;

        /**
         * Zero-arg constructor that sets the language to a random FakeLanguageGen (using
         * {@link FakeLanguageGen#randomLanguage(long)}), then generates a name/shortName with that FakeLanguageGen, and
         * makes the only blocked biome "Ocean".
         */
        public Faction()
        {
            language = FakeLanguageGen.randomLanguage(
                    (long) ((Math.random() - 0.5) * 0x10000000000000L)
                            ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
            shortName = name = language.word(true);
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }

        /**
         * Constructor that sets the language to the specified FakeLanguageGen, then generates a name/shortName with
         * that FakeLanguageGen, and makes the only blocked biome "Ocean".
         * @param language the FakeLanguageGen to use for generating the name of the Faction and potentially place names
         */
        public Faction(FakeLanguageGen language)
        {
            this.language = language;
            shortName = name = language.word(true);
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name and shortName to the
         * specified name, and makes the only blocked biome "Ocean".
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"; will also be the shortName
         */
        public Faction(FakeLanguageGen language, String name)
        {
            this.language = language;
            shortName = this.name = name;
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name to the specified name and
         * the shortName to the specified shortName, and makes the only blocked biome "Ocean".
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         */
        public Faction(FakeLanguageGen language, String name, String shortName)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, and makes the only blocked biome "Ocean". The exact String names that are viable for
         * biomes can be obtained from a BiomeMapper with {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()}.
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()} value
         * 
         */
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, and sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes. The exact String names that are viable for biomes can be obtained from a BiomeMapper with
         * {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()}.
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         */
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            this.blockedBiomes = new UnorderedSet<>(blockedBiomes);
        }
        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes, and sets the preferred height codes to the ints in preferredHeight (with 4 being sea level and
         * 8 being the highest peaks). The exact String names that are viable for biomes can be obtained from a
         * BiomeMapper with {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()}.
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         * @param preferredHeight an int array of height codes this Faction prefers to claim; 4 is sea level and 8 is highest
         */
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes, int[] preferredHeight)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            this.blockedBiomes = new UnorderedSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
        }

        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes, sets the preferred height codes to the ints in preferredHeight (with 4 being sea level and 8
         * being the highest peaks), and sets the preferred heat codes to the ints in preferredHeat (with the exact
         * values depending on the BiomeMapper, but usually 0-5 range from coldest to hottest). The exact String names
         * that are viable for biomes can be obtained from a BiomeMapper with
         * {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()}.
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         * @param preferredHeight an int array of height codes this Faction prefers to claim; 4 is sea level and 8 is highest
         * @param preferredHeat an int array of heat codes this Faction prefers to claim; typically 0 is coldest and 5 is hottest
         */
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes,
                       int[] preferredHeight, int[] preferredHeat)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            this.blockedBiomes = new UnorderedSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
            this.preferredHeat = preferredHeat;
        }
        /**
         * Constructor that sets the language to the specified FakeLanguageGen, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes, sets the preferred height codes to the ints in preferredHeight (with 4 being sea level and 8
         * being the highest peaks), sets the preferred heat codes to the ints in preferredHeat (with the exact values
         * depending on the BiomeMapper, but usually 0-5 range from coldest to hottest), and sets the preferred moisture
         * codes to the ints in preferredMoisture (withe the exact values depending on the BiomeMapper, but usually 0-5
         * range from driest to wettest). The exact String names that are viable for biomes can be obtained from a
         * BiomeMapper with {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()}.
         * @param language the FakeLanguageGen to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link WorldMapGenerator.BiomeMapper#getBiomeNameTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         * @param preferredHeight an int array of height codes this Faction prefers to claim; 4 is sea level and 8 is highest
         * @param preferredHeat an int array of heat codes this Faction prefers to claim; typically 0 is coldest and 5 is hottest
         * @param preferredMoisture an int array of moisture codes this Faction prefers to claim; typically 0 is driest and 5 is wettest
         */
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes,
                       int[] preferredHeight, int[] preferredHeat, int[] preferredMoisture)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            this.blockedBiomes = new UnorderedSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
            this.preferredHeat = preferredHeat;
            this.preferredMoisture = preferredMoisture;
        }
    }

    public int width;
    public int height;
    public StatefulRNG rng;
    public String name;
    public char[][] politicalMap;
    public char[][] zoomedMap;
    public WorldMapGenerator wmg;
    public WorldMapGenerator.BiomeMapper biomeMapper;
    private static final ArrayList<Character> letters = Maker.makeList(
            '~', '%', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'À', 'Á', 'Â', 'Ã', 'Ä', 'Å', 'Æ', 'Ç', 'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï', 'Ð', 'Ñ', 'Ò', 'Ó', 'Ô', 'Õ', 'Ö', 'Ø', 'Ù', 'Ú', 'Û',
            'Ü', 'Ý', 'Þ', 'ß', 'à', 'á', 'â', 'ã', 'ä', 'å', 'æ', 'ç', 'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö',
            'ø', 'ù', 'ú', 'û', 'ü', 'ý', 'þ', 'ÿ', 'Ā', 'ā', 'Ă', 'ă', 'Ą', 'ą', 'Ć', 'ć', 'Ĉ', 'ĉ', 'Ċ', 'ċ', 'Č', 'č', 'Ď', 'ď', 'Đ', 'đ', 'Ē',
            'ē', 'Ĕ', 'ĕ', 'Ė', 'ė', 'Ę', 'ę', 'Ě', 'ě', 'Ĝ', 'ĝ', 'Ğ', 'ğ', 'Ġ', 'ġ', 'Ģ', 'ģ', 'Ĥ', 'ĥ', 'Ħ', 'ħ', 'Ĩ', 'ĩ', 'Ī', 'ī', 'Ĭ', 'ĭ',
            'Į', 'į', 'İ', 'ı', 'Ĵ', 'ĵ', 'Ķ', 'ķ', 'ĸ', 'Ĺ', 'ĺ', 'Ļ', 'ļ', 'Ľ', 'ľ', 'Ŀ', 'ŀ', 'Ł', 'ł', 'Ń', 'ń', 'Ņ', 'ņ', 'Ň', 'ň', 'ŉ', 'Ō',
            'ō', 'Ŏ', 'ŏ', 'Ő', 'ő', 'Œ', 'œ', 'Ŕ', 'ŕ', 'Ŗ', 'ŗ', 'Ř', 'ř', 'Ś', 'ś', 'Ŝ', 'ŝ', 'Ş', 'ş', 'Š', 'š', 'Ţ', 'ţ', 'Ť', 'ť', 'Ŧ', 'ŧ',
            'Ũ', 'ũ', 'Ū', 'ū', 'Ŭ', 'ŭ', 'Ů', 'ů', 'Ű', 'ű', 'Ų', 'ų', 'Ŵ', 'ŵ', 'Ŷ', 'ŷ', 'Ÿ', 'Ź', 'ź', 'Ż', 'ż', 'Ž', 'ž', 'Ǿ', 'ǿ', 'Ș', 'ș',
            'Ț', 'ț', 'Γ', 'Δ', 'Θ', 'Λ', 'Ξ', 'Π', 'Σ', 'Φ', 'Ψ', 'Ω', 'α');
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that store the full name of nations.
     */
    public OrderedMap<Character, Faction> atlas;

    /**
     * Constructs a FantasyPoliticalMapper, but doesn't do anything with a map; you need to call
     * {@link #generate(long, WorldMapGenerator, WorldMapGenerator.BiomeMapper, Collection, int, double)} for results.
     */
    public FantasyPoliticalMapper()
    {
        rng = new StatefulRNG();
    }

    /**
     * For when you really don't care what arguments you give this, you can use this zero-parameter overload of
     * generate() to produce a 128x128 {@link squidpony.squidgrid.mapping.WorldMapGenerator.TilingMap} world map with a
     * {@link squidpony.squidgrid.mapping.WorldMapGenerator.SimpleBiomeMapper} biome mapper, filling it with 30 random
     * Factions and trying to avoid unclaimed land. You may need to use {@link #atlas} to make sense of the randomly
     * generated Factions. The seed will be random here.
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate() {
        wmg = new WorldMapGenerator.TilingMap(rng.nextLong(),128, 128);
        wmg.generate();
        biomeMapper = new WorldMapGenerator.SimpleBiomeMapper();
        biomeMapper.makeBiomes(wmg);
        return generate(rng.nextLong(), wmg, biomeMapper, null, 30, 1.0);
    }

    /**
     * Generates a 2D char array that represents the claims to the land described by the WorldMapGenerator {@code wmg}
     * and the BiomeMapper {@code biomeMapper} by various Factions, where {@link Faction} is an inner class.
     * This starts with two default Factions for "Ocean" and "Wilderness" (unclaimed land) and adds randomly generated
     * Factions to fill factionCount (the two default factions aren't counted against this limit). These Factions
     * typically claim contiguous spans of land stretching out from a starting point that matches the Faction's
     * preferences for biome, land height, heat, and moisture. If a Faction requires a biome (like "TropicalRainforest") 
     * and the world has none of that type, then that Faction won't claim any land. If the WorldMapGenerator zooms in or
     * out, you should call {@link #adjustZoom()} to get a different 2D char array that represents the zoomed-in area.
     * This overload tries to claim all land that can be reached by an existing Faction, though islands will often be
     * unclaimed.
     * @param seed the seed that determines how Factions will randomly spread around the world
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factionCount the number of factions to have claiming land; cannot be negative or more than 253
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, WorldMapGenerator.BiomeMapper biomeMapper,
                             int factionCount) {
        return generate(seed, wmg, biomeMapper, null, factionCount, 1.0);
    }
    /**
     * Generates a 2D char array that represents the claims to the land described by the WorldMapGenerator {@code wmg}
     * and the BiomeMapper {@code biomeMapper} by various Factions, where {@link Faction} is an inner class.
     * This starts with two default Factions for "Ocean" and "Wilderness" (unclaimed land) and adds randomly generated
     * Factions to fill factionCount (the two default factions aren't counted against this limit). These Factions
     * typically claim contiguous spans of land stretching out from a starting point that matches the Faction's
     * preferences for biome, land height, heat, and moisture. If a Faction requires a biome (like "TropicalRainforest") 
     * and the world has none of that type, then that Faction won't claim any land. If the WorldMapGenerator zooms in or
     * out, you should call {@link #adjustZoom()} to get a different 2D char array that represents the zoomed-in area.
     * This overload tries to claim the given {@code controlledFraction} of land in total, though 1.0 can rarely be
     * reached unless there are many factions and few islands.
     * @param seed the seed that determines how Factions will randomly spread around the world
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factionCount the number of factions to have claiming land; cannot be negative or more than 253
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, WorldMapGenerator.BiomeMapper biomeMapper,
                             int factionCount, double controlledFraction) {
        return generate(seed, wmg, biomeMapper, null, factionCount, controlledFraction);
    }
    /**
     * Generates a 2D char array that represents the claims to the land described by the WorldMapGenerator {@code wmg}
     * and the BiomeMapper {@code biomeMapper} by various Factions, where {@link Faction} is an inner class.
     * This starts with two default Factions for "Ocean" and "Wilderness" (unclaimed land) and adds all of
     * {@code factions} until {@code factionCount} is reached; if it isn't reached, random Factions will be generated to
     * fill factionCount (the two default factions aren't counted against this limit). These Factions typically claim
     * contiguous spans of land stretching out from a starting point that matches the Faction's preferences for biome,
     * land height, heat, and moisture. If a Faction requires a biome (like "TropicalRainforest") and the world has none
     * of that type, then that Faction won't claim any land. If the WorldMapGenerator zooms in or out, you should call
     * {@link #adjustZoom()} to get a different 2D char array that represents the zoomed-in area. This overload tries to
     * claim the given {@code controlledFraction} of land in total, though 1.0 can rarely be reached unless there are
     * many factions and few islands.
     *
     * @param seed the seed that determines how Factions will randomly spread around the world
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factions a Collection of {@link Faction} that will be copied, shuffled and used before adding any random Factions
     * @param factionCount the number of factions to have claiming land; cannot be negative or more than 253
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, WorldMapGenerator.BiomeMapper biomeMapper,
                                  Collection<Faction> factions, int factionCount, double controlledFraction) {
        rng.setState(seed);
        factionCount = Math.abs(factionCount % 254);
        Thesaurus th = new Thesaurus(rng.nextLong());
        th.addKnownCategories();
        ArrayList<Faction> fact = factions == null ? new ArrayList<Faction>() : rng.shuffle(factions);
        for (int i = fact.size(); i < factionCount; i++) {
            String name = th.makeNationName(), shortName = th.latestGenerated;
            FakeLanguageGen lang;
            if(th.randomLanguages == null || th.randomLanguages.isEmpty())
                lang = FakeLanguageGen.randomLanguage(rng);
            else
                lang = th.randomLanguages.get(0);
            fact.add(new Faction(lang, name, shortName));
        }
        if(factionCount > 0)
            rng.shuffleInPlace(fact);
        fact.add(0, new Faction(FakeLanguageGen.DEMONIC, "The Lost Wilderness", "Wilderness"));
        fact.add(0, new Faction(FakeLanguageGen.ALIEN_O, "The Vast Domain of the Seafolk", "Seafolk", new String[]{"Ocean"}));
        atlas = new OrderedMap<>(letters.subList(0, factionCount + 2), fact.subList(0, factionCount + 2), 0.5f);
        this.wmg = wmg;
        this.biomeMapper = biomeMapper;
        width = wmg.width;
        height = wmg.height;
        GreasedRegion land = new GreasedRegion(wmg.heightCodeData, 4, 999);
        politicalMap = land.toChars('%', '~');
        int controlled = (int) (land.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));

        int[] centers = land.copy().randomScatter(rng, (int) (Math.sqrt(width * height) * 0.1 + 0.999), factionCount).asTightEncoded();
        int cen, cx, cy, cx2, cy2, biome, high, hot, moist, count = centers.length, re;
        String biomeName;
        String[] biomeTable = biomeMapper.getBiomeNameTable();
        int[] reorder = new int[count];
        Faction current;
        int[] factionIndices = new int[count];
        for (int c = 0; c < count; c++) {
            cen = centers[c];
            cx = cen % width;
            cy = cen / width;
            biome = biomeMapper.getBiomeCode(cx, cy);
            biomeName = biomeTable[biome];
            high = wmg.heightCodeData[cx][cy];
            hot = biomeMapper.getHeatCode(cx, cy);
            moist = biomeMapper.getMoistureCode(cx, cy);
            rng.randomOrdering(count, reorder);
            PER_FACTION:
            for (int i = 0; i < count; i++) {
                current = fact.get(re = reorder[i]);
                if(current.preferredBiomes == null || current.preferredBiomes.contains(biomeName))
                {
                    factionIndices[c] = re;
                    break;
                }
                if(current.blockedBiomes != null && current.blockedBiomes.contains(biomeName))
                    continue;
                if(current.preferredHeight != null)
                {
                    for (int j = 0; j < current.preferredHeight.length; j++) {
                        if(high == current.preferredHeight[j])
                        {
                            factionIndices[c] = re;
                            break PER_FACTION;
                        }
                    }
                }
                if(current.preferredHeat != null)
                {
                    for (int j = 0; j < current.preferredHeat.length; j++) {
                        if(hot == current.preferredHeat[j])
                        {
                            factionIndices[c] = re;
                            break PER_FACTION;
                        }
                    }
                }
                if(current.preferredMoisture != null)
                {
                    for (int j = 0; j < current.preferredMoisture.length; j++) {
                        if(moist == current.preferredMoisture[j])
                        {
                            factionIndices[c] = re;
                            break PER_FACTION;
                        }
                    }
                }
            }
        }
        IntVLA[] fresh = new IntVLA[count];
        int filled = 0;
        boolean hasFresh = false;
        int approximateArea = (controlled * 4) / (count * 3);
        char[] keys = new char[count];
        double[] biases = new double[count];
        for (int i = 0; i < count; i++) {
            fresh[i] = new IntVLA(approximateArea);
            cen = centers[i];
            fresh[i].add(cen);
            cx = cen % width;
            cy = cen / width;
            politicalMap[cx][cy] = keys[i] = atlas.keyAt(factionIndices[i] + 2);
            biases[i] = rng.nextDouble() * rng.nextDouble() + rng.nextDouble() + 0.03125;
            hasFresh = true;
        }
        Direction[] dirs = Direction.CARDINALS;
        IntVLA currentFresh;
        GreasedRegion anySpillMap = new GreasedRegion(width, height),
                anyFreshMap = new GreasedRegion(width, height);

        while (hasFresh && filled < controlled) {
            hasFresh = false;
            for (int i = 0; i < count && filled < controlled; i++) {
                currentFresh = fresh[i];
                if (currentFresh.isEmpty())
                    continue;
                else
                    hasFresh = true;
                if (rng.nextDouble() < biases[i]) {
                    int index = rng.nextIntHasty(currentFresh.size), cell = currentFresh.get(index);
                    cx = cell % width;
                    cy = cell / width;


                    politicalMap[cx][cy] = keys[i];
                    filled++;
                    anySpillMap.insert(cx, cy);

                    for (int d = 0; d < dirs.length; d++) {
                        cx2 = wmg.wrapX(cx + dirs[d].deltaX, cy);
                        cy2 = wmg.wrapY(cx, cy + dirs[d].deltaY);
                        if (cx == cx2 && cy == cy2)
                            continue;
                        if (land.contains(cx2, cy2) && !anySpillMap.contains(cx2, cy2)) {
                            if(!anyFreshMap.contains(cx2, cy2)) {
                                currentFresh.add(cx2 + cy2 * width);
                                anyFreshMap.insert(cx2, cy2);
                            }
                        }
                    }
                    currentFresh.removeIndex(index);
                    anyFreshMap.remove(cx, cy);
                }
            }
        }
        zoomedMap = ArrayTools.copy(politicalMap);
        return politicalMap;
    }

    /**
     * If the WorldMapGenerator used by
     * {@link #generate(long, WorldMapGenerator, WorldMapGenerator.BiomeMapper, Collection, int, double)} zooms in or
     * out, you can call this method to make the {@link #zoomedMap} 2D char array match its zoom. The world-scale map,
     * {@link #politicalMap}, will remain unchanged unless generate() is called again, but zoomedMap will change each
     * time either generate() or adjustZoom() is called. This method isn't 100% precise on how it places borders; for
     * aesthetic reasons, the borders are tattered with {@link GreasedRegion#fray(double)} so they don't look like a
     * wall of angular bubbles. Using fray() at each level of zoom is quasi-random, so if you zoom in on the same
     * sequence of points on two different occasions, the change from fray() will be the same, but it may be slightly
     * different if any point of zoom is different.
     * @return a direct reference to {@link #zoomedMap}, which will hold the correctly-zoomed version of {@link #politicalMap}
     */
    public char[][] adjustZoom() {
        if(wmg.zoom <= 0)
            return zoomedMap;
        ArrayTools.fill(zoomedMap, '~');
        GreasedRegion nation = new GreasedRegion(width, height);
        char c;
        int stx = Math.min(Math.max((wmg.zoomStartX - (width  >> 1)) / ((2 << wmg.zoom) - 2), 0), width ),
                sty = Math.min(Math.max((wmg.zoomStartY - (height >> 1)) / ((2 << wmg.zoom) - 2), 0), height);
        for (int i = 2; i < atlas.size(); i++) {
            nation.refill(politicalMap, c = atlas.keyAt(i));
            if(nation.isEmpty()) continue;
            for (int z = 1; z <= wmg.zoom; z++) {
                nation.zoom(stx, sty).expand8way().expand().fray(0.5);
            }
            nation.intoChars(zoomedMap, c);
        }
        nation.refill(wmg.heightCodeData, 4, 999).and(new GreasedRegion(zoomedMap, '~')).intoChars(zoomedMap, '%');
        nation.refill(wmg.heightCodeData, -999, 4).intoChars(zoomedMap, '~');
        return zoomedMap;
    }

}
