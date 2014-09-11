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
import android.widget.CheckBox;

import com.google.common.collect.Lists;

import java.util.List;

public class AddChatMembersDialog extends DialogFragment {

    private static final String LOGTAG = "AddChatMembersDialog";

    private long mGroupId;
    private CheckBox mGreg;
    private CheckBox mGreg2;
    private CheckBox mGreg3;
    private CheckBox mJeff;
    private CheckBox mKach;

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onAddChatMembersDialogPositiveClick(AddChatMembersDialog dialog);
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
                        if (isAddChatMembersReady()) {
                            mListener.onAddChatMembersDialogPositiveClick(AddChatMembersDialog.this);
                            AddChatMembersDialog.this.dismiss();
                        }
                    }
                });
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_chat_members_dialog, null);

        mGreg = (CheckBox) view.findViewById(R.id.greg);
        mGreg2 = (CheckBox) view.findViewById(R.id.greg2);
        mGreg3 = (CheckBox) view.findViewById(R.id.greg3);
        mJeff = (CheckBox) view.findViewById(R.id.jeff);
        mKach = (CheckBox) view.findViewById(R.id.kach);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view).setTitle(R.string.add_members_to_chat_title)
                // Add action buttons
                .setPositiveButton(R.string.add_members_to_chat, new DialogInterface.OnClickListener() {
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

    public AddChatMembersDialog setGroupId(long groupId) {
        mGroupId = groupId;
        return this;
    }

    public long[] getSelectedIds() {
        List<Long> ids = Lists.newArrayList();
        if (mGreg.isChecked()) {
            ids.add(3l);
        }
        if (mGreg2.isChecked()) {
            ids.add(9l);
        }
        if (mGreg3.isChecked()) {
            ids.add(23l);
        }
        if (mJeff.isChecked()) {
            ids.add(6l);
        }
        if (mKach.isChecked()) {
            ids.add(8l);
        }
        long[] primitveIds = new long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            primitveIds[i] = ids.get(i);
        }
        return primitveIds;
    }

    private boolean isAddChatMembersReady() {
        return (mGreg.isChecked() || mGreg2.isChecked() || mGreg3.isChecked() || mJeff.isChecked() || mKach.isChecked
                ());
    }
}