package com.gjk.chassip.net;

public class TaskResult {
	
	public static final int RC_FAILURE = 0;
	public static final int RC_CANCEL = 1;
	public static final int RC_SUCCESS = 1;
	
	private int mResponseCode;
	private Object mExtraInfo;
	private String mMessage;
	private Task mTask;
	
	public TaskResult(Task task, int responseCode, String message, Object extraInfo) {
		mTask = task;
		mResponseCode = responseCode;
		mExtraInfo = extraInfo;
		mMessage = message;
	}
	
	public boolean isSuccess() {
		return mResponseCode == RC_SUCCESS;
	}

	public int getResponseCode() {
		return mResponseCode;
	}

	public void setResponseCode(int mResponseCode) {
		this.mResponseCode = mResponseCode;
	}

	public Object getExtraInfo() {
		return mExtraInfo;
	}

	public void setExtraInfo(Object mExtraInfo) {
		this.mExtraInfo = mExtraInfo;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String mMessage) {
		this.mMessage = mMessage;
	}

	public Task getTask() {
		return mTask;
	}

	public void setTask(Task mTask) {
		this.mTask = mTask;
	}
	
	

}
