package com.gjk;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.helper.GeneralHelper;
import com.gjk.service.ChassipService;
import com.gjk.utils.media2.ImageManager;
import com.gjk.utils.media2.ImageUtil;
import com.gjk.views.DrawerLayout;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.gjk.Constants.ADD_CHAT_MEMBERS_RESPONSE;
import static com.gjk.Constants.ADD_CONVO_MEMBERS_RESPONSE;
import static com.gjk.Constants.CAMERA_REQUEST;
import static com.gjk.Constants.CAN_FETCH_MORE_MESSAGES;
import static com.gjk.Constants.CHASSIP_ACTION;
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
import static com.gjk.Constants.GCM_MESSAGE_RESPONSE;
import static com.gjk.Constants.GET_ALL_GROUPS_RESPONSE;
import static com.gjk.Constants.GROUP_ID;
import static com.gjk.Constants.GROUP_UPDATE_RESPONSE;
import static com.gjk.Constants.IMAGE_PATH;
import static com.gjk.Constants.IMAGE_URL;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.LAST_NAME;
import static com.gjk.Constants.LOGIN_JSON;
import static com.gjk.Constants.LOGIN_RESPONSE;
import static com.gjk.Constants.LOGOUT_REQUEST;
import static com.gjk.Constants.MEMBER_IDS;
import static com.gjk.Constants.MESSAGE;
import static com.gjk.Constants.NUM_MESSAGES;
import static com.gjk.Constants.OFFSCREEN_PAGE_LIMIT;
import static com.gjk.Constants.PASSWORD;
import static com.gjk.Constants.PROPERTY_REG_ID;
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

    private enum ActivityImageState {
        NONE,
        REGISTERING,
        CHAT_CREATING,
        MESSAGE_ATTACHING,
    }

    private BroadcastReceiver mServerResponseReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String type = extras.getString(INTENT_TYPE);
                if (type != null) {

                    synchronized (syncObj) {

                        if (type.equals(SEND_MESSAGE_RESPONSE)) {

                            mChatDrawerFragment.updateView();
                            if (mConvoPagerAdapter != null) {
                                if (Application.get().getCurrentChat() != null && extras.getLong(GROUP_ID) ==
                                        Application.get().getCurrentChat().getGlobalId() && mConvoPagerAdapter != null) {
                                    mConvoPagerAdapter.handleMessage(extras.getInt(NUM_MESSAGES));
                                }
                                mSend.setEnabled(false);
                                mAttach.setEnabled(true);
                            }

                        } else if (type.equals(GCM_MESSAGE_RESPONSE)) {

                            mChatDrawerFragment.updateView();
                            if (mConvoPagerAdapter != null) {
                                if (Application.get().getCurrentChat() != null && extras.getLong(GROUP_ID) ==
                                        Application.get().getCurrentChat().getGlobalId() && mConvoPagerAdapter != null) {
                                    mConvoPagerAdapter.handleMessage(extras.getInt(NUM_MESSAGES));
                                }
                            }

                        } else if (type.equals(FETCH_CONVO_MEMBERS_RESPONSE)) {

                            if (mConvoPagerAdapter != null) {
                                mConvoPagerAdapter.handleConvoMembers(extras.getLong(GROUP_ID),
                                        extras.getLong(CONVO_ID), extras.getLongArray(MEMBER_IDS));
                            }

                        } else if (type.equals(GROUP_UPDATE_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            mChatDrawerFragment.swapCursor(getGroupsCursor());
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                toggleChat(groupId);
                            }

                        } else if (type.equals(CREATE_CONVO_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                final long convoId = extras.getLong(CONVO_ID);
                                final String name = extras.getString(CONVO_NAME);
                                final ConvoType convoType = ConvoType.getFromValue(extras.getInt(CONVO_TYPE));
                                mConvoPagerAdapter.handleCreateConvo(groupId, convoId, name, convoType);
                            }

                        } else if (type.equals(DELETE_CONVO_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                final long convoId = extras.getLong(CONVO_ID);
                                mConvoPagerAdapter.handleDeleteConvo(convoId);
                            }

                        } else if (type.equals(ADD_CHAT_MEMBERS_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            final long[] members = extras.getLongArray(MEMBER_IDS);
                            mChatDrawerFragment.swapCursor(getGroupsCursor());
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat()
                                    .getGlobalId() == groupId) {
                                mConvoPagerAdapter.handleAddChatMembers(groupId, members);
                            }

                        } else if (type.equals(REMOVE_CHAT_MEMBERS_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                                    groupId) {
                                final long[] members = extras.getLongArray(MEMBER_IDS);
                                mConvoPagerAdapter.handleRemoveChatMembers(members);
                                mConvoDrawerFragment.updateView();
                            }

                        } else if (type.equals(ADD_CONVO_MEMBERS_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                                    groupId) {
                                final long[] members = extras.getLongArray(MEMBER_IDS);
                                final long convoId = extras.getLong(CONVO_ID);
                                mConvoPagerAdapter.handleAddConvoMembers(convoId, members);
                                mConvoDrawerFragment.updateView();
                            }

                        } else if (type.equals(REMOVE_CONVO_MEMBERS_RESPONSE)) {

                            final long groupId = extras.getLong(GROUP_ID);
                            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                                    groupId) {
                                final long[] members = extras.getLongArray(MEMBER_IDS);
                                final long convoId = extras.getLong(CONVO_ID);
                                mConvoPagerAdapter.handleRemoveConvoMembers(convoId, members);
                                mConvoDrawerFragment.updateView();
                            }

                        } else if (type.equals(CONVO_UPDATE_RESPONSE)) {

                            long convoId = extras.getLong(CONVO_ID);
                            List<ConvoFragment> frags = mConvoDrawerFragment.getItems();
                            for (ConvoFragment frag : frags) {
                                if (frag.getConvoId() == convoId) {
                                    toggleChat(frag.getGroupId());
                                    return;
                                }
                            }

                        } else if (type.equals(DELETE_CHAT_RESPONSE)) {

                            long groupId = extras.getLong(GROUP_ID);
                            mChatDrawerFragment.swapCursor(getGroupsCursor());
                            if (Application.get().getCurrentChat().getGlobalId() == groupId) {
                                toggleChat(getFirstStoredGroup());
                            }

                        } else if (type.equals(FETCH_MORE_MESSAGES_RESPONSE)) {

                            if (mConvoPagerAdapter != null) {
                                mConvoPagerAdapter.loadMessagesAfterFetch(extras.getLong(GROUP_ID),
                                        extras.getLong(CONVO_ID), extras.getBoolean(CAN_FETCH_MORE_MESSAGES));
                            }

                        } else if (type.equals(LOGIN_RESPONSE) || type.equals(REGISTER_RESPONSE)) {

                            authenticated();

                        } else if (type.equals(GET_ALL_GROUPS_RESPONSE)) {

                            mChatDrawerFragment.swapCursor(getGroupsCursor());

                        } else if (type.equals(START_PROGRESS)) {

                            setProgressBarIndeterminateVisibility(true);

                        } else if (type.equals(STOP_PROGRESS)) {

                            setProgressBarIndeterminateVisibility(false);

                        } else if (type.equals(ERROR) || type.equals(UNSUCCESSFUL)) {

                            mSend.setEnabled(false);
                            mAttach.setEnabled(true);
                            if (extras.getBoolean(SHOW_TOAST)) {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE), true);
                            } else {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE));
                            }
                            setProgressBarIndeterminateVisibility(false);

                        } else if (type.equals(VERBOSE)) {

                            if (extras.getBoolean(SHOW_TOAST)) {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE), true);
                            } else {
                                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, extras.getString(MESSAGE));
                            }

                        } else {

                            setProgressBarIndeterminateVisibility(false);
                            GeneralHelper.reportMessage(MainActivity.this, LOGTAG, "Received unhandled intent type=" + type);
                        }
                    }

                } else {

                    setProgressBarIndeterminateVisibility(false);
                    GeneralHelper.reportMessage(MainActivity.this, LOGTAG, "Received null intent type");
                }

            } else {

                setProgressBarIndeterminateVisibility(false);
                GeneralHelper.reportMessage(MainActivity.this, LOGTAG, "Received null extras");
            }
        }
    };

    @Override
    public void onNewIntent(Intent i) {
        Log.d(LOGTAG, "Swag");
        if (i.getExtras() != null && i.getExtras().containsKey("group_id")) {
            long chatId = i.getExtras().getLong("group_id");
            long convoId = i.getExtras().getLong("convo_id");
            Application.get().getPreferences().edit().putLong("current_group_id", chatId).commit();
            Application.get().getPreferences().edit().putLong("chat_" + chatId + "_current_convo_id", convoId).commit();
        }
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
                new IntentFilter(CHASSIP_ACTION));

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
//            menu.findItem(R.id.action_fetch_more_messages).setVisible(mConvoPagerAdapter.getCurrentConvo()
//                    .isCanFetchMoreMessagesSet());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        Application.get().activityResumed();

        if (!GeneralHelper.getKachisCachePref()) {
            ImageManager.getInstance(getSupportFragmentManager()).resume();
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("group_id")) {
            toggleChat(getIntent().getExtras().getLong("group_id"));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Application.get().getPreferences().contains(LOGIN_JSON)
                || !Application.get().getPreferences().contains(PROPERTY_REG_ID)) {
            if (!mLoginDialog.isAdded() && !mRegDialog.isAdded()) {
                mLoginDialog.show(getSupportFragmentManager(), "LoginDialog");
            }
        } else if (Application.get().getPreferences().contains("current_group_id")) {
            if (Application.get().getCurrentChat() == null) {
                toggleChat(Application.get().getPreferences().getLong("current_group_id", 0));
            }
        } else {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.get().activityPaused();

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
            long convoId = frag.getConvoId();
            ConvoType convoType = frag.getConvoType();
            switch (convoType) {
                case MAIN_CHAT:
                    return handleChatClicked(item.getTitle(), groupId);
                case SIDE_CONVO:
                case WHISPER:
                    return handleConvoClicked(item.getTitle(), groupId, convoId, convoType,
                            frag.getOtherMembers().toArray(new GroupMember[]{}));
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
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.CREATE_CONVO_REQUEST)
                .putExtra(GROUP_ID, Application.get().getCurrentChat().getGlobalId())
                .putExtra(CONVO_TYPE, dialog.getConvoType().getValue())
                .putExtra(CONVO_NAME, dialog.getConvoName())
                .putExtra(MEMBER_IDS, dialog.getSelectedIds());
        sendServerRequest(i);
    }

    @Override
    public void onAddChatMembersDialogPositiveClick(AddChatMembersDialog dialog) {
        dialog.dismiss();
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.ADD_CHAT_MEMBERS_REQUEST)
                .putExtra(GROUP_ID, dialog.getGroupId())
                .putExtra(MEMBER_IDS, dialog.getSelectedIds());
        sendServerRequest(i);
    }

    @Override
    public void onRemoveChatMembersDialogPositiveClick(RemoveChatMembersDialog dialog) {
        dialog.dismiss();
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.REMOVE_CHAT_MEMBERS_REQUEST)
                .putExtra(GROUP_ID, dialog.getGroupId())
                .putExtra(MEMBER_IDS, dialog.getSelectedIds());
        sendServerRequest(i);
    }

    @Override
    public void onDeleteChatDialogPositiveClick(DeleteChatDialog dialog) {
        dialog.dismiss();
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.DELETE_CHAT_REQUEST)
                .putExtra(GROUP_ID, dialog.getGroupId());
        sendServerRequest(i);
    }

    @Override
    public void onAddConvoMembersDialogPositiveClick(AddConvoMembersDialog dialog) {
        dialog.dismiss();
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.ADD_CONVO_MEMBERS_REQUEST)
                .putExtra(GROUP_ID, dialog.getGroupId())
                .putExtra(CONVO_ID, dialog.getConvoId())
                .putExtra(CONVO_TYPE, dialog.getConvoType().getValue())
                .putExtra(MEMBER_IDS, dialog.getSelectedIds());
        sendServerRequest(i);
    }

    @Override
    public void onRemoveConvoMembersDialogPositiveClick(RemoveConvoMembersDialog dialog) {
        dialog.dismiss();
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.REMOVE_CONVO_MEMBERS_REQUEST)
                .putExtra(GROUP_ID, dialog.getGroupId())
                .putExtra(CONVO_ID, dialog.getConvoId())
                .putExtra(CONVO_TYPE, dialog.getConvoType().getValue())
                .putExtra(MEMBER_IDS, dialog.getSelectedIds());
        sendServerRequest(i);
    }


    @Override
    public void onDeleteConvoDialogPositiveClick(DeleteConvoDialog dialog) {
        dialog.dismiss();
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.DELETE_CONVO_REQUEST)
                .putExtra(GROUP_ID, dialog.getGroupId())
                .putExtra(CONVO_ID, dialog.getConvoId())
                .putExtra(CONVO_TYPE, dialog.getConvoType().getValue())
                .putExtra(MEMBER_IDS, dialog.getIds());
        sendServerRequest(i);
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
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.LOGIN_REQUEST)
                .putExtra(EMAIL, dialog.getEmail())
                .putExtra(PASSWORD, dialog.getPassword());
        sendServerRequest(i);
    }

    @Override
    public void onLoginDialogFacebookClick(LoginDialog dialog) {
        mLoginDialog = dialog;
        Intent i = new Intent(this, ChassipService.class);
        i.putExtra(INTENT_TYPE, Constants.LOGIN_REQUEST)
                .putExtra(EMAIL, (String) dialog.getFacebookUser().asMap().get("email"))
                .putExtra(PASSWORD, dialog.getFacebookUser().getId() + dialog.getFacebookUser().asMap().get("email"));
        GeneralHelper.logoutOfFacebook(this);
        sendServerRequest(i);
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
                    Intent i = new Intent(MainActivity.this, ChassipService.class);
                    i.putExtra(INTENT_TYPE, REGISTER_FACEBOOK_REQUEST)
                            .putExtra(FIRST_NAME, dialog.getFacebookUser().getFirstName())
                            .putExtra(LAST_NAME, dialog.getFacebookUser().getLastName())
                            .putExtra(EMAIL, (String) dialog.getFacebookUser().asMap().get("email"))
                            .putExtra(PASSWORD, dialog.getFacebookUser().getId() + dialog.getFacebookUser().asMap().get("email"))
                            .putExtra(IMAGE_URL, aviUrl);
                    GeneralHelper.logoutOfFacebook(MainActivity.this);
                    sendServerRequest(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ).executeAsync();

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

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST) {
                try {
                    switch (requestCode) {
                        case GALLERY_REQUEST:
                            // grab path from gallery
                            Uri selectedImageUri = data.getData();
                            mLatestPath = getPath(selectedImageUri);
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
                    switch (mState) {
                        case MESSAGE_ATTACHING:
                            if (mLatestPath == null || mLatestPath.isEmpty()) {
                                mAttach.setEnabled(true);
                                resetState();
                            } else {
                                mAttach.setEnabled(false);
                                mSend.setEnabled(true);
                            }
                            break;
                        case CHAT_CREATING:
                            Intent i = new Intent(MainActivity.this, ChassipService.class);
                            i.putExtra(INTENT_TYPE, CREATE_CHAT_REQUEST)
                                    .putExtra(CONVO_NAME, mLatestCreatedName)
                                    .putExtra(MEMBER_IDS, mLatestChosenMemberIds)
                                    .putExtra(IMAGE_PATH, mLatestPath);
                            sendServerRequest(i);
                            resetState();
                            break;
                        case REGISTERING:
                            i = new Intent(MainActivity.this, ChassipService.class);
                            i.putExtra(INTENT_TYPE, REGISTER_REQUEST)
                                    .putExtra(FIRST_NAME, mRegDialog.getFirstName())
                                    .putExtra(LAST_NAME, mRegDialog.getLastName())
                                    .putExtra(EMAIL, mRegDialog.getEmail())
                                    .putExtra(PASSWORD, mRegDialog.getPassword())
                                    .putExtra(IMAGE_PATH, mLatestPath);
                            sendServerRequest(i);
                            resetState();
                            break;
                        default:
                            Log.e(LOGTAG, "Unhandled case=" + mState);
                    }
                } catch (Exception e) {
                    GeneralHelper.reportMessage(this, LOGTAG, e.getMessage());
                }
            }
        }
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    protected void sendServerRequest(Intent i) {
        Log.d(LOGTAG, String.format(Locale.getDefault(), "Sending %s to server: %s",
                i.getExtras().getString(INTENT_TYPE), i.getExtras().toString()));
        startService(i);
    }

    protected void toggleChat(long groupId) {
        toggleChat(getGroup(groupId));
    }

    protected void toggleChat(Group chat) {
        if (chat != null) {
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
            Application.get().getPreferences().edit()
                    .putLong("current_group_id", chat.getGlobalId()).commit();
            invalidateOptionsMenu();
            toggleConvo(Application.get().getPreferences().getLong("chat_" + chat.getGlobalId() + "_current_convo_id", 0));
        }
    }

    protected void toggleConvo(int position) {
        mConvoPagerAdapter.setConvo(position);
        finalizeToggleConvo();
    }

    protected void toggleConvo(long convoId) {
        mConvoPagerAdapter.setConvo(convoId);
        finalizeToggleConvo();
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

    private boolean handleConvoClicked(CharSequence title, long groupId, long convoId, ConvoType convoType, GroupMember[] groupMembers) {
        if (title.equals(Constants.CONVO_DRAWER_ADD_SIDE_CONVO_MEMBERS) || title.equals(Constants.CONVO_DRAWER_ADD_WHISPER_MEMBERS)) {
            showAddConvoMembersDialog(groupId, convoId, convoType);
        } else if (title.equals(Constants.CONVO_DRAWER_REMOVE_SIDE_CONVO_MEMBERS) || title.equals(Constants.CONVO_DRAWER_REMOVE_WHISPER_MEMBERS)) {
            showRemoveConvoMembersDialog(groupId, convoId, convoType, groupMembers);
        } else if (title.equals(Constants.CONVO_DRAWER_DELETE_SIDE_CONVO) || title.equals(Constants.CONVO_DRAWER_DELETE_WHISPER)) {
            showDeleteConvoDialog(groupId, convoId, convoType, groupMembers);
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
        mPendingMessage = (EditText) findViewById(R.id.pendingMessage);
        mPendingMessage.setVisibility(View.GONE);
        mSend = (ImageView) findViewById(R.id.send);
        mSend.setVisibility(View.GONE);
        mSend.setEnabled(false);
        mAttach = (ImageView) findViewById(R.id.attach);
        mAttach.setVisibility(View.GONE);
        mAttach.setEnabled(true);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSend.setEnabled(false);
                mAttach.setEnabled(false);
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
                ConvoFragment frag = mConvoDrawerFragment.getCurrentConvo();
                Intent i = new Intent(MainActivity.this, ChassipService.class);
                i.putExtra(INTENT_TYPE, SEND_MESSAGE_REQUEST)
                        .putExtra(USER_ID, getAccountUserId())
                        .putExtra(GROUP_ID, frag.getGroupId())
                        .putExtra(CONVO_TYPE, frag.getConvoType().getValue())
                        .putExtra(CONVO_ID, frag.getConvoId())
                        .putExtra(MESSAGE, text);
                if (mState == ActivityImageState.MESSAGE_ATTACHING) {
                    i.putExtra(IMAGE_PATH, mLatestPath);
                    resetState();
                }
                sendServerRequest(i);
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
                mSend.setEnabled(s.length() != 0 || mState == ActivityImageState.MESSAGE_ATTACHING);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adjustViewPagerHeight();
            }
        });
    }

    private void adjustViewPagerHeight() {
        ViewGroup.LayoutParams params = mPendingMessage.getLayoutParams();
        if (mPendingMessage.getLineCount() > 1) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            final float scale = getResources().getDisplayMetrics().density;
            int pixels = (int) (50 * scale + 0.5f);
            params.height = pixels;
        }
    }

    private void showAddChatMembersDialog(long groupId) {
        AddChatMembersDialog d = new AddChatMembersDialog();
        d.setGroupId(groupId);
        d.show(getSupportFragmentManager(), "AddChatMembersDialog");
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

    private void showAddConvoMembersDialog(long groupId, long convoId, ConvoType convoType) {
        AddConvoMembersDialog d = new AddConvoMembersDialog();
        d.setGroupId(groupId).setConvoId(convoId).setConvoType(convoType);
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

    private void displayImageChooser(ActivityImageState state) {

        mState = state;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void resetState() {
        mState = ActivityImageState.NONE;
        mLatestCreatedName = null;
        mLatestChosenMemberIds = null;
        mLatestPath = null;
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
                photoFile = ImageUtil.createImageFile(this);
                mLatestPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Save a file: path for use with ACTION_VIEW intents
            // Continue only if the File was successfully created
            if (photoFile != null) {
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(i, CAMERA_REQUEST);
            }
        }
    }

    private String getPath(Uri uri) {
        String result;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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

    private void logout() {
        Intent i = new Intent(MainActivity.this, ChassipService.class);
        i.putExtra(INTENT_TYPE, LOGOUT_REQUEST)
                .putExtra(USER_ID, getAccountUserId())
                .putExtra(USER_NAME, getAccountUserFullName());
        sendServerRequest(i);

        try {
            GoogleCloudMessaging.getInstance(this).unregister();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            if (position < getCount() && mConvoDrawerFragment.getItem(position) != null) {
                toggleConvo(position);
            }
        }

        protected void handleMessage(int numMessages) {
            if (mConvoDrawerFragment != null) {
                for (ConvoFragment frag : mConvoDrawerFragment.getItems()) {
                    frag.loadMessages(numMessages);
                }
            }
        }

        protected void handleConvoMembers(long groupId, long convoId, long[] memberIds) {
            if (mConvoDrawerFragment != null && mConvoDrawerFragment.getCurrentConvo().getGroupId() == groupId) {
                for (ConvoFragment frag : mConvoDrawerFragment.getItems()) {
                    if (frag.getConvoId() == convoId) {
                        GroupMember[] gms = getGroupMembers(memberIds);
                        frag.addMembers(gms);
                        mConvoDrawerFragment.updateView();
                    }
                }
            }
        }

        protected void setChat(Group chat) {
            ConvoFragment[] mainFrag = new ConvoFragment[]{generateMainConvoFragment(chat)};
            ConvoFragment[] sideConvoFrags = generateSideConvoFragments(chat);
            ConvoFragment[] whisperFrags = generateWhisperFragments(chat);
            addConvos(GeneralHelper.concat(mainFrag, sideConvoFrags, whisperFrags));
        }

        protected void setConvo(long convoId) {
            if (mConvoDrawerFragment != null) {
                mConvoDrawerFragment.setConvo(mConvoDrawerFragment.getPosition(convoId));
                mViewPager.setCurrentItem(mConvoDrawerFragment.getPosition(convoId), true);
            }
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
            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                    groupId) {
                if (mConvoDrawerFragment != null) {
                    mConvoDrawerFragment.getMainConvo().addMembers(getGroupMembers(members));
                    mConvoDrawerFragment.updateView();
                }
            }
        }

        private void handleRemoveChatMembers(long[] members) {
            if (mConvoDrawerFragment != null && !mConvoDrawerFragment.getItems().isEmpty()) {
                mConvoDrawerFragment.getMainConvo().removeMembers(members);
                for (ConvoFragment frag : mConvoDrawerFragment.getItems()) {
                    if (frag.getConvoType() != ConvoType.MAIN_CHAT) {
                        Intent i = new Intent(MainActivity.this, ChassipService.class);
                        i.putExtra(INTENT_TYPE, Constants.REMOVE_CONVO_MEMBERS_REQUEST)
                                .putExtra(GROUP_ID, frag.getGroupId())
                                .putExtra(CONVO_ID, frag.getConvoId())
                                .putExtra(CONVO_TYPE, frag.getConvoType().getValue())
                                .putExtra(MEMBER_IDS, members);
                        sendServerRequest(i);
                    }
                }
            }
        }

        private void handleAddConvoMembers(long convoId, long[] members) {
            if (Application.get().getCurrentChat() != null && Application.get().getCurrentChat().getGlobalId() ==
                    convoId) {
                if (mConvoDrawerFragment != null) {
                    mConvoDrawerFragment.getFrag(convoId).addMembers(getGroupMembers(members));
                    mConvoDrawerFragment.updateView();
                }
            }
        }

        private void handleRemoveConvoMembers(long convoId, long[] members) {
            if (mConvoDrawerFragment != null) {
                ConvoFragment frag = mConvoDrawerFragment.getFrag(convoId);
                frag.removeMembers(members);
                if (frag.getMembers().size() < 2) {
                    Intent i = new Intent(MainActivity.this, ChassipService.class);
                    i.putExtra(INTENT_TYPE, Constants.DELETE_CONVO_REQUEST)
                            .putExtra(GROUP_ID, frag.getGroupId())
                            .putExtra(CONVO_ID, frag.getConvoId())
                            .putExtra(CONVO_TYPE, frag.getConvoType().getValue())
                            .putExtra(MEMBER_IDS, GeneralHelper.convertLong(frag.getMemberIds()));
                    sendServerRequest(i);
                }
            }
        }

        public void handleCreateConvo(long groupId, long convoId, String name, ConvoType type) {
            if (mConvoDrawerFragment != null) {
                ConvoFragment frag = mConvoPagerAdapter.generateConvoFragment(convoId, name, type, groupId);
                mConvoDrawerFragment.add(frag);
                mConvoPagerAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
            }
        }

        public void handleDeleteConvo(long convoId) {
            if (mConvoDrawerFragment != null) {
                mConvoDrawerFragment.removeFrag(convoId);
                mConvoPagerAdapter.notifyDataSetChanged();
                invalidateOptionsMenu();
            }
        }
    }
}
