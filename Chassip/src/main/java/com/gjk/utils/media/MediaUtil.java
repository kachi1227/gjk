package com.gjk.utils.media;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.gjk.Application;
import com.gjk.R;
import com.gjk.utils.FileUtility;

public class MediaUtil {
	public final static int MEDIA_PHOTO = 0;
	public final static int MEDIA_VIDEO = 1;
	public final static int MEDIA_AUDIO = 2;
	
	public static Uri LAST_MEDIA_NAME = null;
	public static int LAST_MEDIA_ID = -1;
	
	public static File getDefaultMediaDirectory() {
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM, Application.get().getString(R.string.app_name));
		if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs())
				return null;

		return mediaStorageDir;
	}
	
	public static File getDefaultTempMediaDirectory() {
		File tempDir = new File(getDefaultMediaDirectory(), "Temp"); 
		if(!tempDir.exists()) {
			tempDir.mkdir();
		}
		
		return tempDir;
	}
	
	public static String getUriExtension(Uri uri) {
		return uri.toString().substring(uri.toString().lastIndexOf(".") + 1);
	}
	
	public static int getMediaTypeFromUri(Uri uri) {
		return getMediaTypeFromUri(uri, -1);
	}
	
	public static int getMediaTypeFromUri(Uri uri, int defaultVal) {
		int type = defaultVal;
		String path = uri.getPath().toLowerCase(Locale.getDefault());
		if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".gif") || path.endsWith(".bmp") || path.endsWith(".webp") || path.endsWith(".jpeg"))
			type = MEDIA_PHOTO;
		else if(path.endsWith(".3gp") || path.endsWith(".mp4") || path.endsWith(".ts") || path.endsWith(".webm") || path.endsWith(".mkv"))
			type = MEDIA_VIDEO;
		else if(path.endsWith(".flac") || path.endsWith(".mp3") || path.endsWith(".mid") || path.endsWith(".xmf") || path.endsWith(".mxmf") ||
				path.endsWith(".rtttl") || path.endsWith(".rtx") || path.endsWith(".ota") ||  path.endsWith(".imy") || path.endsWith(".ogg") ||
				path.endsWith(".mkv") || path.endsWith(".wav"))
			type = MEDIA_AUDIO;
		
		return type;
	}
	
	public static String convertMediaTypeToJSONLabel(int type) {
		String convertedType = null;
		switch(type) {
			case 0:
				convertedType = "photo";
				break;
			case 1:
				convertedType = "video";
				break;
			case 2:
				convertedType = "audio";
				break;
		}
		
		return convertedType;
	}
	
	
	public static String convertMediaTypeToMimeType(int type) {
		String convertedType = null;
		switch(type) {
			case 0:
				convertedType = "image";
				break;
			case 1:
				convertedType = "video";
				break;
			case 2:
				convertedType = "audio";
				break;
		}
		
		return convertedType;
	}
	
	public static String getJSONLabelFromUri(Uri uri) {
		return convertMediaTypeToJSONLabel(getMediaTypeFromUri(uri));
	}
	
	public static Uri getMediaPathFromContentResolver(Context c, Uri contentUri) {
		if(!(contentUri.toString().contains("content://media/external/video") || contentUri.toString().contains("content://media/external/images")))
			return contentUri;
		
		//I know they currently point to the same string, but if google changes stuff later, this will still work
		String[] filePathColumn = {contentUri.toString().contains("content://media/external/video") ? MediaStore.Video.Media.DATA : MediaStore.Images.Media.DATA};
		Cursor cursor = c.getContentResolver().query(contentUri, filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		Uri aUri = Uri.parse(cursor.getString(columnIndex));
		aUri = Uri.fromFile(new File(aUri.getPath())); //convert to file Uri
		cursor.close();
		return aUri;
	}
	
	public static Uri createSaveableLocationForUri(Uri uri, boolean isTemp) {
		File mediaStorageDir = isTemp ? getDefaultTempMediaDirectory() : getDefaultMediaDirectory();
		if(mediaStorageDir == null)
			return null;
		
		String prefix = "";
		switch(getMediaTypeFromUri(uri)) {
			case MEDIA_PHOTO:
				prefix = "IMG_";
				break;
			case MEDIA_VIDEO:
				prefix = "VID_";
				break;
			case MEDIA_AUDIO:
				prefix = "AUD_";
				break;
		}
		
		String timeStamp = DateFormat.format("yyyyMMdd_hhmmss", System.currentTimeMillis()).toString();
		String filename = prefix + timeStamp + "." + getUriExtension(uri);
		return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + filename));
		
	}
	
	public static Uri updateMediaName(int type) {
		File mediaStorageDir = getDefaultMediaDirectory();
		if (mediaStorageDir == null)
			return LAST_MEDIA_NAME;
		
		// Create a media file name
		String timeStamp = DateFormat.format("yyyyMMdd_hhmmss", System.currentTimeMillis()).toString();

		switch(type) {
		case MEDIA_PHOTO:
			LAST_MEDIA_NAME = Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg"));
			break;
		case MEDIA_VIDEO:
			LAST_MEDIA_NAME = Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".3gp"));
			break;
		}

	    return LAST_MEDIA_NAME;
	}
		
	public static Uri getMediaName() {
		return LAST_MEDIA_NAME;
	}
	
	public static void updateLastMediaId(Activity currActivity) {
		final String[] imageColumns = { MediaStore.Images.Media._ID };
		final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
		Cursor imageCursor = currActivity.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, imageOrderBy);
	    if(imageCursor.moveToFirst()){
	        LAST_MEDIA_ID = imageCursor.getInt(imageCursor.getColumnIndex(imageColumns[0]));
	    }
	}
	
	public static int getLastMediaId() {
		return LAST_MEDIA_ID;
	}
	
	public static void scanNewPhoto(Uri photoUri) {
		new MediaScanner(Application.get(), photoUri);
	}
	
	/**
	 * Android is a mess. Even if you specify EXTRA_OUTPUT many built in camera apps will do whatever they want. 
	 * This  is here to fix that fragmented case. Goes through many of the different scenarios (camera savin to both
	 * EXTRA_OUTPUT AND gallery, camera ignoring EXTRA_OUTPUT, etc) and corrects these mistakes. So annoying that we have
	 * to do this.
	 * 
	 * @param activity
	 * @param data
	 * @return
	 */
	public static Uri adjustFragmentedAndroidUri(Activity activity, Intent data) {
		Uri tempUri = null;
		
		//do this if android completely ignored the EXTRA_OUTPUT and stored the image uri in the intent 
		if(data != null && data.getData() != null) {
			Uri imagePath = data.getData();	
			String[] filePathColumn = {MediaStore.Images.Media.DATA};
			Cursor cursor = activity.getContentResolver().query(imagePath, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			Uri aUri = Uri.parse(cursor.getString(columnIndex));
			cursor.close();
			try {
				FileUtility.copyFile(new File(aUri.getPath()), new File((tempUri = updateMediaName(MEDIA_PHOTO)).getPath()));
				new File(aUri.getPath()).delete();
			} catch (Exception e) {
				//TODO fill out Toast.makeText(activity, R.string.error_could_not_save_media, Toast.LENGTH_LONG).show();
			}
		} else {
			
			tempUri = LAST_MEDIA_NAME;
			
			//delete gallery image if one was added
			final String[] imageColumns = { MediaStore.Images.Media._ID };
			final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
			Cursor imageCursor = activity.managedQuery(tempUri.toString().endsWith("mp4") ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, imageOrderBy);
		    if(imageCursor.moveToFirst()){
		        int id = imageCursor.getInt(imageCursor.getColumnIndex(imageColumns[0]));
		        if(id > LAST_MEDIA_ID) {
			        String[] filePathColumn = {tempUri.toString().endsWith("mp4") ? MediaStore.Video.Media.DATA : MediaStore.Images.Media.DATA};
					int columnIndex = imageCursor.getColumnIndex(filePathColumn[0]);
					Uri aUri = Uri.parse(imageCursor.getString(columnIndex));
					new File(aUri.getPath()).delete();
		        }
		    }
			
		}
		return tempUri;

	}
}
