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

package squidpony.store.text;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import squidpony.Converters;
import squidpony.LZSPlus;
import squidpony.StringConvert;
import squidpony.squidmath.OrderedMap;
import squidpony.Garbler;

import java.util.Map;

/**
 * Helps games store information in libGDX's Preferences class as Strings, then get it back out. Does not use JSON,
 * instead using a customized and customizable manual serialization style based around {@link StringConvert}.
 * Created by Tommy Ettinger on 9/16/2016.
 */
public class TextStorage {
    public final Preferences preferences;
    public final String storageName;
    protected OrderedMap<String, String> contents;
    public final StringConvert<OrderedMap<String, String>> mapConverter;
    public boolean compress = true;
    public long[] garbleKey;

    /**
     * Please don't use this constructor if possible; it simply calls {@link #TextStorage(String)} with the constant
     * String "nameless". This could easily overlap with other files/sections in Preferences, so you should always
     * prefer giving a String argument to the constructor, typically the name of the game.
     * @see #TextStorage(String) the recommended constructor to use
     */
    public TextStorage()
    {
        this("nameless");
    }

    /**
     * Creates a JsonStorage with the given fileName to save using Preferences from libGDX. The name should generally
     * be the name of this game or application, and must be a valid name for a file (so no slashes, backslashes, colons,
     * semicolons, or commas for certain, and other non-alphanumeric characters are also probably invalid). You should
     * not assume anything is present in the Preferences storage unless you have put it there, and this applies doubly
     * to games or applications other than your own; you should avoid values for fileName that might overlap with
     * another game's Preferences values.
     * <br>
     * To organize saved data into sub-sections, you specify logical units (like different players' saved games) with a
     * String outerName when you call {@link #store(String)}, and can further distinguish data under the outerName when
     * you call {@link #put(String, Object, StringConvert)} to put each individual item into the saved storage with its
     * own innerName.
     * <br>
     * Calling this also sets up custom serializers for several important types in SquidLib; char[][], OrderedMap,
     * IntDoubleOrderedMap, FakeLanguageGen, GreasedRegion, and notably Pattern from RegExodus all have smaller
     * serialized representations than the default. OrderedMap allows non-String keys, which gets around a limitation in
     * JSON maps normally, and both FakeLanguageGen and Pattern are amazingly smaller with the custom representation.
     * The custom char[][] representation is about half the normal size by omitting commas after each char.
     * @param fileName the valid file name to create or open from Preferences; typically the name of the game/app.
     */
    public TextStorage(final String fileName)
    {
        this(fileName, new long[0]);
    }

    /**
     * Creates a JsonStorage with the given fileName to save using Preferences from libGDX. The name should generally
     * be the name of this game or application, and must be a valid name for a file (so no slashes, backslashes, colons,
     * semicolons, or commas for certain, and other non-alphanumeric characters are also probably invalid). You should
     * not assume anything is present in the Preferences storage unless you have put it there, and this applies doubly
     * to games or applications other than your own; you should avoid values for fileName that might overlap with
     * another game's Preferences values. This constructor also allows you to specify a "garble" String; if this is
     * non-null, it will be used as a key to obfuscate the output and de-obfuscate the loaded input using fairly basic
     * methods. If garble is null, it is ignored.
     * <br>
     * To organize saved data into sub-sections, you specify logical units (like different players' saved games) with a
     * String outerName when you call {@link #store(String)}, and can further distinguish data under the outerName when
     * you call {@link #put(String, Object, StringConvert)} to put each individual item into the saved storage with its
     * own innerName.
     * <br>
     * Calling this also sets up custom serializers for several important types in SquidLib; char[][], OrderedMap,
     * IntDoubleOrderedMap, FakeLanguageGen, GreasedRegion, and notably Pattern from RegExodus all have smaller
     * serialized representations than the default. OrderedMap allows non-String keys, which gets around a limitation in
     * JSON maps normally, and both FakeLanguageGen and Pattern are amazingly smaller with the custom representation.
     * The custom char[][] representation is about half the normal size by omitting commas after each char.
     * @param fileName the valid file name to create or open from Preferences; typically the name of the game/app.
     * @param garble the key that must be used exactly to decrypt any data saved by this TextStorage
     */
    public TextStorage(final String fileName, final String garble)
    {
        storageName = fileName;
        preferences = Gdx.app.getPreferences(storageName);
        contents = new OrderedMap<>(16, 0.2f);
        mapConverter = Converters.convertOrderedMap(Converters.convertString, Converters.convertString);
        garbleKey = Garbler.makeKeyArray(5, garble);
    }

    /**
     * Creates a JsonStorage with the given fileName to save using Preferences from libGDX. The name should generally
     * be the name of this game or application, and must be a valid name for a file (so no slashes, backslashes, colons,
     * semicolons, or commas for certain, and other non-alphanumeric characters are also probably invalid). You should
     * not assume anything is present in the Preferences storage unless you have put it there, and this applies doubly
     * to games or applications other than your own; you should avoid values for fileName that might overlap with
     * another game's Preferences values. This constructor also allows you to specify a "garble" long array; if this is
     * non-empty, it will be used as a key to obfuscate the output and de-obfuscate the loaded input using fairly basic
     * methods. If garble is null or empty, it is ignored.
     * <br>
     * To organize saved data into sub-sections, you specify logical units (like different players' saved games) with a
     * String outerName when you call {@link #store(String)}, and can further distinguish data under the outerName when
     * you call {@link #put(String, Object, StringConvert)} to put each individual item into the saved storage with its
     * own innerName.
     * <br>
     * Calling this also sets up custom serializers for several important types in SquidLib; char[][], OrderedMap,
     * IntDoubleOrderedMap, FakeLanguageGen, GreasedRegion, and notably Pattern from RegExodus all have smaller
     * serialized representations than the default. OrderedMap allows non-String keys, which gets around a limitation in
     * JSON maps normally, and both FakeLanguageGen and Pattern are amazingly smaller with the custom representation.
     * The custom char[][] representation is about half the normal size by omitting commas after each char.
     * @param fileName the valid file name to create or open from Preferences; typically the name of the game/app.
     * @param garble the key that must be used exactly to decrypt any data saved by this TextStorage; will be copied
     */
    public TextStorage(final String fileName, final long[] garble) {
        storageName = fileName;
        preferences = Gdx.app.getPreferences(storageName);
        contents = new OrderedMap<>(16, 0.2f);
        mapConverter = Converters.convertOrderedMap(Converters.convertString, Converters.convertString);
        if (garble == null || garble.length == 0)
            garbleKey = null;
        else {
            garbleKey = new long[garble.length];
            System.arraycopy(garble, 0, garbleKey, 0, garble.length);
        }
    }

    /**
     * Prepares to store the Object {@code o} to be retrieved with {@code innerName} in the current group of objects.
     * Does not write to a permanent location until {@link #store(String)} is called. The innerName used to store an
     * object is required to get it back again, and can also be used to remove it before storing (or storing again).
     * @param innerName one of the two Strings needed to retrieve this later
     * @param o the Object to prepare to store
     * @param converter a StringConvert that supports the type of o
     * @return this for chaining
     */
    @SuppressWarnings("unchecked")
    public <T> TextStorage put(String innerName, T o, StringConvert converter)
    {
        contents.put(innerName, (o == null) ? "" : converter.stringify(o));
        return this;
    }

    /**
     * Actually stores all objects that had previously been prepared with {@link #put(String, Object, StringConvert)},
     * with {@code outerName} used as a key to retrieve any object in the current group. Flushes the preferences, making
     * the changes permanent (until overwritten), but does not change the current group (you may want to call this
     * method again with additional items in the current group, and that would simply involve calling put() again). If
     * you want to clear the current group, use {@link #clear()}. If you want to remove just one object from the current
     * group, use {@link #remove(String)}.
     * @param outerName one of the two Strings needed to retrieve any of the objects in the current group
     * @return this for chaining
     */
    public TextStorage store(String outerName)
    {
        if(garbleKey == null) {
            if (compress)
                preferences.putString(outerName, LZSPlus.compress(mapConverter.stringify(contents)));
            else
                preferences.putString(outerName, mapConverter.stringify(contents));
        }
        else
        {
            if (compress)
                preferences.putString(outerName, LZSPlus.compress(mapConverter.stringify(contents), garbleKey));
            else
                preferences.putString(outerName, Garbler.garble(mapConverter.stringify(contents), garbleKey));
        }
        preferences.flush();
        return this;
    }

    /**
     * Gets a String representation of the data that would be saved when {@link #store(String)} is called. This can be
     * useful for finding particularly problematic objects that require unnecessary space when serialized.
     * @return a String that previews what would be stored permanently when {@link #store(String)} is called
     */
    public String show()
    {

        if(garbleKey == null) {
            if (compress)
                return LZSPlus.compress(mapConverter.stringify(contents));
            else
                return mapConverter.stringify(contents);
        }
        else
        {
            if (compress)
                return LZSPlus.compress(mapConverter.stringify(contents), garbleKey);
            else
                return Garbler.garble(mapConverter.stringify(contents), garbleKey);
        }
    }

    /**
     * Clears the current group of objects; recommended if you intend to store under multiple outerName keys.
     * @return this for chaining
     */
    public TextStorage clear()
    {
        contents.clear();
        return this;
    }

    /**
     * Removes one object from the current group by the {@code innerName} it was prepared with using
     * {@link #put(String, Object, StringConvert)}. This does not affect already-stored objects unless
     * {@link #store(String)} is called after this, in which case the new version of the current group, without the
     * object this removed, is stored.
     * @param innerName the String key used to put an object in the current group with {@link #put(String, Object, StringConvert)}
     * @return this for chaining
     */
    public TextStorage remove(String innerName)
    {
        contents.remove(innerName);
        return this;
    }

    /**
     * Gets an object from the storage by the given {@code outerName} key from {@link #store(String)} and
     * {@code innerName} key from {@link #put(String, Object, StringConvert)}, and uses the class given by {@code type}
     * for the returned value, assuming it matches the object that was originally put with those keys. If no such object
     * exists, returns null. Results are undefined if {@code type} doesn't match the actual class of the stored object.
     * @param outerName the key used to store the group of objects with {@link #store(String)}
     * @param innerName the key used to store the specific object with {@link #put(String, Object, StringConvert)}
     * @param converter
     *                  a StringConvert, such as one from {@link Converters} or found with
     *                  {@link StringConvert#get(CharSequence)}, to deserialize the data
     * @param type the class of the value; for a class like RNG, use {@code RNG.class}, but changed to fit
     * @param <T> the type of the value to retrieve; if type was {@code RNG.class}, this would be {@code RNG}
     * @return the retrieved value if successful, or null otherwise
     */
    public <T> T get(String outerName, String innerName, StringConvert<?> converter, Class<T> type)
    {
        OrderedMap<String, String> om;
        String got;
        if(garbleKey == null) {
            if (compress)
                got = LZSPlus.decompress(preferences.getString(outerName));
            else
                got = preferences.getString(outerName);
        }
        else
        {
            if (compress)
                got = LZSPlus.decompress(preferences.getString(outerName), garbleKey);
            else
                got = Garbler.degarble(preferences.getString(outerName), garbleKey);
        }
        if(got == null) return null;
        om = mapConverter.restore(got);
        if(om == null) return null;
        return converter.restore(om.get(innerName), type);
    }

    /**
     * Gets an object from the storage by the given {@code outerName} key from {@link #store(String)} and
     * {@code innerName} key from {@link #put(String, Object, StringConvert)}, and uses the class given by {@code type}
     * for the returned value, assuming it matches the object that was originally put with those keys. Uses typeName to
     * find an appropriate StringConvert that has already been created (and thus registered), and because typeName is a
     * CharSequence instead of a Class, it doesn't suffer from generic type erasure at runtime, It can and should have
     * the generic type arguments as if it were the type for a variable, e.g. {@code OrderedSet<ArrayList<String>>}. If
     * no such object exists, returns null. Results are undefined if {@code type} doesn't match the actual class of the
     * stored object, and this will return null if there is no known StringConvert for the given typeName.
     * @param outerName the key used to store the group of objects with {@link #store(String)}
     * @param innerName the key used to store the specific object with {@link #put(String, Object, StringConvert)}
     * @param typeName the name of the type to produce, with generic type parameters intact; used to find an appropriate StringConvert
     * @param type the class of the value; for a class like RNG, use {@code RNG.class}, but changed to fit
     * @param <T> the type of the value to retrieve; if type was {@code RNG.class}, this would be {@code RNG}
     * @return the retrieved value if successful, or null otherwise
     */
    public <T> T get(String outerName, String innerName, CharSequence typeName, Class<T> type)
    {
        OrderedMap<String, String> om;
        String got;
        if(garbleKey == null) {
            if (compress)
                got = LZSPlus.decompress(preferences.getString(outerName));
            else
                got = preferences.getString(outerName);
        }
        else
        {
            if (compress)
                got = LZSPlus.decompress(preferences.getString(outerName), garbleKey);
            else
                got = Garbler.degarble(preferences.getString(outerName), garbleKey);
        }
        if(got == null) return null;
        om = mapConverter.restore(got);
        if(om == null) return null;
        StringConvert<?> converter = StringConvert.get(typeName);
        if(converter == null) return null;
        got = om.get(innerName);
        if(got == null) return null;
        return converter.restore(got, type);
    }

    /**
     * Gets the approximate size of the currently-stored preferences. This assumes UTF-16 storage, which is the case for
     * GWT's LocalStorage. Since GWT is restricted to the size the browser permits for LocalStorage, and this limit can
     * be rather small (about 5 MB, sometimes more but not reliably), this method is especially useful there, but it may
     * yield inaccurate sizes on other platforms that save Preferences data differently.
     * @return the size, in bytes, of the already-stored preferences
     */
    public int preferencesSize()
    {
        Map<String, ?> p = preferences.get();
        int byteSize = 0;
        for(String k : p.keySet())
        {
            byteSize += k.length();
            byteSize += preferences.getString(k, "").length();
        }
        return byteSize * 2;
    }

}
