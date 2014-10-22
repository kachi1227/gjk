package com.gjk;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.helper.GeneralHelper;
import com.gjk.service.ChassipService;
import com.gjk.utils.FileUtils;
import com.gjk.utils.media2.ImageManager;
import com.gjk.utils.media2.ImageUtil;
import com.gjk.views.DrawerLayout;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gjk.Constants.ADD_CHAT_MEMBERS_REQUEST;
import static com.gjk.Constants.ADD_CHAT_MEMBERS_RESPONSE;
import static com.gjk.Constants.ADD_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.ADD_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.ALL_MEMBER_IDS;
import static com.gjk.Constants.BACKGROUND_ACTION;
import static com.gjk.Constants.CAMERA_REQUEST;
import static com.gjk.Constants.CAN_FETCH_MORE_MESSAGES;
import static com.gjk.Constants.CREATE_CONVO_REQUEST;
import static com.gjk.Constants.DELETE_CHAT_REQUEST;
import static com.gjk.Constants.DELETE_CONVO_REQUEST;
import static com.gjk.Constants.LOGIN_REQUEST;
import static com.gjk.Constants.REMOVE_CHAT_MEMBERS_REQUEST;
import static com.gjk.Constants.REMOVE_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.UI_ACTION;
import static com.gjk.Constants.CHAT_CONTEXT_MENU_ID;
import static com.gjk.Constants.CONVO_CONTEXT_MENU_ID;
import static com.gjk.Constants.CONVO_ID;
import static com.gjk.Constants.CONVO_NAME;
import static com.gjk.Constants.CONVO_TYPE;
import static com.gjk.Constants.CONVO_UPDATE_RESPONSE;
import static com.gjk.Constants.CREATE_CHAT_REQUEST;
import static com.gjk.Constants.CREATE_CONVO_RESPONSE;
import static com.gjk.Constants.DELETE_CHAT_RESPONSE;
import static com.gjk.Constants.DELETE_CONVO_RESPONSE;
import static com.gjk.Constants.EMAIL;
import static com.gjk.Constants.ERROR;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_RESPONSE;
import static com.gjk.Constants.FIRST_NAME;
import static com.gjk.Constants.GALLERY_REQUEST;
import static com.gjk.Constants.GCM_IS_TYPING;
import static com.gjk.Constants.GCM_MESSAGE_RESPONSE;
import static com.gjk.Constants.GET_ALL_GROUPS_RESPONSE;
import static com.gjk.Constants.GROUP_ID;
import static com.gjk.Constants.GROUP_UPDATE_RESPONSE;
import static com.gjk.Constants.IMAGE_PATH;
import static com.gjk.Constants.IMAGE_URL;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.IS_TYPING;
import static com.gjk.Constants.IS_TYPING_INTERVAL_FOR_ME;
import static com.gjk.Constants.IS_TYPING_INTERVAL_FOR_SOMEONE_ELSE;
import static com.gjk.Constants.IS_TYPING_REQUEST;
import static com.gjk.Constants.LAST_NAME;
import static com.gjk.Constants.LOGIN_RESPONSE;
import static com.gjk.Constants.LOGOUT_REQUEST;
import static com.gjk.Constants.MANUAL;
import static com.gjk.Constants.MEMBER_IDS;
import static com.gjk.Constants.MESSAGE;
import static com.gjk.Constants.NUM_MESSAGES;
import static com.gjk.Constants.OFFSCREEN_PAGE_LIMIT;
import static com.gjk.Constants.PASSWORD;
import static com.gjk.Constants.REGISTER_FACEBOOK_REQUEST;
import static com.gjk.Constants.REGISTER_REQUEST;
import static com.gjk.Constants.REGISTER_RESPONSE;
import static com.gjk.Constants.REMOVE_CHAT_MEMBERS_RESPONSE;
import static com.gjk.Constants.REMOVE_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.SEND_MESSAGE_REQUEST;
import static com.gjk.Constants.SEND_MESSAGE_RESPONSE;
import static com.gjk.Constants.SHOW_TOAST;
import static com.gjk.Constants.START_PROGRESS;
import static com.gjk.Constants.STOP_PROGRESS;
import static com.gjk.Constants.UNSUCCESSFUL;
import static com.gjk.Constants.USER_ID;
import static com.gjk.Constants.USER_NAME;
import static com.gjk.Constants.VERBOSE;
import static com.gjk.helper.DatabaseHelper.getAccountUserFullName;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;
import static com.gjk.helper.DatabaseHelper.getFirstStoredGroup;
import static com.gjk.helper.DatabaseHelper.getGroup;
import static com.gjk.helper.DatabaseHelper.getGroupMember;
import static com.gjk.helper.DatabaseHelper.getGroupMemberIds;
import static com.gjk.helper.DatabaseHelper.getGroupMembers;
import static com.gjk.helper.DatabaseHelper.getGroupsCursor;
import static com.gjk.helper.DatabaseHelper.getOtherGroupMembers;

/**
 * @author gpl
 */
public class MainActivity extends FragmentActivity implements LoginDialog.NoticeDialogListener,
        RegisterDialog.NoticeDialogListener, SettingsDialog.NoticeDialogListener,
        CreateChatDialog.NoticeDialogListener, CreateConvoDialog.NoticeDialogListener,
        AddChatMembersDialog.NoticeDialogListener, RemoveChatMembersDialog.NoticeDialogListener,
        DeleteChatDialog.NoticeDialogListener, AddConvoMembersDialog.NoticeDialogListener,
        RemoveConvoMembersDialog.NoticeDialogListener, DeleteConvoDialog.NoticeDialogListener {

    private final static String LOGTAG = "MainActivity";

    private ViewPager mViewPager;
    private ActionBarDrawerToggle mDrawerToggle;
    private ConvoPagerAdapter mConvoPagerAdapter;
    private ChatsDrawerFragment mChatDrawerFragment;
    private ConvosDrawerFragment mConvoDrawerFragment;

    private DrawerLayout mDrawerLayout;

    private TextView mWhosTpying;
    private EditText mPendingMessage;
    private ImageView mAttach;

    private ImageView mSend;
    private LoginDialog mLoginDialog;

    private RegisterDialog mRegDialog;

    private AlertDialog mDialog;

    private SettingsDialog mSettingsDialog;
    private long[] mLatestChosenMemberIds;
    private String mLatestCreatedName;
    private String mLatestPath;

    private ActivityImageState mState;
    private final Object syncObj = new Object();

    private Timer mTimer;
    private AmITypingTask mSendIsTypingTask;
    private Map<Long, IsSomeoneElseTypingTask> mWhosTpyingIds;

    private enum ActivityImageState {
        NONE,
        REGISTERING,
        CHAT_CREATING,
        MESSAGE_ATTACHING,
    }

    private BroadcastReceiver mServerResponseReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final Bundle extras = intent.getExtras();
            if (extras == null) {
                setProgressBarIndeterminateVisibility(false);
                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, "Received null extras");
                return;
            }

            final String type = extras.getString(INTENT_TYPE);
            if (type == null) {
                setProgressBarIndeterminateVisibility(false);
                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, "Received null intent type");
                return;
            }

            try {

                Application.get().updateLastUpdate();

                ImageUtil.removeTempImageFile(MainActivity.this);

                synchronized (syncObj) {

                    Application.get().log(String.format("UI: %s, extras=%s", type, extras.toString()));

                    switch (type) {
                        case GCM_IS_TYPING:

                            if (Application.get().isActivityIsInBackground()) {
                                return;
                            }

                            updateIsTpying(extras.getLong(USER_ID), extras.getLong(GROUP_ID), extras.getBoolean(IS_TYPING));

                            break;
                        case SEND_MESSAGE_RESPONSE:

                            mChatDrawerFragment.updateView();
                            if (mConvoPagerAdapter != null) {
                                if (Application.get().getCurrentChat() != null && extras.getLong(GROUP_ID) ==
                                        Application.get().getCurrentChat().getGlobalId() && mConvoPagerAdapter != null) {
                                    mConvoPagerAdapter.handleMessage(extras.getInt(NUM_MESSAGES));
                                }
                                setSendButtonEnable(false);
                                setActivityImageState(ActivityImageState.NONE);
                            }

                            break;
                        case GCM_MESSAGE_RESPONSE:

                            mChatDrawerFragment.updateView();
                            if (mConvoPagerAdapter != null) {
                                if (Application.get().getCurrentChat() != null && extras.getLong(GROUP_ID) ==
                                        Application.get().getCurrentChat().getGlobalId() && mConvoPagerAdapter != null) {
                                    mConvoPagerAdapter.handleMessage(extras.getInt(NUM_MESSAGES));
                                }
                            }

                            break;
                        case FETCH_CONVO_MEMBERS_RESPONSE:

                            if (mConvoPagerAdapter != null) {
                                mConvoPagerAdapter.handleConvoMembers(extras.getLong(GROUP_ID),
                                        extras.getLong(CONVO_ID), extras.getLongArray(MEMBER_IDS));
                            }

                            break;
                        case GROUP_UPDATE_RESPONSE:

                            if (extras.containsKey(MANUAL) && extras.getBoolean(MANUAL)) {
                                mChatDrawerFragment.resetRefresh();
                            } else {
                                mChatDrawerFragment.swapCursor(getGroupsCursor());
                            }

                            break;
                        case CREATE_CONVO_RESPONSE: {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                final long convoId = extras.getLong(CONVO_ID);
                                final String name = extras.getString(CONVO_NAME);
                                final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
                                mConvoPagerAdapter.handleCreateConvo(groupId, convoId, name, convoType);
                            }

                            break;
                        }
                        case DELETE_CONVO_RESPONSE: {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                final long convoId = extras.getLong(CONVO_ID);
                                mConvoPagerAdapter.handleDeleteConvo(convoId);
                            }

                            break;
                        }
                        case ADD_CHAT_MEMBERS_RESPONSE: {

                            final long groupId = extras.getLong(GROUP_ID);
                            final long[] members = extras.getLongArray(MEMBER_IDS);
                            mChatDrawerFragment.swapCursor(getGroupsCursor());
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                mConvoPagerAdapter.handleAddChatMembers(groupId, members);
                            }

                            break;
                        }
                        case REMOVE_CHAT_MEMBERS_RESPONSE: {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                                    groupId) {
                                final long[] members = extras.getLongArray(MEMBER_IDS);
                                mConvoPagerAdapter.handleRemoveChatMembers(members);
                                mConvoDrawerFragment.updateView();
                            }

                            break;
                        }
                        case ADD_CONVO_MEMBERS_RESPONSE: {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                                    groupId) {
                                final long[] members = extras.getLongArray(MEMBER_IDS);
                                final long convoId = extras.getLong(CONVO_ID);
                                mConvoPagerAdapter.handleAddConvoMembers(convoId, members);
                                mConvoDrawerFragment.updateView();
                            }

                            break;
                        }
                        case REMOVE_CONVO_MEMBERS_RESPONSE: {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                                    groupId) {
                                final long[] members = extras.getLongArray(MEMBER_IDS);
                                final long convoId = extras.getLong(CONVO_ID);
                                mConvoPagerAdapter.handleRemoveConvoMembers(convoId, members);
                                mConvoDrawerFragment.updateView();
                            }

                            break;
                        }
                        case CONVO_UPDATE_RESPONSE:

                            long convoId = extras.getLong(CONVO_ID);
                            List<ConvoFragment> frags = mConvoDrawerFragment.getItems();
                            for (ConvoFragment frag : frags) {
                                if (frag.getConvoId() == convoId) {
                                    toggleChat(frag.getGroupId());
                                    return;
                                }
                            }

                            break;
                        case DELETE_CHAT_RESPONSE: {

                            long groupId = extras.getLong(GROUP_ID);
                            mChatDrawerFragment.swapCursor(getGroupsCursor());
                            if (Application.get().getCurrentChat().getGlobalId() == groupId) {
                                toggleChat(getFirstStoredGroup());
                            }

                            break;
                        }
                        case FETCH_MORE_MESSAGES_RESPONSE:

                            if (mConvoPagerAdapter != null) {
                                mConvoPagerAdapter.loadMessagesAfterFetch(extras.getLong(GROUP_ID),
                                        extras.getLong(CONVO_ID), extras.getBoolean(CAN_FETCH_MORE_MESSAGES));
                            }

                            break;
                        case LOGIN_RESPONSE:
                        case REGISTER_RESPONSE:

                            authenticated();

                            break;
                        case GET_ALL_GROUPS_RESPONSE:

                            mChatDrawerFragment.swapCursor(getGroupsCursor());

                            break;
                        case START_PROGRESS:

                            setProgressBarIndeterminateVisibility(true);

                            break;
                        case STOP_PROGRESS:

                            setProgressBarIndeterminateVisibility(false);

                            break;
                        case ERROR:
                        case UNSUCCESSFUL:

                            setSendButtonEnable(false);
                            setActivityImageState(ActivityImageState.NONE);
                            if (extras.getBoolean(SHOW_TOAST)) {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE), true);
                            } else {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE));
                            }
                            setProgressBarIndeterminateVisibility(false);

                            break;
                        case VERBOSE:

                            if (extras.getBoolean(SHOW_TOAST)) {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE), true);
                            } else {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE));
                            }

                            break;
                        default:

                            setProgressBarIndeterminateVisibility(false);
                            GeneralHelper.reportMessage(MainActivity.this, LOGTAG, "Received unhandled intent type=" + type);
                            break;
                    }
                }

            } catch (Exception e) {
                setProgressBarIndeterminateVisibility(false);
                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, e.getMessage());
            }
        }
    };

    private void updateIsTpying(long id, long groupId, boolean isTyping) {

        if (Application.get().isActivityIsInBackground() || Application.get().getCurrentChat() == null || groupId !=
                Application.get().getCurrentChat().getGlobalId() || getAccountUserId() == id) {
            return;
        }

        if (mWhosTpyingIds.containsKey(id)) {
            mWhosTpyingIds.get(id).cancel();
            mWhosTpyingIds.remove(id);
        }

        if (isTyping) {
            IsSomeoneElseTypingTask task = new IsSomeoneElseTypingTask(id, groupId);
            mTimer.schedule(task, IS_TYPING_INTERVAL_FOR_SOMEONE_ELSE);
            mWhosTpyingIds.put(id, task);
        }

        if (mWhosTpyingIds.isEmpty()) {
            resetIsTypingField();
            return;
        }

        String whosTyping;
        final Iterator<Long> iter = mWhosTpyingIds.keySet().iterator();
        if (mWhosTpyingIds.size() == 1) {
            whosTyping = getGroupMember(iter.next()).getFullName() + " [...]";
        } else if (mWhosTpyingIds.size() == 1) {
            whosTyping = getGroupMember(iter.next()).getFullName() + " & " + getGroupMember(iter.next()).getFullName()
                    + " [...]";
        } else {
            whosTyping = mWhosTpyingIds.size() + " [...]";
        }
        mWhosTpying.setText(whosTyping);
    }

    @Override
    public void onNewIntent(Intent i) {
        Log.d(LOGTAG, "Swag");
        if (i.getExtras() == null || !i.getExtras().containsKey("group_id")) {
            return;
        }

        long chatId = i.getExtras().getLong("group_id");
        long convoId = i.getExtras().getLong("convo_id");
        Application.get().getPreferences().edit().putLong("current_group_id", chatId).commit();
        Application.get().getPreferences().edit().putLong("chat_" + chatId + "_current_convo_id", convoId).commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity);

        initDrawers();
        initMessageViews();

        mLoginDialog = new LoginDialog();
        mRegDialog = new RegisterDialog();
        mSettingsDialog = new SettingsDialog();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
        adjustViewPagerHeight();

        LocalBroadcastManager.getInstance(this).registerReceiver(mServerResponseReceiver,
                new IntentFilter(UI_ACTION));

//        GeneralHelper.showHashKey(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        final MenuItem convosActions = menu.findItem(R.id.action_convos);
        final MenuItem fetchAction = menu.findItem(R.id.action_fetch_more_messages);
        if (Application.get().getCurrentChat() == null || mConvoPagerAdapter == null) {
            convosActions.setVisible(false);
            fetchAction.setVisible(false);
        } else {
            convosActions.setVisible(true);
            convosActions.setTitle(String.format(Locale.getDefault(), "%d Convo%s",
                    mConvoPagerAdapter.getCount(), mConvoPagerAdapter.getCount() == 1 ? "" : "s"));
//            menu.findItem(R.mId.action_fetch_more_messages).setVisible(mConvoPagerAdapter.getCurrentConvo()
//                    .isCanFetchMoreMessagesSet());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mSettingsDialog.show(getSupportFragmentManager(), "SettingsDialog");
            return true;
        } else if (id == R.id.action_fetch_more_messages) {
            mConvoDrawerFragment.getCurrentConvo().loadAndFetchMessages(Constants.PROPERTY_SETTING_MESSAGE_FETCH_LIMIT_DEFAULT);
        } else if (id == R.id.action_convos) {
            if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawerLayout.openDrawer(Gravity.RIGHT);
            }
            return true;
        } else if (id == R.id.action_logout) {
            mDrawerLayout.closeBothDrawers();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Set the dialog title
            builder.setTitle(R.string.logout_title).setMessage(R.string.logout)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            logout();
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Application.get().isLoggedIn()) {
            if (!mLoginDialog.isAdded() && !mRegDialog.isAdded()) {
                mLoginDialog.show(getSupportFragmentManager(), "LoginDialog");
            }
        } else {

            if (Application.get().appIsUpdated()) {
                logout();
                Application.get().updateAppVersionName();
                return;
            }

            if (Application.get().getPreferences().contains("current_group_id")) {
                toggleChat(Application.get().getPreferences().getLong("current_group_id", 0));
            } else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("group_id")) {
                toggleChat(getIntent().getExtras().getLong("group_id"));
            } else {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Application.get().checkPlayServices()) {
            throw new RuntimeException("The fuck");
        }

        invalidateOptionsMenu();
        Application.get().activityResumed();

        if (!GeneralHelper.getKachisCachePref()) {
            ImageManager.getInstance(getSupportFragmentManager()).resume();
        }

        if (mState != ActivityImageState.NONE && mLatestChosenMemberIds == null && mLatestCreatedName == null &&
                mLatestPath == null) {
            setActivityImageState(ActivityImageState.NONE);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.get().activityPaused();

        if (getAccountUserId() != null) {
            resetTimer();
//            resetSendIsTypingTask();
            resetIsTypingField();
        }

        if (!GeneralHelper.getKachisCachePref()) {
            ImageManager.getInstance(getSupportFragmentManager()).pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServerResponseReceiver);

        if (!GeneralHelper.getKachisCachePref()) {
            ImageManager.getInstance(getSupportFragmentManager()).destroy();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getGroupId() == CHAT_CONTEXT_MENU_ID) {
            Cursor c = mChatDrawerFragment.getItem(info.position);
            long groupId = c.getLong(c.getColumnIndex(Group.F_GLOBAL_ID));
            return handleChatClicked(item.getTitle(), groupId);
        } else if (item.getGroupId() == CONVO_CONTEXT_MENU_ID) {
            ConvoFragment frag = mConvoDrawerFragment.getItem(info.position);
            long groupId = frag.getGroupId();
            ConvoType convoType = frag.getConvoType();
            switch (convoType) {
                case MAIN_CHAT:
                    return handleChatClicked(item.getTitle(), groupId);
                case SIDE_CONVO:
                case WHISPER:
                    return handleConvoClicked(item.getTitle(), frag);
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public void onCreateChatDialogPositiveClick(CreateChatDialog dialog) {
        dialog.dismiss();
        mLatestCreatedName = dialog.getChatName();
        mLatestChosenMemberIds = dialog.getSelectedIds();
        displayImageChooser(ActivityImageState.CHAT_CREATING);
    }

    @Override
    public void onCreateConvoDialogPositiveClick(CreateConvoDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, CREATE_CONVO_REQUEST);
        b.putLong(GROUP_ID, Application.get().getCurrentChat().getGlobalId());
        b.putInt(CONVO_TYPE, dialog.getConvoType().getValue());
        b.putString(CONVO_NAME, dialog.getConvoName());
        b.putLongArray(MEMBER_IDS, dialog.getSelectedIds());
        b.putLongArray(ALL_MEMBER_IDS, mConvoDrawerFragment.getMainConvo().getOtherMemberIds());
        sendBackgroundRequest(b);
    }

    @Override
    public void onAddChatMembersDialogPositiveClick(AddChatMembersDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, ADD_CHAT_MEMBERS_REQUEST);
        b.putLong(GROUP_ID, dialog.getGroupId());
        b.putLongArray(MEMBER_IDS, dialog.getSelectedIds());
        sendBackgroundRequest(b);
    }

    @Override
    public void onRemoveChatMembersDialogPositiveClick(RemoveChatMembersDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, REMOVE_CHAT_MEMBERS_REQUEST);
        b.putLong(GROUP_ID, dialog.getGroupId());
        b.putLongArray(MEMBER_IDS, dialog.getSelectedIds());
        sendBackgroundRequest(b);
    }

    @Override
    public void onDeleteChatDialogPositiveClick(DeleteChatDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, DELETE_CHAT_REQUEST);
        b.putLong(GROUP_ID, dialog.getGroupId());
        sendBackgroundRequest(b);
    }

    @Override
    public void onAddConvoMembersDialogPositiveClick(AddConvoMembersDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, ADD_CONVO_MEMBERS_REQUEST);
        b.putLong(GROUP_ID, dialog.getGroupId());
        b.putLong(CONVO_ID, dialog.getConvoId());
        b.putInt(CONVO_TYPE, dialog.getConvoType().getValue());
        b.putLongArray(MEMBER_IDS, dialog.getSelectedIds());
        b.putLongArray(ALL_MEMBER_IDS, mConvoDrawerFragment.getFrag(dialog.getConvoId()).getOtherMemberIds());
        sendBackgroundRequest(b);
    }

    @Override
    public void onRemoveConvoMembersDialogPositiveClick(RemoveConvoMembersDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, REMOVE_CONVO_MEMBERS_REQUEST);
        b.putLong(GROUP_ID, dialog.getGroupId());
        b.putLong(CONVO_ID, dialog.getConvoId());
        b.putInt(CONVO_TYPE, dialog.getConvoType().getValue());
        b.putLongArray(MEMBER_IDS, dialog.getSelectedIds());
        b.putLongArray(ALL_MEMBER_IDS, mConvoDrawerFragment.getFrag(dialog.getConvoId()).getOtherMemberIds());
        sendBackgroundRequest(b);
    }

    @Override
    public void onDeleteConvoDialogPositiveClick(DeleteConvoDialog dialog) {
        dialog.dismiss();
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, DELETE_CONVO_REQUEST);
        b.putLong(GROUP_ID, dialog.getGroupId());
        b.putLong(CONVO_ID, dialog.getConvoId());
        b.putInt(CONVO_TYPE, dialog.getConvoType().getValue());
        b.putLongArray(MEMBER_IDS, dialog.getIds());
        sendBackgroundRequest(b);
    }

    @Override
    public void onSettingsDialogPositiveClick(SettingsDialog dialog, boolean dontRefresh) {
        if (!dontRefresh) {
            toggleChat(Application.get().getCurrentChat());
        }
    }

    @Override
    public void onLoginDialogPositiveClick(LoginDialog dialog) {
        mLoginDialog = dialog;
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, LOGIN_REQUEST);
        b.putString(EMAIL, dialog.getEmail());
        b.putString(PASSWORD, dialog.getPassword());
        sendBackgroundRequest(b);
    }

    @Override
    public void onLoginDialogFacebookClick(LoginDialog dialog) {
        mLoginDialog = dialog;
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, LOGIN_REQUEST);
        b.putString(EMAIL, (String) dialog.getFacebookUser().asMap().get("email"));
        b.putString(PASSWORD, dialog.getFacebookUser().getId() + dialog.getFacebookUser().asMap().get("email"));
        GeneralHelper.logoutOfFacebook(this);
        sendBackgroundRequest(b);
    }

    @Override
    public void onLoginDialogNegativeClick(LoginDialog dialog) {
        mLoginDialog.dismiss();
        mRegDialog.show(getSupportFragmentManager(), "RegisterDialog");
    }

    @Override
    public void onRegisterDialogPositiveClick(RegisterDialog dialog) {
        mRegDialog = dialog;
        displayImageChooser(ActivityImageState.REGISTERING);
    }

    @Override
    public void onRegisterDialogFacebookClick(final RegisterDialog dialog) {
        mRegDialog = dialog;
        Bundle params = new Bundle();
        params.putBoolean("redirect", false);
        params.putString("height", "1000");
        params.putString("type", "large");
        params.putString("width", "1000");
        /* make the API call */
        new Request(Session.getActiveSession(), "/me/picture", params, HttpMethod.GET, new Request.Callback() {
            public void onCompleted(Response response) {
                try {
                    String aviUrl = ((JSONObject) response.getGraphObject().asMap().get("data")).getString("url");
                    final Bundle b = new Bundle();
                    b.putString(INTENT_TYPE, REGISTER_FACEBOOK_REQUEST);
                    b.putString(FIRST_NAME, dialog.getFacebookUser().getFirstName());
                    b.putString(LAST_NAME, dialog.getFacebookUser().getLastName());
                    b.putString(EMAIL, (String) dialog.getFacebookUser().asMap().get("email"));
                    b.putString(PASSWORD, dialog.getFacebookUser().getId() + dialog.getFacebookUser().asMap().get("email"));
                    b.putString(IMAGE_URL, aviUrl);
                    sendBackgroundRequest(b);
                } catch (JSONException e) {
                    GeneralHelper.reportMessage(MainActivity.this, LOGTAG, e.getMessage());
                }
            }
        }).executeAsync();
    }

    @Override
    public void onRegisterDialogNegativeClick(RegisterDialog dialog) {
        mRegDialog.dismiss();
        mLoginDialog.show(getSupportFragmentManager(), "mLoginDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (mLoginDialog.isAdded()) {
            mLoginDialog.handleOnActivityRequest(requestCode, resultCode, data);
        } else if (mRegDialog.isAdded()) {
            mRegDialog.handleOnActivityRequest(requestCode, resultCode, data);
        }

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode != GALLERY_REQUEST && requestCode != CAMERA_REQUEST) {
            return;
        }

        try {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    // grab path from gallery
                    Uri selectedImageUri = data.getData();
                    mLatestPath = FileUtils.getPath(this, selectedImageUri);
                    break;
                case CAMERA_REQUEST:
                    // add pic to gallery
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(mLatestPath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                    break;
                default:
                    break;
            }
            GeneralHelper.reportMessage(this, LOGTAG, "Image path: " + mLatestPath);
            final Bundle b = new Bundle();
            switch (mState) {
                case MESSAGE_ATTACHING:
                    if (mLatestPath == null || mLatestPath.isEmpty()) {
                        setActivityImageState(ActivityImageState.NONE);
                    } else {
                        setActivityImageState(mState, true, true);
                    }
                    break;
                case CHAT_CREATING:
                    b.putString(INTENT_TYPE, CREATE_CHAT_REQUEST);
                    b.putString(CONVO_NAME, mLatestCreatedName);
                    b.putLongArray(MEMBER_IDS, mLatestChosenMemberIds);
                    b.putString(IMAGE_PATH, mLatestPath);
                    sendBackgroundRequest(b);
                    setActivityImageState(ActivityImageState.NONE);
                    break;
                case REGISTERING:
                    b.putString(INTENT_TYPE, REGISTER_REQUEST);
                    b.putString(FIRST_NAME, mRegDialog.getFirstName());
                    b.putString(LAST_NAME, mRegDialog.getLastName());
                    b.putString(EMAIL, mRegDialog.getEmail());
                    b.putString(PASSWORD, mRegDialog.getPassword());
                    b.putString(IMAGE_PATH, mLatestPath);
                    sendBackgroundRequest(b);
                    setActivityImageState(ActivityImageState.NONE);
                    break;
                default:
                    Log.e(LOGTAG, "Unhandled case=" + mState);
            }
        } catch (Exception e) {
            GeneralHelper.reportMessage(this, LOGTAG, e.getMessage());
        }
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    protected void sendBackgroundRequest(Bundle b) {
        final Intent i = new Intent(this, ChassipService.class);
        i.setAction(BACKGROUND_ACTION);
        i.putExtras(b);
        startService(i);
    }

    protected void toggleChat(long groupId) {
        toggleChat(getGroup(groupId));
    }

    protected void toggleChat(Group chat) {

        if (chat == null) {
            return;
        }

        resetTimer();

        mSend.setVisibility(View.VISIBLE);
        mAttach.setVisibility(View.VISIBLE);
        mPendingMessage.setVisibility(View.VISIBLE);
        if (mDrawerLayout.getDrawerLockMode(Gravity.RIGHT) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
        }
        Application.get().setCurrentChat(chat);
        mDrawerLayout.unregisterViews();
        mChatDrawerFragment.updateView();
        mConvoPagerAdapter = new ConvoPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mConvoPagerAdapter);
        mViewPager.setOnPageChangeListener(mConvoPagerAdapter);
        mConvoDrawerFragment.clear();
        mConvoPagerAdapter.setChat(chat);
        mConvoDrawerFragment.updateView();
        setTitle(chat.getName());
        Application.get().getPreferences().edit().putLong("current_group_id", chat.getGlobalId()).commit();
        invalidateOptionsMenu();
        toggleConvo(Application.get().getPreferences().getLong("chat_" + chat.getGlobalId() + "_current_convo_id", 0));
    }

    protected void toggleConvo(int position) {
        mConvoPagerAdapter.setConvo(position);
        finalizeToggleConvo();
    }

    protected void toggleConvo(long convoId) {
        mConvoPagerAdapter.setConvo(convoId);
        finalizeToggleConvo();
    }

    private void resetIsTypingField() {
        mWhosTpying.setText(getResources().getString(R.string.is_typing_holder));
    }

    private void resetSendIsTypingTask() {

        if (mSendIsTypingTask == null) {
            mSendIsTypingTask = new AmITypingTask();
            return;
        }

        mSendIsTypingTask.cancel();
        mSendIsTypingTask = new AmITypingTask();

    }

    private void resetTimer() {

        if (mTimer == null) {
            mTimer = new Timer();
            return;
        }

        mTimer.cancel();
        mTimer = new Timer();
    }

    private boolean handleChatClicked(CharSequence title, long groupId) {
        if (title.equals(Constants.CHAT_DRAWER_ADD_CHAT_MEMBERS)) {
            showAddChatMembersDialog(groupId);
        } else if (title.equals(Constants.CHAT_DRAWER_REMOVE_CHAT_MEMBERS)) {
            showRemoveChatMembersDialog(groupId);
        } else if (title.equals(Constants.CHAT_DRAWER_DELETE_CHAT)) {
            showDeleteChatDialog(groupId);
        } else {
            return false;
        }
        return true;
    }

    private boolean handleConvoClicked(CharSequence title, ConvoFragment frag) {
        final long groupId = frag.getGroupId();
        final long convoId = frag.getConvoId();
        final ConvoType convoType = frag.getConvoType();
        final Set<GroupMember> allConvoMembers = frag.getMembers();
        final Set<GroupMember> otherConvoMembers = frag.getOtherMembers();
        final GroupMember[] otherConvoMembersArry = otherConvoMembers.toArray(new GroupMember[otherConvoMembers.size
                ()]);
        if (title.equals(Constants.CONVO_DRAWER_ADD_SIDE_CONVO_MEMBERS) || title.equals(Constants.CONVO_DRAWER_ADD_WHISPER_MEMBERS)) {
            Set<GroupMember> otherGroupMembers = mConvoDrawerFragment.getMainConvo().getMembers();
            Set<GroupMember> availableMembers = Sets.difference(otherGroupMembers, allConvoMembers);
            if (availableMembers.isEmpty()) {
                Toast.makeText(this, "No one to add!", Toast.LENGTH_SHORT).show();
            } else {
                showAddConvoMembersDialog(groupId, convoId, convoType, availableMembers.toArray(new GroupMember[availableMembers.size()]));
            }
        } else if (title.equals(Constants.CONVO_DRAWER_REMOVE_SIDE_CONVO_MEMBERS) || title.equals(Constants.CONVO_DRAWER_REMOVE_WHISPER_MEMBERS)) {
            showRemoveConvoMembersDialog(groupId, convoId, convoType, otherConvoMembersArry);
        } else if (title.equals(Constants.CONVO_DRAWER_DELETE_SIDE_CONVO) || title.equals(Constants.CONVO_DRAWER_DELETE_WHISPER)) {
            showDeleteConvoDialog(groupId, convoId, convoType, otherConvoMembersArry);
        } else {
            return false;
        }
        return true;
    }

    private void finalizeToggleConvo() {
        switchTitleToConvoInfo();
        //TODO: Find a more clever way to control when the convo drawer closes
//        mDrawerLayout.closeDrawer(Gravity.RIGHT);
        Application.get().getPreferences().edit()
                .putLong("chat_" + mConvoDrawerFragment.getCurrentConvo().getGroupId() + "_current_convo_id",
                        mConvoDrawerFragment.getCurrentConvo().getConvoId()).commit();
        invalidateOptionsMenu();
    }

    private void initDrawers() {

        // Getting reference to the DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mChatDrawerFragment = new ChatsDrawerFragment();
        mConvoDrawerFragment = new ConvosDrawerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.left_drawer, mChatDrawerFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.right_drawer, mConvoDrawerFragment).commit();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mConvoDrawerFragment != null && mConvoDrawerFragment.getCurrentConvo() != null) {
                    switchTitleToConvoInfo();
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (mConvoDrawerFragment != null && mConvoDrawerFragment.getMainConvo() != null) {
                    switchTitleToMainChatInfo();
                }
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        invalidateOptionsMenu();
    }

    private void initMessageViews() {
        mWhosTpying = (TextView) findViewById(R.id.whosTyping);
        mWhosTpyingIds = Maps.newHashMap();
        resetTimer();
        resetIsTypingField();
        resetSendIsTypingTask();
        mPendingMessage = (EditText) findViewById(R.id.pendingMessage);
        mPendingMessage.setVisibility(View.GONE);
        mSend = (ImageView) findViewById(R.id.send);
        mSend.setVisibility(View.GONE);
        setSendButtonEnable(!mPendingMessage.getText().toString().isEmpty());
        mAttach = (ImageView) findViewById(R.id.attach);
        mAttach.setVisibility(View.GONE);
        setActivityImageState(ActivityImageState.NONE);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSendButtonEnable(false);
                String text = mPendingMessage.getText() == null ? "" : mPendingMessage.getText().toString();
                mPendingMessage.setText("");
                boolean newWhisper = text.contains("<whisper>");
                if (newWhisper) {
                    GeneralHelper.showLongToast(MainActivity.this, "WANTS TO WHISPER");
                }
                boolean newSideConvo = text.contains("<side-convo>");
                if (newSideConvo) {
                    GeneralHelper.showLongToast(MainActivity.this, "WANTS TO SIDE-CONVO");
                }
                final ConvoFragment frag = mConvoDrawerFragment.getCurrentConvo();
                final Bundle b = new Bundle();
                b.putString(INTENT_TYPE, SEND_MESSAGE_REQUEST);
                b.putLong(USER_ID, getAccountUserId());
                b.putLong(GROUP_ID, frag.getGroupId());
                b.putInt(CONVO_TYPE, frag.getConvoType().getValue());
                b.putLong(CONVO_ID, frag.getConvoId());
                b.putString(MESSAGE, text);
                if (mState == ActivityImageState.MESSAGE_ATTACHING) {
                    b.putString(IMAGE_PATH, mLatestPath);
                }
                sendBackgroundRequest(b);
                setActivityImageState(ActivityImageState.NONE);
            }
        });
        mAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImageChooser(ActivityImageState.MESSAGE_ATTACHING);
            }
        });
        mPendingMessage.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                adjustViewPagerHeight();
                final boolean hasText = s.length() != 0;
                final boolean isTyping = hasText;
                setSendButtonEnable(hasText);
                if (isTyping) {
                    if (!mSendIsTypingTask.hasStarted()) {
                        mTimer.schedule(mSendIsTypingTask, 0l, IS_TYPING_INTERVAL_FOR_ME);
                    } else {
                        mSendIsTypingTask.keepGoing();
                    }
                } else {
                    resetSendIsTypingTask();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }

    private void adjustViewPagerHeight() {
        ViewGroup.LayoutParams params = mPendingMessage.getLayoutParams();
        if (mPendingMessage.getLineCount() > 1) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            final float scale = getResources().getDisplayMetrics().density;
            params.height = (int) (50 * scale + 0.5f);
        }
    }

    private void showAddChatMembersDialog(long groupId) {
        final AddChatMembersDialog d = new AddChatMembersDialog();
        final Set<Long> memberIds = Sets.newHashSet(GeneralHelper.convertLong(getGroupMemberIds
                (groupId)));
        final Set<Long> allPossibleIds = Sets.newHashSet(Arrays.asList(3l, 6l, 8l, 9l, 23l)); //TODO: TEMPORARY!!!!
        final Set<Long> availalbleIds = Sets.difference(allPossibleIds, memberIds);
        if (availalbleIds.isEmpty()) {
            Toast.makeText(this, "No one to add!", Toast.LENGTH_SHORT).show();
        } else {
            final GroupMember[] gms = getGroupMembers(GeneralHelper.convertLong(availalbleIds.toArray(new Long[availalbleIds.size()])));
            d.setGroupId(groupId).setGroupMembers(gms);
            d.show(getSupportFragmentManager(), "AddChatMembersDialog");
        }
    }

    private void showRemoveChatMembersDialog(long groupId) {
        RemoveChatMembersDialog d = new RemoveChatMembersDialog();
        d.setGroupId(groupId).setGroupMembers(getOtherGroupMembers(groupId));
        d.show(getSupportFragmentManager(), "RemoveChatMembersDialog");
    }

    private void showDeleteChatDialog(long groupId) {
        DeleteChatDialog d = new DeleteChatDialog();
        d.setGroupId(groupId);
        d.show(getSupportFragmentManager(), "DeleteChatDialog");
    }

    private void showAddConvoMembersDialog(long groupId, long convoId, ConvoType convoType, GroupMember[] groupMembers) {
        AddConvoMembersDialog d = new AddConvoMembersDialog();
        d.setGroupId(groupId).setConvoId(convoId).setConvoType(convoType).setGroupMembers(groupMembers);
        d.show(getSupportFragmentManager(), "AddConvoMembersDialog");
    }

    private void showRemoveConvoMembersDialog(long groupId, long convoId, ConvoType convoType, GroupMember[] groupMembers) {
        RemoveConvoMembersDialog d = new RemoveConvoMembersDialog();
        d.setGroupId(groupId).setConvoId(convoId).setConvoType(convoType).setConvoMembers(groupMembers);
        d.show(getSupportFragmentManager(), "RemoveConvoMembersDialog");
    }

    private void showDeleteConvoDialog(long groupId, long convoId, ConvoType convoType, GroupMember[] groupMembers) {
        DeleteConvoDialog d = new DeleteConvoDialog();
        d.setGroupId(groupId).setConvoId(convoId).setConvoType(convoType).setConvoMembers(groupMembers);
        d.show(getSupportFragmentManager(), "DeleteConvoDialog");
    }

    private void setActivityImageState(ActivityImageState state) {
        setActivityImageState(state, true, true);
    }

    private void setActivityImageState(ActivityImageState state, boolean doEnable, boolean doImageToggle) {
        mState = state;

        if (doImageToggle) {
            if (mState == ActivityImageState.MESSAGE_ATTACHING) {
                mAttach.setImageResource(R.drawable.ic_action_attachment_attached);
                setSendButtonEnable(true);
            } else {
                mAttach.setImageResource(R.drawable.ic_action_attachment);
            }
        }

        if (doEnable) {
            if (mState == ActivityImageState.NONE) {
                mLatestCreatedName = null;
                mLatestChosenMemberIds = null;
                mLatestPath = null;
                mAttach.setEnabled(true);
            } else {
                mAttach.setEnabled(false);
            }
        }
    }

    private void setSendButtonEnable(boolean enabled) {
        mSend.setEnabled(enabled);
        mSend.setImageResource(enabled ? R.drawable.ic_action_send_now : R.drawable.ic_action_send_now_disabled);
    }

    private void displayImageChooser(ActivityImageState state) {

        setActivityImageState(state, false, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendCameraIntent();
            }
        }).setNeutralButton(R.string.gallery, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    sendGalleryIntent();
                } else {
                    sendGalleryIntentPreKitKat();
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setActivityImageState(ActivityImageState.NONE);
            }
        });

        builder.setMessage(R.string.select_image_message).setTitle(R.string.select_image_title);

        // Create the AlertDialog
        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    private void switchTitleToConvoInfo() {
        if (mConvoDrawerFragment.getCurrentConvo() != null) {
            setTitle(mConvoDrawerFragment.getCurrentConvo().getName());
            int icon;
            switch (mConvoDrawerFragment.getCurrentConvo().getConvoType()) {
                case MAIN_CHAT:
                    icon = R.drawable.ic_launcher;
                    break;
                case SIDE_CONVO:
                    icon = R.drawable.ic_action_not_secure;
                    break;
                case WHISPER:
                    icon = R.drawable.ic_action_secure;
                    break;
                default:
                    icon = 0;
                    break;
            }
            getActionBar().setIcon(icon);
        }
    }

    private void switchTitleToMainChatInfo() {
        setTitle(mConvoDrawerFragment.getMainConvo().getName());
        getActionBar().setIcon(R.drawable.ic_launcher);
    }

    private void authenticated() {
        if (mLoginDialog.isAdded()) {
            mLoginDialog.dismiss();
        } else if (mRegDialog.isAdded()) {
            mRegDialog.dismiss();
        }
        String fullName = getAccountUserFullName();
        String message = String.format(Locale.getDefault(), "Welcome, %s! Holy shit, you're swagged out kid!",
                fullName);
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    private void sendGalleryIntentPreKitKat() {
        Log.d(LOGTAG, "Running pre-Kit-Kat");
        Intent i = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, GALLERY_REQUEST);
    }

    @TargetApi(19)
    private void sendGalleryIntent() {
        Log.d(LOGTAG, "Running Kit-Kat or higher!");
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(i, GALLERY_REQUEST);
    }

    private void sendCameraIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (i.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageUtil.createTimestampedImageFile(this);
                mLatestPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, e.getMessage());
            }
            // Save a file: path for use with ACTION_VIEW intents
            // Continue only if the File was successfully created
            if (photoFile != null) {
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(i, CAMERA_REQUEST);
            }
        }
    }

    private void logout() {
        final Bundle b = new Bundle();
        b.putString(INTENT_TYPE, LOGOUT_REQUEST);
        b.putLong(USER_ID, getAccountUserId());
        b.putString(USER_NAME, getAccountUserFullName());
        sendBackgroundRequest(b);

        try {
            GoogleCloudMessaging.getInstance(this).unregister();
        } catch (IOException e) {
            GeneralHelper.reportMessage(MainActivity.this, LOGTAG, e.getMessage());
        }

        resetTimer();
        resetIsTypingField();
        Application.get().getDatabaseManager().clear();
        Application.get().getPreferences().edit().clear().commit();
        Application.get().setCurrentChat(null);
        mConvoDrawerFragment.clear();
        mChatDrawerFragment.swapCursor(null);
        mChatDrawerFragment.updateView();
        mConvoPagerAdapter = null;
        mViewPager.setAdapter(null);
        mViewPager.setOnPageChangeListener(null);
        mSend.setVisibility(View.GONE);
        mAttach.setVisibility(View.GONE);
        mPendingMessage.setVisibility(View.GONE);
        mDrawerLayout.closeBothDrawers();
        invalidateOptionsMenu();
        getActionBar().setIcon(R.drawable.ic_launcher);
        setTitle(R.string.app_name);
        Application.get().clearLog();

        mLoginDialog.show(getSupportFragmentManager(), "LoginDialog");
    }

    private class ConvoPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

        protected ConvoPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mConvoDrawerFragment != null ? mConvoDrawerFragment.getCount() : 0;
        }

        @Override
        public Fragment getItem(int position) {
            return mConvoDrawerFragment.getItem(position);
        }

        @Override
        public int getItemPosition(Object item) {
            return POSITION_NONE;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            if (position >= getCount() || mConvoDrawerFragment.getItem(position) == null) {
                return;
            }
            toggleConvo(position);
        }

        protected void handleMessage(int numMessages) {
            if (mConvoDrawerFragment == null) {
                return;
            }
            for (ConvoFragment frag : mConvoDrawerFragment.getItems()) {
                frag.loadMessages(numMessages);
            }
        }

        protected void handleConvoMembers(long groupId, long convoId, long[] memberIds) {
            if (mConvoDrawerFragment == null || mConvoDrawerFragment.getCurrentConvo().getGroupId() != groupId) {
                return;
            }
            for (ConvoFragment frag : mConvoDrawerFragment.getItems()) {
                if (frag.getConvoId() != convoId) {
                    continue;
                }
                GroupMember[] gms = getGroupMembers(memberIds);
                frag.addMembers(gms);
                mConvoDrawerFragment.updateView();
            }
        }

        protected void setChat(Group chat) {
            ConvoFragment[] mainFrag = new ConvoFragment[]{generateMainConvoFragment(chat)};
            ConvoFragment[] sideConvoFrags = generateSideConvoFragments(chat);
            ConvoFragment[] whisperFrags = generateWhisperFragments(chat);
            addConvos(GeneralHelper.concat(mainFrag, sideConvoFrags, whisperFrags));
        }

        protected void setConvo(long convoId) {
            if (mConvoDrawerFragment == null) {
                return;
            }
            mConvoDrawerFragment.setConvo(mConvoDrawerFragment.getPosition(convoId));
            mViewPager.setCurrentItem(mConvoDrawerFragment.getPosition(convoId), true);
        }

        protected void setConvo(int position) {
            mConvoDrawerFragment.setConvo(position);
            mViewPager.setCurrentItem(position, true);
        }

        protected void loadMessagesAfterFetch(long groupId, long convoId, boolean canFetchMore) {
            ConvoFragment frag = mConvoDrawerFragment.getCurrentConvo();
            if (frag == null || frag.getGroupId() != groupId || frag.getConvoId() != convoId) {
                return;
            }
            frag.loadMessagesAfterFetch(canFetchMore);
            invalidateOptionsMenu();
        }

        private ConvoFragment generateMainConvoFragment(Group chat) {
            Bundle b = new Bundle();
            b.putLong("chatId", chat.getGlobalId());
            b.putLong("convoId", 0);
            b.putInt("convoType", ConvoType.MAIN_CHAT.getValue());
            b.putString("name", chat.getName());
            b.putBoolean("canFetchMoreMessages", true);
            ConvoFragment frag = new ConvoFragment();
            frag.setArguments(b);
            frag.addMembers(getGroupMembers(chat.getGlobalId()));
            return frag;
        }

        private void addConvos(ConvoFragment... frags) {
            for (ConvoFragment frag : frags) {
                mConvoDrawerFragment.add(frag);
                notifyDataSetChanged();
            }
        }

        private ConvoFragment[] generateSideConvoFragments(Group chat) {
            String sideConvosStr = chat.getSideChats();
            if (sideConvosStr.isEmpty()) {
                return new ConvoFragment[]{};
            }
            String[] sideConvosStrSplit = sideConvosStr.split("\\|");
            ConvoFragment[] frags = new ConvoFragment[sideConvosStrSplit.length];
            int index = 0;
            for (String sideConvoStr : sideConvosStrSplit) {
                frags[index++] = generateConvoFragment(sideConvoStr, ConvoType.SIDE_CONVO, chat.getGlobalId());
            }
            return frags;
        }

        private ConvoFragment[] generateWhisperFragments(Group chat) {
            String whispersStr = chat.getWhispers();
            if (whispersStr.isEmpty()) {
                return new ConvoFragment[]{};
            }
            String[] whispersStrSplit = whispersStr.split("\\|");
            ConvoFragment[] frags = new ConvoFragment[whispersStrSplit.length];
            int index = 0;
            for (String whisperStr : whispersStrSplit) {
                frags[index++] = generateConvoFragment(whisperStr, ConvoType.WHISPER, chat.getGlobalId());
            }
            return frags;
        }

        private ConvoFragment generateConvoFragment(String convo, ConvoType type, final long chatId) {
            String[] sideConvoStrSplit = convo.split(":");
            long id = Long.valueOf(sideConvoStrSplit[0]);
            String name = sideConvoStrSplit[1];
            Bundle b = new Bundle();
            b.putLong("chatId", chatId);
            b.putLong("convoId", id);
            b.putInt("convoType", type.getValue());
            b.putString("name", name);
            b.putBoolean("canFetchMoreMessages", true);
            final ConvoFragment frag = new ConvoFragment();
            frag.setArguments(b);
            return frag;
        }

        private ConvoFragment generateConvoFragment(long convoId, String name, ConvoType type, final long chatId) {
            Bundle b = new Bundle();
            b.putLong("chatId", chatId);
            b.putLong("convoId", convoId);
            b.putInt("convoType", type.getValue());
            b.putString("name", name);
            b.putBoolean("canFetchMoreMessages", true);
            final ConvoFragment frag = new ConvoFragment();
            frag.setArguments(b);
            return frag;
        }

        private void handleAddChatMembers(long groupId, long[] members) {
            if (Application.get().getCurrentChat() == null || Application.get().getCurrentChat().getGlobalId() !=
                    groupId || mConvoDrawerFragment == null) {
                return;
            }
            mConvoDrawerFragment.getMainConvo().addMembers(getGroupMembers(members));
            mConvoDrawerFragment.updateView();
        }

        private void handleRemoveChatMembers(long[] members) {
            if (mConvoDrawerFragment == null || mConvoDrawerFragment.getItems().isEmpty()) {
                return;
            }
            mConvoDrawerFragment.getMainConvo().removeMembers(members);
            for (ConvoFragment frag : mConvoDrawerFragment.getItems()) {
                if (frag.getConvoType() == ConvoType.MAIN_CHAT) {
                    continue;
                }
                final Bundle b = new Bundle();
                b.putString(INTENT_TYPE, REMOVE_CONVO_MEMBERS_REQUEST);
                b.putLong(GROUP_ID, frag.getGroupId());
                b.putLong(CONVO_ID, frag.getConvoId());
                b.putInt(CONVO_TYPE, frag.getConvoType().getValue());
                b.putLongArray(MEMBER_IDS, members);
                b.putLongArray(ALL_MEMBER_IDS, frag.getOtherMemberIds());
                sendBackgroundRequest(b);
            }
        }

        private void handleAddConvoMembers(long convoId, long[] members) {
            if (Application.get().getCurrentChat() == null || mConvoDrawerFragment == null) {
                return;
            }
            final ConvoFragment frag = mConvoDrawerFragment.getFrag(convoId);
            if (frag == null) {
                return;
            }
            frag.addMembers(getGroupMembers(members));
            mConvoDrawerFragment.updateView();
        }


        private void handleRemoveConvoMembers(long convoId, long[] members) {
            if (mConvoDrawerFragment == null) {
                return;
            }
            ConvoFragment frag = mConvoDrawerFragment.getFrag(convoId);
            frag.removeMembers(members);
            if (frag.getMembers().size() < 2) {
                final Bundle b = new Bundle();
                b.putString(INTENT_TYPE, DELETE_CONVO_REQUEST);
                b.putLong(GROUP_ID, frag.getGroupId());
                b.putLong(CONVO_ID, frag.getConvoId());
                b.putInt(CONVO_TYPE, frag.getConvoType().getValue());
                b.putLongArray(MEMBER_IDS, GeneralHelper.convertLong(frag.getMemberIds()));
                sendBackgroundRequest(b);
            }
        }

        public void handleCreateConvo(long groupId, long convoId, String name, ConvoType type) {
            if (mConvoDrawerFragment == null) {
                return;
            }
            ConvoFragment frag = mConvoPagerAdapter.generateConvoFragment(convoId, name, type, groupId);
            mConvoDrawerFragment.add(frag);
            mConvoPagerAdapter.notifyDataSetChanged();
            invalidateOptionsMenu();
        }

        public void handleDeleteConvo(long convoId) {
            if (mConvoDrawerFragment == null) {
                return;
            }
            mConvoDrawerFragment.removeFrag(convoId);
            mConvoPagerAdapter.notifyDataSetChanged();
            invalidateOptionsMenu();
        }
    }

    private class AmITypingTask extends TimerTask {

        private AtomicBoolean mHasStarted = new AtomicBoolean(false);
        private AtomicBoolean mKeepGoing = new AtomicBoolean(true);

        @Override
        public void run() {
            mHasStarted.set(true);
            if (mKeepGoing.getAndSet(false)) {
                sendIsTyping(true);
            } else {
                resetSendIsTypingTask();
            }
        }

        @Override
        public boolean cancel() {
            sendIsTyping(false);
            return super.cancel();
        }

        public void keepGoing() {
            mKeepGoing.set(true);
        }

        public boolean hasStarted() {
            return mHasStarted.get();
        }

        private void sendIsTyping(boolean isTyping) {
            final Bundle b = new Bundle();
            b.putString(INTENT_TYPE, IS_TYPING_REQUEST);
            b.putLong(USER_ID, getAccountUserId());
            b.putLong(GROUP_ID, Application.get().getCurrentChat().getGlobalId());
            b.putBoolean(IS_TYPING, isTyping);
            sendBackgroundRequest(b);
        }
    }

    private class IsSomeoneElseTypingTask extends TimerTask {

        private final long mId;
        private final long mGroupId;

        public IsSomeoneElseTypingTask(long id, long groupId) {
            mId = id;
            mGroupId = groupId;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateIsTpying(mId, mGroupId, false);
                }
            });
        }
    }
}