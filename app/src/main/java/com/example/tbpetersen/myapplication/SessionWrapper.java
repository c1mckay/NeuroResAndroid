package com.example.tbpetersen.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionWrapper{

  public static final String BASE_URL = "http://neurores.ucsd.edu:3000";
  public static final String LOGIN_ENDPOINT = "/login";
  public static final String GET_USERS_ENDPOINT = "/users_list";
  public static final String CONVERSATIONS_ENDPOINT = "/conversation_data";


  private String loginToken;
  // Used as a context and also for calling onUsersLoaded / onConversationsLoaded
  private MainActivity mainActivity;

  /**
   * Create a session using a valid loginToken and mainActivity
   * (In the main activity, the login activity should have already gotten a login token. Use this
   * token to create a sessionWrapper in the main activity)
   * @param mainActivity
   * @param loginToken
     */
  SessionWrapper(MainActivity mainActivity, String loginToken){
    this.loginToken = loginToken;
    this.mainActivity = mainActivity;
  }

  /**
   * Gets a login token for the supplied username
   * @param username The user to get a login token for
   * @return The loginToken for the user
     */
  public static String getLoginToken(String username){
      List<Pair<String,String>> headers = new ArrayList<Pair<String, String>>();
      headers.add(new Pair("auth", username));

      return request("POST", LOGIN_ENDPOINT, headers);
  }

  /**
   * Send a POST request to /users_list. Data received a JSON array of users. Once the POST request
   * has finished, onUsersLoaded is called in the mainActivity(from within HTTPRequestTread)
   */
  public void  updateUsers(){
    HTTPRequestThread httpRequestThread = new HTTPRequestThread(GET_USERS_ENDPOINT);
    httpRequestThread.execute((Void) null);
  }

  /**
   * Send a POST request to /conversation_data. Data received a JSON array of users. Once the POST
   * request has finished, onConversationsLoaded is called in the mainActivity(from within
   * HTTPRequestTread)
   */
  public void  updateConversations(){
    HTTPRequestThread httpRequestThread = new HTTPRequestThread(CONVERSATIONS_ENDPOINT);
    httpRequestThread.execute((Void) null);
  }

  /**
   *
   * @param requestType Should be either POST of GET
   * @param endpoint endpoint at neurores.ucsd.edu (For example /login or /conversation_data)
   * @param headers The headers to add to the request (usually need to include 'auth' : loginToken)
   * @return The body of the response from the server
   */
  private static String request(String requestType, String endpoint, List<Pair<String,String>> headers){
    int code = 0;
    String message = "";

    try{

      URL url = new URL(BASE_URL + endpoint);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod(requestType);
      if(requestType.toLowerCase().equals("post")){
          con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
      }

      for(Pair p : headers){
          con.setRequestProperty((String)p.first, (String)p.second);
      }

      con.setUseCaches(false);

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

  /**
   * Class for doing async HTTP requests. The thread will request data form the server and then
   * call the appropriate method for mainActivity (mainActivity is a listener) For example,
   * onConversationsLoaded is called when a thread with and endpoint of /conversation_data is done
   * executing
   */
  class HTTPRequestThread extends AsyncTask<Void,Void, Boolean>{

    // The endpoint that will be used in the request
    String endpoint;

    /**
     * Single argument ctor
     * @param endpoint The endpoint that will be used in the request
       */
    HTTPRequestThread(String endpoint){
      this.endpoint = endpoint;
    }

    /**
     * Makes a request to the endpoint that was passed to this thread when it was created
     * @param params
     * @return
       */
    @Override
    protected Boolean doInBackground(Void... params) {

      List<Pair<String,String>> headers = new ArrayList<Pair<String, String>>();
      /* Add the auth header for this request */
      headers.add(new Pair( (String) "auth", (String) loginToken));
      /* Make the request */
      String jsonString = request("POST", endpoint, headers);


      /* Create user objects using the JSON from the server response
         Alert main activity that this request has been made */
      switch (endpoint){

        case GET_USERS_ENDPOINT:
          getUsers(jsonString);
          break;

        case CONVERSATIONS_ENDPOINT:
          getConversations(jsonString);
          break;
      }

      return null;
    }

    private void getUsers(String jsonString){
      List<User> userList = new ArrayList<User>();
      try{
        JSONArray userJSONArray = new JSONArray(jsonString);
        for(int i = 0; i < userJSONArray.length(); i++){
          JSONObject current = userJSONArray.getJSONObject(i);
          String name = current.getString("email");
          String userType = current.getString("user_type");
          long userId = current.getLong("user_id");
          userList.add(new User(mainActivity, userId,name, userType));
        }
      }catch( Exception e){
        Log.v("tag", "Failed to get JSONArray from json from " + endpoint);
      }

      mainActivity.onUsersLoaded(userList);
    }

    private void getConversations(String jsonString){
      List<Conversation> conversationList = new ArrayList<Conversation>();
      try{
        /* The conversations in json form*/
        JSONObject conversationJSONObject = new JSONObject(jsonString);

        /* Calling "next" on the iterator will return the id of the next conversation (id in string form) */
        Iterator<String> iterator = conversationJSONObject.keys();
        Conversation currentConversation = null;
        // Iterate over all conversations
        while(iterator.hasNext()){
          String conversationId = iterator.next();
          currentConversation = new Conversation(new Long(conversationId));
          JSONArray currentArray = conversationJSONObject.getJSONArray(conversationId);
          // Iterate over users in conversations
          for(int i = 0; i < currentArray.length(); i++){
            long currentUserId = currentArray.getLong(i);

            User currentUser = mainActivity.currentConversations.get(currentUserId);
            if(currentUser != null && currentUser != mainActivity.loggedInUser){
              currentConversation.users.add(currentUser);
            }
          }
            conversationList.add(currentConversation);

        }

      }catch( Exception e){
        Log.v("tag", "Failed to get JSONArray from json from " + endpoint);
      }

      mainActivity.onConversationsLoaded(conversationList);
    }

  }

}
