package com.gjk.net;

/*-------------------------------------------------------------------------------

Get Whisper Members

API endpoint: http://skip2milu.com/gjk/api/getWhisperMembers

Sample JSON request:
Required fields:
 {"whisper_id":2}

Sample JSON response:

{"members":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":""},{"id":"6","first_name":"Kachi","last_name":"Nwaobasi","image":"resources\/6\/images\/img20140213064951.jpg"}],"success":true}

----------------------------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class GetWhisperMembersTask extends MiluHTTPTask {

    private long mWhisperId;

    public GetWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisperId) {
        super(ctx, listener);
        mWhisperId = whisperId;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("members"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("whisper_id", mWhisperId);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/getWhisperMembers";
    }

}
