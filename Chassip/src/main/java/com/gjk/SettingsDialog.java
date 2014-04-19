package com.gjk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

public class SettingsDialog extends DialogFragment {

    private static final String LOGTAG = "SettingsDialog";

    private Context mCtx;
    private SettingsDialog mMe = this;
    private Bundle mArguments;

    private CheckBox mInterleave;

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(SettingsDialog dialog);

        public void onDialogNegativeClick(SettingsDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    public void setListener(NoticeDialogListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mCtx = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settiings, null);

        mInterleave = (CheckBox) view.findViewById(R.id.settings_interleaving);
        mInterleave.setChecked(Application.get().getPreferences().getBoolean(Constants.PROPERTY_SETTING_INTERLEAVING,
                Constants.PROPERTY_SETTING_INTERLEAVING_DEFAULT));

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                        Application.get().getPreferences().edit().putBoolean(Constants.PROPERTY_SETTING_INTERLEAVING,
                                mInterleave.isChecked()).commit();
                        mListener.onDialogPositiveClick(mMe);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                getDialog().cancel();
                mListener.onDialogNegativeClick(mMe);
            }
        }).setCancelable(false).setTitle(R.string.settings_title);
        Dialog d = builder.create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        setCancelable(false);
        return d;
    }

    public Bundle getMyArguments() {
        return mArguments;
    }

    @Override
    public void setArguments(Bundle mArguments) {
        this.mArguments = mArguments;
    }
}