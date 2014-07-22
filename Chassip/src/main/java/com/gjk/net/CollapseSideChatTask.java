package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class CollapseSideChatTask extends MiluHTTPTask {

    private long mSideChatId;

    public CollapseSideChatTask(Context ctx, HTTPTaskListener listener, long sideChatId) {
        super(ctx, listener);
        mSideChatId = sideChatId;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
                                                   JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("side_chat_id", mSideChatId);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/collapseSideChat";
    }

}