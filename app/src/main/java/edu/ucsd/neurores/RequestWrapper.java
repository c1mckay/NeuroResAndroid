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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocket;

// Stops complaints that "<Foo> can be replaced with <>"
@SuppressWarnings("Convert2Diamond")

class RequestWrapper {
  static final String BASE_URL = "neurores.ucsd.edu";
  private static final String LOGIN_ENDPOINT = "/login";
  private static final String GET_USERS_ENDPOINT = "/users_list";
  private static final String CONVERSATIONS_ENDPOINT = "/conversation_data";
  private static final String CONVERSATION_CONTENT_ENDPOINT = "/get_messages";
  private static final String CREATE_CONVERSATION = "/start_conversation";
  private static final String WIPE_CONVERSATION = "/wipe_conversation";
  private static final String MARK_SEEN = "/mark_seen";
  private static final String SERVER_CHECK = "/";

  private static final String AUTH_HEADER_KEY = "auth";
  private static final String POST_REQUEST = "POST";
  private static final String GET_REQUEST = "GET";

  static final String ERROR_UNAUTHORIZED = "unauthorized";
  static final String ERROR_BAD_REQUEST = "bad request";
  static final String ERROR_INTERNAL_SERVER = "internal server error";




  static void GetLoginToken(Context context, String loginCredentials, final OnHTTPRequestCompleteListener ocl){
    String firebaseTokenData = getFirebaseTokenData();

    HTTPRequestThread requestThread = new HTTPRequestThread(context, loginCredentials, POST_REQUEST, ocl);
    requestThread.setData(firebaseTokenData);
    requestThread.execute(LOGIN_ENDPOINT);
  }

  static void GetConversationData(Context context, long id, String token, OnHTTPRequestCompleteListener ocl){
    new HTTPRequestThread(context, token,POST_REQUEST, ocl).setData(Long.toString(id)).execute(CONVERSATION_CONTENT_ENDPOINT);
  }

  static void CreateConversation(Context context, List<Long> users, String token, OnHTTPRequestCompleteListener ocl){
    new HTTPRequestThread(context, token,POST_REQUEST, ocl).setData(new JSONArray(users).toString()).execute(CREATE_CONVERSATION);
  }

  static void markConversationSeen(Context context, long id, String token, OnHTTPRequestCompleteListener ocl){
    new HTTPRequestThread(context, token, POST_REQUEST, ocl).setData(Long.toString(id)).execute(MARK_SEEN);
  }

  static void checkServerIsOnline(Context context, OnHTTPRequestCompleteListener ocl){
    new HTTPRequestThread(context, "", GET_REQUEST, ocl).execute(SERVER_CHECK);
  }

  static void UpdateUsers(Context context, String token, OnHTTPRequestCompleteListener oci){
    HTTPRequestThread httpRequestThread = new HTTPRequestThread(context, token, POST_REQUEST, oci);
    httpRequestThread.execute(GET_USERS_ENDPOINT);
  }

  static void  UpdateConversations(Context context, String token, OnHTTPRequestCompleteListener oci){
    new HTTPRequestThread(context, token, POST_REQUEST, oci).execute(CONVERSATIONS_ENDPOINT);
  }

  static void WipeConversation(Context context, long id, String token, OnHTTPRequestCompleteListener ocl){
    new HTTPRequestThread(context, token,POST_REQUEST, ocl).setData(Long.toString(id)).execute(WIPE_CONVERSATION);
  }

  private static String getFirebaseTokenData(){
    String firebaseTokenData = null;
    if(FirebaseInstanceId.getInstance().getToken() != null) {
      try {
        JSONObject firebaseToken = new JSONObject();
        firebaseToken.put("android_token", FirebaseInstanceId.getInstance().getToken());
        firebaseTokenData = firebaseToken.toString();
      }catch(JSONException e){
        firebaseTokenData = null;
      }
    }
    return firebaseTokenData;
  }

  interface OnCompleteListener{
    void onComplete(String s);
    void onError(String s);
  }

  interface OnHTTPRequestCompleteListener{
    void onComplete(String s);
    void onError(int i);
  }

  /*************************************************************************************************
   * Class for doing async HTTP requests. The thread will request data form the server and then
   * call the appropriate method for mainActivity (mainActivity is a listener) For example,
   * onConversationsLoaded is called when a thread with and endpoint of /conversation_data is done
   * executing
   ************************************************************************************************/
  private static class HTTPRequestThread extends AsyncTask<String,Void, String>{

    private String token;
    private String requestType;
    private OnHTTPRequestCompleteListener ocl;
    private Context context;
    private boolean requestFailed;
    private int resultCode;
    private List<Pair<String, String>> headers;



    HTTPRequestThread(Context context, String token,String requestType, OnHTTPRequestCompleteListener ocl){
      this.ocl = ocl;
      this.token = token;
      this.context = context;
      this.requestType = requestType;
      this.requestFailed = false;
      this.headers = new ArrayList<Pair<String, String>>();
      this.resultCode = -1;

      addAuthHeader();
    }

    /**
     * Makes a request to the endpoint that was passed to this thread when it was created
     * @param endpoints the endpoint to talk to
     * @return data from the server
     */
    @Override
    protected String doInBackground(String... endpoints) {
      String endpoint = endpoints[0];

      return request(requestType, BASE_URL,endpoint, headers, data, context);

    }

    protected void onPostExecute(String result){
      if(requestFailed){
        ocl.onError(resultCode);
      }else{
        ocl.onComplete(result);
      }
    }

    private void setFailure(boolean failed){
      this.requestFailed = failed;
    }

    private void addAuthHeader(){
      addHeader(AUTH_HEADER_KEY, token);
    }

    private void addHeader(String key, String value){
      for(int i = 0; i < headers.size(); i++){
        if(headers.get(i).first.equals(key)){
          headers.remove(i);
          headers.add(new Pair<String, String>(key,value));
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

    private String request(String requestType, String hostName, String endpoint, List<Pair<String,String>> headers, String data, Context context){
      String blankLine = "\r\n\r\n";

      NeuroSSLSocketFactory neuroSSLSocketFactory = new NeuroSSLSocketFactory(context);
        //noinspection deprecation
        SSLSocketFactory sslSocketFactory = neuroSSLSocketFactory.createAdditionalCertsSSLSocketFactory();
      try {
        String request = "";
        Socket sock = new Socket(hostName, 443);
        SSLSocket socketSSL = (SSLSocket) sslSocketFactory.createSocket(sock, hostName, 443, false);
        PrintWriter pw = new PrintWriter(socketSSL.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(socketSSL.getInputStream(), "UTF-8"));

        request += requestType + " " + endpoint + " HTTP/1.1\r\n";
        pw.print(requestType + " " + endpoint + " HTTP/1.1\r\n");
        request += "Host: " + hostName + "\r\n";
        request += "Connection: close\r\n";
        pw.print("Host: " + hostName + "\r\n");
        pw.print("Connection: close\r\n");
        for (Pair<String, String> p : headers) {
          pw.print(p.first + ": " + p.second + "\r\n");
          request += (p.first + ": " + p.second + "\r\n");
        }

        if (requestType.toUpperCase().equals(POST_REQUEST)) {
          pw.print("Content-Type: application/json\r\n");
          request += "Content-Type: application/json\r\n";
          if (data != null) {
            pw.print("Content-Length: " + data.length() + "\r\n");
            request += "Content-Length: " + data.length() + "\r\n";

            pw.print("\r\n" + data);
            request += "\r\n" + data;
          }
        }
        pw.write("\r\n");
        request += "\r\n";
        pw.flush();

        Log.v("requestr", request);

        int nextChar;
        String response = "";
        while ((nextChar = br.read()) != -1) {
          response += (char) nextChar;
        }

        Log.v("requestr", response + "\n");

        resultCode = getResponseCode(response);
        if(resultCode > 300){
          throw new HTTPRequestException();
        }

        if (response.contains("Content-Length: ")) {
          int index = response.indexOf("Content-Length: ") + "Content-Length: ".length();
          response = response.substring(index);
          if (!response.contains("\r\n")) {
            throw new Exception("Malformed response");
          }
          index = response.indexOf("\r\n");
          int contentLength = Integer.parseInt(response.substring(0, index));
          response = response.substring(index + "\r\n".length() + 2);
          response = response.substring(0, contentLength);
        } else {
          response = response.substring(response.indexOf(blankLine) + blankLine.length());
        }
        if (response.toLowerCase().equals("invalid token")) {
          throw new UnauthorizedException("Invalid login token");
        }
        return response;
      }catch (HTTPRequestException e){
        Log.v("taggy", "HttpRequestException");
        setFailure(true);
        return getErrorString(resultCode);
      }catch (ConnectException e){
        Log.v("taggy", "Connect exception");
        setFailure(true);
        return "Connect exception";
      }catch(Exception e){
        Log.v("taggy", "Fail");
        Log.v("taggy", e.getMessage());
        e.printStackTrace();
        setFailure(true);
        return null;
      }
    }

    private int getResponseCode(String response) {
      int index = response.indexOf(" ");
      if(index == -1){
        return 0;
      }
      response = response.substring(index + 1);

      index = response.indexOf(" ");
      if(index == -1){
        return 0;
      }
      response = response.substring(0, index);

      try{
        return Integer.parseInt(response);
      }catch (NumberFormatException e){
        return 0;
      }
    }

    private String getErrorString(int responseCode){
      String error = "";

      switch (responseCode){
        case 400:
          error = ERROR_BAD_REQUEST;
          break;
        case 401:
          error = ERROR_UNAUTHORIZED;
          break;
        case 500:{
          error = ERROR_INTERNAL_SERVER;
          break;
        }
        default:
          error = "Unknown error: " + responseCode;
      }

      return error;
    }
  }

}
