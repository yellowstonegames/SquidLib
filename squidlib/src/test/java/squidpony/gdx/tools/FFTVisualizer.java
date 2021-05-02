package squidpony.gdx.tools;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.squidmath.*;

import java.util.Comparator;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;
import static squidpony.squidmath.BlueNoise.ALT_NOISE;
import static squidpony.squidmath.BlueNoise.TRI_NOISE;
//import static squidpony.squidmath.BlueNoise.getChosen;

/**
 */
public class FFTVisualizer extends ApplicationAdapter {

    private final FastNoise noise = new FastNoise(1, 0.25f);
    private final IntPointHash iph = new IntPointHash();
    private final FlawedPointHash.RugHash rug = new FlawedPointHash.RugHash(1);
    private final FlawedPointHash.QuiltHash quilt = new FlawedPointHash.QuiltHash(1, 32);
    private final FlawedPointHash.CubeHash cube = new FlawedPointHash.CubeHash(1, 64);
    private final TorusCachePointHash torus = new TorusCachePointHash(1, 16);
//    private FlawedPointHash.FNVHash fnv = new FlawedPointHash.FNVHash(1);
    private final IPointHash[] pointHashes = new IPointHash[] {iph, torus, cube, rug, quilt};
    private int hashIndex = 0;
    private static final int MODE_LIMIT = 12;
    private int mode = 6;
    private int dim; // this can be 0, 1, 2, or 3; add 2 to get the actual dimensions
    private int octaves = 3;
    private float freq = 0.125f;
    private float threshold = 0.5f;
    private boolean inverse;
    private boolean paused;
    private ImmediateModeRenderer20 renderer;
    
//    private static final int width = 400, height = 400;
    private static final int width = 512, height = 512;
//    private static final int width = 256, height = 256;
    private final double[][] real = new double[width][height], imag = new double[width][height];
    private final double[][] realKnown = new double[width][height], imagKnown = new double[width][height];
    private final float[][] colors = new float[width][height];
    private InputAdapter input;
    
    private Viewport view;
    private long ctr = -128, startTime;
    
    private OrderedMap<Coord, Double> norm;
    StatefulRNG shuffler;
    
    private static final Comparator<Double> doubleComparator = new Comparator<Double>(){
        @Override
        public int compare(Double o1, Double o2) {
            return NumberTools.doubleToHighIntBits(o1 - o2);
        }
    };

    public static float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    public static double basicPrepare(double n)
    {
        return n * 0.5 + 0.5;
    }

    @Override
    public void create() {
        Coord.expandPoolTo(width, height);
        norm = new OrderedMap<>(width * height, 0.75f);
        shuffler = new StatefulRNG(0x1234567890ABCDEFL);
        noise.setNoiseType(FastNoise.CUBIC_FRACTAL);
        noise.setPointHash(pointHashes[hashIndex]);
        Pixmap pm = new Pixmap(Gdx.files.internal("special/BlueNoise512x512.png"));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                realKnown[x][y] = (pm.getPixel(x, y) >>> 24) / 255.0;
            }
        }
        Fft.transformWindowless2D(realKnown, imagKnown);

        startTime = TimeUtils.millis();
        renderer = new ImmediateModeRenderer20(width * height * 2, false, true, 0);
        view = new ScreenViewport();
        input = new InputAdapter(){
            @Override
            public boolean keyUp(int keycode) {
                int s;
                switch (keycode) {
                    case MINUS:
                        mode = (mode + MODE_LIMIT - 1) % MODE_LIMIT;
                        break;
                    case EQUALS:                         
                        mode++;
                        mode %= MODE_LIMIT;
                        break;
                    case SPACE:
                        paused = !paused;
                        break;
                    case C:
                        startTime--;
                        break;
                    case E: //earlier seed
                        s = noise.getSeed() - 1;
                        noise.setSeed(s);
                        cube.setState(s);
                        rug.setState(s);
                        quilt.setState(s);
                        break;
                    case S: //seed after
                        s = noise.getSeed() + 1;
                        noise.setSeed(s);
                        cube.setState(s);
                        rug.setState(s);
                        quilt.setState(s);
                        break;
                    case N: // noise type
                        if(mode == 0) 
                            noise.setNoiseType((noise.getNoiseType() + 1) % 14);
                        break;
                    case ENTER:
                    case D: //dimension
                        dim = (dim + 1) & 3;
                        break;
                    case F: // frequency
                        noise.setFrequency((float) Math.sin(freq += 0.125f) * 0.05f + 0.06f);
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + 1) % 3);
                        break;
                    case G: // Glitch!
                        noise.setPointHash(pointHashes[hashIndex = (hashIndex + 1) % 5]);
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        break;
                    case I: // inverse mode
                        if (inverse = !inverse) {
                            noise.setFractalLacunarity(0.5f);
                            noise.setFractalGain(2f);
                        } else {
                            noise.setFractalLacunarity(2f);
                            noise.setFractalGain(0.5f);
                        }
                        break;
                    case K: // sKip
                        startTime -= 1000000L;
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                    break;
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        if(Gdx.input.isKeyPressed(UP))
            threshold = Math.min(1, threshold + 0x1p-8f);
        else if(Gdx.input.isKeyPressed(DOWN))
            threshold = Math.max(0x1p-8f, threshold - 0x1p-8f);
//// specific thresholds: 32, 96, 160, 224
//        threshold = (TimeUtils.millis() >>> 10 & 3) * 0x40p-8f + 0x20p-8f;
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, nf = noise.getFrequency(), c = (paused ? startTime : TimeUtils.timeSinceMillis(startTime)) * 0x1p-10f / nf, xx, yy;
        double db;
        ArrayTools.fill(imag, 0.0);
        if(mode == 0) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x + c, y + c));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, c));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, c, 0x1p-4f * (x + y - c)));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        xx = x * 0.5f;
                        for (int y = 0; y < height; y++) {
                            yy = y * 0.5f;
                            bright = basicPrepare(noise.getConfiguredNoise(
                                    c + xx, xx - c, yy - c,
                                    c - yy, xx + yy, yy - xx));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 1){
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * IntPointHash.hash256(x, y, noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * PointHash.hash256(x, y, noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = 0x1p-8 * Noise.HastyPointHash.hash256(x, y, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * fancy256(x - (width >>> 1), y - (height >>> 1), noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * castle256(x, y, noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
//                case 1:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = 0x1p-8 * hash256(x, y, ctr, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
//                case 2:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = 0x1p-8 * hash256(x, y, ctr, x + y - ctr, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
//                case 3:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = 0x1p-8 * hash256(ctr + x, x - ctr, y - ctr, 
//                                    ctr - y, x + y, y - x, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
            }
        }
        else if(mode == 2) {
            int ct = (int)(paused ? startTime : TimeUtils.timeSinceMillis(startTime) >>> 5);
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x + ct, y + ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x, y, ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x, y, ct, 1)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x, y, ct, 1, 11, 111)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 3) {
            int ct = (int)(paused ? startTime : TimeUtils.timeSinceMillis(startTime) >>> 5);
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x + ct, y + ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x, y, ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x, y, ct, 1)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x, y, ct, 1, 11, 111)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 4) {
            int ct = (int)(paused ? startTime : TimeUtils.timeSinceMillis(startTime) >>> 9);
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x + ct, y + ct))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x, y, ct))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x, y, ct, 1))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x, y, ct, 1, 11, 111))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 5) {
            norm.clear();
            
            //// Set up an initial Fourier transform for this to invert
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height >>> 1; y++) {
                    norm.put(Coord.get(x, y),
                            real[x][y] = real[width - 1 - x][height - 1 - y] = realKnown[x][y]);
                    imag[x][y] = imag[width - 1 - x][height - 1 - y] = imagKnown[x][y];
                 }
            }
            
//            //// This is likely incorrect... imag probably also needs some values.
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height >>> 1; y++) {
//                    final double hx = 1.0 - Math.abs(x - width * 0.5 + 0.5) / 255.5, hy = 1.0 - (height * 0.5 - 0.5 - y) / 255.5;
//                    final double a = Math.sqrt(hx * hx + hy * hy);
//                    norm.put(Coord.get(x + 256 & 511, y + 256 & 511),
//                            real[x][y] = real[width - 1 - x][height - 1 - y] = 
//                            0x1p-8 * IntPointHash.hash256(x, y, noise.getSeed()) * MathUtils.clamp((a * a * a * (a * (a * 6.0 -15.0) + 10.0) - 0.125), 0.0, 1.0));
//                 }
//            }
//            real[width >>> 1][height >>> 1] = 1.0;
//            real[(width >>> 1)-1][(height >>> 1)] = 1.0;
//            real[(width >>> 1)-1][(height >>> 1)-1] = 1.0;
//            real[(width >>> 1)][(height >>> 1)-1] = 1.0;
//            norm.put(Coord.get(width >>> 1, (height >>> 1)-1), 1.0);
//            norm.put(Coord.get((width >>> 1) - 1, (height >>> 1)-1), 1.0);
            
            //// Done setting up the initial Fourier transform 
            
            Fft.transformWindowless2D(imag, real);
            //// re-normalize

            norm.shuffle(shuffler);
            norm.sortByValue(doubleComparator);
            final int ns = norm.size();
            final double den = (ns - 1.0);
            for (int i = 0; i < ns; i++) {
                final Coord co = norm.keyAt(i);
                real[co.x][co.y] = real[width - 1 - co.x][height - 1 - co.y] = i / den;
            }
            shuffler.setState(0x1234567890ABCDEFL);
            //// done re-normalizing
//            
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    bright = (float) real[x][y];
//                    renderer.color(bright, bright, bright, 1f);
//                    renderer.vertex(x, y, 0);
//                }
//            }
            Fft.getColors(real, imag, colors);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    renderer.color(colors[x][y]);
                    renderer.vertex(x, y, 0);
                }
            }
        }
        else if(mode == 6){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.get(x, y, ALT_NOISE[noise.getSeed() & 63]) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = 0x1p-8 * (BlueNoise.getSeeded(x, y, noise.getSeed()) + 128));
                            bright = (float) (db = 0x1p-8 * (BlueNoise.getChosen(x, y, noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.getSeededTiling(x, y, noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.getSeededTilingOmni(x, y, noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 7){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.get(x, y, TRI_NOISE[noise.getSeed() & 63]) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.getSeeded(x, y, noise.getSeed(), TRI_NOISE[0]) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.getChosen(x, y, noise.getSeed(), TRI_NOISE) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-8 * (BlueNoise.getSeededTriangular(x, y, noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 8){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8f * (BlueNoise.get(x, y, ALT_NOISE[noise.getSeed() & 63]) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
//                            bright = 0x1p-8 * (BlueNoise.getSeeded(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            bright = 0x1p-8 * (BlueNoise.getChosen(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8 * (BlueNoise.getSeededTiling(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8 * (BlueNoise.getSeededTilingOmni(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 9){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8f * (BlueNoise.get(x, y, TRI_NOISE[noise.getSeed() & 63]) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8 * (BlueNoise.getSeeded(x, y, noise.getSeed(), TRI_NOISE[0]) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8 * (BlueNoise.getChosen(x, y, noise.getSeed(), TRI_NOISE) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = 0x1p-8 * (BlueNoise.getSeededTriangular(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 10) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (getWobbled(x, y) + 128) / 255f);
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (getWobbled(x, y) + getWobbled(y + 421, x + 107) + 256) / 510f);
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
            }
        }
        else if(mode == 11) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (getWobbled(x, y) + 128) / 255f <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (getWobbled(x, y) + getWobbled(y + 421, x + 107) + 256) / 510f <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }

            }
        }

        Fft.transform2D(real, imag);
        Fft.getColors(real, imag, colors);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                renderer.color(colors[x][y]);
                renderer.vertex(x + width, y, 0);
            }
        }
        renderer.end();
    }

    /**
     * For whatever reason, this makes output that looks like castles or carpets, kinda.
     * The explanation seems to be that each input is XORed with a large constant plus itself.
     * The constant does not seem to matter; here we use the golden ratio times 2 to the 64,
     * but earlier versions used 3 different numbers. Without the xor-by-constant-plus-itself,
     * this is an ordinary point hash, though not the highest quality.
     * @param x x position, as any long
     * @param y y position, as any long
     * @param s seed, as any long
     * @return a highly artifact-laden hash from 0 to 255, both inclusive
     */
    public static long castle256(long x, long y, long s) {
        x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
        y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
        s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
        return s >>> 56;
    }
    public static int fancy256(int x, int y, int s) {
//        x = Math.abs(x);
//        y = Math.abs(y);
        s ^= (x >> 6) * 0xD1B5;
        s ^= (y >> 6) * 0xABC9;
        x *= x;
        y *= y;
        x = x >>> 1 & 63;
        y = y >>> 1 & 63;
        int a, b;
        if(x > y){
            a = x;
            b = y;
        }
        else {
            a = y;
            b = x;
        }
        a = (a + 0x9E3779B9 ^ a) * (s ^ b);
        b = (b + 0x9E3779B9 ^ b) * (a ^ s);
        s = (s + 0x9E3779B9 ^ s) * (b ^ a);
        return s >>> 24;
    }
//        x *= 0xD1B54A32D192ED03L;
//        y *= 0xABC98388FB8FAC03L;
//        s *= 0x8CB92BA72F3D8DD7L;
//        return (s ^ s >>> 25) & 0xFF;

    public static byte getBlue(int x, int y, int s){
        final int m = Integer.bitCount(ALT_NOISE[(x + 23 >>> 6) + (y + 41 >>> 6) + (s >>> 6) & 63][(x + 23 << 6 & 0xFC0) | (y + 41 & 0x3F)] + 128) 
                * Integer.bitCount(ALT_NOISE[(y + 17 >>> 6) - (x + 47 >>> 7) + (s >>> 12) & 63][(y + 17 << 6 & 0xFC0) | (x + 47 & 0x3F)] + 128)
                * Integer.bitCount(ALT_NOISE[(y + 33 >>> 7) + (x - 31 >>> 6) + (s >>> 18) & 63][(y + 33 << 6 & 0xFC0) | (x - 31 & 0x3F)] + 128)
                >>> 1;
        final int n = Integer.bitCount(ALT_NOISE[(x + 53 >>> 6) - (y + 11 >>> 6) + (s >>> 9) & 63][(x + 53 << 6 & 0xFC0) | (y + 11 & 0x3F)] + 128)
                * Integer.bitCount(ALT_NOISE[(y - 27 >>> 6) + (x - 37 >>> 7) + (s >>> 15) & 63][(y - 27 << 6 & 0xFC0) | (x - 37 & 0x3F)] + 128)
                * Integer.bitCount(ALT_NOISE[-(x + 35 >>> 6) - (y - 29 >>> 7) + (s >>> 21) & 63][(x + 35 << 6 & 0xFC0) | (y - 29 & 0x3F)] + 128)
                >>> 1;
        return (byte) (ALT_NOISE[s & 63][(y + (m >>> 7) - (n >>> 7) << 6 & 0xFC0) | (x + (n >>> 7) - (m >>> 7) & 0x3F)] ^ (m ^ n));
    }

    public static byte getChosen(int x, int y, int seed){
        return getChosen(x, y, seed, ALT_NOISE);
    }
    public static byte getChosen(int x, int y, int seed, final byte[][] noises){
        seed ^= (x >>> 6) * 0x1827F5 ^ (y >>> 6) * 0x123C21;
        // hash for a 64x64 tile on the "normal grid"
        int h = (seed = (seed ^ (seed << 19 | seed >>> 13) ^ (seed << 5 | seed >>> 27) ^ 0xD1B54A35) * 0x125493) ^ seed >>> 11;
        // choose from 64 noise tiles in ALT_NOISE and get the exact pixel for our x,y in its 64x64 area
        final int xc = noises[h & 0x3F][(y << 6 & 0xFC0) | (x & 0x3F)];
        // likely to be a different noise tile, and the x,y position is transposed
        final int yc = noises[h >>> 6 & 0x3F][(x << 6 & 0xFC0) | (y & 0x3F)];
        // altered x/y; here we choose a start position for the "offset grid" based on the previously sampled noise
        final int ax = ((xc) * (xc+1) << 6 < ((x & 0x3F) - 32) * ((x & 0x3F) - 31)) ? x - 32 : x + 32;
        final int ay = ((yc) * (yc+1) << 6 < ((y & 0x3F) - 32) * ((y & 0x3F) - 31)) ? y - 32 : y + 32;
        // get a tile based on the "offset grid" position we chose and the hash for the normal grid, then a pixel
        // this transposes x and y again, it seems to help with the particular blue noise textures we have
        h ^= (ax >>> 6) * 0x1827F5 ^ (ay >>> 6) * 0x123C21;
        return noises[(h ^ (h << 19 | h >>> 13) ^ (h << 5 | h >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26][(x << 6 & 0xFC0) | (y & 0x3F)];
    }

    public static byte getWobbled(int x, int y) {
//        return (byte) (15 - (x & 15) | (y & 15) << 4);
        final int h = Integer.reverse(posToHilbertNoLUT(x, y));
//        final int h = Integer.reverse(CoordPacker.mortonEncode(x, y));
        return (byte) ((h >>> 24 ^ h >>> 23 ^ h >>> 22 ^ y ^ x)); // ^ CoordPacker.mortonEncode(511 - (x & 511), (y & 511) << 9)
    }
    private static int posToHilbertNoLUT(int x, int y )
    {
        x &= 511;
        y &= 511;
        int hilbert = 0, remap = 0xb4, mcode, hcode;

        mcode = ( ( x >>> 8 ) & 1 ) | ( ( ( y >>> ( 8 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( (hilbert << 2) + hcode );

        mcode = ( ( x >>> 7 ) & 1 ) | ( ( ( y >>> ( 7 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( (hilbert << 2) + hcode );

        mcode = ( ( x >>> 6 ) & 1 ) | ( ( ( y >>> ( 6 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 5 ) & 1 ) | ( ( ( y >>> ( 5 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 4 ) & 1 ) | ( ( ( y >>> ( 4 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 3 ) & 1 ) | ( ( ( y >>> ( 3 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 2 ) & 1 ) | ( ( ( y >>> ( 2 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 1 ) & 1 ) | ( ( ( y >>> ( 1 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( x & 1 ) | ( ( y & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );

        hilbert = ( ( hilbert << 2 ) + hcode );

        return hilbert;
    }

    @Override
    public void render() {
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS on mode " + mode + ", dim " + dim);
            // standard clear the background routine for libGDX
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            ctr++;
            putMap();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Test: FFT Visualization");
        config.setWindowedMode(width << 1, height);
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new FFTVisualizer(), config);
    }
}
