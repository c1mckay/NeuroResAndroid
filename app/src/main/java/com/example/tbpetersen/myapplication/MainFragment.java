package com.example.tbpetersen.myapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    // View that holds all the messages
    RecyclerView recyclerView;
    // List that holds all the messages
    ArrayList<Message> messageList;
    // Adapter that links the messageList and recyclerview
    MessageAdapter messageAdapter;

    //Username of the owner of these messages
    private String username = null;
    // Used to temporarily store a message when the view has not yet been made
    private String message = null;

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
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList);


        RecyclerView.LayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(messageAdapter);
        // Display any messages that are waiting to be displayed
        displayMessage();
        // Scroll to the bottom of the list
        recyclerView.scrollToPosition(messageList.size() - 1);

        return v;
    }

    private void loadMessages(){
        messageList.add(new Message(username, "Hello this is a very long line of text and I dont know how the message will look\nLine 2", 1487269703028L));
        messageList.add(new Message(username, "Hello3", 1475269703028L));
        messageList.add(new Message(username, "Hello5", 1487169703028L));
        for(int i = 0; i < 8; i++){
            messageList.add(new Message(username, "First", 1487269703028L));
        }
        messageAdapter.notifyItemRangeInserted(0, messageList.size() - 1);
        //messageAdapter.notifyDataSetChanged();
    }

    /**
     * Queue up a message when the view has not yet been created to display them yet
     * @param username the owner of the message
     * @param message the message text
     */
    public void queueMessage(String username, String message){
        this.username = username;
        this.message = message;
    }

    /**
     * Add a message to be displayed
     * @param username the owner of the message
     * @param message the message text
     */
    public void addMessage(String username, String message){
        this.username = username;
        this.message = message;
        displayMessage();
    }

    /**
     * Display the messages
     */
    private void displayMessage(){
        if(username.equals("Demo")){
            displayDemoMessage();
            return;
        }
        if(username != null && message != null){
            messageList.add(new Message(username, message, System.currentTimeMillis()));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }else{
            //loadMessages();
        }
    }

    /**
     * Set the user of these messages
     * @param user the name of the user
     */
    public void addUser(String user){
        this.username = user;
    }

    public void displayDemoMessage(){
        String dp = "dpiccioni";
        String nk = "nkaranjia";
        long t = System.currentTimeMillis();
        messageList.add(new Message(dp, "Hello",t ));
        messageList.add(new Message(nk, "This is a chat example with an incredibly long message.", t));
        messageList.add(new Message(dp, "Back to me.", t));
        messageList.add(new Message(nk, "I will demonstrate overflowing with two messages.", t));
        messageList.add(new Message(nk, "This is the second message I submitted.", t));
        messageList.add(new Message(nk, "More concept.", t));
        messageList.add(new Message(dp, "Interesting. I can also play with borders to see how that looks like.", t));
        messageList.add(new Message(nk, "Thoughts?", t));

        messageList.add(new Message(dp, "Hello",t ));
        messageList.add(new Message(nk, "This is a chat example with an incredibly long message.", t));
        messageList.add(new Message(dp, "Back to me.", t));
        messageList.add(new Message(nk, "I will demonstrate overflowing with two messages.", t));
        messageList.add(new Message(nk, "This is the second message I submitted.", t));
        messageList.add(new Message(nk, "More concept.", t));
        messageList.add(new Message(dp, "Interesting. I can also play with borders to see how that looks like.", t));
        messageList.add(new Message(nk, "Thoughts?", t));
        messageAdapter.notifyDataSetChanged();
    }

}
