package com.gjk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gjk.Constants;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.gjk.Constants.GCM_GROUP_DELETE;
import static com.gjk.Constants.GCM_GROUP_INVITE;
import static com.gjk.Constants.GCM_GROUP_REMOVE_MEMBERS;
import static com.gjk.Constants.GCM_MESSAGE;
import static com.gjk.Constants.GCM_SIDECONVO_DELETE;
import static com.gjk.Constants.GCM_SIDECONVO_INVITE;
import static com.gjk.Constants.GCM_SIDECONVO_REMOVE_MEMBERS;
import static com.gjk.Constants.GCM_WHISPER_DELETE;
import static com.gjk.Constants.GCM_WHISPER_INVITE;
import static com.gjk.Constants.GCM_WHISPER_REMOVE_MEMBERS;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;

public class GcmIntentService extends IntentService {

    private static final String LOGTAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

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

                final String msgType = extras.getString("msg_type");
                if (msgType.equals("chat_message")) {
                    sendServerRequest(GCM_MESSAGE, extras);
                } else if (msgType.equals("group_invite")) {
                    sendServerRequest(GCM_GROUP_INVITE, extras);
                } else if (msgType.equals("group_member_removal")) {
                    sendServerRequest(GCM_GROUP_REMOVE_MEMBERS, extras);
                } else if (msgType.equals("group_delete")) {
                    sendServerRequest(GCM_GROUP_DELETE, extras);
                } else if (msgType.equals("side_chat_invite")) {
                    sendServerRequest(GCM_SIDECONVO_INVITE, extras);
                } else if (msgType.equals("side_chat_member_removal")) {
                    sendServerRequest(GCM_SIDECONVO_REMOVE_MEMBERS, extras);
                } else if (msgType.equals("side_chat_collapse")) {
                    sendServerRequest(GCM_SIDECONVO_DELETE, extras);
                } else if (msgType.equals("whisper_invite")) {
                    sendServerRequest(GCM_WHISPER_INVITE, extras);
                } else if (msgType.equals("whisper_member_removal")) {
                    sendServerRequest(GCM_WHISPER_REMOVE_MEMBERS, extras);
                } else if (msgType.equals("whisper_delete")) {
                    sendServerRequest(GCM_WHISPER_DELETE, extras);
                }
            }
        }

    }

    private void sendServerRequest(String intentType, Bundle extras) {
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(Constants.INTENT_TYPE, intentType).putExtras(extras);
        Log.d(LOGTAG, String.format("Sending %s to server: %s", i.getExtras().getString(Constants.INTENT_TYPE), i));
        startService(i);
    }
}