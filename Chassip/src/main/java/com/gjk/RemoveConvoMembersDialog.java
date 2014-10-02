package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.gjk.database.objects.GroupMember;
import com.gjk.helper.GeneralHelper;
import com.google.common.collect.Lists;

import java.util.List;

public class RemoveConvoMembersDialog extends DialogFragment {

    private static final String LOGTAG = "AddChatMembersDialog";

    private long mGroupId;
    private long mConvoId;
    private ConvoType mConvoType;
    private GroupMember[] mConvoMembers;
    private List<Long> mSelectedMembers;

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
                        if (mSelectedMembers != null && mSelectedMembers.size() > 0) {
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

        final String[] array = new String[mConvoMembers.length];
        for (int i = 0; i < mConvoMembers.length; i++) {
            array[i] = mConvoMembers[i].getFullName();
        }
        mSelectedMembers = Lists.newArrayList();
        builder.setTitle(R.string.remove_members_from_convo_title)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(array, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                GroupMember gm = mConvoMembers[which];
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedMembers.add(gm.getGlobalId());
                                } else if (mSelectedMembers.contains(gm.getGlobalId())) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedMembers.remove(gm.getGlobalId());
                                }
                            }
                        })
                        // Set the action buttons
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
        return GeneralHelper.convertLong(mSelectedMembers.toArray(new Long[mSelectedMembers.size()]));
    }
}