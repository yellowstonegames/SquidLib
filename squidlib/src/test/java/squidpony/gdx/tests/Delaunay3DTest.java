package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.TimeUtils;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.*;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class Delaunay3DTest extends ApplicationAdapter {
    private static final int SIZE = 1024;
    private IndexedDelaunayTriangulator tri;
//    private ArrayList<Delaunay3D.Triangle> tris;
    private OrderedSet<? extends Color> palette;
    private ImmediateModeRenderer20 imr;
    private Matrix4 proj;
    private float[] vertices;
    private float[] centroids;
    private int[] connections;
    private int connector = 0;
    private long startTime;
//    private Texture whiteSquare;

    @Override
    public void create() {
        startTime = TimeUtils.millis();
//        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
//        pixmap.setColor(-1);
//        pixmap.fill();
//        whiteSquare = new Texture(pixmap);

        float[] points = new float[SIZE * 3];
        double[] pairs = new double[SIZE * 2];
        float[] lon_clat = new float[SIZE * 2];
//        short[] links = new short[SIZE * 3];
        for (int i = 0; i < SIZE; i++) {
//            points.add(new CoordDouble(rng.nextDouble(512.0), rng.nextDouble(512.0)));
//            points.add(new CoordDouble(386.4973651183067 * (i + 1) % 500.0 + rng.nextDouble(12.0),
//                    291.75822899100325 * (i + 1) % 500.0 + rng.nextDouble(12.0)));
            double lon = (VanDerCorputQRNG.determine2(i) * Math.PI * 2.0);
            double lat = ((VanDerCorputQRNG.determine(3, i) - 0.5) * Math.PI);
//            lon_lat[i * 2] = lon;
//            lon_lat[i * 2 + 1] = lat;
            double clat = NumberTools.cos(lat);
            lon_clat[i * 2] = (float) lon;
            lon_clat[i * 2 + 1] = (float) clat;
            double x, y, z;
            points[i * 3] = (float) (x = NumberTools.cos(lon) * clat);
            points[i * 3 + 1] = (float) (y = NumberTools.sin(lon) * clat);
            points[i * 3 + 2] = (float) (z = NumberTools.sin(lat));
            pairs[i * 2] = x / (1.0 - z);
            pairs[i * 2 + 1] = y / (1.0 - z);
        }

//        tri = new Delaunay3D(points);
//        tris = tri.triangulate();
        tri = new IndexedDelaunayTriangulator();
        IntVLA triangles = tri.computeTriangles(pairs, false);
        int[] triangleArray = triangles.items;
        long[] edges = new long[triangles.size];
        connections = new int[triangles.size];
        Arrays.fill(connections, -1);
        centroids = new float[triangles.size];
        long a, b, c;
        for (int i = 0; i < triangles.size; i+=3) {
            centroids[i] = (points[triangleArray[i]] + points[triangleArray[i+1]] + points[triangleArray[i+2]]) * 256f / 3f;
            centroids[i+1] = (points[triangleArray[i]+1] + points[triangleArray[i+1]+1] + points[triangleArray[i+2]+1]) * 256f / 3f;
            centroids[i+2] = (points[triangleArray[i]+2] + points[triangleArray[i+1]+2] + points[triangleArray[i+2]+2]) * 256f / 3f;
            a = triangleArray[i] & 0xFFFFFFFFL;
            b = triangleArray[i+1] & 0xFFFFFFFFL;
            c = triangleArray[i+2] & 0xFFFFFFFFL;
            edges[i]   = (a > b) ? a << 32 | b : b << 32 | a;
            edges[i+1] = (b > c) ? b << 32 | c : c << 32 | b;
            edges[i+2] = (c > a) ? c << 32 | a : a << 32 | c;
        }
        OUTER:
        for (int i = 0; i < edges.length; i++) {
            a = edges[i];
            for (int j = i+1; j < edges.length; j++) {
                if(edges[j] == a)
                {
                    connections[i] = (j / 3) * 3;
                    connections[j] = (i / 3) * 3;
                    continue OUTER;
                }
            }
        }
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
            Color color = palette.getAt(i);
            if(color.a < 1f || SColor.saturation(color) < 0.5f || SColor.value(color) < 0.4f)
                palette.removeAt(i);
        }
        palette.sort(new Comparator<Color>() {
            @Override
            public int compare(Color c1, Color c2) {
                // sorts by hue
                final int diff = NumberTools.floatToIntBits(SColor.hue(c1) - SColor.hue(c2));
                return (diff >> 31 | -diff >>> 31); // project nayuki signum
            }
        });
        imr = new ImmediateModeRenderer20(30000, false, true, 0);
        proj = new Matrix4();
        
//        tris = Maker.makeList(new Delaunay3D.Triangle(new Delaunay3D.MultiCoord(2.0, -1.0, 1.0),
//                new Delaunay3D.MultiCoord(-2.0, 0.0, 0.0),
//                new Delaunay3D.MultiCoord(1.0, 2.0, 0.0)));
        
//        vertices = new float[tris.size() * 12];
//        for (int i = 0; i < tris.size(); i++) {
//            float c = palette.getAt(i % palette.size()).toFloatBits();
//            vertices[i * 12 + 0] = (float) tris.get(i).a.x * 256f;
//            vertices[i * 12 + 1] = (float) tris.get(i).a.y * 256f;
//            vertices[i * 12 + 2] = (float) tris.get(i).a.z * 256f;
//            vertices[i * 12 + 3] = c;
//            vertices[i * 12 + 4] = (float) tris.get(i).b.x * 256f;
//            vertices[i * 12 + 5] = (float) tris.get(i).b.y * 256f;
//            vertices[i * 12 + 6] = (float) tris.get(i).b.z * 256f;
//            vertices[i * 12 + 7] = c;
//            vertices[i * 12 + 8] = (float) tris.get(i).c.x * 256f;
//            vertices[i * 12 + 9] = (float) tris.get(i).c.y * 256f;
//            vertices[i * 12 + 10] = (float) tris.get(i).c.z * 256f;
//            vertices[i * 12 + 11] = c;
//        }
        vertices = new float[(triangles.size / 3) * 10];
        int idx;
        for (int t = 2, i = 0; t < triangles.size; t+=3) {
            vertices[i++] = palette.getAt(t % palette.size()).toFloatBits();
            idx = triangles.get(t-2);
//            vertices[i++] = points[idx*3] * 256f;
//            vertices[i++] = points[idx*3+1] * 256f;
//            vertices[i++] = points[idx*3+2] * 256f;
            vertices[i++] = lon_clat[idx*2];//points[idx] * 256f;
            vertices[i++] = lon_clat[idx*2+1] * 256f;//points[idx+1] * 256f;
            vertices[i++] = points[idx*3+2] * 256f;
            idx = triangles.get(t-1);
//            vertices[i++] = points[idx*3] * 256f;
//            vertices[i++] = points[idx*3+1] * 256f;
//            vertices[i++] = points[idx*3+2] * 256f;
            vertices[i++] = lon_clat[idx*2];//points[idx] * 256f;
            vertices[i++] = lon_clat[idx*2+1] * 256f;//points[idx+1] * 256f;
            vertices[i++] = points[idx*3+2] * 256f;
            idx = triangles.get(t);
//            vertices[i++] = points[idx*3] * 256f;
//            vertices[i++] = points[idx*3+1] * 256f;
//            vertices[i++] = points[idx*3+2] * 256f;
            vertices[i++] = lon_clat[idx*2];//points[idx] * 256f;
            vertices[i++] = lon_clat[idx*2+1] * 256f;//points[idx+1] * 256f;
            vertices[i++] = points[idx*3+2] * 256f;
        }
    }

    @Override
    public void render() {
        Gdx.graphics.setTitle("Delaunay Triangulation at " + Gdx.graphics.getFramesPerSecond() + "FPS");
        proj.setToOrtho2D(-300, -300, 600, 600, -300, 300);

        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        // to be clear, I have no idea how all this works.
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        Gdx.gl.glDepthFunc(GL20.GL_LESS);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        
        imr.begin(proj, GL20.GL_TRIANGLES);
        float lonA, clatA, xA, yA, zA,
                lonB, clatB, xB, yB, zB,
                lonC, clatC, xC, yC, zC,
                xCen, yCen, zCen,
                xM, yM, zM,
                xN, yN, zN,
                xCr, yCr, zCr,
//                color = SColor.CW_RICH_GREEN.toFloatBits();
                time = TimeUtils.timeSinceMillis(startTime) * 0.0006f;
        for (int i = 9; i < vertices.length; i += 10) {
//            zA = vertices[i - 8];
//            xA = vertices[i - 7];
//            yA = vertices[i - 6];
//            zB = vertices[i - 5];
//            xB = vertices[i - 4];
//            yB = vertices[i - 3];
//            zC = vertices[i - 2];
//            xC = vertices[i - 1];
//            yC = vertices[i];

            lonA = vertices[i - 8] + time;
            clatA = vertices[i - 7];
            zA = MathUtils.sin(lonA) * clatA;
            xA = MathUtils.cos(lonA) * clatA;
            yA = vertices[i - 6];
            lonB = vertices[i - 5] + time;
            clatB = vertices[i - 4];
            zB = MathUtils.sin(lonB) * clatB;
            xB = MathUtils.cos(lonB) * clatB;
            yB = vertices[i - 3];
            lonC = vertices[i - 2] + time;
            clatC = vertices[i - 1];
            zC = MathUtils.sin(lonC) * clatC;
            xC = MathUtils.cos(lonC) * clatC;
            yC = vertices[i];

            // get centroid
            xCen = (xA + xB + xC) / 3f;
            yCen = (yA + yB + yC) / 3f;
            zCen = (zA + zB + zC) / 3f;
            ////don't need to normalize I guess?
//            norm = 1f / (float) Math.sqrt(xCen * xCen + yCen * yCen + zCen * zCen);
//            xCen *= norm;
//            yCen *= norm;
//            zCen *= norm;

            // manual cross product
            xM = xA - xB;
            yM = yA - yB;
            zM = zA - zB;

            xN = xB - xC;
            yN = yB - yC;
            zN = zB - zC;

            xCr = yM * zN - zM * yN;
            yCr = zM * xN - xM * zN;
            zCr = xM * yN - yM * xN;
            ////don't need to normalize I guess?
//            norm = 1f / (float) Math.sqrt(xCr * xCr + yCr * yCr + zCr * zCr);
//            xCr *= norm;
//            yCr *= norm;
//            zCr *= norm;

            if (xCen * xCr + yCen * yCr + zCen * zCr >= 0f) {
                imr.color(vertices[i - 9]);
//                imr.color(color);
                imr.vertex(xA, yA, zA);
                imr.color(vertices[i - 9]);
//                imr.color(color);
                imr.vertex(xB, yB, zB);
                imr.color(vertices[i - 9]);
//                imr.color(color);
                imr.vertex(xC, yC, zC);
            }
            else 
            {
                imr.color(vertices[i - 9]);
//                imr.color(color);
                imr.vertex(xA, yA, zA);
                imr.color(vertices[i - 9]);
//                imr.color(color);
                imr.vertex(xC, yC, zC);
                imr.color(vertices[i - 9]);
//                imr.color(color);
                imr.vertex(xB, yB, zB);
            }
        }
        imr.end();
//        imr.begin(proj, GL20.GL_LINES);
//        float startColor = SColor.AURORA_EMBERS.toFloatBits(), endColor = SColor.CW_BRIGHT_AZURE.toFloatBits();
////        connector = ((int) (Math.random() * connections.length) / 3) * 3;
//        imr.color(startColor);
//        imr.vertex(centroids[connector], centroids[connector+1], centroids[connector+2]);
//        int conn = connections[connector] + MathUtils.random(2);
//        if(conn != -1)
//        {
//            connector = conn;
//        }
//        else 
//        {
//            connector = ((int) (Math.random() * connections.length) / 3) * 3;
//        }
//        imr.color(endColor);
//        imr.vertex(centroids[connector], centroids[connector+1], centroids[connector+2]);
//        imr.end();
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
