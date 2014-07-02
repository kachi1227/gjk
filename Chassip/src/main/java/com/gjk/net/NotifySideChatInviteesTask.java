package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifySideChatInviteesTask extends MiluHTTPTask {

    private long mID;
    private long mSideChatID;
    private long[] mRecipients;

    public NotifySideChatInviteesTask(Context ctx, HTTPTaskListener listener, long id, long side_chat_id, long[] recipients) {
        super(ctx, listener);
        // TODO Auto-generated constructor stub
        mID = id;
        mSideChatID = side_chat_id;
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
        payload.put("side_chat_id", mSideChatID);
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
        return "api/notifySideChatInvitees";
    }

}