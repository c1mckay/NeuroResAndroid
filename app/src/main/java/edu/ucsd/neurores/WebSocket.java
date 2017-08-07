package edu.ucsd.neurores;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class WebSocket extends WebSocketClient {

    MainFragment mFrag;
    MainActivity mainActivity;
    WebSocket(MainFragment mFrag, MainActivity mainActivity) throws URISyntaxException {
        super(new URI("wss://neurores.ucsd.edu"));
        this.mFrag = mFrag;
        this.mainActivity = mainActivity;
    }


    public void onOpen(ServerHandshake handshakedata) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("greeting", mFrag.getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(jo.toString());
        //pushMessage("hello");
    }

    public void onMessage(String message) {
        JSONObject jo = null;
        Log.v("sockett", message);
        try {
            jo = new JSONObject(message);
            if(jo.has("userStatusUpdate") && jo.getBoolean("userStatusUpdate")){
                updateUserStatus(jo);
                return;
            }
            if(jo.getLong("conv_id") != mFrag.conversation.getID()){
                long conversationID = jo.getLong("conv_id");
                HashMap<Long,Conversation> currentConversations = mainActivity.currentConversations;

                for(Conversation c : currentConversations.values()){
                    Log.v("taggy", c.toString());
                }

                if(currentConversations.containsKey(conversationID)){
                    moveConversationToUnread(currentConversations.get(conversationID));
                }else{
                    createConversation(conversationID);
                }
                return;
            }
            message = jo.getString("text");
            String from = mFrag.conversation.getUser(jo.getLong("from"));
            if(from == null)
                from = "";//this should just a message I sent, an echo.
            long time = System.currentTimeMillis();
            mFrag.addMessage(from,message,time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onClose(int code, String reason, boolean remote) {
        mFrag.errorVisMessage(reason);
    }

    public void onError(Exception ex) {
        mFrag.errorVisMessage(ex.getLocalizedMessage());
    }

    public void pushMessage(String message){
        JSONObject jo = new JSONObject();
        try {
            jo.put("conv_id", mFrag.conversation.getID());
            jo.put("text", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(jo.toString());
    }

    public void updateUserStatus(JSONObject jo){
        if(jo.has("activeUsers")){
            try{
                JSONArray onlineUsers = jo.getJSONArray("activeUsers");
                for( int i = 0; i < onlineUsers.length(); i++){
                    ((MainActivity)mFrag.getActivity()).updateUserOnline(onlineUsers.getLong(i), true);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        if(jo.has("onlineUser")){
            try{
                long userID = jo.getLong("onlineUser");
                ((MainActivity)mFrag.getActivity()).updateUserOnline(userID, true);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        if(jo.has("offlineUser")){
            try{
                long userID = jo.getLong("offlineUser");
                ((MainActivity)mFrag.getActivity()).updateUserOnline(userID, false);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void moveConversationToUnread(final Conversation conversation){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversation.setNumOfUnseen(conversation.getNumOfUnseen() + 1);
                mainActivity.moveConversationToUnread(conversation);
            }
        });
    }

    private void createConversation(final long conversationID){
                Log.v("taggy", "New Conversation detected!");
                mainActivity.onNewConversationDetected(conversationID);
    }
}
