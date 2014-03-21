package com.gjk.chassip.model;

import java.util.List;
import java.util.Map;

import com.gjk.chassip.InstantMessage;
import com.gjk.chassip.ThreadFragment;
import com.gjk.chassip.account.AccountManager;
import com.google.common.collect.Maps;

/**
 * 
 * @author gpl
 */
public class ChatManager {

	private static ChatManager sInstance;
	
	private long mCurrentChatId;
	private final Map<Long, Chat> mChatMap;
	
	private ChatManager() {
		mChatMap = Maps.newHashMap();
	}
	
	public synchronized static ChatManager getInstance() {
		if (sInstance == null) {
			sInstance = new ChatManager();
		}
		return sInstance;
	}
	
	public boolean chatExists(long chatId) {
		return mChatMap.containsKey(chatId);
	}
	
	public void addChat(ThreadFragment mainChatFrag) {
		mCurrentChatId = mainChatFrag.getChatId();
		Chat newChat = new Chat(mCurrentChatId, mainChatFrag.getName());
		newChat.addThread(mainChatFrag);
		if (!chatExists(mCurrentChatId)) {
			mChatMap.put(mCurrentChatId, newChat);
		}
	}

	public Chat getChat(long chatId) {
		return mChatMap.get(chatId);
	}
	
	public Chat getCurrentChat() {
		return mChatMap.get(mCurrentChatId);
	}
	
	public long getCurrentChatId() {
		return mCurrentChatId;
	}
	
	public void addMember(long chatId, long threadId, User newMember) {
		if (chatExists(chatId)) {
			getChat(chatId).addMember(threadId, newMember);
		}
	}
	
	public void addMembers(long chatId, long threadId, User[] newMembers) {
		for (User newMember : newMembers) {
			addMember(chatId, threadId, newMember);
		}
	}
	
	public void addThread(ThreadFragment newThreadFrag) {
		if (chatExists(newThreadFrag.getChatId())) {
			getChat(newThreadFrag.getChatId()).addThread(newThreadFrag);
		}
	}
	
	public void addInstantMessage(InstantMessage im) {
		if (chatExists(im.getChatId())) {
			getChat(im.getChatId()).addInstantMessage(im);
		}
	}
	
	public void addInstantMessages(List<InstantMessage> ims) {
		for (InstantMessage im : ims) {
			addInstantMessage(im);
		}
	}
	
	public ThreadFragment getThreadFragment(long chatId, long threadId) {
		if (chatExists(chatId)) {
			return getChat(chatId).getThreadFragment(threadId);
		}
		return null;
	}
	
	public int getNumberOfThreads(long chatId) {
		if (chatExists(chatId)) {
			return getChat(chatId).getNumberOfThreads();
		}
		return 0;
	}
	
	public int getNumberOfChats() {
		return mChatMap.keySet().size();
	}
	
	public long[] getThreadIds(long chatId) {
		if (chatExists(chatId)) {
			return getChat(chatId).getThreadIds();
		}
		return new long[0];
	}
	
	public Long[] getChatIds() {
		return mChatMap.keySet().toArray(new Long[getNumberOfChats()]);
	}

	public void setCurrentChat(long mChatId) {
		mCurrentChatId = mChatId;
	}
}
