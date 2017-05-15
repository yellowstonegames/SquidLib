package squidpony.examples;

import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.RNG;

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
        GreasedRegion grc = gr.copy().fringe8way().quasiRandomRegion(0.95, 512);
        int size = points.size(), initialSize = size, csize = grc.size();
        for (int i = 0; i < size && i < csize; i++) {
            points.alter(points.keyAt(i), grc.nth(i));
        }
        System.out.println(points.size() == initialSize);
        grc.fringe8way().quasiRandomRegion(0.95, 512);
        size = points.size();
        csize = grc.size();
        for (int i = 0; i < size && i < csize; i++) {
            points.alter(points.keyAt(i), grc.nth(i));
        }

        System.out.println(points.size() == initialSize);
    }
}
