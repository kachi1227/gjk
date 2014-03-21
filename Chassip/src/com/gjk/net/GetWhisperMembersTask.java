package com.gjk.net;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetWhisperMembersTask extends MiluHTTPTask {
	
	private long mWhisperID;

	public GetWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisper_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mWhisperID = whisper_id;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("whisper"));

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("whisper_id", mWhisperID);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getWhisperMembers";
	}

}
