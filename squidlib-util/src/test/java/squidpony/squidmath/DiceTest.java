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
        System.out.println(d.roll("1d6")); // 5
        System.out.println(d.roll("3d8")); // 8
        System.out.println(d.roll("1d12+3")); // 14
        System.out.println(d.roll("1d12*5")); // 25
        System.out.println(d.roll("3>5d12")); // 26
        System.out.println(d.roll("3<5d12")); // 10
        System.out.println(d.roll("10:20")); // 19
        System.out.println(d.roll("10:11")); // 11
        System.out.println(d.roll("10:11")); // 11
        System.out.println(d.roll("10:11")); // 10
        System.out.println(d.roll("5!4")); // 24
        System.out.println(d.roll("10!2")); // 38
        System.out.println(d.roll("5d6+2d8")); //26
        System.out.println(d.roll("5!6 + 2!8")); //25
        System.out.println(d.roll("3>4d6  *  1!4")); //84
        System.out.println(d.roll("1d20-3")); //17
        System.out.println(d.roll("1d20*-3")); //-24
        System.out.println(d.roll("1>10!2")); // 7
        System.out.println(d.roll("10:100:200")); // 142
        System.out.println(d.roll("3<10!100")); // 44
        System.out.println(d.roll("0:-15:15")); // -3

    }
    @Test
    public void testDiceResults()
    {
        StatefulRNG r = new StatefulRNG(0x1337BEEFDEAL);
        Dice d = new Dice(r);
        Assert.assertEquals(d.roll("1d6"), 5);
        Assert.assertEquals(d.roll("3d8"), 8);
        Assert.assertEquals(d.roll("1d12+3"),14);
        Assert.assertEquals(d.roll("1d12*5"),25);
        Assert.assertEquals(d.roll("3>5d12"),26);
        Assert.assertEquals(d.roll("3<5d12"),10);
        Assert.assertEquals(d.roll("10:20"), 19);
        Assert.assertEquals(d.roll("10:11"), 11);
        Assert.assertEquals(d.roll("10:11"), 11);
        Assert.assertEquals(d.roll("10:11"), 10);
        Assert.assertEquals(d.roll("5!4"), 24);
        Assert.assertEquals(d.roll("10!2"), 38);
        Assert.assertEquals(d.roll("5d6+2d8"), 26);
        Assert.assertEquals(d.roll("5!6 + 2!8"), 25);
        Assert.assertEquals(d.roll("3>4d6  *  1!4"), 84);
        Assert.assertEquals(d.roll("1d20-3"), 17);
        Assert.assertEquals(d.roll("1d20*-3"), -24);
        Assert.assertEquals(d.roll("1>10!2"), 7);
        Assert.assertEquals(d.roll("10:100:200"), 142);
        Assert.assertEquals(d.roll("3<10!100"), 44);
        Assert.assertEquals(d.roll("0:-15:15"), -3);

    }
}
