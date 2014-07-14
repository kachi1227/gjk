package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.gjk.helper.GeneralHelper;

public class RegisterDialog extends DialogFragment {

    private static final String LOGTAG = "RegisterDialog";

    private EditText mFirstNameReg;
    private EditText mLastNameReg;
    private EditText mEmailReg;
    private EditText mPasswordReg;
    private EditText mRePasswordReg;
    private LoginButton mFacebookLoginButton;
    private UiLifecycleHelper mUiHelper;
    private GraphUser mFacebookUser;

    /*
     * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
     * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
     */
    public interface NoticeDialogListener {
        public void onRegisterDialogPositiveClick(RegisterDialog dialog);

        public void onRegisterDialogFacebookClick(RegisterDialog dialog);

        public void onRegisterDialogNegativeClick(RegisterDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.register_dialog, null);

        mFirstNameReg = (EditText) view.findViewById(R.id.firstNameReg);
        mFirstNameReg.requestFocus();
        mFirstNameReg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                            .SOFT_INPUT_STATE_VISIBLE);
                }
            }
        });
        mLastNameReg = (EditText) view.findViewById(R.id.lastNameReg);
        mEmailReg = (EditText) view.findViewById(R.id.emailReg);
        mPasswordReg = (EditText) view.findViewById(R.id.passwordReg);
        mRePasswordReg = (EditText) view.findViewById(R.id.rePasswordReg);
        mFacebookLoginButton = (LoginButton) view.findViewById(R.id.facebookLoginButton);
        mFacebookLoginButton.setReadPermissions("email", "public_profile");
        mFacebookLoginButton.setApplicationId(getActivity().getResources().getString(R.string.app_id));
        mFacebookLoginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if (user != null) {
                    mFacebookUser = user;
                    mListener.onRegisterDialogFacebookClick(RegisterDialog.this);
                }
            }
        });
        mUiHelper = new UiLifecycleHelper(getActivity(), null);
        mUiHelper.onCreate(savedInstanceState);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).setNegativeButton(R.string.go_to_login, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onRegisterDialogNegativeClick(RegisterDialog.this);
            }
        }).setCancelable(false);
        Dialog d = builder.create();
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        setCancelable(false);
        return d;
    }

    @Override
    public void onStart() {
        super.onStart(); // super.onStart() is where dialog.show() is actually called on the underlying dialog, so we
        // have to do it after this point
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRegistrationReady()) {
                            mListener.onRegisterDialogPositiveClick(RegisterDialog.this);
                            mPasswordReg.setText("");
                            mRePasswordReg.setText("");
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mUiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }

    public void handleOnActivityRequest(int requestCode, int resultCode, Intent data) {
        mUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    public String getFirstName() {
        return mFirstNameReg.getText().toString();
    }

    public String getLastName() {
        return mLastNameReg.getText().toString();
    }

    public String getEmail() {
        return mEmailReg.getText().toString();
    }

    public String getPassword() {
        return mPasswordReg.getText().toString();
    }

    public GraphUser getFacebookUser() {
        return mFacebookUser;
    }

    private boolean isRegistrationReady() {

        Context ctx = getActivity();

        boolean ready = true;
        if (getFirstName().isEmpty()) {
            GeneralHelper.showLongToast(getActivity(), "First Name was left empty!");
            ready = false;
        }
        if (getLastName().isEmpty()) {
            GeneralHelper.showLongToast(ctx, "Last Name was left empty!");
            ready = false;
        }
        if (getEmail().isEmpty()) {
            GeneralHelper.showLongToast(ctx, "Email was left empty!");
            ready = false;
        }
        if (getPassword().isEmpty()) {
            GeneralHelper.showLongToast(ctx, "Password was left empty!");
            ready = false;
        }

        String rePassword = mRePasswordReg.getText().toString();
        if (rePassword.isEmpty()) {
            GeneralHelper.showLongToast(ctx, "Re-enter Password was left empty!");
            ready = false;
        }
        if (!rePassword.isEmpty() && !getPassword().equals(rePassword)) {
            GeneralHelper.showLongToast(ctx, "Passwords don't match!");
            mRePasswordReg.setText("");
            ready = false;
        }
        return ready;
    }
}