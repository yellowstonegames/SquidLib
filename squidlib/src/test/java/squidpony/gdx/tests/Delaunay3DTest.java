package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.Delaunay3D;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.VanDerCorputQRNG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class Delaunay3DTest extends ApplicationAdapter {
    private Mesh mesh;
    private Delaunay3D tri;
    private ArrayList<Delaunay3D.Triangle> tris;
    private OrderedSet<? extends Color> palette;
    private ImmediateModeRenderer20 imr;
    private ShaderProgram shader;
    private OrthographicCamera camera;
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
        Collections.sort(tris, new Comparator<Delaunay3D.Triangle>() {
            @Override
            public int compare(Delaunay3D.Triangle t1, Delaunay3D.Triangle t2) {
                return Double.compare(
                        t1.a.flat.distanceSq(0.0, 0.0) + t1.b.flat.distanceSq(0.0, 0.0) + t1.c.flat.distanceSq(0.0, 0.0),
                        t2.a.flat.distanceSq(0.0, 0.0) + t2.b.flat.distanceSq(0.0, 0.0) + t2.c.flat.distanceSq(0.0, 0.0)
                );
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
                // sorts by hue
                final int diff = NumberTools.floatToIntBits(SColor.hue(c1) - SColor.hue(c2));
                return (diff >> 31) | ((-diff) >>> 31); // project nayuki signum
            }
        });
        shader = new ShaderProgram("attribute vec3 a_position;\n" +
                "attribute vec4 a_color;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "void main() {\n" +
                "  v_color = a_color;\n" +
                "  gl_Position = u_projTrans * vec4(a_position, 0.0);\n" +
                "}\n",
                "varying vec4 v_color;\n" +
                        "void main(){\n" +
                        "  gl_FragColor = v_color;\n" +
                        "}");
        imr = new ImmediateModeRenderer20(25000, false, true, 0, shader);
        camera = new OrthographicCamera(512, 512);
        mesh = new Mesh(true, tris.size() * 3, 256, VertexAttribute.Position(), VertexAttribute.ColorPacked());
        float[] fp = new float[tris.size() * 12];
        //short[] sp = new short[256];
        for (int i = 0; i < tris.size(); i++) {
            float c = palette.getAt(i % palette.size()).toFloatBits();
            fp[i * 12 + 0] = (float) tris.get(i).a.x * 256f + 256f;
            fp[i * 12 + 1] = (float) tris.get(i).a.y * 256f + 256f;
            fp[i * 12 + 2] = (float) tris.get(i).a.z * 256f + 256f;
            fp[i * 12 + 3] = c;
            fp[i * 12 + 4] = (float) tris.get(i).b.x * 256f + 256f;
            fp[i * 12 + 5] = (float) tris.get(i).b.y * 256f + 256f;
            fp[i * 12 + 6] = (float) tris.get(i).b.z * 256f + 256f;
            fp[i * 12 + 7] = c;
            fp[i * 12 + 8] = (float) tris.get(i).c.x * 256f + 256f;
            fp[i * 12 + 9] = (float) tris.get(i).c.y * 256f + 256f;
            fp[i * 12 + 10] = (float) tris.get(i).c.z * 256f + 256f;
            fp[i * 12 + 11] = c;
            //sp[i] = (short)i;
        }
        mesh.setVertices(fp);
        //mesh.setIndices(sp);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClearDepthf(1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        
        //set the depth test function to LESS
        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        //5. enable depth writing
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        
        shader.begin();
        //whiteSquare.bind(0);
        camera.update();
        shader.setUniformMatrix("u_projTrans", camera.combined);
        mesh.render(shader, GL20.GL_TRIANGLES, 0, mesh.getNumVertices());
        shader.end();
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