package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import squidpony.squidmath.CoordPacker;

public class DataWriterTool extends ApplicationAdapter {
    @Override
    public void create() {
        CoordPacker.init();
        FileHandle x2 = Gdx.files.local("Hilbert2D_X.dat"), y2 = Gdx.files.local("Hilbert2D_Y.dat"),
                dist2 = Gdx.files.local("Hilbert2D_Dist.dat"),
                x3 = Gdx.files.local("Hilbert3D_X.dat"), y3 = Gdx.files.local("Hilbert3D_Y.dat"), 
                z3 = Gdx.files.local("Hilbert3D_Z.dat"), dist3 = Gdx.files.local("Hilbert3D_Dist.dat");
        byte[] axis = new byte[0x10000];
        for (int i = 0; i < 0x10000; i++) {
            axis[i] = (byte) CoordPacker.hilbertX[i];
        }
        x2.writeBytes(axis, false);
        for (int i = 0; i < 0x10000; i++) {
            axis[i] = (byte) CoordPacker.hilbertY[i];
        }
        y2.writeBytes(axis, false);
        byte[] dist = new byte[0x20000];
        for (int i = 0; i < 0x10000; i++) {
            dist[i<<1] = (byte) CoordPacker.hilbertDistances[i];
            dist[i<<1|1] = (byte) (CoordPacker.hilbertDistances[i] >> 8);
        }
        dist2.writeBytes(dist, false);
        
        axis = new byte[0x200];
        for (int i = 0; i < 0x200; i++) {
            axis[i] = (byte) CoordPacker.hilbert3X[i];
        }
        x3.writeBytes(axis, false);
        for (int i = 0; i < 0x200; i++) {
            axis[i] = (byte) CoordPacker.hilbert3Y[i];
        }
        y3.writeBytes(axis, false);
        for (int i = 0; i < 0x200; i++) {
            axis[i] = (byte) CoordPacker.hilbert3Z[i];
        }
        z3.writeBytes(axis, false);
        dist = new byte[0x400];
        for (int i = 0; i < 0x200; i++) {
            dist[i<<1] = (byte) CoordPacker.hilbert3Distances[i];
            dist[i<<1|1] = (byte) (CoordPacker.hilbert3Distances[i] >> 8);
        }
        dist3.writeBytes(dist, false);
        

    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Data!";
        config.width = 640;
        config.height = 320;
        config.foregroundFPS = 1;
        //config.vSyncEnabled = false;
        //config.fullscreen = true;
        config.backgroundFPS = -1;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DataWriterTool(), config);
    }
}
