package com.gjk.chassip.model;

import java.util.Map;
import java.util.Set;

import com.gjk.chassip.InstantMessage;
import com.gjk.chassip.ThreadFragment;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * @author gpl
 */
public class Chat {

	private long mChatId;
	private Map<Long, ThreadFragment> mThreadsFragMap;
	private ThreadFragment mMainThreadFrag;
	private Set<ThreadFragment> mWhisperThreadFrags;
	private Set<ThreadFragment> mSideConvoThreadFrags;
	
	public Chat(long chatId) {
		mChatId = chatId;
		mThreadsFragMap = Maps.newHashMap();
		mWhisperThreadFrags = Sets.newHashSet();
		mSideConvoThreadFrags = Sets.newHashSet();
	}
	
	public long getChatId() {
		return mChatId;
	}
	
	public Set<User> getMembers() {
		return getMainChatThread().getMembers();
	}
	
	public void addMember(long threadId, User newMember) {
		mThreadsFragMap.get(threadId).addMember(newMember);
	}
	
	public void addMembers(long threadId, User[] newMembers) {
		for (User newMember : newMembers) {
			addMember(threadId, newMember);
		}
	}
	
	public void addThread(ThreadFragment newThreadFrag) {
		switch (newThreadFrag.getThreadType()) {
		case MAIN_CHAT:
			mMainThreadFrag = newThreadFrag;
			break;
		case SIDE_CONVO:
			mSideConvoThreadFrags.add(newThreadFrag);
			break;
		case WHISPER:
			mWhisperThreadFrags.add(newThreadFrag);
			break;
		default:
			break;
		}
		mThreadsFragMap.put(newThreadFrag.getThreadId(), newThreadFrag);
	}
	
	public void addInstantMessage(InstantMessage im) {
		if (mThreadsFragMap.keySet().contains(Long.valueOf(im.getThreadId()))) {
			getMainChatThread().addMessage(im);
			for (ThreadFragment sideConvoFrag : mSideConvoThreadFrags) {
				sideConvoFrag.updateView();
			}
			for (ThreadFragment whisperFrag : mWhisperThreadFrags) {
				whisperFrag.updateView();
			}
		}
	}
	
	public ThreadFragment getThreadFragment(long threadId) {
		return mThreadsFragMap.get(threadId);
	}
	
	public int getNumberOfThreads() {
		return mThreadsFragMap.values().size();
	}
	
	public ThreadFragment getMainChatThread() {
		return mMainThreadFrag;
	}
}
