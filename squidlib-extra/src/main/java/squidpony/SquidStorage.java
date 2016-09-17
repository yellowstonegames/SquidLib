package squidpony;

import blazing.chain.LZSEncoding;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import regexodus.Pattern;
import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Tommy Ettinger on 9/16/2016.
 */
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
    public SquidStorage(String name)
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
        json.addClassTag("S_", String.class);
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
