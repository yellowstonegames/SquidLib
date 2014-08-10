package squidpony.examples.libgdxExample;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Starts up the program, loading resources and starting event creators and
 * listeners as appropriate.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Example {

    public static void main(String... args) {
        //start independant creators
        System.out.println("Loading...");
        //read in all external data files
        System.out.println("Files loaded!");

        //start independant listeners

        //load and initialize resources

        //initialize the display

        //initialize the world

        //start dependant creators

        //start dependant listeners

        //hand control over to the display
        String title = EnvironmentalVariables.getGameTitle();
        int width = EnvironmentalVariables.getScreenWidth(), height = EnvironmentalVariables.getScreenHeight();
        ApplicationListener frame = new GameFrame();
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = width;
        config.height = height;
        config.title = title;
        config.addIcon("./assets/images/icons/logo128.png", Files.FileType.Internal);
        config.addIcon("./assets/images/icons/logo32.png", Files.FileType.Internal);
        config.addIcon("./assets/images/icons/logo16.png", Files.FileType.Internal);
        LwjglApplication app = new LwjglApplication(frame, config);

    }
}
