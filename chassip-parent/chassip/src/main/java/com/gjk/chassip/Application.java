package com.gjk.chassip;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;
	
	public class Application extends android.app.Application {

		private static Application mInstance;
		private WifiManager mWifiManager;
		private SharedPreferences mPrefs;
		private ConnectivityManager mConnManager;
		private ProgressDialog mProgressDialog;
		
		
		public void onCreate() {
			
//			android.os.Debug.waitForDebugger();
	    	
			super.onCreate();
			mInstance = this;
			
			initWifiManager();
			mConnManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			
		}
		
		
		public WifiManager getWifiManager() {
			return mWifiManager;
		}
		
		public void initWifiManager() {
			if(mWifiManager == null)
				mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
		}	
			
		public static Application get() {
			return mInstance;
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
		
		public boolean isNetworkAvailableWithMessage() {
			boolean connected = isNetworkAvailable();
			if(!connected) {
				Application.get().hideProgress();
				Toast.makeText(this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();
			}
			
			return connected;
		}
		
		public boolean isNetworkAvailable() {
			NetworkInfo info = mConnManager.getActiveNetworkInfo();
			if(info != null)
				return info.isConnected();
			else return false; 
		}
		
		public void showProgressDialogIfNecessary(Context c, int count, String title, String message) {
			if(count <= 0) 
				Application.get().showProgress(c, title, message);
		}
		
		public void showProgress(Context c, String title, String message) {
			if(mProgressDialog == null || !mProgressDialog.isShowing()) 
				mProgressDialog = ProgressDialog.show(c, title, message, true, true);		
		}
		
		public void hideProgress() {
			if(mProgressDialog != null)
				mProgressDialog.dismiss();
		}
		
		public void logout() {
			logout(true);
		}
		
		public void logout(boolean shouldLaunchLogin) {
			
		
		}
	}
