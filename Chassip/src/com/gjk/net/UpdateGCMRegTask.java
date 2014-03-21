package com.gjk.net;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class UpdateGCMRegTask extends MiluHTTPTask {

	private long mID;
	private String mRegistrationID;
	private String mPhoneType;
	
	public UpdateGCMRegTask(Context ctx, HTTPTaskListener listener, long id, String registration_id,
			String phone_type) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		
		mID = id;
		mRegistrationID = registration_id;
		mPhoneType = phone_type;
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
		payload.put("registration_id", mRegistrationID);
		payload.put("phone_type", mPhoneType);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/updateGCMRegistration";
	}

}
