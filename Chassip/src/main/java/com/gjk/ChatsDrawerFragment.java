package com.gjk;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gjk.database.objects.Group;
import com.gjk.database.objects.Message;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.utils.media2.ImageManager;
import com.gjk.views.CacheImageView;
import com.gjk.views.RecyclingImageView;
import com.gjk.views.UpdatebaleListView;

import java.util.Date;

import static com.gjk.Constants.BASE_URL;
import static com.gjk.Constants.CHAT_CONTEXT_MENU_ID;
import static com.gjk.Constants.CHAT_DRAWER_ADD_CHAT_MEMBERS;
import static com.gjk.Constants.CHAT_DRAWER_DELETE_CHAT;
import static com.gjk.Constants.CHAT_DRAWER_REMOVE_CHAT_MEMBERS;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.MANUAL;
import static com.gjk.Constants.MANUAL_UPDATE_REQUEST;

/**
 * @author gpl
 */
public class ChatsDrawerFragment extends Fragment implements UpdatebaleListView.Boom {

    private static final int HEADER_HEIGHT = 100;
    private static final int STATE_PULL_TO_REFRESH = 1;
    private static final int STATE_RELEASE_TO_UPDATE = 2;
    private static final int HEADER_TOP = 0;

    private ChatDrawerAdapter mAdapter;
    private UpdatebaleListView mChatsList;
    private Button mCreateChat;
    private RelativeLayout headerRelativeLayout;
    private TextView headerTextView;
    private TextView lastUpdateDateTextView;

    private double startY;
    private boolean isLoading;
    private double deltaY;
    private int currentState;
    private Animation rotateAnimation;
    private Animation reverseRotateAnimation;

    private CreateChatDialog mCreateChatDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chats_drawer, null);
        mChatsList = (UpdatebaleListView) view.findViewById(R.id.chats_list);
        mCreateChat = (Button) view.findViewById(R.id.createChat);
        mCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCreateChatDialog == null || !mCreateChatDialog.isAdded()) {
                    mCreateChatDialog = new CreateChatDialog();
                }
                mCreateChatDialog.show(getActivity().getSupportFragmentManager(), "CreateChatDialog");
                ((MainActivity)getActivity()).findElligibleChassipUsers();
            }
        });
        final View header = inflater.inflate(R.layout.chats_list_header, mChatsList, false);
        headerRelativeLayout = (RelativeLayout) header.findViewById(R.id.header);
        headerTextView = (TextView) header.findViewById(R.id.head_tipsTextView);
        lastUpdateDateTextView = (TextView) header.findViewById(R.id.head_lastUpdatedDateTextView);
        mChatsList.addHeaderView(header);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final MainActivity activity = (MainActivity) getActivity();
        mAdapter = new ChatDrawerAdapter(activity, DatabaseHelper.getGroupsCursor());
        mChatsList.setBoom(this);
        mChatsList.setAdapter(mAdapter);
        mChatsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.getDrawerLayout().closeDrawer(Gravity.LEFT);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                activity.toggleChat(DatabaseHelper.getGroup(cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID))));
            }
        });
        registerForContextMenu(mChatsList);
        resetRefresh();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = (Cursor) mAdapter.getItem(info.position - 1);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Group.F_NAME)));
        menu.add(CHAT_CONTEXT_MENU_ID, v.getId(), 0, CHAT_DRAWER_ADD_CHAT_MEMBERS);//groupId, itemId, order, title
        menu.add(CHAT_CONTEXT_MENU_ID, v.getId(), 1, CHAT_DRAWER_REMOVE_CHAT_MEMBERS);
        menu.add(CHAT_CONTEXT_MENU_ID, v.getId(), 2, CHAT_DRAWER_DELETE_CHAT);
    }

    public void resetUserCursor() {
        mCreateChatDialog.resetCursor();
    }

    public void swapCursor(Cursor cursor) {
        if (mAdapter != null) {
            mAdapter.swapCursor(cursor);
        }
    }

    public void updateView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public Cursor getItem(int position) {
        if (mAdapter != null) {
            return (Cursor) mAdapter.getItem(position);
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //isDragging = true;
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isLoading) {
                    deltaY = ev.getY() - startY;

                    Log.d("debug", String.valueOf(deltaY));

                    headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(),
                            -1 * HEADER_HEIGHT + (int) deltaY, 0, headerRelativeLayout.getPaddingBottom());
                    lastUpdateDateTextView.setText(Application.get().getLastUpdate() != 0 ? new Date(Application.get()
                            .getLastUpdate()).toString() : "Never updated??");

                    if (headerRelativeLayout.getPaddingTop() >= HEADER_HEIGHT && currentState == STATE_PULL_TO_REFRESH) {
                        //change state
                        currentState = STATE_RELEASE_TO_UPDATE;
                        headerTextView.setText("release");
                    } else if (headerRelativeLayout.getPaddingTop() < HEADER_HEIGHT && currentState == STATE_RELEASE_TO_UPDATE) {
                        currentState = STATE_PULL_TO_REFRESH;
                        headerTextView.setText("pull");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //isDragging = false;

                if (!isLoading) {
                    if (headerRelativeLayout.getPaddingTop() < HEADER_HEIGHT) {
                        // come back
                        headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), -1 * HEADER_HEIGHT, 0,
                                headerRelativeLayout.getPaddingBottom());
                    } else {
                        // come to HEADER_HEIGHT and start the trigger
                        headerRelativeLayout.setPadding(headerRelativeLayout.getPaddingLeft(), HEADER_TOP, 0,
                                headerRelativeLayout.getPaddingBottom());
                        headerTextView.setText("Loading");

                        //START LOADING
                        isLoading = true;
                        final Bundle b = new Bundle();
                        b.putString(INTENT_TYPE, MANUAL_UPDATE_REQUEST);
                        b.putBoolean(MANUAL, true);
                        ((MainActivity) getActivity()).sendBackgroundRequest(b);
                    }
                }
                break;
            default:
                break;
        }

        return false;
    }

    public void resetRefresh() {
        headerRelativeLayout.setPadding(0, -1 * HEADER_HEIGHT, 0, 0);
        currentState = STATE_PULL_TO_REFRESH;
        headerTextView.setText("pull");
        isLoading = false;
    }

    private class ChatDrawerAdapter extends CursorAdapter {

        public ChatDrawerAdapter(FragmentActivity a, Cursor c) {
            super(a, c, true);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.chats_drawer_row, parent, false);
            buildView(view, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            buildView(view, cursor);
        }

        public void buildView(View view, Cursor cursor) {
            final RelativeLayout row = (RelativeLayout) view.findViewById(R.id.chatRow);
            final long globalId = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
            final Message last = DatabaseHelper.getLatestMessage(globalId);
            row.setBackgroundResource(getColor(last));
            final TextView chatLabel = (TextView) view.findViewById(R.id.chatLabel);
            chatLabel.setText(cursor.getString(cursor.getColumnIndex(Group.F_NAME)));
            final TextView latestMessage = (TextView) view.findViewById(R.id.latestMessage);
            final String latestMessageStr = getText(last);
            if (latestMessageStr == null) {
                latestMessage.setTypeface(null, Typeface.ITALIC);
                latestMessage.setText("Be first to say something dope");
            } else {
                latestMessage.setTypeface(null, Typeface.NORMAL);
                latestMessage.setText(latestMessageStr);
            }

            CacheImageView avi = (CacheImageView) view.findViewById(R.id.groupAvi);
            RecyclingImageView avi2 = (RecyclingImageView) view.findViewById(R.id.groupAvi2);

            if (GeneralHelper.getKachisCachePref()) {
                avi2.setVisibility(View.INVISIBLE);
                avi.setVisibility(View.VISIBLE);
                avi.configure(BASE_URL + cursor.getString(cursor.getColumnIndex(Group.F_IMAGE_URL)), 0, false);
            } else {
                avi.setVisibility(View.INVISIBLE);
                avi2.setVisibility(View.VISIBLE);
                ImageManager.getInstance(getActivity().getSupportFragmentManager()).loadUncirclizedImage(
                        cursor.getString(cursor.getColumnIndex(Group.F_IMAGE_URL)), avi2);
            }
        }

        private String getText(Message m) {
            if (m == null) {
                return null;
            }
            return String.format("%s %s: %s", m.getSenderFirstName(), m.getSenderLastName(),
                    m.getContent().isEmpty() && !m.getAttachments().isEmpty() ? "an image was sent" : m.getContent());
        }

        private int getColor(Message m) {
            if (m == null) {
                return R.color.menu_item;
            }
            if (m.getSenderId() == DatabaseHelper.getAccountUserId()) {
                return R.color.menu_own_item;
            }
            return R.color.menu_item;
        }
    }
}
