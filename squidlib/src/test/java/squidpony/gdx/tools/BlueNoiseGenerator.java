package squidpony.gdx.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxRuntimeException;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Porting Bart Wronski's blue noise generator from NumPy to Java, see
 * https://bartwronski.com/2021/04/21/superfast-void-and-cluster-blue-noise-in-python-numpy-jax/ for more.
 */
public class BlueNoiseGenerator extends ApplicationAdapter {
    private static final int size = 64, height = 64;
    private static final double sigma = 1.9, sigma2 = sigma * sigma;
    private Pixmap pm;
    private StatefulRNG rng;
    private long seed;
    private long ttg; // time to generate
    private PixmapIO.PNG writer;
    private String date, path;

    @Override
    public void create() {
        date = DateFormat.getDateInstance().format(new Date());
        path = "out/blueNoise/" + date + "/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        writer = new PixmapIO.PNG((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        rng = new StatefulRNG(CrossHash.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getState();

        generate();
        Gdx.app.exit();
    }

    public void generate()
    {
        long startTime = System.currentTimeMillis();

        double[][] wrapping = new double[size][size];
        final int hs = size >>> 1, end = size - 1;
        for (int i = 1; i <= hs; i++) {
            wrapping[end][size - i] = wrapping[end][i] = Math.exp(-0.5 * i * i / sigma2);
        }
        for (int x = 1; x < end; x++) {
            for (int y = 1; y < size; y++) {
                wrapping[x][y] = wrapping[end][x] * wrapping[end][y];
            }
        }
        wrapping[0][0] = Double.POSITIVE_INFINITY;

        final int limit = (size >>> 3) * (size >>> 3);
        ArrayList<Coord> initial = new ArrayList<>(limit);
        for (int i = 1; i <= limit; i++) {
            initial.add(VanDerCorputQRNG.roberts(size, size, 0, 0, i));
        }
        rng.shuffleInPlace(initial);


        pm.setColor(SColor.BLACK);
        pm.fill();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                pm.drawPixel(x, y, 0xFF);
            }
        }

        try {
            writer.write(Gdx.files.local(path + "BlueNoiseTiling.png"), pm); // , false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + path + "BlueNoiseTiling.png", ex);
        }

        ttg = System.currentTimeMillis() - startTime;
    }
    @Override
    public void render() {
    }

    @Override
    public void dispose() {
        super.dispose();
        writer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Tool: Blue Noise Generator");
        config.setWindowedMode(size, size);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new BlueNoiseGenerator(), config);
    }
}
