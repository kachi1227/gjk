package com.gjk.net;

/*---------------------------------------------

Get Group Members

API endpoint: http://skip2milu.com/gjk/api/getGroupMembers

Sample JSON request:
Required fields:
{"group_id":1}


Sample JSON response:

{"members":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"5","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"6","first_name":"Kachi","last_name":"Nwaobasi","image":"resources\/6\/images\/img20140213064951.jpg"},{"id":"7","first_name":"Tukach","last_name":"Shakur","image":"resources\/7\/images\/img20140213181223.jpg"}],"success":true}

---------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class GetGroupMembersTask extends MiluHTTPTask {

    private long mGroupId;

    public GetGroupMembersTask(Context ctx, HTTPTaskListener listener, long groupId) {
        super(ctx, listener);
        mGroupId = groupId;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("members"));

    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("group_id", mGroupId);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/getGroupMembers";
    }

}
