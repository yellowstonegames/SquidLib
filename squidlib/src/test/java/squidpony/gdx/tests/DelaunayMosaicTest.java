package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.TimeUtils;
import squidpony.squidmath.DiverRNG;
import squidpony.squidmath.NumberTools;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class DelaunayMosaicTest extends ApplicationAdapter {
    private ShapeRenderer shaper;
    private DelaunayTriangulator tri;
    private float[] points, pointColors;
    private ShortArray tris;
    private long startTime;
    private Pixmap image;
    private static final int POINTS = 0x17FF;
    @Override
    public void create() {
        image = new Pixmap(Gdx.files.internal("special/Mona_Lisa_404x600.jpg"));
//        image = new Pixmap(Gdx.files.internal("special/Among_the_Sierra_Nevada_by_Albert Bierstadt.jpg"));
        image.setBlending(Pixmap.Blending.None);
        image.setFilter(Pixmap.Filter.BiLinear);
        shaper = new ShapeRenderer();
        points = new float[POINTS * 2];
        pointColors = new float[POINTS * 3];
        float x, y;
        int rgba, pc = 0;
        for (int i = 0; i < POINTS; i++) {
//            points.add(new CoordDouble(rng.nextDouble(512.0), rng.nextDouble(512.0)));
            //0.7548776662466927, 0.5698402909980532
            points[i << 1] = x = (0.7548776662466927f * (i + 1) % 1f) * (image.getWidth() - 10) + DiverRNG.determineFloat(i) * 3f + 5f;
            points[i << 1 | 1] = y = (0.5698402909980532f * (i + 1) % 1f) * (image.getHeight() - 10) + DiverRNG.determineFloat(~i) * 3f + 5f;
            rgba = image.getPixel(MathUtils.roundPositive(x), image.getHeight() - 1 - MathUtils.roundPositive(y));
            pointColors[pc++] = (rgba >>> 24) / 765f;
            pointColors[pc++] = (rgba >>> 16 & 0xFF) / 765f;
            pointColors[pc++] = (rgba >>> 8 & 0xFF) / 765f;
        }
        tri = new DelaunayTriangulator();
        tris = tri.computeTriangles(points, false);
        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shaper.begin(ShapeRenderer.ShapeType.Filled);
        final int len = tris.size, m = (int) (TimeUtils.timeSinceNanos(startTime) >>> 16);
        final float time = m * 0x1p-13f;
        int a, b, c;
        float af, bf, cf;
        for (int i = 0; i < len - 2;) {
            //shaper.setColor(SColor.DAWNBRINGER_AURORA[(color++ % 255) + 1]);
            a = tris.get(i++);
            af = a * 0.125f;
            b = tris.get(i++);
            bf = b * 0.125f;
            c = tris.get(i++);
            cf = c * 0.125f;
            shaper.setColor(pointColors[a * 3] + pointColors[b * 3] + pointColors[c * 3],
                    pointColors[a * 3 + 1] + pointColors[b * 3 + 1] + pointColors[c * 3 + 1],
                    pointColors[a * 3 + 2] + pointColors[b * 3 + 2] + pointColors[c * 3 + 2],
                    1f);
            shaper.triangle(points[a << 1] + NumberTools.swayRandomized(a, time + af) * 5f,
                    points[a << 1 | 1] + NumberTools.swayRandomized(~a, time - af) * 5f,
                    points[b << 1] + NumberTools.swayRandomized(b, time + bf) * 5f,
                    points[b << 1 | 1] + NumberTools.swayRandomized(~b, time - bf) * 5f,
                    points[c << 1] + NumberTools.swayRandomized(c, time + cf) * 5f,
                    points[c << 1 | 1] + NumberTools.swayRandomized(~c, time - cf) * 5f);

//            shaper.triangle(points[a << 1],
//                    points[a << 1 | 1],
//                    points[b << 1],
//                    points[b << 1 | 1],
//                    points[c << 1],
//                    points[c << 1 | 1]);
        }
        shaper.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib/libGDX Demo: Delaunay Mosaic Test");
        config.useVsync(false);
        config.setWindowedMode(404, 600);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new DelaunayMosaicTest(), config);
    }

}
