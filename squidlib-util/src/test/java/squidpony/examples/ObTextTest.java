package squidpony.examples;

import squidpony.ObText;

import java.util.List;

/**
 * Created by Tommy Ettinger on 10/28/2016.
 */
public class ObTextTest {
    public static void main(String[] args){
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
                "// comments like in c are allowed\n" +
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

}
