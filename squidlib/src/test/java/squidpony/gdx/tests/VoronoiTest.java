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
public class VoronoiTest extends ApplicationAdapter {
    private ShapeRenderer shaper;
    private MiniMover64RNG mini;
    private RNG rng;
    private Voronoi voronoid;
    private OrderedSet<CoordDouble> points;
    private ArrayList<Voronoi.Polygon> polygons;
    private ArrayList<Voronoi.Triangle> triangles;
    private OrderedSet<? extends Color> palette;
    private long startTime;

    @Override
    public void create() {
        shaper = new ShapeRenderer();
        mini = new MiniMover64RNG(123);
        rng = new RNG(mini);
        final int COUNT = 20;
        points = new OrderedSet<>(COUNT);
        for (int i = 0; i < COUNT; i++) {
//            points.add(new CoordDouble(rng.nextDouble(512.0), rng.nextDouble(512.0)));
            points.add(new CoordDouble(386.4973651183067 * (i + 1) % 500.0 + rng.nextDouble(12.0),
                    291.75822899100325 * (i + 1) % 500.0 + rng.nextDouble(12.0)));
        }
        voronoid = new Voronoi(points);
        polygons = voronoid.polygonize();
        triangles = voronoid.getTriangles();
        Collections.sort(polygons, new Comparator<Voronoi.Polygon>() {
            @Override
            public int compare(Voronoi.Polygon p1, Voronoi.Polygon p2) {
                return Double.compare(
                        p1.vertices[0].distanceSq(256.0, 256.0) + p1.vertices[2].distanceSq(256.0, 256.0),
                        p2.vertices[0].distanceSq(256.0, 256.0) + p2.vertices[2].distanceSq(256.0, 256.0)
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
                return (diff >> 31 | -diff >>> 31); // project nayuki signum
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
        final int len = polygons.size();
        Voronoi.Polygon p;
        int c = (int) TimeUtils.timeSinceMillis(startTime) >>> 5;
        for (int i = 0; i < len; i++) {
            //shaper.setColor(SColor.DAWNBRINGER_AURORA[(i % 255) + 1]);
//            shaper.setColor(palette.getAt(c++ % palette.size()));
            float color = rng.nextFloat() * 0.7f + 0.15f;
            shaper.setColor(color, color, color, 1f);
            p = polygons.get(i);
            for (int j = 1; j < p.vertices.length; j++) {
                shaper.triangle((float)p.centroid.x, (float)p.centroid.y,
                        (float)p.vertices[j-1].x, (float)p.vertices[j-1].y,
                        (float)p.vertices[j].x, (float)p.vertices[j].y);
            }
            shaper.triangle((float)p.centroid.x, (float)p.centroid.y,
                    (float)p.vertices[p.vertices.length-1].x, (float)p.vertices[p.vertices.length-1].y,
                    (float)p.vertices[0].x, (float)p.vertices[0].y);
        }
        shaper.end();
        shaper.begin(ShapeRenderer.ShapeType.Line);
        shaper.setColor(SColor.RED);
        Voronoi.Triangle t;
        for (int i = 0; i < triangles.size(); i++) {
            t = triangles.get(i);
            shaper.triangle((float)t.a.x, (float)t.a.y,
                    (float)t.b.x, (float)t.b.y,
                    (float)t.c.x, (float)t.c.y);
        }
        shaper.end();
        shaper.begin(ShapeRenderer.ShapeType.Point);
        shaper.setColor(SColor.WHITE);
        for (int i = 0; i < points.size(); i++) {
            CoordDouble cd = points.getAt(i);
            shaper.point((float) cd.x, (float)cd.y, 0f);
        }
        shaper.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Delaunay Test";
        config.width = 512;
        config.height = 512;
        config.vSyncEnabled = true;
        config.foregroundFPS = 5;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new VoronoiTest(), config);
    }

}
