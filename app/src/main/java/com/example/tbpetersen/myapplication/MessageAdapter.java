package com.example.tbpetersen.myapplication;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by tbpetersen on 2/14/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{
    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList){
        this.messageList = messageList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView owner, messageText, time;

        public MyViewHolder(View view){
            super(view);

            owner = (TextView) view.findViewById(R.id.owner);
            messageText = (TextView) view.findViewById(R.id.messageText);
            time = (TextView) view.findViewById(R.id.time);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    /*
                    Message currentMessage = messageList.get(getAdapterPosition());
                    Snackbar snackbar = Snackbar.make(view, currentMessage.getOwner(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    messageList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    */
                }
            });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;

        if(messageList.size() == 1){
            layoutId = R.layout.message_list_row;
        }else{
            Message currentMessage = messageList.get(messageList.size()-1);
            Message previousMessage = messageList.get(messageList.size()-2);

            if(currentMessage.getOwner().equals(previousMessage.getOwner())){
                layoutId = R.layout.message_list_row_only_message;
            }else{
                layoutId = R.layout.message_list_row;
            }
        }

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        int numOfMessages = messageList.size();
        Message message = messageList.get(position);

        if(holder.owner != null){
            if(numOfMessages < 2){
                holder.owner.setText(message.getOwner());
                holder.messageText.setText(message.getMessageText());

                String timeString = getTimeString(message);
                holder.time.setText(timeString);
            }else{
                Message lastMessage = messageList.get(numOfMessages - 2);

                // Do not show name if you sent the last message
                if(message.getOwner().equals(lastMessage.getOwner())){
                    holder.owner.setText("");
                }else{
                    holder.owner.setText(message.getOwner());
                }
                holder.messageText.setText(message.getMessageText());
                String timeString = "";
                if(numOfMessages > 1){
                    long mostRecentMessageTime = lastMessage.getTime();
                    if( System.currentTimeMillis() - mostRecentMessageTime > (1000 * 60 * 5)){
                        timeString = getTimeString(message);
                    }
                }else{
                    timeString = getTimeString(message);
                }

                holder.time.setText(timeString);
            }
        }else{
            holder.messageText.setText(message.getMessageText());
        }





    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String getTimeString(Message message){
        Calendar today = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();

        today.setTimeInMillis(message.getTime());
        currentTime.setTimeInMillis(System.currentTimeMillis());

        Date date = new Date(message.getTime());
        DateFormat format;
        if(today.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH)){
            format = new SimpleDateFormat("hh:mm a");
            String timeString = format.format(date);
            if(timeString.charAt(0) == '0'){
                timeString = timeString.substring(1);
                return timeString;
            }
        }else if(today.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR)){
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
