package squidpony.squidmath;

import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * Class for emulating various traditional RPG-style dice rolls.
 *
 * Based on code from the Blacken library.
 *
 * @author yam655
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Dice implements Serializable {

    private static final long serialVersionUID = -488902743486431146L;

    private static final Pattern guessPattern = Pattern.compile("\\s*(\\d+)?\\s*(?:([:><])\\s*(\\d+))??\\s*(?:([d:])\\s*(\\d+))?\\s*(?:([+/*-])\\s*(\\d+))?\\s*");
    private RNG rng;
    private transient IntVLA temp = new IntVLA(20);
    /**
     * Creates a new dice roller that uses a random RNG seed for an RNG that it owns.
     */
    public Dice() {
        rng = new RNG();
    }

    /**
     * Creates a new dice roller that uses the given RNG, which can be seeded before it's given here. The RNG will be
     * shared, not copied, so requesting a random number from the same RNG in another place may change the value of the
     * next die roll this makes, and dice rolls this makes will change the state of the shared RNG.
     * @param rng an RNG that can be seeded; will be shared (dice rolls will change the RNG state outside here)
     */
    public Dice(RNG rng)
    {
        this.rng = rng;
    }

    /**
     * Creates a new dice roller that will use its own RNG, seeded with the given seed.
     * @param seed a long to use as a seed for a new RNG (can also be an int, short, or byte)
     */
    public Dice(long seed)
    {
        rng = new RNG(seed);
    }
    /**
     * Creates a new dice roller that will use its own RNG, seeded with the given seed.
     * @param seed a String to use as a seed for a new RNG
     */
    public Dice(String seed)
    {
        rng = new RNG(seed);
    }
    /**
     * Sets the random number generator to be used.
     *
     * This method does not need to be called before using the methods of this
     * class.
     *
     * @param rng the source of randomness
     */
    public void setRandom(RNG rng) {
        this.rng = rng;
    }

    /**
     * Rolls the given number of dice with the given number of sides and returns
     * the total of the best n dice.
     *
     * @param n number of best dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of best n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int bestOf(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollDice(1, sides));
        }

        return bestOf(rolls, temp);
    }

    /**
     * Rolls the given number of dice with the given number of sides and returns
     * the total of the lowest n dice.
     *
     * @param n number of worst dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of best n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int worstOf(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollDice(1, sides));
        }

        return worstOf(rolls, temp);
    }

    /**
     * Totals the highest n numbers in the pool.
     *
     * @param n the number of dice to be totaled
     * @param pool the dice to pick from
     * @return the sum
     */
    private int bestOf(int n, IntVLA pool) {
        int rolls = Math.min(n, pool.size);
        pool.sort();

        int ret = 0;
        for (int i = pool.size - 1, r = 0;  r < rolls && i >= 0; i--, r++) {
            ret += pool.get(i);
        }
        return ret;
    }

    /**
     * Totals the lowest n numbers in the pool.
     *
     * @param n the number of dice to be totaled
     * @param pool the dice to pick from
     * @return the sum
     */
    private int worstOf(int n, IntVLA pool) {
        int rolls = Math.min(n, pool.size);
        pool.sort();

        int ret = 0;
        for (int r = 0;  r < rolls; r++) {
            ret += pool.get(r);
        }
        return ret;
    }

    /**
     * Find the best n totals from the provided number of dice rolled according
     * to the roll group string.
     *
     * @param n number of roll groups to total
     * @param dice number of roll groups to roll
     * @param group string encoded roll grouping
     * @return the sum
     */
    public int bestOf(int n, int dice, String group) {
        int rolls = Math.min(n, dice);
        temp.clear();

        for (int i = 0; i < dice; i++) {
            temp.add(roll(group));
        }

        return bestOf(rolls, temp);
    }

    /**
     * Find the worst n totals from the provided number of dice rolled according
     * to the roll group string.
     *
     * @param n number of roll groups to total
     * @param dice number of roll groups to roll
     * @param group string encoded roll grouping
     * @return the sum
     */
    public int worstOf(int n, int dice, String group) {
        int rolls = Math.min(n, dice);
        temp.clear();

        for (int i = 0; i < dice; i++) {
            temp.add(roll(group));
        }

        return worstOf(rolls, temp);
    }

    /**
     * Emulate a dice roll and return the sum.
     *
     * @param n number of dice to sum
     * @param sides number of sides on the rollDice
     * @return sum of rollDice
     */
    public int rollDice(int n, int sides) {
        int ret = 0;
        for (int i = 0; i < n; i++) {
            ret += rng.nextIntHasty(sides) + 1;
        }
        return ret;
    }

    /**
     * Get a list of the independent results of n rolls of dice with the given
     * number of sides.
     *
     * @param n number of dice used
     * @param sides number of sides on each die
     * @return list of results
     */
    public IntVLA independentRolls(int n, int sides) {
        IntVLA ret = new IntVLA(n);
        for (int i = 0; i < n; i++) {
            ret.add(rng.nextIntHasty(sides) + 1);
        }
        return ret;
    }

    /**
     * Evaluate the String {@code rollCode} as dice roll notation and roll to get a random result of that dice roll.
     * This is the main way of using the Dice class. The following notation is supported:
     * <ul>
     *     <li>{@code 42} : simple absolute string</li>
     *     <li>{@code 10:20} : simple random range (inclusive between 10 and 20)</li>
     *     <li>{@code 3d6} : sum of 3 6-sided dice</li>
     *     <li>{@code d6} : synonym for {@code 1d6}</li>
     *     <li>{@code 3>4d6} : best 3 of 4 6-sided dice</li>
     *     <li>{@code 3:4d6} : synonym for {@code 3>4d6}; older syntax</li>
     *     <li>{@code 2<5d6} : worst 2 of 5 6-sided dice</li>
     * </ul>
     * The following types of suffixes are supported:
     * <ul>
     *     <li>{@code +4} : add 4 to the value</li>
     *     <li>{@code -3} : subtract 3 from the value</li>
     *     <li>{@code *100} : multiply value by 100</li>
     *     <li>{@code /8} : divide value by 8</li>
     * </ul>
     * @param rollCode string using dice roll notation
     * @return random number
     */
    public int roll(String rollCode) {//TODO -- rework to tokenize and allow multiple chained operations
        Matcher mat = guessPattern.matcher(rollCode);
        int ret = 0;

        if (mat.matches()) {
            String num1 = mat.group(1); // number constant
            String wmode = mat.group(2); // between notation
            String wnum = mat.group(3); // number constant
            String mode = mat.group(4); // d or colon
            String num2 = mat.group(5); // number constant
            String pmode = mat.group(6); // math operation
            String pnum = mat.group(7); // number constant

            int a = num1 == null ? 0 : StringKit.intFromDec(num1);
            int b = num2 == null ? 0 : StringKit.intFromDec(num2);
            int w = wnum == null ? 0 : StringKit.intFromDec(wnum);

            if (num1 != null && num2 != null) {
                if (wnum != null) {
                    if (">".equals(wmode) || ":".equals(wmode)) {
                        if ("d".equals(mode)) {
                            ret = bestOf(a, w, b);
                        }
                    }
                    else if("<".equals(wmode))
                    {
                        if ("d".equals(mode)) {
                            ret = worstOf(a, w, b);
                        }
                    }
                } else if ("d".equals(mode)) {
                    ret = rollDice(a, b);
                } else if (":".equals(mode)) {
                    ret = a + rng.nextIntHasty(b + 1 - a);
                }
            } else if (num1 != null) {
                if (":".equals(wmode)) {
                    ret = a + rng.nextIntHasty(w + 1 - a);
                } else {
                    ret = a;
                }
            } else if (num2 != null) {
                if (mode != null) {
                    switch (mode) {
                        case "d":
                            ret = rollDice(1, b);
                            break;
                        case ":":
                            ret = rng.nextIntHasty(b + 1);
                            break;
                    }
                }
            }
            if (pmode != null) {
                int p = pnum == null ? 0 : StringKit.intFromDec(pnum);
                switch (pmode) {
                    case "+":
                        ret += p;
                        break;
                    case "-":
                        ret -= p;
                        break;
                    case "*":
                        ret *= p;
                        break;
                    case "/":
                        ret /= p;
                        break;
                }
            }
        }
        return ret;
    }
}
