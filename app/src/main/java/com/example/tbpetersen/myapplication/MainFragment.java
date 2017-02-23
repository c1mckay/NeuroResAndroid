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

    RecyclerView recyclerView;
    ArrayList<Message> messageList;
    MessageAdapter messageAdapter;

    private String username = null;
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


        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList);


        RecyclerView.LayoutManager rvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(rvLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(messageAdapter);

        displayMessage();
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

    public void queueMessage(String username, String message){
        this.username = username;
        this.message = message;
    }

    public void addMessage(String username, String message){
        this.username = username;
        this.message = message;
        displayMessage();
    }

    private void displayMessage(){
        if(username != null && message != null){
            messageList.add(new Message(username, message, System.currentTimeMillis()));
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }else{
            loadMessages();
        }
    }

    public void addUser(String user){
        this.username = user;
    }

}
