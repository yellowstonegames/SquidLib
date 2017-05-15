package squidpony.examples;

import squidpony.squidmath.*;

/**
 * Created by Tommy Ettinger on 10/28/2016.
 */
public class ArrangementTest {
    public static void main(String[] args){
        Arrangement<Coord> points = new Arrangement<>(1024);
        RNG random = new RNG(0x13AFBEEFBA77L);
        GreasedRegion gr = new GreasedRegion(256, 256);
        gr.refill(random, 0.25, 256, 256).quasiRandomRegion(0.5, 512);
        points.addAllIfAbsent(gr);
        Coord pt;
        int idx;
        for (int i = 0; i < points.size(); i++) {
            System.out.print(i + ":" + points.getAt(i) + ", ");
            if(i % 16 == 15)
                System.out.println();

        }
        System.out.println('\n');
        GreasedRegion grc = gr.copy().fringe8way().quasiRandomRegion(0.95, 512);
        int size = points.size(), csize = grc.size();
        for (int i = 0; i < size && i < csize; i++) {
            pt = grc.nth(i);
            points.alter(points.keyAt(i), pt);
            idx = points.getInt(pt);
            if(idx != i)
            {
                System.out.println("UH OH, " + idx + " != " + i);
            }
            if(points.getAt(i) != idx)
            {
                System.out.println("UH OH re: at, " + idx + " != " + points.getAt(i));
            }
        }
        for (int i = 0; i < points.size(); i++) {
            System.out.print(i + ":" + points.getInt(points.keyAt(i)) + ", ");
            if(i % 16 == 15)
                System.out.println();
        }
        System.out.println('\n');
        grc.fringe8way().quasiRandomRegion(0.95, 512);
        size = points.size();
        csize = grc.size();
        for (int i = 0; i < size && i < csize; i++) {
            points.alter(points.keyAt(i), grc.nth(i));
        }
        for (int i = 0; i < points.size(); i++) {
            System.out.print(i + ":" + points.getAt(i) + ", ");
            if(i % 16 == 15)
                System.out.println();
        }
    }
}
