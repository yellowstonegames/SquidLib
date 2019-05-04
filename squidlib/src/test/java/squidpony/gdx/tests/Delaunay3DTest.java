package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.Delaunay3D;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.VanDerCorputQRNG;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class Delaunay3DTest extends ApplicationAdapter {
    private Delaunay3D tri;
    private ArrayList<Delaunay3D.Triangle> tris;
    private OrderedSet<? extends Color> palette;
    private ImmediateModeRenderer20 imr;
    private Matrix4 proj;
    private float[] vertices;
//    private Texture whiteSquare;

    @Override
    public void create() {
//        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
//        pixmap.setColor(-1);
//        pixmap.fill();
//        whiteSquare = new Texture(pixmap);

        double[] points = new double[256 * 3];
        for (int i = 0; i < 256; i++) {
//            points.add(new CoordDouble(rng.nextDouble(512.0), rng.nextDouble(512.0)));
//            points.add(new CoordDouble(386.4973651183067 * (i + 1) % 500.0 + rng.nextDouble(12.0),
//                    291.75822899100325 * (i + 1) % 500.0 + rng.nextDouble(12.0)));
            double lon = VanDerCorputQRNG.determine2(i) * Math.PI * 2.0;
            double lat = (VanDerCorputQRNG.determine(3, i) - 0.5) * Math.PI;
            double clat = NumberTools.cos(lat);
            points[i * 3] = NumberTools.cos(lon) * clat;
            points[i * 3 + 1] = NumberTools.sin(lon) * clat;
            points[i * 3 + 2] = NumberTools.sin(lat);
        }

        tri = new Delaunay3D(points);
        tris = tri.triangulate();
//        Collections.sort(tris, new Comparator<Delaunay3D.Triangle>() {
//            @Override
//            public int compare(Delaunay3D.Triangle t1, Delaunay3D.Triangle t2) {
//                return Double.compare(
//                        t1.a.flat.distanceSq(0.0, 0.0) + t1.b.flat.distanceSq(0.0, 0.0) + t1.c.flat.distanceSq(0.0, 0.0),
//                        t2.a.flat.distanceSq(0.0, 0.0) + t2.b.flat.distanceSq(0.0, 0.0) + t2.c.flat.distanceSq(0.0, 0.0)
//                );
//            }
//        });
        palette = new OrderedSet<>(SColor.FULL_PALETTE);
        for (int i = palette.size() - 1; i >= 0; i--) {
            Color c = palette.getAt(i);
            if(c.a < 1f || SColor.saturation(c) < 0.5f || SColor.value(c) < 0.35f)
                palette.removeAt(i);
        }
        palette.sort(new Comparator<Color>() {
            @Override
            public int compare(Color c1, Color c2) {
                // sorts by hue
                final int diff = NumberTools.floatToIntBits(SColor.hue(c1) - SColor.hue(c2));
                return (diff >> 31) | ((-diff) >>> 31); // project nayuki signum
            }
        });
        imr = new ImmediateModeRenderer20(false, true, 0);
        proj = new Matrix4();
        
//        tris = Maker.makeList(new Delaunay3D.Triangle(new Delaunay3D.MultiCoord(2.0, -1.0, 1.0),
//                new Delaunay3D.MultiCoord(-2.0, 0.0, 0.0),
//                new Delaunay3D.MultiCoord(1.0, 2.0, 0.0)));
        
        vertices = new float[tris.size() * 12];
        for (int i = 0; i < tris.size(); i++) {
            float c = palette.getAt(i % palette.size()).toFloatBits();
//            float c = SColor.RED.toFloatBits();
            vertices[i * 12 + 0] = (float) tris.get(i).a.x * 256f;
            vertices[i * 12 + 1] = (float) tris.get(i).a.y * 256f;
            vertices[i * 12 + 2] = (float) tris.get(i).a.z * 256f;
            vertices[i * 12 + 3] = c;
            vertices[i * 12 + 4] = (float) tris.get(i).b.x * 256f;
            vertices[i * 12 + 5] = (float) tris.get(i).b.y * 256f;
            vertices[i * 12 + 6] = (float) tris.get(i).b.z * 256f;
            vertices[i * 12 + 7] = c;
            vertices[i * 12 + 8] = (float) tris.get(i).c.x * 256f;
            vertices[i * 12 + 9] = (float) tris.get(i).c.y * 256f;
            vertices[i * 12 + 10] = (float) tris.get(i).c.z * 256f;
            vertices[i * 12 + 11] = c;
        }
    }

    @Override
    public void render() {
        proj.setToOrtho2D(-300, -300, 600, 600, -300, 300);

        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
//        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
//        Gdx.gl.glCullFace(GL20.GL_BACK);
//        
        //set the depth test function to LESS
//        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        //5. enable depth writing
//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glDepthMask(true);
        
        imr.begin(proj, GL20.GL_TRIANGLES);
        for (int i = 3; i < vertices.length; i += 4) {
            imr.color(vertices[i]);
            imr.vertex(vertices[i - 3], vertices[i - 2], vertices[i - 1]);
        }
        imr.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Delaunay Test";
        config.width = 512;
        config.height = 512;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.depth = 16;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new Delaunay3DTest(), config);
    }

}