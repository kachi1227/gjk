package com.gjk.net;

/*-------------------------------------------------

Add Side Chat Members


API endpoint: http://skip2milu.com/gjk/api/addSideChatMembers

Sample JSON request:
Required fields:
 {"side_chat_id":1, "recipients": [5, 6]}



Sample JSON response:

{"success":true} (if we were able to successfully add all members that we attempted to the side chat)

---------------------------------------------------------
*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class AddSideChatMembersTask extends MiluHTTPTask {

    private long mSideChatId;
    private long[] mRecipients;


    public AddSideChatMembersTask(Context ctx, HTTPTaskListener listener, long sideChatId, long[] recipients) {
        super(ctx, listener);
        mSideChatId = sideChatId;
        mRecipients = recipients;
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
        for (long id : mRecipients) {
            ids.put(id);
        }
        payload.put("recipients", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/addSideChatMembers";
    }

}
