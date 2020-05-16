package com.rabbit.zeronoise;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class RecordingThread extends Thread {
	private TargetDataLine targetDataLine;
	private AudioFormat audioFormat;

	public RecordingThread(TargetDataLine targetDataLine, AudioFormat audioFormat) {
		this.targetDataLine = targetDataLine;
		this.audioFormat = audioFormat;
	}

	private static final String TEMP_RECORDING_FILE = "MyRecording.wav";

	@Override
	public void run() {
		AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
		File f = new File(TEMP_RECORDING_FILE);

		AudioInputStream input = new AudioInputStream(targetDataLine);

		try {
			targetDataLine.open(audioFormat);
			targetDataLine.start();
			AudioSystem.write(input, fileType, f);
		}

		catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}
}