package com.gjk.net;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;
import java.util.Arrays;

public class GetGroupMembersTask extends MiluHTTPTask{
	
	private long mGroup_id;

	public GetGroupMembersTask(Context ctx, HTTPTaskListener listener, long group_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mGroup_id = group_id;
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
		payload.put("group_id", mGroup_id);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getGroupMembers";
	}

}
