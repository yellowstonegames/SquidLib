package squidpony.examples;

import squidpony.ObText;

/**
 * Created by Tommy Ettinger on 10/28/2016.
 */
public class ObTextTest {
    public static void main(String[] args){
        ObText ot = new ObText("hello world\n" +
                //"'how are you today?' [just \"great\" thanks]\n" +
                "nice\n" +
                "\n" +
                "complexity?\n" +
                "[it is possible [yes this is a good example]\n" +
                " 'escapes like \\[\\'\\] all work'\n" +
                "]\n" +
                "\n" +
                "\n" +
                "'''\n" +
                "raw strings! ]][][[ \\/\\/\\\n" +
                "keeping newlines intact!\n" +
                "]][][[ \\/\\/\\'''\n" +
                "\n" +
                "'''\n" +
                "represented internally as a string array containing all elements\n" +
                "and an int (or unsigned int) array representing the advancement\n" +
                "to get to the next non-child string or 0 for the final element\n" +
                "in a section (either of children or top-level elements).\n" +
                "'''\n");
        ObText.ItemIterator ii = ot.iterator();
        while (ii.hasNext())
        {
            System.out.println(ii.next());
            if(ii.hasChild())
                System.out.println(ii.child());
        }
    }
}
