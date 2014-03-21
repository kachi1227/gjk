/**************************************************
 * PersistentObject.java
 *
 * Created By Bill Killoran on 07/21/2011.
 * Copyright 2011 Densebrain, Inc. All rights reserved.
 **************************************************/

package com.gjk.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONObject;

public abstract class PersistentObject {
	public interface BaseType {
		void setItems(long globalId, String alias, String name);
		BaseType createNewInstance();
	}
	private boolean mIsNew, mIsDirty, mIsComplete;
	private long mId;
	private SQLiteOpenHelper mDatabaseManager;
	public static final DateFormat SQLITE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final DateFormat SQLITE_DATE_NO_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public PersistentObject(SQLiteOpenHelper dbm) {
		setDatabaseManager(dbm);
		initNewObject();
	}
	
	public PersistentObject(SQLiteOpenHelper dbm, long id, boolean list) {
		setDatabaseManager(dbm);
		setIsComplete(!list);
		hydrate(id);
	}
	
	
	public PersistentObject(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		setDatabaseManager(dbm);
		hydrate(c, skipOk);
	}
	
	public PersistentObject(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		setDatabaseManager(dbm);
		initNewObject();
		try {
			hydrate(new JSONObject(jsonString), skipOk);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public PersistentObject(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		setDatabaseManager(dbm);
		initNewObject();
		hydrate(obj, skipOk);
	}


	public void setDatabaseManager(SQLiteOpenHelper dbm) {
		mDatabaseManager = dbm;
	}
	
	public SQLiteOpenHelper getDatabaseManager() {
		return mDatabaseManager;
	}
	
	protected void initNewObject() {
		setId(0);
		setIsDirty(true);
		setIsComplete(true);
	}
	
	public long getId() {
		return mId;
	}
	protected void setId(long id) {
		mId = id;
		setIsNew(mId <= 0);
	}

	public void reset() {
		if (isNew()) {
			initNewObject();
		} else {
			hydrate(getId());
		}
	}
	
	public void hydrate(long id) {
		Cursor c = getDatabaseManager().getReadableDatabase().query(getTableName(), (isComplete() ? getAllColumnNames() : getListColumnNames()), getIdColumnName() + " = " + id, null, null, null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			throw new RuntimeException("Could not find record with id:" + id + " from table '" + getTableName() + "'");
		}
		hydrate(c, !isComplete());
		c.close();
	}
	
	public void save() {
		save(true);
	}
	
	public void save(boolean notifyDataChangeListener) {
		if (isDirty()) {
			ContentValues cv = getContentValues();
			if (isNew()) {
				setId(getDatabaseManager().getWritableDatabase().insert(getTableName(), null, cv));
			} else {
				getDatabaseManager().getWritableDatabase().update(getTableName(), cv, getIdColumnName() + " = " + getId(), null);
			}
			setIsComplete(true);
			setIsDirty(false);
			
			if(getDatabaseManager() instanceof DatabaseManager)
				((DatabaseManager)getDatabaseManager()).notifyDataChangeListeners(getTableName());
		}		
	}

	public void delete() {
		onBeforeDelete();
		getDatabaseManager().getWritableDatabase().delete(getTableName(), getIdColumnName() + " = " + getId(), null);
		onAfterDelete();
		setId(0);
		setIsDirty(true);
	}
	
	public boolean isNew() {
		return mIsNew;
	}
	protected void setIsNew(boolean isNew) {
		mIsNew = isNew;
	}
	public boolean isDirty() {
		return mIsDirty;
	}
	protected void setIsDirty(boolean isDirty) {
		mIsDirty = isDirty;
	}
	public boolean isComplete() {
		return mIsComplete;
	}
	protected void setIsComplete(boolean isComplete) {
		mIsComplete = isComplete;
	}
	public static String formatSQLDate(long dateInMillis) {
		Date d = new Date(dateInMillis);
		return formatSQLDate(d);
	}
	
	public String toJSONString() {
		return getJSONObject().toString();
	}
	
	public static String formatSQLDate(Date date) {
		if (date == null) {
			return null;
		} 
		return SQLITE_DATE_FORMAT.format(date);
	}
	public static long parseSQLDateToMillis(String str) {
		Date d = parseSQLDate(str);
		return d.getTime();
	}
	public static Date parseSQLDate(String str) {
		if (str == null)
			return null;
		try {
			return SQLITE_DATE_FORMAT.parse(str);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	
	public class PersistentObjectHydrateException extends RuntimeException {
		private static final long serialVersionUID = 3292716868806592733L;

		public PersistentObjectHydrateException(String detailMessage,
				Throwable throwable) {
			super(detailMessage, throwable);
		}

		public PersistentObjectHydrateException(String detailMessage) {
			super(detailMessage);
		}

		public PersistentObjectHydrateException(Throwable throwable) {
			super(throwable);
		}
		
	}
	
	
	/*
	 * Classes to override
	 */
	abstract public String getTableName();
	abstract public String[] getAllColumnNames();
	public String[] getListColumnNames() {
		return getAllColumnNames();
	}
	abstract public String getIdColumnName();
	abstract public String getCreateTableStatement();
	abstract public void hydrate(Cursor c, boolean skipOk);
	abstract public void hydrate(JSONObject obj, boolean skipOk);
	abstract public ContentValues getContentValues();
	abstract public JSONObject getJSONObject();
	abstract protected void onBeforeDelete();
	abstract protected void onAfterDelete();
}
