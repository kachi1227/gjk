package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.gjk.database.objects.GroupMember;
import com.gjk.helper.GeneralHelper;
import com.gjk.helper.ViewHelper;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.gjk.helper.DatabaseHelper.getOtherConvoMembersCursor;
import static com.gjk.helper.GeneralHelper.getIdsFromGroupMembers;
import static com.gjk.helper.ViewHelper.resetUserListView;

public class RemoveConvoMembersDialog extends DialogFragment {

    private static final String LOGTAG = "AddChatMembersDialog";

    private long mGroupId;
    private long mConvoId;
    private ConvoType mConvoType;
    private GroupMember[] mConvoMembers;

    private ListView mUsers;

    private Set<Long> mSelectedIds = Sets.newHashSet();

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onRemoveConvoMembersDialogPositiveClick(RemoveConvoMembersDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart(); // super.onStart() is where dialog.show() is actually called on the underlying dialog, so we
        // have to do it after this point
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSelectedIds != null && mSelectedIds.size() > 0) {
                            mListener.onRemoveConvoMembersDialogPositiveClick(RemoveConvoMembersDialog.this);
                            RemoveConvoMembersDialog.this.dismiss();
                        }
                    }
                });
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.remove_convo_members_dialog, null);
        mUsers = (ListView) view.findViewById(R.id.removeConvoMembersListView);
        resetCursor();

        builder.setView(view).setTitle(R.string.remove_members_from_convo_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        }).setCancelable(false);
        Dialog d = builder.create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        setCancelable(false);
        return d;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public long getGroupId() {
        return mGroupId;
    }

    public RemoveConvoMembersDialog setGroupId(long groupId) {
        this.mGroupId = groupId;
        return this;
    }

    public long getConvoId() {
        return mConvoId;
    }

    public RemoveConvoMembersDialog setConvoId(long convoId) {
        mConvoId = convoId;
        return this;
    }

    public ConvoType getConvoType() {
        return mConvoType;
    }

    public RemoveConvoMembersDialog setConvoType(ConvoType convoType) {
        mConvoType = convoType;
        return this;
    }

    public RemoveConvoMembersDialog setConvoMembers(GroupMember[] members) {
        mConvoMembers = members;
        return this;
    }

    public long[] getSelectedIds() {
        return GeneralHelper.convertLong(mSelectedIds.toArray(new Long[mSelectedIds.size()]));
    }

    public void resetCursor() {
        resetUserListView(getActivity(), getOtherConvoMembersCursor(mGroupId, getIdsFromGroupMembers(mConvoMembers)),
                mUsers, new ViewHelper.Checker() {
                    @Override
                    public void checkHasChanged(long id, boolean isChecked) {
                        if (isChecked) {
                            mSelectedIds.add(id);
                        } else {
                            mSelectedIds.remove(id);
                        }
                    }
                });
    }
}