package com.gjk.chassip.net;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;
import com.google.common.collect.Maps;

import android.content.Context;

/*Creating Group
=======
/*--------------------------------------------
Creating Group
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f

API endpoint: http://skip2milu.com/gjk/api/createGroup

Sample JSON request:
{"name":"GJK", "creator_id": 1}

Sample JSON response:

{"group":{"id": "1", "name":"GJK", "creator_id":"1", "first_name":"Kachi", "last_name":"Nwaobasi", "image":"resources\/groups\/group-1\/img20140224010119.png"},"success":true}
-----------------------------------------------
*/



public class CreateGroupTask extends MiluHTTPTask {
	private String mNameOfGroup;//group name
	private long mUserID; //creator_id
	private HashMap<String, Object> mFieldMapping;
	private long[] mMembers;

	public CreateGroupTask(Context ctx, HTTPTaskListener listener, long UserID, String nameOfGroup, HashMap<String, Object> fieldMapping) {
		this(ctx, listener, UserID, nameOfGroup, null, fieldMapping);
	}
	
	public CreateGroupTask(Context ctx, HTTPTaskListener listener, long UserID, String nameOfGroup, long[] members, HashMap<String, Object> fieldMapping) {
		super(ctx, listener);
		mNameOfGroup = nameOfGroup;
		mUserID = UserID;
		mFieldMapping = Maps.newHashMap(fieldMapping);
		mMembers = members;
		extractFiles(fieldMapping, true);
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
		JSONArray ids = new JSONArray();
		for (long id : mMembers) {
			ids.put(id);
		}
		payload.put("recipients", ids);
		Set<String> keys = mFieldMapping.keySet();	
		for(String key : keys){
			payload.put(key, mFieldMapping.get(key));
		}
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/createGroup";
	}

}
