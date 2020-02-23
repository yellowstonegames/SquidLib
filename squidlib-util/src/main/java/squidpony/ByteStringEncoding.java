package squidpony;

import squidpony.annotation.Beta;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * An experimental variant on LZSEncoding to encode byte arrays to compressed Strings, and decode them back. This always
 * uses UTF-16-safe encoding, which means it does not use half of all possible chars in the compressed Strings but makes
 * sure the Strings are valid UTF-16 (so they can be written to and read from file more safely).
 * <br>
 * Created by Tommy Ettinger on 1/11/2020.
 */
@Beta
public final class ByteStringEncoding {
    private ByteStringEncoding(){}

    private static String compress(byte[] uncompressed) {
        if (uncompressed == null) return null;
        if (uncompressed.length == 0) return "";
        final int bitsPerChar = 15, offset = 32;
        int i, value;
        HashMap<String, Integer> context_dictionary = new HashMap<>();
        HashSet<String> context_dictionaryToCreate = new HashSet<>();
        String context_c;
        String context_wc;
        String context_w = "";
        int context_enlargeIn = 2; // Compensate for the first entry which should not count
        int context_dictSize = 3;
        int context_numBits = 2;
        StringBuilder context_data = new StringBuilder(uncompressed.length >>> 1);
        int context_data_val = 0;
        int context_data_position = 0;
        int ii;

        for (ii = 0; ii < uncompressed.length; ii++) {
            context_c = new String(uncompressed, ii, 1, StandardCharsets.ISO_8859_1);
            if (!context_dictionary.containsKey(context_c)) {
                context_dictionary.put(context_c, context_dictSize++);
                context_dictionaryToCreate.add(context_c);
            }

            context_wc = context_w + context_c;
            if (context_dictionary.containsKey(context_wc)) {
                context_w = context_wc;
            } else {
                if (context_dictionaryToCreate.contains(context_w)) {
                    value = (context_w.charAt(0) & 255);
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + offset));
                            context_data_val = 0;
                        } else {
                            context_data_position++;
                        }
                    }
                    for (i = 0; i < 8; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + offset));
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
                    context_dictionaryToCreate.remove(context_w);
                } else {
                    value = context_dictionary.get(context_w);
                    for (i = 0; i < context_numBits; i++) {
                        context_data_val = (context_data_val << 1) | (value & 1);
                        if (context_data_position == bitsPerChar - 1) {
                            context_data_position = 0;
                            context_data.append((char) (context_data_val + offset));
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
//                if (context_w.charAt(0) < 256) {
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (context_data_val << 1);
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        context_data.append((char) (context_data_val + offset));
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
                        context_data.append((char) (context_data_val + offset));
                        context_data_val = 0;
                    } else {
                        context_data_position++;
                    }
                    value >>= 1;
                }
//                } else {
//                    value = 1;
//                    for (i = 0; i < context_numBits; i++) {
//                        context_data_val = (context_data_val << 1) | value;
//                        if (context_data_position == bitsPerChar - 1) {
//                            context_data_position = 0;
//                            context_data.append((char) (context_data_val + offset));
//                            context_data_val = 0;
//                        } else {
//                            context_data_position++;
//                        }
//                        value = 0;
//                    }
//                    value = context_w.charAt(0);
//                    for (i = 0; i < 16; i++) {
//                        context_data_val = (context_data_val << 1) | (value & 1);
//                        if (context_data_position == bitsPerChar - 1) {
//                            context_data_position = 0;
//                            context_data.append((char) (context_data_val + offset));
//                            context_data_val = 0;
//                        } else {
//                            context_data_position++;
//                        }
//                        value >>= 1;
//                    }
//                }

                context_dictionaryToCreate.remove(context_w);
            } else {
                value = context_dictionary.get(context_w);
                for (i = 0; i < context_numBits; i++) {
                    context_data_val = (context_data_val << 1) | (value & 1);
                    if (context_data_position == bitsPerChar - 1) {
                        context_data_position = 0;
                        context_data.append((char) (context_data_val + offset));
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
                context_data.append((char) (context_data_val + offset));
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
                context_data.append((char) (context_data_val + offset));
                break;
            } else
                context_data_position++;
        }
        context_data.append(' ');
        return context_data.toString();
    }

    private static byte[] decompress(String compressed) {
        if (compressed == null)
            return null;
        if (compressed.isEmpty())
            return new byte[0];
        final char[] getNextValue = compressed.toCharArray();
        final int length = getNextValue.length, resetValue = 16384, offset = -32;
        ArrayList<String> dictionary = new ArrayList<>();
        int enlargeIn = 4, dictSize = 4, numBits = 3, position = resetValue, index = 1, resb, maxpower, power,
                resultLength = 0;
        String entry, w, c;
        ArrayList<String> result = new ArrayList<>();
        char bits, val = (char) (getNextValue[0] + offset);

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
                val = (char) (getNextValue[index++] + offset);
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
                        val = (char) (getNextValue[index++] + offset);
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
                        val = (char) (getNextValue[index++] + offset);
                    }
                    bits |= (resb > 0 ? 1 : 0) << power++;
                }
                c = String.valueOf(bits);
                break;
            default:
                return new byte[0];
        }
        dictionary.add(c);
        w = c;
        result.add(w);
        resultLength += w.length();
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
                    val = (char) (getNextValue[index++] + offset);
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
                            val = (char) (getNextValue[index++] + offset);
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
                            val = (char) (getNextValue[index++] + offset);
                        }
                        bits |= (resb > 0 ? 1 : 0) << power++;
                    }
                    dictionary.add(String.valueOf(bits));
                    cc = dictSize++;
                    enlargeIn--;
                    break;
                case 2:
//                    byte[] bytes = new byte[resultLength];
//                    String r;
//                    for (int i = 0, p = 0, n = result.size(); i < n; i++) {
//                        r = result.get(i);
//                        System.arraycopy(r.getBytes(StandardCharsets.ISO_8859_1), 0, bytes, p, r.length());
//                        p += r.length();
//                    }
//                    return bytes;
                    StringBuilder sb = new StringBuilder(resultLength);
                    for (int i = 0, n = result.size(); i < n; i++) {
                        sb.append(result.get(i));
                    }
                    return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
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
                    return new byte[0];
                }
            }
            result.add(entry);
            resultLength += entry.length();

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
