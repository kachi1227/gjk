package com.gjk.net;

import java.util.Arrays;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class RemoveWhisperMembersTask extends MiluHTTPTask {
	
	private long mWhisperID;
	private long [] mMembers;

	public RemoveWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisper_id, long [] members) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		
		mWhisperID = whisper_id;
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
		payload.put("whisper_id", mWhisperID);
		payload.put("members", mMembers);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/removeWhisperMembers";
	}

}
