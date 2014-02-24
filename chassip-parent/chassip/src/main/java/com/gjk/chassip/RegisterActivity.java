package com.gjk.chassip;

import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.net.LoginTask;
import com.gjk.chassip.net.RegisterTask;
import com.gjk.chassip.net.TaskResult;
import com.google.common.collect.Maps;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private static final int GALLERY_REQUEST = 1;
	private static final int CAMERA_REQUEST = 2;
	
	private Button mLogin;
	private Button mRegister;
	private Button mLoadImage;
	private TextView mEmailLogin;
	private TextView mPasswordLogin;
	private TextView mFirstNameReg;
	private TextView mLastNameReg;
	private TextView mEmailReg;
	private TextView mPasswordReg;
	private String selectedImagePath;
	private SharedPreferences mPrefs;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        
        checkIfLoggedIn();
        
        mLogin = (Button) findViewById(R.id.login);
        mRegister = (Button) findViewById(R.id.register);
        mLoadImage = (Button) findViewById(R.id.loadImage);
        
        mEmailLogin = (TextView) findViewById(R.id.emailLogin);
        mPasswordLogin = (TextView) findViewById(R.id.passwordLogin);
        
        mFirstNameReg = (TextView) findViewById(R.id.firstNameReg);
        mLastNameReg = (TextView) findViewById(R.id.lastNameReg);
        mEmailReg = (TextView) findViewById(R.id.emailReg);
        mPasswordReg = (TextView) findViewById(R.id.passwordReg);
        
        initialize();
    }
    
    private void checkIfLoggedIn() {
    	mPrefs = getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
    	if (mPrefs.contains(Constants.ID)) {
    		done();
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
    	mLoadImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// custom dialog
				final Dialog dialog = new Dialog(ctx);
				dialog.setContentView(R.layout.image_selector_dialog);
				dialog.setTitle("Load Image");
	 			Button gallery = (Button) dialog.findViewById(R.id.gallery);
				gallery.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						loadImage();
						dialog.dismiss();
					}
				});
				Button camera = (Button) dialog.findViewById(R.id.camera);
				camera.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						useCamera();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImage() {
        // in onCreate or any event where your want the user to
        // select a file
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), GALLERY_REQUEST);
    }
    
    private void useCamera() {
    	Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    
    /**
     * helper to retrieve the path of an image URI
     */
    private String getPath(Uri uri) {
        // just some safety built in 
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }
    
    private void login() {
    	String email = mEmailLogin.getText().toString();
    	String password = mPasswordLogin.getText().toString();
    	
    	if (email.isEmpty() || password.isEmpty()) {
    		return;
    	}
    	
    	new LoginTask(this, new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.d(this.getClass().getSimpleName(), "Swaggggg");
					JSONObject response = (JSONObject) result.getExtraInfo();
					try {
						long id = response.getLong("id");
						mPrefs.edit().putLong(Constants.ID, id).commit();
				    	done();
						// {last_name=testLast, id=2, image=resources/users/user-2//images/img20140224042542.jpg, first_name=testFirst, bio=null, email=test@email.com}
	
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					Log.d(this.getClass().getSimpleName(), String.format("Login unsuccessful: code %d", result.getResponseCode()));
				}
					
			}
    		
    	}, email, password);
    }
    
    private void register() {
    	
    	String firstName = mFirstNameReg.getText().toString();
    	String lastName = mLastNameReg.getText().toString();
    	String email = mEmailReg.getText().toString();
    	String password = mPasswordReg.getText().toString();
    	
    	if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || selectedImagePath == null) {
    		return;
    	}
    	
    	HashMap<String, Object> fieldMapping = Maps.newHashMap();
    	fieldMapping.put("image", new File(selectedImagePath));
    	new RegisterTask(this, new HTTPTaskListener() {

			@Override
			public void onTaskComplete(TaskResult result) {
				if (result.getResponseCode() == 1) {
					Log.d(this.getClass().getSimpleName(), "Swaggggg");
					JSONObject response = (JSONObject) result.getExtraInfo();
					try {
						long id = response.getLong("id");
						mPrefs.edit().putLong(Constants.ID, id).commit();
				    	done();
						// {last_name=testLast, id=2, image=resources/users/user-2//images/img20140224042542.jpg, first_name=testFirst, bio=null, email=test@email.com}
	
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					Log.d(this.getClass().getSimpleName(), String.format("Registration unsuccessful: code %d", result.getResponseCode()));
				}
			}
    		
    	}, firstName, lastName, email, password, fieldMapping);
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