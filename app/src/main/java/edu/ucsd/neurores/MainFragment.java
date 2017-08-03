package edu.ucsd.neurores;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    // View that holds all the messages
    RecyclerView recyclerView;
    // List that holds all the messages
    public MessageList messageList;
    // Adapter that links the messageList and recyclerview
    MessageAdapter messageAdapter;
    public Conversation conversation;

    //Username of the owner of these messages
    String userName = null;
    // Used to temporarily store a message when the view has not yet been made
    //private String message = null;
    SimpleDateFormat formatter;

    WebSocket socket;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideSoftKeyboard();
            }
        });

        /* Initialize */
        messageList = new MessageList();
        messageAdapter = new MessageAdapter(messageList);

        //Add onboarding message
        if(! hasConversation()){
            addMessage("", getActivity().getResources().getString(R.string.onboardingMessage), System.currentTimeMillis());
        }


        RecyclerView.LayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(messageAdapter);
        // Display any messages that are waiting to be displayed
        //displayMessage();//messages aren't loaded yet
        // Scroll to the bottom of the list
        recyclerView.scrollToPosition(messageList.size() - 1);

        return v;
    }
    
    /**
     * The conversation details to load
     * @param c the conversation to query the server for
     */
    public void loadMessages(final Context context, final Conversation c, final HashMap<Long, User> user){
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getDefault());
        //should actually queue the messages at this point
        if(c == null){
            Log.v("taggy", "Conversation is null");
            return;
        }
        SessionWrapper.GetConversationData(context, c.getID(), getToken(), new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                try {
                    Log.v("tag",s);
                    JSONArray jMessages = new JSONArray(s);
                    JSONObject jo;
                    long user_id;
                    String userName;
                    User u;
                    Date d;
                    for(int i = 0; i < jMessages.length(); i++){
                        jo = jMessages.getJSONObject(i);
                        user_id = jo.getLong("sender");
                        d = formatter.parse(jo.getString("date"));
                        // TODO Work with timeszones
                        d = new Date(d.getTime() - (1000 * 60 * 60 * 7));
                        u = user.get(user_id);
                        if(u == null)
                            userName = "";
                        else
                            userName = u.name;
                        //Log.v("taggy", userName + ": " +  jo.getString("text"));
                        addMessage(userName, jo.getString("text"), d.getTime());
                    }
                    displayMessages();
                }catch(JSONException e){
                    Log.v("taggy", "There was a json error");
                    e.printStackTrace();
                }catch(ParseException e){
                    Log.v("taggy", "There was a parse error");
                    e.printStackTrace();
                }
                ((MainActivity)getActivity()).showMainElements();
                if(conversation.getNumOfUnseen() > 0){
                    markConversationRead(context, conversation);
                }
            }

            @Override
            public void onError(String s) {

            }
        });

        //messageAdapter.notifyDataSetChanged();

    }

    public void markConversationRead(Context context, final Conversation conversation){
        SessionWrapper.markConversationSeen(context, conversation.getID(), getToken(), new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                ((MainActivity)getActivity()).moveConversationToPrivate(conversation);
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    String getToken(){
        return getArguments().getString("token", null);
    }

    boolean hasConversation(){
        return getArguments().getBoolean("hasConversation", true);
    }



    /**
     * Add a message to be displayed
     * @param username the owner of the message
     * @param message the message text
     * @param time the time at which the message was sent
     */
    public void addMessage(String username, String message, long time){
        messageList.add(username, message, time);
        displayMessages();
    }

    /**
     * Display the messages
     */
    private void displayMessages(){
        //messageList.add(username, message, System.currentTimeMillis());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
                //messageAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    
    }

    public void onResume(){
        try {
            socket = new WebSocket(this, (MainActivity) getActivity());
            setupSSL(getActivity(), socket);
        } catch (URISyntaxException e) {
            errorVisMessage("Failed to connect to server");
            e.printStackTrace();
        }
        super.onResume();
    }

    private void setupSSL(final Context context, final WebSocketClient sock){

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try{
                    NeuroSSLSocketFactory neuroSSLSocketFactory = new NeuroSSLSocketFactory(context);
                    org.apache.http.conn.ssl.SSLSocketFactory sslSocketFactory = neuroSSLSocketFactory.createAdditionalCertsSSLSocketFactory();
                    Socket sock1 = new Socket(SessionWrapper.BASE_URL, 443);
                    SSLSocket socketSSL = (SSLSocket) sslSocketFactory.createSocket(sock1, SessionWrapper.BASE_URL, 443, false);


                    sock.setSocket(socketSSL);
                    if(! sock.connectBlocking()){
                        Log.v("sockett", "Failed to connect socket");
                        throw new Exception("Error connecting to the web socket");
                    }else{
                        Log.v("sockett", "Connected");
                    }

                }catch (Exception e){
                    Log.v("taggy", "There was a problem setting up ssl websocket");
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();

    }

    public void errorVisMessage(String s){
        Log.d("visError", s);
    }

    public void onPause(){
        socket.close();
        socket = null;
        super.onPause();
    }

    public void hideSoftKeyboard() {
        if(getActivity() != null && getActivity().getCurrentFocus()!= null) {
            InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }



}
