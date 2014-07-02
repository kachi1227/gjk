package com.gjk.net;

/*------------------------------------------
Removing Group

API endpoint:  http://skip2milu.com/gjk/api/removeGroup

Sample JSON request:
{"group_id": 1}

Sample JSON response:
{"success":true} (if we were able to successfully remove group or if we tried to remove a group that doesn't exist)
-------------------------------------------------
*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class RemoveGroupTask extends MiluHTTPTask {
    private long mGroupID;

    public RemoveGroupTask(Context ctx, HTTPTaskListener listener, long GroupID) {
        super(ctx, listener);
        mGroupID = GroupID;
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
        payload.put("group_id", mGroupID);
        return payload;
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/removeGroup";
    }

}
