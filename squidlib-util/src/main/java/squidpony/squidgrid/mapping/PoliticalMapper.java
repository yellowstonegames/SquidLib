package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.Thesaurus;
import squidpony.annotation.Beta;
import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * When you have a world map as produced by {@link WorldMapGenerator} or some other source, you may want to fill it with
 * claims by various nations/factions, possibly procedural or possibly hand-made. This can assign contiguous areas of
 * land to various factions, while ignoring some amount of "wild" land if desired, and keeping oceans unclaimed.
 * The factions can be given procedural names in an atlas that is linked to the chars used by the world map.
 * Uses {@link MultiSpill} internally to produce the semi-random nation shapes. Stores an {@link #atlas} that can be
 * used to figure out what a char in a produced 2D char array means for its claiming nation, a {@link #briefAtlas} that
 * will have short, identifiable parts of generated names corresponding to the same chars as in atlas, and an OrderedMap
 * of {@link #spokenLanguages} that contains any randomly generated languages produced for nations.
 * <a href="https://gist.github.com/tommyettinger/4a16a09bebed8e2fe8473c8ea444a2dd">Example output of a related class</a>.
 */
@Beta
public class PoliticalMapper {
    public int width;
    public int height;
    public StatefulRNG rng;
    public String name;
    public char[][] politicalMap;
    public static final char[] letters = ArrayTools.letterSpan(256);
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that store the full name of nations.
     */
    public final OrderedMap<Character, String> atlas = new OrderedMap<>(32);
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that store the short name of nations.
     */
    public final OrderedMap<Character, String> briefAtlas = new OrderedMap<>(32);
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that store the languages spoken in those
     * nations, which could be user-assigned, unassigned, or randomly-generated.
     */
    public final OrderedMap<Character, List<FakeLanguageGen>> spokenLanguages = new OrderedMap<>(32);

    public PoliticalMapper()
    {
        name = "Permadeath Planet";
        rng = new StatefulRNG(CrossHash.hash64(name));
    }
    /**
     * Constructs a SpillWorldMap using the given world name, and uses the world name as the
     * basis for all future random generation in this object.
     *
     * @param worldName a String name for the world that will be used as a seed for all random generation here
     */
    public PoliticalMapper(String worldName) {
        name = worldName;
        rng = new StatefulRNG(CrossHash.hash64(name));
    }
    /**
     * Constructs a SpillWorldMap using the given world name, and uses the world name as the
     * basis for all future random generation in this object.
     *
     * @param random an RNG to generate the name for the world in a random language, which will also serve as a seed
     */
    public PoliticalMapper(RNG random) {
        this(FakeLanguageGen.SIMPLISH.word(random, true));
    }
    /**
     * Produces a political map for the land stored in the given WorldMapGenerator, with the given number
     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
     * wilderness. This version generates an atlas with the procedural names of all the factions and a
     * mapping to the chars used in the output; the atlas will be in the {@link #atlas} member of this object. For every
     * Character key in atlas, there will be a String value in atlas that is the name of the nation, and for the same
     * key in {@link #spokenLanguages}, there will be a non-empty List of {@link FakeLanguageGen} languages (usually
     * one, sometimes two) that should match any names generated for the nation. Ocean and Wilderness get the default
     * FakeLanguageGen instances "ELF" and "DEMONIC", in case you need languages for those areas for some reason.
     * @param wmg a WorldMapGenerator that has produced a map; this gets the land parts of the map to assign claims to,
     *            including rivers and lakes as part of nations but not oceans
     * @param factionCount the number of factions to have claiming land, cannot be negative or more than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
     */
    public char[][] generate(WorldMapGenerator wmg, int factionCount, double controlledFraction) {
        return generate(new GreasedRegion(wmg.heightCodeData, 4, 999), factionCount, controlledFraction);
    }
    /**
     * Produces a political map for the land stored in the "on" cells of the given GreasedRegion, with the given number
     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
     * wilderness. This version generates an atlas with the procedural names of all the factions and a
     * mapping to the chars used in the output; the atlas will be in the {@link #atlas} member of this object. For every
     * Character key in atlas, there will be a String value in atlas that is the name of the nation, and for the same
     * key in {@link #spokenLanguages}, there will be a non-empty List of {@link FakeLanguageGen} languages (usually
     * one, sometimes two) that should match any names generated for the nation. Ocean and Wilderness get the default
     * FakeLanguageGen instances "ELF" and "DEMONIC", in case you need languages for those areas for some reason.
     * @param land a GreasedRegion that stores "on" cells for land and "off" cells for anything un-claimable, like ocean
     * @param factionCount the number of factions to have claiming land, cannot be negative or more than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
     */
    public char[][] generate(GreasedRegion land, int factionCount, double controlledFraction) {
        factionCount &= 255;
        width = land.width;
        height = land.height;
        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);
        Coord.expandPoolTo(width, height);
        GreasedRegion map = land.copy();
        //Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
        int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
        map.randomScatter(rng, (width + height) / 25, factionCount);

        spreader.initialize(land.toChars());
        OrderedMap<Coord, Double> entries = new OrderedMap<>();
        entries.put(Coord.get(-1, -1), 0.0);
        for (int i = 0; i < factionCount; i++) {
            entries.put(map.nth(i), rng.between(0.5, 1.0));
        }
        spreader.start(entries, controlled, null);
        short[][] sm = spreader.spillMap;
        politicalMap = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                politicalMap[x][y] = (sm[x][y] == -1) ? '~' : (sm[x][y] == 0) ? '%' : letters[(sm[x][y] - 1) & 255];
            }
        }

        atlas.clear();
        briefAtlas.clear();
        spokenLanguages.clear();
        atlas.put('~', "Ocean");
        briefAtlas.put('~', "Ocean");
        spokenLanguages.put('~', Collections.singletonList(FakeLanguageGen.ELF));
        atlas.put('%', "Wilderness");
        briefAtlas.put('%', "Wilderness");
        spokenLanguages.put('%', Collections.singletonList(FakeLanguageGen.DEMONIC));

        if (factionCount > 0) {
            Thesaurus th = new Thesaurus(rng.nextLong());
            th.addKnownCategories();
            for (int i = 0; i < factionCount && i < 256; i++) {
                atlas.put(letters[i], th.makeNationName());
                briefAtlas.put(letters[i], th.latestGenerated);
                if(th.randomLanguages == null || th.randomLanguages.isEmpty())
                    spokenLanguages.put(letters[i], Collections.singletonList(FakeLanguageGen.randomLanguage(rng)));
                else
                    spokenLanguages.put(letters[i], new ArrayList<>(th.randomLanguages));
            }
        }
        return politicalMap;
    }
    /**
     * Produces a political map for the land stored in the given WorldMapGenerator, with the given number
     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
     * wilderness. This version uses an existing atlas and does not assign to {@link #spokenLanguages}; it does not
     * alter the existingAtlas parameter but uses it to determine what should be in this class' {@link #atlas} field.
     * The atlas field will always contain '~' as the first key in its ordering (with value "Ocean" if no value was
     * already assigned in existingAtlas to that key), and '%' as the second key (with value "Wilderness" if not already
     * assigned); later entries will be taken from existingAtlas (not duplicating '~' or '%', but using the rest).
     * @param wmg a WorldMapGenerator that has produced a map; this gets the land parts of the map to assign claims to,
     *            including rivers and lakes as part of nations but not oceans
     * @param existingAtlas a Map (ideally an OrderedMap) of Character keys to be used in the 2D array, to String values
     *                      that are the names of nations; should not have size greater than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
     */
    public char[][] generate(WorldMapGenerator wmg, Map<Character, String> existingAtlas, double controlledFraction) {
        return generate(new GreasedRegion(wmg.heightCodeData, 4, 999), existingAtlas, controlledFraction);
    }
    /**
     * Produces a political map for the land stored in the "on" cells of the given GreasedRegion, with the given number
     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
     * wilderness. This version uses an existing atlas and does not assign to {@link #spokenLanguages}; it does not
     * alter the existingAtlas parameter but uses it to determine what should be in this class' {@link #atlas} field.
     * The atlas field will always contain '~' as the first key in its ordering (with value "Ocean" if no value was
     * already assigned in existingAtlas to that key), and '%' as the second key (with value "Wilderness" if not already
     * assigned); later entries will be taken from existingAtlas (not duplicating '~' or '%', but using the rest).
     * @param land a GreasedRegion that stores "on" cells for land and "off" cells for anything un-claimable, like ocean
     * @param existingAtlas a Map (ideally an OrderedMap) of Character keys to be used in the 2D array, to String values
     *                      that are the names of nations; should not have size greater than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
     */
    public char[][] generate(GreasedRegion land, Map<Character, String> existingAtlas, double controlledFraction) {
        atlas.clear();
        briefAtlas.clear();
        atlas.putAll(existingAtlas);
        if(atlas.getAndMoveToFirst('%') == null)
            atlas.putAndMoveToFirst('%', "Wilderness");
        if(atlas.getAndMoveToFirst('~') == null)
            atlas.putAndMoveToFirst('~', "Ocean");
        int factionCount = atlas.size() - 2;
        briefAtlas.putAll(atlas);
        width = land.width;
        height = land.height;
        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);
        Coord.expandPoolTo(width, height);
        GreasedRegion map = land.copy();
        //Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
        int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
        map.randomScatter(rng, (width + height) / 25, factionCount);

        spreader.initialize(land.toChars());
        OrderedMap<Coord, Double> entries = new OrderedMap<>();
        entries.put(Coord.get(-1, -1), 0.0);
        for (int i = 0; i < factionCount; i++) {
            entries.put(map.nth(i), rng.between(0.5, 1.0));
        }
        spreader.start(entries, controlled, null);
        short[][] sm = spreader.spillMap;
        politicalMap = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                politicalMap[x][y] = (sm[x][y] == -1) ? '~' : (sm[x][y] == 0) ? '%' : atlas.keyAt((sm[x][y] + 1));
            }
        }
        return politicalMap;
    }
    /**
     * Produces a political map for the land stored in the given WorldMapGenerator, with the given number
     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
     * wilderness. This version uses a "recipe for an atlas" instead of a complete atlas; this is an OrderedMap of
     * Character keys to FakeLanguageGen values, where each key represents a faction and each value is the language to
     * use to generate names for that faction. This does assign to {@link #spokenLanguages}, but it doesn't change the
     * actual FakeLanguageGen objects, since they are immutable. It may add some "factions" if not present to represent
     * oceans and unclaimed land. The atlas field will always contain '~' as the first key in its ordering (with value
     * "Ocean" if no value was already assigned in existingAtlas to that key, or a random nation name in the language
     * that was mapped if there is one), and '%' as the second key (with value "Wilderness" if not already assigned, or
     * a similar random nation name if there is one); later entries will be taken from existingAtlas (not duplicating
     * '~' or '%', but using the rest).
     * @param wmg a WorldMapGenerator that has produced a map; this gets the land parts of the map to assign claims to,
     *            including rivers and lakes as part of nations but not oceans
     * @param atlasLanguages an OrderedMap of Character keys to be used in the 2D array, to FakeLanguageGen objects that
     *                       will be used to generate names; should not have size greater than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
     */
    public char[][] generate(WorldMapGenerator wmg, OrderedMap<Character, FakeLanguageGen> atlasLanguages, double controlledFraction) {
        return generate(new GreasedRegion(wmg.heightCodeData, 4, 999), atlasLanguages, controlledFraction);
    }
    /**
     * Produces a political map for the land stored in the "on" cells of the given GreasedRegion, with the given number
     * of factions trying to take land in the world (essentially, nations). The output is a 2D char array where each
     * letter char is tied to a different faction, while '~' is always water, and '%' is always wilderness or unclaimed
     * land. The amount of unclaimed land is determined by the controlledFraction parameter, which will be clamped
     * between 0.0 and 1.0, with higher numbers resulting in more land owned by factions and lower numbers meaning more
     * wilderness. This version uses a "recipe for an atlas" instead of a complete atlas; this is an OrderedMap of
     * Character keys to FakeLanguageGen values, where each key represents a faction and each value is the language to
     * use to generate names for that faction. This does assign to {@link #spokenLanguages}, but it doesn't change the
     * actual FakeLanguageGen objects, since they are immutable. It may add some "factions" if not present to represent
     * oceans and unclaimed land. The atlas field will always contain '~' as the first key in its ordering (with value
     * "Ocean" if no value was already assigned in existingAtlas to that key, or a random nation name in the language
     * that was mapped if there is one), and '%' as the second key (with value "Wilderness" if not already assigned, or
     * a similar random nation name if there is one); later entries will be taken from existingAtlas (not duplicating
     * '~' or '%', but using the rest).
     * @param land a GreasedRegion that stores "on" cells for land and "off" cells for anything un-claimable, like ocean
     * @param atlasLanguages an OrderedMap of Character keys to be used in the 2D array, to FakeLanguageGen objects that
     *                       will be used to generate names; should not have size greater than 255
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where letters represent the claiming faction, '~' is water, and '%' is unclaimed
     */
    public char[][] generate(GreasedRegion land, OrderedMap<Character, FakeLanguageGen> atlasLanguages, double controlledFraction) {
        atlas.clear();
        briefAtlas.clear();
        spokenLanguages.clear();

        Thesaurus th = new Thesaurus(rng.nextLong());
        th.addKnownCategories();
        FakeLanguageGen flg;
        if((flg = atlasLanguages.get('~')) == null) {
            atlas.put('~', "Ocean");
            briefAtlas.put('~', "Ocean");
            spokenLanguages.put('~', Collections.singletonList(FakeLanguageGen.ELF));
        }
        else {
            atlas.put('~', th.makeNationName(flg));
            briefAtlas.put('~', th.latestGenerated);
            spokenLanguages.put('~', Collections.singletonList(flg));
        }
        if((flg = atlasLanguages.get('%')) == null) {
            atlas.put('%', "Wilderness");
            briefAtlas.put('%', "Wilderness");
            spokenLanguages.put('%', Collections.singletonList(FakeLanguageGen.DEMONIC));
        }
        else {
            atlas.put('%', th.makeNationName(flg));
            briefAtlas.put('%', th.latestGenerated);
            spokenLanguages.put('%', Collections.singletonList(flg));
        }

        for (int i = 0; i < atlasLanguages.size() && i < 256; i++) {
            Character c = atlasLanguages.keyAt(i);
            flg = atlasLanguages.getAt(i);
            atlas.put(c, th.makeNationName(flg));
            briefAtlas.put(c, th.latestGenerated);
            spokenLanguages.put(c, Collections.singletonList(flg));
        }
        int factionCount = atlas.size() - 2;
        width = land.width;
        height = land.height;
        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);
        Coord.expandPoolTo(width, height);
        GreasedRegion map = land.copy();
        //Coord[] centers = map.randomSeparated(0.1, rng, factionCount);
        int controlled = (int) (map.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));
        map.randomScatter(rng, (width + height) / 25, factionCount);

        spreader.initialize(land.toChars());
        OrderedMap<Coord, Double> entries = new OrderedMap<>();
        entries.put(Coord.get(-1, -1), 0.0);
        for (int i = 0; i < factionCount; i++) {
            entries.put(map.nth(i), rng.between(0.5, 1.0));
        }
        spreader.start(entries, controlled, null);
        short[][] sm = spreader.spillMap;
        politicalMap = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                politicalMap[x][y] = atlas.keyAt(sm[x][y] + 1);
            }
        }
        return politicalMap;
    }
}
