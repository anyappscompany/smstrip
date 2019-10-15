package ua.com.anyapps.smstrip;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;

public class AsyncTaskSendQuery extends AsyncTask<String, Void, Void> {
    private static final String TAG = "debapp";
    private SendQueryCompleted taskCompleted;
    private int result;

    private String messageText = "";
    private String phoneNumber = "";
    private Long currentTimeMillis;
    private String androidId = "";
    private String requestMethod = "";
    private String pathToScript = "";
    private String param1 = "";
    private String param2 = "";
    private String param3 = "";
    private String param4 = "";
    private String basicAuthLogin = "";
    private String basicAuthPassword = "";
    private String basicAuthStr = "";

    public AsyncTaskSendQuery(SendQueryCompleted context, String _messageText, String _phoneNumber, Long _currentTimeMillis, String _androidId, String _requestMethod, String _pathToScript, String _param1, String _param2, String _param3, String _param4, String _basicAuthLogin, String _basicAuthPassword) {
        this.taskCompleted = context;
        messageText = _messageText;
        phoneNumber = _phoneNumber;
        currentTimeMillis = _currentTimeMillis;
        androidId = _androidId;
        requestMethod = _requestMethod;
        pathToScript = _pathToScript;
        param1 = _param1;
        param2 = _param2;
        param3 = _param3;
        param4 = _param4;
        basicAuthLogin = _basicAuthLogin;
        basicAuthPassword = _basicAuthPassword;



    }

    @Override
    protected void onPostExecute(Void aVoid) {
        taskCompleted.SendQueryCompleted(result);
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(String... strings) {
        String userAgent = "Incoming SMS redirect to server";
        userAgent += " - " + androidId;

        if(basicAuthLogin.length()>0 && basicAuthPassword.length()>0){
            basicAuthStr = basicAuthLogin + ":" + basicAuthPassword;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        if(param1.length()>0){
            params.put(param1, URLEncoder.encode(messageText));
        }
        if(param2.length()>0){
            params.put(param2, URLEncoder.encode(phoneNumber));
        }
        if(param3.length()>0){
            params.put(param3, URLEncoder.encode(androidId));
        }
        if(param4.length()>0){
            params.put(param4, URLEncoder.encode(currentTimeMillis.toString()));
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", userAgent);

        switch(requestMethod){
            case "POST":
                result = Utilities.POSTQuery(pathToScript, params, headers, basicAuthStr);
                break;
            case "GET":
                result = Utilities.GETQuery(pathToScript, params, headers, basicAuthStr);
                break;
        }
        return null;
    }
}
