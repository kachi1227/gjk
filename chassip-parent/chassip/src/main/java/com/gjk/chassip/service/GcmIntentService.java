package com.gjk.chassip.service;

import static com.gjk.chassip.helper.DatabaseHelper.*;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gjk.chassip.Application;
import com.gjk.chassip.ChatsDrawerFragment;
import com.gjk.chassip.MainActivity;
import com.gjk.chassip.R;
import com.gjk.chassip.database.objects.Group;
import com.gjk.chassip.database.objects.Message;
import com.gjk.chassip.helper.DatabaseHelper;
import com.gjk.chassip.net.GetMessageTask;
import com.gjk.chassip.net.GetSpecificGroupTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class GcmIntentService extends IntentService {

	private final String LOGTAG = getClass().getSimpleName();

	public static final int NEW_MESSAGE_NOTIFACATION = 1;
	public static final int NEW_GROUP_INVITE_NOTIFACATION = 2;
	public static final int NEW_SIDECONVO_INVITE_NOTIFACATION = 3;
	public static final int NEW_WHISPER_INVITE_NOTIFACATION = 4;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (getAccountUserId() == null) {
			return; 
		}
		
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM will be extended in the future with
			 * new message types, just ignore any message types you're not interested in, or that you don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				// notifyNewMessage("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				// notifyNewMessage("Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

				Log.i(LOGTAG, extras.toString());

				if (extras.getString("msg_type").equals("chat_message")) {
					try {
						final long chatId = new JSONObject(extras.getString("msg_content")).getLong("group_id");
						JSONArray jsonArray = new JSONArray();
						long id = getLastStoredMessageId(chatId);
						try {
							jsonArray.put(0, id).put(1, -1);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						new GetMessageTask(getApplicationContext(), new HTTPTaskListener() {
							@Override
							public void onTaskComplete(TaskResult result) {
								if (result.getResponseCode() == 1) {
									JSONArray messages = (JSONArray) result.getExtraInfo();
									try {
										for (int i = 0; i < messages.length(); i++) {
											Message m = addGroupMessage(messages.getJSONObject(i));
											if (m.getSenderId() != getAccountUserId()) {
												if (!Application.get().isActivityIsInForeground()
														|| ChatsDrawerFragment.getCurrentChat() == null
														|| m.getGroupId() != ChatsDrawerFragment.getCurrentChat()
																.getGlobalId()) {
													notifyNewMessage(m);
												}
											}
										}
									} catch (Exception e) {
										handleGetMessagesError(e);
									}
								} else {
									handleGetMessagesFail(result);
								}
							}
						}, getAccountUserId(), chatId, jsonArray);
					} catch (JSONException e) {
						handleMessageError(e);
					}
				}

				else if (extras.getString("msg_type").equals("group_invite")) {
					try {
						final JSONObject content = new JSONObject(extras.getString("msg_content"))
								.getJSONObject("content");
						JSONObject group = content.getJSONObject("group");
						JSONObject inviter = content.getJSONObject("inviter");
						Group g = addGroup(group);
						notifyNewGroup(g, inviter);
					} catch (Exception e) {
						handleGroupInviteError(e);
					}
				}

				else if (extras.getString("msg_type").equals("side_chat_invite")) {
					try {
						final JSONObject content = new JSONObject(extras.getString("msg_content"))
								.getJSONObject("content");
						final JSONObject sideConvo = content.getJSONObject("side_chat");
						final JSONArray members = content.getJSONArray("members");
						final JSONObject inviter = content.getJSONObject("inviter");
						new GetSpecificGroupTask(getApplicationContext(), new HTTPTaskListener() {
							@Override
							public void onTaskComplete(TaskResult result) {
								if (result.getResponseCode() == 1) {
									try {
										JSONObject response = (JSONObject) result.getExtraInfo();
										Group newG = addGroup(response);
										notifyNewSideConvo(newG, sideConvo, members, inviter);
									} catch (Exception e) {
										handleGetGroupError(e);
									}
								} else {
									handleGetGroupFail(result);
								}
							}
						}, getAccountUserId(), sideConvo.getLong("group_id"));
					} catch (Exception e) {
						handleSideConvoInviteError(e);
					}

				} else if (extras.getString("msg_type").equals("whisper_invite")) {
					try {
						final JSONObject content = new JSONObject(extras.getString("msg_content"))
								.getJSONObject("content");
						final JSONObject whisper = content.getJSONObject("whisper");
						final JSONArray members = content.getJSONArray("members");
						final JSONObject inviter = content.getJSONObject("inviter");
						new GetSpecificGroupTask(getApplicationContext(), new HTTPTaskListener() {
							@Override
							public void onTaskComplete(TaskResult result) {
								if (result.getResponseCode() == 1) {
									try {
										JSONObject response = (JSONObject) result.getExtraInfo();
										Group newG = addGroup(response);
										notifyNewWhisper(newG, whisper, members, inviter);
									} catch (Exception e) {
										handleGetGroupError(e);
									}
								} else {
									handleGetGroupFail(result);
								}
							}
						}, getAccountUserId(), whisper.getLong("group_id"));
					} catch (Exception e) {
						handleWhisperInviteError(e);
					}
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void handleGetMessagesFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Chat failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Chat failed: %s", result.getMessage()));
	}

	private void handleGetMessagesError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Chat errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Chat errored: %s", e.getMessage()));
	}

	private void handleGetGroupFail(TaskResult result) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Group failed: %s", result.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Group failed: %s", result.getMessage()));
	}

	private void handleGetGroupError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "Getting Group errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "Getting Group errored: %s", e.getMessage()));
	}

	private void handleMessageError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "GCM Message errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "GCM Message errored: %s", e.getMessage()));
	}

	private void handleGroupInviteError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "GCM Group Invite errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "GCM Group Invite errored: %s", e.getMessage()));
	}

	private void handleSideConvoInviteError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "GCM Side Convo Invite errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "GCM Side Convo Invite errored: %s", e.getMessage()));
	}

	private void handleWhisperInviteError(Exception e) {
		Log.e(LOGTAG, String.format(Locale.getDefault(), "GCM Whisper Invite errored: %s", e.getMessage()));
		showLongToast(String.format(Locale.getDefault(), "GCM Whisper Invite errored: %s", e.getMessage()));
	}

	private void showLongToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void notifyNewMessage(Message m) {
		String content = String.format(Locale.getDefault(), "%s %s: %s", m.getSenderFirstName(), m.getSenderLastName(),
				m.getContent());
		String title = String.format(Locale.getDefault(), "%s", DatabaseHelper.getGroup(m.getGroupId()).getName());
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.speech_bubble).setContentTitle(title).setContentText(content)
				.setTicker(content);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
		resultIntent.setAction(Intent.ACTION_MAIN);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resultIntent.putExtra("group_id", m.getGroupId());
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent).setAutoCancel(true);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NEW_MESSAGE_NOTIFACATION, mBuilder.build());
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void notifyNewGroup(Group g, JSONObject inviter) throws Exception {
		if (inviter.getLong("id") != getAccountUserId()) {
			String content = String.format(Locale.getDefault(), "You've been added to %s's chat called \"%s\"",
					g.getCreatorName(), g.getName());
			String title = "New Chat Invite";
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.plus)
					.setContentTitle(title).setContentText(content).setTicker(content);
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
			resultIntent.setAction(Intent.ACTION_MAIN);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent.putExtra("group_id", g.getGlobalId());
			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent).setAutoCancel(true);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(NEW_GROUP_INVITE_NOTIFACATION, mBuilder.build());
		}
	}

	private void notifyNewSideConvo(Group g, JSONObject sc, JSONArray members, JSONObject inviter) throws JSONException {
		if (inviter.getLong("id") != getAccountUserId()) {
			boolean isInSideConvo = false;
			for (int i = 0; i < members.length(); i++) {
				if (members.getJSONObject(i).getLong("id") == getAccountUserId()) {
					isInSideConvo = true;
				}
			}
			String content = isInSideConvo ? String.format(Locale.getDefault(),
					"%s %s added you to %s's new side convo in %s called \"%s\"", inviter.get("first_name"),
					inviter.get("last_name"), sc.getString("creator_name"), g.getName(), sc.getString("name")) : String
					.format(Locale.getDefault(), "%s has started a new side convo in %s called \"%s\"",
							sc.getString("creator_name"), g.getName(), sc.getString("name"));
			String title = isInSideConvo ? "New Side Convo Invite" : "New Side Convo";
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.plus)
					.setContentTitle(title).setContentText(content).setTicker(content);
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
			resultIntent.setAction(Intent.ACTION_MAIN);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent.putExtra("group_id", g.getGlobalId());
			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent).setAutoCancel(true);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(NEW_SIDECONVO_INVITE_NOTIFACATION, mBuilder.build());
		}
	}

	private void notifyNewWhisper(Group g, JSONObject w, JSONArray members, JSONObject inviter) throws JSONException {
		if (inviter.getLong("id") != getAccountUserId()) {
			boolean isInWhisper = false;
			for (int i = 0; i < members.length(); i++) {
				if (members.getJSONObject(i).getLong("id") == getAccountUserId()) {
					isInWhisper = true;
				}
			}
			String content = isInWhisper ? String.format(Locale.getDefault(),
					"%s %s added you to %s's new whisper in %s called \"%s\"", inviter.get("first_name"),
					inviter.get("last_name"), w.getString("creator_name"), g.getName(), w.getString("name")) : String
					.format(Locale.getDefault(), "%s has started a new whisper in %s called \"%s\"",
							w.getString("creator_name"), g.getName(), w.getString("name"));
			String title = isInWhisper ? "New Whisper Invite" : "New Whisper";
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.plus)
					.setContentTitle(title).setContentText(content).setTicker(content);
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
			resultIntent.setAction(Intent.ACTION_MAIN);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resultIntent.putExtra("group_id", g.getGlobalId());
			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent).setAutoCancel(true);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(NEW_WHISPER_INVITE_NOTIFACATION, mBuilder.build());
		}
	}
}