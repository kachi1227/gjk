package com.gjk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.gjk.Constants.GCM_GROUP_DELETE;
import static com.gjk.Constants.GCM_GROUP_INVITE;
import static com.gjk.Constants.GCM_GROUP_REMOVE_MEMBERS;
import static com.gjk.Constants.GCM_IS_TYPING;
import static com.gjk.Constants.GCM_MESSAGE;
import static com.gjk.Constants.GCM_SIDECONVO_DELETE;
import static com.gjk.Constants.GCM_SIDECONVO_INVITE;
import static com.gjk.Constants.GCM_SIDECONVO_REMOVE_MEMBERS;
import static com.gjk.Constants.GCM_WHISPER_DELETE;
import static com.gjk.Constants.GCM_WHISPER_INVITE;
import static com.gjk.Constants.GCM_WHISPER_REMOVE_MEMBERS;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.IS_FROM_GCM;
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
                if (msgType.equals("typing_change")) {
                    sendServiceRequest(GCM_IS_TYPING, extras);
                } else if (msgType.equals("chat_message")) {
                    sendServiceRequest(GCM_MESSAGE, extras);
                } else if (msgType.equals("group_invite")) {
                    sendServiceRequest(GCM_GROUP_INVITE, extras);
                } else if (msgType.equals("group_member_removal")) {
                    sendServiceRequest(GCM_GROUP_REMOVE_MEMBERS, extras);
                } else if (msgType.equals("group_delete")) {
                    sendServiceRequest(GCM_GROUP_DELETE, extras);
                } else if (msgType.equals("side_chat_invite")) {
                    sendServiceRequest(GCM_SIDECONVO_INVITE, extras);
                } else if (msgType.equals("side_chat_member_removal")) {
                    sendServiceRequest(GCM_SIDECONVO_REMOVE_MEMBERS, extras);
                } else if (msgType.equals("side_chat_collapse")) {
                    sendServiceRequest(GCM_SIDECONVO_DELETE, extras);
                } else if (msgType.equals("whisper_invite")) {
                    sendServiceRequest(GCM_WHISPER_INVITE, extras);
                } else if (msgType.equals("whisper_member_removal")) {
                    sendServiceRequest(GCM_WHISPER_REMOVE_MEMBERS, extras);
                } else if (msgType.equals("whisper_delete")) {
                    sendServiceRequest(GCM_WHISPER_DELETE, extras);
                }
            }
        }
    }

    private void sendServiceRequest(String intentType, Bundle extras) {
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, intentType).putExtras(extras).putExtra(IS_FROM_GCM, !intentType.equals(GCM_IS_TYPING));
        Log.d(LOGTAG, String.format("Got %s from GCM: %s", i.getExtras().getString(INTENT_TYPE), i));
        startService(i);
    }
}