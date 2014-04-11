package com.gjk.chassip.net;

/*----------------------------------------------------------------

Remove Whisper Members

API endpoint: http://skip2milu.com/gjk/api/removeWhisperMembers

Sample JSON request:
Required fields:
{"whisper_id":1, "members": [7]}

Sample JSON response:

{"success":true} (if we were able to successfully remove members or if we tried to remove a member that wasnt in whisper)

--------------------------------------------------------------------
*/

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class RemoveWhisperMembersTask extends MiluHTTPTask {
	
	private long mWhisperID;
	private long [] mMembers;

	public RemoveWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisper_id, long [] members) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		
		mWhisperID = whisper_id;
		mMembers = Arrays.copyOf(members, members.length);
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS,null,json);

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("whisper_id", mWhisperID);
		
		//convert long [] to JSONArray
		JSONArray ids = new JSONArray();
		for(long id : mMembers){
			ids.put(id);
		}//end convert array
		
		payload.put("members", mMembers);
		
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/removeWhisperMembers";
	}

}
