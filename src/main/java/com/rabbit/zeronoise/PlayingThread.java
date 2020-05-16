package com.rabbit.zeronoise;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PlayingThread implements Callable<Clip> {

		private static final String TEMP_RECORDING_FILE = "MyRecording.wav";

		@Override
		public Clip call() throws Exception {
			File fileToPlay = new File(TEMP_RECORDING_FILE);

			Clip clip = null;
			try {
				clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(fileToPlay));
				clip.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
			return clip;
		}
	}