package com.gjk.net;

/*-----------------------------------------------------------------------

Add Whisper Members

API endpoint: http://skip2milu.com/gjk/api/addWhisperMembers

Sample JSON request:
Required fields:
 {"whisper_id":1, "recipients": [7]}



Sample JSON response:

{"success":true} (if we were able to successfully add all members that we attempted to the whisper)

-------------------------------------------------------------------------------
*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;


public class AddWhisperMembersTask extends MiluHTTPTask {

    private long mWhisperId;
    private long[] mRecipients;

    public AddWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisperId, long[] recipients) {
        super(ctx, listener);
        mWhisperId = whisperId;
        mRecipients = recipients;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("whisper_id", mWhisperId);
        JSONArray ids = new JSONArray();
        for (long id : mRecipients) {
            ids.put(id);
        }
        payload.put("recipients", ids);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/addWhisperMembers";
    }

}
