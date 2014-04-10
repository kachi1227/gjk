package com.gjk.chassip.net;

/*-------------------------------------------------

Add Side Chat Members


API endpoint: http://skip2milu.com/gjk/api/addSideChatMembers

Sample JSON request:
Required fields:
 {"side_chat_id":1, "recipients": [5, 6]}



Sample JSON response:

{"success":true} (if we were able to successfully add all members that we attempted to the side chat)

---------------------------------------------------------
*/

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;



public class AddSideChatMembersTask extends MiluHTTPTask {
<<<<<<< HEAD

	private long mSideChatID;
	private long[] mRecipients;

	public AddSideChatMembersTask(Context ctx, HTTPTaskListener listener, long side_chat_id, long[] recipients) {
=======
		
		private long mSideChatID;
		private long [] mRecipients;
		
	public AddSideChatMembersTask(Context ctx, HTTPTaskListener listener, long side_chat_id, long [] recipients) {
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f
		super(ctx, listener);
		// TODO Auto-generated constructor stub
		mSideChatID = side_chat_id;
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
		payload.put("side_chat_id", mSideChatID);
<<<<<<< HEAD
		JSONArray ids = new JSONArray();
		for (long id : mRecipients) {
=======
		
		JSONArray ids = new JSONArray();
		for(long id : mRecipients){
>>>>>>> 0af198c1a8a9fb33cbcd7f61d436533aec8c5c0f
			ids.put(id);
		}
		payload.put("recipients", ids);
		return payload;
	}

	@Override
	public String getUri() {
		// TODO Auto-generated method stub
		return "api/addSideChatMembers";
	}

}
