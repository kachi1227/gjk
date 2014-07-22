package com.gjk.net;

/*---------------------------------------
Delete GCM Registration

API endpoint: http://skip2milu.com/gjk/api/updateGCMRegistration

Sample JSON request:
Deleting GCM - Required fields:
{"id":4, "delete":true}


Sample JSON response:
{"success":true}
----------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;


public class DeleteGcmRegTask extends MiluHTTPTask {

    private long mId;
    private boolean mDelete;

    public DeleteGcmRegTask(Context ctx, HTTPTaskListener listener, long id, boolean delete) {
        super(ctx, listener);
        mId = id;
        mDelete = delete;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getBoolean("success"));

    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", mId);
        payload.put("delete", mDelete);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/updateGCMRegistration";
    }

}
