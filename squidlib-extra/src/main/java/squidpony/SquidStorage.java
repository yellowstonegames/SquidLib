package squidpony;

import blazing.chain.LZSEncoding;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import regexodus.Pattern;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedSet;

/**
 * Helps games store information in libGDX's Preferences class as Strings, then get it back out.
 * Created by Tommy Ettinger on 9/16/2016.
 */
@Beta
public class SquidStorage {
    public final Preferences preferences;
    public final String storageName;
    public final Json json;
    protected OrderedMap<String, String> contents;
    public boolean compress = true;

    /**
     * Please don't use this constructor if possible; it simply calls {@link #SquidStorage(String)} with the constant
     * String "nameless". This could easily overlap with other files/sections in Preferences, so you should always
     * prefer giving a String argument to the constructor, typically the name of the game.
     * @see #SquidStorage(String) the recommended constructor to use
     */
    public SquidStorage()
    {
        this("nameless");
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
        storageName = fileName;
        preferences = Gdx.app.getPreferences(storageName);
        json = new Json(JsonWriter.OutputType.minimal);

        json.setSerializer(Pattern.class, new Json.Serializer<Pattern>() {
            @Override
            public void write(Json json, Pattern object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeValue(object.serializeToString());
            }

            @Override
            public Pattern read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                String data = jsonData.asString();
                if(data == null || data.length() < 2) return null;
                return Pattern.deserializeFromString(data);
            }
        });

        json.setSerializer(GreasedRegion.class, new Json.Serializer<GreasedRegion>() {
            @Override
            public void write(Json json, GreasedRegion object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("w", object.width);
                json.writeValue("h", object.height);
                json.writeValue("d", object.data);
                json.writeObjectEnd();
            }

            @Override
            public GreasedRegion read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new GreasedRegion(jsonData.get("d").asLongArray(), jsonData.getInt("w"), jsonData.getInt("h"));
            }
        });

        json.setSerializer(IntDoubleOrderedMap.class, new Json.Serializer<IntDoubleOrderedMap>() {
            @Override
            public void write(Json json, IntDoubleOrderedMap object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeArrayStart("k");
                IntDoubleOrderedMap.KeyIterator ki = object.keySet().iterator();
                while (ki.hasNext())
                    json.writeValue(ki.nextInt());
                json.writeArrayEnd();
                json.writeArrayStart("v");
                IntDoubleOrderedMap.DoubleIterator vi = object.values().iterator();
                while (vi.hasNext())
                    json.writeValue(vi.nextDouble());
                json.writeArrayEnd();
                json.writeValue("f", object.f);
                json.writeObjectEnd();
            }

            @Override
            public IntDoubleOrderedMap read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new IntDoubleOrderedMap(jsonData.get("k").asIntArray(), jsonData.get("v").asDoubleArray(), jsonData.getFloat("f"));
            }
        });

        json.setSerializer(OrderedMap.class, new Json.Serializer<OrderedMap>() {
            @Override
            public void write(Json json, OrderedMap object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("k", object.keysAsOrderedSet(), OrderedSet.class);
                json.writeValue("v", object.valuesAsList(), ArrayList.class);
                json.writeValue("f", object.f);
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public OrderedMap read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new OrderedMap(json.readValue(OrderedSet.class, jsonData.get("k")),
                        json.readValue(ArrayList.class, jsonData.get("v")), jsonData.getFloat("f"));
            }
        });

        json.setSerializer(Arrangement.class, new Json.Serializer<Arrangement>() {
            @Override
            public void write(Json json, Arrangement object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("k", object.keysAsOrderedSet(), OrderedSet.class);
                json.writeValue("f", object.f);
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public Arrangement read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new Arrangement(json.readValue(OrderedSet.class, jsonData.get("k")), jsonData.getFloat("f"));
            }
        });

        json.setSerializer(TwoKey.class, new Json.Serializer<TwoKey>() {
            @Override
            public void write(Json json, TwoKey object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("a", object.getSetA(), SortedSet.class);
                json.writeValue("b", object.getSetB(), SortedSet.class);
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public TwoKey read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new TwoKey(json.readValue(SortedSet.class, jsonData.get("a")), json.readValue(SortedSet.class, jsonData.get("b")));
            }
        });

        json.setSerializer(char[][].class, new Json.Serializer<char[][]>() {
            @Override
            public void write(Json json, char[][] object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                int sz = object.length;
                json.writeArrayStart();
                for (int i = 0; i < sz; i++) {
                    json.writeValue(String.valueOf(object[i]));
                }
                json.writeArrayEnd();
            }

            @Override
            public char[][] read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull())
                    return null;
                int sz = jsonData.size;
                char[][] data = new char[sz][];
                JsonValue c = jsonData.child();
                for (int i = 0; i < sz && c != null; i++, c = c.next()) {
                    data[i] = c.asString().toCharArray();
                }
                return data;
            }
        });
        json.setSerializer(FakeLanguageGen.class, new Json.Serializer<FakeLanguageGen>() {
            @Override
            public void write(Json json, FakeLanguageGen object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeValue(object.serializeToString());
            }

            @Override
            public FakeLanguageGen read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull())
                    return null;
                return FakeLanguageGen.deserializeFromString(jsonData.asString());
            }
        });
        json.addClassTag("#St", String.class);
        json.addClassTag("#Z", Boolean.class);
        json.addClassTag("#z", boolean.class);
        json.addClassTag("#B", Byte.class);
        json.addClassTag("#b", byte.class);
        json.addClassTag("#S", Short.class);
        json.addClassTag("#s", short.class);
        json.addClassTag("#C", Character.class);
        json.addClassTag("#c", char.class);
        json.addClassTag("#I", Integer.class);
        json.addClassTag("#i", int.class);
        json.addClassTag("#F", Float.class);
        json.addClassTag("#f", float.class);
        json.addClassTag("#L", Long.class);
        json.addClassTag("#l", long.class);
        json.addClassTag("#D", Double.class);
        json.addClassTag("#d", double.class);
        json.addClassTag("#SSet", SortedSet.class);
        json.addClassTag("#Patt", Pattern.class);
        /*
        json.addClassTag("#Mtch", Matcher.class);
        json.addClassTag("#Rplc", Replacer.class);
        json.addClassTag("#Sbst", Substitution.class);
        */
        json.addClassTag("#Grea", GreasedRegion.class);
        json.addClassTag("#IDOM", IntDoubleOrderedMap.class);
        json.addClassTag("#Lang", FakeLanguageGen.class);
        json.addClassTag("#LnAl", FakeLanguageGen.Alteration.class);
        json.addClassTag("#LnMd", FakeLanguageGen.Modifier.class);
        json.addClassTag("#OMap", OrderedMap.class);
        json.addClassTag("#OSet", OrderedSet.class);
        json.addClassTag("#Aran", Arrangement.class);
        json.addClassTag("#Key2", TwoKey.class);
        json.addClassTag("#IVLA", IntVLA.class);
        json.addClassTag("#SVLA", ShortVLA.class);
        json.addClassTag("#RNG", RNG.class);
        json.addClassTag("#SRNG", StatefulRNG.class);
        json.addClassTag("#EdiR", EditRNG.class);
        json.addClassTag("#DhaR", DharmaRNG.class);
        json.addClassTag("#DecR", DeckRNG.class);
        json.addClassTag("#Ligh", LightRNG.class);
        json.addClassTag("#LonP", LongPeriodRNG.class);
        json.addClassTag("#Thun", ThunderRNG.class);
        json.addClassTag("#XoRo", XoRoRNG.class);
        json.addClassTag("#XorR", XorRNG.class);
        json.addClassTag("#Strm", CrossHash.Storm.class);

        contents = new OrderedMap<>(16, 0.2f);
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
        contents.put(innerName, json.toJson(o));
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
        if(compress)
            preferences.putString(outerName, LZSEncoding.compressToUTF16(json.toJson(contents, OrderedMap.class)));
        else
            preferences.putString(outerName, json.toJson(contents, OrderedMap.class));
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
        if(compress)
            return LZSEncoding.compressToUTF16(json.toJson(contents, OrderedMap.class));
        else
            return json.toJson(contents, OrderedMap.class);
    }

    /**
     * Clears the current group of objects; recommended if you intend to store under multiple outerName keys.
     * @return this for chaining
     */
    public SquidStorage clear()
    {
        contents.clear();
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
        contents.remove(innerName);
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
        OrderedMap<String, String> om;
        String got;
        if(compress)
            got = LZSEncoding.decompressFromUTF16(preferences.getString(outerName));
        else
            got = preferences.getString(outerName);
        if(got == null) return null;
        om = json.fromJson(OrderedMap.class, got);
        if(om == null) return null;
        return json.fromJson(type, om.get(innerName));
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
