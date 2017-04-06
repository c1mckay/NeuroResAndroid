package edu.ucsd.neurores;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocket extends WebSocketClient {

    MainFragment mFrag;
    WebSocket(MainFragment mFrag) throws URISyntaxException {
        super(new URI("ws://neurores.ucsd.edu:3000"));
        this.mFrag = mFrag;
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
        Log.d("yup", message);
        JSONObject jo = null;
        try {
            jo = new JSONObject(message);
            if(jo.getLong("conv_id") != mFrag.conversation.getID())
                return;
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
}
