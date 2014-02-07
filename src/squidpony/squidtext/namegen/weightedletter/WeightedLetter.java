package squidpony.squidtext.namegen.weightedletter;

/**
 * Contains the information for how the letters are distributed.
 *
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class WeightedLetter {

    public String letter;
    public WeightedLetterGroup nextLetters = new WeightedLetterGroup();

    public void WeightedLetter(String letter) {
        this.letter = letter;
    }

    /**
     * Have the WeightedLetterGroup keep track of the next letter instead of a
     * simple array. A simple array will do the trick (removing the need for
     * WightedLetterGroup or WeightedLetterCounter), if you use a different
     * algorithm for best-fitting the penultimate letter (see
     * WeightedLetterNamegen.getIntermediateLetter()).
     *
     * @param	nextLetter
     */
    public void addNextLetter(String nextLetter) {
        nextLetters.add(nextLetter);
    }
}
