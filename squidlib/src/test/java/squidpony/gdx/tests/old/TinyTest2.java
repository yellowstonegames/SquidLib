package squidpony.gdx.tests.old;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Test to diagnose SquidLib's GitHub issue #178; code is by gotoss08 .
 * This test should stay in SquidLib even after the bug is resolved, to make sure it doesn't come back.
 */
public class TinyTest2 extends Game {

    @Override
    public void create() {
        setScreen(new TinyTest2Screen());
    }

    @Override
    public void render() {
        getScreen().render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
        getScreen().dispose();
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.width = 640;
        config.height = 480;
        config.forceExit = false;

        new LwjglApplication(new TinyTest2(), config);
    }

}
