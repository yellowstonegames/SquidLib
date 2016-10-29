package squidpony.examples;

import squidpony.squidmath.Arrangement;

/**
 * Created by Tommy Ettinger on 10/28/2016.
 */
public class ArrangementTest {
    public static void main(String[] args){
        Arrangement<String> strings = new Arrangement<>(8);
        System.out.println("aaa: " + strings.add("aaa") + " " + strings.get("aaa"));
        System.out.println("bbb: " + strings.add("bbb") + " " + strings.get("bbb"));
        System.out.println("ccc: " + strings.add("ccc") + " " + strings.get("ccc"));
    }
}
