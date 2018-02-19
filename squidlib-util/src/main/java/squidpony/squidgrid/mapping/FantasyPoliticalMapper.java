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
    public char[][] adjustZoom() {
        if(wmg.zoom <= 0)
            return politicalMap;
        char[][] zoomedMap = ArrayTools.fill('~', width, height);
        GreasedRegion nation = new GreasedRegion(width, height);
        char c;

        for (int i = 2; i < atlas.size(); i++) {
            nation.refill(politicalMap, c = atlas.keyAt(i));
            if(nation.isEmpty()) continue;
            int stx, sty;
//            int stx = Math.min(Math.max((wmg.zoomStartX >> wmg.zoom + 1), 0), width),
//                    sty = Math.min(Math.max((wmg.zoomStartY >> wmg.zoom + 1), 0), height);

            for (int z = 1; z <= wmg.zoom; z++) {
                stx = Math.min(Math.max(wmg.startCacheX.get(z) - wmg.startCacheX.get(z - 1) << z - 1, 0), width); //wmg.startCacheX.get(z - 1)  // - (width >> 2)
                sty = Math.min(Math.max(wmg.startCacheY.get(z) - wmg.startCacheY.get(z - 1) << z - 1, 0), height); //wmg.startCacheY.get(z - 1) // - (height >>2)
//                int stx = Math.min(Math.max((wmg.zoomStartX >> wmg.zoom + 1), 0), width),
//                        sty = Math.min(Math.max((wmg.zoomStartY >> wmg.zoom + 1), 0), height);
//                System.out.printf("z: %d, stx: %d, sty: %d, startCacheX: %s, startCacheY: %s, zoomStartX: %d, zoomStartY: %d\n", z, stx, sty, wmg.startCacheX.toString(), wmg.startCacheY.toString(), wmg.zoomStartX, wmg.zoomStartY);
                nation.zoom(stx, sty).expand8way().expand().fray(0.5);
            }
            nation.intoChars(zoomedMap, c);
        }
        nation.refill(wmg.heightCodeData, 4, 999).and(new GreasedRegion(zoomedMap, '~')).intoChars(zoomedMap, '%');
        nation.refill(wmg.heightCodeData, -999, 4).intoChars(zoomedMap, '~');
//        final int inc = 1 << wmg.zoom;
//        char currentA, currentB, currentC, currentD;
//        UnorderedSet<String> blockedA, blockedB, blockedC, blockedD;
//        String[] names = biomeMapper.getBiomeNameTable();
//        String thisName;
//        int xA, yA, xB, /*yB, xC,*/ yC, xD, yD, wrappedX, wrappedY;
//        for (int x = 0, wx = wmg.startX; x <= width - inc; x += inc, wx++) {
//            for (int y = 0, wy = wmg.startY; y <= height - inc; y += inc, wy++) {
//                zoomedMap[x][y] = currentA = politicalMap[xA = wx][yA = wy];
//                currentB = politicalMap[xB = wmg.wrapX(wx + 1, wy)][yA];
//                currentC = politicalMap[xA][yC = wmg.wrapY(wx, wy + 1)];
//                currentD = politicalMap[xD = wmg.wrapX(wx + 1, wy + 1)][yD = wmg.wrapY(wx + 1, wy + 1)];
//                if (currentA == currentB && currentB == currentC && currentC == currentD) {
//                    for (int ax = 0; ax < inc; ax++) {
//                        for (int ay = 0; ay < inc; ay++) {
//                            zoomedMap[x + ax][y + ay] = (wmg.heightCodeData[wmg.wrapX(x + ax, y + ay)][wmg.wrapY(x + ax, y + ay)] < 4 ? '~' : currentA);
//                        }
//                    }
//                } else {
//                    blockedA = atlas.get(currentA).blockedBiomes;
//                    blockedB = atlas.get(currentB).blockedBiomes;
//                    blockedC = atlas.get(currentC).blockedBiomes;
//                    blockedD = atlas.get(currentD).blockedBiomes;
//                    for (int ax = 0; ax < inc; ax++) {
//                        for (int ay = 0; ay < inc; ay++) {
//                            thisName = names[biomeMapper.getBiomeCode(wrappedX = wmg.wrapX(x + ax, y + ay), wrappedY = wmg.wrapY(x + ax, y + ay))];
//                            if (currentA != '~' && currentA != '%' && !blockedA.contains(thisName))
//                                zoomedMap[x + ax][y + ay] = currentA;
//                            else if (currentB != '~' && currentB != '%' && !blockedB.contains(thisName))
//                                zoomedMap[x + ax][y + ay] = currentB;
//                            else if (currentC != '~' && currentC != '%' && !blockedC.contains(thisName))
//                                zoomedMap[x + ax][y + ay] = currentC;
//                            else if (currentD != '~' && currentD != '%' && !blockedD.contains(thisName))
//                                zoomedMap[x + ax][y + ay] = currentD;
//                            else
//                                zoomedMap[x + ax][y + ay] = (wmg.heightCodeData[wrappedX][wrappedY] < 4 ? '~' : '%');
//                        }
//                    }
//                }
//            }
//        }
        return zoomedMap;
    }

}
