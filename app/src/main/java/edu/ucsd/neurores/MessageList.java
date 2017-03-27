package edu.ucsd.neurores;

import java.util.ArrayList;

public class MessageList {

    private ArrayList<Message> backer;
    String lastUser, loggedIn;
    public MessageList(){
        this.backer = new ArrayList<>();
        this.loggedIn = loggedIn;
    }

    public void add(String user, String message, long id){
        if(user == null)
            throw new NullPointerException();
        if(user.equals(lastUser))
            backer.get(backer.size() - 1).appendText(message);
        else {
            lastUser = user;
            backer.add(new Message(user, message, id));
        }
    }

    public int size(){
        return backer.size();
    }

    public Message get(int i){
        return backer.get(i);
    }

}
