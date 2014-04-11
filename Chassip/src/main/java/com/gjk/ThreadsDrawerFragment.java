package com.gjk;

import static com.gjk.helper.DatabaseHelper.addGroup;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gjk.net.CreateSideChatTask;
import com.gjk.net.CreateWhisperTask;
import com.gjk.net.GetSpecificGroupTask;
import com.gjk.net.NotifySideChatInviteesTask;
import com.gjk.net.NotifyWhisperInviteesTask;
import com.gjk.net.TaskResult;
import com.gjk.net.HTTPTask.HTTPTaskListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.gjk.database.DatabaseManager.DataChangeListener;
import com.gjk.database.PersistentObject;
import com.gjk.database.objects.GroupMember;
import com.gjk.helper.DatabaseHelper;

/**
 * 
 * @author gpl
 */
public class ThreadsDrawerFragment extends ListFragment implements DataChangeListener {

	private final String LOGTAG = getClass().getSimpleName();

	private View mView;
	private Button mCreateSideConvo;
	private Button mCreateWhisper;
	private ThreadAdapter mAdapter;
	private AlertDialog mDialog;
	private List<Long> mSelectedMembers;

	// TODO: Temporary!!
	private final static HashMap<String, Long> mapping = Maps.newHashMap();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ThreadAdapter(getActivity());
		setListAdapter(mAdapter);
		Application.get().getDatabaseManager().registerDataChangeListener(GroupMember.TABLE_NAME, this);
		if (mapping.isEmpty()) {
			mapping.put("Greg", 3L);
			mapping.put("Jeff", 8L);
			mapping.put("Kachi", 6L);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.threads_drawer_list, null);
		mCreateSideConvo = (Button) mView.findViewById(R.id.createSideConvo);
		mCreateSideConvo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectedMembers = new ArrayList<Long>(); // Where we track the selected items
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Set the dialog title
				builder.setTitle(R.string.add_members_to_sideconvo_title)
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
								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								// Get the layout inflater
								LayoutInflater inflater = getActivity().getLayoutInflater();
								View view = inflater.inflate(R.layout.create_sideconvo_dialog, null);
								// Add the buttons
								builder.setView(view);
								final EditText chatName = (EditText) view.findViewById(R.id.sideConvoName);
								builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String name = chatName.getText().toString();
										Log.d(LOGTAG, "Yes was clicked");
										if (!name.isEmpty()) {
											Log.d(LOGTAG, "Creating side-convo with name " + name);
											createSideConvo(name);
										}
									}
								});
								builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								});
								builder.setTitle(R.string.create_sideconvo_title);
								// Create the AlertDialog
								mDialog = builder.create();
								mDialog.setCanceledOnTouchOutside(true);
								mDialog.show();
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
		mCreateWhisper = (Button) mView.findViewById(R.id.createWhisper);
		mCreateWhisper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectedMembers = new ArrayList<Long>(); // Where we track the selected items
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Set the dialog title
				builder.setTitle(R.string.add_members_to_whisper_title)
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
								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								// Get the layout inflater
								LayoutInflater inflater = getActivity().getLayoutInflater();
								View view = inflater.inflate(R.layout.create_whisper_dialog, null);
								// Add the buttons
								builder.setView(view);
								final EditText chatName = (EditText) view.findViewById(R.id.whisperName);
								builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String name = chatName.getText().toString();
										Log.d(LOGTAG, "Yes was clicked");
										if (!name.isEmpty()) {
											Log.d(LOGTAG, "Creating whisper with name " + name);
											createWhisper(name);
										}
									}
								});
								builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								});
								builder.setTitle(R.string.create_whisper_title);
								// Create the AlertDialog
								mDialog = builder.create();
								mDialog.setCanceledOnTouchOutside(true);
								mDialog.show();
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

	@Override
	public void onDestroy() {
		Application.get().getDatabaseManager().unregisterDataChangeListener(GroupMember.TABLE_NAME, this);
		super.onDestroy();
	}

	private void createSideConvo(String name) {
		final long[] members = new long[mSelectedMembers.size()];
		for (int i = 0; i < mSelectedMembers.size(); i++) {
			members[i] = mSelectedMembers.get(i);
		}
		new CreateSideChatTask(getActivity(), new HTTPTaskListener() {
			long[] ids = members;
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					try {
						JSONObject response = (JSONObject) result.getExtraInfo();
						fetchGroup();
						notifyNewSideConvoMembers(response.getLong("id"));
					} catch (Exception e) {
						handleCreateSideConvoError(e);
					}
				} else {
					handleCreateSideConvoFail(result);
				}
			}
		}, ChatsDrawerFragment.getCurrentChat().getGlobalId(), getAccountUserId(), members, name);
	}
	
	private void notifyNewSideConvoMembers(long id) {
		Set<GroupMember> ms = mAdapter.getItem(0).getMembers();
		long[] members = new long[ms.size()];
		int index = 0; 
		for (GroupMember m : ms) {
			members[index++] = m.getGlobalId();
		}
		new NotifySideChatInviteesTask(getActivity(), new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.i(LOGTAG, "Notified side convo invitees");
				} else {
					handleNotifySideConvoInviteFail(result);
				}
			}
		}, DatabaseHelper.getAccountUserId(), id, members);
	}

	private void createWhisper(String name) {
		final long[] members = new long[mSelectedMembers.size()];
		for (int i = 0; i < mSelectedMembers.size(); i++) {
			members[i] = mSelectedMembers.get(i);
		}
		new CreateWhisperTask(getActivity(), new HTTPTaskListener() {
			long[] ids = members;
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					try {
						JSONObject response = (JSONObject) result.getExtraInfo();
						fetchGroup();
						notifyNewWhisperMembers(response.getLong("id"));
					} catch (Exception e) {
						handleCreateWhisperError(e);
					}
				} else {
					handleCreateWhisperFail(result);
				}
			}
		}, ChatsDrawerFragment.getCurrentChat().getGlobalId(), getAccountUserId(), members, name);
	}
	
	private void notifyNewWhisperMembers(long id) {
		Set<GroupMember> ms = mAdapter.getItem(0).getMembers();
		long[] members = new long[ms.size()];
		int index = 0; 
		for (GroupMember m : ms) {
			members[index++] = m.getGlobalId();
		}
		new NotifyWhisperInviteesTask(getActivity(), new HTTPTaskListener() {
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.i(LOGTAG, "Notified side convo invitees");
				} else {
					handleNotifyWhisperInviteFail(result);
				}
			}
		}, DatabaseHelper.getAccountUserId(), id, members);
	}

	private void fetchGroup() {
		new GetSpecificGroupTask(getActivity(), new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					JSONObject response = (JSONObject) result.getExtraInfo();
					try {
						addGroup(response);
					} catch (Exception e) {
						// handleCreateChatError(e);
					}
				} else {
					handleCreateChatFail(result);
				}
			}
		}, getAccountUserId(), ChatsDrawerFragment.getCurrentChat().getGlobalId());
	}

	private void handleCreateChatFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Adding Chat Members failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Adding Chat Members failed: %s", result.getMessage()));
	}

	private void handleCreateSideConvoFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Side Convo failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Side Convo failed: %s", result.getMessage()));
	}

	private void handleCreateSideConvoError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Side Convo errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Side Convo errored: %s", e.getMessage()));
	}

	private void handleCreateWhisperFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Whisper failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Whisper failed: %s", result.getMessage()));
	}

	private void handleCreateWhisperError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Whisper errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Whisper errored: %s", e.getMessage()));
	}
	
	private void handleNotifySideConvoInviteFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Notifying Side Convo Members failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Notifying Side Convo Members failed: %s", result.getMessage()));
	}
	
	private void handleNotifyWhisperInviteFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Notifying Whisper Members failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Notifying Whisper Members failed: %s", result.getMessage()));
	}

	private void showLongToast(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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

		private String getMembers(int position) {
			String tempMembers = "";
			List<GroupMember> members = Lists.newArrayList(getItem(position).getMembers());
			if (members != null) {
				for (GroupMember member : members) {
					tempMembers = tempMembers.concat(member.getFullName()).concat(" ");
				}
			}
			return tempMembers;
		}
	}

	@Override
	public void onDataChanged(final PersistentObject o) {
//		getActivity().runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if (o.getTableName().equals(GroupMember.TABLE_NAME)) {
//					GroupMember gm = (GroupMember) o;
//					if (gm.getGroupId() == ChatsDrawerFragment.getCurrentChat().getGlobalId()) {
//						updateView();
//					}
//				}
//			}
//		});
	}
}
