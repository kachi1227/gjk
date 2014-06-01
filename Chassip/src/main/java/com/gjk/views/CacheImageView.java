package com.gjk.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.gjk.Application;
import com.gjk.R;
import com.gjk.utils.LogWriter;
import com.gjk.utils.media.BitmapLoadListener;
import com.gjk.utils.media.BitmapLoader;
import com.gjk.utils.media.CacheManager.CacheCallback;
import com.gjk.utils.media.LoadSampledBitmapTask;
import com.gjk.utils.media.SampledBitmap;
import com.gjk.utils.media.SampledBitmapLoadListener;
import com.gjk.utils.media2.ImageUtil;

import java.io.File;


public class CacheImageView extends ImageView implements CacheCallback {


    private String mURL;

    private int mPlaceholderResourceId;
    boolean mShouldRemoveDrawableOnDetach;
    boolean mShouldLoadAfterMeasure, mBitmapLoaded = false;
    boolean mTrackBitmap = true;
    int mDesiredBitmapHeight, mDesiredBitmapWidth;
    int mSampleSize;
    boolean mCirclize;

    LoadSampledBitmapTask mCachedBitmapLoaderTask;
    SampledBitmapLoadListener mInternalBitmapListener = new SampledBitmapLoadListener() {

        @Override
        public void onLoadStarted() {
            if (mBitmapListener != null)
                mBitmapListener.onLoadStarted();
        }

        @Override
        public void onLoadComplete(View view, SampledBitmap sampledBitmap) {
            if (!mCachedBitmapLoaderTask.isCancelled()) {
                if (!mShouldLoadAfterMeasure && mURL != null) {
                    //if(mURL != null) { //if not set to true, it means that image request hasnt been reset, we can proceed
                    //certain things may happen where, we recycled the bitmap AFTER we've loaded it in here
                    //need to start over in that case
                    if (sampledBitmap.getBitmap() != null && sampledBitmap.getBitmap().isRecycled()) {
                        findAndLoadBitmap();
                        return;
                    }

                    showAppropriateBitmap(sampledBitmap);
                    if (mTrackBitmap) //tracking the bitmap will take care of the cache
                        BitmapLoader.trackBitmap(view, mURL, sampledBitmap, true);
                    else if (sampledBitmap != null && sampledBitmap.getBitmap() != null)
                        Application.get().addSampledBitmapToMemory(mURL, sampledBitmap);

                    if (mBitmapListener != null)
                        mBitmapListener.onLoadComplete(view, sampledBitmap.getBitmap());
                }

            }
        }
    };

    Runnable mResetRunnable = new Runnable() {
        @Override
        public void run() {
            reset();
        }
    };

    BitmapLoadListener mBitmapListener;

    public CacheImageView(Context context) {
        super(context);
    }

    public CacheImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CacheImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Chassip);
        mShouldRemoveDrawableOnDetach = a.getBoolean(R.styleable.Chassip_removeImageOnDetach, true);
        mTrackBitmap = a.getBoolean(R.styleable.Chassip_trackBitmap, true);
        a.recycle();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetDelayed();

    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
//if(isInLayout()) {
//	System.out.print("");
//}
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
//if(isInLayout()) {
//	System.out.print("");
//}
    }

    public void resetDelayed() {
        reset();//TODOh we have to actually fix this. way too many things can happen
//postDelayed(mResetRunnable, 2000);
    }

    private void reset() {
//TODO this isnt safe. We should take a snapshot of the state in resetDelayed
        if (mShouldRemoveDrawableOnDetach) {
            if (getDrawable() != null)
                getDrawable().setCallback(null);

            if (mTrackBitmap && mURL != null && getDrawable() != null && mSampleSize > 0 && mBitmapLoaded) {
                BitmapLoader.cleanupBitmap(mURL, mSampleSize, this);
                mSampleSize = 0;
            }
            setImageBitmap(null); //do recycle stuff
            setImageDrawable(null);
            mBitmapLoaded = false;
            mShouldLoadAfterMeasure = true;
        }
    }

    private void createBitmapLoaderTask() {
        if (mCachedBitmapLoaderTask != null)
            mCachedBitmapLoaderTask.cancel(true);
        int[] dimensions = mDesiredBitmapWidth > 0 || mDesiredBitmapHeight > 0 ? new int[]{mDesiredBitmapWidth, mDesiredBitmapHeight}
                : new int[]{getMeasuredWidth() > 0 ? getMeasuredWidth() : BitmapLoader.MAX_IMAGE_WIDTH, getMeasuredHeight() > 0 ? getMeasuredHeight() : BitmapLoader.MAX_IMAGE_HEIGHT};
        mCachedBitmapLoaderTask = new LoadSampledBitmapTask(this, dimensions, mCirclize);
        mCachedBitmapLoaderTask.setSampledBitmapLoadListener(mInternalBitmapListener);
    }

    public void configure(String url, int placeHolderResourceId, boolean circlize) {
        configure(url, placeHolderResourceId, -1, -1, circlize);
    }

    public void configure(final String url, final int placeholderResourceId, int desiredBitmapWidth,
                          int desiredBitmapHeight, boolean circlize) {
        removeCallbacks(mResetRunnable);

        mCirclize = circlize;

        mPlaceholderResourceId = placeholderResourceId;
        if (isBadURL(url)) {
            mURL = url;
            mBitmapLoaded = false;
            if (mPlaceholderResourceId > 0) {
                Bitmap bitmap = BitmapLoader.getBitmapFromAsset(CacheImageView.this, mPlaceholderResourceId,
                        BitmapLoader.TYPE_RESOURCE);
                if (mCirclize) {
                    setImageBitmap(ImageUtil.getCroppedBitmap(bitmap));
                } else {
                    setImageBitmap(bitmap);
                }
            } else
                setImageResource(0);
            return;
        } else if (url.equals(mURL) && mBitmapLoaded) //no need to reload the same image twice
            return;

        if (mTrackBitmap && mURL != null && getDrawable() != null && mSampleSize > 0 && mBitmapLoaded) {
            BitmapLoader.cleanupBitmap(mURL, mSampleSize, this);
            mSampleSize = 0;
        }

        mURL = url;
        mBitmapLoaded = false;

        mDesiredBitmapWidth = Math.max(desiredBitmapWidth, 0);
        mDesiredBitmapHeight = Math.max(desiredBitmapHeight, 0);

        mShouldLoadAfterMeasure = desiredBitmapHeight > 0 || desiredBitmapWidth > 0 ? false : getMeasuredHeight() == 0 && getMeasuredWidth() == 0;

        if (!mShouldLoadAfterMeasure)
            findAndLoadBitmap();
    }

    public void cancel() {
        if (mCachedBitmapLoaderTask != null)
            mCachedBitmapLoaderTask.cancel(true);
    }

    private void findAndLoadBitmap() {
        final File cacheFile = Application.get().getCacheManager().getCacheFile(mURL, CacheImageView.this);
        if (cacheFile != null && cacheFile.exists()) {
            createBitmapLoaderTask();
            mCachedBitmapLoaderTask.execute(Uri.parse(cacheFile.getAbsolutePath()), mURL);
        }

        if (mPlaceholderResourceId != 0)
            loadPlaceholderBitmap();
    }

    private void loadPlaceholderBitmap() {
        SampledBitmap placeholder = BitmapLoader.getSampledBitmapFromAsset(CacheImageView.this, mPlaceholderResourceId, BitmapLoader.TYPE_RESOURCE, new int[]{getMeasuredWidth() > 0 ? getMeasuredWidth() : BitmapLoader.MAX_IMAGE_WIDTH, getMeasuredHeight() > 0 ? getMeasuredHeight() : BitmapLoader.MAX_IMAGE_HEIGHT});
        Bitmap bitmap = placeholder.getBitmap();
        if (mCirclize) {
            setImageBitmap(ImageUtil.getCroppedBitmap(bitmap));
        } else {
            setImageBitmap(bitmap);
        }
        Application.get().addSampledBitmapToMemory(mPlaceholderResourceId + "", placeholder);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mShouldLoadAfterMeasure && !isBadURL(mURL) && getMeasuredWidth() > 0 && getMeasuredHeight() > 0) { //we're not loading any images for something with a bs height
            mShouldLoadAfterMeasure = false;
            findAndLoadBitmap();
        } else if (mPlaceholderResourceId != 0 && getDrawable() == null)
            loadPlaceholderBitmap();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            LogWriter.writeLog(Log.DEBUG, "OOM_BITMAP", e.getMessage() + " for " + this + " with url: " + mURL);
        }
    }

    private void showAppropriateBitmap(SampledBitmap sampledBitmap) {
        if (sampledBitmap != null && sampledBitmap.getBitmap() != null) {
            BitmapDrawable oldBitmap = null; //do some cleanup please
            if (getDrawable() instanceof BitmapDrawable) {
                oldBitmap = (BitmapDrawable) getDrawable();
            }

            mSampleSize = sampledBitmap.getSampleSize();
            setImageBitmap(sampledBitmap.getBitmap());
            mBitmapLoaded = true;
            if (oldBitmap != null)
                oldBitmap = null;

        } else if (mPlaceholderResourceId != 0)
            setImageResource(mPlaceholderResourceId);
    }

    public boolean hasBitmapLoaded() {
        return mBitmapLoaded;
    }

    public void clearUrl() {
        mURL = null;
    }

    public String getUrl() {
        return mURL;
    }

    private boolean isBadURL(String url) {
        return url == null || url.contains("null") || url.length() == 0;
    }

    public void onFileAvailable(String url, File cacheFile) {
        createBitmapLoaderTask();
        mCachedBitmapLoaderTask.execute(Uri.parse(cacheFile.getAbsolutePath()), mURL);
    }

    public void onFileFailed(String url, String message, Exception e) {

    }

    public void setBitmapLoadListener(BitmapLoadListener l) {
        mBitmapListener = l;
    }
}
