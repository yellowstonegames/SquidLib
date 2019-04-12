package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.TimeUtils;
import squidpony.squidmath.DiverRNG;

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
    private static final int POINTS = 0x37FF;
    @Override
    public void create() {
        image = new Pixmap(Gdx.files.internal("special/Among_the_Sierra_Nevada_by_Albert Bierstadt.jpg"));
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
            points[i << 1] = x = (0.7548776662466927f * (i + 1) % 1f) * (image.getWidth() - 3) + DiverRNG.determineFloat(i) * 3f;
            points[i << 1 | 1] = y = (0.5698402909980532f * (i + 1) % 1f) * (image.getHeight() - 3) + DiverRNG.determineFloat(~i) * 3f;
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
        final int len = tris.size;
        int a, b, c;
        for (int i = 0; i < len - 2;) {
            //shaper.setColor(SColor.DAWNBRINGER_AURORA[(color++ % 255) + 1]);
            a = tris.get(i++);
            b = tris.get(i++);
            c = tris.get(i++);
            shaper.setColor(pointColors[a * 3] + pointColors[b * 3] + pointColors[c * 3],
                    pointColors[a * 3 + 1] + pointColors[b * 3 + 1] + pointColors[c * 3 + 1],
                    pointColors[a * 3 + 2] + pointColors[b * 3 + 2] + pointColors[c * 3 + 2],
                    1f);
            
            shaper.triangle(points[a << 1],
                    points[a << 1 | 1],
                    points[b << 1],
                    points[b << 1 | 1],
                    points[c << 1],
                    points[c << 1 | 1]);
        }
        shaper.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib/libGDX Demo: Delaunay Mosaic Test";
        config.width = 1279;// 404;
        config.height = 765;//600;
        config.vSyncEnabled = true;
        config.foregroundFPS = 60;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DelaunayMosaicTest(), config);
    }

}