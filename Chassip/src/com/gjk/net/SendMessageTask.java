package com.gjk.net;

/*-------------------------------------
Sending Message

API endpoint: http://skip2milu.com/gjk/api/sendMessage

Sample JSON request:
Required fields:
{"sender_id":4, "group_id": 1, "content": "Hello world"}

Optional fields:
- recipient_id (long): The id of the person that the specific message is geared toward (if any). 
This is for a later feature that we discussed. Most likely will not be in the prototype

- topic_id (integer): The id of the current topic of discussion

- message_type_id (integer): id corresponding to the various message types 
( (1) Standard, (2) Side Conversation, (3) Whisper). If the field is not included in the JSON,
 then the message is assumed to be Standard.

- table_id (long): the id of this particular side conversation or whisper. 
If message_type_id is not equal to 1 (Standard), then this value must absolutely be specified 
else the API will return a "Missing fields" error



Sample JSON response:

{"message":{"id":"5","group_id":"1","sender_id":"4","first_name":"Kachi","last_name":"Nwaobasi",
"image":null,"recipient_id":null,"topic_id":null,"content":"",
"attachment":"resources\/groups\/group-0\/img20140228184604.png","message_type_id":"1","table_id":null,
"date":"1393613164000"},"success":true}

--------------------------------------------------------*/

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
		super(ctx, listener);
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
	/*
	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSender_id = sender_id;
		mGroup_id = group_id;
		mContent = content;
		mFieldMapping = fieldMapping;
		extractFiles(mFieldMapping, true);
		
		//mRecipient_id = recipient_id;
		//mTopic_id = topic_id;
		//mMessage_type_id = message_type_id;
		//mTable_id = table_id;
		execute();
	}

	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping, long recipient_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSender_id = sender_id;
		mGroup_id = group_id;
		mContent = content;
		mFieldMapping = fieldMapping;
		extractFiles(mFieldMapping, true);
		
		mRecipient_id = recipient_id;
		//mTopic_id = topic_id;
		//mMessage_type_id = message_type_id;
		//mTable_id = table_id;
		execute();
	}

	public SendMessageTask(Context ctx, HTTPTaskListener listener, int sender_id, int group_id,
			String content,HashMap<String, Object> fieldMapping, long recipient_id, int topic_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSender_id = sender_id;
		mGroup_id = group_id;
		mContent = content;
		mFieldMapping = fieldMapping;
		extractFiles(mFieldMapping, true);
		
		mRecipient_id = recipient_id;
		mTopic_id = topic_id;
		//mMessage_type_id = message_type_id;
		//mTable_id = table_id;
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
		mFieldMapping = fieldMapping;
		extractFiles(mFieldMapping, true);
		
		mRecipient_id = recipient_id;
		mTopic_id = topic_id;
		mMessage_type_id = message_type_id;
		mTable_id = table_id;
		execute();
	}
*///end optional methods for later
	
	
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
