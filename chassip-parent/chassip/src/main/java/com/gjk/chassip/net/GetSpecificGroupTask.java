package com.gjk.chassip.net;

<<<<<<< HEAD
=======
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

>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetSpecificGroupTask  extends MiluHTTPTask {
	
	private long mID;
	private long mGroupID;

	public GetSpecificGroupTask(Context ctx, HTTPTaskListener listener, long id, long group_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mID = id;
		mGroupID = group_id;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("group"));

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("id", mID);
		payload.put("group_id", mGroupID);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getGroup";
	}

}
