package squidpony.squidsound;

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

/**
 * Simplified class for working with sound output.
 *
 * If fading is used, this class should be wrapped in a Thread to allow automatic volume adjustments
 * as needed.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SoundManager implements Runnable {

    final static private String validExtensions = "aif|aiff|fxm|flv|m3u8|mp3|mp4|m4a|m4v|wav";
    private static boolean fxInitialized = false;

    public volatile float maxMusicVolume = 0.7f, musicVolume = 0.7f, soundfxVolume = 0.7f, fadeVolume = 0.7f;
    private boolean fading = false;
    private TreeMap<String, AudioClip> clipMap = new TreeMap<>();
    private TreeMap<String, Media> mediaMap = new TreeMap<>();
    private Media nowPlaying;
    private MediaPlayer player;

    public SoundManager() {
        if (!fxInitialized) {
            JFXPanel fxPanel = new JFXPanel();//needed only to initialize the Platform
            fxInitialized = true;
        }
    }

    public boolean isFading() {
        return fading;
    }

    /**
     *
     * @param directory
     * @param isMusic
     */
    public void loadMediaResources(File directory, boolean isMusic) {
        for (String fileName : directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(".*\\.(" + validExtensions + ")", name);
            }
        })) {
            File file = new File(directory.getName() + "\\" + fileName);
            String key = fileName.substring(0, fileName.lastIndexOf('.'));
            loadMediaFile(key, file, isMusic);
        }
    }

    public void loadMediaFile(String key, File file, boolean isMusic) {
        String uri = file.toURI().toString();
            System.out.println("loading: " + file.toURI().toString() + " keyed as " + key);
        if (isMusic) {
            mediaMap.put(key, new Media(uri));
        } else {
            clipMap.put(key, new AudioClip(uri));
        }
    }

    public void fadeOut() {
        fadeVolume = musicVolume;
        fading = true;
    }

    /**
     * Plays the sound fx associated with the provided key. The key is the filename of the sound
     * without its extension.
     *
     * @param key
     */
    public void playSoundFX(String key) {
        if (soundfxVolume > 0) {
            AudioClip temp = (clipMap.get(key));
            if (temp == null) {
                return;//track not found, continue current music selection
            }

            temp.play(soundfxVolume);
        }
    }

    /**
     * Plays the music associated with the key. Keys for music are the filenames of the tracks
     * without their extension.
     *
     * @param key
     */
    public void playMusic(String key) {
        if (musicVolume > 0) {
            Media temp = (mediaMap.get(key));
            if (temp == null) {
                System.out.println("Track key not found: " + key);
                return;//track not found, continue current music selection
            }

            if (nowPlaying != temp) {
                if (nowPlaying != null && player != null) {
                    player.stop();
                }
                nowPlaying = temp;
            }

            player = new MediaPlayer(temp);
            player.setVolume(musicVolume);
            player.setCycleCount(MediaPlayer.INDEFINITE);//set to loop
            if (player.getStatus() != Status.PLAYING) {
                player.play();
            }
        } else {
            stopMusic();
            return;
        }
    }

    /**
     * Stops the currently playing music.
     */
    public void stopMusic() {
        if (player != null && player.getStatus() == Status.PLAYING) {
            player.stop();
        }
    }

    /**
     * Sets the music to play at the provided volume, with 0 being off and 1 being full volume.
     *
     * @param volume
     */
    public void setMusicVolume(float volume) {
        volume = Math.max(volume, 0f);
        volume = Math.min(volume, 1.0f);

        if (player != null) {
            player.setVolume(volume);
        }

        musicVolume = volume;
    }

    public void setSoundFXVolume(float volume) {
        volume = Math.max(volume, 0f);
        volume = Math.min(volume, 1.0f);

        soundfxVolume = volume;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (fading) {
                fadeVolume -= .01;
                setMusicVolume(fadeVolume);
                if (fadeVolume < 0.001) {
                    fading = false;
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }

}
