package com.gjk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gjk.database.objects.GroupMember;
import com.gjk.helper.DatabaseHelper;
import com.gjk.helper.GeneralHelper;
import com.gjk.net.CreateSideChatTask;
import com.gjk.net.CreateWhisperTask;
import com.gjk.net.GetSpecificGroupTask;
import com.gjk.net.HTTPTask.HTTPTaskListener;
import com.gjk.net.NotifySideChatInviteesTask;
import com.gjk.net.NotifyWhisperInviteesTask;
import com.gjk.net.TaskResult;
import com.gjk.utils.media2.ImageManager;
import com.gjk.views.CacheImageView;
import com.gjk.views.RecyclingImageView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.gjk.helper.DatabaseHelper.addGroup;
import static com.gjk.helper.DatabaseHelper.getAccountUserId;

/**
 * @author gpl
 */
public class ThreadsDrawerFragment extends ListFragment {

    private static final String LOGTAG = "ThreadsDrawerFragment";

    private Context mCtx;

    private View mView;
    private Button mCreateSideConvo;
    private Button mCreateWhisper;
    private ThreadAdapter mAdapter;
    private AlertDialog mDialog;
    private List<Long> mSelectedMembers;

    // TODO: Temporary!!
    private final static HashMap<String, Long> mapping = Maps.newHashMap();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapping.put("Greg", 3L);
        mapping.put("Greg2", 9L);
        mapping.put("Jeff", 8L);
        mapping.put("Kachi", 6L);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCtx = getActivity();
        mAdapter = new ThreadAdapter(mCtx);
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.threads_drawer_list, null);
        mCreateSideConvo = (Button) mView.findViewById(R.id.createSideConvo);
        mCreateSideConvo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMembers = new ArrayList<Long>(); // Where we track the selected items
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle(R.string.add_members_to_sideconvo_title)
                        // Specify the list array, the items to be selected by default (null for none),
                        // and the listener through which to receive callbacks when items are selected
                        .setMultiChoiceItems(R.array.contacts, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                String name = getResources().getStringArray(R.array.contacts)[which];
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedMembers.add(mapping.get(name));
                                } else if (mSelectedMembers.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedMembers.remove(mapping.get(name));
                                }
                            }
                        })
                                // Set the action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the mSelectedItems results somewhere
                                // or return them to the component that opened the dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                // Get the layout inflater
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View view = inflater.inflate(R.layout.create_sideconvo_dialog, null);
                                // Add the buttons
                                builder.setView(view);
                                final EditText chatName = (EditText) view.findViewById(R.id.sideConvoName);
                                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String name = chatName.getText().toString();
                                        Log.d(LOGTAG, "Yes was clicked");
                                        if (!name.isEmpty()) {
                                            Log.d(LOGTAG, "Creating side-convo with name " + name);
                                            createSideConvo(name);
                                        }
                                    }
                                });
                                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.setTitle(R.string.create_sideconvo_title);
                                // Create the AlertDialog
                                mDialog = builder.create();
                                mDialog.setCanceledOnTouchOutside(true);
                                mDialog.show();
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
            }
        });
        mCreateWhisper = (Button) mView.findViewById(R.id.createWhisper);
        mCreateWhisper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedMembers = new ArrayList<Long>(); // Where we track the selected items
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the dialog title
                builder.setTitle(R.string.add_members_to_whisper_title)
                        // Specify the list array, the items to be selected by default (null for none),
                        // and the listener through which to receive callbacks when items are selected
                        .setMultiChoiceItems(R.array.contacts, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                String name = getResources().getStringArray(R.array.contacts)[which];
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedMembers.add(mapping.get(name));
                                } else if (mSelectedMembers.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedMembers.remove(mapping.get(name));
                                }
                            }
                        })
                                // Set the action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the mSelectedItems results somewhere
                                // or return them to the component that opened the dialog
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                // Get the layout inflater
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View view = inflater.inflate(R.layout.create_whisper_dialog, null);
                                // Add the buttons
                                builder.setView(view);
                                final EditText chatName = (EditText) view.findViewById(R.id.whisperName);
                                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String name = chatName.getText().toString();
                                        Log.d(LOGTAG, "Yes was clicked");
                                        if (!name.isEmpty()) {
                                            Log.d(LOGTAG, "Creating whisper with name " + name);
                                            createWhisper(name);
                                        }
                                    }
                                });
                                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.setTitle(R.string.create_whisper_title);
                                // Create the AlertDialog
                                mDialog = builder.create();
                                mDialog.setCanceledOnTouchOutside(true);
                                mDialog.show();
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
            }
        });
        return mView;
    }

    private void createSideConvo(String name) {
        final long[] members = new long[mSelectedMembers.size()];
        for (int i = 0; i < mSelectedMembers.size(); i++) {
            members[i] = mSelectedMembers.get(i);
        }
        new CreateSideChatTask(getActivity(), new HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    try {
                        JSONObject response = (JSONObject) result.getExtraInfo();
                        fetchGroup();
                        notifyNewSideConvoMembers(response.getLong("id"));
                    } catch (Exception e) {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
                    }
                } else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
            }
        }, ChatsDrawerFragment.getCurrentChat().getGlobalId(), getAccountUserId(), members, name);
    }

    private void notifyNewSideConvoMembers(long id) {
        Set<GroupMember> ms = mAdapter.getItem(0).getMembers();
        long[] members = new long[ms.size()];
        int index = 0;
        for (GroupMember m : ms) {
            members[index++] = m.getGlobalId();
        }
        new NotifySideChatInviteesTask(getActivity(), new HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified side convo invitees");
                } else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
            }
        }, DatabaseHelper.getAccountUserId(), id, members);
    }

    private void createWhisper(String name) {
        final long[] members = new long[mSelectedMembers.size()];
        for (int i = 0; i < mSelectedMembers.size(); i++) {
            members[i] = mSelectedMembers.get(i);
        }
        new CreateWhisperTask(getActivity(), new HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    try {
                        JSONObject response = (JSONObject) result.getExtraInfo();
                        fetchGroup();
                        notifyNewWhisperMembers(response.getLong("id"));
                    } catch (Exception e) {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
                    }
                } else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
            }
        }, ChatsDrawerFragment.getCurrentChat().getGlobalId(), getAccountUserId(), members, name);
    }

    private void notifyNewWhisperMembers(long id) {
        Set<GroupMember> ms = mAdapter.getItem(0).getMembers();
        long[] members = new long[ms.size()];
        int index = 0;
        for (GroupMember m : ms) {
            members[index++] = m.getGlobalId();
        }
        new NotifyWhisperInviteesTask(getActivity(), new HTTPTaskListener() {
            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    Log.i(LOGTAG, "Notified side convo invitees");
                } else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
            }
        }, DatabaseHelper.getAccountUserId(), id, members);
    }

    private void fetchGroup() {
        new GetSpecificGroupTask(getActivity(), new HTTPTaskListener() {

            @Override
            public void onTaskComplete(TaskResult result) {
                if (result.getResponseCode() == 1) {
                    JSONObject response = (JSONObject) result.getExtraInfo();
                    try {
                        addGroup(response);
                    } catch (Exception e) {
//                        handleCreateChatError(e);
                    }
                } else {
                    GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                }
            }
        }, getAccountUserId(), ChatsDrawerFragment.getCurrentChat().getGlobalId());
    }

    public void addThread(ThreadFragment frag) {
        mAdapter.add(frag);
    }

    public void removeAllThreads() {
        mAdapter.clear();
    }

    public void removeThread(ThreadFragment frag) {
        mAdapter.remove(frag);
    }

    public void updateView() {
        mAdapter.notifyDataSetChanged();
    }

    public class ThreadAdapter extends ArrayAdapter<ThreadFragment> {

        public ThreadAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.threads_drawer_row, null);
            }

            TextView threadLabel = (TextView) convertView.findViewById(R.id.threadLabel);
            threadLabel.setText(getItem(position).getName());
            LinearLayout gallery = (LinearLayout) convertView.findViewById(R.id.gallery);
            gallery.removeAllViews();
            for (GroupMember gm : Lists.newArrayList(getItem(position).getMembers())) {
                gallery.addView(insertPhoto(gm));
            }

            return convertView;
        }

        private View insertPhoto(GroupMember gm) {
            if (GeneralHelper.getKachisCachePref()) {
                CacheImageView imageView = new CacheImageView(getActivity().getApplicationContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10, 10, 10, 10);
                imageView.configure(Constants.BASE_URL + gm.getImageUrl(), 0, false);
                return imageView;
            }
            RecyclingImageView imageView = new RecyclingImageView(getActivity().getApplicationContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(10, 10, 10, 10);
            ImageManager.getInstance(getActivity().getSupportFragmentManager()).loadUncirclizedImage(gm.getImageUrl(),
                    imageView);
            return imageView;
        }
    }
}
