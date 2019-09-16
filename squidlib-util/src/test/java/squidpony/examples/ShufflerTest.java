package squidpony.examples;

import org.junit.Ignore;
import org.junit.Test;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.*;

import java.util.ArrayList;

import static squidpony.examples.LanguageGenTest.PRINTING;

/**
 * Created by Tommy Ettinger on 5/21/2016.
 */
@Ignore
public class ShufflerTest {

    @Test
    public void testGapShuffler() {
        for (int n = 8; n <= 48; n+= 8) {
            RNG rng = new RNG("SquidLib!");
            ArrayList<String> names = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                names.add(FakeLanguageGen.FANTASY_NAME.word(rng, true, 3));
            }
            
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            String name;
            for (int i = 0; i < 200; i++) {
                name = gap.next();
                if(PRINTING) 
                    System.out.println(name);
            }

            // alternate API
            /*
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            Iterator<String> it = gap.iterator();
            for (int i = 0; i < 200; i++) {
                if(it.hasNext())
                    System.out.println(it.next());
            }*/

            if(PRINTING)
                System.out.println("\n");
        }
    }
    
    @Test
    public void testGapShufflerPairing()
    {
        String[] zodiac = new String[12];
        RNG shuffleRNG = new RNG(new XoshiroStarPhi32RNG(1234567890123456L));
        for (int i = 0; i < zodiac.length; i++) {
            zodiac[i] = FakeLanguageGen.ANCIENT_EGYPTIAN.word(shuffleRNG, true, shuffleRNG.maxIntOf(4, 3) + 1);
        }
        String[] phrases = new String[]{" is in retrograde", " ascends", " reaches toward the North", " leans Southward",
                " stands against the West wind", " charges into the East", " resides in the Castle",
                " feels pensive", " seizes the day", " looms mightily", " retreats into darkness"},
                meanings = new String[]
                        {". It is a dire omen for those under the sign of @.", ". This bodes ill for the house of @.",
                                ". Mayhaps this is a significant portent for they with the sign of @...",
                                "! Buy a lottery ticket if you're a @!",
                                ". If you're a @, you're probably not gonna die!",
                                ". You should avoid spicy foods if you are under the sign of @.",
                                ". That's some bad juju for those poor fools under the sign of @.",
                                ". This is going to be a bad one.",
                                ". Oh yeah, this is gonna be good...",
                                "! This is the dawning of the Age of Aquarius!"};
        GapShuffler<String> zodiacShuffler = new GapShuffler<>(zodiac, shuffleRNG);
        GapShuffler<String> phraseShuffler = new GapShuffler<>(phrases, shuffleRNG);
        GapShuffler<String> meaningShuffler = new GapShuffler<>(meanings, shuffleRNG);

        if(PRINTING) {
            for (int i = 0; i < 24; i++) {
//            System.out.println(zodiacShuffler.next() + " " + zodiacShuffler.next() + " " + zodiacShuffler.next() + " " + zodiacShuffler.next());
                System.out.println(zodiacShuffler.next() + phraseShuffler.next() + meaningShuffler.next().replace("@", zodiacShuffler.next()));
            }
        }
    }        
    @Test
    public void testLSSBounds()
    {
        for (int i = 2; i <= 42; i++) {
            LowStorageShuffler lss = new LowStorageShuffler(i, 31337);
            System.out.printf("Bound %02d: %d", i, lss.next());
            for (int j = 1; j < i; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testLSSReseed()
    {
        LowStorageShuffler lss = new LowStorageShuffler(7, 0);
        for (int i = 0; i < 30; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testLSSReverse()
    {
        LowStorageShuffler lss = new LowStorageShuffler(7, 0);
        for (int i = 0; i < 10; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X forward: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
            System.out.printf("Seed %08X reverse: %d", i, lss.previous());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.previous());
            }
            System.out.println();

        }
    }
    @Test
    public void testSIS()
    {
        ShuffledIntSequence sis = new ShuffledIntSequence(10, 31337);
        for (int j = 0; j < 10; j++) {
            System.out.print(sis.next());
            for (int i = 1; i < 20; i++) {
                System.out.print(", " + sis.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testSNBounds()
    {
        for (int i = 3; i <= 42; i++) {
            SwapOrNotShuffler sn = new SwapOrNotShuffler(i, 31337 + i);
            System.out.printf("Bound %02d: %d", i, sn.next());
            for (int j = 1; j < i; j++) {
                System.out.print(", " + sn.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testSNReseed()
    {
        SwapOrNotShuffler sn = new SwapOrNotShuffler(7, 0);
        for (int i = 0; i < 30; i++) {
            sn.restart(i);
            System.out.printf("Seed %08X: %d", i, sn.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + sn.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testSNReverse()
    {
        SwapOrNotShuffler sn = new SwapOrNotShuffler(7, 0);
        for (int i = 0; i < 10; i++) {
            sn.restart(i);
            System.out.printf("Seed %08X forward: %d", i, sn.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + sn.next());
            }
            System.out.println();
            System.out.printf("Seed %08X reverse: %d", i, sn.previous());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + sn.previous());
            }
            System.out.println();

        }
    }
    @Test
    public void testSNSIS()
    {
        SNShuffledIntSequence sis = new SNShuffledIntSequence(6, 0xBEEF1E57);
        for (int j = 0; j < 10; j++) {
            System.out.print(sis.next());
            for (int i = 1; i < 20; i++) {
                System.out.print(", " + sis.next());
            }
            System.out.println();
        }
    }

}
