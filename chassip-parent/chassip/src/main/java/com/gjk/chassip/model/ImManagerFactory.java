package com.gjk.chassip.model;

import java.util.LinkedList;
import java.util.Map;

import android.util.Log;

import com.gjk.chassip.Constants;
import com.gjk.chassip.InstantMessage;
import com.gjk.chassip.R;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

/**
 * 
 * @author gpl
 */
public class ImManagerFactory {

	private final String LOGTAG = getClass().getSimpleName();
	
	private static Map<Long, ImManagerFactory> sInstanceMap = Maps.newHashMap();
	
	private LinkedList<InstantMessage> mIms;
	private Table<InstantMessage, Long, Integer> mColorTable;
	
	private ImManagerFactory() {
		mIms = Lists.newLinkedList();
		mColorTable = HashBasedTable.create();
	}
	
	public static synchronized ImManagerFactory getImManger(long chatId) {
		ImManagerFactory instance = sInstanceMap.get(chatId);
		if (instance == null) {
			instance = new ImManagerFactory();
			sInstanceMap.put(chatId, instance);
		}
		return instance;
	}
	
	public void addChat(long threadId) {
		addThread(threadId);
	}
	
	public void addThread(long threadId) {
		for (InstantMessage im : mIms) {
			put(im, threadId);
		}
	}
	
	public void add(InstantMessage im) {
		if (!mIms.contains(im)) {
			Log.e(LOGTAG, "WHOOOOOOOA");
		}
		else {
			for (Long l : ChatManager.getInstance().getChat(im.getChatId()).getThreadIds()) {
				put(im, l);				
			}
		}
	}
	
	public Integer get(InstantMessage im, Long t) {
		return mColorTable.get(im, t);
	}
	
	private void put(InstantMessage im, Long t) {
		if (!mColorTable.contains(im, t)) {
			Integer v = im.getThreadId() == t ?
					R.color.black :
					R.color.lightgrey;
			Log.d(LOGTAG, String.format("putting i=%s t=%d v=%d", im, t, v));
			mColorTable.put(im, t, v);
		}
	}
	
	public LinkedList<InstantMessage> getList() {
		return mIms;
	}
	
	public boolean trim() {
		return mIms.size() > Constants.MAX_IMS;
	}
}
