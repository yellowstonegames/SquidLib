package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class DelaunayTest extends ApplicationAdapter {
    private ShapeRenderer shaper;
    private MiniMover64RNG mini;
    private RNG rng;
    private DelaunayTriangulator tri;
    private OrderedSet<CoordDouble> points;
    private ArrayList<DelaunayTriangulator.Triangle> tris;
    private OrderedSet<? extends Color> palette;
    private long startTime;

    @Override
    public void create() {
        shaper = new ShapeRenderer();
        mini = new MiniMover64RNG(123);
        rng = new RNG(mini);
        points = new OrderedSet<>(255);
        for (int i = 0; i < 255; i++) {
//            points.add(new CoordDouble(rng.nextDouble(512.0), rng.nextDouble(512.0)));
            points.add(new CoordDouble(386.4973651183067 * (i + 1) % 500.0 + rng.nextDouble(12.0),
                    291.75822899100325 * (i + 1) % 500.0 + rng.nextDouble(12.0)));
        }
        tri = new DelaunayTriangulator(points);
        tris = tri.triangulate();
        Collections.sort(tris, new Comparator<DelaunayTriangulator.Triangle>() {
            @Override
            public int compare(DelaunayTriangulator.Triangle t1, DelaunayTriangulator.Triangle t2) {
                return Double.compare(
                        t1.a.distanceSq(256.0, 256.0) + t1.b.distanceSq(256.0, 256.0) + t1.c.distanceSq(256.0, 256.0),
                        t2.a.distanceSq(256.0, 256.0) + t2.b.distanceSq(256.0, 256.0) + t2.c.distanceSq(256.0, 256.0)
                );
//                return Double.compare(t1.a.x + t1.b.x + t1.c.x, t2.a.x + t2.b.x + t2.c.x); 
            }
        });
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
        final int len = tris.size();
        DelaunayTriangulator.Triangle t;
        int c = (int) TimeUtils.timeSinceMillis(startTime) >>> 2;
        for (int i = 0; i < len; i++) {
            //shaper.setColor(SColor.DAWNBRINGER_AURORA[(i % 255) + 1]);
            shaper.setColor(palette.getAt(c++ % palette.size()));
            t = tris.get(i);
            shaper.triangle((float)t.a.x, (float)t.a.y, (float)t.b.x, (float)t.b.y, (float)t.c.x, (float)t.c.y);
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
        new LwjglApplication(new DelaunayTest(), config);
    }

}