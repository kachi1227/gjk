package com.gjk.net;

/*---------------------------------------------

Get Group

API endpoint: http://skip2milu.com/gjk/api/getGroup

Sample JSON request:
Required fields:
{"id":4, "group_id": 1}


Sample JSON response:

IMPORTANT: The ids & names of whispers and sidechats will be returned every time a 
call is made to getGroups OR getGroup. The format for every side chat or whisper that a 
user can see will be "side_chat_id:side_chat_name" OR "whisper_id:whisper_name", respectively. See below

{"group":{"id":"1","name":"GJK","image":"","creator_id":"4","creator_name":"Kachi Nwaobasi",
"side_chats":"2:side_chat-2|3:Hello WOrld|4:Home|5:Sweet|6:Home","whispers":"1:whisper-1|2:Knock, 
Knock|3:Who's There"},"success":true}

-------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class GetGroupTask extends MiluHTTPTask {

    private long mId;
    private long mGroupID;

    public GetGroupTask(Context ctx, HTTPTaskListener listener, long id, long groupId) {
        super(ctx, listener);
        mId = id;
        mGroupID = groupId;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("group"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", mId);
        payload.put("group_id", mGroupID);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/getGroup";
    }

}
