package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.gjk.helper.DatabaseHelper;

public class DeleteChatDialog extends DialogFragment {

    private static final String LOGTAG = "DeleteChatDialog";

    private long mGroupId;

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onDeleteChatDialogPositiveClick(DeleteChatDialog dialog);
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
                        mListener.onDeleteChatDialogPositiveClick(DeleteChatDialog.this);
                        DeleteChatDialog.this.dismiss();
                    }
                });
            }
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(String.format(getResources().getString(R.string.delete_chat_title),
                DatabaseHelper.getGroup(mGroupId).getName())).setMessage(R.string.delete_chat)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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

    public void setGroupId(long groupId) {
        this.mGroupId = groupId;
    }
}