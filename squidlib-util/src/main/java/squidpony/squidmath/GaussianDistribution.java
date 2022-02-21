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
/**
 * An IDistribution that produces double results with a Gaussian (normal) distribution. This means it has no limits in
 * any direction, but is much more likely to produce results close to 0. This now uses the Box-Muller Transform (it
 * previously used the Ziggurat method), which means it relies on trigonometric functions
 * ({@link NumberTools#sin_(double)} and {@link NumberTools#cos_(double)}, which are approximations), square root and
 * logarithm calculations, but it only needs to calculate every other number, and it uses a fixed amount of calls to
 * {@link IRNG#nextDouble()} (for every pair of outputs, it makes two calls to nextDouble()).
 * <br>
 * No argument constructor gives a mean of 0 and standard deviation and variance of 1 for an N(0,1) distribution
 * that Z Score tables are built around. The two-argument constructor allows specifying a different mean and standard
 * deviation. In statistics, mean is also called mu, and standard deviation is sigma squared.
 * <br>
 * Created by Tommy Ettinger on 11/23/2019, rewritten on 7/30/2020.
 */
public class GaussianDistribution implements IDistribution {
    
    public static final GaussianDistribution instance = new GaussianDistribution();
    private double cachedNext = 0.0;
    private boolean needsMore = true;
    private final double mu;
    private final double sigma;
    
    /**
     * Creates a Gaussian (normal) distribution with mean of 0 and standard deviation and variance of 1.
     * This N(0, 1) probability distribution is what Z Score tables are built around.
     */
    public GaussianDistribution() {
    	this.mu = 0;
    	this.sigma = 1;
    }
    
    /**
     * Creates a Gaussian (normal) distribution with specified mean and standard deviation.
     * In statistics, this means providing the mu and sigma, respectively, for an N(mu, pow(sigma, 2)) distribution.
     * @param mean (equivalent to mu) the value at the center of the distribution; also the most common result
     * @param standardDeviation (equivalent to sigma squared) how far and often values should spread out away from the mean
     */
    public GaussianDistribution(double mean, double standardDeviation) {
    	this.mu = mean;
    	this.sigma = standardDeviation;
    }
    
    @Override
    public double nextDouble(IRNG rng) {
        if(needsMore ^= true)
            return cachedNext;
        final double mul = sigma * Math.sqrt(-2.0 * Math.log(1.0 - rng.nextDouble()));
        final double variate = rng.nextDouble();
        cachedNext = mul * NumberTools.cos_(variate) + mu;
        return mul * NumberTools.sin_(variate) + mu;
    }

    /**
     * The lower inclusive bound is negative infinity.
     * @return negative infinity
     */
    @Override
    public double getLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * The upper inclusive bound is infinity.
     * @return positive infinity
     */
    @Override
    public double getUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Gets the mean value of this distribution (mu).
     * @return the mean value of this distribution (mu)
     */
    @Override
    public double getMean(){
        return mu;
    }
}
