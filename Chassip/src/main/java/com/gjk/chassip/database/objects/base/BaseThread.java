package com.gjk.chassip.database.objects.base;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.gjk.chassip.database.PersistentObject;
import com.gjk.chassip.database.objects.Thread;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public abstract class BaseThread extends PersistentObject {

	private static final String ERROR_MSG_CLOSED_CURSOR = "Tried to hyrdate entity from closed cursor.";

	private static final String ERROR_MSG_HYDRATE_NO_ID = "Error fetching column 'id' from table 'thread'";
	private static final String ERROR_MSG_HYDRATE_NO_GROUP_ID = "Error fetching column 'group_id' from table 'thread'";
	private static final String ERROR_MSG_HYDRATE_NO_THREAD_ID = "Error fetching column 'thread_id' from table 'thread'";
	private static final String ERROR_MSG_HYDRATE_NO_THREAD_TYPE = "Error fetching column 'thread_type' from table 'thread'";
	private static final String ERROR_MSG_HYDRATE_NO_NAME = "Error fetching column 'name' from table 'thread'";
	private static final String ERROR_MSG_HYDRATE_NO_CREATOR_ID = "Error fetching column 'creator_id' from table 'thread'";
	private static final String ERROR_MSG_HYDRATE_NO_CREATOR_NAME = "Error fetching column 'creator_name' from table 'thread'";

	public static final String TABLE_NAME = "thread";

	public static final String F_ID = "_id";
	public static final String F_GROUP_ID = "group_id";
	public static final String F_THREAD_ID = "thread_id";
	public static final String F_THREAD_TYPE = "thread_type";
	public static final String F_NAME = "name";
	public static final String F_CREATOR_ID = "creator_id";
	public static final String F_CREATOR_NAME = "creator_name";

	public static final String[] ALL_COLUMN_NAMES = new String[] { F_ID, F_GROUP_ID, F_THREAD_ID, F_THREAD_TYPE, F_NAME, F_CREATOR_ID, F_CREATOR_NAME };

	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE \"thread\"(   \"_id\" INTEGER PRIMARY KEY NOT NULL,		\"group_id\" INTEGER NOT NULL,	\"thread_id\" INTEGER NOT NULL,	\"thread_type\" INTEGER NOT NULL,		\"name\" VARCHAR(256), 	\"creator_id\" INTEGER NOT NULL,	\"creator_name\" VARCHAR(512))";
	public static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS 'thread';";

	private static final String COUNT_STATEMENT = "SELECT COUNT(" + F_ID + ") FROM thread";

	private static final String EMPTY_STRING = "";

	private long mThreadId;
	private long mGroupId;
	private int mThreadType;
	private String mName;
	private long mCreatorId;
	private String mCreatorName;

	public BaseThread(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public BaseThread(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}

	public BaseThread(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}

	public BaseThread(SQLiteOpenHelper dbm, long id, boolean list) {
		super(dbm, id, list);
	}

	public BaseThread(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void initNewObject() {
		super.initNewObject();

		mGroupId = 0;
		mThreadId = 0;
		mThreadType = 0;
		mName = "";
		mCreatorId = 0;
		mCreatorName = "";
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
			setGroupId(c.getLong(c.getColumnIndexOrThrow(F_GROUP_ID)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GROUP_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setThreadId(c.getLong(c.getColumnIndexOrThrow(F_THREAD_ID)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_THREAD_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setThreadType(c.getInt(c.getColumnIndexOrThrow(F_THREAD_TYPE)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_THREAD_TYPE, e);
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
			setGroupId(obj.getLong(F_GROUP_ID));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GROUP_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setThreadId(obj.getLong(F_THREAD_ID));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_THREAD_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setThreadType(obj.getInt(F_THREAD_TYPE));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_THREAD_TYPE, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			setName(obj.getString(F_NAME));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_NAME, e);
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
		
		setIsNew(true);
	}

	@Override
	public JSONObject getJSONObject() {
		try {
			JSONObject obj = new JSONObject();

			obj.put(F_ID, getId());
			obj.put(F_THREAD_ID, mThreadId);
			obj.put(F_THREAD_TYPE, mThreadType);
			obj.put(F_GROUP_ID, mGroupId);
			obj.put(F_NAME, mName);
			obj.put(F_CREATOR_ID, mCreatorId);
			obj.put(F_CREATOR_NAME, mCreatorName);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues(14);

		cv.put(F_THREAD_ID, mThreadId);
		cv.put(F_THREAD_TYPE, mThreadType);
		cv.put(F_GROUP_ID, mGroupId);
		cv.put(F_NAME, mName);
		cv.put(F_CREATOR_ID, mCreatorId);
		cv.put(F_CREATOR_NAME, mCreatorName);
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
		for (int i = 0; i < idsToDelete.size(); i++) {
			if (!idList.equals(EMPTY_STRING)) {
				idList += ", ";
			}
			idList += "" + idsToDelete.get(i);
		}
		return dbm.getWritableDatabase()
				.delete(TABLE_NAME, F_ID + (notIn ? " NOT" : "") + " IN (" + idList + ")", null);
	}

	public static int deleteWhere(SQLiteOpenHelper dbm, String whereClause) {
		return deleteWhere(dbm, whereClause, null);
	}

	public static int deleteWhere(SQLiteOpenHelper dbm, String whereClause, String[] whereArgs) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, whereClause, whereArgs);
	}

	public static List<Thread> findAllObjects(SQLiteOpenHelper dbm, String orderBy) {
		ArrayList<Thread> objList = new ArrayList<Thread>();
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, null, null, null, null, orderBy);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return objList;
		}

		while (!c.isAfterLast()) {
			objList.add(new Thread(dbm, c, false));
			c.moveToNext();
		}
		c.close();
		return objList;
	}

	public static Thread findById(SQLiteOpenHelper dbm, long id) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_ID + " = " + id, null, null, null,
				null, "1");
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		}
		Thread obj = new Thread(dbm, c, false);
		c.close();
		return obj;
	}

	public static Thread findOneByGlobalId(SQLiteOpenHelper dbm, long groupId, long threadId) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES,
				F_GROUP_ID + " = " + groupId + " and " + F_THREAD_ID + " = " + threadId, null, null, null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		}
		Thread obj = new Thread(dbm, c, false);
		c.close();
		return obj;
	}

	// public static int deleteByGlobalId(SQLiteOpenHelper dbm, long val) {
	// return dbm.getWritableDatabase().delete(TABLE_NAME, F_GLOBAL_ID + "=" + val, null);
	// }

	public long getGroupId() {
		return mGroupId;
	}

	public void setGroupId(long val) {
		mGroupId = val;
		setIsDirty(true);
	}

	public long getThreadId() {
		return mThreadId;
	}

	public void setThreadId(long val) {
		mThreadId = val;
		setIsDirty(true);
	}

	public int getThreadType() {
		return mThreadType;
	}

	public void setThreadType(int val) {
		mThreadType = val;
		setIsDirty(true);
	}

	public String getName() {
		return mName;
	}

	public void setName(String val) {
		this.mName = val;
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
	

}
