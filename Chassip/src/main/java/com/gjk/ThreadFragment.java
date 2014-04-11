package com.gjk;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import com.gjk.database.PersistentObject;
import com.gjk.database.DatabaseManager.DataChangeListener;
import com.gjk.database.objects.GroupMember;
import com.gjk.database.objects.Message;
import com.gjk.database.objects.ThreadMember;
import com.gjk.net.GetMessageTask;
import com.gjk.net.NotifyGroupOfMessageTask;
import com.gjk.net.SendMessageTask;
import com.gjk.net.TaskResult;
import com.gjk.net.HTTPTask.HTTPTaskListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.gjk.helper.DatabaseHelper.*;

/**
 * 
 * @author gpl
 * 
 */
public class ThreadFragment extends ListFragment implements DataChangeListener {

	private static final String LOGTAG = "ThreadFragment";

	private long mChatId;
	private long mThreadId;
	private String mName;
	private ThreadType mThreadType;
	private MessagesAdapter mAdapter;
	private Set<GroupMember> mMembers;
	private boolean mInitialized;
	private List<Message> mPendingMessages = Lists.newLinkedList();

	private View mView;
	private EditText mPendingMessage;
	private Button mSend;

	private String mMessage;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mChatId = getArguments().getLong("chatId");
		mThreadId = getArguments().getLong("threadId");
		mThreadType = ThreadType.getFromValue(getArguments().getInt("threadType"));
		mName = getArguments().getString("name");
		mMembers = Sets.newHashSet(getGroupMembers(mChatId));
		mAdapter = new MessagesAdapter(getActivity(), mChatId, mThreadId, mThreadType, new LinkedList<Message>());
		setListAdapter(mAdapter);
		getMessagesFromDb();
		for (Message im : mPendingMessages) {
			mAdapter.add(im);
		}
		mPendingMessages.clear();
		if (mThreadType == ThreadType.MAIN_CHAT) {
			addMembers(getGroupMembers(mChatId));
		}
		Application.get().getDatabaseManager().registerDataChangeListener(GroupMember.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(Message.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(ThreadMember.TABLE_NAME, this);
		mSend.setEnabled(false);
		mSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSend.setEnabled(false);
				String text = mPendingMessage.getText().toString();
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
		});
		mPendingMessage.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				mSend.setEnabled(s.length() != 0);
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		updateView();
	}

	private void getMessagesFromDb() {
		List<Message> messages = getMessages(mChatId);
		if (messages != null && messages.size() > 0) {
			if (mAdapter != null) {
				mAdapter.addAll(messages);
			} else {
				mPendingMessages.addAll(messages);
			}
		}
	}

	private void fetchMessages() {
		JSONArray jsonArray = new JSONArray();
		long id = getLastStoredMessageId(ChatsDrawerFragment.getCurrentChat().getGlobalId());
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
						notifyGroup();
					} catch (Exception e) {
						handleGetMessagesError(e);
					}
				} else {
					handleGetMessagesFail(result);
				}
			}
		}, getAccountUserId(), ChatsDrawerFragment.getCurrentChat().getGlobalId(), jsonArray);
	}

	private void notifyGroup() {
		new NotifyGroupOfMessageTask(getActivity(), new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.i(LOGTAG, "Notified group invitees");
				} else {
					handleGetMessagesFail(result);
				}
			}
		}, ChatsDrawerFragment.getCurrentChat().getGlobalId());
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
				mSend.setEnabled(true);
				if (result.getResponseCode() == 1) {
					fetchMessages();
					mPendingMessage.setText("");
				} else {
					handleSendMessageFail(result);
				}
			}
		}, getAccountUserId(), mChatId, mThreadType.getValue(), mThreadId, mMessage);
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
    public void onViewCreated(View view, Bundle savedInstance) {
        scrollMyListViewToBottom();
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
		updateView();
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

	private void scrollMyListViewToBottom() {
        getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				if (getListView().getCount() > 0) {
					getListView().setSelection(getListView().getCount() - 1);
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		Application.get().getDatabaseManager().unregisterDataChangeListener(GroupMember.TABLE_NAME, this);
		Application.get().getDatabaseManager().unregisterDataChangeListener(Message.TABLE_NAME, this);
		Application.get().getDatabaseManager().unregisterDataChangeListener(ThreadMember.TABLE_NAME, this);
		super.onDestroy();
	}

	@Override
	public void onDataChanged(final PersistentObject o) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (o.getTableName().equals(GroupMember.TABLE_NAME)) {
					GroupMember gm = (GroupMember) o;
					if (gm.getGroupId() == mChatId && mThreadType == ThreadType.MAIN_CHAT) {
						addMember(gm);
					}
				} else if (o.getTableName().equals(Message.TABLE_NAME)) {
					Message m = (Message) o;
					if (ChatsDrawerFragment.getCurrentChat().getGlobalId() == mChatId && m.getGroupId() == mChatId) {
						if (mAdapter == null) {
							mPendingMessages.add(m);
						} else {
							mAdapter.add(m);
						}
					}
				} else if (o.getTableName().equals(ThreadMember.TABLE_NAME)) {
					ThreadMember tm = (ThreadMember) o;
					if (tm.getGroupId() == mChatId && mThreadId == tm.getThreadId()) {
						addMember(getGroupMember(tm.getGlobalId()));
					}
				}
			}
		});
	}
}
