package squidpony;

import squidpony.squidmath.RNG;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A text generator for producing sentences and/or words in nonsense languages that fit a theme.
 * Created by Tommy Ettinger on 11/29/2015.
 */
public class FakeLanguageGen {
    protected final String[] vowels, openingConsonants, midConsonants, closingConsonants, vowelSplitters;
    protected final LinkedHashMap<Integer, Double> syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    protected final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency;

    public static final FakeLanguageGen LOVECRAFT = new FakeLanguageGen(
            new String[]{"a","i","o","e","u","a","i","o","e","u","ia","ai","ei"},
            new String[]{"s","t","k","n","y","p","k","l","g","gl","th","sh","ny","ft","hm","zvr","cth"},
            new String[]{"h","gl","gr","nd","mr","vr","kr"},
            new String[]{"l","p","s","t","n","k","g","x","rl","th","gg","gh","ts","lt","rk","kh","sh","ng","shk"},
            new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2}, 0.4, 0.31, 0.07);
    public FakeLanguageGen(String[] vowels, String[] openingConsonants, String[] midConsonants, String[] closingConsonants,
                           String[] vowelSplitters, int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                           double vowelEndFrequency, double vowelSplitFrequency) {
        this.vowels = vowels;
        this.openingConsonants = openingConsonants;
        this.midConsonants = new String[midConsonants.length + closingConsonants.length];
        System.arraycopy(midConsonants, 0, this.midConsonants, 0, midConsonants.length);
        System.arraycopy(closingConsonants, 0, this.midConsonants, midConsonants.length, closingConsonants.length);
        this.closingConsonants = closingConsonants;
        this.vowelSplitters = vowelSplitters;
        this.syllableFrequencies = new LinkedHashMap<Integer, Double>(syllableLengths.length);
        for (int i = 0; i < syllableLengths.length && i < syllableFrequencies.length; i++) {
            this.syllableFrequencies.put(syllableLengths[i], syllableFrequencies[i]);
        }
        for (Double freq : this.syllableFrequencies.values()) {
            totalSyllableFrequency += freq;
        }
        if (vowelStartFrequency > 1.0)
            this.vowelStartFrequency = 1.0 / vowelStartFrequency;
        else
            this.vowelStartFrequency = vowelStartFrequency;
        if (vowelEndFrequency > 1.0)
            this.vowelEndFrequency = 1.0 / vowelEndFrequency;
        else
            this.vowelEndFrequency = vowelEndFrequency;
        if (vowelSplitFrequency > 1.0)
            this.vowelSplitFrequency = 1.0 / vowelSplitFrequency;
        else
            this.vowelSplitFrequency = vowelSplitFrequency;
    }

    public String word(RNG rng, boolean capitalize) {
        StringBuilder sb = new StringBuilder(20);
        if (rng.nextDouble() < vowelStartFrequency) {
            sb.append(rng.getRandomElement(vowels));
        }
        sb.append(rng.getRandomElement(openingConsonants));

        double syllableChance = rng.nextDouble(totalSyllableFrequency);
        int syllables = 1;
        for (Map.Entry<Integer, Double> kv : syllableFrequencies.entrySet()) {
            if (syllableChance < kv.getValue()) {
                syllables = kv.getKey();
                break;
            } else
                syllableChance -= kv.getValue();
        }
        for (int i = 0; i < syllables - 1; i++) {
            sb.append(rng.getRandomElement(vowels));
            if (rng.nextDouble() < vowelSplitFrequency) {
                sb.append(rng.getRandomElement(vowelSplitters));
                sb.append(rng.getRandomElement(vowels));
            }
            sb.append(rng.getRandomElement(midConsonants));
        }
        sb.append(rng.getRandomElement(vowels));
        if (rng.nextDouble() < vowelSplitFrequency) {
            sb.append(rng.getRandomElement(vowelSplitters));
            sb.append(rng.getRandomElement(vowels));
        }
        if (rng.nextDouble() >= vowelEndFrequency)
            sb.append(rng.getRandomElement(closingConsonants));

        if (capitalize)
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public String sentence(RNG rng, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency) {
        if (minWords < 1)
            minWords = 1;
        if (minWords > maxWords)
            maxWords = minWords;
        if (midPunctuationFrequency > 1.0) {
            midPunctuationFrequency = 1.0 / midPunctuationFrequency;
        }
        StringBuilder sb = new StringBuilder(12 * maxWords);
        sb.append(word(rng, true));
        for (int i = 1; i < minWords; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(rng, false));
        }
        for (int i = minWords; i < maxWords && rng.nextInt(2 * maxWords) > i; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(rng, false));
        }
        sb.append(rng.getRandomElement(endPunctuation));
        return sb.toString();
    }

}


    /*

(defn nested-str [& coll] (apply str (flatten coll)))
(defn rand-str [num-blanks pieces] (nested-str (rand-nth (concat
				(repeat num-blanks "")
				pieces))))
(def boundaries ["!"".""..."])
(def vowels ["a""i""o""e""u""a""i""o""e""u""ia""ai""ei"])
(def possible-syllables [1 2 1 2 1 2 3])
(def opening-consonants ["S""T""K""N""Y""P""K""L""G""Gl""Th""Sh""Ny""Ft""Hm""Zvr""Cth"])
(def opening-vowels (for [v (map st/capitalize vowels) c (map st/lower-case opening-consonants)] (str v c)))
(def closing-consonants ["l""p""s""t""n""k""g""x""rl""th""gg""gh""ts""lt""rk""kh""sh""ng""shk"])
(def mid-consonants (concat closing-consonants ["h""gl""gr""nd""mr""vr""kr"]))

(defn vowel [] (rand-str 0 vowels))
(defn boundary [] (rand-str 6 boundaries))

(defn word []
	[(rand-str 0 (concat (apply concat (repeat (* 5/3 (count vowels)) opening-consonants)) opening-vowels))
	 (repeatedly (dec (rand-nth possible-syllables))
	     #(do[(vowel)
	    	  (rand-str 12 [(str \' (vowel)) (str \- (vowel))])
	    	  (rand-str 0 mid-consonants)]))
	 [(vowel)
	  (rand-str 9 [(str \' (vowel)) (str \- (vowel))])
          (rand-str 10 closing-consonants)]])
(defn phrase [] (let [gap (boundary) w (word)] [gap
		                       (if (seq gap)
		                       	       [" " w]
		                       	       [(rand-str 4 ",")" "(st/lower-case (nested-str w))])]))
     */

