package squidpony.squidgrid.mapping.shape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import squidpony.annotation.Beta;
import squidpony.squidmath.RNG;

/**
 * @author Tommy Ettinger
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class BrickDungeon {

    private Scanner vertScanner;
    public ArrayList<char[][]> tilesVert = new ArrayList<>(128);
    private Scanner horizScanner;
    public ArrayList<char[][]> tilesHoriz = new ArrayList<>(128);
    private char[][] shown;
    public int wide;
    public int high;
    public boolean colorful;
    private RNG rng;
    public static ArrayList<char[][]> tilesVertShared = null,
            tilesHorizShared = null;

    private void loadStreams(InputStream horizStream, InputStream vertStream) {
        if (horizStream == null) {
            horizStream = getClass().getResourceAsStream(
                    "/centeredHoriz.txt");
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
                String[] nx = vertScanner.next().split("\r?\n");
                char[][] curr = new char[nx.length][nx[0].length()];
                for (int i = 0; i < nx.length; i++) {
                    curr[i] = nx[i].toCharArray();
                }
                tilesVert.add(curr);
            }
        } finally {
            if (vertScanner != null) {
                vertScanner.close();
            }
        }
        try {
            while (horizScanner.hasNext()) {
                String[] nx = horizScanner.next().split("\r?\n");
                char[][] curr = new char[nx.length][nx[0].length()];
                for (int i = 0; i < nx.length; i++) {
                    curr[i] = nx[i].toCharArray();
                }
                tilesHoriz.add(curr);
            }
        } finally {
            if (horizScanner != null) {
                horizScanner.close();
            }
        }
    }

    public BrickDungeon() {
        this(20, 80);
    }

    public BrickDungeon(int wide, int high) {
        this(wide, high, new RNG());
    }

    public BrickDungeon(int wide, int high, RNG random) {
        this(wide, high, random, null, null);
    }

    public BrickDungeon(int wide, int high, InputStream horizStream,
            InputStream vertStream) {
        this(wide, high, new RNG(), horizStream, vertStream);
    }

    public BrickDungeon(int wide, int high, RNG random,
            InputStream horizStream, InputStream vertStream) {
        this(wide, high, random, horizStream, vertStream, false);
    }

    public BrickDungeon(int wide, int high, RNG random,
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
        char[][] outer = new char[wide + 20][high + 10];
        this.shown = new char[wide][high];
        for (int i = 0; i < wide + 20; i++) {
            for (int j = 0; j < high + 10; j++) {
                outer[i][j] = '#';
            }
        }
        int nextFillX = 0;
        int nextFillY = 0;
        int startingIndent = 0;
        while ((nextFillY < high)) {
            char[][] horiz = tilesHorizShared.get(rng.nextInt(tilesHorizShared.size()));
            int randColor = (colorful) ? (random.nextInt(7) + 1) * 128 : 0;
            if ((nextFillX < wide) && ((nextFillY < high))) {
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 10; j++) {
                        outer[nextFillX + i][nextFillY + j] = (char) ((int) (horiz[i][j]) + randColor);
                    }
                }
            }
            //int tempNextFill = nextFillX;
            if ((20 + nextFillX) % (wide + 10) < nextFillX) {
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
                    shown[i][j] = '#';
                } else {
                    shown[i][j] = outer[i + 10][j];
                }
            }
        }
    }

    public char[][] getShown() {
        return shown;
    }

    public char[] get1DShown() {
        char[] shown1D = new char[wide * high];
        for (int x = 0; x < wide; x++) {
            for (int y = 0; y < high; y++) {
                shown1D[(y * wide) + x] = shown[x][y];
            }
        }
        return shown1D;
    }

    public void setShown(char[][] shown) {
        this.wide = shown.length;
        this.high = (this.wide > 0) ? shown[0].length : 0;
        this.shown = shown;
    }

    public void set1DShown(char[] shown, int wide) {
        this.wide = wide;
        this.high = shown.length / wide;
        for (int x = 0; x < wide; x++) {
            for (int y = 0; y < high; y++) {
                this.shown[x][y] = shown[(y * wide) + x];
            }
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer("");
        int currentColor = 0;
        for (int j = 0; j < shown[0].length; j++) {
            for (int i = 0; i < shown.length; i++) {
                if (colorful) {
                    if (currentColor != (30 + (shown[i][j] / 128)) && ((int) shown[i][j] / 128) != 0) {
                        s.append("\u001B[0m\u001B[" + (30 + (shown[i][j] / 128)) + "m");
                        currentColor = (30 + (shown[i][j] / 128));
                    } else if (((int) shown[i][j] / 128) == 0) {
                        s.append("\u001B[0m");
                        currentColor = 0;
                    }
                    s.append((char) (shown[i][j] % 128));
                } else {
                    s.append(shown[i][j]);
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
