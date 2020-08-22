package squidpony.gdx.tests.old;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

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
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setWindowedMode(640, 480);
        new Lwjgl3Application(new TinyTest2(), config);
    }

}
