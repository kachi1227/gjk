package com.gjk.net;

/*-------------------------------------
Notifying Group Members of Typing Status Change

API endpoint: http://skip2milu.com/gjk/api/notifyGroupMembersOfUserTypingChange

Sample JSON request:
{"id":4, "group_id" : 1, "is_typing": false} ("id" in this case is the id of the user who is or isn't typing)

Sample JSON response:
{"success":true}

FOR GCM PARSING:

Here's what the "data" json of the GCM message will look like:
{"msg_type" : "typing_change", "msg_content":{"id": 4, "group_id": 1, "is_typing": false}}

--------------------------------------------------------*/

import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class NotifyGroupMembersOfUserTypingChangeTask extends MiluHTTPTask {

    private long mId;
    private long mGroupId;
    private boolean mIsTyping;

    public NotifyGroupMembersOfUserTypingChangeTask(Context ctx, HTTPTaskListener listener, long id, long groupId,
                                                    boolean isTyping) {
        super(ctx, listener);
        mId = id;
        mGroupId = groupId;
        mIsTyping = isTyping;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json);
    }

    @Override
    public JSONObject getPayload() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("id", mId);
        payload.put("group_id", mGroupId);
        payload.put("is_typing", mIsTyping);
        return payload;
    }

    @Override
    public String getUri() {
        return "api/notifyGroupMembersOfUserTypingChange";
    }

}
