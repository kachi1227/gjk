/**************************************************
] * DatabaseManager.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK All rights reserved.
 **************************************************/

package com.gjk.database;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.database.objects.User;
import com.gjk.database.objects.Message;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {
	public static final String DB_NAME = "chassip_sql.sqlite";
	public static final int DB_VERSION = 1;
	
	private static final String PREF_FILE_NAME = "com.chassip.database.db_prefs";
	private static final String PREF_LAST_VERSION = "last_db_version";
	
	private static final boolean PRE_BUILT_DB_INCLUDED = false;
	private static final int PRE_BUILT_DB_RES_ID = 0;
	
	public static interface DataChangeListener {
		public void onDataChanged(String tableName);
	}
	
	private Context mContext;
	private HashMap<String, ArrayList<DataChangeListener>> mListeners;
	public DatabaseManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
		mListeners = new HashMap<String, ArrayList<DataChangeListener>>();
		
		if (PRE_BUILT_DB_INCLUDED) {
			replaceDatabaseIfNecessary();
		}
	}

	public void clear() {
		SQLiteDatabase db = getWritableDatabase();
		clear(db, true);
	}

	public void clear(SQLiteDatabase db, boolean loggingOut) {
		db.execSQL(User.DROP_TABLE_STATEMENT);
		db.execSQL(User.CREATE_TABLE_STATEMENT);
		db.execSQL(Group.DROP_TABLE_STATEMENT);
		db.execSQL(Group.CREATE_TABLE_STATEMENT);
		db.execSQL(GroupMember.DROP_TABLE_STATEMENT);
		db.execSQL(GroupMember.CREATE_TABLE_STATEMENT);
		db.execSQL(Message.DROP_TABLE_STATEMENT);
		db.execSQL(Message.CREATE_TABLE_STATEMENT);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (!PRE_BUILT_DB_INCLUDED) {
			clear(db, false);

		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (!PRE_BUILT_DB_INCLUDED) {

			clear(db, false);
		}
	}
	
	
	
	private void replaceDatabaseIfNecessary() {
		File dbFile = mContext.getDatabasePath(DB_NAME);
		SharedPreferences sharedPrefs = mContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		int lastVersion = sharedPrefs.getInt(PREF_LAST_VERSION, -1);
		if (lastVersion != DB_VERSION || (!dbFile.exists())) {
			replaceDatabaseFile(mContext, PRE_BUILT_DB_RES_ID);
		}
	}
	
	public static void replaceDatabaseFile(Context context, int dbFileResId) {
		replaceDatabaseFile(context, context.getResources().openRawResource(dbFileResId));
	}
	
	public static void replaceDatabaseFile(Context context, File newDatabaseFile) {
		try {
			replaceDatabaseFile(context, new FileInputStream(newDatabaseFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	
		
	public static void replaceDatabaseFile(Context context, InputStream newDatabaseInput) {
		try {
			File dbFile = context.getDatabasePath(DB_NAME);
			dbFile.getParentFile().mkdirs();
			if (dbFile.exists()) {
				dbFile.delete();
			}
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dbFile));
			
			int bytesRead = 0;
			byte[] data = new byte[8192];
			
			while((bytesRead = newDatabaseInput.read(data, 0, data.length)) >= 0) {
				out.write(data, 0, bytesRead);
			}
			out.flush();
			out.close();
			newDatabaseInput.close();
			
			SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
			editor.putInt(PREF_LAST_VERSION, DB_VERSION);
			editor.commit();

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error copying initial database", e);
		}
	}
	
	public void copyExistingDatabaseToOtherLocation(File destFile) {
		copyExistingDatabaseToOtherLocation(mContext, destFile);
	}
	
	public static void copyExistingDatabaseToOtherLocation(Context context, File destFile) {
		File dbFile = context.getDatabasePath(DB_NAME);
		if (!dbFile.exists()) {
			throw new RuntimeException("Database does not exist, cannot copy to another location");
		}
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			destFile.createNewFile();
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
			FileInputStream fis = new FileInputStream(dbFile);
			
			int bytesRead = 0;
			byte[] data = new byte[8192];
			
			while((bytesRead = fis.read(data, 0, data.length)) >= 0) {
				out.write(data, 0, bytesRead);
			}
			out.flush();
			out.close();
			fis.close();
			

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error copying existing database to a new location.", e);
		}
	}
	
	public static File getDatabaseFile(Context c) {
		return c.getDatabasePath(DB_NAME);
	}
	
	public File getDatabaseFile() {
		return mContext.getDatabasePath(DB_NAME);
	}
	
	public void registerDataChangeListener(String tableName, DataChangeListener l) {
		if(tableName == null || l == null)
			return;

		if(!mListeners.containsKey(tableName))
			mListeners.put(tableName, new ArrayList<DataChangeListener>());
		
		List<DataChangeListener> listenerList = mListeners.get(tableName);
		listenerList.add(l);
	}
	
	public void notifyDataChangeListeners(String tableName) {
		if(tableName == null)
			return;
		
		List<DataChangeListener> listeners = mListeners.get(tableName);
		if(listeners != null) {
			for(DataChangeListener listener : listeners) {
				listener.onDataChanged(tableName);
			}
		}
	}
	
	public boolean unregisterDataChangeListener(String tableName, DataChangeListener l) {
		if(tableName == null || l == null)
			return false;
		
		if(mListeners.containsKey(tableName))
			return mListeners.get(tableName).remove(l);
		
		return false;
	}

}
