package com.gjk.chassip.net;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.gjk.chassip.Application;
import com.gjk.chassip.Constants;
import com.gjk.chassip.R;
import com.gjk.chassip.net.MiluHttpRequest.DBHttpUploadFile;


public abstract class MiluHTTPTask extends HTTPTask {
	
	List<DBHttpUploadFile> mFiles = null;
	//this boolean is here because some requests should be sent using MultiPart.
	//even though this is the case, some values may have no files. we should still send
	//the request as multipart even if there are no files included. 
	boolean mHasPotentialFiles = false;

	public MiluHTTPTask(Context ctx, HTTPTaskListener listener) {
		super(ctx, 0, listener);
	}

	protected void executeWithJson(String uri, JSONObject json) {
		String url = Constants.BASE_URL + uri;
		try {
			long currTime = System.currentTimeMillis();
			MiluHttpRequest req = null;
				if(!mHasPotentialFiles)
					req = MiluHttpRequest.generateStringEntityPostRequest(
							mCtx, url, false, "application/json", HTTP.UTF_8, json.toString(), mRequestCode, null);
				else
					req = MiluHttpRequest.generateMultipartRequest(mCtx, url, false, "multipart/form-data", HTTP.UTF_8, json.toString(), mFiles, mRequestCode, null);
		
			super.executeWithJson(req);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected boolean shouldLogOutOnInvalidSession() {
		return true;
	}

	protected boolean shouldSignRequest() {
		return true;
	}

	public abstract TaskResult handleSuccessfulJSONResponse(MiluHttpRequest.DBHttpResponse response, JSONObject json) throws Exception;
	public abstract JSONObject getPayload()throws Exception;
	public abstract String getUri();
	
	public void addFileToUpload(DBHttpUploadFile file) {
		if(mFiles == null)
			mFiles = new ArrayList<DBHttpUploadFile>();
		mFiles.add(file);
	}

	public void execute()  {
		try {
			if(Application.get().isNetworkAvailableWithMessage())
				executeWithJson(getUri(), getPayload());
			else if(mListener != null)
				mListener.onTaskComplete(new TaskResult(this, TaskResult.RC_FAILURE, mCtx.getString(R.string.error_no_connection) , null));
				
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	public void extractFiles(HashMap<String, Object> fieldMapping) {
		extractFiles(fieldMapping, false);
	}
	
	public void extractFiles(HashMap<String, Object> fieldMapping, boolean keepKeyNames) {
		mHasPotentialFiles = true;
		if(fieldMapping != null) {
			Iterator<String> keyIterator = fieldMapping.keySet().iterator();
			int i = 0;
			while(keyIterator.hasNext()) {
				String key = keyIterator.next();
				Object potentialFile = fieldMapping.get(key);
				if(potentialFile instanceof File) {
					addFileToUpload(new DBHttpUploadFile((File)potentialFile, keepKeyNames ? key : "file_" + i));
					i++;
					keyIterator.remove();
				}
			}
		}
	}

	@Override
	public TaskResult handleSuccessfulResponse(MiluHttpRequest.DBHttpResponse response) throws Exception {
		try {
			JSONObject json = new JSONObject(response.getResponseText());
			if (!json.getBoolean("success")){
					
				String message = getJSONErrorMessage(json);
				if (message == null || message.length() == 0)
					message = mCtx.getString(R.string.error_no_message);
				return new TaskResult(this, TaskResult.RC_FAILURE, message, json);
			}

			return handleSuccessfulJSONResponse(response, json);
		} catch (Exception e) {
			return new TaskResult(this, TaskResult.RC_FAILURE, e.getMessage(), null);
		}


	}

	@Override
	public TaskResult handleFailedResponse(MiluHttpRequest.DBHttpResponse response) throws Exception {
		return new TaskResult(this, TaskResult.RC_FAILURE, response.getResponseText(), null);
	}

	private String createSignature(String payload) throws Exception  {
	
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		//m.update(payload.getBytes("UTF-8"));
		byte[] digest = m.digest(payload.getBytes("UTF-8"));
		
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		//String hashtext = bytesToHex(digest);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
			hashtext = "0"+hashtext;
		}

		return hashtext;

	}
	
	  public static String bytesToHex(byte[] data) {
	        StringBuffer buf = new StringBuffer();
	        for (int i = 0; i < data.length; i++) {
	            buf.append(byteToHex(data[i]).toUpperCase());
	        }
	        return (buf.toString());
	    }
	 
	 
	    /**
	     *  method to convert a byte to a hex string.
	     *
	     * @param  data  the byte to convert
	     * @return String the converted byte
	     */
	    public static String byteToHex(byte data) {
	        StringBuffer buf = new StringBuffer();
	        buf.append(toHexChar((data >>> 4) & 0x0F));
	        buf.append(toHexChar(data & 0x0F));
	        return buf.toString();
	    }
	    
	    public static char toHexChar(int i) {
	        if ((0 <= i) && (i <= 9)) {
	            return (char) ('0' + i);
	        } else {
	            return (char) ('a' + (i - 10));
	        }
	    }
	 
}
