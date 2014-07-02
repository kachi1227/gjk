package com.gjk.utils.media2;

import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentManager;

import com.gjk.Application;
import com.gjk.Constants;
import com.gjk.R;
import com.gjk.views.RecyclingImageView;

/**
 * @author gpl
 */
public class ImageManager {

    private static ImageManager sInstance;

    private ImageFetcher mImageFetcherUncirclized;
    private ImageFetcher mImageFetcherCirclized;

    public static synchronized ImageManager getInstance(FragmentManager fm) {
        if (sInstance == null) {
            sInstance = new ImageManager(fm);
        }
        return sInstance;
    }

    private ImageManager(FragmentManager fm) {

        mImageFetcherCirclized = new ImageFetcher(Application.get().getApplicationContext(), 1000, true);
        mImageFetcherCirclized.setLoadingImage(R.drawable.empty_photo);
        mImageFetcherCirclized.setImageFadeIn(true);
        ImageCache.ImageCacheParams params1 = new ImageCache.ImageCacheParams(Application.get().getApplicationContext
                (), "image_cache_circlized");
        params1.setMemCacheSizePercent(0.8f);
        mImageFetcherCirclized.addImageCache(fm, params1);

        mImageFetcherUncirclized = new ImageFetcher(Application.get().getApplicationContext(), 1000, false);
        mImageFetcherUncirclized.setLoadingImage(R.drawable.empty_photo);
        mImageFetcherUncirclized.setImageFadeIn(true);
        ImageCache.ImageCacheParams params2 = new ImageCache.ImageCacheParams(Application.get().getApplicationContext
                (), "image_cache_uncirclized");
        params1.setMemCacheSizePercent(0.8f);
        mImageFetcherUncirclized.addImageCache(fm, params2);
    }

    public void loadCirclizedImage(String url, RecyclingImageView v) {
        mImageFetcherCirclized.loadImage(Constants.BASE_URL + url, v);
    }

    public void loadUncirclizedImage(String url, RecyclingImageView v) {
        mImageFetcherUncirclized.loadImage(Constants.BASE_URL + url, v);
    }

    public BitmapDrawable fetchCirclizedImage(String url) {
        return mImageFetcherCirclized.fetchImage(url);
    }

    public BitmapDrawable fetchUncirclizedImage(String url) {
        return mImageFetcherUncirclized.fetchImage(url);
    }

    public void clearCache() {
        mImageFetcherCirclized.clearCache();
        mImageFetcherUncirclized.clearCache();
    }

    public void resume() {
        mImageFetcherCirclized.setExitTasksEarly(false);
        mImageFetcherUncirclized.setExitTasksEarly(false);
    }

    public void pause() {
        mImageFetcherCirclized.setPauseWork(false);
        mImageFetcherCirclized.setExitTasksEarly(true);
        mImageFetcherCirclized.flushCache();
        mImageFetcherUncirclized.setPauseWork(false);
        mImageFetcherUncirclized.setExitTasksEarly(true);
        mImageFetcherUncirclized.flushCache();
    }

    public void destroy() {
        mImageFetcherCirclized.closeCache();
        mImageFetcherUncirclized.closeCache();
    }
}
