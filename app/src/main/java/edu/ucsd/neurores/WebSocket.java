package edu.ucsd.neurores;

import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

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

            if(! mainActivity.screenIsOn){
                mainActivity.queueToast("New Message");
            }
            if(jo.getLong("conv_id") != mFrag.conversation.getID()){
                long conversationID = jo.getLong("conv_id");
                HashMap<Long,Conversation> currentConversations = mainActivity.currentConversations;

                if(currentConversations.containsKey(conversationID)){
                    moveConversationToUnread(currentConversations.get(conversationID));
                }else{
                    createConversation(conversationID);
                }
                long userID = jo.getLong("from");
                notifyUserOfNewMessage(userID);
                return;
            }
            message = jo.getString("text");
            String from = mFrag.conversation.getUser(jo.getLong("from"));
            if(from == null)
                from = "";//this should just a message I sent, an echo.
            long time = System.currentTimeMillis();
            boolean isAtBottom = mFrag.isAtBottom();
            if(isAtBottom){
                if(mFrag.isLoading()){
                    mFrag.addTempMessage(from,message,time);
                }else{
                    mFrag.addMessage(from,message,time, true);
                }
            }else{
                if(mFrag.isLoading()){
                    mFrag.addTempMessage(from,message,time);
                }else{
                    mFrag.addMessage(from,message,time, false);
                }
                long userID = jo.getLong("from");
                if(userID != mainActivity.loggedInUser.getID()){
                    notifyUserOfNewMessage(userID);
                }
            }
            markAsSeen(mFrag.conversation.getID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void markAsSeen(long conversationID) {
        SessionWrapper.markConversationSeen(mainActivity, conversationID, mFrag.getToken(), new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                // TODO Check response for success
            }

            @Override
            public void onError(String s) {

            }
        });
    }


    public void onClose(int code, String reason, boolean remote) {
        Log.v("sockett", "onClose()");
        mFrag.errorVisMessage(reason);
    }

    public void onError(Exception ex) {
        if(mFrag != null){
            mFrag.errorVisMessage(ex.getLocalizedMessage());
        }
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
                setOnlineStatusOfUsers(onlineUsers);
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

    private void setOnlineStatusOfUsers(JSONArray onlineUsers){
        try{
            for(User user : mainActivity.userList.values()){
                boolean isOnline = false;
                for( int i = 0; i < onlineUsers.length(); i++){
                    if((long)user.getID() == (long)onlineUsers.getLong(i)){
                        isOnline = true;
                    }
                }
                ((MainActivity)mFrag.getActivity()).updateUserOnline(user.getID(), isOnline);
            }
        }catch(JSONException e){
            e.printStackTrace();
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

    private void notifyUserOfNewMessage(final long userID){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mainActivity.userList.containsKey(userID)){
                    User u = mainActivity.userList.get(userID);
                    Toast.makeText(mainActivity, "New message from " + u.getName(),
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mainActivity, "New message received",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateFrag(MainFragment fragment){
        mFrag = fragment;
    }
}
