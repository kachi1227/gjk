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
		Chat newChat = new Chat(chatId);
		newChat.addThread(mainChatFrag);
		if (!chatExists(chatId)) {
			mChatMap.put(chatId, newChat);
		}
	}
	
	public synchronized Chat getChat(long chatId) {
		return mChatMap.get(chatId);
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
	
	public synchronized void addInstantMessage(long chatId, InstantMessage im) {
		if (chatExists(chatId)) {
			getChat(chatId).addInstantMessage(im);
		}
	}
	
	public synchronized void addInstantMessages(long chatId, List<InstantMessage> ims) {
		for (InstantMessage im : ims) {
			addInstantMessage(chatId, im);
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
}
