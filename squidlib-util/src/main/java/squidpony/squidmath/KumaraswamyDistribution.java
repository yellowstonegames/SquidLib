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
 * A parameterized IDistribution that can be configured to take various shapes.
 * See <a href="https://en.wikipedia.org/wiki/Kumaraswamy_distribution">Wikipedia's
 * article on this distribution</a> for more.
 */
public class KumaraswamyDistribution extends IDistribution.SimpleDistribution implements IDistribution {
    public static final KumaraswamyDistribution instance = new KumaraswamyDistribution(2.0, 2.0);

    /**
     * Shape parameter a.
     * Stored internally as its reciprocal to avoid dividing extra times in {@link #nextDouble(IRNG)}.
     */
    private double a;
    /**
     * Shape parameter b.
     * Stored internally as its reciprocal to avoid dividing extra times in {@link #nextDouble(IRNG)}.
     */
    private double b;
    public double getA() {return 1.0/a;}
    public void setA(double a) {this.a = 1.0/a;}
    public double getB() {return 1.0/b;}
    public void setB(double b) {this.b = 1.0/b;}

    public KumaraswamyDistribution() {
        this(2.0, 2.0);
    }
    public KumaraswamyDistribution(double a, double b) {
        this.a = 1.0/a;
        this.b = 1.0/b;
    }

    /**
     * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     *
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    @Override
    public double nextDouble(IRNG rng) {
        return Math.pow(1.0 - Math.pow(1.0 - rng.nextDouble(), b), a);
    }

    /**
     * Gets the mean value of this distribution.
     * <br>
     * Note that this particular implementation does some potentially-heavy calculations, and if the a and b parameters
     * are unchanged, then this should be cached instead of repeatedly calculating it.
     * @return the mean value of this distribution
     */
    @Override
    public double getMean(){
        final double b = 1.0 / this.b;
        return (MathExtras.factorial(a) * MathExtras.gamma(b) / b) / MathExtras.factorial(a + b);
    }
}
