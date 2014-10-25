package com.gjk.net;

import android.content.Context;
import android.util.Log;

import com.gjk.Application;
import com.gjk.net.MiluHttpRequest.DBHttpRequestCompleteListener;
import com.gjk.net.MiluHttpRequest.DBHttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


abstract public class HTTPTask implements DBHttpRequestCompleteListener, Task {

    private static final String TAG = "ApiTask";

    public interface HTTPTaskListener {
        public void onTaskComplete(TaskResult result);

    }

    protected HTTPTaskListener mListener;
    protected Context mCtx;
    protected int mFailCount;
    protected MiluHttpRequest mRequest;
    protected int mRequestCode;
    protected TaskResult mResult;
    private String mId;

    public HTTPTask(Context ctx, int requestCode, HTTPTaskListener listener) {
        mListener = listener;
        mCtx = ctx;
        mFailCount = 0;
        mRequestCode = requestCode;
        mId = UUID.randomUUID().toString();
    }


    abstract public TaskResult handleSuccessfulResponse(DBHttpResponse response) throws Exception;

    abstract public TaskResult handleFailedResponse(DBHttpResponse response) throws Exception;
    //abstract public void notifyUpdateListener(DBHttpResponse response);


    public void onHttpRequestSuccessInBackground(DBHttpResponse response)
            throws Exception {
        mResult = handleSuccessfulResponse(response);
    }

    public void onHttpRequestFailedInBackground(DBHttpResponse response) {
        try {
            mResult = handleFailedResponse(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onHttpRequestSuccess(DBHttpResponse response) {
        if (mListener != null) {
            if (mResult == null)
                mResult = new TaskResult(this, TaskResult.RC_SUCCESS, response.getResponseText(), response);
            Application.get().log(String.format("%s - %s: success, message=%s", this.getClass().getSimpleName(), mId,
                    mResult.getMessage()));
            mListener.onTaskComplete(mResult);
            //notifyUpdateListener(response);
        }
    }

    public void onHttpRequestFailed(DBHttpResponse response) {
        if (mListener != null) {
            try {
                String message = getRootErrorMessage(response);
                if (message == null || message.equals(""))
                    // TODO add error message
                    //message = mApp.getString(R.string.unknown_error);
                    if (mResult == null)
                        mResult = createTaskResultFailed(message, null);
                Application.get().log(String.format("%s - %s: failed, message=%s", this.getClass().getSimpleName(), mId,
                        mResult.getMessage()));
                mListener.onTaskComplete(mResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected String getRootErrorMessage(DBHttpResponse response) {
        String message = response.getResponseText();
        Log.v(TAG, "Parsing error response: " + message);
        try {
            String jsonMessage = getJSONErrorMessage(new JSONObject(message));
            if (jsonMessage != null) {
                message = jsonMessage;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse error message: " + message, e);
        }
        return message;
    }

    protected String getJSONErrorMessage(JSONObject obj) throws JSONException {

        String message;
        if (obj.has("error")) {
            message = obj.getString("error");
        } else if (obj.has("Message")) {
            message = obj.getString("Message");
        } else if (obj.has("message")) {
            message = obj.getString("message");
        } else {
            // TODO add error message
            //message = mApp.getString(R.string.unknown_error);
            message = null;
        }

        return message;
    }

    public void cancel() {
        if (mRequest != null) {
            mRequest.cancelExecuteAsync();
        }
    }

    protected void executeWithJson(MiluHttpRequest req) throws InterruptedException, ExecutionException, TimeoutException {
        mRequest = req;
        req.addHeader("Accept", "application/json");
        req.executeAsync(this);
    }

    protected void executeSync(MiluHttpRequest req) {
        mRequest = req;
        try {
            DBHttpResponse response = req.execute();
            TaskResult result = handleSuccessfulResponse(response);
            mListener.onTaskComplete(result);
        } catch (Exception e) {
            Log.e(TAG, "Request failed", e);
            mListener.onTaskComplete(createTaskResultFailed(e.getMessage(), e));
        }

    }

    protected TaskResult createTaskResultFailed(String message, Exception e) {
        return new TaskResult(this, TaskResult.RC_FAILURE, message, e);
    }

    protected void execute(MiluHttpRequest req) throws InterruptedException, ExecutionException, TimeoutException {
        mRequest = req;
        req.executeAsync(this);
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public boolean isTypeOfRequest(int requestCode) {
        return mRequestCode == requestCode;
    }

    public String getId() {
        return mId;
    }

	/*public static String buildUrl(String path, Object...args) {
        //return null;
		//return API_BASE_URL + String.format(Locale.US, path, args);
	}
	
	public static String buildUrl(String path) {
		return API_BASE_URL + path;
	}*/
}
