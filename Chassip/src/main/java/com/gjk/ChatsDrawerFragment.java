package com.gjk;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gjk.database.objects.Group;
import com.gjk.database.objects.Message;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.utils.media2.ImageManager;
import com.gjk.views.CacheImageView;
import com.gjk.views.RecyclingImageView;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.gjk.Constants.BASE_URL;
import static com.gjk.Constants.CHAT_CONTEXT_MENU_ID;
import static com.gjk.Constants.CHAT_DRAWER_ADD_MEMBERS;
import static com.gjk.Constants.CHAT_DRAWER_DELETE_CHAT;
import static com.gjk.Constants.CHAT_DRAWER_REMOVE_MEMBERS;

/**
 * @author gpl
 */
public class ChatsDrawerFragment extends Fragment {

    private ChatDrawerAdapter mAdapter;
    private ListView mChatsList;
    private Button mCreateChat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chats_drawer, null);
        mChatsList = (ListView) view.findViewById(R.id.chats_list);
        mCreateChat = (Button) view.findViewById(R.id.createChat);
        mCreateChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CreateChatDialog().show(getActivity().getSupportFragmentManager(), "CreateChatDialog");
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final MainActivity activity = (MainActivity) getActivity();
        mAdapter = new ChatDrawerAdapter(activity, DatabaseHelper.getGroupsCursor());
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
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = (Cursor) mAdapter.getItem(info.position);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Group.F_NAME)));
        menu.add(CHAT_CONTEXT_MENU_ID, v.getId(), 0, CHAT_DRAWER_ADD_MEMBERS);//groupId, itemId, order, title
        menu.add(CHAT_CONTEXT_MENU_ID, v.getId(), 1, CHAT_DRAWER_REMOVE_MEMBERS);
        menu.add(CHAT_CONTEXT_MENU_ID, v.getId(), 2, CHAT_DRAWER_DELETE_CHAT);
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

    private class ChatDrawerAdapter extends CursorAdapter {

        private Map<Long, Boolean> mNotifyMap;

        public ChatDrawerAdapter(FragmentActivity a, Cursor c) {
            super(a, c, true);
            mNotifyMap = Maps.newHashMap();
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
            RelativeLayout row = (RelativeLayout) view.findViewById(R.id.chatRow);
            long globalId = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
            Message last = DatabaseHelper.getLatestMessage(globalId);
            row.setBackgroundResource(getColor(last));
            TextView chatLabel = (TextView) view.findViewById(R.id.chatLabel);
            chatLabel.setText(cursor.getString(cursor.getColumnIndex(Group.F_NAME)));
            TextView latestMessage = (TextView) view.findViewById(R.id.latestMessage);
            String latestMessageStr = getText(last);
            latestMessage.setText(latestMessageStr);
            if (mNotifyMap.containsKey(globalId) && mNotifyMap.get(globalId)) {
                latestMessage.setTypeface(null, Typeface.BOLD);
            } else if (latestMessageStr.isEmpty()) {
                latestMessage.setTypeface(null, Typeface.ITALIC);
                latestMessage.setText("Be first to say something dope");
            } else {
                latestMessage.setTypeface(null, Typeface.NORMAL);
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
                return "";
            }
            return String.format("%s %s: %s", m.getSenderFirstName(), m.getSenderLastName(),
                    m.getContent());
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
