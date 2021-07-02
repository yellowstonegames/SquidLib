package squidpony.examples;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.MarkovTextLimited;
import squidpony.StringKit;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Tommy Ettinger on 8/15/2016.
 */
/*
Mixed-script strings: Ыνωплiam аσιοguι ζi ινыπλειtsαυ рæφes
JDK collisions, 32-bit: 146
FNV collisions, 32-bit: 134
Sip collisions, 32-bit: 129
Lit collisions, 32-bit: 125
English strings: Loorly sait nundeags jeefout; dug?
JDK collisions, 32-bit: 123
FNV collisions, 32-bit: 106
Sip collisions, 32-bit: 130
Lit collisions, 32-bit: 118
Japanese (Romanized) strings: Iyainyado su shaisou oyo rogyadedu?
JDK collisions, 32-bit: 113
FNV collisions, 32-bit: 116
Sip collisions, 32-bit: 128
Lit collisions, 32-bit: 113
Arabic (simplified, Romanized) strings: Akhrariid adhirzhaizh luju lari kuuramiit aih!
JDK collisions, 32-bit: 140
FNV collisions, 32-bit: 118
Sip collisions, 32-bit: 141
Lit collisions, 32-bit: 124

 */
@Ignore
public class HashQualityTest {
    public static int jdkHash(char[] data, int start, int end)
    {
        char[] used = new char[end - start];
        System.arraycopy(data, start, used, 0, used.length);
        return Arrays.hashCode(used);
    }

    public static byte[] byteSection(byte[] data, int start, int end)
    {
        byte[] used = new byte[end - start];
        System.arraycopy(data, start, used, 0, used.length);
        return used;
    }
 
    @Test
    public void testNextPowerOfTwo(){
        for (int i = 0; i >= 0; i++) {
            Assert.assertEquals(HashCommon.nextPowerOfTwo(i), 1 << -Integer.numberOfLeadingZeros(i - 1));
//            Assert.assertEquals(HashCommon.nextPowerOfTwo(i), Math.max(1, Integer.highestOneBit(i - 1 << 1)));
        }
    }
    
    //where this restrict is used, it will only use the bottom 16 bits and 8 alternating bits from the top of a hash
    //public static final int restrict = 0x5555FFFF;
    //where this restrict is used, it will use all bits of a hash
    public static final int restrict = -1;
    @Test
    @Ignore
    public void testMost()
    {
        CrossHash.Mist storm = CrossHash.Mist.chi;
        CrossHash.Mist mist = CrossHash.Mist.epsilon;
        byte[][] bytes = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {127}, {127, 127}, {127, 127, 127},
                {-128}, {(byte)0xC0}, {(byte)0xF0},
        };
        short[][] shorts = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {0x7fff}, {0x7fff, 0x7fff}, {0x7fff, 0x7fff, 0x7fff},
                {-0x8000}, {(short)0xC000}, {(short)0xFF00},
        };
        int[][] ints = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {0x7fffffff}, {0x7fffffff, 0x7fffffff}, {0x7fffffff, 0x7fffffff, 0x7fffffff},
                {0x80000000}, {0xC0000000}, {0xFF000000},
        };
        long[][] longs = {
                null,
                {},
                {0}, {0, 0}, {0, 0, 0},
                {1}, {1, 1}, {1, 1, 1},
                {2}, {2, 2}, {2, 2, 2},
                {3}, {3, 3}, {3, 3, 3},
                {4}, {4, 4}, {4, 4, 4},
                {5}, {5, 5}, {5, 5, 5},
                {6}, {6, 6}, {6, 6, 6},
                {7}, {7, 7}, {7, 7, 7},
                {8}, {8, 8}, {8, 8, 8},
                {31}, {31, 31}, {31, 31, 31},
                {-1}, {-1, -1}, {-1, -1, -1},
                {-2}, {-2, -2}, {-2, -2, -2},
                {-3}, {-3, -3}, {-3, -3, -3},
                {-4}, {-4, -4}, {-4, -4, -4},
                {-5}, {-5, -5}, {-5, -5, -5},
                {-6}, {-6, -6}, {-6, -6, -6},
                {-7}, {-7, -7}, {-7, -7, -7},
                {-8}, {-8, -8}, {-8, -8, -8},
                {-31}, {-31, -31}, {-31, -31, -31},
                {0x7fffffffffffffffL}, {0x7fffffffffffffffL, 0x7fffffffffffffffL}, {0x7fffffffffffffffL, 0x7fffffffffffffffL, 0x7fffffffffffffffL},
                {0x8000000000000000L, 0x8000000000000000L, 0x8000000000000000L},
                {0xC000000000000000L, 0xC000000000000000L, 0xC000000000000000L},
                {0xFF00000000000000L, 0xFF00000000000000L, 0xFF00000000000000L},
        };
        Object[][] objects = {
            null,
            {},
                {new OrderedSet<String>(2)},
                {new OrderedSet<String>(2), new OrderedMap<String, String>(2)},
                {new OrderedSet<String>(2), new OrderedMap<String, String>(2), new OrderedSet<String>(2)},

                {new GreasedRegion()},
                {new GreasedRegion(), new GreasedRegion()},
                {new GreasedRegion(), new GreasedRegion(), new GreasedRegion()},

                {new LightRNG(0)},
                {new LightRNG(0), new LightRNG(0)},
                {new LightRNG(0), new LightRNG(0), new LightRNG(0)},

                {FakeLanguageGen.ENGLISH},
                {FakeLanguageGen.ENGLISH, FakeLanguageGen.RUSSIAN_AUTHENTIC},
                {FakeLanguageGen.ENGLISH, FakeLanguageGen.RUSSIAN_AUTHENTIC, FakeLanguageGen.GREEK_AUTHENTIC},

                {new LongPeriodRNG(0)},
                {new LongPeriodRNG(0), new LongPeriodRNG(0)},
                {new LongPeriodRNG(0), new LongPeriodRNG(0), new LongPeriodRNG(0)},

                {new ShortVLA()},
                {new ShortVLA(), new ShortVLA()},
                {new ShortVLA(), new ShortVLA(), new ShortVLA()},

                {new IntVLA()},
                {new IntVLA(), new IntVLA()},
                {new IntVLA(), new IntVLA(), new IntVLA()},

                {Radius.SQUARE},
                {Radius.SQUARE, Radius.CIRCLE},
                {Radius.SQUARE, Radius.CIRCLE, Radius.DIAMOND},

                {new XoRoRNG(0)},
                {new XoRoRNG(0), new XoRoRNG(0)},
                {new XoRoRNG(0), new XoRoRNG(0), new XoRoRNG(0)},

                {new SquidID(5, 5)},
                {new SquidID(5, 5), new SquidID(5, 5)},
                {new SquidID(5, 5), new SquidID(5, 5), new SquidID(5, 5)},


                {new GreasedRegion(), new LightRNG(0)},
                {new GreasedRegion(), new LightRNG(0), new GreasedRegion(), new LightRNG(0)},
                {new GreasedRegion(), new LightRNG(0), new GreasedRegion(), new LightRNG(0), new GreasedRegion(), new LightRNG(0)},

                {new LightRNG(0), FakeLanguageGen.ENGLISH},
                {new LightRNG(0), FakeLanguageGen.ENGLISH, new LightRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC},
                {new LightRNG(0), FakeLanguageGen.ENGLISH, new LightRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC, new LightRNG(0), FakeLanguageGen.GREEK_AUTHENTIC},

                {FakeLanguageGen.ENGLISH, new LongPeriodRNG(0)},
                {FakeLanguageGen.ENGLISH, new LongPeriodRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC, new LongPeriodRNG(0)},
                {FakeLanguageGen.ENGLISH, new LongPeriodRNG(0), FakeLanguageGen.RUSSIAN_AUTHENTIC, new LongPeriodRNG(0), FakeLanguageGen.GREEK_AUTHENTIC, new LongPeriodRNG(0)},

                {new LongPeriodRNG(0), new ShortVLA()},
                {new LongPeriodRNG(0), new ShortVLA(), new LongPeriodRNG(0), new ShortVLA()},
                {new LongPeriodRNG(0), new ShortVLA(), new LongPeriodRNG(0), new ShortVLA(), new LongPeriodRNG(0), new ShortVLA()},

                {new ShortVLA(), new IntVLA()},
                {new ShortVLA(), new IntVLA(), new ShortVLA(), new IntVLA()},
                {new ShortVLA(), new IntVLA(), new ShortVLA(), new IntVLA(), new ShortVLA(), new IntVLA()},

                {new IntVLA(), Radius.SQUARE},
                {new IntVLA(), Radius.SQUARE, new IntVLA(), Radius.CIRCLE},
                {new IntVLA(), Radius.SQUARE, new IntVLA(), Radius.CIRCLE, new IntVLA(), Radius.DIAMOND},

                {Radius.SQUARE, new XoRoRNG(0)},
                {Radius.SQUARE, new XoRoRNG(0), Radius.CIRCLE, new XoRoRNG(0)},
                {Radius.SQUARE, new XoRoRNG(0), Radius.CIRCLE, new XoRoRNG(0), Radius.DIAMOND, new XoRoRNG(0)},

                {new XoRoRNG(0), new SquidID(5, 5)},
                {new XoRoRNG(0), new SquidID(5, 5), new XoRoRNG(0), new SquidID(5, 5)},
                {new XoRoRNG(0), new SquidID(5, 5), new XoRoRNG(0), new SquidID(5, 5), new XoRoRNG(0), new SquidID(5, 5)},

                {new SquidID(5, 5), null},
                {new SquidID(5, 5), null, new SquidID(5, 5), null},
                {new SquidID(5, 5), null, new SquidID(5, 5), null, new SquidID(5, 5), null},

                {Coord.get(0, 0)}, {Coord.get(0, 0), Coord.get(0, 0)}, {Coord.get(0, 0), Coord.get(0, 0), Coord.get(0, 0)},
                {Coord.get(-1, -1)}, {Coord.get(-1, -1), Coord.get(-1, -1)}, {Coord.get(-1, -1), Coord.get(-1, -1), Coord.get(-1, -1)},
        };
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("JDK bytes: " + Arrays.hashCode(bytes[i]));
            System.out.println("JDK shorts: " + Arrays.hashCode(shorts[i]));
            System.out.println("JDK ints: " + Arrays.hashCode(ints[i]));
            System.out.println("JDK longs: " + Arrays.hashCode(longs[i]));
            System.out.println("JDK objects: " + Arrays.hashCode(objects[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("CrossHash bytes: " + CrossHash.hash(bytes[i]));
            System.out.println("CrossHash shorts: " + CrossHash.hash(shorts[i]));
            System.out.println("CrossHash ints: " + CrossHash.hash(ints[i]));
            System.out.println("CrossHash longs: " + CrossHash.hash(longs[i]));
            System.out.println("CrossHash objects: " + CrossHash.hash(objects[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Lightning bytes: " + CrossHash.Lightning.hash(bytes[i]));
            System.out.println("Lightning shorts: " + CrossHash.Lightning.hash(shorts[i]));
            System.out.println("Lightning ints: " + CrossHash.Lightning.hash(ints[i]));
            System.out.println("Lightning longs: " + CrossHash.Lightning.hash(longs[i]));
            System.out.println("Lightning objects: " + CrossHash.Lightning.hash(objects[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Storm bytes: " + storm.hash(bytes[i]));
            System.out.println("Storm shorts: " + storm.hash(shorts[i]));
            System.out.println("Storm ints: " + storm.hash(ints[i]));
            System.out.println("Storm longs: " + storm.hash(longs[i]));
            System.out.println("Storm objects: " + storm.hash(objects[i]));
        }


        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Wisp bytes: " + CrossHash.Wisp.hash(bytes[i]));
            System.out.println("Wisp shorts: " + CrossHash.Wisp.hash(shorts[i]));
            System.out.println("Wisp ints: " + CrossHash.Wisp.hash(ints[i]));
            System.out.println("Wisp longs: " + CrossHash.Wisp.hash(longs[i]));
            System.out.println("Wisp objects: " + CrossHash.Wisp.hash(objects[i]));
        }
        
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Water bytes: " + CrossHash.hash(bytes[i]));
            System.out.println("Water shorts: " + CrossHash.hash(shorts[i]));
            System.out.println("Water ints: " + CrossHash.hash(ints[i]));
            System.out.println("Water longs: " + CrossHash.hash(longs[i]));
            System.out.println("Water objects: " + CrossHash.hash(objects[i]));
        }
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Mist bytes: " + mist.hash(bytes[i]));
            System.out.println("Mist shorts: " + mist.hash(shorts[i]));
            System.out.println("Mist ints: " + mist.hash(ints[i]));
            System.out.println("Mist longs: " + mist.hash(longs[i]));
            System.out.println("Mist objects: " + mist.hash(objects[i]));
        }



        int longHashLength = 0x80000, stringHashLength = 0xC0000;
        IntSet colliderJDK = new IntSet(longHashLength, 0.75f),
                colliderLit = new IntSet(longHashLength, 0.75f),
                colliderSto = new IntSet(longHashLength, 0.75f),
                colliderWis = new IntSet(longHashLength, 0.75f),
                colliderWat = new IntSet(longHashLength, 0.75f),
                colliderMis = new IntSet(longHashLength, 0.75f);
        LongPeriodRNG lprng = new LongPeriodRNG();

        System.out.println("Long Hashing:");
        System.out.println("---------------------------------");

        for (int bits = 16, mask = 0xFFFF; bits < 33; mask |= 1 << bits++) {
            lprng.reseed(0x66L);
            for (int i = 0; i < longHashLength; i++) {
                lprng.nextLong();
                colliderJDK.add(Arrays.hashCode(lprng.state) & mask);
                colliderLit.add(CrossHash.Lightning.hash(lprng.state) & mask);
                colliderSto.add(storm.hash(lprng.state) & mask);
                colliderWis.add(CrossHash.Wisp.hash(lprng.state) & mask);
                colliderWat.add(CrossHash.hash(lprng.state) & mask);
                colliderMis.add(mist.hash(lprng.state) & mask);
            }
            System.out.println("JDK collisions, " + bits + "-bit: " + (longHashLength - colliderJDK.size));
            System.out.println("Lit collisions, " + bits + "-bit: " + (longHashLength - colliderLit.size));
            System.out.println("Sto collisions, " + bits + "-bit: " + (longHashLength - colliderSto.size));
            System.out.println("Wis collisions, " + bits + "-bit: " + (longHashLength - colliderWis.size));
            System.out.println("Wat collisions, " + bits + "-bit: " + (longHashLength - colliderWat.size));
            System.out.println("Mis collisions, " + bits + "-bit: " + (longHashLength - colliderMis.size));
            System.out.println();
            colliderJDK.clear();
            colliderLit.clear();
            colliderSto.clear();
            colliderWis.clear();
            colliderWat.clear();
            colliderMis.clear();
        }

        System.out.println("\nString Hashing:");
        System.out.println("---------------------------------");
        StatefulRNG srng = new StatefulRNG(0x1337CAFE);
        String input;
        FakeLanguageGen oddLang;
        String[] midPunct = {",", ";", " -"}, endPunct = {"..."};
        char[] massive;
        int langLength;
        System.out.println("Arabic-ish text, Romanized");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.ARABIC_ROMANIZED;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.add(jdkHash(massive, s, s+e+32) & restrict);
            colliderLit.add(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict);
            colliderSto.add(storm.hash(massive, s, s+e+32) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict);
            colliderWat.add(CrossHash.hash(massive, s, s+e+32) & restrict);
            colliderMis.add(mist.hash(massive, s, s+e+32) & restrict);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        System.out.println("Japanese-ish text, Romanized");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.JAPANESE_ROMANIZED;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.add(jdkHash(massive, s, s+e+32) & restrict);
            colliderLit.add(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict);
            colliderSto.add(storm.hash(massive, s, s+e+32) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict);
            colliderWat.add(CrossHash.hash(massive, s, s+e+32) & restrict);
            colliderMis.add(mist.hash(massive, s, s+e+32) & restrict);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        System.out.println("Unicode-heavy fantasy text");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.FANCY_FANTASY_NAME.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.67).mix(FakeLanguageGen.RUSSIAN_AUTHENTIC, 0.45);
        String hugeSentence = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3);
        massive = hugeSentence.toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.add(jdkHash(massive, s, s+e+32) & restrict);
            colliderLit.add(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict);
            colliderSto.add(storm.hash(massive, s, s+e+32) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict);
            colliderWat.add(CrossHash.hash(massive, s, s+e+32) & restrict);
            colliderMis.add(mist.hash(massive, s, s+e+32) & restrict);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        System.out.println("English text");
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.ENGLISH;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderJDK.add(jdkHash(massive, s, s+e+32) & restrict);
            colliderLit.add(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict);
            colliderSto.add(storm.hash(massive, s, s+e+32) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict);
            colliderWat.add(CrossHash.hash(massive, s, s+e+32) & restrict);
            colliderMis.add(mist.hash(massive, s, s+e+32) & restrict);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size));
        System.out.println("Mis collisions, 32-bit: " + (stringHashLength - colliderMis.size));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        colliderMis.clear();

        /*
        System.out.println("Unicode fantasy as UTF-8 bytes");
        byte[] massiveBytes = hugeSentence.getBytes(StandardCharsets.UTF_8), section;
        langLength = massiveBytes.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            section = byteSection(massiveBytes, s, s+e+32);
            colliderJDK.add(jdkHash(massive, s, s+e+32) & restrict);
            colliderLit.add(CrossHash.Lightning.hash(massive, s, s+e+32) & restrict);
            colliderSto.add(storm.hash(massive, s, s+e+32) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(massive, s, s+e+32) & restrict);
            colliderWat.add(CrossHash.hash(massive, s, s+e+32) & restrict);
            if(e >= 0x1fd)
                s += 7;
        }

        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size));
        System.out.println("Wis collisions, 32-bit: " + (stringHashLength - colliderWis.size));
        System.out.println("Wat collisions, 32-bit: " + (stringHashLength - colliderWat.size));
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderSto.clear();
        colliderWis.clear();
        colliderWat.clear();
        */
    }
    public static int slitherHashOld(final CharSequence data) {
        if (data == null)
            return 0;
        //long result = 0x9E3779B97F4A7C80L, a = 0x632BE59BD9B4E019L;
        long result = 0x1A976FDF6BF60B8EL, a = 0x60642E2A34326F15L;// 253
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a = (a ^ data.charAt(i)) * 0x41C64E6BL);
        }
        a ^= (result ^ result >>> 27) * 0xAEF17502108EF2D9L;
        return (int) (((a ^ a >>> 25)));// ^ (a >>> 32));
    }
    public static int slitherHashConfig(final CharSequence data, long running, long receiver) {
        if (data == null)
            return 0;
        //long running = 0x9E3779B97F4A7C80L, receiver = 0x632BE59BD9B4E019L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            running += (receiver = (receiver ^ data.charAt(i)) * 0x41C64E6BL);
        }
        receiver ^= (running ^ running >>> 27) * 0xAEF17502108EF2D9L;
        return (int) (((receiver ^ receiver >>> 25)));// ^ (a >>> 32));
// 0x9E3779B97F4A7C15L
        //        if (data == null)
//            return 0;
//        long result = 0x9E3779B97F4A7C80L, a = 0x632BE59BD9B4E019L;
//        final int len = data.length();
//        for (int i = 0; i < len; i++) {
//            result += (a = (a ^ data.charAt(i)) * 0xC6BC279692B5CC83L);
//        }
//        a ^= (result ^ result >>> 27);
//        return (int) (((a ^ a >>> 25)));// ^ (a >>> 32));
    }
//    public static int joltHash(final CharSequence data) {
//        if (data == null)
//            return 0;
//        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
//        for (int i = 0; i < data.length(); i++) {
//            result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
//        }
//        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
//        return (int) (result ^ result >>> 25 ^ z ^ z >>> 29);
//    }
    
    public static int joltHash(final CharSequence data)
    {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length();
        for (int i = 0; i < len; ++i) {
            result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (int)(result ^ result >>> 33);
    }
    
    public static int buzzHash(final CharSequence data) {
        if (data == null)
            return 0;
        //long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        //int result = 0xA539B, z = 0xE47AF;
        int result = 0xF369B, z = 0xEF17B;
        for (int i = 0; i < data.length(); i++) {
            result ^= (z += (data.charAt(i) ^ 0xC74EAD55) * 0xA5CB3);
        }
        result += (z ^ z >>> 13) * 0x62BD5;
        return (result ^ result >>> 11 ^ z ^ z >>> 15);
    }
    public static int buzzHashConfig(final CharSequence data, int result, int z) {
        if (data == null)
            return 0;
        //long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        for (int i = 0; i < data.length(); i++) {
            result ^= (z += (data.charAt(i) ^ 0xC74EAD55) * 0xA5CB3);
        }
        result += (z ^ z >>> 13) * 0x62BD5;
        return (result ^ result >>> 11 ^ z ^ z >>> 15);
    }
    public static int lantern(int state)
    {
        //return (state = ((state = (((state * 0x62BD5) ^ 0xC74EAD55) * 0xA5CB3)) ^ state >>> 13) * 0xAF2D9) ^ state >>> 10;
//        return ((state = ((state = ((state ^ 0xC74EAD55) * 0xA5CB3)) ^ state >>> 13) * 0x62BD5) << 22 | state >>> 10);
//        return ((state = (state ^ 0xC74EAD55) * 0xA5CB3) ^ ((state << 20) | (state >>> 12)) ^ ((state << 9) | (state >>> 23)));
        return (state = (state ^ (state << 21 | state >>> 11) ^ (state << 9 | state >>> 23) ^ 0xC74EAD55) * 0xA5CB3) ^ (state >>> 16);
    }
    public static int xlxs(int state)
    {
//        return ((state = ((state ^ 0xC74EAD55) * 0xA5CB3)) ^ state >>> 13);
        return (state *= 0x9E375) ^ state >>> 16;
    }
    public static int xs3(int state)
    {
        state ^= state >>> 14;
        state ^= state >>> 15;
        return state ^ state << 13;
    }
    public static int yuraHash(final CharSequence data, final int seed)
    {
        int h1 = seed ^ 0x41C64E6B;
        int h2 = (seed << 15 | seed >>> 17);
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            h1 += data.charAt(i);
            h2 += (h1 ^= h1 << 3);
            h2 = (h2 << 7 | h2 >>> 25);
            h2 ^= h2 << 2;
        }
        h1 ^= h2;
        h1 += (h2 << 14 | h2 >>> 18);
        h2 ^= h1;
        h2 += (h1 << 26 | h1 >>> 6);
        h1 ^= h2;
        h1 += (h2 << 5 | h2 >>> 27);
        return (h2 + (h1 << 24 | h1 >>> 8)) ^ h1;
    }

    public int slitherHash(int[] data){
        if (data == null)
            return 0;

        long result = 0xC13FA9A902A6328FL ^ data.length * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length; i += 8) {
            result = 0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data[i]
                    + 0xC862B36DAF790DD5L * data[i + 1]
                    + 0xB8ACD90C142FE10BL * data[i + 2]
                    + 0xAA324F90DED86B69L * data[i + 3]
                    + 0x9CDA5E693FEA10AFL * data[i + 4]
                    + 0x908E3D2C82567A73L * data[i + 5]
                    + 0x8538ECB5BD456EA3L * data[i + 6]
                    + 0xD1B54A32D192ED03L * data[i + 7]
            ;
        }
        for (; i < data.length; i++) {
            result = 0x9E3779B97F4A7C15L * result + data[i];
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int)(result ^ result >>> 28);
    }

    public int slitherHash(CharSequence data){
        if (data == null)
            return 0;

        long result = 0xC13FA9A902A6328FL ^ data.length() * 0x9E3779B97F4A7C15L;
        int i = 0;
        for (; i + 7 < data.length(); i += 8) {
            result =  0xEBEDEED9D803C815L * result
                    + 0xD96EB1A810CAAF5FL * data.charAt(i)
                    + 0xC862B36DAF790DD5L * data.charAt(i + 1)
                    + 0xB8ACD90C142FE10BL * data.charAt(i + 2)
                    + 0xAA324F90DED86B69L * data.charAt(i + 3)
                    + 0x9CDA5E693FEA10AFL * data.charAt(i + 4)
                    + 0x908E3D2C82567A73L * data.charAt(i + 5)
                    + 0x8538ECB5BD456EA3L * data.charAt(i + 6)
                    + 0xD1B54A32D192ED03L * data.charAt(i + 7)
            ;
        }
        for (; i < data.length(); i++) {
            result = 0x9E3779B97F4A7C15L * result + data.charAt(i);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int)(result ^ result >>> 28);
    }


    /**
     * Purely experimental; almost definitely won't be as fast as {@link #slitherHash(CharSequence)}.
     * @param data a String or other CharSequence
     * @return a hash code
     */
    public int slitherHash2(CharSequence data){
        if (data == null)
            return 0;
        final int len = data.length();
        long m = 0xCC62FCEB9202FAADL;//0xCB9C59B3F9F87D4DL;//0xCC62FCEB9202FAADL ^ len * 0x9E3779B97F4A7C16L;
        long result = m * len + 0xD1B54A32D192ED03L;
        int i = 0;
//        final long m0 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m1 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m2 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m3 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m4 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m5 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m6 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m7 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m8 = (m * 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L;
//        for (; i + 7 < len; i += 8) {
//            result =  m0 * result
//                    + m1 * data.charAt(i)
//                    + m2 * data.charAt(i + 1)
//                    + m3 * data.charAt(i + 2)
//                    + m4 * data.charAt(i + 3)
//                    + m5 * data.charAt(i + 4)
//                    + m6 * data.charAt(i + 5)
//                    + m7 * data.charAt(i + 6)
//                    + m8 * data.charAt(i + 7)
//            ;
//        }
        for (; i < len; i++) {
            result = (m *= 0xCC62FCEB9202FAADL) * (result + data.charAt(i));
        }
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int)(result ^ result >>> 28);
//        return (int)(result>>>32); 
    }
    
    public int slitherHash3(CharSequence data){
        if (data == null)
            return 0;
        final int len = data.length();
        long m = 0xCC62FCEB9202FAADL ^ len * 0x9E3779B97F4A7C16L;
        long result = m * len + 0xD1B54A32D192ED03L;
        int i = 0;
//        final long m0 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m1 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m2 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m3 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m4 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m5 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m6 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m7 = ((m *= 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L);
//        final long m8 = (m * 0xCC62FCEB9202FAADL) >>> 1 | 0x8000000000000001L;
//        for (; i + 7 < len; i += 8) {
//            result =  m0 * result
//                    + m1 * data.charAt(i)
//                    + m2 * data.charAt(i + 1)
//                    + m3 * data.charAt(i + 2)
//                    + m4 * data.charAt(i + 3)
//                    + m5 * data.charAt(i + 4)
//                    + m6 * data.charAt(i + 5)
//                    + m7 * data.charAt(i + 6)
//                    + m8 * data.charAt(i + 7)
//            ;
//        }
        for (; i < len; i++) {
            result = ((m *= 0xCB9C59B3F9F87D4DL) >>> 1 | 0x8000000000000001L) * (result + data.charAt(i));
        }
//        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int)(result ^ result >>> 28);
//        return (int)(result>>>32); 
    }
    
    public static int oldCoord(long x, long y)
    {
        x *= 0x9E3779B97F4A7C15L;
        y *= 0x632BE59BD9B4E019L;
        return (int) (((x ^ y) >>> ((x & 15) + (y & 15))) * 0x85157AF5L);

    }

    //179922 with (r << 25 | r >>> 7)
    public static int latheCoord(int x, int y)
    {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << 25 | r >>> 7);
    }
    public static int olderLatheCoord(int x, int y)
    {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ r >>> 8;
    }

    public static int latheCoordConfig(int x, int y, int rot)
    {
//        y ^= x;
//        y = (((x << 13 | x >>> 19) ^ y ^ (y << 5)) + (y << 28 | y >>> 4));
//        return Integer.rotateLeft(y, rot) + x;
        
//        y ^= x;
//        y ^= (x << 13 | x >>> 19) ^ (y << 5) ^ (y << 28 | y >>> 4);
//        return (x ^= (y << 11 | y >>> 21)) ^ x >>> rot;
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << rot | r >>> -rot);
    }

    public static int buzzCoord(int x, int y) {
//        x += y * 0xC13FA9A9;
//        y += x * 0x91E10DA5;
        y = (y + ((x+y) * (x+y+1) >> 1)) * 0x9E375;
        return (y ^ y >>> 14);
//        return ((y = (y ^ y >>> 13 ^ 0x9E3779BD) * 0xC6BC2793) ^ y >>> 11);
//        int result = 0xF369B, z = 0xEF17B;
//        result ^= (z += (x ^ 0xC74EAD55) * 0xA5CB3);
//        result ^= (z += (y ^ 0xC74EAD55) * 0xA5CB3);
//        result += (z ^ z >>> 13) * 0x62BD5;
//        return (result ^ result >>> 11 ^ z ^ z >>> 15);
    }

    public static int szudzikCoord(int x, int y)
    {
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
        x += 3;
        y += 3;
        return (x >= y
                ? x * x + x + y
                : x + y * y);
    }

    public static int rosenbergStrongCoord(int x, int y)
    {
        //// sorta a hack because Coord has an effective min of -3...
        x += 3;
        y += 3;
        //// n is assigned the normal Rosenberg-Strong pairing function's result here.
        int n = (x >= y ? x * (x + 2) - y : y * y + x);
        ////boustrophedonic variant; winds in a serpentine, always-connected path
//        int n;
//        if(x >= y) {
//            if((x & 1) == 1)
//                n = x * x + y;
//            else
//                n = x * (x + 2) - y;
//        }
//        else {
//            if((y & 1) == 1)
//                n = y * (y + 2) - x;
//            else
//                n = y * y + x;
//        }
        //return (x >= y) ? (((x & 1) == 1) ? x * x + y : x * (x + 2) - y) : (((y & 1) == 1) ? y * (y + 2) - x : y * y + x);
        
        //// Gray code, XLCG, XLCG, xor (to stay within int range on GWT).
        //// The Gray code moves bits around just a little, but keeps the same power-of-two upper bound.
        //// the XLCGs together only really randomize the upper bits; they don't change the lower bit at all.
        //// the last xor is just for GWT and could be omitted if not targeting JS Numbers.
//        return ((n ^ n >>> 1 ^ 0xD1B54A35) * 0x9E373 ^ 0x7F4A7C15) * 0x125493 ^ 0x91E10DA5;
//        return ((n ^ n >>> 1) * 0x9E373 ^ 0xD1B54A35) * 0x125493 ^ 0x91E10DA5;
//        return ((x >= y ? x * (x + 2) - y : y * y + x) * 0x9E373 ^ 0xD1B54A35) * 0x125493 ^ 0x91E10DA5;
        return (n ^ n >>> 1);
        //// Other options:

        //// Bijective RRLL shift, XLCG, XLCG, xor (to stay within int range on GWT)
//        return ((n ^ n >>> 11 ^ n >>> 23 ^ n << 7 ^ n << 23 ^ 0xD1B54A35) * 0x9E373 ^ 0x7F4A7C15) * 0x125493 ^ 0x91E10DA5;

        //// Bijective combination of bitwise shifts (only some such combinations work non-destructively) 
//        return n ^ n << 7 ^ n << 23 ^ n >>> 11 ^ n >>> 23;
    }


//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        x += ((x >= y ? x * x + x + x - y : y * y + x) ^ 0xD1B54A35) * 0x9E375 + y;
//        return x ^ x >>> 11 ^ x << 15;

//        return (x =  (int) (0xD1B54A32D192ED03L * (x << 1 ^ x >> 31) + 0xABC98388FB8FAC03L * (y << 1 ^ y >> 31) + 0x8CB92BA72F3D8DD7L * (z << 1 ^ z >> 31) >>> 32)) ^ x >>> 15;
//        x = (x << 1 ^ x >> 31);// * 29;
//        y = (y << 1 ^ y >> 31);// * 463;
//        z = (z << 1 ^ z >> 31);// * 5867;
//

    public static int interleaveXorShiftHash(int x, int y, int z)
    {
        x = (x | (x << 16)) & 0x030000FF;
        x = (x | (x <<  8)) & 0x0300F00F;
        x = (x | (x <<  4)) & 0x030C30C3;
        x = (x | (x <<  2)) & 0x09249249;
        y = (y | (y << 16)) & 0x030000FF;
        y = (y | (y <<  8)) & 0x0300F00F;
        y = (y | (y <<  4)) & 0x030C30C3;
        y = (y | (y <<  2)) & 0x09249249;
        z = (z | (z << 16)) & 0x030000FF;
        z = (z | (z <<  8)) & 0x0300F00F;
        z = (z | (z <<  4)) & 0x030C30C3;
        z = (z | (z <<  2)) & 0x09249249;
        return (x |= y << 1 | z << 2) ^ x >>> 16;

//        int n = (x >= y ? x * (x + 2) - y : y * y + x);
//        return (n >= z ? n * (n + 2) - z : z * z + n);

//        x += ((n >= z ? n * (n + 2) - z : z * z + n) ^ 0xD1B54A35) * 0x9E375 + y + z;
//        return x ^ x >>> 11 ^ x << 15;

//        return 0xABC9B * (0x8CBA5 * (0x346D5 * x + y ^ 0xD1B54A33) + z ^ 0xABC98389) ^ 0x8CB92BA7;

        // the commented out one below is better, but doesn't work on GWT
//        return 0x8CB92BA7 * (0xABC98389 * (0xD1B54A33 * x + y) + z);
    }
    public static int wranglerHash3D(int x, int y, int z) {
//        x = (int)(((x * 0xD1B54A32D192ED03L >>> 32) & 0x49249249) | ((y * 0xABC98388FB8FAC03L >>> 32) & 0x92492492) | ((z * 0x8CB92BA72F3D8DD7L >>> 32) & 0x24924924));
        //y ^= ((x << 1 ^ x >> 31) * 29  );
        //z ^= ((y << 1 ^ y >> 31) * 463 );
        //x ^= ((z << 1 ^ z >> 31) * 5867);
        ////0x9E3779B97F4A7C15L
        //y ^= (int)((x ^ (x << 11 | x >>> 21) ^ (x << 19 | x >>> 13)) * 0xD1B54A32D192ED03L);
        //z ^= (int)((y ^ (y << 11 | y >>> 21) ^ (y << 19 | y >>> 13)) * 0xABC98388FB8FAC03L);
        //x ^= (int)((z ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * 0x8CB92BA72F3D8DD7L);
        //y = (x << 1 ^ x >> 31);
        //z = (y << 1 ^ y >> 31);
        //x = (z << 1 ^ z >> 31);
        //long a = (x * 0x9E3L) ^ (y * (0x779L << 22)) ^ (z * (0xB97L << 43));
        //return (int)DiverRNG.randomize(x * 0xD1B54A32D192ED03L + DiverRNG.randomize(y * 0xABC98388FB8FAC03L + DiverRNG.randomize(z * 0x8CB92BA72F3D8DD7L)));
        //return (x = (x * 0xD1B55 & 0x49249249) ^ (y * 0xABC99 & 0x92492492) ^ (z * 0x8CB93 & 0x24924924)) ^ x >>> 16;
        x = (x + (x << 16)) & 0x030000FF;
        x = (x + (x <<  8)) & 0x0300F00F;
        x = (x + (x <<  4)) & 0x030C30C3;
        x = (x + (x <<  2)) & 0x09249249;
        y = (y + (y << 16)) & 0x030000FF;
        y = (y + (y <<  8)) & 0x0300F00F;
        y = (y + (y <<  4)) & 0x030C30C3;
        y = (y + (y <<  2)) & 0x09249249;
        z = (z + (z << 16)) & 0x030000FF;
        z = (z + (z <<  8)) & 0x0300F00F;
        z = (z + (z <<  4)) & 0x030C30C3;
        z = (z + (z <<  2)) & 0x09249249;
        return (x |= y << 1 | z << 2) ^ x >>> 16;
    }

    public static int szudzik2Coord(int x, int y)
    {
//        s = 42 ^ s * 0x1827F5 ^ y * 0x123C21;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;

        //return (x + (x >= y ? x * x + y : y * y)) * 0x41C6B ^ 0x9E3779BD;
        //x *= 0x41C64E6B;
        //y *= 0x9E3779BD;
//        return (x += (x >= y ? x * x + y : y * y)) ^ (x << 11 | x >>> 21) ^ (x << 20 | x >>> 12);
        return ((x += (x >= y ? x * x + y : y * y)) ^ (x << 11 | x >>> 21) ^ (x << 20 | x >>> 12)) * 0x13C6EF;

//        return x ^ (x << 13 | x >>> 19) ^ (x << 22 | x >>> 12) ^ (x << 6 | x >>> 26) ^ (x << 29 | x >>> 3);
//        x ^= x >>> (x >>> 28) + 4 ^ 0x91E10DA5;
//        return x * 0x125493;
//        return x ^ x >>> (x >>> 28) + 4;

//        x ^= x >> 31;
//        y ^= y >> 31;
//        return ((x >= y 
//                ? x * x + x + y 
//                : x + y * y) ^ 0xD1B54A35) * 0x125493 ^ 0x9E3779B9;

    }


    public static int cantorCoord(int x, int y)
    {
//        x ^= x >> 31;
//        y ^= y >> 31;
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        return (((x+y >> 1) * (x+y+1 >> 1)) + y);// * 0xA5CB3;

//        x ^= x >> 1;
//        y ^= y >> 1;
        x += 3;
        y += 3;
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
        return y + ((x+y) * (x+y+1) >> 1);
//        return y ^ y >>> 1;

//        return (((x+y) * (x+y+1) >> 1) + y);// * 0xA5CB3;
    }

    public static int cantor3D(int x, int y, int z){
        x = x << 1 ^ x >> 31;
        y = y << 1 ^ y >> 31;
        z = z << 1 ^ z >> 31;
        y += ((x+y) * (x+y+1) >> 1);
        return z + ((z+y) * (z+y+1) >> 1);
    }

    public static int balancedCantorCoord(int x, int y) {
        final int sx = x >> 31, sy = y >> 31;
        x ^= sx;
        y ^= sy;
        return (((x + y) * (x + y + 1) >>> 1) + y << 2) - sx - sy - sy;
    }
    public static int goldCoord(int x, int y)
    {
        int s = 42;
        y ^= (s ^ 0xD192ED03) * 0x1A36A9;
        x ^= (y ^ 0xFB8FAC03) * 0x157931;
        s ^= (x ^ 0x2F3D8DD7) * 0x119725;
        return (s = (s ^ s >>> 11 ^ s >>> 21) * (s | 0xFFE00001) ^ x ^ y) ^ s >>> 13 ^ s >>> 19;
    }

    public static int pelotonCoord(int x, int y) {
//        final long a = x + CrossHash.Water.b1, b = y + CrossHash.Water.b2;
//        return (int) ((a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25)) >>> 32);
//        x = (x ^ (x << 13 | x >>> 19) ^ (x << 21 | x >>> 11));
//        y = (y ^ (y << 15 | y >>> 17) ^ (y << 29 | y >>> 3));
//        x ^= x >> 31;
//        y ^= y >> 31;

//        y += ((x+y) * (x+y+1) >> 1);

//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        y += (y >= x ? y * y + x : x * x);
//        x *= 0x125493;
//        y *= 0x177C0B;
        
//        //working fine except for lowest 4 bits
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        y += ((x+y) * (x+y+1) >> 1);
//        y ^= y >>> 2 ^ y >>> 11;
//        y = ((y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35) * 0x125493;
//        return y ^ y >>> 1;

        // similar to above but has slightly better low bits
//        x = x << 1 ^ x >> 31;
//        y = y << 1 ^ y >> 31;
//        y += ((x+y) * (x+y+1) >> 1);
//        y ^= y >>> 2 ^ y >>> 5 ^ y >>> 6 ^ y >>> 11;
//        y = ((y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35) * 0x125493;
//        return y ^ y >>> 1;

        // about the same as either hash above in collision rates, beats raw Cantor, but horrible visual bit bias
        return (int)((x * 0xC13FA9A902A6328FL + y * 0x91E10DA5C79E7B1DL) >>> 32);
//        return (int)((x * 0xC13FA9A902A6328FL ^ y * 0x91E10DA5C79E7B1DL) >>> 32);
//        return (rosenbergStrongCoord(x, y) ^ 0xD1B54A35) * 0x9E3779BB;
// * 0x9E3779B9
// ^ 0x9E3779BD;
//        y ^= y >>> 1 ^ y >>> 6;
//        y = (y ^ (y << 15 | y >>> 17) ^ (y << 23 | y >>> 9)) * 0xACEDB;
//        y = (y ^ (y << 11 | y >>> 21) ^ (y << 29 | y >>> 3)) * 0xACEDB ^ 0xD1B54A35;
//        return y ^ y >>> 1;

        //        return (y ^ (y << 13 | y >>> 19) ^ (y << 21 | y >>> 11) ^ 0xD1B54A35) * 0xACEDB ^ 0x9E3779BD;
//        return (y ^ y >>> 1);// * 0x125493 ^ 0xD1B54A35;

//        y =
//                (x >= y
//                ? x * x + x + y
//                : x + y * y);
        
        
//        int s = 42 ^ x * 0x1827F5 ^ y * 0x123C21;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;

        //0xD1B54A32D192ED03L, 0xABC98388FB8FAC03L, 0x8CB92BA72F3D8DD7L
    }

    public static int peloton3D(int x, int y, int z) {
        final int n = (29 * (x << 1 ^ x >> 31) + 463 * (y << 1 ^ y >> 31) + 5867 * (z << 1 ^ z >> 31));
        return n ^ n >>> 14;

//        int s = 42 ^ x + y + z;
//        s = (x ^ (x << 7 | x >>> 25) ^ (x << 19 | x >>> 13) ^ s) * 0x1A36A9;// ^ 0x02A6328F;
//        s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
//        s = (y ^ (y << 9 | y >>> 23) ^ (y << 21 | y >>> 11) ^ s) * 0x157931;// ^ 0xC79E7B1D;
//        s ^= s >>> 10 ^ s >>> 15 ^ s << 7;
//        s = (z ^ (z << 11 | z >>> 21) ^ (z << 23 | z >>> 9) ^ s) * 0x119725;// ^ 0xC79E7B1D;
//        return s ^ s >>> 10 ^ s >>> 15 ^ s << 7;
//        int s = 0x75AE2165;
//        s = (s + x + y + z ^ 0x75AE2165) * 0x1B69E1;
//        s += (x ^ 0x03A4615F) * 0x177C0B;
//        s += (y ^ 0xA1FE1575) * 0x141E5D;
//        s += (z ^ 0x7D9ED689) * 0x113C31;
//        int s = 0x9E3779B9;
//        s = (s ^ s << 8 ^ s >>> 15 ^ s >>> 9 ^ x) * 0x177C0B;
//        s = (s ^ s << 8 ^ s >>> 15 ^ s >>> 9 ^ y) * 0x141E5B;
//        s = (s ^ s << 8 ^ s >>> 15 ^ s >>> 9 ^ z) * 0x113C33;
//        return s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11);
//        s = (s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11) ^ x * 3) * 0x177C0B;
//        s = (s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11) ^ y * 5) * 0x141E5B;
//        s = (s ^ (s << 11 | s >>> 21) ^ (s << 21 | s >>> 11) ^ z * 7) * 0x113C33;
        //s ^= s << 8 ^ s >>> 15 ^ s >>> 9;
//        int s = 0x9E3779B9 ^ x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;


//        x ^= x >> 31;
//        y ^= y >> 31;
//        z ^= z >> 31;

//        x += (x >= z ? x * x + z : z * z);
//        y += (y >= x ? y * y + x : x * x);

        //y += ((z+y) * (z+y+1) >> 1);
        //y += ((x+y) * (x+y+1) >> 1);
        //y ^= y >>> 1 ^ y >>> 6;
        //return (y ^ (y << 15 | y >>> 17) ^ (y << 21 | y >>> 11)) * 0x125493 ^ 0xD1B54A35;

//        int s = 0x9E3779B9 ^ x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
//        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 6 | s >>> 26) ^ 0xD1B54A35) * 0x125493) ^ s >>> 15;


//        int s = 0x9E3779B9 ^ 0x1A36A9 * (x ^ 0x157931 * (y ^ z * 0x119725));
//        return ((s = ((s = (s ^ (s << 19 | s >>> 13) ^ (s << 7 | s >>> 25) ^ 0xD1B54A35) * 0xAEF17) ^ (s << 20 | s >>> 12) ^ (s << 8 | s >>> 24)) * 0xDB4F) ^ s >>> 14);
        
//        return ((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 14 | state >>> 50) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ (state << 40 | state >>> 24) ^ (state << 15 | state >>> 49)) * 0xDB4F0B9175AE2165L) ^ state >>> 28);
//        int s = 0x75AE2165;
//        s ^= s << 8 ^ s >>> 15 ^ s >>> 9 ^ (x * 0x177C0B);
//        s ^= s << 8 ^ s >>> 15 ^ s >>> 9 ^ (y * 0x141E5B);
//        s ^= s << 8 ^ s >>> 15 ^ s >>> 9 ^ (z * 0x113C33);
//        return s ^ s << 8 ^ s >>> 15 ^ s >>> 9;

        //0xDB4F0B9175AE2165L, 0xBBE0563303A4615FL, 0xA0F2EC75A1FE1575L, 0x89E182857D9ED689L
    }
    
    public static int goRogueCoord(int x, int y){

        x *= 0x9E3779B9;
        y *= 0x632BE5AB;
        return ((x + y) >>> ((x & 15) ^ (y & 15))) * 0x85157AF5;
    }
    public static int scratcherCoord(int x, int y, int n) {
        /*
    const uvec2 m = uvec2(0xA0F2EC75u, 0x91E10DA5u); //0xd1342543de82ef95L, 0xf7c2ebc08f67f2b5L
    const uvec2 a = uvec2(0x1b873593u, 0xcc9e2d51u); //0xa812d533b278e4adL, 0x9c8f2d355d1346b5L
    p = p * m + a;
    uint u = p.x + p.y;
    u ^= u >> 16u;
    u = u * 0x9E3779BDu;
    return u ^ u >> 6u ^ u >> 26u;
         */
        //(n << 13 | n >>> 19) ^ (n << 29 | n >>> 3) ^
//        n += (x * 0x7C735) + (y * 0x75915);
//        return (n >>> ((x & 15) ^ (y & 15))) * 0x9E373;
        n ^= (((x * 0x7C735) + (y * 0x75915) - n ^ 0xD1B54A35) * 0x9E373 ^ 0x91E10DA5) * 0x125493;
        return n ^ n >>> 16;
//        n += (x * 0xFAAE1A75) + (y * 0xEE5C155D);
//        return (n >>> ((x & 15) ^ (y & 15))) * 0x93D765DD;

        //n += y + ((x + y) * (x + y + 1) >>> 1);
//        n = (n ^ n >>> 15) * 0x9E373;
//        n = (n ^ n >>> 15) * 0x7C8A5;
//        return n ^ n >>> 16;

//        n ^= (x * 0x7C8A5) + (y * 0x7E57D);
//        n ^= n >>> 16;
//        n *= 0x9E375;
//        return n ^ n >>> 16;
    }

    public static int iphHash(int x, int y, int s) {
        s ^= x * 0x1827F5 ^ y * 0x123C21;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 16;
    }

//        y ^= (s ^ 0xD192ED03) * 0x1A36A9;
//        x ^= (y ^ 0xFB8FAC03) * 0x157931;
//        s ^= (x ^ 0x2F3D8DD7) * 0x119725;
//        return (s = (s ^ s >>> 11 ^ s >>> 21) * (s | 0xFFE00001) ^ x ^ y) ^ s >>> 13 ^ s >>> 19;

    /**
     * Running on sizes: 16, 56, 61, 65, 92, 95, 103, 106, 116, 131, 136, 150, 152, 172, 220, 273, 293, 297, 311, 316, 324, 327, 353, 357, 360, 369, 370, 376, 386, 398
     * Number of Coords added: 150053244
     * TOTAL Lath collisions: 4498342 (2.997830556732249%), BEST 0, WORST 9010
     * TOTAL Szud collisions: 5240115 (3.492170419188005%), BEST 0, WORST 6438
     * TOTAL Pelo collisions: 4494271 (2.9951175197518554%), BEST 0, WORST 6724
     * TOTAL Cant collisions: 7260455 (4.8385858289075045%), BEST 0, WORST 8859
     * TOTAL Gold collisions: 23246597 (15.492232210587863%), BEST 1, WORST 21527
     * TOTAL Obje collisions: 106566447 (71.01908906414579%), BEST 0, WORST 87793
     * <br>
     * Running on sizes: 16, 56, 61, 65, 92, 95, 103, 106, 116, 131, 136, 150, 152, 154, 172, 173, 220, 273, 293, 297, 311, 316, 324, 327, 353, 357, 360, 369, 370, 376, 386, 398
     * Number of Coords added: 192057238
     * TOTAL Lath collisions: 32765410 (17.060231804437382%), BEST 0, WORST 30062
     * TOTAL Szud collisions: 28092096 (14.6269394960267%), BEST 0, WORST 26903
     * TOTAL Pelo collisions: 28679649 (14.932865482528703%), BEST 1, WORST 27994
     * TOTAL Cant collisions: 28541456 (14.86091141225305%), BEST 0, WORST 27817
     * TOTAL Gold collisions: 38067211 (19.8207635371701%), BEST 2, WORST 33344
     * TOTAL Obje collisions: 112523166 (58.588349583575706%), BEST 1, WORST 99462
     * <br>
     * LightRNG(123)
     * Running on sizes: 50, 63, 81, 97, 139, 140, 141, 146, 163, 167, 184, 203, 222, 269, 289, 298, 299, 327, 382, 384
     * Number of Coords added: 73569142
     * TOTAL Lath collisions: 4044041 (5.49692559959446%), BEST 0, WORST 18731
     * TOTAL Szud collisions: 4700375 (6.389057792735982%), BEST 0, WORST 15880
     * TOTAL Pelo collisions: 11855242 (16.11442199502612%), BEST 0, WORST 29064
     * TOTAL Cant collisions: 4307949 (5.855646651418064%), BEST 3, WORST 14865
     * TOTAL RoSt collisions: 2896004 (3.936438459483461%), BEST 0, WORST 14826
     * TOTAL GoRo collisions: 16773892 (22.800173474906096%), BEST 10, WORST 46038
     * TOTAL Obje collisions: 55388439 (75.2875968024746%), BEST 14, WORST 135199
     * <br>
     * GoatRNG(123456789, 987654321)
     * Running on sizes: 8, 18, 41, 45, 60, 154, 157, 168, 202, 207, 245, 246, 308, 311, 335, 363, 387, 390, 391, 392
     * Number of Coords added: 88148616
     * TOTAL Lath collisions: 4100452 (4.651748587862117%), BEST 0, WORST 21861
     * TOTAL Szud collisions: 4580116 (5.195902338387253%), BEST 0, WORST 17076
     * TOTAL Pelo collisions: 12073632 (13.696904781806216%), BEST 0, WORST 31134
     * TOTAL Cant collisions: 5122095 (5.810749201099198%), BEST 1, WORST 17782
     * TOTAL RoSt collisions: 3055698 (3.466529752435364%), BEST 0, WORST 15972
     * TOTAL GoRo collisions: 19922851 (22.60143369692838%), BEST 1, WORST 49420
     * TOTAL Obje collisions: 69577617 (78.9321717768093%), BEST 0, WORST 141151
     */
    @Test
    @Ignore
    public void testCoord() {
        RNG prng = new RNG(new GoatRNG(1234567890, 987654321));
        final int[] params = new int[20];// ArrayTools.range(10, 26);// new int[]{33, 65, 129, 257, 513};
        System.arraycopy(prng.randomOrdering(400), 0, params, 0, params.length);
        Arrays.sort(params);
//        final int[] params = new int[]{256+3};
//        final int[] params = new int[]{32+3, 64+3, 128+3, 256+3};
        System.out.println("Running on sizes: " + StringKit.join(", ", params));
        long lathTotal = 0L, objeTotal = 0L, peloTotal = 0L, rostTotal = 0L, szudTotal = 0L, cantTotal = 0L, xoroTotal = 0L, total = 0L,
                lathBest = 1000000L,
                objeBest = 1000000L,
                peloBest = 1000000L,
                rostBest = 1000000L,
                szudBest = 1000000L,
                cantBest = 1000000L,
                xoroBest = 1000000L,
                lathWorst = 0L, objeWorst = 0L, peloWorst = 0L, rostWorst = 0L, szudWorst = 0L, cantWorst = 0L, xoroWorst = 0L, t;
//        long[] confTotals = new long[31];
        for (int reduction = 7; reduction >= 0; reduction--) {
            System.out.println("Running reduction level " + reduction);
            for (int w : params) {
                final int WIDTH = w;
                for (int h : params) {
                    final int HEIGHT = h;
                    Coord.expandPoolTo(WIDTH-3, HEIGHT-3);
                    int SIZE = WIDTH * HEIGHT;
                    int restrict = HashCommon.nextPowerOfTwo(SIZE) - 1;

                    IntSet colliderLath = new IntSet(SIZE, 0.5f),
                            colliderObje = new IntSet(SIZE, 0.5f),
                            colliderPelo = new IntSet(SIZE, 0.5f),
                            colliderSzud = new IntSet(SIZE, 0.5f),
                            colliderCant = new IntSet(SIZE, 0.5f),
                            colliderRoSt = new IntSet(SIZE, 0.5f),
                            colliderXoro = new IntSet(SIZE, 0.5f);
//                    IntSet[] colliders = new IntSet[31];
//                    for (int i = 0; i < 31; i++) {
//                        colliders[i] = new IntSet(SIZE, 0.5f);
//                    }
                    LightRNG rng = new LightRNG(1L);
//                    ShuffledIntSequence
//                            xShuffle = new ShuffledIntSequence(WIDTH, 1),
//                            yShuffle = new ShuffledIntSequence(HEIGHT, -1);
                    BitSet points = new BitSet(WIDTH * HEIGHT << 2);
//                    UnorderedSet<Coord> points = new UnorderedSet<>(WIDTH * HEIGHT);
                    for (int i = 0; i < WIDTH; i++) {
                        for (int j = 0; j < HEIGHT; j++) {
//                            int x = xShuffle.next(), y = yShuffle.next();
//                            int x = xShuffle.next() ^ -rng.next(1), y = yShuffle.next() ^ -rng.next(1);
//                            int c = x + WIDTH + 1 + (y + HEIGHT + 1) * WIDTH;
                            int x = i - (WIDTH >>> 1), y = j - (HEIGHT >>> 1);
                            int c = i + j * WIDTH;
                            if (rng.next(3) > reduction || points.get(c)) {
                                --SIZE;
                                continue;
                            }
                            points.set(c);
                            colliderLath.add(latheCoord(x, y) & restrict);
                            colliderSzud.add(balancedCantorCoord(x, y) & restrict);
                            colliderPelo.add(iphHash(x, y, 0x1337BEEF) & restrict);
                            colliderCant.add(cantorCoord(x, y) & restrict);
                            colliderRoSt.add(rosenbergStrongCoord(x, y) & restrict);
                            colliderXoro.add(scratcherCoord(x, y, 0x1337BEEF) & restrict);
                            colliderObje.add(Objects.hash(x, y) & restrict);
                            
//                            for (int i = 0; i < 31; i++) {
//                                colliders[i].add(latheCoordConfig(x, y, i + 1) & restrict);
//                            }
                        }
                    }
//                    System.out.println("WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT + ", SIZE: " + SIZE);
                    t = (SIZE - colliderLath.size); lathBest = Math.min(t, lathBest); lathWorst = Math.max(t, lathWorst);
//                    System.out.println("Lath collisions: " + t);
                    t = (SIZE - colliderPelo.size); peloBest = Math.min(t, peloBest); peloWorst = Math.max(t, peloWorst);
//                    System.out.println("Pelo collisions: " + t);
                    t = (SIZE - colliderSzud.size); szudBest = Math.min(t, szudBest); szudWorst = Math.max(t, szudWorst);
//                    System.out.println("Szud collisions: " + t);
                    t = (SIZE - colliderCant.size); cantBest = Math.min(t, cantBest); cantWorst = Math.max(t, cantWorst);
//                    System.out.println("Cant collisions: " + t);
                    t = (SIZE - colliderRoSt.size); rostBest = Math.min(t, rostBest); rostWorst = Math.max(t, rostWorst);
//                    System.out.println("RoSt collisions: " + t);
                    t = (SIZE - colliderXoro.size); xoroBest = Math.min(t, xoroBest); xoroWorst = Math.max(t, xoroWorst);
//                    System.out.println("xoro collisions: " + t);
                    t = (SIZE - colliderObje.size); objeBest = Math.min(t, objeBest); objeWorst = Math.max(t, objeWorst);
//                    System.out.println("Obje collisions: " + t);

//                    for (int i = 0; i < 31; i++) {
//                        System.out.println("Lathe " + (i + 1) + ": " + (SIZE - colliders[i].size));
//                        confTotals[i] += (SIZE - colliders[i].size);
//                    }
                    lathTotal += (SIZE - colliderLath.size);
                    peloTotal += (SIZE - colliderPelo.size);
                    szudTotal += (SIZE - colliderSzud.size);
                    cantTotal += (SIZE - colliderCant.size);
                    rostTotal += (SIZE - colliderRoSt.size);
                    xoroTotal += (SIZE - colliderXoro.size);
                    objeTotal += (SIZE - colliderObje.size);
                    total += SIZE;
                }
            }
        }
        System.out.println("Number of Coords added: " + total);
        System.out.println("TOTAL Lath collisions: " + lathTotal + " (" + (lathTotal * 100.0 / total) + "%), BEST " + lathBest + ", WORST " + lathWorst);
        System.out.println("TOTAL BalC collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
        System.out.println("TOTAL InPo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
        System.out.println("TOTAL Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
        System.out.println("TOTAL RoSt collisions: " + rostTotal + " (" + (rostTotal * 100.0 / total) + "%), BEST " + rostBest + ", WORST " + rostWorst);
        System.out.println("TOTAL Scra collisions: " + xoroTotal + " (" + (xoroTotal * 100.0 / total) + "%), BEST " + xoroBest + ", WORST " + xoroWorst);
        System.out.println("TOTAL Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);
//        for (int i = 0; i < 31; i++) {
//            System.out.println("TOTAL Lath_"+(i+1)+" collisions: " + confTotals[i] + " (" + (confTotals[i] * 100.0 / total) + "%)");
//        }
    }
    // This one takes a while to run; be advised.
    @Test
    @Ignore
    public void testCoord3() {
        final int[] params = ArrayTools.range(8, 25);// new int[]{33, 65, 129, 257, 513};
//        final int[] params = new int[]{64, 128, 256, 512};
        long baseTotal = 0L, objeTotal = 0L, peloTotal = 0L, hastTotal = 0L, szudTotal = 0L, cantTotal = 0L, total = 0L,
                baseBest = 1000000L,
                objeBest = 1000000L,
                peloBest = 1000000L,
                hastBest = 1000000L,
                szudBest = 1000000L,
                cantBest = 1000000L,
                baseWorst = 0L, objeWorst = 0L, peloWorst = 0L, hastWorst = 0L, szudWorst = 0L, cantWorst = 0L, t;
//        long[] confTotals = new long[31];
        for (int reduction = 7; reduction >= 0; reduction--) {

            for (int d : params) {
                int DEPTH = d + d + 1;
                for (int w : params) {
                    int WIDTH = w + w + 1;
                    for (int h : params) {
                        int HEIGHT = h + h + 1;
                        int SIZE = WIDTH * HEIGHT * DEPTH;
                        int restrict = HashCommon.nextPowerOfTwo(SIZE) - 1;

                        IntSet colliderBase = new IntSet(SIZE, 0.5f),
                                colliderObje = new IntSet(SIZE, 0.5f),
                                colliderPelo = new IntSet(SIZE, 0.5f),
                                colliderSzud = new IntSet(SIZE, 0.5f),
                                colliderCant = new IntSet(SIZE, 0.5f),
                                colliderHast = new IntSet(SIZE, 0.5f);
//                    IntSet[] colliders = new IntSet[31];
//                    for (int i = 0; i < 31; i++) {
//                        colliders[i] = new IntSet(SIZE, 0.5f);
//                    }
                        DiverRNG rng = new DiverRNG(SIZE);
//                        ShuffledIntSequence
//                                xShuffle = new ShuffledIntSequence(WIDTH, rng.nextInt()),
//                                yShuffle = new ShuffledIntSequence(HEIGHT, rng.nextInt()),
//                                zShuffle = new ShuffledIntSequence(DEPTH, rng.nextInt());
                        UnorderedSet<Coord3D> points = new UnorderedSet<>(SIZE);
                        for (int x = -w; x <= w; x++) {
                            for (int y = -h; y <= h; y++) {
                                for (int z = -d; z <= d; z++) {
//                            int x = xShuffle.next(), y = yShuffle.next();
//                                    int x = xShuffle.next() ^ -rng.next(1), y = yShuffle.next() ^ -rng.next(1), z = zShuffle.next() ^ -rng.next(1);
                                    Coord3D c = Coord3D.get(x, y, z);
                                    if (rng.next(3) > reduction || points.contains(c)) {
                                        --SIZE;
                                        continue;
                                    }
                                    points.add(c);
                                    colliderBase.add(IntPointHash.hashAll(x, y, z, 0x9E3779B9) & restrict);
                                    colliderPelo.add(peloton3D(x, y, z) & restrict);
//                                    colliderPelo.add((29 * (x << 1 ^ x >> 31) + 1721 * (y << 1 ^ y >> 31) + 95713 * (z << 1 ^ z >> 31)) & restrict);
//                                    colliderPelo.add((0xD1B54A33 * x + 0xABC98383 * y + 0x8CB92BA7 * z) & restrict);
                                    colliderSzud.add(cantor3D(x, y, z) & restrict);
//                                    colliderSzud.add(szudzikCoord(z, szudzikCoord(x, y)) & restrict);
                                    colliderCant.add(cantorCoord(z, cantorCoord(x, y)) & restrict);
                                    colliderHast.add(interleaveXorShiftHash(x, y, z) & restrict);
//                                    colliderHast.add(rosenbergStrongCoord(z, rosenbergStrongCoord(x, y)) & restrict);
//                                    colliderHast.add((int) Noise.HastyPointHash.hashAll(x, y, z, 0x9E3779B9L) & restrict);
                                    colliderObje.add(Objects.hash(x, y, z) & restrict);
//                            for (int i = 0; i < 31; i++) {
//                                colliders[i].add(latheCoordConfig(x, y, i + 1) & restrict);
//                            }
                                }
                            }
                        }
//                    System.out.println("WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT + ", SIZE: " + SIZE);
                        t = (SIZE - colliderBase.size);
                        baseBest = Math.min(t, baseBest);
                        baseWorst = Math.max(t, baseWorst);
//                    System.out.println("Base collisions: " + t);
                        t = (SIZE - colliderPelo.size);
                        peloBest = Math.min(t, peloBest);
                        peloWorst = Math.max(t, peloWorst);
//                    System.out.println("Szu2 collisions: " + t);
                        t = (SIZE - colliderSzud.size);
                        szudBest = Math.min(t, szudBest);
                        szudWorst = Math.max(t, szudWorst);
//                    System.out.println("Szud collisions: " + t);
                        t = (SIZE - colliderCant.size);
                        cantBest = Math.min(t, cantBest);
                        cantWorst = Math.max(t, cantWorst);
//                    System.out.println("Cant collisions: " + t);
                        t = (SIZE - colliderHast.size);
                        hastBest = Math.min(t, hastBest);
                        hastWorst = Math.max(t, hastWorst);
//                    System.out.println("Gold collisions: " + t);
                        t = (SIZE - colliderObje.size);
                        objeBest = Math.min(t, objeBest);
                        objeWorst = Math.max(t, objeWorst);
//                    System.out.println("Obje collisions: " + t);

//                    for (int i = 0; i < 31; i++) {
//                        System.out.println("Lathe " + (i + 1) + ": " + (SIZE - colliders[i].size));
//                        confTotals[i] += (SIZE - colliders[i].size);
//                    }
                        baseTotal += (SIZE - colliderBase.size);
                        peloTotal += (SIZE - colliderPelo.size);
                        szudTotal += (SIZE - colliderSzud.size);
                        cantTotal += (SIZE - colliderCant.size);
                        hastTotal += (SIZE - colliderHast.size);
                        objeTotal += (SIZE - colliderObje.size);
                        total += SIZE;
                    }
                }
            }
            System.out.println("INTERMEDIATE number of Coords added: " + total + " on reduction " + reduction);
            System.out.println("INTERMEDIATE Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%), BEST " + baseBest + ", WORST " + baseWorst);
            System.out.println("INTERMEDIATE Wran collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
            System.out.println("INTERMEDIATE Pelo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
            System.out.println("INTERMEDIATE Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
            System.out.println("INTERMEDIATE InXS collisions: " + hastTotal + " (" + (hastTotal * 100.0 / total) + "%), BEST " + hastBest + ", WORST " + hastWorst);
            System.out.println("INTERMEDIATE Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);

        }
        System.out.println("Number of Coords added: " + total);
        System.out.println("TOTAL Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%), BEST " + baseBest + ", WORST " + baseWorst);
        System.out.println("TOTAL Wran collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
        System.out.println("TOTAL Pelo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
        System.out.println("TOTAL Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
        System.out.println("TOTAL InXS collisions: " + hastTotal + " (" + (hastTotal * 100.0 / total) + "%), BEST " + hastBest + ", WORST " + hastWorst);
        System.out.println("TOTAL Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);
//        for (int i = 0; i < 31; i++) {
//            System.out.println("TOTAL Lath_"+(i+1)+" collisions: " + confTotals[i] + " (" + (confTotals[i] * 100.0 / total) + "%)");
//        }
    }
    @Test
    @Ignore
    public void testCoordPrimes() {
        final int[] params = ArrayTools.range(8, 14);// new int[]{33, 65, 129, 257, 513};
//        final int[] params = new int[]{64, 128, 256, 512};
        Random r = new Random(123456);
//        BigInteger prime = BigInteger.valueOf(10);
        long bestTotal = Long.MAX_VALUE;
        long usedTotal = 1;
        int best1 = 1, best2 = 1, best3 = 1;
        for (int i = 0; i < 1000; i++) {
//            prime = prime.nextProbablePrime();
//            int p = prime.intValue();
            int p1 = BigInteger.probablePrime(5, r).intValue();
            int p2 = BigInteger.probablePrime(9, r).intValue();
            int p3 = BigInteger.probablePrime(13, r).intValue();
            long total = 0L,
                    baseTotal = 0L,
                    baseBest = 1000000L,
                    baseWorst = 0L,
                    t;
//        long[] confTotals = new long[31];
            for (int reduction = 7; reduction >= 0; reduction--) {

                for (int d : params) {
                    int DEPTH = d + d + 1;
                    for (int w : params) {
                        int WIDTH = w + w + 1;
                        for (int h : params) {
                            int HEIGHT = h + h + 1;
                            int SIZE = WIDTH * HEIGHT * DEPTH;
                            int restrict = HashCommon.nextPowerOfTwo(SIZE) - 1;

                            IntSet colliderBase = new IntSet(SIZE, 0.5f);
// TOTAL Obje collisions: 35610709 (77.4917716409743%), BEST 8911, WORST 39636
                            DiverRNG rng = new DiverRNG(123);
                            UnorderedSet<Coord3D> points = new UnorderedSet<>(SIZE);
                            for (int x = -w; x <= w; x++) {
                                for (int y = -h; y <= h; y++) {
                                    for (int z = -d; z <= d; z++) {
                                        Coord3D c = Coord3D.get(x, y, z);
                                        if (rng.next(3) > reduction || points.contains(c)) {
                                            --SIZE;
                                            continue;
                                        }
                                        points.add(c);
                                        colliderBase.add((p1 * x + p2 * y + p3 * z) & restrict);
//                                        colliderBase.add((p * p * p * x + p * p * y + p * z) & restrict);
                                    }
                                }
                            }
//                    System.out.println("WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT + ", SIZE: " + SIZE);
                            t = (SIZE - colliderBase.size);
                            baseBest = Math.min(t, baseBest);
                            baseWorst = Math.max(t, baseWorst);
                            baseTotal += (SIZE - colliderBase.size);
                            total += SIZE;
                        }
                    }
                }
//            System.out.println("INTERMEDIATE number of Coords added: " + total + " on reduction " + reduction);
//            System.out.println("INTERMEDIATE Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%), BEST " + baseBest + ", WORST " + baseWorst);
//            System.out.println("INTERMEDIATE Szud collisions: " + szudTotal + " (" + (szudTotal * 100.0 / total) + "%), BEST " + szudBest + ", WORST " + szudWorst);
//            System.out.println("INTERMEDIATE Pelo collisions: " + peloTotal + " (" + (peloTotal * 100.0 / total) + "%), BEST " + peloBest + ", WORST " + peloWorst);
//            System.out.println("INTERMEDIATE Cant collisions: " + cantTotal + " (" + (cantTotal * 100.0 / total) + "%), BEST " + cantBest + ", WORST " + cantWorst);
//            System.out.println("INTERMEDIATE RoSt collisions: " + hastTotal + " (" + (hastTotal * 100.0 / total) + "%), BEST " + hastBest + ", WORST " + hastWorst);
//            System.out.println("INTERMEDIATE Obje collisions: " + objeTotal + " (" + (objeTotal * 100.0 / total) + "%), BEST " + objeBest + ", WORST " + objeWorst);
//
            }
//            System.out.println(p1 + "," + p2 + "," + p3 + ": Number of Coords added: " + total);
            System.out.println(p1 + "," + p2 + "," + p3 + ": TOTAL Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%), BEST " + baseBest + ", WORST " + baseWorst);
            if(bestTotal > baseTotal){
                bestTotal = baseTotal;
                usedTotal = total;
                best1 = p1;
                best2 = p2;
                best3 = p3;
            }
        }
        System.out.println();
        System.out.println("BEST SO FAR:");
        System.out.println(best1 + "," + best2 + "," + best3 + ", with " + bestTotal + " collisions (" + (bestTotal * 100.0 / usedTotal) + "%)");
    }






    @Test
    @Ignore
    public void testLimited()
    {
        int restrict = 0xFFFF;
        MarkovTextLimited markovText = new MarkovTextLimited();
//        String theme = "dun dun dun, dun dundun, dun dundun, dun dun dun dun dundun dun dundun.";
        String party = "party party party, I wanna have a party, we're gonna have a party, you better have a party!" +
                "Oh party party party, you're gonna party hearty, we're gonna have a party, or else you will be sorry!";
        markovText.analyze(party);//theme.replace("dun", "wiggle")
        final int LIMIT = restrict * 4 / 5;
        HashSet<String> strings = new HashSet<>(LIMIT);
        StringBuilder sb = new StringBuilder(LIMIT);
        int permissible = StringKit.PERMISSIBLE_CHARS.length();
        for (int i = 0; i < (LIMIT << 2) && strings.size() < LIMIT; i++) {
            // usually not necessary
//            strings.add(markovText.chain(DiverRNG.determine(i * 0x9E3779B97F4A7C15L + 0xC6BC279692B5CC83L), 170));
            // try changing length
            strings.add(markovText.chain(i, 280));
//            strings.add(String.format("       %s       ", Integer.toString(i, 4)));
//            strings.add(StringKit.bin(DiverRNG.determine(i)));
//            strings.add(StringKit.hex(i) + StringKit.bin(i));
//            strings.add("        " + StringKit.hex(i) + "        ");
            //// Wisp does extremely badly here.
//            strings.add(StringKit.bin(i));
//            strings.add("        " + StringKit.bin(i) + "        ");
//            strings.add(StringKit.bin(DiverRNG.randomize(i)) + StringKit.bin(DiverRNG.determine(i)));
//            strings.add(FakeLanguageGen.CHINESE_ROMANIZED.sentence(DiverRNG.determine(i + 0x12345678), 10, 10));
//            strings.add(FakeLanguageGen.GREEK_AUTHENTIC.sentence(DiverRNG.determine(i + 0x12345678), 10, 10));
            //// this is a pathologically bad case for String.hashCode(), but using '\u0000' is even worse
//            strings.add(sb.append(' ').toString());
//            strings.add(sb.append(ArrayTools.letterAt(i)).toString());
        }
        int stringHashLength = strings.size();
        IntSet colliderJDK = new IntSet(stringHashLength, 0.5f),
                colliderLit = new IntSet(stringHashLength, 0.5f),
                colliderWis = new IntSet(stringHashLength, 0.5f),
                colliderSli = new IntSet(stringHashLength, 0.5f),
                colliderWat = new IntSet(stringHashLength, 0.5f),
                colliderJol = new IntSet(stringHashLength, 0.5f),
                colliderHiv = new IntSet(stringHashLength, 0.5f),
                colliderSpl = new IntSet(stringHashLength, 0.5f),
                colliderYur = new IntSet(stringHashLength, 0.5f),
                colliderBuz = new IntSet(stringHashLength, 0.5f);
//        LightRNG rng1 = new LightRNG(DiverRNG.determine(System.nanoTime() * 0x9E3779B97F4A7C15L + 0xC6BC279692B5CC83L));
//        DiverRNG rng2 = new DiverRNG(LightRNG.determine(System.nanoTime() * 0xC6BC279692B5CC83L + 0x9E3779B97F4A7C15L));
//        final int SIZE = 1024;
//        int[][] pairs = new int[SIZE][2];
//        IntSet[] colliders = new IntSet[SIZE];
//        for (int i = 0; i < SIZE; i++) {
//            colliders[i] = new IntSet(stringHashLength, 0.65f);
//            pairs[i][0] = rng1.next(20);
//            pairs[i][1] = rng2.next(20);
//        }
        for(String s : strings)
        {
            colliderJDK.add(s.hashCode() & restrict);
            colliderLit.add(CrossHash.Lightning.hash(s) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(s) & restrict);
            colliderSli.add(slitherHash2(s) & restrict);
            colliderWat.add(CrossHash.hash(s) & restrict);
            colliderJol.add(joltHash(s) & restrict);
            colliderHiv.add(CrossHash.Hive.hash(s) & restrict);
            colliderSpl.add(lantern(s.hashCode()) & restrict);
            colliderYur.add(yuraHash(s, 0xC74EAD55) & restrict);
            colliderBuz.add(buzzHash(s) & restrict);
//            for (int i = 0; i < SIZE; i++) {
//                colliders[i].add(slitherHashConfig(s, pairs[i][0], pairs[i][1]) & restrict);
//            }
//            for (int i = 0; i < SIZE; i++) {
//                colliders[i].add(buzzHashConfig(s, pairs[i][0], pairs[i][1]) & restrict);
//            }
        }
        System.out.println(strings.iterator().next());
        System.out.println("With " + stringHashLength + " distinct Strings:");
        String bits = Integer.bitCount(restrict) + "-bit: ";
        System.out.println("JDK collisions, " + bits + (stringHashLength - colliderJDK.size));
        System.out.println("Lit collisions, " + bits + (stringHashLength - colliderLit.size));
        System.out.println("Wis collisions, " + bits + (stringHashLength - colliderWis.size));
        System.out.println("Sli collisions, " + bits + (stringHashLength - colliderSli.size));
        System.out.println("Wat collisions, " + bits + (stringHashLength - colliderWat.size));
        System.out.println("Jol collisions, " + bits + (stringHashLength - colliderJol.size));
        System.out.println("Lan collisions, " + bits + (stringHashLength - colliderSpl.size));
        System.out.println("Yur collisions, " + bits + (stringHashLength - colliderYur.size));
        System.out.println("Buz collisions, " + bits + (stringHashLength - colliderBuz.size));
        System.out.println("Hiv collisions, " + bits + (stringHashLength - colliderHiv.size));
//        Arrays.sort(colliders, new Comparator<IntSet>() {
//            @Override
//            public int compare(IntSet o1, IntSet o2) {
//                return o2.size - o1.size;
//            }
//        });
//        IntSet idm;
//        int idx;
//        for (int i = 0; i < 10; i++) {
//            idm = colliders[i];
//            idx = (int)idm.get(idm.firstIntKey());
//            System.out.printf("0x%08X, 0x%08X : %d\n", pairs[idx][0], pairs[idx][1], (stringHashLength - idm.size));
//            idm.clear();
//        }
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderWis.clear();
        colliderSli.clear();
        colliderWat.clear();
        colliderJol.clear();
        colliderHiv.clear();
        colliderSpl.clear();
        colliderYur.clear();
        colliderBuz.clear();
    }

    @Test
    @Ignore
    public void testInts()
    {
        int restrict = 0xFFFFF;
        final int LIMIT = restrict * 4 / 5;
        int[][] arrs = new int[LIMIT][34];
        for (int i = 0; i < LIMIT; i++) {
            long d = DiverRNG.randomize(i);
//            arrs[i][(int)(d >>> 60) + 8] = (int) (d);
            Arrays.fill(arrs[i], (int) d);
        }
        IntSet colliderJDK = new IntSet(LIMIT, 0.5f),
                colliderLit = new IntSet(LIMIT, 0.5f),
                colliderWis = new IntSet(LIMIT, 0.5f),
                colliderSli = new IntSet(LIMIT, 0.5f),
                colliderWat = new IntSet(LIMIT, 0.5f),
                colliderMis = new IntSet(LIMIT, 0.5f),
                colliderHiv = new IntSet(LIMIT, 0.5f),
                colliderLan = new IntSet(LIMIT, 0.5f);
//        LightRNG rng1 = new LightRNG(DiverRNG.determine(System.nanoTime() * 0x9E3779B97F4A7C15L + 0xC6BC279692B5CC83L));
//        DiverRNG rng2 = new DiverRNG(LightRNG.determine(System.nanoTime() * 0xC6BC279692B5CC83L + 0x9E3779B97F4A7C15L));
//        final int SIZE = 1024;
//        int[][] pairs = new int[SIZE][2];
//        IntSet[] colliders = new IntSet[SIZE];
//        for (int i = 0; i < SIZE; i++) {
//            colliders[i] = new IntSet(stringHashLength, 0.65f);
//            pairs[i][0] = rng1.next(20);
//            pairs[i][1] = rng2.next(20);
//        }
        for(int[] s : arrs)
        {
//            colliderJDK.add((int) (Arrays.hashCode(s) * 0x9E3779B97F4A7C15L >>> 32) & restrict);
            colliderJDK.add(Arrays.hashCode(s) & restrict);
            colliderLit.add(CrossHash.Lightning.hash(s) & restrict);
            colliderWis.add(CrossHash.Wisp.hash(s) & restrict);
            colliderSli.add(slitherHash(s) & restrict);
            colliderWat.add(CrossHash.hash(s) & restrict);
            colliderMis.add(CrossHash.Mist.alpha.hash(s) & restrict);
            colliderHiv.add(CrossHash.Hive.hash(s) & restrict);
            colliderLan.add(lantern(Arrays.hashCode(s)) & restrict);
//            for (int i = 0; i < SIZE; i++) {
//                colliders[i].add(slitherHashConfig(s, pairs[i][0], pairs[i][1]) & restrict);
//            }
//            for (int i = 0; i < SIZE; i++) {
//                colliders[i].add(buzzHashConfig(s, pairs[i][0], pairs[i][1]) & restrict);
//            }
        }
        System.out.println("With " + LIMIT + " distinct int arrays:");
        String bits = Integer.bitCount(restrict) + "-bit: ";
        System.out.println("JDK collisions, " + bits + (LIMIT - colliderJDK.size));
        System.out.println("Lit collisions, " + bits + (LIMIT - colliderLit.size));
        System.out.println("Wis collisions, " + bits + (LIMIT - colliderWis.size));
        System.out.println("Sli collisions, " + bits + (LIMIT - colliderSli.size));
        System.out.println("Wat collisions, " + bits + (LIMIT - colliderWat.size));
        System.out.println("Mis collisions, " + bits + (LIMIT - colliderMis.size));
        System.out.println("Lan collisions, " + bits + (LIMIT - colliderLan.size));
        System.out.println("Hiv collisions, " + bits + (LIMIT - colliderHiv.size));
//        Arrays.sort(colliders, new Comparator<IntSet>() {
//            @Override
//            public int compare(IntSet o1, IntSet o2) {
//                return o2.size - o1.size;
//            }
//        });
//        IntSet idm;
//        int idx;
//        for (int i = 0; i < 10; i++) {
//            idm = colliders[i];
//            idx = (int)idm.get(idm.firstIntKey());
//            System.out.printf("0x%08X, 0x%08X : %d\n", pairs[idx][0], pairs[idx][1], (stringHashLength - idm.size));
//            idm.clear();
//        }
        System.out.println();
        colliderJDK.clear();
        colliderLit.clear();
        colliderWis.clear();
        colliderSli.clear();
        colliderWat.clear();
        colliderMis.clear();
        colliderHiv.clear();
        colliderLan.clear();
    }
    /*
    0x2EF022689E573495L, 0x8628F680AFADEDABL : 629
    0xCFEB847AE4B6AD26, 0xE7FFD14DDB14DD2D : 652
    0xA8BC4AE2C9542B80, 0xC8D3A0DE9FE21288 : 653
    0x7DC2031CCF49AC00, 0x20BF5813242226B7 : 654
    
    0x7B59E7FC789BA792L, a = 0xA0729045E286D65FL;// 959
    0xABCB254A1454AE87, 0x6762DC42F20490D1 : 963
    0xED55E99E89BAC9AA, 0x2AD7C0BD32AA8502 : 966
    0x63B36541EDEA1AE2, 0x37B32F846A5CB867 : 971
    
    0x1A976FDF6BF60B8E, a = 0x60642E2A34326F15L;// 253
    
    with 32-bit:
    0x000A539B, 0x000E47AF : 6786
    0x0007A702, 0x00023F9F : 6786
    0x000284FD, 0x0008F853 : 6787
    0x00056F55, 0x00021219 : 6789
     */

    @Test
    @Ignore
    public void testMix() {
        final int[] params = new int[]{64, 128, 256, 512, 1024}, increases = {0,1,2};
        long baseTotal = 0, hacoTotal = 0, lantTotal = 0, xlxsTotal = 0, total = 0;
//        long[] confTotals = new long[31];
        NLFSR gen = new NLFSR(1234567);
        int mul;
        for (int r = 1; r < 32; r++) {

            for (int m = 1; m < 0x1000; m++) {
                mul = gen.nextInt() << 1 | 1;
                for (int INCREASE : increases) {
                    for (int SIZE : params) {
                        int restrict = (SIZE << INCREASE) - 1;

                        IntSet colliderBase = new IntSet(SIZE, 0.5f),
                                colliderHaCo = new IntSet(SIZE, 0.5f),
                                colliderLant = new IntSet(SIZE, 0.5f),
                                colliderXLXS = new IntSet(SIZE, 0.5f);
//                    IntSet[] colliders = new IntSet[31];
//                    for (int i = 0; i < 31; i++) {
//                        colliders[i] = new IntSet(SIZE, 0.5f);
//                    }
                        for (int y = 1; y <= SIZE; y++) {
//                        int x = (ThrustAltRNG.determineInt(y));
//                        int x = xs3(y*0x1FFFFFFF);
                            int x = Integer.rotateLeft(y * mul, r);
//                        x = x << 16 | x >>> 16;
                            colliderBase.add(x & restrict);
                            colliderLant.add(xs3(x) & restrict);
                            colliderXLXS.add(xlxs(x) & restrict);
                            colliderHaCo.add(HashCommon.mix(x) & restrict);
//                            for (int i = 0; i < 31; i++) {
//                                colliders[i].add(latheCoordConfig(x, y, i + 1) & restrict);
//                            }
                        }
//                    System.out.println("INCREASE: " + INCREASE + ", SIZE: " + SIZE);
//                    System.out.println("Base collisions: " + (SIZE - colliderBase.size));
//                    System.out.println("Lant collisions: " + (SIZE - colliderLant.size));
//                    System.out.println("XLXS collisions: " + (SIZE - colliderXLXS.size));
//                    System.out.println("HaCo collisions: " + (SIZE - colliderHaCo.size));
//                    for (int i = 0; i < 31; i++) {
//                        System.out.println("Lathe " + (i + 1) + ": " + (SIZE - colliders[i].size));
//                        confTotals[i] += (SIZE - colliders[i].size);
//                    }
                        baseTotal += (SIZE - colliderBase.size);
                        lantTotal += (SIZE - colliderLant.size);
                        xlxsTotal += (SIZE - colliderXLXS.size);
                        hacoTotal += (SIZE - colliderHaCo.size);
                        total += SIZE;
                    }
                }
            }
        }
        System.out.println("Number of ints added: " + total);
        System.out.println("TOTAL Base collisions: " + baseTotal + " (" + (baseTotal * 100.0 / total) + "%)");
        System.out.println("TOTAL Lant collisions: " + lantTotal + " (" + (lantTotal * 100.0 / total) + "%)");
        System.out.println("TOTAL XLXS collisions: " + xlxsTotal + " (" + (xlxsTotal * 100.0 / total) + "%)");
        System.out.println("TOTAL HaCo collisions: " + hacoTotal + " (" + (hacoTotal * 100.0 / total) + "%)");
//        for (int i = 0; i < 31; i++) {
//            System.out.println("TOTAL Lath_"+(i+1)+" collisions: " + confTotals[i] + " (" + (confTotals[i] * 100.0 / total) + "%)");
//        }
    }

    @Test
    @Ignore
    public void testSimpleHashCodes()
    {
        int hamCollisions = 0, fibCollisions = 0, mask = 0x7FFFFF;
        IntSet ham = new IntSet(mask, 0.75f), fib = new IntSet(mask, 0.75f);
        for (int x = -1000; x <= 1000; x++) {
            for (int y = -1000; y <= 1000; y++) {
                int basic = x << 16 ^ y;
                int h = (basic ^ basic >>> 16) & mask;
                int f = (int) (basic * 0x9E3779B97F4A7C15L >>> 41);
                if(!ham.add(h)) hamCollisions++;
                if(!fib.add(f)) fibCollisions++;
            }
        }
        System.out.println("HashMap Collisons:   " + hamCollisions);
        System.out.println("Fibonacci Collisons: " + fibCollisions);
    }
}
