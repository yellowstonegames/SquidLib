package squidpony.squidsound;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Timer;

/**
 * Class that controls the playing of an mp3.
 * 
 * @author Eben Howard - http://squidpony.com
 */
public class PlayMP3 {
    static Map<Integer, String> TITLES = new TreeMap<Integer, String>();
    static public List<Integer> RATINGS = new LinkedList<Integer>();
    static MP3 mp3;
    static PlayMP3 instance = new PlayMP3();
    static Timer timer;
    static ActionListener musicListener;
    static private boolean playing;
    static public boolean SOUND_ON = true;

    public static PlayMP3 getInstance() {
        return instance;
    }

    private PlayMP3() {
        File folder = new File("mix");
        int rating;
        for (String s : folder.list()) {
            if (s.endsWith(".mp3")) {
                rating = Integer.parseInt(String.valueOf(s.substring(0, 2)));
//            System.out.println("" + rating + ", " + s);
                TITLES.put(rating, s);
                RATINGS.add(rating);
            }
        }
        musicListener = new MusicListener();
        playing = false;
        timer = new Timer(50, musicListener);
    }

    public String playSong(int index) {
        if (!SOUND_ON) {
            return null;//don't play anything if the SOUND_ON is off
        }
        if (!timer.isRunning()) {
            timer.start();
        }
        for (int i = index; i >= 0; i--) {//count down to find last valid track
            if (TITLES.containsKey(i)) {
                index = i;
                break;
            }
        }
        String title = "mix/" + TITLES.get(index);
        if (mp3 != null) {
            mp3.kill();
        } else {
            timer.start();
        }
        mp3 = new MP3(title);
        new Thread(mp3).start();
        return ("Now playing: " + TITLES.get(index));
    }

//    public String shuffleSong() {
//        int rating = RLMath.RNG.nextInt(RATINGS.size());
//        playing = true;
//        return playSong(rating);
//    }

    public void stopSong() {
        if (mp3 != null) {
            mp3.kill();
        }
        playing = false;
    }

    class MusicListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (SOUND_ON && playing && mp3 != null && mp3.getComplete()) {
                mp3.play();
            }
        }
    }
}
