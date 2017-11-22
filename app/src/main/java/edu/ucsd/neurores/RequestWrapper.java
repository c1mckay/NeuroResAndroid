package edu.ucsd.neurores;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;

// Stops complaints that "<Foo> can be replaced with <>"
@SuppressWarnings("Convert2Diamond")
class RequestWrapper {
    static final String BASE_URL = "neurores.ucsd.edu";
    private static final String REGISTER_ANDROID_TOKEN_ENDPOINT = "/static/android_token";

    private static final String GET_USERS_ENDPOINT = "/static/users_list";
    private static final String CONVERSATIONS_ENDPOINT = "/static/conversation_data";
    private static final String CONVERSATION_CONTENT_ENDPOINT = "/static/get_messages";
    private static final String CREATE_CONVERSATION = "/static/start_conversation";
    private static final String WIPE_CONVERSATION = "/static/wipe_conversation";
    private static final String MARK_SEEN = "/static/mark_seen";
    private static final String SERVER_CHECK = "/static/privacy.html";

    private static final String AUTH_HEADER_KEY = "auth";
    private static final String POST_REQUEST = "POST";
    private static final String GET_REQUEST = "GET";

    static void login(String username, String password,RequestWrapper.OnCompleteListener ocl){
        LoginTask loginTask = new LoginTask(username, password, ocl);
        loginTask.execute();
    }

    static void registerFirebaseToken(Context context, String token, final OnHTTPRequestCompleteListener ocl){
        HTTPRequestThread httpRequestThread = new HTTPRequestThread(context, token, POST_REQUEST, ocl);
        httpRequestThread.setData(getFirebaseTokenData());
        httpRequestThread.execute(REGISTER_ANDROID_TOKEN_ENDPOINT);
    }

    static void GetConversationData(Context context, long id, String token, OnHTTPRequestCompleteListener ocl) {
        new HTTPRequestThread(context, token, POST_REQUEST, ocl).setData(Long.toString(id)).execute(CONVERSATION_CONTENT_ENDPOINT);
    }

    static void CreateConversation(Context context, List<Long> users, String token, OnHTTPRequestCompleteListener ocl) {
        new HTTPRequestThread(context, token, POST_REQUEST, ocl).setData(new JSONArray(users).toString()).execute(CREATE_CONVERSATION);
    }

    static void markConversationSeen(Context context, long id, String token, OnHTTPRequestCompleteListener ocl) {
        new HTTPRequestThread(context, token, POST_REQUEST, ocl).setData(Long.toString(id)).execute(MARK_SEEN);
    }

    static void checkServerIsOnline(Context context, OnHTTPRequestCompleteListener ocl) {
        HTTPRequestThread thread = new HTTPRequestThread(context, null, GET_REQUEST, ocl);
        thread.execute(SERVER_CHECK);
    }

    static void UpdateUsers(Context context, String token, OnHTTPRequestCompleteListener oci) {
        HTTPRequestThread httpRequestThread = new HTTPRequestThread(context, token, POST_REQUEST, oci);
        httpRequestThread.execute(GET_USERS_ENDPOINT);
    }

    static void UpdateConversations(Context context, String token, OnHTTPRequestCompleteListener oci) {
        new HTTPRequestThread(context, token, POST_REQUEST, oci).execute(CONVERSATIONS_ENDPOINT);
    }

    static void WipeConversation(Context context, long id, String token, OnHTTPRequestCompleteListener ocl) {
        new HTTPRequestThread(context, token, POST_REQUEST, ocl).setData(Long.toString(id)).execute(WIPE_CONVERSATION);
    }

    private static String getFirebaseTokenData() {
        String firebaseTokenData = null;
        if (FirebaseInstanceId.getInstance().getToken() != null) {
            try {
                JSONObject firebaseToken = new JSONObject();
                firebaseToken.put("android_token", FirebaseInstanceId.getInstance().getToken());
                firebaseTokenData = firebaseToken.toString();
            } catch (JSONException e) {
                firebaseTokenData = null;
            }
        }
        return firebaseTokenData;
    }

    interface OnCompleteListener {
        void onComplete(String s);

        void onError(String s);
    }

    interface OnHTTPRequestCompleteListener {
        void onComplete(String s);

        void onError(int i);
    }

    /*************************************************************************************************
     * Class for doing async HTTP requests. The thread will request data form the server and then
     * call the appropriate method for mainActivity (mainActivity is a listener) For example,
     * onConversationsLoaded is called when a thread with and endpoint of /conversation_data is done
     * executing
     ************************************************************************************************/
    private static class HTTPRequestThread extends AsyncTask<String, Void, String> {

        private String token;
        private String requestType;
        private String endpoint;
        private OnHTTPRequestCompleteListener ocl;
        private Context context;
        private boolean requestFailed;
        private int resultCode;
        private List<Pair<String, String>> headers;


        HTTPRequestThread(Context context, String token, String requestType, OnHTTPRequestCompleteListener ocl) {
            this.ocl = ocl;
            this.token = token;
            this.context = context;
            this.requestType = requestType;
            this.requestFailed = false;
            this.headers = new ArrayList<Pair<String, String>>();
            this.resultCode = -1;

            if(token != null){
                addAuthHeader();
            }
        }

        /**
         * Makes a request to the endpoint that was passed to this thread when it was created
         *
         * @param endpoints the endpoint to talk to
         * @return data from the server
         */
        @Override
        protected String doInBackground(String... endpoints) {
            endpoint = endpoints[0];

            return request(requestType, BASE_URL, endpoint, headers, data, context);

        }

        protected void onPostExecute(String result) {
            if(ocl == null){
                return;
            }

            if (requestFailed) {
                ocl.onError(resultCode);
            } else {
                ocl.onComplete(result);
            }
        }

        private void setFailure(boolean failed) {
            this.requestFailed = failed;
        }

        private void addAuthHeader() {
            addHeader(AUTH_HEADER_KEY, token);
        }

        private void addHeader(String key, String value) {
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).first.equals(key)) {
                    headers.remove(i);
                    headers.add(new Pair<String, String>(key, value));
                    return;
                }
            }
            headers.add(new Pair<String, String>(key, value));
        }


        private String data;

        public HTTPRequestThread setData(String data) {
            this.data = data;
            return this;
        }

        private String request(String requestType, String hostName, String endpoint, List<Pair<String, String>> headers, String data, Context context) {
            HttpsURLConnection con = null;
            try {

                URL url = new URL("https://" + hostName + endpoint);
                con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod(requestType);
                for(Pair<String, String> pair : headers){
                    con.addRequestProperty(pair.first, pair.second);
                }
                if(data != null) {
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.write(data.getBytes("UTF-8"));
                    wr.flush();
                    wr.close();
                }
                con.connect();

                BufferedReader in;

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                con.disconnect();

                return response.toString();


            } catch (Exception e) {
                Log.v("taggy", "Fail");
                Log.v("taggy", e.getMessage());
                e.printStackTrace();
                setFailure(true);
                return null;
            }
        }

    }

}
