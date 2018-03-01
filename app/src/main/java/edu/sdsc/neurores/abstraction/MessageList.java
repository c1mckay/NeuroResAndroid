package edu.sdsc.neurores.abstraction;

import java.util.ArrayList;

public class MessageList {

    private ArrayList<Message> backer;
    String lastUser, loggedIn;
    public MessageList(){
        this.backer = new ArrayList<>();
        this.loggedIn = loggedIn;
    }

    public void add(String user, String message, long time){
        Message newMessage = new Message(user, message, time);
        add(newMessage);
    }

    public void add(Message message){
        String user = message.getSender();
        if(user == null)
            throw new NullPointerException();
        if(user.equals(lastUser) && mostRecentMessageWasWithinFiveMin(message))
            backer.get(backer.size() - 1).appendText(message.getMessageText());
        else {
            lastUser = user;
            backer.add(message);
        }
    }

    public void remove(int position){
        backer.remove(position);
    }

    public void clearMessages(){
        backer.clear();
    }

    public int size(){
        return backer.size();
    }

    public Message get(int i){
        return backer.get(i);
    }

    private boolean mostRecentMessageWasWithinFiveMin(Message message){
        if(backer.size() == 0){
            return false;
        }
        Message mostRecentMessage = backer.get(backer.size() - 1);
        boolean tooOld = message.getTime() - mostRecentMessage.getTime() < (1000 * 60 * 5);
        return tooOld;

    }

}
