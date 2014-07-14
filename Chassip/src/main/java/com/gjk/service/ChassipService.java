package com.gjk.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gjk.Application;
import com.gjk.Constants;
import com.gjk.ConvoType;
import com.gjk.MainActivity;
import com.gjk.R;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.Message;
import com.gjk.helper.GeneralHelper;
import com.gjk.net.AddMemberTask;
import com.gjk.net.AddSideChatMembersTask;
import com.gjk.net.AddWhisperMembersTask;
import com.gjk.net.CreateGroupTask;
import com.gjk.net.CreateSideChatTask;
import com.gjk.net.CreateWhisperTask;
import com.gjk.net.DeleteGCMRegTask;
import com.gjk.net.GetGroupMembersTask;
import com.gjk.net.GetMessageTask;
import com.gjk.net.GetMultipleGroupsTask;
import com.gjk.net.GetSideChatMembersTask;
import com.gjk.net.GetSpecificGroupTask;
import com.gjk.net.GetWhisperMembersTask;
import com.gjk.net.HTTPTask;
import com.gjk.net.LoginTask;
import com.gjk.net.NotifyGroupInviteesTask;
import com.gjk.net.NotifyGroupOfMessageTask;
import com.gjk.net.NotifySideChatInviteesTask;
import com.gjk.net.NotifyWhisperInviteesTask;
import com.gjk.net.RegisterTask;
import com.gjk.net.RemoveGroupTask;
import com.gjk.net.RemoveMemberTask;
import com.gjk.net.RemoveSideChatMembersTask;
import com.gjk.net.RemoveWhisperMembersTask;
import com.gjk.net.SendMessageTask;
import com.gjk.net.TaskResult;
import com.gjk.net.UpdateGCMRegTask;
import com.gjk.utils.media2.ImageUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.collect.Maps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.gjk.Constants.ADD_CHAT_MEMBERS_REQUEST;
import static com.gjk.Constants.ADD_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.CAN_FETCH_MORE_MESSAGES;
import static com.gjk.Constants.CHASSIP_ACTION;
import static com.gjk.Constants.CONVO_ID;
import static com.gjk.Constants.CONVO_NAME;
import static com.gjk.Constants.CONVO_TYPE;
import static com.gjk.Constants.CREATE_CHAT_REQUEST;
import static com.gjk.Constants.CREATE_CONVO_REQUEST;
import static com.gjk.Constants.DELETE_CHAT_REQUEST;
import static com.gjk.Constants.DELETE_CHAT_RESPONSE;
import static com.gjk.Constants.EMAIL;
import static com.gjk.Constants.ERROR;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_REQUEST;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_RESPONSE;
import static com.gjk.Constants.FIRST_NAME;
import static com.gjk.Constants.GCM_GROUP_INVITE;
import static com.gjk.Constants.GCM_MESSAGE;
import static com.gjk.Constants.GCM_MESSAGE_RESPONSE;
import static com.gjk.Constants.GCM_SIDECONVO_INVITE;
import static com.gjk.Constants.GCM_WHISPER_INVITE;
import static com.gjk.Constants.GROUP_ID;
import static com.gjk.Constants.GROUP_UPDATE_RESPONSE;
import static com.gjk.Constants.IMAGE_PATH;
import static com.gjk.Constants.IMAGE_URL;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.LAST_NAME;
import static com.gjk.Constants.LOGIN_REQUEST;
import static com.gjk.Constants.LOGIN_RESPONSE;
import static com.gjk.Constants.LOGOUT_REQUEST;
import static com.gjk.Constants.MEMBER_IDS;
import static com.gjk.Constants.MESSAGE;
import static com.gjk.Constants.MESSAGE_RESPONSE_LIMIT_DEFAULT;
import static com.gjk.Constants.NEW_GROUP_INVITE_NOTIFACATION;
import static com.gjk.Constants.NEW_MESSAGE_NOTIFACATION;
import static com.gjk.Constants.NEW_SIDECONVO_INVITE_NOTIFACATION;
import static com.gjk.Constants.NEW_WHISPER_INVITE_NOTIFACATION;
import static com.gjk.Constants.NUM_MESSAGES;
import static com.gjk.Constants.PASSWORD;
import static com.gjk.Constants.PROPERTY_APP_VERSION;
import static com.gjk.Constants.PROPERTY_REG_ID;
import static com.gjk.Constants.REGISTER_FACEBOOK_REQUEST;
import static com.gjk.Constants.REGISTER_REQUEST;
import static com.gjk.Constants.REGISTER_RESPONSE;
import static com.gjk.Constants.REMOVE_CHAT_MEMBERS_REQUEST;
import static com.gjk.Constants.REMOVE_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.SENDER_ID;
import static com.gjk.Constants.SEND_MESSAGE_REQUEST;
import static com.gjk.Constants.SEND_MESSAGE_RESPONSE;
import static com.gjk.Constants.SHOW_TOAST;
import static com.gjk.Constants.UNSUCCESSFUL;
import static com.gjk.Constants.USER_ID;
import static com.gjk.Constants.USER_NAME;
import static com.gjk.helper.DatabaseHelper.addGroup;
import static com.gjk.helper.DatabaseHelper.addGroupMembers;
import static com.gjk.helper.DatabaseHelper.addGroupMessages;
import static com.gjk.helper.DatabaseHelper.addGroups;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;
import static com.gjk.helper.DatabaseHelper.getGroup;
import static com.gjk.helper.DatabaseHelper.getGroupMemberIds;
import static com.gjk.helper.DatabaseHelper.getLeastRecentMessageId;
import static com.gjk.helper.DatabaseHelper.getMostRecentMessageId;
import static com.gjk.helper.DatabaseHelper.removeGroup;
import static com.gjk.helper.DatabaseHelper.removeGroupMember;
import static com.gjk.helper.DatabaseHelper.setAccountUser;

/**
 * @author gpl
 */
public class ChassipService extends IntentService {

    private static final String LOGTAG = "ChassipService";

    public ChassipService() {
        super("ChassipService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                String type = extras.getString(INTENT_TYPE);
                if (type != null) {
                    if (type.equals(SEND_MESSAGE_REQUEST)) {
                        sendMessage(extras);
                    } else if (type.endsWith(GCM_MESSAGE)) {
                        fetchGroupMessagesAfterGcm(new JSONObject(extras.getString("msg_content")).getLong("group_id"));
                    } else if (type.equals(GCM_GROUP_INVITE)) {
                        handleNewGroupInvite(extras);
                    } else if (type.equals(GCM_SIDECONVO_INVITE)) {
                        handleNewSideConvoInvite(extras);
                    } else if (type.equals(GCM_WHISPER_INVITE)) {
                        handleNewWhisperInvite(extras);
                    } else if (type.equals(FETCH_CONVO_MEMBERS_REQUEST)) {
                        fetchConvoMembers(extras, false);
                    } else if (type.equals(CREATE_CHAT_REQUEST)) {
                        createChat(extras);
                    } else if (type.equals(DELETE_CHAT_REQUEST)) {
                        deleteChat(extras);
                    } else if (type.equals(CREATE_CONVO_REQUEST)) {
                        createConvo(extras);
                    } else if (type.equals(ADD_CHAT_MEMBERS_REQUEST)) {
                        addChatMembers(extras);
                    } else if (type.equals(REMOVE_CHAT_MEMBERS_REQUEST)) {
                        removeChatMembers(extras);
                    } else if (type.equals(ADD_CONVO_MEMBERS_REQUEST)) {
                        addConvoMembers(extras);
                    } else if (type.equals(REMOVE_CONVO_MEMBERS_REQUEST)) {
                        removeConvoMembers(extras);
                    } else if (type.equals(FETCH_MORE_MESSAGES_REQUEST)) {
                        fetchGroupMessageBeforeLeastRecent(extras);
                    } else if (type.equals(LOGIN_REQUEST)) {
                        login(extras);
                    } else if (type.equals(REGISTER_REQUEST)) {
                        register(extras);
                    } else if (type.equals(REGISTER_FACEBOOK_REQUEST)) {
                        registerFacebook(extras);
                    } else if (type.equals(LOGOUT_REQUEST)) {
                        logout(extras);
                    }
                } else {
                    reportError(String.format(Locale.getDefault(), "Received null intent type=%s", extras), false);
                }
            } else {
                reportError("Received null extras", false);
            }
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void register(Bundle extras) {
        final String firstName = extras.getString(FIRST_NAME);
        final String lastName = extras.getString(LAST_NAME);
        final String email = extras.getString(EMAIL);
        final String password = extras.getString(PASSWORD);
        String aviPath = extras.getString(IMAGE_PATH);
        HashMap<String, Object> fieldMapping = Maps.newHashMap();
        fieldMapping.put("image", new File(aviPath));
        try {
            if (getRegistrationId().isEmpty()) {
                String regid = GoogleCloudMessaging.getInstance(ChassipService.this).register(SENDER_ID);
                storeRegistrationId(regid);
            }
            if (!getRegistrationId().isEmpty()) {
                new RegisterTask(ChassipService.this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            JSONObject response = (JSONObject) result.getExtraInfo();
                            try {
                                setAccountUser(response);
                                Application.get().getPreferences().edit().putString(Constants.LOGIN_JSON, response.toString())
                                        .commit();
                                updateChassipGcm(getAccountUserId(),
                                        Application.get().getPreferences().getString(PROPERTY_REG_ID, "abc"), new AuthenticatedAction() {
                                            @Override
                                            public void doThis() {
                                                Intent i = new Intent(CHASSIP_ACTION);
                                                i.putExtra(INTENT_TYPE, LOGIN_RESPONSE);
                                                LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
                                            }
                                        }
                                );
                            } catch (Exception e) {
                                reportError(e.getMessage(), true);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), true);
                        }
                    }
                }, firstName, lastName, email, password, fieldMapping);
            } else {
                reportError("GCM registration unsuccessful", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportError(String.format("GCM registration unsuccessful: %s", e), true);
        }
    }

    private void registerFacebook(final Bundle extras) {
        new FetchFacebookAvi().execute(extras);
    }

    private void login(Bundle extras) {
        final String email = extras.getString(EMAIL);
        final String password = extras.getString(PASSWORD);
        try {
            if (getRegistrationId().isEmpty()) {
                String regid = GoogleCloudMessaging.getInstance(ChassipService.this).register(SENDER_ID);
                storeRegistrationId(regid);
            }
            if (!getRegistrationId().isEmpty()) {
                new LoginTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            JSONObject response = (JSONObject) result.getExtraInfo();
                            try {
                                setAccountUser(response);
                                Application.get().getPreferences().edit().putString(Constants.LOGIN_JSON, response.toString())
                                        .commit();
                                updateChassipGcm(getAccountUserId(),
                                        Application.get().getPreferences().getString(PROPERTY_REG_ID, "abc"), new AuthenticatedAction() {
                                            @Override
                                            public void doThis() {
                                                Intent i = new Intent(CHASSIP_ACTION);
                                                i.putExtra(INTENT_TYPE, REGISTER_RESPONSE);
                                                LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
                                            }
                                        }
                                );
                            } catch (Exception e) {
                                reportError(e.getMessage(), true);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), true);
                        }
                    }
                }, email, password);
            } else {
                reportError("GCM registration unsuccessful", true);
            }
        } catch (Exception e) {
            reportError(String.format("GCM registration unsuccessful: %s", e), true);
        }
    }

    private String getRegistrationId() {
        final SharedPreferences prefs = Application.get().getPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(LOGTAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Application.get().getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(LOGTAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = Application.get().getPreferences();
        final int appVersion = Application.get().getAppVersion();
        Log.i(LOGTAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void updateChassipGcm(long id, String gcm, final AuthenticatedAction action) {
        new UpdateGCMRegTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    fetchAllGroups();
                    action.doThis();
                } else {
                    reportError(result.getMessage(), true);
                }
            }
        }, id, gcm, "ANDROID");
    }

    private void addConvoMembers(final Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long convoId = extras.getLong(CONVO_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long[] selectedIds = extras.getLongArray(MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new AddSideChatMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            notifyUiOfGroupUpdate(groupId);
                            fetchConvoMembers(extras, true);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
            case WHISPER:
            default:
                new AddWhisperMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            notifyUiOfGroupUpdate(groupId);
                            fetchConvoMembers(extras, true);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
        }
    }

    private void removeConvoMembers(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long convoId = extras.getLong(CONVO_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long[] selectedIds = extras.getLongArray(MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new RemoveSideChatMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            notifyUiOfGroupUpdate(groupId);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
            case WHISPER:
            default:
                new RemoveWhisperMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            notifyUiOfGroupUpdate(groupId);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
        }
    }

    private void deleteChat(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        new RemoveGroupTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    removeGroup(groupId);
                    notifyUiOfGroupDelete(groupId);
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId);
    }

    private void removeChatMembers(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long[] membersIds = extras.getLongArray(MEMBER_IDS);
        new RemoveMemberTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    for (long memberId : membersIds) {
                        removeGroupMember(groupId, memberId);
                    }
                    notifyUiOfGroupUpdate(groupId);
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId, membersIds);
    }

    private void addChatMembers(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long[] membersIds = extras.getLongArray(MEMBER_IDS);
        new AddMemberTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    fetchGroupMembers(groupId, true);
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId, membersIds);
    }

    private void createConvo(final Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        String convoName = extras.getString(CONVO_NAME);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long[] memberIds = extras.getLongArray(MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new CreateSideChatTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                fetchGroup(groupId);
                                fetchConvoMembers(extras, true);
                            } catch (Exception e) {
                                reportError(e.getMessage(), false);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, groupId, getAccountUserId(), memberIds, convoName);
                break;
            case WHISPER:
            default:
                new CreateWhisperTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                fetchGroup(groupId);
                                fetchConvoMembers(extras, true);
                            } catch (Exception e) {
                                reportError(e.getMessage(), false);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, groupId, getAccountUserId(), memberIds, convoName);
                break;
        }
    }

    private void createChat(Bundle extras) {
        String chatName = extras.getString(CONVO_NAME);
        final long[] memberIds = extras.getLongArray(MEMBER_IDS);
        String aviPath = extras.getString(IMAGE_PATH);
        HashMap<String, Object> fieldMapping = Maps.newHashMap();
        fieldMapping.put("image", new File(aviPath));
        new CreateGroupTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONObject response = (JSONObject) result.getExtraInfo();
                    try {
                        fetchGroup(response.getLong("id"));
                        notifyGroupOfInvite(response.getLong("id"), memberIds);
                    } catch (Exception e) {
                        reportUnsuccess(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, getAccountUserId(), chatName, memberIds, fieldMapping);
    }

    private void fetchConvoMembers(Bundle extras, final boolean notify) {
        final long groupId = extras.getLong(GROUP_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long convoId = extras.getLong(CONVO_ID);
        switch (convoType) {
            case SIDE_CONVO:
                new GetSideChatMembersTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                JSONArray response = (JSONArray) result.getExtraInfo();
                                long memberIds[] = new long[response.length()];
                                for (int i = 0; i < response.length(); i++) {
                                    memberIds[i] = response.getJSONObject(i).getLong("id");
                                }
                                notifyUiOfConvoMembers(memberIds, groupId, convoId);
                                if (notify) {
                                    notifyConvoMembers(memberIds, convoType, groupId);
                                }
                            } catch (Exception e) {
                                reportError(e.getMessage(), false);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId);
                break;
            case WHISPER:
            default:
                new GetWhisperMembersTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                JSONArray response = (JSONArray) result.getExtraInfo();
                                long memberIds[] = new long[response.length()];
                                for (int i = 0; i < response.length(); i++) {
                                    memberIds[i] = response.getJSONObject(i).getLong("id");
                                }
                                notifyUiOfConvoMembers(memberIds, groupId, convoId);
                                if (notify) {
                                    notifyConvoMembers(memberIds, convoType, groupId);
                                }
                            } catch (Exception e) {
                                reportError(e.getMessage(), false);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId);
                break;
        }
    }

    private void notifyConvoMembers(long[] memberIds, ConvoType convoType, long convoId) {
        switch (convoType) {
            case SIDE_CONVO:
                new NotifySideChatInviteesTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            Log.i(LOGTAG, "Notified side convo invitees");
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, getAccountUserId(), convoId, memberIds);
                break;
            case WHISPER:
            default:
                new NotifyWhisperInviteesTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            Log.i(LOGTAG, "Notified whisper invitees");
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, getAccountUserId(), convoId, memberIds);
                break;
        }
    }

    private long[] notifyUiOfConvoMembers(long[] memberIds, long groupId, long convoId) throws JSONException {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, FETCH_CONVO_MEMBERS_RESPONSE)
                .putExtra(GROUP_ID, groupId)
                .putExtra(CONVO_ID, convoId)
                .putExtra(MEMBER_IDS, memberIds);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
        return memberIds;
    }

    private void sendMessage(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        int convoType = extras.getInt(CONVO_TYPE);
        long convoId = extras.getLong(CONVO_ID);
        String message = extras.getString(MESSAGE);
        if (extras.containsKey(IMAGE_PATH)) {
            HashMap<String, Object> fieldMapping = Maps.newHashMap();
            File f = new File(extras.getString(IMAGE_PATH));
            if (f.exists()) {
                Log.d(LOGTAG, f.getAbsolutePath());
            }
            fieldMapping.put("image", f);
            new SendMessageTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if (result.getResponseCode() == 1) {
                        fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
                            @Override
                            public void doThis(List<Message> messages) {
                                Intent i = new Intent(CHASSIP_ACTION);
                                i.putExtra(INTENT_TYPE, SEND_MESSAGE_RESPONSE)
                                        .putExtra(GROUP_ID, groupId)
                                        .putExtra(NUM_MESSAGES, messages.size());
                                LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
                            }
                        });
                        notifyGroupOfMessage(groupId);
                    } else {
                        reportUnsuccess(result.getMessage(), false);
                    }
                }
            }, getAccountUserId(), groupId, convoType, convoId, message, fieldMapping);
        } else {
            new SendMessageTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if (result.getResponseCode() == 1) {
                        fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
                            @Override
                            public void doThis(List<Message> messages) {
                                Intent i = new Intent(CHASSIP_ACTION);
                                i.putExtra(INTENT_TYPE, SEND_MESSAGE_RESPONSE)
                                        .putExtra(GROUP_ID, groupId)
                                        .putExtra(NUM_MESSAGES, messages.size());
                                LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
                            }
                        });
                        notifyGroupOfMessage(groupId);
                    } else {
                        reportUnsuccess(result.getMessage(), false);
                    }
                }
            }, getAccountUserId(), groupId, convoType, convoId, message);
        }
    }

    private void fetchGroupMessagesAfterGcm(final long groupId) {
        fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
            @Override
            public void doThis(List<Message> messages) {
                for (Message m : messages) {
                    if (m != null && m.getSenderId() != getAccountUserId()) {
                        if (!Application.get().isActivityIsInForeground() || m.getGroupId() != groupId) {
                            notifyNewMessage(m);
                        }
                    }
                }
                Intent i = new Intent(CHASSIP_ACTION);
                i.putExtra(INTENT_TYPE, GCM_MESSAGE_RESPONSE)
                        .putExtra(GROUP_ID, groupId)
                        .putExtra(NUM_MESSAGES, messages.size());
                LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
            }
        });
    }

    private void fetchGroupMessagesAfterLogin(final long groupId) {
        fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
            @Override
            public void doThis(List<Message> messages) {
                notifyUiOfGroupUpdate(groupId);
            }
        });
    }

    private void fetchMostRecentGroupMessages(final long groupId, final FetchGroupMessagesAction action) {
        JSONArray jsonArray = new JSONArray();
        long id = getMostRecentMessageId(groupId);
        try {
            jsonArray.put(0, id).put(1, -1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new GetMessageTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONArray messages = (JSONArray) result.getExtraInfo();
                    try {
                        List<Message> groupMessages = addGroupMessages(messages, false);
                        if (action != null) {
                            action.doThis(groupMessages);
                        }
                    } catch (Exception e) {
                        reportError(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, getAccountUserId(), groupId, jsonArray);
    }

    private void fetchGroupMessageBeforeLeastRecent(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long convoId = extras.getLong(CONVO_ID);
        JSONArray jsonArray = new JSONArray();
        long id = getLeastRecentMessageId(groupId);
        try {
            jsonArray.put(0, -1).put(1, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new GetMessageTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONArray messages = (JSONArray) result.getExtraInfo();
                    try {
                        addGroupMessages(messages, false);
                        Intent i = new Intent(CHASSIP_ACTION);
                        i.putExtra(INTENT_TYPE, FETCH_MORE_MESSAGES_RESPONSE)
                                .putExtra(GROUP_ID, groupId)
                                .putExtra(CONVO_ID, convoId)
                                .putExtra(CAN_FETCH_MORE_MESSAGES, messages.length() == MESSAGE_RESPONSE_LIMIT_DEFAULT);
                        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
                    } catch (Exception e) {
                        reportError(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, getAccountUserId(), groupId, jsonArray);
    }

    private void notifyGroupOfMessage(long groupId) {
        new NotifyGroupOfMessageTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified group invitees");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId);
    }

    private void notifyGroupOfInvite(long chatId, long[] ids) {
        final Context ctx = this;
        new NotifyGroupInviteesTask(ctx, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified group invitees");
                } else {
                    GeneralHelper.reportMessage(ctx, LOGTAG, result.getMessage(), false);
                }
            }
        }, getAccountUserId(), chatId, ids);
    }

    private void fetchGroup(long chatId) {
        final Context ctx = this;
        new GetSpecificGroupTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONObject response = (JSONObject) result.getExtraInfo();
                    try {
                        Group g = addGroup(response, true);
                        fetchMembersAndMessages(g.getGlobalId());
                        notifyUiOfGroupUpdate(g.getGlobalId());
                    } catch (Exception e) {
                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage(), false);
                    }
                } else {
                    GeneralHelper.reportMessage(ctx, LOGTAG, result.getMessage(), false);
                }
            }
        }, getAccountUserId(), chatId);
    }

    private void fetchAllGroups() {
        new GetMultipleGroupsTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONArray response = (JSONArray) result.getExtraInfo();
                    try {
                        List<Group> groups = addGroups(response, false);
                        for (Group g : groups) {
                            fetchMembersAndMessages(g.getGlobalId());
                        }
                    } catch (Exception e) {
                        reportError(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, getAccountUserId());
    }

    private void fetchGroupMembers(final long chatId, final boolean notify) {
        new GetGroupMembersTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONArray response = (JSONArray) result.getExtraInfo();
                    try {
                        addGroupMembers(response, chatId, false);
                        notifyUiOfGroupUpdate(chatId);
                        if (notify) {
                            notifyNewChatMembers(getGroupMemberIds(chatId));
                        }
                    } catch (Exception e) {
                        reportError(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, chatId);
    }

    private void notifyNewChatMembers(final long[] members) {
        new NotifyGroupInviteesTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified group invitees");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, getAccountUserId(), Application.get().getCurrentChat().getGlobalId(), members);
    }

    private void reportError(String message, boolean showToast) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, ERROR)
                .putExtra(MESSAGE, message)
                .putExtra(SHOW_TOAST, showToast);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void reportUnsuccess(String message, boolean showToast) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, UNSUCCESSFUL).putExtra(MESSAGE, message)
                .putExtra(SHOW_TOAST, showToast);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void handleNewGroupInvite(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content"))
                    .getJSONObject("content");
            JSONObject group = content.getJSONObject("group");
            JSONObject inviter = content.getJSONObject("inviter");
            Group g = addGroup(group, false);
            notifyUiOfGroupUpdate(g.getGlobalId());
            notifyNewGroup(g, inviter);
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleNewWhisperInvite(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content"))
                    .getJSONObject("content");
            final JSONArray members = content.getJSONArray("members");
            boolean isInWhisper = false;
            for (int i = 0; i < members.length(); i++) {
                isInWhisper |= getAccountUserId() == members.getJSONObject(i).getLong("id");
            }
            if (!isInWhisper) {
                return;
            }

            final JSONObject whisper = content.getJSONObject("whisper");
            final JSONObject inviter = content.getJSONObject("inviter");
            new GetSpecificGroupTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if (result.getResponseCode() == 1) {
                        try {
                            JSONObject response = (JSONObject) result.getExtraInfo();
                            Group newG = addGroup(response, false);
                            notifyUiOfGroupUpdate(newG.getGlobalId());
                            notifyNewWhisper(newG, whisper, members, inviter);
                        } catch (Exception e) {
                            reportError(e.getMessage(), false);
                        }
                    } else {
                        reportUnsuccess(result.getMessage(), false);
                    }
                }
            }, getAccountUserId(), whisper.getLong("group_id"));
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleNewSideConvoInvite(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content"))
                    .getJSONObject("content");
            final JSONObject sideConvo = content.getJSONObject("side_chat");
            final JSONArray members = content.getJSONArray("members");
            final JSONObject inviter = content.getJSONObject("inviter");
            new GetSpecificGroupTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if (result.getResponseCode() == 1) {
                        try {
                            JSONObject response = (JSONObject) result.getExtraInfo();
                            Group newG = addGroup(response, false);
                            notifyUiOfGroupUpdate(newG.getGlobalId());
                            notifyNewSideConvo(newG, sideConvo, members, inviter);
                        } catch (Exception e) {
                            reportError(e.getMessage(), false);
                        }
                    } else {
                        reportUnsuccess(result.getMessage(), false);
                    }
                }
            }, getAccountUserId(), sideConvo.getLong("group_id"));
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void logout(Bundle extras) {
        final String name = extras.getString(USER_NAME);
        long id = extras.getLong(USER_ID);
        new DeleteGCMRegTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.d(LOGTAG, "Logging out " + name);
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, id, true);
    }

    private void fetchMembersAndMessages(long groupId) {
        fetchGroupMembers(groupId, false);
        fetchGroupMessagesAfterLogin(groupId);
    }

    private void notifyUiOfGroupUpdate(long groupId) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, GROUP_UPDATE_RESPONSE)
                .putExtra(GROUP_ID, groupId);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void notifyUiOfGroupDelete(long groupId) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, DELETE_CHAT_RESPONSE)
                .putExtra(GROUP_ID, groupId);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void notifyNewMessage(Message m) {
        String content = String.format(Locale.getDefault(), "%s %s: %s", m.getSenderFirstName(), m.getSenderLastName(),
                m.getContent());
        String title = String.format(Locale.getDefault(), "%s", getGroup(m.getGroupId()).getName());
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
                isInSideConvo |= getAccountUserId() == members.getJSONObject(i).getLong("id");
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
            String content = String.format(Locale.getDefault(),
                    "%s %s added you to %s's new whisper in %s called \"%s\"", inviter.get("first_name"),
                    inviter.get("last_name"), w.getString("creator_name"), g.getName(), w.getString("name"));
            String title = "New Whisper Invite";
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
                icon = R.drawable.ic_action_add_group;
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

    private interface FetchGroupMessagesAction {
        void doThis(List<Message> messages);

    }

    private interface AuthenticatedAction {
        void doThis();
    }

    private class FetchFacebookAvi extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            if (params[0] != null) {
                try {
                    URL url = new URL(params[0].getString(IMAGE_URL));
                    InputStream input = url.openStream();
                    File file = ImageUtil.createImageFile(ChassipService.this);
                    OutputStream output = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.close();
                    input.close();
                    params[0].putString(IMAGE_PATH, file.getAbsolutePath());
                    register(params[0]);
                } catch (Exception e) {
                    reportError("Downloading facebook avi failed, the fuck!?", true);
                }
            }
            return null;
        }
    }
}
