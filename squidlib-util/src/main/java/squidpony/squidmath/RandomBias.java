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

package squidpony.squidmath;

import java.io.Serializable;
import java.util.Map;

/**
 * A class that wraps an RNG and allows different String keys to be associated with biases toward low or high results
 * when a method is called that gets a number from the wrapped RNG. With this, you could make a category of "blessed" or
 * "cursed" biases that, instead of using a uniform distribution that produces all numbers approximately with equal
 * likelihood (with doubles between 0.0 and 1.0 averaging at 0.5), have different averages, like 0.7 for blessed or 0.3
 * for cursed, when generating between 0.0 and 1.0. You could also use this to favor or disfavor the player for "easy
 * mode" or "hard mode" categories of play.
 * <br>
 * The API allows you to associate an alternative average with a kind as a String, like "blessed to-hit" or "hard mode
 * enemy damage", if you expect to use that number more than once and might want to tweak any averages by changing one
 * number at a later point. You can also give an average as a double in place of a kind as a String, which avoids a
 * HashMap lookup and lets you give flexibly-adjusted numbers, but does need more effort to change many values
 * throughout a codebase if averages aren't all generated by a formula. You can also set the distribution in the
 * constructor or by changing the public distribution field; you can use constants in this class, TRIANGULAR,
 * EXPONENTIAL, TRUNCATED, SOFT_TRIANGULAR, and EXP_TRI (the average of EXPONENTIAL and TRIANGULAR), for different
 * choices, with the default being EXP_TRI. Each one of these has different behavior regarding a preference toward
 * extreme values; TRIANGULAR almost never produces very high or very low values, EXPONENTIAL frequently produces the
 * highest or lowest values for high or low expected averages, respectively, TRUNCATED will simply never generate values
 * that are too far from the average (otherwise it's uniform), SOFT_TRIANGULAR will produce a rounded version of
 * TRIANGULAR's distribution with less of an angular peak and more frequent high and low values, and EXP_TRI will have
 * something like a curve shape that may "collide" slightly with the upper bound if the average is high enough.
 * <br>
 * Credit for the technique used for the exponential modification to distributions goes to user pjs on StackOverflow,
 * http://stackoverflow.com/a/17796997 .
 * Credit should also be given to user vydd of the LispGames community, who made a visualization of the distribution
 * changing as the expected average changed (at the time, the typical behavior of an exponential distribution looked
 * like a bug, and the visualization showed that it was correct behavior). Derrick Creamer noticed how strange the
 * exponential distribution would seem to most players, and that led to adding the simple triangular distribution.
 * Created by Tommy Ettinger on 3/20/2016.
 */
public class RandomBias implements Serializable {
    private OrderedMap<String, Double> biases;
    public IRNG rng;
    public int distribution = EXP_TRI;

    /**
     * A constant for a distribution that linearly increases in probability from a 0.0 chance of 0.0. to a 0.3333...
     * chance of getting the expected average, then linearly decreases until it reaches a 0.0 chance of 1.0. Doesn't
     * really support expected averages below 1/3 or above 2/3, due to how the triangular distribution works.
     */
    public static final int TRIANGULAR = 0,
    /**
     * A constant for a distribution that, for all values other than 0.5, will strongly favor either high or low
     * results to push the odds in favor of a high or low expected average. This is probably not what most players
     * expect, since it leads to massively more critical hits or failures if min or max results are counted as such.
     */
    EXPONENTIAL = 1,
    /**
     * Not like the other distributions; this is a constant for a distribution that simply truncates a random number's
     * possible range to less than 1.0, and adjusts the minimum or maximum value so that the average is the desired one.
     * This is a uniform random number generator, unlike the others which have a bias toward certain values; it simply
     * cannot generate values outside a certain range, and the values within the range it generates are all equally
     * likely. The range gets smaller the closer the expected average is to 0.0 or 1.0, with an expected average of 0.4
     * producing values between 0.0 and 0.8, and an expected average of 0.9 producing values of 0.8 to 1.0 (in all
     * cases, this is exclusive on the upper bound).
     */
    TRUNCATED = 2,
    /**
     * A constant for a distribution that averages two random floats, each with a triangular distribution (the same as
     * what using the TRIANGULAR constant would produce, but the distribution becomes more curved when multiple random
     * "dice rolls" are involved), to soften the point of the triangle and make very high or very low values appear
     * somewhat more frequently, while the expected average appears less frequently. This should not be used to generate
     * very large numbers, since the floats this uses lose precision after 24 bits, or about 16 million. It should
     * produce very reasonable results for common values in games, like 0 to 100 or 0 to 20. Doesn't really support
     * expected averages below 1/3 or above 2/3, due to how the triangular distribution works.
     */
    SOFT_TRIANGULAR = 3,
    /**
     * A constant for a distribution that averages two random floats, one with a triangular distribution (the same as
     * what using the TRIANGULAR constant would produce), and one with an exponential distribution (the same as what
     * using the EXPONENTIAL constant would produce) to soften the point of the triangle and make very high or very low
     * values appear much more frequently, while the expected average appears somewhat less frequently. This should not
     * be used to generate very large numbers, since the floats this uses lose precision after 24 bits, or about 16
     * million. It should produce very reasonable results for common values in games, like 0 to 100 or 0 to 20. Has
     * limited support for expected averages below 1/3 or above 2/3; unlike TRIANGULAR or SOFT_TRIANGULAR, expected
     * averages outside that range will still affect the generated average due to the EXPONENTIAL distribution
     * contributing half of the correction needed to match the expected average. An expected average of 5/6 will produce
     * an approximate average with this of 3/4, as opposed to 2/3 (for pure TRIANGULAR) or 5/6 (for EXPONENTIAL).
     */
    EXP_TRI = 4,
    /**
     * "Bathtub-shaped" or "U-shaped" distribution (technically the arcsine distribution) that is significantly more
     * likely to produce results at either extreme than it is to generate them in the center. The extremes in this case
     * are the same as the truncated distribution, so not all values are possible unless the expected average is 0.5.
     */
    BATHTUB_TRUNCATED = 5;

    private static final long serialVersionUID = 4245874924013134958L;

    public RandomBias()
    {
        biases = new OrderedMap<>(32);
        rng = new GWTRNG();
    }
    public RandomBias(IRNG rng)
    {
        this.rng = rng;
        biases = new OrderedMap<>(32);
    }
    public RandomBias(IRNG rng, Map<String, Double> mapping)
    {
        this(rng, mapping, EXP_TRI);
    }
    public RandomBias(IRNG rng, Map<String, Double> mapping, int distribution) {
        this.rng = rng;
        this.distribution = distribution;
        if (mapping == null) {
            biases = new OrderedMap<>(32);
        } else {
            biases = new OrderedMap<>(mapping.size());
            double exp;
            for (Map.Entry<String, Double> kv : mapping.entrySet()) {
                exp = kv.getValue();
                if (exp <= 0) exp = 0.001;
                if (exp >= 1) exp = 0.999;
                biases.put(kv.getKey(), exp);
            }
        }
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

    private double quantile(double expected)
    {
        switch (distribution)
        {
            case EXPONENTIAL: return exponentialQuantile(expected);
            case TRUNCATED: return truncatedQuantile(expected);
            case TRIANGULAR: return triangularQuantile(expected);
            case SOFT_TRIANGULAR: return softQuantile(expected);
            case BATHTUB_TRUNCATED: return bathtubTruncatedQuantile(expected);
            default: return mixQuantile(expected);
        }
    }

    private double triangularQuantile(double expected)
    {
        expected = Math.max(0.001, Math.min(0.999, expected * 3.0 - 1.0));
        double p = rng.nextDouble();
        if(p < expected)
            return Math.sqrt(expected * p);
        if(p > expected)
            return 1 - Math.sqrt((1 - expected) * (1 - p));
        return expected;
    }
    private double truncatedQuantile(double expected)
    {
        if(expected >= 0.5)
            return rng.nextDouble() * (1.0 - expected) * 2 + expected - (1.0 - expected);
        return rng.nextDouble() * expected * 2;
    }
    private double bathtubQuantile(double expected)
    {
        expected = Math.sin(expected * Math.PI * 0.4999999966); // can't be 0.5 because it becomes inclusive on 1.0
        return expected * expected;
    }
    private double bathtubTruncatedQuantile(double expected)
    {
        if(expected >= 0.5)
            return bathtubQuantile(rng.nextDouble()) * (0.9999999999999999 - expected) * 2 + expected - (0.9999999999999999 - expected);
        return bathtubQuantile(rng.nextDouble()) * expected * 2;
    }

    private double exponentialQuantile(double expected)
    {
        return 0.9999999999999999 - Math.pow( rng.nextDouble(), 1.0 / (1.0 - expected) - 1.0);
    }

    private double softQuantile(double expected)
    {
        expected = Math.max(0.001, Math.min(0.999, expected * 3.0 - 1.0));
        long pair = rng.nextLong();
        float left = (pair >>> 40) * 0x1p-24f, right = (pair & 0xFFFFFFL) * 0x1p-24f;
        double v;

        if(left < expected)
            v = Math.sqrt(expected * left);
        else if(left > expected)
            v = 1 - Math.sqrt((1 - expected) * (1 - left));
        else
            v = expected;
        if(right < expected)
            return (v + Math.sqrt(expected * right)) * 0.5;
        if(right > expected)
            return (v + 1 - Math.sqrt((1 - expected) * (1 - right))) * 0.5;
        return expected;
    }
    private double mixQuantile(double expected)
    {
        double d2 = Math.max(0.001, Math.min(0.999, expected * 3.0 - 1.0)), v;
        long pair = rng.nextLong();
        float left = (pair >>> 40) * 0x1p-24f, right = (pair & 0xFFFFFFL) * 0x1p-24f;

        if(left < d2)
            v = Math.sqrt(d2 * left);
        else if(left > d2)
            v = 1 - Math.sqrt((1 - d2) * (1 - left));
        else
            v = d2;
        return (Math.pow( right, 1.0 / expected - 1.0) + v) * 0.5;
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
        return (long)(quantile(d) * Long.MAX_VALUE);
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
        return (long)(quantile(d) * bound);
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
        return quantile(d);
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
        return quantile(d) * bound;
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
        return (int)(quantile(d) * Integer.MAX_VALUE);
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
        return (int)(quantile(d) * bound);
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
        return (float) quantile(d);
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
        return (float)(quantile(d) * bound);
    }

    /**
     * Looks up the given kind in the Map of biases this stores, and generates a random number using this object's RNG.
     * If the kind is in the Map, this adjusts the generated number so it matches a distribution that would have the
     * expected average the kind was associated with. The returned boolean will be true if the random number (between
     * 0.0 and 1.0, exclusive upper) is greater than or equal to 0.5. If the kind is not in the map, this generates a
     * boolean using RNG and no further changes.
     * @param kind the kind of bias to look up
     * @return a random float between 0.0 and 1.0, potentially influenced by the bias associated with kind, if present
     */
    public boolean biasedBoolean(String kind)
    {
        Double d = biases.get(kind);
        if(d == null)
            return rng.nextBoolean();
        return quantile(d) >= 0.5;
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


    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a positive long in either case, but
     * not all long values are possible if this is biased, in part because of generating a double, which has less
     * precision than long, and in part because some numbers need to be more common than others.
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @return a random 63-bit positive long, potentially influenced by the bias associated with kind, if present
     */
    public long biasedLong(double expectedAverage)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return (long)(quantile(expectedAverage) * Long.MAX_VALUE);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a long between 0 and bound (exclusive
     * on bound), where bound can be negative (and this behavior is allowed even though RNG normally returns 0 for all
     * negative bounds).
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @param bound the outer bound, exclusive; can be negative
     * @return a random long between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public long biasedLong(double expectedAverage, long bound)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return (long)(quantile(expectedAverage) * bound);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a double between 0.0 and 1.0 (exclusive
     * on 1.0).
     * @param expectedAverage the desired average
     * @return a random double between 0.0 and 1.0, potentially influenced by the bias associated with kind, if present
     */
    public double biasedDouble(double expectedAverage)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return quantile(expectedAverage);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a double between 0 and bound (exclusive
     * on bound), where bound can be negative (the same as RNG).
     * @param expectedAverage the desired average
     * @param bound the outer bound, exclusive; can be negative
     * @return a random double between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public double biasedDouble(double expectedAverage, double bound)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return quantile(expectedAverage) * bound;
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a positive int from 0 to (2 to the 31)-1
     * in either case.
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @return a random 31-bit positive int, potentially influenced by the bias associated with kind, if present
     */
    public int biasedInt(double expectedAverage)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return (int)(quantile(expectedAverage) * Integer.MAX_VALUE);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be an int between 0 and bound (exclusive
     * on bound), where bound can be negative (and this behavior is allowed even though RNG normally returns 0 for all
     * negative bounds).
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @param bound the outer bound, exclusive; can be negative
     * @return a random int between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public int biasedInt(double expectedAverage, int bound)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return (int)(quantile(expectedAverage) * bound);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a float between 0.0f and 1.0f (exclusive
     * on 1.0f).
     * @param expectedAverage the desired average
     * @return a random float between 0.0 and 1.0, potentially influenced by the bias associated with kind, if present
     */
    public float biasedFloat(double expectedAverage)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return (float) quantile(expectedAverage);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a float between 0f and bound (exclusive
     * on bound), where bound can be negative.
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @param bound the outer bound, exclusive; can be negative
     * @return a random double between 0 and bound, potentially influenced by the bias associated with kind, if present
     */
    public float biasedFloat(double expectedAverage, float bound)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return (float)(quantile(expectedAverage) * bound);
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned boolean will be true if the random number (between 0.0
     * and 1.0, exclusive upper) is greater than or equal to 0.5.
     * @param expectedAverage the desired probability of a true result, between 0.0 and 1.0
     * @return a random float between 0.0 and 1.0, potentially influenced by the bias associated with kind, if present
     */
    public boolean biasedBoolean(double expectedAverage)
    {
        if(expectedAverage <= 0) expectedAverage = 0.001;
        if(expectedAverage >= 1) expectedAverage = 0.999;
        return quantile(expectedAverage) >= 0.5;
    }
    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be an int between min and max (exclusive
     * on max), where min and/or max can be negative, and the difference between the two can be either positive or
     * negative.
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @param min the inner bound, inclusive; can be negative
     * @param max the outer bound, exclusive; can be negative
     * @return a random int between min and max, potentially influenced by the bias associated with kind, if present
     */
    public int biasedBetween(double expectedAverage, int min, int max)
    {
        return biasedInt(expectedAverage, max - min) + min;
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a long between min and max (exclusive
     * on max), where min and/or max can be negative, and the difference between the two can be either positive or
     * negative.
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @param min the inner bound, inclusive; can be negative
     * @param max the outer bound, exclusive; can be negative
     * @return a random long between min and max, potentially influenced by the bias associated with kind, if present
     */
    public long biasedBetween(double expectedAverage, long min, long max)
    {
        return biasedLong(expectedAverage, max - min) + min;
    }

    /**
     * Generates a random number using this object's RNG and adjusts the generated number so it matches a distribution
     * that would have the given expected average. The returned number will be a double between min and max (exclusive
     * on max), where min and/or max can be negative, and the difference between the two can be either positive or
     * negative.
     * @param expectedAverage the desired average if the minimum value was 0.0 and the exclusive max was 1.0
     * @param min the inner bound, inclusive; can be negative
     * @param max the outer bound, exclusive; can be negative
     * @return a random double between min and max, potentially influenced by the bias associated with kind, if present
     */
    public double biasedBetween(double expectedAverage, double min, double max)
    {
        return biasedDouble(expectedAverage, max - min) + min;
    }

    @Override
    public String toString() {
        return "RandomBias{" +
                "biases=" + biases +
                ", rng=" + rng +
                ", distribution=" + distribution +
                '}';
    }

}
