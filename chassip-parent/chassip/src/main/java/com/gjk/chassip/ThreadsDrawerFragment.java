package com.gjk.chassip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gjk.chassip.model.ChatManager;
import com.gjk.chassip.model.User;
import com.gjk.chassip.net.AddMemberTask;
import com.gjk.chassip.net.CreateGroupTask;
import com.gjk.chassip.net.GetGroupMembersTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.service.ChassipService;
import com.google.common.collect.Maps;

/**
 * 
 * @author gpl
 */
public class ThreadsDrawerFragment extends ListFragment {

	private final String LOGTAG = getClass().getSimpleName();

	private View mView;
	private Button mAddChatMembers;
	private ThreadAdapter mAdapter;
	private AlertDialog mDialog;
	private List<Long> mSelectedMembers;
	private String[] mSelectedMembersNames;
	private long[] mSelectedMembersIds;

	// TODO: Temporary!!
	private final static HashMap<String, Long> mapping = Maps.newHashMap();

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mapping.isEmpty()) {
			mapping.put("Kachi", 6L);
			mapping.put("Jeff", 8L);
		}
		mView = inflater.inflate(R.layout.threads_drawer_list, null);
		mAddChatMembers = (Button) mView.findViewById(R.id.addChatMembers);
		mAddChatMembers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectedMembers = new ArrayList<Long>(); // Where we track the selected items
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Set the dialog title
				builder.setTitle(R.string.add_members_to_chat_title)
				// Specify the list array, the items to be selected by default (null for none),
				// and the listener through which to receive callbacks when items are selected
						.setMultiChoiceItems(R.array.contacts, null, new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								String name = getResources().getStringArray(R.array.contacts)[which];
								if (isChecked) {
									// If the user checked the item, add it to the selected items
									mSelectedMembers.add(mapping.get(name));
								} else if (mSelectedMembers.contains(which)) {
									// Else, if the item is already in the array, remove it
									mSelectedMembers.remove(mapping.get(name));
								}
							}
						})
						// Set the action buttons
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK, so save the mSelectedItems results somewhere
								// or return them to the component that opened the dialog
								addChatMembers();
							}
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						});
				// Create the AlertDialog
				mDialog = builder.create();
				mDialog.setCanceledOnTouchOutside(true);
				mDialog.show();
			}
		});
		return mView;
	}

	private void addChatMembers() {

		long[] members = new long[mSelectedMembers.size()];
		for (int i = 0; i < mSelectedMembers.size(); i++) {
			members[i] = mSelectedMembers.get(i);
		}

		new AddMemberTask(getActivity(), new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {

				if (result.getResponseCode() == 1) {
					loadMembers();
				} else {
					handleCreateChatFail(result);
				}
			}
		}, ChatManager.getInstance().getCurrentChatId(), members);
	}

	private void loadMembers() {
		
		mSelectedMembersNames = new String[mSelectedMembers.size()+1];
		mSelectedMembersIds = new long[mSelectedMembers.size()+1];

		new GetGroupMembersTask(getActivity(), new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {

				if (result.getResponseCode() == 1) {
					JSONArray response = (JSONArray) result.getExtraInfo();
					try {

						for (int i=0; i<response.length(); i++) {
							JSONObject member = response.getJSONObject(i);
							mSelectedMembersNames[i] = member.getString("first_name") + " " + member.getString("last_name");
							mSelectedMembersIds[i] = member.getLong("id");
						}
						ChassipService.addChatMembers(ChatManager.getInstance().getCurrentChatId(), mSelectedMembersNames, mSelectedMembersIds);

					} catch (JSONException e) {
						handleGetGroupMembersError(e);
					}
				} else {
					handleGetGroupMembersFail(result);
				}
			}
		}, ChatManager.getInstance().getCurrentChatId()); 
	}

	private void handleCreateChatFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Adding Chat Members failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Adding Chat Members failed: %s", result.getMessage()));
	}

	private void handleCreateChatError(JSONException e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Adding Chat Members errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Adding Chat Members errored: %s", e.getMessage()));
	}
	
	private void handleGetGroupMembersFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Chat Members failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Chat Members failed: %s", result.getMessage()));
	}

	private void handleGetGroupMembersError(JSONException e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Chat Members errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Chat Members errored: %s", e.getMessage()));
	}

	private void showLongToast(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ThreadAdapter(getActivity());
		setListAdapter(mAdapter);
	}

	public void addThread(ThreadFragment frag) {
		mAdapter.add(frag);
	}

	public void removeAllThreads() {
		mAdapter.clear();
	}

	public void removeThread(ThreadFragment frag) {
		mAdapter.remove(frag);
	}

	public void updateView() {
		mAdapter.notifyDataSetChanged();
	}

	public class ThreadAdapter extends ArrayAdapter<ThreadFragment> {

		public ThreadAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.threads_drawer_row, null);
			}
			TextView threadLabel = (TextView) convertView.findViewById(R.id.threadLabel);
			threadLabel.setText(getItem(position).getName());
			TextView members = (TextView) convertView.findViewById(R.id.threadMembers);
			members.setText(getMembers(position));

			return convertView;
		}

		private String getLabel(int position) {
			switch (getItem(position).getThreadType()) {
			case MAIN_CHAT:
				return "MAIN";
			case SIDE_CONVO:
				return "SIDE CONVO";
			case WHISPER:
				return "WHISPER";
			default:
				return "WTF...";
			}
		}

		private String getMembers(int position) {
			String tempMembers = "";
			for (User member : getItem(position).getMembers()) {
				tempMembers = tempMembers.concat(member.getName()).concat(" ");
			}
			return tempMembers;
		}

	}
}
