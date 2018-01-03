/*
MIT License

Copyright (c) 2018 Tommy Ettinger

Based on lz-string4java, which is:
Copyright (c) 2016 rufushuang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package squidpony;

//import com.badlogic.gdx.utils.Array;
//import com.badlogic.gdx.utils.ObjectIntMap;
//import com.badlogic.gdx.utils.ObjectSet;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.UnorderedSet;

import java.util.ArrayList;
import java.util.Objects;

/**
 * LZ-String compression, taking Strings and compressing them to other Strings, optionally with some encryption.
 * This role was performed by the <a href="https://github.com/tommyettinger/BlazingChain">BlazingChain library</a>,
 * which was a dependency of squidlib-extra, but recent developments have allowed all dependencies to be removed other
 * than SquidLib, while also probably reducing memory usage by a fair amount. The actual implementation of this class is
 * very unusual, with the LZ-String encoding part derived from
 * <a href="https://github.com/rufushuang/lz-string4java">rufushuang's lz-string4java</a> (which is a port of
 * <a href="https://github.com/pieroxy/lz-string">pieroxy's lz-string</a>), while the encryption-like part (which is not
 * very strong) was added in SquidLib.
 * <br>
 * Created by Tommy Ettinger on 7/13/2017.
 */
public final class LZSPlus {

    /**
     * Compresses the given text using LZ-String compression; does not encrypt the result.
     * @param uncompressedStr text to compress
     * @return a compressed version of the given text
     */
    public static String compress(String uncompressedStr) {
        return compress(uncompressedStr, null);
    }

    private static final CrossHash.IHasher stringWithIntHasher = new CrossHash.IHasher() {
        @Override
        public int hash(Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data, 0, ((char[]) data).length - 2) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            if (left == right) return true;
            if (left instanceof char[] && right instanceof char[]) {
                char[] l = (char[]) left, r = (char[]) right;
                int len = l.length;
                if (len != r.length)
                    return false;
                for (int i = len - 2; i >= 0; i--) {
                    if(l[i] != r[i])
                        return false;
                }
                return true;
            }
            return Objects.equals(left, right);
        }
    };
    private static char[] combine(final char[] left, final char[] right)
    {
        final char[] next = new char[left.length + right.length - 2];
        System.arraycopy(left, 0, next, 0, left.length - 2);
        System.arraycopy(right, 0, next, left.length - 2, right.length - 2);
        return next;
    }
    /**
     * Compresses the given text using LZ-String compression and encrypts (somewhat) the compressed result so it can't
     * be read back without the same keys as a long array. Shorter long arrays give less security to encryption, though
     * there isn't much security to begin with. You can produce a decent-quality array for this purpose with
     * {@link Garbler#makeKeyArray(int, String)}; the size parameter could reasonably be anywhere from 2 to 32. If the
     * keys array is null or empty, this only compresses and does not perform an additional encryption step.
     * @param uncompressedStr text to compress and optionally encrypt
     * @param keys the long array that will be used to encrypt the output, and will be required to decrypt the result; may be null
     * @return a compressed and optionally encrypted version of the given text
     */
    public static String compress(String uncompressedStr, long[] keys) {
        if (uncompressedStr == null) return null;
        if (uncompressedStr.isEmpty()) return "";
        int i, value;
        UnorderedSet<char[]> context_dictionary = new UnorderedSet<>(128, 0.7f, stringWithIntHasher);
        UnorderedSet<char[]> context_dictionaryToCreate = new UnorderedSet<>(128, 0.7f, stringWithIntHasher);
        char[] context_c;
        char[] context_wc;
        char[] context_w = new char[2];
        char[] tmp;
        int context_enlargeIn = 2; // Compensate for the first entry which should not count
        int context_dictSize = 3;
        int context_numBits = 2;
        int len = uncompressedStr.length();
        StringBuilder context_data = new StringBuilder(len >>> 1);
        int context_data_val = 0;
        int context_data_position = 0;
        int ii;

        for (ii = 0; ii < len; ii++) {
            context_c = new char[]{uncompressedStr.charAt(ii), 0, 0};
            if (!context_dictionary.contains(context_c)) {
                context_c[1] = (char) (context_dictSize >>> 16);
                context_c[2] = (char)(context_dictSize++ & 0xFFFF);
                context_dictionary.add(context_c);
                context_dictionaryToCreate.add(context_c);
            }

            context_wc = combine(context_w, context_c);
            if (context_dictionary.contains(context_wc)) {
                context_w = context_wc;
            } else {
                if (context_dictionaryToCreate.contains(context_w)) {
                    if ((value = context_w[0]) < 256) {
                        for (i = 0; i < context_numBits; i++) {
                            context_data_val = (context_data_val << 1);
                            if (context_data_position == 14) {
                                context_data_position = 0;
                                context_data.append((char) (context_data_val + 32));
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                        }
                        for (i = 0; i < 8; i++) {
                            context_data_val = (context_data_val << 1) | (value & 1);
                            if (context_data_position == 14) {
                                context_data_position = 0;
                                context_data.append((char) (context_data_val + 32));
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value >>= 1;
                        }
                    } else {
                        value = 1;
                        for (i = 0; i < context_numBits; i++) {
                            context_data_val = (context_data_val << 1) | value;
                            if (context_data_position == 14) {
                                context_data_position = 0;
                                context_data.append((char) (context_data_val + 32));
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value = 0;
                        }
                        value = context_w[0];
                        for (i = 0; i < 16; i++) {
                            context_data_val = (context_data_val << 1) | (value & 1);
                            if (context_data_position == 14) {
                                context_data_position = 0;
                                context_data.append((char) (context_data_val + 32));
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value >>= 1;
                        }
                    }
                    context_enlargeIn--;
                    if (context_enlargeIn == 0) {
                        context_enlargeIn = 1 << context_numBits++;
                    }
                    context_dictionaryToCreate.remove(context_w);
                } else {
                    tmp = context_dictionary.get(context_w);
                    if(tmp == null)
                        value = -1;
                    else {
                        value = tmp[tmp.length - 2] << 16 | tmp[tmp.length - 1];
                    }
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == 14) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + 32));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>= 1;
                    }

                }
                context_enlargeIn--;
                if (context_enlargeIn == 0) {
                    context_enlargeIn = 1 << context_numBits++;
                }
                // Add wc to the dictionary.
                context_wc[context_wc.length - 2] = (char) (context_dictSize >>> 16);
                context_wc[context_wc.length - 1] = (char)(context_dictSize++ & 0xFFFF);
                context_dictionary.add(context_wc);
                context_w = context_c;
            }
        }

        // Output the code for w.
        if (context_w.length > 2) {
            if (context_dictionaryToCreate.contains(context_w)) {
                if (context_w[0] < 256) {
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1);
                        if (context_data_position == 14) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + 32));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                    }
                    value = context_w[0];
                    for (i = 0; i < 8; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == 14) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + 32));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>= 1;
                    }
                } else {
                    value = 1;
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1) | value;
                        if (context_data_position == 14) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + 32));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value = 0;
                    }
                    value = context_w[0];
                    for (i = 0; i < 16; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == 14) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + 32));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>= 1;
                    }
                }

                context_dictionaryToCreate.remove(context_w);
            } else {
                tmp = context_dictionary.get(context_w);
                if(tmp == null)
                    value = -1;
                else {
                    value = tmp[tmp.length - 2] << 16 | tmp[tmp.length - 1];
                }
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (context_data_val << 1) | (value & 1);
                    if (context_data_position == 14) {
                        context_data_position = 0;
                        context_data.append((char) (context_data_val + 32));
                        context_data_val = 0;
                    } else {
                        context_data_position++;
                    }
                    value >>= 1;
                }

            }
        }

        // Mark the end of the stream
        value = 2;
        for (i = 0; i < context_numBits; i++) {
            context_data_val = (context_data_val << 1) | (value & 1);
            if (context_data_position == 14) {
                context_data_position = 0;
                context_data.append((char) (context_data_val + 32));
                context_data_val = 0;
            } else {
                context_data_position++;
            }
            value >>= 1;
        }

        // Flush the last char
        while (true) {
            context_data_val = (context_data_val << 1);
            if (context_data_position == 14) {
                context_data.append((char) (context_data_val + 32));
                break;
            } else
                context_data_position++;
        }
        if(keys != null && keys.length > 0)
        {
            int mainLength = context_data.length(), smallLength = mainLength >> 2, rem = mainLength & 3;
            char[] raw = new char[mainLength];
            context_data.getChars(0, mainLength, raw, 0);
            for (int j = 0; j < keys.length; j++) {
                long z = keys[j], inc = 0x9E3779BAL * z + 0x632BE5ABL, r;
                for (int k = 0, n = 0; k < smallLength; k++) {
                    r = splitMix64(z += inc);
                    raw[n] ^= (r & 0x7fff);
                    raw[n++] += 32;
                    raw[n] ^= (r >>> 16 & 0x7fff);
                    raw[n++] += 32;
                    raw[n] ^= (r >>> 32 & 0x7fff);
                    raw[n++] += 32;
                    raw[n] ^= (r >>> 49);
                    raw[n++] += 32;
                }
                z += inc;
                switch(rem)
                {
                    case 3:
                        raw[mainLength-3] ^= splitMix64(z) >>> 49;
                    case 2:
                        raw[mainLength-2] ^= splitMix64(z) >>> 16 & 0x7fff;
                    case 1:
                        raw[mainLength-1] ^= splitMix64(z) & 0x7fff;
                }
            }
            return String.valueOf(raw);
        }
        else
            return context_data.toString();
    }
    /**
     * Decompresses text that was compressed with LZ-String compression; does not reverse decryption so it can only
     * decompress Strings produced by {@link #compress(String)}, or {@link #compress(String, long[])} with an empty or
     * null keys parameter.
     * @param compressed text that was compressed by {@link #compress(String)}
     * @return the original text, decompressed from the given text
     */

    public static String decompress(String compressed) {
        return decompress(compressed, null);
    }

    /**
     * Decompresses text that was compressed with LZ-String compression, reversing any encryption if the keys long array
     * matches the long array passed to {@link #compress(String, long[])} (keys can be null if no array was passed).
     * @param compressed text that was compressed by {@link #compress(String, long[])}
     * @param keys the long array that was used to encrypt the output, and must match to decrypt the result; may be null
     * @return the original text, decompressed and decrypted from compressed
     */
    public static String decompress(String compressed, long[] keys) {
        if (compressed == null)
            return null;
        if (compressed.isEmpty())
            return "";
        final char[] comp = compressed.toCharArray();
        int length = comp.length;
        if(keys != null && keys.length > 0)
        {
            int smallLength = length >> 2, rem = length & 3;
            for (int j = keys.length - 1; j >= 0; j--) {
                long z = keys[j], inc = 0x9E3779BAL * z + 0x632BE5ABL, r;
                z += (1+smallLength) * inc;
                switch (rem)
                {
                    case 3:
                        comp[length-3] ^= splitMix64(z) >>> 49;
                    case 2:
                        comp[length-2] ^= splitMix64(z) >>> 16 & 0x7fff;
                    case 1:
                        comp[length-1] ^= splitMix64(z) & 0x7fff;
                }
                for (int k = 0, n = (length & -4) - 1; k < smallLength ; k++) {
                    r = splitMix64(z -= inc);
                    comp[n] -= 32;
                    comp[n--] ^= (r >>> 49);
                    comp[n] -= 32;
                    comp[n--] ^= (r >>> 32 & 0x7fff);
                    comp[n] -= 32;
                    comp[n--] ^= (r >>> 16 & 0x7fff);
                    comp[n] -= 32;
                    comp[n--] ^= (r & 0x7fff);

                }
            }
        }


        ArrayList<String> dictionary = new ArrayList<>();
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = 16384, index = 1, resb, maxpower, power;
        String entry, w, c;
        ArrayList<String> result = new ArrayList<>();
        char bits, val = (char) (comp[0] - 32);

        for (char i = 0; i < 3; i++) {
            dictionary.add(i, String.valueOf(i));
        }

        bits = 0;
        maxpower = 2;
        power = 0;
        while (power != maxpower) {
            resb = val & position;
            position >>= 1;
            if (position == 0) {
                position = 16384;
                val = (char) (comp[index++] - 32);
            }
            bits |= (resb > 0 ? 1 : 0) << power++;
        }

        switch (bits) {
            case 0:
                bits = 0;
                maxpower = 8;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>= 1;
                    if (position == 0) {
                        position = 16384;
                        val = (char) (comp[index++] - 32);
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            case 1:
                bits = 0;
                maxpower = 16;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>= 1;
                    if (position == 0) {
                        position = 16384;
                        val = (char) (comp[index++] - 32);
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return "";
        }
        dictionary.add(c);
        w = c;
        result.add(w);
        while (true) {
            if (index > length) {
                return "";
            }

            bits = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>= 1;
                if (position == 0) {
                    position = 16384;
                    val = (char) (comp[index++] - 32);
                }
                bits |= (resb > 0 ? 1 : 0) << power++;
            }
            int cc;
            switch (cc = bits) {
                case 0:
                    bits = 0;
                    maxpower = 8;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>= 1;
                        if (position == 0) {
                            position = 16384;
                            val = (char) (comp[index++] - 32);
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }

                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 1:
                    bits = 0;
                    maxpower = 16;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>= 1;
                        if (position == 0) {
                            position = 16384;
                            val = (char) (comp[index++] - 32);
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }
                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
                    StringBuilder sb = new StringBuilder(result.size());
                    for (String s : result)
                        sb.append(s);
                    return sb.toString();
            }

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

            if (cc < dictionary.size() && dictionary.get(cc) != null) {
                entry = dictionary.get(cc);
            } else {
                if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return "";
                }
            }
            result.add(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.add(w + entry.charAt(0));
            dictSize++;
            enlargeIn--;

            w = entry;

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

        }

    }

    /**
     * Get a long from this with {@code splitMix64(z += 0x9E3779B97F4A7C15L)}, where z is a long to use as state.
     * 0x9E3779B97F4A7C15L can be changed for any odd long if the same number is used across calls.
     * @param state long, must be changed with each call; {@code splitMix64(z += 0x9E3779B97F4A7C15L)} is recommended
     * @return a pseudo-random long
     */
    private static long splitMix64(long state) {
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }
}
