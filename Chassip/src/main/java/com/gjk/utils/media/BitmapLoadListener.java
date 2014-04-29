package com.gjk.utils.media;

import android.graphics.Bitmap;
import android.view.View;

public interface BitmapLoadListener {

	public void onLoadStarted();
	public void onLoadComplete(View view, Bitmap bitmap);
}
