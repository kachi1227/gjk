package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifySideChatMembersOfCollapseTask extends MiluHTTPTask {

    private long mSideChatId;
    private long[] mMembers;

    public NotifySideChatMembersOfCollapseTask(Context ctx, HTTPTaskListener listener, long sideChatId, long[] members) {
        super(ctx, listener);
        mSideChatId = sideChatId;
        mMembers = members;
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
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }
        payload.put("members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifySideChatMembersOfCollapse";
    }

}