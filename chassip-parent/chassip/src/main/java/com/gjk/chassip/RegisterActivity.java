package com.gjk.chassip;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

import com.gjk.chassip.database.DatabaseManager;
import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.net.LoginTask;
import com.gjk.chassip.net.RegisterTask;
import com.gjk.chassip.net.TaskResult;
import com.gjk.chassip.net.UpdateGCMRegTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.collect.Maps;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.gjk.chassip.Constants.*;

public class RegisterActivity extends Activity {

	private final String LOGTAG = getClass().getSimpleName();

	private GoogleCloudMessaging mGcm;
	private Context mCtx;

	private Button mLogin;
	private Button mRegister;
	private Button mSelectAvi;
	private EditText mEmailLogin;
	private EditText mPasswordLogin;
	private EditText mFirstNameReg;
	private EditText mLastNameReg;
	private EditText mEmailReg;
	private EditText mPasswordReg;
	private EditText mRePasswordReg;
	private String mAviPath;
	private SharedPreferences mPrefs;
	private AlertDialog mAviSelectorDialog;

	private boolean mLoginWasPressed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_layout);

		mCtx = this;

		checkPlayServices();

		if (checkIfAlreadyLoggedIn()) {
			done();
		}

		mLogin = (Button) findViewById(R.id.login);
		mRegister = (Button) findViewById(R.id.register);
		mSelectAvi = (Button) findViewById(R.id.selectAvi);

		mEmailLogin = (EditText) findViewById(R.id.emailLogin);
		mPasswordLogin = (EditText) findViewById(R.id.passwordLogin);

		mFirstNameReg = (EditText) findViewById(R.id.firstNameReg);
		mLastNameReg = (EditText) findViewById(R.id.lastNameReg);
		mEmailReg = (EditText) findViewById(R.id.emailReg);
		mPasswordReg = (EditText) findViewById(R.id.passwordReg);
		mRePasswordReg = (EditText) findViewById(R.id.rePasswordReg);

		initialize();
	}

	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}

	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(LOGTAG, "This device is not supported.");
				finish();
			}
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId(Context context) {

		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(LOGTAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(LOGTAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {

		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(getClass().getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {

		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 * 
	 * @return
	 */
	private void registerGcmInBackground() {

		AsyncTask<Void, Void, Boolean> gcmRegister = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {

				try {
					if (mGcm == null) {
						mGcm = GoogleCloudMessaging.getInstance(mCtx);
					}
					if (getRegistrationId(getApplicationContext()).isEmpty()) {
						String regid = mGcm.register(SENDER_ID);
						// Persist the regID - no need to register again.
						storeRegistrationId(mCtx, regid);
					}

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					if (mLoginWasPressed) {
						login();
					}
					else {
						register();
					}

				} catch (IOException e) {
					Log.e(LOGTAG, String.format("GCM egistration unsuccessful: %s", e));
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return false;

			}
		};
		gcmRegister.execute();
	}

	private void updateChassipGcm(long id, String gcm) {
		try {
			new UpdateGCMRegTask(this, new HTTPTaskListener() {

				@Override
				public void onTaskComplete(TaskResult result) {

					if (result.getResponseCode() == 1) {
						done();
					} else {
						handleRegistrationFail(result);
					}
				}
			}, id, gcm, "ANDROID");
		} catch (Exception e) {
			handleRegistrationError(e);
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {

		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(LOGTAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private boolean checkIfAlreadyLoggedIn() {
		mPrefs = getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
		return mPrefs.contains(Constants.JSON) && getGCMPreferences(mCtx).contains(PROPERTY_REG_ID);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
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
						this.sendBroadcast(mediaScanIntent);
					}
					showLongToast("Image path: " + mAviPath);
				} catch (Exception e) {
					handleAviError(e);
				}
			}
		}
	}

	private void initialize() {

		new DatabaseManager(this).clear();
		
		mLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLoginWasPressed = true;
				registerGcmInBackground();
			}
		});
		mRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLoginWasPressed = false;
				registerGcmInBackground();
			}
		});
		mSelectAvi.setOnClickListener(new OnClickListener() {

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
				});

				builder.setMessage(R.string.select_avi_message).setTitle(R.string.select_avi_title);

				// Create the AlertDialog
				mAviSelectorDialog = builder.create();
				mAviSelectorDialog.setCanceledOnTouchOutside(true);
				mAviSelectorDialog.show();
			}
		});
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
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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

	private void login() {

		String email = mEmailLogin.getText().toString();
		String password = mPasswordLogin.getText().toString();

		if (!isLoginReady()) {
			return;
		}

		try {
			new LoginTask(this, new HTTPTaskListener() {

				@Override
				public void onTaskComplete(TaskResult result) {

					if (result.getResponseCode() == 1) {
						JSONObject response = (JSONObject) result.getExtraInfo();
						try {
							mPrefs.edit().putString(Constants.JSON, response.toString()).commit();
							updateChassipGcm(response.getLong("id"), getGCMPreferences(mCtx).getString(PROPERTY_REG_ID, "abc"));

						} catch (JSONException e) {
							handleLoginError(e);
						}
					} else {
						handleLoginFail(result);
					}
				}
			}, email, password);
		} catch (Exception e) {
			handleLoginError(e);
		}
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
			new RegisterTask(this, new HTTPTaskListener() {

				@Override
				public void onTaskComplete(TaskResult result) {

					if (result.getResponseCode() == 1) {
						JSONObject response = (JSONObject) result.getExtraInfo();
						try {
							mPrefs.edit().putString(JSON, response.toString()).commit();
							updateChassipGcm(response.getLong("id"), getGCMPreferences(mCtx).getString(PROPERTY_REG_ID, "abc"));

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

	private boolean isLoginReady() {

		String email = mEmailLogin.getText().toString();
		String password = mPasswordLogin.getText().toString();

		boolean ready = true;
		if (email.isEmpty()) {
			showLongToast("Email was left empty!");
			ready = false;
		}
		if (password.isEmpty()) {
			showLongToast("Password was left empty!");
			ready = false;
		}
		return ready;
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
		mPasswordLogin.setText("");
	}

	private void handleLoginError(Exception e) {
		Log.e(LOGTAG, String.format("Login unsuccessful: %s", e));
		showLongToast(e.toString());
		mPasswordLogin.setText("");
	}

	private void handleLoginFail(TaskResult result) {
		Log.d(LOGTAG, String.format("Login unsuccessful: code %s", result.getMessage()));
		showLongToast(String.format("Login unsuccessful: code %s", result.getMessage()));
		mPasswordLogin.setText("");
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

	private void showLongToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	private void done() {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra(JSON, mPrefs.getString(JSON, "Whoa buddy"));
		startActivity(i);
		finish();
	}

}