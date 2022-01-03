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

package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;

import java.io.Serializable;

/**
 * A small class to store the area that a creature is perceived by other creatures to threaten.
 * Created by Tommy Ettinger on 11/8/2015.
 */
public class Threat implements Serializable {
    private static final long serialVersionUID = 1L;
    public Coord position;
    public Reach reach;

    public Threat(Coord position, int maxThreatDistance) {
        this.position = position;
        reach = new Reach(maxThreatDistance);
    }

    public Threat(Coord position, int minThreatDistance, int maxThreatDistance) {
        this.position = position;
        reach = new Reach(minThreatDistance, maxThreatDistance);
    }
    public Threat(Coord position, int minThreatDistance, int maxThreatDistance, Radius measurement) {
        this.position = position;
        reach = new Reach(minThreatDistance, maxThreatDistance, measurement);
    }
    public Threat(Coord position, int minThreatDistance, int maxThreatDistance, Radius measurement, AimLimit limits) {
        this.position = position;
        reach = new Reach(minThreatDistance, maxThreatDistance, measurement, limits);
    }
}
