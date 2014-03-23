package com.gjk.chassip.net;

import java.util.Arrays;

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class RemoveSideChatMembersTask extends MiluHTTPTask{
	
	private long mSideChatID;
	private long [] mMembers;

	public RemoveSideChatMembersTask(Context ctx, HTTPTaskListener listener, long side_chat_id, long [] members) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSideChatID = side_chat_id;
		mMembers = Arrays.copyOf(members, members.length);
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json);
 
	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("side_chat_id", mSideChatID);
		payload.put("members", mMembers);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/removeSideChatMembers";
	}

}
