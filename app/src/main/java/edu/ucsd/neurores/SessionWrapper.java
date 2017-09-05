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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLSocket;

public class SessionWrapper{

  public static final String BASE_URL = "neurores.ucsd.edu";
  public static final String LOGIN_ENDPOINT = "/login";
  public static final String GET_USERS_ENDPOINT = "/users_list";
  public static final String CONVERSATIONS_ENDPOINT = "/conversation_data";
  public static final String CONVERSATION_CONTENT_ENDPOINT = "/get_messages";
  public static final String CREATE_CONVERSATION = "/start_conversation";
  public static final String MARK_SEEN = "/mark_seen";
  public static final String SERVER_CHECK = "/";

  /**
   * Create a session using a valid loginToken and mainActivity
   * (In the main activity, the login activity should have already gotten a login token. Use this
   * token to create a sessionWrapper in the main activity)
   * @param mainActivity
     */


  /**
   * Gets a login token for the supplied username
   * @param username The user to get a login token for
   * @return The loginToken for the user
     */
  static String getLoginToken(Context context, String username){
    List<Pair<String,String>> headers = new ArrayList<>();
      headers.add(new Pair<>("auth", username));

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
    if(context == null){
      Log.v("contextp", "Error getting login token");
    }
      return request("POST", BASE_URL,LOGIN_ENDPOINT, headers, firebaseTokenData, context);
  }

  public static void GetConversationData(Context context, long id, String token, OnCompleteListener ocl){
    new HTTPRequestThread(context, token, ocl).setData(Long.toString(id)).execute(CONVERSATION_CONTENT_ENDPOINT);
  }

  public static void CreateConversation(Context context, List<Long> users, String token, OnCompleteListener ocl){
    new HTTPRequestThread(context, token, ocl).setData(new JSONArray(users).toString()).execute(CREATE_CONVERSATION);
  }

  public static void markConversationSeen(Context context, long id, String token, OnCompleteListener ocl){
    new HTTPRequestThread(context, token, ocl).setData(Long.toString(id)).execute(MARK_SEEN);
  }

  public static void checkServerIsOnline(Context context, OnCompleteListener ocl){
    new HTTPRequestThread(context, "", "GET", ocl).execute(SERVER_CHECK);
  }

  /**
   * Send a POST request to /users_list. Data received a JSON array of users. Once the POST request
   * has finished, onUsersLoaded is called in the mainActivity(from within HTTPRequestTread)
   */
  public static void UpdateUsers(Context context, String token, OnCompleteListener oci){
    HTTPRequestThread httpRequestThread = new HTTPRequestThread(context, token, oci);
    httpRequestThread.execute(GET_USERS_ENDPOINT);
  }

  /**
   * Send a POST request to /conversation_data. Data received a JSON array of users. Once the POST
   * request has finished, onConversationsLoaded is called in the mainActivity(from within
   * HTTPRequestTread)
   */
  public static void  UpdateConversations(Context context, String token, OnCompleteListener oci){
    new HTTPRequestThread(context, token, oci).execute(CONVERSATIONS_ENDPOINT);
  }

  /**
   *
   * @param requestType Should be either POST of GET
   * @param endpoint endpoint at neurores.ucsd.edu (For example /login or /conversation_data)
   * @param headers The headers to add to the request (usually need to include 'auth' : loginToken)
   * @param data the data to put into the HTTP packet
   * @return The body of the response from the server
   */
  private static String request(String requestType, String hostName, String endpoint, List<Pair<String,String>> headers, String data, Context context){
    int code = 0;
    String message = "";
    String blankLine = "\r\n\r\n";

    NeuroSSLSocketFactory neuroSSLSocketFactory = new NeuroSSLSocketFactory(context);
    SSLSocketFactory sslSocketFactory = neuroSSLSocketFactory.createAdditionalCertsSSLSocketFactory();
    try {
      String request = "";
      Socket sock = new Socket();
      sock.connect(new InetSocketAddress(hostName, 443), 1000);
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

      if (requestType.toLowerCase().equals("post")) {
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
        throw new InvalidLoginTokenException("Invalid login token");
      }
      return response;
    }catch (ConnectException e){
      Log.v("taggy", "Socket Timeout");
      return "SocketTimeout";
    }catch (SocketTimeoutException e){
      Log.v("taggy", "Socket Timeout");
      return "SocketTimeout";
    }catch(Exception e){
      Log.v("taggy", "Fail");
      Log.v("taggy", e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  public static boolean isValidJSON(String test) {
    try {
      new JSONObject(test);
    } catch (JSONException ex) {
      // edited, to include @Arthur's comment
      // e.g. in case JSONArray is valid as well...
      try {
        new JSONArray(test);
      } catch (JSONException ex1) {
        return false;
      }
    }
    return true;
  }

  public static boolean isJSONObject(String s){
    try{
      JSONObject jsonObject = new JSONObject(s);
      return true;
    }catch (JSONException e){
      return false;
    }
  }


  private static void writeData(String data, URLConnection conn) throws IOException {
    OutputStream os = conn.getOutputStream();
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    writer.write(data);
    writer.flush();
    writer.close();
    os.close();
  }


  interface OnCompleteListener{
    void onComplete(String s);
    void onError(String s);
  }

  /**
   * Class for doing async HTTP requests. The thread will request data form the server and then
   * call the appropriate method for mainActivity (mainActivity is a listener) For example,
   * onConversationsLoaded is called when a thread with and endpoint of /conversation_data is done
   * executing
   */
  static class HTTPRequestThread extends AsyncTask<String,Void, String>{

    // The endpoint that will be used in the request
    String token;
    String requestType;
    OnCompleteListener ocl;
    Context context;

    /**
     * Single argument ctor
     * @param token the auth token for the request
     * @ocl listener what to do when the request is completed
     */
    HTTPRequestThread(Context context, String token, OnCompleteListener ocl){
      this.ocl = ocl;
      this.token = token;
      this.context = context;
      this.requestType = "POST";
    }

    HTTPRequestThread(Context context, String token,String requestType, OnCompleteListener ocl){
      this.ocl = ocl;
      this.token = token;
      this.context = context;
      this.requestType = requestType;
    }

    /**
     * Makes a request to the endpoint that was passed to this thread when it was created
     * @param endpoints the endpoint to talk to
     * @return
       */
    @Override
    protected String doInBackground(String... endpoints) {
      String endpoint = endpoints[0];

      List<Pair<String, String>> headers = new ArrayList<Pair<String, String>>();
      /* Add the auth header for this request */
      headers.add(new Pair<>("auth", token));
      /* Make the request */

      return request(requestType, BASE_URL,endpoint, headers, data, context);

    }

    protected void onPostExecute(String result){
      if(result != null && result.equals("SocketTimeout")){
        ocl.onError(result);
        return;
      }
      if(ocl != null){
        ocl.onComplete(result);
      }
    }


    private String data;
    public HTTPRequestThread setData(String data) {
      this.data = data;
      return this;
    }
  }

  static List<User> GetUserList(String jsonString){
    List<User> userList = new ArrayList<User>();

    try{
      JSONArray userJSONArray = new JSONArray(jsonString);
      for(int i = 0; i < userJSONArray.length(); i++){
        JSONObject current = userJSONArray.getJSONObject(i);
        String name = current.getString("email");
        String userType = current.getString("user_type");
        long userId = current.getLong("user_id");
        boolean isOnline = current.getBoolean("isOnline");
        userList.add(new User(null, userId,name, userType, isOnline));
      }
    }catch( Exception e){
      Log.v("tag", "Failed to get JSONArray from json from " + GET_USERS_ENDPOINT);
      e.printStackTrace();
    }
    return userList;
  }

  public static List<Conversation> TranslateConversationMetadata(String jsonString, HashMap<Long,User> userList){
    List<Conversation> conversationList = new ArrayList<Conversation>();

    try{
        /* The conversations in json form*/
      JSONObject conversationJSONObject = new JSONObject(jsonString);

        /* Calling "next" on the iterator will return the id of the next conversation (id in string form) */
      Iterator<String> iterator = conversationJSONObject.keys();
      Conversation currentConversation = null;
      User user;
      String userID_s;
      long userID;
      // Iterate over all conversations
      while(iterator.hasNext()){
        String conversationId = iterator.next();
        currentConversation = new Conversation(Long.valueOf(conversationId), null);
        //currentConversation.setUnread(conversationJsonObject.lastUnread);
        JSONObject currentConversationObject = conversationJSONObject.getJSONObject(conversationId);
        String stringUnseen = currentConversationObject.getString("unseen_count");
        if(stringUnseen.equals("null")){
          currentConversation.setNumOfUnseen(0);
        }else{
          currentConversation.setNumOfUnseen(Integer.parseInt(stringUnseen));
        }

        JSONArray currentArray = currentConversationObject.getJSONArray("members");
        //JSONArray currentArray = conversationJSONObject.getJSONArray(conversationId).getJSONArray("members");
        // Iterate over users in conversations
        for(int i = 0; i < currentArray.length(); i++){
          userID_s = currentArray.get(i).toString();
          userID = Long.parseLong(userID_s);
          if(userList.containsKey(userID)){
            User u = userList.get(userID);
            currentConversation.addUser(u);
          }else{
            Log.v("warning", "User with id " + userID + " in a conversation but not in the user list");
          }
        }
        conversationList.add(currentConversation);
      }

    }catch( Exception e){
      Log.v("tag", "Failed to get JSONArray from json from " + CONVERSATIONS_ENDPOINT);
      e.printStackTrace();
    }

    return conversationList;
  }
}
