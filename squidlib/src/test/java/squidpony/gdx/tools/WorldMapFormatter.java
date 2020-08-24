package squidpony.gdx.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

public class WorldMapFormatter extends ApplicationAdapter {
    @Override
    public void create() {
        Pixmap pix = new Pixmap(Gdx.files.internal("special/Earth_map.png"));
        final int bigWidth = pix.getWidth() / 4, bigHeight = pix.getHeight() / 4;
        GreasedRegion basis = new GreasedRegion(bigWidth, bigHeight);
        for (int x = 0; x < bigWidth; x++) {
            for (int y = 0; y < bigHeight; y++) {
                if(pix.getPixel(x * 4, y * 4) < 0) // only counts every fourth row and every fourth column
                    basis.insert(x, y);
            }
        }
        FileHandle handle = Gdx.files.local("Earth_map.txt");
        handle.writeString(basis.toCompressedString(), false, "UTF-16");
        pix.dispose();
        System.out.println(basis.equals(GreasedRegion.decompress(handle.readString("UTF-16"))));
        Gdx.app.exit();
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Map Stuff!");
        config.setWindowedMode(512, 256);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new WorldMapFormatter(), config);
    }
}
