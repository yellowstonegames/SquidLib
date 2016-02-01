package squidpony;

import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.util.*;

/**
 * Created by Tommy Ettinger on 1/31/2016.
 */
public class MonsterGen {

    public StatefulRNG srng = new StatefulRNG();

    public String[] adjectives = new String[]{"hairy", "scaly", "feathered", "chitinous", "pulpy", "writhing", "horrid",
            "fuzzy", "reptilian", "avian", "insectoid", "tentacled", "thorny", "angular", "curvaceous", "lean",
            "metallic", "stony", "glassy", "gaunt", "obese", "ill-proportioned", "sickly", "asymmetrical", "muscular"},
    powerAdjectives = new String[]{"fire-breathing", "electrified", "frigid", "toxic", "noxious", "nimble",
            "brutish", "bloodthirsty", "furious", "reflective", "regenerating", "earth-shaking", "thunderous",
            "screeching", "all-seeing", "semi-corporeal", "vampiric", "skulking", "terrifying", "undead", "mechanical",
            "angelic", "plant-like", "fungal", "contagious", "graceful", "malevolent", "gigantic", "wailing"},
    powerPhrases = new String[]{"can evoke foul magic", "can petrify with its gaze", "hurts your eyes to look at",
            "can spit venom", "can cast arcane spells", "can call on divine power", "embodies the wilderness",
            "hates all other species", "constantly drools acid", "whispers maddening secrets in forgotten tongues"};
    public static class Chimera
    {
        public LinkedHashMap<String, List<String>> parts;
        public LinkedHashSet<String> unsaidAdjectives, wholeAdjectives, talentAdjectives, talentPhrases;
        public String name, mainForm;
        public Chimera(String name, Chimera other)
        {
            this.name = name;
            mainForm = other.name;
            parts = new LinkedHashMap<String, List<String>>(other.parts);
            List<String> oldParts = parts.remove(mainForm);
            parts.put(name, oldParts);
            unsaidAdjectives = other.unsaidAdjectives;
            wholeAdjectives = other.wholeAdjectives;
            talentAdjectives = other.talentAdjectives;
            talentPhrases = other.talentPhrases;
        }
        public Chimera(String name, String... terms)
        {
            this.name = name;
            mainForm = name;
            parts = new LinkedHashMap<String, List<String>>();
            unsaidAdjectives = new LinkedHashSet<String>();
            wholeAdjectives = new LinkedHashSet<String>();
            talentAdjectives = new LinkedHashSet<String>();
            talentPhrases = new LinkedHashSet<String>();
            ArrayList<String> selfParts = new ArrayList<String>();
            int t = 0;
            for (; t < terms.length; t++) {
                if(terms[t].equals(";"))
                {
                    t++;
                    break;
                }
                selfParts.add(terms[t]);
            }
            parts.put(name, selfParts);
            for (; t < terms.length; t++) {
                if (terms[t].equals(";")) {
                    t++;
                    break;
                }
                unsaidAdjectives.add(terms[t]);
            }
            for (; t < terms.length; t++) {
                if (terms[t].equals(";")) {
                    t++;
                    break;
                }
                wholeAdjectives.add(terms[t]);
            }
            wholeAdjectives.removeAll(unsaidAdjectives);
            for (; t < terms.length; t++) {
                if (terms[t].equals(";")) {
                    t++;
                    break;
                }
                talentAdjectives.add(terms[t]);
            }
            for (; t < terms.length; t++) {
                if (terms[t].equals(";")) {
                    break;
                }
                talentPhrases.add(terms[t]);
            }
        }
        public String present(boolean capitalize)
        {
            StringBuilder sb = new StringBuilder(), tmp = new StringBuilder();
            if(capitalize)
                sb.append('A');
            else
                sb.append('a');
            int i = 0;
            LinkedHashSet<String> allAdjectives = new LinkedHashSet<String>(wholeAdjectives);
            allAdjectives.addAll(talentAdjectives);
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
            sb.append(' ');
            sb.append(ts);
            if(!(talentPhrases.isEmpty() && parts.size() == 1))
                sb.append(' ');
            if(parts.size() > 1)
            {
                sb.append("with the");
                i = 1;
                for(Map.Entry<String, List<String>> ent : parts.entrySet())
                {
                    if(ent.getKey().equals(name))
                        continue;
                    if(ent.getValue().isEmpty())
                        sb.append(" feel");
                    else
                    {
                        int j = 1;
                        for(String p : ent.getValue())
                        {
                            sb.append(' ');
                            sb.append(p);
                            if(j++ < ent.getValue().size() && ent.getValue().size() > 2)
                                sb.append(',');
                            if(j == ent.getValue().size() && ent.getValue().size() >= 2)
                                sb.append(" and");
                        }
                    }
                    sb.append(" of a ");
                    sb.append(ent.getKey());


                    if(i++ < parts.size() && parts.size() > 3)
                        sb.append(',');
                    if(i == parts.size() && parts.size() >= 3)
                        sb.append(" and");
                    sb.append(' ');
                }
            }

            if(!talentPhrases.isEmpty())
                sb.append("that");
            i = 1;
            for(String phr : talentPhrases)
            {
                sb.append(' ');
                sb.append(phr);
                if(i++ < talentPhrases.size() && talentPhrases.size() > 2)
                    sb.append(',');
                if(i == talentPhrases.size() && talentPhrases.size() >= 2)
                    sb.append(" and");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return name;
        }
        public Chimera mix(RNG rng, String newName, Chimera other, double otherInfluence)
        {
            Chimera next = new Chimera(newName, this);
            List<String> otherParts = other.parts.get(other.name),
                    p2 = rng.randomPortion(otherParts, (int)Math.round(otherParts.size() * otherInfluence * 0.5));
            next.parts.put(other.name, p2);
            String[] unsaid = other.unsaidAdjectives.toArray(new String[other.unsaidAdjectives.size()]),
                    talentAdj = other.talentAdjectives.toArray(new String[other.talentAdjectives.size()]),
                    talentPhr = other.talentPhrases.toArray(new String[other.talentPhrases.size()]);
            unsaid = rng.randomPortion(unsaid, (int)Math.round(unsaid.length * otherInfluence));
            talentAdj = rng.randomPortion(talentAdj, (int)Math.round(talentAdj.length * otherInfluence));
            talentPhr = rng.randomPortion(talentPhr, (int)Math.round(talentPhr.length * otherInfluence));
            Collections.addAll(next.wholeAdjectives, unsaid);
            Collections.addAll(next.talentAdjectives, talentAdj);
            Collections.addAll(next.talentPhrases, talentPhr);

            return next;
        }
    }
    public static final Chimera SNAKE = new Chimera("snake", "head", "tail", "fangs", "eyes", ";",
            "reptilian", "scaly", "lean", "curvaceous", ";",
            ";",
            "toxic"),
    LION = new Chimera("lion", "head", "tail", "legs", "claws", "fangs", "eyes", ";",
            "hairy", "muscular", ";",
            ";",
            "furious");


}
