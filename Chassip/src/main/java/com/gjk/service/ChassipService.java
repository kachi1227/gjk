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
import com.gjk.ConvoType;
import com.gjk.MainActivity;
import com.gjk.R;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.database.objects.Message;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.net.AddMembersTask;
import com.gjk.net.AddSideChatMembersTask;
import com.gjk.net.AddWhisperMembersTask;
import com.gjk.net.CollapseSideChatTask;
import com.gjk.net.CreateGroupTask;
import com.gjk.net.CreateSideChatTask;
import com.gjk.net.CreateWhisperTask;
import com.gjk.net.DeleteGcmRegTask;
import com.gjk.net.DeleteWhisperTask;
import com.gjk.net.GetGroupMembersTask;
import com.gjk.net.GetGroupTask;
import com.gjk.net.GetGroupsTask;
import com.gjk.net.GetMessagesTask;
import com.gjk.net.GetSideChatMembersTask;
import com.gjk.net.GetWhisperMembersTask;
import com.gjk.net.HTTPTask;
import com.gjk.net.LoginTask;
import com.gjk.net.NotifyGroupInviteesTask;
import com.gjk.net.NotifyGroupMembersOfUserTypingChangeTask;
import com.gjk.net.NotifyGroupOfMessageTask;
import com.gjk.net.NotifyMembersOfGroupDeletionTask;
import com.gjk.net.NotifyMembersOfGroupMemberRemovalTask;
import com.gjk.net.NotifySideChatInviteesTask;
import com.gjk.net.NotifySideChatMembersOfCollapseTask;
import com.gjk.net.NotifySideChatMembersOfMemberRemovalTask;
import com.gjk.net.NotifyWhisperInviteesTask;
import com.gjk.net.NotifyWhisperMembersOfDeletionTask;
import com.gjk.net.NotifyWhisperMembersOfMemberRemovalTask;
import com.gjk.net.Pool;
import com.gjk.net.RegisterTask;
import com.gjk.net.RemoveGroupTask;
import com.gjk.net.RemoveMembersTask;
import com.gjk.net.RemoveSideChatMembersTask;
import com.gjk.net.RemoveWhisperMembersTask;
import com.gjk.net.SendMessageTask;
import com.gjk.net.TaskResult;
import com.gjk.net.UpdateGcmRegTask;
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
import java.util.Timer;
import java.util.TimerTask;

import static com.gjk.Constants.ADD_CHAT_MEMBERS_REQUEST;
import static com.gjk.Constants.ADD_CHAT_MEMBERS_RESPONSE;
import static com.gjk.Constants.ADD_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.ADD_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.ALL_MEMBER_IDS;
import static com.gjk.Constants.CAN_FETCH_MORE_MESSAGES;
import static com.gjk.Constants.CHASSIP_ACTION;
import static com.gjk.Constants.CONVO_ID;
import static com.gjk.Constants.CONVO_NAME;
import static com.gjk.Constants.CONVO_TYPE;
import static com.gjk.Constants.CREATE_CHAT_REQUEST;
import static com.gjk.Constants.CREATE_CONVO_REQUEST;
import static com.gjk.Constants.CREATE_CONVO_RESPONSE;
import static com.gjk.Constants.DELETE_CHAT_REQUEST;
import static com.gjk.Constants.DELETE_CHAT_RESPONSE;
import static com.gjk.Constants.DELETE_CONVO_REQUEST;
import static com.gjk.Constants.DELETE_CONVO_RESPONSE;
import static com.gjk.Constants.EMAIL;
import static com.gjk.Constants.ERROR;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_REQUEST;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_RESPONSE;
import static com.gjk.Constants.FIRST_NAME;
import static com.gjk.Constants.GCM_GROUP_DELETE;
import static com.gjk.Constants.GCM_GROUP_INVITE;
import static com.gjk.Constants.GCM_GROUP_REMOVE_MEMBERS;
import static com.gjk.Constants.GCM_IS_TYPING;
import static com.gjk.Constants.GCM_MESSAGE;
import static com.gjk.Constants.GCM_MESSAGE_RESPONSE;
import static com.gjk.Constants.GCM_SIDECONVO_DELETE;
import static com.gjk.Constants.GCM_SIDECONVO_INVITE;
import static com.gjk.Constants.GCM_SIDECONVO_REMOVE_MEMBERS;
import static com.gjk.Constants.GCM_WHISPER_DELETE;
import static com.gjk.Constants.GCM_WHISPER_INVITE;
import static com.gjk.Constants.GCM_WHISPER_REMOVE_MEMBERS;
import static com.gjk.Constants.GROUP_ID;
import static com.gjk.Constants.GROUP_UPDATE_RESPONSE;
import static com.gjk.Constants.IMAGE_PATH;
import static com.gjk.Constants.IMAGE_URL;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.IS_FROM_GCM;
import static com.gjk.Constants.IS_TYPING;
import static com.gjk.Constants.IS_TYPING_REQUEST;
import static com.gjk.Constants.LAST_NAME;
import static com.gjk.Constants.LOGIN_JSON;
import static com.gjk.Constants.LOGIN_REQUEST;
import static com.gjk.Constants.LOGIN_RESPONSE;
import static com.gjk.Constants.LOGOUT_REQUEST;
import static com.gjk.Constants.MEMBER_IDS;
import static com.gjk.Constants.MESSAGE;
import static com.gjk.Constants.MESSAGE_RESPONSE_LIMIT_DEFAULT;
import static com.gjk.Constants.NEW_GROUP_INVITE_NOTIFACATION;
import static com.gjk.Constants.NEW_GROUP_MEMBER_INVITE_NOTIFACATION;
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
import static com.gjk.Constants.REMOVE_CHAT_MEMBERS_RESPONSE;
import static com.gjk.Constants.REMOVE_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.REMOVE_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.SENDER_ID;
import static com.gjk.Constants.SEND_MESSAGE_REQUEST;
import static com.gjk.Constants.SEND_MESSAGE_RESPONSE;
import static com.gjk.Constants.SHOW_TOAST;
import static com.gjk.Constants.UNSUCCESSFUL;
import static com.gjk.Constants.USER_ID;
import static com.gjk.Constants.USER_NAME;
import static com.gjk.Constants.VERBOSE;
import static com.gjk.helper.DatabaseHelper.addGroup;
import static com.gjk.helper.DatabaseHelper.addGroupMembers;
import static com.gjk.helper.DatabaseHelper.addGroupMessages;
import static com.gjk.helper.DatabaseHelper.addGroups;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;
import static com.gjk.helper.DatabaseHelper.getGroup;
import static com.gjk.helper.DatabaseHelper.getGroupIdFromSideConvoId;
import static com.gjk.helper.DatabaseHelper.getGroupIdFromWhisperId;
import static com.gjk.helper.DatabaseHelper.getGroupMemberIds;
import static com.gjk.helper.DatabaseHelper.getGroupMembers;
import static com.gjk.helper.DatabaseHelper.getLeastRecentMessageId;
import static com.gjk.helper.DatabaseHelper.getMostRecentMessageId;
import static com.gjk.helper.DatabaseHelper.getOtherGroupMemberIds;
import static com.gjk.helper.DatabaseHelper.groupExists;
import static com.gjk.helper.DatabaseHelper.removeGroup;
import static com.gjk.helper.DatabaseHelper.removeGroupMember;
import static com.gjk.helper.DatabaseHelper.setAccountUser;

/**
 * @author gpl
 */
public class ChassipService extends IntentService {

    private static final String LOGTAG = "ChassipService";

    private static BackgroundTaskListener backgroundTaskListener;

    public ChassipService() {
        super("ChassipService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            final Bundle extras = intent.getExtras();

            if (extras != null) {

                final String type = extras.getString(INTENT_TYPE);
                if (extras.containsKey(IS_FROM_GCM) && extras.getBoolean(IS_FROM_GCM)) {

                    Pool.resetListener();

                    if (backgroundTaskListener == null) {
                        backgroundTaskListener = new BackgroundTaskListener(intent);
                    } else {
                        backgroundTaskListener.cancel();
                    }
                    Pool.setListener(backgroundTaskListener);
                    backgroundTaskListener.set();
                }

                if (type != null) {
                    if (type.equals(GCM_IS_TYPING)) {
                        gcmIsTyping(extras);
                    } else if (type.equals(IS_TYPING_REQUEST)) {
                        isTyping(extras);
                    } else if (type.equals(SEND_MESSAGE_REQUEST)) {
                        sendMessage(extras);
                    } else if (type.endsWith(GCM_MESSAGE)) {
                        fetchGroupMessagesAfterGcm(extras);
                    } else if (type.equals(GCM_GROUP_INVITE)) {
                        handleGroupInvite(extras);
                    } else if (type.equals(GCM_GROUP_REMOVE_MEMBERS)) {
                        handleGroupRemoveMembers(extras);
                    } else if (type.equals(GCM_GROUP_DELETE)) {
                        handleGroupDeletion(extras);
                    } else if (type.equals(GCM_SIDECONVO_INVITE)) {
                        handleSideConvoInvite(extras);
                    } else if (type.equals(GCM_SIDECONVO_REMOVE_MEMBERS)) {
                        handleSideConvoRemoveMembers(extras);
                    } else if (type.equals(GCM_SIDECONVO_DELETE)) {
                        handleSideConvoDeletion(extras);
                    } else if (type.equals(GCM_WHISPER_INVITE)) {
                        handleWhisperInvite(extras);
                    } else if (type.equals(GCM_WHISPER_REMOVE_MEMBERS)) {
                        handleWhisperRemoveMembers(extras);
                    } else if (type.equals(GCM_WHISPER_DELETE)) {
                        handleWhisperDeletion(extras);
                    } else if (type.equals(FETCH_CONVO_MEMBERS_REQUEST)) {
                        fetchConvoMembersRequest(extras);
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
                    } else if (type.equals(DELETE_CONVO_REQUEST)) {
                        deleteConvo(extras);
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
                                Application.get().getPreferences().edit().putString(LOGIN_JSON, response.toString())
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
                                Application.get().getPreferences().edit().putString(LOGIN_JSON, response.toString())
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
        new UpdateGcmRegTask(this, new HTTPTask.HTTPTaskListener() {
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
        final long convoId = extras.getLong(CONVO_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long[] selectedIds = extras.getLongArray(MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new AddSideChatMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            fetchConvoMembers(extras, new FetchConvoMemberAction() {
                                @Override
                                public void doThis(long convoId, ConvoType convoType, long[] memberIds,
                                                   long[] allMemberIds) {
                                    notifyConvoMembers(allMemberIds, convoType, convoId);
                                }
                            });
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
            case WHISPER:
                new AddWhisperMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            fetchConvoMembers(extras, new FetchConvoMemberAction() {
                                @Override
                                public void doThis(long convoId, ConvoType convoType, long[] memberIds,
                                                   long[] allMemberIds) {
                                    notifyConvoMembers(allMemberIds, convoType, convoId);
                                }
                            });
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
            default:
                break;
        }
    }

    private void removeConvoMembers(final Bundle extras) {
        final long convoId = extras.getLong(CONVO_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long[] selectedIds = extras.getLongArray(MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new RemoveSideChatMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            updateUiForRemoveConvoMembers(extras);
                            notifySideConvoOfMemberRemoval(convoId, selectedIds);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
            case WHISPER:
                new RemoveWhisperMembersTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            updateUiForRemoveConvoMembers(extras);
                            notifyWhisperOfMemberRemoval(convoId, selectedIds);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId, selectedIds);
                break;
            default:
                break;
        }
    }

    private void deleteConvo(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long convoId = extras.getLong(CONVO_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long[] memberIds = extras.getLongArray(MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new CollapseSideChatTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            finalizeSideConvoDelete(groupId, convoId);
                            notifySideConvoOfDelete(convoId, memberIds);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId);
                break;
            case WHISPER:
                new DeleteWhisperTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            finalizeWhisperDelete(groupId, convoId);
                            notifyWhisperOfDelete(convoId, memberIds);
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId);
                break;
            default:
                break;
        }
    }

    private void finalizeSideConvoDelete(long groupId, final long sideConvoId) {
        int numOfCollaspedMessages = DatabaseHelper.collaspeSideConvoMessages(sideConvoId);
        reportVerbose("Collasping " + numOfCollaspedMessages + " messages into main chat", true);
        fetchGroup(groupId, new FetchGroupAction() {
            @Override
            public void doThis(long groupId) {
                updateUiForRemoveConvo(groupId, sideConvoId);
            }
        });
    }

    private void finalizeWhisperDelete(long groupId, final long whisperId) {
        DatabaseHelper.removeWhisperMessages(whisperId);
        fetchGroup(groupId, new FetchGroupAction() {
            @Override
            public void doThis(long groupId) {
                updateUiForRemoveConvo(groupId, whisperId);
            }
        });
    }

    private void notifySideConvoOfMemberRemoval(long convoId, long[] memberIds) {
        new NotifySideChatMembersOfMemberRemovalTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified side-convo of member removal");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, convoId, memberIds);
    }

    private void notifyWhisperOfMemberRemoval(long convoId, long[] memberIds) {
        new NotifyWhisperMembersOfMemberRemovalTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified whisper of member removal");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, convoId, memberIds);
    }

    private void notifySideConvoOfDelete(long convoId, long[] memberIds) {
        new NotifySideChatMembersOfCollapseTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified side-convo of deletion");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, convoId, memberIds);
    }

    private void notifyWhisperOfDelete(long convoId, long[] memberIds) {
        new NotifyWhisperMembersOfDeletionTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified whisper of deletion");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, convoId, memberIds);
    }

    private void deleteChat(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        new RemoveGroupTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    notifyGroupOfDelete(groupId);
                    finalizeDeleteChat(groupId);
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId);
    }

    private void finalizeDeleteChat(long groupId) {
        removeGroup(groupId);
        updateUiForGroupDelete(groupId);
    }

    private void notifyGroupOfDelete(long groupId) {
        new NotifyMembersOfGroupDeletionTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified group invitees");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId, getOtherGroupMemberIds(groupId));
    }

    private void removeChatMembers(final Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long[] memberIds = extras.getLongArray(MEMBER_IDS);
        new RemoveMembersTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    notifyGroupOfMemberRemoval(groupId, memberIds);
                    finalizeChatMemberRemoval(extras);
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId, memberIds);
    }

    private void notifyGroupOfMemberRemoval(long groupId, long[] memberIds) {
        new NotifyMembersOfGroupMemberRemovalTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified group of members being removed");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId, memberIds);
    }

    private void finalizeChatMemberRemoval(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long[] memberIds = extras.getLongArray(MEMBER_IDS);
        for (long memberId : memberIds) {
            removeGroupMember(groupId, memberId);
        }
        updateUiForRemoveChatMember(extras);
    }

    private void addChatMembers(final Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final long[] membersIds = extras.getLongArray(MEMBER_IDS);
        new AddMembersTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    fetchGroupMembers(groupId, new FetchGroupMemberAction() {
                        @Override
                        public void doThis(long groupId) {
                            notifyNewChatMembers(getOtherGroupMemberIds(groupId));
                            updateUiForAddChatMembers(extras);
                        }
                    });
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
                                final JSONObject response = (JSONObject) result.getExtraInfo();
                                final long convoId = response.getLong("id");
                                final String name = response.getString("name");
                                fetchGroup(groupId, new FetchGroupAction() {
                                    @Override
                                    public void doThis(long groupId) {
                                        fetchMembersAndMessagesForConvo(groupId);
                                        updateUiForAddConvo(groupId, convoId, name, ConvoType.SIDE_CONVO);
                                    }
                                });
                                final Bundle moreExtras = new Bundle(extras);
                                moreExtras.putLong(CONVO_ID, convoId);
                                fetchConvoMembers(moreExtras, new FetchConvoMemberAction() {
                                    @Override
                                    public void doThis(long convoId, ConvoType convoType, long[] memberIds,
                                                       long[] allMemberIds) {
                                        notifyConvoMembers(allMemberIds, convoType, convoId);
                                    }
                                });
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
                new CreateWhisperTask(this, new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                final JSONObject response = (JSONObject) result.getExtraInfo();
                                final long convoId = response.getLong("id");
                                final String name = response.getString("name");
                                fetchGroup(groupId, new FetchGroupAction() {
                                    @Override
                                    public void doThis(long groupId) {
                                        fetchMembersAndMessagesForConvo(groupId);
                                        updateUiForAddConvo(groupId, convoId, name, ConvoType.WHISPER);
                                    }
                                });
                                final Bundle moreExtras = new Bundle(extras);
                                moreExtras.putLong(CONVO_ID, convoId);
                                fetchConvoMembers(moreExtras, new FetchConvoMemberAction() {
                                    @Override
                                    public void doThis(long convoId, ConvoType convoType, long[] memberIds,
                                                       long[] allMemberIds) {
                                        notifyConvoMembers(allMemberIds, convoType, convoId);
                                    }
                                });
                            } catch (Exception e) {
                                reportError(e.getMessage(), false);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, groupId, getAccountUserId(), memberIds, convoName);
                break;
            default:
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
                        fetchGroup(response.getLong("id"), new FetchGroupAction() {
                            @Override
                            public void doThis(long groupId) {
                                updateUiForGroupUpdate();
                                fetchMembersAndMessagesForGroup(groupId);
                            }
                        });
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
            default:
                break;
        }
    }

    private void fetchConvoMembersRequest(Bundle extras) {
        fetchConvoMembers(extras, new FetchConvoMemberAction() {
            @Override
            public void doThis(long convoId, ConvoType convoType, long[] memberIds,
                               long[] allMemberIds) {
            }
        });
    }

    private void fetchConvoMembers(Bundle extras, final FetchConvoMemberAction action) {
        final long groupId = extras.getLong(GROUP_ID);
        final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
        final long convoId = extras.getLong(CONVO_ID);
        final long[][] memberIds = {extras.getLongArray(MEMBER_IDS)};
        final long[] allMemberIds = extras.getLongArray(ALL_MEMBER_IDS);
        switch (convoType) {
            case SIDE_CONVO:
                new GetSideChatMembersTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                if (memberIds[0] == null) {
                                    JSONArray response = (JSONArray) result.getExtraInfo();
                                    memberIds[0] = new long[response.length()];
                                    for (int i = 0; i < response.length(); i++) {
                                        memberIds[0][i] = response.getJSONObject(i).getLong("id");
                                    }
                                }
                                notifyUiOfConvoMembers(memberIds[0], groupId, convoId);
                                action.doThis(convoId, convoType, memberIds[0],
                                        GeneralHelper.concatLong(memberIds[0], allMemberIds));
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
                new GetWhisperMembersTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                    @Override
                    public void onTaskComplete(TaskResult result) {
                        if (result.getResponseCode() == 1) {
                            try {
                                if (memberIds[0] == null) {
                                    JSONArray response = (JSONArray) result.getExtraInfo();
                                    memberIds[0] = new long[response.length()];
                                    for (int i = 0; i < response.length(); i++) {
                                        memberIds[0][i] = response.getJSONObject(i).getLong("id");
                                    }
                                }
                                notifyUiOfConvoMembers(memberIds[0], groupId, convoId);
                                action.doThis(convoId, convoType, memberIds[0],
                                        GeneralHelper.concatLong(memberIds[0], allMemberIds));
                            } catch (Exception e) {
                                reportError(e.getMessage(), false);
                            }
                        } else {
                            reportUnsuccess(result.getMessage(), false);
                        }
                    }
                }, convoId);
                break;
            default:
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

    private void isTyping(Bundle extras) {
        final long id = extras.getLong(USER_ID);
        final long groupId = extras.getLong(GROUP_ID);
        final boolean isTyping = extras.getBoolean(IS_TYPING);
        new NotifyGroupMembersOfUserTypingChangeTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, isTyping ? "I'm typing!" : "I'm not typing!");
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, id, groupId, isTyping);
    }

    private void gcmIsTyping(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content"));
            final long id = content.getLong("id");
            final long groupId = content.getLong("group_id");
            final boolean isTyping = content.getBoolean("is_typing");
            Intent i = new Intent(CHASSIP_ACTION);
            i.putExtra(INTENT_TYPE, GCM_IS_TYPING)
                    .putExtra(USER_ID, id)
                    .putExtra(GROUP_ID, groupId)
                    .putExtra(IS_TYPING, isTyping);
            LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void sendMessage(Bundle extras) {
        final long groupId = extras.getLong(GROUP_ID);
        final int convoType = extras.getInt(CONVO_TYPE);
        final long convoId = extras.getLong(CONVO_ID);
        final String message = extras.getString(MESSAGE);
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
                                updateUiForMessages(groupId, messages.size());
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
                                updateUiForMessages(groupId, messages.size());
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

    private void fetchGroupMessagesAfterGcm(Bundle extras) {
        final long groupId;
        try {
            groupId = new JSONObject(extras.getString("msg_content")).getLong("group_id");
        } catch (JSONException e) {
            reportError(e.getMessage(), false);
            return;
        }
        fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
            @Override
            public void doThis(List<Message> messages) {
                for (Message m : messages) {
                    if (m != null && m.getSenderId() != getAccountUserId()) {
                        if (!Application.get().isActivityIsInForeground() || m.getGroupId() != groupId) {
                            notifyUserOfNewMessage(m);
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

    private void fetchMostRecentGroupMessages(final long groupId, final FetchGroupMessagesAction action) {
        JSONArray jsonArray = new JSONArray();
        long id = getMostRecentMessageId(groupId);
        try {
            jsonArray.put(0, id).put(1, -1);
        } catch (JSONException e) {
            reportError(e.getMessage(), false);
            return;
        }
        new GetMessagesTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
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
            reportError(e.getMessage(), false);
            return;
        }
        new GetMessagesTask(this, new HTTPTask.HTTPTaskListener() {
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

    private void fetchGroup(final long groupId, final FetchGroupAction fetchGroupAction) {
        final Context ctx = this;
        new GetGroupTask(this, new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    try {
                        JSONObject response = (JSONObject) result.getExtraInfo();
                        addGroup(response, true);
                        fetchGroupAction.doThis(groupId);
                    } catch (Exception e) {
                        GeneralHelper.reportMessage(ctx, LOGTAG, e.getMessage(), false);
                    }
                } else {
                    GeneralHelper.reportMessage(ctx, LOGTAG, result.getMessage(), false);
                }
            }
        }, getAccountUserId(), groupId);
    }

    private void fetchAllGroups() {
        new GetGroupsTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    try {
                        JSONArray response = (JSONArray) result.getExtraInfo();
                        List<Group> groups = addGroups(response, false);
                        for (Group g : groups) {
                            fetchMembersAndMessagesForGroup(g.getGlobalId());
                        }
                        updateUiForGroupUpdate();
                    } catch (Exception e) {
                        reportError(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, getAccountUserId());
    }

    private void fetchGroupMembers(final long groupId, final FetchGroupMemberAction action) {
        new GetGroupMembersTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    try {
                        JSONArray response = (JSONArray) result.getExtraInfo();
                        addGroupMembers(response, groupId, false);
                        action.doThis(groupId);
                    } catch (Exception e) {
                        reportError(e.getMessage(), false);
                    }
                } else {
                    reportUnsuccess(result.getMessage(), false);
                }
            }
        }, groupId);
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
                .putExtra(SHOW_TOAST, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void reportUnsuccess(String message, boolean showToast) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, UNSUCCESSFUL)
                .putExtra(MESSAGE, message)
                .putExtra(SHOW_TOAST, showToast);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void reportVerbose(String message, boolean showToast) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, VERBOSE)
                .putExtra(MESSAGE, message)
                .putExtra(SHOW_TOAST, showToast);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void handleGroupInvite(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content")).getJSONObject("content");
            final JSONObject group = content.getJSONObject("group");
            final JSONObject inviter = content.getJSONObject("inviter");
            final JSONArray members = content.getJSONArray("members");
            final boolean groupAlreadyExists = groupExists(group);
            final long[] previousMemberIds = getGroupMemberIds(group.getLong("id"));
            final Group g = addGroup(group, false);
            addGroupMembers(members, g.getGlobalId(), false);
            if (!groupAlreadyExists) {
                notifyUserOfNewGroup(g, inviter);
                fetchMembersAndMessagesForGroup(g.getGlobalId());
                updateUiForGroupUpdate();
            } else {
                final long groupId = g.getGlobalId();
                final long[] memberIds = getGroupMemberIds(groupId);
                final Bundle moreExtras = new Bundle();
                moreExtras.putLong(GROUP_ID, groupId);
                moreExtras.putLongArray(MEMBER_IDS, memberIds);
                updateUiForAddChatMembers(moreExtras);
                final long[] newMembers = GeneralHelper.diff(previousMemberIds, memberIds);
                notifyUserOfNewGroupMember(g, newMembers);
            }
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleGroupRemoveMembers(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content")).getJSONObject("content");
            long groupId = content.getLong("group_id");
            JSONArray removedMembers = content.getJSONArray("removed_members");
            long[] memberIds = new long[removedMembers.length()];
            for (int i = 0; i < removedMembers.length(); i++) {
                memberIds[i] = removedMembers.getLong(i);
            }
            final Bundle moreExtras = new Bundle();
            moreExtras.putLong(GROUP_ID, groupId);
            moreExtras.putLongArray(MEMBER_IDS, memberIds);
            finalizeChatMemberRemoval(moreExtras);
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleGroupDeletion(Bundle extras) {
        try {
            long groupId = new JSONObject(extras.getString("msg_content")).getLong("group_id");
            finalizeDeleteChat(groupId);
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleWhisperInvite(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content")).getJSONObject("content");
            final JSONArray members = content.getJSONArray("members");
            boolean isInWhisper = false;
            for (int i = 0; i < members.length(); i++) {
                isInWhisper |= getAccountUserId() == members.getJSONObject(i).getLong("id");
            }
            if (!isInWhisper) {
                return;
            }

            final JSONObject whisper = content.getJSONObject("whisper");
            final long whisperId = whisper.getLong("id");
            final JSONObject inviter = content.getJSONObject("inviter");
            new GetGroupTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if (result.getResponseCode() == 1) {
                        try {
                            final JSONObject response = (JSONObject) result.getExtraInfo();
                            final boolean whisperExisted = DatabaseHelper.getGroupIdFromWhisperId(whisperId) > -1;
                            Group newG = addGroup(response, false);
                            notifyUserOfNewWhisper(newG, whisper, members, inviter);
                            if (whisperExisted) {
                                long[] memberIds = new long[members.length()];
                                for (int i = 0; i < members.length(); i++) {
                                    final JSONObject member = new JSONObject(members.getString(i));
                                    memberIds[i] = member.getLong("id");
                                }
                                final Bundle moreExtras = new Bundle();
                                moreExtras.putLong(GROUP_ID, newG.getGlobalId());
                                moreExtras.putLong(CONVO_ID, whisperId);
                                moreExtras.putInt(CONVO_TYPE, ConvoType.WHISPER.getValue());
                                moreExtras.putLongArray(MEMBER_IDS, memberIds);
                                updateUiForAddConvoMembers(moreExtras);
                            } else {
                                updateUiForAddConvo(newG.getGlobalId(), whisper.getLong("id"),
                                        whisper.getString("name"), ConvoType.WHISPER);
                            }
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

    private void handleWhisperRemoveMembers(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content")).getJSONObject("content");
            final long whisperId = content.getLong("whisper_id");
            final long groupId = DatabaseHelper.getGroupIdFromWhisperId(whisperId);
            final JSONArray removedMembers = content.getJSONArray("removed_members");
            final long[] memberIds = new long[removedMembers.length()];
            boolean isNotInWhisper = false;
            for (int i = 0; i < removedMembers.length(); i++) {
                memberIds[i] = removedMembers.getLong(i);
                isNotInWhisper |= removedMembers.getLong(i) == getAccountUserId();
            }
            if (isNotInWhisper) {
                fetchGroup(groupId, new FetchGroupAction() {
                    @Override
                    public void doThis(long groupId) {

                    }
                });
                updateUiForRemoveConvo(groupId, whisperId);
            } else {
                final Bundle moreExtras = new Bundle();
                moreExtras.putLong(GROUP_ID, DatabaseHelper.getGroupIdFromWhisperId(whisperId));
                moreExtras.putLong(CONVO_ID, whisperId);
                moreExtras.putInt(CONVO_TYPE, ConvoType.WHISPER.getValue());
                moreExtras.putLongArray(MEMBER_IDS, memberIds);
                updateUiForRemoveConvoMembers(moreExtras);

            }
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleWhisperDeletion(Bundle extras) {
        try {
            final long whisperId = new JSONObject(extras.getString("msg_content")).getLong("whisper_id");
            final long groupId = getGroupIdFromWhisperId(whisperId);
            finalizeWhisperDelete(groupId, whisperId);
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleSideConvoInvite(final Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content")).getJSONObject("content");
            final JSONObject sideConvo = content.getJSONObject("side_chat");
            final long sideConvoId = sideConvo.getLong("id");
            final JSONArray members = content.getJSONArray("members");
            final JSONObject inviter = content.getJSONObject("inviter");
            new GetGroupTask(getApplicationContext(), new HTTPTask.HTTPTaskListener() {
                @Override
                public void onTaskComplete(TaskResult result) {
                    if (result.getResponseCode() == 1) {
                        try {
                            final JSONObject response = (JSONObject) result.getExtraInfo();
                            final boolean sideChatExisted = DatabaseHelper.getGroupIdFromSideConvoId(sideConvoId) > -1;
                            Group newG = addGroup(response, false);
                            notifyUserOfNewSideConvo(newG, sideConvo, members, inviter);
                            if (sideChatExisted) {
                                long[] memberIds = new long[members.length()];
                                for (int i = 0; i < members.length(); i++) {
                                    final JSONObject member = new JSONObject(members.getString(i));
                                    memberIds[i] = member.getLong("id");
                                }
                                final Bundle moreExtras = new Bundle();
                                moreExtras.putLong(GROUP_ID, newG.getGlobalId());
                                moreExtras.putLong(CONVO_ID, sideConvoId);
                                moreExtras.putInt(CONVO_TYPE, ConvoType.SIDE_CONVO.getValue());
                                moreExtras.putLongArray(MEMBER_IDS, memberIds);
                                updateUiForAddConvoMembers(moreExtras);
                            } else {
                                updateUiForAddConvo(newG.getGlobalId(), sideConvo.getLong("id"),
                                        sideConvo.getString("name"), ConvoType.SIDE_CONVO);
                            }
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

    private void handleSideConvoRemoveMembers(Bundle extras) {
        try {
            final JSONObject content = new JSONObject(extras.getString("msg_content"));
            final long sideConvoId = content.getLong("side_chat_id");
            final long groupId = DatabaseHelper.getGroupIdFromSideConvoId(sideConvoId);
            final JSONArray removedMembers = content.getJSONArray("removed_members");
            final long[] memberIds = new long[removedMembers.length()];
            boolean isNotInSideConvo = false;
            for (int i = 0; i < removedMembers.length(); i++) {
                memberIds[i] = removedMembers.getLong(i);
                isNotInSideConvo |= removedMembers.getLong(i) == getAccountUserId();
            }
            if (isNotInSideConvo) {
                fetchGroup(groupId, new FetchGroupAction() {
                    @Override
                    public void doThis(long groupId) {
                        updateUiForRemoveConvo(groupId, sideConvoId);
                    }
                });
            } else {
                final Bundle moreExtras = new Bundle();
                moreExtras.putLong(GROUP_ID, groupId);
                moreExtras.putLong(CONVO_ID, sideConvoId);
                moreExtras.putInt(CONVO_TYPE, ConvoType.SIDE_CONVO.getValue());
                moreExtras.putLongArray(MEMBER_IDS, memberIds);
                updateUiForRemoveConvoMembers(moreExtras);

            }
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void handleSideConvoDeletion(Bundle extras) {
        try {
            final long sideConvoId = new JSONObject(extras.getString("msg_content")).getLong("side_chat_id");
            final long groupId = getGroupIdFromSideConvoId(sideConvoId);
            finalizeSideConvoDelete(groupId, sideConvoId);
        } catch (Exception e) {
            reportError(e.getMessage(), false);
        }
    }

    private void logout(Bundle extras) {
        final String name = extras.getString(USER_NAME);
        long id = extras.getLong(USER_ID);
        new DeleteGcmRegTask(this, new HTTPTask.HTTPTaskListener() {
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

    private void fetchMembersAndMessagesForGroup(final long groupId) {
        fetchGroupMembers(groupId, new FetchGroupMemberAction() {
            @Override
            public void doThis(final long groupId) {
                fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
                    @Override
                    public void doThis(List<Message> messages) {
                        updateUiForMessages(groupId, messages.size());
                    }
                });
            }
        });
    }

    private void fetchMembersAndMessagesForConvo(final long groupId) {
//        fetchGroupMembers(groupId, new FetchGroupMemberAction() {
//            @Override
//            public void doThis(long groupId) {
//            }
//        });
        fetchMostRecentGroupMessages(groupId, new FetchGroupMessagesAction() {
            @Override
            public void doThis(List<Message> messages) {
                updateUiForMessages(groupId, messages.size());
            }
        });
    }

    private void updateUiForAddChatMembers(Bundle extras) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, ADD_CHAT_MEMBERS_RESPONSE)
                .putExtra(GROUP_ID, extras.getLong(GROUP_ID))
                .putExtra(MEMBER_IDS, extras.getLongArray(MEMBER_IDS));
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForAddConvo(long groupId, long convoId, String name, ConvoType type) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, CREATE_CONVO_RESPONSE)
                .putExtra(GROUP_ID, groupId)
                .putExtra(CONVO_ID, convoId)
                .putExtra(CONVO_NAME, name)
                .putExtra(CONVO_TYPE, type.getValue());
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForRemoveConvo(long groupId, long convoId) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, DELETE_CONVO_RESPONSE)
                .putExtra(GROUP_ID, groupId)
                .putExtra(CONVO_ID, convoId);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForAddConvoMembers(Bundle extras) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, ADD_CONVO_MEMBERS_RESPONSE)
                .putExtra(GROUP_ID, extras.getLong(GROUP_ID))
                .putExtra(CONVO_ID, extras.getLong(CONVO_ID))
                .putExtra(CONVO_TYPE, ConvoType.getFromValue(extras.getInt(GROUP_ID)))
                .putExtra(MEMBER_IDS, extras.getLongArray(MEMBER_IDS));
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForRemoveConvoMembers(Bundle extras) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, REMOVE_CONVO_MEMBERS_RESPONSE)
                .putExtra(GROUP_ID, extras.getLong(GROUP_ID))
                .putExtra(CONVO_ID, extras.getLong(CONVO_ID))
                .putExtra(CONVO_TYPE, ConvoType.getFromValue(extras.getInt(CONVO_TYPE)))
                .putExtra(MEMBER_IDS, extras.getLongArray(MEMBER_IDS));
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForRemoveChatMember(Bundle extras) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, REMOVE_CHAT_MEMBERS_RESPONSE)
                .putExtra(GROUP_ID, extras.getLong(GROUP_ID))
                .putExtra(MEMBER_IDS, extras.getLongArray(MEMBER_IDS));
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForMessages(long groupId, int numMessages) {
        final Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, SEND_MESSAGE_RESPONSE)
                .putExtra(GROUP_ID, groupId)
                .putExtra(NUM_MESSAGES, numMessages);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForGroupUpdate() {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, GROUP_UPDATE_RESPONSE);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    private void updateUiForGroupDelete(long groupId) {
        Intent i = new Intent(CHASSIP_ACTION);
        i.putExtra(INTENT_TYPE, DELETE_CHAT_RESPONSE)
                .putExtra(GROUP_ID, groupId);
        LocalBroadcastManager.getInstance(ChassipService.this).sendBroadcast(i);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void notifyUserOfNewMessage(Message m) {
        String content = String.format(Locale.getDefault(), "%s %s: %s", m.getSenderFirstName(), m.getSenderLastName(),
                m.getContent());
        String title = String.format(Locale.getDefault(), "%s", getGroup(m.getGroupId()).getName());
        notificationAlert(title, content, m.getGroupId(), NEW_MESSAGE_NOTIFACATION);
    }

    private void notifyUserOfNewGroup(Group g, JSONObject inviter) throws Exception {
        if (inviter.getLong("id") != getAccountUserId()) {
            String content = String.format(Locale.getDefault(), "You've been added to %s's chat called \"%s\"",
                    g.getCreatorName(), g.getName());
            String title = "New Chat Invite";
            notificationAlert(title, content, g.getGlobalId(), NEW_GROUP_INVITE_NOTIFACATION);
        }
    }

    private void notifyUserOfNewGroupMember(Group g, long[] newMembers) throws Exception {
        if (newMembers.length > 0) {
            final GroupMember[] gms = getGroupMembers(newMembers);
            final String names = concatNames(gms);
            String content = String.format(Locale.getDefault(), "%s been added to %s's chat called \"%s\"", names,
                    g.getCreatorName(), g.getName());
            String title = "New Group Member";
            notificationAlert(title, content, g.getGlobalId(), NEW_GROUP_MEMBER_INVITE_NOTIFACATION);
        }
    }

    private void notifyUserOfNewSideConvo(Group g, JSONObject sc, JSONArray members, JSONObject inviter) throws JSONException {
        if (inviter.getLong("id") != getAccountUserId()) {
//            boolean isInSideConvo = false;
//            for (int i = 0; i < members.length(); i++) {
//                isInSideConvo |= getAccountUserId() == members.getJSONObject(i).getLong("id");
//            }
//            String content = isInSideConvo ? String.format(Locale.getDefault(),
//                    "%s %s added you to %s's new side convo in %s called \"%s\"", inviter.get("first_name"),
//                    inviter.get("last_name"), sc.getString("creator_name"), g.getName(), sc.getString("name")) : String
//                    .format(Locale.getDefault(), "%s has started a new side convo in %s called \"%s\"",
//                            sc.getString("creator_name"), g.getName(), sc.getString("name"));
//            String title = isInSideConvo ? "New Side Convo Invite" : "New Side Convo";
            final String title = "Side Convo";
            final String content = String.format("The %s side convo has been updated", sc.getString("name"));
            notificationAlert(title, content, g.getGlobalId(), NEW_SIDECONVO_INVITE_NOTIFACATION);
        }
    }

    private void notifyUserOfNewWhisper(Group g, JSONObject w, JSONArray members, JSONObject inviter) throws JSONException {
        if (inviter.getLong("id") != getAccountUserId()) {
//            String content = String.format(Locale.getDefault(),
//                    "%s %s added you to %s's new whisper in %s called \"%s\"", inviter.get("first_name"),
//                    inviter.get("last_name"), w.getString("creator_name"), g.getName(), w.getString("name"));
//            String title = "New Whisper Invite";
//            notificationAlert(title, content, g.getGlobalId(), NEW_WHISPER_INVITE_NOTIFACATION);
            final String title = "Whsiper";
            final String content = String.format("The %s whsiper has been updated", w.getString("name"));
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
            case NEW_GROUP_MEMBER_INVITE_NOTIFACATION:
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

    private String concatNames(GroupMember[] gms) {
        String names = gms[0].getFullName();
        for (int i = 1; i < gms.length; i++) {
            names = names + ", " + gms[i].getFullName();
        }
        return names;
    }

    private void releaseWakeLockFromGcmProcessing(Intent i) {
        Pool.resetListener();
        backgroundTaskListener = null;
        GcmBroadcastReceiver.completeWakefulIntent(i);
    }

    private class BackgroundTaskListener implements Pool.PoolListener {

        private final Timer mTimer = new Timer();

        private final Intent mI;

        private TimerTask backgroundTaskListenerTimerTask;

        BackgroundTaskListener(Intent i) {
            mI = i;
        }

        @Override
        public void cancel() {
            if (backgroundTaskListenerTimerTask != null) {
                backgroundTaskListenerTimerTask.cancel();
                backgroundTaskListenerTimerTask = null;
            }
        }

        @Override
        public void set() {
            if (backgroundTaskListenerTimerTask == null) {
                backgroundTaskListenerTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        releaseWakeLockFromGcmProcessing(mI);
                    }
                };
            }
            mTimer.schedule(backgroundTaskListenerTimerTask, 5000l);
        }
    }

    private interface FetchGroupMessagesAction {
        void doThis(List<Message> messages);
    }

    private interface AuthenticatedAction {
        void doThis();
    }

    private interface FetchGroupAction {
        void doThis(long groupId);
    }

    private interface FetchGroupMemberAction {
        void doThis(long groupId);
    }

    private interface FetchConvoMemberAction {
        void doThis(long convoId, ConvoType convoType, long[] memberIds, long[] allMemberIds);
    }

    private class FetchFacebookAvi extends AsyncTask<Bundle, Void, Void> {
        @Override
        protected Void doInBackground(Bundle... params) {
            if (params[0] != null) {
                try {
                    URL url = new URL(params[0].getString(IMAGE_URL));
                    InputStream input = url.openStream();
                    File file = ImageUtil.createTimestampedImageFile(ChassipService.this);
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
