package com.gjk.chassip.net;

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetSpecificGroupTask  extends MiluHTTPTask {
	
	private long mID;
	private long mGroupID;

	public GetSpecificGroupTask(Context ctx, HTTPTaskListener listener, long id, long group_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mID = id;
		mGroupID = group_id;
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
		payload.put("id", mID);
		payload.put("group_id", mGroupID);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getGroup";
	}

}
