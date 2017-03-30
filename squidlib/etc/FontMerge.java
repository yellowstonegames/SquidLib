package squidpony.gdx.tests;

import com.badlogic.gdx.utils.IntSet;

import java.io.InputStream;
import java.util.TreeSet;

/**
 * Created by Tommy Ettinger on 12/1/2016.
 */
public class FontMerge {
    private static String stringifyStream(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is);
        s.useDelimiter("\\A");
        String nx = s.hasNext() ? s.next() : "";
        s.close();
        return nx;
    }

    public static void main(String[] args)
    {
        IntSet inco = new IntSet(1024), scp = new IntSet(1024),
                cm = new IntSet(1024), gent = new IntSet(1024);
        TreeSet<Character> all = new TreeSet<>();
        String iStr = stringifyStream(FontMerge.class.getResourceAsStream("/InconsolataLGC.txt")),
        sStr = stringifyStream(FontMerge.class.getResourceAsStream("/SourceCodePro.txt")),
        cStr = stringifyStream(FontMerge.class.getResourceAsStream("/CM.txt")),
        gStr = stringifyStream(FontMerge.class.getResourceAsStream("/Gentium.txt"));

        for (int i = 0; i < iStr.length(); i++) {
            inco.add(iStr.codePointAt(i));
        }
        for (int i = 0; i < sStr.length(); i++) {
            scp.add(sStr.codePointAt(i));
        }
        for (int i = 0; i < cStr.length(); i++) {
            cm.add(cStr.codePointAt(i));
        }
        for (int i = 0; i < gStr.length(); i++) {
            gent.add(gStr.codePointAt(i));
        }
        IntSet.IntSetIterator ii = inco.iterator();
        int q;
        while (ii.hasNext)
        {
            q = ii.next();
            if(scp.contains(q) && cm.contains(q))// && gent.contains(q))
            {
                all.add((char)q);
            }
        }
        int shown = 0;
        for(Character c : all)
        {
            if(c >= 32)
            {
                System.out.print(c);
                if(++shown >= 80)
                {
                    shown = 0;
                    System.out.println();
                }
            }
        }
    }
}
/*
" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno"+
"pqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàá"+
"âãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİı"+
"ĲĳĴĵĶķĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƒǺǻǼǽǾ"+
"ǿȘșȚțȷˆˇˉˋ˘˙˚˛˜˝;΄΅Ά·ΈΉΊΌΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυ"+
"φχψωϊϋόύώЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхц"+
"чшщъыьэюяѐёђѓєѕіїјљњћќѝўџѢѣѲѳѴѵҐґẀẁẂẃẄẅỲỳ–—‘’‚‛“”„†‡•…‰‹›ⁿ₤€№™Ω℮←↑→↓∆−√≈─│┌┐└┘├┤"+
"┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫■□▪▫▲▼◊○●◦♀♂♠♣♥♦♪"
 */
