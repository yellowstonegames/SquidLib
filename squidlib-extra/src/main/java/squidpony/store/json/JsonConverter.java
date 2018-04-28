package squidpony.store.json;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import regexodus.Pattern;
import squidpony.FakeLanguageGen;
import squidpony.Maker;
import squidpony.StringStringMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.util.*;

/**
 * Augmented version of LibGDX's Json class that knows how to handle various data types common in SquidLib.
 * This includes OrderedMap, which notably allows non-String keys (LibGDX's default Map serializer requires keys to be
 * Strings), but does not currently allow the IHasher to be set (which only should affect OrderedMaps with array keys).
 * It also makes significantly shorter serialized output for 2D char arrays, GreasedRegion and FakeLanguageGen objects,
 * and various collections (IntDoubleOrderedMap, IntVLA, Arrangement, K2, and K2V1 at least).
 * Created by Tommy Ettinger on 1/9/2017.
 */
public class JsonConverter extends Json {
    /**
     * Creates a new JsonConverter using "minimal" output type, so it omits double quotes whenever possible but gives
     * up compatibility with most other JSON readers. Give the constructor
     * {@link JsonWriter.OutputType#json} if you need full compatibility.
     */
    public JsonConverter() {
        super();
        initialize(this);
    }

    /**
     * Creates a new JsonConverter with the given OutputType; typically minimal is fine, but compatibility may require
     * you to use json; the javascript type is a sort of middle ground.
     * @param outputType a JsonWriter.OutputType enum value that determines what syntax can be omitted from the output
     */
    public JsonConverter(JsonWriter.OutputType outputType) {
        super(outputType);
        initialize(this);
    }

    public static final Object INVALID = Float.NaN;

    public static void initialize(Json json)
    {
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
        json.addClassTag("#Co", Coord.class);
        json.addClassTag("#Co3", Coord3D.class);
        json.addClassTag("#CoD", CoordDouble.class);
        json.addClassTag("#SSet", SortedSet.class);
        json.addClassTag("#Patt", Pattern.class);
        json.addClassTag("#Grea", GreasedRegion.class);
        json.addClassTag("#IDOM", IntDoubleOrderedMap.class);
        json.addClassTag("#Lang", FakeLanguageGen.class);
        json.addClassTag("#LnAl", FakeLanguageGen.Alteration.class);
        json.addClassTag("#LnMd", FakeLanguageGen.Modifier.class);
        json.addClassTag("#SSMp", StringStringMap.class);
        json.addClassTag("#OMap", OrderedMap.class);
        json.addClassTag("#EOMp", EnumOrderedMap.class);
        json.addClassTag("#OSet", OrderedSet.class);
        json.addClassTag("#UOSt", UnorderedSet.class);
        json.addClassTag("#EOSt", EnumOrderedSet.class);
        json.addClassTag("#Aran", Arrangement.class);
        json.addClassTag("#K2", K2.class);
        json.addClassTag("#K2V1", K2V1.class);
        json.addClassTag("#IVLA", IntVLA.class);
        json.addClassTag("#SVLA", ShortVLA.class);
        json.addClassTag("#SSet", ShortSet.class);
        json.addClassTag("#RNG", RNG.class);
        json.addClassTag("#SRNG", StatefulRNG.class);
        json.addClassTag("#EdiR", EditRNG.class);
        json.addClassTag("#CriR", CriticalRNG.class);
        json.addClassTag("#DhaR", DharmaRNG.class);
        json.addClassTag("#DecR", DeckRNG.class);
        json.addClassTag("#Ligh", LightRNG.class);
        json.addClassTag("#LonP", LongPeriodRNG.class);
        json.addClassTag("#MnwR", MoonwalkRNG.class);
        json.addClassTag("#ThrA", ThrustAltRNG.class);
        json.addClassTag("#JabR", JabRNG.class);
        json.addClassTag("#LthR", Lathe32RNG.class);
        json.addClassTag("#SblR", SobolQRNG.class);
        json.addClassTag("#GWTR", GWTRNG.class);
        json.addClassTag("#XoRo", XoRoRNG.class);
        json.addClassTag("#OriR", Oriole32RNG.class);
        json.addClassTag("#Strm", CrossHash.Storm.class);
        json.addClassTag("#Strm", CrossHash.Mist.class);
        json.addClassTag("#Dir", Direction.class);
        json.addClassTag("#Rad", Radius.class);

        json.setSerializer(Pattern.class, new Serializer<Pattern>() {
            @Override
            public void write(Json json, Pattern object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeValue((Object) object.serializeToString(), String.class);
            }

            @Override
            public Pattern read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                String data = jsonData.asString();
                if(data == null || data.length() < 2) return null;
                return Pattern.deserializeFromString(data);
            }
        });

        json.setSerializer(GreasedRegion.class, new Serializer<GreasedRegion>() {
            @Override
            public void write(Json json, GreasedRegion object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeObjectStart("items", GreasedRegion.class, GreasedRegion.class);
                json.writeValue("w", object.width);
                json.writeValue("h", object.height);
                json.writeValue("d", object.data);
                json.writeObjectEnd();
                json.writeObjectEnd();
            }

            @Override
            public GreasedRegion read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new GreasedRegion(jsonData.get("d").asLongArray(), jsonData.getInt("w"), jsonData.getInt("h"));
            }
        });
        json.setSerializer(IntVLA.class, new Serializer<IntVLA>() {
            @Override
            public void write(Json json, IntVLA object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeValue(object.toArray(), int[].class);
            }

            @Override
            public IntVLA read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new IntVLA(jsonData.asIntArray());
            }
        });

        json.setSerializer(IntDoubleOrderedMap.class, new Serializer<IntDoubleOrderedMap>() {
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

        json.setSerializer(StringStringMap.class, new Serializer<StringStringMap>() {
            @Override
            public void write(Json json, StringStringMap object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("k", object.keysAsOrderedSet(), OrderedSet.class, String.class);
                json.writeValue("v", object.valuesAsList(), ArrayList.class, String.class);
                json.writeValue("f", object.f);
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StringStringMap read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new StringStringMap(json.readValue(OrderedSet.class, String.class, jsonData.get("k")),
                        json.readValue(ArrayList.class, String.class, jsonData.get("v")), jsonData.getFloat("f"));
            }
        });

        json.setSerializer(OrderedMap.class, new Serializer<OrderedMap>() {
            @Override
            public void write(Json json, OrderedMap object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("f", object.f);
                if(!object.isEmpty()) {
                    json.writeValue("k", object.firstKey(), null);
                    json.writeValue("v", object.getAt(0), null);
                    int sz = object.size();
                    Object[] r = new Object[(sz - 1) * 2];
                    for (int i = 1, p = 0; i < sz; i++) {
                        r[p++] = object.keyAt(i);
                        r[p++] = object.getAt(i);
                    }
                    json.writeValue("r", r, Object[].class, Object.class);
                }
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public OrderedMap read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                float f = json.readValue("f", float.class, jsonData);
                Object k = json.readValue("k", null, INVALID, jsonData);
                Object v = json.readValue("v", null, INVALID, jsonData);
                Object[] r = json.readValue("r", Object[].class, jsonData);
                if(k == INVALID)
                    return new OrderedMap(0, f);
                return Maker.makeOM(f, k, v, r);
                //return new OrderedMap(json.readValue(OrderedSet.class, jsonData.get("k")),
                //        json.readValue(ArrayList.class, jsonData.get("v")), jsonData.getFloat("f"));
            }
        });
        json.setSerializer(EnumOrderedMap.class, new Serializer<EnumOrderedMap>() {
            @Override
            public void write(Json json, EnumOrderedMap object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                if(!object.isEmpty()) {
                    json.writeValue("c", object.firstKey().getClass().getName());
                    json.writeValue("k", object.firstKey(), null);
                    json.writeValue("v", object.getAt(0), null);
                    int sz = object.size();
                    Object[] r = new Object[(sz - 1) * 2];
                    for (int i = 1, p = 0; i < sz; i++) {
                        r[p++] = object.keyAt(i);
                        r[p++] = object.getAt(i);
                    }
                    json.writeValue("r", r, Object[].class, Object.class);
                }
                else
                    json.writeValue("c", "default");
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public EnumOrderedMap read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                String c = json.readValue("c", String.class, "", jsonData);
                if("default".equals(c))
                    return new EnumOrderedMap();
                try {
                    Class<? extends Enum<?>> cl = ClassReflection.forName(c);
                    if(!ClassReflection.isEnum(cl))
                        return null;
                    Enum<?> k = json.readValue("k", cl, jsonData);
                    Object v = json.readValue("v", null, INVALID, jsonData);
                    Object[] r = json.readValue("r", Object[].class, jsonData);
                    if(v == INVALID)
                        return new EnumOrderedMap();
                    return Maker.makeEOM(k, v, r);
                } catch (ReflectionException e) {
                    return null;
                }
                //return new OrderedMap(json.readValue(OrderedSet.class, jsonData.get("k")),
                //        json.readValue(ArrayList.class, jsonData.get("v")), jsonData.getFloat("f"));
            }
        });
        json.setSerializer(EnumOrderedSet.class, new Serializer<EnumOrderedSet>() {
            @Override
            public void write(Json json, EnumOrderedSet object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                if(!object.isEmpty()) {
                    json.writeValue("c", object.first().getClass().getName());
                    json.writeValue("i", object.first(), null);
                    int sz = object.size();
                    Object[] r = new Object[sz - 1];
                    for (int i = 1, p = 0; i < sz; i++) {
                        r[p++] = object.getAt(i);
                    }
                    json.writeValue("r", r, Object[].class, Object.class);
                }
                else
                    json.writeValue("c", "default");
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public EnumOrderedSet read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                String c = json.readValue("c", String.class, "", jsonData);
                if("default".equals(c))
                    return new EnumOrderedSet();
                try {
                    Class<? extends Enum<?>> cl = ClassReflection.forName(c);
                    if(!ClassReflection.isEnum(cl))
                        return null;
                    Enum<?> i = json.readValue("i", cl, jsonData);
                    Object[] r = json.readValue("r", Object[].class, jsonData);
                    return Maker.makeEOS(i, (Enum<?>[]) r);
                } catch (ReflectionException e) {
                    return null;
                }
                //return new OrderedMap(json.readValue(OrderedSet.class, jsonData.get("k")),
                //        json.readValue(ArrayList.class, jsonData.get("v")), jsonData.getFloat("f"));
            }
        });

        json.setSerializer(EnumMap.class, new Serializer<EnumMap>() {
            @Override
            public void write(Json json, EnumMap object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                if(!object.isEmpty()) {
                    Iterator it = object.entrySet().iterator();
                    Map.Entry en = (Map.Entry)it.next();
                    json.writeValue("e", en.getKey(), Enum.class);
                    json.writeValue("v", en.getValue(), null);
                    int sz = object.size();
                    Object[] r = new Object[(sz - 1) * 2];
                    for (int i = 1, p = 0; i < sz; i++) {
                        if(!it.hasNext())
                            break;
                        en = (Map.Entry)it.next();
                        r[p++] = en.getKey();
                        r[p++] = en.getValue();
                    }
                    json.writeValue("r", r, Object[].class, Object.class);
                }
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public EnumMap read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull() || jsonData.size == 0) return null;
                return new EnumMap(Maker.makeOM(0.75f,
                        json.readValue("e", null, jsonData),
                        json.readValue("v", null, jsonData),
                        json.readValue("r", Object[].class, jsonData)));
                //return new OrderedMap(json.readValue(OrderedSet.class, jsonData.get("k")),
                //        json.readValue(ArrayList.class, jsonData.get("v")), jsonData.getFloat("f"));
            }
        });
        json.setSerializer(Arrangement.class, new Serializer<Arrangement>() {
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

        json.setSerializer(K2.class, new Serializer<K2>() {
            @Override
            public void write(Json json, K2 object, Class knownType) {
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
            public K2 read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new K2(json.readValue(SortedSet.class, jsonData.get("a")), json.readValue(SortedSet.class, jsonData.get("b")));
            }
        });

        json.setSerializer(K2V1.class, new Serializer<K2V1>() {
            @Override
            public void write(Json json, K2V1 object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                json.writeObjectStart();
                json.writeValue("a", object.getSetA(), SortedSet.class);
                json.writeValue("b", object.getSetB(), SortedSet.class);
                json.writeValue("q", object.getListQ(), ArrayList.class);
                json.writeObjectEnd();
            }

            @Override
            @SuppressWarnings("unchecked")
            public K2V1 read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull()) return null;
                return new K2V1(
                        json.readValue(SortedSet.class, jsonData.get("a")),
                        json.readValue(SortedSet.class, jsonData.get("b")),
                        json.readValue(ArrayList.class, jsonData.get("q")));
            }
        });

        json.setSerializer(char[][].class, new Serializer<char[][]>() {
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
        json.setSerializer(FakeLanguageGen.class, new Serializer<FakeLanguageGen>() {
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
    }
}
