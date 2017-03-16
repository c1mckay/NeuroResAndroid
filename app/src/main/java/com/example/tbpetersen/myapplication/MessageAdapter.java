package com.example.tbpetersen.myapplication;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by tbpetersen on 2/14/2017.
 */

/**
 * Message adapter for the recycler view
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{
    // List to hold messages
    private MessageList messageList;
    /* The minimum amount of minutes that must pass until timestamps are shown again */
    private static int MIN_MINUTES = 5;
    Random r;

    /**
     * Single argument ctor
     * @param messageList the list that the messages will be stored in
     */
    public MessageAdapter(MessageList messageList){
        this.messageList = messageList;
        r = new Random();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView owner, messageText, time;
        public LinearLayout userAndTimeLayout, singleMessageContainer;
        public View leftView, rightView;

        public MyViewHolder(View view){
            super(view);

            owner = (TextView) view.findViewById(R.id.owner);
            messageText = (TextView) view.findViewById(R.id.messageText);
            leftView = view.findViewById(R.id.message_left_space);
            rightView = view.findViewById(R.id.message_right_space);
            time = (TextView) view.findViewById(R.id.time);
            userAndTimeLayout = (LinearLayout) view.findViewById(R.id.owner_and_time_layout);
            singleMessageContainer = (LinearLayout) view.findViewById(R.id.message_container);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                // Do stuff when the message is clicked on
                }
            });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        int numOfMessages = messageList.size();
        Message message = messageList.get(position);

        // Check if message_list_row is the layout that was used
        if(holder.owner != null){
            // Always have the time shown on the first message
            if(position == 0){
                holder.owner.setText(message.getOwner());
                holder.messageText.setText(message.getMessageText());

                String timeString = getTimeString(message);
                holder.time.setText(timeString);
            // Decide what information will be displayed
            }else{
                Message lastMessage = messageList.get(position - 1);

                // Do not show name or time if you sent the last message
                if(message.getOwner().equals(lastMessage.getOwner())){
                    holder.owner.setText("");
                    holder.userAndTimeLayout.setVisibility(View.GONE);
                }else{
                    holder.owner.setText(message.getOwner());
                }
                holder.messageText.setText(message.getMessageText());
                String timeString = "";
                // Only show the time the last message was sent longer than MIN_MINUTES ago
                if(numOfMessages > 1){
                    long mostRecentMessageTime = lastMessage.getTime();
                    if( System.currentTimeMillis() - mostRecentMessageTime > (1000 * 60 * MIN_MINUTES)){
                        timeString = getTimeString(message);
                    }
                }else{
                    timeString = getTimeString(message);
                }

                holder.time.setText(timeString);
            }
         // message_list_row_only_message is the layout
        }else{
            holder.messageText.setText(message.getMessageText());
        }

        boolean currentUserSaidThis = r.nextBoolean();
        holder.singleMessageContainer.setGravity(currentUserSaidThis ? Gravity.LEFT : Gravity.RIGHT);
        holder.messageText.setBackgroundResource(currentUserSaidThis ? R.drawable.balloon_incoming_normal : R.drawable.balloon_outgoing_normal);

        int paddingTop = holder.messageText.getPaddingTop();
        int paddingBottom = holder.messageText.getPaddingBottom();
        int paddingRight = holder.messageText.getPaddingRight();
        int paddingLeft = holder.messageText.getPaddingLeft();

        int switcher;
        if(!currentUserSaidThis) {
            holder.userAndTimeLayout.setVisibility(View.GONE);
            holder.rightView.setVisibility(View.GONE);
        }else {
            switcher = paddingRight;
            paddingRight = paddingLeft;
            paddingLeft = switcher;
            holder.leftView.setVisibility(View.GONE);
        }
        holder.messageText.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * Get the formatted time for this message. Three formats:
     * Message was sent same day: Hour of day(2:23 pm)
     * Message was sent same month: Month, day and time(Feb 2, 2:23 pm)
     * Message was sent same different month: Month, day and year(Feb 2, 2017)
     *
     * @param message the message to format the time for
     * @return the formatted time string
     */
    private String getTimeString(Message message){
        Calendar messageTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();

        messageTime.setTimeInMillis(message.getTime());
        currentTime.setTimeInMillis(System.currentTimeMillis());

        Date date = new Date(message.getTime());
        DateFormat format;
        boolean sameDay = messageTime.get(Calendar.DAY_OF_YEAR) == currentTime.get(Calendar.DAY_OF_YEAR) &&
                          messageTime.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR);

        boolean sameMonth = messageTime.get(Calendar.MONTH) == currentTime.get(Calendar.MONTH) &&
                messageTime.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR);

        if(sameDay){
            format = new SimpleDateFormat("hh:mm a");
            String timeString = format.format(date);
            if(timeString.charAt(0) == '0'){
                timeString = timeString.substring(1);
                return timeString;
            }
        }else if(sameMonth){
            format = new SimpleDateFormat("MMM dd, hh:mm a");
            String timeString = format.format(date);
            if(timeString.charAt(8) == '0'){
                timeString = timeString.substring(0,8) + timeString.substring(9);
                return timeString;
            }
        }else{
            format = new SimpleDateFormat("MMM dd, yyyy");
        }
        return format.format(date);
    }

}
