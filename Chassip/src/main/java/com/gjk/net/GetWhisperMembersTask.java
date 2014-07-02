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

    private long mWhisperID;

    public GetWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisper_id) {
        super(ctx, listener);
        // TODO Auto-generated constructor stub
        mWhisperID = whisper_id;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
                                                   JSONObject json) throws Exception {
        // TODO Auto-generated method stub
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("members"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        // TODO Auto-generated method stub
        JSONObject payload = new JSONObject();
        payload.put("whisper_id", mWhisperID);
        return payload;
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/getWhisperMembers";
    }

}
