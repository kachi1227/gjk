package com.gjk.utils.media;

import android.graphics.Bitmap;
import android.view.View;

public interface SampledBitmapLoadListener {

	public void onLoadStarted();
	public void onLoadComplete(View view, SampledBitmap bitmap);
}
