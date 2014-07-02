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

    private long mSideChatID;
    private long[] mMembers;

    public RemoveSideChatMembersTask(Context ctx, HTTPTaskListener listener, long side_chat_id, long[] members) {
        super(ctx, listener);
        // TODO Auto-generated constructor stub
        mSideChatID = side_chat_id;
        mMembers = members;
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
        payload.put("side_chat_id", mSideChatID);
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }

        payload.put("members", ids);

        return payload;
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/removeSideChatMembers";
    }

}
