package com.rabbit.zeronoise;
public class Recording {

    private String fileName = null;

    public Recording(String fileName) {
    	this.fileName = fileName;
    }

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}