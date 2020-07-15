package squidpony.examples;

/**
 * Created by Tommy Ettinger on 6/28/2020.
 */
public class TestConfiguration {
    public static boolean PRINTING;
    public static void print(char c){
        if(PRINTING) System.out.print(c);
    }
    public static void print(CharSequence text){
        if(PRINTING) System.out.print(text);
    }

    public static void println(){
        if(PRINTING) System.out.println();
    }
    public static void println(CharSequence text){
        if(PRINTING) System.out.println(text);
    }
    public static void print(Object toStringable){
        if(PRINTING) System.out.print(toStringable);
    }

    public static void println(Object toStringable){
        if(PRINTING) System.out.println(toStringable);
    }
    
    public static void printf(String text, Object... args){
        if(PRINTING) System.out.printf(text, args);
    }
}
