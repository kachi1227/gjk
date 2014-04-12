package com.gjk.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.gjk.Application;
import com.gjk.ChatsDrawerFragment;
import com.gjk.MainActivity;
import com.gjk.R;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.Message;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.net.GetMessageTask;
import com.gjk.net.GetSpecificGroupTask;
import com.gjk.net.HTTPTask.HTTPTaskListener;
import com.gjk.net.TaskResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static com.gjk.helper.DatabaseHelper.addGroup;
import static com.gjk.helper.DatabaseHelper.addGroupMessage;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;
import static com.gjk.helper.DatabaseHelper.getLastStoredMessageId;

public class GcmIntentService extends IntentService {

	private static final String LOGTAG = "GcmIntentService";

    public static final int NEW_MESSAGE_NOTIFACATION = 1;
    public static final int NEW_GROUP_INVITE_NOTIFACATION = 2;
    public static final int NEW_SIDECONVO_INVITE_NOTIFACATION = 3;
    public static final int NEW_WHISPER_INVITE_NOTIFACATION = 4;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final Context ctx = getApplicationContext();

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

                if (getAccountUserId() == null) {
                    return;
                }

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
                                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
									}
								} else {
                                    GeneralHelper.reportMessage(ctx, LOGTAG, result.getMessage());
								}
							}
						}, getAccountUserId(), chatId, jsonArray);
					} catch (Exception e) {
                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
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
                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
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
                                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
									}
								} else {
                                    GeneralHelper.reportMessage(ctx, LOGTAG, result.getMessage());
								}
							}
						}, getAccountUserId(), sideConvo.getLong("group_id"));
					} catch (Exception e) {
                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
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
                                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
									}
								} else {
                                    GeneralHelper.reportMessage(ctx, LOGTAG, result.getMessage());
								}
							}
						}, getAccountUserId(), whisper.getLong("group_id"));
					} catch (Exception e) {
                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage());
					}
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void notifyNewMessage(Message m) {
        String content = String.format(Locale.getDefault(), "%s %s: %s", m.getSenderFirstName(), m.getSenderLastName(),
                m.getContent());
        String title = String.format(Locale.getDefault(), "%s", DatabaseHelper.getGroup(m.getGroupId()).getName());
        notificationAlert(title, content, m.getGroupId(), NEW_MESSAGE_NOTIFACATION);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void notifyNewGroup(Group g, JSONObject inviter) throws Exception {
        if (inviter.getLong("id") != getAccountUserId()) {
            String content = String.format(Locale.getDefault(), "You've been added to %s's chat called \"%s\"",
                    g.getCreatorName(), g.getName());
            String title = "New Chat Invite";
            notificationAlert(title, content, g.getGlobalId(), NEW_GROUP_INVITE_NOTIFACATION);
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
            notificationAlert(title, content, g.getGlobalId(), NEW_SIDECONVO_INVITE_NOTIFACATION);
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
            notificationAlert(title, content, g.getGlobalId(), NEW_WHISPER_INVITE_NOTIFACATION);
        }
    }

    private void notificationAlert(String title, String content, long groupId, int type) {
        int icon;
        switch (type) {
            case NEW_MESSAGE_NOTIFACATION:
                icon = R.drawable.speech_bubble;
                break;
            case NEW_GROUP_INVITE_NOTIFACATION:
            case NEW_SIDECONVO_INVITE_NOTIFACATION:
            case NEW_WHISPER_INVITE_NOTIFACATION:
            default:
                icon = R.drawable.plus;
                break;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(icon)
                .setContentTitle(title).setContentText(content).setTicker(content).setVibrate(new long[]{200, 200, 200, 200, 200})
                .setLights(Color.YELLOW, 500, 500).setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                        + "://" + getPackageName() + "/raw/these_hoes_aint_loyal"));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.putExtra("group_id", groupId);
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
        mNotificationManager.notify(type, mBuilder.build());
    }
}