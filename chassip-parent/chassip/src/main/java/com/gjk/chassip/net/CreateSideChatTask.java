package com.gjk.chassip.net;

/*---------------------------------------------

Create Side Chat

API endpoint: http://skip2milu.com/gjk/api/createSideChat

Sample JSON request:
Required fields:
{"group_id":1, "creator_id": 4,  "members": [5, 6]}



Optional fields:
"name" - the name of the side chat

Sample JSON response:

{"side_chat":{"id":"1","group_id":"1","name":"","creator_id":"4","first_name":"Kachi","last_name":"Nwaobasi", "collapsed":0},"success":true}

-------------------------------------------------*/


import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;


public class CreateSideChatTask extends MiluHTTPTask {
	//members
	private long mGroup_id;
	private long mCreator_id;
	private long [] mMembers;
	private String mName = null;
	//end members
	
	//constructor with null for optional field
	public CreateSideChatTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long [] members) {
		// TODO Auto-generated constructor stub
		this(ctx, listener, group_id, creator_id, members, null);
	}//end constructor sans optional
	
	//main constructor all fields
	public CreateSideChatTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long [] members, String name) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mGroup_id = group_id;
		mCreator_id = creator_id;
		mMembers = Arrays.copyOf(members, members.length);
		
		mName = name;
		execute();
	}//end main constructor 

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("side_chat"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("group_id", mGroup_id);
		payload.put("creator_id", mCreator_id);
<<<<<<< HEAD
		JSONArray ids = new JSONArray();
		for (long id : mMembers) {
			ids.put(id);
		}
=======
		
		//change long [] to JSONarray
		JSONArray ids = new JSONArray();
		for(long id : mMembers){
			ids.put(id);
		}//end change array
		
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f
		payload.put("members", ids);
		if(mName != null)
			payload.put("name", mName);
		
		return payload;
	}//end getPayload

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/createSideChat";
	}//end getUri

}
