/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.issues;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Measurement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.MoonwalkRNG;

import java.util.ArrayList;

import static squidpony.examples.TestConfiguration.PRINTING;

public class TestDijkstraMapBlocking {
    public static void print(char[][] level) {
        if(!PRINTING) return;
        for (int y = level[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < level.length; x++) {
                System.out.print(level[x][y]);
            }
            System.out.println();
        }
    }
    public static void debugPrint(DijkstraMap d)
    {
        if(!PRINTING) return;
        int high = d.height;
        int wide = d.width;
        double[][] dungeon = d.gradientMap;
        StringBuffer sb = new StringBuffer();
        for (int row = high - 1; row >= 0; row--) {
            for (int col = 0; col < wide; col++) {
                sb.append(String.format("%06.0f ", dungeon[col][row]));
            }
            sb.append('\n');
        }
        System.out.println(sb);
    }

    public static void main(String[] args) {
        char[][] map = {
                "#########".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#....#..#".toCharArray(),
                "#...#...#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#########".toCharArray(),
        };
        DijkstraMap dm = new DijkstraMap(map, Measurement.EUCLIDEAN, new MoonwalkRNG(123L));
        dm.setBlockingRequirement(2);
        ArrayList<Coord> path = new ArrayList<>(16);
        dm.setGoal(3, 3);
        dm.partialScan(10, null);
        debugPrint(dm);
        dm.findPathPreScanned(path, Coord.get(5, 5));
        char ch = '0';
        for(Coord c : path) {
            map[c.x][c.y] = ch++;
        }
        print(map);
    }
}
