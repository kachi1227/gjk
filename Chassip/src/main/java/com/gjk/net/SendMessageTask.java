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

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;
import com.google.common.collect.Maps;

import org.json.JSONObject;

import java.util.HashMap;

public class SendMessageTask extends MiluHTTPTask {

    private long mSenderId;
    private long mGroupId;
    private String mContent;// text of the message

    //optional fields
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

    public SendMessageTask(Context ctx, HTTPTaskListener listener, long senderId, long groupId, int type, long tableId,
                           String content, HashMap<String, Object> fieldMapping) {
        this(ctx, listener, senderId, groupId, content, fieldMapping, null, null, type, tableId);
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
        extractFiles(fieldMapping, false);
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
                                                   JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("message"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("sender_id", mSenderId);
        payload.put("content", mContent);
        payload.put("group_id", mGroupId);
        payload.put("recipient_id", mRecipientId);
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
