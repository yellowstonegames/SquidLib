package squidpony.gdx.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.WorldMapView;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Porting Bart Wronski's blue noise generator from NumPy to Java, see
 * https://bartwronski.com/2021/04/21/superfast-void-and-cluster-blue-noise-in-python-numpy-jax/ for more.
 */
public class BlueNoiseGenerator extends ApplicationAdapter {
    private static final int width = 256, height = 256;
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
        pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);
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

        double[][] wrapping = new double[64][64];
        for (int i = 1; i <= 32; i++) {
            wrapping[63][64 - i] = wrapping[63][i] = Math.exp(-0.5 * i * i / sigma2);
        }
        for (int x = 1; x < 63; x++) {
            for (int y = 1; y < 64; y++) {
                wrapping[x][y] = wrapping[63][x] * wrapping[63][y];
            }
        }
        wrapping[0][0] = Double.POSITIVE_INFINITY;

        pm.setColor(SColor.BLACK);
        pm.fill();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
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
        config.setWindowedMode(width, height);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new BlueNoiseGenerator(), config);
    }
}
