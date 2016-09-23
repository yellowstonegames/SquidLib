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
    public SquidStorage()
    {
        this("nameless");
    }
    public SquidStorage(final String name)
    {
        storageName = name;
        preferences = Gdx.app.getPreferences(storageName);
        json = new Json(JsonWriter.OutputType.minimal);

        json.setSerializer(Pattern.class, new Json.Serializer<Pattern>() {
            @Override
            public void write(Json json, Pattern object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Pattern read(Json json, JsonValue jsonData, Class type) {
                return Pattern.deserializeFromString(jsonData.asString());
            }
        });

        json.setSerializer(GreasedRegion.class, new Json.Serializer<GreasedRegion>() {
            @Override
            public void write(Json json, GreasedRegion object, Class knownType) {
                json.writeObjectStart();
                json.writeValue("w", object.width);
                json.writeValue("h", object.height);
                json.writeValue("d", object.data);
                json.writeObjectEnd();
            }

            @Override
            public GreasedRegion read(Json json, JsonValue jsonData, Class type) {
                return new GreasedRegion(jsonData.get("d").asLongArray(), jsonData.getInt("w"), jsonData.getInt("h"));
            }
        });

        json.setSerializer(IntDoubleOrderedMap.class, new Json.Serializer<IntDoubleOrderedMap>() {
            @Override
            public void write(Json json, IntDoubleOrderedMap object, Class knownType) {
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
                return new IntDoubleOrderedMap(jsonData.get("k").asIntArray(), jsonData.get("v").asDoubleArray(), jsonData.getFloat("f"));
            }
        });

        json.setSerializer(OrderedMap.class, new Json.Serializer<OrderedMap>() {
            @Override
            public void write(Json json, OrderedMap object, Class knownType) {
                json.writeObjectStart();
                int sz = object.size();
                json.writeValue("k", object.keysAsOrderedSet(), OrderedSet.class);
                json.writeValue("v", object.valuesAsList(), ArrayList.class);
                json.writeValue("f", object.f);
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public OrderedMap read(Json json, JsonValue jsonData, Class type) {
                return new OrderedMap(json.readValue(OrderedSet.class, jsonData.get("k")),
                        json.readValue(ArrayList.class, jsonData.get("v")), jsonData.getFloat("f"));
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
                if(jsonData.isNull())
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
                json.writeValue(object.serializeToString());
            }

            @Override
            public FakeLanguageGen read(Json json, JsonValue jsonData, Class type) {
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
        json.addClassTag("#Grea", GreasedRegion.class);
        json.addClassTag("#IDOM", IntDoubleOrderedMap.class);
        json.addClassTag("#Lang", FakeLanguageGen.class);
        json.addClassTag("#LnAl", FakeLanguageGen.Alteration.class);
        json.addClassTag("#LnMd", FakeLanguageGen.Modifier.class);
        json.addClassTag("#OMap", OrderedMap.class);
        json.addClassTag("#OSet", OrderedSet.class);
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
    public SquidStorage put(String innerName, Object o)
    {
        contents.put(innerName, json.toJson(o));
        return this;
    }
    public SquidStorage store(String outerName)
    {
        if(compress)
            preferences.putString(outerName, LZSEncoding.compressToUTF16(json.toJson(contents, OrderedMap.class)));
        else
            preferences.putString(outerName, json.toJson(contents, OrderedMap.class));
        preferences.flush();
        return this;
    }
    public String show()
    {
        if(compress)
            return LZSEncoding.compressToUTF16(json.toJson(contents, OrderedMap.class));
        else
            return json.toJson(contents, OrderedMap.class);
    }
    public SquidStorage clear()
    {
        contents.clear();
        return this;
    }
    public SquidStorage remove(String innerName)
    {
        contents.remove(innerName);
        return this;
    }
    @SuppressWarnings("unchecked")
    public <T> T get(String outerName, String innerName, Class<T> type)
    {
        OrderedMap<String, String> om;
        if(compress)
            om = json.fromJson(OrderedMap.class, LZSEncoding.decompressFromUTF16(preferences.getString(outerName)));
        else
            om = json.fromJson(OrderedMap.class, preferences.getString(outerName));

        return json.fromJson(type, om.get(innerName));
    }
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
