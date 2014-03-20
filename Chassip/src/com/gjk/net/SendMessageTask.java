package com.gjk.net;

import java.util.HashMap;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class SendMessageTask extends MiluHTTPTask {
	
	private int mSender_id = -1;
	private int mGroup_id = -1;
	private String mContent;// text of the message 
	
	
	//optional fields
	private HashMap<String, Object> mFieldMapping;//attachments to message, if any
	private long mRecipient_id = -1;//if not group message 
	private int mTopic_id = -1; //id of current topic of discussion
	private int mMessage_type_id = -1; // message type 1:standard 2: Sideconvo 3:whisper
	private long mTable_id = -1;//requisite while message type != 1 or api return error
	//end optional fields 
	
	
	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content) {
		this(ctx, listener, sender_id, group_id, content, null, -1, -1, -1, -1);
		// TODO Auto-generated constructor stub
		mSender_id = sender_id;
		mGroup_id = group_id;
		mContent = content;
		//mFieldMapping = fieldMapping;
		//extractFiles(mFieldMapping, true);
		
		//mRecipient_id = recipient_id;
		//mTopic_id = topic_id;
		//mMessage_type_id = message_type_id;
		//mTable_id = table_id;
		execute();
	}

	//Optional overloaded methods for later
	
	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping) {
		this(ctx, listener, sender_id, group_id, content, fieldMapping, -1, -1, -1, -1);
		// TODO Auto-generated constructor stub
		execute();
	}

	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping, long recipient_id) {

		this(ctx, listener, sender_id, group_id, content, fieldMapping, recipient_id, -1, -1, -1);
		// TODO Auto-generated constructor stub
		execute();
	}

	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping, long recipient_id, int topic_id) {
		
		// TODO Auto-generated constructor stub
		this(ctx, listener, sender_id, group_id, content, fieldMapping, recipient_id, topic_id, -1, -1);

		execute();
	}

	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping, long recipient_id, int topic_id,
			int message_type_id, int table_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSender_id = sender_id;
		mGroup_id = group_id;
		mContent = content;
		mRecipient_id = recipient_id;
		if(fieldMapping != null)
			mFieldMapping.putAll(fieldMapping);
		mTopic_id = topic_id;
		mMessage_type_id = message_type_id;
		mTable_id = table_id;
		
		//extractFiles(mFieldMapping, true);
		//if(mFieldMapping != null)
			//payload.put("attachment", mFieldMapping);
		if(mRecipient_id != -1)
			mFieldMapping.put("recipient_id",mRecipient_id);
		if(mTopic_id != -1)
			mFieldMapping.put("topic_id", mTopic_id);
		if(mMessage_type_id != -1)
			mFieldMapping.put("message_type_id", mMessage_type_id);
		
		if(mTable_id != -1)
			mFieldMapping.put("table_id", mTable_id);
		//mRecipient_id = recipient_id;
		//mFieldMapping.put("topic_id",topic_id);//mTopic_id = topic_id;
		//mFieldMapping.put("message_type_id", message_type_id);//mMessage_type_id = message_type_id;
		//mFieldMapping.put("table_id", table_id);//mTable_id = table_id;
		extractFiles(mFieldMapping, true);
		execute();
	}
///end optional methods for later
	
	
	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("message"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("sender_id", mSender_id);
		payload.put("content", mContent);
		payload.put("group_id", mGroup_id);
		if(mRecipient_id != -1)
			payload.put("recipient_id",mRecipient_id);
		if(mTopic_id != -1)
			payload.put("topic_id", mTopic_id);
		if(mMessage_type_id != -1)
			payload.put("message_type_id", mMessage_type_id);
		if(mFieldMapping != null)
			payload.put("attachment", mFieldMapping);
		if(mTable_id != -1)
			payload.put("table_id", mTable_id);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/sendMessage";
	}

}
