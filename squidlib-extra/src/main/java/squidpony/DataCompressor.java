package squidpony;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonWriter;
import squidpony.store.json.JsonCompressor;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A variant of {@link DataConverter} (and an extension of libGDX's {@link com.badlogic.gdx.utils.Json} class) that
 * compresses its JSON output and reads compressed input. Due to limits on the String compression library this uses
 * (namely, it only compresses Strings, so input must be able to be interpreted as a String), this only allows String
 * and FileHandle input formats, and throws exceptions if you try to deserialize a char array, InputStream, or Reader
 * with fromJson() . Otherwise, it acts like DataConverter, so the same docs apply:
 * <br>
 * Augmented version of LibGDX's Json class that knows how to handle various data types common in SquidLib.
 * This includes OrderedMap, which notably allows non-String keys (LibGDX's default Map serializer requires keys to be
 * Strings), but does not currently allow the IHasher to be set (which only should affect OrderedMaps with array keys).
 * It also makes significantly shorter serialized output for 2D char arrays, GreasedRegion and FakeLanguageGen objects,
 * and various collections (IntDoubleOrderedMap, IntVLA, Arrangement, K2, and K2V1 at least).
 * Created by Tommy Ettinger on 1/9/2017.
 */
public class DataCompressor extends JsonCompressor {
    public DataCompressor() {
        super();
    }

    public DataCompressor(JsonWriter.OutputType outputType) {
        super(outputType);
    }

    /**
     * @param object      The object to serialize
     * @param knownType   May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     */
    @Override
    public String toJson(Object object, Class knownType, Class elementType) {
        return super.toJson(object, knownType, elementType);
    }

    /**
     * @param object      The object to serialize
     * @param knownType   May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @param file        A LibGDX FileHandle that can be written to; overwrites, does not append
     */
    @Override
    public void toJson (Object object, Class knownType, Class elementType, FileHandle file) {
        super.toJson(object, knownType, elementType, file);
    }

    /**
     * Don't use this, please! This method doesn't compress its output.
     * @param object      The object to serialize
     * @param knownType   May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @param writer      A Writer that will be the recipient of this class' JSON output
     */
    @Override
    @Deprecated
    public void toJson(Object object, Class knownType, Class elementType, Writer writer) {
        super.toJson(object, knownType, elementType, writer);
    }

    /**
     * @param type   May be null if the type is unknown.
     * @param reader
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, Reader reader) {
        throw new UnsupportedOperationException("fromJson() given a char[], Reader or InputStream won't decompress;" +
                "use the overloads that take a String or FileHandle instead");
    }

    /**
     * @param type        May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @param reader
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, Class elementType, Reader reader) {
        throw new UnsupportedOperationException("fromJson() given a char[], Reader or InputStream won't decompress;" +
                "use the overloads that take a String or FileHandle instead");
    }

    /**
     * @param type  May be null if the type is unknown.
     * @param input
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, InputStream input) {
        throw new UnsupportedOperationException("fromJson() given a char[], Reader or InputStream won't decompress;" +
                "use the overloads that take a String or FileHandle instead");
    }

    /**
     * @param type        May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @param input
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, Class elementType, InputStream input) {
        throw new UnsupportedOperationException("fromJson() given a char[], Reader or InputStream won't decompress;" +
                "use the overloads that take a String or FileHandle instead");
    }

    /**
     * @param type May be null if the type is unknown.
     * @param file
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, FileHandle file) {
        return super.fromJson(type, file);
    }

    /**
     * @param type        May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @param file
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, Class elementType, FileHandle file) {
        return super.fromJson(type, elementType, file);
    }

    /**
     * @param type   May be null if the type is unknown.
     * @param data
     * @param offset
     * @param length
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, char[] data, int offset, int length) {
        throw new UnsupportedOperationException("fromJson() given a char[], Reader or InputStream won't decompress;" +
                "use the overloads that take a String or FileHandle instead");
    }

    /**
     * @param type        May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @param data
     * @param offset
     * @param length
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, Class elementType, char[] data, int offset, int length) {
        throw new UnsupportedOperationException("fromJson() given a char[], Reader or InputStream won't decompress;" +
                "use the overloads that take a String or FileHandle instead");
    }

    /**
     * @param type May be null if the type is unknown.
     * @param json
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, String json) {
        return super.fromJson(type, json);
    }

    /**
     * @param type        May be null if the type is unknown.
     * @param elementType
     * @param json
     * @return May be null.
     */
    @Override
    public <T> T fromJson(Class<T> type, Class elementType, String json) {
        return super.fromJson(type, elementType, json);
    }
}
