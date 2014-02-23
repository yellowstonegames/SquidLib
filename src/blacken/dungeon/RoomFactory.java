/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.googlecode.blacken.dungeon;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.blacken.core.Random;
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.Regionlike;

/**
 *
 * @param <T> type this room can contain
 * @author Steven Black
 */
public class RoomFactory<T> {
    private class CheckData<T> {
        CheckData(ThingTypeCheck<T> verifier, boolean sizeLimited) {
            this.verifier = verifier;
            this.sizeLimited = sizeLimited;
        }
        public ThingTypeCheck<T> verifier;
        public boolean sizeLimited;
    }
    private Map<String, CheckData<T>> checks;
    private Random rng = Random.getInstance();
    private Map<String, T> config = Collections.emptyMap();

    /**
     * A room factory creating simple single-item rooms.
     * @param simple null to disable type checking
     */
    public RoomFactory(ThingTypeCheck<T> simple) {
        checks = new LinkedHashMap<>(2);
        checks.put("simple", new CheckData(simple, true));
    }

    /**
     * A room factory creating rooms with separate monsters and objects.
     *
     * <p>The implementation is generic, but 'large' items can never coexist
     * on a same square, but 'small' items can (potentially) coexist. In this
     * case 'large' would include terrain features precluding other 'large'
     * things from co-habitating the square.</p>
     *
     * @param large
     * @param small
     * @param havePiles
     */
    public RoomFactory(ThingTypeCheck<T> large, ThingTypeCheck<T> small, boolean havePiles) {
        checks = new LinkedHashMap<>(2);
        checks.put("large", new CheckData(large, true));
        checks.put("small", new CheckData(small, !havePiles));
    }

    /**
     * A room factory creating rooms with separate terrain, monsters and objects.
     *
     * <p>Remember that rooms have no concept of placement within the room.
     * Making sense of what is requested in a room should happen elsewhere.</p>
     *
     * <p>An important note: This may be totally useless due to having no
     * relationship between 'terrain' and 'large' items. Some 'terrain' is
     * incompatible with some 'large' items co-habitating the same square.</p>
     *
     * @param terrain
     * @param large
     * @param small
     * @param havePiles
     */
    public RoomFactory(ThingTypeCheck<T> terrain, ThingTypeCheck<T> large,
            ThingTypeCheck<T> small, boolean havePiles) {
        checks = new LinkedHashMap<>(2);
        checks.put("terrain", new CheckData(terrain, true));
        checks.put("large", new CheckData(large, true));
        checks.put("small", new CheckData(small, !havePiles));
    }

    /**
     * Create a room filling a region.
     * @param region
     * @return
     */
    public Room<T> createRoom(Regionlike region) {
        Room<T> ret = new Room<>(region);
        if (checks != null) {
            for (Entry<String, CheckData<T>> check : checks.entrySet()) {
                ret.assignContainer(check.getKey(), new SimpleContainer(
                        check.getValue().verifier,
                        check.getValue().sizeLimited ? 1 : -1));
                ret.setConfig(config);
                ret.setRandom(rng);
            }
        }
        return ret;
    }

    /**
     * Create a room mostly filling a region
     *
     * <p>This has a lower limit of 3x3 for the room size. It won't shrink
     * rooms by more than this.</p>
     *
     * @param region general region to create the room
     * @param per percentage of original room to change multiplied to 1000 (0-1000)
     * @return
     */
    public Room<T> createApproximateRoom(Regionlike region, int per) {
        Regionlike r = new BoxRegion(region);
        int meddleX = r.getWidth() * 1000 / per;
        int meddleY = r.getHeight() * 1000 / per;
        if (r.getWidth() < 5) {
            meddleX = 0;
        } else if (r.getWidth() - meddleX < 5) {
            meddleX = r.getWidth() - 5;
        }
        if (r.getHeight() < 5) {
            meddleY = 0;
        } else if (r.getHeight() - meddleY < 5) {
            meddleY = r.getHeight() - 5;
        }
        if (meddleY != 0 || meddleX != 0) {
            int shrinkY = meddleY == 0 ? 0 : rng.nextInt(meddleY) + 1;
            int shrinkX = meddleX == 0 ? 0 : rng.nextInt(meddleX) + 1;
            r.setSize(r.getHeight()-shrinkY, r.getWidth()-shrinkX);
            int moveY = shrinkY == 0 ? 0 : rng.nextInt(shrinkY) + 1;
            int moveX = shrinkX == 0 ? 0 : rng.nextInt(shrinkX) + 1;
            if (moveY != 0 || moveX != 0) {
                r.setPosition(r.getY() + moveY, r.getX() + moveX);
            }
        }
        return createRoom(r);
    }

    public void setRandomNumberGenerator(Random randomNumberGenerator) {
        this.rng = randomNumberGenerator;
    }

    public Random getRandomNumberGenerator() {
        return rng;
    }

    public void setConfig(Map<String, T> config) {
        this.config = config;
    }

    public Map<String, T> getConfig() {
        return config;
    }
}
