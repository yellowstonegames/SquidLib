package squidpony;

import squidpony.squidmath.*;

import java.util.*;

/**
 * A class for generating random monster descriptions; can be subclassed to generate stats for a specific game. Use the
 * nested Chimera class for most of the functionality here; MonsterGen is here so you can change the descriptors that
 * monsters can be given (they are in public array fields). You can call randomizeAppearance or randomizePowers on a
 * Chimera to draw from the list of descriptors in MonsterGen, or fuse two Chimera objects with the mix method in the
 * Chimera class. Chimeras can be printed to a usable format with presentVisible or present; the former does not print
 * special powers and is suitable for monsters being encountered, and the latter is more useful for texts in the game
 * world that describe some monster.
 * Created by Tommy Ettinger on 1/31/2016.
 */
public class MonsterGen {

    public static StatefulRNG srng = new StatefulRNG();

    public String[] components = new String[]{"head", "tail", "legs", "claws", "fangs", "eyes", "hooves", "beak",
            "wings", "pseudopods", "snout", "carapace", "sting", "pincers", "fins", "shell"},
            adjectives = new String[]{"hairy", "scaly", "feathered", "chitinous", "pulpy", "writhing", "horrid",
            "fuzzy", "reptilian", "avian", "insectoid", "tentacled", "thorny", "angular", "curvaceous", "lean",
            "metallic", "stony", "glassy", "gaunt", "obese", "ill-proportioned", "sickly", "asymmetrical", "muscular"},
    powerAdjectives = new String[]{"fire-breathing", "electrified", "frigid", "toxic", "noxious", "nimble",
            "brutish", "bloodthirsty", "furious", "reflective", "regenerating", "earth-shaking", "thunderous",
            "screeching", "all-seeing", "semi-corporeal", "vampiric", "skulking", "terrifying", "undead", "mechanical",
            "angelic", "plant-like", "fungal", "contagious", "graceful", "malevolent", "gigantic", "wailing"},
    powerPhrases = new String[]{"can evoke foul magic", "can petrify with its gaze", "hurts your eyes to look at",
            "can spit venom", "can cast arcane spells", "can call on divine power", "embodies the wilderness",
            "hates all other species", "constantly drools acid", "whispers maddening secrets in forgotten tongues",
            "shudders between impossible dimensions", "withers any life around it", "revels in pain"};

    /**
     * A creature that can be mixed with other Chimeras or given additional descriptors, then printed in a usable format
     * for game text.
     */
    public static class Chimera
    {
        public OrderedMap<String, List<String>> parts;
        public OrderedSet<String> unsaidAdjectives, wholeAdjectives, powerAdjectives, powerPhrases;
        public String name, mainForm, unknown;

        /**
         * Copies an existing Chimera other into a new Chimera with potentially a different name.
         * @param name the name to use for the Chimera this constructs
         * @param other the existing Chimera to copy all fields but name from.
         */
        public Chimera(String name, Chimera other)
        {
            this.name = name;
            unknown = other.unknown;
            if(unknown != null)
                mainForm = unknown;
            else
                mainForm = other.name;
            parts = new OrderedMap<>(other.parts);
            List<String> oldParts = new ArrayList<>(parts.remove(mainForm));
            parts.put(name, oldParts);
            unsaidAdjectives = new OrderedSet<>(other.unsaidAdjectives);
            wholeAdjectives = new OrderedSet<>(other.wholeAdjectives);
            powerAdjectives = new OrderedSet<>(other.powerAdjectives);
            powerPhrases = new OrderedSet<>(other.powerPhrases);
        }

        /**
         * Constructs a Chimera given a name (typically all lower-case), null if the creature is familiar or a String if
         * the creature's basic shape is likely to be unknown to players, and an array or vararg of String terms
         * containing, usually, several groups of String elements separated by the literal string ";" . The first group
         * in terms contains what body parts this creature has and could potentially grant to another creature if mixed;
         * examples are "head", "legs", "claws", "wings", and "eyes". In the next group are the "unsaid" adjectives,
         * which are not listed if unknown is false, but may be contributed to other creatures if mixed (mixing a horse
         * with a snake may make the horse scaly, since "scaly" is an unsaid adjective for snakes). Next are adjectives
         * that apply to the whole creature's appearance, which don't need to replicate the unsaid adjectives and are
         * often added as a step to randomize a creature; this part is often empty and simply ends on the separator ";"
         * . Next are the power adjectives, which are any special abilities a creature might have that aren't
         * immediately visible, like "furious" or "toxic". Last are the power phrases, which follow a format like "can
         * cast arcane spells", "embodies the wilderness", or "constantly drools acid"; it should be able to be put in a
         * sentence after the word "that", like "a snake that can cast arcane spells".
         * <br>
         * The unknown argument determines if descriptions need to include basic properties like calling a Snake scaly
         * (null in this case) or a Pestilence Fiend chitinous (no one knows what that creature is, so a String needs to
         * be given so a player and player character that don't know its name can call it something, like "demon").
         * <br>
         * An example is {@code Chimera SNAKE = new Chimera("snake", null, "head", "tail", "fangs", "eyes", ";",
         * "reptilian", "scaly", "lean", "curvaceous", ";", ";", "toxic");}
         * @param name the name to refer to the creature by and its body parts by when mixed
         * @param unknown true if the creature's basic shape is unlikely to be known by a player, false for animals and
         *                possibly common mythological creatures like dragons
         * @param terms an array or vararg of String elements, separated by ";" , see method documentation for details
         */
        public Chimera(String name, String unknown, String... terms)
        {
            this.name = name;
            this.unknown = unknown;
            if(unknown != null)
                mainForm = unknown;
            else
                mainForm = name;
            parts = new OrderedMap<>();
            unsaidAdjectives = new OrderedSet<>();
            wholeAdjectives = new OrderedSet<>();
            powerAdjectives = new OrderedSet<>();
            powerPhrases = new OrderedSet<>();
            ArrayList<String> selfParts = new ArrayList<>();
            int t = 0;
            for (; t < terms.length; t++) {
                if(";".equals(terms[t]))
                {
                    t++;
                    break;
                }
                selfParts.add(terms[t]);
            }
            parts.put(name, selfParts);
            for (; t < terms.length; t++) {
                if (";".equals(terms[t])) {
                    t++;
                    break;
                }
                unsaidAdjectives.add(terms[t]);
            }
            for (; t < terms.length; t++) {
                if (";".equals(terms[t])) {
                    t++;
                    break;
                }
                wholeAdjectives.add(terms[t]);
            }
            wholeAdjectives.removeAll(unsaidAdjectives);
            for (; t < terms.length; t++) {
                if (";".equals(terms[t])) {
                    t++;
                    break;
                }
                powerAdjectives.add(terms[t]);
            }
            for (; t < terms.length; t++) {
                if (";".equals(terms[t])) {
                    break;
                }
                powerPhrases.add(terms[t]);
            }
        }
        /**
         * Constructs a Chimera given a name (typically all lower-case), null if the creature is familiar or a String if
         * the creature's basic shape is likely to be unknown to players, and several String Collection args for the
         * different aspects of the Chimera. The first Collection contains what body parts this creature has and could
         * potentially grant to another creature if mixed; examples are "head", "legs", "claws", "wings", and "eyes".
         * The next Collection contains "unsaid" adjectives, which are not listed if unknown is false, but may be
         * contributed to other creatures if mixed (mixing a horse with a snake may make the horse scaly, since "scaly"
         * is an unsaid adjective for snakes). Next are adjectives that apply to the "whole" creature's appearance,
         * which don't need to replicate the unsaid adjectives and are often added as a step to randomize a creature;
         * this Collection is often empty. Next are the power adjectives, which are any special abilities a creature
         * might have that aren't immediately visible, like "furious" or "toxic". Last are the power phrases, which
         * follow a format like "can cast arcane spells", "embodies the wilderness", or "constantly drools acid"; it
         * should be able to be put in a sentence after the word "that", like "a snake that can cast arcane spells".
         * <br>
         * The unknown argument determines if descriptions need to include basic properties like calling a Snake scaly
         * (null in this case) or a Pestilence Fiend chitinous (no one knows what that creature is, so a String needs to
         * be given so a player and player character that don't know its name can call it something, like "demon").
         * <br>
         * An example is {@code Chimera SNAKE = new Chimera("snake", null, "head", "tail", "fangs", "eyes", ";",
         * "reptilian", "scaly", "lean", "curvaceous", ";", ";", "toxic");}
         * @param name the name to refer to the creature by and its body parts by when mixed
         * @param unknown true if the creature's basic shape is unlikely to be known by a player, false for animals and
         *                possibly common mythological creatures like dragons
         * @param parts the different body part nouns this creature can contribute to a creature when mixed
         * @param unsaid appearance adjectives that don't need to be said if the creature is familiar
         * @param whole appearance adjectives that apply to the whole creature
         * @param powerAdj power adjectives like "furious" or "fire-breathing"
         * @param powerPhr power phrases like "can cast arcane spells"
         */
        public Chimera(String name, String unknown, Collection<String> parts, Collection<String> unsaid,
                       Collection<String> whole, Collection<String> powerAdj, Collection<String> powerPhr)
        {
            this.name = name;
            this.unknown = unknown;
            if(unknown != null)
                mainForm = unknown;
            else
                mainForm = name;
            this.parts = new OrderedMap<String, List<String>>();
            unsaidAdjectives = new OrderedSet<String>(unsaid);
            wholeAdjectives = new OrderedSet<String>(whole);
            powerAdjectives = new OrderedSet<String>(powerAdj);
            powerPhrases = new OrderedSet<String>(powerPhr);
            ArrayList<String> selfParts = new ArrayList<String>(parts);
            this.parts.put(name, selfParts);
        }

        /**
         * Get a string description of this monster's appearance and powers.
         * @param capitalize true if the description should start with a capital letter.
         * @return a String description including both appearance and powers
         */
        public String present(boolean capitalize)
        {
            StringBuilder sb = new StringBuilder(), tmp = new StringBuilder();
            if(capitalize)
                sb.append('A');
            else
                sb.append('a');
            int i = 0;
            OrderedSet<String> allAdjectives = new OrderedSet<>(wholeAdjectives);
            if(unknown != null)
                allAdjectives.addAll(unsaidAdjectives);
            allAdjectives.addAll(powerAdjectives);
            for(String adj : allAdjectives)
            {
                tmp.append(adj);
                if(++i < allAdjectives.size())
                    tmp.append(',');
                tmp.append(' ');
            }
            tmp.append(mainForm);
            String ts = tmp.toString();
            if(ts.matches("^[aeiouAEIOU].*"))
                sb.append('n');
            sb.append(' ').append(ts);
            if(!(powerPhrases.isEmpty() && parts.size() == 1))
                sb.append(' ');
            if(parts.size() > 1)
            {
                sb.append("with the");
                i = 1;
                for(Map.Entry<String, List<String>> ent : parts.entrySet())
                {
                    if(name != null && name.equals(ent.getKey()))
                        continue;
                    if(ent.getValue().isEmpty())
                        sb.append(" feel");
                    else
                    {
                        int j = 1;
                        for(String p : ent.getValue())
                        {
                            sb.append(' ').append(p);
                            if(j++ < ent.getValue().size() && ent.getValue().size() > 2)
                                sb.append(',');
                            if(j == ent.getValue().size() && ent.getValue().size() >= 2)
                                sb.append(" and");
                        }
                    }
                    sb.append(" of a ").append(ent.getKey());


                    if(i++ < parts.size() && parts.size() > 3)
                        sb.append(',');
                    if(i == parts.size() && parts.size() >= 3)
                        sb.append(" and");
                    sb.append(' ');
                }
            }

            if(!powerPhrases.isEmpty())
                sb.append("that");
            i = 1;
            for(String phr : powerPhrases)
            {
                sb.append(' ').append(phr);
                if(i++ < powerPhrases.size() && powerPhrases.size() > 2)
                    sb.append(',');
                if(i == powerPhrases.size() && powerPhrases.size() >= 2)
                    sb.append(" and");
            }
            return sb.toString();
        }

        /**
         * Get a string description of this monster's appearance.
         * @param capitalize true if the description should start with a capital letter.
         * @return a String description including only the monster's appearance
         */
        public String presentVisible(boolean capitalize)
        {
            StringBuilder sb = new StringBuilder(), tmp = new StringBuilder();
            if(capitalize)
                sb.append('A');
            else
                sb.append('a');
            int i = 0;

            OrderedSet<String> allAdjectives = new OrderedSet<>(wholeAdjectives);
            if(unknown != null)
                allAdjectives.addAll(unsaidAdjectives);
            for(String adj : allAdjectives)
            {
                tmp.append(adj);
                if(++i < allAdjectives.size())
                    tmp.append(',');
                tmp.append(' ');
            }
            tmp.append(mainForm);
            String ts = tmp.toString();
            if(ts.matches("^[aeiouAEIOU].*"))
                sb.append('n');
            sb.append(' ').append(ts);
            if(parts.size() > 1)
            {
                sb.append(" with the");
                i = 1;
                for(Map.Entry<String, List<String>> ent : parts.entrySet())
                {
                    if(name != null && name.equals(ent.getKey()))
                        continue;
                    if(ent.getValue().isEmpty())
                        sb.append(" feel");
                    else
                    {
                        int j = 1;
                        for(String p : ent.getValue())
                        {
                            sb.append(' ').append(p);
                            if(j++ < ent.getValue().size() && ent.getValue().size() > 2)
                                sb.append(',');
                            if(j == ent.getValue().size() && ent.getValue().size() >= 2)
                                sb.append(" and");
                        }
                    }
                    sb.append(" of a ").append(ent.getKey());


                    if(i++ < parts.size() && parts.size() > 3)
                        sb.append(',');
                    if(i == parts.size() && parts.size() >= 3)
                        sb.append(" and");
                    sb.append(' ');
                }
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * Fuse two Chimera objects by some fraction of influence, using the given RNG and possibly renaming the
         * creature. Does not modify the existing Chimera objects.
         * @param rng the RNG to determine random factors
         * @param newName the name to call the produced Chimera
         * @param other the Chimera to mix with this one
         * @param otherInfluence the fraction between 0.0 and 1.0 of descriptors from other to use
         * @return a new Chimera mixing features from both inputs
         */
        public Chimera mix(RNG rng, String newName, Chimera other, double otherInfluence)
        {
            Chimera next = new Chimera(newName, this);
            List<String> otherParts = other.parts.get(other.name),
                    p2 = rng.randomPortion(otherParts, (int)Math.round(otherParts.size() * otherInfluence * 0.5));
            next.parts.put(other.name, p2);
            String[] unsaid = other.unsaidAdjectives.toArray(new String[other.unsaidAdjectives.size()]),
                    talentAdj = other.powerAdjectives.toArray(new String[other.powerAdjectives.size()]),
                    talentPhr = other.powerPhrases.toArray(new String[other.powerPhrases.size()]);
            unsaid = portion(rng, unsaid, (int)Math.round(unsaid.length * otherInfluence));
            talentAdj = portion(rng, talentAdj, (int)Math.round(talentAdj.length * otherInfluence));
            talentPhr = portion(rng, talentPhr, (int)Math.round(talentPhr.length * otherInfluence));
            Collections.addAll(next.wholeAdjectives, unsaid);
            Collections.addAll(next.powerAdjectives, talentAdj);
            Collections.addAll(next.powerPhrases, talentPhr);

            return next;
        }

        /**
         * Fuse two Chimera objects by some fraction of influence, using the default RNG and possibly renaming the
         * creature. Does not modify the existing Chimera objects.
         * @param newName the name to call the produced Chimera
         * @param other the Chimera to mix with this one
         * @param otherInfluence the fraction between 0.0 and 1.0 of descriptors from other to use
         * @return a new Chimera mixing features from both inputs
         */
        public Chimera mix(String newName, Chimera other, double otherInfluence)
        {
            return mix(srng, newName, other, otherInfluence);
        }
    }
    public static final Chimera SNAKE = new Chimera("snake", null, "head", "tail", "fangs", "eyes", ";",
            "reptilian", "scaly", "lean", "curvaceous", ";",
            ";",
            "toxic"),
            LION = new Chimera("lion", null, "head", "tail", "legs", "claws", "fangs", "eyes", ";",
                    "hairy", "muscular", ";",
                    ";",
                    "furious"),
            HORSE = new Chimera("horse", null, "head", "tail", "legs", "hooves", "eyes", ";",
                    "fuzzy", "muscular", "lean", ";",
                    ";",
                    "nimble"),
            HAWK = new Chimera("hawk", null, "head", "tail", "legs", "claws", "beak", "eyes", "wings", ";",
                    "feathered", "avian", "lean", ";",
                    ";",
                    "screeching", "nimble"),
            SHOGGOTH = new Chimera("shoggoth", "non-Euclidean ooze", "eyes", "fangs", "pseudopods", ";",
                    "pulpy", "horrid", "tentacled", ";",
                    ";",
                    "terrifying", "regenerating", "semi-corporeal", ";",
                    "shudders between impossible dimensions");

    /**
     * Constructs a MonsterGen with a random seed for the default RNG.
     */
    public MonsterGen()
    {

    }
    /**
     * Constructs a MonsterGen with the given seed for the default RNG.
     */
    public MonsterGen(long seed)
    {
        srng.setState(seed);
    }
    /**
     * Constructs a MonsterGen with the given seed (hashing seed with CrossHash) for the default RNG.
     */
    public MonsterGen(String seed)
    {
        srng.setState(CrossHash.hash(seed));
    }

    /**
     * Randomly add appearance descriptors to a copy of the Chimera creature. Produces a new Chimera, potentially with a
     * different name, and adds the specified count of adjectives (if any are added that the creature already has, they
     * are ignored, and this includes unsaid adjectives if the creature is known).
     * @param rng the RNG to determine random factors
     * @param creature the Chimera to add descriptors to
     * @param newName the name to call the produced Chimera
     * @param adjectiveCount the number of adjectives to add; may add less if some overlap
     * @return a new Chimera with additional appearance descriptors
     */
    public Chimera randomizeAppearance(RNG rng, Chimera creature, String newName, int adjectiveCount)
    {
        Chimera next = new Chimera(newName, creature);
        Collections.addAll(next.wholeAdjectives, portion(rng, adjectives, adjectiveCount));
        next.wholeAdjectives.removeAll(next.unsaidAdjectives);
        return next;
    }

    /**
     * Randomly add appearance descriptors to a copy of the Chimera creature. Produces a new Chimera, potentially with a
     * different name, and adds the specified count of adjectives (if any are added that the creature already has, they
     * are ignored, and this includes unsaid adjectives if the creature is known).
     * @param creature the Chimera to add descriptors to
     * @param newName the name to call the produced Chimera
     * @param adjectiveCount the number of adjectives to add; may add less if some overlap
     * @return a new Chimera with additional appearance descriptors
     */
    public Chimera randomizeAppearance(Chimera creature, String newName, int adjectiveCount)
    {
        return randomizeAppearance(srng, creature, newName, adjectiveCount);
    }

    /**
     * Randomly add power descriptors to a copy of the Chimera creature. Produces a new Chimera, potentially with a
     * different name, and adds the specified total count of power adjectives and phrases (if any are added that the
     * creature already has, they are ignored).
     * @param rng the RNG to determine random factors
     * @param creature the Chimera to add descriptors to
     * @param newName the name to call the produced Chimera
     * @param powerCount the number of adjectives to add; may add less if some overlap
     * @return a new Chimera with additional power descriptors
     */
    public Chimera randomizePowers(RNG rng, Chimera creature, String newName, int powerCount)
    {
        Chimera next = new Chimera(newName, creature);
        int adjs = rng.nextInt(powerCount + 1), phrs = powerCount - adjs;
        Collections.addAll(next.powerAdjectives, portion(rng, powerAdjectives, adjs));
        Collections.addAll(next.powerPhrases, portion(rng, powerPhrases, phrs));
        return next;
    }

    /**
     * Randomly add power descriptors to a copy of the Chimera creature. Produces a new Chimera, potentially with a
     * different name, and adds the specified total count of power adjectives and phrases (if any are added that the
     * creature already has, they are ignored).
     * @param creature the Chimera to add descriptors to
     * @param newName the name to call the produced Chimera
     * @param powerCount the number of adjectives to add; may add less if some overlap
     * @return a new Chimera with additional power descriptors
     */
    public Chimera randomizePowers(Chimera creature, String newName, int powerCount)
    {
        return randomizePowers(srng, creature, newName, powerCount);
    }

    /**
     * Randomly add appearance and power descriptors to a new Chimera creature with random body part adjectives.
     * Produces a new Chimera with the specified name, and adds the specified total count (detail) of appearance
     * adjectives, power adjectives and phrases, and the same count (detail) of body parts.
     * @param rng the RNG to determine random factors
     * @param newName the name to call the produced Chimera
     * @param detail the number of adjectives and phrases to add, also the number of body parts
     * @return a new Chimera with random traits
     */
    public Chimera randomize(RNG rng, String newName, int detail)
    {
        ArrayList<String> ps = new ArrayList<String>();
        Collections.addAll(ps, portion(rng, components, detail));
        Chimera next = new Chimera(newName, "thing", ps, new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        if(detail > 0) {
            int powerCount = rng.nextInt(detail), bodyCount = detail - powerCount;
            int adjs = rng.nextInt(powerCount + 1), phrs = powerCount - adjs;

            Collections.addAll(next.unsaidAdjectives, portion(rng, adjectives, bodyCount));
            Collections.addAll(next.powerAdjectives, portion(rng, powerAdjectives, adjs));
            Collections.addAll(next.powerPhrases, portion(rng, powerPhrases, phrs));
        }
        return next;
    }

    /**
     * Randomly add appearance and power descriptors to a new Chimera creature with random body part adjectives.
     * Produces a new Chimera with the specified name, and adds the specified total count (detail) of appearance
     * adjectives, power adjectives and phrases, and the same count (detail) of body parts.
     * @param newName the name to call the produced Chimera
     * @param detail the number of adjectives and phrases to add, also the number of body parts
     * @return a new Chimera with random traits
     */
    public Chimera randomize(String newName, int detail)
    {
        return randomize(srng, newName, detail);
    }

    /**
     * Randomly add appearance and power descriptors to a new Chimera creature with random body part adjectives.
     * Produces a new Chimera with a random name using FakeLanguageGen, and adds a total of 5 appearance adjectives,
     * power adjectives and phrases, and 5 body parts.
     * @return a new Chimera with random traits
     */
    public Chimera randomize()
    {
        return randomize(srng, randomName(srng), 5);
    }

    /**
     * Gets a random name as a String using FakeLanguageGen.
     * @param rng the RNG to use for random factors
     * @return a String meant to be used as a creature name
     */
    public String randomName(RNG rng)
    {
        return FakeLanguageGen.FANTASY_NAME.word(rng, false, rng.between(2, 4));
    }

    /**
     * Gets a random name as a String using FakeLanguageGen.
     * @return a String meant to be used as a creature name
     */
    public String randomName()
    {
        return randomName(srng);
    }

    private static String[] portion(RNG rng, String[] source, int amount)
    {
        return rng.randomPortion(source, new String[Math.min(source.length, amount)]);
    }

}
