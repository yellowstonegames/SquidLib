package squidpony;

import squidpony.squidmath.GapShuffler;
import squidpony.squidmath.StatefulRNG;

/**
 * Combines {@link Messaging} with {@link Thesaurus} and optionally {@link NaturalLanguageCipher} to make variations on
 * a sentence structure.
 * <br>
 * Created by Tommy Ettinger on 11/20/2017.
 */
public class ProceduralMessaging {
    /**
     * Data class that stores a name String and one or more Strings that may be used as part of a title with that name,
     * typically using categories from {@link Thesaurus} to add variety.
     */
    public static class AssociatedName {
        public String name;
        public Messaging.NounTrait pronoun;
        public GapShuffler<String> themes, titles;
        public StatefulRNG srng;

        /**
         * Creates an AssociatedName with the being's name as a String and any associated themes and titles as String
         * arrays, with a boolean after the name that determines whether the name should be "translated" using a
         * NaturalLanguageCipher to some other form. If you gave this:
         * <code>"Brunhilda", true, new String[]{"ice`noun`"}, "Goddess`noun` of Ice`nouns`", "Winter-Empress`noun`", "Heroine`noun` of the North"</code>,
         * it could use any of the terms in {@link Thesaurus} associated with the category {@code "ice`noun`} as themes,
         * could generate titles like "Mother of Blizzards", "Winter-Queen", and "Maiden of the North", and would not
         * actually show the name "Brunhilda" in use, instead producing some similar-length name using the
         * NaturalLanguageCipher that a ProceduralMessaging is created with (defaulting to generic fantasy names).
         * This overload always treats the being as if it is being addressed directly, in second-person singular form.
         * @param name the String name for the being, which will be changed if {@code cipherName} is true
         * @param cipherName if true, the name will be changed using a NaturalLanguageCipher before being shown
         * @param themes a String array (which may be null) of words that may appear more often regarding this being
         * @param titles a String array or vararg (which should probably not be null) of special titles for the being
         */
        public AssociatedName(String name, boolean cipherName, String[] themes, String... titles)
        {
            this.name = cipherName ? "[?]" + name + "[?]" : name;
            this.pronoun = Messaging.NounTrait.SECOND_PERSON_SINGULAR;
            srng = new StatefulRNG(name);
            this.themes = (themes == null) ? null : new GapShuffler<String>(themes, srng);
            this.titles = (titles == null) ? null : new GapShuffler<String>(titles, srng);
        }
        /**
         * Creates an AssociatedName with the being's name as a String and any associated themes and titles as String
         * arrays, with a boolean after the name that determines whether the name should be "translated" using a
         * NaturalLanguageCipher to some other form. If you gave this:
         * <code>"Brunhilda", true, new String[]{"ice`noun`"}, "Goddess`noun` of Ice`nouns`", "Winter-Empress`noun`", "Heroine`noun` of the North"</code>,
         * it could use any of the terms in {@link Thesaurus} associated with the category {@code "ice`noun`} as themes,
         * could generate titles like "Mother of Blizzards", "Winter-Queen", and "Maiden of the North", and would not
         * actually show the name "Brunhilda" in use, instead producing some similar-length name using the
         * NaturalLanguageCipher that a ProceduralMessaging is created with (defaulting to generic fantasy names).
         * This overload allows the {@link Messaging.NounTrait} to be specified, which allows various ways of addressing
         * the being (first person, second person, or third person; singular or plural; various gender options in the
         * third person).
         * @param name the String name for the being, which will be changed if {@code cipherName} is true
         * @param cipherName if true, the name will be changed using a NaturalLanguageCipher before being shown
         * @param pronoun a NounTrait enum that designates how this being should be addressed (often second person singular, but not always)
         * @param themes a String array (which may be null) of words that may appear more often regarding this being
         * @param titles a String array or vararg (which should probably not be null) of special titles for the being
         */
        public AssociatedName(String name, boolean cipherName, Messaging.NounTrait pronoun, String[] themes, String... titles) {
            this.name = cipherName ? "[?]" + name + "[?]" : name;
            this.pronoun = pronoun;
            srng = new StatefulRNG(name);
            this.themes = (themes == null) ? null : new GapShuffler<String>(themes, srng);
            this.titles = (titles == null) ? null : new GapShuffler<String>(titles, srng);
        }
    }
    public Thesaurus thesaurus;
    public NaturalLanguageCipher language;

    public ProceduralMessaging() {
        thesaurus = new Thesaurus();
        language = new NaturalLanguageCipher(FakeLanguageGen.FANTASY_NAME);
        thesaurus.addKnownCategories();
    }
    public ProceduralMessaging(long seed)
    {
        thesaurus = new Thesaurus(seed);
        language = new NaturalLanguageCipher(FakeLanguageGen.FANTASY_NAME);
        thesaurus.addKnownCategories();
    }
    public ProceduralMessaging(long seed, FakeLanguageGen nameLanguage)
    {
        thesaurus = new Thesaurus(seed);
        language = new NaturalLanguageCipher(nameLanguage == null ? FakeLanguageGen.FANTASY_NAME : nameLanguage);
        thesaurus.addKnownCategories();
    }
    public ProceduralMessaging(Thesaurus existingThesaurus, NaturalLanguageCipher existingLanguage)
    {
        thesaurus = (existingThesaurus == null)
                ? new Thesaurus()
                : existingThesaurus;
        language = (existingLanguage == null)
                ? new NaturalLanguageCipher(FakeLanguageGen.FANTASY_NAME)
                : existingLanguage;
        thesaurus.addKnownCategories();
    }
    public String transform(CharSequence message, String userName, Messaging.NounTrait userTrait, String targetName, Messaging.NounTrait targetTrait)
    {
        return language.cipherMarkup(thesaurus.process(Messaging.transform(message, userName, userTrait, targetName, targetTrait)));
    }
    public String transform(CharSequence message, AssociatedName user, String targetName, Messaging.NounTrait targetTrait)
    {
        return language.cipherMarkup(thesaurus.process(Messaging.transform(message, user.name, user.pronoun, targetName, targetTrait,
                (user.titles == null) ? "The Great" : user.titles.next(), (user.themes == null) ? "uncertainty" : user.themes.next())));
    }
    public String transform(CharSequence message, String userName, Messaging.NounTrait userTrait, AssociatedName target)
    {
        return language.cipherMarkup(thesaurus.process(Messaging.transform(message, userName, userTrait, target.name, target.pronoun,
                (target.titles == null) ? "The Great" : target.titles.next(), (target.themes == null) ? "uncertainty" : target.themes.next())));
    }
}
