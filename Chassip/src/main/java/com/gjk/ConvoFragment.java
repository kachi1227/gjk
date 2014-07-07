package com.gjk;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.gjk.database.objects.GroupMember;
import com.gjk.helper.GeneralHelper;
import com.gjk.service.ChassipService;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.gjk.Constants.CHASSIP_ACTION;
import static com.gjk.Constants.CONVO_ID;
import static com.gjk.Constants.CONVO_TYPE;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_REQUEST;
import static com.gjk.Constants.GROUP_ID;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT;
import static com.gjk.helper.DatabaseHelper.getGroupMember;
import static com.gjk.helper.DatabaseHelper.getGroupMembers;
import static com.gjk.helper.DatabaseHelper.getMessagesCursor;

/**
 * @author gpl
 */
public class ConvoFragment extends ListFragment {

    private String mLogtag;

    private Activity mCtx;

    private Bundle mArgs;

    private Set<GroupMember> mMembers;
    private MessagesAdapter mAdapter;

    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        Bundle useTheseArgs;
        if (mArgs == null && savedInstanceState != null) {
            mArgs = savedInstanceState;
        }
        if (mMembers == null) {
            mMembers = Sets.newHashSet();
        }
        if (getConvoType() == ConvoType.MAIN_CHAT) {
            addMembers(getGroupMembers(getChatId()));
        } else {
            if (mArgs.containsKey("members")) {
                long[] ids = mArgs.getLongArray("members");
                for (long id : ids) {
                    mMembers.add(getGroupMember(id));
                }
            }
        }
        mLogtag = String.format("ConvoFragment id=%d type=%s", getConvoId(), getConvoType());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.message_list, null);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Cursor cursor;
        if (GeneralHelper.getInterleavingPref()) {
            cursor = getMessagesCursor(getChatId(), PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
        } else {
            cursor = getMessagesCursor(getChatId(), getConvoId(), PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
        }
        mAdapter = new MessagesAdapter(getActivity(), cursor, getConvoId(), getConvoType());
        setListAdapter(mAdapter);
        scrollToBottom();
        if (getConvoType() != ConvoType.MAIN_CHAT) {
            Intent i = new Intent(getActivity(), ChassipService.class);
            i.setAction(CHASSIP_ACTION);
            i.putExtra(INTENT_TYPE, FETCH_CONVO_MEMBERS_REQUEST)
                    .putExtra(GROUP_ID, getChatId())
                    .putExtra(CONVO_TYPE, getConvoType().getValue())
                    .putExtra(CONVO_ID, getConvoId());
            ((MainActivity) getActivity()).sendServerRequest(i);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            private Integer mItemCountAtOnScroll = 0;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                synchronized (mItemCountAtOnScroll) {
                    if (mAdapter != null && totalItemCount != 0 && mItemCountAtOnScroll != totalItemCount &&
                            firstVisibleItem == 0 && visibleItemCount != 0 && visibleItemCount < totalItemCount) {
                        mItemCountAtOnScroll = totalItemCount;
                        loadMessagesAndFetchMore();
                        getListView().setSelection(getListView().getCount() - totalItemCount);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(mArgs);
    }

    public long getChatId() {
        return mArgs.getLong("chatId");
    }

    public String getName() {
        return mArgs.getString("name");
    }

    public ConvoType getConvoType() {
        return ConvoType.getFromValue(mArgs.getInt("convoType"));
    }

    public long getConvoId() {
        return mArgs.getLong("convoId");
    }

    public boolean isCanFetchMoreMessagesSet() {
        return mArgs.getBoolean("canFetchMoreMessages");
    }

    public void addMembers(GroupMember... members) {
        if (mMembers == null) {
            mMembers = Sets.newHashSet();
        }
        Long[] newIds = new Long[members.length];
        for (int i = 0; i < members.length; i++) {
            mMembers.add(members[i]);
            newIds[i] = members[i].getGlobalId();
        }
        if (mArgs == null) {
            mArgs = new Bundle();
            mArgs.putLongArray("members", GeneralHelper.convertLong(newIds));
        } else if (!mArgs.containsKey("members")) {
            mArgs.putLongArray("members", GeneralHelper.convertLong(newIds));
        } else if (mArgs.containsKey("members")) {
            Long[] currentIds = GeneralHelper.convertLong(mArgs.getLongArray("members"));
            mArgs.putLongArray("members", GeneralHelper.concatLong(currentIds, newIds));
        }
    }

    public Set<GroupMember> getMembers() {
        return mMembers;
    }

    private void scrollToBottom() {
        if (mCtx != null) {
            getListView().post(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    if (getListView().getCount() > 0) {
                        getListView().setSelection(getListView().getCount() - 1);
                    }
                }
            });
        }
    }

    public void loadMessages(int numMessages) {
        swap(numMessages);
    }

    public void loadMessagesAfterFetch(boolean canFetchMore) {
        mArgs.putBoolean("canFetchMoreMessages", canFetchMore);
        swap(PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
        if (!canFetchMore) {
            GeneralHelper.showLongToast(getActivity(), "No more messages to fetch...");
        }
    }

    public void loadMessagesAndFetchMore() {
        Log.i(mLogtag, "Trying to loading more messages");
        if (isCanFetchMoreMessagesSet()) {
            Cursor oldCursor = swap(PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
            if (mAdapter.getCount() - oldCursor.getCount() != PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT) {
                Intent i = new Intent(getActivity(), ChassipService.class);
                i.setAction(CHASSIP_ACTION);
                i.putExtra(INTENT_TYPE, FETCH_MORE_MESSAGES_REQUEST)
                        .putExtra(GROUP_ID, getChatId())
                        .putExtra(CONVO_ID, getConvoId());
                ((MainActivity) getActivity()).sendServerRequest(i);
            }
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        if (mArgs == null) {
            mArgs = args;
        } else {
            mArgs.putAll(args);
        }
    }

    private Cursor swap(int numMessages) {
        Cursor newCursor;
        if (GeneralHelper.getInterleavingPref()) {
            newCursor = getMessagesCursor(getChatId(), mAdapter.getCount() + numMessages);
        } else {
            newCursor = getMessagesCursor(getChatId(), getConvoId(), mAdapter.getCount() + numMessages);
        }
        Cursor oldCursor = mAdapter.swapCursor(newCursor);
        return oldCursor;
    }
}
