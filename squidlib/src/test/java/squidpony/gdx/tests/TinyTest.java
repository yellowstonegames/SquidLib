package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import squidpony.squidgrid.gui.gdx.SquidLayers;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class TinyTest extends ApplicationAdapter {
    private SpriteBatch batch;
    private SquidLayers layers;
    @Override
    public void create() {
        batch = new SpriteBatch();
        layers = new SquidLayers(40, 25);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        layers.put(10, 10, '@');

        batch.begin();
        layers.draw(batch, 1f);
        batch.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Tiny Test";
        config.width = 400;
        config.height = 250;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new TinyTest(), config);
    }

}