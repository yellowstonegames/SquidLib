package squidpony.squidmath;

import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * Class for emulating various traditional RPG-style dice rolls.
 * Supports rolling multiple virtual dice of arbitrary size, summing all, the highest <i>n</i>, or the lowest <i>n</i>
 * dice, treating dice as "exploding" as in some tabletop games (where the max result is rolled again and added),
 * getting value from inside a range, and applying simple arithmetic modifiers to the result (like adding a number).
 * <br>
 * Based on code from the Blacken library.
 *
 * @author yam655
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 */
@Beta
public class Dice implements Serializable {

    private static final long serialVersionUID = -488902743486431146L;

    private static final Matcher mat = Pattern.compile("\\s*(?:(?:(-?\\d+)?\\s*(?:([:><])\\s*(\\d+))?\\s*(?:([d:!])\\s*(\\d+))?)|([+/*-]))\\s*").matcher();
    private IRNG rng;
    private transient IntVLA temp = new IntVLA(20);
    /**
     * Creates a new dice roller that uses a random RNG seed for an RNG that it owns.
     */
    public Dice() {
        rng = new RNG();
    }

    /**
     * Creates a new dice roller that uses the given IRNG, which can be seeded before it's given here. The IRNG will be
     * shared, not copied, so requesting a random number from the same IRNG in another place may change the value of the
     * next die roll this makes, and dice rolls this makes will change the state of the shared IRNG.
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}; will be shared (dice rolls will change the IRNG state outside here)
     */
    public Dice(IRNG rng)
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
    public void setRandom(IRNG rng) {
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
     * Rolls the given number of exploding dice with the given number of sides and returns
     * the total of the best n dice (counting a die that explodes as one die).
     *
     * @param n number of best dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of best n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int bestOfExploding(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollExplodingDice(1, sides));
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
     * @return sum of worst n out of <em>dice</em><b>d</b><em>sides</em>
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
     * Rolls the given number of exploding dice with the given number of sides and returns
     * the total of the lowest n dice (counting a die that explodes as one die).
     *
     * @param n number of worst dice to total
     * @param dice total number of dice to roll
     * @param sides number of sides on the dice
     * @return sum of worst n out of <em>dice</em><b>d</b><em>sides</em>
     */
    private int worstOfExploding(int n, int dice, int sides) {
        int rolls = Math.min(n, dice);
        temp.clear();
        for (int i = 0; i < dice; i++) {
            temp.add(rollExplodingDice(1, sides));
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
     * @param sides positive integer; number of sides on the rolled dice
     * @return sum of rolled dice
     */
    public int rollDice(int n, int sides) {
        int ret = 0;
        for (int i = 0; i < n; i++) {
            ret += rng.nextInt(sides) + 1;
        }
        return ret;
    }

    /**
     * Emulate an exploding dice roll and return the sum.
     *
     * @param n number of dice to sum
     * @param sides number of sides on the rollDice; should be greater than 1
     * @return sum of rollDice
     */
    public int rollExplodingDice(int n, int sides) {
        int ret = 0, curr;
        if(sides <= 1) return n; // avoid infinite loop, act like they can't explode
        for (int i = 0; i < n;) {
            ret += (curr = rng.nextInt(sides) + 1);
            if(curr != sides) i++;
        }
        return ret;
    }

    /**
     * Get a list of the independent results of n rolls of dice with the given
     * number of sides.
     *
     * @param n number of dice used
     * @param sides positive integer; number of sides on each die
     * @return list of results
     */
    public IntVLA independentRolls(int n, int sides) {
        IntVLA ret = new IntVLA(n);
        for (int i = 0; i < n; i++) {
            ret.add(rng.nextInt(sides) + 1);
        }
        return ret;
    }

    /**
     * Evaluate the String {@code rollCode} as dice roll notation and roll to get a random result of that dice roll.
     * This is the main way of using the Dice class. This effectively allows rolling one or more dice and performing
     * certain operations on the dice and their result. One of the more frequent uses is rolling some amount of dice and
     * summing their values, which can be done with e.g. "4d10" to roll four ten-sided dice and add up their results.
     * You can choose to sum only some of the dice, either the "n highest" or "n lowest" values in a group, with "3>4d6"
     * to sum the three greatest-value dice in four rolls of six-sided dice, or "2<3d8" to sum the two lowest-value dice
     * in three rolls of eight-sided dice. You can apply modifiers to these results, such as "1d20+7" to roll one
     * twenty-sided die and add 7 to its result. These modifiers can be other dice, such as "1d10-1d6", and while
     * multiplication and division are supported, order of operations isn't, so it just rolls dice from left to right
     * and applies operators it find along the way. You can get a random value in an inclusive range with "50:100",
     * which is equivalent to "1d51+49" but is easier to read and understand. You can treat dice as "exploding,"
     * where any dice that get the maximum result are rolled again and added to the total along with the previous
     * maximum result. As an example, if two exploding six-sided dice are rolled, and their results are 3 and 6, then
     * because 6 is the maximum value it is rolled again and added to the earlier rolls; if the additional roll is a 5,
     * then the sum is 3 + 6 + 5 (for a total of 14), but if the additional roll was a 6, then it would be rolled again
     * and added again, potentially many times if 6 is rolled continually. Some players may be familiar with this game
     * mechanic from various tabletop games, but many potential players might not be, so it should be explained if you
     * show the kinds of dice being rolled to players. The syntax used for exploding dice replaced the "d" in "3d6" for
     * normal dice with "!", making "3!6" for exploding dice. Inclusive ranges are not supported with best-of and
     * worst-of notation, but exploding dice are. If using a range, the upper bound can be random, decided by dice rolls
     * such as with "1:6d6" (which rolls six 6-sided dice and uses that as the upper bound of the range) or by other
     * ranges such as with "10:100:200", which gets a random number between 100 and 200, then returns a random number
     * between 10 and that. While it is technically allowed to end a dice string with an operator, the partial
     * operator will be ignored. If you start a dice string with an operator, its left-hand-side will always be 0. If
     * you have two operators in a row, only the last will be used, unless one is '-' and can be treated as part of a
     * negative number (this allows "1d20 * -3" to work). Whitespace is allowed between most parts of a dice string.
     * <br>
     * The following notation is supported:
     * <ul>
     *     <li>{@code 42} : simple absolute string; can start with {@code -} to make it negative</li>
     *     <li>{@code 3d6} : sum of 3 6-sided dice</li>
     *     <li>{@code d6} : synonym for {@code 1d6}</li>
     *     <li>{@code 3>4d6} : best 3 of 4 6-sided dice</li>
     *     <li>{@code 3:4d6} : gets a random value between 3 and a roll of {@code 4d6}; this syntax has changed</li>
     *     <li>{@code 2<5d6} : worst 2 of 5 6-sided dice</li>
     *     <li>{@code 10:20} : simple random range (inclusive between 10 and 20)</li>
     *     <li>{@code :20} : synonym for {@code 0:20}</li>
     *     <li>{@code 3!6} : sum of 3 "exploding" 6-sided dice; see above for the semantics of "exploding" dice</li>
     *     <li>{@code !6} : synonym for {@code 1!6}</li>
     * </ul>
     * The following types of operators are supported:
     * <ul>
     *     <li>{@code +4} : add 4 to the value</li>
     *     <li>{@code -3} : subtract 3 from the value</li>
     *     <li>{@code *100} : multiply value by 100</li>
     *     <li>{@code /8} : divide value by 8</li>
     * </ul>
     * @param rollCode dice string using the above notation
     * @return a random number that is possible with the given dice string
     */
    public int roll(String rollCode) {
        mat.setTarget(rollCode);
        int ret, prev = 0;
        char currentMode = '+';
        while (mat.find()) {
            ret = 0;
            String num1 = mat.group(1); // number constant
            String wmode = mat.group(2); // between notation
            String wnum = mat.group(3); // number constant
            String mode = mat.group(4); // dice, range, or explode
            String num2 = mat.group(5); // number constant
            String pmode = mat.group(6); // math operation

            if(pmode != null)
            {
                currentMode = pmode.charAt(0);
                continue;
            }
            int a = num1 == null ? 0 : StringKit.intFromDec(num1);
            int b = num2 == null ? 0 : StringKit.intFromDec(num2);
            int w = wnum == null ? 0 : StringKit.intFromDec(wnum);

            if (num1 != null && num2 != null) {
                if (wnum != null) {
                    if (">".equals(wmode)) {
                        if ("d".equals(mode)) {
                            ret = bestOf(a, w, b);
                        }
                        else if("!".equals(mode))
                        {
                            ret = bestOfExploding(a, w, b);
                        }
                    }
                    else if("<".equals(wmode))
                    {
                        if ("d".equals(mode)) {
                            ret = worstOf(a, w, b);
                        }
                        else if("!".equals(mode))
                        {
                            ret = worstOfExploding(a, w, b);
                        }
                    }
                    else
                    // Here, wmode is ":", there is a constant lower bound for the range, and the upper bound is some
                    // dice roll or other range. This can be negative, easily, if the random upper bound is negative
                    {
                        if ("d".equals(mode)) {
                            ret = a + rng.nextSignedInt(rollDice(w, b) + 1 - a);
                        } else if ("!".equals(mode)) {
                            ret = a + rng.nextSignedInt(rollExplodingDice(w, b) + 1 - a);
                        } else if (":".equals(mode)) {
                            ret = a + rng.nextSignedInt(w + rng.nextSignedInt(b + 1 - w) + 1 - a);
                        }
                    }
                } else if ("d".equals(mode)) {
                    ret = rollDice(a, b);
                } else if ("!".equals(mode)) {
                    ret = rollExplodingDice(a, b);
                } else if (":".equals(mode)) {
                    ret = a + rng.nextSignedInt(b + 1 - a);
                }
            } else if (num1 != null) {
                if (":".equals(wmode)) {
                    ret = a + rng.nextSignedInt(w + 1 - a);
                } else {
                    ret = a;
                }
            } else if (num2 != null) {
                if (mode != null) {
                    switch (mode) {
                        case "d":
                            ret = rollDice(1, b);
                            break;
                        case "!":
                            ret = rollExplodingDice(1, b);
                            break;
                        case ":":
                            ret = rng.nextSignedInt(b + 1);
                            break;
                    }
                }
            }
            switch (currentMode)
            {
                case '-':
                    prev -= ret;
                    break;
                case '*':
                    prev *= ret;
                    break;
                case '/':
                    prev /= ret;
                    break;
                default:
                    prev += ret;
                    break;
            }
            currentMode = '+';
        }
        return prev;
    }
}
