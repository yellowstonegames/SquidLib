package squidpony.squidmath;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Tommy Ettinger on 9/26/2017.
 */
public class DiceTest
{
    //uncomment when you want to know what the seeded RNG will produce; it should produce this reliably
    //@Test
    public void showDiceResults()
    {
        StatefulRNG r = new StatefulRNG(0x1337BEEFDEAL);
        Dice d = new Dice(r);
        System.out.println(d.roll("1d6")); // 3
        System.out.println(d.roll("3d8")); // 17
        System.out.println(d.roll("1d12+3")); // 15
        System.out.println(d.roll("1d12*5")); // 15
        System.out.println(d.roll("3>5d12")); // 26
        System.out.println(d.roll("3<5d12")); // 9
        System.out.println(d.roll("10:20")); // 14
        System.out.println(d.roll("10:11")); // 11
        System.out.println(d.roll("10:11")); // 11
        System.out.println(d.roll("10:11")); // 10
        System.out.println(d.roll("5!4")); // 28
        System.out.println(d.roll("10!2")); // 36
        System.out.println(d.roll("5d6+2d8")); //26
        System.out.println(d.roll("5!6 + 2!8")); //17
        System.out.println(d.roll("3>4d6  *  1!4")); //85
        System.out.println(d.roll("1d20-3")); //17
        System.out.println(d.roll("1d20*-3")); //-42
        System.out.println(d.roll("1>10!2")); // 5
        System.out.println(d.roll("10:100:200")); // 110
        System.out.println(d.roll("3<10!100")); // 48
        System.out.println(d.roll("0:-15:15")); // -15
        System.out.println(d.roll("d20")); // 19
        System.out.println(d.roll("!20")); // 9
        System.out.println(d.roll("!20")); // 4
        System.out.println(d.roll("42")); // 42
        System.out.println(d.roll("42d")); // 42
        System.out.println(d.roll("42!")); // 42
        System.out.println(d.roll("d")); // 0
        System.out.println(d.roll("!")); // 0
    }
    @Test
    public void testDiceResults()
    {
        StatefulRNG r = new StatefulRNG(0x1337BEEFDEAL);
        Dice d = new Dice(r);
        Assert.assertEquals(d.roll("1d6"),   3 );
        Assert.assertEquals(d.roll("3d8"),   17);
        Assert.assertEquals(d.roll("1d12+3"),15);
        Assert.assertEquals(d.roll("1d12*5"),15);
        Assert.assertEquals(d.roll("3>5d12"),26);
        Assert.assertEquals(d.roll("3<5d12"),9 );
        Assert.assertEquals(d.roll("10:20"), 14);
        Assert.assertEquals(d.roll("10:11"), 11);
        Assert.assertEquals(d.roll("10:11"), 11);
        Assert.assertEquals(d.roll("10:11"), 10);
        Assert.assertEquals(d.roll("5!4"),   28);
        Assert.assertEquals(d.roll("10!2"),  36);
        Assert.assertEquals(d.roll("5d6+2d8"), 26);
        Assert.assertEquals(d.roll("5!6 + 2!8"), 17);
        Assert.assertEquals(d.roll("3>4d6  *  1!4"), 85);
        Assert.assertEquals(d.roll("1d20-3"), 17);
        Assert.assertEquals(d.roll("1d20*-3"), -42);
        Assert.assertEquals(d.roll("1>10!2"), 5);
        Assert.assertEquals(d.roll("10:100:200"), 110);
        Assert.assertEquals(d.roll("3<10!100"), 48);
        Assert.assertEquals(d.roll("0:-15:15"), -15);
        Assert.assertEquals(d.roll("d20"), 19);
        Assert.assertEquals(d.roll("!20"), 9);
        Assert.assertEquals(d.roll("!20"), 4);
        Assert.assertEquals(d.roll("42"), 42);
        Assert.assertEquals(d.roll("42d"), 42);
        Assert.assertEquals(d.roll("42!"), 42);
        Assert.assertEquals(d.roll("d"), 0);
        Assert.assertEquals(d.roll("!"), 0);

    }
}
