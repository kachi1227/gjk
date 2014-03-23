package com.gjk.chassip.net;
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

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class CreateWhisperTask extends MiluHTTPTask{
	
	private long mGroupID;
	private long mCreatorID;
	private long [] mMembers;
	
	//optional field
	private String mName;
	
	public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long [] members) {
			
		this(ctx, listener, group_id, creator_id, members, null);
		
	}


	public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long [] members, String name) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mGroupID = group_id;
		mCreatorID = creator_id;
		mMembers = Arrays.copyOf(members, members.length);
		mName = name;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json.getJSONObject("whisper"));

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("group_id", mGroupID);
		payload.put("creator_id", mCreatorID);
		if(mName != null){ 
			payload.put("name", mName);
			}
		
		//change long [] to JSONarray
		JSONArray ids = new JSONArray();
		for(long id : mMembers){
			ids.put(id);
		}//end change
		
		payload.put("members", ids);
				
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/createWhisper";
	}

}
