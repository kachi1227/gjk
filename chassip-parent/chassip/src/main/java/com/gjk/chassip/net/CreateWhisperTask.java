package com.gjk.chassip.net;

import java.util.Arrays;

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class CreateWhisperTask extends MiluHTTPTask{
	
	private long mGroupID;
	private long mCreatorID;
	private long [] mMembers;

	public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long [] members) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mGroupID = group_id;
		mCreatorID = creator_id;
		mMembers = Arrays.copyOf(members, members.length);
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
		payload.put("group_id", mGroupID);
		payload.put("creator_id", mCreatorID);
		payload.put("members", mMembers);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/createWhisper";
	}

}
