package com.gjk.net;
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


import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class CreateWhisperTask extends MiluHTTPTask {
    private long mGroupId;
    private long mCreatorId;
    private long[] mMembers;
    private String mName = null;

    // constructor with null for optional field
    public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long groupId, long creatorId, long[] members) {
        this(ctx, listener, groupId, creatorId, members, null);
    }// end constructor sans optional

    // main constructor all fields
    public CreateWhisperTask(Context ctx, HTTPTaskListener listener, long groupId, long creatorId, long[] members, String name) {
        super(ctx, listener);
        mGroupId = groupId;
        mCreatorId = creatorId;
        mMembers = members;
        mName = name;
        execute();
    }// end main constructor

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("whisper"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("group_id", mGroupId);
        payload.put("creator_id", mCreatorId);
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
        return "api/createWhisper";
    }// end getUri

}
