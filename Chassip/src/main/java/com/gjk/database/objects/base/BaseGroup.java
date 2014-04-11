/**************************************************
 * BaseGroup.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK, Inc. All rights reserved.
 **************************************************/
 
package com.gjk.database.objects.base;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.gjk.database.PersistentObject;
import com.gjk.database.objects.Group;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public abstract class BaseGroup extends PersistentObject {
	
	private static final String ERROR_MSG_CLOSED_CURSOR = "Tried to hyrdate Group from closed cursor.";

	private static final String ERROR_MSG_HYDRATE_NO_ID = "Error fetching column 'id' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_GLOBAL_ID = "Error fetching column 'global_id' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_NAME = "Error fetching column 'name' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_IMAGE_URL = "Error fetching column 'image_url' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_CREATOR_ID = "Error fetching column 'creator_id' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_CREATOR_NAME = "Error fetching column 'creator_name' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_SIDE_CHATS = "Error fetching column 'side_chats' from table 'group_chat'";
	private static final String ERROR_MSG_HYDRATE_NO_WHISPERS = "Error fetching column 'whispers' from table 'group_chat'";
	
	public static final String TABLE_NAME = "group_chat";
	

	public static final String F_ID = "_id";
	public static final String F_GLOBAL_ID = "global_id";
	public static final String F_NAME = "name";
	public static final String F_IMAGE_URL = "image_url";
	public static final String F_CREATOR_ID = "creator_id";
	public static final String F_CREATOR_NAME = "creator_name";
	public static final String F_SIDE_CHATS = "side_chats";
	public static final String F_WHISPERS = "whispers";
	
	public static final String[] ALL_COLUMN_NAMES = new String[] {F_ID, F_GLOBAL_ID, F_NAME, F_IMAGE_URL, F_CREATOR_ID, F_CREATOR_NAME, F_SIDE_CHATS, F_WHISPERS};
	
	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE \"group_chat\"(   \"_id\" INTEGER PRIMARY KEY NOT NULL,   \"global_id\" INTEGER NOT NULL,   \"name\" VARCHAR(256),	\"image_url\" VARCHAR(2000),	\"creator_id\" INTEGER NOT NULL,	\"creator_name\" VARCHAR(512),   \"side_chats\" TEXT, 	\"whispers\" TEXT)";
	public static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS 'group_chat';";
	
	private static final String COUNT_STATEMENT = "SELECT COUNT(" + F_ID + ") FROM group_chat";
	
	private static final String EMPTY_STRING = "";
	

	private long mGlobalId;
	private String mName;
	private String mImageUrl;
	private long mCreatorId;
	private String mCreatorName;
	private String mSideChats;
	private String mWhispers;
		
	public BaseGroup(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}
	
	public BaseGroup(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}
	
	public BaseGroup(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}

	public BaseGroup(SQLiteOpenHelper dbm, long id, boolean list) {
		super(dbm, id, list);
	}

	public BaseGroup(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void initNewObject() {
		super.initNewObject();

		mGlobalId = 0;
		mName = "";
		mImageUrl = "";
		mCreatorId = 0;
		mCreatorName = "";
		mSideChats = "";
		mWhispers = "";
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
			setName(c.getString(c.getColumnIndexOrThrow(F_NAME)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_NAME, e);
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
		try {
			setCreatorId(c.getLong(c.getColumnIndexOrThrow(F_CREATOR_ID)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_CREATOR_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setCreatorName(c.getString(c.getColumnIndexOrThrow(F_CREATOR_NAME)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_CREATOR_NAME, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setSideChats(c.getString(c.getColumnIndexOrThrow(F_SIDE_CHATS)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SIDE_CHATS, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setWhispers(c.getString(c.getColumnIndexOrThrow(F_WHISPERS)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_WHISPERS, e);
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
			mName = obj.getString(F_NAME);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_NAME, e);
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
		try {
			mCreatorId = obj.getLong(F_CREATOR_ID);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_CREATOR_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mCreatorName = obj.getString(F_CREATOR_NAME);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_CREATOR_NAME, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mSideChats = obj.getString(F_SIDE_CHATS);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_SIDE_CHATS, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mWhispers = obj.getString(F_WHISPERS);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_WHISPERS, e);
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
			obj.put(F_NAME, mName);
			obj.put(F_IMAGE_URL, mImageUrl);
			obj.put(F_CREATOR_ID, mCreatorId);
			obj.put(F_CREATOR_NAME, mCreatorName);
			obj.put(F_SIDE_CHATS, mSideChats);
			obj.put(F_WHISPERS, mWhispers);
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
		cv.put(F_NAME, mName);
		cv.put(F_IMAGE_URL, mImageUrl);
		cv.put(F_CREATOR_ID, mCreatorId);
		cv.put(F_CREATOR_NAME, mCreatorName);
		cv.put(F_SIDE_CHATS, mSideChats);
		cv.put(F_WHISPERS, mWhispers);
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

	public static List<Group> findAllObjects(SQLiteOpenHelper dbm, String orderBy) {
		ArrayList<Group> objList = new ArrayList<Group>();
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, null, null, null, null, orderBy);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return objList;
		} 
		
		
		while (!c.isAfterLast()) {
			objList.add(new Group(dbm, c, false));
			c.moveToNext();
		}
		c.close();
		return objList;
	}
	
	public static Group findById(SQLiteOpenHelper dbm, long id) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_ID + " = " + id, null, null, null, null, "1");
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		} 
		Group obj = new Group(dbm, c, false);
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
	
	
	
	public static Group findOneByGlobalId(SQLiteOpenHelper dbm, long val) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_GLOBAL_ID + " = " + val, null, null, null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		} 
		Group obj = new Group(dbm, c, false);
		c.close();
		return obj;
	}
	
	public static int deleteByGlobalId(SQLiteOpenHelper dbm, long val) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_GLOBAL_ID + "=" + val, null);
	}
	
	public String getName() {
		return mName;
	}

	public void setName(String val) {
		this.mName = val;
		setIsDirty(true);
	}
	
	
	public String getImageUrl() {
		return mImageUrl;
	}
	
	public void setImageUrl(String val) {
		this.mImageUrl = val;
		setIsDirty(true);
	}
	
	public long getCreatorId() {
		return mCreatorId;
	}
	
	public void setCreatorId(long val) {
		this.mCreatorId = val;
		setIsDirty(true);
	}

	public String getCreatorName() {
		return mCreatorName;
	}

	public void setCreatorName(String val) {
		this.mCreatorName = val;
		setIsDirty(true);
	}
	
	public String getSideChats() {
		return mSideChats;
	}

	public void setSideChats(String val) {
		this.mSideChats = val;
		setIsDirty(true);
	}
	
	public String getWhispers() {
		return mWhispers;
	}

	public void setWhispers(String val) {
		this.mWhispers = val;
		setIsDirty(true);
	}
}
