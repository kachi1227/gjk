package com.gjk.utils.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.gjk.utils.media2.ImageUtil;

import java.lang.ref.WeakReference;

public class LoadSampledBitmapTask extends AsyncTask<Object, Void, SampledBitmap> {

    private final WeakReference<ImageView> mReferenceView;
    private int[] mImageDimensions;
    private Object mData = 0;
    private SampledBitmapLoadListener mListener;
    private boolean mCirclize;

    public LoadSampledBitmapTask(ImageView imageView, int[] dimensions, boolean circlize) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        mReferenceView = new WeakReference<ImageView>(imageView);
        mImageDimensions = dimensions;
        mCirclize = circlize;
    }

    public void setSampledBitmapLoadListener(SampledBitmapLoadListener listener) {
        mListener = listener;
    }

    public void setImageDimensions(int[] dimensions) {
        mImageDimensions = dimensions;
    }

    public int getImageWidth() {
        return mImageDimensions[0];
    }

    public int getImageHeight() {
        return mImageDimensions[1];
    }

    public Object getData() {
        return mData;
    }

    // Decode image in background.
    @Override
    protected SampledBitmap doInBackground(Object... params) {
        mData = params[0];
        if ((mData instanceof Integer || mData instanceof Uri) && mReferenceView != null && mReferenceView.get() != null) {
            if (mListener != null)
                mListener.onLoadStarted();

            Object cacheKey = mData;
            if (params.length > 1)
                cacheKey = params[1];

            return BitmapLoader.getSampledBitmapFromAsset(mReferenceView.get(), mData, cacheKey.toString(), mData instanceof Integer ? BitmapLoader.TYPE_RESOURCE : BitmapLoader.TYPE_URI, mImageDimensions);
        } else
            return null;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(SampledBitmap sampledBitmap) {
        if (isCancelled()) {
            sampledBitmap.setBitmap(null);
            sampledBitmap = null;
        }

        ImageView imageView = null;
        if (mReferenceView != null && (imageView = mReferenceView.get()) != null) {
            if (mListener != null)
                mListener.onLoadComplete(imageView, sampledBitmap);
            else {
                Bitmap bitmap = sampledBitmap.getBitmap();
                if (mCirclize) {
                    imageView.setImageBitmap(ImageUtil.getCroppedBitmap(bitmap));
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }


    }
}
