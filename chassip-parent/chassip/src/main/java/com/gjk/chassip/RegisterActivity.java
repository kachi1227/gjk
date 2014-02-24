package com.gjk.chassip;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private static final int GALLERY_REQUEST = 1;
	private static final int CAMERA_REQUEST = 2;
	
	private Button mLogin;
	private Button mRegister;
	private Button mLoadImage;
	private String selectedImagePath;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        
        mLogin = (Button) findViewById(R.id.login);
        mRegister = (Button) findViewById(R.id.register);
        mLoadImage = (Button) findViewById(R.id.loadImage);
        
        initialize();
    }
    
    private void initialize() {
    	
    	final Context ctx = this; 
    	
    	mLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            done();
			}
		});
    	mRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            done();
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
    
    private void done() {
    	startActivity(new Intent(this, ChatActivity.class));
    	finish();
    }
    
}