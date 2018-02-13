package squidpony.squidgrid.mapping;

import squidpony.FakeLanguageGen;
import squidpony.Maker;
import squidpony.Thesaurus;
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

    public static class Faction implements Serializable
    {
        private static final long serialVersionUID = 0L;

        public String name, shortName;
        public FakeLanguageGen language;
        public UnorderedSet<String> preferredBiomes;
        public UnorderedSet<String> blockedBiomes;
        public int[] preferredHeight;
        public int[] preferredHeat;
        public int[] preferredMoisture;
        public Faction()
        {
            language = FakeLanguageGen.randomLanguage(
                    (long) ((Math.random() - 0.5) * 0x10000000000000L)
                            ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
            shortName = name = language.word(true);
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        public Faction(FakeLanguageGen language)
        {
            this.language = language;
            shortName = name = language.word(true);
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        public Faction(FakeLanguageGen language, String name)
        {
            this.language = language;
            shortName = this.name = name;
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        public Faction(FakeLanguageGen language, String name, String shortName)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            blockedBiomes = new UnorderedSet<>();
            blockedBiomes.add("Ocean");
        }
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            this.blockedBiomes = new UnorderedSet<>(blockedBiomes);
        }
        public Faction(FakeLanguageGen language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes, int[] preferredHeight)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new UnorderedSet<>(preferredBiomes);
            this.blockedBiomes = new UnorderedSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
        }
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

    public FantasyPoliticalMapper()
    {
        rng = new StatefulRNG();

    }
    /**
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factionCount the number of factions to have claiming land, cannot be negative or more than 253
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, WorldMapGenerator.BiomeMapper biomeMapper,
                                  Collection<Faction> factions, int factionCount, double controlledFraction) {
        rng.setState(seed);
        factionCount = Math.abs(factionCount % 253);
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
        //ArrayList<Coord> spillers = new ArrayList<>(entries); // centers
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
        return politicalMap;
    }
//    /**
//     * Produces a political map for the land stored in the "on" cells of the given GreasedRegion, with the given number
//     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
//     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
//     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
//     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
//     * wilderness. This version generates an atlas with the procedural names of all the factions and a
//     * mapping to the chars used in the output; the atlas will be in the {@link #atlas} member of this object. For every
//     * Character key in atlas, there will be a String value in atlas that is the name of the nation, and for the same
//     * key in {@link #spokenLanguages}, there will be a non-empty List of {@link FakeLanguageGen} languages (usually
//     * one, sometimes two) that should match any names generated for the nation. Ocean and Wilderness get the default
//     * FakeLanguageGen instances "ELF" and "DEMONIC", in case you need languages for those areas for some reason.
//     * @param land a GreasedRegion that stores "on" cells for land and "off" cells for anything un-claimable, like ocean
//     * @param factionCount the number of factions to have claiming land, cannot be negative or more than 255
//     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
//     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
//     */
//    public char[][] generate(GreasedRegion land, int factionCount, double controlledFraction) {
//        factionCount &= 255;
//        width = land.width;
//        height = land.height;
//        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);
//        Coord.expandPoolTo(width, height);
//        GreasedRegion map = land.copy();
//        //Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
//        int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
//        map.randomScatter(rng, (int) (Math.sqrt(width * height) * 0.1 + 0.999), factionCount);
//
//        spreader.initialize(land.toChars());
//        OrderedMap<Coord, Double> entries = new OrderedMap<>();
//        entries.put(Coord.get(-1, -1), 0.0);
//        for (int i = 0; i < factionCount; i++) {
//            entries.put(map.nth(i), rng.between(0.5, 1.0));
//        }
//        spreader.start(entries, controlled, null);
//        short[][] sm = spreader.spillMap;
//        politicalMap = new char[width][height];
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                politicalMap[x][y] = (sm[x][y] == -1) ? '~' : (sm[x][y] == 0) ? '%' : letters[(sm[x][y] - 1) & 255];
//            }
//        }
//
//        atlas.clear();
//        briefAtlas.clear();
//        spokenLanguages.clear();
//        atlas.put('~', "Ocean");
//        briefAtlas.put('~', "Ocean");
//        spokenLanguages.put('~', Collections.singletonList(FakeLanguageGen.ELF));
//        atlas.put('%', "Wilderness");
//        briefAtlas.put('%', "Wilderness");
//        spokenLanguages.put('%', Collections.singletonList(FakeLanguageGen.DEMONIC));
//
//        if (factionCount > 0) {
//            Thesaurus th = new Thesaurus(rng.nextLong());
//            th.addKnownCategories();
//            for (int i = 0; i < factionCount && i < 256; i++) {
//                atlas.put(letters[i], th.makeNationName());
//                briefAtlas.put(letters[i], th.latestGenerated);
//                if(th.randomLanguages == null || th.randomLanguages.isEmpty())
//                    spokenLanguages.put(letters[i], Collections.singletonList(FakeLanguageGen.randomLanguage(rng)));
//                else
//                    spokenLanguages.put(letters[i], new ArrayList<>(th.randomLanguages));
//            }
//        }
//        return politicalMap;
//    }
//    /**
//     * Produces a political map for the land stored in the given WorldMapGenerator, with the given number
//     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
//     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
//     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
//     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
//     * wilderness. This version uses an existing atlas and does not assign to {@link #spokenLanguages}; it does not
//     * alter the existingAtlas parameter but uses it to determine what should be in this class' {@link #atlas} field.
//     * The atlas field will always contain '~' as the first key in its ordering (with value "Ocean" if no value was
//     * already assigned in existingAtlas to that key), and '%' as the second key (with value "Wilderness" if not already
//     * assigned); later entries will be taken from existingAtlas (not duplicating '~' or '%', but using the rest).
//     * @param wmg a WorldMapGenerator that has produced a map; this gets the land parts of the map to assign claims to,
//     *            including rivers and lakes as part of nations but not oceans
//     * @param existingAtlas a Map (ideally an OrderedMap) of Character keys to be used in the 2D array, to String values
//     *                      that are the names of nations; should not have size greater than 255
//     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
//     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
//     */
//    public char[][] generate(WorldMapGenerator wmg, Map<Character, String> existingAtlas, double controlledFraction) {
//        return generate(new GreasedRegion(wmg.heightCodeData, 4, 999), existingAtlas, controlledFraction);
//    }
//    /**
//     * Produces a political map for the land stored in the "on" cells of the given GreasedRegion, with the given number
//     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
//     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
//     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
//     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
//     * wilderness. This version uses an existing atlas and does not assign to {@link #spokenLanguages}; it does not
//     * alter the existingAtlas parameter but uses it to determine what should be in this class' {@link #atlas} field.
//     * The atlas field will always contain '~' as the first key in its ordering (with value "Ocean" if no value was
//     * already assigned in existingAtlas to that key), and '%' as the second key (with value "Wilderness" if not already
//     * assigned); later entries will be taken from existingAtlas (not duplicating '~' or '%', but using the rest).
//     * @param land a GreasedRegion that stores "on" cells for land and "off" cells for anything un-claimable, like ocean
//     * @param existingAtlas a Map (ideally an OrderedMap) of Character keys to be used in the 2D array, to String values
//     *                      that are the names of nations; should not have size greater than 255
//     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
//     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
//     */
//    public char[][] generate(GreasedRegion land, Map<Character, String> existingAtlas, double controlledFraction) {
//        atlas.clear();
//        briefAtlas.clear();
//        atlas.putAll(existingAtlas);
//        if(atlas.getAndMoveToFirst('%') == null)
//            atlas.putAndMoveToFirst('%', "Wilderness");
//        if(atlas.getAndMoveToFirst('~') == null)
//            atlas.putAndMoveToFirst('~', "Ocean");
//        int factionCount = atlas.size() - 2;
//        briefAtlas.putAll(atlas);
//        width = land.width;
//        height = land.height;
//        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);
//        Coord.expandPoolTo(width, height);
//        GreasedRegion map = land.copy();
//        //Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
//        int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
//        map.randomScatter(rng, (width + height) / 25, factionCount);
//
//        spreader.initialize(land.toChars());
//        OrderedMap<Coord, Double> entries = new OrderedMap<>();
//        entries.put(Coord.get(-1, -1), 0.0);
//        for (int i = 0; i < factionCount; i++) {
//            entries.put(map.nth(i), rng.between(0.5, 1.0));
//        }
//        spreader.start(entries, controlled, null);
//        short[][] sm = spreader.spillMap;
//        politicalMap = new char[width][height];
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                politicalMap[x][y] = (sm[x][y] == -1) ? '~' : (sm[x][y] == 0) ? '%' : atlas.keyAt((sm[x][y] + 1));
//            }
//        }
//        return politicalMap;
//    }
//    /**
//     * Produces a political map for the land stored in the given WorldMapGenerator, with the given number
//     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
//     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
//     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
//     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
//     * wilderness. This version uses a "recipe for an atlas" instead of a complete atlas; this is an OrderedMap of
//     * Character keys to FakeLanguageGen values, where each key represents a faction and each value is the language to
//     * use to generate names for that faction. This does assign to {@link #spokenLanguages}, but it doesn't change the
//     * actual FakeLanguageGen objects, since they are immutable. It may add some "factions" if not present to represent
//     * oceans and unclaimed land. The atlas field will always contain '~' as the first key in its ordering (with value
//     * "Ocean" if no value was already assigned in existingAtlas to that key, or a random nation name in the language
//     * that was mapped if there is one), and '%' as the second key (with value "Wilderness" if not already assigned, or
//     * a similar random nation name if there is one); later entries will be taken from existingAtlas (not duplicating
//     * '~' or '%', but using the rest).
//     * @param wmg a WorldMapGenerator that has produced a map; this gets the land parts of the map to assign claims to,
//     *            including rivers and lakes as part of nations but not oceans
//     * @param atlasLanguages an OrderedMap of Character keys to be used in the 2D array, to FakeLanguageGen objects that
//     *                       will be used to generate names; should not have size greater than 255
//     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
//     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
//     */
//    public char[][] generate(WorldMapGenerator wmg, OrderedMap<Character, FakeLanguageGen> atlasLanguages, double controlledFraction) {
//        return generate(new GreasedRegion(wmg.heightCodeData, 4, 999), atlasLanguages, controlledFraction);
//    }
//    /**
//     * Produces a political map for the land stored in the "on" cells of the given GreasedRegion, with the given number
//     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
//     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
//     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
//     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
//     * wilderness. This version uses a "recipe for an atlas" instead of a complete atlas; this is an OrderedMap of
//     * Character keys to FakeLanguageGen values, where each key represents a faction and each value is the language to
//     * use to generate names for that faction. This does assign to {@link #spokenLanguages}, but it doesn't change the
//     * actual FakeLanguageGen objects, since they are immutable. It may add some "factions" if not present to represent
//     * oceans and unclaimed land. The atlas field will always contain '~' as the first key in its ordering (with value
//     * "Ocean" if no value was already assigned in existingAtlas to that key, or a random nation name in the language
//     * that was mapped if there is one), and '%' as the second key (with value "Wilderness" if not already assigned, or
//     * a similar random nation name if there is one); later entries will be taken from existingAtlas (not duplicating
//     * '~' or '%', but using the rest).
//     * @param land a GreasedRegion that stores "on" cells for land and "off" cells for anything un-claimable, like ocean
//     * @param atlasLanguages an OrderedMap of Character keys to be used in the 2D array, to FakeLanguageGen objects that
//     *                       will be used to generate names; should not have size greater than 255
//     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
//     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
//     */
//    public char[][] generate(GreasedRegion land, OrderedMap<Character, FakeLanguageGen> atlasLanguages, double controlledFraction) {
//        atlas.clear();
//        briefAtlas.clear();
//        spokenLanguages.clear();
//
//        Thesaurus th = new Thesaurus(rng.nextLong());
//        th.addKnownCategories();
//        FakeLanguageGen flg;
//        if((flg = atlasLanguages.get('~')) == null) {
//            atlas.put('~', "Ocean");
//            briefAtlas.put('~', "Ocean");
//            spokenLanguages.put('~', Collections.singletonList(FakeLanguageGen.ELF));
//        }
//        else {
//            atlas.put('~', th.makeNationName(flg));
//            briefAtlas.put('~', th.latestGenerated);
//            spokenLanguages.put('~', Collections.singletonList(flg));
//        }
//        if((flg = atlasLanguages.get('%')) == null) {
//            atlas.put('%', "Wilderness");
//            briefAtlas.put('%', "Wilderness");
//            spokenLanguages.put('%', Collections.singletonList(FakeLanguageGen.DEMONIC));
//        }
//        else {
//            atlas.put('%', th.makeNationName(flg));
//            briefAtlas.put('%', th.latestGenerated);
//            spokenLanguages.put('%', Collections.singletonList(flg));
//        }
//
//        for (int i = 0; i < atlasLanguages.size() && i < 256; i++) {
//            Character c = atlasLanguages.keyAt(i);
//            flg = atlasLanguages.getAt(i);
//            atlas.put(c, th.makeNationName(flg));
//            briefAtlas.put(c, th.latestGenerated);
//            spokenLanguages.put(c, Collections.singletonList(flg));
//        }
//        int factionCount = atlas.size() - 2;
//        width = land.width;
//        height = land.height;
//        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);
//        Coord.expandPoolTo(width, height);
//        GreasedRegion map = land.copy();
//        //Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
//        int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
//        map.randomScatter(rng, (width + height) / 25, factionCount);
//
//        spreader.initialize(land.toChars());
//        OrderedMap<Coord, Double> entries = new OrderedMap<>();
//        entries.put(Coord.get(-1, -1), 0.0);
//        for (int i = 0; i < factionCount; i++) {
//            entries.put(map.nth(i), rng.between(0.5, 1.0));
//        }
//        spreader.start(entries, controlled, null);
//        short[][] sm = spreader.spillMap;
//        politicalMap = new char[width][height];
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                politicalMap[x][y] = atlas.keyAt(sm[x][y] + 1);
//            }
//        }
//        return politicalMap;
//    }
}
