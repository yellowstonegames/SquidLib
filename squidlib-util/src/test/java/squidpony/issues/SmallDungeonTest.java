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
import squidpony.squidgrid.mapping.*;
import squidpony.squidmath.RNG;

public class SmallDungeonTest {

    public static void main(String[] args) {
        ConnectingMapGenerator gen = new ConnectingMapGenerator(11, 11, 1, 1, new RNG(), 1, 0.5);

        char[][] generated = gen.generate();
        SectionDungeonGenerator dg = new SectionDungeonGenerator(11, 11);
        dg.addStairs();
        generated = dg.generate(generated, gen.getEnvironment());

        DungeonUtility.debugPrint(generated);
    }
}
