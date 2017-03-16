package com.example.tbpetersen.myapplication;

/**
 * Created by tbpetersen on 2/14/2017.
 */

/**
 * Represents a message from a user. Contains a user's name, message text
 * and the time that the message was sent.
 */
public class Message {
    private String owner;
    private String messageText;
    private long time;

    /**
     * Public constructor
     * @param owner the owner of the message
     * @param messageText the message text
     * @param time the time the message was sent
     */
    public Message(String owner, String messageText, long time){
        this.owner = owner;
        this.messageText = messageText;
        this.time = time;
    }

    /*****  Getters and setters for variables *****/
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

    public void appendText(String text){
        messageText += ("\n" + text);
    }
}
