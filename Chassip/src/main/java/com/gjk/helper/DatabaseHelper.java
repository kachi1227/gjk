package com.gjk.helper;

import android.database.Cursor;
import android.os.Bundle;

import com.gjk.Application;
import com.gjk.Constants;
import com.gjk.database.DatabaseManager;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.database.objects.Message;
import com.gjk.database.objects.User;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.gjk.Constants.JSON;

public final class DatabaseHelper {

    private final static DatabaseManager sDm = Application.get().getDatabaseManager();

    public static User setAccountUser(Bundle bundle) throws Exception {
        return User.insertOrUpdate(sDm, DatabaseHelper.bundleToJson(bundle));
    }

    public static Long getAccountUserId() {
        Cursor cursor = sDm.getReadableDatabase().query(User.TABLE_NAME, new String[]{User.F_GLOBAL_ID}, null, null,
                null, null, null);
        cursor.moveToFirst();
        Long id = cursor.getLong(0);
        cursor.close();
        return id;
    }

    public static String getAccountUserFullName() {
        Cursor cursor = sDm.getReadableDatabase().query(User.TABLE_NAME,
                new String[]{User.F_FIRST_NAME, User.F_LAST_NAME}, null, null, null, null, null);
        cursor.moveToFirst();
        String firstName = cursor.getString(cursor.getColumnIndex(User.F_FIRST_NAME));
        String lastName = cursor.getString(cursor.getColumnIndex(User.F_LAST_NAME));
        cursor.close();
        return String.format(Locale.getDefault(), "%s %s", firstName, lastName);
    }

    public static boolean isGroupTableEmpty() {
        return Group.isTableEmpty(sDm);
    }

    public static List<Group> addGroups(JSONArray groups) throws Exception {
        List<Group> listOfGroups = Lists.newArrayList();
        for (int i = 0; i < groups.length(); i++) {
            listOfGroups.add(addGroup(groups.getJSONObject(i)));
        }
        return listOfGroups;
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

    public static List<Group> getGroups() {
        return Group.findAllObjects(sDm, Group.F_ID + " ASC");
    }

    public static Group getFirstStoredGroup() {
        Cursor cursor = sDm.getReadableDatabase().query(Group.TABLE_NAME, new String[]{Group.F_GLOBAL_ID}, null,
                null, null, null, Group.F_ID + " ASC", "1");
        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
        cursor.close();
        return getGroup(id);
    }

    public static Group getLastStoredGroup() {
        Cursor cursor = sDm.getReadableDatabase().query(Group.TABLE_NAME, new String[]{Group.F_GLOBAL_ID}, null,
                null, null, null, Group.F_ID + " DESC", "1");
        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
        cursor.close();
        return getGroup(id);
    }

    public static boolean groupExists(Group group) {
        return Group.findOneByGlobalId(sDm, group.getGlobalId()) != null;
    }

    public static void addGroupMembers(JSONArray members, long chatId) throws Exception {
        for (int i = 0; i < members.length(); i++) {
            addGroupMember(members.getJSONObject(i), chatId, i == members.length() - 1);
        }
    }

    public static GroupMember addGroupMember(JSONObject member, long chatId, boolean isLast) throws Exception {
        member.put("group_id", chatId);
        return GroupMember.insertOrUpdate(sDm, member, isLast);
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

    public static boolean messageExists(JSONObject json) {
        try {
            return Message.findOneByGlobalId(sDm, json.getLong("id")) != null;
        } catch (JSONException e) {
            return false;
        }
    }

    public static void addGroupMessages(JSONArray members) throws Exception {
        for (int i = 0; i < members.length(); i++) {
            addGroupMessage(members.getJSONObject(i));
        }
    }

    public static Message addGroupMessage(JSONObject message) throws Exception {
        return messageExists(message) ? null : Message.insertOrUpdate(sDm, message);
    }

    public static List<Message> getMessages(long chatId) {
        ArrayList<Message> objList = new ArrayList<Message>();
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
                Message.F_GROUP_ID + " = " + chatId, null, null, null, Message.F_ID + " DESC",
                String.valueOf(Constants.PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT));
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return objList;
        }
        while (!c.isAfterLast()) {
            objList.add(0, new Message(sDm, c, false));
            c.moveToNext();
        }
        c.close();
        return objList;
    }

    public static List<Message> getMessages(long chatId, Message before) {
        ArrayList<Message> objList = new ArrayList<Message>();
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
                Message.F_GROUP_ID + " = " + chatId + " AND " + Message.F_DATE + " < " + String.valueOf(before
                        .getDate())
                , null, null, null, Message.F_ID + " DESC", String.valueOf(Constants.PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT));
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return objList;
        }
        while (!c.isAfterLast()) {

            objList.add(0, new Message(sDm, c, false));
            c.moveToNext();
        }
        c.close();
        return objList;
    }

    public static List<Message> getMessages(long chatId, long threadId) {
        ArrayList<Message> objList = new ArrayList<Message>();
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
                Message.F_GROUP_ID + " = " + chatId + " AND " + Message.F_TABLE_ID + " = " + threadId
                , null, null, null, Message.F_ID + " DESC", String.valueOf(Constants.PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT));
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return objList;
        }
        while (!c.isAfterLast()) {

            objList.add(0, new Message(sDm, c, false));
            c.moveToNext();
        }
        c.close();
        return objList;
    }

    public static List<Message> getMessages(long chatId, long threadId, Message before) {
        ArrayList<Message> objList = new ArrayList<Message>();
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
                Message.F_GROUP_ID + " = " + chatId + " AND " + Message.F_TABLE_ID + " = " + threadId + " AND " +
                        Message.F_DATE + " < " + before.getDate(), null, null, null, Message.F_ID + " DESC", String.valueOf(Constants.PROPERTY_SETTING_MESSAGE_LOAD_LIMIT_DEFAULT));
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return objList;
        }
        while (!c.isAfterLast()) {

            objList.add(0, new Message(sDm, c, false));
            c.moveToNext();
        }
        c.close();
        return objList;
    }

    public static Message getLatestMessage(long chatId) {
        Cursor cursor = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
                Message.F_GROUP_ID + " = " + chatId, null, null, null, Message.F_DATE + " DESC", "1");
        cursor.moveToFirst();
        Message m;
        if (cursor.isAfterLast()) {
            m = null;
        } else {
            m = new Message(sDm, cursor, false);
        }
        cursor.close();
        return m;
    }

    public static long getLastStoredMessageId(long chatId) {
        Cursor cursor = sDm.getReadableDatabase().query(Message.TABLE_NAME, new String[]{Group.F_GLOBAL_ID},
                Message.F_GROUP_ID + " = " + chatId, null, null, null, Message.F_DATE + " DESC", "1");
        cursor.moveToFirst();
        long id;
        if (cursor.isAfterLast()) {
            id = 0;
        } else {
            id = cursor.getLong(cursor.getColumnIndex(Message.F_GLOBAL_ID));
        }
        cursor.close();
        return id;
    }

    public static JSONObject bundleToJson(Bundle bundle) throws JSONException {
        return new JSONObject(bundle.getString(JSON));
    }
}
