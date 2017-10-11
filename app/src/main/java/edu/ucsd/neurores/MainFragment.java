package edu.ucsd.neurores;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


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
    ImageButton messageSendButton;
    EditText messageEditText;
    TextView errorTextView;

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
        errorTextView = (TextView) v.findViewById(R.id.error_message);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Load more messages on pull down
                swipeRefreshLayout.setRefreshing(false);
            }
        });

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

        messageSendButton = (ImageButton) v.findViewById(R.id.message_send_button);
        messageEditText = (EditText) v.findViewById(R.id.message_edit_text);

        messageSendButton.setEnabled(false);

        //Add onboarding message
        if(! hasConversation()){
            addMessage("", mainActivity.getResources().getString(R.string.onboardingMessage), System.currentTimeMillis(),true);
        }

        messageEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(messageEditText.getText().length() == 500){
                    mainActivity.showToast("Maximum message length is 500 characters", Toast.LENGTH_LONG);
                }
                if(s.toString().trim().length() == 0){
                    messageSendButton.setEnabled(false);
                } else {
                    messageSendButton.setEnabled(true);
                }
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
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
        centerToolbarTitle();

        return v;
    }

    private void centerToolbarTitle() {
        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.toolbar_title);
        Toolbar.LayoutParams params =  (Toolbar.LayoutParams)toolbarTitle.getLayoutParams();
        params.gravity = Gravity.CENTER;
        toolbarTitle.setLayoutParams(params);
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
        RequestWrapper.GetConversationData(context, conversation.getID(), getToken(), new RequestWrapper.OnHTTPRequestCompleteListener() {
            @Override
            public void onComplete(String s) {
                List<Message> messages = JSONConverter.toMessageList(s, users);
                updateMessageView(context, messages);
                errorTextView.setVisibility(View.GONE);
                mainActivity.dismissNotifications(conversation.getID());

                mainActivity.messageDatabaseHelper.makeDatabaseMatchMessageList(conversation.getID(),messages );
            }

            @Override
            public void onError(int i) {
                Log.v("taggy", "Error loading messages");
                if(i == 401){
                    mainActivity.showToast(getString(R.string.cred_expired), Toast.LENGTH_LONG);
                    mainActivity.logout(null);
                    return;
                }

                MessageDatabaseHelper messageDatabaseHelper = new MessageDatabaseHelper(mainActivity);
                String databaseJSON = messageDatabaseHelper.getMessagesJSON(conversation.getID());
                if( databaseJSON!= null){
                    List<Message> messages = JSONConverter.toMessageList(databaseJSON, users);
                    updateMessageView(context, messages);
                    errorTextView.setVisibility(View.GONE);
                    mainActivity.dismissNotifications(conversation.getID());
                    return;
                }

                mainActivity.showMainElements();
                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void updateMessageView(Context context, List<Message> messages){
        for(int i = 0; i < messages.size(); i ++){
            Message message = messages.get(i);
            addMessage(message,true);
        }

        addMessagesFromTemp();
        isLoading = false;
        displayMessages(true);
        if(conversation.hasUnreadMessages()){
            markConversationRead(context, conversation);
        }
    }

    private void addMessagesFromTemp() {
        while(temp.size() > 0){
            addMessage(temp.get(0), true);
            temp.remove(0);
        }
    }

    public void markConversationRead(Context context, final Conversation conversation){
        RequestWrapper.markConversationSeen(context, conversation.getID(), getToken(), new RequestWrapper.OnHTTPRequestCompleteListener() {
            @Override
            public void onComplete(String s) {
                mainActivity.moveConversationToPrivate(conversation);
                mainActivity.messageDatabaseHelper.insertConversation(conversation);
            }

            @Override
            public void onError(int i) {

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

    public void wipeConversation(boolean alertServer){
        if(alertServer){
            RequestWrapper.WipeConversation(mainActivity, conversation.getID(), getToken(), new RequestWrapper.OnHTTPRequestCompleteListener() {
                @Override
                public void onComplete(String s) {
                    clearMessages();
                    Log.v("taggy", s);
                }

                @Override
                public void onError(int i) {

                }
            });
        }else{
         clearMessages();
        }
    }

    public void clearMessages(){
        messageList.clearMessages();
        displayMessages(false);
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

    public boolean isLoading(){
        return isLoading;
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
            scrollToBottom();
        }
    }

    public void clearMessage(){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageEditText.setText("");
            }
        });
    }

}
