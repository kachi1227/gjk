package com.gjk.chassip;

import com.gjk.chassip.model.User;

/**
 * 
 * @author gpl
 */
public class InstantMessage {
	
	private long mChatId;
	private long mThreadId;
	private User mUser;
	private String mIm;
	private long mTime;
	
	public InstantMessage(long chatId, long threadId, User user, String im, long time) {
		mChatId = chatId;
		mThreadId = threadId;
		mUser = user;
		mIm = im;
		mTime = time;
	}
	
	public InstantMessage() {}

	/**
	 * @return the mChatId
	 */
	public long getChatId() {
		return mChatId;
	}

	/**
	 * @param mChatId the mChatId to set
	 */
	public void setmChatId(long mChatId) {
		this.mChatId = mChatId;
	}
	
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
	
	/**
	 * @return the mTime
	 */
	public long getTime() {
		return mTime;
	}
	
	/**
	 * @param mTime the mTime to set
	 */
	public void setmTime(long mTime) {
		this.mTime = mTime;
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(mTime).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		InstantMessage im = (InstantMessage) o;
		return (im.mIm == mIm) && (im.mThreadId == mThreadId) && (im.mUser == mUser) && (im.mTime == mTime);
	}
	
	@Override
	public String toString() {
		return String.format("user=%s im=%s", mUser, mIm);
	}
}
