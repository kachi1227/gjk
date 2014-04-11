package com.gjk.net;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class NotifyWhisperInviteesTask extends MiluHTTPTask {
	
	private long mID;
	private long mWhisperID;
	private long [] mRecipients;

	public NotifyWhisperInviteesTask(Context ctx, HTTPTaskListener listener, long id, long whisper_id, long [] recipients) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mID = id;
		mWhisperID = whisper_id;
		mRecipients = recipients;
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
		payload.put("id", mID);
		payload.put("whisper_id", mWhisperID);
		JSONArray ids = new JSONArray();
		for(long id : mRecipients){
			ids.put(id);
		}
		payload.put("recipients", ids);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/notifyWhisperInvitees";
	}

}