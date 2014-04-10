package com.gjk.chassip.net;

/*---------------------------------------------------------

Get Side Chat Members

API endpoint: http://skip2milu.com/gjk/api/getSideChatMembers

Sample JSON request:
Required fields:
 {"side_chat_id":2}

Sample JSON response:

{"members":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"5","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"7","first_name":"Tukach","last_name":"Shakur","image":"resources\/7\/images\/img20140213181223.jpg"}],"success":true}

----------------------------------------------------------------*/

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetSideChatMembersTask extends MiluHTTPTask {
	
	private long mSideChatID;

	public GetSideChatMembersTask(Context ctx, HTTPTaskListener listener, long side_chat_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSideChatID = side_chat_id;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONArray("members"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("side_chat_id", mSideChatID);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getSideChatMembers";
	}

}
