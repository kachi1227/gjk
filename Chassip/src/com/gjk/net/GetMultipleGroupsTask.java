package com.gjk.net;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetMultipleGroupsTask extends MiluHTTPTask {
	
	private long mID;

	public GetMultipleGroupsTask(Context ctx, HTTPTaskListener listener, long id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mID = id;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("groups"));

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("id", mID);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getGroups";
	}

}
