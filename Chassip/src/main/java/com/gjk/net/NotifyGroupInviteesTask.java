package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifyGroupInviteesTask extends MiluHTTPTask {

    private long mID;
    private long mGroupID;
    private long[] mRecipients;

    public NotifyGroupInviteesTask(Context ctx, HTTPTaskListener listener, long id, long group_id, long[] recipients) {
        super(ctx, listener);
        // TODO Auto-generated constructor stub
        mID = id;
        mGroupID = group_id;
        mRecipients = recipients;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
                                                   JSONObject json) throws Exception {
        // TODO Auto-generated method stub
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);
    }

    @Override
    public JSONObject getPayload() throws Exception {
        // TODO Auto-generated method stub
        JSONObject payload = new JSONObject();
        payload.put("id", mID);
        payload.put("group_id", mGroupID);
        JSONArray ids = new JSONArray();
        for (long id : mRecipients) {
            ids.put(id);
        }
        payload.put("recipients", ids);
        return payload;
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/notifyGroupInvitees";
    }

}