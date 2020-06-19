package com.rabbit.zeronoise;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import com.rabbit.zeronoise.util.ZConstants;
import com.sun.media.jfxmedia.AudioClip;

public class PlayingThread implements Callable<AudioClip> {

	private String fileNameToPlay;

	public PlayingThread() {

	}

	public PlayingThread(String fileNameToPlay) {
		this.fileNameToPlay = fileNameToPlay;
	}

	public AudioClip call() throws Exception {
		Preferences prefs = Preferences.userRoot().node(ZConstants.PREF_ROOT_NODE_NAME_ZERONOISE_MEDIA);
		int previousSequence = prefs.getInt(ZConstants.PREF_KEY_PREV_SEQUENCE, 0);

		File dirToReadFrom = new File(ZConstants.SYSTEM_TEMP_DIRECTORY + File.separatorChar + "zeronoise");
		final String latestFileName = ZConstants.TEMP_RECORDING_FILE.replaceAll("#sequence#", String.valueOf(previousSequence));

		File fileToPlay = new File(dirToReadFrom, this.fileNameToPlay != null ? this.fileNameToPlay : latestFileName);
		AudioClip audioClip = null;
		try {
			audioClip = AudioClip.load(fileToPlay.toURI());
			audioClip.play();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return audioClip;
	}
}