package squidpony.squidmath;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class that wraps an RNG and allows different String keys to be associated with biases toward low or high results
 * when a method is called that gets a number from the wrapped RNG. With this, you could make a category of "blessed" or
 * "cursed" biases that, instead of using a uniform distribution that produces all numbers approximately with equal
 * likelihood (with doubles between 0.0 and 1.0 averaging at 0.5), have different averages, like 0.75 for blessed or
 * 0.25 for cursed, when generating between 0.0 and 1.0. You could also use this to favor or disfavor the player for
 * "easy mode" or "hard mode" categories of play.
 * <br>
 * Credit for the technique used here goes to user pjs on StackOverflow, http://stackoverflow.com/a/17796997
 * Created by Tommy Ettinger on 3/20/2016.
 */
public class RandomBias implements Serializable {
    private LinkedHashMap<String, Double> biases;
    public RNG rng;
    private static final long serialVersionUID = 5345874924013134933L;

    public RandomBias()
    {
        biases = new LinkedHashMap<>(32);
        rng = new RNG();
    }
    public RandomBias(RNG rng)
    {
        biases = new LinkedHashMap<>(32);
        this.rng = rng;
    }
    public RandomBias(RNG rng, Map<String, Double> mapping)
    {
        biases = new LinkedHashMap<>(mapping.size());
        double exp;
        for(Map.Entry<String, Double> kv : mapping.entrySet())
        {
            exp = kv.getValue();
            if(exp <= 0) exp = 0.001;
            if(exp >= 1) exp = 0.999;
            biases.put(kv.getKey(), exp);
        }
        this.rng = rng;
    }

    /**
     * Adds a kind of bias that can be used to change the average of random numbers generated when specified with that
     * kind.
     * @param kind a String that will be used as a key in a Map; can be given later on to bias results using this key
     * @param expectedAverage above 0.0 and below 1.0, with 0.5 as the normal average but other values are more useful.
     * @return this for chaining
     */
    public RandomBias putBias(String kind, double expectedAverage)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        biases.put(kind, expectedAverage);
        return this;
    }
    /**
     * Adds a number of kinds of bias that can be used to change the average of random numbers generated when specified
     * with one of those kinds.
     * @param mapping should have String keys that can be used later, and double values greater than 0 but less than 1.
     * @return this for chaining
     */
    public RandomBias putBiases(Map<String, Double> mapping)
    {
        double exp;
        for(Map.Entry<String, Double> kv : mapping.entrySet())
        {
            exp = kv.getValue();
            if(exp <= 0) exp = 0.001;
            if(exp >= 1) exp = 0.999;
            biases.put(kv.getKey(), exp);
        }
        return this;
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a positive long in either case, but
     * not all long values are possible if this is biased, in part because of generating a double, which has less
     * precision than long, and in part because some numbers need to be more common than others. If the kind is not in
     * the map, this generates a positive long, using 63 bits instead of RNG's normal 64 bits since it never generates
     * negative numbers.
     * @param kind the kind of bias to look up
     * @return a random 63-bit positive long, potentially influenced by the bias associated with kind, if present
     */
    public long biasedLong(String kind)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextLong() >>> 1;
        return (long)(Math.pow(rng.nextDouble(), 1.0 / d - 1.0) * Long.MAX_VALUE);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a long between 0 and bound (exclusive
     * on bound), where bound can be negative (and this behavior is allowed even though RNG normally returns 0 for all
     * negative bounds). If the kind is not in the map, this generates a long between 0 and bound (exclusive on bound),
     * even if bound is negative.
     * @param kind the kind of bias to look up
     * @param bound the outer bound, exclusive; can be negative
     * @return a random long between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public long biasedLong(String kind, long bound)
    {
        boolean n = bound < 0;
        Double d = biases.get(kind);
        if(d == null)
            return n ? rng.nextLong(-bound) * -1 : rng.nextLong(bound);
        return (long)(Math.pow(rng.nextDouble(), 1.0 / d - 1.0) * bound);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a double between 0.0 and 1.0
     * (exclusive on 1.0). If the kind is not in the map, this generates a double using RNG and no further changes.
     * @param kind the kind of bias to look up
     * @return a random double between 0.0 and 1.0, potentially influenced by the bias associated with kind, if present
     */
    public double biasedDouble(String kind)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextDouble();
        return Math.pow(rng.nextDouble(), 1.0 / d - 1.0);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a double between 0 and bound (exclusive
     * on bound), where bound can be negative (the same as RNG). If the kind is not in the map, this doesn't adjust the
     * average, and acts exactly like RNG.
     * @param kind the kind of bias to look up
     * @param bound the outer bound, exclusive; can be negative
     * @return a random double between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public double biasedDouble(String kind, double bound)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextDouble(bound);
        return Math.pow(rng.nextDouble(), 1.0 / d - 1.0) * bound;
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a positive int in either case. If the
     * kind is not in the map, this generates a positive int, using 31 bits instead of RNG's normal 32 bits since it
     * never generates negative numbers.
     * @param kind the kind of bias to look up
     * @return a random 31-bit positive int, potentially influenced by the bias associated with kind, if present
     */
    public int biasedInt(String kind)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextInt() >>> 1;
        return (int)(Math.pow(rng.nextDouble(), 1.0 / d - 1.0) * Integer.MAX_VALUE);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be an int between 0 and bound (exclusive
     * on bound), where bound can be negative (and this behavior is allowed even though RNG normally returns 0 for all
     * negative bounds). If the kind is not in the map, this generates an int between 0 and bound (exclusive on bound),
     * even if bound is negative.
     * @param kind the kind of bias to look up
     * @param bound the outer bound, exclusive; can be negative
     * @return a random int between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public int biasedInt(String kind, int bound)
    {
        boolean n = bound < 0;
        Double d = biases.get(kind);
        if(d == null)
            return n ? rng.nextInt(-bound) * -1 : rng.nextInt(bound);
        return (int)(Math.pow(rng.nextDouble(), 1.0 / d - 1.0) * bound);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a float between 0.0 and 1.0
     * (exclusive on 1.0). If the kind is not in the map, this generates a float using RNG and no further changes.
     * @param kind the kind of bias to look up
     * @return a random float between 0.0 and 1.0, potentially influenced by the bias associated with kind, if present
     */
    public float biasedFloat(String kind)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextFloat();
        return (float)(Math.pow(rng.nextDouble(), 1.0 / d - 1.0));
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a float between 0 and bound (exclusive
     * on bound), where bound can be negative. If the kind is not in the map, this doesn't adjust the average.
     * @param kind the kind of bias to look up
     * @param bound the outer bound, exclusive; can be negative
     * @return a random double between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public float biasedFloat(String kind, float bound)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextFloat() * bound;
        return (float)(Math.pow(rng.nextDouble(), 1.0 / d - 1.0) * bound);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be an int between min and max (exclusive
     * on max), where min and/or max can be negative, and the difference between the two can be either positive or
     * negative. If the kind is not in the map, this doesn't adjust the average.
     * @param kind the kind of bias to look up
     * @param min the inner bound, inclusive; can be negative
     * @param max the outer bound, exclusive; can be negative
     * @return a random int between min and max, potentially influenced by the bias associated with kind, if present
     */
    public int biasedBetween(String kind, int min, int max)
    {
        return biasedInt(kind, max - min) + min;
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a long between min and max (exclusive
     * on max), where min and/or max can be negative, and the difference between the two can be either positive or
     * negative. If the kind is not in the map, this doesn't adjust the average.
     * @param kind the kind of bias to look up
     * @param min the inner bound, inclusive; can be negative
     * @param max the outer bound, exclusive; can be negative
     * @return a random long between min and max, potentially influenced by the bias associated with kind, if present
     */
    public long biasedBetween(String kind, long min, long max)
    {
        return biasedLong(kind, max - min) + min;
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned number will be a double between min and max
     * (exclusive on max), where min and/or max can be negative, and the difference between the two can be either
     * positive or negative. If the kind is not in the map, this doesn't adjust the average.
     * @param kind the kind of bias to look up
     * @param min the inner bound, inclusive; can be negative
     * @param max the outer bound, exclusive; can be negative
     * @return a random double between min and max, potentially influenced by the bias associated with kind, if present
     */
    public double biasedBetween(String kind, double min, double max)
    {
        return biasedDouble(kind, max - min) + min;
    }
}
