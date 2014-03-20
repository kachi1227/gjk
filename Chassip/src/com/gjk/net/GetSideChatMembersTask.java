package com.gjk.net;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

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
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("sidechat"));
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
