package com.gjk.net;

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class LoginTask extends MiluHTTPTask {
    private String mPassword;
    private String mEmail;


    public LoginTask(Context ctx, HTTPTaskListener listener, String email, String password) {
        super(ctx, listener);
        mPassword = password;
        mEmail = email;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
                                                   JSONObject json) throws Exception {
        // TODO Auto-generated method stub
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("user"));

    }


    @Override
    public JSONObject getPayload() throws Exception {
        // TODO Auto-generated method stub
        JSONObject payload = new JSONObject();
        payload.put("email", mEmail);
        payload.put("password", mPassword);
        return payload;
    }


    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/login";
    }


}
