package com.gjk.chassip.model;

import java.util.List;
import java.util.Map;

import com.gjk.chassip.InstantMessage;
import com.gjk.chassip.ThreadFragment;
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
	
	public static synchronized ChatManager getInstance() {
		if (sInstance == null) {
			sInstance = new ChatManager();
		}
		return sInstance;
	}
	
	public synchronized boolean chatExists(long chatId) {
		return mChatMap.containsKey(chatId);
	}
	
	public synchronized void addChat(long chatId, ThreadFragment mainChatFrag) {
		mCurrentChatId = chatId;
		Chat newChat = new Chat(chatId);
		newChat.addThread(mainChatFrag);
		if (!chatExists(chatId)) {
			mChatMap.put(chatId, newChat);
		}
	}

	public synchronized Chat getChat(long chatId) {
		return mChatMap.get(chatId);
	}
	
	public synchronized Chat getCurrentChat() {
		return mChatMap.get(mCurrentChatId);
	}
	
	public synchronized void addMember(long chatId, long threadId, User newMember) {
		if (chatExists(chatId)) {
			getChat(chatId).addMember(threadId, newMember);
		}
	}
	
	public synchronized void addMembers(long chatId, long threadId, User[] newMembers) {
		for (User newMember : newMembers) {
			addMember(chatId, threadId, newMember);
		}
	}
	
	public synchronized void addThread(long chatId, ThreadFragment newThreadFrag) {
		if (chatExists(chatId)) {
			getChat(chatId).addThread(newThreadFrag);
		}
	}
	
	public synchronized void addInstantMessage(InstantMessage im) {
		if (chatExists(im.getChatId())) {
			getChat(im.getChatId()).addInstantMessage(im);
		}
	}
	
	public synchronized void addInstantMessages(List<InstantMessage> ims) {
		for (InstantMessage im : ims) {
			addInstantMessage(im);
		}
	}
	
	public synchronized ThreadFragment getThreadFragment(long chatId, long threadId) {
		if (chatExists(chatId)) {
			return getChat(chatId).getThreadFragment(threadId);
		}
		return null;
	}
	
	public synchronized int getNumberOfThreads(long chatId) {
		if (chatExists(chatId)) {
			return getChat(chatId).getNumberOfThreads();
		}
		return 0;
	}
	
	public synchronized int getNumberOfChats() {
		return mChatMap.keySet().size();
	}
	
	public synchronized long[] getThreadIds(long chatId) {
		if (chatExists(chatId)) {
			return getChat(chatId).getThreadIds();
		}
		return new long[0];
	}
	
	public synchronized Long[] getChatIds() {
		return mChatMap.keySet().toArray(new Long[getNumberOfChats()]);
	}
}
