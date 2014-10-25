package com.gjk.net;

/*----------------------------
Getting Users By Phone Number

API endpoint: http://skip2milu.com/gjk/api/getUsersByPhoneNumbers

Sample JSON request:

Required Fields
{"phone_numbers":["(781)9862274", "617-888-1939"]}


Sample JSON response:
{"users":[{"id":"4","first_name":"Kachi","last_name":"Nwaobasi","image":"","bio":"This is my bio"}],"success":true}

------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;


public class GetUsersByPhoneNumbersTask extends MiluHTTPTask {
    private String[] mPhoneNumbers;

    public GetUsersByPhoneNumbersTask(Context ctx, HTTPTaskListener listener, String[] phoneNumbers) {
        super(ctx, listener);
        mPhoneNumbers = phoneNumbers;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("users"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        JSONArray ids = new JSONArray();
        for (String id : mPhoneNumbers) {
            ids.put(id);
        }
        payload.put("phone_numbers", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/getUsersByPhoneNumbers";
    }

    @Override
    public boolean showProgress() {
        return true;
    }
}
