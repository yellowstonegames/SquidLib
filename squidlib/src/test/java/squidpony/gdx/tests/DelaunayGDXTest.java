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
            //0.7548776662466927, 0.5698402909980532
            points[i << 1] = (0.7548776662466927f * (i + 1) % 1f) * 480f + mini.nextFloat() * 16f + 8f;
            points[i << 1 | 1] = (0.5698402909980532f * (i + 1) % 1f) * 480f + mini.nextFloat() * 16f + 8f;
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
        final int len = tris.size, m = (int) (TimeUtils.timeSinceNanos(startTime) >>> 16);
        final float time = m * 0x1p-13f;
        int color = 0, a, b, c;
        float af, bf, cf;
        for (int i = 0; i < len - 2;) {
            //shaper.setColor(SColor.DAWNBRINGER_AURORA[(color++ % 255) + 1]);
            shaper.setColor(palette.getAt(color++ % palette.size()));
            a = tris.get(i++);
            af = a * 0.125f;
            b = tris.get(i++);
            bf = b * 0.125f;
            c = tris.get(i++);
            cf = c * 0.125f;
            shaper.triangle(points[a << 1] + NumberTools.swayRandomized(a, time + af) * 8f,
                    points[a << 1 | 1] + NumberTools.swayRandomized(~a, time - af) * 8f,
                    points[b << 1] + NumberTools.swayRandomized(b, time + bf) * 8f,
                    points[b << 1 | 1] + NumberTools.swayRandomized(~b, time - bf) * 8f,
                    points[c << 1] + NumberTools.swayRandomized(c, time + cf) * 8f,
                    points[c << 1 | 1] + NumberTools.swayRandomized(~c, time - cf) * 8f);
        }
        shaper.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib/libGDX Demo: Delaunay Test";
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