package squidpony;

import squidpony.annotation.Beta;
import squidpony.store.json.JsonStorage;

/**
 * Helps games store information in libGDX's Preferences class as Strings, then get it back out.
 * Created by Tommy Ettinger on 9/16/2016.
 */
@Beta
public class SquidStorage extends JsonStorage {
    /**
     * Please don't use this constructor if possible; it simply calls {@link #SquidStorage(String)} with the constant
     * String "nameless". This could easily overlap with other files/sections in Preferences, so you should always
     * prefer giving a String argument to the constructor, typically the name of the game.
     * @see #SquidStorage(String) the recommended constructor to use
     */
    public SquidStorage()
    {
        super("nameless");
    }

    /**
     * Creates a SquidStorage with the given fileName to save using Preferences from libGDX. The name should generally
     * be the name of this game or application, and must be a valid name for a file (so no slashes, backslashes, colons,
     * semicolons, or commas for certain, and other non-alphanumeric characters are also probably invalid). You should
     * not assume anything is present in the Preferences storage unless you have put it there, and this applies doubly
     * to games or applications other than your own; you should avoid values for fileName that might overlap with
     * another game's Preferences values.
     * <br>
     * To organize saved data into sub-sections, you specify logical units (like different players' saved games) with a
     * String outerName when you call {@link #store(String)}, and can further distinguish data under the outerName when
     * you call {@link #put(String, Object)} to put each individual item into the saved storage with its own innerName.
     * <br>
     * Calling this also sets up custom serializers for several important types in SquidLib; char[][], OrderedMap,
     * IntDoubleOrderedMap, FakeLanguageGen, GreasedRegion, and notably Pattern from RegExodus all have smaller
     * serialized representations than the default. OrderedMap allows non-String keys, which gets around a limitation in
     * JSON maps normally, and both FakeLanguageGen and Pattern are amazingly smaller with the custom representation.
     * The custom char[][] representation is about half the normal size by omitting commas after each char.
     * @param fileName the valid file name to create or open from Preferences; typically the name of the game/app.
     */
    public SquidStorage(final String fileName)
    {
        super(fileName);
    }
    /**
     * Creates a SquidStorage with the given fileName to save using Preferences from libGDX. The name should generally
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
     * you call {@link #put(String, Object)} to put each individual item into the saved storage with its own innerName.
     * <br>
     * Calling this also sets up custom serializers for several important types in SquidLib; char[][], OrderedMap,
     * IntDoubleOrderedMap, FakeLanguageGen, GreasedRegion, and notably Pattern from RegExodus all have smaller
     * serialized representations than the default. OrderedMap allows non-String keys, which gets around a limitation in
     * JSON maps normally, and both FakeLanguageGen and Pattern are amazingly smaller with the custom representation.
     * The custom char[][] representation is about half the normal size by omitting commas after each char.
     * @param fileName the valid file name to create or open from Preferences; typically the name of the game/app.
     * @param garble a String that will be used as a key to obfuscate the saved output if non-null
     */
    public SquidStorage(final String fileName, final String garble)
    {
        super(fileName, garble);
    }
    /**
     * Prepares to store the Object {@code o} to be retrieved with {@code innerName} in the current group of objects.
     * Does not write to a permanent location until {@link #store(String)} is called. The innerName used to store an
     * object is required to get it back again, and can also be used to remove it before storing (or storing again).
     * @param innerName one of the two Strings needed to retrieve this later
     * @param o the Object to prepare to store
     * @return this for chaining
     */
    public SquidStorage put(String innerName, Object o)
    {
        super.put(innerName, o);
        return this;
    }

    /**
     * Actually stores all objects that had previously been prepared with {@link #put(String, Object)}, with
     * {@code outerName} used as a key to retrieve any object in the current group. Flushes the preferences, making the
     * changes permanent (until overwritten), but does not change the current group (you may want to call this method
     * again with additional items in the current group, and that would simply involve calling put() again). If you want
     * to clear the current group, use {@link #clear()}. If you want to remove just one object from the current group,
     * use {@link #remove(String)}.
     * @param outerName one of the two Strings needed to retrieve any of the objects in the current group
     * @return this for chaining
     */
    public SquidStorage store(String outerName)
    {
        super.store(outerName);
        return this;
    }

    /**
     * Gets a String representation of the data that would be saved when {@link #store(String)} is called. This can be
     * useful for finding particularly problematic objects that require unnecessary space when serialized.
     * @return a String that previews what would be stored permanently when {@link #store(String)} is called
     */
    public String show()
    {
        return super.show();
    }

    /**
     * Clears the current group of objects; recommended if you intend to store under multiple outerName keys.
     * @return this for chaining
     */
    public SquidStorage clear()
    {
        super.clear();
        return this;
    }

    /**
     * Removes one object from the current group by the {@code innerName} it was prepared with using
     * {@link #put(String, Object)}. This does not affect already-stored objects unless {@link #store(String)} is called
     * after this, in which case the new version of the current group, without the object this removed, is stored.
     * @param innerName the String key used to put an object in the current group with {@link #put(String, Object)}
     * @return this for chaining
     */
    public SquidStorage remove(String innerName)
    {
        super.remove(innerName);
        return this;
    }

    /**
     * Gets an object from the storage by the given {@code outerName} key from {@link #store(String)} and
     * {@code innerName} key from {@link #put(String, Object)}, and uses the class given by {@code type} for the
     * returned value, assuming it matches the object that was originally put with those keys. If no such object is
     * present, returns null. Results are undefined if {@code type} doesn't match the actual class of the stored object.
     * @param outerName the key used to store the group of objects with {@link #store(String)}
     * @param innerName the key used to store the specific object with {@link #put(String, Object)}
     * @param type the class of the value; for a class like RNG, use {@code RNG.class}, but changed to fit
     * @param <T> the type of the value to retrieve; if type was {@code RNG.class}, this would be {@code RNG}
     * @return the retrieved value if successful, or null otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String outerName, String innerName, Class<T> type)
    {
        return super.get(outerName, innerName, type);
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
        return super.preferencesSize();
    }

}
