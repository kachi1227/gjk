package com.gjk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gjk.helper.GeneralHelper;
import com.gjk.net.HTTPTask.HTTPTaskListener;
import com.gjk.net.LoginTask;
import com.gjk.net.TaskResult;
import com.gjk.net.UpdateGCMRegTask;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import static com.gjk.Constants.PROPERTY_APP_VERSION;
import static com.gjk.Constants.PROPERTY_REG_ID;
import static com.gjk.Constants.SENDER_ID;

public class LoginDialog extends DialogFragment {

	private static final String LOGTAG = "LoginDialog";

	private Context mCtx;
	private LoginDialog mMe = this;
	private Bundle mArguments;

	private EditText mEmailLogin;
	private EditText mPasswordLogin;

	/*
	 * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
	 * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
	 */
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(LoginDialog dialog);

		public void onDialogNegativeClick(LoginDialog dialog);
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
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		mCtx = getActivity();

		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.login_layout, null);

		mEmailLogin = (EditText) view.findViewById(R.id.emailLogin);
		mPasswordLogin = (EditText) view.findViewById(R.id.passwordLogin);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view)
		// Add action buttons
				.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				}).setNegativeButton(R.string.go_to_register, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						getDialog().cancel();
						mListener.onDialogNegativeClick(mMe);
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
			Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					registerGcmInBackground();
				}
			});
		}
	}

	private void registerGcmInBackground() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					if (getRegistrationId().isEmpty()) {
						String regid = GoogleCloudMessaging.getInstance(mCtx).register(SENDER_ID);
						storeRegistrationId(regid);
					}
					login();
				} catch (Exception e) {
					Log.e(LOGTAG, String.format("GCM egistration unsuccessful: %s", e));
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return null;
			}
		}.execute();
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId() {

		final SharedPreferences prefs = Application.get().getPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(LOGTAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = Application.get().getAppVersion();
		if (registeredVersion != currentVersion) {
			Log.i(LOGTAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 *
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(String regId) {
		final SharedPreferences prefs = Application.get().getPreferences();
		final int appVersion = Application.get().getAppVersion();
		Log.i(LOGTAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private void login() {

		String email = mEmailLogin.getText().toString();
		String password = mPasswordLogin.getText().toString();

		if (!isLoginReady()) {
			return;
		}

		try {
			new LoginTask(mCtx, new HTTPTaskListener() {
				@Override
				public void onTaskComplete(TaskResult result) {
					if (result.getResponseCode() == 1) {
						JSONObject response = (JSONObject) result.getExtraInfo();
						try {
							Application.get().getPreferences().edit().putString(Constants.JSON, response.toString())
									.commit();
							Bundle b = new Bundle();
							b.putString(Constants.JSON, response.toString());
							setArguments(b);
							updateChassipGcm(response.getLong("id"),
									Application.get().getPreferences().getString(PROPERTY_REG_ID, "abc"));
						} catch (Exception e) {
                            GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
                            mPasswordLogin.setText("");
						}
					} else {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
                        mPasswordLogin.setText("");
					}
				}
			}, email, password);
		} catch (Exception e) {
            GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
            mPasswordLogin.setText("");
		}
	}

	private boolean isLoginReady() {

		String email = mEmailLogin.getText().toString();
		String password = mPasswordLogin.getText().toString();

		boolean ready = true;
		if (email.isEmpty()) {
			GeneralHelper.showLongToast(mCtx, "Email was left empty!");
			ready = false;
		}
		if (password.isEmpty()) {
            GeneralHelper.showLongToast(mCtx, "Password was left empty!");
			ready = false;
		}
		return ready;
	}

	private void updateChassipGcm(long id, String gcm) {
		try {
			new UpdateGCMRegTask(mCtx, new HTTPTaskListener() {
				@Override
				public void onTaskComplete(TaskResult result) {
					if (result.getResponseCode() == 1) {
						mListener.onDialogPositiveClick(mMe);
						dismiss();
					} else {
                        GeneralHelper.reportMessage(mCtx, LOGTAG, result.getMessage());
					}
				}
			}, id, gcm, "ANDROID");
		} catch (Exception e) {
            GeneralHelper.reportMessage(mCtx, LOGTAG, e.getMessage());
		}
	}

	public Bundle getMyArguments() {
		return mArguments;
	}

	@Override
	public void setArguments(Bundle mArguments) {
		this.mArguments = mArguments;
	}
}