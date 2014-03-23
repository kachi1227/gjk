/**************************************************
 * GroupMember.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK. All rights reserved.
 **************************************************/
 
package com.gjk.chassip.database.objects;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import com.gjk.chassip.database.objects.base.BaseGroupMember;


public class GroupMember extends BaseGroupMember {
	
	public static GroupMember insertOrUpdate(SQLiteOpenHelper dbm, JSONObject json, boolean isLast) throws Exception {
		long id = json.getLong("id");
		GroupMember groupMember = findOneByGlobalId(dbm, id);
		if (groupMember == null) {
			groupMember = new GroupMember(dbm);
		}
		groupMember.setGlobalId(id);
		if(!json.isNull("group_id"))
			groupMember.setGroupId(json.getLong("group_id"));
		if(!json.isNull("first_name"))
			groupMember.setFirstName(json.getString("first_name"));
		if(!json.isNull("last_name"))
			groupMember.setLastName(json.getString("last_name"));
		if(!json.isNull("image"))
			groupMember.setImageUrl(json.getString("image"));

		groupMember.save(isLast);
		return groupMember;
	}

	public GroupMember(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public GroupMember(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}
	
	public GroupMember(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}
	
		
	public GroupMember(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void onBeforeDelete() {

	}

	@Override
	protected void onAfterDelete() {

	}

	public String getFullName() {
		return getFirstName() + " " + getLastName();
	}
	
	@Override
	public boolean equals(Object o) {
		return getGlobalId() == ((GroupMember) o).getGlobalId();
	}
}
