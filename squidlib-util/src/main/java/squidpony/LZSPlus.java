/*
 * Copyright (c) 2016-2022  Eben Howard, Tommy Ettinger, and contributors
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
package squidpony;

/**
 * LZ-String compression, taking Strings and compressing them to other Strings, optionally with some encryption.
 * This role was performed by the <a href="https://github.com/tommyettinger/BlazingChain">BlazingChain library</a>,
 * which was a dependency of squidlib-extra, but recent developments have allowed all dependencies to be removed other
 * than SquidLib, while also probably reducing memory usage by a fair amount. The actual implementation of this class is
 * very unusual, with the LZ-String encoding part derived from
 * <a href="https://github.com/rufushuang/lz-string4java">rufushuang's lz-string4java</a> (which is a port of
 * <a href="https://github.com/pieroxy/lz-string">pieroxy's lz-string</a>), while the encryption-like part (which is not
 * very strong) was added in SquidLib. This uses {@link Garbler} to do the encryption and {@link LZSEncoding} to do the
 * compression; LZSEncoding uses the original JavaScript lz-string library almost verbatim when run on GWT, so it
 * performs better than code that has been compiled to JavaScript from Java by GWT, and it performs like Java when run
 * on a real JVM. 
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
        return LZSEncoding.compressToUTF16(uncompressedStr);
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
        if(keys == null) return LZSEncoding.compressToUTF16(uncompressedStr);
        if (uncompressedStr == null) return null;
        if (uncompressedStr.isEmpty()) return " ";
        return Garbler.garble(LZSEncoding.compressToUTF16(uncompressedStr), keys);
    }
    /**
     * Decompresses text that was compressed with LZ-String compression; does not reverse decryption so it can only
     * decompress Strings produced by {@link #compress(String)}, or {@link #compress(String, long[])} with an empty or
     * null keys parameter.
     * @param compressed text that was compressed by {@link #compress(String)}
     * @return the original text, decompressed from the given text
     */

    public static String decompress(String compressed) {
        return LZSEncoding.decompressFromUTF16(compressed);
    }

    /**
     * Decompresses text that was compressed with LZ-String compression, reversing any encryption if the keys long array
     * matches the long array passed to {@link #compress(String, long[])} (keys can be null if no array was passed).
     * @param compressed text that was compressed by {@link #compress(String, long[])}
     * @param keys the long array that was used to encrypt the output, and must match to decrypt the result; may be null
     * @return the original text, decompressed and decrypted from compressed
     */
    public static String decompress(String compressed, long[] keys) {
        if(keys == null) return LZSEncoding.decompressFromUTF16(compressed);
        if (compressed == null) return null;
        if (compressed.isEmpty()) return "";
        return LZSEncoding.decompressFromUTF16(Garbler.degarble(compressed, keys));
    }
}
