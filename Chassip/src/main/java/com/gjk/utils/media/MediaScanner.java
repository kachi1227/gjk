package com.gjk.utils.media;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class MediaScanner implements MediaScannerConnectionClient {

	private MediaScannerConnection mMs;
	private Uri mUri;

	public MediaScanner(Context context, Uri uri) {
		mUri = uri;
		mMs = new MediaScannerConnection(context, this);
		mMs.connect();
	}

	@Override
	public void onMediaScannerConnected() {
		mMs.scanFile(mUri.getPath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		mMs.disconnect();
	}
}