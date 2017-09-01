package edu.ucsd.neurores;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.net.ssl.SSLSocket;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment{

    // View that holds all the messages
    RecyclerView recyclerView;
    // List that holds all the messages
    public MessageList messageList;
    public MessageList temp;
    // Adapter that links the messageList and recyclerview
    MessageAdapter messageAdapter;
    public Conversation conversation;

    //Username of the owner of these messages
    String userName = null;
    // Used to temporarily store a message when the view has not yet been made
    //private String message = null;
    public SimpleDateFormat formatter;

    SwipeRefreshLayout swipeRefreshLayout;
    Button messageSendButton;
    EditText messageEditText;

    WebSocket socket;
    Toast mostRecentToast;
    private volatile boolean isLoading;
    MainActivity mainActivity;

    public MainFragment() {
        // Required empty public constructor
        temp = new MessageList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mainActivity = (MainActivity) getActivity();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Load more messages on pull down
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        /*
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideSoftKeyboard();
            }
        });
        */

        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, final int oldLeft, final int oldTop, int oldRight, final int oldBottom) {
                if ( bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });
        /* Initialize */
        isLoading = true;
        mostRecentToast = null;
        messageList = new MessageList();
        messageAdapter = new MessageAdapter(mainActivity, messageList);

        messageSendButton = (Button) v.findViewById(R.id.message_send_button);
        messageEditText = (EditText) v.findViewById(R.id.message_edit_text);

        messageSendButton.setEnabled(false);

        //Add onboarding message
        if(! hasConversation()){
            addMessage("", mainActivity.getResources().getString(R.string.onboardingMessage), System.currentTimeMillis(),true);
        }

        messageEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().trim().length()==0){
                    messageSendButton.setEnabled(false);
                } else {
                    messageSendButton.setEnabled(true);
                }
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });


        RecyclerView.LayoutManager rvLayoutManager = new LinearLayoutManager(mainActivity);
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(messageAdapter);
        // Display any messages that are waiting to be displayed
        //displayMessage();//messages aren't loaded yet
        // Scroll to the bottom of the list
        recyclerView.scrollToPosition(messageList.size() - 1);

        setupSendMessageButton(v);

        return v;
    }
    
    /**
     * The conversation details to load
     * @param conversation the conversation to query the server for
     */
    public void loadMessages(final Context context, final Conversation conversation, final HashMap<Long, User> users){
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getDefault());
        //should actually queue the messages at this point
        if(conversation == null){
            Log.v("taggy", "Conversation is null");
            return;
        }
        SessionWrapper.GetConversationData(context, conversation.getID(), getToken(), new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                updateMessageView(context, s, users);
                mainActivity.dismissNotifications(conversation.getID());
            }

            @Override
            public void onError(String s) {

            }
        });

        //messageAdapter.notifyDataSetChanged();

    }

    public void updateMessageView(Context context, String s, HashMap<Long, User> users){
        Log.v("tag",s);
        try{
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
                u = users.get(user_id);
                if(u == null)
                    userName = "";
                else
                    userName = u.getName();
                //Log.viewInNavDrawer("taggy", userName + ": " +  jo.getString("text"));
                addMessage(userName, jo.getString("text"), d.getTime(),true);
            }
            while(temp.size() > 0){
                addMessage(temp.get(0), true);
                temp.remove(0);
            }
            isLoading = false;
            displayMessages(true);
        }catch(JSONException e){
            Log.v("taggy", "There was a json error");
            e.printStackTrace();
        }catch(ParseException e){
            Log.v("taggy", "There was a parse error");
            e.printStackTrace();
        }
        if(conversation.getNumOfUnseen() > 0){
            markConversationRead(context, conversation);
        }

    }

    public void markConversationRead(Context context, final Conversation conversation){
        SessionWrapper.markConversationSeen(context, conversation.getID(), getToken(), new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                mainActivity.moveConversationToPrivate(conversation);
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
    public void addMessage(String username, String message, long time, boolean scrollToBottom){
        messageList.add(username, message, time);
        displayMessages(scrollToBottom);
    }

    public void addMessage(Message message, boolean scrollToBottom){
        messageList.add(message);
        displayMessages(scrollToBottom);
    }

    public void addTempMessage(String username, String message, long time){
        temp.add(username, message, time);
    }

    /**
     * Display the messages
     */
    private void displayMessages(final boolean scrollToBottom){
        //messageList.add(username, message, System.currentTimeMillis());
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
                //messageAdapter.notifyItemInserted(messageList.size() - 1);
                if(scrollToBottom){
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(messageList.size() - 1);
                            mainActivity.showMainElements();
                        }
                    },100);
                }
            }
        });
    
    }

    public void onResume(){
        super.onResume();
    }

    private void setupSSL(final Context context, final WebSocket sock){

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
                    showToast(getContext().getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                    Log.v("taggy", "There was a problem setting up ssl websocket");
                }
            }
        };

        Thread thread = new Thread(r);
        thread.start();

    }

    private void setupSSLAndSendMessage(final Context context, final WebSocket sock, final String message){

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
                        sock.pushMessage(message);
                    }

                }catch (Exception e){
                    ((MainActivity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(context.getResources().getString(R.string.no_connection), Toast.LENGTH_LONG);
                        }
                    });
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
        if(socket != null){
            socket.close();
            socket = null;
        }
        super.onPause();
    }

    public void scrollToBottom(){
        if(recyclerView != null){
            recyclerView.scrollToPosition(messageList.size() - 1);
        }
    }

    public boolean isAtBottom(){
        return ! recyclerView.canScrollVertically(1);
    }

    private void scrollToShowNewMessage(){
        recyclerView.scrollToPosition(messageAdapter.getItemCount() -1);
    }

    public void showToast(String message, int length){
        if (mostRecentToast != null && mostRecentToast.getView().isShown()){
            mostRecentToast.cancel();
        }
        mostRecentToast = Toast.makeText(getContext(), message, length);
        mostRecentToast.show();
    }

    public boolean isLoading(){
        return isLoading;
    }

    public boolean canScroll(){
        return recyclerView.canScrollVertically(1) || recyclerView.canScrollVertically(-1);
    }

    private void setupSendMessageButton(final View parent){
        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            sendMessage();
            }
        });

        messageEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
    }

    private void sendMessage(){
        // The text in the input field
        String newMessage = messageEditText.getText().toString();
        //Only send the message if it is not empty
        if(! newMessage.equals("") && mainActivity.selectedConversation != null){
            mainActivity.pushMessage(newMessage);
            messageEditText.setText("");
            scrollToBottom();
        }
    }

}
