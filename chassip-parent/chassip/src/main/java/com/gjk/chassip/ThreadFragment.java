package com.gjk.chassip;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import com.gjk.chassip.database.PersistentObject;
import com.gjk.chassip.database.DatabaseManager.DataChangeListener;
import com.gjk.chassip.database.objects.GroupMember;
import com.gjk.chassip.database.objects.Message;
import com.gjk.chassip.net.GetMessageTask;
import com.gjk.chassip.net.SendMessageTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
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

import static com.gjk.chassip.helper.DatabaseHelper.*;

/**
 * 
 * @author gpl
 * 
 */
@SuppressLint("ValidFragment")
public class ThreadFragment extends ListFragment implements DataChangeListener {

	private final String LOGTAG = getClass().getSimpleName();

	private long mChatId;
	private long mThreadId;
	private String mName;
	private ThreadType mThreadType;
	private MessagesAdapter mAdapter;
	private Set<GroupMember> mMembers;
	private boolean mInitialized;
	private List<Message> mPendingMessages;

	private View mView;
	private EditText mPendingMessage;
	private Button mSend;

	private String mMessage;

	public ThreadFragment() {
		super();
		setRetainInstance(true);
		mMembers = Sets.newHashSet();
		mInitialized = false;
	}

	public ThreadFragment(long chatId, long threadId, ThreadType threadType, String name, GroupMember... members) {
		super();
		mChatId = chatId;
		mThreadId = threadId;
		mThreadType = threadType;
		mName = name;
		mMembers = Sets.newHashSet();
		mMembers.addAll(Arrays.asList(members));
		mInitialized = false;
		setRetainInstance(true);
		mPendingMessages = Lists.newLinkedList();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new MessagesAdapter(getActivity(), mChatId, mThreadId, new LinkedList<Message>());
		setListAdapter(mAdapter);
		getMessagesFromDb();
		fetchMessages();
		for (Message im : mPendingMessages) {
			mAdapter.add(im);
		}
		mPendingMessages.clear();
		mMembers.addAll(Arrays.asList(getGroupMembers(mChatId)));
		Application.get().getDatabaseManager().registerDataChangeListener(GroupMember.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(Message.TABLE_NAME, this);
		mSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = mPendingMessage.getText().toString();
				if (!text.isEmpty()) {
					boolean newWhisper = text.contains("<whisper>");
					if (newWhisper) {
						Toast.makeText(getActivity(), "WANTS TO WHISPER", Toast.LENGTH_LONG).show();
					}
					boolean newSideConvo = text.contains("<side-convo>");
					if (newSideConvo) {
						Toast.makeText(getActivity(), "WANTS TO SIDE-CONVO", Toast.LENGTH_LONG).show();
					}

					mMessage = text;
					sendMessage();
				}
			}
		});
		updateView();
	}
	
	private void getMessagesFromDb() {
		List<Message> messages = getMessages(mChatId);
		if (messages != null && messages.size() > 0) {
			if (mAdapter != null) {
				mAdapter.addAll(messages);
			}
			else {
				mPendingMessages.addAll(messages);
			}
		}
	}
	
	private void fetchMessages() {
		
		JSONArray jsonArray = new JSONArray();
		long id = getLastStoredMessageId();
		try {
			jsonArray.put(0, id).put(1, -1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		new GetMessageTask(getActivity(), new HTTPTaskListener() {
			
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					JSONArray messages = (JSONArray) result.getExtraInfo();
					try {
						addGroupMessages(messages);
					} catch (Exception e) {
						handleGetMessagesError(e);
					}
				} else {
					handleGetMessagesFail(result);
				}
			}
		}, getAccountUserId(), ChatsDrawerFragment.getCurrentChat().getGlobalId(), jsonArray);
	}
	
	private void handleGetMessagesError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Get Messages errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Get Messages errored: %s", e.getMessage()));
	}
	
	private void handleGetMessagesFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Get Messages failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Get Messages failed: %s", result.getMessage()));
	}
	
	private void sendMessage() {

		new SendMessageTask(getActivity(), new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {

				if (result.getResponseCode() == 1) {
					fetchMessages();
					mPendingMessage.setText("");
				} else {
					handleSendMessageFail(result);
				}
			}
		}, getAccountUserId(), ChatsDrawerFragment.getCurrentChat().getGlobalId(), mMessage);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mThreadId = savedInstanceState.getLong("mThreadId");
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

	private void handleSendMessageFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Chat failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Chat failed: %s", result.getMessage()));
	}

	private void handleSendMessagerError(JSONException e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Chat errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Chat errored: %s", e.getMessage()));
	}

	private void showLongToast(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}

	public long getChatId() {
		return mChatId;
	}
	
	public String getName() {
		return mName;
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

	public void addMember(GroupMember user) {
		mMembers.add(user);
	}

	public void addMembers(GroupMember[] members) {
		for (GroupMember member : members) {
			addMember(member);
		}
	}

	public Set<GroupMember> getMembers() {
		return mMembers;
	}

	public void updateView() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDataChanged(PersistentObject o) {
		if (o.getTableName().equals(GroupMember.TABLE_NAME)) {
			GroupMember gm = (GroupMember) o;
			if (gm.getGroupId() == mChatId) { //TODO: will need to include thread id!!
				mMembers.add(gm);
			}
		}
		else if (o.getTableName().equals(Message.TABLE_NAME)) {
			Message m = (Message) o;
			if (mAdapter == null) {
				mPendingMessages.add(m);
			} else {
				mAdapter.add(m);
			}
		}
	}
}
