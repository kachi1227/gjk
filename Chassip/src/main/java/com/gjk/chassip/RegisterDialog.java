package com.gjk.chassip;

import static com.gjk.chassip.Constants.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.gjk.chassip.net.RegisterTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.UpdateGCMRegTask;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.collect.Maps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterDialog extends DialogFragment {

	private final String LOGTAG = getClass().getSimpleName();

	private Context mCtx;
	private RegisterDialog mMe = this;
	private Bundle mArguments;

	private EditText mFirstNameReg;
	private EditText mLastNameReg;
	private EditText mEmailReg;
	private EditText mPasswordReg;
	private EditText mRePasswordReg;
	private Button mSelectAvi;
	private String mAviPath;

	/*
	 * The activity that creates an instance of this dialog fragment must implement this interface in order to receive
	 * event callbacks. Each method passes the DialogFragment in case the host needs to query it.
	 */
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(RegisterDialog dialog);

		public void onDialogNegativeClick(RegisterDialog dialog);
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
		View view = inflater.inflate(R.layout.register_layout2, null);

		mFirstNameReg = (EditText) view.findViewById(R.id.firstNameReg);
		mLastNameReg = (EditText) view.findViewById(R.id.lastNameReg);
		mEmailReg = (EditText) view.findViewById(R.id.emailReg);
		mPasswordReg = (EditText) view.findViewById(R.id.passwordReg);
		mRePasswordReg = (EditText) view.findViewById(R.id.rePasswordReg);
		mSelectAvi = (Button) view.findViewById(R.id.selectAvi);
		mSelectAvi.setOnClickListener(new OnClickListener() {
			private AlertDialog dialog;

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
				// Add the buttons
				builder.setPositiveButton(R.string.camera, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendCameraIntent();
					}
				});
				builder.setNeutralButton(R.string.gallery, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (android.os.Build.VERSION.SDK_INT >= 19) {
							sendGalleryIntent();
						} else {
							sendGalleryIntentPreKitKat();
						}
					}
				}).setCancelable(false);
				builder.setMessage(R.string.select_avi_message).setTitle(R.string.select_avi_title);
				dialog = builder.create();
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
			}
		});

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST) {
				try {
					if (requestCode == GALLERY_REQUEST) {
						// grab path from gallery
						Uri selectedImageUri = data.getData();
						mAviPath = getPath(selectedImageUri);
					} else if (requestCode == CAMERA_REQUEST) {
						// add pic to gallery
						Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
						File f = new File(mAviPath);
						Uri contentUri = Uri.fromFile(f);
						mediaScanIntent.setData(contentUri);
						getActivity().sendBroadcast(mediaScanIntent);
					}
					showLongToast("Image path: " + mAviPath);
				} catch (Exception e) {
					handleAviError(e);
				}
			}
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
					register();
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

	private void sendGalleryIntentPreKitKat() {

		Log.d(LOGTAG, "Running pre-Kit-Kat");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
	}

	@TargetApi(19)
	private void sendGalleryIntent() {

		Log.d(LOGTAG, "Running Kit-Kat or higher!");
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
	}

	private void sendCameraIntent() {

		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				// Create an image file name
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
				String imageFileName = String.format("%s_%s.jpg", getResources().getString(R.string.app_name),
						timeStamp);
				File storageDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(
						R.string.app_name));
				storageDir.mkdirs();
				photoFile = new File(storageDir, imageFileName);
				photoFile.createNewFile();

				// Save a file: path for use with ACTION_VIEW intents
				mAviPath = photoFile.getAbsolutePath();
			} catch (IOException ex) {
				// Error occurred while creating the File
				showLongToast("Saving temp image file failed, the fuck!?");
			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, CAMERA_REQUEST);
			}
		}
	}

	/**
	 * helper to retrieve the path of an image URI
	 */
	private String getPath(Uri uri) {

		String result;
		Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file
								// path
			result = uri.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			result = cursor.getString(idx);
			cursor.close();
		}
		return result;
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
	 * @param context
	 *            application's context.
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

	private void register() {

		String firstName = mFirstNameReg.getText().toString();
		String lastName = mLastNameReg.getText().toString();
		String email = mEmailReg.getText().toString();
		String password = mPasswordReg.getText().toString();

		if (!isRegistrationReady()) {
			return;
		}

		try {
			HashMap<String, Object> fieldMapping = Maps.newHashMap();
			fieldMapping.put("image", new File(mAviPath));
			new RegisterTask(getActivity(), new HTTPTaskListener() {

				@Override
				public void onTaskComplete(TaskResult result) {

					if (result.getResponseCode() == 1) {
						JSONObject response = (JSONObject) result.getExtraInfo();
						try {
							Application.get().getPreferences().edit().putString(JSON, response.toString()).commit();
							Bundle b = new Bundle();
							b.putString(Constants.JSON, response.toString());
							setArguments(b);
							updateChassipGcm(response.getLong("id"),
									Application.get().getPreferences().getString(PROPERTY_REG_ID, "abc"));
						} catch (JSONException e) {
							handleRegistrationError(e);
						}
					} else {
						handleRegistrationFail(result);
					}
				}
			}, firstName, lastName, email, password, fieldMapping);
		} catch (Exception e) {
			handleRegistrationError(e);
		}
	}

	private boolean isRegistrationReady() {

		String firstName = mFirstNameReg.getText().toString();
		String lastName = mLastNameReg.getText().toString();
		String email = mEmailReg.getText().toString();
		String password = mPasswordReg.getText().toString();
		String rePassword = mRePasswordReg.getText().toString();

		boolean ready = true;
		if (firstName.isEmpty()) {
			showLongToast("First Name was left empty!");
			ready = false;
		}
		if (lastName.isEmpty()) {
			showLongToast("Last Name was left empty!");
			ready = false;
		}
		if (email.isEmpty()) {
			showLongToast("Email was left empty!");
			ready = false;
		}
		if (password.isEmpty()) {
			showLongToast("Password was left empty!");
			ready = false;
		}
		if (rePassword.isEmpty()) {
			showLongToast("Re-enter Password was left empty!");
			ready = false;
		}
		if (!rePassword.isEmpty() && !password.equals(rePassword)) {
			showLongToast("Passwords don't match!");
			mRePasswordReg.setText("");
			ready = false;
		}
		if (mAviPath == null) {
			showLongToast("An avatar wasn't chosen!");
			ready = false;
		}
		return ready;
	}

	private void handleAviError(Exception e) {
		Log.e(LOGTAG, e.toString());
		showLongToast(e.toString());
		mPasswordReg.setText("");
		mRePasswordReg.setText("");
	}

	private void handleRegistrationError(Exception e) {
		Log.e(LOGTAG, String.format("Registration unsuccessful: %s", e));
		showLongToast(e.toString());
		mPasswordReg.setText("");
		mRePasswordReg.setText("");
	}

	private void handleRegistrationFail(TaskResult result) {
		Log.d(LOGTAG, String.format("Registration unsuccessful: %s", result.getMessage()));
		showLongToast(String.format("Registration unsuccessful: %s", result.getMessage()));
		mPasswordReg.setText("");
		mRePasswordReg.setText("");
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
						handleGcmRegistrationFail(result);
					}
				}
			}, id, gcm, "ANDROID");
		} catch (Exception e) {
			handleGcmRegistrationError(e);
		}
	}

	private void handleGcmRegistrationError(Exception e) {
		Log.e(LOGTAG, String.format("GCM Registration unsuccessful: %s", e));
		showLongToast(e.toString());
	}

	private void handleGcmRegistrationFail(TaskResult result) {
		Log.d(LOGTAG, String.format("GCM Registration unsuccessful: %s", result.getMessage()));
		showLongToast(String.format("GCM Registration unsuccessful: %s", result.getMessage()));
	}

	private void showLongToast(final String message) {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(mCtx, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	public Bundle getMyArguments() {
		return mArguments;
	}

	@Override
	public void setArguments(Bundle mArguments) {
		this.mArguments = mArguments;
	}
}