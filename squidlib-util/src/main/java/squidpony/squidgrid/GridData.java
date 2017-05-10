package squidpony.squidgrid;

import squidpony.squidmath.Arrangement;
import squidpony.squidmath.CrossHash;

import java.util.ArrayList;

/**
 * Customized storage of multiple types of 2D data, accessible by String (or CharSequence) keys.
 * Meant for cases where SquidLib needs to produce a grid with multiple types of data, such as what could be represented
 * by a 2D array of objects containing only char, int, double, and/or float data, but this is much more memory-efficient
 * than producing many objects. This can be seen as a "struct-of-array" approach, to use C terminology, rather than an
 * "array-of-struct" approach. This also allows new 2D arrays to be added to an existing GridData if needed.
 */
public class GridData {
    public Arrangement<CharSequence> names;
    public ArrayList<char[][]> charMaps;
    public ArrayList<int[][]> intMaps;
    public ArrayList<double[][]> doubleMaps;
    public ArrayList<float[][]> floatMaps;

    public GridData()
    {
        this(16);
    }
    public GridData(int expectedSize)
    {
        names = new Arrangement<>(expectedSize, CrossHash.stringHasher);
        charMaps = new ArrayList<>(expectedSize);
        intMaps = new ArrayList<>(expectedSize);
        doubleMaps = new ArrayList<>(expectedSize);
        floatMaps = new ArrayList<>(expectedSize);
    }
    public boolean contains(CharSequence item)
    {
        return names.containsKey(item);
    }
    public int indexOf(CharSequence item)
    {
        return names.getInt(item);
    }
    public char[][] getChars(CharSequence item)
    {
        int i = names.getInt(item);
        char[][] d = null;
        if(i >= 0 && i < charMaps.size())
        {
            d = charMaps.get(i);
        }
        return d;
    }

    public char[][] getChars(int index)
    {
        char[][] d = null;
        if(index >= 0 && index < charMaps.size())
        {
            d = charMaps.get(index);
        }
        return d;
    }

    public int[][] getInts(CharSequence item)
    {
        int i = names.getInt(item);
        int[][] d = null;
        if(i >= 0 && i < intMaps.size())
        {
            d = intMaps.get(i);
        }
        return d;
    }

    public int[][] getInts(int index)
    {
        int[][] d = null;
        if(index >= 0 && index < intMaps.size())
        {
            d = intMaps.get(index);
        }
        return d;
    }

    public double[][] getDoubles(CharSequence item)
    {
        int i = names.getInt(item);
        double[][] d = null;
        if(i >= 0 && i < charMaps.size())
        {
            d = doubleMaps.get(i);
        }
        return d;
    }

    public double[][] getDoubles(int index)
    {
        double[][] d = null;
        if(index >= 0 && index < doubleMaps.size())
        {
            d = doubleMaps.get(index);
        }
        return d;
    }

    public float[][] getFloats(CharSequence item)
    {
        int i = names.getInt(item);
        float[][] d = null;
        if(i >= 0 && i < floatMaps.size())
        {
            d = floatMaps.get(i);
        }
        return d;
    }

    public float[][] getFloats(int index)
    {
        float[][] d = null;
        if(index >= 0 && index < floatMaps.size())
        {
            d = floatMaps.get(index);
        }
        return d;
    }

    public int putChars(CharSequence name, char[][] item)
    {
        int i = names.getInt(name);
        if(i < 0) {
            i = names.size();
            names.add(name);
            charMaps.add(item);
            intMaps.add(null);
            doubleMaps.add(null);
            floatMaps.add(null);
        }
        else
        {
            charMaps.set(i, item);
            intMaps.set(i, null);
            doubleMaps.set(i, null);
            floatMaps.set(i, null);
        }
        return i;
    }
    public int putInts(CharSequence name, int[][] item)
    {
        int i = names.getInt(name);
        if(i < 0) {
            i = names.size();
            names.add(name);
            charMaps.add(null);
            intMaps.add(item);
            doubleMaps.add(null);
            floatMaps.add(null);
        }
        else
        {
            charMaps.set(i, null);
            intMaps.set(i, item);
            doubleMaps.set(i, null);
            floatMaps.set(i, null);
        }
        return i;
    }
    public int putDoubles(CharSequence name, double[][] item)
    {
        int i = names.getInt(name);
        if(i < 0) {
            i = names.size();
            names.add(name);
            charMaps.add(null);
            intMaps.add(null);
            doubleMaps.add(item);
            floatMaps.add(null);
        }
        else
        {
            charMaps.set(i, null);
            intMaps.set(i, null);
            doubleMaps.set(i, item);
            floatMaps.set(i, null);
        }
        return i;
    }
    public int putFloats(CharSequence name, float[][] item)
    {
        int i = names.getInt(name);
        if(i < 0) {
            i = names.size();
            names.add(name);
            charMaps.add(null);
            intMaps.add(null);
            doubleMaps.add(null);
            floatMaps.add(item);
        }
        else
        {
            charMaps.set(i, null);
            intMaps.set(i, null);
            doubleMaps.set(i, null);
            floatMaps.set(i, item);
        }
        return i;
    }
}
