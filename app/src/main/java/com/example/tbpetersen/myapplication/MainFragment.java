package com.example.tbpetersen.myapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    // View that holds all the messages
    RecyclerView recyclerView;
    // List that holds all the messages
    MessageList messageList;
    // Adapter that links the messageList and recyclerview
    MessageAdapter messageAdapter;
    public Conversation conversation;

    //Username of the owner of these messages
    String userName = null;
    // Used to temporarily store a message when the view has not yet been made
    //private String message = null;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        /* Initialize */
        messageList = new MessageList();
        messageAdapter = new MessageAdapter(messageList);


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
    public void loadMessages(Conversation c, final HashMap<Long, User> user){
        //should actually queue the messages at this point
        SessionWrapper.GetConversationData(c.getID(), getToken(), new SessionWrapper.OnCompleteListener() {
            @Override
            public void onComplete(String s) {
                try {
                    JSONArray jMessages = new JSONArray(s);
                    JSONObject jo;
                    long user_id;
                    String userName;
                    User u;
                    Date d;
                    for(int i = 0; i < jMessages.length(); i++){
                        jo = jMessages.getJSONObject(i);
                        user_id = jo.getLong("sender");
                        u = user.get(user_id);
                        if(u == null)
                            userName = "";
                        else
                            userName = u.name;
                        addMessage(userName, jo.getString("text"), System.currentTimeMillis());
                    }
                    displayMessages();
                }catch(JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String s) {

            }
        });

        //messageAdapter.notifyDataSetChanged();
    }

    private String getToken(){
        return getArguments().getString("token", null);
    }
    

    /**
     * Add a message to be displayed
     * @param username the owner of the message
     * @param message the message text
     * @param time the time at which the message was sent
     */
    public void addMessage(String username, String message, long time){
        messageList.add(username, message, time);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    /**
     * Display the messages
     */
    private void displayMessages(){
        
        //messageList.add(username, message, System.currentTimeMillis());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    
    }

    public void displayDemoMessage(){
        String dp = "dpiccioni";
        String nk = "nkaranjia";
        long t = System.currentTimeMillis();
        messageList.add(dp, "Hello",t );
        messageList.add(nk, "This is a chat example with an incredibly long message.", t);
        messageList.add(dp, "Back to me.", t);
        messageList.add(nk, "I will demonstrate overflowing with two messages.", t);
        messageList.add(nk, "This is the second message I submitted.", t);
        messageList.add(nk, "More concept.", t);
        messageList.add(dp, "Interesting. I can also play with borders to see how that looks like.", t);
        messageList.add(nk, "Thoughts?", t);

        messageList.add(dp, "Hello",t );
        messageList.add(nk, "This is a chat example with an incredibly long message.", t);
        messageList.add(dp, "Back to me.", t);
        messageList.add(nk, "I will demonstrate overflowing with two messages.", t);
        messageList.add(nk, "This is the second message I submitted.", t);
        messageList.add(nk, "More concept.", t);
        messageList.add(dp, "Interesting. I can also play with borders to see how that looks like.", t);
        messageList.add(nk, "Thoughts?", t);
        messageAdapter.notifyDataSetChanged();
    }

}
