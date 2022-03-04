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

package squidpony.squidmath;

/**
 * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, 5, or 6 dimensions and
 * a long for state; this is mostly meant as an optimization of {@link HastyPointHash}. This implementation has
 * high enough quality to be useful as a source of random numbers based on positions. You
 * can also consider {@link IntPointHash} if your input and output types are usually int, since it's often faster.
 * <br>
 * This implements {@link IPointHash} and has a long it uses internally for state, exposed by {@link #getState()}.
 */
public class HushPointHash extends IPointHash.LongImpl {

    public static final HushPointHash INSTANCE = new HushPointHash();

    public HushPointHash() {
        super();
    }

    public HushPointHash(int state) {
        super(state);
    }

    public HushPointHash(long state) {
        super(state);
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return (int)hashAll(x, y, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return (int)hashAll(x, y, z, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return (int)hashAll(x, y, z, w, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return (int)hashAll(x, y, z, w, u, state);
    }
    
    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return (int)hashAll(x, y, z, w, u, v, state);
    }
    
    public long getState(){
        return state;
    }

    /**
     * Gets a 64-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y point with the given state
     */
    public static long hashAll(long x, long y, long s) {
        s += (y + s + (x + s + 0xD1B54A32D192ED03L) * 0xABC98388FB8FAC03L) * 0x8CB92BA72F3D8DD7L;
        return ((s = (s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L) ^ s >>> 53);
    }

    /**
     * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y,z point with the given state
     */
    public static long hashAll(long x, long y, long z, long s) {
        s = (z + s + (y + s + (x + s + 0xDB4F0B9175AE2165L) * 0xBBE0563303A4615FL) * 0xA0F2EC75A1FE1575L) * 0x89E182857D9ED689L;
        return ((s = (s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L) ^ s >>> 53);
    }

    /**
     * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long s) {
        s += (w + s + (z + s + (y + s + (x + s + 0xE19B01AA9D42C633L) * 0xC6D1D6C8ED0C9631L) * 0xAF36D01EF7518DBBL) * 0x9A69443F36F710E7L) * 0x881403B9339BD42DL;
        return ((s = (s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L) ^ s >>> 53);
    }

    /**
     * Gets a 64-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w,u point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long s) {
        s = (u + s + (w + s + (z + s + (y + s + (x + s + 0xE60E2B722B53AEEBL) * 0xCEBD76D9EDB6A8EFL) * 0xB9C9AA3A51D00B65L) * 0xA6F5777F6F88983FL) * 0x9609C71EB7D03F7BL) * 0x86D516E50B04AB1BL;
        return ((s = (s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L) ^ s >>> 53);
    }

    /**
     * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long v, long s) {
        s += (v + s + (u + s + (w + s + (z + s + (y + s + (x + s + 0xE95E1DD17D35800DL) * 0xD4BC74E13F3C782FL) * 0xC1EDBC5B5C68AC25L) * 0xB0C8AC50F0EDEF5DL) * 0xA127A31C56D1CDB5L) * 0x92E852C80D153DB3L) * 0x85EB75C3024385C3L;
        return ((s = (s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L) ^ s >>> 53);
    }
    /**
     * Gets an 8-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y point with the given state
     */
    public static int hash256(long x, long y, long s) {
        s += (y + s + (x + s + 0xD1B54A32D192ED03L) * 0xABC98388FB8FAC03L) * 0x8CB92BA72F3D8DD7L;
        return (int)((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y,z point with the given state
     */
    public static int hash256(long x, long y, long z, long s) {
        s = (z + s + (y + s + (x + s + 0xDB4F0B9175AE2165L) * 0xBBE0563303A4615FL) * 0xA0F2EC75A1FE1575L) * 0x89E182857D9ED689L;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long s) {
        s += (w + s + (z + s + (y + s + (x + s + 0xE19B01AA9D42C633L) * 0xC6D1D6C8ED0C9631L) * 0xAF36D01EF7518DBBL) * 0x9A69443F36F710E7L) * 0x881403B9339BD42DL;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 53);
    }


    /**
     * Gets an 8-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long u, long s) {
        s = (u + s + (w + s + (z + s + (y + s + (x + s + 0xE60E2B722B53AEEBL) * 0xCEBD76D9EDB6A8EFL) * 0xB9C9AA3A51D00B65L) * 0xA6F5777F6F88983FL) * 0x9609C71EB7D03F7BL) * 0x86D516E50B04AB1BL;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long u, long v, long s) {
        s += (v + s + (u + s + (w + s + (z + s + (y + s + (x + s + 0xE95E1DD17D35800DL) * 0xD4BC74E13F3C782FL) * 0xC1EDBC5B5C68AC25L) * 0xB0C8AC50F0EDEF5DL) * 0xA127A31C56D1CDB5L) * 0x92E852C80D153DB3L) * 0x85EB75C3024385C3L;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 56);
    }
    /**
     * Gets a 5-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y point with the given state
     */
    public static int hash32(long x, long y, long s) {
        s += (y + s + (x + s + 0xD1B54A32D192ED03L) * 0xABC98388FB8FAC03L) * 0x8CB92BA72F3D8DD7L;
        return (int)((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y,z point with the given state
     */
    public static int hash32(long x, long y, long z, long s) {
        s = (z + s + (y + s + (x + s + 0xDB4F0B9175AE2165L) * 0xBBE0563303A4615FL) * 0xA0F2EC75A1FE1575L) * 0x89E182857D9ED689L;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long s) {
        s += (w + s + (z + s + (y + s + (x + s + 0xE19B01AA9D42C633L) * 0xC6D1D6C8ED0C9631L) * 0xAF36D01EF7518DBBL) * 0x9A69443F36F710E7L) * 0x881403B9339BD42DL;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 59);
    }

    /**
     * Gets an 5-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long u, long s) {
        s = (u + s + (w + s + (z + s + (y + s + (x + s + 0xE60E2B722B53AEEBL) * 0xCEBD76D9EDB6A8EFL) * 0xB9C9AA3A51D00B65L) * 0xA6F5777F6F88983FL) * 0x9609C71EB7D03F7BL) * 0x86D516E50B04AB1BL;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long u, long v, long s) {
        s += (v + s + (u + s + (w + s + (z + s + (y + s + (x + s + 0xE95E1DD17D35800DL) * 0xD4BC74E13F3C782FL) * 0xC1EDBC5B5C68AC25L) * 0xB0C8AC50F0EDEF5DL) * 0xA127A31C56D1CDB5L) * 0x92E852C80D153DB3L) * 0x85EB75C3024385C3L;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 59);
    }
    
    /**
     * Gets a 6-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 6-bit hash of the x,y point with the given state
     */
    public static int hash64(long x, long y, long s) {
        s += (y + s + (x + s + 0xD1B54A32D192ED03L) * 0xABC98388FB8FAC03L) * 0x8CB92BA72F3D8DD7L;
        return (int)((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 6-bit hash of the x,y,z point with the given state
     */
    public static int hash64(long x, long y, long z, long s) {
        s = (z + s + (y + s + (x + s + 0xDB4F0B9175AE2165L) * 0xBBE0563303A4615FL) * 0xA0F2EC75A1FE1575L) * 0x89E182857D9ED689L;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 6-bit hash of the x,y,z,w point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long s) {
        s += (w + s + (z + s + (y + s + (x + s + 0xE19B01AA9D42C633L) * 0xC6D1D6C8ED0C9631L) * 0xAF36D01EF7518DBBL) * 0x9A69443F36F710E7L) * 0x881403B9339BD42DL;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 58);
    }

    /**
     * Gets an 5-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long u, long s) {
        s = (u + s + (w + s + (z + s + (y + s + (x + s + 0xE60E2B722B53AEEBL) * 0xCEBD76D9EDB6A8EFL) * 0xB9C9AA3A51D00B65L) * 0xA6F5777F6F88983FL) * 0x9609C71EB7D03F7BL) * 0x86D516E50B04AB1BL;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 6-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long u, long v, long s) {
        s += (v + s + (u + s + (w + s + (z + s + (y + s + (x + s + 0xE95E1DD17D35800DL) * 0xD4BC74E13F3C782FL) * 0xC1EDBC5B5C68AC25L) * 0xB0C8AC50F0EDEF5DL) * 0xA127A31C56D1CDB5L) * 0x92E852C80D153DB3L) * 0x85EB75C3024385C3L;
        return (int) ((s ^ s >>> 20 ^ s >>> 37) * 0xF1357AEA2E62A9C5L >>> 58);
    }
}
