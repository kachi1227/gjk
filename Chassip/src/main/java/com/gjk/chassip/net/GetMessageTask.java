package com.gjk.chassip.net;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

import android.content.Context;

public class GetMessageTask extends MiluHTTPTask {

	private long mId;
	private long mGroupId;
	private JSONArray mMessageRange;

	public GetMessageTask(Context ctx, HTTPTaskListener listener, long id, long groupId, JSONArray messageRange) {
		super(ctx, listener);
		mId = id;
		mGroupId = groupId;
		mMessageRange = messageRange;
		execute();
	}

	@Override
	public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
			JSONObject json) throws Exception {
		return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("messages"));
	}

	@Override
	public JSONObject getPayload() throws Exception {
		JSONObject payload = new JSONObject();
		payload.put("id", mId);
		payload.put("group_id", mGroupId);
		if(mMessageRange != null)
			payload.put("message_range", mMessageRange);
		return payload;
	}

	@Override
	public String getUri() {
		return "api/getMessages";
	}
}
