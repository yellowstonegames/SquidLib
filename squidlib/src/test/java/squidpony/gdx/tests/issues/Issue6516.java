package squidpony.gdx.tests.issues;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Issue6516 extends ApplicationAdapter {
    final int width = 256;
    final int height = 256;
    final float[][][] data = new float[width][height][4];
    FloatBuffer dataBuf;
    int textureHandle;
    ImmediateModeRenderer20 renderer;
    Viewport viewport;
    @Override
    public void create () {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j][0] = 0.4f;  /* r */
                data[i][j][1] = 0.38f; /* g */
                data[i][j][2] = 0.2f;  /* b */
                data[i][j][3] = 0.9f;  /* a */
            }
        }
        dataBuf = ByteBuffer.allocateDirect( Float.BYTES * 4 * width * height)
                .order(ByteOrder.LITTLE_ENDIAN) // actually quite important!
                .asFloatBuffer();
        for (float[][] dat : data) { /* Float Byte size * RGBA * width * height */
            for (float[] floats : dat) {
                dataBuf.put(floats, 0, 4);
            }
        }
        dataBuf.position(0); /* reset the caret position to the beginning of the array */
        textureHandle = Gdx.gl.glGenTexture();
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL30.GL_TEXTURE_2D, textureHandle);
        Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
        Gdx.gl.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        Gdx.gl.glTexImage2D(
                GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F,
                width, height, 0, GL30.GL_RGBA, GL30.GL_FLOAT, dataBuf
        );
        viewport = new ScreenViewport();
        renderer = new ImmediateModeRenderer20(false, false, 1);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.3f, 0.3f, 0.3f, 1.0f);
        renderer.begin(viewport.getCamera().combined, GL20.GL_TRIANGLES);
        final float low = width * -0.5f, high = width * 0.5f;
        renderer.vertex(low, low, 0f);
        renderer.texCoord(0f, 0f);
        renderer.vertex(low, high, 0f);
        renderer.texCoord(0f, 1f);
        renderer.vertex(high, high, 0f);
        renderer.texCoord(1f, 1f);
        renderer.vertex(low, low, 0f);
        renderer.texCoord(0f, 0f);
        renderer.vertex(high, high, 0f);
        renderer.texCoord(1f, 1f);
        renderer.vertex(high, low, 0f);
        renderer.texCoord(1f, 0f);
        renderer.end();

        byte[] raw = ScreenUtils.getFrameBufferPixels(false);
        System.out.printf("First four bytes: 0x%02X 0x%02X 0x%02X 0x%02X\n", raw[0], raw[1], raw[2], raw[3]);
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Issue 6516 Test");
        config.useVsync(true);
        config.setWindowedMode(256, 256);
        new Lwjgl3Application(new Issue6516(), config);
    }

}
