package squidpony.examples.libgdxExample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Singleton class which controls sound output.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SoundManager {

    static private SoundManager instance = new SoundManager();
    private boolean musicLoaded = false, soundfxLoaded = false;
    private TreeMap<String, Sound> soundMap = new TreeMap<>();
    private TreeMap<String, Music> musicMap = new TreeMap<>();
    private Music nowPlaying;
    private List<String> readableFileTypes = Arrays.asList(new String[]{"wav", "mp3", "ogg"});

    private SoundManager() {
        if (EnvironmentalVariables.isSoundfxOn()) {
            loadSoundFXResources();
        }
        if (EnvironmentalVariables.isMusicOn()) {
            loadMusicResources();
        }
    }

    private void loadSoundFXResources() {
        for (File f : (new File("./assets/sound fx/")).listFiles()) {
            String name = f.getName();
            if (readableFileTypes.contains(name.substring(name.lastIndexOf('.') + 1))) {
                Sound sound = Gdx.audio.newSound(Gdx.files.local("./assets/sound fx/" + name));
                soundMap.put(f.getName().substring(0, name.lastIndexOf('.')), sound);
            }
        }

        soundfxLoaded = true;
    }

    private void unloadSoundFXResources() {
        if (!soundfxLoaded) {
            return;//no sound fx loaded so nothing to unload
        }

        for (Sound s : soundMap.values()) {
            s.stop();
            s.dispose();
        }

        soundfxLoaded = false;
    }

    /**
     * Plays the sound fx associated with the provided key. The key is the
     * filename of the sound without its extension.
     *
     * @param key
     */
    public void playSoundFX(String key) {
        if (!EnvironmentalVariables.isSoundfxOn()) {
            return;//don't do anything if the sound effects are off
        }

        if (!soundfxLoaded) {
            loadSoundFXResources();
        }

        Sound temp = (soundMap.get(key));
        if (temp == null) {
            return;//track not found, continue current music selection
        }

        temp.play(EnvironmentalVariables.getSoundfxVolume());
    }

    private void loadMusicResources() {
        for (File f : (new File("./assets/music")).listFiles()) {
            String name = f.getName();
            if (readableFileTypes.contains(name.substring(name.lastIndexOf('.') + 1))) {
                Music music = Gdx.audio.newMusic(Gdx.files.local("./assets/music/" + name));
                music.setLooping(true);
                musicMap.put(f.getName().substring(0, name.lastIndexOf('.')), music);
            }
        }

        musicLoaded = true;
    }

    private void unloadMusicResources() {
        if (!musicLoaded) {
            return;//no music loaded so nothing to unload
        }

        if (nowPlaying != null) {
            nowPlaying = null;
        }

        for (Music m : musicMap.values()) {
            m.stop();
            m.dispose();
        }

        musicLoaded = false;
    }

    /**
     * Plays the music associated with the key. Keys for music are the filenames
     * of the tracks without their extension.
     *
     * @param key
     */
    public void playMusic(String key) {
        if (!EnvironmentalVariables.isMusicOn()) {
            stopMusic();
            return;//don't do anything if the music is off
        }

        if (!musicLoaded) {
            loadMusicResources();
        }

        Music temp = (musicMap.get(key));
        if (temp == null) {
            return;//track not found, continue current music selection
        }

        if (nowPlaying != temp) {
            if (nowPlaying != null) {
                nowPlaying.stop();
            }
            nowPlaying = temp;
        }

        nowPlaying.setVolume(EnvironmentalVariables.getMusicVolume());
        nowPlaying.setLooping(true);
        if (!nowPlaying.isPlaying()) {
            nowPlaying.play();
        }
    }

    /**
     * Stops the currently playing music.
     */
    public void stopMusic() {
        if (nowPlaying != null && nowPlaying.isPlaying()) {
            nowPlaying.stop();
        }
    }

    /**
     * Sets the music to play at the provided volume, with 0 being off and 1
     * being full volume.
     *
     * @param volume
     */
    public void setMusicVolume(float volume) {
        volume = Math.max(volume, 0f);
        volume = Math.min(volume, 1.0f);

        if (musicLoaded) {
            if (volume < 0.001) {
                unloadMusicResources();//unload if volume set to effectively zero
                EnvironmentalVariables.setMusicOn(false);
            } else if (nowPlaying != null) {
                nowPlaying.setVolume(volume);
            }
        }

        EnvironmentalVariables.setMusicVolume(volume);
    }

    public void dispose() {
        unloadMusicResources();
        unloadSoundFXResources();
    }

    static public SoundManager getInstance() {
        return instance;
    }
}
