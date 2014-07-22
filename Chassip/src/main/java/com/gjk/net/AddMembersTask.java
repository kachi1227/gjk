package com.gjk.net;

/*----------------------------
 Adding Member

API endpoint:  http://skip2milu.com/gjk/api/addMembers

Sample JSON request:
{"group_id":1, "recipients": [2, 4, 5]}

Sample JSON response:
{"success":true} (if we were able to successfully add all members that we attempted to add)

------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;


public class AddMembersTask extends MiluHTTPTask {
    private long mGroupId;
    private long[] mRecipients;

    public AddMembersTask(Context ctx, HTTPTaskListener listener, long groupId, long[] recipients) {
        super(ctx, listener);
        mGroupId = groupId;
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
        payload.put("group_id", mGroupId);
        JSONArray ids = new JSONArray();
        for (long id : mRecipients) {
            ids.put(id);
        }
        payload.put("recipients", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/addMembers";
    }

}
