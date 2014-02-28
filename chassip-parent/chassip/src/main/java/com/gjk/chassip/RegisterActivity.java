package com.gjk.chassip;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.net.LoginTask;
import com.gjk.chassip.net.RegisterTask;
import com.gjk.chassip.net.TaskResult;
import com.google.common.collect.Maps;

import android.net.Uri;
import android.os.Build;
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
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private static final int GALLERY_REQUEST = 1;
	private static final int CAMERA_REQUEST = 2;
	
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        
        checkIfLoggedIn();
        
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
    
    private void checkIfLoggedIn() {
    	mPrefs = getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
    	if (mPrefs.contains(Constants.ID)) {
    		done();
    	}
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
		            }
		            else if (requestCode == CAMERA_REQUEST) {
		            	// add pic to gallery
		                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		                File f = new File(mAviPath);
		                Uri contentUri = Uri.fromFile(f);
		                mediaScanIntent.setData(contentUri);
		                this.sendBroadcast(mediaScanIntent);
		            }
	            	showLongToast("Image path: " + mAviPath);
            	}
            	catch (Exception e) {
            		handleAviError(e);
            	}
            }
        }
    }

    private void initialize() {
    	
    	final Context ctx = this; 
    	
    	mLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            login();
			}
		});
    	mRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            register();
			}
		});
    	mSelectAvi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
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
				        }
						else{
							sendGalleryIntentPreKitKat();
				        }
					}
				});
				
				builder.setMessage(R.string.select_avi_message)
			           .setTitle(R.string.select_avi_title);
				
				// Create the AlertDialog
				mAviSelectorDialog = builder.create();
				mAviSelectorDialog.setCanceledOnTouchOutside(true);
				mAviSelectorDialog.show();
			}
		});
    }

	private void sendGalleryIntentPreKitKat() {
		Log.d(this.getClass().getSimpleName(), "Running pre-Kit-Kat");
    	Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
    }
	
    @TargetApi(19)
    private void sendGalleryIntent() {
    	Log.d(this.getClass().getSimpleName(), "Running Kit-Kat or higher!");
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
                String imageFileName = String.format("%s_%s.jpg", getResources().getString(R.string.app_name), timeStamp);
                File storageDir = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.app_name));
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
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
        if (cursor == null) { // Source is Dropbox or other similar local file path
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
							long id = response.getLong("id");
							mPrefs.edit().putLong(Constants.ID, id).commit();
					    	done();
							// {last_name=testLast, id=2, image=resources/users/user-2//images/img20140224042542.jpg, first_name=testFirst, bio=null, email=test@email.com}
		
						} catch (JSONException e) {
							handleLoginError(e);
						}
					}
					else {
						handleLoginFail(result);
					}
				}
	    	}, email, password);
    	}
    	catch (Exception e) {
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
							long id = response.getLong("id");
							mPrefs.edit().putLong(Constants.ID, id).commit();
					    	done();
							// {last_name=testLast, id=2, image=resources/users/user-2//images/img20140224042542.jpg, first_name=testFirst, bio=null, email=test@email.com}
		
						} catch (JSONException e) {
							handleRegistrationError(e);
						}
					}
					else {
						handleRegistrationFail(result);
					}
				}
	    	}, firstName, lastName, email, password, fieldMapping);
    	}
    	catch (Exception e) {
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
		Log.e(this.getClass().getSimpleName(), e.toString());
		showLongToast(e.toString());
		mPasswordLogin.setText("");
    }
    
    private void handleLoginError(Exception e) {
		Log.e(this.getClass().getSimpleName(), String.format("Login unsuccessful: %s", e));
		showLongToast(e.toString());
		mPasswordLogin.setText("");
    }
    
    private void handleLoginFail(TaskResult result) {
		Log.d(this.getClass().getSimpleName(), String.format("Login unsuccessful: code %d", result.getMessage()));
		showLongToast(String.format("Login unsuccessful: code %d", result.getMessage()));
		mPasswordLogin.setText("");
    }
    
    private void handleRegistrationError(Exception e) {
		Log.e(this.getClass().getSimpleName(), String.format("Registration unsuccessful: %s", e));
		showLongToast(e.toString());
		mPasswordReg.setText("");
		mRePasswordReg.setText("");
    }
    
    private void handleRegistrationFail(TaskResult result) {
		Log.d(this.getClass().getSimpleName(), String.format("Registration unsuccessful: %s", result.getMessage()));
		showLongToast(String.format("Registration unsuccessful: %s", result.getMessage()));
		mPasswordReg.setText("");
		mRePasswordReg.setText("");
    }
    
    private void showLongToast(String message) {
    	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    
    private void done() {
    	Intent i = new Intent(this, ChatActivity.class);
    	long id = mPrefs.getLong(Constants.ID, Long.MIN_VALUE);
    	if (id == Long.MIN_VALUE) {
    		throw new RuntimeException("WOW SON");
    	}
    	i.putExtra(Constants.ID, id);
    	startActivity(i);
    	finish();
    }
    
}