package com.gjk.net;

/*---------------------------------------------

Create Side Chat

API endpoint: http://skip2milu.com/gjk/api/createSideChat

Sample JSON request:
Required fields:
{"group_id":1, "creator_id": 4,  "members": [5, 6]}



Optional fields:
"name" - the name of the side chat

Sample JSON response:

{"side_chat":{"id":"1","group_id":"1","name":"","creator_id":"4","first_name":"Kachi","last_name":"Nwaobasi", "collapsed":0},"success":true}

-------------------------------------------------*/


import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;


public class CreateSideChatTask extends MiluHTTPTask {
    private long mGroupId;
    private long mCreatorId;
    private long[] mMembers;
    private String mName = null;
    //end members

    //constructor with null for optional field
    public CreateSideChatTask(Context ctx, HTTPTaskListener listener, long groupId, long creatorId, long[] members) {
        this(ctx, listener, groupId, creatorId, members, null);
    }//end constructor sans optional

    //main constructor all fields
    public CreateSideChatTask(Context ctx, HTTPTaskListener listener, long groupId, long creatorId, long[] members, String name) {
        super(ctx, listener);
        mGroupId = groupId;
        mCreatorId = creatorId;
        mMembers = members;
        mName = name;
        execute();
    }//end main constructor

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response, JSONObject json) throws Exception {
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONObject("side_chat"));
    }

    @Override
    public JSONObject getPayload() throws Exception {
        // TODO Auto-generated method stub
        JSONObject payload = new JSONObject();
        payload.put("group_id", mGroupId);
        payload.put("creator_id", mCreatorId);
        //change long [] to JSONarray
        JSONArray ids = new JSONArray();
        for (long id : mMembers) {
            ids.put(id);
        }//end change array
        payload.put("members", ids);
        if (mName != null)
            payload.put("name", mName);

        return payload;
    }//end getPayload

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/createSideChat";
    }//end getUri

}
