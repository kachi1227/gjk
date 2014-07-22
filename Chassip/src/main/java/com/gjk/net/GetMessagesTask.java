package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetMessagesTask extends MiluHTTPTask {

    private long mId;
    private long mGroupId;
    private JSONArray mMessageRange;

    public GetMessagesTask(Context ctx, HTTPTaskListener listener, long id, long groupId, JSONArray messageRange) {
        super(ctx, listener);
        mId = id;
        mGroupId = groupId;
        mMessageRange = messageRange;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("messages"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", mId);
        payload.put("group_id", mGroupId);
        if (mMessageRange != null)
            payload.put("message_range", mMessageRange);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/getMessages";
    }
}
