/*
 * LZString4Java By Rufus Huang
 * https://github.com/rufushuang/lz-string4java
 * MIT License
 *
 * Port from original JavaScript version by pieroxy
 * https://github.com/pieroxy/lz-string
 */

package squidpony;

import org.github.jamm.MemoryMeter;

import java.util.ArrayList;

/**
 * LZ-String compression; wrapped by {@link LZSPlus} for convenience, but extended functionality is available here.
 * You can compress a String to a smaller, technically-invalid UTF-16 String with {@link #compress(String)}, and undo
 * that with {@link #decompress(String)}. Compress to a valid UTF-16 String with {@link #compressToUTF16(String)},
 * decompress such a String with {@link #decompressFromUTF16(String)} (LZSPlus uses these). Compress to Base64 with
 * {@link #compressToBase64(String)}, decompress Base64 with {@link #decompressFromBase64(String)}. Compress to
 * URI-encoded Strings with {@link #compressToEncodedURIComponent(String)}, decompress those with
 * {@link #decompressFromEncodedURIComponent(String)}. This class is super-sourced with a compatible alternative
 * implementation on GWT for performance, and a main goal is to provide UTF-16 Strings that can be stored in a browser's
 * LocalStorage on GWT. This class is also sometimes used internally when a large compressed String in Java source code
 * makes more sense than an even larger resource file.
 */
public final class LZSEncoding {

    private LZSEncoding() {};
    private static final char[] keyStrBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray(),
            keyStrUriSafe = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$".toCharArray(),
            valStrBase64 = new char[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 64, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
                    0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51},
            valStrUriSafe = new char[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 64, 0, 0, 0, 0, 0, 0, 62, 0, 63, 0, 0,
                    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
                    0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    /**
     * Compress a String using LZ-String encoding but only using Base64 characters ('A'-'Z', 'a'-'z', '0'-'9', '+', '/',
     * and '=' for Base64 validity).
     * @param uncompressed an uncompressed String to encode
     * @return the encoded, compressed String
     */
    public static String compressToBase64(String uncompressed) {
        if (uncompressed == null)
            return null;
        String res = _compress(uncompressed, 6, keyStrBase64, 0);
        switch (res.length() & 3) { // To produce valid Base64
            default: // When could this happen ?
            case 0:
                return res;
            case 1:
                return res + "===";
            case 2:
                return res + "==";
            case 3:
                return res + "=";
        }
    }

    /**
     * Decompresses a String that had been compressed with {@link #compressToBase64(String)}.
     * @param compressed a Base64-encoded, compressed String
     * @return the original uncompressed version of the String
     */
    public static String decompressFromBase64(String compressed) {
        if (compressed == null)
            return null;
        if (compressed.isEmpty())
            return "";
        final char[] input = compressed.toCharArray();
        return _decompress(input.length, 32, input, valStrBase64, 0);
    }

    /**
     * Compresses a String using the properties of UTF-16 encoding to store approximately 15 bits of LZW-compressed text
     * in each 2-byte Unicode character, which does particularly well with ASCII text and can be smaller than UTF-8 in
     * some cases, especially where each char must be stored as UTF-16, e.g. Java Strings or browser-based LocalStorage.
     * @param uncompressed an uncompressed String to encode
     * @return the encoded, compressed String
     */
    public static String compressToUTF16(String uncompressed) {
        if (uncompressed == null)
            return null;
        return _compress(uncompressed, 15, null, 32) + " ";
    }
    /**
     * Decompresses a String that had been compressed with {@link #compressToUTF16(String)}.
     * @param compressed a UTF16-encoded (as by {@link #compressToUTF16(String)}), compressed String
     * @return the original uncompressed version of the String
     */
    public static String decompressFromUTF16(String compressed) {
        if (compressed == null)
            return null;
        if (compressed.isEmpty())
            return "";
        final char[] comp = compressed.toCharArray();
        return _decompress(comp.length, 16384, comp, null, -32);
    }

    /**
     * Compress a String using LZ-String encoding but only using valid URI component characters ('A'-'Z', 'a'-'z',
     * '0'-'9', '+', '-', and possibly '$').
     * @param uncompressed an uncompressed String to encode
     * @return the encoded, compressed String
     */
    public static String compressToEncodedURIComponent(String uncompressed) {
        if (uncompressed == null)
            return null;
        return _compress(uncompressed, 6, keyStrUriSafe, 0) + '+';
    }
    /**
     * Decompresses a String that had been compressed with {@link #compressToEncodedURIComponent(String)}.
     * @param compressed a URI-encoded, compressed String
     * @return the original uncompressed version of the String
     */
    public static String decompressFromEncodedURIComponent(String compressed) {
        if (compressed == null) return null;
        if (compressed.isEmpty()) return "";
        final char[] input = compressed.toCharArray();
        return _decompress(input.length, 32, input, valStrUriSafe, 0);
    }

    /**
     * Compresses a String as tightly as possible by using 16 bits of each 16-bit character, which can infrequently
     * result in invalid UTF-16 codepoints, but that may not matter for all applications.
     * @param uncompressed an uncompressed String to encode
     * @return the encoded, compressed String
     */
    public static String compress(String uncompressed) {
        return _compress(uncompressed, 16, null, 0);
    }

    private static String _compress(String uncompressedStr, int bitsPerChar, char[] getCharFromInt, int offset) {
        if (uncompressedStr == null) return null;
        if (uncompressedStr.isEmpty()) return "";
        MemoryMeter meter = new MemoryMeter();
        int i, value;
//        HashMap<String, Integer> context_dictionary = new HashMap<>();   //67345272 bytes
//        HashSet<String> context_dictionaryToCreate = new HashSet<>();
//        HashMap<String, Integer> context_dictionary = new HashMap<>(1024, 0.5f);       //71539576 bytes
//        HashSet<String> context_dictionaryToCreate = new HashSet<>();
//        HashMap<String, Integer> context_dictionary = new HashMap<>(1024, 0.8f);       //67345272 bytes
//        HashSet<String> context_dictionaryToCreate = new HashSet<>();
//        ObjectMap<String, Integer> context_dictionary = new ObjectMap<>(1024, 0.5f); //61142360 bytes
//        ObjectSet<String> context_dictionaryToCreate = new ObjectSet<>();
        ObjectMap<String, Integer> context_dictionary = new ObjectMap<>(1024, 0.8f); //52753736 bytes
        ObjectSet<String> context_dictionaryToCreate = new ObjectSet<>();
//        ObjectMap<String, Integer> context_dictionary = new ObjectMap<>(); //52753736 bytes
//        ObjectSet<String> context_dictionaryToCreate = new ObjectSet<>();
        String context_c;
        String context_wc;
        String context_w = "";
        int context_enlargeIn = 2; // Compensate for the first entry which should not count
        int context_dictSize = 3;
        int context_numBits = 2;
        StringBuilder context_data = new StringBuilder(uncompressedStr.length() >>> 1);
        int context_data_val = 0;
        int context_data_position = 0;
        int ii;

        char[] uncompressed = uncompressedStr.toCharArray();
        for (ii = 0; ii < uncompressed.length; ii++) {
            context_c = String.valueOf(uncompressed[ii]);
            if (!context_dictionary.containsKey(context_c)) {
                context_dictionary.put(context_c, context_dictSize++);
                context_dictionaryToCreate.add(context_c);
            }

            context_wc = context_w + context_c;
            if (context_dictionary.containsKey(context_wc)) {
                context_w = context_wc;
            } else {
                if (context_dictionaryToCreate.contains(context_w)) {
                    if ((value = context_w.charAt(0)) < 256) {
                        for (i = 0; i < context_numBits; i++) {
                            context_data_val = (context_data_val << 1);
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                        }
                        for (i = 0; i < 8; i++) {
                            context_data_val = (context_data_val << 1) | (value & 1);
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
                                context_data_val = 0;
                            } else {
                                context_data_position++;
                            }
                            value = 0;
                        }
                        value = context_w.charAt(0);
                        for (i = 0; i < 16; i++) {
                            context_data_val = (context_data_val << 1) | (value & 1);
                            if (context_data_position == bitsPerChar - 1) {
                                context_data_position = 0;
                                context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                    value = context_dictionary.get(context_w);
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                context_dictionary.put(context_wc, context_dictSize++);
                context_w = context_c;
            }
        }

        // Output the code for w.
        if (!context_w.isEmpty()) {
            if (context_dictionaryToCreate.contains(context_w)) {
                if (context_w.charAt(0) < 256) {
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                            context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value = 0;
                    }
                    value = context_w.charAt(0);
                    for (i = 0; i < 16; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                        value >>= 1;
                    }
                }

                context_dictionaryToCreate.remove(context_w);
            } else {
                value = context_dictionary.get(context_w);
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (context_data_val << 1) | (value & 1);
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
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
                context_data.append((char) (((getCharFromInt == null) ? context_data_val : getCharFromInt[context_data_val]) + offset));
                break;
            } else
                context_data_position++;
        }
        System.out.println(meter.measureDeep(context_dictionary) + " bytes used for the dictionary");
        return context_data.toString();
    }
    /**
     * Decompresses a String that had been compressed with {@link #compress(String)}.
     * @param compressed a compressed String using the default encoding from {@link #compress(String)}
     * @return the original uncompressed version of the String
     */
    public static String decompress(final String compressed) {
        if (compressed == null)
            return null;
        if (compressed.isEmpty())
            return "";
        return _decompress(compressed.length(), 32768, compressed.toCharArray(), null, 0);
    }

    private static String _decompress(int length, int resetValue, char[] getNextValue, char[] modify, int offset) {
        if(getNextValue == null)
            return null;
        if(getNextValue.length == 0)
            return "";
        ArrayList<String> dictionary = new ArrayList<>();
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = 1, resb, maxpower, power;
        String entry, w, c;
        ArrayList<String> result = new ArrayList<>();
        char bits, val = (modify == null) ? (char) (getNextValue[0] + offset) : modify[getNextValue[0]];

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
                position = resetValue;
                val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
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
                        val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
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
                        position = resetValue;
                        val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
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
            int cc = 0;
            maxpower = numBits;
            power = 0;
            while (power != maxpower) {
                resb = val & position;
                position >>= 1;
                if (position == 0) {
                    position = resetValue;
                    val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
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
                            val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
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
                            position = resetValue;
                            val = (modify == null) ? (char) (getNextValue[index++] + offset) : modify[getNextValue[index++]];
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
}
