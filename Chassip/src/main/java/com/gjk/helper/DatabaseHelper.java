package com.gjk.helper;

import android.database.Cursor;
import android.os.Bundle;

import com.gjk.Application;
import com.gjk.ConvoType;
import com.gjk.database.DatabaseManager;
import com.gjk.database.objects.Group;
import com.gjk.database.objects.GroupMember;
import com.gjk.database.objects.Message;
import com.gjk.database.objects.User;
import com.gjk.database.objects.base.BaseMessage;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.gjk.Constants.LOGIN_JSON;

public final class DatabaseHelper {

    private final static DatabaseManager sDm = Application.get().getDatabaseManager();

    public static User setAccountUser(Bundle bundle) throws Exception {
        return User.insertOrUpdate(sDm, DatabaseHelper.bundleToJson(bundle));
    }

    public static User setAccountUser(JSONObject json) throws Exception {
        return User.insertOrUpdate(sDm, json);
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
        return String.format("%s %s", firstName, lastName);
    }

    public static List<Group> addGroups(JSONArray groups, boolean notify) throws Exception {
        List<Group> listOfGroups = Lists.newArrayList();
        for (int i = 0; i < groups.length(); i++) {
            listOfGroups.add(addGroup(groups.getJSONObject(i), notify));
        }
        return listOfGroups;
    }

    public static Group addGroup(JSONObject group, boolean isLast) throws Exception {
        return Group.insertOrUpdate(sDm, group, isLast);
    }

    public static Group getGroup(long chatId) {
        return Group.findOneByGlobalId(sDm, chatId);
    }

    public static long getGroupIdFromSideConvoId(long sideConvoId) {
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, new String[]{Message.F_GROUP_ID},
                Message.F_MESSAGE_TYPE_ID + " = " + ConvoType.SIDE_CONVO.getValue() + " AND " + Message.F_TABLE_ID +
                        " = " + sideConvoId, null, null, null, null
        );
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return -1;
        }
        long id = c.getLong(c.getColumnIndex(Message.F_GLOBAL_ID));
        c.close();
        return id;
    }

    public static long getGroupIdFromWhisperId(long whisperId) {
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, new String[]{Message.F_GROUP_ID},
                Message.F_MESSAGE_TYPE_ID + " = " + ConvoType.WHISPER.getValue() + " AND " + Message.F_TABLE_ID +
                        " = " + whisperId, null, null, null, null
        );
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            return -1;
        }
        long id = c.getLong(c.getColumnIndex(Message.F_GLOBAL_ID));
        c.close();
        return id;
    }

    public static Cursor getGroupsCursor() {
        return sDm.getReadableDatabase().query(Group.TABLE_NAME, Group.ALL_COLUMN_NAMES, null, null, null, null, Group.F_ID + " ASC");
    }

    public static Group getFirstStoredGroup() {
        Cursor cursor = sDm.getReadableDatabase().query(Group.TABLE_NAME, Group.ALL_COLUMN_NAMES, null,
                null, null, null, Group.F_ID + " ASC", "1");
        if (cursor.isAfterLast()) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(Group.F_GLOBAL_ID));
        cursor.close();
        return getGroup(id);
    }

    public static boolean removeGroup(long groupId) {
        return Group.deleteWhere(sDm, Group.F_GLOBAL_ID + " = " + groupId) > 0;
    }

    public static List<GroupMember> addGroupMembers(JSONArray members, long chatId, boolean notify) throws Exception {
        List<GroupMember> groupMembers = Lists.newArrayList();
        for (int i = 0; i < members.length(); i++) {
            groupMembers.add(addGroupMember(members.getJSONObject(i), chatId, notify));
        }
        return groupMembers;
    }

    public static GroupMember addGroupMember(JSONObject member, long chatId, boolean isLast) throws Exception {
        member.put("group_id", chatId);
        return GroupMember.insertOrUpdate(sDm, member, isLast);
    }

    public static GroupMember getGroupMember(long memberId) {
        return GroupMember.findOneByGlobalId(sDm, memberId);
    }

    public static GroupMember[] getGroupMembers(long[] memberIds) {
        final GroupMember[] groupMembers = new GroupMember[memberIds.length];
        for (int i = 0; i < memberIds.length; i++) {
            groupMembers[i] = getGroupMember(memberIds[i]);
        }
        return groupMembers;
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

    public static GroupMember[] getOtherGroupMembers(long chatId) {
        final GroupMember[] allGms = getGroupMembers(chatId);
        final GroupMember[] otherGms = new GroupMember[allGms.length - 1];
        int j = 0;
        for (GroupMember gm : allGms) {
            if (gm.getGlobalId() != getAccountUserId()) {
                otherGms[j] = gm;
                j++;
            }
        }
        return otherGms;
    }

    public static boolean removeGroupMember(long chatId, long memberId) {
        return GroupMember.deleteWhere(sDm, GroupMember.F_GROUP_ID + " = " + chatId + " AND " + GroupMember.F_GLOBAL_ID
                + " = " + memberId) > 0;
    }

    public static long[] getOtherGroupMemberIds(long chatId) {
        GroupMember[] members = getGroupMembers(chatId);
        long[] ids = new long[members.length];
        for (int i = 0; i < members.length; i++) {
            if (members[i].getGlobalId() != getAccountUserId()) {
                ids[i] = members[i].getGlobalId();
            }
        }
        return ids;
    }

    public static boolean messageExists(JSONObject json) {
        try {
            return Message.findOneByGlobalId(sDm, json.getLong("id")) != null;
        } catch (JSONException e) {
            return false;
        }
    }

    public static List<Message> addGroupMessages(JSONArray messages) throws Exception {
        List<Message> groupMessages = Lists.newArrayList();
        for (int i = 0; i < messages.length(); i++) {
            addGroupMessage(messages.getJSONObject(i), i == messages.length() - 1);
        }
        return groupMessages;
    }

    public static List<Message> addGroupMessages(JSONArray messages, boolean notify) throws Exception {
        List<Message> groupMessages = Lists.newArrayList();
        for (int i = 0; i < messages.length(); i++) {
            groupMessages.add(addGroupMessage(messages.getJSONObject(i), notify));
        }
        return groupMessages;
    }

    public static Message addGroupMessage(JSONObject message, boolean isLast) throws Exception {
        return messageExists(message) ? null : Message.insertOrUpdate(sDm, message, isLast);
    }

    public static Cursor getMessagesCursor(long chatId, int limit) {
        String where = Message.F_GROUP_ID + " = " + chatId;
        long totalNum = Message.getCount(sDm, where);
        long offset = Math.max(totalNum - limit, 0);
        return sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES, where, null, null, null,
                Message.F_DATE + " ASC", offset + "," + limit);
    }

    public static Cursor getMessagesCursor(long chatId, Message before, int limit) {
        String where = Message.F_GROUP_ID + " = " + chatId + " AND " + Message.F_DATE + " < " + String.valueOf(before
                .getDate());
        long totalNum = Message.getCount(sDm, where);
        long offset = Math.max(totalNum - limit, 0);
        return sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES, where, null, null, null,
                Message.F_DATE + " ASC", offset + "," + limit);
    }

    public static List<Message> getMessagesBySideConvo(long sideConvoId) {
        ArrayList<Message> objList = new ArrayList<Message>();
        Cursor c = sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES,
                Message.F_MESSAGE_TYPE_ID + " = " + ConvoType.SIDE_CONVO.getValue() + " AND " + Message.F_TABLE_ID +
                        " = " + sideConvoId, null, null, null, null
        );
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

    public static Cursor getMessagesCursor(long chatId, long convoId, int limit) {
        String where = Message.F_GROUP_ID + " = " + chatId + " AND " + Message.F_TABLE_ID + " = " + convoId;
        long totalNum = Message.getCount(sDm, where);
        long offset = Math.max(totalNum - limit, 0);
        return sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES, where, null, null, null,
                Message.F_DATE + " ASC", offset + "," + limit);
    }

    public static Cursor getMessagesCursor(long chatId, long convoId, Message before, int limit) {
        String where = Message.F_GROUP_ID + " = " + chatId + " AND " + Message.F_TABLE_ID + " = " + convoId + " AND " +
                Message.F_DATE + " < " + before.getDate();
        long totalNum = Message.getCount(sDm, where);
        long offset = Math.max(totalNum - limit, 0);
        return sDm.getReadableDatabase().query(Message.TABLE_NAME, Message.ALL_COLUMN_NAMES, where, null, null, null,
                Message.F_DATE + " ASC", offset + "," + limit);
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

    public static long getMostRecentMessageId(long chatId) {
        Cursor cursor = sDm.getReadableDatabase().query(Message.TABLE_NAME, new String[]{Group.F_GLOBAL_ID},
                Message.F_GROUP_ID + " = " + chatId, null, null, null, Message.F_DATE + " DESC", "1");
        cursor.moveToFirst();
        long id;
        if (cursor.isAfterLast()) {
            id = -1;
        } else {
            id = cursor.getLong(cursor.getColumnIndex(Message.F_GLOBAL_ID));
        }
        cursor.close();
        return id;
    }

    public static long getLeastRecentMessageId(long chatId) {
        Cursor cursor = sDm.getReadableDatabase().query(Message.TABLE_NAME, new String[]{Group.F_GLOBAL_ID},
                Message.F_GROUP_ID + " = " + chatId, null, null, null, Message.F_DATE + " ASC", "1");
        cursor.moveToFirst();
        long id;
        if (cursor.isAfterLast()) {
            id = -1;
        } else {
            id = cursor.getLong(cursor.getColumnIndex(Message.F_GLOBAL_ID));
        }
        cursor.close();
        return id;
    }

    public static int collaspeSideConvoMessages(long sideConvoId) {
        List<Message> sideConvoMessages = getMessagesBySideConvo(sideConvoId);
        for (Message m : sideConvoMessages) {
            m.setMessageTypeId(ConvoType.MAIN_CHAT.getValue());
            m.setTableId(0l);
            m.save();
        }
        return sideConvoMessages.size();
    }

    public static int removeWhisperMessages(long whisperId) {
        return BaseMessage.deleteWhere(sDm, Message.F_MESSAGE_TYPE_ID + " = " + ConvoType.WHISPER.getValue() + " AND " +
                Message.F_TABLE_ID + " = " + whisperId);
    }

    public static JSONObject bundleToJson(Bundle bundle) throws JSONException {
        return new JSONObject(bundle.getString(LOGIN_JSON));
    }
}
