package com.gjk.chassip;

import static com.gjk.chassip.Constants.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.gjk.chassip.database.PersistentObject;
import com.gjk.chassip.database.DatabaseManager.DataChangeListener;
import com.gjk.chassip.database.objects.Group;
import com.gjk.chassip.database.objects.GroupMember;
import com.gjk.chassip.net.CreateGroupTask;
import com.gjk.chassip.net.GetSpecificGroupTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.service.ChassipService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.gjk.chassip.helper.DatabaseHelper.*;

/**
 *
 * @author gpl
 */
public class ChatsDrawerFragment extends ListFragment implements DataChangeListener {

	private final String LOGTAG = getClass().getSimpleName();
	
	private Context mCtx;
	private static Group mCurrentChat;
	private ChatAdapter mAdapter;
	private AlertDialog mDialog;
	private Map<Integer, Group> mPositionToChat;
	private Map<Long, List<GroupMember>> mChatIdToMembers;
	
	private View mView;
	private Button mCreateChat;
	private String mChatName;
	private String mAviPath;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.chats_drawer_list, null);
		mCreateChat = (Button) mView.findViewById(R.id.createChat);
		mCreateChat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				// Get the layout inflater
			    LayoutInflater inflater = getActivity().getLayoutInflater();
			    View view = inflater.inflate(R.layout.create_chat_dialog, null);
				// Add the buttons
				builder.setView(view);
				final EditText chatName = (EditText) view.findViewById(R.id.chatName); 
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String name = chatName.getText().toString();
						Log.d(LOGTAG, "Yes was clicked");
						if (!name.isEmpty()) {
							Log.d(LOGTAG, "Creating chat with name "+name);
							mChatName = name;
							displayImageChooser();
						}
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				builder.setTitle(R.string.create_chat_title);

				// Create the AlertDialog
				mDialog = builder.create();
				mDialog.setCanceledOnTouchOutside(true);
				mDialog.show();
			}
		});
		return mView;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPositionToChat = Maps.newHashMap();
		mChatIdToMembers = Maps.newHashMap();
		mCtx = getActivity();
		mAdapter = new ChatAdapter(mCtx);
		setListAdapter(mAdapter);
		Application.get().getDatabaseManager().registerDataChangeListener(Group.TABLE_NAME, this);
		Application.get().getDatabaseManager().registerDataChangeListener(GroupMember.TABLE_NAME, this);
	}
	
	public void addChat(Group chat) {
		mAdapter.add(chat);
	}
	
	public void updateView() {
		mAdapter.notifyDataSetChanged();
	}
	
	public Group getChatIdFromPosition(int position) {
		return mPositionToChat.get(position);
	}
	
	private void displayImageChooser() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Add the buttons
		builder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				sendCameraIntent();
			}
		});
		builder.setNeutralButton(R.string.gallery, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (android.os.Build.VERSION.SDK_INT >= 19) {
					sendGalleryIntent();
				} else {
					sendGalleryIntentPreKitKat();
				}
			}
		});

		builder.setMessage(R.string.select_avi_message).setTitle(R.string.select_avi_title);

		// Create the AlertDialog
		mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
	}
	
	private void createChat() {
		
		HashMap<String, Object> fieldMapping = Maps.newHashMap();
		fieldMapping.put("image", new File(mAviPath));
		
		new CreateGroupTask(getActivity(), new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {

				if (result.getResponseCode() == 1) {
					JSONObject response = (JSONObject) result.getExtraInfo();
					try {
						fetchGroup(response.getLong("id"));
					} catch (Exception e) {
						handleCreateChatError(e);
					}
				} else {
					handleCreateChatFail(result);
				}
			}
		}, getAccountUserId(), mChatName, fieldMapping); 
	}
	
	private void fetchGroup(long chatId) {
		new GetSpecificGroupTask(mCtx, new HTTPTaskListener() {
			
			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					JSONObject response = (JSONObject) result.getExtraInfo();
					try {
						addGroup(response);
					} catch (Exception e) {
						handleCreateChatError(e);
					}
				} else {
					handleCreateChatFail(result);
				}
			}
		}, getAccountUserId(), chatId);
	}
	
	private void handleAviError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Avi errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Avi errored: %s", e.getMessage()));
	}
	
	private void handleCreateChatFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Chat failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Chat failed: %s", result.getMessage()));
	}

	private void handleCreateChatError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Creating Chat errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Creating Chat errored: %s", e.getMessage()));
	}
	
	private void showLongToast(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST) {
				try {
					if (requestCode == GALLERY_REQUEST) {
						// grab path from gallery
						Uri selectedImageUri = data.getData();
						mAviPath = getPath(selectedImageUri);
					} else if (requestCode == CAMERA_REQUEST) {
						// add pic to gallery
						Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
						File f = new File(mAviPath);
						Uri contentUri = Uri.fromFile(f);
						mediaScanIntent.setData(contentUri);
						getActivity().sendBroadcast(mediaScanIntent);
					}
					showLongToast("Image path: " + mAviPath);
					createChat();
				} catch (Exception e) {
					handleAviError(e);
				}
			}
		}
	}
	
	private void sendGalleryIntentPreKitKat() {

		Log.d(LOGTAG, "Running pre-Kit-Kat");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
	}

	@TargetApi(19)
	private void sendGalleryIntent() {

		Log.d(LOGTAG, "Running Kit-Kat or higher!");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
	}

	private void sendCameraIntent() {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				// Create an image file name
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
				String imageFileName = String.format("%s_%s.jpg", getResources().getString(R.string.app_name),
						timeStamp);
				File storageDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(
						R.string.app_name));
				storageDir.mkdirs();
				photoFile = new File(storageDir, imageFileName);
				photoFile.createNewFile();

				// Save a file: path for use with ACTION_VIEW intents
				mAviPath = photoFile.getAbsolutePath();
			} catch (IOException ex) {
				// Error occurred while creating the File
				showLongToast("Saving temp image file failed, the fuck!?");
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, CAMERA_REQUEST);
			}
		}
	}

	/**
	 * helper to retrieve the path of an image URI
	 */
	private String getPath(Uri uri) {

		String result;
		Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file
								// path
			result = uri.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			result = cursor.getString(idx);
			cursor.close();
		}
		return result;
	}

	private class ChatAdapter extends ArrayAdapter<Group> {

		public ChatAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.chats_drawer_row, null);
			}
			
			if (!mPositionToChat.containsKey(position)) {
				mPositionToChat.put(position, getItem(position));
			}
			
			TextView chatLabel = (TextView) convertView.findViewById(R.id.chatLabel);
			chatLabel.setText(mPositionToChat.get(position).getName());
			TextView members = (TextView) convertView.findViewById(R.id.chatMembers);
			members.setText(getMembers(position));

			return convertView;
		}
		
		private String getMembers(int position) {
			String tempMembers = "";
			List<GroupMember> groupMembers = mChatIdToMembers.get(mPositionToChat.get(position).getGlobalId());
			if (groupMembers != null) {
				for (GroupMember member : mChatIdToMembers.get(mPositionToChat.get(position).getGlobalId())) {
					tempMembers = tempMembers.concat(member.getFullName()).concat(" ");
				}
			}
			return tempMembers;
		}

	}

	public static void setCurrentChat(Group chat) {
		mCurrentChat = chat;
	}
	
	public static Group getCurrentChat() {
		return mCurrentChat;
	}
	
	public GroupMember[] getMembers(long chatId) {
		if (mChatIdToMembers.get(chatId) == null) {
			return new GroupMember[0];
		}
		return mChatIdToMembers.get(chatId).toArray(new GroupMember[mChatIdToMembers.get(chatId).size()]);
	}
	
	@Override
	public void onDataChanged(PersistentObject o) {
		if (o.getTableName().equals(Group.TABLE_NAME)) {
			Group g = (Group) o;
			if (mPositionToChat.containsValue(g)) {
				int position = mAdapter.getPosition(g);
				mAdapter.getItem(position).setName(g.getName());
				mAdapter.getItem(position).setImageUrl(g.getImageUrl());
				mAdapter.getItem(position).setSideChats(g.getSideChats());
				mAdapter.getItem(position).setWhispers(g.getWhispers());
			}
			else {
				mAdapter.add(g);
			}
		}
		else if (o.getTableName().equals(GroupMember.TABLE_NAME)) {
			GroupMember gm = (GroupMember) o;
			mChatIdToMembers.put(gm.getGroupId(), Arrays.asList(getGroupMembers(gm.getGroupId())));
			updateView();
		}
	}
}
