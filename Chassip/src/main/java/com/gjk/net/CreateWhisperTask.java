package com.gjk.net;
/*--------------------------------------------------------------------

Create Whisper

API endpoint: http://skip2milu.com/gjk/api/createWhisper

Sample JSON request:
Required fields:
{"group_id":1, "creator_id": 4,  "members": [7]}



Optional fields:
"name" - the name of the whisper

Sample JSON response:

{"whisper":{"id":"1","group_id":"1","name":"","creator_id":"4","first_name":"Kachi","last_name":"Nwaobasi"},"success":true}

-----------------------------------------------------------------------*/


import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class CreateWhisperTask extends MiluHTTPTask {
	// members
	private long mGroup_id;
	private long mCreator_id;
	private long[] mMembers;
	private String mName = null;

	// end members

	// constructor with null for optional field
	public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long[] members) {
		// TODO Auto-generated constructor stub
		this(ctx, listener, group_id, creator_id, members, null);
	}// end constructor sans optional

	// main constructor all fields
	public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long [] members, String name) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mGroup_id = group_id;
		mCreator_id = creator_id;
		mMembers = Arrays.copyOf(members, members.length);
		mName = name;
		execute();
	}// end main constructor

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("whisper"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("group_id", mGroup_id);
		payload.put("creator_id", mCreator_id);
		JSONArray ids = new JSONArray();
		for (long id : mMembers) {
			ids.put(id);
		}
		payload.put("members", ids);
		if (mName != null)
			payload.put("name", mName);

		return payload;
	}// end getPayload

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/createWhisper";
	}// end getUri

}
