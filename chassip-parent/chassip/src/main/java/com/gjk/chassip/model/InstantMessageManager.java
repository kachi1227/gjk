package com.gjk.chassip.model;

import java.util.LinkedList;

import com.gjk.chassip.Constants;
import com.gjk.chassip.InstantMessage;
import com.google.common.collect.Lists;

/**
 * 
 * @author gpl
 */
public class InstantMessageManager {

	private static InstantMessageManager sInstance;
	
	private final LinkedList<InstantMessage> mIms;
	
	private InstantMessageManager() {
		mIms = Lists.newLinkedList();
	}
	
	public static synchronized InstantMessageManager getInstance() {
		if (sInstance == null) {
			sInstance = new InstantMessageManager();
		}
		return sInstance;
	}
	
	public LinkedList<InstantMessage> getList() {
		return mIms;
	}
	
	public void trim() {
		while (isTooBig()) {
			mIms.poll();
		}
	}
	
	private boolean isTooBig() {
		return mIms.size() > Constants.MAX_IMS;
	}
}
