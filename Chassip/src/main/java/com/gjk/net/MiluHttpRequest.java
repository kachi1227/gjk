package com.gjk.net;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.gjk.helper.DataHelper;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MiluHttpRequest {
    public static final int REQUEST_TYPE_GET = 1;
    public static final int REQUEST_TYPE_POST_NORM = 2;
    public static final int REQUEST_TYPE_POST_MULTIPART = 3;
    public static final int REQUEST_TYPE_POST_STRING_ENTITY = 4;
    public static final int REQUEST_TYPE_HEAD = 5;

    public static final int DEFAULT_TIMEOUT_SECONDS = 30;

    public interface DBHttpRequestCompleteListener {
        public void onHttpRequestSuccessInBackground(DBHttpResponse response) throws Exception;

        public void onHttpRequestFailedInBackground(DBHttpResponse response);

        public void onHttpRequestSuccess(DBHttpResponse response);

        public void onHttpRequestFailed(DBHttpResponse response);
    }

    private static final String LAST_MOD_HEADER_NAME = "Last-Modified";
//	private static final String LINE_END = "\r\n";
//	private static final String TWO_HYPHENS = "--";
//	private static final String BOUNDRY = "*****";
//	private static final String POST_SEPERATOR = TWO_HYPHENS + BOUNDRY + LINE_END;
//	private static final String POST_CLOSER = TWO_HYPHENS + BOUNDRY + TWO_HYPHENS + LINE_END;
//	private static final String PARAM_CONTENT_DISPO_FORMAT = "Content-Disposition: form-data; name=\"%s\"" + LINE_END;
//	private static final String FILE_CONTENT_DISPO_FORMAT = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"" + LINE_END;
//	
//	private static final String HTTP_POST_METHOD = "POST";
//	private static final String HTTP_FILE_POST_CONTENT_TYPE = "multipart/form-data;boundary=" + BOUNDRY;


    private String mUrl;
    private int mRequestType, mRequestCode;
    private String mEntityType, mEntityEncoding, mStringEntity;
    private HashMap<String, String> mHeaders;
    private ArrayList<NameValuePair> mPostParams;
    private List<DBHttpUploadFile> mFilesToUpload;
    private int mTimeoutSeconds;
    private boolean mRaw;

    private Context mContext;

    private DBHttpRequestCompleteListener mListener;
    DBHttpRequestTask mAsyncTask;

    private Object mUserInfo;

    private MiluHttpRequest() {
        mContext = null;
        mUrl = null;
        mRequestType = 0;
        mRequestCode = 0;
        mEntityEncoding = null;
        mEntityType = null;
        mStringEntity = null;
        mHeaders = null;
        mPostParams = null;
        mFilesToUpload = null;
        mAsyncTask = null;
        mTimeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    }


    public static MiluHttpRequest generateGetRequest(Context context, String url, boolean raw, int requestCode, Object userInfo) {
        MiluHttpRequest req = generateGenericRequest(context, url, REQUEST_TYPE_GET, raw, requestCode, userInfo);
        return req;
    }

    public static MiluHttpRequest generateNormalPostRequest(Context context, String url, boolean raw, int requestCode, Object userInfo) {
        MiluHttpRequest req = generateGenericRequest(context, url, REQUEST_TYPE_POST_NORM, raw, requestCode, userInfo);
        return req;
    }

    public static MiluHttpRequest generateFileUploadRequest(Context context, String url, boolean raw, int requestCode, Object userInfo) {
        MiluHttpRequest req = generateGenericRequest(context, url, REQUEST_TYPE_POST_MULTIPART, raw, requestCode, userInfo);
        return req;
    }

    public static MiluHttpRequest generateMultipartRequest(Context context, String url, boolean raw, String entityType, String entityEncoding, String stringEntity, List<DBHttpUploadFile> files, int requestCode, Object userInfo) {
        MiluHttpRequest req = generateGenericRequest(context, url, REQUEST_TYPE_POST_MULTIPART, raw, requestCode, userInfo);
        req.setEntityEncoding(entityEncoding);
        req.setEntityType(entityType);
        req.setStringEntity(stringEntity);
        req.setFilesToUpload(files);
        return req;
    }

    public static MiluHttpRequest generateStringEntityPostRequest(Context context, String url, boolean raw, String entityType, String entityEncoding, String stringEntity, int requestCode, Object userInfo) {
        MiluHttpRequest req = generateGenericRequest(context, url, REQUEST_TYPE_POST_STRING_ENTITY, raw, requestCode, userInfo);
        req.setEntityEncoding(entityEncoding);
        req.setEntityType(entityType);
        req.setStringEntity(stringEntity);
        return req;
    }

    public static MiluHttpRequest generateHeadRequest(Context context, String url, int timeoutSeconds, int requestCode, Object userInfo) {
        MiluHttpRequest req = generateGenericRequest(context, url, REQUEST_TYPE_HEAD, false, requestCode, userInfo);
        return req;
    }

    private static MiluHttpRequest generateGenericRequest(Context context, String url, int requestType, boolean raw, int requestCode, Object userInfo) {
        MiluHttpRequest req = new MiluHttpRequest();
        req.setContext(context);
        req.setUrl(url);
        req.setRequestCode(requestCode);
        req.setRequestType(requestType);
        req.setUserInfo(userInfo);
        req.setRaw(raw);
//		req.setTimeoutSeconds((timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS));
        return req;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setRaw(boolean raw) {
        this.mRaw = raw;
    }

    public boolean isRaw() {
        return mRaw;
    }

    public int getRequestType() {
        return mRequestType;
    }

    private void setRequestType(int requestType) {
        this.mRequestType = requestType;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    private void setRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
    }

    public String getEntityType() {
        return mEntityType;
    }

    public void setEntityType(String entityType) {
        this.mEntityType = entityType;
    }

    public String getEntityEncoding() {
        return mEntityEncoding;
    }

    public void setEntityEncoding(String entityEncoding) {
        this.mEntityEncoding = entityEncoding;
    }

    public String getStringEntity() {
        return mStringEntity;
    }

    public void setStringEntity(String stringEntity) {
        this.mStringEntity = stringEntity;
    }

    public HashMap<String, String> getHeaders() {
        return mHeaders;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.mHeaders = headers;
    }

    public ArrayList<NameValuePair> getPostParams() {
        return mPostParams;
    }

    public void setPostParams(ArrayList<NameValuePair> postParams) {
        this.mPostParams = postParams;
    }

    public List<DBHttpUploadFile> getFilesToUpload() {
        return mFilesToUpload;
    }

    public void setFilesToUpload(List<DBHttpUploadFile> filesToUpload) {
        this.mFilesToUpload = filesToUpload;
    }

    public int getTimeoutSeconds() {
        return mTimeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.mTimeoutSeconds = timeoutSeconds;
    }

    public DBHttpRequestCompleteListener getListener() {
        return mListener;
    }

    public void setListener(DBHttpRequestCompleteListener listener) {
        this.mListener = listener;
    }

    public Object getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(Object userInfo) {
        this.mUserInfo = userInfo;
    }

    public void addHeader(String name, String value) {
        if (mHeaders == null) {
            mHeaders = new HashMap<String, String>();
        }
        mHeaders.put(name, value);
    }

    public void addPostParam(String name, String value) {
        if (mPostParams == null) {
            mPostParams = new ArrayList<NameValuePair>();
        }
        mPostParams.add(new BasicNameValuePair(name, value));
    }

    public void addFileToUpload(DBHttpUploadFile file) {
        if (mFilesToUpload == null) {
            mFilesToUpload = new ArrayList<MiluHttpRequest.DBHttpUploadFile>();
        }
        mFilesToUpload.add(file);
    }

    public void addFileToUpload(File file, String paramName) {
        addFileToUpload(new DBHttpUploadFile(file, paramName));
    }

    public void addFileToUpload(File file, String paramName, String filenameToSendAs) {
        addFileToUpload(new DBHttpUploadFile(file, paramName, filenameToSendAs));
    }

    public void addFileToUpload(String filepath, String paramName) {
        addFileToUpload(new DBHttpUploadFile(filepath, paramName));
    }

    public void addFileToUpload(String filepath, String paramName, String filenameToSendAs) {
        addFileToUpload(new DBHttpUploadFile(filepath, paramName, filenameToSendAs));
    }

    public void addBasicAuth(String username, String password) {
        String authStr = Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
        addHeader("Authorization", "Basic " + authStr);
    }

    public void executeAsync(DBHttpRequestCompleteListener listener) {
        setListener(listener);
        mAsyncTask = new DBHttpRequestTask(this);
        mAsyncTask.execute((Void) null);
    }

    public void cancelExecuteAsync() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
    }

    public DBHttpResponse execute() {
        DBHttpResponse response = new DBHttpResponse(this);

        if (mRequestType == REQUEST_TYPE_POST_MULTIPART) {
            try {
                doFileUploadRequest(response);
            } catch (Exception e) {
                response.mSuccesful = false;
            }
        } else {
            try {
                HttpParams httpConnParms = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpConnParms, mTimeoutSeconds * 1000);
                HttpConnectionParams.setSoTimeout(httpConnParms, mTimeoutSeconds * 1000);

                HttpUriRequest httpMessage = null;
                switch (mRequestType) {
                    case REQUEST_TYPE_GET:
                        httpMessage = new HttpGet(mUrl);
                        break;
                    case REQUEST_TYPE_POST_NORM:
                        httpMessage = new HttpPost(mUrl);
                        if (mPostParams != null) {
                            ((HttpPost) httpMessage).setEntity(new UrlEncodedFormEntity(mPostParams, HTTP.UTF_8));
                        }
                        break;
                    case REQUEST_TYPE_POST_STRING_ENTITY:
                        httpMessage = new HttpPost(mUrl);
                        StringEntity stringEntity = new StringEntity(mStringEntity, mEntityEncoding);
                        stringEntity.setContentType(mEntityType);
                        ((HttpPost) httpMessage).setEntity(stringEntity);
                        break;
                    case REQUEST_TYPE_HEAD:
                        httpMessage = new HttpHead(mUrl);
                        break;
                }

                if (mHeaders != null) {
                    for (String key : mHeaders.keySet()) {
                        httpMessage.addHeader(key, mHeaders.get(key));
                    }
                }

                HttpClient client = new DefaultHttpClient(httpConnParms);
                HttpResponse httpResponse = client.execute(httpMessage);
                HttpEntity entity = httpResponse.getEntity();

                response.mResponseCode = httpResponse.getStatusLine().getStatusCode();
                if (response.mResponseCode != 200 && response.mResponseCode != 206) {
                    //response.mResponseText = httpResponse.getStatusLine().getReasonPhrase();
                    response.mResponseText = DataHelper.getTextFromStream(entity.getContent());
                    if (response.mResponseText == null) {
                        response.mResponseText = httpResponse.getStatusLine().getReasonPhrase();
                    }
                    response.mSuccesful = false;
                } else {

                    //try to get last mod time
                    try {
                        Header lastModHeader = httpResponse.getFirstHeader(LAST_MOD_HEADER_NAME);
                        if (lastModHeader != null) {
                            Date d = DateUtils.parseDate(lastModHeader.getValue());
                            response.mResponseLastModTime = d.getTime();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //get the content headers

                    if (entity.getContentEncoding() != null)
                        response.mResponseContentEncoding = entity.getContentEncoding().getValue();
                    if (entity.getContentType() != null)
                        response.mResponseContentType = entity.getContentType().getValue();
                    response.mResponseContentLength = entity.getContentLength();

                    //get the content (if its not a head request)
                    if (mRequestType != REQUEST_TYPE_HEAD) {
                        if (mRaw) {
                            File tmpFile = File.createTempFile("httpdata", null, mContext.getFilesDir());
                            DataHelper.copyFromStreamToFile(entity.getContent(), tmpFile);
                            response.mResponseFile = tmpFile;
                        } else {
                            response.mResponseText = DataHelper.getTextFromStream(entity.getContent());
                        }

                        entity.consumeContent();
                        response.mSuccesful = true;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                response.mResponseText = e.getMessage();
                response.mSuccesful = false;
            }
        }

        return response;
    }

    private void doFileUploadRequest(DBHttpResponse response) throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(mUrl);
        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        //not sure if we'll ever need this again, but keep it here, why don'tcha
//		if (mPostParams != null) {
//			for (int i = 0; i<mPostParams.size(); i++) {
//				NameValuePair nvp = mPostParams.get(i);
//			}
//		}

        if (mHeaders != null) {
            for (String key : mHeaders.keySet())
                request.addHeader(key, mHeaders.get(key));
        }

        if (mEntityType != null)
            entity.addPart("string", new StringBody(mStringEntity, "applicatoin/json", Charset.forName(HTTP.UTF_8)));

        if (mFilesToUpload != null) {
            int numOfFiles = mFilesToUpload.size();
            for (int i = 0; i < numOfFiles; i++)
                entity.addPart(mFilesToUpload.get(i).mParamName, new FileBody(mFilesToUpload.get(i).mFile));
        }

        request.setEntity(entity);

        // Execute HTTP Post Request
        HttpResponse httpResponse = client.execute(request);
        HttpEntity responseEntity = httpResponse.getEntity();
        response.mResponseCode = httpResponse.getStatusLine().getStatusCode();
        if (response.mResponseCode != 200 && response.mResponseCode != 206) {
            response.mResponseText = EntityUtils.toString(responseEntity);
            response.mSuccesful = false;
        } else {
            response.mResponseContentEncoding = responseEntity.getContentEncoding() != null ? responseEntity.getContentEncoding().getValue() : "";
            response.mResponseContentLength = responseEntity.getContentLength();
            response.mResponseContentType = responseEntity.getContentType() != null ? responseEntity.getContentType().getValue() : "";
            response.mResponseLastModTime = 0;

            if (mRaw) {
                File tmpFile = File.createTempFile("httpdata", null, mContext.getFilesDir());
                DataHelper.copyFromStreamToFile(responseEntity.getContent(), tmpFile);
                response.mResponseFile = tmpFile;
            } else {

                response.mResponseText = EntityUtils.toString(responseEntity);
            }
            response.mSuccesful = true;
        }

    }

    public class DBHttpResponse {
        private MiluHttpRequest mRequest;
        private boolean mSuccesful;
        private int mResponseCode;
        private String mResponseText;
        private File mResponseFile;
        private String mResponseContentType, mResponseContentEncoding;
        private long mResponseLastModTime, mResponseContentLength;

        public DBHttpResponse(MiluHttpRequest request) {
            mRequest = request;
            mSuccesful = false;
            mResponseCode = -1;
            mResponseText = null;
            mResponseFile = null;
            mResponseContentType = null;
            mResponseContentEncoding = null;
            mResponseLastModTime = 0;
        }

        public boolean wasSuccesful() {
            return mSuccesful;
        }

        public MiluHttpRequest getRequest() {
            return mRequest;
        }

        public Object getUserInfo() {
            return mRequest.getUserInfo();
        }

        public int getResponseCode() {
            return mResponseCode;
        }

        public String getResponseText() {
            return mResponseText;
        }

        public File getResponseFile() {
            return mResponseFile;
        }

        public boolean isRaw() {
            return mRequest.isRaw();
        }

        public String getResponseContentType() {
            return mResponseContentType;
        }

        public String getResponseContentEncoding() {
            return mResponseContentEncoding;
        }

        public long getResponseLastModTime() {
            return mResponseLastModTime;
        }

        public long getResponseContentLength() {
            return mResponseContentLength;
        }

        public int getRequestCode() {
            return mRequest.getRequestCode();
        }

        public Context getContext() {
            return mRequest.getContext();
        }
    }


    public static class DBHttpUploadFile {
        private File mFile;
        private String mParamName;
        private String mFilenameToSendAs;

        public DBHttpUploadFile(File file, String paramName) {
            initDBHttpUploadFile(file, paramName, file.getName());
        }

        public DBHttpUploadFile(File file, String paramName, String filenameToSendAs) {
            initDBHttpUploadFile(file, paramName, filenameToSendAs);
        }

        public DBHttpUploadFile(String filepath, String paramName) {
            File f = new File(filepath);
            initDBHttpUploadFile(f, paramName, f.getName());
        }

        public DBHttpUploadFile(String filepath, String paramName, String filenameToSendAs) {
            File f = new File(filepath);
            initDBHttpUploadFile(f, paramName, filenameToSendAs);
        }

        private void initDBHttpUploadFile(File file, String paramName, String filenameToSend) {
            if (file == null) {
                throw new NullPointerException("Cannot create a DBHttpUpload file with a null file.");
            }
            if (!file.exists()) {
                throw new RuntimeException(new FileNotFoundException("The file you are trying to upload does not exist - fullpath:" + file.getAbsolutePath()));
            }
            if (paramName == null || paramName.length() < 1) {
                throw new NullPointerException("Cannot create a DBHttpUpload file with a null or blank param name.");
            }
            if (filenameToSend == null || filenameToSend.length() < 1) {
                throw new NullPointerException("Cannot create a DBHttpUpload file with a null or blank filenameToSend.");
            }
            mFile = file;
            mParamName = paramName;
            mFilenameToSendAs = filenameToSend;
        }

        public File getFile() {
            return mFile;
        }

        public String getParamName() {
            return mParamName;
        }

        public String getFilenameToSendAs() {
            return mFilenameToSendAs;
        }
    }


    private class DBHttpRequestTask extends AsyncTask<Void, Void, Void> {
        private MiluHttpRequest mRequest;
        private DBHttpResponse mResponse;

        public DBHttpRequestTask(MiluHttpRequest request) {
            mRequest = request;
        }


        @Override
        protected Void doInBackground(Void... arg0) {
            mResponse = mRequest.execute();
            mRequest.mAsyncTask = null;
            if (mResponse.wasSuccesful()) {
                try {
                    mRequest.getListener().onHttpRequestSuccessInBackground(mResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    mResponse.mSuccesful = false;
                    mRequest.getListener().onHttpRequestFailedInBackground(mResponse);
                }
            } else {
                mRequest.getListener().onHttpRequestFailedInBackground(mResponse);
            }

            if (mRequest.getListener() != null) {
                if (mResponse.wasSuccesful()) {
                    mRequest.getListener().onHttpRequestSuccess(mResponse);
                } else {
                    mRequest.getListener().onHttpRequestFailed(mResponse);
                }
            }

            return null;
        }
    }
}
