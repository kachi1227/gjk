/**************************************************
 * User.java
 *
 * Created By Kachi Nwaobasi on 03/18/2014.
 * Copyright 2014 GJK. All rights reserved.
 **************************************************/
 
package com.gjk.database.objects;

import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

import com.gjk.database.objects.base.BaseGroup;


public class Group extends BaseGroup {
	
	//{"group":{"id":"1","name":"GJK","image":"","creator_id":"4","creator_name":"Kachi Nwaobasi","side_chats":"2:side_chat-2|3:Hello WOrld|4:Home|5:Sweet|6:Home","whispers":"1:whisper-1|2:Knock, Knock|3:Who's There"},"success":true}
	public static Group insertOrUpdate(SQLiteOpenHelper dbm, JSONObject json) throws Exception {
		long id = json.getLong("id");
		Group group = findOneByGlobalId(dbm, id);
		if (group == null) {
			group = new Group(dbm);
		}
		group.setGlobalId(id);
		if(!json.isNull("name"))
			group.setName(json.getString("name"));
		if(!json.isNull("image"))
			group.setImageUrl(json.getString("image"));
		if(!json.isNull("creator_id"))
			group.setCreatorId(json.getLong("creator_id"));
		if(!json.isNull("creator_name"))
			group.setCreatorName(json.getString("creator_name"));
		if(!json.isNull("side_chats"))
			group.setSideChats(json.getString("side_chats"));
		if(!json.isNull("whispers"))

			group.setWhispers(json.getString("whispers"));
		
		if (!group.getSideChats().isEmpty()) {
			Log.d("asd","asd");
		}
		if (!group.getWhispers().isEmpty()) {
			Log.d("asd","asd");
		}
		group.save();
		return group;
	}

	public Group(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public Group(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}
	
	public Group(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}
	
	public Group(SQLiteOpenHelper dbm) {
		super(dbm);
	}

	@Override
	protected void onBeforeDelete() {

	}

	@Override
	protected void onAfterDelete() {

	}
	
	@Override
	public boolean equals(Object o) {
		return getGlobalId() == ((Group) o).getGlobalId();
	}
	
	@Override
	public String toString() {
		return String.format(Locale.getDefault(), "#%d: %s", getGlobalId(), getName());
	}

}
