package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.TimeUtils;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.MiniMover64RNG;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedSet;

import java.util.Comparator;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class DelaunayGDXTest extends ApplicationAdapter {
    private ShapeRenderer shaper;
    private MiniMover64RNG mini;
    private DelaunayTriangulator tri;
    private float[] points;
    private ShortArray tris;
    private OrderedSet<? extends Color> palette;
    private long startTime;
    @Override
    public void create() {
        shaper = new ShapeRenderer();
        mini = new MiniMover64RNG(123);
        points = new float[510];
        for (int i = 0; i < 255; i++) {
//            points.add(new CoordDouble(rng.nextDouble(512.0), rng.nextDouble(512.0)));
            points[i << 1] = 386.4973651183067f * (i + 1) % 500f + mini.nextFloat() * 12f;
            points[i << 1 | 1] = 291.75822899100325f * (i + 1) % 500f + mini.nextFloat() * 12f;
        }
        tri = new DelaunayTriangulator();
        tris = tri.computeTriangles(points, false);
        palette = new OrderedSet<>(SColor.FULL_PALETTE);
        for (int i = palette.size() - 1; i >= 0; i--) {
            Color c = palette.getAt(i);
            if(c.a < 1f || SColor.saturation(c) < 0.3f || SColor.value(c) < 0.35f)
                palette.removeAt(i);
        }
        palette.sort(new Comparator<Color>() {
            @Override
            public int compare(Color c1, Color c2) {
                final int diff = NumberTools.floatToIntBits(SColor.hue(c1) - SColor.hue(c2));
                return (diff >> 31) | ((-diff) >>> 31); // project nayuki signum
            }
        });
        startTime = TimeUtils.millis();
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shaper.begin(ShapeRenderer.ShapeType.Filled);
        final int len = tris.size;
        int c = (int) TimeUtils.timeSinceMillis(startTime) >>> 2;
        for (int i = 0; i < len - 2;) {
            //shaper.setColor(SColor.DAWNBRINGER_AURORA[(c++ % 255) + 1]);
            shaper.setColor(palette.getAt(c++ % palette.size()));
            shaper.triangle(points[tris.get(i) << 1], points[tris.get(i++) << 1 | 1],
                    points[tris.get(i) << 1], points[tris.get(i++) << 1 | 1],
                    points[tris.get(i) << 1], points[tris.get(i++) << 1 | 1]);
        }
        shaper.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Delaunay Test";
        config.width = 512;
        config.height = 512;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DelaunayGDXTest(), config);
    }

}