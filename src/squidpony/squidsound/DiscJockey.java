package squidpony.squidsound;

import java.util.TreeMap;
import javazoom.jl.player.Player;

/**
 * Singleton class for playing music and sound effects.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class DiscJockey {
    Player player;
    TreeMap<String, MP3> tracks;

    private DiscJockey() {
    }
}
