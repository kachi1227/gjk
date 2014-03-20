package com.gjk.chassip;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gjk.chassip.account.AccountManager;
import com.gjk.chassip.model.ChatManager;
import com.gjk.chassip.model.ImManagerFactory;
import com.gjk.chassip.model.ThreadType;
import com.gjk.chassip.model.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author gpl
 *
 */
@SuppressLint("ValidFragment")
public class ThreadFragment extends ListFragment  {
	
	private final String LOGTAG = getClass().getSimpleName();
	
	private long mChatId;
	private long mThreadId;
	private ThreadType mThreadType;
	private MessagesAdapter mAdapter;
	private Set<User> mUsers;
	private boolean mInitialized;
	private List<InstantMessage> mPendingMessages;
	
	private View mView;
	private EditText mPendingMessage;
	private Button mSend;
	
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
		ImManagerFactory.getImManger(chatId).addThread(threadId);
		mPendingMessages = Lists.newLinkedList();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new MessagesAdapter(getActivity(), mChatId, mThreadId, ImManagerFactory.getImManger(mChatId).getList());
		setListAdapter(mAdapter);
		mSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = mPendingMessage.getText().toString();
				boolean newWhisper = text.contains("<whisper>");
				if (newWhisper) {
					Toast.makeText(getActivity(), "WANTS TO WHISPER", Toast.LENGTH_LONG).show();
				}
				boolean newSideConvo = text.contains("<side-convo>");
				if (newSideConvo) {
					Toast.makeText(getActivity(), "WANTS TO SIDE-CONVO", Toast.LENGTH_LONG).show();
				}
				InstantMessage im = new InstantMessage(mChatId, mThreadId, 0, AccountManager.getInstance().getUser(), text);
				ChatManager.getInstance().addInstantMessage(im);
				mPendingMessage.setText("");
			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mThreadId = savedInstanceState.getLong("mThreadId");
		}
		for (InstantMessage im : mPendingMessages) {
			mAdapter.add(im);
		}
		mView = inflater.inflate(R.layout.message_list, null);
		mPendingMessage = (EditText) mView.findViewById(R.id.pendingMessage);
		mSend = (Button) mView.findViewById(R.id.send);
		return mView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("mThreadId", mThreadId);
	}
	
	public long getChatId() {
		return mChatId;
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
		if (mChatId != im.getChatId()) {
			Log.d(LOGTAG, "WELLL..");
		}
		if (mAdapter == null) {
			mPendingMessages.add(im);
		}
		else {
			mAdapter.add(im);
		}
	}
	
	public void addMessages(Collection<InstantMessage> ims) {
		for (InstantMessage im : ims) {
			addMessage(im);
		}
	}
	
	public void updateView() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
}
