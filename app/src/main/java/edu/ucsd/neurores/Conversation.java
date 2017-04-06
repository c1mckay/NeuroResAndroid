package edu.ucsd.neurores;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tbpetersen on 3/23/2017.
 */

public class Conversation extends NavDrawerItem{
    List<User> users;

    Conversation(long id, Context c){
        super(id, c);
        users = new ArrayList<User>();
    }

    void addUser(User user) {
        users.add(user);
    }

    public String getName() {
        if(users == null)
            return "Bugged out conversation";
        if(users.size() == 1)
            return users.get(0).name;
        StringBuilder sb = new StringBuilder();
        for(User u: users){
            sb.append(u.name);
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private ArrayList<Long> tempIDs;
    void addTemporaryUser(long userID) {
        if(tempIDs == null)
            tempIDs = new ArrayList<>();
        tempIDs.add(userID);
    }

    public void setContext(MainActivity activity) {
        super.setContext(activity);
        User u;
        for(Long userID: tempIDs) {
            u = activity.userList.get(userID);
            if(u != null)
                addUser(u);
        }
    }

    public String getUser(long from) {
        for(User u: users){
            if(u.getID().equals(from))
                return u.name;
        }
        return null;
    }

    public int getSize() {
        return users.size();
    }

    public void logAllNames(){
        for(User u : users){
            Log.v("tag", "Name: " + u.name);
        }
    }
}
