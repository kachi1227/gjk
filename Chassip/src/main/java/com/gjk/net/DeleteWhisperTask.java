package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class DeleteWhisperTask extends MiluHTTPTask {

    private long mWhisperId;

    public DeleteWhisperTask(Context ctx, HTTPTaskListener listener, long whisperId) {
        super(ctx, listener);
        mWhisperId = whisperId;
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
        return payload;
    }

    @Override
    public String getUri() {
        return "api/deleteWhisper";
    }

}