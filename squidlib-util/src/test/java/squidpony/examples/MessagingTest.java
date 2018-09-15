package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.Messaging;
import squidpony.ProceduralMessaging;

import static squidpony.Messaging.NounTrait.*;

/**
 * Created by Tommy Ettinger on 11/2/2016.
 */
public class MessagingTest {
    public static void main(String[] args)
    {
        String message = "@Name hit$ ^ for ~ ~ damage and ~ ~ damage!";
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblin", MALE_GENDER, "10", "bludgeoning", "4", "lightning"));
        System.out.println(Messaging.transform(message, "the goblin", MALE_GENDER, "Heero Supra", SECOND_PERSON_SINGULAR, "10", "poison", "3", "piercing"));

        message = "@Name hit$ ^! ";
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblin", MALE_GENDER));
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblins", GROUP));
        System.out.println(Messaging.transform(message, "the goblin", MALE_GENDER, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "the goblins", GROUP, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.print(Messaging.transform(message, new Messaging.Group("the goblin", "the kobold", "the owlbears"), GROUP, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, new Messaging.Group("the goblin", "the kobold", "the owlbears"), GROUP));

        message = "@Name spit$ in ^name_s face^s!";
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblin", MALE_GENDER));
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblins", GROUP));
        System.out.println(Messaging.transform(message, "the goblin", MALE_GENDER, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "the goblins", GROUP, "Heero Supra", SECOND_PERSON_SINGULAR));

        message = "@Name @don_t care what ^name think^$, @i'll get @myself onto that dancefloor!";
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblin", MALE_GENDER));
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR, "the goblins", GROUP));
        System.out.println(Messaging.transform(message, "the goblin", MALE_GENDER, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "the goblins", GROUP, "Heero Supra", SECOND_PERSON_SINGULAR));

        message = "@Name@m gonna try out the escapes! \\@, \\^ \\$\\~!";
        System.out.println(Messaging.transform(message, "Heero Supra", FIRST_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "the goblin", MALE_GENDER, "Heero Supra", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "the goblins", GROUP, "Heero Supra", SECOND_PERSON_SINGULAR));

        message = "@I @am @my own boss@ss.";
        System.out.println(Messaging.transform(message, "Captain Spectacular", FIRST_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "Captain Spectacular", FIRST_PERSON_PLURAL));
        System.out.println(Messaging.transform(message, "Captain Spectacular", SECOND_PERSON_SINGULAR));
        System.out.println(Messaging.transform(message, "Captain Spectacular", SECOND_PERSON_PLURAL));
        System.out.println(Messaging.transform(message, "Captain Spectacular", NO_GENDER));
        System.out.println(Messaging.transform(message, "Captain Spectacular", MALE_GENDER));
        System.out.println(Messaging.transform(message, "Captain Spectacular", FEMALE_GENDER));
        System.out.println(Messaging.transform(message, "Captain Spectacular", UNSPECIFIED_GENDER));
        System.out.println(Messaging.transform(message, "Captain Spectacular", ADDITIONAL_GENDER));
        System.out.println(Messaging.transform(message, "Captain Spectacular", SPECIAL_CASE_GENDER));
        System.out.println(Messaging.transform(message, "Captain Spectacular", GROUP));

        ProceduralMessaging pm = new ProceduralMessaging(123456789L, FakeLanguageGen.DEMONIC), 
                pm2 = new ProceduralMessaging(987654321L, FakeLanguageGen.ELF),
                pm3 = new ProceduralMessaging(12345432123454321L, FakeLanguageGen.DEEP_SPEECH.mix(FakeLanguageGen.LOVECRAFT, 0.45));
        ProceduralMessaging.AssociatedName meanie = new ProceduralMessaging.AssociatedName("Meanie", true, SECOND_PERSON_SINGULAR, null, "The Eternal Monster`noun`", "That Which Broods In Shadow`noun`", "The Black Fire`noun` of [?]Prussia[?]"),
        good = new ProceduralMessaging.AssociatedName("Nicety", true, SECOND_PERSON_SINGULAR, null, "Goddess`noun` of Light`nouns`", "Our Popular`adj` Heroine`noun`", "The Light`adj` One"),
                weird = new ProceduralMessaging.AssociatedName("Zalzaron", true, SECOND_PERSON_PLURAL, null, "Those With Mad Gaze`noun`", "The Impossible`adj` Flock", "The Many`adj` Brood");
        
        for (int i = 0; i < 32; i++) {
            System.out.println(
                    pm.transform("@Direct, ~, bring forth @name_s terrible rage`noun` against ^this wretch^ss!",
                            meanie, "Some Knights Or Something",
                            (i & 1) == 0 ? Messaging.NounTrait.NO_GENDER : Messaging.NounTrait.GROUP)
            );
            System.out.println(
                    pm2.transform("By the eight`noun` glyph`nouns` of @Direct, in the name of ~, protect us from this villain`noun`!",
                            good, "Bad Guy",
                            Messaging.NounTrait.MALE_GENDER)
            );
            System.out.println(
                    pm3.transform("With smart`noun` beyond my own, I conjure the Twelve`adj` Glyph`noun` of @Direct! ~, brandish thy POWER`noun`!",
                            weird, "Bad Guy",
                            Messaging.NounTrait.MALE_GENDER)
            );
        }
    }
}

