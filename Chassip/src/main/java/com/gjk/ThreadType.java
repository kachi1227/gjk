package com.gjk;

/**
 * 
 * @author gpl
 */
public enum ThreadType {
	MAIN_CHAT(1),
	SIDE_CONVO(2),
	WHISPER(3);
	
	private final int value;
	
	private ThreadType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public static ThreadType getFromValue(int value) {
		if (MAIN_CHAT.value == value) {
			return MAIN_CHAT;
		}
		else if (SIDE_CONVO.value == value) {
			return SIDE_CONVO;
		}
		else if (WHISPER.value == value) {
			return WHISPER;
		}
		return null;
	}
}
