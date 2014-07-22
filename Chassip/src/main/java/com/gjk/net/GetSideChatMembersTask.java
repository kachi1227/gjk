package com.gjk.net;

/*---------------------------------------------------------

Get Side Chat Members

API endpoint: http://skip2milu.com/gjk/api/getSideChatMembers

Sample JSON request:
Required fields:
 {"side_chat_id":2}

Sample JSON response:

{"members":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"5","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"7","first_name":"Tukach","last_name":"Shakur","image":"resources\/7\/images\/img20140213181223.jpg"}],"success":true}

----------------------------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class GetSideChatMembersTask extends MiluHTTPTask {

    private long mSideChatId;

    public GetSideChatMembersTask(Context ctx, HTTPTaskListener listener, long sideChatId) {
        super(ctx, listener);
        mSideChatId = sideChatId;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("members"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("side_chat_id", mSideChatId);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/getSideChatMembers";
    }

}
