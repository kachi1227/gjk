package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gjk.helper.GeneralHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SettingsDialog extends DialogFragment {

    private static final String LOGTAG = "SettingsDialog";

    private Context mCtx;
    private SettingsDialog mMe = this;
    private Bundle mArguments;

    private CheckBox mInterleave;
    private CheckBox mUseKachisCache;
    private CheckBox mShowDebugToasts;
    private CheckBox mCirclizedMemberAvis;
    private Button mShowLog;
    private Button mClearLog;
    private TextView mVersionText;

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onSettingsDialogPositiveClick(SettingsDialog dialog, boolean dontRefresh);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mCtx = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settiings_dialog, null);

        mVersionText = (TextView) view.findViewById(R.id.settings_version_text);
        mVersionText.setText("v." + Application.get().getAppVersionName());
        mInterleave = (CheckBox) view.findViewById(R.id.settings_interleaving);
        mInterleave.setChecked(GeneralHelper.getInterleavingPref());
        mUseKachisCache = (CheckBox) view.findViewById(R.id.settings_use_kachis_cache);
        mUseKachisCache.setChecked(GeneralHelper.getKachisCachePref());
        mShowDebugToasts = (CheckBox) view.findViewById(R.id.settings_show_debug_toasts);
        mShowDebugToasts.setChecked(GeneralHelper.getShowDebugToastsPref());
        mCirclizedMemberAvis = (CheckBox) view.findViewById(R.id.settings_circlize_member_avis);
        mCirclizedMemberAvis.setChecked(GeneralHelper.getCirclizeMemberAvisPref());
        mShowLog = (Button) view.findViewById(R.id.settings_show_log);
        mShowLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(Application.get().getLogFile().getPath());
                    final FileReader fileReader = new FileReader(Application.get().getLogFile());
                    final BufferedReader bufferedReader = new BufferedReader(fileReader);
                    final StringBuilder stringBuffer = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                        stringBuffer.append("\n");
                    }
                    fileReader.close();
                    builder.setMessage(stringBuffer.toString());
                    AlertDialog dialog = builder.show();
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    textView.setTextSize(8);
//                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                    lp.copyFrom(dialog.getWindow().getAttributes());
//                    lp.width = 2500;
//                    dialog.getWindow().setAttributes(lp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mClearLog = (Button) view.findViewById(R.id.settings_clear_log);
        mClearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Application.get().clearLog();
            }
        });
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

                        mListener.onSettingsDialogPositiveClick(mMe, dontRefresh);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
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