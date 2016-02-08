package squidpony.examples;

import squidpony.squidgrid.SoundMap;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tommy Ettinger on 4/25/2015.
 */
public class SoundTest {
    public static void main(String[] args) {
        for (SoundMap.Measurement m : SoundMap.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x57a8deadbeef0ffal);
            RNG rng = new RNG(lrng);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            char[][] dun = dg.generate();
            SoundMap audio = new SoundMap(dun, m);

            System.out.println(dg);

            Set<Coord> dudes = new HashSet<>(5);
            dudes.add(dg.utility.randomFloor(dun));
            dudes.add(dg.utility.randomFloor(dun));
            dudes.add(dg.utility.randomFloor(dun));
            dudes.add(dg.utility.randomFloor(dun));
            dudes.add(dg.utility.randomFloor(dun));

            Map<Coord, Double> noises = new HashMap<>(8);
            for(int i = 0; i < 6; i++)
            {
                noises.put(dg.utility.randomStep(dun, dg.utility.randomFloor(dun),
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
            for (Map.Entry<Coord, Double> snd : audio.sounds.entrySet()) {
                md[snd.getKey().x * 2][snd.getKey().y] = '!';
            }

            for (Map.Entry<Coord, Double> guy : audio.alerted.entrySet()) {
                    md[guy.getKey().x * 2][guy.getKey().y] = (String.format("%-2d", (int) Math.floor(guy.getValue()))).charAt(0);
                    md[guy.getKey().x * 2 + 1][guy.getKey().y] = (String.format("%-2d", (int) Math.floor(guy.getValue()))).charAt(1);
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


        }
    }
}
