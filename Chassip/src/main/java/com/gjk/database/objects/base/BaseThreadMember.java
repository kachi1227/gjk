package com.gjk.database.objects.base;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.gjk.database.PersistentObject;
import com.gjk.database.objects.ThreadMember;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public abstract class BaseThreadMember extends PersistentObject {

	private static final String ERROR_MSG_CLOSED_CURSOR = "Tried to hyrdate entity from closed cursor.";

	private static final String ERROR_MSG_HYDRATE_NO_ID = "Error fetching column 'id' from table 'thread_member'";
	private static final String ERROR_MSG_HYDRATE_NO_GLOBAL_ID = "Error fetching column 'global_id' from table 'thread_member'";
	private static final String ERROR_MSG_HYDRATE_NO_GROUP_ID = "Error fetching column 'group_id' from table 'thread_member'";
	private static final String ERROR_MSG_HYDRATE_NO_THREAD_ID = "Error fetching column 'thread_id' from table 'thread_member'";

	public static final String TABLE_NAME = "thread_member";

	public static final String F_ID = "_id";
	public static final String F_GLOBAL_ID = "global_id";
	public static final String F_GROUP_ID = "group_id";
	public static final String F_THREAD_ID = "thread_id";

	public static final String[] ALL_COLUMN_NAMES = new String[] { F_ID, F_GLOBAL_ID, F_GROUP_ID, F_THREAD_ID };

	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE \"thread_member\"(   \"_id\" INTEGER PRIMARY KEY NOT NULL,	\"global_id\" INTEGER NOT NULL,		\"group_id\" INTEGER NOT NULL,		\"thread_id\" INTEGER NOT NULL)";
	public static final String DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS 'thread_member';";

	private static final String COUNT_STATEMENT = "SELECT COUNT(" + F_ID + ") FROM thread_member";

	private static final String EMPTY_STRING = "";

	private long mGlobalId;
	private long mGroupId;
	private int mThreadId;

	public BaseThreadMember(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public BaseThreadMember(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}

	public BaseThreadMember(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}

	public BaseThreadMember(SQLiteOpenHelper dbm, long id, boolean list) {
		super(dbm, id, list);
	}

	public BaseThreadMember(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void initNewObject() {
		super.initNewObject();

		mGlobalId = 0;
		mGroupId = 0;
		mThreadId = 0;
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
			setThreadId(c.getInt(c.getColumnIndexOrThrow(F_THREAD_ID)));
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_THREAD_ID, e);
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
			mGroupId = obj.getLong(F_GROUP_ID);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_GROUP_ID, e);
			} else {
				setIsComplete(false);
			}
		}
		try {
			mThreadId = obj.getInt(F_THREAD_ID);
		} catch (Exception e) {
			if (!skipOk) {
				e.printStackTrace();
				throw new PersistentObjectHydrateException(ERROR_MSG_HYDRATE_NO_THREAD_ID, e);
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
			obj.put(F_GROUP_ID, mGroupId);
			obj.put(F_THREAD_ID, mThreadId);
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
		cv.put(F_GROUP_ID, mGroupId);
		cv.put(F_THREAD_ID, mThreadId);
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

	public static List<ThreadMember> findAllObjects(SQLiteOpenHelper dbm, String orderBy) {
		ArrayList<ThreadMember> objList = new ArrayList<ThreadMember>();
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, null, null, null, null, orderBy);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return objList;
		}

		while (!c.isAfterLast()) {
			objList.add(new ThreadMember(dbm, c, false));
			c.moveToNext();
		}
		c.close();
		return objList;
	}

	public static ThreadMember findById(SQLiteOpenHelper dbm, long id) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_ID + " = " + id, null, null, null,
				null, "1");
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		}
		ThreadMember obj = new ThreadMember(dbm, c, false);
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

	public static ThreadMember findOneByGlobalId(SQLiteOpenHelper dbm, long val) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES, F_GLOBAL_ID + " = " + val, null, null,
				null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		}
		ThreadMember obj = new ThreadMember(dbm, c, false);
		c.close();
		return obj;
	}

	public static ThreadMember findOneByGlobalAndGroupId(SQLiteOpenHelper dbm, long val, long groupId) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES,
				F_GLOBAL_ID + " = " + val + " and " + F_GROUP_ID + " = " + groupId, null, null, null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		}
		ThreadMember obj = new ThreadMember(dbm, c, false);
		c.close();
		return obj;
	}
	
	public static ThreadMember findOneByGlobalAndGroupAndThreadId(SQLiteOpenHelper dbm, long val, long groupId, int threadId) {
		Cursor c = dbm.getReadableDatabase().query(TABLE_NAME, ALL_COLUMN_NAMES,
				F_GLOBAL_ID + " = " + val + " and " + F_GROUP_ID + " = " + groupId + " and " + F_THREAD_ID + " = " + threadId, null, null, null, null);
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return null;
		}
		ThreadMember obj = new ThreadMember(dbm, c, false);
		c.close();
		return obj;
	}

	public static int deleteByGlobalId(SQLiteOpenHelper dbm, long val) {
		return dbm.getWritableDatabase().delete(TABLE_NAME, F_GLOBAL_ID + "=" + val, null);
	}

	public long getGroupId() {
		return mGroupId;
	}

	public void setGroupId(long val) {
		mGroupId = val;
		setIsDirty(true);
	}

	public int getThreadId() {
		return mThreadId;
	}

	public void setThreadId(int val) {
		mThreadId = val;
		setIsDirty(true);
	}

}
