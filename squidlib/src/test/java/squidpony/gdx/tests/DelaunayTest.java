package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.*;

import java.util.ArrayList;

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
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mini.setState(123L);
        shaper.begin(ShapeRenderer.ShapeType.Filled);
        final int len = tris.size();
        DelaunayTriangulator.Triangle t;
        for (int i = 0; i < len; i++) {
            shaper.setColor(SColor.DAWNBRINGER_AURORA[(i % 255) + 1]);
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
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DelaunayTest(), config);
    }

}