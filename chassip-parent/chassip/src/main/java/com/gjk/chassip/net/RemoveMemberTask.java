package com.gjk.chassip.net;

/*---------------------------------------------
Removing Member


API endpoint:  http://skip2milu.com/gjk/api/removeMembers

Sample JSON request:
{"group_id":1, "members": [2, 4, 5]}

Sample JSON response:
{"success":true} (if we were able to successfully remove members or if we tried to remove a member that wasnt in group)
--------------------------------------------*/

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;
import java.util.Arrays;


public class RemoveMemberTask extends MiluHTTPTask{
	
	private long mGroupID;
	private long[] mRemovedIDs;

	public RemoveMemberTask(Context ctx, HTTPTaskListener listener, long groupID, long[] removedIDs) {
		super(ctx, listener);
		mGroupID = groupID;
		mRemovedIDs = Arrays.copyOf(removedIDs, removedIDs.length);
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
		for(long id : mRemovedIDs){
			ids.put(id);
		}//end convert array
		
		payload.put("members", mRemovedIDs);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/removeMembers";
	}

}
