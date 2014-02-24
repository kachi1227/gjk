package com.gjk.chassip;

import com.gjk.chassip.model.User;

/**
 * 
 * @author gpl
 */
public class InstantMessage {
	
	private long mThreadId;
	private User mUser;
	private String mIm;
	
	public InstantMessage(long threadId, User user, String im) {
		mThreadId = threadId;
		mUser = user;
		mIm = im;
	}
	
	public InstantMessage() {}
	
	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return mThreadId;
	}
	/**
	 * @param threadId the threadId to set
	 */
	public void setThreadId(long threadId) {
		this.mThreadId = threadId;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return mUser;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.mUser = user;
	}
	/**
	 * @return the im
	 */
	public String getIm() {
		return mIm;
	}
	/**
	 * @param im the im to set
	 */
	public void setIm(String im) {
		this.mIm = im;
	}
}
