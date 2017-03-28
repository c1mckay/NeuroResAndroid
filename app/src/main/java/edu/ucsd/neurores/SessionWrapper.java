package edu.ucsd.neurores;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionWrapper{

  public static final String BASE_URL = "http://neurores.ucsd.edu:3000";
  public static final String LOGIN_ENDPOINT = "/login";
  public static final String GET_USERS_ENDPOINT = "/users_list";
  public static final String CONVERSATIONS_ENDPOINT = "/conversation_data";
  public static final String CONVERSATION_CONTENT_ENDPOINT = "/get_messages";
  public static final String CREATE_CONVERSATION = "/start_conversation";

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
  //I dont think you can/are supposed to do HTTP requests on the main thread
  static String getLoginToken(String username){
      List<Pair<String,String>> headers = new ArrayList<>();
      headers.add(new Pair<>("auth", username));

      return request("POST", LOGIN_ENDPOINT, headers, null);
  }

  public static void GetConversationData(long id, String token, OnCompleteListener ocl){
    new HTTPRequestThread(token, ocl).setData(Long.toString(id)).execute(CONVERSATION_CONTENT_ENDPOINT);
  }

  public static void CreateConversation(ArrayList<Long> users, String token, OnCompleteListener ocl){
    new HTTPRequestThread(token, ocl).setData(new JSONArray(users).toString()).execute(CREATE_CONVERSATION);
  }

  /**
   * Send a POST request to /users_list. Data received a JSON array of users. Once the POST request
   * has finished, onUsersLoaded is called in the mainActivity(from within HTTPRequestTread)
   */
  public static void UpdateUsers(String token, OnCompleteListener oci){
    HTTPRequestThread httpRequestThread = new HTTPRequestThread(token, oci);
    httpRequestThread.execute(GET_USERS_ENDPOINT);
  }

  /**
   * Send a POST request to /conversation_data. Data received a JSON array of users. Once the POST
   * request has finished, onConversationsLoaded is called in the mainActivity(from within
   * HTTPRequestTread)
   */
  public static void  UpdateConversations(String token, OnCompleteListener oci){
    new HTTPRequestThread(token, oci).execute(CONVERSATIONS_ENDPOINT);
  }

  /**
   *
   * @param requestType Should be either POST of GET
   * @param endpoint endpoint at neurores.ucsd.edu (For example /login or /conversation_data)
   * @param headers The headers to add to the request (usually need to include 'auth' : loginToken)
   * @param data the data to put into the HTTP packet
   * @return The body of the response from the server
   */
  private static String request(String requestType, String endpoint, List<Pair<String,String>> headers, String data){
    int code = 0;
    String message = "";

    try{

      URL url = new URL(BASE_URL + endpoint);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod(requestType);
      if(requestType.equalsIgnoreCase("post")){
          con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
      }

      for(Pair p : headers){
          con.setRequestProperty((String)p.first, (String)p.second);
      }

      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);

      if(data != null){
        writeData(data, con);
      }

      code = con.getResponseCode();
      message = con.getResponseMessage();
      Log.v("tag", code + "");
      if(code == 200){
        InputStream is = con.getInputStream();
        int readByte;
        String body = "";
        while( (readByte = is.read()) != -1){
          body += (char) readByte;
        }
        Log.v("tag", "Body: " + body);
        return body;
      }else{
        return null;
      }

    }catch(Exception e){
      Log.v("tag", e.toString());
      return null;
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
    OnCompleteListener ocl;

    /**
     * Single argument ctor
     * @param token the auth token for the request
     * @ocl listener what to do when the request is completed
     */
    HTTPRequestThread(String token, OnCompleteListener ocl){
      this.ocl = ocl;
      this.token = token;
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
      return request("POST", endpoint, headers, data);

    }

    protected void onPostExecute(String result){
        if(ocl != null)
            ocl.onComplete(result);
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
        userList.add(new User(null, userId,name, userType));
      }
    }catch( Exception e){
      Log.v("tag", "Failed to get JSONArray from json from " + GET_USERS_ENDPOINT);
      e.printStackTrace();
    }
    return userList;
  }

  public static List<Conversation> TranslateConversationMetadata(String jsonString){
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
        JSONArray currentArray = conversationJSONObject.getJSONArray(conversationId);
        //JSONArray currentArray = conversationJSONObject.getJSONArray(conversationId).getJSONArray("members");
        // Iterate over users in conversations
        for(int i = 0; i < currentArray.length(); i++){
          userID_s = currentArray.get(i).toString();
          userID = Long.parseLong(userID_s);

          currentConversation.addTemporaryUser(userID);
        }
        conversationList.add(currentConversation);
      }

    }catch( Exception e){
      Log.v("tag", "Failed to get JSONArray from json from " + CONVERSATIONS_ENDPOINT);
      e.printStackTrace();
    }

    return conversationList;
  }

  private void pushConversationMessages(JSONObject jo){

  }

}
