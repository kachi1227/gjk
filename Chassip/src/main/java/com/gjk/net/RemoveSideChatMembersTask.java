package com.gjk.net;

/*----------------------------------------------------------------

Remove Side Chat Members

API endpoint: http://skip2milu.com/gjk/api/removeSideChatMembers

Sample JSON request:
Required fields:
{"side_chat_id":1, "members": [5]}

Sample JSON response:

{"success":true} (if we were able to successfully remove members or if we tried to remove a member that wasnt in side chat)

--------------------------------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RemoveSideChatMembersTask extends MiluHTTPTask {

    private long mSideChatId;
    private long[] mMembers;

    public RemoveSideChatMembersTask(Context ctx, HTTPTaskListener listener, long sideChatId, long[] members) {
        super(ctx, listener);
        mSideChatId = sideChatId;
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
        return "api/removeSideChatMembers";
    }

}
