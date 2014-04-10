package com.gjk.chassip.net;

/*-----------------------------------------------------------------------

Add Whisper Members

API endpoint: http://skip2milu.com/gjk/api/addWhisperMembers

Sample JSON request:
Required fields:
 {"whisper_id":1, "recipients": [7]}



Sample JSON response:

{"success":true} (if we were able to successfully add all members that we attempted to the whisper)

-------------------------------------------------------------------------------
*/

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

<<<<<<< HEAD
public class AddWhisperMembersTask extends MiluHTTPTask {

=======


public class AddWhisperMembersTask extends MiluHTTPTask{
	
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f
	private long mWhisperID;
	private long[] mRecipients;

	public AddWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisper_id, long[] recipients) {
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mWhisperID = whisper_id;
		mRecipients = Arrays.copyOf(recipients, recipients.length);
		execute();

	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);

	}

	@Override
	public JSONObject getPayload() throws Exception {
		// TODO Auto-generated method stub
		JSONObject payload = new JSONObject();
		payload.put("whisper_id", mWhisperID);
<<<<<<< HEAD
		JSONArray ids = new JSONArray();
		for (long id : mRecipients) {
			ids.put(id);
		}
		payload.put("recipients", ids);
=======
		
		//change long [] to JSONarray
		JSONArray ids = new JSONArray();
		for (long id : mRecipients){
			ids.put(id);
		}//end change array 
		
		payload.put("recipients", mRecipients);
		
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/addWhisperMembers";
	}

}
