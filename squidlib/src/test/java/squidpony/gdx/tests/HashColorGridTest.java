package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;
import squidpony.StringKit;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class HashColorGridTest extends ApplicationAdapter {
    private Texture texture, badTexture;
    private SpriteBatch batch;
    @Override
    public void create() {
        batch = new SpriteBatch();
        String hashes = Gdx.files.internal("special/WordHashes.txt").readString("UTF8");
        int length = hashes.length() / 8;
        int sideLength = (int) Math.ceil(Math.sqrt(length));
        Pixmap pixmap = new Pixmap(sideLength * 8, sideLength * 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(-1);
        pixmap.fill();
        int x = 0, y = 0;
        for (int i = 0, s = 0; i < length; i++, s += 8) {
            pixmap.setColor(StringKit.intFromHex(hashes, s, s + 8) | 0x202020FF);
            pixmap.fillRectangle(x++ * 8 + 1, y * 8 + 1, 6, 6);
            if(x >= sideLength)
            {
                x = 0;
                y++;
            }
            if(y >= sideLength) break;
        }
        texture = new Texture(pixmap);
        
        hashes = Gdx.files.internal("special/BadHashes.txt").readString("UTF8");
        pixmap = new Pixmap(sideLength * 8, sideLength * 8, Pixmap.Format.RGBA8888);
        pixmap.setColor(-1);
        pixmap.fill();
        x = 0;
        y = 0;
        for (int i = 0, s = 0; i < length; i++, s += 8) {
            pixmap.setColor(StringKit.intFromHex(hashes, s, s + 8) | 0x202020FF);
            pixmap.fillRectangle(x++ * 8 + 1, y * 8 + 1, 6, 6);
            if(x >= sideLength)
            {
                x = 0;
                y++;
            }
            if(y >= sideLength) break;
        }
        badTexture = new Texture(pixmap);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        if((TimeUtils.millis() & 1023) < 511) 
        {
            Gdx.graphics.setTitle("CrossHash.Hive");
            batch.draw(texture, 24f, 24f);
        }
        else 
        {
            Gdx.graphics.setTitle("String.hashCode()");
            batch.draw(badTexture, 24f, 24f);
        }
        batch.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Word Hash Color Test";
        config.width = 950;
        config.height = 950;
        config.vSyncEnabled = true;
        config.foregroundFPS = 10;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new HashColorGridTest(), config);
    }

}