package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifyWhisperMembersOfDeletionTask extends MiluHTTPTask {

    private long mWhisperId;
    private long[] mMembers;

    public NotifyWhisperMembersOfDeletionTask(Context ctx, HTTPTaskListener listener, long whisperID, long[] members) {
        super(ctx, listener);
        mWhisperId = whisperID;
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
        payload.put("whisper_id", mWhisperId);
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }
        payload.put("members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifyWhisperMembersOfDeletion";
    }

}