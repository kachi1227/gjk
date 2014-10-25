package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gjk.database.objects.User;
import com.gjk.views.CacheImageView;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import static com.gjk.helper.DatabaseHelper.getUsersCursor;

public class CreateChatDialog extends DialogFragment {

    private static final String LOGTAG = "CreateChatDialog";

    private UsersAdapter mAdapter;
    private EditText mChatName;
    private ListView mUsers;

    private Set<Long> mSelectedIds = Sets.newHashSet();

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onCreateChatDialogPositiveClick(CreateChatDialog dialog);
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
                        if (isCreateChatReady()) {
                            mListener.onCreateChatDialogPositiveClick(CreateChatDialog.this);
                            CreateChatDialog.this.dismiss();
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
        View view = inflater.inflate(R.layout.create_chat_dialog, null);

        mChatName = (EditText) view.findViewById(R.id.chatName);
        mUsers = (ListView) view.findViewById(R.id.users);
        resetCursor();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view).setTitle(R.string.create_chat_title)
                // Add action buttons
                .setPositiveButton(R.string.select_avi_group, new DialogInterface.OnClickListener() {
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

    public long[] getSelectedIds() {
        List<Long> ids = Lists.newArrayList();
        long[] primitveIds = new long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            primitveIds[i] = ids.get(i);
        }
        return primitveIds;
    }

    public String getChatName() {
        return mChatName.getText().toString();
    }

    public void resetCursor() {
        mAdapter = new UsersAdapter(getActivity(), getUsersCursor());
        mUsers.setAdapter(mAdapter);
    }

    private boolean isCreateChatReady() {
        return !mSelectedIds.isEmpty() || !mChatName.getText().toString().isEmpty();
    }

    private class UsersAdapter extends CursorAdapter {

        public UsersAdapter(FragmentActivity a, Cursor cursor) {
            super(a, cursor, true);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.user_row, parent, false);
            buildView(view, cursor);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            buildView(view, cursor);
        }

        private void buildView(View view, final Cursor cursor) {

            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final long id = cursor.getLong(cursor.getColumnIndex(User.F_GLOBAL_ID));
                    if (isChecked) {
                        mSelectedIds.add(id);
                    } else {
                        mSelectedIds.remove(id);
                    }
                }
            });
            final CacheImageView memberAvi = (CacheImageView) view.findViewById(R.id.memberAvi);
            final TextView userName = (TextView) view.findViewById(R.id.userName);

            memberAvi.configure(Constants.BASE_URL + cursor.getString(cursor.getColumnIndex(User.F_IMAGE_URL)), 0,
                    true);
            userName.setText(cursor.getString(cursor.getColumnIndex(User.F_FIRST_NAME)) + " " + cursor.getString
                    (cursor.getColumnIndex(User.F_LAST_NAME)));
        }
    }
}