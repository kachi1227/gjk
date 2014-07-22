package com.gjk.net;

/*------------------------------------
 Update GCM Registration

API endpoint: http://skip2milu.com/gjk/api/updateGCMRegistration

Sample JSON request:
Updating GCM - Required fields:
 {"id":4, "registration_id":"test_reg", "phone_type": "ANDROID"}
 
 
Sample JSON response:
{"success":true}
 -------------------------------------
*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class UpdateGcmRegTask extends MiluHTTPTask {

    private long mId;
    private String mRegistrationID;
    private String mPhoneType;

    public UpdateGcmRegTask(Context ctx, HTTPTaskListener listener, long id, String registrationId, String phoneType) {
        super(ctx, listener);
        mId = id;
        mRegistrationID = registrationId;
        mPhoneType = phoneType;
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
        payload.put("registration_id", mRegistrationID);
        payload.put("phone_type", mPhoneType);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/updateGCMRegistration";
    }

}
