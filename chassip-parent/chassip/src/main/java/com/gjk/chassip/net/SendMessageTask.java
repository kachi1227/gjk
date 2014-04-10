package com.gjk.chassip.net;

import java.util.HashMap;

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;


import android.content.Context;

public class SendMessageTask extends MiluHTTPTask {
	
	private long mSenderId;
	private long mGroupId;
	private String mContent;// text of the message 
	
	
	//optional fields
	private HashMap<String, Object> mFieldMapping;//attachments to message, if any
	private Long mRecipientId;//if not group message 
	private Long mTopicId; //id of current topic of discussion
	private Integer mMessageTypeId; // message type 1:standard 2: Sideconvo 3:whisper
	private Long mTableId;//requisite while message type != 1 or api return error
	//end optional fields 
	
	
	public SendMessageTask(Context ctx, HTTPTaskListener listener, long senderId, long groupId,
			String content) {
		this(ctx, listener, senderId, groupId, content, null, null, null, null, null);
	}
	
	public SendMessageTask(Context ctx, HTTPTaskListener listener, long senderId, long groupId, int type, long tableId,
			String content) {
		this(ctx, listener, senderId, groupId, content, null, null, null, type, tableId);
	}

	public SendMessageTask(Context ctx, HTTPTaskListener listener, long senderId, long groupId,
			String content, HashMap<String, Object> fieldMapping, Long recipientId, Long topicId,
			Integer messageTypeId, Long tableId) {
		super(ctx, listener);
		mSenderId = senderId;
		mGroupId = groupId;
		mContent = content;
		mRecipientId = recipientId;
		mTopicId = topicId;
		mMessageTypeId = messageTypeId;
		mTableId = tableId;
		extractFiles(mFieldMapping, true);
		execute();
	}

	
	
	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("message"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("sender_id", mSenderId);
		payload.put("content", mContent);
		payload.put("group_id", mGroupId);
		payload.put("recipient_id",mRecipientId);
		payload.put("topic_id", mTopicId);
		payload.put("message_type_id", mMessageTypeId);
		payload.put("table_id", mTableId);
		return payload;
	}

	@Override
	public String getUri() {
		return "api/sendMessage";
	}

}
