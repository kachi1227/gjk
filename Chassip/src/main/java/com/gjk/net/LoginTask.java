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
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("user"));

    }


    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("email", mEmail);
        payload.put("password", mPassword);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/login";
    }

    @Override
    public boolean showProgress() {
        return true;
    }
}
