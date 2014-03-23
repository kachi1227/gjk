package com.gjk.chassip.net;

/*----------------------------
 Adding Member

API endpoint:  http://skip2milu.com/gjk/api/addMembers

Sample JSON request:
{"group_id":1, "recipients": [2, 4, 5]}

Sample JSON response:
{"success":true} (if we were able to successfully add all members that we attempted to add)

------------------------------------------*/

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;
import java.util.Arrays;


public class AddMemberTask extends MiluHTTPTask{
	private long mGroupID;
	private long[] mInvitedIDs;

	public AddMemberTask(Context ctx, HTTPTaskListener listener, long groupID, long[] invitedIDs) {
		super(ctx, listener);
		
		mGroupID = groupID;
		mInvitedIDs = Arrays.copyOf(invitedIDs, invitedIDs.length);
		execute();
		
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {

		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json);

	}

	@Override
	public JSONObject getPayload() throws Exception {
		
		JSONObject payload = new JSONObject();
		payload.put("group_id", mGroupID);
		
		//convert long[] to JSONArray
		JSONArray ids = new JSONArray();
		for(long id : mInvitedIDs){
			ids.put(id);
		}//end convert array
		
		payload.put("recipients", mInvitedIDs);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/addMembers";
	}

}
