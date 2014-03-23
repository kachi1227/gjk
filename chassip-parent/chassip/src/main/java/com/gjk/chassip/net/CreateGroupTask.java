package com.gjk.chassip.net;

/*--------------------------------------------
Creating Group

API endpoint: http://skip2milu.com/gjk/api/createGroup

Sample JSON request:
{"name":"GJK", "creator_id": 1}

Sample JSON response:

{"group":{"id": "1", "name":"GJK", "creator_id":"1", "first_name":"Kachi", "last_name":"Nwaobasi", "image":"resources\/groups\/group-1\/img20140224010119.png"},"success":true}
-----------------------------------------------
*/

import java.util.HashMap;

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;



public class CreateGroupTask extends MiluHTTPTask {
	private String mNameOfGroup;//group name
	private long mUserID; //creator_id
	private HashMap<String, Object> mFieldMapping;
	
	public CreateGroupTask(Context ctx, HTTPTaskListener listener, long UserID, String nameOfGroup, HashMap<String, Object> fieldMapping) {
		super(ctx, listener);
		
		mNameOfGroup = nameOfGroup;
		mUserID = UserID;
		mFieldMapping = fieldMapping; 
		extractFiles(mFieldMapping, true);
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {

		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("group"));

	}

	@Override
	public JSONObject getPayload() throws Exception {
		
		JSONObject payload = new JSONObject();
		payload.put("name", mNameOfGroup);
		payload.put("creator_id", mUserID);
		
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/createGroup";
	}

}
