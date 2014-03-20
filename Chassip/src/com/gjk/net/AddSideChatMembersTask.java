package com.gjk.net;

import java.util.Arrays;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class AddSideChatMembersTask extends MiluHTTPTask {
		
		private long mSideChatID;
		private long [] mRecipients;
	public AddSideChatMembersTask(Context ctx, HTTPTaskListener listener, long side_chat_id, long [] recipients) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSideChatID = side_chat_id;
		mRecipients = Arrays.copyOf(recipients, recipients.length);
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
		payload.put("recipients", mRecipients);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/addSideChatMembers";
	}

}
