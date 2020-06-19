package com.rabbit.zeronoise;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.rabbit.zeronoise.util.ZConstants;

public class RecordingThread extends Thread {

	private TargetDataLine targetDataLine;
	private AudioFormat audioFormat;

	public RecordingThread(TargetDataLine targetDataLine, AudioFormat audioFormat) {
		this.targetDataLine = targetDataLine;
		this.audioFormat = audioFormat;
	}

	@Override
	public void run() {
		Preferences prefs = Preferences.userRoot().node(ZConstants.PREF_ROOT_NODE_NAME_ZERONOISE_MEDIA);
		int previousSequence = prefs.getInt(ZConstants.PREF_KEY_PREV_SEQUENCE, 0);
		prefs.putInt(ZConstants.PREF_KEY_PREV_SEQUENCE, ++previousSequence);
		
		File dirToCreate = new File(ZConstants.SYSTEM_TEMP_DIRECTORY + File.separatorChar + "zeronoise");
		if (!dirToCreate.exists()) {
			dirToCreate.mkdir();
		}
		final String tempFileName = ZConstants.TEMP_RECORDING_FILE.replaceAll("#sequence#", String.valueOf(previousSequence));
		File fileToUseForRec = new File(dirToCreate, tempFileName);
		
		AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
		AudioInputStream input = new AudioInputStream(targetDataLine);

		try {
			targetDataLine.open(audioFormat);
			targetDataLine.start();
			AudioSystem.write(input, fileType, fileToUseForRec);
		}

		catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}
}