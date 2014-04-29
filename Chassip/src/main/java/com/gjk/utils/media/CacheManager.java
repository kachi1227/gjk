package com.gjk.utils.media;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.gjk.Application;
import com.gjk.utils.FileUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CacheManager {
	
	public interface CacheCallback {

		void onFileAvailable(String url, File cacheFile);
		void onFileFailed(String url, String message, Exception e);
	}
	
	private static final String TAG = "CacheManager";

	private static final int MAX_CACHE_WORKERS = 5;

	private Application mApp;
	private Map<String, List<CacheCallback>> mCallbackMap= new Hashtable<String, List<CacheCallback>>();
	private Map<String, CacheWorker> mWorkerMap = new Hashtable<String, CacheWorker>();
	private ExecutorService mExecutor;
	private String mCacheDir;
	private Handler mMainHandler;

	public CacheManager(Application app) {
		mApp = app;
		mMainHandler = new Handler(Looper.getMainLooper());
		try {
			mCacheDir = app.getFileStreamPath("cache").getAbsolutePath();// StorageHelper.getBundleDirectory(mApp) + "/.mm_cache";
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public File getCacheFile(String url, CacheCallback callback) {
		File cacheDir = new File(mCacheDir);
		if (!cacheDir.exists()) {
			if (!cacheDir.mkdirs()) {
				throw new RuntimeException("Failed to create cache directory");
			}
		}

		String encodedName = Base64.encodeToString(url.getBytes(), Base64.DEFAULT);

		//Log.v(TAG, "Loading file from cache (" + encodedName + ") for (" + url + ")");
		File cacheFile = new File(cacheDir.getAbsolutePath() + File.separator + encodedName);

		if (!cacheFile.exists() && callback != null) {
			synchronized(this) {
				List<CacheCallback> callbacks = mCallbackMap.get(url);
				if (callbacks == null) {
					callbacks = new LinkedList<CacheCallback>();
					mCallbackMap.put(url, callbacks);
				}

				callbacks.add(callback);

				CacheWorker worker = mWorkerMap.get(url);
				if (worker == null) {
					worker = new CacheWorker(url, cacheFile);
					mWorkerMap.put(url, worker);
					mExecutor.execute(worker);
				}

				return null;
			}
		} else
			return cacheFile;
	}
	
	public String getFilePathForUrl(String url) {
		return (mCacheDir + File.separator + Base64.encodeToString(url.getBytes(), Base64.DEFAULT));
	}

	public void startup() {
		mExecutor = Executors.newFixedThreadPool(MAX_CACHE_WORKERS);
	}

	public void shutdown() {
		try {
			if (mExecutor != null) {
				mExecutor.awaitTermination(60, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to stop cache worker pool", e);
		}
	}

	class CacheWorker implements Runnable {

		private String mURL;
		private File mFile;

		CacheWorker(String url, File file) {
			mURL = url;
			mFile = file;
		}

		public void run() {
			FileOutputStream fos = null;
			File tmpFile = null;
			try {
				tmpFile = new File(mCacheDir + File.separator + ".temp_" + UUID.randomUUID());
				URL url = new URL(mURL);
				URLConnection conn = url.openConnection();
				InputStream is = conn.getInputStream();
				int read;
				byte[] buf = new byte[8192];
				while ((read = is.read(buf)) > -1) {
					if (fos == null) {
						fos = new FileOutputStream(tmpFile);
					}

					fos.write(buf, 0, read);
				}

				is.close();
				fos.close();
				fos = null;

				FileUtility.copyFile(tmpFile, mFile);
				//if (!tmpFile.renameTo(mFile))
				//	throw new IOException("Failed to move " + tmpFile.getAbsolutePath() + " to " + mFile.getAbsolutePath());

				if (mApp != null) {
					mMainHandler.post(new Runnable() {

						public void run() {
							List<CacheCallback> callbacks = mCallbackMap.get(mURL);
							for(CacheCallback callback : callbacks) {
								callback.onFileAvailable(mURL, mFile);
							}

							mCallbackMap.remove(mURL);
							mWorkerMap.remove(mURL);
						}
					});
				}
			} catch (final Exception e) {
				Log.e(TAG, "Failed to download file for cache", e);
				mMainHandler.post(new Runnable() {

					public void run() {
						List<CacheCallback> callbacks = mCallbackMap.get(mURL);
						for(CacheCallback callback : callbacks) {
							callback.onFileFailed(mURL, e.getMessage(), e);
						}

						mCallbackMap.remove(mURL);
						mWorkerMap.remove(mURL);
					}
				});

			} finally {
				if (fos != null) {
					try { fos.close(); } catch (Exception e) { }
				}

				if (tmpFile != null) tmpFile.delete();
			}
		}
	}
}
