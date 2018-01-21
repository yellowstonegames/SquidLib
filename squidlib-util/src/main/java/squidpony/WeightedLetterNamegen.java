package squidpony;

import regexodus.Category;
import squidpony.annotation.Beta;
import squidpony.squidmath.ProbabilityTable;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class WeightedLetterNamegen {
//<editor-fold defaultstate="collapsed" desc="Viking Style static name list">

    public static final String[] VIKING_STYLE_NAMES = new String[]{
            "Andor",
            "Baatar",
            "Beowulf",
            "Drogo",
            "Freya",
            "Grog",
            "Gruumsh",
            "Grunt",
            "Hodor",
            "Hrothgar",
            "Hrun",
            "Korg",
            "Lothar",
            "Odin",
            "Theodrin",
            "Thor",
            "Yngvar",
            "Xandor"
    };
    //</editor-fold>
//<editor-fold defaultstate="collapsed" desc="Star Wars Style static name list">
    public static final String[] STAR_WARS_STYLE_NAMES = new String[]{
            "Lutoif Vap",
            "Nasoi Seert",
            "Jitpai",
            "Sose",
            "Vainau",
            "Jairkau",
            "Tirka Kist",
            "Boush",
            "Wofe",
            "Voxin Voges",
            "Koux Boiti",
            "Loim",
            "Gaungu",
            "Mut Tep",
            "Foimo Saispi",
            "Toneeg Vaiba",
            "Nix Nast",
            "Gup Dangisp",
            "Distark Toonausp",
            "Tex Brinki",
            "Kat Tosha",
            "Tauna Foip",
            "Frip Cex",
            "Fexa Lun",
            "Tafa",
            "Zeesheerk",
            "Cremoim Kixoop",
            "Tago",
            "Kesha Diplo"
    };
    //</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA male names static name list">
    public static final String[] COMMON_USA_MALE_NAMES = new String[]{
            "James",
            "John",
            "Robert",
            "Michael",
            "William",
            "David",
            "Richard",
            "Charles",
            "Joseph",
            "Tomas",
            "Christopher",
            "Daniel",
            "Paul",
            "Mark",
            "Donald",
            "George",
            "Kenneth",
            "Steven",
            "Edward",
            "Brian",
            "Ronald",
            "Anthony",
            "Kevin",
            "Jason",
            "Matthew",
            "Gary",
            "Timothy",
            "Jose",
            "Larry",
            "Jeffrey",
            "Frank",
            "Scott",
            "Eric",
            "Stephen",
            "Andrew",
            "Raymond",
            "Gregory",
            "Joshua",
            "Jerry",
            "Dennis",
            "Walter",
            "Patrick",
            "Peter",
            "Harold",
            "Douglas",
            "Henry",
            "Carl",
            "Arthur",
            "Ryan",
            "Roger"
    };
    //</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA female names static name list">
    public static final String[] COMMON_USA_FEMALE_NAMES = new String[]{
            "Mary",
            "Patricia",
            "Linda",
            "Barbara",
            "Elizabeth",
            "Jennifer",
            "Maria",
            "Susan",
            "Margaret",
            "Dorothy",
            "Lisa",
            "Nancy",
            "Karen",
            "Betty",
            "Helen",
            "Sandra",
            "Donna",
            "Carol",
            "Ruth",
            "Sharon",
            "Michelle",
            "Laura",
            "Sarah",
            "Kimberly",
            "Deborah",
            "Jessica",
            "Shirley",
            "Cynthia",
            "Angela",
            "Melissa",
            "Brenda",
            "Amy",
            "Anna",
            "Crystal",
            "Virginia",
            "Kathleen",
            "Pamela",
            "Martha",
            "Becky",
            "Amanda",
            "Stephanie",
            "Carolyn",
            "Christine",
            "Marie",
            "Janet",
            "Catherine",
            "Frances",
            "Ann",
            "Joyce",
            "Diane",
            "Jane",
            "Shauna",
            "Trisha",
            "Eileen",
            "Danielle",
            "Jacquelyn",
            "Lynn",
            "Hannah",
            "Brittany"
    };
    //</editor-fold>
//<editor-fold defaultstate="collapsed" desc="USA last names static name list">
    public static final String[] COMMON_USA_LAST_NAMES = new String[]{
            "Smith",
            "Johnson",
            "Williams",
            "Brown",
            "Jones",
            "Miller",
            "Davis",
            "Wilson",
            "Anderson",
            "Taylor",
            "Thomas",
            "Moore",
            "Martin",
            "Jackson",
            "Thompson",
            "White",
            "Clark",
            "Lewis",
            "Robinson",
            "Walker",
            "Willis",
            "Carter",
            "King",
            "Lee",
            "Grant",
            "Howard",
            "Morris",
            "Bartlett",
            "Paine",
            "Wayne",
            "Lorraine"
    };
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Lovecraft Mythos style static name list">
    public static final String[] LOVECRAFT_MYTHOS_NAMES = new String[]{
            "Koth",
            "Ghlatelilt",
            "Siarlut",
            "Nyogongogg",
            "Nyialan",
            "Nyithiark",
            "Lyun",
            "Kethoshigr",
            "Shobik",
            "Tekogr",
            "Hru-yn",
            "Lya-ehibos",
            "Hruna-oma-ult",
            "Shabo'en",
            "Shrashangal",
            "Shukhaniark",
            "Thaghum",
            "Shrilang",
            "Lukhungu'ith",
            "Nyun",
            "Nyia-ongin",
            "Shogia-usun",
            "Lyu-yl",
            "Liathiagragr",
            "Lyathagg",
            "Hri'osurkut",
            "Shothegh",
            "No-orleshigh",
            "Zvriangekh",
            "Nyesashiv",
            "Lyarkio",
            "Le'akh",
            "Liashi-en",
            "Shurkano'um",
            "Hrakhanoth",
            "Ghlotsuban",
            "Cthitughias",
            "Ftanugh"
    };
//</editor-fold>

    private static final char[] vowels = {'a', 'e', 'i', 'o'};//not using y because it looks strange as a vowel in names
    private static final int LAST_LETTER_CANDIDATES_MAX = 52;

    private RNG rng;
    private String[] names;
    private int consonantLimit;
    private ArrayList<Integer> sizes;

    private HashMap<Character, HashMap<Character, ProbabilityTable<Character>>> letters;
    private ArrayList<Character> firstLetterSamples;
    private ArrayList<Character> lastLetterSamples;
    private DamerauLevenshteinAlgorithm dla = new DamerauLevenshteinAlgorithm(1, 1, 1, 1);

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names an array of Strings that are typical names to be emulated
     */
    public WeightedLetterNamegen(String[] names) {
        this(names, 2);
    }

    /**
     * Creates the generator by seeding the provided list of names.
     *
     * @param names          an array of Strings that are typical names to be emulated
     * @param consonantLimit the maximum allowed consonants in a row
     */
    public WeightedLetterNamegen(String[] names, int consonantLimit) {
        this(names, consonantLimit, new RNG());
    }

    /**
     * Creates the generator by seeding the provided list of names. The given RNG will be used for
     * all random decisions this has to make, so if it has the same state (and RandomnessSource) on
     * different runs through the program, it will produce the same names reliably.
     *
     * @param names          an array of Strings that are typical names to be emulated
     * @param consonantLimit the maximum allowed consonants in a row
     * @param rng            the source of randomness to be used
     */
    public WeightedLetterNamegen(String[] names, int consonantLimit, RNG rng) {
        this.names = names;
        this.consonantLimit = consonantLimit;
        this.rng = rng;
        init();
    }

    /**
     * Initialization, statistically measures letter likelihood.
     */
    private void init() {
        sizes = new ArrayList<>();
        letters = new HashMap<>();
        firstLetterSamples = new ArrayList<>();
        lastLetterSamples = new ArrayList<>();

        for (int i = 0; i < names.length - 1; i++) {
            String name = names[i];
            if (name == null || name.length() < 1) {
                continue;
            }

            // (1) Insert size
            sizes.add(name.length());

            // (2) Grab first letter
            firstLetterSamples.add(name.charAt(0));

            // (3) Grab last letter
            lastLetterSamples.add(name.charAt(name.length() - 1));

            // (4) Process all letters
            for (int n = 0; n < name.length() - 1; n++) {
                char letter = name.charAt(n);
                char nextLetter = name.charAt(n + 1);

                // Create letter if it doesn't exist
                HashMap<Character, ProbabilityTable<Character>> wl = letters.get(letter);
                if (wl == null) {
                    wl = new HashMap<>();
                    letters.put(letter, wl);
                }
                ProbabilityTable<Character> wlg = wl.get(letter);
                if (wlg == null) {
                    wlg = new ProbabilityTable<>(rng);
                    wl.put(letter, wlg);
                }
                wlg.add(nextLetter, 1);

                // If letter was uppercase (beginning of name), also add a lowercase entry
                if (Category.Lu.contains(letter)) {
                    letter = Character.toLowerCase(letter);

                    wlg = wl.get(letter);
                    if (wlg == null) {
                        wlg = new ProbabilityTable<>(rng);
                        wl.put(letter, wlg);
                    }
                    wlg.add(nextLetter, 1);
                }
            }
        }
    }

    private StringBuilder generateInner(StringBuilder name) {
        for (int runs = 0; runs < LAST_LETTER_CANDIDATES_MAX; runs++) {
            name.setLength(0);
            // Pick size
            int size = rng.getRandomElement(sizes);

            // Pick first letter
            char latest = rng.getRandomElement(firstLetterSamples);
            name.append(latest);

            for (int i = 1; i < size - 2; i++) {
                name.append(latest = getRandomNextLetter(latest));
            }

            // Attempt to find a last letter
            for (int lastLetterFits = 0; lastLetterFits < LAST_LETTER_CANDIDATES_MAX; lastLetterFits++) {
                char lastLetter = rng.getRandomElement(lastLetterSamples);
                char intermediateLetterCandidate = getIntermediateLetter(latest, lastLetter);

                // Only attach last letter if the candidate is valid (if no candidate, the antepenultimate letter always occurs at the end)
                if (Category.L.contains(intermediateLetterCandidate)) {
                    name.append(intermediateLetterCandidate).append(lastLetter);
                    break;
                }
            }

            // Check that the word has no triple letter sequences, and that the Levenshtein distance is kosher
            if (validateGrouping(name) && checkLevenshtein(name)) {
                return name;
            }
        }
        name.setLength(0);
        return name.append(rng.getRandomElement(names));
    }
    /**
     * Gets one random String name.
     *
     * @return a single random String name
     */

    public String generate() {
        return generateInner(new StringBuilder(32)).toString();
    }

    /**
     * Gets an ArrayList of random String names, sized to match amountToGenerate.
     * @param amountToGenerate how many String items to include in the returned ArrayList
     * @return an ArrayList of random String names
     */
    public ArrayList<String> generateList(int amountToGenerate) {
        ArrayList<String> result = new ArrayList<>();

        StringBuilder name = new StringBuilder(32);
        for (int i = 0; i < amountToGenerate; i++) {
            result.add(generateInner(name).toString());
        }

        return result;
    }
    /**
     * Gets an array of random String names, sized to match amountToGenerate.
     *
     * @param amountToGenerate how many String items to include in the returned array
     * @return an array of random String names
     */

    public String[] generate(int amountToGenerate)
    {
        return generateList(amountToGenerate).toArray(new String[0]);
    }

    /**
     * Searches for the best fit letter between the letter before and the letter
     * after (non-random). Used to determine penultimate letters in names.
     *
     * @param	letterBefore	The letter before the desired letter.
     * @param	letterAfter	The letter after the desired letter.
     * @return	The best fit letter between the provided letters.
     */
    private char getIntermediateLetter(char letterBefore, char letterAfter) {
        if (Category.L.contains(letterBefore) && Category.L.contains(letterAfter)) {
            // First grab all letters that come after the 'letterBefore'
            HashMap<Character, ProbabilityTable<Character>> wl = letters.get(letterBefore);
            if (wl == null) {
                return getRandomNextLetter(letterBefore);
            }
            Set<Character> letterCandidates = wl.get(letterBefore).items();

            char bestFitLetter = '\'';
            int bestFitScore = 0;

            // Step through candidates, and return best scoring letter
            for (char letter : letterCandidates) {
                wl = letters.get(letter);
                if (wl == null) {
                    continue;
                }
                ProbabilityTable<Character> weightedLetterGroup = wl.get(letterBefore);
                if (weightedLetterGroup != null) {
                    int letterCounter = weightedLetterGroup.weight(letterAfter);
                    if (letterCounter > bestFitScore) {
                        bestFitLetter = letter;
                        bestFitScore = letterCounter;
                    }
                }
            }

            return bestFitLetter;
        } else {
            return '-';
        }
    }

    /**
     * Checks that no three letters happen in succession.
     *
     * @param	name	The name CharSequence
     * @return	True if no triple letter sequence is found.
     */
    private boolean validateGrouping(CharSequence name) {
        for (int i = 2; i < name.length(); i++) {
            if (name.charAt(i) == name.charAt(i - 1) && name.charAt(i) == name.charAt(i - 2)) {
                return false;
            }
        }
        int consonants = 0;
        for (int i = 0; i < name.length(); i++) {
            if (isVowel(name.charAt(i))) {
                consonants = 0;
            } else {
                if (++consonants > consonantLimit) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isVowel(char c) {
        switch(c)
        {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks that the Damerau-Levenshtein distance of this name is within a
     * given bias from a name on the master list.
     *
     * @param	name	The name string.
     * @return	True if a name is found that is within the bias.
     */
    private boolean checkLevenshtein(CharSequence name) {
        int levenshteinBias = name.length() / 2;

        for (String name1 : names) {
            int levenshteinDistance = dla.execute(name, name1);
            if (levenshteinDistance <= levenshteinBias) {
                return true;
            }
        }

        return false;
    }

    private char getRandomNextLetter(char letter) {
        if (letters.containsKey(letter)) {
            return letters.get(letter).get(letter).random();
        } else {
            return vowels[rng.nextIntHasty(4)];
        }
    }
}
