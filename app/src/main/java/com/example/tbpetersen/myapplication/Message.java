package com.example.tbpetersen.myapplication;

/**
 * Created by tbpetersen on 2/14/2017.
 */

public class Message {
    private String messageText;
    private String owner;
    private long time;

    public Message(String owner, String messageText, long time){
        this.owner = owner;
        this.messageText = messageText;
        this.time = time;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
