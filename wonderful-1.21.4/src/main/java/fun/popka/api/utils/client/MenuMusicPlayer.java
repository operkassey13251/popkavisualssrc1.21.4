package fun.popka.api.utils.client;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class MenuMusicPlayer {

    public static final File MUSIC_FILE = new File("C:\\PopkaClient\\popka\\menu_music.wav");
    public static final double DEFAULT_VOLUME = 0.4;

    private static Clip clip;
    private static double volume = DEFAULT_VOLUME;

    public static void start() {
        if (clip != null && clip.isRunning()) {
            return;
        }
        if (!MUSIC_FILE.exists()) {
            return;
        }
        stop();
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(MUSIC_FILE);
            clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && clip != null && !clip.isRunning()) {
                    // keep clip open for restart on resume; closing handled by stop()
                }
            });
            clip.open(stream);
            stream.close();
            setVolume(volume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {
        }
    }

    public static void stop() {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {
            }
            clip = null;
        }
    }

    public static boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public static boolean isAvailable() {
        return MUSIC_FILE.exists();
    }

    public static void setVolume(double newVolume) {
        volume = Math.max(0.0, Math.min(1.0, newVolume));
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log10(volume <= 0.0 ? 0.0001 : volume) * 20.0);
        control.setValue(dB);
    }

    public static double getVolume() {
        return volume;
    }
}
