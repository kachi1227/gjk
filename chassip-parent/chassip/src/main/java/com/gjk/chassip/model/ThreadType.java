package com.gjk.chassip.model;

/**
 * 
 * @author gpl
 */
public enum ThreadType {
	MAIN_CHAT(0),
	SIDE_CONVO(1),
	WHISPER(2);
	
	private final int value;
	
	private ThreadType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
}
