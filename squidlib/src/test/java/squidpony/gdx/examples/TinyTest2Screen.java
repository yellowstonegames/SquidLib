package squidpony.gdx.examples;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidgrid.gui.gdx.SquidPanel;

/**
 * Test to diagnose SquidLib's GitHub issue #178; code is by gotoss08 .
 * This test should stay in SquidLib even after the bug is resolved, to make sure it doesn't come back.
 */
public class TinyTest2Screen implements Screen {
    private SquidPanel panel;
    private SpriteBatch batch;
    private Stage stage;

    @Override
    public void show() {
        panel = new SquidPanel(40, 40);
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);
        stage.addActor(panel);
    }

    public void clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void render(float delta) {
        clearScreen();

        stage.act(delta);
        panel.put(10, 10, '@');
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}