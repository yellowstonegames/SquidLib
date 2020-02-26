package squidpony.squidmath;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Tommy Ettinger on 9/26/2017.
 */
public class DiceTest
{
    @Test
    //comment when you want to know what the seeded RNG will produce; it should produce this reliably
    @Ignore
    public void showDiceResults()
    {
        StatefulRNG r = new StatefulRNG(new LightRNG(0x1337BEEFDEAL));
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
        StatefulRNG r = new StatefulRNG(new LightRNG(0x1337BEEFDEAL));
        Dice d = new Dice(r);
        assertEquals(d.roll("1d6"),   3 );
        assertEquals(d.roll("3d8"),   17);
        assertEquals(d.roll("1d12+3"),15);
        assertEquals(d.roll("1d12*5"),15);
        assertEquals(d.roll("3>5d12"),26);
        assertEquals(d.roll("3<5d12"),9 );
        assertEquals(d.roll("10:20"), 14);
        assertEquals(d.roll("10:11"), 11);
        assertEquals(d.roll("10:11"), 11);
        assertEquals(d.roll("10:11"), 10);
        assertEquals(d.roll("5!4"),   28);
        assertEquals(d.roll("10!2"),  36);
        assertEquals(d.roll("5d6+2d8"), 26);
        assertEquals(d.roll("5!6 + 2!8"), 17);
        assertEquals(d.roll("3>4d6  *  1!4"), 85);
        assertEquals(d.roll("1d20-3"), 17);
        assertEquals(d.roll("1d20*-3"), -42);
        assertEquals(d.roll("1>10!2"), 5);
        assertEquals(d.roll("10:100:200"), 110);
        assertEquals(d.roll("3<10!100"), 48);
        assertEquals(d.roll("0:-15:15"), -15);
        assertEquals(d.roll("d20"), 19);
        assertEquals(d.roll("!20"), 9);
        assertEquals(d.roll("!20"), 4);
        assertEquals(d.roll("42"), 42);
        assertEquals(d.roll("42d"), 42);
        assertEquals(d.roll("42!"), 42);
        assertEquals(d.roll("d"), 0);
        assertEquals(d.roll("!"), 0);
    }
}
