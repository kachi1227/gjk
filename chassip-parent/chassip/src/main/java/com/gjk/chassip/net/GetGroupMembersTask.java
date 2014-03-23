package com.gjk.chassip.net;

/*---------------------------------------------

Get Group Members

API endpoint: http://skip2milu.com/gjk/api/getGroupMembers

Sample JSON request:
Required fields:
{"group_id":1}


Sample JSON response:

{"members":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"5","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"6","first_name":"Kachi","last_name":"Nwaobasi","image":"resources\/6\/images\/img20140213064951.jpg"},{"id":"7","first_name":"Tukach","last_name":"Shakur","image":"resources\/7\/images\/img20140213181223.jpg"}],"success":true}

---------------------------------------------*/

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

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
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONArray("members"));

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
