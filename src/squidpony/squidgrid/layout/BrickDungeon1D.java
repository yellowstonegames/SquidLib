package squidpony.squidgrid.layout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import squidpony.annotation.Beta;
import squidpony.squidmath.RNG;

/**
 * @author Tommy Ettinger
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class BrickDungeon1D {

    private Scanner vertScanner;
    public ArrayList<char[]> tilesVert = new ArrayList<char[]>(128);
    private Scanner horizScanner;
    public ArrayList<char[]> tilesHoriz = new ArrayList<char[]>(128);
    private char[] shown;
    public int wide;
    public int high;
    public boolean colorful;
    private Random rng;
    public static ArrayList<char[]> tilesVertShared = null,
            tilesHorizShared = null;

    private void loadStreams(InputStream horizStream, InputStream vertStream) {
        if (horizStream == null) {
            horizStream = getClass().getResourceAsStream(
                    "/centeredVert.txt");
        }
        if (vertStream == null) {
            vertStream = getClass().getResourceAsStream(
                    "/centeredVert.txt");
        }
        vertScanner = new Scanner(vertStream);
        vertScanner.useDelimiter("\r?\n\r?\n");
        horizScanner = new Scanner(horizStream);
        horizScanner.useDelimiter("\r?\n\r?\n");
        try {
            while (vertScanner.hasNext()) {
                char[] nx = vertScanner.next().replace("\r\n", "").replace("\n", "").toCharArray();
                tilesVert.add(nx);
            }
        } finally {
            if (vertScanner != null) {
                vertScanner.close();
            }
        }
        try {
            while (horizScanner.hasNext()) {
                char[] nx = horizScanner.next().replace("\r\n", "").replace("\n", "").toCharArray();
                tilesHoriz.add(nx);
            }
        } finally {
            if (horizScanner != null) {
                horizScanner.close();
            }
        }
    }

    public BrickDungeon1D() {
        this(20, 80);
    }

    public BrickDungeon1D(int wide, int high) {
        this(wide, high, new RNG());
    }

    public BrickDungeon1D(int wide, int high, Random random) {
        this(wide, high, random, null, null);
    }

    public BrickDungeon1D(int wide, int high, InputStream horizStream,
            InputStream vertStream) {
        this(wide, high, new RNG(), horizStream, vertStream);
    }

    public BrickDungeon1D(int wide, int high, Random random,
            InputStream horizStream, InputStream vertStream) {
        this(wide, high, random, horizStream, vertStream, false);
    }

    public BrickDungeon1D(int wide, int high, Random random,
            InputStream horizStream, InputStream vertStream, boolean colorful) {

        if ((tilesVertShared == null && tilesVertShared == null) || (horizStream != null || vertStream != null)) {
            loadStreams(horizStream, vertStream);
            tilesVertShared = tilesVert;
            tilesHorizShared = tilesHoriz;
        }
        this.colorful = colorful;

        this.wide = wide;
        this.high = high;
        rng = random;
        // char[][] base = herringbonesHoriz[rng.between(0,
        // herringbonesHoriz.length - 1)];
        int wider = wide + 20;
        int higher = high + 10;
        char[] outer = new char[wider * higher];
        this.shown = new char[wide * high];

        for (int i = 0; i < wider; i++) {
            for (int j = 0; j < higher; j++) {
                outer[i + (wider * j)] = '#';
            }
        }
        int nextFillX = 0;
        int nextFillY = 0;
        int startingIndent = 0;
        while ((nextFillY < high)) {
            char[] horiz = tilesHorizShared.get(rng.nextInt(tilesHorizShared.size()));
            int randColor = (colorful) ? (random.nextInt(7) + 1) * 128 : 0;
            if ((nextFillX < wide) && ((nextFillY < high))) {
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 10; j++) {
                        outer[(nextFillX + i) + wider * (nextFillY + j)] = (char) ((int) (horiz[i + 20 * j]) + randColor);
                    }
                }
            }
            //int tempNextFill = nextFillX;
            if ((20 + nextFillX) % (wider - 10) < nextFillX) {
                nextFillY += 10;
                startingIndent = (startingIndent + 10) % 20;
                nextFillX = startingIndent;
            } else {
                nextFillX += 20;
            }
        }
        for (int i = 0; i < wide; i++) {
            for (int j = 0; j < high; j++) {
                if (i == 0 || j == 0 || i == wide - 1 || j == high - 1) {
                    shown[i + wide * j] = '#';
                } else {
                    shown[i + wide * j] = outer[i + 10 + wider * j];
                }
            }
        }
    }

    public char[][] getShown() {
        char[][] shown2D = new char[wide][high];
        for (int x = 0; x < wide; x++) {
            for (int y = 0; y < high; y++) {
                shown2D[x][y] = shown[(y * wide) + x];
            }
        }
        return shown2D;
    }

    public char[] get1DShown() {
        return shown;
    }

    public void setShown(char[][] shown) {
        this.wide = shown.length;
        this.high = (this.wide > 0) ? shown[0].length : 0;
        for (int x = 0; x < wide; x++) {
            for (int y = 0; y < high; y++) {
                this.shown[x + (wide * y)] = shown[x][y];
            }
        }
    }

    public void set1DShown(char[] shown, int wide) {
        this.wide = wide;
        this.high = shown.length / wide;
        this.shown = shown;
    }

    public String toString() {
        StringBuffer s = new StringBuffer("");
        int currentColor = 0;
        for (int j = 0; j < high; j++) {
            for (int i = 0; i < wide; i++) {
                if (colorful) {
                    if (currentColor != (30 + (shown[i + wide * j] / 128)) && ((int) shown[i + wide * j] / 128) != 0) {
                        s.append("\u001B[0m\u001B[" + (30 + (shown[i + wide * j] / 128)) + "m");
                        currentColor = (30 + (shown[i + wide * j] / 128));
                    } else if (((int) shown[i + wide * j] / 128) == 0) {
                        s.append("\u001B[0m");
                        currentColor = 0;
                    }
                    s.append((char) (shown[i + wide * j] % 128));
                } else {
                    s.append(shown[i + wide * j]);
                }
            }
            s.append('\n');
        }

        if (colorful) {
            s.append("\u001B[0m");
        }
        return s.toString();
    }
}
