package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.Thesaurus;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.PoliticalMapper;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.*;

/**
 * Created by Tommy Ettinger on 6/26/2017.
 */
public class PoliticalDungeonTest {
    public static void main(String[] args) {
        RNG rng = new RNG(new LongPeriodRNG("Stake a claim to your land in THE DUNGEON!"));
        DungeonGenerator dg = new DungeonGenerator(200, 100, rng);
        char[][] dun = dg.generate(TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS);
        GreasedRegion gr = new GreasedRegion(dun, '.');
        PoliticalMapper pm = new PoliticalMapper(rng);
        OrderedMap<Character, FakeLanguageGen> langs = new OrderedMap<>(44);
        Thesaurus.languages.shuffle(rng);
        for (int i = 0; i < 32 && i < Thesaurus.languages.size(); i++) {
            langs.put(ArrayTools.letterAt(i), Thesaurus.languages.getAt(i));
        }
        char[][] political = pm.generate(gr, langs, 1.0);
        for (int y = 0; y < 100; y++) {
            for (int x = 0; x < 200; x++) {
                if(political[x][y] == '~') political[x][y] = '#';
            }
        }
        DungeonUtility.debugPrint(political);
        System.out.println();
        for (int i = 0; i < pm.atlas.size(); i++) {
            System.out.printf("%s: %s (alternately, %s)\n", pm.atlas.keyAt(i), pm.atlas.getAt(i), pm.briefAtlas.getAt(i));
        }
    }
}
