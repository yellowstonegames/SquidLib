package squidpony.squidsound;

import java.io.File;

/**
 * Creates a media player that sits in an unattached JFXPanel to allow audio in
 * a Swing application using the JavaFX audio framework.
 */
public class JavaFXMedia {

    public static void main(String[] args) {
        SoundManager sm = new SoundManager();
        sm.loadMediaResources(new File("./"), true);
        sm.playMusic("test");
    }
}
