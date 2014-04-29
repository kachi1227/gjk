package com.gjk;

import com.gjk.database.DatabaseManager;
import com.gjk.net.Pool;
import com.gjk.utils.media.BitmapLoader;
import com.gjk.utils.media.CacheManager;
import com.gjk.utils.media.SampledBitmap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.LruCache;
import android.widget.Toast;

public class Application extends android.app.Application {

	private static Application mInstance;
	private WifiManager mWifiManager;
	private SharedPreferences mPrefs;
	private ConnectivityManager mConnManager;
    private CacheManager mCacheManager;
    private LruCache<String, SampledBitmap> mBitmapCache;
    private ProgressDialog mProgressDialog;
	private DatabaseManager mDm;
	private Integer mVersion;
	private boolean mActivityIsInForeground;
	private Pool mSemaphores;

	public void onCreate() {

		super.onCreate();
		mInstance = this;

		initWifiManager();
		mConnManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        initCacheManager();
        initBitmapCache();
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public void initWifiManager() {
		if (mWifiManager == null)
			mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	}

	public static Application get() {
		return mInstance;
	}

	public DatabaseManager getDatabaseManager() {
		if (mDm == null) {
			mDm = new DatabaseManager(this);
		}
		return mDm;
	}

    private void initCacheManager() {
        mCacheManager = new CacheManager(this);
        mCacheManager.startup();
    }

    private void initBitmapCache() {

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 20;

        mBitmapCache = new LruCache<String, SampledBitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, SampledBitmap sampledBitmap) {
                Bitmap bitmap = sampledBitmap.getBitmap();
                int size = (Build.VERSION.SDK_INT < 12 ? bitmap.getRowBytes() * bitmap.getHeight() : bitmap.getByteCount())/1024;
                return size;
            }
            @Override
            protected void entryRemoved(boolean evicted, String key, SampledBitmap oldValue, SampledBitmap newValue) {
                if(oldValue != newValue && !BitmapLoader.isBitmapReferenced(key))
                    oldValue.getBitmap().recycle();
            }
        };
    }

    public SampledBitmap getSampledBitmap(String url) {
        return mBitmapCache.get(url);
    }

    public synchronized void addSampledBitmapToMemory(String url, SampledBitmap bitmap) {
        mBitmapCache.put(url, bitmap);
    }

    public synchronized void removeSampledBitmap(String url) {
        mBitmapCache.remove(url);
    }

    public CacheManager getCacheManager() {
        return mCacheManager;
    }

    public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
	}

	public SharedPreferences getPreferences() {
		if (mPrefs == null) {
			synchronized (this) {
				if (mPrefs == null) {
					mPrefs = getPreferences(this);
				}
			}
		}
		return mPrefs;
	}

	public static int getAppVersion(Context context) throws NameNotFoundException {
		PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		return packageInfo.versionCode;
	}

	public int getAppVersion() {
		if (mVersion == null) {
			synchronized (this) {
				if (mVersion == null) {
					try {
						mVersion = getAppVersion(this);
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return mVersion;
	}

	public boolean isNetworkAvailableWithMessage() {
		boolean connected = isNetworkAvailable();
		if (!connected) {
			Application.get().hideProgress();
			Toast.makeText(this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
		}

		return connected;
	}

	public boolean isNetworkAvailable() {
		NetworkInfo info = mConnManager.getActiveNetworkInfo();
		if (info != null)
			return info.isConnected();
		else
			return false;
	}

	public void showProgressDialogIfNecessary(Context c, int count, String title, String message) {
		if (count <= 0)
			Application.get().showProgress(c, title, message);
	}

	public void showProgress(Context c, String title, String message) {
		if (mProgressDialog == null || !mProgressDialog.isShowing())
			mProgressDialog = ProgressDialog.show(c, title, message, true, true);
	}

	public void hideProgress() {
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
	}

	public boolean isActivityIsInForeground() {
		return mActivityIsInForeground;
	}

	public void activityResumed() {
		mActivityIsInForeground = true;
	}

	public void activityPaused() {
		mActivityIsInForeground = false;
	}
	
	public Pool getSemaphores() {
		if (mSemaphores == null) {
			mSemaphores = new Pool();
		}
		return mSemaphores;
	}

	public void logout() {
		logout(true);
	}

	public void logout(boolean shouldLaunchLogin) {

	}
}
