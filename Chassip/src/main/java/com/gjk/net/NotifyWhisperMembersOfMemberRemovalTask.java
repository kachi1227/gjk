package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifyWhisperMembersOfMemberRemovalTask extends MiluHTTPTask {

    private long mWhisperId;
    private long[] mRemovedMembers;

    public NotifyWhisperMembersOfMemberRemovalTask(Context ctx, HTTPTaskListener listener, long whisperId, long[] removedMembers) {
        super(ctx, listener);
        mWhisperId = whisperId;
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
        payload.put("whisper_id", mWhisperId);
        JSONArray ids = new JSONArray();
        for (long id : mRemovedMembers) {
            ids.put(id);
        }
        payload.put("removed_members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifyWhisperMembersOfMemberRemoval";
    }

}