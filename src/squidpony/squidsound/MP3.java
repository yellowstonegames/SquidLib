package squidpony.squidsound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.sound.midi.*;
import javazoom.jl.player.Player;

/**
 * Represents a single mp3 file that can be played.
 * 
 * @author Eben Howard - http://squidpony.com
 */
public class MP3 implements Runnable {

    private String filename;
    private Player mp3Player;
    private Sequencer midiPlayer;
    private boolean isMidi;

    // constructor that takes the name of an MP3 file
    public MP3(String filename) {
        this.filename = filename;
    }

    public void close() {
        if (mp3Player != null) {
            mp3Player.close();
        }
    }

    // play the MP3 file to the sound card
    public void play() {
        System.out.println("Attempting to play " + filename);
        if (filename.endsWith(".mp3")) {
            isMidi = false;
            try {
                FileInputStream fis = new FileInputStream(filename);
                BufferedInputStream bis = new BufferedInputStream(fis);
                mp3Player = new Player(bis);

            } catch (Exception e) {
                System.out.println("Problem playing file " + filename);
                System.out.println(e);
            } catch (Error e) {
                System.out.println(e);
            }

            // run in new thread to play in background
            new Thread() {

                public void run() {
                    try {
                        mp3Player.play();
                    } catch (Exception e) {
                        System.out.println(e);
                    } catch (Error e) {
                        System.out.println(e);
                    }
                }
            }.start();
        } else if (filename.endsWith(".midi") || filename.endsWith(".mid")) {
            try {
                // From file
                Sequence sequence = MidiSystem.getSequence(new File(filename));

                // Create a sequencer for the sequence
                midiPlayer = MidiSystem.getSequencer();
                midiPlayer.open();
                midiPlayer.setSequence(sequence);

                // Start playing
                midiPlayer.start();

            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (MidiUnavailableException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (InvalidMidiDataException e) {
                System.out.println(e.getLocalizedMessage());
            }
        } else {
            System.out.println("File " + filename + " unplayable.");
        }
    }

    public boolean getComplete() {
        if (isMidi) {
            return !midiPlayer.isRunning();
        } else {
            return mp3Player.isComplete();
        }
    }

    public void run() {
        play();
    }

    public void kill() {
        if (isMidi) {
            midiPlayer.stop();
        } else {
            mp3Player.close();
        }
    }
}
