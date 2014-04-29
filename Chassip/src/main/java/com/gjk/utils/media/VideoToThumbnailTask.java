package com.gjk.utils.media;

import com.gjk.Constants;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Video.Thumbnails;
import android.widget.ImageView;

public class VideoToThumbnailTask extends AsyncTask<String, Void, Bitmap> {

	ImageView mImage;
	
	public VideoToThumbnailTask(ImageView image) {
		mImage = image;
	}
	@Override
	protected Bitmap doInBackground(String... args) {
		return ThumbnailUtils.createVideoThumbnail(Constants.BASE_URL + args[0], Thumbnails.MINI_KIND);
	}
	
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		mImage.setImageBitmap(bitmap);
	}

}
