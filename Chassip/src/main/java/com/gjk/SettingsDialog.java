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

import com.gjk.helper.GeneralHelper;

public class SettingsDialog extends DialogFragment {

    private static final String LOGTAG = "SettingsDialog";

    private Context mCtx;
    private SettingsDialog mMe = this;
    private Bundle mArguments;

    private CheckBox mInterleave;
    private CheckBox mUseKachisCache;
    private CheckBox mShowDebugToasts;
    private CheckBox mCirclizedMemberAvis;

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(SettingsDialog dialog, boolean dontRefresh);

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
        mInterleave.setChecked(GeneralHelper.getInterleavingPref());
        mUseKachisCache = (CheckBox) view.findViewById(R.id.settings_use_kachis_cache);
        mUseKachisCache.setChecked(GeneralHelper.getKachisCachePref());
        mShowDebugToasts = (CheckBox) view.findViewById(R.id.settings_show_debug_toasts);
        mShowDebugToasts.setChecked(GeneralHelper.getShowDebugToastsPref());
        mCirclizedMemberAvis = (CheckBox) view.findViewById(R.id.settings_circlize_member_avis);
        mCirclizedMemberAvis.setChecked(GeneralHelper.getCirclizeMemberAvisPref());
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                        if (GeneralHelper.getKachisCachePref() == mUseKachisCache.isChecked() || GeneralHelper
                                .getCirclizeMemberAvisPref() == mCirclizedMemberAvis.isChecked()) {
//                            Application.get().clearCache();
//                            Application.get().getCacheManager().cleanCache();
//                            ImageManager.getInstance(getActivity().getSupportFragmentManager()).clearCache();
//                            BitmapLoader.clearCache();
                        }
                        boolean dontRefresh = GeneralHelper.getInterleavingPref() == mInterleave.isChecked() &&
                                GeneralHelper.getKachisCachePref() == mUseKachisCache.isChecked() && GeneralHelper
                                .getCirclizeMemberAvisPref() == mCirclizedMemberAvis.isChecked();
                        GeneralHelper.setInterleavingPref(mInterleave.isChecked());
                        GeneralHelper.setKachisCachePref(mUseKachisCache.isChecked());
                        GeneralHelper.setShowDebugToastsPref(mShowDebugToasts.isChecked());
                        GeneralHelper.setCirclizeMemberAvisPref(mCirclizedMemberAvis.isChecked());

                        mListener.onDialogPositiveClick(mMe, dontRefresh);
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