package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidgrid.SoundMap;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Tommy Ettinger on 4/25/2015.
 */
public class SoundTest {
    public static void main(String[] args) {
        for (SoundMap.Measurement m : SoundMap.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x57a8deadbeef0ffal);
            RNG rng = new RNG(lrng);
            DungeonUtility.rng = rng;
            DungeonBoneGen dg = new DungeonBoneGen(rng);

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            char[][] dun = dg.getDungeon();
            SoundMap audio = new SoundMap(dun, m);

            System.out.println(dg);

            HashSet<Point> dudes = new HashSet<Point>(5);
            dudes.add(DungeonUtility.randomFloor(dun));
            dudes.add(DungeonUtility.randomFloor(dun));
            dudes.add(DungeonUtility.randomFloor(dun));
            dudes.add(DungeonUtility.randomFloor(dun));
            dudes.add(DungeonUtility.randomFloor(dun));

            HashMap<Point, Double> noises = new HashMap<Point, Double>(8);
            for(int i = 0; i < 6; i++)
            {
                noises.put(DungeonUtility.randomStep(dun, DungeonUtility.randomFloor(dun),
                        (m == SoundMap.Measurement.CHEBYSHEV)), rng.nextDouble(10.0) + 1.0 + i);
            }

            audio.findAlerted(dudes, noises);
            double[][] gm = audio.gradientMap;
            char[][] md = DungeonUtility.doubleWidth(dun),
                    hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (gm[x / 2][y] == SoundMap.WALL)
                        t = hl[x][y];
                    else if (x % 2 == 0 && gm[x / 2][y] == 0)
                        t = '.';
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            int i = 1;
            for (Map.Entry<Point, Double> snd : audio.sounds.entrySet()) {
                md[snd.getKey().x * 2][snd.getKey().y] = '!';
            }

            for (Map.Entry<Point, Double> guy : audio.alerted.entrySet()) {
                    md[guy.getKey().x * 2][guy.getKey().y] = (String.format("%-2d", (int) Math.floor(guy.getValue()))).charAt(0);
                    md[guy.getKey().x * 2 + 1][guy.getKey().y] = (String.format("%-2d", (int) Math.floor(guy.getValue()))).charAt(1);
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


        }
    }
}
