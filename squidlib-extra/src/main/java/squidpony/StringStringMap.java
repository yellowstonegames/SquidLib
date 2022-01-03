package squidpony;

import squidpony.squidmath.OrderedMap;

import java.util.Collection;

/**
 * Created by Tommy Ettinger on 1/2/2017.
 */
public class StringStringMap extends OrderedMap<String, String>
{
    public StringStringMap()
    {
        super();
    }
    public StringStringMap(int size, float f)
    {
        super(size, f);
    }
    public StringStringMap(Collection<String> k, Collection<String> v, float f)
    {
        super(k, v, f);
    }
}