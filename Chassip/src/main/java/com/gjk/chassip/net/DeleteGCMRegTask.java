package com.gjk.chassip.net;

/*---------------------------------------
Delete GCM Registration

API endpoint: http://skip2milu.com/gjk/api/updateGCMRegistration

Sample JSON request:
Deleting GCM - Required fields:
{"id":4, "delete":true}


Sample JSON response:
{"success":true}
----------------------------------------*/

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;



public class DeleteGCMRegTask extends MiluHTTPTask {
	
	private long mID;
	private boolean mIsDelete;

	public DeleteGCMRegTask(Context ctx, HTTPTaskListener listener, long id, boolean _delete) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mID = id;
		mIsDelete = _delete;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getBoolean("success"));

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("id", mID);
		payload.put("delete", mIsDelete);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/updateGCMRegistration";
	}

}
