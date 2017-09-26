package squidpony.squidmath;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Tommy Ettinger on 9/26/2017.
 */
public class DiceTest
{
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
    }
}
