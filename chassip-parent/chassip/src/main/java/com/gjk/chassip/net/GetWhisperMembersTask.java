package com.gjk.chassip.net;

/*-------------------------------------------------------------------------------

Get Whisper Members

API endpoint: http://skip2milu.com/gjk/api/getWhisperMembers

Sample JSON request:
Required fields:
 {"whisper_id":2}

Sample JSON response:

{"members":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"6","first_name":"Kachi","last_name":"Nwaobasi","image":"resources\/6\/images\/img20140213064951.jpg"}],"success":true}

----------------------------------------------------------------*/

import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetWhisperMembersTask extends MiluHTTPTask {
	
	private long mWhisperID;

	public GetWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisper_id) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mWhisperID = whisper_id;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
<<<<<<< HEAD
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONArray("members"));
=======
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("members"));
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("whisper_id", mWhisperID);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/getWhisperMembers";
	}

}
