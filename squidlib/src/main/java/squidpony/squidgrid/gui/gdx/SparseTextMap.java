/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.squidmath.HashCommon;
import squidpony.squidmath.NumberTools;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An unordered map where the keys are two positive ints up to 16 bits (x and y, between 0 and 65535) and there are
 * multiple kinds of value per key, here just a char and a float for color. Can be rendered if given a running Batch
 * and a TextCellFactory using {@link #draw(Batch, TextCellFactory, Frustum)}.
 * <br>
 *
 * @author Nathan Sweet
 * @author Tommy Ettinger
 */
public class SparseTextMap implements Iterable<SparseTextMap.Entry> {
    //private static final int PRIME1 = 0xbe1f14b1;
    private static final int PRIME2 = 0xb4b82e39;
    private static final int PRIME3 = 0xced1c241;
    private static final int EMPTY = 0;

    public int size;

    private int[] keyTable;
    private char[] charValueTable;
    private float[] floatValueTable;
    private int capacity, stashSize;
    private char zeroChar;
    private float zeroFloat;
    private boolean hasZeroValue;

    private float loadFactor;
    private int hashShift, mask, threshold;
    private int stashCapacity;
    private int pushIterations;

    private transient Entries entries1, entries2;
    private transient CharValues charValues1, charValues2;
    private transient FloatValues floatValues1, floatValues2;
    private transient Keys keys1, keys2;

    /**
     * Creates a new map with an initial capacity of 51 and a load factor of 0.55.
     */
    public SparseTextMap() {
        this(63, 0.55f);
    }

    /**
     * Creates a new map with a load factor of 0.55.
     *
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public SparseTextMap(int initialCapacity) {
        this(initialCapacity, 0.55f);
    }

    /**
     * Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
     * growing the backing table.
     *
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
     */
    public SparseTextMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
        initialCapacity = HashCommon.nextPowerOfTwo((int) Math.ceil(initialCapacity / loadFactor));
        if (initialCapacity > 1 << 30)
            throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
        capacity = initialCapacity;

        if (loadFactor <= 0) throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
        this.loadFactor = loadFactor;

        threshold = (int) (capacity * loadFactor);
        mask = capacity - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
        stashCapacity = Math.max(3, (int) Math.ceil(Math.log(capacity)) * 2);
        pushIterations = Math.max(Math.min(capacity, 8), (int) Math.sqrt(capacity) / 8);

        keyTable = new int[capacity + stashCapacity];
        charValueTable = new char[keyTable.length];
        floatValueTable = new float[keyTable.length];
    }

    /**
     * Creates a new map identical to the specified map.
     */
    public SparseTextMap(SparseTextMap map) {
        //this((int) Math.floor(map.capacity * map.loadFactor), map.loadFactor);
        capacity = map.capacity;
        loadFactor = map.loadFactor;
        threshold = map.threshold;
        mask = map.mask;
        hashShift = map.hashShift;
        stashCapacity = map.stashCapacity;
        pushIterations = map.pushIterations;
        stashSize = map.stashSize;
        keyTable = new int[map.keyTable.length];
        System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
        charValueTable = new char[map.charValueTable.length];
        System.arraycopy(map.charValueTable, 0, charValueTable, 0, map.charValueTable.length);
        floatValueTable = new float[map.floatValueTable.length];
        System.arraycopy(map.floatValueTable, 0, floatValueTable, 0, map.floatValueTable.length);
        size = map.size;
        zeroChar = map.zeroChar;
        zeroFloat = map.zeroFloat;
        hasZeroValue = map.hasZeroValue;
    }

    /**
     * Draws the contents of this SparseTextMap, using the keys as x,y pairs as they would be entered by calling
     * {@link #place(int, int, char, float)} and drawing the associated char at that x,y position. Uses a
     * {@link Frustum} object, usually obtained from the current Camera with
     * {@link com.badlogic.gdx.graphics.Camera#frustum}, to cull anything that would be drawn out of view. Treats the
     * float value as a color for the char, using the encoding for colors as floats that {@link Color#toFloatBits()}
     * uses. Relies on the sizing information from the given TextCellFactory (its
     * {@link TextCellFactory#actualCellWidth} and {@link TextCellFactory#actualCellHeight}, which may differ from its
     * width and height if either of {@link TextCellFactory#tweakWidth(float)} or
     * {@link TextCellFactory#tweakHeight(float)} were called). It also, of course, uses the TextCellFactory to
     * determine what its text will look like (font, size, and so on). The TextCellFactory must have been initialized,
     * probably with {@link TextCellFactory#initBySize()} after setting the width and height as desired. This method
     * should be called between {@code batch.begin()} and {@code batch.end()} with the Batch passed to this.
     *
     * @param batch       the SpriteBatch or other Batch used to draw this; should have already have had begin() called
     * @param textFactory used to determine the font, size, cell size, and other information; must be initialized
     * @param frustum     a {@link Frustum} object to determine culling, almost always obtained from {@link com.badlogic.gdx.graphics.Camera#frustum}
     */
    public void draw(Batch batch, TextCellFactory textFactory, Frustum frustum) {
        draw(batch, textFactory, frustum, 0f, 0f);
    }
    /**
     * Draws the contents of this SparseTextMap, using the keys as x,y pairs as they would be entered by calling
     * {@link #place(int, int, char, float)} and drawing the associated char at that x,y position, potentially with an
     * offset on x and/or y. Uses a {@link Frustum} object, usually obtained from the current Camera with
     * {@link com.badlogic.gdx.graphics.Camera#frustum}, to cull anything that would be drawn out of view. Treats the
     * float value as a color for the char, using the encoding for colors as floats that {@link Color#toFloatBits()}
     * uses. Relies on the sizing information from the given TextCellFactory (its
     * {@link TextCellFactory#actualCellWidth} and {@link TextCellFactory#actualCellHeight}, which may differ from its
     * width and height if either of {@link TextCellFactory#tweakWidth(float)} or
     * {@link TextCellFactory#tweakHeight(float)} were called). It also, of course, uses the TextCellFactory to
     * determine what its text will look like (font, size, and so on). The TextCellFactory must have been initialized,
     * probably with {@link TextCellFactory#initBySize()} after setting the width and height as desired. This method
     * should be called between {@code batch.begin()} and {@code batch.end()} with the Batch passed to this.
     *
     * @param batch       the SpriteBatch or other Batch used to draw this; should have already have had begin() called
     * @param textFactory used to determine the font, size, cell size, and other information; must be initialized
     * @param frustum     a {@link Frustum} object to determine culling, almost always obtained from {@link com.badlogic.gdx.graphics.Camera#frustum}
     * @param screenOffsetX offset to apply to the x position of each char rendered; positive moves chars right
     * @param screenOffsetY offset to apply to the y position of each char rendered; positive moves chars up
     */
    public void draw(Batch batch, TextCellFactory textFactory, Frustum frustum, float screenOffsetX, float screenOffsetY) {
        textFactory.configureShader(batch);
        final float widthInc = textFactory.actualCellWidth, heightInc = -textFactory.actualCellHeight;
        float x, y;
        int n;
        for (Entry entry : entries()) {
            n = entry.key;
            n ^= n << 26;
            n ^= n >>> 15;
            n ^= n << 17;
            x = (n & 0xFFFF) * widthInc + screenOffsetX;
            y = (n >>> 16) * heightInc + screenOffsetY;
            if(frustum.boundsInFrustum(x, y, 0f, widthInc, -heightInc, 0f))
                textFactory.draw(batch, entry.charValue, entry.floatValue, x, y);
        }
    }
    /**
     * Draws the contents of this SparseTextMap, using the keys as x,y pairs as they would be entered by calling
     * {@link #place(int, int, char, float)} and drawing the char replacement at that x,y position, potentially with an
     * offset on x and/or y. Uses a {@link Frustum} object, usually obtained from the current Camera with
     * {@link com.badlogic.gdx.graphics.Camera#frustum}, to cull anything that would be drawn out of view. Treats the
     * float value as a color for replacement, using the encoding for colors as floats that {@link Color#toFloatBits()}
     * uses. Relies on the sizing information from the given TextCellFactory (its
     * {@link TextCellFactory#actualCellWidth} and {@link TextCellFactory#actualCellHeight}, which may differ from its
     * width and height if either of {@link TextCellFactory#tweakWidth(float)} or
     * {@link TextCellFactory#tweakHeight(float)} were called). It also, of course, uses the TextCellFactory to
     * determine what its text will look like (font, size, and so on). The TextCellFactory must have been initialized,
     * probably with {@link TextCellFactory#initBySize()} after setting the width and height as desired. This method
     * should be called between {@code batch.begin()} and {@code batch.end()} with the Batch passed to this.
     *
     * @param batch       the SpriteBatch or other Batch used to draw this; should have already have had begin() called
     * @param textFactory used to determine the font, size, cell size, and other information; must be initialized
     * @param frustum     a {@link Frustum} object to determine culling, almost always obtained from {@link com.badlogic.gdx.graphics.Camera#frustum}
     * @param screenOffsetX offset to apply to the x position of each char rendered; positive moves chars right
     * @param screenOffsetY offset to apply to the y position of each char rendered; positive moves chars up
     * @param replacement a char that will be used in place of the normal char values stored in this; often the char
     *                    with Unicode value 0, which renders as a solid block.
     */
    public void draw(Batch batch, TextCellFactory textFactory, Frustum frustum, float screenOffsetX, float screenOffsetY, char replacement) {
        textFactory.configureShader(batch);
        final float widthInc = textFactory.actualCellWidth, heightInc = -textFactory.actualCellHeight;
        float x, y;
        int n;
        for (Entry entry : entries()) {
            n = entry.key;
            n ^= n << 26;
            n ^= n >>> 15;
            n ^= n << 17;
//            n =    ((n & 0x22222222) << 1) | ((n >>> 1) & 0x22222222) | (n & 0x99999999);
//            n =    ((n & 0x0c0c0c0c) << 2) | ((n >>> 2) & 0x0c0c0c0c) | (n & 0xc3c3c3c3);
//            n =    ((n & 0x00f000f0) << 4) | ((n >>> 4) & 0x00f000f0) | (n & 0xf00ff00f);
//            n =    ((n & 0x0000ff00) << 8) | ((n >>> 8) & 0x0000ff00) | (n & 0xff0000ff);
            x = (n & 0xFFFF) * widthInc + screenOffsetX;
            y = (n >>> 16) * heightInc + screenOffsetY;
            if(frustum.boundsInFrustum(x, y, 0f, widthInc, -heightInc, 0f)) 
                textFactory.draw(batch, replacement, entry.floatValue, x, y);
        }
    }

    /**
     * Packs the given x,y position into one int in the type that is used elsewhere in this class.
     * This requires x and y to each be between 0 and 65535, both inclusive.
     *
     * @param x the x component to encode; this must be between 0 and 65535, both inclusive
     * @param y the y component to encode; this must be between 0 and 65535, both inclusive
     * @return an encoded form of the x,y pair in the style used elsewhere in this class
     */
    public static int encodePosition(final int x, final int y) {
        int n = (x | y << 16);
        n ^= n << 17;
        n ^= n >>> 15;
        n ^= n >>> 30;
        return n ^ n << 26;
        //return GreasedRegion.interleaveBits(x, y);
    }

    /**
     * Decodes a packed position and gets the x component from it, if it was produced by
     * {@link #encodePosition(int, int)} or {@link #place(int, int, char, float)}.
     *
     * @param encoded a packed position that holds x and y components
     * @return the x component packed in the parameter
     */
    public static int decodeX(int encoded) {
        encoded ^= encoded << 26;
        return (encoded ^ encoded >>> 15) & 0xFFFF;
        //return GreasedRegion.disperseBits(encoded) & 0xFFFF;
    }

    /**
     * Decodes a packed position and gets the y component from it, if it was produced by
     * {@link #encodePosition(int, int)} or {@link #place(int, int, char, float)}.
     *
     * @param encoded a packed position that holds x and y components
     * @return the y component packed in the parameter
     */
    public static int decodeY(int encoded) {
        encoded ^= encoded << 26;
        encoded ^= encoded >>> 15;
        return (encoded ^ encoded << 17) >>> 16;
        //return GreasedRegion.disperseBits(encoded) >>> 16;
    }

    /**
     * Places a char in the given libGDX Color at the requested x, y position in grid cells, where x and y must each be
     * between 0 and 65535, both inclusive. The color can also be an SColor, such as one of the many constants in that
     * class. Returns the int code that can be used to locate the x,y pair as a key in this SparseTextMap.
     *
     * @param x         the x position of the colorful char; this must be between 0 and 65535, both inclusive
     * @param y         the y position of the colorful char; this must be between 0 and 65535, both inclusive
     * @param charValue the char to put into the SparseTextMap
     * @param color     the libGDX Color or SColor to use for the char
     * @return the int that the x,y pair will be stored at, and used as the single key associated with the colorful char
     */
    public int place(int x, int y, char charValue, Color color) {
        return place(x, y, charValue, color.toFloatBits());
    }

    /**
     * Places a char in the given encoded color at the requested x, y position in grid cells, where x and y must each be
     * between 0 and 65535, both inclusive, and the encoded color is one produced by {@link Color#toFloatBits()} or any
     * of several methods in {@link SColor}, such as {@link SColor#floatGetI(int, int, int)},
     * {@link SColor#floatGetHSV(float, float, float, float)}, or {@link SColor#lerpFloatColors(float, float, float)}.
     * Returns the int code that can be used to locate the x,y pair as a key in this SparseTextMap.
     *
     * @param x            the x position of the colorful char; this must be between 0 and 65535, both inclusive
     * @param y            the y position of the colorful char; this must be between 0 and 65535, both inclusive
     * @param charValue    the char to put into the SparseTextMap
     * @param encodedColor the encoded color to use for the char, as produced by {@link Color#toFloatBits()}
     * @return the int that the x,y pair will be stored at, and used as the single key associated with the colorful char
     */
    public int place(int x, int y, char charValue, float encodedColor) {
        int code = encodePosition(x, y);
        put(code, charValue, encodedColor);
        return code;
    }

    public void put(int key, char charValue, float floatValue) {
        if (key == 0) {
            zeroChar = charValue;
            zeroFloat = floatValue;
            if (!hasZeroValue) {
                hasZeroValue = true;
                size++;
            }
            return;
        }

        int[] keyTable = this.keyTable;

        // Check for existing keys.
        int index1 = key & mask;
        int key1 = keyTable[index1];
        if (key == key1) {
            charValueTable[index1] = charValue;
            floatValueTable[index1] = floatValue;
            return;
        }

        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if (key == key2) {
            charValueTable[index2] = charValue;
            floatValueTable[index2] = floatValue;
            return;
        }

        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if (key == key3) {
            charValueTable[index3] = charValue;
            floatValueTable[index3] = floatValue;
            return;
        }

        // Update key in the stash.
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (key == keyTable[i]) {
                charValueTable[i] = charValue;
                floatValueTable[i] = floatValue;
                return;
            }
        }

        // Check for empty buckets.
        if (key1 == EMPTY) {
            keyTable[index1] = key;
            charValueTable[index1] = charValue;
            floatValueTable[index1] = floatValue;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        if (key2 == EMPTY) {
            keyTable[index2] = key;
            charValueTable[index2] = charValue;
            floatValueTable[index2] = floatValue;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        if (key3 == EMPTY) {
            keyTable[index3] = key;
            charValueTable[index3] = charValue;
            floatValueTable[index3] = floatValue;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        push(key, charValue, floatValue, index1, key1, index2, key2, index3, key3);
    }

    /**
     * If and only if key is already present, this changes the float associated with it while leaving the char the same.
     * @param key the encoded key as produced by {@link #encodePosition(int, int)}
     * @param floatValue
     */
    public void updateFloat(int key, float floatValue)
    {
        if (key == 0 && hasZeroValue) {
            //zeroChar = charValue;
            zeroFloat = floatValue;
            return;
        }

        int[] keyTable = this.keyTable;

        // Check for existing keys.
        int index1 = key & mask;
        int key1 = keyTable[index1];
        if (key == key1) {
            //charValueTable[index1] = charValue;
            floatValueTable[index1] = floatValue;
            return;
        }

        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if (key == key2) {
            //charValueTable[index2] = charValue;
            floatValueTable[index2] = floatValue;
            return;
        }

        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if (key == key3) {
            //charValueTable[index3] = charValue;
            floatValueTable[index3] = floatValue;
            return;
        }

        // Update key in the stash.
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (key == keyTable[i]) {
                //charValueTable[i] = charValue;
                floatValueTable[i] = floatValue;
                return;
            }
        }
    }

    public void updateChar(int key, char charValue)
    {
        if (key == 0 && hasZeroValue) {
            zeroChar = charValue;
            //zeroFloat = floatValue;
            return;
        }

        int[] keyTable = this.keyTable;

        // Check for existing keys.
        int index1 = key & mask;
        int key1 = keyTable[index1];
        if (key == key1) {
            charValueTable[index1] = charValue;
            //floatValueTable[index1] = floatValue;
            return;
        }

        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if (key == key2) {
            charValueTable[index2] = charValue;
            //floatValueTable[index2] = floatValue;
            return;
        }

        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if (key == key3) {
            charValueTable[index3] = charValue;
            //floatValueTable[index3] = floatValue;
            return;
        }

        // Update key in the stash.
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (key == keyTable[i]) {
                charValueTable[i] = charValue;
                //floatValueTable[i] = floatValue;
                return;
            }
        }
    }

    public void putAll(SparseTextMap map) {
        for (Entry entry : map.entries())
            put(entry.key, entry.charValue, entry.floatValue);
    }

    /**
     * Skips checks for existing keys.
     */
    private void putResize(int key, char charValue, float floatValue) {
        if (key == 0) {
            zeroChar = charValue;
            zeroFloat = floatValue;
            hasZeroValue = true;
            return;
        }

        // Check for empty buckets.
        int index1 = key & mask;
        int key1 = keyTable[index1];
        if (key1 == EMPTY) {
            keyTable[index1] = key;
            charValueTable[index1] = charValue;
            floatValueTable[index1] = floatValue;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if (key2 == EMPTY) {
            keyTable[index2] = key;
            charValueTable[index2] = charValue;
            floatValueTable[index2] = floatValue;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if (key3 == EMPTY) {
            keyTable[index3] = key;
            charValueTable[index3] = charValue;
            floatValueTable[index3] = floatValue;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        push(key, charValue, floatValue, index1, key1, index2, key2, index3, key3);
    }

    private void push(int insertKey, char insertChar, float insertFloat, int index1, int key1, int index2, int key2, int index3, int key3) {
        int[] keyTable = this.keyTable;
        char[] charTable = this.charValueTable;
        float[] floatTable = this.floatValueTable;
        int mask = this.mask;

        // Push keys until an empty bucket is found.
        int evictedKey;
        char evictedChar;
        float evictedFloat;
        int i = 0, pushIterations = this.pushIterations;
        do {
            // Replace the key and value for one of the hashes.
            switch (MathUtils.random(2)) {
                case 0:
                    evictedKey = key1;
                    evictedChar = charTable[index1];
                    evictedFloat = floatTable[index1];
                    keyTable[index1] = insertKey;
                    charTable[index1] = insertChar;
                    floatTable[index1] = insertFloat;
                    break;
                case 1:
                    evictedKey = key2;
                    evictedChar = charTable[index2];
                    evictedFloat = floatTable[index2];
                    keyTable[index2] = insertKey;
                    charTable[index2] = insertChar;
                    floatTable[index2] = insertFloat;
                    break;
                default:
                    evictedKey = key3;
                    evictedChar = charTable[index3];
                    evictedFloat = floatTable[index3];
                    keyTable[index3] = insertKey;
                    charTable[index3] = insertChar;
                    floatTable[index3] = insertFloat;
                    break;
            }

            // If the evicted key hashes to an empty bucket, put it there and stop.
            index1 = evictedKey & mask;
            key1 = keyTable[index1];
            if (key1 == EMPTY) {
                keyTable[index1] = evictedKey;
                charTable[index1] = evictedChar;
                floatTable[index1] = evictedFloat;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index2 = hash2(evictedKey);
            key2 = keyTable[index2];
            if (key2 == EMPTY) {
                keyTable[index2] = evictedKey;
                charTable[index2] = evictedChar;
                floatTable[index2] = evictedFloat;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index3 = hash3(evictedKey);
            key3 = keyTable[index3];
            if (key3 == EMPTY) {
                keyTable[index3] = evictedKey;
                charTable[index3] = evictedChar;
                floatTable[index3] = evictedFloat;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            if (++i == pushIterations) break;

            insertKey = evictedKey;
            insertChar = evictedChar;
            insertFloat = evictedFloat;
        } while (true);

        putStash(evictedKey, evictedChar, evictedFloat);
    }

    private void putStash(int key, char charValue, float floatValue) {
        if (stashSize == stashCapacity) {
            // Too many pushes occurred and the stash is full, increase the table size.
            resize(capacity << 1);
            put(key, charValue, floatValue);
            return;
        }
        // Store key in the stash.
        int index = capacity + stashSize;
        keyTable[index] = key;
        charValueTable[index] = charValue;
        floatValueTable[index] = floatValue;
        stashSize++;
        size++;
    }
    /**
     * @param x the x-component of the position to look up 
     * @param y the y-component of the position to look up
     * @param defaultValue Returned if the key was not associated with a value.
     * @return the char associated with the given position, or defaultValue if no char is associated
     */
    public char getChar(int x, int y, char defaultValue) {
        return getChar(encodePosition(x, y), defaultValue);
    }

    /**
     * @param key the encoded key as produced by {@link #encodePosition(int, int)}
     * @param defaultValue Returned if the key was not associated with a value.
     * @return the char associated with the given key, or defaultValue if no char is associated
     */
    public char getChar(int key, char defaultValue) {
        if (key == 0) {
            if (!hasZeroValue) return defaultValue;
            return zeroChar;
        }
        int index = key & mask;
        if (keyTable[index] != key) {
            index = hash2(key);
            if (keyTable[index] != key) {
                index = hash3(key);
                if (keyTable[index] != key) return getStashChar(key, defaultValue);
            }
        }
        return charValueTable[index];
    }

    private char getStashChar(int key, char defaultValue) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (key == keyTable[i]) return charValueTable[i];
        return defaultValue;
    }

    /**
     * @param x the x-component of the position to look up 
     * @param y the y-component of the position to look up
     * @param defaultValue Returned if the key was not associated with a value.
     * @return the float associated with the given position, or defaultValue if no float is associated
     */
    public float getFloat(int x, int y, float defaultValue) {
        return getFloat(encodePosition(x, y), defaultValue);
    }

    /**
     * @param key the encoded key as produced by {@link #encodePosition(int, int)}
     * @param defaultValue Returned if the key was not associated with a value.
     * @return the float associated with the given key, or defaultValue if no float is associated
     */
    public float getFloat(int key, float defaultValue) {
        if (key == 0) {
            if (!hasZeroValue) return defaultValue;
            return zeroFloat;
        }
        int index = key & mask;
        if (keyTable[index] != key) {
            index = hash2(key);
            if (keyTable[index] != key) {
                index = hash3(key);
                if (keyTable[index] != key) return getStashFloat(key, defaultValue);
            }
        }
        return floatValueTable[index];
    }

    private float getStashFloat(int key, float defaultValue) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (key == keyTable[i]) return floatValueTable[i];
        return defaultValue;
    }

    public void remove(int key) {
        remove(key, '#');
    }

    public char remove(int key, char defaultValue) {
        if (key == 0) {
            if (!hasZeroValue) return defaultValue;
            hasZeroValue = false;
            size--;
            return zeroChar;
        }

        int index = key & mask;
        if (key == keyTable[index]) {
            keyTable[index] = EMPTY;
            char oldValue = charValueTable[index];
            size--;
            return oldValue;
        }

        index = hash2(key);
        if (key == keyTable[index]) {
            keyTable[index] = EMPTY;
            char oldValue = charValueTable[index];
            size--;
            return oldValue;
        }

        index = hash3(key);
        if (key == keyTable[index]) {
            keyTable[index] = EMPTY;
            char oldValue = charValueTable[index];
            size--;
            return oldValue;
        }

        return removeStash(key, defaultValue);
    }

    private char removeStash(int key, char defaultValue) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (key == keyTable[i]) {
                char oldValue = charValueTable[i];
                removeStashIndex(i);
                size--;
                return oldValue;
            }
        }
        return defaultValue;
    }

    void removeStashIndex(int index) {
        // If the removed location was not last, move the last tuple to the removed location.
        stashSize--;
        int lastIndex = capacity + stashSize;
        if (index < lastIndex) {
            keyTable[index] = keyTable[lastIndex];
            charValueTable[index] = charValueTable[lastIndex];
        }
    }

    /**
     * Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
     * done. If the map contains more items than the specified capacity, the next highest power of two capacity is used instead.
     */
    public void shrink(int maximumCapacity) {
        if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
        if (size > maximumCapacity) maximumCapacity = size;
        if (capacity <= maximumCapacity) return;
        maximumCapacity = HashCommon.nextPowerOfTwo(maximumCapacity);
        resize(maximumCapacity);
    }

    /**
     * Clears the map and reduces the size of the backing arrays to be the specified capacity if they are larger.
     */
    public void clear(int maximumCapacity) {
        if (capacity <= maximumCapacity) {
            clear();
            return;
        }
        hasZeroValue = false;
        size = 0;
        resize(maximumCapacity);
    }

    public void clear() {
        if (size == 0) return;
        int[] keyTable = this.keyTable;
        for (int i = capacity + stashSize; i-- > 0; )
            keyTable[i] = EMPTY;
        size = 0;
        stashSize = 0;
        hasZeroValue = false;
    }

    /**
     * Returns true if the specified char value is in the map. Note: this traverses the entire map and compares every
     * value, which may be an expensive operation.
     */
    public boolean containsCharValue(char value) {
        if (hasZeroValue && zeroChar == value) return true;
        int[] keyTable = this.keyTable;
        char[] valueTable = this.charValueTable;
        for (int i = capacity + stashSize; i-- > 0; )
            if (keyTable[i] != 0 && valueTable[i] == value) return true;
        return false;
    }

    /**
     * Returns true if the specified char value is in the map. Note: this traverses the entire map and compares every
     * value, which may be an expensive operation.
     */
    public boolean containsFloatValue(float value) {
        if (hasZeroValue && zeroFloat == value) return true;
        int[] keyTable = this.keyTable;
        float[] valueTable = this.floatValueTable;
        for (int i = capacity + stashSize; i-- > 0; )
            if (keyTable[i] != 0 && valueTable[i] == value) return true;
        return false;
    }

    public boolean containsKey(int key) {
        if (key == 0) return hasZeroValue;
        int index = key & mask;
        if (keyTable[index] != key) {
            index = hash2(key);
            if (keyTable[index] != key) {
                index = hash3(key);
                if (keyTable[index] != key) return containsKeyStash(key);
            }
        }
        return true;
    }

    private boolean containsKeyStash(int key) {
        int[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (key == keyTable[i]) return true;
        return false;
    }

    /**
     * Returns the key for the specified char value, or notFound if it is not in the map. Note this traverses the
     * entire map and compares every value, which may be an expensive operation.
     */
    public int findKey(char value, int notFound) {
        if (hasZeroValue && zeroChar == value) return 0;
        int[] keyTable = this.keyTable;
        char[] valueTable = this.charValueTable;
        for (int i = capacity + stashSize; i-- > 0; )
            if (keyTable[i] != 0 && valueTable[i] == value) return keyTable[i];
        return notFound;
    }

    /**
     * Returns the key for the specified float value, or notFound if it is not in the map. Note this traverses the
     * entire map and compares every value, which may be an expensive operation.
     */
    public int findKey(float value, int notFound) {
        if (hasZeroValue && zeroFloat == value) return 0;
        int[] keyTable = this.keyTable;
        float[] valueTable = this.floatValueTable;
        for (int i = capacity + stashSize; i-- > 0; )
            if (keyTable[i] != 0 && valueTable[i] == value) return keyTable[i];
        return notFound;
    }

    /**
     * Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes.
     */
    public void ensureCapacity(int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded >= threshold) resize(HashCommon.nextPowerOfTwo((int) Math.ceil(sizeNeeded / loadFactor)));
    }

    private void resize(int newSize) {
        int oldEndIndex = capacity + stashSize;

        capacity = newSize;
        threshold = (int) (newSize * loadFactor);
        mask = newSize - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
        stashCapacity = Math.max(3, (int) Math.ceil(Math.log(newSize)) * 2);
        pushIterations = Math.max(Math.min(newSize, 8), (int) Math.sqrt(newSize) / 8);

        int[] oldKeyTable = keyTable;
        char[] oldCharTable = charValueTable;
        float[] oldFloatTable = floatValueTable;

        keyTable = new int[newSize + stashCapacity];
        charValueTable = new char[newSize + stashCapacity];
        floatValueTable = new float[newSize + stashCapacity];

        int oldSize = size;
        size = hasZeroValue ? 1 : 0;
        stashSize = 0;
        if (oldSize > 0) {
            for (int i = 0; i < oldEndIndex; i++) {
                int key = oldKeyTable[i];
                if (key != EMPTY) putResize(key, oldCharTable[i], oldFloatTable[i]);
            }
        }
    }

    private int hash2(int h) {
        h *= PRIME2;
        return (h ^ h >>> hashShift) & mask;
    }

    private int hash3(int h) {
        h *= PRIME3;
        return (h ^ h >>> hashShift) & mask;
    }

    public int hashCode() {
        int h = 0;
        if (hasZeroValue) {
            h += zeroChar * 31 + NumberTools.floatToIntBits(zeroFloat);
        }
        int[] keyTable = this.keyTable;
        char[] charTable = this.charValueTable;
        float[] floatTable = this.floatValueTable;
        for (int i = 0, n = capacity + stashSize; i < n; i++) {
            int key = keyTable[i];
            if (key != EMPTY) {
                h += key * 421;
                h += charTable[i] * 31;
                h += NumberTools.floatToIntBits(floatTable[i]);
            }
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SparseTextMap)) return false;
        SparseTextMap other = (SparseTextMap) obj;
        if (other.size != size) return false;
        if (other.hasZeroValue != hasZeroValue) return false;
        if (hasZeroValue && (other.zeroChar != zeroChar || other.zeroFloat != zeroFloat)) {
            return false;
        }
        int[] keyTable = this.keyTable;
        char[] valueTable = this.charValueTable;
        for (int i = 0, n = capacity + stashSize; i < n; i++) {
            int key = keyTable[i];
            if (key != EMPTY) {
                char otherValue = other.getChar(key, '\0');
                if (otherValue == 0 && !other.containsKey(key)) return false;
                int value = valueTable[i];
                if (otherValue != value) return false;
            }
        }
        return true;
    }

    public String toString() {
        if (size == 0) return "{}";
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('{');
        int[] keyTable = this.keyTable;
        char[] charTable = this.charValueTable;
        float[] floatTable = this.floatValueTable;
        int i = keyTable.length;
        if (hasZeroValue) {
            buffer.append("0=").append(zeroChar).append(',').append(zeroFloat);
        } else {
            while (i-- > 0) {
                int key = keyTable[i];
                if (key == EMPTY) continue;
                buffer.append(key).append('=').append(charTable[i]).append(',').append(floatTable[i]);
                break;
            }
        }
        while (i-- > 0) {
            int key = keyTable[i];
            if (key == EMPTY) continue;
            buffer.append(", ").append(key).append('=').append(charTable[i]).append(',').append(floatTable[i]);
        }
        buffer.append('}');
        return buffer.toString();
    }

    public Iterator<Entry> iterator() {
        return entries();
    }

    /**
     * Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration.
     */
    public Entries entries() {
        if (entries1 == null) {
            entries1 = new Entries(this);
            entries2 = new Entries(this);
        }
        if (!entries1.valid) {
            entries1.reset();
            entries1.valid = true;
            entries2.valid = false;
            return entries1;
        }
        entries2.reset();
        entries2.valid = true;
        entries1.valid = false;
        return entries2;
    }

    /**
     * Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration.
     */
    public CharValues charValues() {
        if (charValues1 == null) {
            charValues1 = new CharValues(this);
            charValues2 = new CharValues(this);
        }
        if (!charValues1.valid) {
            charValues1.reset();
            charValues1.valid = true;
            charValues2.valid = false;
            return charValues1;
        }
        charValues2.reset();
        charValues2.valid = true;
        charValues1.valid = false;
        return charValues2;
    }

    /**
     * Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration.
     */
    public FloatValues floatValues() {
        if (floatValues1 == null) {
            floatValues1 = new FloatValues(this);
            floatValues2 = new FloatValues(this);
        }
        if (!floatValues1.valid) {
            floatValues1.reset();
            floatValues1.valid = true;
            floatValues2.valid = false;
            return floatValues1;
        }
        floatValues2.reset();
        floatValues2.valid = true;
        floatValues1.valid = false;
        return floatValues2;
    }

    /**
     * Returns an iterator for the keys in the map. Remove is supported. Note that the same iterator instance is returned each time
     * this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration.
     */
    public Keys keys() {
        if (keys1 == null) {
            keys1 = new Keys(this);
            keys2 = new Keys(this);
        }
        if (!keys1.valid) {
            keys1.reset();
            keys1.valid = true;
            keys2.valid = false;
            return keys1;
        }
        keys2.reset();
        keys2.valid = true;
        keys1.valid = false;
        return keys2;
    }

    static public class Entry {
        public int key;
        public char charValue;
        public float floatValue;

        public String toString() {
            return key + "=" + charValue + "," + floatValue;
        }
    }

    static private class MapIterator {
        static final int INDEX_ILLEGAL = -2;
        static final int INDEX_ZERO = -1;

        public boolean hasNext;

        final SparseTextMap map;
        int nextIndex, currentIndex;
        boolean valid = true;

        public MapIterator(SparseTextMap map) {
            this.map = map;
            reset();
        }

        public void reset() {
            currentIndex = INDEX_ILLEGAL;
            nextIndex = INDEX_ZERO;
            if (map.hasZeroValue)
                hasNext = true;
            else
                findNextIndex();
        }

        void findNextIndex() {
            hasNext = false;
            int[] keyTable = map.keyTable;
            for (int n = map.capacity + map.stashSize; ++nextIndex < n; ) {
                if (keyTable[nextIndex] != EMPTY) {
                    hasNext = true;
                    break;
                }
            }
        }

        public void remove() {
            if (currentIndex == INDEX_ZERO && map.hasZeroValue) {
                map.hasZeroValue = false;
            } else if (currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else if (currentIndex >= map.capacity) {
                map.removeStashIndex(currentIndex);
                nextIndex = currentIndex - 1;
                findNextIndex();
            } else {
                map.keyTable[currentIndex] = EMPTY;
            }
            currentIndex = INDEX_ILLEGAL;
            map.size--;
        }
    }

    static public class Entries extends MapIterator implements Iterable<Entry>, Iterator<Entry> {
        private Entry entry = new Entry();

        public Entries(SparseTextMap map) {
            super(map);
        }

        /**
         * Note the same entry instance is returned each time this method is called.
         */
        public Entry next() {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            if (nextIndex == INDEX_ZERO) {
                entry.key = 0;
                entry.charValue = map.zeroChar;
                entry.floatValue = map.zeroFloat;
            } else {
                entry.key = map.keyTable[nextIndex];
                entry.charValue = map.charValueTable[nextIndex];
                entry.floatValue = map.floatValueTable[nextIndex];
            }
            currentIndex = nextIndex;
            findNextIndex();
            return entry;
        }

        public boolean hasNext() {
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        public Iterator<Entry> iterator() {
            return this;
        }

        public void remove() {
            super.remove();
        }
    }

    static public class CharValues extends MapIterator {
        public CharValues(SparseTextMap map) {
            super(map);
        }

        public boolean hasNext() {
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        public char next() {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            char value;
            if (nextIndex == INDEX_ZERO)
                value = map.zeroChar;
            else
                value = map.charValueTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return value;
        }

        /**
         * Returns a new array containing the remaining values.
         */
        public char[] toArray() {
            char[] array = new char[map.size];
            int idx = 0;
            while (hasNext && idx < array.length)
                array[idx++] = next();
            return array;
        }
    }

    static public class FloatValues extends MapIterator {
        public FloatValues(SparseTextMap map) {
            super(map);
        }

        public boolean hasNext() {
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        public float next() {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            float value;
            if (nextIndex == INDEX_ZERO)
                value = map.zeroFloat;
            else
                value = map.floatValueTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return value;
        }

        /**
         * Returns a new array containing the remaining values.
         */
        public float[] toArray() {
            float[] array = new float[map.size];
            int idx = 0;
            while (hasNext && idx < array.length)
                array[idx++] = next();
            return array;
        }
    }

    static public class Keys extends MapIterator {
        public Keys(SparseTextMap map) {
            super(map);
        }

        public boolean hasNext() {
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        public int next() {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
            int key = nextIndex == INDEX_ZERO ? 0 : map.keyTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return key;
        }

        /**
         * Returns a new array containing the remaining keys.
         */
        public int[] toArray() {
            int[] array = new int[map.size];
            int idx = 0;
            while (hasNext && idx < array.length)
                array[idx++] = next();
            return array;
        }
    }
}