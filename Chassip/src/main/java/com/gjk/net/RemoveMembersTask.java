package com.gjk.net;

/*---------------------------------------------
Removing Member


API endpoint:  http://skip2milu.com/gjk/api/removeMembers

Sample JSON request:
{"group_id":1, "members": [2, 4, 5]}

Sample JSON response:
{"success":true} (if we were able to successfully remove members or if we tried to remove a member that wasnt in group)
--------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;


public class RemoveMembersTask extends MiluHTTPTask {

    private long mGroupId;
    private long[] mMembers;

    public RemoveMembersTask(Context ctx, HTTPTaskListener listener, long groupId, long[] members) {
        super(ctx, listener);
        mGroupId = groupId;
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
        payload.put("group_id", mGroupId);
        //convert long[] to JSONArray
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }//end convert array
        payload.put("members", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/removeMembers";
    }

}
