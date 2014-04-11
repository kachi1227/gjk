/**************************************************
 * GroupMember.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK. All rights reserved.
 **************************************************/
 
package com.gjk.database.objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import com.gjk.database.objects.base.BaseThreadMember;


public class ThreadMember extends BaseThreadMember {
	
	public static ThreadMember insertOrUpdate(SQLiteOpenHelper dbm, JSONObject json, boolean isLast) throws Exception {
		long id = json.getLong("id");
		long groupId = json.getLong("group_id");
		int threadId = json.getInt("thread_id");
		ThreadMember threadMember = findOneByGlobalAndGroupAndThreadId(dbm, id, groupId, threadId);
		if (threadMember == null) {
			threadMember = new ThreadMember(dbm);
		}
		threadMember.setGlobalId(id);
		threadMember.setGroupId(groupId);
		threadMember.setThreadId(threadId);

		threadMember.save(isLast);
		return threadMember;
	}

	public ThreadMember(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public ThreadMember(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}
	
	public ThreadMember(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}
	
		
	public ThreadMember(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void onBeforeDelete() {

	}

	@Override
	protected void onAfterDelete() {

	}

}
