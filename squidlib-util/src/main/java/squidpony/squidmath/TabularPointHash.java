/*
 * Copyright (c) 2022  Eben Howard, Tommy Ettinger, and contributors
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
 *
 *
 */

package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Just another experiment with precomputed point hashes. Nothing too useful here yet.
 */
@Beta
public class TabularPointHash extends IPointHash.IntImpl {
    private final int[] table;
    private final FourWheelRNG random;
    public TabularPointHash() {
        this(42);
    }

    public TabularPointHash(int state) {
        super(state);
        table = new int[0x20000];
        random = new FourWheelRNG(state);
        for (int i = 0; i < table.length; i++) {
            long r = random.nextLong();
            table[i] = (int) r;
            table[++i] = (int) (r >>> 32);
        }
    }

    @Override
    public void setState(int state) {
        super.setState(state);
        random.setSeed(state);
        for (int i = 0; i < table.length; i++) {
            long r = random.nextLong();
            table[i] = (int) r;
            table[++i] = (int) (r >>> 32);
        }
    }

    @Override
    public void setState(long state) {
        super.setState(state);
        random.setSeed(state);
        for (int i = 0; i < table.length; i++) {
            long r = random.nextLong();
            table[i] = (int) r;
            table[++i] = (int) (r >>> 32);
        }
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return table[table[x + state & 0x1FFFF] + y & 0x1FFFF] ^ state;
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return table[table[table[x + state & 0x1FFFF] + y & 0x1FFFF] + z & 0x1FFFF] ^ state;
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return table[table[table[table[x + state & 0x1FFFF] + y & 0x1FFFF] + z & 0x1FFFF] + w & 0x1FFFF] ^ state;
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return table[table[table[table[table[x + state & 0x1FFFF] + y & 0x1FFFF] + z & 0x1FFFF] + w & 0x1FFFF] + u & 0x1FFFF] ^ state;
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return table[table[table[table[table[table[x + state & 0x1FFFF] + y & 0x1FFFF] + z & 0x1FFFF] + w & 0x1FFFF] + u & 0x1FFFF] + v & 0x1FFFF] ^ state;
    }
}
