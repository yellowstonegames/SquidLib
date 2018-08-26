package squidpony.examples;

import squidpony.squidgrid.DetailedMimic;
import squidpony.squidgrid.mapping.*;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Tommy Ettinger on 6/9/2016.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DetailedMimicDungeonTest {
    public static int width = 50, height = 50;
    public static void main( String[] args )
    {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);

        rng.setState(2252637788195L);
        SectionDungeonGenerator sdg = new SectionDungeonGenerator(width, height, rng);
        DungeonGenerator dg;
        sdg.addDoors(12, false);
        //sdg.addWater(SectionDungeonGenerator.CAVE, 13);
        sdg.addBoulders(SectionDungeonGenerator.ALL, 13);
        sdg.addWater(SectionDungeonGenerator.CAVE, 14);
        sdg.addGrass(SectionDungeonGenerator.ALL, 7);
        sdg.addLake(12, '£', '¢');
        rng.setState(0xFEEEEEEEEEL);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng, 0.2);
        serpent.putWalledBoxRoomCarvers(4);
        serpent.putWalledRoundRoomCarvers(2);
        serpent.putCaveCarvers(5);
        char[][] map = serpent.generate();
        int[][] env = serpent.getEnvironment();
        map = sdg.generate(map, env);
        //RNG rand = new RNG();
        //sdg.generate(rand.getRandomElement(TilesetType.values()));
        char[][] sdungeon = sdg.getDungeon();

        sdg.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, true)));
        System.out.println(sdg);
        System.out.println("------------------------------------------------------------");
        /*
        sdg.addDoors(30, false);
        //sdg.addWater(SectionDungeonGenerator.CAVE, 13);
        sdg.addBoulders(SectionDungeonGenerator.ALL, 13);
        sdg.addWater(SectionDungeonGenerator.ALL, 10);
        sdg.addGrass(SectionDungeonGenerator.ALL, 7);
        //sdg.addLake(12, '£', '¢');
        rng.setState(0xFEEEEEEEEEL);
        DenseRoomMapGenerator dense = new DenseRoomMapGenerator(width, height, rng);
        char[][] map = dense.generate();
        int[][] env = dense.getEnvironment();
        //map = sdg.generate(map, env);
        //RNG rand = new RNG();
        //sdg.generate(rand.getRandomElement(TilesetType.values()));
        //char[][] sdungeon = sdg.getDungeon();
        char[][] sdungeon = map;

        sdg.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, false)));
        System.out.println(sdg);
        System.out.println("------------------------------------------------------------");
        */
        DetailedMimic dm = new DetailedMimic(null, rng);
        int[] sample = convertCharToInt(map);
        /*
        dm.analyze(sample, width, height, 2, 3, true);
        for (double i = 0.875; i <= 2.0; i+= 0.125) {
            int w = (int)(width * i), h = (int)(height * i);
            dg = new DungeonGenerator(w, h, rng);
            map = convertIntToChar(dm.process(sample, width, height, w, h,
                    2, 3, 0.7, true), w, h);
            DungeonUtility.ensurePath(map, rng, '\t', '#');
            map = DungeonUtility.wallWrap(map);
            sdungeon = dg.generate(map);
            sdg.setDungeon(DungeonUtility.doubleWidth(
                    DungeonUtility.hashesToLines(sdungeon, false)));
            System.out.println(sdg);
            System.out.println("------------------------------------------------------------");
        }
        */
        for (double i = 0.875; i <= 2.0; i+= 0.125) {
            int w = (int)(width * i), h = (int)(height * i);
            dg = new DungeonGenerator(w, h, rng);
            map = convertIntToChar(dm.neoProcess(sample, width, height, w, h,
                    2, 3, true), w, h);
            DungeonUtility.ensurePath(map, rng, '\t', '#');
            map = DungeonUtility.wallWrap(map);
            sdungeon = dg.generate(map);
            sdg.setDungeon(DungeonUtility.doubleWidth(
                    DungeonUtility.hashesToLines(sdungeon, false)));
            System.out.println(sdg);
            System.out.println("------------------------------------------------------------");
        }

    }

    public static int[] convertCharToInt(char[][] map)
    {
        int w = map.length, h = map[0].length;
        int[] result = new int[w * h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                result[x * h + y] = map[x][y];
            }
        }
        return result;
    }

    public static char[][] convertIntToChar(int[] arr, int w, int h)
    {
        char[][] result = new char[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                result[x][y] = (char) arr[x * h + y];
            }
        }
        return result;
    }


    // Generates a preview webpage when given the right images.
    // See the following for generated images with different parameters (scroll down and right to see the map)
    // http://tommyettinger.github.io/home/PixVoxel/dungeon/dungeon.html   (DungeonGenerator)
    // http://tommyettinger.github.io/home/PixVoxel/dungeon/serpent.html   (SerpentMapGenerator)
    // http://tommyettinger.github.io/home/PixVoxel/dungeon/occupied.html  (SectionDungeonGenerator with people)
    // The assets are CC0 licensed (effectively public domain), made by me (Tommy Ettinger). If you intend to use
    // them in a project, maybe ask to see if there's a newer version already made or in progress.
    public static void mainAlt(String[] args ) {
        RNG rng = new RNG(2252637788195L);
        SectionDungeonGenerator dungeonGenerator = new SectionDungeonGenerator(120, 120, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(SectionDungeonGenerator.CAVE, 15);
        dungeonGenerator.addGrass(SectionDungeonGenerator.CAVE, 15);
        dungeonGenerator.addBoulders(SectionDungeonGenerator.ALL, 6);
        dungeonGenerator.addLake(20);
        SerpentMapGenerator serpent = new SerpentMapGenerator(120, 120, rng, 0.1);
        serpent.putWalledBoxRoomCarvers(3);
        serpent.putWalledRoundRoomCarvers(1);
        serpent.putCaveCarvers(6);
        //dungeonGenerator.generate(serpent.generate(), serpent.getEnvironment());
        dungeonGenerator.generate();

        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeonGenerator.getDungeon(), true));
        char[][] iso = dungeonGenerator.getDungeon();
        int[][] water = new int[120][120];
        for (int i = 0; i < 120; i++)
        {
            for (int j = 0; j < 120; j++)
            {
                water[i][j] = -1;
            }
        }
        boolean even = true;
        StringBuilder sb = new StringBuilder(64000);
        String[] people = new String[]{
                "palette0_Hero_Chain_Bow_Iso.gif",
                "palette0_Hero_Chain_Dagger_Iso.gif",
                "palette0_Hero_Chain_Mace_Iso.gif",
                "palette0_Hero_Chain_Staff_Iso.gif",
                "palette0_Hero_Chain_Sword_Iso.gif",
                "palette0_Hero_Leather_Bow_Iso.gif",
                "palette0_Hero_Leather_Dagger_Iso.gif",
                "palette0_Hero_Leather_Mace_Iso.gif",
                "palette0_Hero_Leather_Staff_Iso.gif",
                "palette0_Hero_Leather_Sword_Iso.gif",
                "palette0_Hero_Plate_Bow_Iso.gif",
                "palette0_Hero_Plate_Dagger_Iso.gif",
                "palette0_Hero_Plate_Mace_Iso.gif",
                "palette0_Hero_Plate_Robe_Iso.gif",
                "palette0_Hero_Plate_Staff_Iso.gif",
                "palette0_Hero_Plate_Sword_Iso.gif",
                "palette0_Hero_Robe_Bow_Iso.gif",
                "palette0_Hero_Robe_Dagger_Iso.gif",
                "palette0_Hero_Robe_Staff_Iso.gif",
                "palette0_Hero_Robe_Sword_Iso.gif"
        };
        for(int row = 0, sx = -59, sy = 59; row < 240; ++row, even = (row % 2 == 0), sx += (even) ? 1 : 0, sy += (!even) ? 1 : 0)
        {
            sb.append("<div class=\"row\">\n");
            for(int col = 0; col < 120; col++)
            {
                int x = sx + col;
                int y = sy - col;
                if(x < 0 || y < 0 || x > 119 || y > 119)
                {
                    sb.append("<div class=\"isotile\"></div>\n");
                }
                else
                {
                    switch (iso[x][y])
                    {
                        case '.':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Floor_Huge_face" + rng.nextInt(4) + "_0.png\" />");
                            if(rng.nextInt(120) == 0) {
                                sb.append("<img class=\"ppl\" src=\"dungeon/").append(rng.getRandomElement(people)).append("\" />");
                            }
                            sb.append("</div>\n");
                            break;
                        case '"':
                        case ':':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette47_Grass_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '#':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Boulder_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '~':
                        case ',':
                            water[x][y] = rng.nextInt(16);
                            if(water[x][y-1] > -1) water[x][y] = (water[x][y] & 14) | ((water[x][y-1] & 4) >> 2);
                            if(water[x][y+1] > -1) water[x][y] = (water[x][y] & 11) | ((water[x][y+1] & 1) * 4);
                            if(water[x-1][y] > -1) water[x][y] = (water[x][y] & 7)  | ((water[x-1][y] & 2) * 4);
                            if(water[x+1][y] > -1) water[x][y] = (water[x][y] & 13) | ((water[x+1][y] & 8) >> 2);

                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette0_Water_Huge_face0_" + (Integer.toHexString(water[x][y])) + ".png\" /></div>\n");
                            break;
                        case '│':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
                            break;
                        case '─':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
                            break;

                        case '┌':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face2_0.png\" /></div>\n");
                            break;
                        case '┐':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face3_0.png\" /></div>\n");
                            break;
                        case '└':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face1_0.png\" /></div>\n");
                            break;
                        case '┘':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face0_0.png\" /></div>\n");
                            break;

                        case '┤':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face0_0.png\" /></div>\n");
                            break;
                        case '┴':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face1_0.png\" /></div>\n");
                            break;
                        case '├':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face2_0.png\" /></div>\n");
                            break;
                        case '┬':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face3_0.png\" /></div>\n");
                            break;

                        case '┼':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Cross_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;

                        case '+':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
                            break;
                        case '/':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
                            break;

                        case ' ':
                            sb.append("<div class=\"isotile\"></div>\n");
                            break;
                        default:
                            System.out.println("BAD GLYPH: " + iso[x][y] + " AT X: " + x + ", Y: " + y);
                    }
                }
            }
            sb.append("</div>\n");
        }
        try {
            FileWriter fw = new FileWriter("data.html");
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
