package squidpony.examples;
import squidpony.Messaging;
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


    }
}
