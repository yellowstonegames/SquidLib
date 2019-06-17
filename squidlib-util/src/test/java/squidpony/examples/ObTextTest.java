package squidpony.examples;

import org.junit.Test;
import squidpony.ObText;
import squidpony.StringKit;

import java.util.List;

/**
 * Created by Tommy Ettinger on 10/28/2016.
 */
public class ObTextTest {
    @Test
    public void basicTest(){
        ObText ot = new ObText(
                "hello world\n" +
                "'how are you today?' [just great thanks]\n" +
                "hooray!\n" +
                "\n" +
                "complexity?\n" +
                "[it is possible [yes this is a good example]\n" +
                "'escapes like \\[\\'\\] all work'\n" +
                "]\n" +
                "\n" +
                "comments are allowed // like this\n" +
                "comments can have different forms # like this\n" +
                "// comments like in Java are allowed\n" +
                "/* like so */\n" +
                "/[delimit/block comments with delimiters work too/delimit]/\n" +
                "\n" +
                "'''\n" +
                "raw strings (heredocs) look like this normally.\n" +
                "    they permit characters without escapes, ]][][[ \\/\\/\\ ,\n" +
                "    except for triple quotes.\n" +
                "    they keep newlines and indentation intact,\n" +
                "except for up to one newline ignored adjacent to each triple quote.\n" +
                "'''\n" +
                "\n" +
                "[[different[\n" +
                "if you may need \"triple quotes\"\n" +
                "    in the raw string, use a different syntax, [[delim[ ]delim]] , that allows delimiters.\n" +
                "here, the delimiter is '''different''', just to be different.]different]]\n");
        String o = ot.toString();
        System.out.println(o);
        System.out.println(ObText.deserializeFromString(o).toString());
        iterate(ot);
    }
    public static void iterate(List<ObText.ObTextEntry> it)
    {
        for(ObText.ObTextEntry entry : it)
        {
            System.out.print('"');
            System.out.print(entry.primary);
            System.out.println("\",");
            if(entry.hasAssociated())
                iterate(entry.associated);
        }
    }
    
    @Test
    public void complexTest()
    {
        String roles = " \"Abjurer\"\n" +
                " [\"melee\"\n" +
                "  [1]\n" +
                "  \"ranged\"\n" +
                "  [1]\n" +
                "  \"magic\"\n" +
                "  [4]\n" +
                "  \"ailment\"\n" +
                "  [2]\n" +
                "  \"field\"\n" +
                "  [\"ward\" [\"spread\"]]\n" +
                "  \"spell\"\n" +
                "  [\"earth\" [\"duration\"]]\n" +
                "  \"item\"\n" +
                "  [\"grimoire+\" [\"fused\" [\"shield\"]]]\n" +
                "  \"counter\"\n" +
                "  [\"magic\" [\"weaken\" [\"nullify\"]]]]\n" +
                " \"Acrobat\"\n" +
                " [\"melee\"\n" +
                "  [2]\n" +
                "  \"ranged\"\n" +
                "  [3]\n" +
                "  \"magic\"\n" +
                "  [1]\n" +
                "  \"ailment\"\n" +
                "  [2]\n" +
                "  \"attack\"\n" +
                "  [\"unarmed\" [\"anti\" [\"moving\"]]]\n" +
                "  \"passive\"\n" +
                "  [\"mobile\" [\"dominate\"]]\n" +
                "  \"item\"\n" +
                "  [\"utility harness+\" [\"fused\" [\"sandals\"]]]\n" +
                "  \"tricks\"\n" +
                "  [\"speed\" [\"speed\"]]]\n";
        ObText obText = new ObText(roles);
        ObText.ObTextEntry ent = obText.get(0);
        System.out.println(ent.associated.get(6).primary);
        System.out.println(StringKit.join(", ", ent.associated.get(6).shallowContents()));
    }

}
