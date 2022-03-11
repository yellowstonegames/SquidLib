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

import squidpony.annotation.Beta;

/**
 * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, 5, or 6 dimensions and
 * a long for state; like {@link IntPointHash} in most respects, but uses longs. Marked Beta, may change!
 * <br>
 * This implements {@link IPointHash} and has a long it uses internally for state, exposed by {@link #getState()}.
 */
@Beta
public class HappyPointHash extends IPointHash.LongImpl {

    public static final HappyPointHash INSTANCE = new HappyPointHash();

    public HappyPointHash() {
        super();
    }

    public HappyPointHash(int state) {
        super(state);
    }

    public HappyPointHash(long state) {
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
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y point with the given state
     */
    public static long hashAll(long x, long y, long s) {
        s ^= x * 0x8CB92BA72F3D8DD7L ^ y * 0xABC98388FB8FAC03L;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }
//        return (s = ((s = ((s = (s ^ s >>> 32) * 0xBEA225F9EB34556DL) ^ s >>> 29) * 0xBEA225F9EB34556DL) ^ s >>> 32) * 0xBEA225F9EB34556DL) ^ s >>> 29;

    /**
     * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y,z point with the given state
     */
    public static long hashAll(long x, long y, long z, long s) {
        s ^= x * 0x89E182857D9ED689L ^ y * 0xA0F2EC75A1FE1575L ^ z * 0xBBE0563303A4615FL;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long s) {
        s ^= x * 0x881403B9339BD42DL ^ y * 0x9A69443F36F710E7L ^ z * 0xAF36D01EF7518DBBL ^ w * 0xC6D1D6C8ED0C9631L;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * Gets a 64-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w,u point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long s) {
        s ^= x * 0x86D516E50B04AB1BL ^ y * 0x9609C71EB7D03F7BL ^ z * 0xA6F5777F6F88983FL ^ w * 0xB9C9AA3A51D00B65L ^ u * 0xCEBD76D9EDB6A8EFL;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
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
        s ^= x * 0x85EB75C3024385C3L ^ y * 0x92E852C80D153DB3L ^ z * 0xA127A31C56D1CDB5L ^ w * 0xB0C8AC50F0EDEF5DL ^ u * 0xC1EDBC5B5C68AC25L ^ v * 0xD4BC74E13F3C782FL;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }
    /**
     * Gets an 8-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y point with the given state
     */
    public static int hash256(long x, long y, long s) {
        s ^= x * 0x8CB92BA72F3D8DD7L ^ y * 0xABC98388FB8FAC03L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y,z point with the given state
     */
    public static int hash256(long x, long y, long z, long s) {
        s ^= x * 0x89E182857D9ED689L ^ y * 0xA0F2EC75A1FE1575L ^ z * 0xBBE0563303A4615FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long s) {
        s ^= x * 0x881403B9339BD42DL ^ y * 0x9A69443F36F710E7L ^ z * 0xAF36D01EF7518DBBL ^ w * 0xC6D1D6C8ED0C9631L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }
    
    /**
     * Gets an 8-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long u, long s) {
        s ^= x * 0x86D516E50B04AB1BL ^ y * 0x9609C71EB7D03F7BL ^ z * 0xA6F5777F6F88983FL ^ w * 0xB9C9AA3A51D00B65L ^ u * 0xCEBD76D9EDB6A8EFL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
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
        s ^= x * 0x85EB75C3024385C3L ^ y * 0x92E852C80D153DB3L ^ z * 0xA127A31C56D1CDB5L ^ w * 0xB0C8AC50F0EDEF5DL ^ u * 0xC1EDBC5B5C68AC25L ^ v * 0xD4BC74E13F3C782FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }
    /**
     * Gets a 5-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y point with the given state
     */
    public static int hash32(long x, long y, long s) {
        s ^= x * 0x8CB92BA72F3D8DD7L ^ y * 0xABC98388FB8FAC03L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y,z point with the given state
     */
    public static int hash32(long x, long y, long z, long s) {
        s ^= x * 0x89E182857D9ED689L ^ y * 0xA0F2EC75A1FE1575L ^ z * 0xBBE0563303A4615FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long s) {
        s ^= x * 0x881403B9339BD42DL ^ y * 0x9A69443F36F710E7L ^ z * 0xAF36D01EF7518DBBL ^ w * 0xC6D1D6C8ED0C9631L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long u, long s) {
        s ^= x * 0x86D516E50B04AB1BL ^ y * 0x9609C71EB7D03F7BL ^ z * 0xA6F5777F6F88983FL ^ w * 0xB9C9AA3A51D00B65L ^ u * 0xCEBD76D9EDB6A8EFL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
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
        s ^= x * 0x85EB75C3024385C3L ^ y * 0x92E852C80D153DB3L ^ z * 0xA127A31C56D1CDB5L ^ w * 0xB0C8AC50F0EDEF5DL ^ u * 0xC1EDBC5B5C68AC25L ^ v * 0xD4BC74E13F3C782FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 59);
    }

    /**
     * Gets a 6-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 6-bit hash of the x,y point with the given state
     */
    public static int hash64(long x, long y, long s) {
        s ^= x * 0x8CB92BA72F3D8DD7L ^ y * 0xABC98388FB8FAC03L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 6-bit hash of the x,y,z point with the given state
     */
    public static int hash64(long x, long y, long z, long s) {
        s ^= x * 0x89E182857D9ED689L ^ y * 0xA0F2EC75A1FE1575L ^ z * 0xBBE0563303A4615FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 6-bit hash of the x,y,z,w point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long s) {
        s ^= x * 0x881403B9339BD42DL ^ y * 0x9A69443F36F710E7L ^ z * 0xAF36D01EF7518DBBL ^ w * 0xC6D1D6C8ED0C9631L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 6-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long u, long s) {
        s ^= x * 0x86D516E50B04AB1BL ^ y * 0x9609C71EB7D03F7BL ^ z * 0xA6F5777F6F88983FL ^ w * 0xB9C9AA3A51D00B65L ^ u * 0xCEBD76D9EDB6A8EFL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
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
        s ^= x * 0x85EB75C3024385C3L ^ y * 0x92E852C80D153DB3L ^ z * 0xA127A31C56D1CDB5L ^ w * 0xB0C8AC50F0EDEF5DL ^ u * 0xC1EDBC5B5C68AC25L ^ v * 0xD4BC74E13F3C782FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 58);
    }
}
