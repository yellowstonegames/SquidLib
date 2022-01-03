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

/**
 * Graphs and graph algorithms used primarily (but not exclusively) for pathfinding.
 * <br>
 * This package is almost entirely based on <a href="https://github.com/earlygrey/simple-graphs">simple-graphs</a>,
 * and most files in this package share simple-graphs' MIT license (they have the MIT license header). Heuristic.java
 * is partly from gdx-ai, though more of its lines of code are from SquidLib, so it shares the Apache license of both,
 * while CostlyGraph and DefaultGraph are purely SquidLib.
 */
package squidpony.squidai.graph;
