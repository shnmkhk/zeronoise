package com.rabbit.zeronoise.util;

public class ZConstants {

	public static final String PREF_KEY_PREV_SEQUENCE = "ZERONOISE_PREVIOUS_SEQUENCE";
	public static final String PREF_ROOT_NODE_NAME_ZERONOISE_MEDIA = "ZERONOISE_MEDIA";
	public static String TEMP_RECORDING_FILE = "#sequence#_zeronoise_recording.wav";
	
	static {
		TEMP_RECORDING_FILE = "#sequence#_"+System.getProperty("user.name")+"_recording.wav";
	}
	public static final String SYSTEM_TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
}
