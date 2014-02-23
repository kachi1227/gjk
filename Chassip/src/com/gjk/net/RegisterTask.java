package com.gjk.net;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class RegisterTask extends MiluHTTPTask {
	private String mFirstName;
	private String mLastName; 
	private String mEmail;
	private String mPassword;
	private HashMap<String, Object> mFieldMapping;

	public RegisterTask(Context ctx, HTTPTaskListener listener, String firstName, String lastName,
			String email, String password, HashMap<String, Object> fieldMapping) {
		super(ctx, listener);
		mFirstName = firstName;
		mLastName = lastName;
		mEmail = email;
		mPassword = password;
		mFieldMapping = fieldMapping; 
		extractFiles(mFieldMapping, true);
		execute();
		
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("user"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("first name", mFirstName);
		payload.put("last name", mLastName);
		payload.put("password", mPassword);
		payload.put("email", mEmail);
		Set<String> keys = mFieldMapping.keySet();	
		for(String key : keys){
			payload.put(key, mFieldMapping.get(key));
		}
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/register";
	}
	
	

}
