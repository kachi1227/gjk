/**************************************************
 * BaseUser.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK, Inc. All rights reserved.
 **************************************************/
 
package com.gjk.database.objects.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.gjk.database.PersistentObject;
import com.gjk.database.objects.User;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public abstract class BaseUser extends PersistentObject {
	
	private static final String ERROR_MSG_CLOSED_CURSOR = "Tried to hyrdate User from closed cursor.";

	private static final String ERROR_MSG_HYDRATE_NO_ID = "Error fetching column 'id' from table 'user'";
	private static final String ERROR_MSG_HYDRATE_NO_GLOBAL_ID = "Error fetching column 'global_id' from table 'user'";
	private static final String ERROR_MSG_HYDRATE_NO_FIRST_NAME = "Error fetching column 'first_name' from table 'user'";
	private static final String ERROR_MSG_HYDRATE_NO_LAST_NAME = "Error fetching column 'last_name' from table 'user'";
	private static final String ERROR_MSG_HYDRATE_NO_BIO = "Error fetching column 'bio' from table 'user'";
	private static final String ERROR_MSG_HYDRATE_NO_IMAGE_URL = "Error fetching column 'image_url' from table 'user'";
	
	public static final String TABLE_NAME = "user";
	

	public static final String F_ID = "_id";
	public static final String F_GLOBAL_ID = "global_id";

	public static final String F_FIRST_NAME = "first_name";
	public static final String F_LAST_NAME = "last_name";
	public static final String F_BIO = "bio";
	public static final String F_IMAGE_URL = "image_url";
	
	public static final String[] ALL_COLUMN_NAMES = new String[] {F_ID, F_GLOBAL_ID, F_FIRST_NAME, F_LAST_NAME, F_BIO, F_IMAGE_URL};
	
	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE \"user\"(   \"_id\" INTEGER PRIMARY KEY NOT NULL,   \"global_id\" INTEGER NOT NULL,   \"first_name\" VARCHAR(256),	\"last_name\" VARCHAR(256),   \"bio\" TEXT,	\"image_url\" VARCHAR(2000))";
	public static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS 'user';";
	
	private static final String COUNT_STATEMENT = "SELECT COUNT(" + F_ID + ") FROM user";
	
	private static final String EMPTY_STRING = "";
	

	private long mGlobalId;
	private String mFirstName;
	private String mLastName;
	private String mBio;
	private String mImageUrl;
	
	public BaseUser(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}
	
	public BaseUser(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}
	
	public BaseUser(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}

	public BaseUser(SQLiteOpenHelper dbm, long id, boolean list) {
		super(dbm, id, list);
	}

	public BaseUser(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void initNewObject() {
		super.initNewObject();

		mGlobalId = 0;
		mFirstName = "";
		mLastName = "";
		mBio = "";
		mImageUrl = "";
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String[] getAllColumnNames() {
		return ALL_COLUMN_NAMES;
	}

	@Override
	public String getIdColumnName() {
		return F_ID;
	}

	@Override
	public String getCreateTableStatement() {
		return CREATE_TABLE_STATEMENT;
	}

	@Override
	public void hydrate(Cursor c, boolean skipOk) {
		if (c.isClosed()) {
			throw new PersistentObjectHydrateException(ERROR_MSG_CLOSED_CURSOR);
		}
		
		

		try {
			setId(c.getLong(c.getColumnIndexOrThrow(F_ID)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setGlobalId(c.getLong(c.getColumnIndexOrThrow(F_GLOBAL_ID)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GLOBAL_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setFirstName(c.getString(c.getColumnIndexOrThrow(F_FIRST_NAME)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_FIRST_NAME, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setLastName(c.getString(c.getColumnIndexOrThrow(F_LAST_NAME)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_LAST_NAME, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setBio(c.getString(c.getColumnIndexOrThrow(F_BIO)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_BIO, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setImageUrl(c.getString(c.getColumnIndexOrThrow(F_IMAGE_URL)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_IMAGE_URL, e);
			} else {
				setIsComplete(false);
			}
		}
		
		setIsDirty(false);
	}
	
	@Override
	public void hydrate(JSONObject obj, boolean skipOk) {

		try {
			setId(obj.getLong(F_ID));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mGlobalId = obj.getLong(F_GLOBAL_ID);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GLOBAL_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mFirstName = obj.getString(F_FIRST_NAME);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_FIRST_NAME, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mLastName = obj.getString(F_LAST_NAME);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_LAST_NAME, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mBio = obj.getString(F_BIO);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_BIO, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mImageUrl = obj.getString(F_IMAGE_URL);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_IMAGE_URL, e);
			} else {
				setIsComplete(false);
			}
		}
		setIsDirty(true);
		setIsNew(true);
	}
	
	@Override
	public JSONObject getJSONObject() {
		try {
			JSONObject obj = new JSONObject();

			obj.put(F_ID, getId());
			obj.put(F_GLOBAL_ID, mGlobalId);
			obj.put(F_FIRST_NAME, mFirstName);
			obj.put(F_LAST_NAME, mLastName);
			obj.put(F_BIO, mBio);
			obj.put(F_IMAGE_URL, mImageUrl);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues(14);

		cv.put(F_GLOBAL_ID, mGlobalId);
		cv.put(F_FIRST_NAME, mFirstName);
		cv.put(F_LAST_NAME, mLastName);
		cv.put(F_BIO, mBio);
		cv.put(F_IMAGE_URL, mImageUrl);
		return cv;
	}
	
	public static long getCount(SQLiteOpenHelper dbm) {
		return getCount(dbm, null);
	}
	
	public static long getCount(SQLiteOpenHelper dbm, String whereClause) {
		long count = 0;
		SQLiteStatement stmt = null;
		if (whereClause == null) {
			stmt = dbm.getReadableDatabase().compileStatement(COUNT_STATEMENT);
		} else {
			stmt = dbm.getReadableDatabase().compileStatement(COUNT_STATEMENT + " WHERE " + whereClause);
		}
		count = stmt.simpleQueryForLong();
		stmt.close();
		return count;
	}
	
	public static boolean isTableEmpty(SQLiteOpenHelper dbm) {
		return getCount(dbm, null) == 0;
	}
	
	public static int deleteById(SQLiteOpenHelper dbm, long id, boolean notEqual) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_ID + (notEqual ? " <> " : " = ") + id, null);
	}
	
	public static int deleteByIds(SQLiteOpenHelper dbm, List<Long> idsToDelete, boolean notIn) {
		String idList = "";
		for (int i = 0; i<idsToDelete.size(); i++) {
			if (!idList.equals(EMPTY_STRING)) {
				idList += ", ";
			}
			idList += "" + idsToDelete.get(i);
		}
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_ID + (notIn ? " NOT" : "") + " IN (" + idList + ")", null);
	}
	
	public static int deleteWhere(SQLiteOpenHelper dbm, String whereClause) {
		return deleteWhere(dbm, whereClause, null);
	}
	
	public static int deleteWhere(SQLiteOpenHelper dbm, String whereClause, String[] whereArgs) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, whereClause, whereArgs);
	}

	public static List<User> findAllObjects(SQLiteOpenHelper dbm, String orderBy) {
		ArrayList<User> objList = new ArrayList<User>();
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, null, null, null, null, orderBy);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return objList;
		} 
		
		
		while (!c.isAfterLast()) {
			objList.add(new User(dbm, c, false));
			c.moveToNext();
		}
		c.close();
		return objList;
	}
	
	public static User findById(SQLiteOpenHelper dbm, long id) {
	Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_ID + " = " + id, null, null, null, null, "1");
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		} 
		User obj = new User(dbm, c, false);
		c.close();
		return obj;
	}



	public long getGlobalId() {
		return mGlobalId;
	}

	public void setGlobalId(long val) {
		this.mGlobalId = val;
		setIsDirty(true);
	}
	
	
	
	public static User findOneByGlobalId(SQLiteOpenHelper dbm, long val) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_GLOBAL_ID + " = " + val, null, null, null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		} 
		User obj = new User(dbm, c, false);
		c.close();
		return obj;
	}
	
	public static int deleteByGlobalId(SQLiteOpenHelper dbm, long val) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_GLOBAL_ID + "=" + val, null);
	}
	
	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String val) {
		this.mFirstName = val;
		setIsDirty(true);
	}
	
	public static int deleteByFirstName(SQLiteOpenHelper dbm, String val) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_FIRST_NAME + " = ?", new String[] {val});
	}

	public String getLastName() {
		return mLastName;
	}

	public void setLastName(String val) {
		this.mLastName = val;
		setIsDirty(true);
	}
	
	public static int deleteByLastName(SQLiteOpenHelper dbm, String val) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_LAST_NAME + " = ?", new String[] {val});
	}
	public String getBio() {
		return mBio;
	}

	public void setBio(String val) {
		this.mBio = val;
		setIsDirty(true);
	}
		
	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String val) {
		this.mImageUrl = val;
		setIsDirty(true);
	}
}
