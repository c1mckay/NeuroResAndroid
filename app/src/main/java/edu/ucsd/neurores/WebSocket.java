package edu.ucsd.neurores;

import android.app.ActivityManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class WebSocket extends WebSocketClient {

    public static final String WEBSOCKET_URI_STRING = "wss://neurores.ucsd.edu:3001";

    Fragment currentFragment;
    MainActivity mainActivity;


    WebSocket(Fragment currentFragment, MainActivity mainActivity, RequestWrapper.OnCompleteListener ocl) {
        super(getWebsocketURI());
        this.currentFragment = currentFragment;
        this.mainActivity = mainActivity;

        setupSSLSocket(mainActivity, ocl);
    }

    private static URI getWebsocketURI(){
        try{
            return new URI(WEBSOCKET_URI_STRING);
        }catch (URISyntaxException e){
            throw new RuntimeException("URISyntaxException: " + WEBSOCKET_URI_STRING);
        }
    }

    private void setupSSLSocket(final MainActivity mainActivity, final RequestWrapper.OnCompleteListener ocl) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SocketFactory socketFactory = SSLSocketFactory.getDefault();
                    Socket socketSSL = socketFactory.createSocket(RequestWrapper.BASE_URL, 3001);

                    setSocket(socketSSL);

                    if (!connectBlocking()) {
                        if (!isOpen()) {
                            Log.v("sockett", "Failed to connect socket");
                            throw new SocketException("Error connecting to the web socket");
                        } else {
                            mainActivity.hideWarningBanner();
                            if (ocl != null) {
                                ocl.onComplete("Connected WEIRD");
                            }
                        }
                    } else {
                        mainActivity.hideWarningBanner();
                        Log.v("sockett", "Connected");
                        if (ocl != null) {
                            ocl.onComplete("Connected");
                        }
                    }

                } catch (SocketException e) {
                    Log.v("sockett", "Socket Exception");
                    Log.v("sockett", e.getMessage() + "!!");
                    e.printStackTrace();
                    if (ocl != null) {
                        ocl.onError("There was a problem setting up the websocket");
                    }
                } catch (IOException e) {
                    Log.v("sockett", "There was a problem while setting up the socket");
                    Log.v("sockett", e.getMessage() + "!!");
                    e.printStackTrace();
                    if (ocl != null) {
                        ocl.onError("There was a problem setting up the websocket");
                    }
                } catch (InterruptedException e) {
                    Log.v("sockett", "There was a problem while connecting the socket");
                    Log.v("sockett", e.getMessage() + "!!");
                    e.printStackTrace();
                    if (ocl != null) {
                        ocl.onError("There was a problem setting up the websocket");
                    }
                }
            }
        };

        Thread connectSSLSocketThread = new Thread(runnable);
        connectSSLSocketThread.start();

    }


    public void onOpen(ServerHandshake handshakedata) {
        JSONObject jo = new JSONObject();
        try {
            if (currentFragment instanceof MainFragment) {
                MainFragment mainFragment = (MainFragment) currentFragment;
                jo.put("greeting", mainFragment.getToken());
            }
            //TODO Check if socket connection is actually accepted (is not accepted when token is bad)
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(jo.toString());
    }

    public void onMessage(String message) {
        JSONObject jo = null;
        Log.v("sockett", message);

        try {
            jo = new JSONObject(message);
            if (isUserStatusUpdate(jo)) {
                updateUserStatus(jo);
                return;
            } else if (isWipeUpdate(jo)) {
                long conversationID = jo.getLong("convID");
                mainActivity.wipeConversation(conversationID, false);
                return;
            } else {

                if (!mainActivity.screenIsOn) {
                    mainActivity.queueToast("New Message");
                }

                long conversationID = jo.getLong("conv_id");
                long fromID = jo.getLong("from");
                String messageText = jo.getString("text");
                int messageID = Integer.parseInt(jo.getString("mID"));
                long time = System.currentTimeMillis();
                String timeString = Message.getTimeStringFormattedForDB(time);

                mainActivity.messageDatabaseHelper.insertMessage(messageID, messageText, conversationID, fromID, timeString);
                if (userIsNotViewingThisConversation(conversationID)) {
                    //long conversationID = jo.getLong("conv_id");
                    HashMap<Long, Conversation> currentConversations = mainActivity.currentConversations;
                    boolean conversationExists = currentConversations.containsKey(conversationID);

                    if (conversationExists) {
                        moveConversationToUnread(currentConversations.get(conversationID));
                    } else {
                        createConversation(conversationID);
                    }

                    notifyUserOfNewMessage(fromID);
                } else {
                    MainFragment mainFragment = (MainFragment) currentFragment;
                    String from = mainFragment.conversation.getUser(fromID);
                    if (from == null)
                        from = "";//this should just a message I sent, an echo.
                    boolean isAtBottom = mainFragment.isAtBottom();
                    displayMessage(from, messageText, time, isAtBottom, fromID);
                    markAsSeen(mainFragment.conversation.getID());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(String from, String message, long time, boolean isAtBottom, long userID) {
        if (!(currentFragment instanceof MainFragment)) {
            Log.v("taggy", "Trying to display message while current fragment is not a MainFragment");
            return;
        }

        MainFragment mainFragment = (MainFragment) currentFragment;
        if (isAtBottom) {
            if (mainFragment.isLoading()) {
                mainFragment.addTempMessage(from, message, time);
            } else {
                mainFragment.addMessage(from, message, time, true);
            }
        } else {
            if (mainFragment.isLoading()) {
                mainFragment.addTempMessage(from, message, time);
            } else {
                mainFragment.addMessage(from, message, time, false);
            }
            if (userID != mainActivity.loggedInUser.getID()) {
                notifyUserOfNewMessage(userID);
            }
        }
    }

    private boolean isUserStatusUpdate(JSONObject jo) {
        try {
            return jo.has("userStatusUpdate") && jo.getBoolean("userStatusUpdate");
        } catch (JSONException e) {
            Log.v("sockett", "Failed reading json in isUserStatusUpdate()");
            return false;
        }
    }

    private boolean isWipeUpdate(JSONObject jo) {
        try {
            return jo.has("wipeThread") && jo.getBoolean("wipeThread");
        } catch (JSONException e) {
            Log.v("error", "Error trying to read value of wipeThread in socket message");
            return false;
        }
    }

    private boolean userIsNotViewingThisConversation(long conversationID) {
        if (!(currentFragment instanceof MainFragment)) {
            return true;
        } else {
            MainFragment mainFragment = (MainFragment) currentFragment;
            return mainFragment.conversation == null || mainFragment.conversation.getID() != conversationID;
        }
    }

    private void markAsSeen(long conversationID) {
        if (!(currentFragment instanceof MainFragment)) {
            Log.v("taggy", "Trying to mark message as seen while current fragment is not a MainFragment");
            return;
        }
        MainFragment mainFragment = (MainFragment) currentFragment;
        RequestWrapper.markConversationSeen(mainActivity, conversationID, mainFragment.getToken(), new RequestWrapper.OnHTTPRequestCompleteListener() {
            @Override
            public void onComplete(String s) {
                // TODO Check response for success
            }

            @Override
            public void onError(int i) {
                Log.v("taggy", "Error marking message as seen in web socket");
            }
        });
    }

    public void onClose(int code, String reason, boolean remote) {
        Log.v("sockett", "onClose()");
        mainActivity.onSocketDisconnected();
        if (currentFragment instanceof MainFragment) {
            MainFragment mainFragment = (MainFragment) currentFragment;
            mainFragment.errorVisMessage(reason);
        }
    }

    public void onError(Exception ex) {
        Log.v("sockett", ex.getMessage());

        if (currentFragment != null && currentFragment instanceof MainFragment) {
            MainFragment mainFragment = (MainFragment) currentFragment;
            mainFragment.errorVisMessage(ex.getLocalizedMessage());
        }


    }

    public void pushMessage(String message) {
        if (!(currentFragment instanceof MainFragment)) {
            Log.v("sockett", "Trying to send message while current fragment is not a MainFragment");
            return;
        }

        MainFragment mainFragment = (MainFragment) currentFragment;
        JSONObject jo = new JSONObject();
        try {
            jo.put("conv_id", mainFragment.conversation.getID());
            jo.put("text", message);
            Log.v("sockett", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send(jo.toString());
        Log.v("sockett", "Message sent");
    }

    public void updateUserStatus(JSONObject jo) {
        if (jo.has("activeUsers")) {
            try {
                JSONArray onlineUsers = jo.getJSONArray("activeUsers");
                setOnlineStatusOfUsers(onlineUsers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jo.has("onlineUser")) {
            try {
                long userID = jo.getLong("onlineUser");
                mainActivity.updateUserOnline(userID, true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jo.has("offlineUser")) {
            try {
                long userID = jo.getLong("offlineUser");
                mainActivity.updateUserOnline(userID, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setOnlineStatusOfUsers(JSONArray onlineUsers) {
        try {
            for (User user : mainActivity.userList.values()) {
                boolean isOnline = false;
                for (int i = 0; i < onlineUsers.length(); i++) {
                    if ((long) user.getID() == (long) onlineUsers.getLong(i)) {
                        isOnline = true;
                    }
                }
                mainActivity.updateUserOnline(user.getID(), isOnline);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void moveConversationToUnread(final Conversation conversation) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                conversation.setNumOfUnread(conversation.getNumOfUnread() + 1);
                mainActivity.moveConversationToUnread(conversation);
            }
        });
    }

    private void createConversation(final long conversationID) {
        Log.v("taggy", "New Conversation detected!");
        mainActivity.onNewConversationDetected(conversationID);
    }

    private void notifyUserOfNewMessage(final long userID) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mainActivity.userList.containsKey(userID)) {
                    User u = mainActivity.userList.get(userID);
                    Toast.makeText(mainActivity, "New message from " + u.getName(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mainActivity, "New message received",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateFrag(Fragment fragment) {
        currentFragment = fragment;
    }
}
