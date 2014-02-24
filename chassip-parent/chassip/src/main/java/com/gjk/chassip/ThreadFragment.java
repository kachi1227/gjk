package com.gjk.chassip;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gjk.chassip.model.InstantMessageManager;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;
import com.google.common.collect.Sets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author gpl
 *
 */
@SuppressLint("ValidFragment")
public class ThreadFragment extends ListFragment  {
	
	private long mChatId;
	private long mThreadId;
	private ThreadType mThreadType;
	private MessagesAdapter mAdapter;
	private Set<User> mUsers;
	private boolean mInitialized;
	private List<InstantMessage> mIms;
	
	public ThreadFragment() {
		super();
		setRetainInstance(true);
		mUsers = Sets.newHashSet();
		mInitialized = false;
	}
	
	public ThreadFragment(long chatId, long threadId, ThreadType threadType, User[] members) { 
		super();
		mChatId = chatId;
		mThreadId = threadId;
		mThreadType = threadType;
		mUsers = Sets.newHashSet();
		mInitialized = false;
		setRetainInstance(true);
		addMembers(members);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null)
			mThreadId = savedInstanceState.getLong("mThreadId");
		return inflater.inflate(R.layout.message_list, null);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new MessagesAdapter(getActivity(), mThreadId, InstantMessageManager.getInstance().getList());
		setListAdapter(mAdapter);
		if (mInitialized) {
			addMessages(mIms);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("mThreadId", mThreadId);
	}
	
	public long getChatId() {
		return mChatId;
	}
	
	public void initializeWithMessages(List<InstantMessage> ims) {
		mIms = ims;
		mInitialized = true;
	}
	
	public boolean isInitialized() {
		return mInitialized;
	}
	
	public MessagesAdapter getMessageAdapter() {
		return mAdapter;
	}
	
	public ThreadType getThreadType() {
		return mThreadType;
	}
	
	public long getThreadId() {
		return mThreadId;
	}
	
	public void addMember(User user) {
		mUsers.add(user);
	}
	
	public void addMembers(User[] members) {
		for (User member : members) {
			addMember(member);
		}
	}
	
	public Set<User> getMembers() {
		return mUsers;
	}

	public void addMessage(InstantMessage im) {
		mAdapter.add(im);
	}
	
	public void addMessages(Collection<InstantMessage> ims) {
		for (InstantMessage im : ims) {
			addMessage(im);
		}
	}
	
	public void updateView() {
		mAdapter.notifyDataSetChanged();
	}
}
