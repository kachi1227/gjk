package com.gjk.chassip.net;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gjk.chassip.net.HTTPTask.HTTPTaskListener;
import com.gjk.chassip.net.MiluHttpRequest.DBHttpResponse;

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
	public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long group_id, long creator_id, long[] members,
			String name) {
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
