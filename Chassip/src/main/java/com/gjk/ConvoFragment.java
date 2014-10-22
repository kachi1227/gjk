package com.gjk;

import android.app.Activity;
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
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gjk.Constants.CONVO_ID;
import static com.gjk.Constants.CONVO_TYPE;
import static com.gjk.Constants.FETCH_CONVO_MEMBERS_REQUEST;
import static com.gjk.Constants.FETCH_MORE_MESSAGES_REQUEST;
import static com.gjk.Constants.GROUP_ID;
import static com.gjk.Constants.INTENT_TYPE;
import static com.gjk.Constants.PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;
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

    private boolean mViewIsCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = getActivity();
        if (mArgs == null && savedInstanceState != null) {
            mArgs = savedInstanceState;
        }
        if (mMembers == null) {
            mMembers = Sets.newHashSet();
        }
        if (getConvoType() == ConvoType.MAIN_CHAT) {
            addMembers(getGroupMembers(getGroupId()));
        } else {
            if (mArgs.containsKey("members")) {
                final long[] ids = mArgs.getLongArray("members");
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
        mViewIsCreated = true;
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setAdapter();
        scrollToBottom();
        if (getConvoType() != ConvoType.MAIN_CHAT) {
            final Bundle b = new Bundle();
            b.putString(INTENT_TYPE, FETCH_CONVO_MEMBERS_REQUEST);
            b.putLong(GROUP_ID, getGroupId());
            b.putLong(CONVO_ID, getConvoId());
            b.putInt(CONVO_TYPE, getConvoType().getValue());
            ((MainActivity) getActivity()).sendBackgroundRequest(b);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {

                                              private AtomicInteger mItemCountAtOnScroll = new AtomicInteger(0);

                                              @Override
                                              public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                                  if (mAdapter != null && totalItemCount != 0 && mItemCountAtOnScroll.get() != totalItemCount &&
                                                          firstVisibleItem == 0 && visibleItemCount != 0 && visibleItemCount < totalItemCount) {
                                                      mItemCountAtOnScroll.set(totalItemCount);
                                                      loadAndFetchMessages(PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
                                                      getListView().setSelection(getListView().getCount() - totalItemCount);
                                                  }
                                              }

                                              @Override
                                              public void onScrollStateChanged(AbsListView view, int scrollState) {
                                              }
                                          }
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(mArgs);
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

    public long getGroupId() {
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
        for (GroupMember member : members) {
            if (member != null) {
                mMembers.add(member);
            }
        }
        if (mArgs == null) {
            mArgs = new Bundle();
        }
        if (mArgs.containsKey("members")) {
            Long[] currentIds = GeneralHelper.convertLong(mArgs.getLongArray("members"));
            mArgs.putLongArray("members", GeneralHelper.concatLong(currentIds, getMemberIds()));
        } else {
            mArgs.putLongArray("members", GeneralHelper.convertLong(getMemberIds()));
        }
    }

    public Set<GroupMember> getMembers() {
        if (mMembers == null) {
            return Collections.emptySet();
        }
        return new HashSet<GroupMember>(mMembers);
    }

    public Long[] getMemberIds() {
        final List<GroupMember> members = new ArrayList<GroupMember>(mMembers);
        Long[] ids = new Long[members.size()];
        for (int i = 0; i < members.size(); i++) {
            ids[i] = members.get(i).getGlobalId();
        }
        return ids;
    }

    public void removeMembers(long[] ids) {
        if (mMembers != null) {
            List<GroupMember> removeThese = new ArrayList<GroupMember>();
            for (GroupMember gm : mMembers) {
                for (long id : ids) {
                    if (gm.getGlobalId() == id) {
                        removeThese.add(gm);
                    }
                }
            }
            mMembers.removeAll(removeThese);
        }
    }

    public Set<GroupMember> getOtherMembers() {
        if (mMembers == null) {
            return Collections.emptySet();
        }
        final Set<GroupMember> others = Sets.newHashSet(mMembers);
        for (GroupMember gm : mMembers) {
            if (gm.getGlobalId() == getAccountUserId()) {
                others.remove(gm);
            }
        }
        return others;
    }


    public long[] getOtherMemberIds() {
        final long[] ids = new long[getOtherMembers().size()];
        int i = 0;
        for (GroupMember gm : getOtherMembers()) {
            ids[i] = gm.getGlobalId();
            i++;
        }
        return ids;
    }

    private void scrollToBottom() {
        if (mViewIsCreated) {
            getListView().post(new Runnable() {
                @Override
                public void run() {
                    // Select the last row so it will scroll into view...
                    try {
                        if (getListView().getCount() > 0) {
                            getListView().setSelection(getListView().getCount() - 1);
                        }
                    } catch (Exception e) {
                        Log.e("ConvoFragment", "Whoa hoe..", e);
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void loadMessagesAfterFetch(boolean canFetchMore) {
        mArgs.putBoolean("canFetchMoreMessages", canFetchMore);
        swapCursor(PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
    }

    public void loadAndFetchMessages(int numMessages) {
        final Cursor oldCursor = swapCursor(numMessages);
        if (oldCursor == null) {
            return;
        }
        if (isCanFetchMoreMessagesSet()) {
            if (mAdapter.getCount() == oldCursor.getCount()) {
                final Bundle b = new Bundle();
                b.putString(INTENT_TYPE, FETCH_MORE_MESSAGES_REQUEST);
                b.putLong(GROUP_ID, getGroupId());
                b.putLong(CONVO_ID, getConvoId());
                ((MainActivity) getActivity()).sendBackgroundRequest(b);
            }
        } else {
            GeneralHelper.showLongToast(getActivity(), "No more messages to fetch...");
        }
    }

    public void loadMessages(int numMessages) {
        swapCursor(numMessages);
    }

    private Cursor swapCursor(int numMessages) {
        if (mAdapter == null) {
            Log.w(mLogtag, "Tried to swap cursors on a null adapter. Will try to set it now...");
            setAdapter();
        }
        Log.i(mLogtag, "Trying to loading more messages");
        final Cursor newCursor;
        if (GeneralHelper.getInterleavingPref()) {
            newCursor = getMessagesCursor(getGroupId(), mAdapter.getCount() + numMessages);
        } else {
            newCursor = getMessagesCursor(getGroupId(), getConvoId(), mAdapter.getCount() + numMessages);
        }
        return mAdapter.swapCursor(newCursor);
    }

    private void setAdapter() {
        final Cursor cursor;
        if (GeneralHelper.getInterleavingPref()) {
            cursor = getMessagesCursor(getGroupId(), PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
        } else {
            cursor = getMessagesCursor(getGroupId(), getConvoId(), PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT);
        }
        mAdapter = new MessagesAdapter(getActivity(), cursor, getConvoId(), getConvoType());
        setListAdapter(mAdapter);
    }
}
