package com.gjk.database.objects;

import org.json.JSONObject;

import com.gjk.database.objects.base.BaseMessage;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public class Message extends BaseMessage {
	public static final int ROW_LIMIT = 500;
	public static final String TYPE_INTERACTION = "INTERACTION";
	public static final String TYPE_LIVE_STREAM = "LIVE_STREAM";
	
	
	public static Message insertOrUpdate(SQLiteOpenHelper dbm, JSONObject json) throws Exception {
		long id = json.getLong("id");
		Message message = findOneByGlobalId(dbm, id);
		if (message == null) {
			message = new Message(dbm);
		}

		message.setGlobalId(id);
		if(!json.isNull("group_id"))
			message.setGroupId(json.getLong("group_id"));
		if(!json.isNull("sender_id"))
			message.setSenderId(json.getLong("sender_id"));
		if(!json.isNull("sender_first_name"))
			message.setSenderFirstName(json.getString("sender_first_name"));
		if(!json.isNull("sender_last_name"))
			message.setSenderLastName(json.getString("sender_last_name"));
		if(!json.isNull("sender_image"))
			message.setSenderImageUrl(json.getString("sender_image"));
		if(!json.isNull("recipient_id"))
			message.setRecipientId(json.getLong("recipient_id"));
		if(!json.isNull("recipient_first_name"))
			message.setRecipientFirstName(json.getString("recipient_first_name"));
		if(!json.isNull("recipient_last_name"))
			message.setRecipientLastName(json.getString("recipient_last_name"));
		if(!json.isNull("topic_id"))
			message.setTopicId(json.getLong("topic_id"));
		if(!json.isNull("topic_name"))
			message.setTopicName(json.getString("topic_name"));
		if(!json.isNull("content"))
			message.setContent(json.getString("content"));
		if(!json.isNull("attachment"))
			message.setAttachment(json.getString("attachment"));
		if(!json.isNull("message_type_id"))
			message.setMessageTypeId(json.getInt("message_type_id"));
		if(!json.isNull("table_id"))
			message.setTableId(json.getLong("table_id"));
		if(!json.isNull("date"))
			message.setDate(json.getLong("date"));

		message.save();
		return message;
	}

	
	public Message(SQLiteOpenHelper dbm, Cursor c, boolean skipOk) {
		super(dbm, c, skipOk);
	}

	public Message(SQLiteOpenHelper dbm, String jsonString, boolean skipOk) {
		super(dbm, jsonString, skipOk);
	}

	public Message(SQLiteOpenHelper dbm, JSONObject obj, boolean skipOk) {
		super(dbm, obj, skipOk);
	}

	public Message(SQLiteOpenHelper dbm) {
		super(dbm);
	}
	
	@Override
	protected void onBeforeDelete() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAfterDelete() {
		// TODO Auto-generated method stub

	}

	public static void deleteOlderPostItems(SQLiteOpenHelper dbm, long limit) {
//		int totalCount = dbm.getWritableDatabase().delete(TABLE_NAME, F_GLOBAL_ID + " NOT IN (SELECT " + F_GLOBAL_ID +
//				" FROM " + TABLE_NAME + " ORDER BY " + F_DATE + " DESC limit " + limit + ")", null);
	}
}
