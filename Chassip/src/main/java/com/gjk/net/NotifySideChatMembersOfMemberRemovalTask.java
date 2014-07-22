package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotifySideChatMembersOfMemberRemovalTask extends MiluHTTPTask {

    private long mSideChatId;
    private long[] mRemovedMembers;

    public NotifySideChatMembersOfMemberRemovalTask(Context ctx, HTTPTaskListener listener, long sideChatId, long[] removedMembers) {
        super(ctx, listener);
        mSideChatId = sideChatId;
        mRemovedMembers = removedMembers;
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
        for (long id : mRemovedMembers) {
            ids.put(id);
        }
        payload.put("removed_members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifySideChatMembersOfMemberRemoval";
    }

}