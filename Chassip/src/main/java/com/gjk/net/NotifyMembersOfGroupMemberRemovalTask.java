package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifyMembersOfGroupMemberRemovalTask extends MiluHTTPTask {

    private long mGroupId;
    private long[] mRemovedMembers;

    public NotifyMembersOfGroupMemberRemovalTask(Context ctx, HTTPTaskListener listener, long groupId, long[] removedMembers) {
        super(ctx, listener);
        mGroupId = groupId;
        mRemovedMembers = removedMembers;
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
        for (long id : mRemovedMembers) {
            ids.put(id);
        }
        payload.put("removed_members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifyMembersOfGroupMemberRemoval";
    }

}