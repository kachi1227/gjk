package com.gjk.chassip.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gjk.chassip.Application;
import com.gjk.chassip.database.DatabaseManager;
import com.gjk.chassip.database.objects.Group;
import com.gjk.chassip.database.objects.GroupMember;
import com.gjk.chassip.database.objects.Message;
import com.gjk.chassip.database.objects.User;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import static com.gjk.chassip.Constants.*;

public final class DatabaseHelper {

	private final static DatabaseManager sDm = Application.get().getDatabaseManager();

	public static User setAccountUser(Bundle bundle) throws Exception {
		return User.insertOrUpdate(sDm, DatabaseHelper.bundleToJson(bundle));
	}

	public static long getAccountUserId() {
		Cursor cursor = sDm.getReadableDatabase().query(User.TABLE_NAME, new String[] { User.F_GLOBAL_ID }, null, null,
				null, null, null);
		cursor.moveToFirst();
		long id = cursor.getLong(0);
		cursor.close();
		return id;
	}

	public static String getAccountUserFullName() {
		Cursor cursor = sDm.getReadableDatabase().query(User.TABLE_NAME,
				new String[] { User.F_FIRST_NAME, User.F_LAST_NAME }, null, null, null, null, null);
		cursor.moveToFirst();
		String firstName = cursor.getString(cursor.getColumnIndex(User.F_FIRST_NAME));
		String lastName = cursor.getString(cursor.getColumnIndex(User.F_LAST_NAME));
		cursor.close();
		return String.format(Locale.getDefault(), "%s %s", firstName, lastName);
	}

	public static boolean isGroupTableEmpty() {
		return Group.isTableEmpty(sDm);
	}

	public static void addGroups(JSONArray groups) throws Exception {
		for (int i = 0; i < groups.length(); i++) {
			addGroup(groups.getJSONObject(i));
		}
	}

	public static Group addGroup(JSONObject group) throws Exception {
		return Group.insertOrUpdate(sDm, group);
	}

	public static void removeGroup(long chatId) throws Exception {
		Group.deleteByGlobalId(sDm, chatId);
	}

	public static Group getGroup(long chatId) {
		return Group.findOneByGlobalId(sDm, chatId);
	}

	public static Group getFirstStoredGroup() {
		Cursor cursor = sDm.getReadableDatabase().query(Group.TABLE_NAME, new String[] { Group.F_GLOBAL_ID }, null,
				null, null, null, Group.F_ID + " ASC", "1");
		cursor.moveToFirst();
		long id = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
		cursor.close();
		return getGroup(id);
	}

	public static Group getLastStoredGroup() {
		Cursor cursor = sDm.getReadableDatabase().query(Group.TABLE_NAME, new String[] { Group.F_GLOBAL_ID }, null,
				null, null, null, Group.F_ID + " DESC", "1");
		cursor.moveToFirst();
		long id = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
		cursor.close();
		return getGroup(id);
	}

	public static boolean chatExists(Group group) {
		return Group.findOneByGlobalId(sDm, group.getGlobalId()) != null;
	}

	public static boolean chatDoesntExist(Group group) {
		return !chatExists(group);
	}

	// public static void addGroupMembers(JSONArray members) throws Exception {
	// for (int i = 0; i < members.length(); i++) {
	// addGroupMember(members.getJSONObject(i), i == members.length() - 1);
	// }
	// }

	public static void addGroupMembers(JSONArray members, long chatId) throws Exception {
		for (int i = 0; i < members.length(); i++) {
			addGroupMember(members.getJSONObject(i), chatId, i == members.length() - 1);
		}
	}

	public static void addGroupMember(JSONObject member, long chatId, boolean isLast) throws Exception {
		member.put("group_id", chatId);
		GroupMember.insertOrUpdate(sDm, member, isLast);
	}

	public static GroupMember getGroupMember(long memberId) {
		return GroupMember.findOneByGlobalId(sDm, memberId);
	}

	public static GroupMember[] getGroupMembers(long chatId) {
		Cursor cursor = sDm.getReadableDatabase().query(GroupMember.TABLE_NAME, GroupMember.ALL_COLUMN_NAMES,
				GroupMember.F_GROUP_ID + " = " + chatId, null, null, null, null);
		cursor.moveToFirst();
		GroupMember[] members = new GroupMember[cursor.getCount()];
		for (int i = 0; i < cursor.getCount(); i++) {
			members[i] = new GroupMember(sDm, cursor, false);
			cursor.moveToNext();
		}
		cursor.close();
		return members;
	}

	public static void addGroupMessage(JSONObject message) throws Exception {
		Message.insertOrUpdate(sDm, message);
	}

	public static void addGroupMessages(JSONArray members) throws Exception {
		for (int i = 0; i < members.length(); i++) {
			addGroupMessage(members.getJSONObject(i));
		}
	}

	public static List<Message> getMessages(long chatId) {
		ArrayList<Message> objList = new ArrayList<Message>();
		Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
				Message.F_GROUP_ID + " = " + chatId, null, null, null, Message.F_ID + " ASC");
		c.moveToFirst();
		if (c.isAfterLast()) {
			c.close();
			return objList;
		}
		while (!c.isAfterLast()) {
			objList.add(new Message(sDm, c, false));
			c.moveToNext();
		}
		c.close();
		return objList;
	}

	public static Message getMessage(long messageId) {
		return Message.findOneByGlobalId(sDm, messageId);
	}

	public static long getLastStoredMessageId() {
		Cursor cursor = sDm.getReadableDatabase().query(Message.TABLE_NAME, new String[] { Group.F_GLOBAL_ID }, null,
				null, null, null, Group.F_ID + " DESC", "1");
		cursor.moveToFirst();
		long id;
		if (cursor.isAfterLast()) {
			id = -1;
		} else {
			id = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
		}
		cursor.close();
		return id;
	}

	public static JSONObject bundleToJson(Bundle bundle) throws JSONException {
		return new JSONObject(bundle.getString(JSON));
	}

	public static Bundle jsonToBundle(JSONObject json) {
		// try {
		// return new JSONObject(bundle.getString(JSON));
		// } catch (JSONException e) {
		// return new JSONObject();
		// }
		return null;
	}

}
