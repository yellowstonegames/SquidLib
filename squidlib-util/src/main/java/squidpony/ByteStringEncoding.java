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

package squidpony;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A variant on LZSEncoding to encode byte arrays to compressed Strings, and decode them back. This always uses
 * UTF-16-safe encoding, which means it does not use one bit of each char in the compressed Strings but makes
 * sure the Strings are valid UTF-16 (so they can be written to and read from file more safely).
 * <br>
 * Like almost all of SquidLib, this class is not thread-safe. It reuses internal data structures rather than repeatedly
 * re-creating them, which strongly helps its single-threaded performance.
 * <br>
 * Created by Tommy Ettinger on 1/11/2020.
 */
public final class ByteStringEncoding {
    private ByteStringEncoding(){}

    private static final String[] BYTE_STRINGS = new String[256];

    static {
        for (int i = 0; i < 256; i++) {
            BYTE_STRINGS[i] = Character.toString((char) i);
        }
    }

    private static final HashMap<String, Integer> contextDictionary = new HashMap<>(256, 0.5f);
    private static final HashSet<String> contextDictionaryToCreate = new HashSet<>(256, 0.5f);
    private static final ArrayList<String> allStrings = new ArrayList<>(256);
    private static final StringBuilder sb = new StringBuilder(1024);

    public static String compress(byte[] uncompressed) {
        if (uncompressed == null) return null;
        if (uncompressed.length == 0) return "";
        final int bitsPerChar = 15, offset = 32;
        int i, value;
        String context_c;
        String context_wc;
        String context_w = "";
        int context_enlargeIn = 2; // Compensate for the first entry which should not count
        int context_dictSize = 3;
        int context_numBits = 2;
        int context_data_val = 0;
        int context_data_position = 0;
        int ii;

        contextDictionary.clear();
        contextDictionaryToCreate.clear();
        sb.setLength(0);

        for (ii = 0; ii < uncompressed.length; ii++) {
            context_c = BYTE_STRINGS[uncompressed[ii] & 255];
            if (!contextDictionary.containsKey(context_c)) {
                contextDictionary.put(context_c, context_dictSize++);
                contextDictionaryToCreate.add(context_c);
            }

            context_wc = context_w + context_c;
            if (contextDictionary.containsKey(context_wc)) {
                context_w = context_wc;
            } else {
                if (contextDictionaryToCreate.contains(context_w)) {
                    value = (context_w.charAt(0) & 255);
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            sb.append((char) (context_data_val + offset));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                    }
                    for (i = 0; i < 8; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            sb.append((char) (context_data_val + offset));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>= 1;
                    }
                    context_enlargeIn--;
                    if (context_enlargeIn == 0) {
                        context_enlargeIn = 1 << context_numBits++;
                    }
                    contextDictionaryToCreate.remove(context_w);
                } else {
                    value = contextDictionary.get(context_w);
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            sb.append((char) (context_data_val + offset));
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
                contextDictionary.put(context_wc, context_dictSize++);
                context_w = context_c;
            }
        }

        // Output the code for w.
        if (!context_w.isEmpty()) {
            if (contextDictionaryToCreate.contains(context_w)) {
//                if (context_w.charAt(0) < 256) {
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (context_data_val << 1);
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        sb.append((char) (context_data_val + offset));
                        context_data_val = 0;
                    } else {
                        context_data_position++;
                    }
                }
                value = context_w.charAt(0);
                for (i = 0; i < 8; i++) {
                    context_data_val = (context_data_val << 1) | (value & 1);
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        sb.append((char) (context_data_val + offset));
                        context_data_val = 0;
                    } else {
                        context_data_position++;
                    }
                    value >>= 1;
                }
                contextDictionaryToCreate.remove(context_w);
            } else {
                value = contextDictionary.get(context_w);
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (context_data_val << 1) | (value & 1);
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        sb.append((char) (context_data_val + offset));
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
            if (context_data_position == bitsPerChar - 1) {
                context_data_position = 0;
                sb.append((char) (context_data_val + offset));
                context_data_val = 0;
            } else {
                context_data_position++;
            }
            value >>= 1;
        }

        // Flush the last char
        while (true) {
            context_data_val = (context_data_val << 1);
            if (context_data_position == bitsPerChar - 1) {
                sb.append((char) (context_data_val + offset));
                break;
            } else
                context_data_position++;
        }
        sb.append(' ');
        return sb.toString();
    }

    public static byte[] decompress(String compressed) {
        if (compressed == null)
            return null;
        if (compressed.isEmpty())
            return new byte[0];
        final int length = compressed.length(), resetValue = 16384, offset = -32;
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = 1, resb, maxpower, power;
        String entry, w, c;
        sb.setLength(0);
        char bits, val = (char) (compressed.charAt(0) + offset);

        allStrings.clear();
        for (int i = 0; i < 3; i++) {
            allStrings.add(i, BYTE_STRINGS[i]);
        }

        bits = 0;
        maxpower = 2;
        power = 0;
        while (power != maxpower) {
            resb = val & position;
            position >>= 1;
            if (position == 0) {
                position = resetValue;
                val = (char) (compressed.charAt(index++) + offset);
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
                        position = resetValue;
                        val = (char) (compressed.charAt(index++) + offset);
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = BYTE_STRINGS[bits];
                break;
            case 1:
                bits = 0;
                maxpower = 16;
                power = 0;
                while (power != maxpower) {
                    resb = val & position;
                    position >>= 1;
                    if (position == 0) {
                        position = resetValue;
                        val = (char) (compressed.charAt(index++) + offset);
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return new byte[0];
        }
        allStrings.add(c);
        w = c;
        sb.append(w);
        while (true) {
            if (index > length) {
                return new byte[0];
            }
            int cc = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>= 1;
                if (position == 0) {
                    position = resetValue;
                    val = (char) (compressed.charAt(index++) + offset);
                }
                cc |= (resb > 0 ? 1 : 0) << power++;
            }
            switch (cc) {
                case 0:
                    bits = 0;
                    maxpower = 8;
                    power = 0;
                    while (power != maxpower) {
                        resb = val & position;
                        position >>= 1;
                        if (position == 0) {
                            position = resetValue;
                            val = (char) (compressed.charAt(index++) + offset);
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }

                    allStrings.add(BYTE_STRINGS[bits]);
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
                            position = resetValue;
                            val = (char) (compressed.charAt(index++) + offset);
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }
                    allStrings.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
                    try {
                        return sb.toString().getBytes("ISO-8859-1");
                    } catch (UnsupportedEncodingException e) {
                        return null; // should never happen, unless you're deep in the crazy mines.
                    }
                    // this is a possible alternative, but StandardCharsets may add to startup time if loaded early.
//                    return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
            }

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }

            if (cc < allStrings.size() && allStrings.get(cc) != null) {
                entry = allStrings.get(cc);
            } else {
                if (cc == dictSize) {
                    entry = w + w.charAt(0);
                } else {
                    return new byte[0];
                }
            }
            sb.append(entry);

            // Add w+entry[0] to the dictionary.
            allStrings.add(w + entry.charAt(0));
            dictSize++;
            enlargeIn--;

            w = entry;

            if (enlargeIn == 0) {
                enlargeIn = 1 << numBits;
                numBits++;
            }
        }
    }
}
