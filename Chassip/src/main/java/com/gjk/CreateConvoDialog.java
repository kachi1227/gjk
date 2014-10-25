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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.google.common.collect.Sets;

import java.util.Set;

import static com.gjk.helper.DatabaseHelper.getOtherGroupMembersCursor;
import static com.gjk.helper.GeneralHelper.convertLong;
import static com.gjk.helper.ViewHelper.Checker;
import static com.gjk.helper.ViewHelper.resetUserListView;

public class CreateConvoDialog extends DialogFragment {

    private static final String LOGTAG = "CreateConvoDialog";

    private ListView mUsers;
    private RadioButton mSideConvo;
    private RadioButton mWhisper;
    private EditText mConvoName;

    private long mGroupId;

    private Set<Long> mSelectedIds = Sets.newHashSet();

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onCreateConvoDialogPositiveClick(CreateConvoDialog dialog);
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
                        if (isCreateConvoReady()) {
                            mListener.onCreateConvoDialogPositiveClick(CreateConvoDialog.this);
                            CreateConvoDialog.this.dismiss();
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
        View view = inflater.inflate(R.layout.create_convo_dialog, null);

        mSideConvo = (RadioButton) view.findViewById(R.id.sideConvo);
        mSideConvo.setChecked(true);
        mSideConvo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWhisper.setChecked(!mSideConvo.isChecked());
            }
        });
        mWhisper = (RadioButton) view.findViewById(R.id.whisper);
        mWhisper.setChecked(false);
        mWhisper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSideConvo.setChecked(!mWhisper.isChecked());
            }
        });
        mConvoName = (EditText) view.findViewById(R.id.convoName);
        mUsers = (ListView) view.findViewById(R.id.createConvoChatMembersListView);
        resetCursor();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view).setTitle(R.string.create_convo_title)
                // Add action buttons
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
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

    public CreateConvoDialog setGroupId(long groupId) {
        this.mGroupId = groupId;
        return this;
    }

    public ConvoType getConvoType() {
        if (mSideConvo.isChecked()) {
            return ConvoType.SIDE_CONVO;
        }
        return ConvoType.WHISPER;
    }

    public long[] getSelectedIds() {
        return convertLong(mSelectedIds.toArray(new Long[mSelectedIds.size()]));
    }

    public String getConvoName() {
        return mConvoName.getText().toString();
    }

    private boolean isCreateConvoReady() {
        return !mSelectedIds.isEmpty() || !mConvoName.getText().toString().isEmpty();
    }

    public void resetCursor() {
        resetUserListView(getActivity(), getOtherGroupMembersCursor(mGroupId), mUsers, new Checker() {
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