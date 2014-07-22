package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifyGroupInviteesTask extends MiluHTTPTask {

    private long mId;
    private long mGroupId;
    private long[] mRecipients;

    public NotifyGroupInviteesTask(Context ctx, HTTPTaskListener listener, long id, long groupId, long[] recipients) {
        super(ctx, listener);
        mId = id;
        mGroupId = groupId;
        mRecipients = recipients;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", mId);
        payload.put("group_id", mGroupId);
        JSONArray ids = new JSONArray();
        for (long id : mRecipients) {
            ids.put(id);
        }
        payload.put("recipients", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifyGroupInvitees";
    }

}