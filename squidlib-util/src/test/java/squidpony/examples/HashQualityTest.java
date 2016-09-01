package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.LongPeriodRNG;
import squidpony.squidmath.StatefulRNG;

import java.util.Arrays;

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
public class HashQualityTest {

    public static void main(String[] args)
    {
        CrossHash.Sip sip = new CrossHash.Sip(0x9E3779B97F4A7C15L, 0xBF58476D1CE4E5B9L);
        CrossHash.Storm storm = CrossHash.Storm.chi;
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
        };

        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("JDK bytes: " + Arrays.hashCode(bytes[i]));
            System.out.println("JDK shorts: " + Arrays.hashCode(shorts[i]));
            System.out.println("JDK ints: " + Arrays.hashCode(ints[i]));
            System.out.println("JDK longs: " + Arrays.hashCode(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("CrossHash bytes: " + CrossHash.hash(bytes[i]));
            System.out.println("CrossHash shorts: " + CrossHash.hash(shorts[i]));
            System.out.println("CrossHash ints: " + CrossHash.hash(ints[i]));
            System.out.println("CrossHash longs: " + CrossHash.hash(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Sip bytes: " + sip.hash(bytes[i]));
            System.out.println("Sip shorts: " + sip.hash(shorts[i]));
            System.out.println("Sip ints: " + sip.hash(ints[i]));
            System.out.println("Sip longs: " + sip.hash(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Lightning bytes: " + CrossHash.Lightning.hash(bytes[i]));
            System.out.println("Lightning shorts: " + CrossHash.Lightning.hash(shorts[i]));
            System.out.println("Lightning ints: " + CrossHash.Lightning.hash(ints[i]));
            System.out.println("Lightning longs: " + CrossHash.Lightning.hash(longs[i]));
        }

        for (int i = 0; i < len; i++) {
            System.out.println(Arrays.toString(longs[i]));
            System.out.println("Storm bytes: " + storm.hash(bytes[i]));
            System.out.println("Storm shorts: " + storm.hash(shorts[i]));
            System.out.println("Storm ints: " + storm.hash(ints[i]));
            System.out.println("Storm longs: " + storm.hash(longs[i]));
        }

        int longHashLength = 0x100000, stringHashLength = 0xC0000;
        System.out.println("Long Hashing:");
        System.out.println("---------------------------------");
        IntDoubleOrderedMap colliderJDK = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderFNV = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderSip = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderLit = new IntDoubleOrderedMap(longHashLength, 0.75f),
                colliderSto = new IntDoubleOrderedMap(longHashLength, 0.75f);
        LongPeriodRNG lprng = new LongPeriodRNG();

        for (int bits = 16, mask = 0xFFFF; bits < 33; mask |= 1 << bits++) {
            lprng.reseed(0x66L);
            for (int i = 0; i < longHashLength; i++) {
                lprng.nextLong();
                colliderJDK.put(Arrays.hashCode(lprng.state) & mask, i);
                colliderFNV.put(CrossHash.hash(lprng.state) & mask, i);
                colliderSip.put(sip.hash(lprng.state) & mask, i);
                colliderLit.put(CrossHash.Lightning.hash(lprng.state) & mask, i);
                colliderSto.put(storm.hash(lprng.state) & mask, i);
            }
            System.out.println("JDK collisions, " + bits + "-bit: " + (longHashLength - colliderJDK.size()));
            System.out.println("FNV collisions, " + bits + "-bit: " + (longHashLength - colliderFNV.size()));
            System.out.println("Sip collisions, " + bits + "-bit: " + (longHashLength - colliderSip.size()));
            System.out.println("Lit collisions, " + bits + "-bit: " + (longHashLength - colliderLit.size()));
            System.out.println("Sto collisions, " + bits + "-bit: " + (longHashLength - colliderSto.size()));
            System.out.println();
            colliderJDK.clear();
            colliderFNV.clear();
            colliderSip.clear();
            colliderLit.clear();
            colliderSto.clear();

        }

        System.out.println("\nString Hashing:");
        System.out.println("---------------------------------");
        StatefulRNG srng = new StatefulRNG(0x1337CAFE);
        String input;
        FakeLanguageGen oddLang;
        String[] midPunct = {",", ";", " -"}, endPunct = {"..."};
        char[] massive;
        int langLength;
        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.ARABIC_ROMANIZED.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_ARABIC);
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderFNV.put(CrossHash.hash(massive, s, s+e+32), i);
            colliderSip.put(sip.hash(massive, s, s+e+32), i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32), i);
            colliderSto.put(storm.hash(massive, s, s+e+32), i);
            if(e >= 0x1fd)
                s += 7;
        }
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();
        colliderSto.clear();

        srng.setState(0x1337CAFE);
        oddLang = FakeLanguageGen.JAPANESE_ROMANIZED;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderFNV.put(CrossHash.hash(massive, s, s+e+32), i);
            colliderSip.put(sip.hash(massive, s, s+e+32), i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32), i);
            colliderSto.put(storm.hash(massive, s, s+e+32), i);
            if(e >= 0x1fd)
                s += 7;
        }
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();
        colliderSto.clear();


        oddLang = FakeLanguageGen.FANCY_FANTASY_NAME.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.67).mix(FakeLanguageGen.RUSSIAN_AUTHENTIC, 0.45);
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderFNV.put(CrossHash.hash(massive, s, s+e+32), i);
            colliderSip.put(sip.hash(massive, s, s+e+32), i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32), i);
            colliderSto.put(storm.hash(massive, s, s+e+32), i);
            if(e >= 0x1fd)
                s += 7;
        }
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();
        colliderSto.clear();


        oddLang = FakeLanguageGen.ENGLISH;
        massive = oddLang.sentence(srng, 0x50000,0x50100, midPunct, endPunct, 0.3).toCharArray();
        langLength = massive.length;
        for (int i = 0, s = 0, e = 0; i < stringHashLength && s + 290 < langLength; i++, e = ((e+3) & 0x1ff)) {
            colliderFNV.put(CrossHash.hash(massive, s, s+e+32), i);
            colliderSip.put(sip.hash(massive, s, s+e+32), i);
            colliderLit.put(CrossHash.Lightning.hash(massive, s, s+e+32), i);
            colliderSto.put(storm.hash(massive, s, s+e+32), i);
            if(e >= 0x1fd)
                s += 7;
        }
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println("Sto collisions, 32-bit: " + (stringHashLength - colliderSto.size()));
        System.out.println();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();
        colliderSto.clear();


        /*
        for (int i = 0; i < stringHashLength; i++) {
            srng.setState(i);
            input = oddLang.word(srng, true, 7);
            colliderJDK.put(input.hashCode(), i);
            colliderFNV.put(CrossHash.hash(input), i);
            colliderSip.put(sip.hash(input), i);
            colliderLit.put(CrossHash.Lightning.hash(input), i);
            if((i & 0xffff) == 0)
                System.out.println(input);
        }
        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println();
        colliderJDK.clear();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();

        oddLang = FakeLanguageGen.JAPANESE_ROMANIZED;
        for (int i = 0; i < stringHashLength; i++) {
            srng.setState(i);
            input = oddLang.word(srng, true, 7);
            colliderJDK.put(input.hashCode(), i);
            colliderFNV.put(CrossHash.hash(input), i);
            colliderSip.put(sip.hash(input), i);
            colliderLit.put(CrossHash.Lightning.hash(input), i);
            if((i & 0xffff) == 0)
                System.out.println(input);
        }
        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println();
        colliderJDK.clear();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();

        oddLang = FakeLanguageGen.FANCY_FANTASY_NAME.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.67).mix(FakeLanguageGen.RUSSIAN_AUTHENTIC, 0.45);
        for (int i = 0; i < stringHashLength; i++) {
            srng.setState(i);
            input = oddLang.word(srng, true, 7);
            colliderJDK.put(input.hashCode(), i);
            colliderFNV.put(CrossHash.hash(input), i);
            colliderSip.put(sip.hash(input), i);
            colliderLit.put(CrossHash.Lightning.hash(input), i);
            if((i & 0xffff) == 0)
                System.out.println(input);
        }
        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println();
        colliderJDK.clear();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();

        oddLang = FakeLanguageGen.ENGLISH;
        for (int i = 0; i < stringHashLength; i++) {
            srng.setState(i);
            input = oddLang.word(srng, true, 7);
            colliderJDK.put(input.hashCode(), i);
            colliderFNV.put(CrossHash.hash(input), i);
            colliderSip.put(sip.hash(input), i);
            colliderLit.put(CrossHash.Lightning.hash(input), i);
            if((i & 0xffff) == 0)
                System.out.println(input);
        }
        System.out.println("JDK collisions, 32-bit: " + (stringHashLength - colliderJDK.size()));
        System.out.println("FNV collisions, 32-bit: " + (stringHashLength - colliderFNV.size()));
        System.out.println("Sip collisions, 32-bit: " + (stringHashLength - colliderSip.size()));
        System.out.println("Lit collisions, 32-bit: " + (stringHashLength - colliderLit.size()));
        System.out.println();
        colliderJDK.clear();
        colliderFNV.clear();
        colliderSip.clear();
        colliderLit.clear();
        */
    }
}
