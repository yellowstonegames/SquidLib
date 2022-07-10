package squidpony.squidmath;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;

public class FOVSymmetryDemo {

    public static void main(String[] args) {
        RNG random = new RNG(0xB0BAFE77);
        DungeonGenerator gen = new DungeonGenerator(70, 70, random);
        gen.addBoulders(8);
        char[][] dungeon = gen.generate();
        System.out.println(gen);
        double[][] res = DungeonUtility.generateSimpleResistances(dungeon);
        double[][] light = new double[gen.getWidth()][gen.getHeight()];
        GreasedRegion floorRegion = new GreasedRegion(dungeon, '.'), working = floorRegion.copy();
        Coord[] floors = floorRegion.asCoords();
//        for (int i = 0; i < floors.length; i++) {
//            Coord center = floors[i];
//            for (int j = 0; j < floors.length; j++) {
//                if(i == j) continue;
//                Coord target = floors[j];
//                if(los.isReachable(res, center.x, center.y, target.x, target.y) != los.isReachable(res, target.x, target.y, center.x, center.y))
//                    System.out.println("Asymmetry between " + center + " and " + target);
//            }
//        }
        for (int i = 0; i < floors.length; i++) {
            Coord center = floors[i];
            FOV.reuseFOVSymmetrical(res, light, center.x, center.y, 7f, Radius.CIRCLE);
            working.refill(light, 0.001f, 100f).and(floorRegion);
            for(Coord c : working) {
                if(FOV.reuseFOVSymmetrical(res, light, c.x, c.y, 7f, Radius.CIRCLE)[center.x][center.y] <= 0f)
                {
                    System.out.println(working);
                    System.out.println("Not symmetrical between " + center + " and " + c + "!");
                    System.out.println(floorRegion.refill(light, 0.001f, 100f));
                    return;
                }
            }
        }
//        LOS los = new LOS(LOS.RAY);
//        for (int i = 0; i < floors.length; i++) {
//            Coord center = floors[i];
//            FOV.reuseFOVLinear(res, light, center.x, center.y, 7f, Radius.CIRCLE, los);
//            working.refill(light, 0.001f, 100f).and(floorRegion);
//            for(Coord c : working) {
//                if(FOV.reuseFOVLinear(res, light, c.x, c.y, 7f, Radius.CIRCLE, los)[center.x][center.y] <= 0f)
//                {
//                    System.out.println(working);
//                    System.out.println("Not symmetrical between " + center + " and " + c + "!");
//                    System.out.println(floorRegion.refill(light, 0.001f, 100f));
//                    return;
//                }
//            }
//        }
    }
}
