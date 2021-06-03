package squidpony.examples;

import squidpony.squidmath.Dice;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.SilkRNG;

/**
 * Created by Tommy Ettinger on 5/18/2017.
 */
public class DiceRollRuleTest {

    public static void rollMany(Dice dice, IntVLA rule){
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                System.out.printf("%3d ", dice.runRollRule(rule));
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args)
    {
        Dice d = new Dice(new SilkRNG(12345));
        IntVLA rule = new IntVLA();
        d.parseRollRuleInto(rule, "-10");
        System.out.println(rule);
        rollMany(d, rule);
        rule.clear();
        d.parseRollRuleInto(rule, "2d6");
        System.out.println(rule);
        rollMany(d, rule);
        rule.clear();
        d.parseRollRuleInto(rule, "3>4d6");
        System.out.println(rule);
        rollMany(d, rule);
        rule.clear();
        d.parseRollRuleInto(rule, "0:10:100");
        System.out.println(rule);
        rollMany(d, rule);
        rule.clear();
        d.parseRollRuleInto(rule, "2d4+3");
        System.out.println(rule);
        rollMany(d, rule);

    }
}
