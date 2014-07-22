package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifyMembersOfGroupDeletionTask extends MiluHTTPTask {

    private long mGroupId;
    private long[] mMembers;

    public NotifyMembersOfGroupDeletionTask(Context ctx, HTTPTaskListener listener, long groupId, long[] members) {
        super(ctx, listener);
        mGroupId = groupId;
        mMembers = members;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("group_id", mGroupId);
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }
        payload.put("members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifyMembersOfGroupDeletion";
    }

}