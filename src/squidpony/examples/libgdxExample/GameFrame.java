package squidpony.examples.libgdxExample;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * The primary frame for the game.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class GameFrame extends Game {
    
    Pixmap[] icons = new Pixmap[3];

    @Override
    public void create() {
        System.out.println("Creating new game.");
        System.out.println("Working in folder: " + System.getProperty("user.dir"));
//        super.setScreen(new DisplayMaster());
    }
    
    @Override
    public void resize(int width, int height) {
        System.out.println("New size: " + width + ", " + height);
    }

    @Override
    public void render() {
        //fpsLogger.log();
        super.render();
    }

    @Override
    public void pause() {
        System.out.println("Pausing game.");
        super.pause();
    }

    @Override
    public void resume() {
        System.out.println("Resuming game.");
        super.resume();
    }

    @Override
    public void dispose() {
        System.out.println("Disposing game.");
        super.dispose();
    }
}
