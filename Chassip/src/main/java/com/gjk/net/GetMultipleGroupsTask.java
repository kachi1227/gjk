package com.gjk.net;

/*--------------------------------------
 Get Groups

API endpoint: http://skip2milu.com/gjk/api/getGroups

Sample JSON request:
Required fields:
{"id":4}


Sample JSON response:

IMPORTANT: The ids & names of whispers and sidechats will be returned every time a 
call is made to getGroups OR getGroup. The format for every side chat or whisper that 
a user can see will be "side_chat_id:side_chat_name" OR "whisper_id:whisper_name", respectively. See below

{"groups":[{"id":"1","name":"GJK","image":"","creator_id":"4","creator_name":"Kachi Nwaobasi",
	"side_chats":"2:side_chat-2|3:Hello WOrld|4:Home|5:Sweet|6:Home","whispers":"1:whisper-1|2:Knock, " +
			"Knock|3:Who's There"},
			{"id":"9","name":"GJK Back","image":"resources\/groups\/group-9\/img20140224010119.png","creator_id":"4",
				"creator_name":"Kachi Nwaobasi","side_chats":null,"whispers":"4:John"}],"success":true}

---------------------------------------------------------------
*/


import android.content.Context;

import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONObject;

public class GetMultipleGroupsTask extends MiluHTTPTask {

    private long mID;

    public GetMultipleGroupsTask(Context ctx, HTTPTaskListener listener, long id) {
        super(ctx, listener);
        // TODO Auto-generated constructor stub
        mID = id;
        execute();
    }

    @Override
    public TaskResult handleSuccessfulJSONResponse(DBHttpResponse response,
                                                   JSONObject json) throws Exception {
        // TODO Auto-generated method stub
        return new TaskResult(this, TaskResult.RC_SUCCESS, null, json.getJSONArray("groups"));

    }

    @Override
    public JSONObject getPayload() throws Exception {
        // TODO Auto-generated method stub
        JSONObject payload = new JSONObject();
        payload.put("id", mID);

        return payload;
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return "api/getGroups";
    }

}
