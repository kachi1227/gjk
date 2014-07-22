package com.gjk.net;

/*----------------------------------------------------------------

Remove Whisper Members

API endpoint: http://skip2milu.com/gjk/api/removeWhisperMembers

Sample JSON request:
Required fields:
{"whisper_id":1, "members": [7]}

Sample JSON response:

{"success":true} (if we were able to successfully remove members or if we tried to remove a member that wasnt in whisper)

--------------------------------------------------------------------
*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RemoveWhisperMembersTask extends MiluHTTPTask {

    private long mWhisperId;
    private long[] mMembers;

    public RemoveWhisperMembersTask(Context ctx, HTTPTaskListener listener, long whisperId, long[] members) {
        super(ctx, listener);
        mWhisperId = whisperId;
        mMembers = members;
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
        //convert long [] to JSONArray
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }//end convert array
        payload.put("members", ids);

        return payload;
    }

    @Override
    public String getUri() {
        return "api/removeWhisperMembers";
    }

}
