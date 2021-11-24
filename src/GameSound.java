import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// Set Sound Effect
// Sound format is wav file.
public class GameSound {

	File Filename;
	private Clip WAVClip = null;
	public GameSound() {}

	public GameSound(String fName) {
		// Filename = fName;
		LoadSoundFile(fName);
	}

	public void Play() {
		if (WAVClip.isActive()) {
			WAVClip.stop();
		}

		WAVClip.setFramePosition(0);
		WAVClip.loop(0);
	}

	public void Play(boolean isloop) {

		if (WAVClip.isActive()) {
			WAVClip.stop();
		}
		WAVClip.setFramePosition(0);

		if (isloop) {
			WAVClip.loop(Clip.LOOP_CONTINUOUSLY);
		}

		else {
			WAVClip.loop(0);
		}
	}

	public void Stop() {
		WAVClip.stop();
		WAVClip.setFramePosition(0);
	}

	public void LoadSoundFile(String fName) {
		debug.print("GameSound: LoadSoundFile - Start of Code");
		Filename = new File(fName);

		try {
			AudioInputStream source = AudioSystem.getAudioInputStream(Filename);
			DataLine.Info clipInfo = new DataLine.Info(Clip.class, source.getFormat());

			if (AudioSystem.isLineSupported(clipInfo)) {
				WAVClip = (Clip) AudioSystem.getLine(clipInfo);
				WAVClip.open(source);
			}

			else {
				debug.print("GameSound: LoadSoundFile - The Clip was not supported");
			}
		} catch (UnsupportedAudioFileException e) {
			debug.print(e);
		} catch (LineUnavailableException e) {
			debug.print(e);
		} catch (IOException e) {
			debug.print(e);
		}
		debug.print("GameSound: LoadSoundFile - End of Code");
	}
}