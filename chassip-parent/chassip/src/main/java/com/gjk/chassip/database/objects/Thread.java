/**************************************************
 * User.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK. All rights reserved.
 **************************************************/
 
package com.gjk.chassip.database.objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import com.gjk.chassip.database.objects.base.BaseThread;


public class Thread extends BaseThread {
	

	public static Thread insertOrUpdate(SQLiteOpenHelper dbm, JSONObject json) throws Exception {
		long groupId = json.getLong("group_id");
		long id = json.getLong("id");
		Thread thread = findOneByGlobalId(dbm, groupId, id);
		if (thread == null) {
			thread = new Thread(dbm);
		}
		thread.setGroupId(groupId);
		thread.setThreadId(id);
		if(!json.isNull("thread_type"))
			thread.setThreadType(json.getInt("thread_type"));
		if(!json.isNull("name"))
			thread.setName(json.getString("name"));
		if(!json.isNull("creator_id"))
			thread.setCreatorId(json.getLong("creator_id"));
		if(!json.isNull("creator_name"))
			thread.setCreatorName(json.getString("creator_name"));
		thread.save();
		return thread;
	}

	public Thread(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public Thread(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}
	
	public Thread(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}
	
	public Thread(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void onBeforeDelete() {

	}

	@Override
	protected void onAfterDelete() {

	}
	
}
